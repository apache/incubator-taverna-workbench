/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workflow.edits;

import java.util.List;

import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyNode;
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyParent;
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;
import org.apache.taverna.scufl2.api.iterationstrategy.PortNode;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.ReceiverPort;

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
	private int portPosition;
	private IterationStrategyTopNode parent;

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
						IterationStrategyParent parentNode = portNode.getParent();
						if (parentNode instanceof IterationStrategyTopNode) {
							parent = (IterationStrategyTopNode) parentNode;
							portPosition = parent.indexOf(portNode);
							parent.remove(portNode);
						}
						break;
					}
				}
			}
		} else if (datalinksTo.size() == 1) {
			datalinksTo.get(0).setMergePosition(null);
		} else {
			for (int i = 0; i < datalinksTo.size(); i++)
				datalinksTo.get(i).setMergePosition(i);
		}
	}

	@Override
	protected void undoEditAction(Workflow workflow) {
		ReceiverPort sink = dataLink.getSendsTo();
		List<DataLink> datalinksTo = scufl2Tools.datalinksTo(sink);
		if (dataLink.getMergePosition() != null)
			for (int i = dataLink.getMergePosition(); i < datalinksTo.size(); i++)
				datalinksTo.get(i).setMergePosition(i + 1);
		if (portNode != null) {
			parent.add(portPosition, portNode);
			portNode.setParent(parent);
		}
		dataLink.setParent(workflow);
	}

	private PortNode findPortNode(IterationStrategyTopNode topNode,
			InputProcessorPort port) {
		for (IterationStrategyNode node : topNode) {
			if (node instanceof PortNode) {
				PortNode portNode = (PortNode) node;
				if (port.equals(portNode.getInputProcessorPort()))
					return portNode;
			} else if (node instanceof IterationStrategyTopNode) {
				IterationStrategyTopNode iterationStrategyTopNode = (IterationStrategyTopNode) node;
				PortNode result = findPortNode(iterationStrategyTopNode, port);
				if (result != null)
					return result;
			}
		}
		return null;
	}
}
