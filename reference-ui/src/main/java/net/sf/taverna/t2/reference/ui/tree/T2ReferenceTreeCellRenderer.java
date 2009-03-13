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
package net.sf.taverna.t2.reference.ui.tree;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.reference.T2Reference;

/**
 * A cell renderer for the T2ReferenceTreeModel, using icons from Eclipse to
 * differentiate between error documents, reference sets and lists, and using
 * the toURI method on T2Reference to get the text.
 * 
 * @author Tom Oinn
 * 
 */
public class T2ReferenceTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -4366082340946921832L;

	private ImageIcon referenceSetIcon = new ImageIcon(getClass().getResource(
			"/icons/det_pane_hide.gif"));
	private ImageIcon errorDocumentIcon = new ImageIcon(getClass().getResource(
			"/icons/errorwarning_tab.gif"));

	@Override
	public synchronized Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		if (value instanceof T2Reference) {
			T2Reference ref = (T2Reference) value;
			switch (ref.getReferenceType()) {
			case IdentifiedList:
				break;
			case ReferenceSet:
				setIcon(referenceSetIcon);
				break;
			case ErrorDocument:
				setIcon(errorDocumentIcon);
				break;
			}
			setText(ref.toUri().toASCIIString());
		}
		return this;
	}

}
