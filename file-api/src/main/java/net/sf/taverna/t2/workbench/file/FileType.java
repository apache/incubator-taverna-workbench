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

import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * A filetype to identify a way to (de)serialise a {@link Dataflow} with the
 * {@link FileManager}.
 * <p>
 * Two filetypes are considered equal if they share an extension or mime type or
 * are the same instance.
 * </p>
 * 
 * @see net.sf.taverna.t2.workbench.file.impl.T2FlowFileType
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class FileType {

	public abstract String getExtension();

	public abstract String getMimeType();

	public abstract String getDescription();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof FileType)) {
			return false;
		}
		FileType other = (FileType) obj;
		if (getMimeType() != null && other.getMimeType() != null) {
			return getMimeType().equalsIgnoreCase(other.getMimeType());
		}
		if (getExtension() != null && other.getExtension() != null) {
			return getExtension().equalsIgnoreCase(other.getExtension());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + getExtension().hashCode();
		hash = 31 * hash + getMimeType().hashCode();
		return hash;
	}
}
