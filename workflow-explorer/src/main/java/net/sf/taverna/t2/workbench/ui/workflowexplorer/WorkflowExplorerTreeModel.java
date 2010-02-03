/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.workflowexplorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.NamedWorkflowEntityComparator;
import net.sf.taverna.t2.workflowmodel.utils.PortComparator;

/**
 * Workflow Explorer tree model. The tree root has four children nodes,
 * representing the workflow inputs, outputs, services (processors), datalinks, 
 * control links (coordinations) and merges.
 * <p>
 * A service node can contain a nested workflow if it contains an activity of 
 * of type DataflowActivity. In this case, the service node will have 3 children:
 * the input and output of the DataflowActivity and the workflow node itself
 * (containing the nested workflow being wrapped inside the DataflowActivity). 
 * The structure of the workflow node sub-tree (the tree whose root is the 
 * workflow node) is the same as that of the main workflow and it gets named 
 * after the nested workflow. Alternatively, a service (processor) can be simple 
 * and have only the processor's input and output ports as children. 
 * <p>
 * Input, output, data link, control link and merge nodes are leaves.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 *
 */
public class WorkflowExplorerTreeModel extends DefaultTreeModel{


	private static final long serialVersionUID = -2327461863858923772L;
	
	public static final String INPUTS = "Workflow input ports";
	public static final String OUTPUTS = "Workflow output ports";
	public static final String PROCESSORS = "Services";
	public static final String DATALINKS = "Data links";
	public static final String CONTROLLINKS = "Control links";
	public static final String MERGES = "Merges";

	private final PortComparator portComparator = new PortComparator();
	private final NamedWorkflowEntityComparator namedWorkflowEntitiyComparator = new NamedWorkflowEntityComparator();
	
	/* Root of the tree. */
	private DefaultMutableTreeNode rootNode;
	
	public WorkflowExplorerTreeModel(Dataflow df){
		
		super(new DefaultMutableTreeNode(df)); // root node contains the whole workflow		
		rootNode = (DefaultMutableTreeNode) this.getRoot();
		createTree(df, rootNode);			
	}
	
	/**
	 * Creates the tree model from a given workflow, for a given tree root.
	 */
	private void createTree(Dataflow df, DefaultMutableTreeNode root) {

		// Create the four main node groups - inputs, outputs, 
		// services, data links, control links and merges.
		DefaultMutableTreeNode inputs = new DefaultMutableTreeNode(INPUTS);
		DefaultMutableTreeNode outputs = new DefaultMutableTreeNode(OUTPUTS);
		DefaultMutableTreeNode services = new DefaultMutableTreeNode(PROCESSORS);
		DefaultMutableTreeNode datalinks = new DefaultMutableTreeNode(DATALINKS);
		DefaultMutableTreeNode controllinks = new DefaultMutableTreeNode(CONTROLLINKS);
		DefaultMutableTreeNode merges = new DefaultMutableTreeNode(MERGES);
		
		// Attach them to the root of the tree
		root.add(inputs);
		root.add(outputs);
		root.add(services);
		root.add(datalinks);
		root.add(controllinks);
		root.add(merges);

		// Populate the workflow's inputs.
		List<DataflowInputPort> inputsList =new ArrayList<DataflowInputPort>(df.getInputPorts());
		Collections.sort(inputsList, portComparator);		
		for (DataflowInputPort dataflowInput : inputsList) {
			inputs.add(new DefaultMutableTreeNode(dataflowInput));
		}
		
		// Populate the workflow's outputs.
		List<DataflowOutputPort> outputsList =new ArrayList<DataflowOutputPort>(df.getOutputPorts());
		Collections.sort(outputsList, portComparator);
		for (DataflowOutputPort dataflowOutput : outputsList) {
			outputs.add(new DefaultMutableTreeNode(dataflowOutput));
		}
	
		// Populate the workflow's processors (which in turn can contain a nested workflow).
		List<Processor> processorsList = new ArrayList<Processor>(df.getProcessors());
		Collections.sort(processorsList, namedWorkflowEntitiyComparator);
		for (Processor processor : processorsList){
			DefaultMutableTreeNode processorNode = new DefaultMutableTreeNode(
					processor);
			services.add(processorNode);
			if (processor.getActivityList().isEmpty()) {
				continue;
			}				
			Activity<?> activity = processor.getActivityList().get(0);
			
		    // A processor node can have children (input and output ports of its
			// associated activity/activities).
			// Currently we just look at the first activity in the list.
			List<ActivityInputPort> inputPorts = new ArrayList<ActivityInputPort>(
					activity.getInputPorts());
			Collections.sort(inputPorts, portComparator);
			for (ActivityInputPort inputPort : inputPorts) {
				processorNode.add(new DefaultMutableTreeNode(inputPort));
			}
			List<OutputPort> outputPorts = new ArrayList<OutputPort>(activity
					.getOutputPorts());
			Collections.sort(outputPorts, portComparator);
			for (OutputPort outputPort : outputPorts) {
				processorNode.add(new DefaultMutableTreeNode(outputPort));
			}
		
		}
		
		// Populate the workflow's data links.
		List<? extends Datalink> datalinksList = (List<? extends Datalink>) df.getLinks();
		// TODO: Sort datalinks - but by what?
		for (Datalink datalink: datalinksList) {
			datalinks.add(new DefaultMutableTreeNode(datalink));
		}
		
		// Populate the workflow's control links.
		for (Processor processor : processorsList){
			List<? extends Condition> controllinksList = (List<? extends Condition>) processor.getControlledPreconditionList();
			if (!controllinksList.isEmpty()) {
				for (Condition controllink: controllinksList) {
					controllinks.add(new DefaultMutableTreeNode(controllink));
				}
			}
		}
		
		// Populate the workflow's merges.
		List<Merge> mergesList = new ArrayList<Merge>(df.getMerges());
		Collections.sort(mergesList, namedWorkflowEntitiyComparator);
		for (Merge merge: mergesList) {
			merges.add(new DefaultMutableTreeNode(merge));
		}
	}
	
	/**
	 * Returns a path from the root to the node containing the object. For a nested workflow,
	 * only a path for the DataflowActivity and its input and output ports is returned - for all other
	 * nested workflow objects we return null as we do not want them to be selectable in the tree.
	 */
	public static TreePath getPathForObject(Object userObject, DefaultMutableTreeNode root){
		
		if (userObject instanceof Dataflow){ // node contains a Dataflow object
			if (root.getUserObject().equals(userObject)){ // is it the root of the tree?
				return new TreePath(root.getPath());
			}
		}
		else if (userObject instanceof DataflowInputPort){
			// Get the root inputs node
			DefaultMutableTreeNode inputs = (DefaultMutableTreeNode) root.getChildAt(0);
			for (int i = 0; i< inputs.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) inputs.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}			
			// The node we are looking for must be under some nested workflow then - but
			// we do not want to let the user select a node under a nested workflow so return here
			return null;
			/*DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = 0; i < processors.getChildCount(); i++){
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				// If this is a nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					// Get the nested workflow node - it is always the last child of the 
					// wrapping processor's node
					DefaultMutableTreeNode nestedWorkflowNode = (DefaultMutableTreeNode) processor.getLastChild(); 
					TreePath tp = getPathForObject(userObject, nestedWorkflowNode);
					if (tp != null)
						return tp;
				}
			}*/
		}
		else if (userObject instanceof DataflowOutputPort){
			// Get the root outputs node
			DefaultMutableTreeNode outputs = (DefaultMutableTreeNode) root.getChildAt(1);
			for (int i = 0; i< outputs.getChildCount(); i++){ // loop through the outputs
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) outputs.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}			
			// The node we are looking for must be under some nested workflow then - but
			// we do not want to let the user select a node under a nested workflow so return here
			return null;
			/*DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = 0; i < processors.getChildCount(); i++){
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				// If this is a nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					// Get the nested workflow node - it is always the last child of the 
					// wrapping processor's node
					DefaultMutableTreeNode nestedWorkflowNode = (DefaultMutableTreeNode) processor.getLastChild(); 
					TreePath tp = getPathForObject(userObject, nestedWorkflowNode);
					if (tp != null)
						return tp;	
					}
			}*/
		}
		else if (userObject instanceof Processor){ 
			// Get the root services (processors) node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = 0; i< processors.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) processors.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}
			// The node we are looking for must be under some nested workflow then - but
			// we do not want to let the user select a node under a nested workflow so return here
			return null;
			/*for (int i = 0; i < processors.getChildCount(); i++){
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				// If this is a nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					// Get the nested workflow node - it is always the last child of the 
					// wrapping processor's node
					DefaultMutableTreeNode nestedWorkflowNode = (DefaultMutableTreeNode) processor.getLastChild(); 
					TreePath tp = getPathForObject(userObject, nestedWorkflowNode);
					if (tp != null)
						return tp;				
					}
			}*/
		}
		else if (userObject instanceof ActivityInputPort){
			// This is an input port of a processor (i.e. of its associated activity)
			// Get the root processors node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = processors.getChildCount() - 1; i >= 0 ; i--){
				// Looping backwards so that nested workflows are checked last
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);

				// We actually do not want to check nested workflows as we do not want the user
				// to be able to select a component of a nested workflow
				/*
				// If this is nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){ 
					// Check the associated DataflowActivity's input ports first
					// Do not check the last child as it is the nested workflow node
					for (int j = 0; j < processor.getChildCount()-1; j++){
						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor.getChildAt(j);
						if ((port_node.getUserObject() instanceof ActivityInputPort) && 
								(((ActivityInputPort) port_node.getUserObject()).equals(userObject))){
							return new TreePath(port_node.getPath());
						}
					}
					
					// Get the nested workflow node - it is always the last child of the 
					// wrapping processor's node
					DefaultMutableTreeNode nestedWorkflowNode = (DefaultMutableTreeNode) processor.getLastChild(); 
					TreePath tp = getPathForObject(userObject, nestedWorkflowNode);
					if ( tp != null){
						return tp;
					}
				}
				else { */
					// This is not a nested workflow, so loop 
					// thought the processor's input and output ports,
					// and see if there is a matching input port
					for (int j = 0; j < processor.getChildCount(); j++){
						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor.getChildAt(j);
						if ((port_node.getUserObject() instanceof ActivityInputPort) && 
								(((ActivityInputPort) port_node.getUserObject()).equals(userObject))){
							return new TreePath(port_node.getPath());
						}
					}
				//}
			}
			return null; // The node is inside a nested workflow so just return here
		}
		else if (userObject instanceof ActivityOutputPort){
			// This is an output port of a processor (i.e. of its associated activity)
			// Get the root processors node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = processors.getChildCount() - 1; i >= 0 ; i--){
				// Looping backwards so that nested workflows are checked last
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				
				// We actually do not want to check nested workflows as we do not want the user
				// to be able to select a component of a nested workflow
				/*
				// If this is nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){ 
					// Check the associated DataflowActivity's output ports first
					// Do not check the last child as it is the nested workflow node
					for (int j = 0; j < processor.getChildCount()-1; j++){
						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor.getChildAt(j);
						if ((port_node.getUserObject() instanceof ActivityOutputPortImpl) && 
								(((ActivityOutputPortImpl) port_node.getUserObject()).equals(userObject))){
							return new TreePath(port_node.getPath());
						}
					}
					
					// Get the nested workflow node - it is always the last child of the 
					// wrapping processor's node
					DefaultMutableTreeNode nestedWorkflowNode = (DefaultMutableTreeNode) processor.getLastChild(); 
					TreePath tp = getPathForObject(userObject, nestedWorkflowNode);
					if ( tp != null){
						return tp;
					}
				}
				else { */
					// This is not a nested workflow, so loop 
					// thought the processor's input and output ports,
					// and see if there is a matching output port
					for (int j = 0; j < processor.getChildCount(); j++){

						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor.getChildAt(j);
						if ((port_node.getUserObject() instanceof ActivityOutputPort) && 
								(((OutputPort) port_node.getUserObject()).equals(userObject))){
							return new TreePath(port_node.getPath());
						}
					}
				//}
			}
			return null; // The node is inside a nested workflow so just return here
		}
		else if (userObject instanceof Datalink){
			// Get the root data links node
			DefaultMutableTreeNode datalinks = (DefaultMutableTreeNode) root.getChildAt(3);
			for (int i = 0; i< datalinks.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) datalinks.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}
			// The node we are looking for must be under some nested workflow then - but
			// we do not want to let the user select a node under a nested workflow so return here
			return null;
			/*DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = 0; i < processors.getChildCount(); i++){
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				// If this is a nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					// Get the nested workflow node - it is always the last child of the 
					// wrapping processor's node
					DefaultMutableTreeNode nestedWorkflowNode = (DefaultMutableTreeNode) processor.getLastChild(); 
					TreePath tp = getPathForObject(userObject, nestedWorkflowNode);
					if (tp != null)
						return tp;				
				}
			}*/
		}
		else if (userObject instanceof Condition){
			// Get the root control links node
			DefaultMutableTreeNode controllinks = (DefaultMutableTreeNode) root.getChildAt(4);
			for (int i = 0; i< controllinks.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) controllinks.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}
			// The node we are looking for must be under some nested workflow then - but
			// we do not want to let the user select a node under a nested workflow so return here
			return null;
			/*DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = 0; i < processors.getChildCount(); i++){
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				// If this is a nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					// Get the nested workflow node - it is always the last child of the 
					// wrapping processor's node
					DefaultMutableTreeNode nestedWorkflowNode = (DefaultMutableTreeNode) processor.getLastChild(); 
					TreePath tp = getPathForObject(userObject, nestedWorkflowNode);
					if (tp != null)
						return tp;				
				}
			}*/
		}
		else if (userObject instanceof Merge){
			// Get the root merges node
			DefaultMutableTreeNode merges = (DefaultMutableTreeNode) root.getChildAt(5);
			for (int i = 0; i< merges.getChildCount(); i++){
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) merges.getChildAt(i);
				if (node.getUserObject().equals(userObject)){
					return new TreePath(node.getPath());
				}
			}
			// The node we are looking for must be under some nested workflow then - but
			// we do not want to let the user select a node under a nested workflow so return here
			return null;
			/*DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = 0; i < processors.getChildCount(); i++){
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
				// If this is a nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					// Get the nested workflow node - it is always the last child of the 
					// wrapping processor's node
					DefaultMutableTreeNode nestedWorkflowNode = (DefaultMutableTreeNode) processor.getLastChild(); 
					TreePath tp = getPathForObject(userObject, nestedWorkflowNode);
					if (tp != null)
						return tp;				
				}
			}*/
		}
		
		return null;
	}
	
}
