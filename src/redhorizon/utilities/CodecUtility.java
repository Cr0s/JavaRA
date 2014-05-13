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

package redhorizon.utilities;

import redhorizon.utilities.codecs.Base64;
import redhorizon.utilities.codecs.Format2;
import redhorizon.utilities.codecs.Format40;
import redhorizon.utilities.codecs.Format80;
import redhorizon.utilities.codecs.IMAADPCM_16bit;
import redhorizon.utilities.codecs.PackData;
import redhorizon.utilities.codecs.RunLengthEncoding;
import redhorizon.utilities.codecs.WSADPCM_8bit;

import java.nio.ByteBuffer;

/**
 * Utility class to call upon the various encoders/decoders used throughout the
 * project.
 * <p>
 * Encoding was not normally in the scope of this program, but a request for
 * some modern SHP file encoding led to it's creation.  That, and current SHP
 * encoders have restrictions which have since been loosened through .exe hacks
 * and other advanced editing, or they just don't work as advertised.
 * <p>
 * Much of the C&C-specific methods (Format20, Format40, Format80,
 * 16bitIMAADPCM) have been adapted from code written by Vladan Bato.  Credit
 * also goes to Asatur V. Nazarian for the 8bitWSADPCM code.  Format2 is
 * something I came up with based on one rare document and my own observations.
 * <p>
 * All file format documentation can be found in the <tt>Documentation/File Formats</tt>
 * directory.
 * 
 * @author Emanuel Rabina
 */
public class CodecUtility {

	// Generic decoders
	private static final Base64   base64      = new Base64();
	private static final PackData overlaypack = new PackData(2);
	private static final PackData mappack     = new PackData(6);
	private static final RunLengthEncoding rle67 = new RunLengthEncoding((byte)0xc0);

	// Audio decoders
	private static final WSADPCM_8bit   wsadpcm8bit   = new WSADPCM_8bit();
	private static final IMAADPCM_16bit imaadpcm16bit = new IMAADPCM_16bit();

	// Image decoders
	private static final Format2  format2  = new Format2();
	private static final Format40 format40 = new Format40();
	private static final Format80 format80 = new Format80();

	/**
	 * Hidden default constructor, as this class is only ever meant to be used
	 * statically.
	 */
	private CodecUtility() {
	}

	/**
	 * Decompresses AUD file data using Westwood's proprietary 8-bit ADPCM
	 * decompression.  This is used for very few sound samples in both Red Alert
	 * and Tiberium Dawn, most notably the infantry death sounds.
	 * <p>
	 * This decompression technique is only for mono sound data.
	 * <p>
	 * NOTE: does this method even get used?  I haven't found a single instance
	 *       of an 8-bit file.
	 * 
	 * @param source Original compressed audio data.
	 * @param dest	 Buffer to store the uncompressed data.
	 */
	public static void decode8bitWSADPCM(ByteBuffer source, ByteBuffer dest) {

		wsadpcm8bit.decode(source, dest);
	}

	/**
	 * Decompresses AUD file data using IMA-ADPCM decompression.  This
	 * decompression technique is used with Red Alert's and Tiberium Dawn's
	 * 16-bit audio files.  For the 8-bit, a proprietary format is used and can
	 * be decoded/decompressed using
	 * {@link #decode8bitWSADPCM(ByteBuffer,ByteBuffer)}.
	 * 
	 * @param source Original compressed audio data.
	 * @param dest	 Buffer to store the uncompressed data.
	 * @param update 2-<tt>int</tt> array, containing the latest index and
	 * 				 sample values respectively.
	 */
	public static void decode16bitIMAADPCM(ByteBuffer source, ByteBuffer dest, int[] update) {

		ByteBuffer index  = ByteBuffer.allocate(4).putInt(0, update[0]);
		ByteBuffer sample = ByteBuffer.allocate(4).putInt(0, update[1]);

		imaadpcm16bit.decode(source, dest, index, sample);

		update[0] = index.getInt(0);
		update[1] = sample.getInt(0);
	}

	/**
	 * Decodes base64 data into standard binary.
	 * 
	 * @param source Original encoded data.
	 * @param dest	 Buffer to store the decoded data.
	 */
	public static void decodeBase64(ByteBuffer source, ByteBuffer dest) {

		base64.decode(source, dest);
	}

	/**
	 * Decodes Format2-encoded data.
	 * 
	 * @param source Original encoded data.
	 * @param dest	 Buffer to store the decoded data.
	 */
	public static void decodeFormat2(ByteBuffer source, ByteBuffer dest) {

		format2.decode(source, dest);
	}

	/**
	 * Decodes Format20-encoded data.
	 * <p>
	 * In the context of images, Format20 is just Format40 XOR'ed over the
	 * previous image.  This method is merely a call to the Format40 decoding
	 * method, but kept so that the differences between format types can be
	 * upheld.
	 * 
	 * @param source Original encoded data.
	 * @param base	 Bytes from the frame the new data is based upon.
	 * @param dest	 Buffer to store the decoded data.
	 */
	public static void decodeFormat20(ByteBuffer source, ByteBuffer base, ByteBuffer dest) {

		decodeFormat40(source, dest, base);
	}

	/**
	 * Decodes Format40-encoded data.
	 * 
	 * @param source Original encoded data.
	 * @param dest	 Buffer to store the decoded data.
	 * @param base	 Bytes from the frame the new data is based upon.
	 */
	public static void decodeFormat40(ByteBuffer source, ByteBuffer dest, ByteBuffer base) {

		format40.decode(source, dest, base);
	}

	/**
	 * Decodes Format80-encoded data.
	 * 
	 * @param source Original compressed image bytes.
	 * @param dest	 Buffer to store the uncompressed image bytes.
	 */
	public static void decodeFormat80(ByteBuffer source, ByteBuffer dest) {

		format80.decode(source, dest);
	}

	/**
	 * Decodes the [MapPack] section of a scenario file, converting the base-64
	 * data to binary, and then decompressing it to the full map size (48k).
	 * 
	 * @param source Original encoded data as per the characters in the map
	 * 				 file.  These chars are intended to be 8-bit, instead of
	 * 				 Java's 16-bit chars, so conversion will need to be done
	 * 				 before-hand.
	 * @param dest	 48k destination buffer containing full uncompressed binary
	 * 				 data on the [MapPack] section.
	 */
	public static void decodeMapPack(ByteBuffer source, ByteBuffer dest) {

		mappack.decode(source, dest);
	}

	/**
	 * Decodes the [OverlayPack] section of a scenario file, converting the
	 * base-64 data to binary, and then decompressing it to the full overlay
	 * size (16k).
	 * 
	 * @param source Original encoded data as per the characters in the map
	 * 				 file.  These chars are intended to be 8-bit, instead of
	 * 				 Java's 16-bit chars, so conversion will need to be done
	 * 				 before-hand.
	 * @param dest	 16k destination buffer containing full uncompressed binary
	 * 				 data on the [OverlayPack] section.
	 */
	public static void decodeOverlayPack(ByteBuffer source, ByteBuffer dest) {

		overlaypack.decode(source, dest);
	}

	/**
	 * Decodes a section of data which has been encoded using the run-length
	 * encoding scheme.  The 67 variant of RLE is one where the count byte is
	 * denoted by bits 6 & 7 set to 1.
	 * 
	 * @param source Original encoded data.
	 * @param dest	 Buffer to store the uncompressed data.
	 */
	public static void decodeRLE67(ByteBuffer source, ByteBuffer dest) {

		rle67.decode(source, dest);
	}

	/**
	 * Encodes a series of bytes using the Format2 algorithm.
	 *
	 * @param source Original raw data.
	 * @param dest   Buffer to store the encoded data.
	 */
	public static void encodeFormat2(ByteBuffer source, ByteBuffer dest) {

		format2.encode(source, dest);
	}

	/**
	 * Encodes a series of bytes using the Format40 algorithm.
	 * 
	 * @param source Original raw data.
	 * @param dest   Buffer to store the encoded data.
	 * @param base	 Bytes from the frame the new data must be based upon.
	 */
	public static void encodeFormat40(ByteBuffer source, ByteBuffer dest, ByteBuffer base) {

		format40.encode(source, dest, base);
	}

	/**
	 * Encodes a series of bytes using the Format80 algorithm.
	 *
	 * @param source Original raw image bytes.
	 * @param dest   Buffer to store the encoded image bytes.
	 */
	public static void encodeFormat80(ByteBuffer source, ByteBuffer dest) {

		format80.encode(source, dest);
	}
}
