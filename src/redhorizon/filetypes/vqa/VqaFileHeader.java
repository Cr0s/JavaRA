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

package redhorizon.filetypes.vqa;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Representation of a VQA header.  Contains information on the VQA file,
 * sounds, video data, amongst other things.
 * 
 * @author Emanuel Rabina
 */
public class VqaFileHeader {

	static final int HEADER_SIZE = 54;

	final char[] signature;
	final int startpos;
	final short version;
	final short flags;
	final short numframes;
	final short width;
	final short height;
	final byte blockwidth;
	final byte blockheight;
	final byte framerate;
	final byte cbparts;
	final short numcolours;
	final short maxblocks;
	final short unknown1;
	final int unknown2;
	final short frequency;
	final byte channels;
	final byte bits;
	final byte[] unknown3;

	final VqaFrameOffset[] offsets;

	/**
	 * Constructor, obtains parameters on the video from the given
	 * {@link ByteBuffer}.
	 * 
	 * @param bytes Buffer containing the VQA file's header data.
	 */
	VqaFileHeader(ByteBuffer bytes) {

		bytes.limit(8);
		signature   = Charset.defaultCharset().decode(bytes).array();
		bytes.limit(bytes.capacity());
		startpos    = Integer.reverseBytes(bytes.getInt());
		version     = bytes.getShort();
		flags       = bytes.getShort();
		numframes   = bytes.getShort();
		width       = bytes.getShort();
		height      = bytes.getShort();
		blockwidth  = bytes.get();
		blockheight = bytes.get();
		framerate   = bytes.get();
		cbparts     = bytes.get();
		numcolours  = bytes.getShort();
		maxblocks   = bytes.getShort();
		unknown1    = bytes.getShort();
		unknown2    = bytes.getInt();
		frequency   = bytes.getShort();
		channels    = bytes.get();
		bits        = bytes.get();
		unknown3    = new byte[14];
		bytes.get(unknown3);
		offsets     = new VqaFrameOffset[numframes];
	}
}
