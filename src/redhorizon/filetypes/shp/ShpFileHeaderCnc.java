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
import java.nio.ByteOrder;

/**
 * Representation of a SHP file's header format.
 * 
 * @author Emanuel Rabina
 */
public class ShpFileHeaderCnc extends ShpFileHeader {

	static final int HEADER_SIZE = 14;

	short unknown1;
	short unknown2;
	final short width;
	final short height;
	final int unknown3;

	/**
	 * Constructor, obtains image parameters from the <tt>ByteBuffer</tt>.
	 * 
	 * @param bytes <tt>ByteBuffer</tt> containing SHP file header data.
	 */
	ShpFileHeaderCnc(ByteBuffer bytes) {
		super((short) (bytes.getShort() & 0xFFFF));

		bytes.getInt();
		width    = bytes.getShort();
		height   = bytes.getShort();
		unknown3 = bytes.getInt();
	}

	/**
	 * Constructor, uses the given parameters to fill-in this header.
	 * 
	 * @param numimages Number of images in the SHP file.
	 * @param width		Width of each image in the file.
	 * @param height	Height of each image in the file.
	 */
	ShpFileHeaderCnc(short numimages, short width, short height) {

		super(numimages);

		this.width  = width;
		this.height = height;

		unknown1 = 0;
		unknown2 = 0;
		unknown3 = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	ByteBuffer toByteBuffer() {

		ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
		header.putShort(numimages)
			  .putShort(unknown1)
			  .putShort(unknown2)
			  .putShort(width)
			  .putShort(height)
			  .putInt(unknown3)
			  .rewind();

		return header;
	}
}
