/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workbench.iterationstrategy.editor;

import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;
import org.apache.taverna.workbench.iterationstrategy.IterationStrategyIcons;
import org.apache.taverna.workbench.ui.zaria.UIComponentSPI;

@SuppressWarnings("serial")
public class IterationStrategyTree extends JTree implements UIComponentSPI {

	private IterationStrategyTopNode strategy = null;

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
			IterationStrategyTopNode strategy2) {
		if (strategy2 != this.strategy) {
			this.strategy = strategy2;
			TreeNode terminal = strategy2.getTerminalNode();
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
