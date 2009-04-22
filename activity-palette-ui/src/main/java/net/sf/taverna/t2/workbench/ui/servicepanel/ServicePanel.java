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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.events.AbstractProviderNotification;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionProvidedEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
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

	private static final int STATUS_LINE_UPDATE_MS = 400;

	private TreeUpdaterThread populateThread;

	private FilterTreeNode root = new FilterTreeNode(AVAILABLE_SERVICES);

	private ServiceComparator serviceComparator = new ServiceComparator();

	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	private ServiceTreePanel serviceTreePanel;

	private JLabel statusLine;

	private FilterTreeModel treeModel;

	protected ServiceDescriptionRegistryObserver serviceDescriptionRegistryObserver = new ServiceDescriptionRegistryObserver();

	protected Queue<String> statusMessages = new ConcurrentLinkedQueue<String>();

	protected Timer statusUpdateTimer;

	protected Object updateLock = new Object();

	public ServicePanel(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		serviceDescriptionRegistry
				.addObserver(serviceDescriptionRegistryObserver);
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

	private static Logger logger = Logger.getLogger(ServicePanel.class);

	public void providerStatus(ServiceDescriptionProvider provider,
			String message) {
		logger.info(message + " " + provider);
		statusMessages.add("<html>" + message + " <small>(" + provider
				+ ")</small></html>");
	}

	protected void initialise() {
		removeAll();
		setLayout(new BorderLayout());
		treeModel = new FilterTreeModel(root);
		serviceTreePanel = new ServiceTreePanel(treeModel);
		add(serviceTreePanel);
		statusLine = new JLabel();
		add(statusLine, BorderLayout.SOUTH);
		if (statusUpdateTimer != null) {
			statusUpdateTimer.cancel();
		}
		statusUpdateTimer = new Timer("Update status line");
		statusUpdateTimer.scheduleAtFixedRate(new UpdateStatusLineTask(), 0,
				STATUS_LINE_UPDATE_MS);
		updateTree();
	}

	protected void updateTree() {
		synchronized (updateLock) {
			if (populateThread != null && populateThread.isAlive()) {
				return;
			}

			populateThread = new TreeUpdaterThread();
			populateThread.start();
		}
	}

	public class TreeUpdaterThread extends Thread {

		private boolean aborting = false;

		private TreeUpdaterThread() {
			super("Updating service panel");
		}

		public void abort() {
			aborting = true;
			interrupt();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			Map<Comparable, Map> pathMap = buildPathMap();
			populateChildren(root, pathMap);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					serviceTreePanel.setFilter(null);
				}
			});
		}

		@SuppressWarnings("unchecked")
		protected Map<Comparable, Map> buildPathMap() {
			Map<Comparable, Map> paths = new HashMap<Comparable, Map>();
			for (ServiceDescription serviceDescription : serviceDescriptionRegistry
					.getServiceDescriptions()) {
				if (aborting) {
					return paths;
				}
				Map currentPath = paths;
				Map pathEntry = paths;
				for (Object pathElem : serviceDescription.getPath()) {
					pathEntry = (Map) currentPath.get(pathElem);
					if (pathEntry == null) {
						pathEntry = new HashMap();
						currentPath.put(pathElem, pathEntry);
					}
					currentPath = pathEntry;
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

		@SuppressWarnings("unchecked")
		protected void populateChildren(FilterTreeNode node, Map pathMap) {
			if (aborting) {
				return;
			}
			if (node == root) {
				// Clear top root
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (aborting) {
							return;
						}
						serviceTreePanel.setFilter(null);
						root.removeAllChildren();
					}
				});
			}

			List<Comparable> paths = new ArrayList<Comparable>(pathMap.keySet());
			Collections.sort(paths);
			for (Comparable path : paths) {
				if (aborting) {
					return;
				}
				if (path.equals(SERVICES)) {
					continue;
				}
				FilterTreeNode childNode = new FilterTreeNode(path);
				SwingUtilities
						.invokeLater(new AddNodeRunnable(node, childNode));
				populateChildren(childNode, (Map) pathMap.get(path));
			}
			List<ServiceDescription> services = (List<ServiceDescription>) pathMap
					.get(SERVICES);
			if (services != null) {
				Collections.sort(services, serviceComparator);
				for (ServiceDescription service : services) {
					if (aborting) {
						return;
					}
					SwingUtilities.invokeLater(new AddNodeRunnable(node,
							new FilterTreeNode(service)));
				}
			}
		}

		public class AddNodeRunnable implements Runnable {
			private final FilterTreeNode node;
			private final FilterTreeNode root;

			public AddNodeRunnable(FilterTreeNode root, FilterTreeNode node) {
				this.root = root;
				this.node = node;
			}

			public void run() {
				if (aborting) {
					return;
				}
				root.add(node);
			}
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

	@SuppressWarnings("unchecked")
	public static class ServiceComparator implements
			Comparator<ServiceDescription> {
		public int compare(ServiceDescription o1, ServiceDescription o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}

	private final class ServiceDescriptionRegistryObserver implements
			Observer<ServiceDescriptionRegistryEvent> {
		public void notify(Observable<ServiceDescriptionRegistryEvent> sender,
				ServiceDescriptionRegistryEvent message) throws Exception {
			if (message instanceof ServiceDescriptionProvidedEvent) {
				// TODO: Support other events
				// and only update relevant parts of tree, or at least select
				// the recently added provider
				updateTree();
			}
			if (message instanceof AbstractProviderNotification) {
				AbstractProviderNotification abstractProviderNotification = (AbstractProviderNotification) message;
				providerStatus(abstractProviderNotification.getProvider(),
						abstractProviderNotification.getMessage());
			}
		}
	}

	private final class UpdateStatusLineTask extends TimerTask {

		private static final int INITIAL_BLANK_OUT_COUNTER = 5;
		private int blankOutCounter = 0;

		@Override
		public void run() {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						String statusMessage = statusMessages.poll();
						if (statusMessage != null) {
							blankOutCounter = INITIAL_BLANK_OUT_COUNTER;
							statusLine.setText(statusMessage);
							statusLine.setVisible(true);							
						} else {
							if (blankOutCounter-- < 1) {
								statusLine.setVisible(false);
							}
						}
					}
				});
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (InvocationTargetException e) {
				logger.warn("Could not invoke timer ", e.getCause());
			}
		}
	}
}
