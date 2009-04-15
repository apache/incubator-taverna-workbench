package net.sf.taverna.t2.servicedescriptions;

import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.ConfigurationException;

public abstract class AbstractConfigurableServiceDescriptionProvider<ConfigType>
		implements ConfigurableServiceDescriptionProvider<ConfigType> {

	protected ConfigType serviceProviderConfig;

	public AbstractConfigurableServiceDescriptionProvider() {
		super();
	}

	public void configure(ConfigType conf) throws ConfigurationException {
		this.serviceProviderConfig = conf;
	}

	public ConfigType getConfiguration() {
		return serviceProviderConfig;
	}

	public List<ConfigType> getDefaultConfigurations() {
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public AbstractConfigurableServiceDescriptionProvider<ConfigType> clone() {
		AbstractConfigurableServiceDescriptionProvider<ConfigType> provider;
		try {
			provider = getClass().newInstance();
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(
					"Can't clone accessing default constructor", ex);
		} catch (InstantiationException ex) {
			throw new RuntimeException("Can't clone using default constructor",
					ex);
		}

		ConfigType configuration = getConfiguration();
		if (configuration != null) {
			try {
				provider.configure(configuration);
			} catch (ConfigurationException ex) {
				throw new RuntimeException("Can't configure clone", ex);
			}
		}
		return provider;
	}

}