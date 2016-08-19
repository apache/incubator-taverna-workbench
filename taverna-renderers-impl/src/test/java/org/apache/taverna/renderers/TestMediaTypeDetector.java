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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.taverna.renderers.impl.MediaTypeDetectorImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestMediaTypeDetector {

	public MediaTypeDetector detector = new MediaTypeDetectorImpl();

	@Parameters(name = "{0}")
	public static List<Object[]> expectedMediaTypes() {
		// extension -> mediaType
		// Each test is represented as "test.$extension" from src/test/resources
		return Arrays.asList(new Object[][] {
			// TODO: Re-enable ALL of the below and fix in Tika configuration
			// (or upstream) - some of these fail in guessByAnonymousPath 
			// when they can't check the file extension
			
			// mis-matched as text/plain
			//{ "csv", "text/csv" },
			// mis-matched as application/zip
			//{ "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" }, 
			{ "html", "text/html" }, 
			{ "jpeg", "image/jpeg" },
			{ "ods", "application/vnd.oasis.opendocument.spreadsheet" },
			{ "odt", "application/vnd.oasis.opendocument.text" }, 
			{ "pdf", "application/pdf" },
			{ "png", "image/png" }, 
			// { "svg", "image/svg+xml" }, // Mis-matched as application/xml 
			{ "txt", "text/plain" },
			// Mis-matched as application/zip
			// { "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
			{ "xml", "application/xml" }, 
			{ "zip", "application/zip" } 
		});
	}

	@Parameter(0)
	public String extension;

	@Parameter(1)
	public String expectedMediaType;

	private Path path;

	private Path anonymousPath;

	@Before
	public void writePaths() throws IOException {
		path = Files.createTempFile("test", "." + extension);
		try (InputStream in = getClass().getResourceAsStream("/test." + extension)) {
			Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
		}		
		anonymousPath = Files.createTempFile("test", "");		
		Files.copy(path, anonymousPath, StandardCopyOption.REPLACE_EXISTING);		
	}
	
	@After
	public void deletePaths() throws IOException {
		Files.deleteIfExists(path);
		Files.deleteIfExists(anonymousPath);
	}
	
	@Test
	public void guessByPath() throws Exception {
		List<String> guess = detector.guessMediaTypes(path);
		assertEquals(expectedMediaType, guess.get(0));		
	}
	
	
	@Test
	public void guessByAnonymousPath() throws Exception {
		List<String> guess = detector.guessMediaTypes(anonymousPath);
		assertEquals(expectedMediaType, guess.get(0));		
	}
	

	@Test
	public void guessByUrl() throws Exception {		
		List<String> guess = detector.guessMediaTypes(path.toUri());
		assertEquals(expectedMediaType, guess.get(0));		
	}
	
	@Test
	public void guessByAnonymousUrl() throws Exception {		
		List<String> guess = detector.guessMediaTypes(anonymousPath.toUri());
		assertEquals(expectedMediaType, guess.get(0));		
	}
	
	@Test
	public void guessByBytes() throws Exception {
		// Our test-resources are deliberately small, so this won't
		// consume too much memory. Normal use of the MediaTypeDetector
		// would be to only read the first kilobytes of a file.
		byte[] bytes = Files.readAllBytes(path);
		List<String> guess = detector.guessMediaTypes(bytes);
		assertEquals(expectedMediaType, guess.get(0));		
	}

	@Test
	public void guessByString() {
		List<String> strings;
		try {
			strings = Files.readAllLines(path, StandardCharsets.UTF_8);
		} catch (IOException e) {
			Assume.assumeNoException(e);
			return;
		}
		// NOTE: The below will join \r\n to \n
		// We assume that any binary formats like PNG failed the above test, 
		// and that the remaining text-based formats support both kinds of newlines
		String joined = strings.stream().limit(5). // first 5 lines
				collect(Collectors.joining("\n"));	
		List<String> guess = detector.guessMediaTypes(joined);
		assertEquals(expectedMediaType, guess.get(0));		
	}


}
