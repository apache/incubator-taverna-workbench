package net.sf.taverna.t2.workbench.ui.servicepanel.config;

import javax.swing.JPanel;

import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionsConfig;
import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;

public class ServiceDescriptionConfigUIFactory implements
		ConfigurationUIFactory {
	
	protected ServiceDescriptionsConfig serviceDescriptionsConfig = ServiceDescriptionsConfig
			.getInstance();

	public boolean canHandle(String uuid) {
		return uuid.equals(serviceDescriptionsConfig.getUUID());
	}

	public Configurable getConfigurable() {
		return serviceDescriptionsConfig;
	}

	public JPanel getConfigurationPanel() {
		return new ServiceDescriptionConfigPanel(serviceDescriptionsConfig, ServiceDescriptionRegistryImpl.getInstance());
	}

}
