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
/*

package org.apache.taverna.workbench.views.graph.menu;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.zoomInIcon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.DesignOrResultsAction;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ZoomInAction extends AbstractAction implements
		DesignOrResultsAction {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ZoomInAction.class);
	private static Action designAction = null;
	@SuppressWarnings("unused")
	private static Action resultsAction = null;

	public static void setResultsAction(Action resultsAction) {
		ZoomInAction.resultsAction = resultsAction;
	}

	public static void setDesignAction(Action designAction) {
		ZoomInAction.designAction = designAction;
	}

	ZoomInAction() {
		super("Zoom in", zoomInIcon);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_EQUALS, getDefaultToolkit()
						.getMenuShortcutKeyMask()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		if (isWorkflowPerspective()) {
//			if (designAction != null)
				designAction.actionPerformed(e);
//			else
//				logger.error("ZoomInAction.designAction is null");
//		} else if (isResultsPerspective() && (resultsAction != null))
//			resultsAction.actionPerformed(e);
	}
}
