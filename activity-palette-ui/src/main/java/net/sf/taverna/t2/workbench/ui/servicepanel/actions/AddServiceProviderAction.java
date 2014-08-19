/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.servicepanel.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.uibuilder.UIBuilder;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider.CustomizedConfigureCallBack;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.events.ProviderErrorNotification;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionProvidedEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

/**
 * Action for adding a service provider
 * 
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public class AddServiceProviderAction extends AbstractAction {

	private static Logger logger = Logger
			.getLogger(AddServiceProviderAction.class);

	// protected static Dimension DIALOG_SIZE = new Dimension(400, 300);

	private ServiceDescriptionRegistry serviceDescriptionRegistry;

	@SuppressWarnings("rawtypes")
	private final ConfigurableServiceProvider confProvider;

	@SuppressWarnings("rawtypes")
	public AddServiceProviderAction(
			final ConfigurableServiceProvider confProvider,
			final Component owner) {
		super(confProvider.getName() + "...", confProvider.getIcon());
		this.confProvider = confProvider;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void actionPerformed(final ActionEvent e) {
		if (confProvider instanceof CustomizedConfigurePanelProvider) {
			// Clone it and run the configure on the new one
			final CustomizedConfigurePanelProvider customProvider = (CustomizedConfigurePanelProvider) confProvider
					.clone();
			final CustomizedConfigureCallBack callBack = new CustomizedConfigureCallBack() {
				@Override
				public void newProviderConfiguration(final Object providerConfig) {
					addNewProvider(providerConfig);
				}

				@Override
				public Object getTemplateConfig() {
					try {
						return BeanUtils.cloneBean(confProvider
								.getConfiguration());
					} catch (final Exception ex) {
						throw new RuntimeException(
								"Can't clone configuration bean", ex);
					}
				}

				@Override
				public ServiceDescriptionRegistry getServiceDescriptionRegistry() {
					return AddServiceProviderAction.this
							.getServiceDescriptionRegistry();
				}

			};
			customProvider.createCustomizedConfigurePanel(callBack);
			return;
		}

		Object configurationBean;
		try {
			configurationBean = BeanUtils.cloneBean(confProvider
					.getConfiguration());
		} catch (final Exception ex) {
			throw new RuntimeException("Can't clone configuration bean", ex);
		}
		final JPanel buildEditor = buildEditor(configurationBean);
		final String title = "Add " + confProvider.getName();
		final JDialog dialog = new HelpEnabledDialog(
				MainWindow.getMainWindow(), title, true, null);
		final JPanel iconPanel = new JPanel();
		iconPanel.add(new JLabel(confProvider.getIcon()), BorderLayout.NORTH);
		dialog.add(iconPanel, BorderLayout.WEST);
		dialog.add(buildEditor, BorderLayout.CENTER);
		final JPanel buttonPanel = new JPanel();
		final AddProviderAction addProviderAction = new AddProviderAction(
				configurationBean, dialog);
		final JButton addProviderButton = new JButton(addProviderAction);
		buttonPanel.add(addProviderButton, BorderLayout.WEST);

		dialog.add(buttonPanel, BorderLayout.SOUTH);
		// When user presses "Return" key fire the action on the "Add" button
		addProviderButton.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(final java.awt.event.KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					addProviderAction.actionPerformed(null);
				}
			}
		});
		dialog.getRootPane().setDefaultButton(addProviderButton);

		// dialog.setSize(buttonPanel.getPreferredSize());
		dialog.pack();
		dialog.setLocationRelativeTo(MainWindow.getMainWindow());
		// dialog.setLocation(owner.getLocationOnScreen().x + owner.getWidth(),
		// owner.getLocationOnScreen().y + owner.getHeight());
		dialog.setVisible(true);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void addNewProvider(final Object configurationBean) {
		final ConfigurableServiceProvider cloned = confProvider.clone();
		try {
			cloned.configure(configurationBean);
			getServiceDescriptionRegistry().addObserver(
					new CheckAddedCorrectlyObserver(cloned));
			getServiceDescriptionRegistry().addServiceDescriptionProvider(
					cloned);
		} catch (final ConfigurationException e1) {
			logger.warn("Can't configure provider " + cloned + " using "
					+ configurationBean, e1);
			JOptionPane.showMessageDialog(null,
					"Can't configure service provider " + cloned.getName(),
					"Can't add service provider", JOptionPane.ERROR_MESSAGE);

		}

	}

	protected JPanel buildEditor(final Object configurationBean) {
		PropertyDescriptor[] properties;
		try {
			properties = PropertyUtils
					.getPropertyDescriptors(configurationBean);
		} catch (final Exception ex) {
			throw new RuntimeException("Can't inspect configuration bean", ex);
		}
		final List<String> uiBuilderConfig = new ArrayList<String>();
		int lastPreferred = 0;
		for (final PropertyDescriptor property : properties) {
			if (property.isHidden() || property.isExpert()) {
				// TODO: Add support for expert properties
				continue;
			}
			final String propertySpec = property.getName() + ":name="
					+ property.getDisplayName();
			if (property.isPreferred()) {
				// Add it to the front
				uiBuilderConfig.add(lastPreferred++, propertySpec);
			} else {
				uiBuilderConfig.add(propertySpec);
			}
		}

		return UIBuilder.buildEditor(configurationBean,
				uiBuilderConfig.toArray(new String[uiBuilderConfig.size()]));
	}

	public void setServiceDescriptionRegistry(
			final ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public ServiceDescriptionRegistry getServiceDescriptionRegistry() {
		return serviceDescriptionRegistry;
	}

	public class AddProviderAction extends AbstractAction {

		private final Object configurationBean;
		private final JDialog dialog;

		private AddProviderAction(final Object configurationBean,
				final JDialog dialog) {
			super("Add");
			this.configurationBean = configurationBean;
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			addNewProvider(configurationBean);
			dialog.setVisible(false);
		}
	}

	public class CheckAddedCorrectlyObserver implements
			Observer<ServiceDescriptionRegistryEvent> {
		@SuppressWarnings("rawtypes")
		private final ConfigurableServiceProvider provider;

		@SuppressWarnings("rawtypes")
		private CheckAddedCorrectlyObserver(
				final ConfigurableServiceProvider provider) {
			this.provider = provider;
		}

		@Override
		public void notify(
				final Observable<ServiceDescriptionRegistryEvent> sender,
				final ServiceDescriptionRegistryEvent message) throws Exception {
			if (message instanceof ProviderErrorNotification) {
				final ProviderErrorNotification errorMsg = (ProviderErrorNotification) message;
				if (errorMsg.getProvider() == provider) {
					getServiceDescriptionRegistry().removeObserver(this);
					getServiceDescriptionRegistry()
							.removeServiceDescriptionProvider(provider);
					// JOptionPane.showMessageDialog(null,
					// errorMsg.getMessage(),
					// "Can't add provider " + provider,
					// JOptionPane.ERROR_MESSAGE);
				}
			} else if (message instanceof ServiceDescriptionProvidedEvent) {
				final ServiceDescriptionProvidedEvent providedMsg = (ServiceDescriptionProvidedEvent) message;
				if (providedMsg.getProvider() == provider) {
					getServiceDescriptionRegistry().removeObserver(this);
				}
			}
		}
	}

}