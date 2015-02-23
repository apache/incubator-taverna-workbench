/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.renderers;

import static org.purl.wf4ever.robundle.Bundles.getReference;
import static org.purl.wf4ever.robundle.Bundles.getStringValue;
import static org.purl.wf4ever.robundle.Bundles.isReference;
import static uk.org.taverna.databundle.DataBundles.isValue;

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
