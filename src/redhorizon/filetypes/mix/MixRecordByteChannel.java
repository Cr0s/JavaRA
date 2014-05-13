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

package redhorizon.filetypes.mix;

import redhorizon.utilities.channels.AbstractDuplicateReadOnlyByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * Byte channel over a record in the mix file.
 * 
 * @author Emanuel Rabina
 */
public class MixRecordByteChannel extends AbstractDuplicateReadOnlyByteChannel {

	private final FileChannel filechannel;
	private final long lowerbound;
	private final long upperbound;

	/**
	 * Constructor, creates a byte channel backed by the mix file's file
	 * channel.
	 * 
	 * @param filechannel
	 * @param lowerbound
	 * @param size
	 */
	MixRecordByteChannel(FileChannel filechannel, int lowerbound, int size) {

		this.filechannel = filechannel;
		this.lowerbound  = lowerbound;
		this.upperbound  = lowerbound + size;

		position = lowerbound;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isOpenImpl() {

		return filechannel.isOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(ByteBuffer dst) {
		dst.order(ByteOrder.LITTLE_ENDIAN);
		
		int remaining = (int)(upperbound - position);
		if (remaining == 0) {
		return -1;
		}
		int oldlimit = dst.limit();
		dst.limit(dst.position() + Math.min(dst.remaining(), remaining));
		int read = 0;
		try {
			read = filechannel.read(dst, position);
			position += read;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dst.limit(oldlimit);
		return read;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long size() {

		return upperbound - lowerbound;
	}
}
