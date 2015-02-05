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
package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;

import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira
 */
public class PluginPreferencesDialog extends HelpEnabledDialog implements ComponentListener, ActionListener {
  // CONSTANTS

  // components for accessing application's main elements
  private final MainComponent pluginMainComponent;
  private final MyExperimentClient myExperimentClient;
  private final Logger logger;

  // COMPONENTS
  private JTextField tfMyExperimentURL;
  private JComboBox cbDefaultLoggedInTab;
  private JComboBox cbDefaultNotLoggedInTab;
  private JCheckBox cbMyStuffWorkflows;
  private JCheckBox cbMyStuffFiles;
  private JCheckBox cbMyStuffPacks;
  private JButton bSave;
  private JButton bCancel;
  private JClickableLabel jclClearPreviewHistory;
  private JClickableLabel jclClearSearchHistory;
  private JClickableLabel jclClearFavouriteSearches;

  // DATA STORAGE
  private final Component[] pluginTabComponents;
  private final ArrayList<String> alPluginTabComponentNames;

  public PluginPreferencesDialog(JFrame owner, MainComponent component, MyExperimentClient client, Logger logger) {
	super(owner, "Plugin preferences", true);

	// set main variables to ensure access to myExperiment, logger and the parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	// set options of the preview dialog box
	this.addComponentListener(this);
	//this.setIconImage(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("myexp_icon")).getImage());

	// prepare plugin tab names to display in the UI afterwards
	this.alPluginTabComponentNames = new ArrayList<String>();
	this.pluginTabComponents = this.pluginMainComponent.getMainTabs().getComponents();
	for (int i = 0; i < this.pluginTabComponents.length; i++) {
	  alPluginTabComponentNames.add(this.pluginMainComponent.getMainTabs().getTitleAt(i));
	}

	this.initialiseUI();

	// this is not computation-intensive method, so no need to run in a new thread
	this.initialiseData();
  }

  private void initialiseUI() {
	// this constraints instance will be shared among all components in the window
	GridBagConstraints c = new GridBagConstraints();

	// create the myExperiment API address box
	JPanel jpApiLocation = new JPanel();
	jpApiLocation.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " myExperiment Location "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
	jpApiLocation.setLayout(new GridBagLayout());

	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 1.0;
	c.anchor = GridBagConstraints.WEST;
	jpApiLocation.add(new JLabel("Base URL of myExperiment instance to connect to"), c);

	c.gridy = 1;
	c.fill = GridBagConstraints.HORIZONTAL;
	this.tfMyExperimentURL = new JTextField();
	this.tfMyExperimentURL.setToolTipText("<html>Here you can specify the base URL of the myExperiment "
		+ "instance that you wish to connect to.<br>This allows the plugin to connect not only to the "
		+ "<b>main myExperiment website</b> (default value:<br><b>http://www.myexperiment.org</b>) but "
		+ "also to any other myExperiment instance that might<br>exist elsewhere.<br><br>It is recommended "
		+ "that you only change this setting if you are certain in your actions.</html>");
	jpApiLocation.add(this.tfMyExperimentURL, c);

	// create startup tab choice box
	JPanel jpStartupTabChoice = new JPanel();
	jpStartupTabChoice.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Plugin Start-up Settings "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
	jpStartupTabChoice.setLayout(new GridBagLayout());

	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 0;
	c.insets = new Insets(0, 0, 0, 10);
	jpStartupTabChoice.add(new JLabel("Default startup tab for anonymous user"), c);

	c.gridx = 1;
	c.weightx = 1.0;
	c.insets = new Insets(0, 0, 2, 0);
	this.cbDefaultNotLoggedInTab = new JComboBox(this.alPluginTabComponentNames.toArray());
	this.cbDefaultNotLoggedInTab.setToolTipText("<html>This tab will be automatically opened at plugin start up time if you are <b>not</b> logged id to myExperiment.</html>");
	jpStartupTabChoice.add(this.cbDefaultNotLoggedInTab, c);

	c.gridx = 0;
	c.gridy = 1;
	c.weightx = 0;
	c.insets = new Insets(0, 0, 0, 10);
	jpStartupTabChoice.add(new JLabel("Default startup tab after successful auto-login"), c);

	c.gridx = 1;
	c.weightx = 1.0;
	c.insets = new Insets(2, 0, 0, 0);
	this.cbDefaultLoggedInTab = new JComboBox(this.alPluginTabComponentNames.toArray());
	this.cbDefaultLoggedInTab.setToolTipText("<html>This tab will be automatically opened at plugin start up time if you have chosen to use <b>auto logging in</b> to myExperiment.</html>");
	jpStartupTabChoice.add(this.cbDefaultLoggedInTab, c);

	// create 'my stuff' tab preference box
	JPanel jpMyStuffPrefs = new JPanel();
	jpMyStuffPrefs.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " 'My Stuff' Tab Settings "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
	jpMyStuffPrefs.setLayout(new GridBagLayout());

	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 0;
	c.insets = new Insets(0, 0, 0, 0);
	jpMyStuffPrefs.add(new JLabel("Sections to show in this tab:"), c);

	c.gridx = 1;
	c.gridy = 0;
	c.weightx = 1.0;
	c.insets = new Insets(0, 10, 0, 0);
	this.cbMyStuffWorkflows = new JCheckBox("My Workflows");
	jpMyStuffPrefs.add(this.cbMyStuffWorkflows, c);

	c.gridy = 1;
	this.cbMyStuffFiles = new JCheckBox("My Files");
	jpMyStuffPrefs.add(this.cbMyStuffFiles, c);

	c.gridy = 2;
	this.cbMyStuffPacks = new JCheckBox("My Packs");
	jpMyStuffPrefs.add(this.cbMyStuffPacks, c);

	// create privacy settings box
	JPanel jpPrivacySettings = new JPanel();
	jpPrivacySettings.setLayout(new BoxLayout(jpPrivacySettings, BoxLayout.Y_AXIS));
	jpPrivacySettings.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Privacy Settings "), BorderFactory.createEmptyBorder(0, 7, 5, 5)));

	this.jclClearPreviewHistory = new JClickableLabel("Clear browsing history", "clear_preview_history", this);
	jpPrivacySettings.add(this.jclClearPreviewHistory);

	this.jclClearSearchHistory = new JClickableLabel("Clear search history", "clear_search_history", this);
	this.jclClearSearchHistory.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
	jpPrivacySettings.add(this.jclClearSearchHistory);

	this.jclClearFavouriteSearches = new JClickableLabel("Clear favourite searches", "clear_favourite_searches", this);
	this.jclClearFavouriteSearches.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
	jpPrivacySettings.add(this.jclClearFavouriteSearches);

	// create button panel
	this.bSave = new JButton("Save");
	this.bSave.addActionListener(this);

	this.bCancel = new JButton("Cancel");
	this.bCancel.addActionListener(this);

	JPanel jpButtons = new JPanel();
	jpButtons.setLayout(new GridBagLayout());

	c.gridx = 0;
	c.gridy = 0;
	c.weightx = 0;
	c.insets = new Insets(0, 0, 0, 2);
	jpButtons.add(bSave, c);

	c.gridx = 1;
	c.insets = new Insets(0, 2, 0, 0);
	jpButtons.add(bCancel, c);

	// PUT EVERYTHING TOGETHER
	this.setTitle("myExperiment Plugin Preferences");
	BorderLayout layout = new BorderLayout();
	JPanel jpEverything = new JPanel();
	GridBagLayout jpEverythingLayout = new GridBagLayout();
	jpEverything.setLayout(jpEverythingLayout);
	this.getContentPane().setLayout(layout);

	GridBagConstraints gbConstraints = new GridBagConstraints();
	gbConstraints.fill = GridBagConstraints.BOTH;
	gbConstraints.weightx = 1;
	gbConstraints.gridx = 0;

	gbConstraints.gridy = 0;
	jpEverything.add(jpApiLocation, gbConstraints);

	gbConstraints.gridy = 1;
	jpEverything.add(jpStartupTabChoice, gbConstraints);

	gbConstraints.gridy = 2;
	jpEverything.add(jpMyStuffPrefs, gbConstraints);

	gbConstraints.gridy = 3;
	jpEverything.add(jpPrivacySettings, gbConstraints);

	gbConstraints.gridy = 4;
	jpEverything.add(jpButtons, gbConstraints);

	this.add(jpEverything);
	this.setResizable(false);

	// pack() sets preferred size of the dialog box;
	// after this, can set the minimum size to that value too
	this.pack();
	this.setMinimumSize(this.getPreferredSize());
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

  // *** Callbacks for ComponentListener interface ***

  public void componentShown(ComponentEvent e) {
	// every time the settings window is shown, make sure that the dialog box appears
	// centered horizontally and vertically relatively to the main component
	Util.centerComponentWithinAnother(this.pluginMainComponent, this);

	// also, need to make sure that correct settings get shown
	// (e.g. especially relevant when this window was last closed with 'cancel',
	//  but some options were changed prior to that)
	this.initialiseData();
  }

  public void componentHidden(ComponentEvent e) {
	// do nothing
  }

  public void componentResized(ComponentEvent e) {
	// do nothing
  }

  public void componentMoved(ComponentEvent e) {
	// do nothing
  }

  // *** Callback for ActionListener interface ***

  public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.bSave)) {
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

	  // NB! changed myExperiment location will not take action until the next application restart
	  if (!strNewMyExperimentURL.equals(myExperimentClient.getBaseURL())) {
		// turn off auto-login
		myExperimentClient.getSettings().put(MyExperimentClient.INI_AUTO_LOGIN, new Boolean(false).toString());

		javax.swing.JOptionPane.showMessageDialog(null, "You have selected a new Base URL for myExperiment. "
			+ "Your new setting has been saved,\nbut will not take effect until you restart Taverna.\n\n"
			+ "Auto-login feature has been switched off for you to check the login details at the next launch.", "myExperiment Plugin - Info", JOptionPane.INFORMATION_MESSAGE);
	  }

	  // all values should be present - store these into Properties object
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_BASE_URL, strNewMyExperimentURL);
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_DEFAULT_ANONYMOUS_TAB, new Integer(cbDefaultNotLoggedInTab.getSelectedIndex()).toString());
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_DEFAULT_LOGGED_IN_TAB, new Integer(cbDefaultLoggedInTab.getSelectedIndex()).toString());
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_MY_STUFF_WORKFLOWS, new Boolean(cbMyStuffWorkflows.isSelected()).toString());
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_MY_STUFF_FILES, new Boolean(cbMyStuffFiles.isSelected()).toString());
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_MY_STUFF_PACKS, new Boolean(cbMyStuffPacks.isSelected()).toString());

	  // close the window eventually
	  setVisible(false);
	} else if (e.getSource().equals(this.bCancel)) {
	  // simply close the preferences window
	  setVisible(false);
	} else if (e.getSource().equals(this.jclClearPreviewHistory)) {
	  // request user confirmation and clear browsing history (preview history)
	  if (JOptionPane.showConfirmDialog(null, "This will delete the browsing history - the lists of previously previewed,\n"
		  + "downloaded, opened and commented on items will be emptied.\n\nDo you want to proceed?", "myExperiment Plugin - Confirmation Required", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
		pluginMainComponent.getPreviewBrowser().clearPreviewHistory();
		pluginMainComponent.getHistoryBrowser().clearDownloadedItemsHistory();
		pluginMainComponent.getHistoryBrowser().clearOpenedItemsHistory();
		pluginMainComponent.getHistoryBrowser().clearCommentedOnItemsHistory();
		pluginMainComponent.getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.PREVIEWED_ITEMS_HISTORY);
		pluginMainComponent.getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.DOWNLOADED_ITEMS_HISTORY);
		pluginMainComponent.getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.OPENED_ITEMS_HISTORY);
		pluginMainComponent.getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.COMMENTED_ON_ITEMS_HISTORY);
	  }
	} else if (e.getSource().equals(this.jclClearSearchHistory)) {
	  // request user confirmation and clear search history (tag search history + query search history)
	  if (JOptionPane.showConfirmDialog(null, "This will delete both query and tag search history.\nDo you want to proceed?", "myExperiment Plugin - Confirmation Required", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
		pluginMainComponent.getSearchTab().getSearchHistory().clear();
		pluginMainComponent.getTagBrowserTab().getTagSearchHistory().clear();
		pluginMainComponent.getSearchTab().updateSearchHistory();
		pluginMainComponent.getHistoryBrowser().refreshSearchHistory();
		pluginMainComponent.getHistoryBrowser().refreshTagSearchHistory();
	  }
	} else if (e.getSource().equals(this.jclClearFavouriteSearches)) {
	  // request user confirmation and clear favourite searches
	  if (JOptionPane.showConfirmDialog(null, "This will delete all your favourite search settings.\nDo you want to proceed?", "myExperiment Plugin - Confirmation Required", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
		pluginMainComponent.getSearchTab().getSearchFavouritesList().clear();
		pluginMainComponent.getSearchTab().updateFavouriteSearches();
	  }
	}
  }
}
