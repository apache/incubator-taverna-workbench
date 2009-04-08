package net.sf.taverna.t2.servicedescriptions;
import net.sf.taverna.t2.workflowmodel.Configurable;

public interface ConfigurableServiceDescriptionProvider<ConfigurationBean>
		extends ServiceDescriptionProvider, Configurable<ConfigurationBean> {

}
