/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.workflow.edits;

import java.util.List;

import net.sf.taverna.t2.workbench.edits.EditException;
import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.port.ReceiverPort;

/**
 * Change datalink merge positions based on ordered list of data links.
 * 
 * @author David Withers
 * @author Stian Soiland-Reyes
 */
public class ReorderMergePositionsEdit extends AbstractEdit<ReceiverPort> {
	private List<DataLink> newMergePositions;
	private final List<DataLink> oldMergePositions;

	public ReorderMergePositionsEdit(List<DataLink> dataLinks,
			List<DataLink> newMergePositions) {
		super(dataLinks.get(0).getSendsTo());
		this.oldMergePositions = dataLinks;
		this.newMergePositions = newMergePositions;
	}

	@Override
	protected void doEditAction(ReceiverPort subject) throws EditException {
		for (int i = 0; i < newMergePositions.size(); i++)
			newMergePositions.get(i).setMergePosition(i);
	}

	@Override
	protected void undoEditAction(ReceiverPort subject) {
		for (int i = 0; i < oldMergePositions.size(); i++)
			oldMergePositions.get(i).setMergePosition(i);
	}
}
