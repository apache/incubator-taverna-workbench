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

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.workbench.iterationstrategy.IterationStrategyIcons;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;

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
			IterationStrategyImpl theStrategy) {
		if (theStrategy != this.strategy) {
			this.strategy = theStrategy;
			TreeNode terminal = theStrategy.getTerminalNode();
			setModel(new DefaultTreeModel(terminal));
			revalidate();
		}
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

}
