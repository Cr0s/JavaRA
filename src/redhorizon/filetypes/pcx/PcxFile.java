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

package redhorizon.filetypes.pcx;

import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.FileType;
import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.PalettedInternal;
import redhorizon.filetypes.UnsupportedFileException;
import redhorizon.media.Palette;
import redhorizon.utilities.BufferUtility;
import redhorizon.utilities.CodecUtility;
import redhorizon.utilities.ImageUtility;
import redhorizon.utilities.channels.ReadableByteChannelAdapter;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGB;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

/**
 * Implementation of the PCX file format.  PCX files are used for the higher
 * resolution (640x400) still images used in Red Alert and Tiberium Dawn.
 * <p>
 * PCX files can come in many flavours (8-bit, 16-bit, 24-bit, no palette, etc),
 * but for the purpose of Red Horizon, PCX files will be of the type used in the
 * old RA & TD games: a 256-colour file with an internal palette located at the
 * tail of the file.  Attempts to load any other types will result in an
 * {@link UnsupportedFileException}.  The addition of support for other sorts of
 * PCX files may be done in future.
 * 
 * @author Emanuel Rabina.
 */
@FileExtensions("pcx")
@FileType(ImageFile.class)
public class PcxFile extends AbstractFile implements ImageFile, PalettedInternal {

	private static final int PALETTE_SIZE         = 768;
	private static final int PALETTE_PADDING_SIZE = 1;

	private PcxFileHeader pcxheader;
	private PcxPalette pcxpalette;
	private ByteBuffer pcximage;

	/**
	 * Constructor, creates a new pcx file with the given file name and file
	 * data..
	 * 
	 * @param name		  Name of the pcx file.
	 * @param bytechannel Input stream of the pcx file data.
	 */
	public PcxFile(String name, ReadableByteChannel bytechannel) {

		super(name);

		try {
			// Read header
			ByteBuffer headerbytes = ByteBuffer.allocate(PcxFileHeader.HEADER_SIZE);
			bytechannel.read(headerbytes);
			headerbytes.rewind();
			pcxheader  = new PcxFileHeader(headerbytes);

			// Read the rest of the stream
			ByteBuffer pcxdata = BufferUtility.readRemaining(bytechannel);

			// Decode PCX run-length encoded image data scanline-by-scanline
			ByteBuffer sourcebytes = ByteBuffer.allocate(pcxdata.limit() - PALETTE_SIZE);
			ArrayList<ByteBuffer> scanlines = new ArrayList<>();

			while (sourcebytes.hasRemaining()) {
				ByteBuffer scanline = ByteBuffer.allocate(pcxheader.planes * pcxheader.bytesperline);
				CodecUtility.decodeRLE67(sourcebytes, scanline);
				scanlines.add(scanline);
			}

			// Cull the image to the appropriate width/height dimensions (for when
			// scanlines extend beyond the image borders)
			pcximage = ByteBuffer.allocate(width() * height());
			for (int y = pcxheader.ymin; y <= pcxheader.ymax; y++) {
				ByteBuffer scanline = scanlines.get(y);
				for (int x = pcxheader.xmin; x <= pcxheader.xmax; x++) {
					pcximage.put(scanline.get(x));
				}
			}
			pcximage.rewind();

			// Assign palette (from tail of file, after the padding byte)
			ByteBuffer palettebytes = ByteBuffer.allocate(PALETTE_SIZE);
			palettebytes.put((ByteBuffer)pcxdata.position(pcxdata.position() + PALETTE_PADDING_SIZE));
			pcxpalette = new PcxPalette(palettebytes);
		} catch (IOException e) { e.printStackTrace(); }
		finally {
			try {
				bytechannel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void close() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ColourFormat format() {

		return FORMAT_RGB;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getImageData() {

		// Apply internal palette
		ByteBuffer rgbimage = ByteBuffer.allocate(width() * height() * format().size);
		ImageUtility.applyPalette(pcximage, rgbimage, pcxpalette);
		return new ReadableByteChannelAdapter(rgbimage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Palette getPalette() {

		return pcxpalette;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getRawImageData() {
		try (ReadableByteChannelAdapter readableByteChannelAdapter = new ReadableByteChannelAdapter(pcximage)) {
			return pcxpalette != null ? readableByteChannelAdapter : null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int height() {

		return pcxheader.ymax - pcxheader.ymin + 1;
	}

	/**
	 * Returns some information on this PCX file.
	 * 
	 * @return PCX file info.
	 */
	@Override
	public String toString() {

		return filename + " (PCX file)" +
			"\n  Image width: " + width() +
			"\n  Image height: " + height() +
			"\n  Colour depth: 8-bit " + (pcxpalette != null ? "(using internal palette)" : "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int width() {

		return pcxheader.xmax - pcxheader.xmin + 1;
	}
}
