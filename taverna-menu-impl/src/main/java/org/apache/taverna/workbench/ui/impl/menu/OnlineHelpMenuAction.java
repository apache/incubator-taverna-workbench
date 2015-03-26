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

package org.apache.taverna.workbench.ui.impl.menu;

import static java.awt.event.KeyEvent.VK_F1;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.helper.Helper.displayDefaultHelp;
import static org.apache.taverna.workbench.ui.impl.menu.HelpMenu.HELP_URI;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;

/**
 * MenuItem for help
 * 
 * @author alanrw
 */
public class OnlineHelpMenuAction extends AbstractMenuAction {
	public OnlineHelpMenuAction() {
		super(HELP_URI, 10);
	}

	@Override
	protected Action createAction() {
		return new OnlineHelpAction();
	}

	@SuppressWarnings("serial")
	private final class OnlineHelpAction extends AbstractAction {
		private OnlineHelpAction() {
			super("Online help");
			putValue(ACCELERATOR_KEY, getKeyStroke(VK_F1, 0));

		}

		/**
		 * When selected, use the Helper to display the default help.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			displayDefaultHelp((AWTEvent) e);
			// TODO change helper to bean?
		}
	}
}
