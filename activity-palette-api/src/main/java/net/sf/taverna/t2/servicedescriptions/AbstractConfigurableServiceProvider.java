package net.sf.taverna.t2.servicedescriptions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.ConfigurationException;

public abstract class AbstractConfigurableServiceProvider<ConfigType>
		implements ConfigurableServiceProvider<ConfigType> {

	@SuppressWarnings("unchecked")
	private Collection<? extends ServiceDescription> cachedDescriptions;

	protected ConfigType serviceProviderConfig;

	/**
	 * Construct configurable service provider.
	 * 
	 * @param configTemplate
	 *            Template configuration
	 */
	public AbstractConfigurableServiceProvider(ConfigType configTemplate) {
		if (configTemplate == null) {
			throw new NullPointerException("Default config can't be null");
		}
		serviceProviderConfig = configTemplate;
	}

	/**
	 * Package access constructor - only used with {@link #clone()} - otherwise
	 * use {@link #AbstractConfigurableServiceProvider(Object)}
	 */
	AbstractConfigurableServiceProvider() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public AbstractConfigurableServiceProvider<ConfigType> clone() {
		AbstractConfigurableServiceProvider<ConfigType> provider;
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

	public synchronized void configure(ConfigType conf)
			throws ConfigurationException {
		if (conf == null) {
			throw new NullPointerException("Config can't be null");
		}
		this.serviceProviderConfig = conf;
	}

	public ConfigType getConfiguration() {
		return serviceProviderConfig;
	}

	public List<ConfigType> getDefaultConfigurations() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return getName() + " " + getConfiguration();
	}

}