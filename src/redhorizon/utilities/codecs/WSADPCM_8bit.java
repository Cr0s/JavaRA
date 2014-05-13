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
 * Decoder for the 8-bit Westwood Studios ADPCM encoding scheme.
 * 
 * @author Emanuel Rabina
 */
public class WSADPCM_8bit implements Decoder {

	// WS-ADPCM 2-bit adjustment table
	private static final int[] WS_TABLE_2BIT = {
		-2, -1, 0, 1
	};

	// WS-ADPCM 4-bit adjustment table
	private static final int[] WS_TABLE_4BIT = {
		-9, -8, -6, -5, -4, -3, -2, -1, 0,  1,  2,  3,  4,  5,  6,  8
	};

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra) {

		// Mere copy if chunk is uncompressed
		if (source.limit() == dest.limit()) {
			dest.put(source).rewind();
			return;
		}

		// Decompression
		short sample = 0x80;
		while (dest.hasRemaining()) {

			short input = source.getShort();
			input <<= 2;
			byte command = (byte)(input >>> 8);
			byte count   = (byte)((input & 0xff) >> 2);

			// No compression
			if (command == 2) {

				if ((count & 0x20) != 0) {
					count <<= 3;
					sample += count >> 3;
					dest.put((byte)sample);
				}
				else {
					for ( ; count >= 0; count--) {
						dest.put(source.get());
					}
					sample = dest.get(dest.position() - 1);
					sample &= 0xffff;
				}
			}

			// 2x compression (4-bit -> 8-bit)
			else if (command == 1) {

				for ( ; count >= 0; count--) {
					command = source.get();

					sample += WS_TABLE_4BIT[command & 0x0f];
					sample = (short)Math.min((short)Math.max(sample, 0), 255);
					dest.put((byte)sample);

					sample += WS_TABLE_4BIT[command >>> 4];
					sample = (short)Math.min((short)Math.max(sample, 0), 255);
					dest.put((byte)sample);
				}
			}

			// 4x compression (2-bit -> 8-bit)
			else if (command == 0) {

				for ( ; count >= 0; count--) {
					command = source.get();

					sample += WS_TABLE_2BIT[command & 0x03];
					sample = (short)Math.min((short)Math.max(sample, 0), 255);
					dest.put((byte)sample);

					sample += WS_TABLE_2BIT[(command >>> 2) & 0x03];
					sample = (short)Math.min((short)Math.max(sample, 0), 255);
					dest.put((byte)sample);

					sample += WS_TABLE_2BIT[(command >>> 4) & 0x03];
					sample = (short)Math.min((short)Math.max(sample, 0), 255);
					dest.put((byte)sample);

					sample += WS_TABLE_2BIT[(command >>> 6) & 0x03];
					sample = (short)Math.min((short)Math.max(sample, 0), 255);
					dest.put((byte)sample);
				}
			}

			// Straight copy
			else {
				while (count-- >= 0) {
					dest.put((byte)sample);
				}
			}
		}
		dest.flip();
	}
}
