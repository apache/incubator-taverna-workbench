/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.servicepanel;

import static java.awt.datatransfer.DataFlavor.javaJVMLocalObjectMimeType;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.servicepanel.menu.AddServiceProviderMenu;
import org.apache.taverna.workbench.ui.servicepanel.tree.Filter;
import org.apache.taverna.workbench.ui.servicepanel.tree.FilterTreeModel;
import org.apache.taverna.workbench.ui.servicepanel.tree.FilterTreeNode;
import org.apache.taverna.workbench.ui.servicepanel.tree.TreePanel;

import org.apache.log4j.Logger;

import org.apache.taverna.services.ServiceRegistry;

public class ServiceTreePanel extends TreePanel {
	private static final long serialVersionUID = 6611462684296693909L;
	private static Logger logger = Logger.getLogger(ServiceTreePanel.class);

	private final ServiceDescriptionRegistry serviceDescriptionRegistry;
	private final EditManager editManager;
	private final MenuManager menuManager;
	private final SelectionManager selectionManager;
	private final ServiceRegistry serviceRegistry;

	public ServiceTreePanel(FilterTreeModel treeModel,
			ServiceDescriptionRegistry serviceDescriptionRegistry, EditManager editManager,
			MenuManager menuManager, SelectionManager selectionManager, ServiceRegistry serviceRegistry) {
		super(treeModel);
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		this.editManager = editManager;
		this.menuManager = menuManager;
		this.selectionManager = selectionManager;
		this.serviceRegistry = serviceRegistry;
		initialize();
	}

	@Override
	protected void initialize() {
		super.initialize();
		tree.setDragEnabled(true);
		tree.setTransferHandler(new ServiceTransferHandler());
		tree.addTreeWillExpandListener(new AvoidRootCollapse());
		tree.expandRow(0);

		invokeLater(new Runnable() {
			@Override
			public void run() {
				tree.addMouseListener(new ServiceTreeClickListener(tree,
						ServiceTreePanel.this, serviceDescriptionRegistry,
						editManager, menuManager, selectionManager,
						serviceRegistry));
			}
		});
	}

	@Override
	protected Component createExtraComponent() {
		JComponent buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(new AddServiceProviderMenu(serviceDescriptionRegistry));
		// buttonPanel.add(new JButton(new RefreshProviderRegistryAction()));
		return buttonPanel;
	}

	@Override
	public Filter createFilter(String text) {
		return new ServiceFilter(text, filterTreeModel.getRoot());
	}

	@Override
	protected TreeCellRenderer createCellRenderer() {
		return new ServiceTreeCellRenderer();
	}

	public static class AvoidRootCollapse implements TreeWillExpandListener {
		@Override
		public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
			if (event.getPath().getPathCount() == 1)
				throw new ExpandVetoException(event, "Can't collapse root");
		}

		@Override
		public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
		}
	}

	private final class ServiceTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 4347965626386951176L;

		/**
		 * Triggered when a node ie. an {@link ActivityItem} is dragged out of
		 * the tree. Figures out what node it is being dragged and then starts a
		 * drag action with it
		 */
		@Override
		protected Transferable createTransferable(JComponent c) {
			TreePath selectionPath = tree.getSelectionPath();
			if (selectionPath == null)
				return null;
			FilterTreeNode lastPathComponent = (FilterTreeNode) selectionPath
					.getLastPathComponent();
			if (!(lastPathComponent.getUserObject() instanceof ServiceDescription))
				return null;
			final ServiceDescription serviceDescription = (ServiceDescription) lastPathComponent
					.getUserObject();

			return new Transferable() {
				@Override
				public Object getTransferData(DataFlavor flavor)
						throws UnsupportedFlavorException, IOException {
					return serviceDescription;
				}

				@Override
				public DataFlavor[] getTransferDataFlavors() {
					DataFlavor[] flavors = new DataFlavor[1];
					try {
						flavors[0] = getFlavorForClass(ServiceDescription.class);
					} catch (ClassNotFoundException e) {
						logger.error("Error casting Dataflavor", e);
						flavors[0] = null;
					}
					return flavors;
				}

				@Override
				public boolean isDataFlavorSupported(DataFlavor flavor) {
					DataFlavor thisFlavor = null;
					try {
						thisFlavor = getFlavorForClass(ServiceDescription.class);
					} catch (ClassNotFoundException e) {
						logger.error("Error casting Dataflavor", e);
					}
					return flavor.equals(thisFlavor);
				}
			};
		}

		@Override
		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE;
		}
	}

	private DataFlavor getFlavorForClass(Class<?> clazz)
			throws ClassNotFoundException {
		String name = clazz.getName();
		return new DataFlavor(javaJVMLocalObjectMimeType + ";class=" + clazz,
				name.substring(name.lastIndexOf('.') + 1),
				clazz.getClassLoader());
	}
}
