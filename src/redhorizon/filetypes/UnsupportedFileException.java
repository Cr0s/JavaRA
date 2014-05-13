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

package redhorizon.filetypes;

/**
 * Exception to be thrown when the reading of a file runs into some file
 * information, normally in the header, that it cannot interpret.
 * 
 * @author Emanuel Rabina
 */
public class UnsupportedFileException extends RuntimeException {

	/**
	 * Constructor, takes an error message as parameter.
	 * 
	 * @param message Additional error message.
	 */
	public UnsupportedFileException(String message) {

		super(message);
	}
}
