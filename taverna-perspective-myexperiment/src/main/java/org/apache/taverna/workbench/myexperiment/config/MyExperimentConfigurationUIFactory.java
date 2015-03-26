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

import uk.org.taverna.configuration.Configurable;
import uk.org.taverna.configuration.ConfigurationUIFactory;

import org.apache.taverna.ui.perspectives.myexperiment.MainComponent;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;

/**
 * @author Emmanuel Tagarira, Alan Williams
 */
public class MyExperimentConfigurationUIFactory implements ConfigurationUIFactory {

  private EditManager editManager;
private FileManager fileManager;

public boolean canHandle(String uuid) {
	return uuid.equals(getConfigurable().getUUID());
  }

  public JPanel getConfigurationPanel() {
	if (MainComponent.MAIN_COMPONENT == null)
	  MainComponent.MAIN_COMPONENT = new MainComponent(editManager, fileManager);
	return new MyExperimentConfigurationPanel();
  }

  public Configurable getConfigurable() {
	return MyExperimentConfiguration.getInstance();
  }

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

}
