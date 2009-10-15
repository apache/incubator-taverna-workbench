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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

//import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowInputAction;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowOutputAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.dndhandler.ServiceTransferHandler;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
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
public class WorkflowExplorer extends WorkflowView {

	//private static Logger logger = Logger.getLogger(WorkflowExplorer.class);

	/* Purple colour for shaded label on pop up menus */
	public static final Color PURPLISH = new Color(0x8070ff);

	/* Manager of all opened workflows */
	private DataflowSelectionManager openedWorkflowsManager = DataflowSelectionManager
										.getInstance();

	private MenuManager menuManager = MenuManager.getInstance();

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
	private JScrollPane scrollPane;

	protected FileManager fileManager = FileManager.getInstance();
	protected FileManagerObserver fileManagerObserver = new FileManagerObserver();

	protected EditManager editManager = EditManager.getInstance();
	protected EditManagerObserver editManagerObserver = new EditManagerObserver();

	
	private static class WorkflowExplorerInstanceHolder {
		private static final WorkflowExplorer instance = new WorkflowExplorer();
	}
	

	/**
	 * Returns a WorkflowExplorer instance.
	 */
	public static WorkflowExplorer getInstance() {
		return WorkflowExplorerInstanceHolder.instance;
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
		
		super();
		this.setTransferHandler(new ServiceTransferHandler());

		// Create a tree that will represent a view over the current workflow
		// Initially, there is no workflow opened, so we create an empty tree,
		// but immediately after all visual components of the Workbench are
		// created (including Workflow Explorer) a new empty workflow is
		// created, which is represented with a NON-empty JTree with four nodes
		// (Inputs, Outputs, Processors, and Data links) that themselves have no
		// children.
		assignWfTree(new JTree(new DefaultMutableTreeNode("No workflow open")));

		// Start observing workflow switching or closing events on File Manager
		fileManager.addObserver(fileManagerObserver);
		
		// Start observing events on Edit Manager when current workflow is
		// edited (e.g. a node added, deleted or updated)
		editManager.addObserver(editManagerObserver);

		// Draw visual components
		initComponents();
	}
	
	private void assignWfTree(JTree tree) {
		wfTree = tree;
		wfTree.setTransferHandler(new ServiceTransferHandler());
	}

	/**
	 * Lays out the swing components.
	 */
	public void initComponents() {

		setLayout(new BorderLayout());

		// Workflow tree scroll pane
		scrollPane = new JScrollPane(wfTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(new EtchedBorder());

		// Title - not needed as it is now located on a tab labelled 'Workflow Explorer' 
		//JLabel wfExplorerLabel = new JLabel("Workflow Explorer");
		//wfExplorerLabel.setMinimumSize(new Dimension(0, 0)); // so that it can shrink completely
		//wfExplorerLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

		//add(wfExplorerLabel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

	}

	/**
	 * Gets called when a workflow is opened or a new (empty) one created.
	 */
	public void createWorkflowTree(Dataflow df) {

		// Set the current workflow
		workflow = df;

		// Create a new tree and populate it with the workflow's data
		assignWfTree(createTreeFromWorkflow(workflow));

		// Add the new tree to the list of opened workflow trees
		openedWorkflowsTrees.put(workflow, wfTree);
		
		// Expand the tree
		expandAll(wfTree);

		// Repaint the scroll pane containing the tree
		scrollPane.setViewportView(wfTree);
		scrollPane.revalidate();
		scrollPane.repaint();
	}

	/**
	 * Switch the current workflow to a previously opened workflow.
	 */
	private void switchWorkflowTree(Dataflow df) {
		
		// Set the current workflow to the one we have switched to
		workflow = df;
			
		// Set the tree for the current workflow
		wfTree = openedWorkflowsTrees.get(workflow);
		
		// Repaint the scroll pane containing the tree
		scrollPane.setViewportView(wfTree);
		
		// Select the node(s) that should be selected (do this after assigning the tree to the scroll pane)
		setSelectedNodes(wfTree, workflow);
		
		scrollPane.revalidate();
		scrollPane.repaint();										
	}
	
	/**
	 * Gets called when the current workflow is edited, or when a parent workflow of
	 * a nested workflow is edited due to saved changes in the nested workflow 
	 * (which is the current workflow).
	 */
	public void updateWorkflowTree(Dataflow df) {
		
		// Create the new tree from the updated workflow
		JTree newTree = createTreeFromWorkflow(df);
		
		// Get the old workflow tree
		JTree oldTree = openedWorkflowsTrees.get(df);
		
		// Update the tree in the list of opened workflow trees
		openedWorkflowsTrees.put(df, newTree);
		
		// Update the new tree's expansion state based on the old tree
		// i.e. all nodes in the old tree that have been expanded/collapsed 
		// should also be expanded/collapsed in the new tree (unless an 
		// expanded node has been removed)
		copyExpansionState(oldTree, (DefaultMutableTreeNode) oldTree.getModel().getRoot(), newTree, (DefaultMutableTreeNode) newTree.getModel().getRoot());
		
		// Get the current workflow from FileManager.
		// If current workflow is different from the workflow df passed through 
		// this method then this means that the current workflow is the nested workflow 
		// (whose parent is workflow df) and that the nested workflow has been previously 
		// edited and then saved which triggered the update on the parent workflow df. 
		// In this case, we should just update the parent workflow tree but keep the nested 
		// workflow as the current workflow. On the other hand, if the current workflow is 
		// the same as workflow df then this is just an update to the current workflow so 
		// we have to update and redraw the workflow tree.
		if (df.equals(fileManager.getCurrentDataflow())){ //this was an update on the current workflow
			
			// Update the current workflow
			workflow = df; // although they are the same anyway
			
			// Set the current tree to the new tree
			assignWfTree(newTree);
			
			// Repaint the scroll pane containing the tree
			scrollPane.setViewportView(wfTree);	
			
			// Select the node(s) that should be selected (do this after assigning the tree to the scroll pane)
			setSelectedNodes(wfTree, workflow);
			
			scrollPane.revalidate();
			scrollPane.repaint();
		}
		else{ // just update the parent tree (already done above) but do not switch the trees

			// Do nothing
			
			// Do not revalidate/repaint as we are not switching to the 
			// new tree but keep showing the nested wf that has not changed
		}
	}
	
	/**
	 * Copies the expansion state of the old tree starting from the given node in the old tree to
	 * the new tree starting from the new node. We normally use it starting from the root nodes of
	 * both trees when an update has happened to the tree and we want to preserve the expansion state 
	 * in the updated tree. 
	 */
	@SuppressWarnings("unchecked")
	private void copyExpansionState(JTree oldTree, DefaultMutableTreeNode oldNode, JTree newTree,  DefaultMutableTreeNode newNode) {
		
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
        		copyExpansionState(oldTree, oldChild, newTree, newChild);
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
    		int row = newTree.getRowForPath(new TreePath(newNode.getPath()));
    		newTree.expandRow(row);
        }
        else if (oldNode.isLeaf()){ // if it is a leaf - expand/collapse does not work, so use isVisible/makeVisible
        	if (oldTree.isVisible(new TreePath(oldNode.getPath()))){
        		newTree.makeVisible(new TreePath(newNode.getPath()));
        	}
        }
        else if (oldTree.isExpanded(new TreePath(oldNode.getPath()))){
    		int row = newTree.getRowForPath(new TreePath(newNode.getPath()));
    		newTree.expandRow(row);
    	}
    	else{ // node was collapsed
    		int row = newTree.getRowForPath(new TreePath(newNode.getPath()));
    		newTree.collapseRow(row);
    	}
	}

	/**
	 * Returns a child of a given node that contains the same user object as 
	 * the one passed to the method.
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
//		tree.setSelectionModel(new WorkflowExplorerTreeSelectionModel());
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				TreePath selectionPath = e.getNewLeadSelectionPath();
				if (selectionPath != null) {
					final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectionPath
					.getLastPathComponent();

			DataflowSelectionModel selectionModel = openedWorkflowsManager.getDataflowSelectionModel(
							workflow);

			// If the node that was clicked on was inputs,
			// outputs, services, data links, control links or 
			// merges in the main workflow then just make it selected
			// and clear the selection model (as these are just
			// containers for the 'real' workflow components).
			if ((selectedNode.getUserObject() instanceof String) && (selectionPath.getPathCount() == 2)) {
				selectionModel.clearSelection();
				tree
						.getSelectionModel()
						.setSelectionPath(selectionPath);

			} else { // a 'real' workflow component or the 'whole' workflow (i.e. the tree root) was clicked on
				
				// We want to disable selection of any nested workflow components (apart from
				// input and output ports in the wrapping DataflowActivity)
				TreePath path = WorkflowExplorerTreeModel.getPathForObject(selectedNode
						.getUserObject(), (DefaultMutableTreeNode)tree.getModel().getRoot());
				
				// The getPathForObject() method will return null in a node is inside 
				// a nested workflow and should not be selected
				if (path == null){
					// Just return 
					return;
				}
				else{
					// Add it to selection model so it is also selected on the graph as well
					// that listens to the selection model
					selectionModel.addSelection(selectedNode
							.getUserObject());		}
			}
				}}
			
		});
		
		tree.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent evt) {
				
				if (evt.getButton() != MouseEvent.BUTTON3) {
					return;
				}

				// Discover the tree row that was clicked on
				int selRow = tree.getRowForLocation(evt.getX(), evt
						.getY());
				if (selRow != -1) {
					// Get the selection path for the row
					TreePath selectionPath = tree.getPathForLocation(evt
							.getX(), evt.getY());
					if (selectionPath != null) {
						// Get the selected node
						final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectionPath
								.getLastPathComponent();

						// For both left and right click - add the workflow
						// object to selection model
						// This will cause the node to become selected (from
						// the selection listener's code)
						DataflowSelectionModel selectionModel = openedWorkflowsManager.getDataflowSelectionModel(
										workflow);

						// If the node that was clicked on was inputs,
						// outputs, services, data links, control links or 
						// merges in the main workflow then just make it selected
						// and clear the selection model (as these are just
						// containers for the 'real' workflow components).
						if ((selectedNode.getUserObject() instanceof String) && (selectionPath.getPathCount() == 2)) {
							selectionModel.clearSelection();
							tree
									.getSelectionModel()
									.setSelectionPath(selectionPath);

							// If this was a right click - show a pop-up
							// menu as well if there is one defined
							if (evt.getButton() == MouseEvent.BUTTON3) {
								if (selectedNode.getUserObject().equals(WorkflowExplorerTreeModel.PROCESSORS)){
									JPopupMenu menu = new JPopupMenu();
									menu.add(new ShadedLabel("Tree", PURPLISH));
									menu.add(new JMenuItem(new AbstractAction("Expand", WorkbenchIcons.plusIcon) {
										public void actionPerformed(ActionEvent evt) {
											expandAscendants(tree, selectedNode);
										}
									}));
									menu.add(new JMenuItem(new AbstractAction("Collapse", WorkbenchIcons.minusIcon) {
										public void actionPerformed(ActionEvent evt) {
											collapseAscendants(tree, selectedNode);
										}
									}));
									menu.show(evt.getComponent(), evt.getX(),
											evt.getY());
								}
								else if (selectedNode.getUserObject().equals(WorkflowExplorerTreeModel.INPUTS)){
									JPopupMenu menu = new JPopupMenu();
									menu.add(new ShadedLabel("Workflow input ports", ShadedLabel.GREEN));
									menu.add(new JMenuItem(new AbstractAction("Add workflow input port", WorkbenchIcons.inputIcon) {

										public void actionPerformed(ActionEvent evt) {
											new AddDataflowInputAction((Dataflow) ((DefaultMutableTreeNode) tree.getModel().getRoot()).getUserObject(), wfTree.getParent()).actionPerformed(evt);
										}
									}));
									menu.show(evt.getComponent(), evt.getX(),
											evt.getY());
								}
								else if (selectedNode.getUserObject().equals(WorkflowExplorerTreeModel.OUTPUTS)){ 
									JPopupMenu menu = new JPopupMenu();
									menu.add(new ShadedLabel("Workflow output ports", ShadedLabel.GREEN));
									menu.add(new JMenuItem(new AbstractAction("Add workflow output port", WorkbenchIcons.outputIcon) {
										public void actionPerformed(ActionEvent evt) {
											new AddDataflowOutputAction((Dataflow) ((DefaultMutableTreeNode) tree.getModel().getRoot()).getUserObject(), wfTree.getParent()).actionPerformed(evt);
										}
									}));
									menu.show(evt.getComponent(), evt.getX(),
											evt.getY());
								}
							}
							
						} else { // a 'real' workflow component or the 'whole' workflow (i.e. the tree root) was clicked on
							
							// We want to disable selection of any nested workflow components (apart from
							// input and output ports in the wrapping DataflowActivity)
							TreePath path = WorkflowExplorerTreeModel.getPathForObject(selectedNode
									.getUserObject(), (DefaultMutableTreeNode)tree.getModel().getRoot());
							
							// The getPathForObject() method will return null in a node is inside 
							// a nested workflow and should not be selected
							if (path == null){
								// Just return 
								return;
							}
							else{
								// Add it to selection model so it is also selected on the graph as well
								// that listens to the selection model
								selectionModel.addSelection(selectedNode
										.getUserObject());

								// If this was a right click - show a pop-up
								// menu as well if there is one defined
								if (evt.getButton() == MouseEvent.BUTTON3) {

									// Show a contextual pop-up menu
									JPopupMenu menu = menuManager
											.createContextMenu(workflow,
													selectedNode.getUserObject(),
													wfTree.getParent());
									if (menu == null) {
										menu = new JPopupMenu();
									}
									if (selectedNode.getUserObject() instanceof Dataflow){
										menu.add(new ShadedLabel("Tree", PURPLISH));
										// Action to expand the whole tree
										menu.add(new JMenuItem(new AbstractAction("Expand all", WorkbenchIcons.plusIcon) {
											public void actionPerformed(ActionEvent evt) {
												expandAll(tree);
											}
										}));
										// Action to collapse the whole tree
										menu.add(new JMenuItem(new AbstractAction("Collapse all", WorkbenchIcons.minusIcon) {
											public void actionPerformed(ActionEvent evt) {
												collapseAll(tree);
											}
										}));
									}
									
									menu.show(evt.getComponent(), evt.getX(),
											evt.getY());
								}
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
	private void setSelectedNodes(JTree tree, Dataflow wf) {

		DataflowSelectionModel selectionModel = openedWorkflowsManager.getDataflowSelectionModel(wf);

		// List of all selected objects in the graph view
		Set<Object> selection = selectionModel.getSelection();
		if (!selection.isEmpty()) {
			// Selection path(s) - can be multiple if more objects are selected
			int i = selection.size();
			TreePath[] paths = new TreePath[i];

			for (Object selected : selection) {
				TreePath path = WorkflowExplorerTreeModel.getPathForObject(
						selected, (DefaultMutableTreeNode) tree
								.getModel().getRoot());
				paths[--i] = path;
			}
			tree.setSelectionPaths(paths);
			tree.scrollPathToVisible(paths[0]);
			//revalidate();
			//repaint();			
		}
	}

	/**
	 * Expands all nodes in the tree that have children.
	 */
	private void expandAll(JTree tree) {

		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}
	
	/**
	 * Collapses all but the root node in the tree that have children.
	 */
	private void collapseAll(JTree tree) {

		int row = 1;
		while (row < tree.getRowCount()) {
			tree.collapseRow(row);
			row++;
		}
	}

	/**
	 * Expands all ascendants of a node in the tree.
	 */
	@SuppressWarnings("unchecked")
	private void expandAscendants(JTree tree, DefaultMutableTreeNode node) {

	    Enumeration<DefaultMutableTreeNode> children = node.children();
        while (children.hasMoreElements()) {
        	DefaultMutableTreeNode child = children.nextElement();
        	if (child.isLeaf()){
        		tree.makeVisible(new TreePath(child.getPath()));
        	}
        	else{
        		expandAscendants(tree, child);
        	}
        }
	}
	
	/**
	 * Collapses all direct ascendants of a node in the tree.
	 */
	@SuppressWarnings("unchecked")
	private void collapseAscendants(JTree tree, DefaultMutableTreeNode node) {

	    Enumeration<DefaultMutableTreeNode> children = node.children();
        while (children.hasMoreElements()) {
        	DefaultMutableTreeNode child = children.nextElement();
    		int row = tree.getRowForPath(new TreePath(child.getPath()));
    		tree.collapseRow(row);
        }
	}
	
	/**
	 * Update workflow explorer when current dataflow changes or closes.
	 *
	 */
	public class FileManagerObserver implements Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			
			if (message instanceof SetCurrentDataflowEvent) { // switched the current workflow 
				// Remove the workflow selection model listener from the 
				// previous (if any) and add to the new workflow (if any)
				Dataflow oldWF = workflow; // previous workflow
				final Dataflow newWF = ((SetCurrentDataflowEvent) message).getDataflow(); // the newly switched to workflow
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
						if (openedWorkflowsTrees.containsKey(newWF)){
							switchWorkflowTree(newWF);
						}
						else{ // otherwise create a new tree for the workflow
							createWorkflowTree(newWF);
						}
					}
				}.start();
			}
			else if (message instanceof ClosedDataflowEvent) { //closed the current workflow
				// Remove the closed workflow tree from the map of opened workflow trees
				openedWorkflowsTrees.remove(((ClosedDataflowEvent) message).getDataflow());
			} 
		}
	}


	/**
	 * Update workflow tree on edits to the workflow. Gets called when either 
	 * current workflow is edited or when current workflow is a nested workflow
	 * that had been edited and then saved which will trigger update to the parent
	 * workflow which is not the current workflow.
	 *
	 */
	public class EditManagerObserver implements Observer<EditManagerEvent> {
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
						// Update the workflow tree to reflect the changes
						updateWorkflowTree(((AbstractDataflowEditEvent) message).getDataFlow());
					}
				}.start();
			}
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

			setSelectedNodes(wfTree, workflow);
			scrollPane.revalidate();
			scrollPane.repaint();
		}
	}

}
