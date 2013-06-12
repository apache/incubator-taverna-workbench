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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import uk.org.taverna.databundle.DataBundles;

/**
 *
 *
 * @author David Withers
 */
public class RendererUtils {

	public static long getSizeInBytes(Path path) throws IOException {
		if (DataBundles.isValue(path)) {
			return Files.size(path);
		} else if (DataBundles.isReference(path)) {
			URL url = DataBundles.getReference(path).toURL();
			String protocol = url.getProtocol();
			if ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol)) {
				String contentLength = url.openConnection().getHeaderField("Content-Length");
				if (contentLength != null && !contentLength.isEmpty()){
					return new Long(contentLength);
				}
			} else if ("file".equalsIgnoreCase(protocol)) {
				return FileUtils.toFile(url).length();
			}
			return -1;
		} else {
			throw new IllegalArgumentException("Path is not a value or reference");
		}
	}

	public static String getString(Path path) throws IOException {
		if (DataBundles.isValue(path)) {
			return DataBundles.getStringValue(path);
		} else if (DataBundles.isReference(path)) {
			return IOUtils.toString(DataBundles.getReference(path));
		} else {
			throw new IllegalArgumentException("Path is not a value or reference");
		}
	}

	public static InputStream getInputStream(Path path) throws MalformedURLException, IOException {
		if (DataBundles.isValue(path)) {
			return Files.newInputStream(path);
		} else if (DataBundles.isReference(path)) {
			return DataBundles.getReference(path).toURL().openStream();
		} else {
			throw new IllegalArgumentException("Path is not a value or reference");
		}
	}

}
