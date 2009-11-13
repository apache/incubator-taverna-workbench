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
/**
 * 
 */
package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.workbench.iterationstrategy.IterationStrategyIcons;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;

import org.apache.log4j.Logger;

final class IterationStrategyCellRenderer extends DefaultTreeCellRenderer {

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(IterationStrategyCellRenderer.class);

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded,
				leaf, row, hasFocus);
		if (value instanceof CrossProduct) {
			setIcon(IterationStrategyIcons.joinIteratorIcon);
			setText("Cross product");
		} else if (value instanceof DotProduct) {
			setIcon(IterationStrategyIcons.lockStepIteratorIcon);
			setText("Dot product");
		} else if (value instanceof NamedInputPortNode) {
			setIcon(IterationStrategyIcons.leafnodeicon);
			NamedInputPortNode namedInput = (NamedInputPortNode) value;
			setText(namedInput.getPortName());
		} else {
			setText("List handling");
		}
		return this;
	}
}
