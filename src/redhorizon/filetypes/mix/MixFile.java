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

import blowfishj.BlowfishECB;
import redhorizon.filetypes.ArchiveFile;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.AbstractFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Implementation of a Red Alert MIX file.  The MIX format is a file package,
 * much like a ZIP file, but without compression.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions("mix")
public class MixFile extends AbstractFile implements ArchiveFile<MixRecord> {

	private static final int FLAG_CHECKSUM  = 0x00010000;
	private static final int FLAG_ENCRYPTED = 0x00020000;
	private static final int FLAG_SIZE      = 4;

	private static final int KEY_SIZE_BLOWFISH = 56;
	private static final int KEY_SIZE_SOURCE   = 80;

	private static final int   ENCRYPT_BLOCK_SIZEI = 8;
	private static final float ENCRYPT_BLOCK_SIZEF = 8f;

	private final FileChannel filechannel;

	private boolean checksum;
	private boolean encrypted;
	private MixFileHeader mixheader;
	private MixRecord[] mixrecords;

	/**
	 * Constructor, creates a mix file from a proper file on the file system.
	 * 
	 * @param filename	  Name of the mix file.
	 * @param filechannel The mix file proper.
	 */
	public MixFile(String filename, FileChannel filechannel) {

		super(filename);
		this.filechannel = filechannel;

		// Find out if this file has a checksum/encryption
		ByteBuffer flagbuffer = ByteBuffer.allocate(FLAG_SIZE);
		try {
			flagbuffer.order(ByteOrder.LITTLE_ENDIAN);
			filechannel.read(flagbuffer);
			flagbuffer.rewind();
			int flag = flagbuffer.getInt();
			checksum  = (flag & FLAG_CHECKSUM)  != 0;
			encrypted = (flag & FLAG_ENCRYPTED) != 0;
	
			// If encrypted, decrypt the mixheader and index
			if (encrypted) {
	
				// Perform the public -> private/Blowfish key function
				ByteBuffer keysource = ByteBuffer.allocate(KEY_SIZE_SOURCE);
				filechannel.read(keysource);
				ByteBuffer key = ByteBuffer.allocate(KEY_SIZE_BLOWFISH);
				MixFileKey.getBlowfishKey(keysource, key);
				BlowfishECB blowfish = new BlowfishECB();
				blowfish.initialize(key.array(), 0, key.capacity());
	
				// Decrypt the mixheader
				ByteBuffer headerbytesenc = ByteBuffer.allocate(ENCRYPT_BLOCK_SIZEI);
				ByteBuffer headerbytesdec = ByteBuffer.allocate(ENCRYPT_BLOCK_SIZEI);
				filechannel.read(headerbytesenc);
				blowfish.decrypt(headerbytesenc.array(), 0, headerbytesdec.array(), 0, headerbytesdec.capacity());
				mixheader = new MixFileHeader(headerbytesdec);
	
				// Now figure-out how many more on 8-byte blocks (+2) to decrypt
				int numblocks = (int)Math.ceil((MixRecord.RECORD_SIZE * numFiles()) / ENCRYPT_BLOCK_SIZEF);
				int numbytes = numblocks * 8;
	
				ByteBuffer encryptedbytes = ByteBuffer.allocate(numbytes);
				ByteBuffer decryptedbytes = ByteBuffer.allocate(numbytes);
				filechannel.read(encryptedbytes);
				blowfish.decrypt(encryptedbytes.array(), 0, decryptedbytes.array(), 0, decryptedbytes.capacity());
	
				ByteBuffer recordsbytes = ByteBuffer.allocate(numbytes + 2);
				recordsbytes.put(headerbytesdec).put(decryptedbytes).rewind();
	
				// Take all the decrypted data and turn them into the index records
				mixrecords = new MixRecord[numFiles()];
				for (int i = 0; i < mixrecords.length; i++) {
					mixrecords[i] = new MixRecord(recordsbytes);
				}
			}
			// If not encrypted, just read the straight data
			else {
				System.out.println("Non-encrypted mix");
				// Read the mixheader
				filechannel.position(4);
				ByteBuffer headerbytes = ByteBuffer.allocate(MixFileHeader.HEADER_SIZE);
				headerbytes.order(ByteOrder.LITTLE_ENDIAN);
				filechannel.read(headerbytes);
				headerbytes.rewind();
				mixheader = new MixFileHeader(headerbytes);
	
				// Now figure-out how much more of the file is the index
				int numblocks = MixRecord.RECORD_SIZE * numFiles();
				int numbytes = numblocks * 8;
	
				ByteBuffer recordsbytes = ByteBuffer.allocate(numbytes);
				recordsbytes.order(ByteOrder.LITTLE_ENDIAN);
				filechannel.read(recordsbytes);
				recordsbytes.rewind();
	
				// Take all the data and turn it into the index records
				mixrecords = new MixRecord[numFiles()];
				for (int i = 0; i < mixrecords.length; i++) {
					mixrecords[i] = new MixRecord(recordsbytes);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Calculates an ID for a {@link MixRecord} given the original file
	 * name for the entry to which it is referring to.
	 * 
	 * @param filename The original filename of the item in the MIX body.
	 * @return The ID of the entry from the filename.
	 */
	private static int calculateID(String filename) {

		String name = filename.toUpperCase();
		int id = 0;

		for (int i = 0; i < name.length(); ) {
			int a = 0;
			for (int j = 0; j < 4; j++) {
				a >>>= 8;
				if (i < name.length()) {
					a += name.charAt(i) << 24;
				}
				i++;
			}
			id = (id << 1 | id >>> 31) + a;
		}
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		try {
			filechannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getEntryData(MixRecord record) {

		return new MixRecordByteChannel(filechannel, (int)record.offset + offsetAdjustSize(), (int)record.length);
	}

	/**
	 * Returns a record for an item in the MIX file, instead of the item itself.
	 * Uses a binary search algorithm to locate the record.
	 * 
	 * @param name Name of the item and the record.
	 * @return <tt>MixRecord</tt> object of the record for the item.
	 */
	@Override
	public MixRecord getEntry(String name) {

		int itemid = calculateID(name);

		// Binary search for the record with the calculated value
		MixRecord record = null;
		int lo = 0;
		int hi = mixrecords.length - 1;

		while (lo <= hi) {
			int mid = (lo + hi) >> 1;
			int midval = (int)mixrecords[mid].id;

			if (itemid < midval) {
				hi = mid - 1;
			}
			else if (itemid > midval) {
				lo = mid + 1;
			}
			else {
				record = mixrecords[mid];
				break;
			}
		}

		// Set the name on the record
		if (record != null) {
			record.name = name;
		}
		return record;
	}

	/**
	 * Returns the number of files inside the MIX file.
	 * 
	 * @return Number of files within.
	 */
	private int numFiles() {

		return mixheader.numfiles & 0xffff;
	}

	/**
	 * Returns the amount to adjust the offset values in a record, by
	 * calculating the size of all the data that comes before the first internal
	 * file in the MIX file.
	 * 
	 * @return Size, in bytes, of all the data before the first internal file.
	 */
	private int offsetAdjustSize() {

		int flag = checksum || encrypted ? FLAG_SIZE : 0;
		int encryption = encrypted ? KEY_SIZE_SOURCE : 0;
		int header = encrypted ? MixFileHeader.HEADER_SIZE + 2 : MixFileHeader.HEADER_SIZE;
		int index = MixRecord.RECORD_SIZE * numFiles();
		if (encrypted) {
			index = (int)Math.ceil(index / ENCRYPT_BLOCK_SIZEF) * ENCRYPT_BLOCK_SIZEI;
		}

		return flag + header + encryption + index + 4;
	}
}
