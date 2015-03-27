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

package org.apache.taverna.workbench.views.graph.menu;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_MINUS;
import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.DesignOrResultsAction;
import org.apache.taverna.workbench.icons.WorkbenchIcons;

@SuppressWarnings("serial")
public class ZoomOutAction extends AbstractAction implements
		DesignOrResultsAction {
	private static Action designAction = null;
	@SuppressWarnings("unused")
	private static Action resultsAction = null;

	public static void setResultsAction(Action resultsAction) {
		ZoomOutAction.resultsAction = resultsAction;
	}

	public static void setDesignAction(Action designAction) {
		ZoomOutAction.designAction = designAction;
	}

	ZoomOutAction() {
		super("Zoom out", WorkbenchIcons.zoomOutIcon);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_MINUS, getDefaultToolkit()
						.getMenuShortcutKeyMask()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		if (isWorkflowPerspective() && (designAction != null))
			designAction.actionPerformed(e);
//		else if (isResultsPerspective() && (resultsAction != null))
//			resultsAction.actionPerformed(e);
	}
}
