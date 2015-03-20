/**********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester
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
 **********************************************************************/
package net.sf.taverna.t2.workbench.parallelize;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import uk.org.taverna.scufl2.api.core.Processor;

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
