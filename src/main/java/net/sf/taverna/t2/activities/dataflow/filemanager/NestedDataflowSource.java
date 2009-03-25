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
/**
 * 
 */
package net.sf.taverna.t2.activities.dataflow.filemanager;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * A source description for a nested dataflow, opened from a
 * {@link DataflowActivity} within an a {@link Processor} which is in the parent
 * {@link Dataflow}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class NestedDataflowSource {

	private final DataflowActivity dataflowActivity;

	private final Dataflow parentDataflow;

	public NestedDataflowSource(Dataflow parentDataflow,
			DataflowActivity dataflowActivity) {
		this.parentDataflow = parentDataflow;
		this.dataflowActivity = dataflowActivity;
	}

	public DataflowActivity getDataflowActivity() {
		return dataflowActivity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dataflowActivity == null) ? 0 : dataflowActivity.hashCode());
		result = prime * result
				+ ((parentDataflow == null) ? 0 : parentDataflow.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final NestedDataflowSource other = (NestedDataflowSource) obj;
		if (dataflowActivity == null) {
			if (other.dataflowActivity != null)
				return false;
		} else if (!dataflowActivity.equals(other.dataflowActivity))
			return false;
		if (parentDataflow == null) {
			if (other.parentDataflow != null)
				return false;
		} else if (!parentDataflow.equals(other.parentDataflow))
			return false;
		return true;
	}

	public Dataflow getParentDataflow() {
		return parentDataflow;
	}
	
}