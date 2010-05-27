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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;

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

	public ProcessorEnactmentsTreeModel(List<ProcessorEnactment> processorEnactments){
		
		super(new DefaultMutableTreeNode("Invocations of processor"));
		
		for (ProcessorEnactment processorEnactment : processorEnactments){
			List<Integer> iteration = iterationToIntegerList(processorEnactment.getIteration());
			DefaultMutableTreeNode parent = getParent(getRoot(), iteration, "Iteration ");
			int childPos = 0;
			if (! iteration.isEmpty()) {
				childPos = iteration.get(iteration.size()-1);
			}
			if (parent.getChildCount() > childPos) {
				parent.remove(childPos);
			}
			parent.insert(new ProcessorEnactmentsTreeNode(processorEnactment), childPos);
		}
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
	
	private DefaultMutableTreeNode getParent(DefaultMutableTreeNode node, List<Integer> iteration, String prefix) {
		if (iteration.isEmpty()) {
			if (node.isRoot()) { return node; }
			return (DefaultMutableTreeNode) node.getParent();		
		} else {
			int childPos = iteration.get(0);
			int needChildren = childPos+1;
			String newPrefix = prefix;
			if (! (prefix.endsWith(" ") || prefix.equals(""))) {
				// Not for the initial prefix
				newPrefix = newPrefix + ".";
			}
			while (node.getChildCount() < needChildren) {
				node.add(new DefaultMutableTreeNode(newPrefix + (node.getChildCount()+1)));
			}
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(childPos);
		
			// Iteration 3.1.3
			newPrefix = newPrefix + (childPos+1); 
			return getParent(child, iteration.subList(1, iteration.size()), newPrefix);			
		}
	}
}
