/**
 *
 */
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import uk.org.taverna.commons.services.ActivityTypeNotFoundException;
import uk.org.taverna.commons.services.InvalidConfigurationException;
import uk.org.taverna.commons.services.ServiceRegistry;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.OutputActivityPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author alanrw
 */
@SuppressWarnings("serial")
public abstract class ActivityConfigurationPanel extends JPanel {

	private static final Logger logger = Logger.getLogger(ActivityConfigurationPanel.class);

	private final static Scufl2Tools scufl2Tools = new Scufl2Tools();
	// protected final URITools uriTools = new URITools();
	private final Activity activity;
	private final Configuration configuration;
	private final List<ActivityPortConfiguration> inputPorts;
	private final List<ActivityPortConfiguration> outputPorts;
	protected ObjectNode json;

	public ActivityConfigurationPanel(Activity activity) {
		this(activity, scufl2Tools.configurationFor(activity, activity.getParent()));
	}

	public ActivityConfigurationPanel(Activity activity, Configuration configuration) {
		this.activity = activity;
		this.configuration = configuration;
		inputPorts = new ArrayList<>();
		outputPorts = new ArrayList<>();
	}

	/**
	 * Initializes the configuration panel.
	 * <p>
	 * This method is also used to discard any changes and reset the panel to its initial state.
	 * <p>
	 * Subclasses should implement this method to set up the panel and must call super.initialise()
	 * first.
	 */
	protected void initialise() {
		json = configuration.getJson().deepCopy();
		inputPorts.clear();
		for (InputActivityPort activityPort : activity.getInputPorts()) {
			inputPorts.add(new ActivityPortConfiguration(activityPort));
		}
		outputPorts.clear();
		for (OutputActivityPort activityPort : activity.getOutputPorts()) {
			outputPorts.add(new ActivityPortConfiguration(activityPort));
		}
	}

	public abstract boolean checkValues();

	public abstract void noteConfiguration();

	public boolean isConfigurationChanged() {
		// TODO check ports
		return !json.equals(configuration.getJson());
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public ObjectNode getJson() {
		return json;
	}

	protected void setJson(ObjectNode json) {
		this.json = json;
	}

	public void refreshConfiguration() {
		initialise();
	}

	public void whenOpened() {
	}

	public void whenClosed() {
	}

	/**
	 * Convenience method for getting simple String property values.
	 *
	 * @param name
	 *            the property name
	 * @return the property value
	 */
	protected String getProperty(String name) {
		JsonNode jsonNode = json.get(name);
		if (jsonNode == null) {
			return null;
		}
		return json.get(name).asText();
	}

	/**
	 * Convenience method for setting simple String property values.
	 *
	 * @param name
	 *            the property name
	 * @param value
	 *            the property value
	 */
	protected void setProperty(String name, String value) {
		json.put(name, value);
	}

	public List<ActivityPortConfiguration> getInputPorts() {
		return inputPorts;
	}

	public List<ActivityPortConfiguration> getOutputPorts() {
		return outputPorts;
	}

	protected void configureInputPorts(ServiceRegistry serviceRegistry) {
		try {
			Map<String, InputActivityPort> newInputPorts = new HashMap<>();
			Set<InputActivityPort> activityInputPorts = serviceRegistry.getActivityInputPorts(
					getActivity().getType(), getJson());
			for (InputActivityPort inputActivityPort : activityInputPorts) {
				newInputPorts.put(inputActivityPort.getName(), inputActivityPort);
			}
			List<ActivityPortConfiguration> inputPorts = getInputPorts();
			for (ActivityPortConfiguration portConfiguration : new ArrayList<>(inputPorts)) {
				if (newInputPorts.containsKey(portConfiguration.getName())) {
					InputActivityPort port = newInputPorts.remove(portConfiguration.getName());
					portConfiguration.setDepth(port.getDepth());
				} else {
					inputPorts.remove(portConfiguration);
				}
			}
			for (InputActivityPort newPort : newInputPorts.values()) {
				inputPorts.add(new ActivityPortConfiguration(newPort.getName(), newPort.getDepth()));
			}
		} catch (InvalidConfigurationException | ActivityTypeNotFoundException e) {
			logger.warn("Error configuring input ports", e);
		}
	}

	protected void configureOutputPorts(ServiceRegistry serviceRegistry) {
		try {
			Map<String, OutputActivityPort> newOutputPorts = new HashMap<>();
			Set<OutputActivityPort> activityOutputPorts = serviceRegistry.getActivityOutputPorts(
					getActivity().getType(), getJson());
			for (OutputActivityPort outputActivityPort : activityOutputPorts) {
				newOutputPorts.put(outputActivityPort.getName(), outputActivityPort);
			}
			List<ActivityPortConfiguration> outputPorts = getOutputPorts();
			for (ActivityPortConfiguration portConfiguration : new ArrayList<>(outputPorts)) {
				if (newOutputPorts.containsKey(portConfiguration.getName())) {
					OutputActivityPort port = newOutputPorts.remove(portConfiguration.getName());
					portConfiguration.setDepth(port.getDepth());
					portConfiguration.setGranularDepth(port.getGranularDepth());
				} else {
					outputPorts.remove(portConfiguration);
				}
			}
			for (OutputActivityPort newPort : newOutputPorts.values()) {
				outputPorts.add(new ActivityPortConfiguration(newPort.getName(), newPort.getDepth()));
			}
		} catch (InvalidConfigurationException | ActivityTypeNotFoundException e) {
			logger.warn("Error configuring output ports", e);
		}
	}

	public Activity getActivity() {
		return activity;
	}

}
