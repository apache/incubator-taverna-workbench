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
package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.taverna.t2.workbench.iterationstrategy.IterationStrategyIcons;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;

public class IterationStrategyTree extends JTree implements UIComponentSPI {

	private IterationStrategy strategy = null;

	public IterationStrategyTree() {
		super();
		setCellRenderer(new IterationStrategyCellRenderer());
	}

	public ImageIcon getIcon() {
		return IterationStrategyIcons.leafnodeicon;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		this.strategy = null;
		setModel(null);
	}

	public synchronized void setIterationStrategy(
			IterationStrategy theStrategy) {
		if (theStrategy != this.strategy) {
			this.strategy = theStrategy;
			TreeNode terminal = theStrategy.getTerminalNode();
			setModel(new DefaultTreeModel(terminal));
			this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			expandTree();
			revalidate();
		}
	}
	
	protected synchronized void refreshModel() {
		this.getModel().nodeStructureChanged(strategy.getTerminalNode());
		expandTree();
	}

	@Override
	public DefaultTreeModel getModel() {
		return (DefaultTreeModel) super.getModel();
	}

	@Override
	public void setModel(TreeModel newModel) {
		if (newModel != null && !(newModel instanceof DefaultTreeModel)) {
			throw new IllegalArgumentException(
					"Model must be a DefaultTreeModel");
		}
		super.setModel(newModel);
	}

	protected void expandTree() {  
		DefaultMutableTreeNode root =  
	        (DefaultMutableTreeNode)this.getModel().getRoot();  
	    Enumeration e = root.breadthFirstEnumeration();  
	    while(e.hasMoreElements()) {  
	        DefaultMutableTreeNode node =  
	            (DefaultMutableTreeNode)e.nextElement();  
	        if(node.isLeaf()) continue;  
	        int row = this.getRowForPath(new TreePath(node.getPath()));  
	        this.expandRow(row);  
	    }  
	}

}
