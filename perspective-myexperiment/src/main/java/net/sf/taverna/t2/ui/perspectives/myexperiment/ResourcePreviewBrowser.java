package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Base64;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira
 */
public class ResourcePreviewBrowser extends JFrame implements ActionListener, HyperlinkListener, ComponentListener {
  // CONSTANTS
  protected static final int PREFERRED_WIDTH = 750;
  protected static final int PREFERRED_HEIGHT = 600;
  protected static final int PREFERRED_SCROLL = 10;
  protected static final int PREVIEW_HISTORY_LENGTH = 50;

  // navigation data
  private int iCurrentHistoryIdx; // index within the current history
  private final ArrayList<String> alCurrentHistory; // current history - e.g. if one
  // opens Page1, then Page2; goes back and opens Page3 - current preview would hold only [Page1, Page3]
  private ArrayList<Resource> alFullHistory; // all resources that were
  // previewed since application started (will be used by ResourcePreviewHistoryBrowser)

  // components for accessing application's main elements
  private final MainComponent pluginMainComponent;
  private final MyExperimentClient myExperimentClient;
  private final Logger logger;

  // holder of the data about currently previewed item
  private ResourcePreviewContent rpcContent;
  private Resource resource;

  // components of the preview window
  private JPanel jpMain;
  private JPanel jpStatusBar;
  private JLabel lSpinnerIcon;
  private JButton bBack;
  private JButton bForward;
  private JButton bRefresh;
  private JButton bOpenInMyExp;
  private JButton bDownload;
  private JButton bOpenInTaverna;
  private JButton bImportIntoTaverna;
  private JButton bAddComment;
  private JButton bAddRemoveFavourite;
  private JButton bUpload;
  private JButton bEditMetadata;
  private JScrollPane spContentScroller;

  // icons
  private final ImageIcon iconOpenInMyExp = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("open_in_my_experiment_icon"));
  private final ImageIcon iconAddFavourite = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("add_favourite_icon"));
  private final ImageIcon iconDeleteFavourite = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("delete_favourite_icon"));
  private final ImageIcon iconAddComment = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("add_comment_icon"));
  private final ImageIcon iconSpinner = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner"));
  private final ImageIcon iconSpinnerStopped = new ImageIcon(MyExperimentPerspective.getLocalResourceURL("spinner_stopped"));

  public ResourcePreviewBrowser(MainComponent component, MyExperimentClient client, Logger logger) {
	super();

	// set main variables to ensure access to myExperiment, logger and the
	// parent component
	this.pluginMainComponent = component;
	this.myExperimentClient = client;
	this.logger = logger;

	// initialise previewed items history
	String strPreviewedItemsHistory = (String) myExperimentClient.getSettings().get(MyExperimentClient.INI_PREVIEWED_ITEMS_HISTORY);
	if (strPreviewedItemsHistory != null) {
	  Object oPreviewedItemsHistory = Base64.decodeToObject(strPreviewedItemsHistory);
	  this.alFullHistory = (ArrayList<Resource>) oPreviewedItemsHistory;
	} else {
	  this.alFullHistory = new ArrayList<Resource>();
	}

	// no navigation history at loading
	this.iCurrentHistoryIdx = -1;
	this.alCurrentHistory = new ArrayList<String>();

	// set options of the preview dialog box
	this.setIconImage(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("myexp_icon")).getImage());
	this.addComponentListener(this);

	this.initialiseUI();
  }

  /**
   * Accessor method for getting a full history of previewed resources as a
   * list.
   */
  public ArrayList<Resource> getPreviewHistory() {
	return (this.alFullHistory);
  }

  /**
   * As opposed to getPreviewHistory() which returns full history of previewed
   * resources, this helper method only retrieves the current history stack.
   * 
   * Example: if a user was to view the following items - A -> B -> C B <- C B
   * -> D, the full history would be [A,C,B,D]; current history stack would be
   * [A,B,D] - note how item C was "forgotten" (this works the same way as all
   * web browsers do)
   */
  public List<String> getCurrentPreviewHistory() {
	return (this.alCurrentHistory);
  }

  /**
   * Deletes both 'current history' (the latest preview history stack) and the
   * 'full preview history'. Also, resets the index in the current history, so
   * that the preview browser would not allow using Back-Forward buttons until
   * some new previews are opened.
   */
  public void clearPreviewHistory() {
	this.iCurrentHistoryIdx = -1;
	this.alCurrentHistory.clear();
	this.alFullHistory.clear();
  }

  /**
   * This method is a launcher for the real worker method ('createPreview()')
   * that does all the job.
   * 
   * The purpose of having this method is to manage history. This method is to
   * be called every time when a "new" preview is requested. This will add a new
   * link to the CurrentHistory stack.
   * 
   * Clicks on "Back" and "Forward" buttons will only need to advance the
   * counter of the current position in the CurrentHistory. Therefore, these
   * will directly call 'createPreview()'.
   */
  public void preview(String action) {
	// *** History Update ***
	// if this is not the "newest" page in current history, remove all newer
	// ones
	// (that is if the user went "back" and opened some new link from on of the
	// older pages)
	while (alCurrentHistory.size() > iCurrentHistoryIdx + 1) {
	  alCurrentHistory.remove(alCurrentHistory.size() - 1);
	}

	boolean bPreviewNotTheSameAsTheLastOne = true;
	if (alCurrentHistory.size() > 0) {
	  // will add new page to the history only if it's not the same as the last
	  // one!
	  if (action.equals(alCurrentHistory.get(alCurrentHistory.size() - 1))) {
		bPreviewNotTheSameAsTheLastOne = false;
	  }

	  // this is not the first page in the history, enable "Back" button (if
	  // only this isn't the same page as was the first one);
	  // (this, however, is the last page in the history now - so disable
	  // "Forward" button)
	  bBack.setEnabled(bPreviewNotTheSameAsTheLastOne
		  || alCurrentHistory.size() > 1);
	  bForward.setEnabled(false);
	} else if (alCurrentHistory.size() == 0) {
	  // this is the first preview after application has loaded or since the
	  // preview history was cleared - disable both Back and Forward buttons
	  bBack.setEnabled(false);
	  bForward.setEnabled(false);
	}

	// add current preview URI to the history
	if (bPreviewNotTheSameAsTheLastOne) {
	  iCurrentHistoryIdx++;
	  alCurrentHistory.add(action);
	}

	// *** Launch Preview ***
	createPreview(action);
  }

  private void createPreview(String action) {
	// JUST FOR TESTING THE CURRENT_HISTORY OPERATION
	// javax.swing.JOptionPane.showMessageDialog(null, "History idx: " +
	// this.iCurrentHistoryIdx + "\n" + alCurrentHistory.toString());

	// show that loading is in progress
	this.setTitle("Loading preview...");
	this.lSpinnerIcon.setIcon(this.iconSpinner);

	// disable all action buttons while loading is in progress
	bOpenInMyExp.setEnabled(false);
	bDownload.setEnabled(false);
	bOpenInTaverna.setEnabled(false);
	bImportIntoTaverna.setEnabled(false);
	bAddRemoveFavourite.setEnabled(false);
	bAddComment.setEnabled(false);
	bUpload.setEnabled(false);
	bEditMetadata.setEnabled(false);

	// Make call to myExperiment API in a different thread
	// (then use SwingUtilities.invokeLater to update the UI when ready).
	final String strAction = action;
	final EventListener self = this;

	new Thread("Load myExperiment resource preview content") {
	  @Override
	  public void run() {
		logger.debug("Starting to fetch the preview content data");

		try {
		  // *** Fetch Data and Create Preview Content ***
		  rpcContent = pluginMainComponent.getPreviewFactory().createPreview(strAction, self);

		  // as all the details about the previewed resource are now known, can
		  // store this into full preview history
		  // (before that make sure that if the this item was viewed before,
		  // it's removed and re-added at the "top" of the list)
		  // (also make sure that the history size doesn't exceed the pre-set
		  // value)
		  alFullHistory.remove(rpcContent.getResource());
		  alFullHistory.add(rpcContent.getResource());
		  if (alFullHistory.size() > PREVIEW_HISTORY_LENGTH)
			alFullHistory.remove(0);
		  pluginMainComponent.getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.PREVIEWED_ITEMS_HISTORY);

		  // *** Update the Preview Dialog Box when everything is ready ***
		  SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			  // 'stop' loading action in the status bar and window title
			  setTitle(Resource.getResourceTypeName(rpcContent.getResourceType())
				  + ": " + rpcContent.getResourceTitle());
			  lSpinnerIcon.setIcon(iconSpinnerStopped);

			  // update the state of action buttons in the button bar
			  updateButtonBarState(rpcContent);

			  // wrap received content into a ScrollPane
			  spContentScroller = new JScrollPane(rpcContent.getContent());
			  spContentScroller.setBorder(BorderFactory.createEmptyBorder());
			  spContentScroller.getVerticalScrollBar().setUnitIncrement(ResourcePreviewBrowser.PREFERRED_SCROLL);

			  // remove everything from the preview and re-add all components
			  // (NB! Removing only CENTER component didn't work properly)
			  jpMain.removeAll();
			  jpMain.add(redrawStatusBar(), BorderLayout.NORTH);
			  jpMain.add(spContentScroller, BorderLayout.CENTER);
			  validate();
			  repaint();
			}
		  });
		} catch (Exception ex) {
		  logger.error("Exception on attempt to login to myExperiment:\n", ex);
		}
	  }
	}.start();

	// show the dialog box
	this.setVisible(true);
  }

  private void initialiseUI() {
	// create the STATUS BAR of the preview window
	createButtonsForStatusBar();

	// put everything together
	jpMain = new JPanel();
	jpMain.setOpaque(true);
	jpMain.setLayout(new BorderLayout());
	jpMain.add(redrawStatusBar(), BorderLayout.NORTH);

	// add all content into the main dialog
	this.getContentPane().add(jpMain);

  }

  private void createButtonsForStatusBar() {
	// navigation buttons => far left of status bar
	bBack = new JButton(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("back_icon")));
	bBack.setToolTipText("Back");
	bBack.addActionListener(this);
	bBack.setEnabled(false);

	bForward = new JButton(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("forward_icon")));
	bForward.setToolTipText("Forward");
	bForward.addActionListener(this);
	bForward.setEnabled(false);

	// refresh buttons => far right of status bar
	bRefresh = new JButton(new ImageIcon(MyExperimentPerspective.getLocalResourceURL("refresh_icon")));
	bRefresh.setToolTipText("Refresh");
	bRefresh.addActionListener(this);

	lSpinnerIcon = new JLabel(this.iconSpinner);

	// ACTION BUTTONS
	// 'open in myExperiment' button is the only one that is always available,
	// still will be set available during loading of the preview for consistency of the UI

	// myExperiment "webby" functions
	bOpenInMyExp = new JButton(iconOpenInMyExp);
	bOpenInMyExp.setEnabled(false);
	bOpenInMyExp.addActionListener(this);

	bAddRemoveFavourite = new JButton(iconAddFavourite);
	bAddRemoveFavourite.setEnabled(false);
	bAddRemoveFavourite.addActionListener(this);

	bAddComment = new JButton(iconAddComment);
	bAddComment.setEnabled(false);
	bAddComment.addActionListener(this);

	bEditMetadata = new JButton("Update", WorkbenchIcons.editIcon);
	bEditMetadata.setEnabled(false);
	bEditMetadata.addActionListener(this);

	bUpload = new JButton("Upload", WorkbenchIcons.upArrowIcon);
	bUpload.setEnabled(false);
	bUpload.addActionListener(this);

	// functions more specific to taverna
	bOpenInTaverna = new JButton(WorkbenchIcons.openIcon);
	bOpenInTaverna.setEnabled(false);
	bOpenInTaverna.addActionListener(this);

	bImportIntoTaverna = new JButton();
	bImportIntoTaverna.setEnabled(false);
	bImportIntoTaverna.addActionListener(this);

	bDownload = new JButton(WorkbenchIcons.saveIcon);
	bDownload.setEnabled(false);
	bDownload.addActionListener(this);
  }

  private JPanel redrawStatusBar() {
	// far left of button bar
	JPanel jpNavigationButtons = new JPanel();
	jpNavigationButtons.add(bBack);
	jpNavigationButtons.add(bForward);

	// far right of button bar
	JPanel jpRefreshButtons = new JPanel();
	jpRefreshButtons.add(bRefresh);
	jpRefreshButtons.add(lSpinnerIcon);

	// myExperiment buttons: second left of the button bar
	JPanel jpMyExperimentButtons = new JPanel();
	jpMyExperimentButtons.add(bOpenInMyExp);
	jpMyExperimentButtons.add(bAddRemoveFavourite);
	jpMyExperimentButtons.add(bAddComment);
	jpMyExperimentButtons.add(bEditMetadata);
	jpMyExperimentButtons.add(bUpload);

	// taverna buttons: second right of the button bar
	JPanel jpTavernaButtons = new JPanel();
	jpTavernaButtons.add(bOpenInTaverna);
	jpTavernaButtons.add(bImportIntoTaverna);
	jpTavernaButtons.add(bDownload);

	// put all action buttons into a button bar
	JPanel jpStatusBar = new JPanel();
	jpStatusBar.setLayout(new GridBagLayout());
	int spaceBetweenSections = 40;

	GridBagConstraints c = new GridBagConstraints();
	c.insets = new Insets(0, spaceBetweenSections, 0, spaceBetweenSections / 2);
	c.anchor = GridBagConstraints.WEST;
	c.fill = GridBagConstraints.HORIZONTAL;
	c.gridx = 0;
	c.gridy = 0;
	jpStatusBar.add(jpNavigationButtons, c);

	c.gridx++;
	c.insets = new Insets(0, spaceBetweenSections / 2, 0, spaceBetweenSections / 2);
	jpStatusBar.add(jpMyExperimentButtons, c);

	c.gridx++;
	jpStatusBar.add(jpTavernaButtons, c);

	c.gridx++;
	c.insets = new Insets(0, spaceBetweenSections / 2, 0, spaceBetweenSections);
	jpStatusBar.add(jpRefreshButtons, c);

	return jpStatusBar;

	//	// put all action buttons into a button bar
	//	JPanel jpActionButtons = new JPanel();
	//	jpActionButtons.setLayout(new GridBagLayout());
	//	GridBagConstraints c = new GridBagConstraints();
	//	double spacing = ResourcePreviewBrowser.PREFERRED_WIDTH * 0.1;
	//	c.insets = new Insets(0, (int) spacing, 0, (int) spacing / 2);
	//	c.gridx = 0;
	//	c.gridy = 0;
	//	jpActionButtons.add(jpMyExperimentButtons, c);
	//	
	//	c.gridx++;
	//	c.insets = new Insets(0, (int) spacing / 2, 0, (int) spacing);
	//	jpActionButtons.add(jpTavernaButtons, c);
	//	
	//	jpStatusBar = new JPanel();
	//	jpStatusBar.setLayout(new BorderLayout());
	//	jpStatusBar.add(jpNavigationButtons, BorderLayout.WEST);
	//	jpStatusBar.add(jpActionButtons, BorderLayout.CENTER);
	//	jpStatusBar.add(jpRefreshButtons, BorderLayout.EAST);
  }

  private void updateButtonBarState(ResourcePreviewContent content) {
	// get the visible type name of the resource
	Resource r = this.rpcContent.getResource();
	String strResourceType = Resource.getResourceTypeName(r.getItemType()).toLowerCase();

	// "Open in myExperiment" is always available for every item type
	this.bOpenInMyExp.setEnabled(true);
	this.bOpenInMyExp.setToolTipText("Open this " + strResourceType
		+ " in myExperiment");

	// "edit metadata" to myExperiment is only available for logged in
	// users who are the owners of the workflow
	String strTooltip = "It is currently not possible to edit the metadata for this workflow";
	boolean bUpdateMetaAvailable = false;

	if (myExperimentClient.isLoggedIn()
		&& (myExperimentClient.getCurrentUser().equals(r.getUploader()))
		&& (r.getItemTypeName().equals("Workflow"))) {
	  strTooltip = "Update the metadata of this workflow.";
	  bUpdateMetaAvailable = true;
	} else {
	  strTooltip = "Only the owners of workflows can change the metadata of workflows.";
	}
	this.bEditMetadata.setToolTipText(strTooltip);
	this.bEditMetadata.setEnabled(bUpdateMetaAvailable);

	// "upload new version" to myExperiment is only available for logged in
	// users who are the owners of the workflow
	strTooltip = "It is currently not possible to upload a new version of this workflow.";
	boolean bUploadAvailable = false;

	if (myExperimentClient.isLoggedIn()
		&& (myExperimentClient.getCurrentUser().equals(r.getUploader()))
		&& (r.getItemTypeName().equals("Workflow"))) {
	  strTooltip = "Upload a new version of this workflow.";
	  bUploadAvailable = true;
	} else {
	  strTooltip = "Only the owners of workflows can upload new versions of workflows.";
	}
	this.bUpload.setToolTipText(strTooltip);
	this.bUpload.setEnabled(bUploadAvailable);

	// "Download" - only for selected types and based on current user's
	// permissions (these conditions are checked within the action)
	this.bDownload.setAction(pluginMainComponent.new DownloadResourceAction(r, false));

	// "Open in Taverna" - only for Taverna workflows and when download is
	// allowed for current user (these checks are carried out inside the action)
	this.bOpenInTaverna.setAction(pluginMainComponent.new LoadResourceInTavernaAction(r, true));

	// "Import into Taverna" - only for Taverna workflows and when download is
	// allowed for current user (these checks are carried out inside the action)
	// the import button
	this.bImportIntoTaverna.setAction(pluginMainComponent.new ImportIntoTavernaAction(r));

	// "Add to Favourites" - for all types, but only for logged in users
	strTooltip = "It is currently not possible to add " + strResourceType
		+ "s to favourites";
	boolean bFavouritingAvailable = false;
	if (r.isFavouritable()) {
	  if (myExperimentClient.isLoggedIn()) {
		if (r.isFavouritedBy(myExperimentClient.getCurrentUser())) {
		  strTooltip = "Remove this " + strResourceType
			  + " from your favourites";
		  this.bAddRemoveFavourite.setIcon(iconDeleteFavourite);
		} else {
		  strTooltip = "Add this " + strResourceType + " to your favourites";
		  this.bAddRemoveFavourite.setIcon(iconAddFavourite);
		}
		bFavouritingAvailable = true;
	  } else {
		// TODO should be changed to display login box first, then favouriting option
		strTooltip = "Only logged in users can add items to favourites";
	  }
	}
	this.bAddRemoveFavourite.setToolTipText(strTooltip);
	this.bAddRemoveFavourite.setEnabled(bFavouritingAvailable);

	// "Add Comment" - for all types besides users and only for logged in users
	strTooltip = "It is currently not possible to comment on "
		+ strResourceType + "s";
	boolean bCommentingAvailable = false;
	if (r.isCommentableOn()) {
	  if (myExperimentClient.isLoggedIn()) {
		strTooltip = "Add a comment on this " + strResourceType;
		bCommentingAvailable = true;
	  } else {
		// TODO should be changed to display login box first, then commenting option
		strTooltip = "Only logged in users can make comments";
	  }
	}
	this.bAddComment.setToolTipText(strTooltip);
	this.bAddComment.setEnabled(bCommentingAvailable);
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
	if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	  String strAction = e.getDescription().toString();

	  if (strAction.startsWith("preview:")) {
		this.preview(strAction);
	  } else {
		try {
		    Desktop.getDesktop().browse(new URI(strAction));
		} catch (Exception ex) {
		  logger.error("Failed while trying to open the URL in a standard browser; URL was: "
			  + strAction + "\nException was: " + ex);
		}
	  }
	}
  }

  public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.bBack)) {
	  // "Back" button clicked

	  // update position in the history
	  iCurrentHistoryIdx--;

	  // enable or disable "back"/"forward" buttons as appropriate
	  bBack.setEnabled(iCurrentHistoryIdx > 0);
	  bForward.setEnabled(iCurrentHistoryIdx < alCurrentHistory.size() - 1);

	  // open requested preview from the history
	  this.createPreview(alCurrentHistory.get(iCurrentHistoryIdx));
	} else if (e.getSource().equals(this.bForward)) {
	  // "Forward" button clicked

	  // update position in the history
	  iCurrentHistoryIdx++;

	  // enable or disable "back"/"forward" buttons as appropriate
	  bBack.setEnabled(iCurrentHistoryIdx > 0);
	  bForward.setEnabled(iCurrentHistoryIdx < alCurrentHistory.size() - 1);

	  // open requested preview from the history
	  this.createPreview(alCurrentHistory.get(iCurrentHistoryIdx));
	} else if (e.getSource().equals(this.bRefresh)) {
	  // "Refresh" button clicked

	  // simply reload the same preview
	  this.createPreview(alCurrentHistory.get(iCurrentHistoryIdx));
	} else if (e.getSource().equals(this.bOpenInMyExp)) {
	  // "Open in myExperiment" button clicked
	  try {
	      Desktop.getDesktop().browse(new URI(this.rpcContent.getResourceURL()));
	  } catch (Exception ex) {
		logger.error("Failed while trying to open the URL in a standard browser; URL was: "
			+ this.rpcContent.getResourceURL() + "\nException was: " + ex);
	  }
	} else if (e.getSource().equals(this.bUpload)) {
	  /* ************************************************************************* */
	  Resource resource = this.rpcContent.getResource();
	  if (resource.getItemTypeName().equals("Workflow")) {
		UploadWorkflowDialog uploadWorkflowDialog = new UploadWorkflowDialog(this, true, resource);

		if (uploadWorkflowDialog.launchUploadDialogAndPostIfRequired()) {
		  // "true" has been returned so update the resource
		  this.actionPerformed(new ActionEvent(this.bRefresh, 0, ""));
		}
	  }
	} else if (e.getSource().equals(this.bEditMetadata)) {
	  Resource resource = this.rpcContent.getResource();
	  if (resource.getItemTypeName().equals("Workflow")) {
		UploadWorkflowDialog uploadWorkflowDialog = new UploadWorkflowDialog(this, false, resource);

		if (uploadWorkflowDialog.launchUploadDialogAndPostIfRequired()) {
		  // "true" has been returned so update the resource
		  this.actionPerformed(new ActionEvent(this.bRefresh, 0, ""));
		}
	  }
	  /* ************************************************************************* */
	} else if (e.getSource().equals(this.bAddComment)) {
	  // "Add Comment" button was clicked
	  String strComment = null;
	  AddCommentDialog commentDialog = new AddCommentDialog(this, this.rpcContent.getResource(), pluginMainComponent, myExperimentClient, logger);
	  if ((strComment = commentDialog.launchAddCommentDialogAndPostCommentIfRequired()) != null) {
		// comment was added because return value is not null;
		// a good option now would be to reload only the comments tab, but
		// for now we refresh the whole of the preview
		this.actionPerformed(new ActionEvent(this.bRefresh, 0, ""));

		// update history of the items that were commented on, making sure that:
		// - there's only one occurrence of this item in the history;
		// - if this item was in the history before, it is moved to the 'top' now;
		// - predefined history size is not exceeded
		this.pluginMainComponent.getHistoryBrowser().getCommentedOnItemsHistoryList().remove(this.rpcContent.getResource());
		this.pluginMainComponent.getHistoryBrowser().getCommentedOnItemsHistoryList().add(this.rpcContent.getResource());
		if (this.pluginMainComponent.getHistoryBrowser().getCommentedOnItemsHistoryList().size() > HistoryBrowserTabContentPanel.COMMENTED_ON_ITEMS_HISTORY) {
		  this.pluginMainComponent.getHistoryBrowser().getCommentedOnItemsHistoryList().remove(0);
		}

		// now update the history of the items that were commented on in 'History' tab
		if (this.pluginMainComponent.getHistoryBrowser() != null) {
		  this.pluginMainComponent.getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.COMMENTED_ON_ITEMS_HISTORY);
		}
	  }
	} else if (e.getSource().equals(this.bAddRemoveFavourite)) {
	  boolean bItemIsFavourited = this.rpcContent.getResource().isFavouritedBy(this.myExperimentClient.getCurrentUser());

	  AddRemoveFavouriteDialog favouriteDialog = new AddRemoveFavouriteDialog(this, !bItemIsFavourited, this.rpcContent.getResource(), pluginMainComponent, myExperimentClient, logger);
	  int iFavouritingStatus = favouriteDialog.launchAddRemoveFavouriteDialogAndPerformNecessaryActionIfRequired();

	  // if the operation wasn't cancelled, update status of the
	  // "add/remove favourite" button and the list of favourites in the user profile
	  if (iFavouritingStatus != AddRemoveFavouriteDialog.OPERATION_CANCELLED) {
		this.updateButtonBarState(this.rpcContent);
		this.pluginMainComponent.getMyStuffTab().getSidebar().repopulateFavouritesBox();
		this.pluginMainComponent.getMyStuffTab().getSidebar().revalidate();
	  }
	} else if (e.getSource() instanceof JClickableLabel) {
	  // clicked somewhere on a JClickableLabel; if that's a 'preview' request - launch preview
	  if (e.getActionCommand().startsWith("preview:")) {
		this.preview(e.getActionCommand());
	  } else if (e.getActionCommand().startsWith("tag:")) {
		// pass this event onto the Tag Browser tab
		this.pluginMainComponent.getTagBrowserTab().actionPerformed(e);
		this.pluginMainComponent.getMainTabs().setSelectedComponent(this.pluginMainComponent.getTagBrowserTab());
	  } else {
		// show the link otherwise
		try {
		    Desktop.getDesktop().browse(new URI(e.getActionCommand()));
		} catch (Exception ex) {
		  logger.error("Failed while trying to open the URL in a standard browser; URL was: "
			  + e.getActionCommand() + "\nException was: " + ex);
		}
	  }
	} else if (e.getSource() instanceof TagCloudPanel
		&& e.getActionCommand().startsWith("tag:")) {
	  // close the window and pass this event onto the Tag Browser tab
	  this.setVisible(false);
	  this.pluginMainComponent.getTagBrowserTab().actionPerformed(e);
	  this.pluginMainComponent.getMainTabs().setSelectedComponent(this.pluginMainComponent.getTagBrowserTab());
	}
  }

  // *** Callbacks for ComponentListener interface ***

  public void componentShown(ComponentEvent e) {
	// every time the preview browser window is shown, it will start loading a preview 
	// - this state is set in the preview() method; (so this won't have to be done here)

	// remove everything from the preview and re-add only the status bar
	// (this is done so that newly opened preview window won't show the old
	// preview)
	jpMain.removeAll();
	jpMain.add(redrawStatusBar(), BorderLayout.NORTH);
	repaint();

	// set the size of the dialog box (NB! Size needs to be set before the position!)
	this.setSize(ResourcePreviewBrowser.PREFERRED_WIDTH, ResourcePreviewBrowser.PREFERRED_HEIGHT);
	this.setMinimumSize(new Dimension(ResourcePreviewBrowser.PREFERRED_WIDTH, ResourcePreviewBrowser.PREFERRED_HEIGHT));

	// make sure that the dialog box appears centered horizontally relatively to
	// the main component; also, pad by 30px vertically from the top of the main component
	int iMainComponentCenterX = (int) Math.round(this.pluginMainComponent.getLocationOnScreen().getX()
		+ (this.pluginMainComponent.getWidth() / 2));
	int iPosX = iMainComponentCenterX - (this.getWidth() / 2);
	int iPosY = ((int) this.pluginMainComponent.getLocationOnScreen().getY()) + 30;
	this.setLocation(iPosX, iPosY);

	myExperimentClient.storeHistoryAndSettings();
  }

  public void componentHidden(ComponentEvent e) {
	myExperimentClient.storeHistoryAndSettings();
  }

  public void componentResized(ComponentEvent e) {
	// do nothing
  }

  public void componentMoved(ComponentEvent e) {
	// do nothing
  }

  public Resource getResource() {
	return resource;
  }

}
