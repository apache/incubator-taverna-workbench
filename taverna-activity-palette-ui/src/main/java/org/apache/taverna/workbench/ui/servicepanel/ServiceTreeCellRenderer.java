/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.servicepanel;

import static org.apache.taverna.workbench.activityicons.DefaultActivityIcon.getDefaultIcon;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;

import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.workbench.ui.servicepanel.tree.FilterTreeCellRenderer;
import org.apache.taverna.workbench.ui.servicepanel.tree.FilterTreeNode;

@SuppressWarnings("serial")
public class ServiceTreeCellRenderer extends FilterTreeCellRenderer {
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component result = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);
		if (result instanceof ServiceTreeCellRenderer
				&& value instanceof FilterTreeNode
				&& ((FilterTreeNode) value).getUserObject() instanceof ServiceDescription)
			prettifyServiceTreeCell((ServiceTreeCellRenderer) result,
					(ServiceDescription) ((FilterTreeNode) value)
							.getUserObject());
		else {
			// Commented out - these are ugly, use the default folder icons instead
			/*
			 * if (expanded) { ((ServiceTreeCellRenderer) result)
			 * .setIcon(WorkbenchIcons.folderOpenIcon); } else {
			 * ((ServiceTreeCellRenderer) result)
			 * .setIcon(WorkbenchIcons.folderClosedIcon); }
			 */
		}
		return result;
	}

	private void prettifyServiceTreeCell(ServiceTreeCellRenderer renderer,
			ServiceDescription item) {
		String name = item.getName();
		if (getFilter() != null)
			name = getFilter().filterRepresentation(name);
		// serviceTreeCellRenderer.setForeground(Color.red);
		String displayName = name;

		String textualDescription = item.getDescription();
		if (textualDescription != null && !textualDescription.isEmpty())
			displayName = displayName + " - " + textualDescription;
		renderer.setText(displayName);

		Icon activityIcon = item.getIcon();
		renderer.setIcon(activityIcon != null ? activityIcon
				: getDefaultIcon());
	}
}
