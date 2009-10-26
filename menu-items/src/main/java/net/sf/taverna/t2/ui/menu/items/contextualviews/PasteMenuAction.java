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
package net.sf.taverna.t2.ui.menu.items.contextualviews;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Set;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.actions.PasteGraphComponentAction;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.merge.MergeConfigurationView;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workbench.views.graph.actions.DesignOnlyAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.Processor;

public class PasteMenuAction extends AbstractContextualMenuAction {
	
	private static final URI PASTE_SERVICE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/paste#pasteServiceComponent");

	private static final String PASTE_SERVICE = "Paste service";

	private String namedComponent = "contextualView";

	public PasteMenuAction() {
		super(EditSection.editSection, 20, PASTE_SERVICE_URI);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return PasteGraphComponentAction.getInstance();
	}
	
	public boolean isEnabled() {
		return super.isEnabled() && (getContextualSelection().getSelection() instanceof Dataflow);
	}

}
