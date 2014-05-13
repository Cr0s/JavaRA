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
 * Interface for classes that can encode information.
 *
 * @author Emanuel Rabina
 */
public interface Encoder {

	/**
	 * Encodes the information in the <tt>source</tt> buffer, putting it into
	 * the <tt>dest</tt> buffer.
	 * 
	 * @param source
	 * @param dest
	 * @param extra	 Optional buffers to pass to the encoder.  Used in some
	 * 				 encoders that require multiple source buffers to produce an
	 * 				 encoded result (eg: XOR'ed images).
	 */
	public void encode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra);
}
