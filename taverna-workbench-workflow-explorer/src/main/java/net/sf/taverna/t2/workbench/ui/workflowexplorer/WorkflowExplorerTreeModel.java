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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import uk.org.taverna.scufl2.api.common.NamedSet;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.core.ControlLink;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;

/**
 * Workflow Explorer tree model. The tree root has four children nodes,
 * representing the workflow inputs, outputs, services (processors), dataLinks,
 * controlLinks.
 * <p>
 * A service node can contain a nested workflow if it contains an activity of of
 * type DataflowActivity. In this case, the service node will have 3 children:
 * the input and output of the DataflowActivity and the workflow node itself
 * (containing the nested workflow being wrapped inside the DataflowActivity).
 * The structure of the workflow node sub-tree (the tree whose root is the
 * workflow node) is the same as that of the main workflow and it gets named
 * after the nested workflow. Alternatively, a service (processor) can be simple
 * and have only the processor's input and output ports as children.
 * <p>
 * Input, output, data link and control link nodes are leaves.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * @author David Withers
 */
public class WorkflowExplorerTreeModel extends DefaultTreeModel{
	private static final long serialVersionUID = -2327461863858923772L;
	public static final String INPUTS = "Workflow input ports";
	public static final String OUTPUTS = "Workflow output ports";
	public static final String PROCESSORS = "Services";
	public static final String DATALINKS = "Data links";
	public static final String CONTROLLINKS = "Control links";
	public static final String MERGES = "Merges";

	@SuppressWarnings("unused")
	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	/* Root of the tree. */
	private DefaultMutableTreeNode rootNode;

	public WorkflowExplorerTreeModel(Workflow df) {
		super(new DefaultMutableTreeNode(df)); // root node contains the whole workflow
		rootNode = (DefaultMutableTreeNode) this.getRoot();
		createTree(df, rootNode);
	}

	/**
	 * Creates the tree model from a given workflow, for a given tree root.
	 */
	private void createTree(Workflow df, DefaultMutableTreeNode root) {
		// Create the four main node groups - inputs, outputs,
		// services, data links, control links and merges.
		DefaultMutableTreeNode inputs = new DefaultMutableTreeNode(INPUTS);
		DefaultMutableTreeNode outputs = new DefaultMutableTreeNode(OUTPUTS);
		DefaultMutableTreeNode services = new DefaultMutableTreeNode(PROCESSORS);
		DefaultMutableTreeNode datalinks = new DefaultMutableTreeNode(DATALINKS);
		DefaultMutableTreeNode controllinks = new DefaultMutableTreeNode(CONTROLLINKS);

		// Attach them to the root of the tree
		root.add(inputs);
		root.add(outputs);
		root.add(services);
		root.add(datalinks);
		root.add(controllinks);

		// Populate the workflow's inputs.
		for (InputWorkflowPort dataflowInput : df.getInputPorts())
			inputs.add(new DefaultMutableTreeNode(dataflowInput));

		// Populate the workflow's outputs.
		for (OutputWorkflowPort dataflowOutput : df.getOutputPorts())
			outputs.add(new DefaultMutableTreeNode(dataflowOutput));

		/*
		 * Populate the workflow's processors (which in turn can contain a
		 * nested workflow).
		 */
		NamedSet<Processor> processorsList = df.getProcessors();
		for (Processor processor : processorsList) {
			DefaultMutableTreeNode processorNode = new DefaultMutableTreeNode(
					processor);
			services.add(processorNode);

		    // A processor node can have children (input and output ports).
			for (InputProcessorPort inputPort : processor.getInputPorts())
				processorNode.add(new DefaultMutableTreeNode(inputPort));
			for (OutputProcessorPort outputPort : processor.getOutputPorts())
				processorNode.add(new DefaultMutableTreeNode(outputPort));
		}

		// Populate the workflow's data links.
		for (DataLink datalink: df.getDataLinks())
			datalinks.add(new DefaultMutableTreeNode(datalink));

		// Populate the workflow's control links.
		for (ControlLink controlLink : df.getControlLinks())
			controllinks.add(new DefaultMutableTreeNode(controlLink));
	}

	private static final int INPUT_IDX = 0;
	private static final int OUTPUT_IDX = 1;
	private static final int PROCESSOR_IDX = 2;
	private static final int DATA_IDX = 3;
	private static final int CONTROL_IDX = 4;

	/**
	 * Returns a path from the root to the node containing the object. For a
	 * nested workflow, only a path for the DataflowActivity and its input and
	 * output ports is returned - for all other nested workflow objects we
	 * return null as we do not want them to be selection in the tree.
	 */
	public static TreePath getPathForObject(Object userObject,
			DefaultMutableTreeNode root) {
		if (userObject instanceof Workflow) { // node contains a Dataflow object
			if (root.getUserObject().equals(userObject)) // is it the root of the tree?
				return new TreePath(root.getPath());
		} else if (userObject instanceof InputWorkflowPort) {
			// Get the root inputs node
			DefaultMutableTreeNode inputs = (DefaultMutableTreeNode) root
					.getChildAt(INPUT_IDX);
			for (int i = 0; i < inputs.getChildCount(); i++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) inputs
						.getChildAt(i);
				if (node.getUserObject().equals(userObject))
					return new TreePath(node.getPath());
			}
			/*
			 * The node we are looking for must be under some nested workflow
			 * then - but we do not want to let the user select a node under a
			 * nested workflow so return here
			 */
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
		} else if (userObject instanceof OutputWorkflowPort) {
			// Get the root outputs node
			DefaultMutableTreeNode outputs = (DefaultMutableTreeNode) root
					.getChildAt(OUTPUT_IDX);
			for (int i = 0; i< outputs.getChildCount(); i++) { // loop through the outputs
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) outputs
						.getChildAt(i);
				if (node.getUserObject().equals(userObject))
					return new TreePath(node.getPath());
			}
			/*
			 * The node we are looking for must be under some nested workflow
			 * then - but we do not want to let the user select a node under a
			 * nested workflow so return here
			 */
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
		} else if (userObject instanceof Processor) {
			// Get the root services (processors) node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root
					.getChildAt(PROCESSOR_IDX);
			for (int i = 0; i < processors.getChildCount(); i++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) processors
						.getChildAt(i);
				if (node.getUserObject().equals(userObject))
					return new TreePath(node.getPath());
			}
			/*
			 * The node we are looking for must be under some nested workflow
			 * then - but we do not want to let the user select a node under a
			 * nested workflow so return here
			 */
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
		} else if (userObject instanceof InputProcessorPort) {
			// This is an input port of a processor
			// Get the root processors node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root
					.getChildAt(PROCESSOR_IDX);
			for (int i = processors.getChildCount() - 1; i >= 0; i--) {
				// Looping backwards so that nested workflows are checked last
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors
						.getChildAt(i);

				/*
				 * We actually do not want to check nested workflows as we do
				 * not want the user to be able to select a component of a
				 * nested workflow
				 */

				/*
				// If this is nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					// Check the associated DataflowActivity's input ports first
					// Do not check the last child as it is the nested workflow node
					for (int j = 0; j < processor.getChildCount()-1; j++){
						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor.getChildAt(j);
						if ((port_node.getUserObject() instanceof ActivityInputPort) &&
								(((ActivityInputPort) port_node.getUserObject()).equals(userObject)))
							return new TreePath(port_node.getPath());
					}

					// Get the nested workflow node - it is always the last child of the
					// wrapping processor's node
					DefaultMutableTreeNode nestedWorkflowNode = (DefaultMutableTreeNode) processor.getLastChild();
					TreePath tp = getPathForObject(userObject, nestedWorkflowNode);
					if (tp != null)
						return tp;
				} else */
					/*
					 * This is not a nested workflow, so loop thought the
					 * processor's input and output ports, and see if there is a
					 * matching input port
					 */
					for (int j = 0; j < processor.getChildCount(); j++) {
						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor
								.getChildAt(j);
						if ((port_node.getUserObject() instanceof InputProcessorPort)
								&& (((InputProcessorPort) port_node
										.getUserObject()).equals(userObject)))
							return new TreePath(port_node.getPath());
					}
			}
			return null; // The node is inside a nested workflow so just return here
		} else if (userObject instanceof OutputProcessorPort) {
			// This is an output port of a processor (i.e. of its associated activity)
			// Get the root processors node
			DefaultMutableTreeNode processors = (DefaultMutableTreeNode) root
					.getChildAt(PROCESSOR_IDX);
			for (int i = processors.getChildCount() - 1; i >= 0 ; i--){
				// Looping backwards so that nested workflows are checked last
				DefaultMutableTreeNode processor = (DefaultMutableTreeNode) processors
						.getChildAt(i);

				/*
				 * We actually do not want to check nested workflows as we do
				 * not want the user to be able to select a component of a
				 * nested workflow
				 */

				/*
				// If this is nested workflow - descend into it
				if (Tools.containsNestedWorkflow((Processor) processor.getUserObject())){
					// Check the associated DataflowActivity's output ports first
					// Do not check the last child as it is the nested workflow node
					for (int j = 0; j < processor.getChildCount()-1; j++){
						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor.getChildAt(j);
						if ((port_node.getUserObject() instanceof ActivityOutputPortImpl) &&
								(((ActivityOutputPortImpl) port_node.getUserObject()).equals(userObject)))
							return new TreePath(port_node.getPath());
					}

					// Get the nested workflow node - it is always the last child of the
					// wrapping processor's node
					DefaultMutableTreeNode nestedWorkflowNode = (DefaultMutableTreeNode) processor.getLastChild();
					TreePath tp = getPathForObject(userObject, nestedWorkflowNode);
					if (tp != null)
						return tp;
				} else */
				{
					/*
					 * This is not a nested workflow, so loop thought the
					 * processor's input and output ports, and see if there is a
					 * matching output port
					 */
					for (int j = 0; j < processor.getChildCount(); j++) {
						DefaultMutableTreeNode port_node = (DefaultMutableTreeNode) processor
								.getChildAt(j);
						if ((port_node.getUserObject() instanceof OutputProcessorPort)
								&& (((OutputProcessorPort) port_node
										.getUserObject()).equals(userObject)))
							return new TreePath(port_node.getPath());
					}
				}
			}
			return null; // The node is inside a nested workflow so just return here
		} else if (userObject instanceof DataLink) {
			// Get the root data links node
			DefaultMutableTreeNode datalinks = (DefaultMutableTreeNode) root
					.getChildAt(DATA_IDX);
			for (int i = 0; i < datalinks.getChildCount(); i++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) datalinks
						.getChildAt(i);
				if (node.getUserObject().equals(userObject))
					return new TreePath(node.getPath());
			}
			/*
			 * The node we are looking for must be under some nested workflow
			 * then - but we do not want to let the user select a node under a
			 * nested workflow so return here
			 */
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
		} else if (userObject instanceof ControlLink) {
			// Get the root control links node
			DefaultMutableTreeNode controllinks = (DefaultMutableTreeNode) root
					.getChildAt(CONTROL_IDX);
			for (int i = 0; i < controllinks.getChildCount(); i++) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) controllinks
						.getChildAt(i);
				if (node.getUserObject().equals(userObject))
					return new TreePath(node.getPath());
			}
			/*
			 * The node we are looking for must be under some nested workflow
			 * then - but we do not want to let the user select a node under a
			 * nested workflow so return here
			 */
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
