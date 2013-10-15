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
package net.sf.taverna.t2.workbench.retry;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.dispatchstack.DispatchStack;
import uk.org.taverna.scufl2.api.dispatchstack.DispatchStackLayer;

public class RetryConfigureMenuAction extends AbstractContextualMenuAction {

	public static final URI configureRunningSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configureRunning");

	private static final URI RETRY_CONFIGURE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/retryConfigure");

	public static URI TYPE = URI.create("http://ns.taverna.org.uk/2010/scufl2/taverna/dispatchlayer/Retry");

	private EditManager editManager;

	private SelectionManager selectionManager;

	public RetryConfigureMenuAction() {
		super(configureRunningSection, 30, RETRY_CONFIGURE_URI);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Retries...") {
			public void actionPerformed(ActionEvent e) {
				DispatchStackLayer retryLayer = null;
				Processor processor = (Processor) getContextualSelection().getSelection();
				DispatchStack dispatchStack = processor.getDispatchStack();
				for (DispatchStackLayer dl : dispatchStack) {
					if (TYPE.equals(dl.getType())) {
						retryLayer = dl;
						break;
					}
				}
				if (retryLayer != null) {
					RetryConfigureAction retryConfigureAction = new RetryConfigureAction(null,
							null, retryLayer, editManager, selectionManager);
					retryConfigureAction.actionPerformed(e);
				}
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
