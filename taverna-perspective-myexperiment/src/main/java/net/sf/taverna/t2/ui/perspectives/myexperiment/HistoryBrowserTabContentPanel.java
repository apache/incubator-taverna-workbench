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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Base64;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Tag;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.SearchEngine.QuerySearchInstance;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira
 */
public class HistoryBrowserTabContentPanel extends JPanel implements ActionListener {
  // CONSTANTS
  public static final int DOWNLOADED_ITEMS_HISTORY_LENGTH = 50;
  public static final int OPENED_ITEMS_HISTORY_LENGTH = 50;
  public static final int UPLOADED_ITEMS_HISTORY_LENGTH = 50;
  public static final int COMMENTED_ON_ITEMS_HISTORY_LENGTH = 50;

  public static final int PREVIEWED_ITEMS_HISTORY = 0;
  public static final int DOWNLOADED_ITEMS_HISTORY = 1;
  public static final int OPENED_ITEMS_HISTORY = 2;
  public static final int UPLOADED_ITEMS_HISTORY = 4;
  public static final int COMMENTED_ON_ITEMS_HISTORY = 3;

  private final MainComponent pluginMainComponent;
  private final MyExperimentClient myExperimentClient;
  private final Logger logger;

  // STORAGE
  private ArrayList<Resource> lDownloadedItems;
  private ArrayList<Resource> lOpenedItems;
  private ArrayList<Resource> lUploadedItems;
  private ArrayList<Resource> lCommentedOnItems;

  // COMPONENTS
  private JPanel jpPreviewHistory;
  private JPanel jpSearchHistory;
  private JPanel jpTagSearchHistory;
  private JPanel jpDownloadedItemsHistory;
  private JPanel jpOpenedItemsHistory;
  private JPanel jpUploadedItemsHistory;
  private JPanel jpCommentedOnHistory;
  private JSplitPane spMain;
  private JClickableLabel jclPreviewHistory;
  private JClickableLabel jclSearchHistory;
  private JClickableLabel jclTagSearchHistory;
  private JClickableLabel jclDownloadedItemsHistory;
  private JClickableLabel jclOpenedItemsHistory;
  private JClickableLabel jclUploadedItemsHistory;
  private JClickableLabel jclCommentedOnHistory;
  private JPanel jpPreviewHistoryBox;
  private JPanel jpSearchHistoryBox;
  private JPanel jpTagSearchHistoryBox;
  private JPanel jpDownloadedItemsHistoryBox;
  private JPanel jpOpenedItemsHistoryBox;
  private JPanel jpUploadedItemsHistoryBox;
  private JPanel jpCommentedOnHistoryBox;

  @SuppressWarnings("unchecked")
  public HistoryBrowserTabContentPanel(MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	// initialise downloaded items history
	String strDownloadedItemsHistory = (String) myExperimentClient.getSettings().get(MyExperimentClient.INI_DOWNLOADED_ITEMS_HISTORY);
	if (strDownloadedItemsHistory != null) {
	  Object oDownloadedItemsHistory = Base64.decodeToObject(strDownloadedItemsHistory);
	  this.lDownloadedItems = (ArrayList<Resource>) oDownloadedItemsHistory;
	} else {
	  this.lDownloadedItems = new ArrayList<Resource>();
	}

	// initialise opened items history
	String strOpenedItemsHistory = (String) myExperimentClient.getSettings().get(MyExperimentClient.INI_OPENED_ITEMS_HISTORY);
	if (strOpenedItemsHistory != null) {
	  Object oOpenedItemsHistory = Base64.decodeToObject(strOpenedItemsHistory);
	  this.lOpenedItems = (ArrayList<Resource>) oOpenedItemsHistory;
	} else {
	  this.lOpenedItems = new ArrayList<Resource>();
	}

	// initialise uploaded items history
	String strUploadedItemsHistory = (String) myExperimentClient.getSettings().get(MyExperimentClient.INI_UPLOADED_ITEMS_HISTORY);
	if (strUploadedItemsHistory != null) {
	  Object oUploadedItemsHistory = Base64.decodeToObject(strUploadedItemsHistory);
	  this.lUploadedItems = (ArrayList<Resource>) oUploadedItemsHistory;
	} else {
	  this.lUploadedItems = new ArrayList<Resource>();
	}

	// initialise history of the items that were commented on
	String strCommentedItemsHistory = (String) myExperimentClient.getSettings().get(MyExperimentClient.INI_COMMENTED_ITEMS_HISTORY);
	if (strCommentedItemsHistory != null) {
	  Object oCommentedItemsHistory = Base64.decodeToObject(strCommentedItemsHistory);
	  this.lCommentedOnItems = (ArrayList<Resource>) oCommentedItemsHistory;
	} else {
	  this.lCommentedOnItems = new ArrayList<Resource>();
	}

	this.initialiseUI();
	this.refreshAllData();
  }

  private void confirmHistoryDelete(final int id, String strBoxTitle) {
	if (JOptionPane.showConfirmDialog(null, "This will the "
		+ strBoxTitle.toLowerCase() + " list.\nDo you want to proceed?", "myExperiment Plugin - Confirmation Required", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
	  switch (id) {
		case 1:
		  pluginMainComponent.getPreviewBrowser().clearPreviewHistory();
		  break;
		case 2:
		  pluginMainComponent.getSearchTab().getSearchHistory().clear();
		  pluginMainComponent.getSearchTab().updateSearchHistory();
		  break;
		case 3:
		  pluginMainComponent.getTagBrowserTab().getTagSearchHistory().clear();
		  break;
		case 4:
		  clearDownloadedItemsHistory();
		  break;
		case 5:
		  clearOpenedItemsHistory();
		  break;
		case 6:
		  clearCommentedOnItemsHistory();
		  break;
		case 7:
		  clearUploadedItemsHistory();
		  break;
	  }
	  refreshAllData();
	}
  }

  private JPanel addSpecifiedPanel(final int id, final String strBoxTitle, JPanel jPanel) {
	JPanel jpTemp = new JPanel();
	jpTemp.setLayout(new BorderLayout());
	jpTemp.add(generateContentBox(strBoxTitle, jPanel), BorderLayout.CENTER);
	JButton bClear = new JButton("Clear " + strBoxTitle, WorkbenchIcons.deleteIcon);
	bClear.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
		confirmHistoryDelete(id, strBoxTitle);
	  }
	});

	jpTemp.add(bClear, BorderLayout.SOUTH);
	jpTemp.setMinimumSize(new Dimension(500, 0));
	return jpTemp;
  }

  private void initialiseUI() {
	// create helper text
	ShadedLabel lHelper = new ShadedLabel("All history sections are local to myExperiment plugin usage."
		+ " Detailed history of your actions on myExperiment is available in your profile on myExperiment.", ShadedLabel.BLUE);

	// create all individual content holder panels
	this.jpPreviewHistory = new JPanel();
	this.jpTagSearchHistory = new JPanel();
	this.jpSearchHistory = new JPanel();
	this.jpDownloadedItemsHistory = new JPanel();
	this.jpOpenedItemsHistory = new JPanel();
	this.jpUploadedItemsHistory = new JPanel();
	this.jpCommentedOnHistory = new JPanel();

	// create sidebar
	JPanel jpSidebar = new JPanel();
	jpSidebar.setLayout(new BoxLayout(jpSidebar, BoxLayout.Y_AXIS));
	Border border = BorderFactory.createEmptyBorder(5, 3, 10, 3);
	jclPreviewHistory = new JClickableLabel("Previewed Items", "preview_history", this, WorkbenchIcons.editIcon, SwingConstants.LEFT, "tooltip");
	jclPreviewHistory.setBorder(border);
	jpSidebar.add(jclPreviewHistory);

	jclSearchHistory = new JClickableLabel("Search History", "search_history", this, WorkbenchIcons.editIcon, SwingConstants.LEFT, "tooltip");
	jclSearchHistory.setBorder(border);
	jpSidebar.add(jclSearchHistory);

	jclTagSearchHistory = new JClickableLabel("Tag Searches Made", "tag_history", this, WorkbenchIcons.editIcon, SwingConstants.LEFT, "tooltip");
	jclTagSearchHistory.setBorder(border);
	jpSidebar.add(jclTagSearchHistory);

	jclDownloadedItemsHistory = new JClickableLabel("Downloaded Items", "downloads_history", this, WorkbenchIcons.editIcon, SwingConstants.LEFT, "tooltip");
	jclDownloadedItemsHistory.setBorder(border);
	jpSidebar.add(jclDownloadedItemsHistory);

	jclOpenedItemsHistory = new JClickableLabel("Opened Items", "opened_history", this, WorkbenchIcons.editIcon, SwingConstants.LEFT, "tooltip");
	jclOpenedItemsHistory.setBorder(border);
	jpSidebar.add(jclOpenedItemsHistory);

	jclUploadedItemsHistory = new JClickableLabel("Updated Items", "uploaded_history", this, WorkbenchIcons.editIcon, SwingConstants.LEFT, "tooltip");
	jclUploadedItemsHistory.setBorder(border);
	jpSidebar.add(jclUploadedItemsHistory);

	jclCommentedOnHistory = new JClickableLabel("Items Commented On", "comments_history", this, WorkbenchIcons.editIcon, SwingConstants.LEFT, "tooltip");
	jclCommentedOnHistory.setBorder(border);
	jpSidebar.add(jclCommentedOnHistory);

	JPanel jpSidebarContainer = new JPanel();
	jpSidebarContainer.add(jpSidebar, BorderLayout.NORTH);
	JScrollPane spSidebar = new JScrollPane(jpSidebarContainer);
	spSidebar.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);
	spSidebar.setMinimumSize(new Dimension(245, 0));
	spSidebar.setMaximumSize(new Dimension(300, 0));

	// create standard boxes for each content holder panels
	// only one of these will hold the right hand side of spMain at any given time
	// arg 1: is the ID, which will be used by confirmHistoryDelete() to decide
	// how to delete the history item
	jpPreviewHistoryBox = addSpecifiedPanel(1, "Items you have previewed", jpPreviewHistory);
	jpSearchHistoryBox = addSpecifiedPanel(2, "Terms you have searched for", jpSearchHistory);
	jpTagSearchHistoryBox = addSpecifiedPanel(3, "Tags searches you have made", jpTagSearchHistory);
	jpDownloadedItemsHistoryBox = addSpecifiedPanel(4, "Items you have downloaded", jpDownloadedItemsHistory);
	jpOpenedItemsHistoryBox = addSpecifiedPanel(5, "Workflows you have opened in Taverna", jpOpenedItemsHistory);
	jpCommentedOnHistoryBox = addSpecifiedPanel(6, "Items you have commented on in myExperiment", jpCommentedOnHistory);
	jpUploadedItemsHistoryBox = addSpecifiedPanel(7, "Items you have updated on myExperiment", jpUploadedItemsHistory);

	// put everything together
	spMain = new JSplitPane();
	spMain.setLeftComponent(spSidebar);
	spMain.setRightComponent(jpPreviewHistoryBox);

	spMain.setOneTouchExpandable(true);
	spMain.setDividerLocation(247);
	spMain.setDoubleBuffered(true);

	// spMyStuff will be the only component in the Panel
	this.setLayout(new BorderLayout());
	this.add(spMain);
	this.add(lHelper, BorderLayout.NORTH);

  }

  public ArrayList<Resource> getDownloadedItemsHistoryList() {
	return (this.lDownloadedItems);
  }

  public void clearDownloadedItemsHistory() {
	this.lDownloadedItems.clear();
  }

  public ArrayList<Resource> getOpenedItemsHistoryList() {
	return (this.lOpenedItems);
  }

  public ArrayList<Resource> getUploadedItemsHistoryList() {
	return (this.lUploadedItems);
  }

  public void clearOpenedItemsHistory() {
	this.lOpenedItems.clear();
  }

  public void clearUploadedItemsHistory() {
	this.lUploadedItems.clear();
  }

  public ArrayList<Resource> getCommentedOnItemsHistoryList() {
	return (this.lCommentedOnItems);
  }

  public void clearCommentedOnItemsHistory() {
	this.lCommentedOnItems.clear();
  }

  /**
   * Used to refresh all boxes at a time (for example at launch time).
   */
  private void refreshAllData() {
	this.refreshHistoryBox(PREVIEWED_ITEMS_HISTORY);
	this.refreshHistoryBox(DOWNLOADED_ITEMS_HISTORY);
	this.refreshHistoryBox(OPENED_ITEMS_HISTORY);
	this.refreshHistoryBox(UPLOADED_ITEMS_HISTORY);
	this.refreshHistoryBox(COMMENTED_ON_ITEMS_HISTORY);
	this.refreshSearchHistory();
	this.refreshTagSearchHistory();
  }

  /**
   * This helper can be called externally to refresh the following history
   * boxes: previewed items history, downloaded items history, opened items
   * history and the history of items that were commented on.
   * 
   * Is used inside ResourcePreviewBrowser and MainComponent every time a
   * relevant action occurs. Also useful, when an option to 'clear preview
   * history' is used in the Preferences window for. a particular history type.
   */
  public void refreshHistoryBox(int historyType) {
	switch (historyType) {
	  case PREVIEWED_ITEMS_HISTORY:
		this.jpPreviewHistory.removeAll();
		populateHistoryBox(this.pluginMainComponent.getPreviewBrowser().getPreviewHistory(), this.jpPreviewHistory, "No items were previewed yet");
		break;
	  case DOWNLOADED_ITEMS_HISTORY:
		this.jpDownloadedItemsHistory.removeAll();
		populateHistoryBox(this.lDownloadedItems, this.jpDownloadedItemsHistory, "No items were downloaded");
		break;
	  case OPENED_ITEMS_HISTORY:
		this.jpOpenedItemsHistory.removeAll();
		populateHistoryBox(this.lOpenedItems, this.jpOpenedItemsHistory, "No items were opened");
		break;
	  case UPLOADED_ITEMS_HISTORY:
		this.jpUploadedItemsHistory.removeAll();
		populateHistoryBox(this.lUploadedItems, this.jpUploadedItemsHistory, "No items were updated");
		break;
	  case COMMENTED_ON_ITEMS_HISTORY:
		this.jpCommentedOnHistory.removeAll();
		populateHistoryBox(this.lCommentedOnItems, this.jpCommentedOnHistory, "You didn't comment on any items");
		break;
	}
  }

  /**
   * Retrieves history data from a relevant list and populates the specified
   * panel with it. All listed items will be resources that can be opened by
   * Preview Browser.
   */
  private void populateHistoryBox(List<Resource> lHistory, JPanel jpPanelToPopulate, String strLabelIfNoItems) {
	if (lHistory.size() > 0) {
	  for (int i = lHistory.size() - 1; i >= 0; i--) {
		Resource r = lHistory.get(i);
		if (r != null) {
		  JClickableLabel lResource = Util.generateClickableLabelFor(r, this.pluginMainComponent.getPreviewBrowser());
		  jpPanelToPopulate.add(lResource);
		}
	  }
	} else {
	  jpPanelToPopulate.add(Util.generateNoneTextLabel(strLabelIfNoItems));
	}

	// make sure that the component is updated after population
	jpPanelToPopulate.revalidate();
	jpPanelToPopulate.repaint();
  }

  /**
   * This helper can be called externally to refresh the search history. Is used
   * inside SearchTabContentPanel every time a new item is added to search
   * history.
   */
  public void refreshSearchHistory() {
	this.jpSearchHistory.removeAll();
	populateSearchHistory();
  }

  /**
   * Retrieves search history data from SearchTabContentPanel and populates the
   * relevant panel.
   */
  private void populateSearchHistory() {
	List<QuerySearchInstance> lSearchHistory = this.pluginMainComponent.getSearchTab().getSearchHistory();

	// prepare layout
	this.jpSearchHistory.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.anchor = GridBagConstraints.NORTHWEST;

	if (lSearchHistory.size() > 0) {
	  for (int i = lSearchHistory.size() - 1; i >= 0; i--) {
		QuerySearchInstance qsiCurrent = lSearchHistory.get(i);
		JClickableLabel jclCurrentEntryLabel = new JClickableLabel(qsiCurrent.getSearchQuery(), SearchTabContentPanel.SEARCH_FROM_HISTORY
			+ ":" + i, this, WorkbenchIcons.findIcon, SwingUtilities.LEFT, qsiCurrent.toString());
		JLabel jlCurrentEntrySettings = new JLabel(qsiCurrent.detailsAsString());
		jlCurrentEntrySettings.setBorder(BorderFactory.createEmptyBorder(3, 5, 0, 0));

		JPanel jpCurrentSearchHistoryEntry = new JPanel();
		jpCurrentSearchHistoryEntry.setLayout(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		jpCurrentSearchHistoryEntry.add(jclCurrentEntryLabel, c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		jpCurrentSearchHistoryEntry.add(jlCurrentEntrySettings, c);

		c.gridy = lSearchHistory.size() - 1 - i;
		if (i == 0)
		  c.weighty = 1.0;
		this.jpSearchHistory.add(jpCurrentSearchHistoryEntry, c);
	  }
	} else {
	  c.weightx = 1.0;
	  c.weighty = 1.0;
	  this.jpSearchHistory.add(Util.generateNoneTextLabel(SearchResultsPanel.NO_SEARCHES_STATUS), c);
	}

	// make sure that the component is updated after population
	this.jpSearchHistory.revalidate();
	this.jpSearchHistory.repaint();
  }

  /**
   * This helper can be called externally to refresh the tag search history. Is
   * used inside TagBrowserTabContentPanel every time a new tag is searched for.
   */
  public void refreshTagSearchHistory() {
	this.jpTagSearchHistory.removeAll();
	populateTagSearchHistory();
  }

  /**
   * Retrieves tag search history data from Tag Browser and populates the
   * relevant panel.
   */
  private void populateTagSearchHistory() {
	List<Tag> lTagSearchHistory = this.pluginMainComponent.getTagBrowserTab().getTagSearchHistory();

	if (lTagSearchHistory.size() > 0) {
	  for (int i = lTagSearchHistory.size() - 1; i >= 0; i--) {
		Tag t = lTagSearchHistory.get(i);
		JClickableLabel lTag = new JClickableLabel(t.getTagName(), "tag:"
			+ t.getTagName(), this, new ImageIcon(MyExperimentPerspective.getLocalIconURL(Resource.TAG)), // HACK: after deserialization t.getItemType() return "Unknown" type
		SwingConstants.LEFT, "Tag: " + t.getTagName());
		this.jpTagSearchHistory.add(lTag);
	  }
	} else {
	  this.jpTagSearchHistory.add(Util.generateNoneTextLabel("No searches by tags have been made yet"));
	}

	// make sure that the component is updated after population
	this.jpTagSearchHistory.revalidate();
	this.jpTagSearchHistory.repaint();
  }

  /**
   * @param strBoxTitle
   *          Title of the content box.
   * @param jpContentPanel
   *          JPanel which will be populated with history listing.
   * @return Prepared JPanel with a border, title and jpContentPanel wrapped
   *         into a scroll pane.
   */
  private static JPanel generateContentBox(String strBoxTitle, JPanel jpContentPanel) {
	// set layout for the content panel
	jpContentPanel.setLayout(new BoxLayout(jpContentPanel, BoxLayout.Y_AXIS));

	// wrap content panel into a standard scroll pane
	JScrollPane spContent = new JScrollPane(jpContentPanel);
	spContent.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	spContent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	spContent.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

	// create the actual box stub with a border which will contain the scroll pane
	JPanel jpBox = new JPanel();
	jpBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " "
		+ strBoxTitle + " ")));

	jpBox.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.anchor = GridBagConstraints.NORTHWEST;
	c.fill = GridBagConstraints.BOTH;
	c.weightx = 1.0;
	c.weighty = 1.0;
	jpBox.add(spContent, c);

	return (jpBox);
  }

  // *** Callback for ActionListener interface ***
  public void actionPerformed(ActionEvent e) {
	if (e.getSource() instanceof JClickableLabel) {
	  if (e.getActionCommand().startsWith(SearchTabContentPanel.SEARCH_FROM_HISTORY)) {
		// open search tab and start the chosen search
		this.pluginMainComponent.getSearchTab().actionPerformed(e);
		this.pluginMainComponent.getMainTabs().setSelectedComponent(this.pluginMainComponent.getSearchTab());
	  } else if (e.getActionCommand().startsWith("tag:")) {
		// open tag browser tab and start the chosen tag search
		this.pluginMainComponent.getTagBrowserTab().actionPerformed(e);
		this.pluginMainComponent.getMainTabs().setSelectedComponent(this.pluginMainComponent.getTagBrowserTab());
	  } else if (e.getActionCommand().contains("history")) {
		if (e.getActionCommand() == "preview_history")
		  spMain.setRightComponent(jpPreviewHistoryBox);
		else if (e.getActionCommand() == "search_history")
		  spMain.setRightComponent(jpSearchHistoryBox);
		else if (e.getActionCommand() == "tag_history")
		  spMain.setRightComponent(jpTagSearchHistoryBox);
		else if (e.getActionCommand() == "downloads_history")
		  spMain.setRightComponent(jpDownloadedItemsHistoryBox);
		else if (e.getActionCommand() == "opened_history")
		  spMain.setRightComponent(jpOpenedItemsHistoryBox);
		else if (e.getActionCommand() == "uploaded_history")
		  spMain.setRightComponent(jpUploadedItemsHistoryBox);
		else if (e.getActionCommand() == "comments_history")
		  spMain.setRightComponent(jpCommentedOnHistoryBox);
	  }
	}

  }

}
