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

import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.iterationstrategy.CrossProduct;
import uk.org.taverna.scufl2.api.iterationstrategy.DotProduct;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyParent;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;
import uk.org.taverna.scufl2.api.iterationstrategy.PortNode;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.ReceiverPort;

/**
 * Adds a DataLink to a Workflow.
 * <p>
 * Handles setting the merge position of all dataLinks with the same receiver port.
 * <p>
 * Modifies the processor's iteration strategy or when the first DataLink is added.
 *
 * @author David Withers
 */
public class AddDataLinkEdit extends AbstractEdit<Workflow> {
	private DataLink dataLink;
	private PortNode portNode;

	public AddDataLinkEdit(Workflow workflow, DataLink dataLink) {
		super(workflow);
		this.dataLink = dataLink;
	}

	@Override
	protected void doEditAction(Workflow workflow) {
		ReceiverPort sink = dataLink.getSendsTo();
		List<DataLink> datalinksTo = scufl2Tools.datalinksTo(sink);
		if (datalinksTo.size() > 0) {
			if (datalinksTo.size() == 1)
				datalinksTo.get(0).setMergePosition(0);
			dataLink.setMergePosition(datalinksTo.size());
		} else {
			dataLink.setMergePosition(null);
			if (sink instanceof InputProcessorPort) {
				InputProcessorPort inputProcessorPort = (InputProcessorPort) sink;
				for (IterationStrategyTopNode node : inputProcessorPort.getParent().getIterationStrategyStack()) {
					portNode = new PortNode(node, inputProcessorPort);
					portNode.setDesiredDepth(inputProcessorPort.getDepth());
					break;
				}
			}
		}
		dataLink.setParent(workflow);
	}

	@Override
	protected void undoEditAction(Workflow workflow) {
		dataLink.setParent(null);
		ReceiverPort sink = dataLink.getSendsTo();
		List<DataLink> datalinksTo = scufl2Tools.datalinksTo(sink);
		if (datalinksTo.size() == 1)
			datalinksTo.get(0).setMergePosition(null);
		else if (datalinksTo.isEmpty()&&portNode != null) {
			IterationStrategyParent parent = portNode.getParent();
			if (parent instanceof DotProduct)
				((DotProduct) parent).remove(portNode);
			else if (parent instanceof CrossProduct)
				((CrossProduct) parent).remove(portNode);
		}
	}
}
