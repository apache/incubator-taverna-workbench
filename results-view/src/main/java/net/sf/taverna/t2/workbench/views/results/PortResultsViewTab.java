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
package net.sf.taverna.t2.workbench.views.results;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.MutableTreeNode;

import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Port;

/**
 * A tab containing result tree for an output port and a panel with rendered result
 * of the currently selected node in the tree.
 * 
 * @author Alex Nenadic
 *
 */
public class PortResultsViewTab extends JPanel{
	
	private static final long serialVersionUID = -5531195402446371947L;

	// Output port this panel is displaying results for
	private Port dataflowOutputPort;
	
	// Tree model of results
	private ResultTreeModel resultModel;
	
	// Rendered result component
	private RenderedResultComponent renderedResultComponent;
	
	public PortResultsViewTab(Port dataflowOutputPort){
		super(new BorderLayout());

		this.dataflowOutputPort = dataflowOutputPort;
		
		initComponents();
	}

	private void initComponents() {
		
		// Split pane containing a tree with all results from an output port and 
		// rendered result component for individual result rendered currently selected 
		// from the tree
		JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		// Results tree (containing T2References to all individual results for this port)
		resultModel =  new ResultTreeModel(dataflowOutputPort.getName(),
				dataflowOutputPort.getDepth());

		final JTree tree = new JTree(getResultModel());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setExpandsSelectedPaths(true);
		tree.setLargeModel(true);
		tree.setRootVisible(false);
		tree.setCellRenderer(new PortResultCellRenderer());
		
		// Component for rendering individual results
		renderedResultComponent = new RenderedResultComponent(); 

		tree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				TreePath selectionPath = e.getNewLeadSelectionPath();
				if (selectionPath != null) {
					// Get the selected node
					final Object selectedNode = selectionPath.getLastPathComponent();				
					renderedResultComponent.setNode((ResultTreeNode)selectedNode);
				}
			}
			
		});

		getResultModel().addTreeModelListener(new TreeModelListener() {

			public void treeNodesChanged(TreeModelEvent e) {
				tree.expandPath(e.getTreePath());
				tree.scrollPathToVisible(e.getTreePath());
			}

			public void treeNodesInserted(TreeModelEvent e) {
			}

			public void treeNodesRemoved(TreeModelEvent e) {
			}

			public void treeStructureChanged(TreeModelEvent e) {
			}
		});
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(new JLabel("Click to view results"), BorderLayout.NORTH);
		leftPanel.add(new JScrollPane(tree), BorderLayout.CENTER);
		splitPanel.setTopComponent(leftPanel);
		splitPanel.setBottomComponent(renderedResultComponent);
		splitPanel.setDividerLocation(400);
		
		// Add all to main panel
		add(splitPanel, BorderLayout.CENTER);
		
	}

	/**
	 * @return the resultModel
	 */
	public ResultTreeModel getResultModel() {
		return resultModel;
	}
}
