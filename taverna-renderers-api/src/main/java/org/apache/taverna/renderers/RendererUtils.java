/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.renderers;

import static org.apache.taverna.robundle.Bundles.getReference;
import static org.apache.taverna.robundle.Bundles.getStringValue;
import static org.apache.taverna.robundle.Bundles.isReference;
import static org.apache.taverna.databundle.DataBundles.isValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author David Withers
 */
public class RendererUtils {
	public static long getSizeInBytes(Path path) throws IOException {
		if (isValue(path))
			return Files.size(path);
		if (!isReference(path))
			throw new IllegalArgumentException(
					"Path is not a value or reference");

		URL url = getReference(path).toURL();
		switch (url.getProtocol().toLowerCase()) {
		case "http":
		case "https":
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.connect();
			String contentLength = conn.getHeaderField("Content-Length");
			conn.disconnect();
			if (contentLength != null && !contentLength.isEmpty())
				return Long.parseLong(contentLength);
			return -1;
		case "file":
			return FileUtils.toFile(url).length();
		default:
			return -1;
		}
	}

	public static String getString(Path path) throws IOException {
		if (isValue(path))
			return getStringValue(path);
		else if (isReference(path))
			return IOUtils.toString(getReference(path));
		else
			throw new IllegalArgumentException(
					"Path is not a value or reference");
	}

	public static InputStream getInputStream(Path path)
			throws MalformedURLException, IOException {
		if (isValue(path))
			return Files.newInputStream(path);
		else if (isReference(path))
			return getReference(path).toURL().openStream();
		else
			throw new IllegalArgumentException(
					"Path is not a value or reference");
	}
}
