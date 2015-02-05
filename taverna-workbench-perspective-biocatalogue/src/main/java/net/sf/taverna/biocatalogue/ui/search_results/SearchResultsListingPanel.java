package net.sf.taverna.biocatalogue.ui.search_results;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.LoadingExpandedResource;
import net.sf.taverna.biocatalogue.model.LoadingResource;
import net.sf.taverna.biocatalogue.model.Resource;
import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.Util;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.model.search.SearchInstance;
import net.sf.taverna.biocatalogue.ui.JWaitDialog;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.Integration;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check.ServiceHealthChecker;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.ui.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

import org.apache.log4j.Logger;
import org.biocatalogue.x2009.xml.rest.ResourceLink;
import org.biocatalogue.x2009.xml.rest.RestMethod;
import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.ServiceTechnologyType;

/**
 * This class is responsible for producing search results listing panel. It only
 * shows a single listing for a specified type. Multiple types are handled by
 * having different tabs in {@link SearchResultsMainPanel} with instances of
 * this class in each.
 * 
 * @author Sergejs Aleksejevs
 */
@SuppressWarnings("serial")
public class SearchResultsListingPanel extends JPanel implements MouseListener,
		SearchResultsRenderer, MouseMotionListener {
	public static final int SEARCH_STATUS_TOOLTIP_LINE_LENGTH = 65;

	private static final Logger logger = Logger.getLogger(SearchResultsListingPanel.class);
	private final SearchResultsMainPanel parentMainSearchResultsPanel;

	// currently displayed search results
	SearchInstance searchInstance;

	// main UI components
	private SearchResultsListingPanel thisPanel;
	private DefaultListModel resultsListingModel;
	private JList jlResultsListing;
	private JScrollPane spResultsListing;

	// contextual menu
	private JPopupMenu contextualMenu;
	private Action addToServicePanelAction;
	private Action addToWorkflowDiagramAction;
	private Action openInBioCatalogueAction;
	private Action doHealthCheckAction;
	private Action addAllOperationsToServicePanelAction;

	// search status and actions on selected items in the list
	private JToolBar tbSelectedItemActions;
	protected JPanel jpSearchStatus;
	private JLabel jlSearchStatus;

	// this is used for previewing items from the result listing through
	// contextual menu -
	// value will be updated by mouse event accordingly
	private ResourceLink potentialObjectToPreview;
	private final TYPE typeToPreview;

	// Design perspective - some actions require switching to it
	PerspectiveSPI designPerspective;

	private ListCellRenderer listingCellRenderer;
	
	/**
	 * @param typeToPreview
	 *            Resource type that will be previewed in this panel.
	 * @param parentMainSearchResultsPanel
	 *            Reference to a "parent" of this panel - this is needed to
	 *            notify the main results panel with the
	 */
	public SearchResultsListingPanel(TYPE typeToPreview,
			SearchResultsMainPanel parentMainSearchResultsPanel) {
		this.thisPanel = this;

		this.typeToPreview = typeToPreview;
		listingCellRenderer = this.typeToPreview
		.getResultListingCellRenderer();
		this.parentMainSearchResultsPanel = parentMainSearchResultsPanel;
		MainComponentFactory
				.getSharedInstance();

		initialiseUI();

		this.setPreferredSize(new Dimension(800, 400));
	}

	private void initialiseUI() {

		this.addToServicePanelAction = new AbstractAction(
				"Add to Service Panel",
				ResourceManager
						.getImageIcon(ResourceManager.ADD_PROCESSOR_AS_FAVOURITE_ICON)) {
			// Tooltip
			{
				this.putValue(SHORT_DESCRIPTION, "Add selected "
						+ typeToPreview.getTypeName()
						+ " to the Service Panel");
			}

			public void actionPerformed(ActionEvent e) {
				final JWaitDialog jwd = new JWaitDialog(
						MainComponent.dummyOwnerJFrame,
						"Service Catalogue Plugin - Adding "
								+ typeToPreview.getTypeName(),
						"<html><center>Please wait for selected "
								+ typeToPreview.getTypeName()
								+ " details to be fetched from the Service Catalogue<br>"
								+ "and to be added into the Service Panel.</center></html>");

				new Thread("Adding " + typeToPreview.getTypeName()
						+ " into Service Panel") {
					public void run() {
						// if it is the expanded that we are looking at, need to extract
						// the 'associated' object
						ResourceLink processorResourceToAdd = (potentialObjectToPreview instanceof LoadingExpandedResource ? ((LoadingExpandedResource) potentialObjectToPreview)
								.getAssociatedObj()
								: potentialObjectToPreview);

						JComponent insertionOutcome = Integration
								.insertProcesorIntoServicePanel(processorResourceToAdd);
						jwd.waitFinished(insertionOutcome);
						
						// Switch to Design Perspective
						switchToDesignPerspective();
					}
				}.start();

				// NB! The modal dialog window needs to be made visible after
				// the background
				// process (i.e. adding a processor) has already been started!
				jwd.setVisible(true);
			}
		};

		// For a parent Web service, action to add all operations to the Service Panel.
		// Works for SOAP services at the moment.
		this.addAllOperationsToServicePanelAction = new AbstractAction(
				"Add all operations to Service Panel",
				ResourceManager
						.getImageIcon(ResourceManager.ADD_ALL_SERVICES_AS_FAVOURITE_ICON)) {
			// Tooltip
			{
				this.putValue(SHORT_DESCRIPTION, "Add all associated services to the Service Panel");
			}

			public void actionPerformed(ActionEvent e) {
				final JWaitDialog jwd = new JWaitDialog(
						MainComponent.dummyOwnerJFrame,
						"Service Catalogue Plugin - Adding "
								+ typeToPreview.getTypeName(),
						"<html><center>Please wait for selected "
								+ typeToPreview.getTypeName()
								+ " details to be fetched from the Service Catalogue<br>"
								+ "and to be added into the Service Panel.</center></html>");

				new Thread("Adding all operations of " + typeToPreview.getTypeName()
						+ " to the Service Panel") {
					public void run() {
						// if it is the expanded that we are looking at, need to extract
						// the 'associated' object
						ResourceLink resourceLink = (potentialObjectToPreview instanceof LoadingExpandedResource ? ((LoadingExpandedResource) potentialObjectToPreview)
								.getAssociatedObj()
								: potentialObjectToPreview);						

						JComponent insertionOutcome = Integration
								.insertAllOperationsIntoServicePanel(resourceLink);
						jwd.waitFinished(insertionOutcome);
						
						// Switch to Design Perspective
						switchToDesignPerspective();
					}
				}.start();

				// NB! The modal dialog window needs to be made visible after
				// the background
				// process (i.e. adding a processor) has already been started!
				jwd.setVisible(true);
			}
		};

		this.addToWorkflowDiagramAction = new AbstractAction(
				"Add to workflow",
				ResourceManager
						.getImageIcon(ResourceManager.ADD_PROCESSOR_TO_WORKFLOW_ICON)) {
			// Tooltip
			{
				this.putValue(SHORT_DESCRIPTION, "<html>Insert selected "
						+ typeToPreview.getTypeName()
						+ " into the current workflow</html>");
			}

			public void actionPerformed(ActionEvent e) {
				final JWaitDialog jwd = new JWaitDialog(
						MainComponent.dummyOwnerJFrame,
						"Service Catalogue Plugin - Adding "
								+ typeToPreview.getTypeName(),
						"<html><center>Please wait for selected "
								+ typeToPreview.getTypeName()
								+ " details to be fetched from the Service Catalogue<br>"
								+ "and to be added into the current workflow.</center></html>");

				new Thread("Adding " + typeToPreview.getTypeName()
						+ " into workflow") {
					public void run() {
						// if it is the expanded that we are looking at, need to extract
						// the 'associated' object
						ResourceLink processorResourceToAdd = (potentialObjectToPreview instanceof LoadingExpandedResource ? ((LoadingExpandedResource) potentialObjectToPreview)
								.getAssociatedObj()
								: potentialObjectToPreview);

						JComponent insertionOutcome = Integration
								.insertProcessorIntoCurrentWorkflow(processorResourceToAdd);
						jwd.waitFinished(insertionOutcome);

						// Switch to Design Perspective
						switchToDesignPerspective();
					}
				}.start();

				// NB! The modal dialog window needs to be made visible after
				// the background
				// process (i.e. adding a processor) has already been started!
				jwd.setVisible(true);
			}
		};

		this.openInBioCatalogueAction = new AbstractAction(
				"Open in the Service Catalogue",
				ResourceManager
						.getImageIcon(ResourceManager.OPEN_IN_BIOCATALOGUE_ICON)) {
			// Tooltip
			{
				this.putValue(SHORT_DESCRIPTION, "<html>View selected "
						+ typeToPreview.getTypeName()
						+ " on the Service Catalogue Web site.<br>"
						+ "This will open your standard Web browser.</html>");
			}

			public void actionPerformed(ActionEvent e) {
				String hrefString = potentialObjectToPreview.getHref();
				   try {
						Desktop.getDesktop().browse(new URI(hrefString));
					    }
					    catch (Exception ex) {
					      logger.error("Failed while trying to open the URL in a standard browser; URL was: " +
					           hrefString + "\nException was: " + ex + "\n" + ex.getStackTrace());
					    };
			}
		};

		this.doHealthCheckAction = new AbstractAction(
				"Check monitoring status",
				ResourceManager
						.getImageIcon(ResourceManager.EXECUTE_HEALTH_CHECK_ICON)) {
			// Tooltip
			{
				this
						.putValue(
								SHORT_DESCRIPTION,
								"<html>Fetch the latest monitoring data for selected "
										+ typeToPreview.getTypeName()
										+ ".<br>"
										+ "Data will be obtained from the Service Catalogue and displayed in a popup window.</html>");
			}

			public void actionPerformed(ActionEvent e) {
				// if it is the expanded that we are looking at, need to extract
				// the 'associated' object
				ResourceLink resourceForHealthCheck = (potentialObjectToPreview instanceof LoadingExpandedResource ? ((LoadingExpandedResource) potentialObjectToPreview)
						.getAssociatedObj()
						: potentialObjectToPreview);

				ServiceHealthChecker.checkResource(resourceForHealthCheck);
			}
		};

		tbSelectedItemActions = new JToolBar(JToolBar.HORIZONTAL);
		tbSelectedItemActions.setBorderPainted(true);
		tbSelectedItemActions.setBorder(BorderFactory.createEmptyBorder(5, 5,
				5, 3));
		tbSelectedItemActions.setFloatable(false);
		if (typeToPreview.isSuitableForAddingToServicePanel()) {
			tbSelectedItemActions.add(addToServicePanelAction);
		}
		if (typeToPreview.isSuitableForAddingAllToServicePanel()) {
			tbSelectedItemActions.add(addAllOperationsToServicePanelAction);
		}
		if (typeToPreview.isSuitableForAddingToWorkflowDiagram()) {
			tbSelectedItemActions.add(addToWorkflowDiagramAction);
		}
		if (typeToPreview.isSuitableForHealthCheck()) {
			tbSelectedItemActions.add(doHealthCheckAction);
		}
		tbSelectedItemActions.add(openInBioCatalogueAction);

		// *** Prepare search results status panel ***

		GridBagConstraints c = new GridBagConstraints();
		jpSearchStatus = new JPanel(new GridBagLayout());
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0;
		jpSearchStatus.add(tbSelectedItemActions, c);

		jlSearchStatus = new JLabel();
		jlSearchStatus.setIconTextGap(20);
		c.weightx = 1.0;
		c.insets = new Insets(0, 20, 0, 0);
		jpSearchStatus.add(jlSearchStatus, c);

		if (parentMainSearchResultsPanel.getFilterTreePaneFor(typeToPreview) != null) {
			Dimension preferredSize = new Dimension(200,
					parentMainSearchResultsPanel.getFilterTreePaneFor(
							typeToPreview).getTreeToolbarPreferredSize().height);

			// HACK: due to concurrency issues, sometimes this doesn't work
			// correctly -
			// to rectify the problem using the hard-coded value that was
			// correct at
			// the time of coding...
			if (preferredSize.height < 30) {
				preferredSize.height = 33;
			}

			jpSearchStatus.setPreferredSize(preferredSize);
		}

		// *** Create list to hold search results and wrap it into a scroll pane
		// ***
		resultsListingModel = new DefaultListModel();
		jlResultsListing = new JList(resultsListingModel);
		jlResultsListing.setDoubleBuffered(true);
		jlResultsListing.setCellRenderer(listingCellRenderer);
		jlResultsListing.addMouseListener(this);
		jlResultsListing.addMouseMotionListener(this);
		jlResultsListing.setBackground(thisPanel.getBackground());

		jlResultsListing.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlResultsListing.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					// update value to be used in contextual menu click handler
					// to act on the just-selected entry
					potentialObjectToPreview = getResourceSelectedInJList();

					if (potentialObjectToPreview != null) {

						// only enable actions in the menu if the list entry
						// that is being
						// clicked on is beyond the initial 'loading' state
						boolean shown = !isListEntryOnlyWithInitialDetails(potentialObjectToPreview);
						boolean shownAndNotArchived = shown && !isArchived(potentialObjectToPreview);
						addToServicePanelAction
								.setEnabled(shownAndNotArchived);
						addAllOperationsToServicePanelAction
						.setEnabled(shownAndNotArchived && !(potentialObjectToPreview instanceof RestMethod));
						addToWorkflowDiagramAction
								.setEnabled(shownAndNotArchived);
						openInBioCatalogueAction
								.setEnabled(shown);
						doHealthCheckAction
								.setEnabled(shown);
					    
						return;
					}
				}

				// disable actions if nothing is selected in the list or if
				// selection is still "adjusting"
				addToServicePanelAction.setEnabled(false);
				addAllOperationsToServicePanelAction.setEnabled(false);
				addToWorkflowDiagramAction.setEnabled(false);
				openInBioCatalogueAction.setEnabled(false);
				doHealthCheckAction.setEnabled(false);
			}
		});

		spResultsListing = new JScrollPane(jlResultsListing);
		spResultsListing.getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					public void adjustmentValueChanged(AdjustmentEvent e) {
						if (!e.getValueIsAdjusting()) {
							// load missing details on adjusting the scroll bar
							//
							// only start loading more results in case if the
							// value is "not adjusting" -
							// this means that the mouse has been released and
							// is not dragging the scroll bar
							// any more, so effectively the user has stopped
							// scrolling
							checkAllEntriesInTheVisiblePartOfJListAreLoaded();
						}
					}
				});

		// tie components to the class panel itself
		this.resetSearchResultsListing(true);

		// *** Create CONTEXTUAL MENU ***

		contextualMenu = new JPopupMenu();
		if (typeToPreview.isSuitableForAddingToServicePanel()) {
			contextualMenu.add(addToServicePanelAction);
			contextualMenu.add(addAllOperationsToServicePanelAction);
		}
		if (typeToPreview.isSuitableForAddingToWorkflowDiagram()) {
			contextualMenu.add(addToWorkflowDiagramAction);
		}
		if (typeToPreview.isSuitableForHealthCheck()) {
			contextualMenu.add(doHealthCheckAction);
		}
		contextualMenu.add(openInBioCatalogueAction);
	}

	/**
	 * Allows to set the search status by supplying the message to display.
	 */
	protected void setSearchStatusText(final String statusString,
			final boolean spinnerActive) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jlSearchStatus
						.setIcon(spinnerActive ? ResourceManager
								.getImageIcon(ResourceManager.BAR_LOADER_ORANGE)
								: null);

				jlSearchStatus.setText(statusString);
				jlSearchStatus.setToolTipText("<html>"
						+ Util.ensureLineLengthWithinString(statusString,
								SEARCH_STATUS_TOOLTIP_LINE_LENGTH, false)
						+ "</html>");
			}
		});
	}

	/**
	 * This helper method is used to initialise this panel. Also invoked when
	 * search results need to be cleared.
	 * 
	 * @param showSuggestion
	 *            <code>true</code> should be used on first load of the panel -
	 *            in that case a suggestion would be displayed to perform a
	 *            search, tag search or start directly with filtering;<br/>
	 *            <code>false</code> to be used when resetting the panel after
	 *            perfoming the search, but not finding any results.
	 */
	public void resetSearchResultsListing(boolean showSuggestion) {
		setSearchStatusText("No searches were made yet", false);

		String labelText = "<html><center>"
				+ (showSuggestion ? "You can find "
						+ this.typeToPreview.getCollectionName()
						+ " by typing a search query."
						+ (this.typeToPreview.isSuitableForFiltering() ? "<br><br>Alternatively, you can select some filters from the tree on the left."
								: "")
						: "There are no "
								+ this.typeToPreview.getCollectionName()
								+ " that match your search criteria<br><br>"
								+ "Please try making the search query shorter or selecting fewer filters")
				+ "</center></html>";

		JLabel jlMainLabel = new JLabel(labelText, JLabel.CENTER);
		jlMainLabel.setFont(jlMainLabel.getFont().deriveFont(Font.PLAIN, 16));
		jlMainLabel.setBorder(BorderFactory.createEtchedBorder());

		this.removeAll();
		this.setLayout(new BorderLayout(0, 0));
		this.add(jpSearchStatus, BorderLayout.NORTH);
		this.add(jlMainLabel, BorderLayout.CENTER);
		this.validate();

		// disable the toolbar actions
		this.addToServicePanelAction.setEnabled(false);
		this.addToWorkflowDiagramAction.setEnabled(false);
		this.openInBioCatalogueAction.setEnabled(false);
		this.doHealthCheckAction.setEnabled(false);
		this.addAllOperationsToServicePanelAction.setEnabled(false);
	}

	/**
	 * Statistics will be rendered along with the collection of found items.
	 * 
	 * @param searchInstance
	 *            SearchInstance containing search results to render.
	 */
	public void renderResults(SearchInstance searchInstance) {
		// make the current search instance available globally within this class
		this.searchInstance = searchInstance;

		// stop spinner icon on the tab that is populated and add number of
		// results
		parentMainSearchResultsPanel.setDefaultIconForTab(typeToPreview);
		parentMainSearchResultsPanel.setDefaultTitleForTabWithSuffix(
				typeToPreview, " ("
						+ searchInstance.getSearchResults()
								.getTotalMatchingItemCount() + ")");

		// if nothing was found - display notification and finish result
		// processing
		if (searchInstance.getSearchResults().getTotalMatchingItemCount() == 0) {
			resetSearchResultsListing(false);

			// must happen after resetting the listing, as it replaces the
			// default status text
			setSearchStatusText("No results found for "
					+ searchInstance.getDescriptionStringForSearchStatus(),
					false);
			return;
		}

		// populate results
		if (searchInstance.getSearchResults().getTotalMatchingItemCount() > 0) {
			// populate the list box with users

			List<? extends ResourceLink> foundItems = searchInstance
					.getSearchResults().getFoundItems();
			for (ResourceLink item : foundItems) {
				resultsListingModel.addElement(item);
			}
		}

		// update the UI once contents are ready
		thisPanel.removeAll();
		thisPanel.setLayout(new BorderLayout(0, 0));
		thisPanel.add(jpSearchStatus, BorderLayout.NORTH);
		thisPanel.add(spResultsListing, BorderLayout.CENTER);
		thisPanel.repaint();

		// automatically start loading details for the first section of result
		// listing
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				checkAllEntriesInTheVisiblePartOfJListAreLoaded();
			}
		});

		// *** Also update status text ***

		setSearchStatusText("Search results for "
				+ searchInstance.getDescriptionStringForSearchStatus(), false);
	}

	/**
	 * Check if details are fetched for all result entries that are currently
	 * visible in the JList.
	 * 
	 * If some are not yet loaded, identifies the page in the index of
	 * corresponding resources to fetch details.
	 * 
	 * When done, recursively calls itself again to verify that no more entries
	 * need further details loaded.
	 */
	private void checkAllEntriesInTheVisiblePartOfJListAreLoaded() {
		int firstVisibleIndex = jlResultsListing.getFirstVisibleIndex();

		if (firstVisibleIndex >= 0) {
			int lastVisibleIndex = jlResultsListing.getLastVisibleIndex();

			final int firstNotFetchedMatchingItemIndex = searchInstance
					.getSearchResults().getFirstMatchingItemIndexNotYetFetched(
							firstVisibleIndex, lastVisibleIndex);
			final int pageToFetchNumber = searchInstance.getSearchResults()
					.getMatchingItemPageNumberFor(
							firstNotFetchedMatchingItemIndex);

			// check if found a valid page to load
			if (pageToFetchNumber != -1) {
				int numberOfResourcesPerPageForThisResourceType = searchInstance
						.getSearchResults().getTypeOfResourcesInTheResultSet()
						.getApiResourceCountPerIndexPage();

				int firstListIndexToLoad = searchInstance.getSearchResults()
						.getFirstItemIndexOn(pageToFetchNumber); // first
																	// element
																	// on the
																	// page that
																	// is about
																	// to be
																	// loaded
				int countToLoad = Math.min(
						numberOfResourcesPerPageForThisResourceType, // if the
																		// last
																		// page
																		// isn't
																		// full,
																		// need
																		// to
																		// mark
																		// less
																		// items
																		// than
																		// the
																		// full
																		// page
						resultsListingModel.getSize() - firstListIndexToLoad);

				// mark the next "page" of items in the JList as "loading" -
				// but also mark them in the SearchResults backing list, so
				// that next calls to this listener are aware of the previous
				// items that were marked as "loading"
				for (int i = firstListIndexToLoad; i < firstListIndexToLoad
						+ countToLoad; i++) {
					((LoadingResource) searchInstance.getSearchResults()
							.getFoundItems().get(i)).setLoading(true);
				}

				// update the UI to show 'loading' state on relevant entries
				renderFurtherResults(searchInstance, firstListIndexToLoad,
						countToLoad);

				// now start loading data for the 'loading' entries
				final CountDownLatch latch = new CountDownLatch(1);
				new Thread("Search via the API") {
					public void run() {
						try {
							searchInstance.fetchMoreResults(
									parentMainSearchResultsPanel, latch,
									thisPanel, pageToFetchNumber);
						} catch (Exception e) {
							logger.error("Error while searching via the Service Catalogue API", e);

						}
					}
				}.start();

				// wait for the previous portion of results to load, then fetch
				// the next portion
				new Thread(
						"Fetch more another page of details for search results") {
					public void run() {
						try {
							latch.await();
							checkAllEntriesInTheVisiblePartOfJListAreLoaded();
						} catch (InterruptedException e) {
							logger
									.error(
											"Failed to wait for the previous page of results to load to check if "
													+ "another one needs loading as well. Details in the attache exception.",
											e);
						}
					}
				}.start();

			}
		}
	}

	/**
	 * Tests whether {@link ResourceLink} object corresponding to an entry in
	 * the search results list is in the state where only the first (initial)
	 * fragment of data was loaded (through BioCatalogue LITE JSON API) that
	 * contains just the title + URL of the resource.
	 * 
	 * @param resource
	 * @return
	 */
	private boolean isListEntryOnlyWithInitialDetails(ResourceLink resource) {
		return (resource instanceof LoadingResource);
	}
	
	private boolean isArchived(ResourceLink resource) {
		if (listingCellRenderer instanceof ExpandableOnDemandLoadedListCellRenderer) {
			ExpandableOnDemandLoadedListCellRenderer r = (ExpandableOnDemandLoadedListCellRenderer) listingCellRenderer;
			return r.shouldBeHidden(resource);
		}
		return false;
	}


	// ***** Callbacks for MouseListener *****

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) { /* NOT IN USE */
	}

	public void mouseExited(MouseEvent e) { /* NOT IN USE */
	}

	public void mousePressed(MouseEvent e) {
		// checked in both mousePressed() & mouseReleased() for cross-platform
		// operation
		maybeShowPopupMenu(e);
	}

	public void mouseReleased(MouseEvent e) {
		// checked in both mousePressed() & mouseReleased() for cross-platform
		// operation
		maybeShowPopupMenu(e);
	}

	// ***** Callbacks for MouseMotionListener *****

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) { /* do nothing */
	}

	/**
	 * Gets the selected object from the specified list. Used for previewing
	 * items through double-clicks and contextual menu.
	 * 
	 * @return <code>null</code> if no selection in the list,
	 *         <code>ResourceLink</code> object that is currently selected
	 *         otherwise.
	 */
	private ResourceLink getResourceSelectedInJList() {
		return (jlResultsListing.getSelectedIndex() == -1 ? null
				: (ResourceLink) jlResultsListing.getSelectedValue());
	}

	private void maybeShowPopupMenu(MouseEvent e) {
		if (e.getSource().equals(jlResultsListing) && e.isPopupTrigger()
				&& jlResultsListing.locationToIndex(e.getPoint()) != -1) {
			// select the entry in the list that triggered the event to show
			// this popup menu
			jlResultsListing.setSelectedIndex(jlResultsListing
					.locationToIndex(e.getPoint()));

			// update value to be used in contextual menu click handler to act
			// on the just-selected entry
			potentialObjectToPreview = getResourceSelectedInJList();

			// show the contextual menu
			this.contextualMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	// *** Callbacks for SearchResultsRenderer ***

	public void renderInitialResults(final SearchInstance si) {
		// NB! critical to have UI update done within the invokeLater()
		// method - this is to prevent UI from 'flashing' and to
		// avoid concurrency-related errors
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// make sure to remove any old results from the list model!
				resultsListingModel.clear();

				// display the partial search results
				logger.debug("Started rendering initial search results for "
						+ si.getResourceTypeToSearchFor().getCollectionName());
				renderResults(si);
				logger.debug("Finished rendering initial search results for "
						+ si.getResourceTypeToSearchFor().getCollectionName());
			}
		});
	}

	public void renderFurtherResults(SearchInstance si, int startIndex,
			int count) {
		renderFurtherResults(si, startIndex, count, false);
	}

	public void renderFurtherResults(final SearchInstance si,
			final int startIndex, final int count,
			final boolean disableListDataListeners) {
		logger.debug("Started rendering further search results for "
				+ si.getResourceTypeToSearchFor().getCollectionName());

		// NB! very important to remove all listeners here, so that the JList
		// won't "freeze"
		// on updating the components
		ListDataListener[] listeners = null;
		if (disableListDataListeners) {
			listeners = resultsListingModel.getListDataListeners();
			for (ListDataListener listener : listeners) {
				resultsListingModel.removeListDataListener(listener);
			}
		}

		for (int i = startIndex; i < startIndex + count
				&& i < resultsListingModel.getSize(); i++) {
			resultsListingModel.set(i, searchInstance.getSearchResults()
					.getFoundItems().get(i));
		}

		// reset all listeners in case they were removed
		if (disableListDataListeners) {
			for (ListDataListener listener : listeners) {
				resultsListingModel.addListDataListener(listener);
			}
		}

		// NB! critical to have UI update done within the invokeLater()
		// method - this is to prevent UI from 'flashing' and to
		// avoid some weird errors
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jlResultsListing.validate();
				jlResultsListing.repaint();

				logger.debug("Finished rendering further search results for "
						+ si.getResourceTypeToSearchFor().getCollectionName());
			}
		});
	}

	private void switchToDesignPerspective() {
		if (designPerspective == null) {
			for (PerspectiveSPI perspective : Workbench.getInstance()
					.getPerspectives().getPerspectives()) {
				if (perspective.getText().equalsIgnoreCase("design")) {
					designPerspective = perspective;
					break;
				}
			}
		}
		
		if (designPerspective != null) {
			ModelMap.getInstance().setModel(
					ModelMapConstants.CURRENT_PERSPECTIVE, designPerspective);
		}
	}
}
