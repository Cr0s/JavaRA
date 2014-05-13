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
 * Yet another Base64 decoder.  (When I first wrote this I didn't know the JDK
 * had an internal one, nor did I know about existing libraries that did the
 * same thing.)
 *
 * @author Emanuel Rabina
 */
public class Base64 implements Decoder {

	// Base-64 table as used by the *pack sections of RA's maps
	private static final String BASE64_ALPHABET =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra) {

		// Find out how many bits to cut from the end
		int cullbits = 0;
		for (int i = source.limit() - 1; source.get(i) == (byte)'='; i--) {
			cullbits += 2;
			source.limit(source.limit() - 1);
		}

		// Construct the binary values
		for (int i = 0; source.limit() - i >= 4; i += 4) {
			byte a = decodeBase64CharVal(source.get());
			byte b = decodeBase64CharVal(source.get());
			byte c = decodeBase64CharVal(source.get());
			byte d = decodeBase64CharVal(source.get());

			dest.put((byte)(a << 2 | b >>> 4));
			dest.put((byte)(b << 4 | c >>> 2));
			dest.put((byte)(c << 6 | d));
		}

		// Pick-up any leftovers
		int diff = source.limit() - source.position();
		if (cullbits == 4 && diff == 2) {
			byte a = decodeBase64CharVal(source.get());
			byte b = decodeBase64CharVal(source.get());

			dest.put((byte)(a << 2 | b >>> 4));
		}
		else if (cullbits == 2 && diff == 3) {
			byte a = decodeBase64CharVal(source.get());
			byte b = decodeBase64CharVal(source.get());
			byte c = decodeBase64CharVal(source.get());

			dest.put((byte)(a << 2 | b >>> 4));
			dest.put((byte)(b << 4 | c >>> 2));
		}
		dest.flip();
	}

	/**
	 * Gets the Base-64 value assigned to the characters used in the table.
	 * 
	 * @param charval The character whose value is to be found.
	 * @return The value assigned to that character.
	 */
	private static byte decodeBase64CharVal(byte charval) {

		for (int i = 0; i < BASE64_ALPHABET.length(); i++) {
			if (charval == (byte)BASE64_ALPHABET.charAt(i)) {
				return (byte)i;
			}
		}
		return (byte)0x80;
	}
}
