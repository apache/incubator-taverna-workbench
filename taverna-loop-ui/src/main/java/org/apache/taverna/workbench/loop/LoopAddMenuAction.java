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
package org.apache.taverna.workbench.loop;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.scufl2.api.core.Processor;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;

public class LoopAddMenuAction extends AbstractContextualMenuAction {

	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");

	private static final URI LOOP_ADD_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/loopAdd");

	private static final String LOOP_ADD = "Loop add";

	public LoopAddMenuAction() {
		super(configureRunningSection, 20, LOOP_ADD_URI);
	}

	private AddLoopFactory addLoopFactory;

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Looping...") {
			public void actionPerformed(ActionEvent e) {
				//Loop loopLayer = null;
				Processor p = (Processor) getContextualSelection().getSelection();
				addLoopFactory.getAddLayerActionFor(p).actionPerformed(e);
				//LoopConfigureMenuAction.configureLoopLayer(p, e); // Configuration dialog pop up is now done from getAddLayerActionFor()
			}
		};
	}

	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		return (super.isEnabled() && (selection instanceof Processor) && (LoopConfigureMenuAction.getLoopLayer((Processor)selection) == null));
	}

	public void setAddLoopFactory(AddLoopFactory addLoopFactory) {
		this.addLoopFactory = addLoopFactory;
	}



}
