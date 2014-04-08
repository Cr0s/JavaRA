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
 * Representation of a Dune 2 SHP file header.
 * 
 * @author Emanuel Rabina
 */
public class ShpFileHeaderDune2 extends ShpFileHeader {

	private static final int HEADER_SIZE = 2;

	static final int IMAGE_OFFSET_2 = 2;
	static final int IMAGE_OFFSET_4 = 4;

	final int offsetsize;

	/**
	 * Constructor, fills-out the header from the <tt>ByteBuffer</tt>.
	 * 
	 * @param bytes {@link ByteBuffer} containing shp file header data.
	 */
	ShpFileHeaderDune2(ByteBuffer bytes) {

		super(bytes.getShort());

		// Discover the offset size
		offsetsize = bytes.get(4) != 0 ? IMAGE_OFFSET_2 : IMAGE_OFFSET_4;
	}

	/**
	 * Constructor, creates a header with the given number of images and an
	 * offset type of {@link #IMAGE_OFFSET_4}.
	 * 
	 * @param numimages Number of images in the file.
	 */
	ShpFileHeaderDune2(short numimages) {

		super(numimages);
		offsetsize = IMAGE_OFFSET_4;
	}

	/**
	 * Return the size of the file header.
	 * 
	 * @return File header size (constant).
	 */
	static int size() {

		return HEADER_SIZE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	ByteBuffer toByteBuffer() {

		ByteBuffer header = ByteBuffer.allocate(2);
		header.putShort(numimages);
		header.rewind();
		return header;
	}
}
