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

package org.apache.taverna.workbench.file.impl;

import java.io.File;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import org.apache.taverna.workbench.file.FileType;

public class MultipleFileTypes extends FileFilter {
	private String description;
	private final Set<FileType> fileTypes;

	public MultipleFileTypes(Set<FileType> fileTypes, String description) {
		this.fileTypes = fileTypes;
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory())
			return true;

		String lowerFileName = file.getName().toLowerCase();
		for (FileType fileType : fileTypes) {
			if (fileType.getExtension() == null)
				continue;
			if (lowerFileName.endsWith(fileType.getExtension()))
				return true;
		}
		return false;
	}
}
