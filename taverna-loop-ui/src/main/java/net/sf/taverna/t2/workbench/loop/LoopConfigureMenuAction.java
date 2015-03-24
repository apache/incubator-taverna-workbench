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
package net.sf.taverna.t2.workbench.loop;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.taverna.scufl2.api.core.Processor;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;

public class LoopConfigureMenuAction extends AbstractContextualMenuAction {

	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");

	private static final URI LOOP_CONFIGURE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/loopConfigure");

	private static final String LOOP_CONFIGURE = "Loop configure";

	private EditManager editManager;

	private FileManager fileManager;

	public LoopConfigureMenuAction() {
		super(configureRunningSection, 20, LOOP_CONFIGURE_URI);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Looping...") {
			public void actionPerformed(ActionEvent e) {
				Processor p = (Processor) getContextualSelection().getSelection();
				configureLoopLayer(p, e);
			}
		};
	}

	public void configureLoopLayer(Processor p, ActionEvent e) {
	    ObjectNode loopLayer = getLoopLayer(p);
		if (loopLayer != null) {
			LoopConfigureAction loopConfigureAction = new LoopConfigureAction(null, null, loopLayer, editManager, fileManager);
			loopConfigureAction.actionPerformed(e);
		}
	}

	public static ObjectNode getLoopLayer(Processor p) {
		for (DispatchLayer dl : p.getDispatchStack().getLayers()) {
			if (dl instanceof Loop) {
				result = (Loop) dl;
				break;
			}
		}
		return result;
	}

	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		return (super.isEnabled() && (selection instanceof Processor) && (getLoopLayer((Processor)selection) != null));
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

}
