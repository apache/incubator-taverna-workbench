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
package org.apache.taverna.workbench.ui.servicepanel.tree;

import static org.apache.taverna.workbench.icons.WorkbenchIcons.folderClosedIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.folderOpenIcon;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
public class FilterTreeCellRenderer extends DefaultTreeCellRenderer {
	private Filter filter = null;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		Filter filter = getFilter();
		if (filter != null)
			setText(filter.filterRepresentation(getText()));
		if (expanded)
			setIcon(folderOpenIcon);
		else
			setIcon(folderClosedIcon);
		return this;
	}

	public Filter getFilter() {
		return filter;
	}

	public void setFilter(Filter currentFilter) {
		this.filter = currentFilter;
	}
}
