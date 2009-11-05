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

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Set;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.merge.MergeConfigurationView;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workbench.views.graph.actions.DesignOnlyAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.Processor;

public class ShowConfigureMenuAction extends AbstractMenuAction {
	
	private static Log logger = Log.getLogger(ShowConfigureMenuAction.class);

	public static final URI GRAPH_DETAILS_MENU_SECTION = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#graphDetailsMenuSection");
	
	private static final URI SHOW_CONFIGURE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuShowConfigureComponent");

	private static final String SHOW_CONFIGURE = "Configure";

	private String namedComponent = "contextualView";

	public ShowConfigureMenuAction() {
		super(GRAPH_DETAILS_MENU_SECTION, 20, SHOW_CONFIGURE_URI);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new ShowConfigureAction();
	}
	
	protected class ShowConfigureAction extends DesignOnlyAction {
		
		ShowConfigureAction() {
		super();
		putValue(NAME, "Configure");	
		putValue(SHORT_DESCRIPTION, "Configure selected component");
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
		
		KeyboardFocusManager focusManager =
		    KeyboardFocusManager.getCurrentKeyboardFocusManager();
		focusManager.addPropertyChangeListener(
		    new PropertyChangeListener() {
		        public void propertyChange(PropertyChangeEvent e) {
		            String prop = e.getPropertyName();
		            if ("focusOwner".equals(prop)) {
						if (e.getNewValue() instanceof JTextComponent) {
									ShowConfigureAction.this.setEnabled(false);
						} else {
									ShowConfigureAction.this
											.setEnabled(inWorkflow);
								}
							}
						}
		    }
		);

		}
		public void actionPerformed(ActionEvent e) {
			Dataflow dataflow = FileManager.getInstance().getCurrentDataflow();
			DataflowSelectionModel dataFlowSelectionModel = DataflowSelectionManager
			.getInstance().getDataflowSelectionModel(dataflow);
			// Get selected port
			Set<Object> selectedWFComponents = dataFlowSelectionModel
					.getSelection();
			if (selectedWFComponents.size() > 0) {
				Object component = selectedWFComponents.iterator().next();
				if (component instanceof Processor) {
					Action action = WorkflowView.getConfigureAction((Processor) component);
					if (action != null) {
					action.actionPerformed(e);
					}
				} else if (component instanceof Merge) {
					Merge merge = (Merge) component;
						MergeConfigurationView	mergeConfigurationView = new MergeConfigurationView(merge);
						mergeConfigurationView.setLocationRelativeTo(null);
						mergeConfigurationView.setVisible(true);
				} else if (component instanceof DataflowInputPort) {
					DataflowInputPort port = (DataflowInputPort) component;
					new EditDataflowInputPortAction(dataflow,
							port, null)
							.actionPerformed(e);
				} else if (component instanceof DataflowOutputPort) {
					DataflowOutputPort port = (DataflowOutputPort) component;
					new EditDataflowOutputPortAction(dataflow, port, null).actionPerformed(e);
				}
			}
		}
	}

}
