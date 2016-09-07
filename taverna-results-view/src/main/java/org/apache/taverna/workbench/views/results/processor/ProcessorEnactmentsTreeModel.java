package org.apache.taverna.workbench.views.results.processor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;
import org.apache.taverna.provenance.lineageservice.utils.ProcessorEnactment;
import org.apache.taverna.workbench.views.results.processor.IterationTreeNode.ErrorState;

/**
 * Model of the tree that contains enactments of a processor. Clicking on the
 * nodes of this tree triggers showing of results for this processor for this
 * particular enactment (invocation).
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 */
@SuppressWarnings("serial")
public class ProcessorEnactmentsTreeModel extends DefaultTreeModel {
	private static Logger logger = Logger
			.getLogger(ProcessorEnactmentsTreeModel.class);

	private Map<ProcessorEnactment, ProcessorEnactmentsTreeNode> processorEnactments = new ConcurrentHashMap<>();
	private Map<String, ProcessorEnactment> processorEnactmentsById = new ConcurrentHashMap<>();
	private final Set<ProcessorEnactment> enactmentsWithErrorOutputs;
	private final Set<ProcessorEnactment> enactmentsWithErrorInputs;

	public ProcessorEnactmentsTreeModel(
			Set<ProcessorEnactment> enactmentsGotSoFar,
			Set<ProcessorEnactment> enactmentsWithErrorInputs,
			Set<ProcessorEnactment> enactmentsWithErrorOutputs) {
		super(new DefaultMutableTreeNode("Invocations of processor"));
		this.enactmentsWithErrorInputs = enactmentsWithErrorInputs;
		this.enactmentsWithErrorOutputs = enactmentsWithErrorOutputs;
		update(enactmentsGotSoFar);
	}

	public void update(Set<ProcessorEnactment> newEnactments) {
		// First populate the ID map, so we can find parents later
		for (ProcessorEnactment processorEnactment : newEnactments)
			processorEnactmentsById.put(
					processorEnactment.getProcessEnactmentId(),
					processorEnactment);
		for (ProcessorEnactment processorEnactment : newEnactments)
			addProcessorEnactment(processorEnactment);
	}

	public ProcessorEnactmentsTreeNode addProcessorEnactment(
			ProcessorEnactment processorEnactment) {
		ProcessorEnactmentsTreeNode treeNode = processorEnactments
				.get(processorEnactment);
		boolean containsErrorsInOutputs = enactmentsWithErrorOutputs
				.contains(processorEnactment);
		boolean containsErrorsInInputs = enactmentsWithErrorInputs
				.contains(processorEnactment);
		if (treeNode != null) {
			if (treeNode.getProcessorEnactment() != processorEnactment)
				// Update it
				treeNode.setProcessorEnactment(processorEnactment);
			if (containsErrorsInInputs)
				treeNode.setErrorState(ErrorState.INPUT_ERRORS);
			else if (containsErrorsInOutputs)
				treeNode.setErrorState(ErrorState.OUTPUT_ERRORS);
			return treeNode;
		}

		List<Integer> iteration = iterationToIntegerList(processorEnactment
				.getIteration());
		String parentId = processorEnactment.getParentProcessorEnactmentId();
		ProcessorEnactment parentProc = null;
		List<Integer> parentIteration = null;
		DefaultMutableTreeNode parentNode = getRoot();
		if (parentId != null) {
			parentProc = processorEnactmentsById.get(parentId);
			if (parentProc == null)
				logger.error("Can't find parent " + parentId);
			else {
				// Use treenode parent instead
				parentNode = addProcessorEnactment(parentProc);
				parentIteration = ((ProcessorEnactmentsTreeNode) parentNode)
						.getIteration();
			}
		}

		DefaultMutableTreeNode nodeToReplace = getNodeFor(parentNode,
				iteration, parentIteration);
		DefaultMutableTreeNode iterationParent = (DefaultMutableTreeNode) nodeToReplace
				.getParent();
		int position;
		if (iterationParent == null) {
			// nodeToReplace is the root, insert as first child
			iterationParent = getRoot();
			position = 0;
		} else {
			if (nodeToReplace.getChildCount() > 0)
				logger.error("Replacing node " + nodeToReplace
						+ " with unexpected " + nodeToReplace.getChildCount()
						+ " children");
			position = iterationParent.getIndex(nodeToReplace);
			removeNodeFromParent(nodeToReplace);
		}

		ProcessorEnactmentsTreeNode newNode = new ProcessorEnactmentsTreeNode(
				processorEnactment, parentIteration);
		if (containsErrorsInInputs)
			newNode.setErrorState(ErrorState.INPUT_ERRORS);
		else if (containsErrorsInOutputs)
			newNode.setErrorState(ErrorState.OUTPUT_ERRORS);

		insertNodeInto(newNode, iterationParent, position);
		processorEnactments.put(processorEnactment, newNode);
		return newNode;
	}

	public static List<Integer> iterationToIntegerList(String iteration) {
		// Strip []
		iteration = iteration.substring(1, iteration.length()-1);
		String[] iterationSlit = iteration.split(",");
		List<Integer> integers =  new ArrayList<Integer>();
		for (String index : iterationSlit) {
			if (index.isEmpty())
				continue;
			integers.add(Integer.valueOf(index));
		}
		return integers;
	}

	@Override
	public DefaultMutableTreeNode getRoot() {
		return (DefaultMutableTreeNode) super.getRoot();
	}

	private DefaultMutableTreeNode getNodeFor(DefaultMutableTreeNode node,
			List<Integer> remainingIteration, List<Integer> parentIteration) {
		if (remainingIteration.isEmpty())
			return node;
		if (parentIteration == null)
			parentIteration = new ArrayList<>();
		int childPos = remainingIteration.get(0);
		int needChildren = childPos + 1;
		while (node.getChildCount() < needChildren) {
			List<Integer> childIteration = new ArrayList<>(parentIteration);
			childIteration.add(node.getChildCount());
			DefaultMutableTreeNode newChild = new IterationTreeNode(
					childIteration);
			insertNodeInto(newChild, node, node.getChildCount());
		}
		DefaultMutableTreeNode child = (DefaultMutableTreeNode) node
				.getChildAt(childPos);

		List<Integer> childIteration = new ArrayList<>(parentIteration);
		childIteration.add(childPos);
		// Iteration 3.1.3
//		if (iteration.size() > 1) {
			// Recurse next iteration levels
			return getNodeFor(child,
					remainingIteration.subList(1, remainingIteration.size()),
					childIteration);
//		}
//		return child;
	}
}
