/*******************************************************************************
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
 ******************************************************************************/
package net.sf.taverna.t2.workbench.ui.servicepanel;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeModel;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeNode;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/**
 * A panel of available services
 * 
 * @author Stian Soiland-Reyes
 * 
 */
@SuppressWarnings("serial")
public class ServicePanel extends JPanel implements UIComponentSPI {

	private static final String AVAILABLE_SERVICES = "Available services";

	/**
	 * A Comparable constant to be used with buildPathMap
	 */
	private static final UUID SERVICES = UUID
			.fromString("4DA84170-7746-4817-8C2E-E29AF8B2984D");

	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	public ServicePanel(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		initialise();
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Service panel";
	}

	public void onDisplay() {
	}

	public void onDispose() {
	}

	@SuppressWarnings("unchecked")
	protected Map<Comparable, Map> buildPathMap() {
		Map<Comparable, Map> paths = new HashMap<Comparable, Map>();
		for (ServiceDescription serviceDescription : serviceDescriptionRegistry
				.getServiceDescriptions()) {
			Map currentPath = paths;
			Map pathEntry = paths;
			for (Object pathElem : serviceDescription.getPath()) {
				pathEntry = (Map) currentPath.get(pathElem);
				if (pathEntry == null) {
					pathEntry = new HashMap();
					currentPath.put(pathElem, pathEntry);
				}
			}
			List<ServiceDescription> services = (List<ServiceDescription>) pathEntry
					.get(SERVICES);
			if (services == null) {
				services = new ArrayList<ServiceDescription>();
				pathEntry.put(SERVICES, services);
			}
			if (!services.contains(serviceDescription)) {
				services.add(serviceDescription);
			}
		}
		return paths;
	}

	protected FilterTreeNode createRoot() {
		final FilterTreeNode root = new FilterTreeNode(AVAILABLE_SERVICES);
		new PopulateThread(root).start();
		return root;
	}

	protected void initialise() {
		removeAll();
		setLayout(new BorderLayout());
		FilterTreeNode root = createRoot();
		add(new ServiceTreePanel(new FilterTreeModel(root)));
	}

	@SuppressWarnings("unchecked")
	protected void populateChildren(FilterTreeNode root, Map pathMap) {
		List<Comparable> paths = new ArrayList<Comparable>(pathMap.keySet());
		Collections.sort(paths);
		for (Comparable path : paths) {
			if (path.equals(SERVICES)) {
				continue;
			}
			FilterTreeNode node = new FilterTreeNode(path);
			SwingUtilities.invokeLater(new AddNodeRunnable(root, node));
			populateChildren(node, (Map) pathMap.get(path));
		}
		List<ServiceDescription> services = (List<ServiceDescription>) pathMap
				.get(SERVICES);
		if (services != null) {
			for (ServiceDescription service : services) {
				SwingUtilities.invokeLater(new AddNodeRunnable(root,
						new FilterTreeNode(service)));
			}
		}
	}

	public class PopulateThread extends Thread {
		private final FilterTreeNode root;

		private PopulateThread(FilterTreeNode root) {
			super("Populating service panel");
			this.root = root;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			Map<Comparable, Map> pathMap = buildPathMap();
			populateChildren(root, pathMap);
		}
	}

	public static class AddNodeRunnable implements Runnable {
		private final FilterTreeNode node;
		private final FilterTreeNode root;

		public AddNodeRunnable(FilterTreeNode root, FilterTreeNode node) {
			this.root = root;
			this.node = node;
		}

		public void run() {
			root.add(node);
		}
	}

	public static class RemoveNodeRunnable implements Runnable {
		private final FilterTreeNode root;

		public RemoveNodeRunnable(FilterTreeNode root) {
			this.root = root;
		}

		public void run() {
			root.removeFromParent();
		}
	}

}
