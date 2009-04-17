package net.sf.taverna.t2.servicedescriptions;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.Configurable;

public interface ConfigurableServiceProvider<ConfigurationBean>
		extends ServiceDescriptionProvider, Configurable<ConfigurationBean>,
		Cloneable {

	public List<ConfigurationBean> getDefaultConfigurations();

	public ConfigurableServiceProvider<ConfigurationBean> clone();
	
}
