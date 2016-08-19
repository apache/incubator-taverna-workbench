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
package org.apache.taverna.renderers.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.renderers.MediaTypeDetector;
import org.apache.tika.Tika;

/**
 * Media Type detector that uses Apache Tika
 *
 */
public class MediaTypeDetectorImpl implements MediaTypeDetector {

	Tika tika = new Tika();
	String BINARY = "application/octet-stream";
	String TEXT_PLAIN = "text/plain";
	
	@Override
	public List<String> guessMediaTypes(Path path) throws IOException {
		List<String> mediaTypes = new ArrayList<>();
		String detected = tika.detect(path);
		if (detected != null) {			
			// Note: Tika does not return null, but its javadoc does not
			// make such a promise
			mediaTypes.add(detected);
		}
		if (! mediaTypes.contains(BINARY) && ! mediaTypes.contains(TEXT_PLAIN)) {
			// This fallback will also make sure we never return an empty list
			mediaTypes.add(BINARY);
		}
		return mediaTypes;
	}

	@Override
	public List<String> guessMediaTypes(String string) {
		List<String> mediaTypes = new ArrayList<>();
		// We'll assume that as a String it can be detected from UTF_8 bytes,
		// which makes sense for XML, JSON, SVG, etc
		String mediaType = tika.detect(string.getBytes(StandardCharsets.UTF_8));
		if (mediaType != null) {
			mediaTypes.add(mediaType);
		}

		if (! mediaTypes.contains(BINARY) && ! mediaTypes.contains(TEXT_PLAIN)) {
			mediaTypes.add(TEXT_PLAIN);
		}
		return mediaTypes;
	}

	@Override
	public List<String> guessMediaTypes(byte[] bytes) {
		List<String> mediaTypes = new ArrayList<>();
		String detected = tika.detect(bytes);
		if (detected != null) {			
			// Note: Tika does not return null, but its javadoc does not
			// make such a promise
			mediaTypes.add(detected);
		}
		if (! mediaTypes.contains(BINARY) && ! mediaTypes.contains(TEXT_PLAIN)) {
			// This fallback will also make sure we never return an empty list
			mediaTypes.add(BINARY);
		}
		return mediaTypes;
	}

	@Override
	public List<String> guessMediaTypes(URI uri) throws IOException {
		List<String> mediaTypes = new ArrayList<>();
		String detected = tika.detect(uri.toURL());
		if (detected != null) {			
			// Note: Tika does not return null, but its javadoc does not
			// make such a promise
			mediaTypes.add(detected);
		}
		if (! mediaTypes.contains(BINARY) && ! mediaTypes.contains(TEXT_PLAIN)) {
			// This fallback will also make sure we never return an empty list
			mediaTypes.add(BINARY);
		}
		return mediaTypes;
	}
	
}
