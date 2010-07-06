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
import java.io.IOException;
import java.util.Date;

import net.sf.taverna.t2.workbench.file.DataflowInfo;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * Information about an open dataflow that was opened from or saved to a
 * {@link File}.
 * 
 * @see DataflowInfo
 * @see FileManager
 * @author Stian Soiland-Reyes
 * 
 */
public class FileDataflowInfo extends DataflowInfo {
	private static Logger logger = Logger.getLogger(FileDataflowInfo.class);

	public FileDataflowInfo(FileType fileType, File source, Dataflow dataflow) {
		super(fileType, canonicalFile(source), dataflow,
				lastModifiedFile(source));
	}

	protected static Date lastModifiedFile(File file) {
		long lastModifiedLong = file.lastModified();
		if (lastModifiedLong == 0) {
			return null;
		}
		return new Date(lastModifiedLong);
	}

	public static File canonicalFile(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			logger.warn("Could not find canonical file for " + file);
			return file;
		}
	}
}
