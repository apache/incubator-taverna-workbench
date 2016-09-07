/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.myexperiment.config;

import java.awt.Dimension;

import javax.swing.JFrame;

/**
 * This is a class to get a visual on what the preferences will look like when
 * integrated into the main taverna preferences.
 * 
 * @author Emmanuel Tagarira
 */
public class TestJFrameForPreferencesLocalLaunch {

  public static void main(String[] args) {
	JFrame frame = new JFrame("myExperiment Preferences Test");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	frame.setMinimumSize(new Dimension(500, 300));
	frame.setLocation(300, 150);
	frame.getContentPane().add(new MyExperimentConfigurationPanel());

	frame.pack();
	frame.setVisible(true);
  }
}
