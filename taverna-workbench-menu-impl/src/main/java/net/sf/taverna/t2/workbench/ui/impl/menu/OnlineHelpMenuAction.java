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
package net.sf.taverna.t2.workbench.ui.impl.menu;

import static java.awt.event.KeyEvent.VK_F1;
import static javax.swing.KeyStroke.getKeyStroke;
import static net.sf.taverna.t2.workbench.helper.Helper.displayDefaultHelp;
import static net.sf.taverna.t2.workbench.ui.impl.menu.HelpMenu.HELP_URI;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

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
