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
package net.sf.taverna.t2.workflow.edits;

import java.util.List;

import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyNode;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyParent;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;
import uk.org.taverna.scufl2.api.iterationstrategy.PortNode;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.ReceiverPort;

/**
 * Remove a DataLink from a Workflow.
 * <p>
 * Handles setting the merge position of all dataLinks with the same receiver port.
 *
 * @author David Withers
 */
public class RemoveDataLinkEdit extends AbstractEdit<Workflow> {

	private final DataLink dataLink;
	private PortNode portNode;
	private IterationStrategyParent parent;

	public RemoveDataLinkEdit(Workflow workflow, DataLink dataLink) {
		super(workflow);
		this.dataLink = dataLink;
	}

	@Override
	protected void doEditAction(Workflow workflow) {
		dataLink.setParent(null);
		ReceiverPort sink = dataLink.getSendsTo();
		List<DataLink> datalinksTo = scufl2Tools.datalinksTo(sink);
		if (datalinksTo.isEmpty()) {
			if (sink instanceof InputProcessorPort) {
				InputProcessorPort port = (InputProcessorPort) sink;
				for (IterationStrategyTopNode topNode : port.getParent().getIterationStrategyStack()) {
					portNode = findPortNode(topNode, port);
					if (portNode != null) {
						parent = portNode.getParent();
						portNode.setParent(null);
						break;
					}
				}
			}
		} else if (datalinksTo.size() == 1) {
			datalinksTo.get(0).setMergePosition(null);
		} else {
			for (int i = 0; i < datalinksTo.size(); i++) {
				datalinksTo.get(i).setMergePosition(i);
			}
		}
	}

	@Override
	protected void undoEditAction(Workflow workflow) {
		ReceiverPort sink = dataLink.getSendsTo();
		List<DataLink> datalinksTo = scufl2Tools.datalinksTo(sink);
		if (dataLink.getMergePosition() != null) {
			for (int i = dataLink.getMergePosition(); i < datalinksTo.size(); i++) {
				datalinksTo.get(i).setMergePosition(i + 1);
			}
		}
		if (portNode != null) {
			portNode.setParent(parent);
		}
		dataLink.setParent(workflow);
	}

	private PortNode findPortNode(IterationStrategyTopNode topNode, InputProcessorPort port) {
		PortNode result = null;
		for (IterationStrategyNode iterationStrategyNode : topNode) {
			if (iterationStrategyNode instanceof PortNode) {
				PortNode portNode = (PortNode) iterationStrategyNode;
				if (port.equals(portNode.getInputProcessorPort())) {
					result =  portNode;
					break;
				}
			} else if (iterationStrategyNode instanceof IterationStrategyTopNode) {
				IterationStrategyTopNode iterationStrategyTopNode = (IterationStrategyTopNode) iterationStrategyNode;
				result = findPortNode(iterationStrategyTopNode, port);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

}
