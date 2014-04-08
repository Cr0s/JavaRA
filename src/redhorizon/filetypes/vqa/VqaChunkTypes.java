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

/**
 * List of known chunks for the VQA file format.  Other types exist, but are not
 * important for VQA file decoding.
 * 
 * @author Emanuel Rabina.
 */
public enum VqaChunkTypes {

	CHUNK_FORM ("FORM"),
	CHUNK_FINF ("FINF"),
	CHUNK_VQFR ("VQFR"),

	CHUNK_CBF  ("CBF"),
	CHUNK_CBP  ("CBP"),
	CHUNK_CPL  ("CPL"),
	CHUNK_SND  ("SND"),
	CHUNK_VPT  ("VPT");

	private final String chunkname;

	/**
	 * Constructor, matches the enumerated type with the chunk designation.
	 * 
	 * @param chunkname Identifying name of the chunk.
	 */
	private VqaChunkTypes(String chunkname) {

		this.chunkname = chunkname;
	}

	/**
	 * Returns the matching chunk type for the given chunk.
	 * 
	 * @param chunkname Chunk 4-byte name to test against.
	 * @return Matching <tt>VqaChunks</tt> enum for the chunk, or
	 * 		   <tt>null</tt> if no matching type exists.
	 */
	static VqaChunkTypes getMatchingType(String chunkname) {

		for (VqaChunkTypes vqachunk: VqaChunkTypes.values()) {
			if (chunkname.startsWith(vqachunk.chunkname)) {
				return vqachunk;
			}
		}
//		throw new EnumConstantNotPresentException(VqaChunkTypes.class, chunkname);
		return null;
	}
}
