/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.taverna.renderers;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public interface MediaTypeDetector {

	/**
	 * Guess the media types of a given {@link Path}.
	 * <p>
	 * The media types in the list are ordered by decreasing likelyhood, e.g.
	 * the first item in the list is the most likely (in many cases most
	 * specific) media type.
	 * <p>
	 * The returned list will never be empty, as it will always contain either
	 * <code>text/plain</code> or <code>application/octet-stream</code>
	 * 
	 * @param path
	 *            A {@link Path} to be guessed. The file extension of the path
	 *            MAY be taken into consideration.
	 * @return A list of guessed media types
	 * @throws IOException If the path can't be accessed (e.g. lacking file permissions)
	 */
	public List<String> guessMediaTypes(Path path) throws IOException ;

	/**
	 * Guess the media types of a given {@link String} value.
	 * <p>
	 * The media types in the list are ordered by decreasing likelihood, e.g.
	 * the first item in the list is the most likely (in many cases most
	 * specific) media type.
	 * <p>
	 * The returned list will never be empty, as it will always contain either
	 * <code>text/plain</code>.
	 * 
	 * @param String
	 *            A string to check, e.g. an XML document. Note that only the first
	 *            few lines of the string to test is required.
	 * @return A list of guessed media types
	 */	
	public List<String> guessMediaTypes(String string);

	/**
	 * Guess the media types of a given byte array.
	 * <p>
	 * The media types in the list are ordered by decreasing likelihood, e.g.
	 * the first item in the list is the most likely (in many cases most
	 * specific) media type.
	 * <p>
	 * The returned list will never be empty, as it will always contain
	 * <code>application/octet-stream</code>
	 * 
	 * @param bytes
	 *            Bytes to check. Note that only the "first bits" of the
	 *            resource is required, e.g. the initial 8192 bytes.
	 * @return A list of guessed media types
	 */	
	public List<String> guessMediaTypes(byte[] bytes);

	/**
	 * Guess the media types of a given {@link URI}.
	 * <p>
	 * The detector MAY try to retrieve the given URI, e.g. issuing a
	 * HTTP <code>HEAD</code> request.  
	 * <p>
	 * The media types in the list are ordered by decreasing likelihood, e.g.
	 * the first item in the list is the most likely (in many cases most
	 * specific) media type.
	 * <p>
	 * The returned list will never be empty, as it will always contain either
	 * <code>text/plain</code> or <code>application/octet-stream</code>
	 * 
	 * @param uri
	 *            A {@link URI} to be guessed. Any file extension of the URL
	 *            MAY be taken into consideration.
	 * @return A list of guessed media types
	 * @throws IOException If the URL can't be accessed (e.g. a network issue)
	 */	
	public List<String> guessMediaTypes(URI uri) throws IOException;

	
	/**
	 * Dummy MediaTypeDetector implementation
	 * <p>
	 * This can be used for test purposes which don't want to use
	 * MediaTypeDetectorImpl.
	 * <p>
	 * This class always return "application/octet-stream" as the media type.
	 *
	 */
	public static class Dummy implements MediaTypeDetector {
		private static List<String> BINARY = Collections.singletonList("application/octet-stream"); 
		@Override
		public List<String> guessMediaTypes(Path path) throws IOException {
			return BINARY;
		}
		@Override
		public List<String> guessMediaTypes(String string) {
			return BINARY;
		}
		@Override
		public List<String> guessMediaTypes(byte[] bytes) {
			return BINARY;
		}
		@Override
		public List<String> guessMediaTypes(URI uri) throws IOException {
			return BINARY;
		}		
	}
	
}
