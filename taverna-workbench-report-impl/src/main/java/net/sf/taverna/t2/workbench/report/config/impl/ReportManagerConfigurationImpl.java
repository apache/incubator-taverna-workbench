/**
 *
 */
package net.sf.taverna.t2.workbench.report.config.impl;

import java.util.HashMap;
import java.util.Map;

import uk.org.taverna.configuration.AbstractConfigurable;
import uk.org.taverna.configuration.ConfigurationManager;

import net.sf.taverna.t2.workbench.report.config.ReportManagerConfiguration;
import net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker;

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
