/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.ui.perspectives.design;

import java.awt.Graphics;
import java.awt.Graphics2D;

import net.sf.taverna.t2.lang.ui.tabselector.Tab;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Tab for selecting current workflow.
 *
 * @author David Withers
 */
public class WorkflowTab extends Tab<WorkflowBundle> {

	private static final long serialVersionUID = 1L;

	private final SelectionManager selectionManager;
	private final FileManager fileManager;

	public WorkflowTab(final WorkflowBundle workflowBundle, final SelectionManager selectionManager, final FileManager fileManager) {
		super(fileManager.getDataflowName(workflowBundle), workflowBundle);
		this.selectionManager = selectionManager;
		this.fileManager = fileManager;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		if (getModel().isPressed()) {
			g2.translate(1, 1);
		}
		if (!getModel().isSelected()) {
			g2.setColor(lightGrey);
			g2.fillRoundRect(1, 0, getWidth() - 3, getHeight() - 1, 4, 10);
		}
		g2.setColor(midGrey);
		g2.drawRoundRect(1, 0, getWidth() - 3, getHeight(), 4, 10);
		if (getModel().isSelected()) {
			g2.setColor(getParent().getBackground());
			g2.drawLine(1, getHeight() - 1, getWidth() - 2, getHeight() - 1);
		}
		g2.dispose();
	}

	protected void clickTabAction() {
		selectionManager.setSelectedWorkflowBundle(selection);
	}

	protected void closeTabAction() {
		try {
			fileManager.closeDataflow(selection, false);
		} catch (UnsavedException e1) {
			// ignore
		}
	}

}
