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

/**
 * Representation of the VQA file's offset data.
 * 
 * @author Emanuel Rabina
 */
public class VqaFrameOffset {

	static final int FRAME_OFFSET_SIZE = 4;

	final int offset;

	/**
	 * Constructor, creates an offset record from the given {@link ByteBuffer}.
	 * 
	 * @param bytes {@link ByteBuffer} to a VQA offset record.
	 */
	VqaFrameOffset(ByteBuffer bytes) {

		offset = (bytes.getInt() & 0x3fffffff) << 1;
	}
}
