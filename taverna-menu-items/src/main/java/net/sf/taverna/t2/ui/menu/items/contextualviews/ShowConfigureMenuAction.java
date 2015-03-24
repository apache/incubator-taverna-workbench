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
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.DesignOnlyAction;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.merge.MergeConfigurationView;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;

public class ShowConfigureMenuAction extends AbstractMenuAction {

	private static Logger logger = Logger.getLogger(ShowConfigureMenuAction.class);

	public static final URI GRAPH_DETAILS_MENU_SECTION = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphDetailsMenuSection");

	private static final URI SHOW_CONFIGURE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuShowConfigureComponent");

	private EditManager editManager;

	private SelectionManager selectionManager;

	private MenuManager menuManager;

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public ShowConfigureMenuAction() {
		super(GRAPH_DETAILS_MENU_SECTION, 20, SHOW_CONFIGURE_URI);
	}

	@Override
	protected Action createAction() {
		return new ShowConfigureAction();
	}

	@SuppressWarnings("serial")
	protected class ShowConfigureAction extends AbstractAction implements DesignOnlyAction {

		private boolean enabled;

		ShowConfigureAction() {
			super();
			putValue(NAME, "Configure");
			putValue(SHORT_DESCRIPTION, "Configure selected component");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));

			KeyboardFocusManager focusManager = KeyboardFocusManager
					.getCurrentKeyboardFocusManager();
			focusManager.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
					String prop = e.getPropertyName();
					if ("focusOwner".equals(prop)) {
						if (e.getNewValue() instanceof JTextComponent) {
							ShowConfigureAction.super.setEnabled(false);
						} else {
							ShowConfigureAction.this.setEnabled(enabled);
						}
					}
				}
			});
		}

		@Override
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
			super.setEnabled(enabled);
		}

		public void actionPerformed(ActionEvent e) {
			WorkflowBundle workflowBundle = selectionManager.getSelectedWorkflowBundle();
			DataflowSelectionModel dataFlowSelectionModel = selectionManager
					.getDataflowSelectionModel(workflowBundle);
			// Get selected port
			Set<Object> selectedWFComponents = dataFlowSelectionModel.getSelection();
			if (selectedWFComponents.size() > 0) {
				Object component = selectedWFComponents.iterator().next();
				if (component instanceof Processor) {
					Action action = WorkflowView.getConfigureAction((Processor) component,
							menuManager);
					if (action != null) {
						action.actionPerformed(e);
					}
				} else if (component instanceof DataLink) {
					DataLink dataLink = (DataLink) component;
					if (dataLink.getMergePosition() != null) {
						List<DataLink> datalinks = scufl2Tools.datalinksTo(dataLink.getSendsTo());
						MergeConfigurationView mergeConfigurationView = new MergeConfigurationView(
								datalinks, editManager, selectionManager);
						mergeConfigurationView.setLocationRelativeTo(null);
						mergeConfigurationView.setVisible(true);
					}
				} else if (component instanceof InputWorkflowPort) {
					InputWorkflowPort port = (InputWorkflowPort) component;
					new EditDataflowInputPortAction(port.getParent(), port, null, editManager,
							selectionManager).actionPerformed(e);
				} else if (component instanceof OutputWorkflowPort) {
					OutputWorkflowPort port = (OutputWorkflowPort) component;
					new EditDataflowOutputPortAction(port.getParent(), port, null, editManager,
							selectionManager).actionPerformed(e);
				}
			}
		}
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
