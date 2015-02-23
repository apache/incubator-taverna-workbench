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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira
 */
public class SearchOptionsPanel extends JPanel implements ItemListener, KeyListener {
  // CONSTANTS
  protected static final int SEARCH_RESULT_LIMIT_MIN = 1;
  protected static final int SEARCH_RESULT_LIMIT_INIT = 20;
  protected static final int SEARCH_RESULT_LIMIT_MAX = 100;

  private final MainComponent pluginMainComponent;
  private final MyExperimentClient myExperimentClient;
  private final Logger logger;
  private final ActionListener clickHandler;

  // COMPONENTS
  protected JButton bSearch;
  private JTextField tfSearchQuery;
  private JCheckBox cbSearchAllTypes;
  private JCheckBox cbWorkflows;
  private JCheckBox cbFiles;
  private JCheckBox cbPacks;
  private JCheckBox cbUsers;
  private JCheckBox cbGroups;
  protected JSpinner jsResultLimit;
  protected JTextField tfResultLimitTextField;

  // Data
  ArrayList<JCheckBox> alDataTypeCheckboxes;

  public SearchOptionsPanel(ActionListener actionListener, MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;
	this.clickHandler = actionListener;

	this.initialiseUI();

	// this will hold the collection of all checkboxes that correspond to data types (will be used in item event handling)
	alDataTypeCheckboxes = new ArrayList<JCheckBox>(Arrays.asList(new JCheckBox[] { cbWorkflows, cbFiles, cbPacks, cbUsers, cbGroups }));
  }

  private void initialiseUI() {
	this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Search Settings "), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

	this.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	c.gridx = 0;
	c.gridy = 0;
	c.anchor = GridBagConstraints.WEST;
	this.add(new JLabel("Query"), c);

	c.gridx = 0;
	c.gridy = 1;
	c.gridwidth = 4;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	tfSearchQuery = new JTextField();
	tfSearchQuery.setToolTipText("<html>Tips for creating search queries:<br>1) Use wildcards to make more "
		+ "flexible queries. Asterisk (<b>*</b>) matches any zero or more<br>&nbsp;&nbsp;&nbsp;&nbsp;characters (e.g. "
		+ "<b><i>Seq*</i></b> would match <b><i>Sequence</i></b>), question mark (<b>?</b>) matches any single<br>"
		+ "&nbsp;&nbsp;&nbsp;&nbsp;character (e.g. <b><i>Tave?na</i></b> would match <b><i>Taverna</i></b>).<br>"
		+ "2) Enclose the <b><i>\"search query\"</i></b> in double quotes to make exact phrase matching, otherwise<br>"
		+ "&nbsp;&nbsp;&nbsp;&nbsp;items that contain any (or all) words in the <b><i>search query</i></b> will be found.</html>");
	tfSearchQuery.addKeyListener(this);
	this.add(tfSearchQuery, c);

	c.gridx = 4;
	c.gridy = 1;
	c.gridwidth = 1;
	c.fill = GridBagConstraints.NONE;
	c.weightx = 0;
	c.insets = new Insets(0, 5, 0, 0);
	bSearch = new JButton("Search", WorkbenchIcons.searchIcon);
	bSearch.addActionListener(this.clickHandler);
	bSearch.addKeyListener(this);
	this.add(bSearch, c);

	c.gridx = 0;
	c.gridy = 2;
	c.insets = new Insets(10, 0, 3, 0);
	this.add(new JLabel("Search for..."), c);

	c.gridx = 0;
	c.gridy = 3;
	c.gridwidth = 2;
	c.insets = new Insets(0, 0, 0, 0);
	cbSearchAllTypes = new JCheckBox("all resource types", true);
	cbSearchAllTypes.addItemListener(this);
	cbSearchAllTypes.addKeyListener(this);
	this.add(cbSearchAllTypes, c);

	c.gridx = 0;
	c.gridy = 4;
	c.gridwidth = 1;
	c.ipady = 0;
	cbWorkflows = new JCheckBox("workflows", true);
	cbWorkflows.addItemListener(this);
	cbWorkflows.addKeyListener(this);
	this.add(cbWorkflows, c);

	c.gridx = 0;
	c.gridy = 5;
	cbFiles = new JCheckBox("files", true);
	cbFiles.addItemListener(this);
	cbFiles.addKeyListener(this);
	this.add(cbFiles, c);

	c.gridx = 0;
	c.gridy = 6;
	cbPacks = new JCheckBox("packs", true);
	cbPacks.addItemListener(this);
	cbPacks.addKeyListener(this);
	this.add(cbPacks, c);

	c.gridx = 1;
	c.gridy = 4;
	cbUsers = new JCheckBox("users", true);
	cbUsers.addItemListener(this);
	cbUsers.addKeyListener(this);
	this.add(cbUsers, c);

	c.gridx = 1;
	c.gridy = 5;
	cbGroups = new JCheckBox("groups", true);
	cbGroups.addItemListener(this);
	cbGroups.addKeyListener(this);
	this.add(cbGroups, c);

	c.gridx = 3;
	c.gridy = 2;
	c.insets = new Insets(10, 25, 3, 0);
	JLabel jlResultLimit = new JLabel("Result limit");
	this.add(jlResultLimit, c);

	c.gridx = 3;
	c.gridy = 3;
	c.insets = new Insets(0, 25, 0, 0);
	jsResultLimit = new JSpinner(new SpinnerNumberModel(SEARCH_RESULT_LIMIT_INIT, SEARCH_RESULT_LIMIT_MIN, SEARCH_RESULT_LIMIT_MAX, 1));
	jsResultLimit.setPreferredSize(new Dimension(jlResultLimit.getPreferredSize().width, jsResultLimit.getPreferredSize().height));
	this.add(jsResultLimit, c);

	// adding KeyListener to JSpinner directly doesn't make sense; need to attach KeyListener to the text field that is associated with spinner
	tfResultLimitTextField = ((JSpinner.DefaultEditor) this.jsResultLimit.getEditor()).getTextField();
	tfResultLimitTextField.addKeyListener(this);
  }

  public String getSearchQuery() {
	return (this.tfSearchQuery.getText());
  }

  public void setSearchQuery(String strSearchQuery) {
	this.tfSearchQuery.setText(strSearchQuery);
  }

  public void focusSearchQueryField() {
	this.tfSearchQuery.selectAll();
	this.tfSearchQuery.requestFocusInWindow();
  }

  public void setSearchAllResourceTypes(boolean bSearchAllTypes) {
	this.cbSearchAllTypes.setSelected(bSearchAllTypes);
  }

  public boolean getSearchWorkflows() {
	return (this.cbWorkflows.isSelected());
  }

  public void setSearchWorkflows(boolean bSearchWorkflows) {
	this.cbWorkflows.setSelected(bSearchWorkflows);
  }

  public boolean getSearchFiles() {
	return (this.cbFiles.isSelected());
  }

  public void setSearchFiles(boolean bSearchFiles) {
	this.cbFiles.setSelected(bSearchFiles);
  }

  public boolean getSearchPacks() {
	return (this.cbPacks.isSelected());
  }

  public void setSearchPacks(boolean bSearchPacks) {
	this.cbPacks.setSelected(bSearchPacks);
  }

  public boolean getSearchUsers() {
	return (this.cbUsers.isSelected());
  }

  public void setSearchUsers(boolean bSearchUsers) {
	this.cbUsers.setSelected(bSearchUsers);
  }

  public boolean getSearchGroups() {
	return (this.cbGroups.isSelected());
  }

  public void setSearchGroups(boolean bSearchGroups) {
	this.cbGroups.setSelected(bSearchGroups);
  }

  public int getResultCountLimit() {
	// JSpinner handles value validation itself, so there is no need to
	// make our own validation too
	return (Integer.parseInt(this.jsResultLimit.getValue().toString()));
  }

  public void setResultCountLimit(int iLimit) {
	this.jsResultLimit.setValue(iLimit);
  }

  // this monitors all checkbox clicks and selects / deselects other checkboxes which are relevant
  public void itemStateChanged(ItemEvent e) {
	if (e.getItemSelectable().equals(this.cbSearchAllTypes)) {
	  // "all resource types" clicked - need to select / deselect all data type checkboxes accordingly
	  for (JCheckBox cb : this.alDataTypeCheckboxes) {
		cb.removeItemListener(this);
		cb.setSelected(this.cbSearchAllTypes.isSelected());
		cb.addItemListener(this);
	  }

	  // also, enable / disable the search button
	  this.bSearch.setEnabled(this.cbSearchAllTypes.isSelected());
	} else if (this.alDataTypeCheckboxes.contains(e.getItemSelectable())) {
	  // one of the checkboxes for data types was clicked (e.g. workflows, files, etc);
	  // need to calculate how many of those are currently selected
	  int iSelectedCnt = 0;
	  for (JCheckBox cb : this.alDataTypeCheckboxes) {
		if (cb.isSelected())
		  iSelectedCnt++;
	  }

	  // if all are selected, tick "search all types" checkbox; uncheck otherwise
	  this.cbSearchAllTypes.removeItemListener(this);
	  this.cbSearchAllTypes.setSelected(iSelectedCnt == this.alDataTypeCheckboxes.size());
	  this.cbSearchAllTypes.addItemListener(this);

	  // enable search button if at least one data type is selected; disable otherwise
	  this.bSearch.setEnabled(iSelectedCnt > 0);
	}
  }

  public void keyPressed(KeyEvent e) {
	// ENTER pressed - start search by simulating "search" button click
	// (only do this if the "search" button was active at that moment)
	if (e.getKeyCode() == KeyEvent.VK_ENTER
		&& this.bSearch.isEnabled()
		&& (Arrays.asList(new JComponent[] { this.tfSearchQuery, this.bSearch, this.cbSearchAllTypes, this.tfResultLimitTextField }).contains(e.getSource()) || this.alDataTypeCheckboxes.contains(e.getSource()))) {
	  this.clickHandler.actionPerformed(new ActionEvent(this.bSearch, 0, ""));
	}
  }

  public void keyReleased(KeyEvent e) {
	// do nothing
  }

  public void keyTyped(KeyEvent e) {
	// do nothing
  }

}
