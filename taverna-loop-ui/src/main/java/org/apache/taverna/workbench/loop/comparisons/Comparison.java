package org.apache.taverna.workbench.loop.comparisons;

import org.apache.taverna.workbench.loop.LoopConfigurationPanel;

/**
 * A comparison beanshell template for {@link LoopConfigurationPanel}.
 * <p>
 * A comparison is a template for generating a beanshell that can be used for
 * comparisons in say the {@link Loop} layer.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class Comparison {

	public String toString() {
		return getName();
	}

	public abstract String getId();

	public abstract String getName();

	public abstract String getValueType();

	public abstract String getScriptTemplate();
}