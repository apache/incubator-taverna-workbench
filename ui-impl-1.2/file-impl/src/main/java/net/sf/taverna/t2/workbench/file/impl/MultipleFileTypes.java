/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.workbench.file.impl;

import java.io.File;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.workbench.file.FileType;

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
		if (file.isDirectory()) {
			return true;
		}

		String lowerFileName = file.getName().toLowerCase();
		for (FileType fileType : fileTypes) {
			if (fileType.getExtension() == null) {
				continue;
			}
			if (lowerFileName.endsWith(fileType.getExtension())) {
				return true;
			}
		}
		return false;
	}

}
