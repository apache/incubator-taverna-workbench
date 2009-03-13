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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Workflow Explorer provides a context sensitive tree view of a workflow
 * (showing its inputs, outputs, processors, datalinks, etc.). Selection of 
 * a node in the Model Explorer tree and a right-click leads to context 
 * sensitive options appearing in a pop-up menu.
 * 
 * @author Alex Nenadic
 * 
 */
@SuppressWarnings("serial")
public class WorkflowExplorer extends JPanel implements UIComponentSPI {

	/* Manager of all opened workflows */
	private DataflowSelectionManager openedWorkflowsManager = DataflowSelectionManager
										.getInstance();

	private MenuManager menuManager = MenuManager.getInstance();

	//private static Logger logger = Logger.getLogger(WorkflowExplorer.class);

	/* Currently selected workflow (to be displayed in the Workflow Explorer). */
	private Dataflow workflow;
	
	/* Map of trees for all opened workflows. */
	private Map<Dataflow, JTree> openedWorkflowsTrees = new HashMap<Dataflow, JTree>();
	
	/* Tree representation of the currently selected workflow. */
	private JTree wfTree;

	/* Current workflow's selection model event observer - telling us
	 * what is the currently selected object in the current workflow. */
	private Observer<DataflowSelectionMessage> workflowSelectionListener = new DataflowSelectionListener();

	/* Scroll pane containing the workflow tree. */
	private JScrollPane jspTree;

	/* WorkflowExplorer singleton. */
	private static WorkflowExplorer INSTANCE;

	/**
	 * Returns a WorkflowExplorer instance.
	 */
	public static WorkflowExplorer getInstance() {
		synchronized (WorkflowExplorer.class) {
			if (INSTANCE == null)
				INSTANCE = new WorkflowExplorer();
		}
		return INSTANCE;
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Workflow Explorer";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
	}

	public void onDispose() {
		// TODO Auto-generated method stub
	}

	/**
	 * Constructs the Workflow Explorer.
	 */
	public WorkflowExplorer() {

		// Create a tree that will represent a view over the current workflow
		// Initially, there is no workflow opened, so we create an empty tree,
		// but immediately after all visual components of the Workbench are
		// created (including Workflow Explorer) a new empty workflow is
		// created, which is represented with a NON-empty JTree with four nodes
		// (Inputs, Outputs, Processors, and Data links) that themselves have no
		// children.
		wfTree = new JTree(new DefaultMutableTreeNode("No workflow open"));

		// Start observing when current workflow is switched (e.g. a new workflow
		// opened or switched between opened workflows). Note that closing a workflow
		// also causes either a switch to another opened workflow or, if it was the last one, 
		// opening of a new empty workflow.
		ModelMap.getInstance().addObserver(
				new Observer<ModelMap.ModelMapEvent>() {
					public void notify(Observable<ModelMapEvent> sender,
							final ModelMapEvent message) {
						if (message.getModelName().equals(
								ModelMapConstants.CURRENT_DATAFLOW)) {
							if (message.getNewModel() instanceof Dataflow) {

								// Remove the workflow selection model listener from the 
								// previous (if any) and add to the new workflow (if any)
								Dataflow oldWF = (Dataflow) message.getOldModel();
								Dataflow newWF = (Dataflow) message.getNewModel();
								if (oldWF != null) {
									openedWorkflowsManager
											.getDataflowSelectionModel(oldWF)
											.removeObserver(workflowSelectionListener);
								}

								if (newWF != null) {
									openedWorkflowsManager
											.getDataflowSelectionModel(newWF)
											.addObserver(workflowSelectionListener);
								}

								// Create a new thread to prevent drawing the
								// current workflow tree to take over completely
								new Thread(
										"Workflow Explorer - model map message: current workflow switched.") {
									@Override
									public void run() {
										// If the workflow tree has already been created - switch to it
										if (openedWorkflowsTrees.containsKey(((Dataflow) message.getNewModel()))){
											switchWorkflowTree((Dataflow) message.getNewModel());
										}
										else{ // otherwise create a new tree for the workflow
											createWorkflowTree((Dataflow) message.getNewModel());
										}
									}
								}.start();
							}
						}
					}
				});

		// Start observing workflow closing events on File Manager
		FileManager.getInstance().addObserver(new Observer<FileManagerEvent>(){

			public void notify(Observable<FileManagerEvent> sender,
					FileManagerEvent message) throws Exception {
				// Remove the closed workflow tree from the map of opened workflow trees
				if (message instanceof ClosedDataflowEvent) {
					openedWorkflowsTrees.remove(((ClosedDataflowEvent) message).getDataflow());
				}
			}
		});
		
		// Start observing events on Edit Manager when current workflow is
		// edited (e.g. a node added, deleted or updated)
		EditManager.getInstance().addObserver(new Observer<EditManagerEvent>() {
			public void notify(Observable<EditManagerEvent> sender,
					final EditManagerEvent message) throws Exception {
				if (message instanceof AbstractDataflowEditEvent) {
					// React to edits in the current workflow
					// Create a new thread to prevent drawing the workflow
					// tree to take over completely
					new Thread(
							"Workflow Explorer - edit manager message: current workflow edited.") {
						@Override
						public void run() {
							// Create a new tree to reflect the changes to
							// the current tree
							updateWorkflowTree();
						}
					}.start();
				}
			}
		});

		// Draw visual components
		initComponents();
	}

	/**
	 * Lays out the swing components.
	 */
	public void initComponents() {

		setLayout(new BorderLayout());

		// Workflow tree scroll pane
		jspTree = new JScrollPane(wfTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jspTree.setBorder(new EtchedBorder());

		// Title
		JLabel wfExplorerLabel = new JLabel("Workflow Explorer");
		wfExplorerLabel.setMinimumSize(new Dimension(0, 0)); // so that it can shrink completely
		wfExplorerLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

		JPanel wfExplorerPanel = new JPanel(new BorderLayout());
		wfExplorerPanel.add(wfExplorerLabel, BorderLayout.NORTH);
		wfExplorerPanel.add(jspTree, BorderLayout.CENTER);

		add(wfExplorerLabel, BorderLayout.NORTH);
		add(wfExplorerPanel, BorderLayout.CENTER);

	}

	/**
	 * Gets called when a workflow is opened or a new (empty) one created.
	 */
	public void createWorkflowTree(Dataflow df) {

		// Set the current workflow
		workflow = FileManager.getInstance().getCurrentDataflow(); // == df

		// Create a new tree and populate it with the workflow's data
		wfTree = createTreeFromWorkflow(workflow);

		// Add the new tree to the list of opened workflow trees
		openedWorkflowsTrees.put(workflow, wfTree);
		
		// Expand the tree
		expandAll();

		// Repaint the scroll pane containing the tree
		jspTree.setViewportView(wfTree);
		jspTree.revalidate();
		jspTree.repaint();
	}

	/**
	 * Gets called when the current workflow is switched.
	 */
	private void switchWorkflowTree(
			Dataflow df) {
		
		// Set the current workflow to the one we have switched to
		workflow = FileManager.getInstance().getCurrentDataflow(); // == df
		
		// Get the tree for the current workflow
		wfTree = openedWorkflowsTrees.get(workflow);
		
		// Repaint the scroll pane containing the tree
		jspTree.setViewportView(wfTree);
		jspTree.revalidate();
		jspTree.repaint();										
	}
	
	/**
	 * Gets called when the current workflow is edited.
	 * @param message 
	 */
	public void updateWorkflowTree() {
			
		// Get the updated workflow
		workflow = FileManager.getInstance().getCurrentDataflow();
		
		// Get the old workflow tree
		JTree oldTree = openedWorkflowsTrees.get(workflow);
		
		// Create the new tree from the updated workflow
		wfTree = createTreeFromWorkflow(workflow);
		
		// Update the tree in the list of opened workflow trees
		openedWorkflowsTrees.put(workflow, wfTree);
		
		// Update the new tree's expansion state based on the old tree
		// i.e. all nodes in the old tree that have been expanded/collapsed 
		// should also be expanded/collapsed in the new tree (unless an 
		// expanded node has been removed)
		copyExpansionState(oldTree, (DefaultMutableTreeNode) oldTree.getModel().getRoot(), (DefaultMutableTreeNode) wfTree.getModel().getRoot());

		// Repaint the scroll pane containing the tree
		jspTree.setViewportView(wfTree);
		jspTree.revalidate();
		jspTree.repaint();

		// Select the node(s) that should be selected
		setSelectedNodes();
	}
	
	@SuppressWarnings("unchecked")
	private void copyExpansionState(JTree oldTree, DefaultMutableTreeNode oldNode, DefaultMutableTreeNode newNode) {
		
		boolean expandParentNode = false;
		
		// Do the children on the node first (so we can set the node's children 
		// to be expanded even if the node itself is collapsed)
	    Enumeration<DefaultMutableTreeNode> children = newNode.children();
        while (children.hasMoreElements()) {
        	DefaultMutableTreeNode newChild = children.nextElement();
        	// Find the corresponding node in the old tree, if any
        	DefaultMutableTreeNode oldChild = findChildWithUserObject(oldNode, newChild.getUserObject());
        	
        	if (oldChild != null){ // corresponding node found in the old tree
        		// Recursively do the same for each child
        		copyExpansionState(oldTree, oldChild, newChild);
        	}
        	else{ // corresponding node not found in the old tree -
        		// a new node has been added or a node had been edited in the new tree so 
        		// make that node visible now by expanding the parent node
        		expandParentNode = true;
        	}
        }
    	
    	// Now do the node
        if (expandParentNode){ 
        	// Order matters - we first check if a new child was inserted to this node
        	// (that means that the old node might have been a leaf before)
    		int row = wfTree.getRowForPath(new TreePath(newNode.getPath()));
    		wfTree.expandRow(row);
        }
        else if (oldNode.isLeaf()){ // if it is a leaf - expand/collapse does not work, so use isVisible/makeVisible
        	if (oldTree.isVisible(new TreePath(oldNode.getPath()))){
        		wfTree.makeVisible(new TreePath(newNode.getPath()));
        	}
        }
        else if (oldTree.isExpanded(new TreePath(oldNode.getPath()))){
    		int row = wfTree.getRowForPath(new TreePath(newNode.getPath()));
    		wfTree.expandRow(row);
    	}
    	else{ // node was collapsed
    		int row = wfTree.getRowForPath(new TreePath(newNode.getPath()));
    		wfTree.collapseRow(row);
    	}
		
	}

	/**
	 * Returns a child of a given node that contains the same user object as 
	 * the one passed to the method.
	 * @param node
	 * @param userObject
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode findChildWithUserObject(DefaultMutableTreeNode node, Object userObject) {
		
		Enumeration<DefaultMutableTreeNode> children = node.children();
        while (children.hasMoreElements()) {
        	DefaultMutableTreeNode child = children.nextElement();
        	
        	if (child.getUserObject().equals(userObject)){
        		return child;
        	}
        }		
        return null;
	}

	/**
	 * 
	 */
	private JTree createTreeFromWorkflow(final Dataflow workflow){
		
		// Create a new tree and populate it with the workflow's data
		final JTree tree = new JTree(new WorkflowExplorerTreeModel(workflow));
		tree.setRowHeight(18);
		tree.setEditable(false);
		tree.setExpandsSelectedPaths(true);
		tree.setDragEnabled(false);
		tree.setScrollsOnExpand(false);
		tree.setCellRenderer(new WorkflowExplorerTreeCellRenderer());
		tree.setSelectionModel(new WorkflowExplorerTreeSelectionModel());
		tree.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent evt) {

				// Discover the tree row that was clicked on
				int selRow = tree.getRowForLocation(evt.getX(), evt
						.getY());
				if (selRow != -1) {
					// Get the selection path for the row
					TreePath selectionPath = tree.getPathForLocation(evt
							.getX(), evt.getY());
					if (selectionPath != null) {
						// Get the selected node
						DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectionPath
								.getLastPathComponent();

						// For both left and right click - add the workflow
						// object to selection model
						// This will cause the node to become selected (from
						// the selection listener's code)
						DataflowSelectionModel selectionModel = openedWorkflowsManager.getDataflowSelectionModel(
										workflow);

						// If the node that was clicked on was inputs,
						// outputs, processors,
						// datalinks or nested workflow root node than just
						// make it selected
						// and clear the selection model (these are just
						// containers for the 'real'
						// workflow components).
						// Also if the selected node is the root of the
						// nested workflow - do not
						// add to selection model as we do not want to show
						// contextual view for it
						if (selectedNode.getUserObject() instanceof String
								|| ((selectedNode.getUserObject() instanceof Dataflow) && (!selectedNode
										.isRoot()))) {
							selectionModel.clearSelection();
							((WorkflowExplorerTreeSelectionModel) tree
									.getSelectionModel())
									.mySetSelectionPath(selectionPath);
						} else { // a 'real' workflow component or the
									// 'whole' workflow (i.e. the tree root)
									// was clicked on
							selectionModel.addSelection(selectedNode
									.getUserObject());

							// If this was a right click - show a pop-up
							// menu as well if there is one defined
							if (evt.getButton() == MouseEvent.BUTTON3) {

								// Show a contextual pop-up menu
								JPopupMenu menu = menuManager
										.createContextMenu(workflow,
												selectedNode.getUserObject(),
												tree);
								menu.show(evt.getComponent(), evt.getX(),
										evt.getY());
							}
						}
					}
				}
			}
		});
		return tree;
	}
	
	/**
	 * Sets the currently selected node(s) based on the workflow selection
	 * model, i.e. the node(s) currently selected in the workflow graph view
	 * also become selected in the tree view.
	 */
	private void setSelectedNodes() {

		DataflowSelectionModel selectionModel = openedWorkflowsManager.getDataflowSelectionModel(workflow);

		// List of all selected objects in the graph view
		Set<Object> selection = selectionModel.getSelection();
		if (!selection.isEmpty()) {
			// Selection path(s) - can be multiple if more objects are selected
			int i = selection.size();
			TreePath[] paths = new TreePath[i];

			for (Iterator<Object> iterator = selection.iterator(); iterator
					.hasNext();) {
				TreePath path = WorkflowExplorerTreeModel.getPathForObject(
						iterator.next(), (DefaultMutableTreeNode) wfTree
								.getModel().getRoot());
				paths[--i] = path;
			}
			wfTree.setSelectionPaths(paths);
			wfTree.scrollPathToVisible(paths[0]);
		}
	}

	/**
	 * Expands all nodes in the workflow tree that have children.
	 */
	private void expandAll() {

		int row = 0;
		while (row < wfTree.getRowCount()) {
			wfTree.expandRow(row);
			row++;
		}
	}

	/**
	 * Observes events on workflow Selection Manager, i.e. when a workflow node
	 * is selected in the graph view.
	 */
	private final class DataflowSelectionListener implements
			Observer<DataflowSelectionMessage> {

		public void notify(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) throws Exception {

			setSelectedNodes();
		}
	}

}
