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

package redhorizon.filetypes.shp;

import java.nio.ByteBuffer;

/**
 * Representation of the Dune 2 SHP image header (different from the file
 * header), which contains data on the image it references.
 * 
 * @author Emanuel Rabina
 */
public class ShpImageHeaderDune2 {

	// The various known flags
	static final short FLAG_LOOKUP_TABLE                 = 0b00000001;
	static final short FLAG_NO_COMPRESSION               = 0b00000010;
	static final short FLAG_VARIABLE_LENGTH_LOOKUP_TABLE = 0b00000100;

	final short  flags;
	final byte   slices;
	final short  width;
	final byte   height;
	final short  filesize;
	final short  datasize;
	final byte[] lookuptable;

	/**
	 * Constructor, creates a Dune 2 shp file image header.
	 * 
	 * @param bytes {@link ByteBuffer} containing Dune 2 image offset data.
	 */
	ShpImageHeaderDune2(ByteBuffer bytes) {

		this.flags    = bytes.getShort();
		this.slices   = bytes.get();
		this.width    = bytes.getShort();
		this.height   = bytes.get();
		this.filesize = bytes.getShort();
		this.datasize = bytes.getShort();

		// Optional lookup table
		if (hasLookupTable()) {
			lookuptable = hasVariableLengthLookupTable() ? new byte[bytes.get() & 0xff] : new byte[16];
			for (int i = 0; i < lookuptable.length; i++) {
				lookuptable[i] = bytes.get();
			}
		}
		else {
			lookuptable = null;
		}
	}

	/**
	 * Constructor, create an image header based on the given information.
	 * 
	 * @param width			Image width.
	 * @param height		Image height.
	 * @param lookuptable	Internal lookup table.
	 * @param imagefilesize Size of the compressed image data.
	 * @param format2size	Size of the Format2 compressed image data.
	 */
	ShpImageHeaderDune2(short width, byte height, byte[] lookuptable, int imagefilesize, int format2size) {

		this.width       = width;
		this.height      = height;
		this.lookuptable = lookuptable;

		this.flags = (short)(
				(lookuptable != null ? FLAG_LOOKUP_TABLE : 0) |
				FLAG_NO_COMPRESSION |
				(lookuptable != null && lookuptable.length != 16 ? FLAG_VARIABLE_LENGTH_LOOKUP_TABLE : 0)
		);
		this.slices   = height;
		this.filesize = (short)(size() + imagefilesize);
		this.datasize = (short)format2size;
	}

	/**
	 * Return whether or not the image data contains a lookup table (flag 1 is
	 * set).
	 * 
	 * @return <tt>true</tt> if the image data contains a lookup table.
	 */
	boolean hasLookupTable() {

		return (flags & FLAG_LOOKUP_TABLE) == FLAG_LOOKUP_TABLE; 
	}

	/**
	 * Return whether or not the lookup table in the image data is of a variable
	 * length (both flags 3 and 1 are set).
	 * 
	 * @return <tt>true</tt> if the image data's lookup table is of a variable
	 * 		   length.
	 */
	boolean hasVariableLengthLookupTable() {

		return hasLookupTable() &&
				(flags & FLAG_VARIABLE_LENGTH_LOOKUP_TABLE) == FLAG_VARIABLE_LENGTH_LOOKUP_TABLE;
	}

	/**
	 * Return whether or not the image data is compressed using Format80 (flag 2
	 * is <i>not</i> set).
	 * 
	 * @return <tt>true</tt> if the image data is compressed using Format80
	 * 		   compression.
	 */
	boolean isCompressed() {

		return (flags & FLAG_NO_COMPRESSION) != FLAG_NO_COMPRESSION;
	}

	/**
	 * Returns the size (in bytes) of this image header.
	 * 
	 * @return The size of this image header in bytes.
	 */
	private int size() {

		return hasLookupTable() ? hasVariableLengthLookupTable() ? 11 + lookuptable.length : 26 : 10;
	}

	/**
	 * Returns this image header in it's <tt>ByteBuffer</tt> representation.
	 * 
	 * @return <tt>ByteBuffer</tt> containing this image header data.
	 */
	ByteBuffer toByteBuffer() {

		ByteBuffer imageheader = ByteBuffer.allocate(size());
		imageheader.putShort(flags)
				   .put(slices)
				   .putShort(width)
				   .put(height)
				   .putShort(filesize)
				   .putShort(datasize);
		if (hasLookupTable()) {
			if (hasVariableLengthLookupTable()) {
				imageheader.put((byte)lookuptable.length);
			}
			imageheader.put(lookuptable);
		}
		imageheader.rewind();

		return imageheader;
	}
}
