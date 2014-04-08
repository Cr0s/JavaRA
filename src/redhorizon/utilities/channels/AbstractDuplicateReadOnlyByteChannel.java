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

import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * Most of the duplicate channels are read-only, so this implements many of the
 * write operations to throw the {@link NonWritableChannelException}.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractDuplicateReadOnlyByteChannel extends AbstractDuplicateByteChannel {

	/**
	 * Create a duplicate read-only channel.
	 */
	protected AbstractDuplicateReadOnlyByteChannel() {
	}

	/**
	 * Throws a {@link NonWritableChannelException}.
	 */
	@Override
	public final SeekableByteChannel truncate(long size) {

		throw new NonWritableChannelException();
	}

	/**
	 * Throws a {@link NonWritableChannelException}.
	 */
	@Override
	public final int write(ByteBuffer src) {

		throw new NonWritableChannelException();
	}
}
