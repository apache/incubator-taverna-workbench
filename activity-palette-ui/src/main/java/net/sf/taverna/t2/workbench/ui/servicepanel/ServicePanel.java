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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.events.AbstractProviderEvent;
import net.sf.taverna.t2.servicedescriptions.events.AbstractProviderNotification;
import net.sf.taverna.t2.servicedescriptions.events.PartialServiceDescriptionsNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderErrorNotification;
import net.sf.taverna.t2.servicedescriptions.events.RemovedProviderEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionProvidedEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import net.sf.taverna.t2.workbench.ui.servicepanel.servicetree.ServiceTreeModel;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

import org.apache.log4j.Logger;

/**
 * A panel of available services
 * 
 * @author Stian Soiland-Reyes
 * 
 */
@SuppressWarnings("serial")
public class ServicePanel extends JPanel implements UIComponentSPI {

	private static Logger logger = Logger.getLogger(ServicePanel.class);

	private static final int INITIAL_BLANK_OUT_COUNTER = 2;

	public int blankOutCounter = 0;

	private static final int STATUS_LINE_MESSAGE_MS = 600;

	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	private ServiceTreePanel serviceTreePanel;

	private JLabel statusLine;

	private ServiceTreeModel serviceTreeModel;

	protected ServiceDescriptionRegistryObserver serviceDescriptionRegistryObserver = new ServiceDescriptionRegistryObserver();

	protected Timer statusUpdateTimer;

	protected Object updateLock = new Object();

	private final Set<PartialServiceDescriptionsNotification> addedServiceNotifications = new HashSet<PartialServiceDescriptionsNotification>();

	private final Set<RemovedProviderEvent> removedProviderNotifications = new HashSet<RemovedProviderEvent>();

	public ServicePanel(
			final ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		initialise();

		final TitledBorder border = new TitledBorder("Service panel");
		border.setTitleJustification(TitledBorder.CENTER);
		this.setBorder(border);

		@SuppressWarnings("rawtypes")
		final Set<ServiceDescription> existingDescriptions = serviceDescriptionRegistry
				.getServiceDescriptions();

		serviceDescriptionRegistry
				.addObserver(serviceDescriptionRegistryObserver);

		SwingUtilities.invokeLater(new TreeAdderRunnable(existingDescriptions));
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return "Service panel";
	}

	@Override
	public void onDisplay() {
	}

	@Override
	public void onDispose() {
	}

	public void providerStatus(final ServiceDescriptionProvider provider,
			final String message) {
		logger.info(message + " " + provider);
		final String htmlMessage = "<html><small>" + message + " [" + provider
				+ "]</small></html>";

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				blankOutCounter = INITIAL_BLANK_OUT_COUNTER;
				statusLine.setText(htmlMessage);
				statusLine.setVisible(true);
			}
		});
	}

	protected void initialise() {
		removeAll();
		setLayout(new BorderLayout());

		serviceTreeModel = new ServiceTreeModel();
		serviceTreePanel = new ServiceTreePanel(serviceTreeModel,
				serviceDescriptionRegistry);
		add(serviceTreePanel, BorderLayout.CENTER);
		statusLine = new JLabel();
		add(statusLine, BorderLayout.SOUTH);
		if (statusUpdateTimer != null) {
			statusUpdateTimer.cancel();
		}
		statusUpdateTimer = new Timer("Clear status line", true);
		statusUpdateTimer.scheduleAtFixedRate(new UpdateStatusLineTask(), 0,
				STATUS_LINE_MESSAGE_MS);
		;
	}

	public class TreeAdderRunnable implements Runnable {

		@SuppressWarnings("rawtypes")
		private final Collection<? extends ServiceDescription> addedDescriptions;

		public TreeAdderRunnable(
				@SuppressWarnings("rawtypes") final Collection<? extends ServiceDescription> collection) {
			this.addedDescriptions = collection;
		}

		@Override
		public void run() {
			serviceTreeModel.addServiceDescriptions(addedDescriptions);
			serviceTreePanel.expandRoot();
		}

	}

	public class TreeRemoverRunnable implements Runnable {

		@SuppressWarnings("rawtypes")
		private final Collection<? extends ServiceDescription> removedDescriptions;

		public TreeRemoverRunnable(
				@SuppressWarnings("rawtypes") final Collection<? extends ServiceDescription> collection) {
			this.removedDescriptions = collection;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@SuppressWarnings("rawtypes")
		@Override
		public void run() {
			for (final ServiceDescription sd : removedDescriptions) {
				serviceTreeModel.removeServiceDescription(sd);
			}
		}

	}

	private final class ServiceDescriptionRegistryObserver implements
			Observer<ServiceDescriptionRegistryEvent> {
		Set<ServiceDescriptionProvider> alreadyComplainedAbout = new HashSet<ServiceDescriptionProvider>();

		@Override
		public void notify(
				final Observable<ServiceDescriptionRegistryEvent> sender,
				final ServiceDescriptionRegistryEvent message) throws Exception {
			logger.info("Received a " + message.getClass().getCanonicalName());
			if (message instanceof ProviderErrorNotification) {
				final ProviderErrorNotification pen = (ProviderErrorNotification) message;
				reportServiceProviderError(pen);
			} else if ((message instanceof ServiceDescriptionProvidedEvent)
					|| (message instanceof RemovedProviderEvent)) {
				final AbstractProviderEvent ape = (AbstractProviderEvent) message;
				alreadyComplainedAbout.remove(ape.getProvider());
			}

			if (message instanceof AbstractProviderNotification) {
				final AbstractProviderNotification abstractProviderNotification = (AbstractProviderNotification) message;
				providerStatus(abstractProviderNotification.getProvider(),
						abstractProviderNotification.getMessage());
			}
			if (message instanceof PartialServiceDescriptionsNotification) {
				final PartialServiceDescriptionsNotification notification = (PartialServiceDescriptionsNotification) message;
				synchronized (addedServiceNotifications) {
					addedServiceNotifications.add(notification);
				}

				SwingUtilities.invokeLater(new TreeAdderRunnable(notification
						.getServiceDescriptions()));
			}
			if (message instanceof RemovedProviderEvent) {
				final RemovedProviderEvent rpe = (RemovedProviderEvent) message;
				synchronized (removedProviderNotifications) {
					removedProviderNotifications.add(rpe);
				}

				SwingUtilities.invokeLater(new TreeRemoverRunnable(rpe
						.getRemovedDescriptions()));
			}
		}

		private void reportServiceProviderError(
				final ProviderErrorNotification pen) {
			final ServiceDescriptionProvider provider = pen.getProvider();

			if (serviceDescriptionRegistry
					.getDefaultServiceDescriptionProviders().contains(provider)) {
				return;
			}
			if (alreadyComplainedAbout.contains(provider)) {
				return;
			}
			alreadyComplainedAbout.add(provider);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(ServicePanel.this,
							pen.getMessage() + "\n" + pen.getProvider(),
							"Import service error", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}

	private final class UpdateStatusLineTask extends TimerTask {
		@Override
		public void run() {
			if ((blankOutCounter < 0) || (blankOutCounter-- > 0)) {
				// Only clear it once
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (blankOutCounter < 0) {
						statusLine.setVisible(false);
					}
				}
			});
		}
	}
}
