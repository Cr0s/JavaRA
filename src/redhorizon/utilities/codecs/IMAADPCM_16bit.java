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
 * Decoder for the 16-bit IMA-ADPCM encoding scheme.
 * 
 * @author Emanuel Rabina
 */
public class IMAADPCM_16bit implements Decoder {

	// IMA-ADPCM adjustment table
	private static final int[] IMA_ADJUST_TABLE = {
		-1, -1, -1, -1, 2, 4, 6, 8
	};

	// IMA-ADPCM step table
	private static final int[] IMA_STEP_TABLE = {
		    7,     8,     9,    10,    11,    12,     13,    14,    16,
		   17,    19,    21,    23,    25,    28,     31,    34,    37,
		   41,    45,    50,    55,    60,    66,     73,    80,    88,
		   97,   107,   118,   130,   143,   157,    173,   190,   209,
		  230,   253,   279,   307,   337,   371,    408,   449,   494,
		  544,   598,   658,   724,   796,   876,    963,  1060,  1166,
		 1282,  1411,  1552,  1707,  1878,  2066,   2272,  2499,  2749,
		 3024,  3327,  3660,  4026,  4428,  4871,   5358,  5894,  6484,
		 7132,  7845,  8630,  9493, 10442, 11487,  12635, 13899, 15289,
		16818, 18500, 20350, 22385, 24623, 27086,  29794, 32767
	};

	/**
	 * {@inheritDoc}
	 * 
	 * @param extra 2 additional buffers, the first holding the last decoded
	 * 				PCM index value, the second holding the last decoded PCM
	 * 				sample value.  The buffer values are then updated after
	 * 				decoding.
	 */
	@Override
	public void decode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra) {

		int index  = extra[0].getInt(0);
		int sample = extra[1].getInt(0);

		// Until all the compressed data has been decompressed
		for (int sampleindex = 0; sampleindex < source.limit() << 1; sampleindex++) {

			// The 4-bit command
			byte code = source.get(sampleindex >> 1);
			code = (sampleindex % 2 == 1) ? (byte)(code >>> 4) : (byte)(code & 0x0f);

			int step = IMA_STEP_TABLE[index];
			int delta = step >>> 3;

			// Expansion of the multiplication in the original pseudo code
			if ((code & 0x01) != 0) {
				delta += step >>> 2;
			}
			if ((code & 0x02) != 0) {
				delta += step >>> 1;
			}
			if ((code & 0x04) != 0) {
				delta += step;
			}

			// Sign bit = 1
			if ((code & 0x08) != 0) {
				sample -= delta;
				sample = Math.max(sample, -32768);
			}
			// Sign bit = 0
			else {
				sample += delta;
				sample = Math.min(sample, 32767);
			}

			// Save result to destination buffer
			dest.putShort((short)sample);

			// Index/Step adjustments
			index += IMA_ADJUST_TABLE[code & 0x07];
			index = Math.min(Math.max(index, 0), 88);
		}
		dest.flip();

		extra[0].putInt(0, index);
		extra[1].putInt(0, sample);
	}
}
