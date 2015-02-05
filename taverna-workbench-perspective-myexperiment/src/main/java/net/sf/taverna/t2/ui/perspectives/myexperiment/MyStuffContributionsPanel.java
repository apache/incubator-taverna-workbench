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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.File;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Pack;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Workflow;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;


/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira, Jiten Bhagat
 */
public class MyStuffContributionsPanel extends JPanel implements ActionListener {
  // CONSTANTS
  private static final int SHADED_LABEL_HEIGHT = 20;
  private static final int SECTION_VSPACING = 5;
  private static final int SCROLL_PANE_PADDING = 3;
  private static final int TOTAL_SECTION_VSPACING = SHADED_LABEL_HEIGHT
	  + SECTION_VSPACING + SCROLL_PANE_PADDING + 5; // the last literal is to cover all paddings / margins / etc

  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;

  // MAIN COMPONENTS of the view
  JPanel jpMyWorkflows;
  JPanel jpMyFiles;
  JPanel jpMyPacks;

  // HELPER COMPONENTS
  // these are the individual content listings for 'my workfows', 'my files' and 'my packs' ..
  ResourceListPanel jpMyWorkflowsContent = null;
  ResourceListPanel jpMyFilesContent = null;
  ResourceListPanel jpMyPacksContent = null;

  // .. these will be wrapped into individual scroll panes to ensure correct sizes
  JScrollPane spMyWorkflowsContent = null;
  JScrollPane spMyFilesContent = null;
  JScrollPane spMyPacksContent = null;

  // STORAGE
  private ArrayList<JPanel> alVisiblePanels;
  private ArrayList<JComponent[]> alVisiblePanelsWithHelperElements;

  public MyStuffContributionsPanel(MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	alVisiblePanels = new ArrayList<JPanel>();
	alVisiblePanelsWithHelperElements = new ArrayList<JComponent[]>();

	// check that settings for this panel were set correctly in the INI file
	// (if any record is missing - assume that this section is visible)
	// (this will ensure that these values can be used with no further validity checks
	//  across the plugin; this is because this method will be executed at start of the plugin)
	if (myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_MY_STUFF_WORKFLOWS) == null) {
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_MY_STUFF_WORKFLOWS, new Boolean(true).toString());
	}
	if (myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_MY_STUFF_FILES) == null) {
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_MY_STUFF_FILES, new Boolean(true).toString());
	}
	if (myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_MY_STUFF_PACKS) == null) {
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_MY_STUFF_PACKS, new Boolean(true).toString());
	}

	// create and initialise the UI of MyStuff tab
	initialiseUI();
	initialiseData();
  }

  private void initialiseUI() {
	JPanel jpMyWorkflowsContainer = new JPanel();
	jpMyWorkflowsContainer.setBorder(BorderFactory.createEmptyBorder());
	if (Boolean.parseBoolean(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_MY_STUFF_WORKFLOWS))) {
	  // "My Workflows" panel
	  jpMyWorkflowsContainer.setBorder(BorderFactory.createEtchedBorder());
	  jpMyWorkflowsContainer.setLayout(new BorderLayout());

	  ShadedLabel l0 = new ShadedLabel("My Workflows", ShadedLabel.BLUE);
	  jpMyWorkflowsContainer.add(l0, BorderLayout.NORTH);

	  jpMyWorkflows = new JPanel();
	  jpMyWorkflows.setLayout(new BorderLayout());
	  jpMyWorkflows.setBackground(Color.WHITE);
	  jpMyWorkflows.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
	  jpMyWorkflows.add(new JLabel("Loading...", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner")), SwingConstants.CENTER));

	  jpMyWorkflowsContainer.add(jpMyWorkflows, BorderLayout.CENTER);
	  alVisiblePanels.add(jpMyWorkflows);
	}

	JPanel jpMyFilesContainer = new JPanel();
	if (Boolean.parseBoolean(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_MY_STUFF_FILES))) {
	  // "My Files" panel
	  jpMyFilesContainer.setBorder(BorderFactory.createEtchedBorder());
	  jpMyFilesContainer.setLayout(new BorderLayout());

	  ShadedLabel l1 = new ShadedLabel("My Files", ShadedLabel.BLUE);
	  jpMyFilesContainer.add(l1, BorderLayout.NORTH);

	  jpMyFiles = new JPanel();
	  jpMyFiles.setLayout(new BorderLayout());
	  jpMyFiles.setBackground(Color.WHITE);
	  jpMyFiles.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
	  jpMyFiles.add(new JLabel("Loading...", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner")), SwingConstants.CENTER));

	  jpMyFilesContainer.add(jpMyFiles, BorderLayout.CENTER);
	  alVisiblePanels.add(jpMyFiles);
	}

	JPanel jpMyPacksContainer = new JPanel();
	if (Boolean.parseBoolean(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_MY_STUFF_PACKS))) {
	  // "My Packs" panel
	  jpMyPacksContainer.setBorder(BorderFactory.createEtchedBorder());
	  jpMyPacksContainer.setLayout(new BorderLayout());

	  ShadedLabel l2 = new ShadedLabel("My Packs", ShadedLabel.BLUE);
	  jpMyPacksContainer.add(l2, BorderLayout.NORTH);

	  jpMyPacks = new JPanel();
	  jpMyPacks.setLayout(new BorderLayout());
	  jpMyPacks.setBackground(Color.WHITE);
	  jpMyPacks.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
	  jpMyPacks.add(new JLabel("Loading...", new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner")), SwingConstants.CENTER));

	  jpMyPacksContainer.add(jpMyPacks, BorderLayout.CENTER);
	  alVisiblePanels.add(jpMyPacks);
	}

	// ..putting everything together    
	JPanel jpEverything = new JPanel();
	jpEverything.setLayout(new GridBagLayout());

	GridBagConstraints gbConstraints = new GridBagConstraints();
	gbConstraints.anchor = GridBagConstraints.NORTHWEST;
	gbConstraints.fill = GridBagConstraints.BOTH;
	gbConstraints.gridx = 0;
	gbConstraints.weightx = 1;
	gbConstraints.weighty = 1;
	int index = 0;

	gbConstraints.gridy = index++;
	jpEverything.add(jpMyWorkflowsContainer, gbConstraints);

	gbConstraints.gridy = index++;
	jpEverything.add(jpMyFilesContainer, gbConstraints);

	gbConstraints.gridy = index++;
	jpEverything.add(jpMyPacksContainer, gbConstraints);

	this.setLayout(new BorderLayout());
	this.add(jpEverything, BorderLayout.NORTH);
  }

  private void initialiseData() {
	// Make call to myExperiment API in a different thread
	// (then use SwingUtilities.invokeLater to update the UI when ready).
	new Thread("Loading data about contributions of current user.") {
	  @SuppressWarnings("unchecked")
	  public void run() {
		logger.debug("Loading contributions data for current user");

		try {
		  final ArrayList<Workflow> alWorkflowInstances = new ArrayList<Workflow>();
		  if (alVisiblePanels.contains(jpMyWorkflows)) {
			boolean anyMore = true;
			for (int page = 1; anyMore; page++) {
				// fetch all user workflows
				Document doc = myExperimentClient.getUserContributions(myExperimentClient.getCurrentUser(), Resource.WORKFLOW, Resource.REQUEST_SHORT_LISTING, page);
				if (doc != null) {
					List<Element> foundElements = doc.getRootElement().getChildren();
					anyMore = !foundElements.isEmpty();
					for (Element e : foundElements) {
						Workflow wfCurrent = Workflow.buildFromXML(e, logger);
						alWorkflowInstances.add(wfCurrent);
					}
				}
				}
		  }

		  final ArrayList<File> alFileInstances = new ArrayList<File>();
		  if (alVisiblePanels.contains(jpMyFiles)) {
				boolean anyMore = true;
				for (int page = 1; anyMore; page++) {
			// fetch all user files
			Document doc = myExperimentClient.getUserContributions(myExperimentClient.getCurrentUser(), Resource.FILE, Resource.REQUEST_SHORT_LISTING, page);
			if (doc != null) {
			  List<Element> foundElements = doc.getRootElement().getChildren();
				anyMore = !foundElements.isEmpty();
			  for (Element e : foundElements) {
				File fCurrent = File.buildFromXML(e, logger);
				alFileInstances.add(fCurrent);
			  }
			}
				}
		  }

		  final ArrayList<Pack> alPackInstances = new ArrayList<Pack>();
		  if (alVisiblePanels.contains(jpMyPacks)) {
				boolean anyMore = true;
				for (int page = 1; anyMore; page++) {
			// fetch all user packs
			Document doc = myExperimentClient.getUserContributions(myExperimentClient.getCurrentUser(), Resource.PACK, Resource.REQUEST_SHORT_LISTING, page);
			if (doc != null) {
			  List<Element> foundElements = doc.getRootElement().getChildren();
				anyMore = !foundElements.isEmpty();
			  for (Element e : foundElements) {
				Pack pCurrent = Pack.buildFromXML(e, myExperimentClient, logger);
				alPackInstances.add(pCurrent);
			  }
			}
				}
		  }

		  SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			  // now create views for all user contributions
			  if (alVisiblePanels.contains(jpMyWorkflows)) {
				// .. workflows ..
				jpMyWorkflowsContent = new ResourceListPanel(pluginMainComponent, myExperimentClient, logger);
				jpMyWorkflowsContent.setFullSizeItemsList(false);
				jpMyWorkflowsContent.setListItems(new ArrayList<Resource>(alWorkflowInstances));

				spMyWorkflowsContent = new JScrollPane(jpMyWorkflowsContent);
				spMyWorkflowsContent.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

				jpMyWorkflows.removeAll();
				jpMyWorkflows.setBackground(null); // return background to default colour
				jpMyWorkflows.setBorder(BorderFactory.createEmptyBorder()); // remove border that was added prior to loading
				jpMyWorkflows.add(spMyWorkflowsContent);

				alVisiblePanelsWithHelperElements.add(new JComponent[] { jpMyWorkflows, spMyWorkflowsContent, jpMyWorkflowsContent });
			  }

			  if (alVisiblePanels.contains(jpMyFiles)) {
				// .. files ..
				jpMyFilesContent = new ResourceListPanel(pluginMainComponent, myExperimentClient, logger);
				jpMyFilesContent.setFullSizeItemsList(false);
				jpMyFilesContent.setListItems(new ArrayList<Resource>(alFileInstances));

				spMyFilesContent = new JScrollPane(jpMyFilesContent);
				spMyFilesContent.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

				jpMyFiles.removeAll();
				jpMyFiles.setBackground(null); // return background to default colour
				jpMyFiles.setBorder(BorderFactory.createEmptyBorder()); // remove border that was added prior to loading
				jpMyFiles.add(spMyFilesContent);

				alVisiblePanelsWithHelperElements.add(new JComponent[] { jpMyFiles, spMyFilesContent, jpMyFilesContent });
			  }

			  if (alVisiblePanels.contains(jpMyPacks)) {
				// .. packs ..
				jpMyPacksContent = new ResourceListPanel(pluginMainComponent, myExperimentClient, logger);
				jpMyPacksContent.setFullSizeItemsList(false);
				jpMyPacksContent.setListItems(new ArrayList<Resource>(alPackInstances));

				spMyPacksContent = new JScrollPane(jpMyPacksContent);
				spMyPacksContent.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

				jpMyPacks.removeAll();
				jpMyPacks.setBackground(null); // return background to default colour
				jpMyPacks.setBorder(BorderFactory.createEmptyBorder()); // remove border that was added prior to loading
				jpMyPacks.add(spMyPacksContent);

				alVisiblePanelsWithHelperElements.add(new JComponent[] { jpMyPacks, spMyPacksContent, jpMyPacksContent });
			  }

			  // now work out correct sizes for each section - the goal is to maximize the usage of the space on the page

			  int iFullAvailableHeight = getSize().height;
			  ArrayList<Integer> alIndexesToScale = new ArrayList<Integer>();
			  for (int i = 0; i < alVisiblePanelsWithHelperElements.size(); i++) {
				JScrollPane spContent = (JScrollPane) alVisiblePanelsWithHelperElements.get(i)[1];
				JPanel jpContent = (JPanel) alVisiblePanelsWithHelperElements.get(i)[2];

				if ((jpContent.getPreferredSize().height + TOTAL_SECTION_VSPACING) < (iFullAvailableHeight / alVisiblePanels.size())) {
				  Dimension d = jpContent.getPreferredSize();
				  d.height += SCROLL_PANE_PADDING;
				  spContent.setPreferredSize(d);

				  iFullAvailableHeight -= (jpContent.getPreferredSize().height)
					  + TOTAL_SECTION_VSPACING;
				} else
				  alIndexesToScale.add(i);
			  }

			  if (alIndexesToScale.size() > 0) {
				Dimension d = new Dimension();

				for (Integer i : alIndexesToScale) {
				  d.height = (iFullAvailableHeight / alIndexesToScale.size())
					  - TOTAL_SECTION_VSPACING;
				  if (d.height > ((JPanel) alVisiblePanelsWithHelperElements.get(i)[2]).getPreferredSize().height
					  + SCROLL_PANE_PADDING)
					d.height = ((JPanel) alVisiblePanelsWithHelperElements.get(i)[2]).getPreferredSize().height
						+ SCROLL_PANE_PADDING;

				  JScrollPane spCurrent = (JScrollPane) alVisiblePanelsWithHelperElements.get(i)[1];
				  spCurrent.setPreferredSize(d);
				}
			  }

			  // report that this component has been loaded
			  pluginMainComponent.getMyStuffTab().cdlComponentLoadingDone.countDown();

			  validate();
			  repaint();
			}
		  });
		} catch (Exception ex) {
		  logger.error("Failed to populate some panel in My Stuff tab (User's files, workflows or packs)", ex);
		}
	  }
	}.start();
  }

  public void actionPerformed(ActionEvent arg0) {
	javax.swing.JOptionPane.showMessageDialog(null, "button clicked");
  }

}
