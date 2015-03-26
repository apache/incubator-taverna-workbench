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

package org.apache.taverna.workbench.parallelize;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.Processor;

public class ParallelizeConfigureMenuAction extends AbstractContextualMenuAction {

	public static final URI configureRunningSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configureRunning");

	private static final URI PARALLELIZE_CONFIGURE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/parallelizeConfigure");

	public static URI TYPE = URI.create("http://ns.taverna.org.uk/2010/scufl2/taverna/dispatchlayer/Parallelize");

	private EditManager editManager;

	private SelectionManager selectionManager;

	public ParallelizeConfigureMenuAction() {
		super(configureRunningSection, 10, PARALLELIZE_CONFIGURE_URI);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Parallel jobs...") {
			public void actionPerformed(ActionEvent e) {
				Processor processor = (Processor) getContextualSelection().getSelection();
				ParallelizeConfigureAction parallelizeConfigureAction = new ParallelizeConfigureAction(
						null, null, processor, editManager, selectionManager);
				parallelizeConfigureAction.actionPerformed(e);
			}
		};
	}

	public boolean isEnabled() {
		return super.isEnabled() && (getContextualSelection().getSelection() instanceof Processor);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
