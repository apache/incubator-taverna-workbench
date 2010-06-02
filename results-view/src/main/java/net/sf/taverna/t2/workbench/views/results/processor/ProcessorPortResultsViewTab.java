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
package net.sf.taverna.t2.workbench.views.results.processor;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * A tab containing result tree for an input or output port of a processor 
 * and a panel with rendered result
 * of the currently selected node in the tree.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ProcessorPortResultsViewTab extends JPanel{
	
	// Rendered result component
	private RenderedProcessorResultComponent renderedResultComponent;

	private boolean isOutputPortTab = true;

	private JTree resultsTree;

	private String portName;

	// Panel holding the results tree
	private JPanel treePanel;

	// Split pane holding the result tree panel on the left 
	// and rendering component on the right
	private JSplitPane splitPanel;

	public ProcessorPortResultsViewTab(String portName) {
		this.portName = portName;
		initComponents();
	}

	private void initComponents() {
		
		setLayout(new BorderLayout());

		// Split pane containing a tree with results from an output port for the selected enactment and 
		// rendered result component for rendering an individual result currently selected 
		// from the results tree.
		splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		// Results tree (containing T2References to all individual results for this port)
		//resultsTree = new JTree(); // initially tree is empty - will be set when user selects a particular enactment

		// Component for rendering individual results
		renderedResultComponent = new RenderedProcessorResultComponent(); 

		treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout());
		treePanel.add(new JLabel("Click to view values"), BorderLayout.NORTH);
		treePanel.add(new JScrollPane(resultsTree), BorderLayout.CENTER);
		splitPanel.setTopComponent(treePanel);
		splitPanel.setBottomComponent(renderedResultComponent);
		splitPanel.setDividerLocation(400);
		
		// Add all to main panel
		add(splitPanel, BorderLayout.CENTER);
		
	}

	public void setIsOutputPortTab(boolean isOutputPortTab) {
		this.isOutputPortTab  = isOutputPortTab;
	}
	
	public boolean getIsOutputPortTab() {
		return this.isOutputPortTab;
	}

	public void setResultsTree(JTree tree) {

		resultsTree = tree;

		treePanel.removeAll();
		treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout());
		if (tree == null) {
			splitPanel.setVisible(false);
			revalidate();
			return;
		} else {
			splitPanel.setTopComponent(treePanel);
			splitPanel.setBottomComponent(renderedResultComponent);
			splitPanel.setVisible(true);
		}
		
		treePanel.add(new JLabel("Click to view values"), BorderLayout.NORTH);
		treePanel.add(new JScrollPane(resultsTree), BorderLayout.CENTER);
		splitPanel.setTopComponent(treePanel);
		TreeModel treeModel = tree.getModel();
		
		if (treeModel.getChildCount(treeModel.getRoot()) == 1) {
			Object child = treeModel.getChild(treeModel.getRoot(), 0);
			if (treeModel.getChildCount(child) ==0) {
				Object[] objectPath = new Object[]{
						treeModel.getRoot(), 
						child
				};
				TreePath path = new TreePath(objectPath);
				tree.setSelectionPath(path);
				splitPanel.setTopComponent(new JPanel());
				splitPanel.setDividerLocation(0);
			}
		}
		
		revalidate();
		

		
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getPortName() {
		return portName;
	}

	public RenderedProcessorResultComponent getRenderedResultComponent() {
		return renderedResultComponent;
	}
}

