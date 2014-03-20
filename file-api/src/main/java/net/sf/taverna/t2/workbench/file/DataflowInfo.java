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
package net.sf.taverna.t2.workbench.file;

import java.util.Date;

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Information about a dataflow that has been opened by the {@link FileManager}.
 * <p>
 * This class, or a subclass of it, is used by
 * {@link DataflowPersistenceHandler}s to keep information about where a
 * dataflow came from or where it was saved to.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class DataflowInfo {
	private final FileType fileType;
	private final Dataflow dataflow;
	private final Date lastModified;
	private final Object canonicalSource;

	public DataflowInfo(FileType fileType, Object canonicalSource,
			Dataflow dataflow, Date lastModified) {
		this.fileType = fileType;
		this.canonicalSource = canonicalSource;
		this.dataflow = dataflow;
		this.lastModified = lastModified;
	}

	public DataflowInfo(FileType fileType, Object canonicalSource,
			Dataflow dataflow) {
		this(fileType, canonicalSource, dataflow, null);
	}

	/**
	 * Return the canonical source of where the dataflow was opened from or
	 * saved to.
	 * <p>
	 * This is not necessarily the source provided to
	 * {@link FileManager#openDataflow(FileType, Object)} or
	 * {@link FileManager#saveDataflow(Dataflow, FileType, Object, boolean)},
	 * but it's canonical version.
	 * </p>
	 * <p>
	 * For instance, if a dataflow was opened from a
	 * File("relative/something.t2flow") this canonical source would resolve the
	 * relative path.
	 * </p>
	 * 
	 * @return
	 */
	public Object getCanonicalSource() {
		return canonicalSource;
	}

	/**
	 * Return the dataflow that is open.
	 * 
	 * @return The open dataflow
	 */
	public Dataflow getDataflow() {
		return dataflow;
	}

	/**
	 * Get the last modified {@link Date} of the source at the time when it was
	 * opened/saved.
	 * <p>
	 * It is important that this value is checked on creation time, and not on
	 * demand.
	 * </p>
	 * 
	 * @return The {@link Date} of the source/destination's last modified
	 *         timestamp, or <code>null</code> if unknown.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * The {@link FileType} of this {@link Dataflow} serialisation used for
	 * opening/saving.
	 * 
	 * @return The {@link FileType}, for instance
	 *         {@link net.sf.taverna.t2.workbench.file.impl.T2FlowFileType}
	 */
	public FileType getFileType() {
		return fileType;
	}
}
