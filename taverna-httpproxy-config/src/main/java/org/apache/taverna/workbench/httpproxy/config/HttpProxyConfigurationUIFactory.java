/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.httpproxy.config;

import javax.swing.JPanel;

import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationUIFactory;
import org.apache.taverna.configuration.proxy.HttpProxyConfiguration;

/**
 * A Factory to create a HttpProxyConfiguration
 *
 * @author alanrw
 * @author David Withers
 */
public class HttpProxyConfigurationUIFactory implements ConfigurationUIFactory {
	private HttpProxyConfiguration httpProxyConfiguration;

	@Override
	public boolean canHandle(String uuid) {
		return uuid.equals(getConfigurable().getUUID());
	}

	@Override
	public JPanel getConfigurationPanel() {
		return new HttpProxyConfigurationPanel(httpProxyConfiguration);
	}

	@Override
	public Configurable getConfigurable() {
		return httpProxyConfiguration;
	}

	public void setHttpProxyConfiguration(HttpProxyConfiguration httpProxyConfiguration) {
		this.httpProxyConfiguration = httpProxyConfiguration;
	}
}
