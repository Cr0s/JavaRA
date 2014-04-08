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

package redhorizon.utilities.codecs;

import java.nio.ByteBuffer;

/**
 * Decoder for the [*Pack] sections found inside CNC map file data.
 *
 * @author Emanuel Rabina
 */
public class PackData implements Decoder {

	private final Base64 base64 = new Base64();
	private final Format80 format80 = new Format80();

	private final int chunks;

	/**
	 * Constructor, create a pack data decoder to expect the given number of
	 * chunks during decoding.
	 * 
	 * @param chunks
	 */
	public PackData(int chunks) {

		this.chunks = chunks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra) {

		// Decode base64 data into standard binary
		ByteBuffer mapbytes2 = ByteBuffer.allocate(8192 * chunks);
		base64.decode(source, mapbytes2);

		// Decode pack data, 'chunks' number of chunks
		ByteBuffer[] mapchunks = new ByteBuffer[chunks];
		for (int i = 0; i < chunks; i++) {
			mapchunks[i] = ByteBuffer.allocate(8192);
		}

		for (int i = 0, pos = 0; i < chunks; i++) {

			// Get following chunk size, skip 0x20 (unknown) byte
			int a = mapbytes2.get() & 0xff;
			int b = mapbytes2.get() & 0xff;
			int c = mapbytes2.get() & 0xff;
					mapbytes2.position(mapbytes2.position() + 1);
			int chunksize = (c << 16) | (b << 8) | a;

			// Decode that chunk, put it into one of the buffers in the array
			format80.decode(mapbytes2, mapchunks[i]);
			pos += 4 + chunksize;
			mapbytes2.position(pos);
		}

		// Collate chunks into total map data
		for (ByteBuffer mapchunk: mapchunks) {
			dest.put(mapchunk);
		}
		dest.rewind();
	}
}
