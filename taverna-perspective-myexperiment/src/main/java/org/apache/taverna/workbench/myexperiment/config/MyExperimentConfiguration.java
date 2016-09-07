package org.apache.taverna.workbench.myexperiment.config;
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
import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationManager;


/**
 * @author Emmanuel Tagarira, Alan Williams
 */
public class MyExperimentConfiguration extends AbstractConfigurable {

  //private static Logger logger = Logger.getLogger(MyExperimentConfiguration.class);

  public MyExperimentConfiguration(ConfigurationManager configurationManager) {
		super(configurationManager);
	}

private Map<String, String> defaultPropertyMap;

  public String getCategory() {
	return "general";
  }

  public Map<String, String> getDefaultPropertyMap() {
	if (defaultPropertyMap == null) {
	  defaultPropertyMap = new HashMap<String, String>();
	}
	return defaultPropertyMap;
  }

  public String getDisplayName() {
	return "myExperiment";
  }

  public String getFilePrefix() {
		return "myExperiment";
	  }

  public String getUUID() {
	return "d25867g1-6078-22ee-bf27-1911311d0b77";
  }
}
