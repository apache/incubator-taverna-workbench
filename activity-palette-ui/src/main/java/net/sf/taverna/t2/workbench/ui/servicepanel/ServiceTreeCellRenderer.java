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
package net.sf.taverna.t2.workbench.ui.servicepanel;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workbench.activityicons.DefaultActivityIcon;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeCellRenderer;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeNode;

@SuppressWarnings("serial")
public class ServiceTreeCellRenderer extends FilterTreeCellRenderer {

	@SuppressWarnings("unchecked")
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		Component result = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);
		if (result instanceof ServiceTreeCellRenderer
				&& value instanceof FilterTreeNode
				&& ((FilterTreeNode) value).getUserObject() instanceof ServiceDescription) {
			ServiceTreeCellRenderer serviceTreeCellRenderer = (ServiceTreeCellRenderer) result;
			ServiceDescription item = (ServiceDescription) ((FilterTreeNode) value)
					.getUserObject();
			String name = item.getName();
			if (getFilter() != null) {
				name = getFilter().filterRepresentation(name);
			}
//			serviceTreeCellRenderer.setForeground(Color.red);
			String displayName = name;
			String textualDescription = item.getDescription();
			if ((textualDescription != null) && !textualDescription.equals("")) {
				displayName = displayName + " - " + textualDescription;
			}
			serviceTreeCellRenderer.setText(displayName);
			Icon activityIcon = item.getIcon();
			if (activityIcon != null) {
				serviceTreeCellRenderer.setIcon(activityIcon);
			} else {
				serviceTreeCellRenderer.setIcon(DefaultActivityIcon.getDefaultIcon());				
			}
		} else {
			// Commented out - these are ugly, use the default folder icons
			// instead
			/*
			 * if (expanded) { ((ServiceTreeCellRenderer) result)
			 * .setIcon(WorkbenchIcons.folderOpenIcon); } else {
			 * ((ServiceTreeCellRenderer) result)
			 * .setIcon(WorkbenchIcons.folderClosedIcon); }
			 */
		}
		return result;
	}

}
