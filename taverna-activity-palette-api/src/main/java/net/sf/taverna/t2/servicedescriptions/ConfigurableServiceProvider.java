package net.sf.taverna.t2.servicedescriptions;

import org.apache.taverna.scufl2.api.common.Configurable;
import org.apache.taverna.scufl2.api.configurations.Configuration;

public interface ConfigurableServiceProvider extends
		ServiceDescriptionProvider, Configurable, Cloneable {
	void configure(Configuration configuration) throws Exception;
	Configuration getConfiguration();
}
