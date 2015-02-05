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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.ui.perspectives.myexperiment.MainComponent;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.workbench.helper.Helper;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

// import org.apache.log4j.Logger;

/**
 * @author Emmanuel Tagarira, Sergejs Aleksejevs, Alan Williams
 */
@SuppressWarnings("serial")
public class MyExperimentConfigurationPanel extends JPanel implements ActionListener {
  // CONSTANTS

  // components for accessing application's main elements
  private final MainComponent pluginMainComponent = MainComponent.MAIN_COMPONENT;
  private final MyExperimentClient myExperimentClient = MainComponent.MY_EXPERIMENT_CLIENT;
  //private final Logger logger = MainComponent.LOGGER;

  // COMPONENTS
  private JTextField tfMyExperimentURL;
  private JComboBox cbDefaultLoggedInTab;
  private JComboBox cbDefaultNotLoggedInTab;
  private JCheckBox cbMyStuffWorkflows;
  private JCheckBox cbMyStuffFiles;
  private JCheckBox cbMyStuffPacks;
  private JButton bApply;
  private JButton bReset;
  private JButton bHelp;

  // DATA STORAGE
  private final Component[] pluginTabComponents;
  private final ArrayList<String> alPluginTabComponentNames;

  public MyExperimentConfigurationPanel() {
	super();

	// prepare plugin tab names to display in the UI afterwards
	this.alPluginTabComponentNames = new ArrayList<String>();
	this.pluginTabComponents = pluginMainComponent.getMainTabs().getComponents();
	for (int i = 0; i < this.pluginTabComponents.length; i++)
	  alPluginTabComponentNames.add(pluginMainComponent.getMainTabs().getTitleAt(i));

	this.initialiseUI();
	this.initialiseData();
  }

  private void initialiseUI() {
	// this constraints instance will be shared among all components in the window
	GridBagConstraints c = new GridBagConstraints();

	// create the myExperiment API address box
	JPanel jpApiLocation = new JPanel();
	jpApiLocation.setLayout(new GridBagLayout());

	Insets insLabel = new Insets(0, 0, 0, 10);
	Insets insParam = new Insets(0, 3, 5, 3);

	// Title describing what kind of settings we are configuring here
	JTextArea descriptionText = new JTextArea("Configure the myExperiment integration functionality");
	descriptionText.setLineWrap(true);
	descriptionText.setWrapStyleWord(true);
	descriptionText.setEditable(false);
	descriptionText.setFocusable(false);
	descriptionText.setBorder(new EmptyBorder(10, 10, 10, 10));
	c.anchor = GridBagConstraints.WEST;
	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 1.0;
	c.weighty = 0.0;
	c.fill = GridBagConstraints.HORIZONTAL;
	jpApiLocation.add(descriptionText, c);

	c.gridx = 0;
	c.gridy = 1;
	c.weightx = 1.0;
	//c.insets = insLabel;
	c.anchor = GridBagConstraints.WEST;
	c.insets = new Insets(10, 0, 0, 10);
	jpApiLocation.add(new JLabel("Base URL of myExperiment instance to connect to"), c);

	c.gridy = 2;
	//c.insets = insParam;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.insets = new Insets(0, 0, 0, 0);
	this.tfMyExperimentURL = new JTextField();
	this.tfMyExperimentURL.setToolTipText("<html>Here you can specify the base URL of the myExperiment "
		+ "instance that you wish to connect to.<br>This allows the plugin to connect not only to the "
		+ "<b>main myExperiment website</b> (default value:<br><b>http://www.myexperiment.org</b>) but "
		+ "also to any other myExperiment instance that might<br>exist elsewhere.<br><br>It is recommended "
		+ "that you only change this setting if you are certain in your actions.</html>");
	jpApiLocation.add(this.tfMyExperimentURL, c);

	// create startup tab choice box
	JPanel jpStartupTabChoice = new JPanel();
	jpStartupTabChoice.setLayout(new GridBagLayout());

	c.gridy = 0;
	c.insets = insLabel;
	jpStartupTabChoice.add(new JLabel("Default startup tab for anonymous user"), c);

	c.gridx = 1;
	c.insets = insParam;
	this.cbDefaultNotLoggedInTab = new JComboBox(this.alPluginTabComponentNames.toArray());
	this.cbDefaultNotLoggedInTab.setToolTipText("<html>This tab will be automatically opened at plugin start up time if you are <b>not</b> logged id to myExperiment.</html>");
	jpStartupTabChoice.add(this.cbDefaultNotLoggedInTab, c);

	c.gridy = 1;
	c.gridx = 0;
	c.insets = insLabel;
	jpStartupTabChoice.add(new JLabel("Default startup tab after successful auto-login"), c);

	c.gridx = 1;
	c.insets = insParam;
	this.cbDefaultLoggedInTab = new JComboBox(this.alPluginTabComponentNames.toArray());
	this.cbDefaultLoggedInTab.setToolTipText("<html>This tab will be automatically opened at plugin start up time if you have chosen to use <b>auto logging in</b> to myExperiment.</html>");
	jpStartupTabChoice.add(this.cbDefaultLoggedInTab, c);

	// create 'my stuff' tab preference box
	JPanel jpMyStuffPrefs = new JPanel();
	jpMyStuffPrefs.setLayout(new GridBagLayout());

	c.gridx = 0;
	c.gridy = 0;
	c.insets = insLabel;
	jpMyStuffPrefs.add(new JLabel("Sections to show in this tab:"), c);

	c.gridx = 1;
	c.gridy = 0;
	c.weightx = 1.0;
	c.insets = new Insets(insParam.top, 100, insParam.bottom / 3, insParam.right);
	this.cbMyStuffWorkflows = new JCheckBox("My Workflows");
	jpMyStuffPrefs.add(this.cbMyStuffWorkflows, c);

	c.gridy = 1;
	this.cbMyStuffFiles = new JCheckBox("My Files");
	jpMyStuffPrefs.add(this.cbMyStuffFiles, c);

	c.gridy = 2;
	this.cbMyStuffPacks = new JCheckBox("My Packs");
	jpMyStuffPrefs.add(this.cbMyStuffPacks, c);

	// create button panel
	this.bApply = new JButton("Apply");
	this.bApply.addActionListener(this);

	this.bReset = new JButton("Reset");
	this.bReset.addActionListener(this);

	this.bHelp = new JButton("Help");
	this.bHelp.addActionListener(this);

	JPanel jpButtons = new JPanel();
	jpButtons.add(bHelp, c);
	jpButtons.add(bReset, c);
	jpButtons.add(bApply, c);

	// PUT EVERYTHING TOGETHER
	JPanel jpEverything = new JPanel();
	GridBagLayout jpEverythingLayout = new GridBagLayout();
	jpEverything.setLayout(jpEverythingLayout);

	GridBagConstraints gbConstraints = new GridBagConstraints();
	gbConstraints.fill = GridBagConstraints.BOTH;
	c.anchor = GridBagConstraints.NORTHWEST;

	gbConstraints.weightx = 1;
	gbConstraints.gridx = 0;

	gbConstraints.gridy = 0;
	jpEverything.add(jpApiLocation, gbConstraints);

	gbConstraints.gridy++;
	jpEverything.add(jpStartupTabChoice, gbConstraints);

	gbConstraints.gridy++;
	jpEverything.add(jpMyStuffPrefs, gbConstraints);

	gbConstraints.gridy++;
	c.insets = new Insets(10, 0, 0, 0);
	jpEverything.add(jpButtons, gbConstraints);

	BorderLayout layout = new BorderLayout();
	this.setLayout(layout);
	this.add(jpEverything, BorderLayout.NORTH);

	if (MyExperimentClient.baseChangedSinceLastStart) {
	  JPanel jpInfo = new JPanel();
	  jpInfo.setLayout(new BoxLayout(jpInfo, BoxLayout.Y_AXIS));
	  String info = "<html>Your myExperiment base url has been modified since Taverna was started;<br>"
		  + "this change will not take effect until you restart Taverna.</html>";
	  jpInfo.add(new JLabel(info, WorkbenchIcons.leafIcon, SwingConstants.LEFT));
	  this.add(jpInfo, BorderLayout.SOUTH);
	}
  }

  private void initialiseData() {
	// myExperiment Base URL
	this.tfMyExperimentURL.setText(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_BASE_URL));

	// default tabs
	this.cbDefaultNotLoggedInTab.setSelectedIndex(Integer.parseInt(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_DEFAULT_ANONYMOUS_TAB)));
	this.cbDefaultLoggedInTab.setSelectedIndex(Integer.parseInt(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_DEFAULT_LOGGED_IN_TAB)));

	// components of "My Stuff" tab
	this.cbMyStuffWorkflows.setSelected(Boolean.parseBoolean(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_MY_STUFF_WORKFLOWS)));
	this.cbMyStuffFiles.setSelected(Boolean.parseBoolean(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_MY_STUFF_FILES)));
	this.cbMyStuffPacks.setSelected(Boolean.parseBoolean(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_MY_STUFF_PACKS)));
  }

  // *** Callback for ActionListener interface ***

  public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.bApply)) {
	  // check if myExperiment address is present
	  String strNewMyExperimentURL = this.tfMyExperimentURL.getText().trim();
	  if (strNewMyExperimentURL.length() == 0) {
		javax.swing.JOptionPane.showMessageDialog(null, "Please specify a base URL of myExperiment instance that you wish to connect to", "Error", JOptionPane.WARNING_MESSAGE);
		this.tfMyExperimentURL.requestFocusInWindow();
		return;
	  }

	  // check if at least one of the checkboxes (for sections in 'My Stuff' tab) is selected
	  if (!(this.cbMyStuffWorkflows.isSelected()
		  || this.cbMyStuffFiles.isSelected() || this.cbMyStuffPacks.isSelected())) {
		javax.swing.JOptionPane.showMessageDialog(null, "Please choose at least one section to display in 'My Stuff' tab", "Error", JOptionPane.WARNING_MESSAGE);
		this.cbMyStuffWorkflows.requestFocusInWindow();
		return;
	  }

	  // all values should be present - store these into Properties object
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_BASE_URL, strNewMyExperimentURL);
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_DEFAULT_ANONYMOUS_TAB, new Integer(cbDefaultNotLoggedInTab.getSelectedIndex()).toString());
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_DEFAULT_LOGGED_IN_TAB, new Integer(cbDefaultLoggedInTab.getSelectedIndex()).toString());
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_MY_STUFF_WORKFLOWS, new Boolean(cbMyStuffWorkflows.isSelected()).toString());
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_MY_STUFF_FILES, new Boolean(cbMyStuffFiles.isSelected()).toString());
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_MY_STUFF_PACKS, new Boolean(cbMyStuffPacks.isSelected()).toString());

	  // NB! changed myExperiment location will not take action until the next application restart
	  if (MyExperimentClient.baseChangedSinceLastStart
		  || !strNewMyExperimentURL.equals(myExperimentClient.getBaseURL())) {
		// turn off auto-login
		myExperimentClient.getSettings().put(MyExperimentClient.INI_AUTO_LOGIN, new Boolean(false).toString());

		javax.swing.JOptionPane.showMessageDialog(null, "You have selected a new Base URL for myExperiment.\n"
			+ "Your new setting has been saved, but will not take\n"
			+ "effect until you restart Taverna.\n\n"
			+ "The auto-login feature has been disabled for you to\n"
			+ "check the login details at the next launch.", "myExperiment Plugin - Info", JOptionPane.INFORMATION_MESSAGE);

		MyExperimentClient.baseChangedSinceLastStart = true;
	  }

	  myExperimentClient.storeHistoryAndSettings();
	  //	  pluginMainComponent = new MainComponent();
	} else if (e.getSource().equals(this.bHelp)) {
	  Helper.showHelp(this);
	} else if (e.getSource().equals(this.bReset)) {
	  initialiseData();
	}
  }
}
