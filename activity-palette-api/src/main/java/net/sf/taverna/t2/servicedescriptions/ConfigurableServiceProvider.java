package net.sf.taverna.t2.servicedescriptions;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sf.taverna.t2.workflowmodel.Configurable;

public interface ConfigurableServiceProvider extends
		ServiceDescriptionProvider, Configurable<ObjectNode>, Cloneable {
	List<ObjectNode> getDefaultConfigurations();

	ConfigurableServiceProvider clone();
}
