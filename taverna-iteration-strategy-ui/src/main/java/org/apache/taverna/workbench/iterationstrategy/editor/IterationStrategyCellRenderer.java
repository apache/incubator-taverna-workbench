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
package org.apache.taverna.workbench.iterationstrategy.editor;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.taverna.workbench.icons.WorkbenchIcons;
import org.apache.taverna.workbench.iterationstrategy.IterationStrategyIcons;
import org.apache.taverna.workflowmodel.processor.iteration.CrossProduct;
import org.apache.taverna.workflowmodel.processor.iteration.DotProduct;
import org.apache.taverna.workflowmodel.processor.iteration.NamedInputPortNode;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
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
			if (!leaf){
				if (expanded) {
					setIcon(WorkbenchIcons.folderOpenIcon);
				} else {
					setIcon(WorkbenchIcons.folderClosedIcon);
				}
			}
		}
		return this;
	}
}
