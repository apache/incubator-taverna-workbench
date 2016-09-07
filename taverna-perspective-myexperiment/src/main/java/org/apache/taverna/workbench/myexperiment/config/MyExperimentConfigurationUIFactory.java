/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.myexperiment.config;

import javax.swing.JPanel;

import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationManager;
import org.apache.taverna.configuration.ConfigurationUIFactory;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;

/**
 * @author Emmanuel Tagarira, Alan Williams
 */
public class MyExperimentConfigurationUIFactory implements ConfigurationUIFactory {

	private ConfigurationManager configurationManager;

	public boolean canHandle(String uuid) {
		return uuid.equals(getConfigurable().getUUID());
	}

	public JPanel getConfigurationPanel() {
		// FIXME: This is insane.. why would we initialize the UI from here?
		// if (MainComponent.MAIN_COMPONENT == null)
		// MainComponent.MAIN_COMPONENT = new MainComponent(editManager, fileManager);
		return new MyExperimentConfigurationPanel();
	}

	public Configurable getConfigurable() {
		return new MyExperimentConfiguration(configurationManager);
	}

	public void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

}
