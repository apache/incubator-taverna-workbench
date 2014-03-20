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

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Loop;

import org.apache.log4j.Logger;

public class LoopRemoveMenuAction extends AbstractContextualMenuAction {
	
	private static Logger logger = Logger
	.getLogger(LoopRemoveMenuAction.class);
	
	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");
	
	private static final URI LOOP_REMOVE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/loopRemove");

	private static final String LOOP_REMOVE = "Loop remove";

	public LoopRemoveMenuAction() {
		super(configureRunningSection, 25, LOOP_REMOVE_URI);
	}
	
	private EditManager editManager = EditManager.getInstance();

	private FileManager fileManager = FileManager.getInstance();

	private Edits edits = EditManager.getInstance().getEdits();

	
	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Disable looping") {
			public void actionPerformed(ActionEvent e) {
				Processor p = (Processor) getContextualSelection().getSelection();
					Loop loopLayer = LoopConfigureMenuAction.getLoopLayer(p);
					Edit<DispatchStack> deleteEdit = edits.getDeleteDispatchLayerEdit(
							p.getDispatchStack(), loopLayer);
					// TODO: Should warn before removing "essential" layers
					try {
						editManager.doDataflowEdit(fileManager.getCurrentDataflow(),
								deleteEdit);
					} catch (EditException ex) {
						logger.warn("Could not remove layer " + loopLayer, ex);
					}
				
			}
		};
	}
	
	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		return (super.isEnabled() && (selection instanceof Processor) && (LoopConfigureMenuAction.getLoopLayer((Processor)selection) != null));
	}

}
