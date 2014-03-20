/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.loop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workbench.loop.comparisons.Comparison;
import net.sf.taverna.t2.workbench.loop.comparisons.EqualTo;
import net.sf.taverna.t2.workbench.loop.comparisons.IsGreaterThan;
import net.sf.taverna.t2.workbench.loop.comparisons.IsLessThan;
import net.sf.taverna.t2.workbench.loop.comparisons.Matches;
import net.sf.taverna.t2.workbench.loop.comparisons.NotEqualTo;
import net.sf.taverna.t2.workbench.loop.comparisons.NotMatches;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Loop;

import org.apache.log4j.Logger;

public class ActivityGenerator {

	public static final String DEFAULT_DELAY_S = "0.2";

	
	public static final String COMPARE_PORT = "comparePort";
	public static final String COMPARISON = "comparison";
	public static final String CUSTOM_COMPARISON = "custom";
	public static final String COMPARE_VALUE = "compareValue";
	public static final String IS_FEED_BACK = "isFeedBack";
	public static final String DELAY = "delay";

	private static Logger logger = Logger.getLogger(ActivityGenerator.class);
	private final Properties loopProperties;
	private final Activity<?> activityToCompare;

	public ActivityGenerator(Properties loopProperties,
			Activity<?> activityToCompare) {
		this.loopProperties = loopProperties;
		this.activityToCompare = activityToCompare;
	}

	protected Activity<?> generateActivity() {
		BeanshellActivity beanshell = new BeanshellActivity();
		BeanshellActivityConfigurationBean beanshellConfig = generateBeanshellConfig();
		try {
			beanshell.configure(beanshellConfig);
		} catch (ActivityConfigurationException e) {
			logger.error("Could not configure beanshell", e);
			return null;
		}
		return beanshell;
	}

	private BeanshellActivityConfigurationBean generateBeanshellConfig() {
		BeanshellActivityConfigurationBean config = new BeanshellActivityConfigurationBean();
		config.setInputPortDefinitions(generateInputPorts());
		config.setOutputPortDefinitions(generateOutputPorts());
		config.setScript(generateScript());
		return config;
	}

	protected static List<Comparison> comparisons = Arrays.asList(
			new EqualTo(), new NotEqualTo(), new Matches(), new NotMatches(),
			new IsGreaterThan(), new IsLessThan());

	protected static Comparison getComparisonById(String id) {
		for (Comparison potentialComparison : comparisons) {
			if (potentialComparison.getId().equals(id)) {
				return potentialComparison;
			}
		}
		return null;
	}

	@SuppressWarnings("boxing")
	private String generateScript() {
		Map<String, String> replacements = new HashMap<String, String>();
		replacements.put("${loopPort}", Loop.LOOP_PORT);
		replacements.put("${port}", loopProperties.getProperty(COMPARE_PORT));
		replacements.put("${value}", beanshellString(loopProperties
				.getProperty(COMPARE_VALUE)));
		
		
		String delaySeconds = loopProperties.getProperty(DELAY, DEFAULT_DELAY_S);
		Double delay;
		try {
			delay = Double.parseDouble(delaySeconds) * 1000;
		} catch (NumberFormatException ex) {
			logger.warn("Invalid number for loop delay: " + delaySeconds);
			delay = 0.0;
		}
		delay = Math.max(0.0, delay);
		
		replacements.put("${delay}", Integer.toString(delay.intValue()));
		
		String template = getComparisonById(
				loopProperties.getProperty(COMPARISON)).getScriptTemplate();

		template += "\nif (\"true\".matches(${loopPort})) {\n";
		template += "   Thread.sleep(${delay});\n";
		template += "}";
		
		String script = template;
		for (Entry<String, String> mapping : replacements.entrySet()) {
			script = script.replace(mapping.getKey(), mapping.getValue());
		}
		return script;
	}

	private String beanshellString(String value) {
		value = value.replace("\\", "\\\\");
		value = value.replace("\n", "\\n");
		value = value.replace("\"", "\\\"");
		return '"' + value + '"';
	}

	private List<ActivityInputPortDefinitionBean> generateInputPorts() {
		List<ActivityInputPortDefinitionBean> inputs = new ArrayList<ActivityInputPortDefinitionBean>();
		if (activityToCompare == null) {
			return inputs;
		}
		for (OutputPort outputPort : activityToCompare.getOutputPorts()) {
			ActivityInputPortDefinitionBean inputDef = new ActivityInputPortDefinitionBean();
			String activityPortName = outputPort.getName();
			String processorPortName = activityToCompare.getOutputPortMapping()
					.get(activityPortName);
			if (processorPortName == null) {
				// We'll need to map it later
				processorPortName = activityPortName;
			}
			inputDef.setName(processorPortName);
			inputDef.setDepth(outputPort.getDepth());

			inputDef.setAllowsLiteralValues(true);
			if (loopProperties.getProperty(COMPARE_PORT).equals(
					activityPortName)) {
				inputDef.setAllowsLiteralValues(true);
				inputDef.setTranslatedElementType(String.class);
			} else if (Boolean.parseBoolean(loopProperties
					.getProperty(IS_FEED_BACK))) {
				// Don't translate
				// inputDef.setTranslatedElementType(T2Reference.class);
				inputDef.setTranslatedElementType(Object.class);
				// FIXME: Need a way to allow T2Reference into beanshell
			} else {
				continue;
			}
			inputs.add(inputDef);
		}
		return inputs;
	}

	private List<ActivityOutputPortDefinitionBean> generateOutputPorts() {
		List<ActivityOutputPortDefinitionBean> outputs = new ArrayList<ActivityOutputPortDefinitionBean>();
		ActivityOutputPortDefinitionBean outputDef = new ActivityOutputPortDefinitionBean();
		outputDef.setName(Loop.LOOP_PORT);
		outputDef.setDepth(0);
		outputDef.setGranularDepth(0);
		outputs.add(outputDef);

		if (activityToCompare == null
				|| !Boolean.parseBoolean(loopProperties
						.getProperty(IS_FEED_BACK))) {
			return outputs;
		}

		Set<String> feedbackPorts = findFeedbackPorts();

		// We'll add the feedback ports as outputs
		for (OutputPort outputPort : activityToCompare.getOutputPorts()) {
			outputDef = new ActivityOutputPortDefinitionBean();
			String activityPortName = outputPort.getName();
			String processorPortName = activityToCompare.getOutputPortMapping()
					.get(activityPortName);
			if (processorPortName == null) {
				// We'll need to map it later
				processorPortName = activityPortName;
			}
			if (!feedbackPorts.contains(processorPortName)) {
				// Skip output ports that don't have a matching input port
				// etc.
				continue;
			}
			outputDef.setName(processorPortName);
			outputDef.setDepth(outputPort.getDepth());
			outputDef.setGranularDepth(outputDef.getDepth());
			outputs.add(outputDef);
		}

		return outputs;
	}

	private HashSet<String> findFeedbackPorts() {
		HashSet<String> incoming = new HashSet<String>();
		for (InputPort inputPort : activityToCompare.getInputPorts()) {
			String activityPortName = inputPort.getName();
			String processorPortName = activityToCompare.getInputPortMapping()
					.get(activityPortName);
			if (processorPortName == null) {
				// We'll need to map it later
				processorPortName = activityPortName;
			}
			incoming.add(processorPortName);
		}

		HashSet<String> outgoing = new HashSet<String>();
		for (OutputPort outputPort : activityToCompare.getOutputPorts()) {
			String activityPortName = outputPort.getName();
			String processorPortName = activityToCompare.getOutputPortMapping()
					.get(activityPortName);
			if (processorPortName == null) {
				// We'll need to map it later
				processorPortName = activityPortName;
			}
			outgoing.add(processorPortName);
		}

		// Return the port names that are common
		incoming.retainAll(outgoing);
		return incoming;
	}
}
