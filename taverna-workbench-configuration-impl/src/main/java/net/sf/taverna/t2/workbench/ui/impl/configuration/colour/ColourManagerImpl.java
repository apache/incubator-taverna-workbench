/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.impl.configuration.colour;

import static java.awt.Color.WHITE;
import static java.awt.Color.decode;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import uk.org.taverna.configuration.AbstractConfigurable;
import uk.org.taverna.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;

/**
 * A factory class that determines the colour that a Colourable UI component
 * should be displayed as, according to a schema configured by the user.
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * @see Colourable
 */
public class ColourManagerImpl extends AbstractConfigurable implements
		ColourManager {
	// Names of things that may be coloured
	private static final String WORKFLOW_PORT_OBJECT = "uk.org.taverna.scufl2.api.port.WorkflowPort";
	private static final String PROCESSOR_PORT_OBJECT = "uk.org.taverna.scufl2.api.port.ProcessorPort";
	private static final String PROCESSOR_OBJECT = "uk.org.taverna.scufl2.api.core.Processor";
	private static final String MERGE_OBJECT = "net.sf.taverna.t2.workflowmodel.Merge";
	private static final String NONEXECUTABLE_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/nonExecutable";
	private static final String XML_SPLITTER_OUT_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/xml-splitter/out";
	private static final String XML_SPLITTER_IN_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/xml-splitter/in";
	private static final String LOCALWORKER_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/localworker";
	private static final String WSDL_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/wsdl";
	private static final String CONSTANT_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/constant";
	private static final String SOAPLAB_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/soaplab";
	private static final String RSHELL_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/rshell";
	private static final String NESTED_WORKFLOW = "http://ns.taverna.org.uk/2010/activity/nested-workflow";
	private static final String MOBY_PARSER_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/biomoby/parser";
	private static final String MOBY_OBJECT_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/biomoby/object";
	private static final String MOBY_SERVICE_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/biomoby/service";
	private static final String BIOMART_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/biomart";
	private static final String BEANSHELL_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/beanshell";
	private static final String APICONSUMER_ACTIVITY = "http://ns.taverna.org.uk/2010/activity/apiconsumer";

	// Names of colours used
	private static final String burlywood2 = "#deb887";
	private static final String darkgoldenrod1 = "#ffb90f";
	private static final String darkolivegreen3 = "#a2cd5a";
	private static final String gold = "#ffd700";
	private static final String grey = "#777777";
	private static final String lightcyan2 = "#d1eeee";
	private static final String lightgoldenrodyellow = "#fafad2";
	// light purple non standard
	private static final String lightpurple = "#ab92ea";
	private static final String lightsteelblue = "#b0c4de";
	private static final String mediumorchid2 = "#d15fee";
	private static final String palegreen = "#98fb98";
	private static final String pink = "#ffc0cb";
	private static final String purplish = "#8070ff";
	// ShadedLabel.Orange
	private static final String shadedorange = "#eece8f";
	// ShadedLabel.Green
	private static final String shadedgreen = "#a1c69d";
	// slightly lighter than the real steelblue4
	private static final String steelblue4 = "#648faa";
	private static final String turquoise = "#77aadd";
	private static final String white = "#ffffff";

	private Map<String, String> defaultPropertyMap;
	private Map<Object, Color> cachedColours;

	public ColourManagerImpl(ConfigurationManager configurationManager) {
		super(configurationManager);
		initialiseDefaults();
	}

	@Override
	public String getCategory() {
		return "colour";
	}

	@Override
	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap == null)
			initialiseDefaults();
		return defaultPropertyMap;
	}

	@Override
	public String getDisplayName() {
		return "Colour Management";
	}

	@Override
	public String getFilePrefix() {
		return "ColourManagement";
	}

	/**
	 * Unique identifier for this ColourManager
	 */
	@Override
	public String getUUID() {
		return "a2148420-5967-11dd-ae16-0800200c9a66";
	}

	private void initialiseDefaults() {
		defaultPropertyMap = new HashMap<>();
		cachedColours = new HashMap<>();

		defaultPropertyMap.put(APICONSUMER_ACTIVITY, palegreen);
		defaultPropertyMap.put(BEANSHELL_ACTIVITY, burlywood2);
		defaultPropertyMap.put(BIOMART_ACTIVITY, lightcyan2);
		defaultPropertyMap.put(CONSTANT_ACTIVITY, lightsteelblue);
		defaultPropertyMap.put(LOCALWORKER_ACTIVITY, mediumorchid2);
		defaultPropertyMap.put(MOBY_SERVICE_ACTIVITY, darkgoldenrod1);
		defaultPropertyMap.put(MOBY_OBJECT_ACTIVITY, gold);
		defaultPropertyMap.put(MOBY_PARSER_ACTIVITY, white);
		defaultPropertyMap.put(NESTED_WORKFLOW, pink);
		defaultPropertyMap.put(RSHELL_ACTIVITY, steelblue4);
		defaultPropertyMap.put(SOAPLAB_ACTIVITY, lightgoldenrodyellow);
		defaultPropertyMap.put(WSDL_ACTIVITY, darkolivegreen3);
		defaultPropertyMap.put(XML_SPLITTER_IN_ACTIVITY, lightpurple);
		defaultPropertyMap.put(XML_SPLITTER_OUT_ACTIVITY, lightpurple);

		defaultPropertyMap.put(NONEXECUTABLE_ACTIVITY, grey);

		defaultPropertyMap.put(MERGE_OBJECT, turquoise);
		defaultPropertyMap.put(PROCESSOR_OBJECT, shadedgreen);
		defaultPropertyMap.put(PROCESSOR_PORT_OBJECT, purplish);
		defaultPropertyMap.put(WORKFLOW_PORT_OBJECT, shadedorange);
	}

	@Override
	public Color getPreferredColour(String itemKey) {
		Color colour = cachedColours.get(itemKey);
		if (colour == null) {
			String colourString = (String) getProperty(itemKey);
			colour = colourString == null ? WHITE : decode(colourString);
			cachedColours.put(itemKey, colour);
		}
		return colour;
	}

	@Override
	public void setPreferredColour(String itemKey, Color colour) {
		cachedColours.put(itemKey, colour);
	}

	@Override
	public void restoreDefaults() {
		super.restoreDefaults();
		if (cachedColours == null)
			cachedColours = new HashMap<>();
		else
			cachedColours.clear();
	}
}
