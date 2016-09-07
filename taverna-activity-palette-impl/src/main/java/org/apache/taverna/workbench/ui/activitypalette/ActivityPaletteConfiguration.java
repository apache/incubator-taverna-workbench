package org.apache.taverna.workbench.ui.activitypalette;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.HashMap;
import java.util.Map;
import org.apache.taverna.configuration.AbstractConfigurable;
import org.apache.taverna.configuration.ConfigurationManager;

public class ActivityPaletteConfiguration extends AbstractConfigurable {
	private Map<String,String> defaultPropertyMap;

	public ActivityPaletteConfiguration(ConfigurationManager configurationManager) {
		super(configurationManager);
	}

	@Override
	public String getCategory() {
		return "Services";
	}

	@Override
	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap == null) {
			defaultPropertyMap = new HashMap<>();

			// //wsdl
			//defaultPropertyMap.put("taverna.defaultwsdl", "http://www.ebi.ac.uk/xembl/XEMBL.wsdl,"+
			//                    "http://soap.genome.jp/KEGG.wsdl,"+
			//                    "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils.wsdl,"+
			//                    "http://soap.bind.ca/wsdl/bind.wsdl,"+
			//                    "http://www.ebi.ac.uk/ws/services/urn:Dbfetch?wsdl");

			// //soaplab
			//defaultPropertyMap.put("taverna.defaultsoaplab", "http://www.ebi.ac.uk/soaplab/services/");

			// //biomart
			//defaultPropertyMap.put("taverna.defaultmartregistry","http://www.biomart.org/biomart");

			//add property names
			//defaultPropertyMap.put("name.taverna.defaultwsdl", "WSDL");
			//defaultPropertyMap.put("name.taverna.defaultsoaplab","Soaplab");
			//defaultPropertyMap.put("name.taverna.defaultmartregistry", "Biomart");
		}
		return defaultPropertyMap;
	}

	@Override
	public String getDisplayName() {
		return "Activity Palette";
	}

	@Override
	public String getFilePrefix() {
		return "ActivityPalette";
	}

	@Override
	public String getUUID() {
		return "ad9f3a60-5967-11dd-ae16-0800200c9a66";
	}
}
