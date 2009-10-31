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
package net.sf.taverna.t2.ui.perspectives.myexperiment.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.ui.perspectives.myexperiment.MainComponent;
import net.sf.taverna.t2.ui.perspectives.myexperiment.SearchResultsPanel;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Sergejs Aleksejevs
 */
public class SearchEngine {
  // holds a reference to the instance of the search thread in the current context
  // that should be active at the moment (will aid early termination of older searches
  // when new ones are started)
  private Vector<Long> vCurrentSearchThreadID;

  private boolean bSearchByTag; // indicates what kind of search this is
  private String strSearchQuery; // stores the actual query

  private SearchResultsPanel jpResultsPanel;
  private MainComponent pluginMainComponent;
  private MyExperimentClient myExperimentClient;
  private Logger logger;

  /**
   * Creates an instance of a search engine that is aware of the current context
   * of the search.
   */
  public SearchEngine(Vector<Long> currentSearchThreadIDVector, boolean bSearchByTag, SearchResultsPanel resultsPanel, MainComponent pluginMainComponent, MyExperimentClient client, Logger logger) {
	super();

	this.vCurrentSearchThreadID = currentSearchThreadIDVector;
	this.bSearchByTag = bSearchByTag;

	this.jpResultsPanel = resultsPanel;
	this.pluginMainComponent = pluginMainComponent;
	this.myExperimentClient = client;
	this.logger = logger;
  }

  public void searchAndPopulateResults(String tagQuery) {
	this.strSearchQuery = tagQuery;

	new Thread("Search by tag") {
	  public void run() {
		// Record 'this' search thread and set it as the new "primary" one
		// (this way it if a new search thread starts afterwards, it is possible to
		//  detect this and stop the 'older' search, because it is no longer relevant)
		long lThisSearchThreadID = Thread.currentThread().getId();
		vCurrentSearchThreadID.set(0, lThisSearchThreadID);

		// set the status in the status bar
		pluginMainComponent.getStatusBar().setStatus(pluginMainComponent.getTagBrowserTab().getClass().getName(), "Searching");

		// get the search query (which is the tag name in this case)
		strSearchQuery = Tag.instantiateTagFromActionCommand(strSearchQuery).getTagName();
		jpResultsPanel.setCurrentSearchTerm(strSearchQuery);
		jpResultsPanel.setStatus("Starting to search for a tag '"
			+ strSearchQuery + "'...");

		// Execute the search query
		Document doc = myExperimentClient.searchByTag(strSearchQuery);

		if (doc == null) {
		  logger.error("Failed while fetching tagged items. See the error message above. Can't continue.");
		  javax.swing.JOptionPane.showMessageDialog(null, "An error occurred while searching for the results of your query. Please try again.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
		} else {
		  // search was successful, get tagged items' collection
		  Element root = doc.getRootElement();
		  processSearchResults(lThisSearchThreadID, root.getChildren());
		}
	  }
	}.start();
  }

  public void searchAndPopulateResults(QuerySearchInstance querySearchDetails) {
	final QuerySearchInstance searchQuery = querySearchDetails;
	this.strSearchQuery = searchQuery.getSearchQuery();

	new Thread("Search by query") {
	  public void run() {
		// Record 'this' search thread and set it as the new "primary" one
		// (this way it if a new search thread starts afterwards, it is possible to
		//  detect this and stop the 'older' search, because it is no longer relevant)
		long lThisSearchThreadID = Thread.currentThread().getId();
		vCurrentSearchThreadID.set(0, lThisSearchThreadID);

		// set the status in the status bar
		pluginMainComponent.getStatusBar().setStatus(pluginMainComponent.getSearchTab().getClass().getName(), "Searching");

		// strip out the leading "search:"
		strSearchQuery = strSearchQuery.replaceFirst("search:", "");
		jpResultsPanel.setCurrentSearchTerm(strSearchQuery);
		jpResultsPanel.setStatus("Starting to search for '" + strSearchQuery
			+ "'...");

		// Execute the search query
		Document doc = myExperimentClient.searchByQuery(searchQuery);

		if (doc == null) {
		  logger.error("Failed while fetching search result XML document. See the error message above. Can't continue.");
		  javax.swing.JOptionPane.showMessageDialog(null, "An error occurred while searching for the results of your query. Please try again.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
		} else {
		  // search was successful, get found items' collection
		  Element root = doc.getRootElement();
		  processSearchResults(lThisSearchThreadID, root.getChildren());
		}
	  }
	}.start();
  }

  /**
   * Generic worker method to process search results from both search by tags
   * and search by query.
   * 
   * @param foundSearchItemElements
   *          List of Elements that comprise the search results.
   */
  private void processSearchResults(Long thisSearchThreadID, List<Element> foundSearchItemElements) {
	// prepare variables
	final Long lThisSearchThreadID = thisSearchThreadID;
	final List<Element> lFoundSearchItemElements = foundSearchItemElements;
	final HashMap<Integer, ArrayList<Resource>> hmClassifiedResults = new HashMap<Integer, ArrayList<Resource>>();

	// iterate through all found items, fetch more data about each and classify
	// the results by resource type
	for (Element foundItem : foundSearchItemElements) {
	  // check if the current thread is still the active one (that is if a new search
	  // thread hasn't been started yet - if the new search has been started, the
	  // current one should terminate)
	  if (!lThisSearchThreadID.equals(vCurrentSearchThreadID.get(0)))
		return;

	  // PROCESS THE NEXT ELEMENT
	  // data required for generating full listing preview was fetched along with the list of items -
	  // so now can build the item directly from the received data
	  Resource res = Resource.buildFromXML(foundItem, myExperimentClient, logger);

	  // check if this is a new type of resource - if so, create new ArrayList for these
	  if (!hmClassifiedResults.containsKey(res.getItemType())) {
		hmClassifiedResults.put(res.getItemType(), new ArrayList<Resource>());
	  }

	  // add the resource into the result set
	  hmClassifiedResults.get(res.getItemType()).add(res);
	}

	// populate the results when everything's done
	// (only make a final check to make sure that the "top" search thread is still the current one -
	//  otherwise, it's not necessary to make any rendering of unwanted results)
	if (lThisSearchThreadID.equals(vCurrentSearchThreadID.get(0))) {
	  SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		  jpResultsPanel.setStatus(lFoundSearchItemElements.size()
			  + " items found " + (bSearchByTag ? "with tag '" : "by query '")
			  + jpResultsPanel.getCurrentSearchTerm() + "'");
		  jpResultsPanel.setSearchResultsData(hmClassifiedResults);
		  jpResultsPanel.refresh();

		  // if this search thread is displaying results, then this was the last search thread - can display
		  // "ready" status in the status bar now
		  JComponent jcTabForWhichToSetStatus = (bSearchByTag ? pluginMainComponent.getTagBrowserTab() : pluginMainComponent.getSearchTab());
		  pluginMainComponent.getStatusBar().setStatus(jcTabForWhichToSetStatus.getClass().getName(), null);
		}
	  });
	}
  }

  /**
   * Class to hold settings for a query search. This will then be used to re-run
   * a search instance at a later time.
   */
  public static class QuerySearchInstance implements Comparable<QuerySearchInstance>, Serializable {
	private String strSearchQuery;
	private int iResultCountLimit;
	private boolean bSearchWorkflows;
	private boolean bSearchFiles;
	private boolean bSearchPacks;
	private boolean bSearchUsers;
	private boolean bSearchGroups;

	// constructor
	public QuerySearchInstance(String searchQuery, int resultCountLimit, boolean searchWorkflows, boolean searchFiles, boolean searchPacks, boolean searchUsers, boolean searchGroups) {
	  this.strSearchQuery = searchQuery;
	  this.iResultCountLimit = resultCountLimit;
	  this.bSearchWorkflows = searchWorkflows;
	  this.bSearchFiles = searchFiles;
	  this.bSearchPacks = searchPacks;
	  this.bSearchUsers = searchUsers;
	  this.bSearchGroups = searchGroups;
	}

	// determines whether the two search instances are identical
	public boolean equals(Object other) {
	  if (other instanceof QuerySearchInstance) {
		QuerySearchInstance si = (QuerySearchInstance) other;

		return (this.strSearchQuery.equals(si.getSearchQuery())
			&& this.iResultCountLimit == si.getResultCountLimit()
			&& this.bSearchWorkflows == si.getSearchWorkflows()
			&& this.bSearchFiles == si.getSearchFiles()
			&& this.bSearchPacks == si.getSearchPacks()
			&& this.bSearchUsers == si.getSearchUsers() && this.bSearchGroups == si.getSearchGroups());
	  } else
		return (false);
	}

	public int compareTo(QuerySearchInstance other) {
	  if (this.equals(other))
		return (0);
	  else {
		// this will return results in the descending order - which is
		// fine, because the way this collection will be rendered will
		// eventually traverse it from the rear end first; so results
		// will be shown alphabetically
		return (-1 * this.toString().compareTo(other.toString()));
	  }
	}

	public String toString() {
	  return ("Search: '" + this.strSearchQuery + "' " + this.detailsAsString());
	}

	public String detailsAsString() {
	  String str = "";

	  // output types that were searched for
	  int iCnt = 0;
	  if (this.bSearchWorkflows) {
		str += "workflows, ";
		iCnt++;
	  }
	  if (this.bSearchFiles) {
		str += "files, ";
		iCnt++;
	  }
	  if (this.bSearchPacks) {
		str += "packs, ";
		iCnt++;
	  }
	  if (this.bSearchUsers) {
		str += "users, ";
		iCnt++;
	  }
	  if (this.bSearchGroups) {
		str += "groups, ";
		iCnt++;
	  }

	  // if that's all types, have just the word 'all'
	  if (iCnt == 5)
		str = "all";
	  else
		str = str.substring(0, str.length() - 2); // remove trailing ", "

	  // add the rest to the string representation of the search instance
	  str = "[" + str + "; limit: " + this.iResultCountLimit + "]";

	  return (str);
	}

	public String getSearchQuery() {
	  return (this.strSearchQuery);
	}

	public int getResultCountLimit() {
	  return (this.iResultCountLimit);
	}

	public boolean getSearchWorkflows() {
	  return (this.bSearchWorkflows);
	}

	public boolean getSearchFiles() {
	  return (this.bSearchFiles);
	}

	public boolean getSearchPacks() {
	  return (this.bSearchPacks);
	}

	public boolean getSearchUsers() {
	  return (this.bSearchUsers);
	}

	public boolean getSearchGroups() {
	  return (this.bSearchGroups);
	}
  }
}
