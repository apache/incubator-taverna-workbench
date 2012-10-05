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
 *
 */
public class ColourManagerImpl extends AbstractConfigurable implements ColourManager {

	private Map<String, String> defaultPropertyMap;
	private Map<Object,Color> cachedColours;

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
		if (defaultPropertyMap==null) initialiseDefaults();
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
		defaultPropertyMap=new HashMap<String, String>();
		cachedColours=new HashMap<Object, Color>();
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/apiconsumer", "#98fb98");//palegreen
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/beanshell", "#deb887");//burlywood2
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/biomart", "#d1eeee");//lightcyan2
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/biomoby/service", "#ffb90f");//darkgoldenrod1
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/biomoby/object", "#ffd700");//gold
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/biomoby/parser", "#ffffff");//white
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/nested-workflow", "#ffc0cb");//pink
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/rshell", "#648faa");//slightly lighter than steelblue4
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/soaplab", "#fafad2");//lightgoldenrodyellow
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/constant", "#b0c4de");//lightsteelblue
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/wsdl", "#a2cd5a");//darkolivegreen3
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/localworker", "#d15fee"); //mediumorchid2
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/xml-splitter/in", "#ab92ea"); //light purple non standard
		defaultPropertyMap.put("http://ns.taverna.org.uk/2010/activity/xml-splitter/out", "#ab92ea"); //light purple non-standard
		defaultPropertyMap.put("net.sf.taverna.t2.workflowmodel.Merge", "#77aadd");
		defaultPropertyMap.put("net.sf.taverna.t2.workflowmodel.Processor", "#a1c69d"); // ShadedLabel.Green
		defaultPropertyMap.put("net.sf.taverna.t2.workflowmodel.ProcessorPort", "#8070ff"); // purplish
		defaultPropertyMap.put("net.sf.taverna.t2.workflowmodel.DataflowPort", "#eece8f"); // ShadedLabel.Orange
		defaultPropertyMap.put("net.sf.taverna.t2.workflowmodel.processor.activity.NonExecutableActivity", "#777777");

	}

	@Override
	public Color getPreferredColour(String itemKey) {
		Color colour = cachedColours.get(itemKey);
		if (colour == null) {
			String colourString=(String)getProperty(itemKey);
			colour = colourString==null ? Color.WHITE : Color.decode(colourString);
			cachedColours.put(itemKey,colour);
		}
		return colour;
	}

	@Override
	public void setPreferredColour(String itemKey, Color colour) {
		cachedColours.put(itemKey,colour);
	}

	@Override
	public void restoreDefaults() {
		super.restoreDefaults();
		if (cachedColours==null) {
			cachedColours=new HashMap<Object, Color>();
		}
		else {
			cachedColours.clear();
		}
	}

}
