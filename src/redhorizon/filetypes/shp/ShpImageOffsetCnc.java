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
 * Representation of an offset record found in SHP files.  It turns out that not
 * all SHP files are just straight images in their own right, but some form of
 * 'difference' data (think video difference encoding) which has to be matched
 * to a certain key image to obtain the full frame.  The link to which frame has
 * to be matched with which, is found in an offset.
 * 
 * @author Emanuel Rabina
 */
public class ShpImageOffsetCnc {

	static final int OFFSET_SIZE = 8;

	final int offset;
	byte offsetformat;
	final int refoff;
	final byte refoffformat;

	/**
	 * Constructor, creates an offset record from the data at the current
	 * {@link ByteBuffer}.
	 * 
	 * @param bytes Data at an offset for the SHP file.
	 */
	ShpImageOffsetCnc(ByteBuffer bytes) {
		bytes.order(ByteOrder.LITTLE_ENDIAN);
		
		byte[] b = new byte[4];
	
		bytes.get(b);
		offsetformat = (byte)(b[3] & 0xFF);
		offset = ((b[0] & 0xFF) <<  0) |
		         ((b[1] & 0xFF) <<  8) |
		         ((b[2] & 0xFF) << 16);

		bytes.get(b);
		refoffformat = (byte)(b[3] & 0xFF);
		refoff = ((b[0] & 0xFF) <<  0) |
		         ((b[1] & 0xFF) <<  8) |
		         ((b[2] & 0xFF) << 16);
	}

	/**
	 * Constructor, creates an offset record with the given parameters.
	 * 
	 * @param offset	   Image offset value.
	 * @param offsetformat Format of the image at that offset.
	 * @param refoff	   Reference offset value.
	 * @param refoffformat Format of the image at the reference offset.
	 */
	ShpImageOffsetCnc(int offset, byte offsetformat, int refoff, byte refoffformat) {

		this.offset       = offset;
		this.offsetformat = offsetformat;
		this.refoff       = refoff;
		this.refoffformat = refoffformat;
	}

	/**
	 * Returns this offset in it's <tt>ByteBuffer</tt> representation.
	 * 
	 * @return <tt>ByteBuffer</tt> containing this offset's data.
	 */
	ByteBuffer toByteBuffer() {

		ByteBuffer offsetbytes = ByteBuffer.allocate(OFFSET_SIZE);
		offsetbytes.putInt(offset | (offsetformat << 24))
				   .putInt(refoff | (refoffformat << 24))
				   .rewind();

		return offsetbytes;
	}
}
