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
