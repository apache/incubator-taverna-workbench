/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.report.config.ui;

import javax.swing.JPanel;

import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationUIFactory;

import org.apache.taverna.workbench.report.config.ReportManagerConfiguration;

/**
 * ConfigurationFactory for the ReportManagerConfiguration.
 *
 * @author Alan R Williams
 */
public class ReportManagerConfigurationUIFactory  implements ConfigurationUIFactory {

	private ReportManagerConfiguration reportManagerConfiguration;

	public boolean canHandle(String uuid) {
		return uuid.equals(getConfigurable().getUUID());
	}

	public JPanel getConfigurationPanel() {
		return new ReportManagerConfigurationPanel(reportManagerConfiguration);
	}

	public Configurable getConfigurable() {
		return reportManagerConfiguration;
	}

	public void setReportManagerConfiguration(ReportManagerConfiguration reportManagerConfiguration) {
		this.reportManagerConfiguration = reportManagerConfiguration;
	}

}
