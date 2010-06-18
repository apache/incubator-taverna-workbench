/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.views.results.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;

import org.apache.log4j.Logger;

/**
 * Model of the tree that contains enactments of a processor.
 * Clicking on the nodes of this tree triggers showing of
 * results for this processor for this particular enactment (invocation).
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 *
 */
@SuppressWarnings("serial")
public class ProcessorEnactmentsTreeModel extends DefaultTreeModel{

	private Map<ProcessorEnactment, ProcessorEnactmentsTreeNode> processorEnactments = new ConcurrentHashMap<ProcessorEnactment, ProcessorEnactmentsTreeNode>();
	private Map<String, ProcessorEnactment> processorEnactmentsById = new ConcurrentHashMap<String, ProcessorEnactment>();
	private static Logger logger = Logger.getLogger(ProcessorEnactmentsTreeModel.class);
	private final Set<ProcessorEnactment> enactmentsWithErrorOutputs;
	
	public ProcessorEnactmentsTreeModel(Set<ProcessorEnactment> enactmentsGotSoFar, Set<ProcessorEnactment> enactmentsWithErrorOutputs){
		super(new DefaultMutableTreeNode("Invocations of processor"));
		this.enactmentsWithErrorOutputs = enactmentsWithErrorOutputs;		
		update(enactmentsGotSoFar);
	}

	public void update(Set<ProcessorEnactment> newEnactments) {
		// First populate the ID map, so we can find parents later
		for (ProcessorEnactment processorEnactment : newEnactments){
			processorEnactmentsById.put(processorEnactment.getProcessEnactmentId(), processorEnactment);				
		}				
		for (ProcessorEnactment processorEnactment : newEnactments){
			addProcessorEnactment(processorEnactment);
		}
		
	}
	
	public ProcessorEnactmentsTreeNode addProcessorEnactment(ProcessorEnactment processorEnactment) {
		ProcessorEnactmentsTreeNode treeNode = processorEnactments.get(processorEnactment);
		if (treeNode != null) {
			if (treeNode.getProcessorEnactment() != processorEnactment) {
				// Update it
				treeNode.setProcessorEnactment(processorEnactment);				
			}	
			treeNode.setContainsErrorsInOutputs(enactmentsWithErrorOutputs.contains(processorEnactment));
			return treeNode;
		}
		
		List<Integer> iteration = iterationToIntegerList(processorEnactment.getIteration());
		String parentId = processorEnactment.getParentProcessorEnactmentId();
		ProcessorEnactment parentProc = null;
		List<Integer> parentIteration = null;
		DefaultMutableTreeNode parentNode = getRoot();
		if (parentId != null) {
			parentProc = processorEnactmentsById.get(parentId);
			if (parentProc == null) {
				logger.error("Can't find parent " + parentId);
			} else {
				// Use treenode parent instead
				parentNode = addProcessorEnactment(parentProc);
				parentIteration = ((ProcessorEnactmentsTreeNode)parentNode).getIteration();
			}
		}
		
		DefaultMutableTreeNode nodeToReplace = getNodeFor(parentNode, iteration, parentIteration);
		DefaultMutableTreeNode iterationParent = (DefaultMutableTreeNode) nodeToReplace.getParent();
		int position;
		if (iterationParent == null) {
			// nodeToReplace is the root, insert as first child
			iterationParent = getRoot();
			position = 0;
		} else {
			if (nodeToReplace.getChildCount() > 0) {
				logger.error("Replacing node " + nodeToReplace + " with unexpected " + nodeToReplace.getChildCount() + " children");			
			}
			position = iterationParent.getIndex(nodeToReplace);
			removeNodeFromParent(nodeToReplace);
		} 
		
		ProcessorEnactmentsTreeNode newNode = new ProcessorEnactmentsTreeNode(processorEnactment, parentIteration, enactmentsWithErrorOutputs.contains(processorEnactment));
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
			if (index.equals("")) {
				continue;
			}
			integers.add(Integer.valueOf(index));
		}
		return integers;
	}

	@Override
	public DefaultMutableTreeNode getRoot() {
		return (DefaultMutableTreeNode) super.getRoot();
	}
	
	private DefaultMutableTreeNode getNodeFor(DefaultMutableTreeNode node, List<Integer> remainingIteration, List<Integer> parentIteration) {
		if (remainingIteration.isEmpty()) {
			return node;
		}		
		if (parentIteration == null) {
			parentIteration = new ArrayList<Integer>();
		} 		
		int childPos = remainingIteration.get(0);		
		int needChildren = childPos+1;	
		while (node.getChildCount() < needChildren) {
			List<Integer> childIteration = new ArrayList<Integer>(parentIteration);
			childIteration.add(node.getChildCount());			
			DefaultMutableTreeNode newChild = new IterationTreeNode(childIteration);
			insertNodeInto(newChild, node, node.getChildCount());
		}
		DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(childPos);
	
		List<Integer> childIteration = new ArrayList<Integer>(parentIteration);
		childIteration.add(childPos);
		// Iteration 3.1.3
//		if (iteration.size() > 1) {
			// Recurse next iteration levels
			return getNodeFor(child, remainingIteration.subList(1, remainingIteration.size()), childIteration);
//		}
//		return child;
	}
}
