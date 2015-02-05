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
import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Base64;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.SearchEngine;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.SearchEngine.QuerySearchInstance;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs
 */
public class SearchTabContentPanel extends JPanel implements ActionListener {
  // CONSTANTS
  private final static int SEARCH_HISTORY_LENGTH = 50;
  private final static int SEARCH_FAVOURITES_LENGTH = 30;
  protected final static String SEARCH_FROM_FAVOURITES = "searchFromFavourites";
  protected final static String SEARCH_FROM_HISTORY = "searchFromHistory";
  protected final static String ADD_FAVOURITE_SEARCH_INSTANCE = "addFavouriteSearchInstance";
  protected final static String REMOVE_FAVOURITE_SEARCH_INSTANCE = "removeFavouriteSearchInstance";

  private final MainComponent pluginMainComponent;
  private final MyExperimentClient myExperimentClient;
  private final Logger logger;

  // COMPONENTS
  private JSplitPane spMainSplitPane;
  private SearchOptionsPanel jpSearchOptions;
  private JPanel jpFavouriteSearches;
  private JPanel jpSearchHistory;
  private SearchResultsPanel jpSearchResults;
  private final ImageIcon iconFavourite = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("favourite_icon"));
  private final ImageIcon iconRemove = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("destroy_icon"));

  // Data storage
  private QuerySearchInstance siPreviousSearch;
  private LinkedList<QuerySearchInstance> llFavouriteSearches;
  private LinkedList<QuerySearchInstance> llSearchHistory;

  // Search components 
  private final SearchEngine searchEngine; // The search engine for executing keyword query searches 
  private final Vector<Long> vCurrentSearchThreadID; // This will keep ID of the current search thread (there will only be one such thread)

  public SearchTabContentPanel(MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	// initialise the favourite searches
	String strFavouriteSearches = (String) myExperimentClient.getSettings().get(MyExperimentClient.INI_FAVOURITE_SEARCHES);
	if (strFavouriteSearches != null) {
	  Object oFavouriteSearches = Base64.decodeToObject(strFavouriteSearches);
	  this.llFavouriteSearches = (LinkedList<QuerySearchInstance>) oFavouriteSearches;
	} else {
	  this.llFavouriteSearches = new LinkedList<QuerySearchInstance>();
	}

	// initialise the search history
	String strSearchHistory = (String) myExperimentClient.getSettings().get(MyExperimentClient.INI_SEARCH_HISTORY);
	if (strSearchHistory != null) {
	  Object oSearchHistory = Base64.decodeToObject(strSearchHistory);
	  this.llSearchHistory = (LinkedList<QuerySearchInstance>) oSearchHistory;
	} else {
	  this.llSearchHistory = new LinkedList<QuerySearchInstance>();
	}

	this.initialiseUI();
	this.updateFavouriteSearches();
	this.updateSearchHistory();

	// initialise the search engine
	vCurrentSearchThreadID = new Vector<Long>(1);
	vCurrentSearchThreadID.add(null); // this is just a placeholder, so that it's possible to update this value instead of adding new ones later
	this.searchEngine = new SearchEngine(vCurrentSearchThreadID, false, jpSearchResults, pluginMainComponent, myExperimentClient, logger);

	SwingUtilities.invokeLater(new Runnable() {
	  public void run() {
		// THIS MIGHT NOT BE NEEDED AS THE SEARCH OPTIONS BOX NOW
		// SETS THE MINIMUM SIZE OF THE SIDEBAR PROPERLY
		spMainSplitPane.setDividerLocation(390);
		spMainSplitPane.setOneTouchExpandable(true);
		spMainSplitPane.setDoubleBuffered(true);
	  }
	});
  }

  private void initialiseUI() {
	// create search options panel
	jpSearchOptions = new SearchOptionsPanel(this, this.pluginMainComponent, this.myExperimentClient, this.logger);
	jpSearchOptions.setMaximumSize(new Dimension(1024, 0)); // HACK: this is to make sure that search options box won't be stretched

	// create favourite searches panel
	jpFavouriteSearches = new JPanel();
	jpFavouriteSearches.setMaximumSize(new Dimension(1024, 0)); // HACK: this is to make sure that favourite searches box won't be stretched
	jpFavouriteSearches.setLayout(new GridBagLayout());
	jpFavouriteSearches.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Favourite Searches "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));

	// create search history panel
	jpSearchHistory = new JPanel();
	jpSearchHistory.setMaximumSize(new Dimension(1024, 0)); // HACK: this is to make sure that search history box won't be stretched
	jpSearchHistory.setLayout(new GridBagLayout());
	jpSearchHistory.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Search History "), BorderFactory.createEmptyBorder(0, 5, 5, 5)));

	// create the search sidebar
	JPanel jpSearchSidebar = new JPanel();
	jpSearchSidebar.setLayout(new GridBagLayout());

	GridBagConstraints gbConstraints = new GridBagConstraints();
	gbConstraints.anchor = GridBagConstraints.NORTHWEST;
	gbConstraints.fill = GridBagConstraints.BOTH;
	gbConstraints.weightx = 1;
	gbConstraints.gridx = 0;

	gbConstraints.gridy = 0;
	jpSearchSidebar.add(jpSearchOptions, gbConstraints);

	gbConstraints.gridy = 1;
	jpSearchSidebar.add(jpFavouriteSearches, gbConstraints);

	gbConstraints.gridy = 2;
	jpSearchSidebar.add(jpSearchHistory, gbConstraints);

	JPanel jpSidebarContainer = new JPanel();
	jpSidebarContainer.setLayout(new BorderLayout());
	jpSidebarContainer.add(jpSearchSidebar, BorderLayout.NORTH);

	// wrap sidebar in a scroll pane
	JScrollPane spSearchSidebar = new JScrollPane(jpSidebarContainer);
	spSearchSidebar.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);
	spSearchSidebar.setMinimumSize(new Dimension(jpSidebarContainer.getMinimumSize().width + 20, 0));

	// create panel for search results
	this.jpSearchResults = new SearchResultsPanel(this, pluginMainComponent, myExperimentClient, logger);

	spMainSplitPane = new JSplitPane();
	spMainSplitPane.setLeftComponent(spSearchSidebar);
	spMainSplitPane.setRightComponent(jpSearchResults);

	// PUT EVERYTHING TOGETHER
	this.setLayout(new BorderLayout());
	this.add(spMainSplitPane);
  }

  private void addToSearchListQueue(LinkedList<QuerySearchInstance> searchInstanceList, QuerySearchInstance searchInstanceToAdd, int queueSize) {
	// check if such entry is already in the list
	int iDuplicateIdx = searchInstanceList.indexOf(searchInstanceToAdd);

	// only do the following if the new search instance list OR current instance is not the same
	// as the last one in the list
	if (searchInstanceList.size() == 0
		|| iDuplicateIdx != searchInstanceList.size() - 1) {
	  // if the current item is already in the list, remove it (then re-add at the end of the list)
	  if (iDuplicateIdx >= 0)
		searchInstanceList.remove(iDuplicateIdx);

	  // we want to keep the history size constant, therefore when it reaches a certain
	  // size, oldest element needs to be removed
	  if (searchInstanceList.size() >= queueSize)
		searchInstanceList.remove();

	  // in either case, add the new element to the tail of the search history
	  searchInstanceList.offer(searchInstanceToAdd);
	}
  }

  private void addToFavouriteSearches(QuerySearchInstance searchInstance) {
	this.addToSearchListQueue(this.llFavouriteSearches, searchInstance, SEARCH_FAVOURITES_LENGTH);
	Collections.sort(this.llFavouriteSearches);
  }

  // the method to update search history listing
  protected void updateFavouriteSearches() {
	this.jpFavouriteSearches.removeAll();

	if (this.llFavouriteSearches.size() == 0) {
	  GridBagConstraints c = new GridBagConstraints();
	  c.weightx = 1.0;
	  c.anchor = GridBagConstraints.WEST;
	  this.jpFavouriteSearches.add(Util.generateNoneTextLabel("No favourite searches"), c);
	} else {
	  for (int i = this.llFavouriteSearches.size() - 1; i >= 0; i--) {
		addEntryToSearchListingPanel(this.llFavouriteSearches, i, SEARCH_FROM_FAVOURITES, this.jpFavouriteSearches, this.iconRemove, REMOVE_FAVOURITE_SEARCH_INSTANCE, "<html>Click to remove from your local favourite searches.<br>"
			+ "(This will not affect your myExperiment profile settings.)</html>");
	  }
	}

	this.jpFavouriteSearches.repaint();
	this.jpFavouriteSearches.revalidate();
  }

  private void addToSearchHistory(QuerySearchInstance searchInstance) {
	this.addToSearchListQueue(this.llSearchHistory, searchInstance, SEARCH_HISTORY_LENGTH);
  }

  // the method to update search history listing
  protected void updateSearchHistory() {
	this.jpSearchHistory.removeAll();

	if (this.llSearchHistory.size() == 0) {
	  GridBagConstraints c = new GridBagConstraints();
	  c.weightx = 1.0;
	  c.anchor = GridBagConstraints.WEST;
	  this.jpSearchHistory.add(Util.generateNoneTextLabel(SearchResultsPanel.NO_SEARCHES_STATUS), c);
	} else {
	  for (int i = this.llSearchHistory.size() - 1; i >= 0; i--) {
		addEntryToSearchListingPanel(this.llSearchHistory, i, SEARCH_FROM_HISTORY, this.jpSearchHistory, this.iconFavourite, ADD_FAVOURITE_SEARCH_INSTANCE, "<html>Click to add to your local favourite"
			+ " searches - these will be available every time you use Taverna.<br>(This will not affect your"
			+ " myExperiment profile settings.)</html>");
	  }
	}

	this.jpSearchHistory.repaint();
	this.jpSearchHistory.revalidate();

	// also update search history in History tab
	if (this.pluginMainComponent.getHistoryBrowser() != null) {
	  this.pluginMainComponent.getHistoryBrowser().refreshSearchHistory();
	}
  }

  private void addEntryToSearchListingPanel(List<QuerySearchInstance> searchInstanceList, int iIndex, String searchAction, JPanel panelToPopulate, ImageIcon entryIcon, String iconAction, String iconActionTooltip) {
	// labels with search query and search settings
	JClickableLabel jclCurrentEntryLabel = new JClickableLabel(searchInstanceList.get(iIndex).getSearchQuery(), searchAction
		+ ":" + iIndex, this, WorkbenchIcons.findIcon, SwingUtilities.LEFT, searchInstanceList.get(iIndex).toString());
	JLabel jlCurrentEntrySettings = new JLabel(searchInstanceList.get(iIndex).detailsAsString());
	jlCurrentEntrySettings.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

	// grouping search details and search settings together
	JPanel jpCurentEntryDetails = new JPanel();
	jpCurentEntryDetails.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	c.anchor = GridBagConstraints.WEST;
	jpCurentEntryDetails.add(jclCurrentEntryLabel, c);
	c.weightx = 1.0;
	jpCurentEntryDetails.add(jlCurrentEntrySettings, c);

	// creating a "button" to add current item to favourites
	JClickableLabel jclFavourite = new JClickableLabel("", iconAction + ":"
		+ iIndex, this, entryIcon, SwingUtilities.LEFT, iconActionTooltip);
	jclFavourite.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

	// putting all pieces of current item together
	JPanel jpCurrentEntry = new JPanel();
	jpCurrentEntry.setLayout(new GridBagLayout());

	c.anchor = GridBagConstraints.WEST;
	c.weightx = 1.0;
	jpCurrentEntry.add(jpCurentEntryDetails, c);

	c.anchor = GridBagConstraints.WEST;
	c.weightx = 0;
	jpCurrentEntry.add(jclFavourite, c);

	// adding current item to the history list 
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 1.0;
	c.gridx = 0;
	c.gridy = GridBagConstraints.RELATIVE;
	panelToPopulate.add(jpCurrentEntry, c);
  }

  public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.jpSearchOptions.bSearch)) {
	  // "Search" button was clicked

	  // if no search query is specified, display error message
	  if (jpSearchOptions.getSearchQuery().length() == 0) {
		javax.swing.JOptionPane.showMessageDialog(null, "Search query is empty. Please specify your search query and try again.", "Error", JOptionPane.WARNING_MESSAGE);
		jpSearchOptions.focusSearchQueryField();
	  } else {
		// will ensure that if the value in the search result limit editor
		// is invalid, it is processed properly
		try {
		  this.jpSearchOptions.jsResultLimit.commitEdit();
		} catch (ParseException ex) {
		  JOptionPane.showMessageDialog(null, "Invalid search result limit value. This should be an\n"
			  + "integer in the range of "
			  + SearchOptionsPanel.SEARCH_RESULT_LIMIT_MIN
			  + ".."
			  + SearchOptionsPanel.SEARCH_RESULT_LIMIT_MAX, "MyExperiment Plugin - Error", JOptionPane.WARNING_MESSAGE);
		  this.jpSearchOptions.tfResultLimitTextField.selectAll();
		  this.jpSearchOptions.tfResultLimitTextField.requestFocusInWindow();
		  return;
		}

		// all fine, settings present - store the settings..
		siPreviousSearch = new SearchEngine.QuerySearchInstance(jpSearchOptions.getSearchQuery(), jpSearchOptions.getResultCountLimit(), jpSearchOptions.getSearchWorkflows(), jpSearchOptions.getSearchFiles(), jpSearchOptions.getSearchPacks(), jpSearchOptions.getSearchUsers(), jpSearchOptions.getSearchGroups());

		// .. and execute the query
		this.jpSearchOptions.focusSearchQueryField();
		this.runSearch();
	  }
	} else if (e.getSource() instanceof JClickableLabel) {
	  if (e.getActionCommand().startsWith(SEARCH_FROM_HISTORY)
		  || e.getActionCommand().startsWith(SEARCH_FROM_FAVOURITES)) {
		// the part of the action command that is following the prefix is the ID in the search history / favourites storage;
		// this search instance is removed from history and will be re-added at the top of it when search is launched 
		int iEntryID = Integer.parseInt(e.getActionCommand().substring(e.getActionCommand().indexOf(":") + 1));
		final QuerySearchInstance si = (e.getActionCommand().startsWith(SEARCH_FROM_HISTORY) ? this.llSearchHistory.remove(iEntryID) : this.llFavouriteSearches.get(iEntryID)); // in case of favourites, no need to remove the entry

		// re-set search options in the settings box and re-run the search
		SwingUtilities.invokeLater(new Runnable() {
		  public void run() {
			jpSearchOptions.setSearchQuery(si.getSearchQuery());
			jpSearchOptions.setSearchAllResourceTypes(false); // reset the 'all resource types'
			jpSearchOptions.setSearchWorkflows(si.getSearchWorkflows());
			jpSearchOptions.setSearchFiles(si.getSearchFiles());
			jpSearchOptions.setSearchPacks(si.getSearchPacks());
			jpSearchOptions.setSearchUsers(si.getSearchUsers());
			jpSearchOptions.setSearchGroups(si.getSearchGroups());
			jpSearchOptions.setResultCountLimit(si.getResultCountLimit());

			// set this as the 'latest' search
			siPreviousSearch = si;

			// run the search (and update the search history)
			runSearch();
		  }
		});
	  } else if (e.getActionCommand().startsWith(ADD_FAVOURITE_SEARCH_INSTANCE)) {
		// get the ID of the entry in the history listing first; then fetch the instance itself
		int iHistID = Integer.parseInt(e.getActionCommand().substring(e.getActionCommand().indexOf(":") + 1));
		final QuerySearchInstance si = this.llSearchHistory.get(iHistID);

		// add item to favourites and re-draw the panel
		SwingUtilities.invokeLater(new Runnable() {
		  public void run() {
			addToFavouriteSearches(si);
			updateFavouriteSearches();
		  }
		});
	  } else if (e.getActionCommand().startsWith(REMOVE_FAVOURITE_SEARCH_INSTANCE)) {
		// get the ID of the entry in the favourite searches listing first; then remove the instance with that ID from the list
		int iFavouriteID = Integer.parseInt(e.getActionCommand().substring(e.getActionCommand().indexOf(":") + 1));
		this.llFavouriteSearches.remove(iFavouriteID);

		// item removed from favourites - re-draw the panel now
		SwingUtilities.invokeLater(new Runnable() {
		  public void run() {
			updateFavouriteSearches();
		  }
		});
	  }
	} else if (e.getSource().equals(this.jpSearchResults.bRefresh)) {
	  // "Refresh" button clicked; disable clearing results and re-run previous search
	  this.jpSearchResults.bClear.setEnabled(false);
	  this.rerunLastSearch();
	} else if (e.getSource().equals(this.jpSearchResults.bClear)) {
	  // "Clear" button clicked - clear last search and disable re-running it
	  siPreviousSearch = null;
	  vCurrentSearchThreadID.set(0, null);
	  this.jpSearchResults.clear();
	  this.jpSearchResults.setStatus(SearchResultsPanel.NO_SEARCHES_STATUS);
	  this.jpSearchResults.bClear.setEnabled(false);
	  this.jpSearchResults.bRefresh.setEnabled(false);
	}
  }

  // search history will get updated by default
  private void runSearch() {
	runSearch(true);
  }

  // makes preparations and runs the current search 
  // (has an option not to update the search history)
  private void runSearch(boolean bUpdateHistory) {
	if (bUpdateHistory) {
	  // add current search to search history ..
	  this.addToSearchHistory(siPreviousSearch);

	  // .. update the search history box ..
	  SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		  updateSearchHistory();
		}
	  });
	}

	// ..and run the query
	this.searchEngine.searchAndPopulateResults(siPreviousSearch);
  }

  // re-executes the search for the most recent query
  // (if searches have already been done before or were not cleared)
  public void rerunLastSearch() {
	if (this.siPreviousSearch != null) {
	  runSearch();
	}
  }

  public SearchResultsPanel getSearchResultPanel() {
	return (this.jpSearchResults);
  }

  public LinkedList<QuerySearchInstance> getSearchFavouritesList() {
	return (this.llFavouriteSearches);
  }

  public LinkedList<QuerySearchInstance> getSearchHistory() {
	return (this.llSearchHistory);
  }

}
