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

package org.apache.taverna.workbench.ui.views.contextualviews.merge;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workflow.edits.ReorderMergePositionsEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.DataLink;

/**
 * Configuration action for a Merge. This action changes the order of
 * merge's incoming ports.
 *
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
class MergeConfigurationAction extends AbstractAction {
	private static Logger logger = Logger
			.getLogger(MergeConfigurationAction.class);

	private final List<DataLink> reorderedDataLinksList;
	private final List<DataLink> datalinks;
	private final EditManager editManager;
	private final SelectionManager selectionManager;

	MergeConfigurationAction(List<DataLink> datalinks,
			List<DataLink> reorderedDataLinksList, EditManager editManager,
			SelectionManager selectionManager) {
		this.datalinks = datalinks;
		this.reorderedDataLinksList = reorderedDataLinksList;
		this.editManager = editManager;
		this.selectionManager = selectionManager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ReorderMergePositionsEdit edit = new ReorderMergePositionsEdit(
				datalinks, reorderedDataLinksList);

		WorkflowBundle bundle = selectionManager.getSelectedWorkflowBundle();

		try {
			editManager.doDataflowEdit(bundle, edit);
		} catch (IllegalStateException ex1) {
			logger.error("Could not configure merge", ex1);
		} catch (EditException ex2) {
			logger.error("Could not configure merge", ex2);
		}
	}
}
