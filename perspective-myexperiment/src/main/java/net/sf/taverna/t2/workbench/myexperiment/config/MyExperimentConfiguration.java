/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.myexperiment.config;

import java.util.HashMap;
import java.util.Map;
//import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;
import net.sf.taverna.t2.workbench.configuration.Configurable;

/**
 * @author Emmanuel Tagarira, Alan Williams
 */
public class MyExperimentConfiguration extends AbstractConfigurable {
  private static class Singleton {
	private static MyExperimentConfiguration instance = new MyExperimentConfiguration();
  }

  //private static Logger logger = Logger.getLogger(MyExperimentConfiguration.class);

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

  public static Configurable getInstance() {
	return Singleton.instance;
  }
}
