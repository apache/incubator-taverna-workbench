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

package org.apache.taverna.workbench.ui.servicepanel.actions;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.BorderLayout.WEST;
import static java.awt.event.KeyEvent.VK_ENTER;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.workbench.MainWindow.getMainWindow;
import static org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptors;
import static org.apache.log4j.Logger.getLogger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.lang.uibuilder.UIBuilder;
import org.apache.taverna.servicedescriptions.ConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.CustomizedConfigurePanelProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.servicedescriptions.CustomizedConfigurePanelProvider.CustomizedConfigureCallBack;
import org.apache.taverna.servicedescriptions.events.ProviderErrorNotification;
import org.apache.taverna.servicedescriptions.events.ServiceDescriptionProvidedEvent;
import org.apache.taverna.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import org.apache.taverna.workbench.helper.HelpEnabledDialog;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.configurations.Configuration;

/**
 * Action for adding a service provider
 * 
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public class AddServiceProviderAction extends AbstractAction {
	private static Logger logger = getLogger(AddServiceProviderAction.class);

	// protected static Dimension DIALOG_SIZE = new Dimension(400, 300);

	private ServiceDescriptionRegistry serviceDescriptionRegistry;

	private final ConfigurableServiceProvider confProvider;
	private final Component owner;

	public AddServiceProviderAction(ConfigurableServiceProvider confProvider,
			Component owner) {
		super(confProvider.getName() + "...", confProvider.getIcon());
		this.confProvider = confProvider;
		this.owner = owner;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (confProvider instanceof CustomizedConfigurePanelProvider) {
			final CustomizedConfigurePanelProvider provider = (CustomizedConfigurePanelProvider) confProvider;
			provider.createCustomizedConfigurePanel(new CustomizedConfigureCallBack() {
				@Override
				public Configuration getTemplateConfig() {
					return (Configuration) provider.getConfiguration().clone();
				}

				@Override
				public ServiceDescriptionRegistry getServiceDescriptionRegistry() {
					return AddServiceProviderAction.this.getServiceDescriptionRegistry();
				}

				@Override
				public void newProviderConfiguration(Configuration providerConfig) {
					addNewProvider(providerConfig);
				}
			});
			return;
		}

		Configuration configuration;
		try {
			configuration = (Configuration) confProvider.getConfiguration().clone();
		} catch (Exception ex) {
			throw new RuntimeException("Can't clone configuration bean", ex);
		}
		JPanel buildEditor = buildEditor(configuration);
		String title = "Add " + confProvider.getName();
		JDialog dialog = new HelpEnabledDialog(getMainWindow(), title, true, null);
		JPanel iconPanel = new JPanel();
		iconPanel.add(new JLabel(confProvider.getIcon()), NORTH);
		dialog.add(iconPanel, WEST);
		dialog.add(buildEditor, CENTER);
		JPanel buttonPanel = new JPanel(new BorderLayout());
		final AddProviderAction addProviderAction = new AddProviderAction(configuration,
				dialog);
		JButton addProviderButton = new JButton(addProviderAction);
		buttonPanel.add(addProviderButton, WEST);
		
		dialog.add(buttonPanel, SOUTH);
	    // When user presses "Return" key fire the action on the "Add" button
		addProviderButton.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == VK_ENTER)
					addProviderAction.actionPerformed(null);
			}
		});
		dialog.getRootPane().setDefaultButton(addProviderButton);
		
		// dialog.setSize(buttonPanel.getPreferredSize());
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
//		dialog.setLocation(owner.getLocationOnScreen().x + owner.getWidth(),
//				owner.getLocationOnScreen().y + owner.getHeight());
		dialog.setVisible(true);
	}

	protected void addNewProvider(Configuration configurationBean) {
		ConfigurableServiceProvider cloned = (ConfigurableServiceProvider) confProvider
				.newInstance();
		try {
			cloned.configure(configurationBean);
			getServiceDescriptionRegistry().addObserver(
					new CheckAddedCorrectlyObserver(cloned));
			getServiceDescriptionRegistry().addServiceDescriptionProvider(
					cloned);
		} catch (Exception ex) {
			logger.warn("Can't configure provider " + cloned + " using "
					+ configurationBean, ex);
			showMessageDialog(owner, "Can't configure service provider "
					+ cloned.getName(), "Can't add service provider",
					ERROR_MESSAGE);
		}
	}

	private PropertyDescriptor[] getProperties(Configuration configuration) {
		// FIXME This is *so* wrong!
		try {
			return getPropertyDescriptors(configuration);
		} catch (Exception ex) {
			throw new RuntimeException("Can't inspect configuration bean", ex);
		}
	}

	// TODO This is probably not right
	protected JPanel buildEditor(Configuration configuration) {
		List<String> uiBuilderConfig = new ArrayList<>();
		int lastPreferred = 0;
		for (PropertyDescriptor property : getProperties(configuration)) {
			if (property.isHidden() || property.isExpert())
				// TODO: Add support for expert properties
				continue;
			String propertySpec = property.getName() + ":name="
					+ property.getDisplayName();
			if (property.isPreferred())
				// Add it to the front
				uiBuilderConfig.add(lastPreferred++, propertySpec);
			else
				uiBuilderConfig.add(propertySpec);
		}

		return UIBuilder.buildEditor(configuration, uiBuilderConfig
				.toArray(new String[0]));
	}

	public void setServiceDescriptionRegistry(
			ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public ServiceDescriptionRegistry getServiceDescriptionRegistry() {
		return serviceDescriptionRegistry;
	}

	public class AddProviderAction extends AbstractAction {
		private final Configuration configurationBean;
		private final JDialog dialog;

		private AddProviderAction(Configuration configurationBean, JDialog dialog) {
			super("Add");
			this.configurationBean = configurationBean;
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			addNewProvider(configurationBean);
			dialog.setVisible(false);
		}
	}

	public class CheckAddedCorrectlyObserver implements
			Observer<ServiceDescriptionRegistryEvent> {
		private final ConfigurableServiceProvider provider;

		private CheckAddedCorrectlyObserver(ConfigurableServiceProvider provider) {
			this.provider = provider;
		}

		@Override
		public void notify(Observable<ServiceDescriptionRegistryEvent> sender,
				ServiceDescriptionRegistryEvent message) throws Exception {
			if (message instanceof ProviderErrorNotification)
				notify((ProviderErrorNotification) message);
			else if (message instanceof ServiceDescriptionProvidedEvent)
				notify((ServiceDescriptionProvidedEvent) message);
		}

		private void notify(ServiceDescriptionProvidedEvent providedMsg) {
			if (providedMsg.getProvider() == provider)
				getServiceDescriptionRegistry().removeObserver(this);
		}

		private void notify(ProviderErrorNotification errorMsg) {
			if (errorMsg.getProvider() != provider)
				return;
			getServiceDescriptionRegistry().removeObserver(this);
			getServiceDescriptionRegistry().removeServiceDescriptionProvider(
					provider);
//			showMessageDialog(owner, errorMsg.getMessage(),
//					"Can't add provider " + provider, ERROR_MESSAGE);
		}
	}
}