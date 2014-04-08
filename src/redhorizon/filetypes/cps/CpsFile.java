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

package redhorizon.filetypes.cps;

import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.FileType;
import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.Paletted;
import redhorizon.filetypes.PalettedInternal;
import redhorizon.filetypes.WritableFile;
import redhorizon.filetypes.pcx.PcxFile;
import redhorizon.media.Palette;
import redhorizon.utilities.BufferUtility;
import redhorizon.utilities.CodecUtility;
import redhorizon.utilities.ImageUtility;
import redhorizon.utilities.channels.ReadableByteChannelAdapter;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGB;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Implementation of the CPS file used in C&C and Dune 2.  The CPS file is a low
 * resolution (320x200 usually) image file that may or may not contain a
 * palette.
 * <p>
 * The CPS file is only used for the conversion utility, and does not take part
 * in the Red Horizon game.
 * 
 * @author Emanuel Rabina
 */
@FileType(ImageFile.class)
public class CpsFile extends AbstractFile implements ImageFile, Paletted, PalettedInternal, WritableFile {

	// CPS constants
	private static final int IMAGE_WIDTH  = 320;
	private static final int IMAGE_HEIGHT = 200;

	private static final String PARAM_NOPALETTE = "-nopal";

	// Read-from information
	private CpsFileHeader cpsheader;
	private CpsPalette cpspalette;
	private ByteBuffer cpsimage;

	/**
	 * Constructor, creates a new cps file with the given file name and data.
	 * 
	 * @param name		  Name of the CPS file.
	 * @param bytechannel Data of the cps file.
	 */
	public CpsFile(String name, ReadableByteChannel bytechannel) {

		super(name);

		try {
			// File header
			ByteBuffer headerbytes = ByteBuffer.allocate(CpsFileHeader.HEADER_SIZE);
			bytechannel.read(headerbytes);
			headerbytes.rewind();
			cpsheader = new CpsFileHeader(headerbytes);

			// File palette?
			if (cpsheader.palettesize != 0) {
				ByteBuffer palettebytes = ByteBuffer.allocate(256 * 3);
				palettebytes.rewind();
				cpspalette = new CpsPalette(palettebytes);
			}

			// File image (Format80 compressed)
			ByteBuffer imagebytes = ByteBuffer.allocate((cpsheader.filesize & 0xffff) -
					CpsFileHeader.HEADER_SIZE - 2 - cpsheader.palettesize);
			bytechannel.read(imagebytes);
			imagebytes.rewind();
			cpsimage = ByteBuffer.allocate(cpsheader.imagesize);
			CodecUtility.decodeFormat80(imagebytes, cpsimage);
		} catch (IOException e) {}
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
	 * Constructor, creates a new cps file from another image.
	 * 
	 * @param name		Name of the CPS file.
	 * @param imagefile {@link ImageFile} instance.
	 * @param params	Additional parameters: unpaletted (opt).
	 */
	private CpsFile(String name, ImageFile imagefile, String... params) {

		super(name);

		boolean usepalette = true;

		// Grab the parameters
		for (String param: params) {
			if (param.equals(PARAM_NOPALETTE)) {
				usepalette = false;
			}
		}

		// Ensure the image meets CPS file requirements
		if (imagefile.width() != IMAGE_WIDTH) {
			throw new IllegalArgumentException("CPS file image size isn't 0xFA00 (320x200)");
		}
		if (imagefile.height() != IMAGE_HEIGHT) {
			throw new IllegalArgumentException("CPS file image size isn't 0xFA00 (320x200)");
		}

		// Check for a palette if creating a paletted CPS
		if (usepalette && !(imagefile instanceof PalettedInternal)) {
			throw new IllegalArgumentException(
					"No palette found in source image for use in paletted CPS file");
		}

		// Copy palette, image
		cpspalette = usepalette ? new CpsPalette(((PalettedInternal)imagefile).getPalette()) : null;
		cpsimage   = BufferUtility.readRemaining(((PalettedInternal)imagefile).getRawImageData());
	}

	/**
	 * Constructor, creates a new cps file from a pcx file.
	 * 
	 * @param name	  The name of this file.
	 * @param pcxfile PCX file to draw data from.
	 * @param params  Additional parameters: unpaletted (opt).
	 */
	public CpsFile(String name, PcxFile pcxfile, String... params) {

		this(name, (ImageFile)pcxfile, params);
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

		if (cpspalette != null) {
			ByteBuffer image = ByteBuffer.allocate(width() * height() * format().size);
			ImageUtility.applyPalette(cpsimage, image, cpspalette);
			return new ReadableByteChannelAdapter(image);
		}
		return new ReadableByteChannelAdapter(cpsimage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Palette getPalette() {

		return cpspalette;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getRawImageData() {

		return cpspalette != null ? new ReadableByteChannelAdapter(cpsimage) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int height() {

		return IMAGE_HEIGHT;
	}

	/**
	 * Returns some information on this CPS file.
	 * 
	 * @return CPS file info.
	 */
	@Override
	public String toString() {

		return filename + " (CPS file)" +
			"\n  Image width: " + width() +
			"\n  Image height: " + height() +
			"\n  Colour depth: 8-bit " +
			(cpspalette != null ? "(using internal palette)" : "(no internal palette)");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int width() {

		return IMAGE_WIDTH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(GatheringByteChannel outputchannel) {

		try {
			// Encode image
			ByteBuffer image = ByteBuffer.allocate(cpsimage.capacity());
			CodecUtility.encodeFormat80(cpsimage, image);

			// Build palette (if exists)
			ByteBuffer palette = cpspalette != null ? cpspalette.toByteBuffer() : null;

			// Construct header, store to ByteBuffer
			cpsheader = cpspalette != null ?
					new CpsFileHeader((short)(CpsFileHeader.HEADER_SIZE + CpsFileHeader.PALETTE_SIZE + image.limit() - 2),
							CpsFileHeader.PALETTE_SIZE) :
					new CpsFileHeader((short)(CpsFileHeader.HEADER_SIZE + image.limit() - 2), (short)0);
			ByteBuffer header = cpsheader.toByteBuffer();

			// Write file
			outputchannel.write(cpspalette != null ?
					new ByteBuffer[]{ header, palette, image } :
					new ByteBuffer[]{ header, image });
		} catch (IOException e) {}
		finally {
			try {
				outputchannel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
