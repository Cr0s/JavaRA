/*
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package redhorizon.filetypes.wsa;

import redhorizon.filetypes.AnimationFile;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.FileType;
import redhorizon.filetypes.PalettedInternal;
import redhorizon.filetypes.StreamingDataDecoder;
import redhorizon.filetypes.WritableFile;
import redhorizon.filetypes.png.PngFile;
import redhorizon.media.Palette;
import redhorizon.utilities.BufferUtility;
import redhorizon.utilities.CodecUtility;
import redhorizon.utilities.ImageUtility;
import redhorizon.utilities.channels.DuplicateReadOnlyByteChannel;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Implementation of a C&C WSA file, which is the format of short animations in
 * Red Alert and Tiberium Dawn.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions("wsa")
@FileType(AnimationFile.class)
public class WsaFileCNC extends WsaFile<WsaFileHeaderCNC> implements PalettedInternal, WritableFile {

	private static final int PALETTE_SIZE = 768;

	// Save-to information
	private static final String PARAM_NOHIRES = "-nohires";

	private WsaPalette wsapalette;
	private SeekableByteChannel bytechannel;
	private final ExecutorService decoderthreadpool = Executors.newCachedThreadPool();

	private boolean srcnohires;

	/**
	 * Constructor, creates a new wsa file with the given name and data.
	 * 
	 * @param name		  The name of this file.
	 * @param bytechannel The data of the file.
	 */
	public WsaFileCNC(String name, ReadableByteChannel bytechannel) {

		super(name);

		// Read file header
		ByteBuffer headerbytes = ByteBuffer.allocate(WsaFileHeaderCNC.HEADER_SIZE);
		try {
			bytechannel.read(headerbytes);

			headerbytes.rewind();
			wsaheader = new WsaFileHeaderCNC(headerbytes);
	
			// Read offsets
			wsaoffsets = new int[numImages() + 2];
			ByteBuffer offsetsbytes = ByteBuffer.allocate(wsaoffsets.length * 4);
			bytechannel.read(offsetsbytes);
			offsetsbytes.rewind();
			for (int i = 0; i < wsaoffsets.length; i++) {
				wsaoffsets[i] = offsetsbytes.getInt();
			}
	
			// Read internal palette
			ByteBuffer palettebytes = ByteBuffer.allocate(PALETTE_SIZE);
			bytechannel.read(palettebytes);
			palettebytes.rewind();
			wsapalette = new WsaPalette(palettebytes);
	
			// Store seekable channel types
			if (bytechannel instanceof SeekableByteChannel) {
				this.bytechannel = (SeekableByteChannel)bytechannel;
			}
	
			// If the input channel isn't seekable, create a temp file that is seekable
			else {
				File tempanimdatafile = File.createTempFile(name, null);
				tempanimdatafile.deleteOnExit();
				FileChannel filechannel = FileChannel.open(Paths.get(tempanimdatafile.getAbsolutePath()), WRITE);
				filechannel.write(BufferUtility.readRemaining(bytechannel));
				filechannel.position(0);
				this.bytechannel = filechannel;
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Constructor, builds this filetype using the data from a <tt>PngFile</tt>
	 * type.
	 * 
	 * @param name	  The name of this file.
	 * @param pngfile Fully read PNG file to extract data from.
	 * @param params  Additional parameters: width, height, numimgs, framerate,
	 * 				  looping.
	 */
	public WsaFileCNC(String name, PngFile pngfile, String... params) {

		super(name, pngfile, params);

		// Check C&C-specific parameters
		for (String param: params) {
			if (param.equals(PARAM_NOHIRES)) {
				srcnohires = true;
				break;
			}
		}

		// Set the palette
		wsapalette = new WsaPalette(pngfile.getPalette());
	}

	/**
	 * Constructor, builds this filetype using the data from several
	 * <tt>PngFile</tt> types.
	 * 
	 * @param name	   The name of this file.
	 * @param pngfiles Fully read PNG files to extract data from.
	 * @param params   Additonal parameters: framerate, looping, nohires.
	 */
	public WsaFileCNC(String name, ArrayList<PngFile> pngfiles, String... params) {

		super(name, pngfiles.toArray(new PngFile[pngfiles.size()]), params);

		// Check C&C-specific parameters
		for (String param: params) {
			if (param.equals(PARAM_NOHIRES)) {
				srcnohires = true;
				break;
			}
		}

		// Set the palette
		wsapalette = new WsaPalette(pngfiles.get(0).getPalette());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float adjustmentFactor() {

		return 1.2f;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void buildFile(int width, int height, float framerate, boolean looping, ByteBuffer[] frames) {

		// Build file header
		wsaheader = new WsaFileHeaderCNC(looping ? (short)(frames.length - 1) : (short)frames.length,
				(short)0, (short)0, (short)width, (short)height,
				(int)((1f / framerate) * 1000f * 1024f));

		// Build frames
		wsaframes = frames;
	}

	/**
	 * Takes care of resource cleanup if the decoder never had a chance to
	 * finish.
	 */
	@Override
	public void close() {

		try {
			bytechannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		decoderthreadpool.shutdownNow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float frameRate() {

		return 1f / (wsaheader.delta / 1024f) * 1000f;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getImagesData() {

		// Leverage the raw frame decoder as input to the colour decoder
		Pipe pipe = null;
		try {
			pipe = Pipe.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		decoderthreadpool.execute(new ColourFrameDecoder(getRawImageData(), pipe.sink()));
		return pipe.source();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Palette getPalette() {

		return wsapalette;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getRawImageData() {

		Pipe pipe;
		try {
			pipe = Pipe.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		decoderthreadpool.execute(new RawFrameDecoder(
				new DuplicateReadOnlyByteChannel(bytechannel), pipe.sink()));
		return pipe.source();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int height() {

		return wsaheader.height & 0xffff;
	}

	/**
	 * Returns some information on this WSA file.
	 * 
	 * @return WSA file info.
	 */
	@Override
	public String toString() {

		DecimalFormat dp2 = new DecimalFormat("0.00");
		return filename + " (C&C WSA file)" +
			"\n  Number of images: " + numImages() +
					 (isLooping() ? " + 1 loop frame" : "") + 
			"\n  Framerate: " + dp2.format(frameRate()) + "fps" +
			"\n  Image width: " + width() +
			"\n  Image height: " + height() +
			"\n  Colour depth: 8-bit (using internal palette)";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int width() {

		return wsaheader.width & 0xffff;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(GatheringByteChannel outputchannel) {

		int numimages = numImages();

		// Build header
		ByteBuffer header = wsaheader.toByteBuffer();

		// Build palette
		ByteBuffer palette = wsapalette.toByteBuffer();

		// Encode each frame, construct matching offsets
		ByteBuffer[] frames = new ByteBuffer[isLooping() ? numimages + 1 : numimages];
		ByteBuffer lastbytes = ByteBuffer.allocate(width() * height());

		ByteBuffer frameoffsets = ByteBuffer.allocate((numimages + 2) * 4);
		int offsettotal = WsaFileHeaderCNC.HEADER_SIZE + ((numimages + 2) * 4);

		for (int i = 0; i < frames.length; i++) {
			ByteBuffer framebytes = wsaframes[i];
			ByteBuffer frameint = ByteBuffer.allocate((int)(framebytes.capacity() * 1.5));
			ByteBuffer frame    = ByteBuffer.allocate((int)(framebytes.capacity() * 1.5));

			// First encode in Format40, then Format80
			CodecUtility.encodeFormat40(framebytes, frameint, lastbytes);
			CodecUtility.encodeFormat80(frameint, frame);

			frames[i] = frame;
			lastbytes = framebytes;

			frameoffsets.putInt(offsettotal);
			offsettotal += frame.limit();
		}

		// Last offset for EOF
		frameoffsets.putInt(offsettotal);
		frameoffsets.rewind();


		// Write file to disk
		try {
			outputchannel.write(header);
			outputchannel.write(frameoffsets);
			outputchannel.write(palette);
			outputchannel.write(frames);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Generate high-res colour lookup table
		if (!srcnohires) {

			// Figure-out the appropriate file name
			String lookupname = filename.contains(".") ?
					filename.substring(0, filename.lastIndexOf('.')) + ".pal" :
					filename + ".pal";

			// Write the index of the closest interpolated palette colour
			// TODO: Perform proper colour interpolation
			ByteBuffer lookup = ByteBuffer.allocate(256);
			for (int i = 0; i < 256; i++) {
				lookup.put((byte)i);
			}
			lookup.rewind();

			try (FileChannel lookupfile = FileChannel.open(Paths.get(lookupname), WRITE)) {
				for (int i = 0; i < 256; i++) {
					lookupfile.write(lookup);
				}
			}
			// TODO: Should be able to soften the auto-close without needing this
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Decoder task for returning the raw indexed frame data.
	 */
	private class RawFrameDecoder extends StreamingDataDecoder {

		/**
		 * Constructor, set the input and output channels for the decoder to
		 * use.
		 * 
		 * @param inputchannel
		 * @param outputchannel
		 */
		private RawFrameDecoder(ReadableByteChannel inputchannel, WritableByteChannel outputchannel) {

			super(inputchannel, outputchannel);
		}

		/**
		 * Decodes a single frame of animation.  Does not colour the frame with
		 * the palette afterwards.
		 * 
		 * @param framenum	The number of the frame to decode.
		 * @param lastbytes Frame data of the last frame decoded.
		 * @return Raw decoded frame data.
		 */
		private ByteBuffer decodeFrame(int framenum, ByteBuffer lastbytes) {

			int offset = wsaoffsets[framenum];
			int sourcelength = wsaoffsets[framenum + 1] - offset;

			// Source frame data (is at frame offset + palette size)
			ByteBuffer sourcebytes = ByteBuffer.allocate(sourcelength);
			try {
				inputchannel.read(sourcebytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sourcebytes.rewind();

			// Intermediate and final frame data
			int framesize = width() * height();
			ByteBuffer intbytes   = ByteBuffer.allocate(framesize);
			ByteBuffer framebytes = ByteBuffer.allocate(framesize);

			// First decompress from Format80, then decode as Format40
			CodecUtility.decodeFormat80(sourcebytes, intbytes);
			CodecUtility.decodeFormat40(intbytes, framebytes, lastbytes);

			return framebytes;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void decode() {

			ByteBuffer lastframebytes = ByteBuffer.allocate(width() * height());

			for (int i = 0; i < numImages(); i++) {
				ByteBuffer framebytes = decodeFrame(i, lastframebytes);
				try {
					outputchannel.write(framebytes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				lastframebytes = (ByteBuffer)framebytes.rewind();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String threadName() {

			return "WsaFileCNC :: " + filename + " :: Animation data decoding thread";
		}
	}

	/**
	 * Decoder task for returning full colour frames.
	 */
	private class ColourFrameDecoder extends StreamingDataDecoder {

		/**
		 * Constructor, set the input and output channels for the decoder to
		 * use.
		 * 
		 * @param inputchannel
		 * @param outputchannel
		 */
		private ColourFrameDecoder(ReadableByteChannel inputchannel, WritableByteChannel outputchannel) {

			super(inputchannel, outputchannel);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void decode() {

			ByteBuffer framebytes  = ByteBuffer.allocate(width() * height());
			ByteBuffer colourbytes = ByteBuffer.allocate(width() * height() * format().size);

			// Colour every decoded frame
			for (int i = 0; i < numImages(); i++) {
				framebytes.clear();
				try {
					inputchannel.read(framebytes);
					framebytes.rewind();
	
					colourbytes.clear();
					ImageUtility.applyPalette(framebytes, colourbytes, wsapalette);
					outputchannel.write(colourbytes);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String threadName() {

			return "WsaFileCNC :: " + filename + " :: Animation data colouring thread";
		}
	}
}
