/**
 *
 */
package org.apache.taverna.workbench.report.config.impl;
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

import org.apache.taverna.workbench.report.config.ReportManagerConfiguration;
import org.apache.taverna.workflowmodel.health.RemoteHealthChecker;

/**
 * @author alanrw
 *
 */
public final class ReportManagerConfigurationImpl extends AbstractConfigurable implements ReportManagerConfiguration {

	private static final int DEFAULT_TIMEOUT = 10;

	private Map<String, String> defaultPropertyMap;

	public ReportManagerConfigurationImpl(ConfigurationManager configurationManager) {
		super(configurationManager);
	}

    public String getCategory() {
        return "general";
    }

    public Map<String, String> getDefaultPropertyMap() {

        if (defaultPropertyMap == null) {
            defaultPropertyMap = new HashMap<String, String>();
            defaultPropertyMap.put(TIMEOUT, Integer.toString(DEFAULT_TIMEOUT));
            defaultPropertyMap.put(ON_EDIT, QUICK_CHECK);
            defaultPropertyMap.put(ON_OPEN, QUICK_CHECK);
            defaultPropertyMap.put(BEFORE_RUN, FULL_CHECK);
            defaultPropertyMap.put(QUERY_BEFORE_RUN, ERRORS_OR_WARNINGS);
            defaultPropertyMap.put(REPORT_EXPIRATION, Integer.toString(DEFAULT_REPORT_EXPIRATION));
        }
        return defaultPropertyMap;
    }

    public String getDisplayName() {
        return "Validation report";
    }

    public String getFilePrefix() {
        return "ReportManager";
    }

	public String getUUID() {
		return "F86378E5-0EC4-4DE9-8A55-6098595413DC";
	}

	@Override
	public void applySettings() {
		RemoteHealthChecker.setTimeoutInSeconds(Integer.parseInt(this.getProperty(TIMEOUT)));
	}

	public void setProperty(String key, String value) {
		super.setProperty(key, value);
		if (key.equals(TIMEOUT)) {
			applySettings();
		}
	}

}
