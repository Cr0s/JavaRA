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

package redhorizon.filetypes.vqa;

import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.FileType;
import redhorizon.filetypes.SoundBitrate;
import redhorizon.filetypes.SoundChannels;
import redhorizon.filetypes.StreamingDataDecoder;
import redhorizon.filetypes.VideoFile;
import redhorizon.utilities.BufferUtility;
import redhorizon.utilities.CodecUtility;
import redhorizon.utilities.ImageUtility;
import redhorizon.utilities.channels.DuplicateReadOnlyByteChannel;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGB;
import static redhorizon.filetypes.SoundBitrate.*;
import static redhorizon.filetypes.SoundChannels.*;
import static redhorizon.filetypes.vqa.VqaChunkTypes.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Implementation of a VQA file.  This is the video format used in Red Alert and
 * Tiberium Dawn.
 * <p>
 * The 2 documents I've obtained conflict slightly with each other as to the
 * interpretation of the file layout.  I'll use my own, combined with the
 * knowledge gained from the old FreeCNC project, to structure the VQA files.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions("vqa")
@FileType(VideoFile.class)
public class VqaFile extends AbstractFile implements VideoFile {

	private static final String TYPE_WS_ADPCM  = "SND1";
	private static final String TYPE_IMA_ADPCM = "SND2";

	private VqaFileHeader vqaheader;
	private SeekableByteChannel bytechannel;
	private final ExecutorService decoderthreadpool = Executors.newCachedThreadPool();

	/**
	 * Constructor, creates a vqa file with the given name and file data.
	 * 
	 * @param name		  The name of this file.
	 * @param bytechannel The data of this file.
	 */
	public VqaFile(String name, ReadableByteChannel bytechannel) {

		super(name);

		// Read header
		readNextChunk(bytechannel);
		ByteBuffer headerbytes = ByteBuffer.allocate(VqaFileHeader.HEADER_SIZE);
		try {
			bytechannel.read(headerbytes);

			headerbytes.rewind();
			vqaheader = new VqaFileHeader(headerbytes);
	
			// Read offsets
			readNextChunk(bytechannel);
			ByteBuffer offsetsbytes = ByteBuffer.allocate(
					VqaFrameOffset.FRAME_OFFSET_SIZE * numImages());
			bytechannel.read(offsetsbytes);
			offsetsbytes.rewind();
			for (int i = 0; i < numImages(); i++) {
				vqaheader.offsets[i] = new VqaFrameOffset(offsetsbytes);
			}
	
			// Store seekable channel types
			if (bytechannel instanceof SeekableByteChannel) {
				this.bytechannel = (SeekableByteChannel)bytechannel;
			}
	
			// If the input channel isn't seekable, create a temp file that is seekable
			else {
				File tempviddatafile = File.createTempFile(name, null);
				tempviddatafile.deleteOnExit();
				FileChannel filechannel = FileChannel.open(Paths.get(tempviddatafile.getAbsolutePath()), WRITE);
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
	public SoundBitrate bitrate() {

		return vqaheader.bits == 8 ? BITRATE_8 : BITRATE_16;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SoundChannels channels() {

		return vqaheader.channels == 1 ? CHANNELS_MONO : CHANNELS_STEREO;
	}

	/**
	 * Does nothing.
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
	public ColourFormat format() {

		return FORMAT_RGB;
	}

	/**
	 * Returns the frames/second at which this VQA file plays.
	 * 
	 * @return Frames/second of the video.
	 */
	@Override
	public float frameRate() {

		return vqaheader.framerate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int frequency() {

		return vqaheader.frequency;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getImagesData() {

		Pipe pipe = null;
		try {
			pipe = Pipe.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		decoderthreadpool.execute(new ImageDataDecoder(
				new DuplicateReadOnlyByteChannel(bytechannel), pipe.sink()));
		return pipe.source();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getSoundData() {
		Pipe pipe = null;
		
		try {
			pipe = Pipe.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		decoderthreadpool.execute(new SoundDataDecoder(
				new DuplicateReadOnlyByteChannel(bytechannel), pipe.sink()));
		return pipe.source();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int height() {

		return vqaheader.height & 0xffff;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int numImages() {

		return vqaheader.numframes & 0xffff;
	}

	/**
	 * Reads the next chunk of data, bypassing the null bytes used to make the
	 * offsets all even.
	 * 
	 * @param inputchannel Channel to use with which to find the next chunk.
	 * @return The next <tt>VqaChunk</tt>.
	 */
	private static VqaChunkHeader readNextChunk(ReadableByteChannel inputchannel) {

		ByteBuffer nullbyte = ByteBuffer.allocate(1);
		VqaChunkHeader vqachunk;
		while (true) {

			// Check for null bytes
			try {
				inputchannel.read(nullbyte);

				ByteBuffer chunkbytes = ByteBuffer.allocate(VqaChunkHeader.CHUNK_SIZE);
				if (nullbyte.get() != 0) {
					chunkbytes.put(nullbyte.array());
				}
				inputchannel.read(chunkbytes);
				chunkbytes.rewind();
				vqachunk = new VqaChunkHeader(chunkbytes);
	
				// Break only on known chunk names
				if (vqachunk.chunkType() != null) {
					break;
				}
				nullbyte.clear();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return vqachunk;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int width() {

		return vqaheader.width & 0xffff;
	}

	/**
	 * Decoder task for the sound part of the vqa file.
	 */
	private class SoundDataDecoder extends StreamingDataDecoder {

		private ByteBuffer soundbytes = ByteBuffer.allocate(524288);	// 512K

		/**
		 * Constructor, sets the input and output channels.
		 * 
		 * @param inputchannel
		 * @param outputchannel
		 */
		private SoundDataDecoder(ReadableByteChannel inputchannel, WritableByteChannel outputchannel) {

			super(inputchannel, outputchannel);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void decode() {

			// Sound decoding components
			ByteBuffer chunkheaderbytes = ByteBuffer.allocate(VqaChunkHeader.CHUNK_SIZE);
			ByteBuffer chunkbytes = ByteBuffer.allocate(524288);	// 512K
			int[] update = {0,0};

			// Sound decoding loop
			while (true) {
				chunkheaderbytes.clear();
				int read;
				try {
					read = inputchannel.read(chunkheaderbytes);

					if (read == -1) {
						break;
					}
					chunkheaderbytes.rewind();
					VqaChunkHeader vqachunk = new VqaChunkHeader(chunkheaderbytes);
	
					// Decode sound data
					if (vqachunk.chunkType() == CHUNK_SND) {
						chunkbytes.clear();
						chunkbytes.limit(vqachunk.length);
						inputchannel.read(chunkbytes);
						chunkbytes.rewind();
						ByteBuffer sounddata = decodeSNDChunk(vqachunk, chunkbytes, update);
						outputchannel.write(sounddata);
					}
	
					// Skip everything else
					else {
						ByteBuffer skipbuffer = ByteBuffer.allocate(vqachunk.length);
						inputchannel.read(skipbuffer);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * Decodes the SND chunk of a VQA file.
		 * 
		 * @param vqachunk Currenlty read chunk.
		 * @param bytes	   Buffer containing the sound data.
		 * @param update   2-<tt>int</tt> array of the latest index and sample
		 * 				   values respectively.
		 * @return The sound data from this chunk.
		 */
		private ByteBuffer decodeSNDChunk(VqaChunkHeader vqachunk, ByteBuffer bytes, int[] update) {

			soundbytes.clear();

			// Decode compressed
			if (vqachunk.isCompressed()) {
				if (vqachunk.chunkname.equals(TYPE_WS_ADPCM)) {
					CodecUtility.decode8bitWSADPCM(bytes, soundbytes);
				}
				else if (vqachunk.chunkname.equals(TYPE_IMA_ADPCM)) {
					CodecUtility.decode16bitIMAADPCM(bytes, soundbytes, update);
				}
			}

			// Decode uncompressed
			else {
				soundbytes.put(bytes).flip();
			}

			return soundbytes;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String threadName() {

			return "VqaFile :: " + filename + " :: Sound data decoding thread";
		}
	}

	/**
	 * Decoder task for the image part of the vqa file.
	 */
	private class ImageDataDecoder extends StreamingDataDecoder {

		private ByteBuffer framebytes  = ByteBuffer.allocate(width() * height());
		private ByteBuffer colourbytes = ByteBuffer.allocate(width() * height() * format().size);

		private final int blocksize;
		private final int blockparts;
		private final int tablesize;

		/**
		 * Constructor, sets the input and output channels.
		 * 
		 * @param inputchannel
		 * @param outputchannel
		 */
		private ImageDataDecoder(ReadableByteChannel inputchannel, WritableByteChannel outputchannel) {

			super(inputchannel, outputchannel);

			// Set some useful constants
			blocksize  = blockWidth() * blockHeight();
			blockparts = (width() / blockWidth()) * (height() / blockHeight());
			tablesize  = maxBlocks() * blocksize;
		}

		/**
		 * Returns the height of each video block.
		 * 
		 * @return The height of each block.
		 */
		private byte blockHeight() {

			return vqaheader.blockheight;
		}

		/**
		 * Returns the width of each video block.
		 * 
		 * @return The width of each block.
		 */
		private byte blockWidth() {

			return vqaheader.blockwidth;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("incomplete-switch")
		protected void decode() {

			// Image decoding components
			ByteBuffer chunkheaderbytes = ByteBuffer.allocate(VqaChunkHeader.CHUNK_SIZE);
			ByteBuffer chunkbytes = ByteBuffer.allocate(524288);	// 512K

			ArrayList<ByteBuffer> partialtables = new ArrayList<>();
			VqaTable vqatable = null;
			boolean tablescompressed = false;

			VqaPalette vqapalette = null;

			// Image decoding loop
			while (true) {
				chunkheaderbytes.clear();
				int read;
				try {
					read = inputchannel.read(chunkheaderbytes);

					if (read == -1) {
						break;
					}
					chunkheaderbytes.rewind();
					VqaChunkHeader vqachunk = new VqaChunkHeader(chunkheaderbytes);
	
					// Decode image and image-related data
					if (vqachunk.chunkType() == CHUNK_VQFR) {
						outer: while (true) {
							vqachunk = readNextChunk(inputchannel);
							chunkbytes.clear();
							inputchannel.read(chunkbytes);
							chunkbytes.flip();
	
							switch (vqachunk.chunkType()) {
	
							// Full lookup table
							case CHUNK_CBF:
								vqatable = decodeCBFChunk(vqachunk, chunkbytes);
								break;
							// Partial lookup table
							case CHUNK_CBP:
								tablescompressed = decodeCBPChunk(vqachunk, chunkbytes, partialtables);
								break;
							// Palette
							case CHUNK_CPL:
								vqapalette = decodeCPLChunk(vqachunk, chunkbytes);
								break;
							// Video data
							case CHUNK_VPT:
								ByteBuffer vqaframe = decodeVPTChunk(vqachunk, chunkbytes, vqatable, vqapalette);
								outputchannel.write(vqaframe);
								break outer;
							}
						}
	
						// If full, replace the old lookup table
						if (partialtables.size() == numLookupTables()) {
							vqatable = replaceLookupTable(partialtables, tablescompressed);
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * Decodes the complete lookup table (CBF chunk) of a VQA file.
		 * 
		 * @param vqachunk Currently read chunk.
		 * @param bytes	   Buffer containing the lookup table data.
		 * @return A full lookup table.
		 */
		private VqaTable decodeCBFChunk(VqaChunkHeader vqachunk, ByteBuffer bytes) {

			ByteBuffer tablebytes;

			// Decode compressed
			if (vqachunk.isCompressed()) {
				tablebytes = ByteBuffer.allocate(tablesize);
				CodecUtility.decodeFormat80(bytes, tablebytes);
			}
			// Decode uncompressed
			else {
				tablebytes = bytes;
			}
			return new VqaTable(tablebytes);
		}

		/**
		 * Decodes the partial lookup table (CBP chunk) of a VQA file.
		 * 
		 * @param vqachunk		Currenlty read chunk.
		 * @param bytes			Buffer containing the partial lookup data.
		 * @param partialtables List of partial lookup tables.
		 * @return <tt>true</tt> if the partials are compressed, <tt>false</tt>
		 * 		   otherwise.
		 */
		private boolean decodeCBPChunk(VqaChunkHeader vqachunk, ByteBuffer bytes,
			ArrayList<ByteBuffer> partialtables) {

			partialtables.add(bytes);
			return vqachunk.isCompressed();
		}

		/**
		 * Decodes the palette data (CPL chunk) of a VQA file.
		 * 
		 * @param vqachunk Currenlty read <tt>VqaChunk</tt>.
		 * @param bytes	   <tt>ByteBuffer</tt> containing the palette data.
		 * @return The palette from this chunk.
		 */
		private VqaPalette decodeCPLChunk(VqaChunkHeader vqachunk, ByteBuffer bytes) {

			ByteBuffer palbytes;

			// Decode compressed
			if (vqachunk.isCompressed()) {
				palbytes = ByteBuffer.allocate(blocksize);
				CodecUtility.decodeFormat80(bytes, palbytes);
			}
			// Decode uncompressed
			else {
				palbytes = bytes;
			}

			// Build the new palette
			return new VqaPalette(numColours(), palbytes);
		}

		/**
		 * Decodes the video data (VPT chunk) of a VQA file.
		 * 
		 * @param vqachunk	 Currently read chunk.
		 * @param bytes		 Buffer containing the sound data.
		 * @param vqatable	 Current values of the lookup table.
		 * @param vqapalette Current palette for the frame.
		 * @return A fully decoded frame of video.
		 */
		private ByteBuffer decodeVPTChunk(VqaChunkHeader vqachunk, ByteBuffer bytes,
			VqaTable vqatable, VqaPalette vqapalette) {

			ByteBuffer videobytes;

			// Decode compressed
			if (vqachunk.isCompressed()) {
				videobytes = ByteBuffer.allocate(blockparts << 1);
				CodecUtility.decodeFormat80(bytes, videobytes);
			}
			// Decode uncompressed
			else {
				videobytes = bytes;
			}

			framebytes.clear();

			// Now decode every block
			int nextline = width() - blockWidth();
			int modifier = blockHeight() == 2 ? 0xf : 0xff;
			int block = 0;

			// Go across first, then down
			for (int y = 0; y < height(); y += blockHeight()) {
				for (int x = 0; x < width(); x += blockWidth(), block++) {

					framebytes.position(y * width() + x);

					// Get the proper lookup value for the block
					int topval = videobytes.get(block) & 0xff;
					int botval = videobytes.get(block + blockparts) & 0xff;

					// Fill the block with 1 colour
					if (botval == modifier) {

						for (int i = 1; i <= blocksize; i++) {
							framebytes.put((byte)topval);

							if ((i % blockWidth() == 0) && (i != blocksize)) {
								framebytes.position(framebytes.position() + nextline);
							}
						}
					}

					// Otherwise, fill the block with the one in the lookup table
					else {
						int ref = ((botval << 8) + topval) * blocksize;

						for (int i = 1; i <= blocksize; i++) {
							framebytes.put(vqatable.getValueAt(ref++));

							if ((i % blockWidth() == 0) && (i != blocksize)) {
								framebytes.position(framebytes.position() + nextline);
							}
						}
					}
				}
			}
			framebytes.rewind();

			// Apply the palette
			colourbytes.clear();
			ImageUtility.applyPalette(framebytes, colourbytes, vqapalette);

			return colourbytes;
		}

		/**
		 * Returns the maximum number of blocks used in the compression of this
		 * video.
		 * 
		 * @return The maximum number of blocks per frame.
		 */
		private int maxBlocks() {

			return vqaheader.maxblocks;
		}

		/**
		 * Returns the number of colours used for this video.
		 * 
		 * @return Number of colours in this video.
		 */
		private int numColours() {

			return vqaheader.numcolours & 0xffff;
		}

		/**
		 * Returns the number of partial lookup tables required to make a
		 * complete one.
		 * 
		 * @return The number of frames which share a lookup table.
		 */
		private int numLookupTables() {

			return vqaheader.cbparts & 0xff;
		}

		/**
		 * Replaces the VQA lookup table with the complete construction of
		 * several partials.
		 * 
		 * @param partialtables List of partials so far.
		 * @param compressed	Whether or not the array of partials is
		 * 						compressed.
		 * @return The replacement lookup table.
		 */
		private VqaTable replaceLookupTable(ArrayList<ByteBuffer> partialtables, boolean compressed) {

			// Generate complete table
			ByteBuffer fulltable = ByteBuffer.allocate(tablesize);
			for (ByteBuffer partial: partialtables) {
				fulltable.put(partial);
			}
			partialtables.clear();
			fulltable.flip();

			// Decode uncompressed
			ByteBuffer tablebytes;
			if (compressed) {
				tablebytes = ByteBuffer.allocate(tablesize);
				CodecUtility.decodeFormat80(fulltable, tablebytes);
			}
			// Decode compressed
			else {
				tablebytes = fulltable;
			}
			return new VqaTable(tablebytes);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String threadName() {

			return "VqaFile :: " + filename + " :: Image data decoding thread";
		}
	}
}
