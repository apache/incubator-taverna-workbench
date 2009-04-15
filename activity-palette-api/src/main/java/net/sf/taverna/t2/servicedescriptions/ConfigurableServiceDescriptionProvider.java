package net.sf.taverna.t2.servicedescriptions;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.Configurable;

public interface ConfigurableServiceDescriptionProvider<ConfigurationBean>
		extends ServiceDescriptionProvider, Configurable<ConfigurationBean>,
		Cloneable {

	public List<ConfigurationBean> getDefaultConfigurations();

	public ConfigurableServiceDescriptionProvider<ConfigurationBean> clone();

}
