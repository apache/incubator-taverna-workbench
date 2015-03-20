package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config;

import javax.swing.JPanel;

import uk.org.taverna.configuration.Configurable;
import uk.org.taverna.configuration.ConfigurationUIFactory;

/**
 *
 * @author Sergejs Aleksejevs
 */
public class BioCataloguePluginConfigurationUIFactory implements ConfigurationUIFactory
{

  public boolean canHandle(String uuid) {
    return uuid.equals(getConfigurable().getUUID());
  }

  public Configurable getConfigurable() {
    return BioCataloguePluginConfiguration.getInstance();
  }

  public JPanel getConfigurationPanel() {
    return new BioCataloguePluginConfigurationPanel();
  }

}
