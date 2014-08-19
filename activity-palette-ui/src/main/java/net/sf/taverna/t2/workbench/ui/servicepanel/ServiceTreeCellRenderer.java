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
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workbench.activityicons.DefaultActivityIcon;
import net.sf.taverna.t2.workbench.ui.servicepanel.servicetree.ServiceTreeNode;

@SuppressWarnings("serial")
public class ServiceTreeCellRenderer extends DefaultTreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(final JTree tree,
			final Object value, final boolean sel, final boolean expanded,
			final boolean leaf, final int row, final boolean hasFocus) {

		final Component result = super.getTreeCellRendererComponent(tree,
				value, sel, expanded, leaf, row, hasFocus);
		if (!(result instanceof JLabel)) {
			return result;
		}
		final JLabel l = (JLabel) result;
		if (!(value instanceof ServiceTreeNode)) {
			return l;
		}
		final ServiceTreeNode node = (ServiceTreeNode) value;

		final Object o = node.getUserObject();
		if (!(o instanceof ServiceDescription)) {
			return l;
		}
		final ServiceDescription<?> sd = (ServiceDescription<?>) o;

		String displayName = sd.getName();
		final String textualDescription = sd.getDescription();
		if ((textualDescription != null) && !textualDescription.isEmpty()) {
			displayName = displayName + " - " + textualDescription;
		}
		l.setText(displayName);

		final Icon activityIcon = sd.getIcon();
		if (activityIcon != null) {
			l.setIcon(activityIcon);
		} else {
			l.setIcon(DefaultActivityIcon.getDefaultIcon());
		}
		return l;
	}

}
