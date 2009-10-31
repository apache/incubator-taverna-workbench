/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
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

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.taverna.t2.ui.perspectives.myexperiment.MainComponent;
import net.sf.taverna.t2.ui.perspectives.myexperiment.MainComponentFactory;

/**
 * @author Emmanuel Tagarira
 */
public class TestJFrameForPreferencesLocalLaunch {

  public static void main(String[] args) {
	JFrame frame = new JFrame("myExperiment Preferences Test");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	frame.setMinimumSize(new Dimension(500, 300));
	frame.setLocation(300, 150);
	MainComponentFactory factory = new net.sf.taverna.t2.ui.perspectives.myexperiment.MainComponentFactory();
	MainComponent mainPluginComponent = (MainComponent) factory.getComponent();
	frame.getContentPane().add((JPanel) mainPluginComponent.getMyExperimentConfigurationPanel());

	frame.pack();
	frame.setVisible(true);
  }

}
