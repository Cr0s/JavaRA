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

import redhorizon.filetypes.UnsupportedFileException;

import java.nio.ByteBuffer;

/**
 * Representation of a CPS file's header.
 * 
 * @author Emanuel Rabina
 */
public class CpsFileHeader {

	// Header constants
	static final int   HEADER_SIZE  = 10;
	static final short IMAGE_SIZE   = (short)0xfa00;
	static final short PALETTE_SIZE = 0x0300;

	static final short COMPRESSION_LBM      = 0x0003;	// From WestPak2, don't know what this is
	static final short COMPRESSION_FORMAT80 = 0x0004;

	private static final short UNKNOWN_VAL = 0x0000;

	final short filesize;
	final short compression;
	final short imagesize;
	final short unknown;
	final short palettesize;

	/**
	 * Constructor, assigns the variables of the header with the data in the
	 * buffer.
	 * 
	 * @param buffer {@link ByteBuffer} containing the CPS file's header.
	 */
	CpsFileHeader(ByteBuffer buffer) {

		filesize    = buffer.getShort();
		compression = buffer.getShort();
		imagesize   = buffer.getShort();
		if (imagesize != IMAGE_SIZE) {
			throw new UnsupportedFileException("CPS file image size isn't 0xFA00 (320x200)");
		}
		unknown     = buffer.getShort();
		palettesize = buffer.getShort();
		if (palettesize != 0 && palettesize != PALETTE_SIZE) {
			throw new UnsupportedFileException("CPS file palette isn't 0 or 0x0300 (768) bytes");
		}
	}

	/**
	 * Constructor, assigns the variables of the header.  The constant fields
	 * are assigned their constant values.
	 * 
	 * @param filesize	  The size of the file - 2.
	 * @param palettesize 768 for a 256-coloud palette, or 0 if no palette.
	 */
	CpsFileHeader(short filesize, short palettesize) {

		this.filesize    = filesize;
		this.compression = COMPRESSION_FORMAT80;
		this.imagesize   = IMAGE_SIZE;
		this.unknown     = UNKNOWN_VAL;
		this.palettesize = palettesize;
	}

	/**
	 * Returns this header in it's {@link ByteBuffer} representation.
	 * 
	 * @return {@link ByteBuffer} containing this header's data.
	 */
	ByteBuffer toByteBuffer() {

		ByteBuffer header = ByteBuffer.allocate(CpsFileHeader.HEADER_SIZE);
		header.putShort(filesize);
		header.putShort(compression);
		header.putShort(imagesize);
		header.putShort(unknown);
		header.putShort(palettesize);
		header.rewind();

		return header;
	}
}
