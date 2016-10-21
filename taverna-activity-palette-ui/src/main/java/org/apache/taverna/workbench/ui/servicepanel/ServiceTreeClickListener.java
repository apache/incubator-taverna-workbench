/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.workbench.ui.servicepanel;

import static java.awt.Color.RED;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.taverna.lang.ui.ShadedLabel.BLUE;
import static org.apache.taverna.lang.ui.ShadedLabel.GREEN;
import static org.apache.taverna.lang.ui.ShadedLabel.ORANGE;
import static org.apache.taverna.lang.ui.ShadedLabel.halfShade;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.minusIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.plusIcon;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.taverna.lang.ui.ShadedLabel;
import org.apache.taverna.servicedescriptions.ConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.servicepanel.actions.ExportServiceDescriptionsAction;
import org.apache.taverna.workbench.ui.servicepanel.actions.ImportServiceDescriptionsFromFileAction;
import org.apache.taverna.workbench.ui.servicepanel.actions.ImportServiceDescriptionsFromURLAction;
import org.apache.taverna.workbench.ui.servicepanel.actions.RemoveDefaultServicesAction;
import org.apache.taverna.workbench.ui.servicepanel.actions.RemoveUserServicesAction;
import org.apache.taverna.workbench.ui.servicepanel.actions.RestoreDefaultServicesAction;
import org.apache.taverna.workbench.ui.servicepanel.tree.FilterTreeNode;
import org.apache.taverna.workbench.ui.servicepanel.tree.FilterTreeSelectionModel;
import org.apache.taverna.workbench.ui.servicepanel.tree.TreePanel;
import org.apache.taverna.workbench.ui.workflowview.WorkflowView;

import org.apache.log4j.Logger;

import org.apache.taverna.services.ServiceRegistry;

/**
 * @author alanrw
 */
public class ServiceTreeClickListener extends MouseAdapter {
	private static Logger logger = Logger.getLogger(ServiceTreeClickListener.class);

	private JTree tree;
	private TreePanel panel;
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;
	private final EditManager editManager;
	private final MenuManager menuManager;
	private final SelectionManager selectionManager;
	private final ServiceRegistry serviceRegistry;

	public ServiceTreeClickListener(JTree tree, TreePanel panel,
			ServiceDescriptionRegistry serviceDescriptionRegistry, EditManager editManager,
			MenuManager menuManager, SelectionManager selectionManager, ServiceRegistry serviceRegistry) {
		this.tree = tree;
		this.panel = panel;
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		this.editManager = editManager;
		this.menuManager = menuManager;
		this.selectionManager = selectionManager;
		this.serviceRegistry = serviceRegistry;
	}

	@SuppressWarnings("serial")
	private void handleMouseEvent(MouseEvent evt) {
		FilterTreeSelectionModel selectionModel = (FilterTreeSelectionModel) tree
				.getSelectionModel();
		// Discover the tree row that was clicked on
		int selRow = tree.getRowForLocation(evt.getX(), evt.getY());
		if (selRow == -1)
			return;

		// Get the selection path for the row
		TreePath selectionPath = tree
				.getPathForLocation(evt.getX(), evt.getY());
		if (selectionPath == null)
			return;

		// Get the selected node
		final FilterTreeNode selectedNode = (FilterTreeNode) selectionPath
				.getLastPathComponent();

		selectionModel.clearSelection();
		selectionModel.mySetSelectionPath(selectionPath);

		if (evt.isPopupTrigger()) {
			JPopupMenu menu = new JPopupMenu();
			Object selectedObject = selectedNode.getUserObject();
			logger.info(selectedObject.getClass().getName());
			if (!(selectedObject instanceof ServiceDescription)) {
				menu.add(new ShadedLabel("Tree", BLUE));
				menu.add(new JMenuItem(new AbstractAction("Expand all",
						plusIcon) {
					@Override
					public void actionPerformed(ActionEvent evt) {
						invokeLater(new Runnable() {
							@Override
							public void run() {
								panel.expandAll(selectedNode, true);
							}
						});
					}
				}));
				menu.add(new JMenuItem(new AbstractAction("Collapse all",
						minusIcon) {
					@Override
					public void actionPerformed(ActionEvent evt) {
						invokeLater(new Runnable() {
							@Override
							public void run() {
								panel.expandAll(selectedNode, false);
							}
						});
					}
				}));
			}

			if (selectedObject instanceof ServiceDescription) {
				final ServiceDescription sd = (ServiceDescription) selectedObject;
				menu.add(new ShadedLabel(sd.getName(), ORANGE));
				menu.add(new AbstractAction("Add to workflow") {
					@Override
					public void actionPerformed(ActionEvent e) {
						WorkflowView.importServiceDescription(sd, false, editManager,
								menuManager, selectionManager, serviceRegistry);
					}
				});
				menu.add(new AbstractAction("Add to workflow with name...") {
					@Override
					public void actionPerformed(ActionEvent e) {
						WorkflowView.importServiceDescription(sd, true, editManager,
								menuManager, selectionManager, serviceRegistry);
					}
				});
			}

			Map<String, ServiceDescriptionProvider> nameMap = getServiceDescriptionProviderMap(selectedNode);

			boolean first = true;
			for (String name : nameMap.keySet()) {
				final ServiceDescriptionProvider sdp = nameMap.get(name);
				if (!(sdp instanceof ConfigurableServiceProvider))
					continue;
				if (first) {
					menu.add(new ShadedLabel(
							"Remove individual service provider", GREEN));
					first = false;
				}
				menu.add(new AbstractAction(name) {
					@Override
					public void actionPerformed(ActionEvent e) {
						serviceDescriptionRegistry
								.removeServiceDescriptionProvider(sdp);
					}
				});
			}

			if (selectedNode.isRoot()) { // Root "Available services"
				menu.add(new ShadedLabel("Default and added service providers",
						ORANGE));
				menu.add(new RemoveUserServicesAction(
						serviceDescriptionRegistry));
				menu.add(new RemoveDefaultServicesAction(
						serviceDescriptionRegistry));
				menu.add(new RestoreDefaultServicesAction(
						serviceDescriptionRegistry));

				menu.add(new ShadedLabel("Import/export services", halfShade(RED)));
				menu.add(new ImportServiceDescriptionsFromFileAction(
						serviceDescriptionRegistry));
				menu.add(new ImportServiceDescriptionsFromURLAction(
						serviceDescriptionRegistry));
				menu.add(new ExportServiceDescriptionsAction(
						serviceDescriptionRegistry));
			}

			menu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	private Map<String, ServiceDescriptionProvider> getServiceDescriptionProviderMap(
			FilterTreeNode selectedNode) {
		Set<ServiceDescriptionProvider> providers;

		if (selectedNode.isRoot())
			providers = serviceDescriptionRegistry
					.getServiceDescriptionProviders();
		else {
			providers = new HashSet<>();
			for (FilterTreeNode leaf : selectedNode.getLeaves()) {
				if (!leaf.isLeaf())
					logger.info("Not a leaf");
				if (!(leaf.getUserObject() instanceof ServiceDescription)) {
					logger.info(leaf.getUserObject().getClass()
							.getCanonicalName());
					logger.info(leaf.getUserObject().toString());
					continue;
				}
				providers
						.addAll(serviceDescriptionRegistry
								.getServiceDescriptionProviders((ServiceDescription) leaf
										.getUserObject()));
			}
		}

		TreeMap<String, ServiceDescriptionProvider> nameMap = new TreeMap<>();
		for (ServiceDescriptionProvider sdp : providers)
			nameMap.put(sdp.toString(), sdp);
		return nameMap;
	}

	@Override
	public void mousePressed(MouseEvent evt) {
		handleMouseEvent(evt);
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		handleMouseEvent(evt);
	}
}
