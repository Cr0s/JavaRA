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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

/**
 * Utility class for common buffer operations.
 * 
 * @author Emanuel Rabina.
 */
public class BufferUtility {

	/**
	 * Hidden default constructor, as this class is only ever meant to be used
	 * statically.
	 */
	private BufferUtility() {
	}

	/**
	 * Read a byte channel until all input is exhausted, returning the results
	 * in a byte buffer.
	 * 
	 * @param bytechannel
	 * @return All remaining data in the stream.
	 */
	public static ByteBuffer readRemaining(ReadableByteChannel bytechannel) {

		ArrayList<ByteBuffer> bytes = new ArrayList<>();
		int size = 0;
		while (true) {
			ByteBuffer bytedata = ByteBuffer.allocate(1024);
			int read = -1;
			try {
				read = bytechannel.read(bytedata);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (read > 0) {
				size += read;
				bytes.add((ByteBuffer)bytedata.flip());
			}
			if (read != 1024) {
				break;
			}
		}

		ByteBuffer data = ByteBuffer.allocate(size);
		for (ByteBuffer bytedata: bytes) {
			data.put(bytedata);
		}
		data.rewind();
		return data;
	}
}
