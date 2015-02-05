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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.ui.perspectives.myexperiment.ResourceListPanel;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Workflow;

/**
 * @author Sergejs Aleksejevs, Jiten Bhagat
 */
public class ExampleWorkflowsPanel extends JPanel implements ActionListener, ChangeListener {

  private static final String ACTION_REFRESH = "refresh_example_workflows";

  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;

  private JLabel statusLabel;
  private JButton refreshButton;

  private List<Workflow> workflows = new ArrayList<Workflow>();

  private ResourceListPanel workflowsListPanel;

  public ExampleWorkflowsPanel(MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the
	// parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	this.initialiseUI();
  }

  public void actionPerformed(ActionEvent event) {
	if (ACTION_REFRESH.equals(event.getActionCommand())) {
	  this.refresh();
	}
  }

  public void stateChanged(ChangeEvent event) {

  }

  public void clear() {
	this.statusLabel.setText("");
	this.workflowsListPanel.clear();
  }

  public void refresh() {
	this.pluginMainComponent.getStatusBar().setStatus(this.getClass().getName(), "Fetching example workflows from myExperiment");
	this.statusLabel.setText("");

	// Make call to myExperiment API in a different thread
	// (then use SwingUtilities.invokeLater to update the UI when ready).
	new Thread("Refresh for ExampleWorkflowsPanel") {
	  public void run() {
		logger.debug("Refreshing Example Workflows tab");

		try {
		  workflows = myExperimentClient.getExampleWorkflows();

		  SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			  repopulate();
			}
		  });
		} catch (Exception ex) {
		  logger.error("Failed to refresh Example Workflows panel", ex);
		}
	  }
	}.start();

  }

  public void repopulate() {
	logger.debug("Repopulating Example Workflows tab");

	this.pluginMainComponent.getStatusBar().setStatus(this.getClass().getName(), null);
	this.statusLabel.setText("This will contain example resources to"
		+ " help get you started in Taverna.  Coming Soon.");
	this.statusLabel.setText(this.workflows.size() + " example workflows found");

	// cannot cast a list of subclass items into a list of superclass items -
	// hence a new list is created
	this.workflowsListPanel.setListItems(new ArrayList<Resource>(this.workflows));

	this.revalidate();
  }

  private void initialiseUI() {
	this.setLayout(new BorderLayout());

	JPanel topPanel = new JPanel(new BorderLayout());
	topPanel.setBorder(BorderFactory.createEtchedBorder());
	this.statusLabel = new JLabel();
	this.statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));
	topPanel.add(this.statusLabel, BorderLayout.CENTER);
	this.refreshButton = new JButton("Refresh", WorkbenchIcons.refreshIcon);
	this.refreshButton.setActionCommand(ACTION_REFRESH);
	this.refreshButton.addActionListener(this);
	this.refreshButton.setToolTipText("Click this button to refresh the Example Workflows list");
	topPanel.add(this.refreshButton, BorderLayout.EAST);
	this.add(topPanel, BorderLayout.NORTH);

	this.workflowsListPanel = new ResourceListPanel(this.pluginMainComponent, this.myExperimentClient, this.logger);
	JScrollPane spExampleWorkflowList = new JScrollPane(this.workflowsListPanel);
	spExampleWorkflowList.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

	this.add(spExampleWorkflowList, BorderLayout.CENTER);
  }
}
