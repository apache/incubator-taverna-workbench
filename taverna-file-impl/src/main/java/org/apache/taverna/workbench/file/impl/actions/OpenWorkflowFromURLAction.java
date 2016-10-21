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
package org.apache.taverna.workbench.file.impl.actions;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_L;
import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.openurlIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;

import org.apache.taverna.workbench.file.FileManager;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * An action for opening a workflow from a url.
 * 
 * @author David Withers
 */
public class OpenWorkflowFromURLAction extends AbstractAction {
	private static final long serialVersionUID = 1474356457949961974L;
	private static Logger logger = Logger
			.getLogger(OpenWorkflowFromURLAction.class);
	private static Preferences prefs = Preferences
			.userNodeForPackage(OpenWorkflowFromURLAction.class);
	private static final String PREF_CURRENT_URL = "currentUrl";
	private static final String ACTION_NAME = "Open workflow location...";
	private static final String ACTION_DESCRIPTION = "Open a workflow from the web into a new workflow";

	private Component component;
	private FileManager fileManager;

	public OpenWorkflowFromURLAction(final Component component,
			FileManager fileManager) {
		this.component = component;
		this.fileManager = fileManager;
		putValue(SMALL_ICON, openurlIcon);
		putValue(NAME, ACTION_NAME);
		putValue(SHORT_DESCRIPTION, ACTION_DESCRIPTION);
		putValue(MNEMONIC_KEY, VK_L);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_L, getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String currentUrl = prefs.get(PREF_CURRENT_URL, "http://");

		final String url = (String) showInputDialog(component,
				"Enter the URL of a workflow definition to load",
				"Workflow URL", QUESTION_MESSAGE, null, null, currentUrl);
		if (url != null)
			new Thread("OpenWorkflowFromURLAction") {
				@Override
				public void run() {
					openFromURL(url);
				}
			}.start();
	}

	private void openFromURL(String urlString) {
		try {
			URL url = new URL(urlString);

			Object canonicalSource = fileManager.getCanonical(url);
			WorkflowBundle alreadyOpen = fileManager
					.getDataflowBySource(canonicalSource);
			if (alreadyOpen != null) {
				/*
				 * The workflow from the same source is already opened - ask the
				 * user if they want to switch to it or open another copy.
				 */

				Object[] options = { "Switch to opened", "Open new copy",
						"Cancel" };
				int iSelected = showOptionDialog(
						null,
						"The workflow from the same location is already opened.\n"
								+ "Do you want to switch to it or open a new copy?",
						"File Manager Alert", YES_NO_CANCEL_OPTION,
						QUESTION_MESSAGE, null, options, // the titles of buttons
						options[0]); // default button title

				if (iSelected == YES_OPTION) {
					fileManager.setCurrentDataflow(alreadyOpen);
					return;
				} else if (iSelected == CANCEL_OPTION) {
					// do nothing
					return;
				}
				// else open the workflow as usual
			}

			fileManager.openDataflow(null, url);
			prefs.put(PREF_CURRENT_URL, urlString);
		} catch (Exception ex) {
			logger.warn("Failed to open the workflow from url " + urlString
					+ " \n", ex);
			showMessageDialog(component,
					"Failed to open the workflow from url " + urlString + " \n"
							+ ex.getMessage(), "Error!", ERROR_MESSAGE);
		}
	}
}
