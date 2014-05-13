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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * A class similar to what the {@link ByteBuffer#duplicate()} method does in
 * that this class creates a new {@link SeekableByteChannel} instance over an
 * existing seekable channel type so that it shares the same data but maintains
 * its own position and limit, so as not to mess with the original's
 * position/limit.
 * <p>
 * While closing this channel has no effect, closing the original will close
 * this duplicate.
 * 
 * @author Emanuel Rabina
 */
public abstract class AbstractDuplicateByteChannel implements SeekableByteChannel {

	protected long position;
	protected boolean closed;

	/**
	 * Create a duplicate channel.
	 */
	protected AbstractDuplicateByteChannel() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void close() {

		closed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean isOpen() {

		return !closed && isOpenImpl();
	}

	/**
	 * Return whether the source is open.
	 * 
	 * @return <tt>true</tt> if the underlying source channel is open.
	 */
	protected abstract boolean isOpenImpl();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long position() {

		if (!isOpen()) {
			throw new RuntimeException(new ClosedChannelException());
		}
		return position;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final SeekableByteChannel position(long newposition) {

		if (newposition < 0) {
			throw new IllegalArgumentException();
		}
		if (!isOpen()) {
			throw new RuntimeException(new ClosedChannelException());
		}
		position = newposition;
		return this;
	}
}
