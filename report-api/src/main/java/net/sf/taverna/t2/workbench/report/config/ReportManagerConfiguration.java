/**
 *
 */
package net.sf.taverna.t2.workbench.report.config;

import java.util.HashMap;
import java.util.Map;

import uk.org.taverna.configuration.AbstractConfigurable;
import uk.org.taverna.configuration.ConfigurationManager;

import net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker;

/**
 * @author alanrw
 *
 */
public final class ReportManagerConfiguration extends AbstractConfigurable {

	public static final String TIMEOUT = "TIMEOUT";
	public static final String ON_EDIT = "ON_EDIT";
	public static final String ON_OPEN = "ON_OPEN";
	public static final String BEFORE_RUN = "BEFORE_RUN";

	private static final int DEFAULT_TIMEOUT = 10;

	public static final String NO_CHECK = "NoCheck";
	public static final String QUICK_CHECK = "QuickCheck";
	public static final String FULL_CHECK = "FullCheck";

	public static final String NONE = "Do not care";
	public static final String ERRORS_OR_WARNINGS = "Errors or warnings";
	public static final String ERRORS = "Errors";

	public static final String QUERY_BEFORE_RUN = "QUERY_BEFORE_RUN";

    public static final int DEFAULT_REPORT_EXPIRATION = 0;
    public static final String REPORT_EXPIRATION = "REPORT_EXPIRATION";

    private Map<String, String> defaultPropertyMap;

	public ReportManagerConfiguration(ConfigurationManager configurationManager) {
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
