/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.myexperiment.config;

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
