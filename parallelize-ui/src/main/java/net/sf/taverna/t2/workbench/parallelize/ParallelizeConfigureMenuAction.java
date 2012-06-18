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
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;

public class ParallelizeConfigureMenuAction extends AbstractContextualMenuAction {



	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");

	private static final URI PARALLELIZE_CONFIGURE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/parallelizeConfigure");

	private EditManager editManager;

	private FileManager fileManager;

	public ParallelizeConfigureMenuAction() {
		super(configureRunningSection, 10, PARALLELIZE_CONFIGURE_URI);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Parallel jobs...") {
			public void actionPerformed(ActionEvent e) {
				Parallelize parallelizeLayer = null;
				Processor p = (Processor) getContextualSelection().getSelection();
				for (DispatchLayer dl : p.getDispatchStack().getLayers()) {
					if (dl instanceof Parallelize) {
						parallelizeLayer = (Parallelize) dl;
						break;
					}
				}
				if (parallelizeLayer != null) {
				ParallelizeConfigureAction parallelizeConfigureAction = new ParallelizeConfigureAction(null, null, parallelizeLayer, editManager, fileManager);
				parallelizeConfigureAction.actionPerformed(e);
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

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

}
