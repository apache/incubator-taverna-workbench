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
/*

package org.apache.taverna.renderers.impl;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A FileFilter implementation that can be configured to show only specific file
 * suffixes.
 * 
 * @author Tom Oinn
 */
public class ExtensionFileFilter extends FileFilter {
	String[] allowedExtensions;

	public ExtensionFileFilter(String[] allowedExtensions) {
		this.allowedExtensions = allowedExtensions;
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		String extension = getExtension(f);
		if (extension != null)
			for (int i = 0; i < allowedExtensions.length; i++)
				if (extension.equalsIgnoreCase(allowedExtensions[i]))
					return true;
		return false;
	}

	String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1)
			ext = s.substring(i + 1).toLowerCase();
		return ext;
	}

	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Filter for extensions : ");
		String sep = "";
		for (String ext : allowedExtensions) {
			sb.append(sep).append(ext);
			sep = ", ";
		}
		return sb.toString();
	}
}
