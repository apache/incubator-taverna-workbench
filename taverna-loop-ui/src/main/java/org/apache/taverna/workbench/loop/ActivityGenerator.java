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
package org.apache.taverna.workbench.loop;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.taverna.workbench.loop.comparisons.Comparison;
import org.apache.taverna.workbench.loop.comparisons.EqualTo;
import org.apache.taverna.workbench.loop.comparisons.IsGreaterThan;
import org.apache.taverna.workbench.loop.comparisons.IsLessThan;
import org.apache.taverna.workbench.loop.comparisons.Matches;
import org.apache.taverna.workbench.loop.comparisons.NotEqualTo;
import org.apache.taverna.workbench.loop.comparisons.NotMatches;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.port.InputActivityPort;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.OutputActivityPort;
import org.apache.taverna.scufl2.api.port.OutputProcessorPort;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ActivityGenerator {

    private static final String LOOP_PORT = "loop";

    private static final String SCRIPT = "script";

    public static URI BEANSHELL_ACTIVITY = URI
            .create("http://ns.taverna.org.uk/2010/activity/beanshell");

    public static URI BEANSHELL_CONFIG = BEANSHELL_ACTIVITY.resolve("#Config");

    
	public static final double DEFAULT_DELAY_S = 0.2;
	public static final String COMPARE_PORT = "comparePort";
	public static final String COMPARISON = "comparison";
	public static final String CUSTOM_COMPARISON = "custom";
	public static final String COMPARE_VALUE = "compareValue";
	public static final String IS_FEED_BACK = "isFeedBack";
	public static final String DELAY = "delay";

	private static Logger logger = Logger.getLogger(ActivityGenerator.class);
	private final ObjectNode loopProperties;
	private final Processor processorToCompare;
	private static Scufl2Tools scufl2Tools = new Scufl2Tools();

	public ActivityGenerator(ObjectNode configuration,
			Processor processorToCompare) {
		this.loopProperties = configuration;
		this.processorToCompare = processorToCompare;
	}

	protected Activity generateActivity() {
		Activity beanshell = new Activity();
		beanshell.setType(BEANSHELL_ACTIVITY);
		Configuration config = generateBeanshellConfig(beanshell);
		// TODO: Where to put the config?
		return beanshell;
	}

	private Configuration generateBeanshellConfig(Activity beanshell) {
	    Configuration config = scufl2Tools.createConfigurationFor(beanshell, BEANSHELL_CONFIG);
	    generateInputPorts(beanshell);
	    generateOutputPorts(beanshell);
	    config.getJsonAsObjectNode().put(SCRIPT, generateScript());
		return config;
	}

	protected static List<Comparison> comparisons = Arrays.asList(
			new EqualTo(), new NotEqualTo(), new Matches(), new NotMatches(),
			new IsGreaterThan(), new IsLessThan());

	protected static Comparison getComparisonById(String id) {
	    if (id == null || id.isEmpty()) {
	        return comparisons.get(0);
	    }
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
		replacements.put("${loopPort}", LOOP_PORT);
		replacements.put("${port}", loopProperties.findValue(COMPARE_PORT).asText());
		replacements.put("${value}", beanshellString(loopProperties
				.findValue(COMPARE_VALUE).asText()));


		// as seconds
		Double delay = loopProperties.findPath(DELAY).asDouble(DEFAULT_DELAY_S);
		// as milliseconds
		delay = Math.max(0.0, delay) * 1000;
		// as integer (for Thread.sleep)
		replacements.put("${delay}", Integer.toString(delay.intValue()));

		String template = getComparisonById(
				loopProperties.findValue(COMPARISON).asText()).getScriptTemplate();

		if (delay > 0.0) {
    		template += "\nif (\"true\".matches(${loopPort})) {\n";
    		template += "   Thread.sleep(${delay});\n";
    		template += "}";
		}

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

	private void generateInputPorts(Activity beanshell) {
		if (processorToCompare == null) {
		    return;
		}
		for (OutputProcessorPort procOut : processorToCompare.getOutputPorts()) {
		    // Any of the outputs are available to the script, giving
		    // a custom script that compares multiple outputs a better
		    // starting point.
			String portName = procOut.getName();
			if (portName.equals(loopProperties.findValue(COMPARE_PORT).asText()) ||
			        (loopProperties.findValue(IS_FEED_BACK).asBoolean())) {
				InputActivityPort input = new InputActivityPort(beanshell, portName);
				input.setDepth(procOut.getDepth());
				input.setParent(beanshell);
			}
		}
	}

	private void generateOutputPorts(Activity beanshell) {
	       OutputActivityPort loopPort = new OutputActivityPort(beanshell, LOOP_PORT);
	        loopPort.setDepth(0);
	        loopPort.setGranularDepth(0);
	    if (processorToCompare == null) {
            return;
	    }	    
	    if (! loopProperties.findValue(IS_FEED_BACK).asBoolean()) {
           return;
	    }
	    for (InputProcessorPort procIn : processorToCompare.getInputPorts()) {
            String portName = procIn.getName();
            if (processorToCompare.getOutputPorts().containsName(portName)) {
                OutputActivityPort actOut = new OutputActivityPort(beanshell, portName);
                actOut.setDepth(procIn.getDepth());
                actOut.setGranularDepth(procIn.getDepth());
            }
	    }
	}
}
