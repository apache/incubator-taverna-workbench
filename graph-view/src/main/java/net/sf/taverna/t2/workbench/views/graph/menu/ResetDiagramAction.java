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
package net.sf.taverna.t2.workbench.views.graph.menu;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_0;
import static javax.swing.KeyStroke.getKeyStroke;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.refreshIcon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.DesignOrResultsAction;

@SuppressWarnings("serial")
public class ResetDiagramAction extends AbstractAction implements
		DesignOrResultsAction {
	private static Action designAction = null;
	@SuppressWarnings("unused")
	private static Action resultsAction = null;

	public static void setResultsAction(Action resultsAction) {
		ResetDiagramAction.resultsAction = resultsAction;
	}

	public static void setDesignAction(Action designAction) {
		ResetDiagramAction.designAction = designAction;
	}

	public ResetDiagramAction() {
		super("Reset diagram", refreshIcon);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_0, getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		if (isWorkflowPerspective() && (designAction != null))
			designAction.actionPerformed(e);
//		else if (isResultsPerspective() && (resultsAction != null))
//			resultsAction.actionPerformed(e);
	}
}
