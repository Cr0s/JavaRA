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

import java.nio.ByteBuffer;

/**
 * Representation of a Dune 2 WSA file header.
 * 
 * @author Emanuel Rabina
 */
public class WsaFileHeaderDune2 extends WsaFileHeader {

	static final int HEADER_SIZE = 10;

	final short width, height;
	final int delta;

	/**
	 * Constructor, fills-out the header from the <tt>ByteBuffer</tt>.
	 * 
	 * @param bytes <tt>ByteBuffer</tt> to the Dune 2 WSA file.
	 */
	WsaFileHeaderDune2(ByteBuffer bytes) {

		super(bytes.getShort());
		width  = bytes.getShort();
		height = bytes.getShort();
		delta  = bytes.getInt();
	}

	/**
	 * Constructor, uses the given parameters to complete this header.
	 * 
	 * @param numframes Number of frames in the file.
	 * @param width		Width of each frame.
	 * @param height	Height of each frame.
	 * @param delta		Animation delta.
	 */
	WsaFileHeaderDune2(short numframes, short width, short height, int delta) {

		super(numframes);
		this.width  = width;
		this.height = height;
		this.delta  = delta;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	ByteBuffer toByteBuffer() {

		ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
		header.putShort(numframes);
		header.putShort(width);
		header.putShort(height);
		header.putInt(delta);
		header.rewind();
		return header;
	}
}
