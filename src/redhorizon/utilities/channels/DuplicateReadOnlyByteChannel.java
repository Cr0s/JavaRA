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

package redhorizon.utilities.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * A class similar to what the {@link ByteBuffer#duplicate()} method does in
 * that this class creates a new {@link SeekableByteChannel} instance over an
 * existing {@link SeekableByteChannel} so that it shares the same data but
 * maintains its own position and limit, so as not to mess with the original
 * byte channel's position/limit.
 * <p>
 * While closing this channel has no effect, closing the original will close
 * this duplicate.
 * 
 * @author Emanuel Rabina
 */
public class DuplicateReadOnlyByteChannel extends AbstractDuplicateReadOnlyByteChannel {

	private final SeekableByteChannel bytechannel;

	/**
	 * Constructor, builds a seekable byte channel over an existing one.
	 * 
	 * @param bytechannel
	 */
	public DuplicateReadOnlyByteChannel(SeekableByteChannel bytechannel) {

		this.bytechannel = bytechannel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isOpenImpl() {

		return bytechannel.isOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(ByteBuffer dst) {

		try {
			return bytechannel.read(dst);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size() {

		try {
			return bytechannel.size();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
}
