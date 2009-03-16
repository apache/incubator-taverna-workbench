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
package net.sf.taverna.t2.workbench.ui.workflowexplorer;

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
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityOutputPortImpl;

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
 *
 */
public class WorkflowExplorerTreeModel extends DefaultTreeModel{

	private static final long serialVersionUID = -2327461863858923772L;
	
	public static final String INPUTS = "Inputs";
	public static final String OUTPUTS = "Outputs";
	public static final String SERVICES = "Services";
	public static final String DATALINKS = "Data links";
	public static final String CONTROLLINKS = "Control links";
	public static final String MERGES = "Merges";
	
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
		DefaultMutableTreeNode services = new DefaultMutableTreeNode(SERVICES);
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
		List<? extends DataflowInputPort> inputsList = (List<? extends DataflowInputPort>) df.getInputPorts();
		if (inputsList != null) {
			for (DataflowInputPort dataflowInput : inputsList) {
				inputs.add(new DefaultMutableTreeNode(dataflowInput));
			}
		}
		
		// Populate the workflow's outputs.
		List<? extends DataflowOutputPort> outputsList = (List<? extends DataflowOutputPort>) df.getOutputPorts();
		if (outputsList != null) {
			for (DataflowOutputPort dataflowOutput : outputsList) {
				outputs.add(new DefaultMutableTreeNode(dataflowOutput));
			}
		}
		
		// Populate the workflow's processors (which in turn can contain a nested workflow).
		List<? extends Processor> processorsList = (List<? extends Processor>) df.getProcessors();
		if (!processorsList.isEmpty()) {
			for (Processor processor : processorsList){
				DefaultMutableTreeNode processorNode = new DefaultMutableTreeNode(
						processor);
				services.add(processorNode);
				
				// Nested workflow case
				if (Tools.containsNestedWorkflow(processor)){
							
					// Input ports of the contained DataflowActivity
					for (ActivityInputPort inputPort : processor.getActivityList().get(0).getInputPorts()) {
						processorNode.add(new DefaultMutableTreeNode(inputPort));
					}
					// Output ports of the contained DataflowActivity
					for (OutputPort outputPort : processor.getActivityList().get(0).getOutputPorts()) {
						processorNode.add(new DefaultMutableTreeNode(outputPort));
					}				
					// The nested workflow itself
					Dataflow nestedWorkflow = ((NestedDataflow) processor.getActivityList().get(0)).getNestedDataflow();
					DefaultMutableTreeNode nestedWorkflowNode = new DefaultMutableTreeNode(nestedWorkflow);
					processorNode.add(nestedWorkflowNode);
					// The nested workflow node is the root of the new nested tree
					createTree(nestedWorkflow, nestedWorkflowNode);
				}
				else{
					// A processor node can have children (input and output ports of its associated activity/activities).
					// Currently we just look at the first activity in the list.
					for (ActivityInputPort inputPort : processor.getActivityList().get(0).getInputPorts()){
						processorNode.add(new DefaultMutableTreeNode(inputPort));
					}
					
					for (OutputPort outputPort : processor.getActivityList().get(0).getOutputPorts()){
						processorNode.add(new DefaultMutableTreeNode(outputPort));
					}
				}
			}
		}
		
		// Populate the workflow's data links.
		List<? extends Datalink> datalinksList = (List<? extends Datalink>) df.getLinks();
		if (!datalinksList.isEmpty()) {
			for (Datalink datalink: datalinksList) {
				datalinks.add(new DefaultMutableTreeNode(datalink));
			}
		}
		
		// Populate the workflow's control links.
		for (Processor processor : processorsList){
			List<? extends Condition> controllinksList = (List<? extends Condition>) processor.getControlledPreconditionList();
			if (!datalinksList.isEmpty()) {
				for (Condition controllink: controllinksList) {
					controllinks.add(new DefaultMutableTreeNode(controllink));
				}
			}
		}
		
		// Populate the workflow's merges.
		List<? extends Merge> mergesList = (List<? extends Merge>) df.getMerges();
		if (!mergesList.isEmpty()) {
			for (Merge merge: mergesList) {
				merges.add(new DefaultMutableTreeNode(merge));
			}
		}
	}
	
	/**
	 * Returns a path from the root to the node containing the object.
	 */
	public static TreePath getPathForObject(Object userObject, DefaultMutableTreeNode root){
		
		if (userObject instanceof Dataflow){ // node contains a Dataflow object
			if (root.getUserObject().equals(userObject)){ // is it the root of the tree?
				return new TreePath(root.getPath());
			}
			/*else{ // it is a nested workflow node (root of the nested sub-tree)
				// Get all the processors and see which one contains a nested workflow
				DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
				...
				We do not deal with this here as at the moment we do not want any action for
				nested workflow root - this case is dealt with in mouseClicked on workflow tree 
				(wfTree) in WorkflowExplorerTree class.
			}*/
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
			// The node we are looking for must be under some nested workflow then
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
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
			}
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
			// The node we are looking for must be under some nested workflow then
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
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
			}
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
			// The node we are looking for must be under some nested workflow then
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
			}
		}
		else if (userObject instanceof ActivityInputPort){
			// This is an input port of a processor (i.e. of its associated activity)
			// Get the root procesors node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = processors.getChildCount() - 1; i >= 0 ; i--){
				// Looping backwards so that nested workflows are checked last
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
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
				else { 
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
				}
			}
		}
		else if (userObject instanceof ActivityOutputPortImpl){
			// This is an output port of a processor (i.e. of its associated activity)
			// Get the root processors node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
			for (int i = processors.getChildCount() - 1; i >= 0 ; i--){
				// Looping backwards so that nested workflows are checked last
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors.getChildAt(i);
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
				else { 
					// This is not a nested workflow, so loop 
					// thought the processor's input and output ports,
					// and see if there is a matching output port
					for (int j = 0; j < processor.getChildCount(); j++){

						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor.getChildAt(j);
						if ((port_node.getUserObject() instanceof ActivityOutputPortImpl) && 
								(((OutputPort) port_node.getUserObject()).equals(userObject))){
							return new TreePath(port_node.getPath());
						}
					}
				}
			}
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
			// The node we are looking for must be under some nested workflow then
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
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
			}
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
			// The node we are looking for must be under some nested workflow then
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
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
			}
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
			// The node we are looking for must be under some nested workflow then
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root.getChildAt(2);
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
			}
		}
		
		return null;
	}
	
}
