package net.sf.taverna.t2.servicedescriptions;

import uk.org.taverna.scufl2.api.configurations.Configuration;

import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractConfigurableServiceProvider extends
		IdentifiedObject implements ConfigurableServiceProvider {
	protected Configuration serviceProviderConfig;

	/**
	 * Construct configurable service provider.
	 * 
	 * @param configTemplate
	 *            Template configuration
	 */
	public AbstractConfigurableServiceProvider(Configuration configTemplate) {
		if (configTemplate == null)
			throw new NullPointerException("Default config can't be null");
		serviceProviderConfig = configTemplate;
	}

	/**
	 * Package access constructor - only used with {@link #clone()} - otherwise
	 * use {@link #AbstractConfigurableServiceProvider(Object)}
	 */
	AbstractConfigurableServiceProvider() {
	}

	@Override
	public AbstractConfigurableServiceProvider clone() {
		AbstractConfigurableServiceProvider provider;
		try {
			provider = getClass().newInstance();
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(
					"Can't clone accessing default constructor", ex);
		} catch (InstantiationException ex) {
			throw new RuntimeException("Can't clone using default constructor",
					ex);
		}

		Configuration configuration = getConfiguration();
		if (configuration != null)
			provider.configure(configuration);
		return provider;
	}

	@Override
	public synchronized void configure(Configuration conf) {
		if (conf == null)
			throw new IllegalArgumentException("Config can't be null");
		this.serviceProviderConfig = conf;
	}

	@Override
	public Configuration getConfiguration() {
		return serviceProviderConfig;
	}

	@Override
	public String toString() {
		return getName() + " " + getConfiguration();
	}
}
