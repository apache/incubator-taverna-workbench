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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.BorderLayout.WEST;
import static java.awt.event.KeyEvent.VK_ENTER;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static net.sf.taverna.t2.workbench.MainWindow.getMainWindow;
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

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.uibuilder.UIBuilder;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.CustomizedConfigurePanelProvider.CustomizedConfigureCallBack;
import net.sf.taverna.t2.servicedescriptions.events.ProviderErrorNotification;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionProvidedEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.configurations.Configuration;

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
		final ConfigurableServiceProvider cloned = (ConfigurableServiceProvider) confProvider
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

	// FIXME This is *so* wrong!
	protected JPanel buildEditor(Configuration configuration) {
		PropertyDescriptor[] properties;
		try {
			properties = getPropertyDescriptors(configuration);
		} catch (Exception ex) {
			throw new RuntimeException("Can't inspect configuration bean", ex);
		}
		List<String> uiBuilderConfig = new ArrayList<>();
		int lastPreferred = 0;
		for (PropertyDescriptor property : properties) {
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
				.toArray(new String[uiBuilderConfig.size()]));
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