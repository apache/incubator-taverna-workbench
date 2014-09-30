package net.sf.taverna.t2.servicedescriptions;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractConfigurableServiceProvider extends
		IdentifiedObject implements ConfigurableServiceProvider {
	protected ObjectNode serviceProviderConfig;

	/**
	 * Construct configurable service provider.
	 * 
	 * @param configTemplate
	 *            Template configuration
	 */
	public AbstractConfigurableServiceProvider(ObjectNode configTemplate) {
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

		ObjectNode configuration = getConfiguration();
		if (configuration != null)
			provider.configure(configuration);
		return provider;
	}

	@Override
	public synchronized void configure(ObjectNode conf) {
		if (conf == null)
			throw new NullPointerException("Config can't be null");
		this.serviceProviderConfig = conf;
	}

	@Override
	public ObjectNode getConfiguration() {
		return serviceProviderConfig;
	}

	@Override
	public List<ObjectNode> getDefaultConfigurations() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return getName() + " " + getConfiguration();
	}
}
