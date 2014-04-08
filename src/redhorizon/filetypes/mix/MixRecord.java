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

import java.nio.ByteBuffer;

import redhorizon.filetypes.ArchiveFileEntry;

/**
 * Representation of a Red Alert MIX file index record, found in the header of
 * MIX files to indicate where content can be located within the body.
 * 
 * @author Emanuel Rabina
 */
public class MixRecord implements ArchiveFileEntry, Comparable<MixRecord> {

	static final int RECORD_SIZE = 12;

	String name;	// Name cannot be determined initially
	final long id;
	final long offset;
	final long length;

	/**
	 * Constructor, assigns the ID, offset, and length of this entry from the
	 * current byte channel.
	 * 
	 * @param bytes Buffer containing the entry bytes.
	 */
	MixRecord(ByteBuffer bytes) {
		id     = bytes.getInt();
		offset = bytes.getInt();
		length = bytes.getInt();
	}
	/**
	 * Compares this record to the other, returns negative, zero, or positive if
	 * this record's ID is less than, equal to, or greater than the one being
	 * compared to.
	 * 
	 * @param other The other <tt>MixRecord</tt> to compare with.
	 * @return -1, 0, 1 :: less-than, equal to, greater than.
	 */
	@Override
	public int compareTo(MixRecord other) {

		long thisid = this.id;
		long otherid = other.id;

		if (thisid < otherid) {
			return -1;
		}
		else if (thisid > otherid) {
			return 1;
		}
		else {
			return 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {

		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSize() {

		return length;
	}
}
