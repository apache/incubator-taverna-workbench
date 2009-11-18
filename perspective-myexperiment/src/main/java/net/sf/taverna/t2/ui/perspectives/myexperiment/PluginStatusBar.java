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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira
 */
public class PluginStatusBar extends JPanel implements ActionListener {
  // CONSTANTS
  private static final String STATUS_MESSAGE_READY = "Ready";

  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;

  // all components that represent the status
  private JLabel lSpinnerIcon;
  private JLabel lStatusMsg;
  private JLabel lCurrentUser;
  //  private JButton bPreferences;

  // collections to keep the statuses for different tabs
  ArrayList<String> alTabClassNames;
  ArrayList<String> alTabStatuses;

  // spinner icons
  ImageIcon iconSpinner;
  ImageIcon iconSpinnerStopped;

  public PluginStatusBar(MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the
	// parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	// prepare status collections for different tabs
	alTabClassNames = new ArrayList<String>();
	alTabStatuses = new ArrayList<String>();

	// load icons
	this.iconSpinner = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner"));
	this.iconSpinnerStopped = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner_stopped"));

	// prepare main panel for the status bar
	this.setLayout(new BorderLayout());
	this.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 1)); // this will
	// add a bit
	// more spacing
	// on the left
	// - before the
	// status
	// message

	// prepare status labels
	this.lSpinnerIcon = new JLabel("", iconSpinnerStopped, SwingConstants.LEFT);
	this.lStatusMsg = new JLabel("Ready");
	this.lCurrentUser = new JLabel("Please log in to access your profile", SwingConstants.CENTER);

	// 'Plugin Preferences' button
	//	this.bPreferences = new JButton("Plugin Preferences", WorkbenchIcons.configureIcon);
	//	this.bPreferences.addActionListener(this);

	// put everything together
	JPanel pWestStatusBarSection = new JPanel();
	pWestStatusBarSection.add(lSpinnerIcon);
	pWestStatusBarSection.add(lStatusMsg);

	this.add(pWestStatusBarSection, BorderLayout.WEST);
	this.add(this.lCurrentUser, BorderLayout.EAST);
	//	this.add(this.bPreferences, BorderLayout.EAST);
  }

  // updates the current user name in the middle of the status bar
  public void setCurrentUser(String strUsername) {
	// if "null" or "" is submitted as a parameter, the status will be set to
	// "Ready"
	if (strUsername == null || strUsername.length() == 0)
	  this.lCurrentUser.setText("Please log in to access your profile");
	else
	  this.lCurrentUser.setText("<html>Logged in as <b>" + strUsername
		  + "</b></html>");
  }

  // sets the status message to the one that is relevant to the current tab
  public void displayStatus(String strTabClassName) {
	int iTabIdx = -1;
	String strBaseClassName = Util.getBaseClassName(strTabClassName);

	if ((iTabIdx = alTabClassNames.indexOf(strBaseClassName)) != -1) {
	  // tab found - show its status message
	  String strCurStatus = alTabStatuses.get(iTabIdx);
	  this.lStatusMsg.setText(strCurStatus);
	  startSpinner(!strCurStatus.equals(PluginStatusBar.STATUS_MESSAGE_READY));
	} else {
	  // tab not found - assume no actions are happening
	  // (this will create the 'ready' status for the current tab,
	  // then return to display it)
	  setStatus(strBaseClassName, null);
	}
  }

  // sets the status message for a particular tab;
  // if this tab is currently active, the status will get displayed immediately
  // (alternatively it will be displayed at the time when the tab becomes
  // active)
  public void setStatus(String strTabClassName, String strStatus) {
	// PREPROCESSING - if "null" or "" is submitted as a parameter, the status
	// will be set to "Ready"
	if (strStatus == null || strStatus.length() == 0)
	  strStatus = PluginStatusBar.STATUS_MESSAGE_READY;
	String strBaseClassName = Util.getBaseClassName(strTabClassName);

	// STORING the status it in the collection
	int iTabIdx = -1;
	if ((iTabIdx = alTabClassNames.indexOf(strBaseClassName)) != -1) {
	  // only a change of status, already dealt with this tab before
	  alTabStatuses.set(iTabIdx, strStatus);
	} else {
	  // never worked with this tab before, add new one
	  alTabClassNames.add(strBaseClassName);
	  alTabStatuses.add(strStatus);
	}

	// display the new status if the updated status is on the active tab
	if (isTabActive(strBaseClassName))
	  displayStatus(strBaseClassName);
  }

  // helper to start / stop the spinner in the status bar that
  // indicates that some action is currently in progress
  // (action will be displayed by lStatusMsg label)
  public void startSpinner(boolean bStart) {
	this.lSpinnerIcon.setIcon(bStart ? this.iconSpinner : this.iconSpinnerStopped);
  }

  // Determine whether the tab in the parameter is currently active in the main
  // tabbed pane.
  private boolean isTabActive(String strTabClassName) {
	// get the current active tab (this is a normal class name of the main tab
	// content component)
	String strCurSelectedTabClassName = this.pluginMainComponent.getMainTabs().getSelectedComponent().getClass().getName();

	// get the real class name to match
	String strBaseClassName = Util.getBaseClassName(strTabClassName);

	// compare the two class names
	return (strBaseClassName.equals(strCurSelectedTabClassName));
  }

  public void actionPerformed(ActionEvent e) {
	//	if (e.getSource().equals(this.bPreferences)) {
	//	  // open preferences dialog box
	//	  pluginMainComponent.getPreferencesDialog().setVisible(true);
	//	}
  }

}
