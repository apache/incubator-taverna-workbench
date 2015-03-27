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

import javax.swing.filechooser.FileFilter;

import org.apache.taverna.workbench.file.FileType;

public class FileTypeFileFilter extends FileFilter {
	private final FileType fileType;

	public FileTypeFileFilter(FileType fileType) {
		this.fileType = fileType;
	}

	@Override
	public String getDescription() {
		return fileType.getDescription();
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory())
			// Don't grey out directories
			return true;
		if (fileType.getExtension() == null)
			return false;
		return file.getName().toLowerCase()
				.endsWith("." + fileType.getExtension());
	}

	public FileType getFileType() {
		return fileType;
	}
}
