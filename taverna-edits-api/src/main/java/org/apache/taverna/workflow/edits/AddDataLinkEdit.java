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

package org.apache.taverna.workflow.edits;

import java.util.List;

import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.iterationstrategy.CrossProduct;
import org.apache.taverna.scufl2.api.iterationstrategy.DotProduct;
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyParent;
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;
import org.apache.taverna.scufl2.api.iterationstrategy.PortNode;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.ReceiverPort;

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
