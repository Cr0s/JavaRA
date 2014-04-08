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

import redhorizon.filetypes.UnsupportedFileException;

import java.nio.ByteBuffer;

/**
 * Representation of the PCX file's header, which contains information about the
 * data in the file.
 * 
 * @author Emanuel Rabina
 */
public class PcxFileHeader {

	static final int HEADER_SIZE = 128;

	// Header constants
	private static final int PALETTE_SIZE = 48;

	private static final byte MANUFACTURER_ZSOFT = 0x0a;	// 10 = ZSoft .pcx

	private static final byte VERSION_PCP25        = 0;		// PC Paintbrush 2.5
	private static final byte VERSION_PCP28_PAL    = 2;		// PC Paintbrush 2.8 w/ palette
	private static final byte VERSION_PCP28_NO_PAL = 3;		// PC Paintbrush 2.8 w/o palette
	private static final byte VERSION_PCP4WIN      = 4;		// PC Paintbrush for Windows
	private static final byte VERSION_PCPPLUS      = 5;		// PC Paintbrush+

	private static final byte ENCODING_RLE = 1;				// 1 = run-length encoding

	private static final byte BPP_8 = 8;					// 8-bits-per-pixel, 256 colours

	// Header data
	final byte manufacturer;
	final byte version;
	final byte encoding;
	final byte bitsperpixel;
	final short xmin;
	final short ymin;
	final short xmax;
	final short ymax;
	final short hdpi;
	final short vdpi;
	final byte[] palette;
	final byte reserved;
	final byte planes;
	final short bytesperline;
	final short paletteinfo;
//	final short hscreensize;
//	final short vscreensize;
//	final byte[] filler;

	/**
	 * Constructor, assigns the variables of the header with the data from the
	 * <tt>ByteBuffer</tt>.
	 * 
	 * @param bytes <tt>ByteBuffer</tt> containing PCX file header data.
	 */
	PcxFileHeader(ByteBuffer bytes) {

		manufacturer = bytes.get();
		if (manufacturer != MANUFACTURER_ZSOFT) {
			throw new UnsupportedFileException("Unsupported PCX file data found in the header");
		}
		version = bytes.get();
		if (version != VERSION_PCP25 && version != VERSION_PCP28_PAL && version != VERSION_PCP28_NO_PAL &&
			version != VERSION_PCP4WIN && version != VERSION_PCPPLUS) {
			throw new UnsupportedFileException("Unsupported PCX file data found in the header");
		}
		encoding = bytes.get();
		if (encoding != ENCODING_RLE) {
			throw new UnsupportedFileException("Unsupported PCX file data found in the header");
		}
		bitsperpixel = bytes.get();
		if (bitsperpixel != BPP_8) {
			throw new UnsupportedFileException("Only 8-bit (256 colour) PCX files are currently supported");
		}

		xmin = bytes.getShort();
		ymin = bytes.getShort();
		xmax = bytes.getShort();
		ymax = bytes.getShort();
		hdpi = bytes.getShort();
		vdpi = bytes.getShort();

		palette = new byte[PALETTE_SIZE];
		bytes.get(palette);

		reserved     = bytes.get();
		planes       = bytes.get();
		bytesperline = bytes.getShort();
		paletteinfo  = bytes.getShort();
	}
}
