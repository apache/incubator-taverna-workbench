package net.sf.taverna.biocatalogue.ui.previews;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.Resource;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.ResourcePreviewContent;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.ui.HistoryOrFavoritesBlockEntryDetailsProvider;
import net.sf.taverna.biocatalogue.ui.HistoryOrFavouritesBlock;
import net.sf.taverna.biocatalogue.ui.JPanelWithOverlay;
import net.sf.taverna.biocatalogue.ui.HistoryOrFavouritesBlock.Entry;

import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;

import org.apache.log4j.Logger;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 * A class to show a frame with previews of resources.
 * 
 * @author Sergejs Aleksejevs
 */
public class ResourcePreviewBrowser extends JFrame implements ActionListener, HistoryOrFavoritesBlockEntryDetailsProvider
{
  // navigation data
  private int iCurrentHistoryIdx;        // index within the current history
  private List<String> alCurrentHistory; // current history - e.g. if one opens Page1, then Page2; goes back and opens Page3 - current preview would hold only [Page1, Page3]
  private List<Resource> llFullHistory;  // all resources that were previewed since application started (will be used by ResourcePreviewHistoryBrowser)
  
  // components for accessing application's main elements
  private MainComponent pluginMainComponent;
  private BioCatalogueClient client;
  private Logger logger;
  
  // holder of the data about currently previewed item
  private ResourcePreviewContent rpcContent;
  
  // components of the preview window
  private JPanel jpMain;
  private JPanel jpStatusBar;
  private JLabel lSpinnerIcon;
  private JToggleButton bTogglePreviewHistory;
  private JButton bBack;
  private JButton bForward;
  private JButton bRefresh;
  private JButton bOpenInBioCatalogue;
  
  private JPanelWithOverlay jpContentWithPreviewHistoryOverlay;
  private HistoryOrFavouritesBlock jpPreviewHistory;
  private JPanel jpMainContent;
  
  // icons
  private ImageIcon iconSpinner = ResourceManager.getImageIcon(ResourceManager.SPINNER);
  private ImageIcon iconSpinnerStopped = ResourceManager.getImageIcon(ResourceManager.SPINNER_STILL);
  private ImageIcon iconBarLoader = ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_ORANGE);
  
  
  public ResourcePreviewBrowser(MainComponent component, BioCatalogueClient client, Logger logger)
  {
    super();
    
    // set main variables to ensure access to myExperiment, logger and the parent component
    this.pluginMainComponent = component;
    this.client = client;
    this.logger = logger;
    
    // TODO - initialise preview history here
    this.llFullHistory = new LinkedList<Resource>();
    
    // no navigation history at loading
    this.iCurrentHistoryIdx = -1;
    this.alCurrentHistory = new ArrayList<String>();
    
    // set options of the preview dialog box
    this.setIconImage(ResourceManager.getImageIcon(ResourceManager.FAVICON).getImage());
    
    this.initialiseUI();
  }
  
  
  private void initialiseUI()
  {
    // create the STATUS BAR of the preview window
    JPanel jpNavigationButtons = new JPanel();
    
    bTogglePreviewHistory = new JToggleButton(ResourceManager.getImageIcon(ResourceManager.HISTORY_ICON));
    bTogglePreviewHistory.setToolTipText("View history of the recently previewed resources");
    bTogglePreviewHistory.setMargin(new Insets(2, 2, 2, 2));
    bTogglePreviewHistory.setFocusPainted(false);
    bTogglePreviewHistory.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jpContentWithPreviewHistoryOverlay.setOverlayVisible(bTogglePreviewHistory.isSelected());
      }
    });
    jpNavigationButtons.add(bTogglePreviewHistory);
    
    bBack = new JButton(ResourceManager.getImageIcon(ResourceManager.BACK_ICON));
    bBack.setToolTipText("Back");
    bBack.addActionListener(this);
    bBack.setEnabled(false);
    jpNavigationButtons.add(bBack);
    
    bForward = new JButton(ResourceManager.getImageIcon(ResourceManager.FORWARD_ICON));
    bForward.setToolTipText("Forward");
    bForward.addActionListener(this);
    bForward.setEnabled(false);
    jpNavigationButtons.add(bForward);
    
    JPanel jpStatusRefresh = new JPanel();
    bRefresh = new JButton(ResourceManager.getImageIcon(ResourceManager.REFRESH_ICON));
    bRefresh.setToolTipText("Refresh");
    bRefresh.addActionListener(this);
    jpStatusRefresh.add(bRefresh);
    
    lSpinnerIcon = new JLabel(this.iconSpinner);
    jpStatusRefresh.add(lSpinnerIcon);
    
    
    // ACTION BUTTONS
    // 'open in myExperiment' button is the only one that is always available,
    // still will be set available during loading of the preview for consistency of the UI
    bOpenInBioCatalogue = new JButton(ResourceManager.getImageIcon(ResourceManager.OPEN_IN_BIOCATALOGUE_ICON));
    bOpenInBioCatalogue.setToolTipText("View currently previewed resource on BioCatalogue website");
    bOpenInBioCatalogue.addActionListener(this);
    
    // put all action buttons into a button bar
    JPanel jpActionButtons = new JPanel();
    jpActionButtons.add(bOpenInBioCatalogue);
    
    
    // STATUS BAR
    jpStatusBar = new JPanel();
    jpStatusBar.setLayout(new BorderLayout());
    jpStatusBar.add(jpNavigationButtons, BorderLayout.WEST);
    jpStatusBar.add(jpActionButtons, BorderLayout.CENTER);
    jpStatusBar.add(jpStatusRefresh, BorderLayout.EAST);
    
    
    // PREVIEW HISTORY overlay panel
    jpMainContent = new JPanel(new GridLayout());
    
    jpPreviewHistory = new HistoryOrFavouritesBlock("Preview History", "No earlier previewed resources to show",
                                               BioCataloguePluginConstants.RESOURCE_PREVIEW_HISTORY_LENGTH, this);
    jpPreviewHistory.setObjectCollection((LinkedList)this.llFullHistory);
    
    JPanel jpOverlayPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.weighty = 1.0;
    jpOverlayPanel.add(jpPreviewHistory, c);
    
    JScrollPane spOverlayPanel = new JScrollPane(jpOverlayPanel);
    spOverlayPanel.setPreferredSize(new Dimension(250,0));
    
    
    jpContentWithPreviewHistoryOverlay = new JPanelWithOverlay(jpMainContent, spOverlayPanel,
                                                               JSplitPane.HORIZONTAL_SPLIT, false, true, false);
    jpContentWithPreviewHistoryOverlay.registerOverlayActivationToggleButton(this.bTogglePreviewHistory);
    
    
    // PUT EVERYTHING TOGETHER
    jpMain = new JPanel(new BorderLayout());
    jpMain.setOpaque(true);
    jpMain.add(jpStatusBar, BorderLayout.NORTH);
    jpMain.add(jpContentWithPreviewHistoryOverlay, BorderLayout.CENTER);
    
    
    // add all content into the main dialog
    this.getContentPane().add(jpMain);
    
    
    // set the size of the dialog box
    // (NB! Size needs to be set before the position!)
    this.setSize(BioCataloguePluginConstants.RESOURCE_PREVIEW_BROWSER_PREFERRED_WIDTH,
                 BioCataloguePluginConstants.RESOURCE_PREVIEW_BROWSER_PREFERRED_HEIGHT);
    
    // make sure that this window appears centered within the main app window
    // (NB! This must be done after the size of the window was set/calculated!)
    this.setLocationRelativeTo(this.pluginMainComponent);
  }
  
  
  /**
   * This is a worker method. It is normally called when a preview
   * of a new resource is initiated (not by Forward/Backward button,
   * but by click somewhere in the application).
   * 
   * It updates the "current history" - the one which is responsible for Back-Forward
   * navigation, not the overall preview history which contains all previewed resources.
   * 
   * @param action Preview action string, starting with <code>BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE</code>.
   */
  private void updateCurrentHistory(String action)
  {
    // only do so if this is the real preview request, not preparation for preview request
    if (action.startsWith(BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE))
    {
      // if this is not the "newest" page in current history, remove all newer ones
      // (that is if the user went "back" and opened some new link from on of the older pages)
      while(alCurrentHistory.size() > iCurrentHistoryIdx + 1)
      {
        alCurrentHistory.remove(alCurrentHistory.size() - 1);
      }
      
      boolean bPreviewNotTheSameAsTheLastOne = true;
      if(alCurrentHistory.size() > 0)
      {
        // will add new page to the history only if it's not the same as the last one!
        if(action.equals(alCurrentHistory.get(alCurrentHistory.size() - 1))) {
          bPreviewNotTheSameAsTheLastOne = false;
        }
        
        // this is not the first page in the history, enable "Back" button (if only this isn't the same page as was the first one);
        // (this, however, is the last page in the history now - so disable "Forward" button)
        bBack.setEnabled(bPreviewNotTheSameAsTheLastOne || alCurrentHistory.size() > 1);
        bForward.setEnabled(false);
      }
      else if (alCurrentHistory.size() == 0) {
        // this is the first preview after application has loaded or since the
        // preview history was cleared - disable both Back and Forward buttons
        bBack.setEnabled(false);
        bForward.setEnabled(false);
      }
      
      // add current preview URI to the history
      if(bPreviewNotTheSameAsTheLastOne)
      {
        iCurrentHistoryIdx++;
        alCurrentHistory.add(action);
      }
    }
  }
  
  
  
  /**
   * This method is a launcher for the real worker method ('createPreview()')
   * that does all the job.
   * 
   * The purpose of having this method is to manage history. This method is to
   * be called every time when a "new" preview is requested. This will add a new
   * link to the CurrentHistory stack.
   * 
   * Clicks on "Back" and "Forward" buttons will only need to advance the counter
   * of the current position in the CurrentHistory. Therefore, these will directly
   * call 'createPreview()'.
   */
  public void preview(String action)
  {
    // *** History Update ***
    if (action.startsWith(BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE)) {
      // only do so if this is a real request for preview - if preparation (e.g. resource lookup)
      // is required, this will be called after the preparation is done
      updateCurrentHistory(action);
    }
    
    // *** Launch Preview ***
    createPreview(action);
  }
  
  
  private void createPreview(String action)
  {
    // FIXME
//    // JUST FOR TESTING THE CURRENT_HISTORY OPERATION
//    //javax.swing.JOptionPane.showMessageDialog(null, "History idx: " + this.iCurrentHistoryIdx + "\n" + alCurrentHistory.toString());
//    
//    
//    // *** Switch preview browser to loading state ***
//    
//    // show that loading is in progress
//    this.setTitle("Loading preview...");
//    this.lSpinnerIcon.setIcon(this.iconSpinner);
//    
//    jpMainContent.removeAll();
//    jpMainContent.add(new JLabel(this.iconBarLoader));
//    jpMainContent.setBorder(BorderFactory.createLoweredBevelBorder());
//    jpMainContent.validate();
//    jpMainContent.repaint();
//    
//    
//    // update the state of action buttons in the button bar (most will be disabled)
//    updateStatusOfCustomActionButtons(true);
//    
//    
//    // *** Request preview to be created and then show it ***
//    
//    // Make call to myExperiment API in a different thread
//    // (then use SwingUtilities.invokeLater to update the UI when ready).
//    final String strAction = action;
//    final EventListener self = this;
//    
//    new Thread("Load BioCatalogue resource preview content") {
//      public void run() {
//        logger.debug("Starting to fetch the preview content data");
//
//        try {
//          // *** Fetch Data and Create Preview Content ***
//          rpcContent = ResourcePreviewFactory.createPreview(strAction, client, logger);
//          
//          // this test will succeed if a real value was found
//          if (Resource.isValidType(rpcContent.getResource().getType()))
//          {
//            // NB! the preview was generated, so all (if any) preparations + real preview are now available -
//            //     if the preparations were required and the "real" action string was unavailable,
//            //     the "current history" couldn't be updated earlier, so do it now
//            if (strAction.startsWith(BioCataloguePluginConstants.ACTION_PREVIEW_SOAP_OPERATION_AFTER_LOOKUP)) {
//              updateCurrentHistory(BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE + rpcContent.getResource().getURL());
//            }
//            
//            // as all the details about the previewed resource are now known, can store this into full preview history
//            // (before that make sure that if the this item was viewed before, it's removed and re-added at the "top" of the list)
//            // (also make sure that the history size doesn't exceed the pre-set value)
//            llFullHistory.remove(rpcContent.getResource());
//            llFullHistory.add(rpcContent.getResource());
//            if (llFullHistory.size() > BioCataloguePluginConstants.RESOURCE_PREVIEW_HISTORY_LENGTH) llFullHistory.remove(0);
//            
//            // update preview history box
//            jpPreviewHistory.updateUIFromObjectCollection();
//          }
//          
//          // *** Update the Preview Dialog Box when everything is ready ***
//          SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//              // 'stop' loading action in the status bar and window title
//              setTitle((Resource.isValidType(rpcContent.getResource().getType()) ?
//                        Resource.getResourceTypeName(rpcContent.getResource().getType()) + ": " :
//                        "") +
//                       rpcContent.getResource().getTitle());
//              lSpinnerIcon.setIcon(iconSpinnerStopped);
//              
//              // update the state of action buttons in the button bar
//              updateStatusOfCustomActionButtons(false);
//              
//              // remove old preview and add the new one
//              jpMainContent.removeAll();
//              jpMainContent.add(rpcContent.getContent());
//              jpMainContent.setBorder(null);
//              validate();
//              repaint();
//            }
//          });
//        }
//        catch (Exception ex) {
//          logger.error("ERROR: failed somewhere midway through creation of a resource preview; details:\n", ex);
//        }
//      }
//    }.start();
//        
//    
//    // show the dialog box
//    this.setVisible(true);
  }
  
  
  /**
   * Accessor method for getting a full history of previewed resources as a list.
   */
  public List<Resource> getPreviewHistory() {
    return (this.llFullHistory);
  }
  
  
  /**
   * As opposed to getPreviewHistory() which returns full history of previewed resources,
   * this helper method only retrieves the current history stack.
   * 
   * Example: if a user was to view the following items - A -> B -> C
   *                                                           B <- C
   *                                                           B -> D,
   * the full history would be [A,C,B,D];
   * current history stack would be [A,B,D] - note how item C was "forgotten" (this works the same way as all web browsers do)
   */
  public List<String> getCurrentPreviewHistory() {
    return (this.alCurrentHistory);
  }
  
  
  /**
   * Deletes both 'current history' (the latest preview history stack) and the
   * 'full preview history'. Also, resets the index in the current history,
   * so that the preview browser would not allow using Back-Forward buttons until
   * some new previews are opened.
   */
  public void clearPreviewHistory() {
    this.iCurrentHistoryIdx = -1;
    this.alCurrentHistory.clear();
    this.llFullHistory.clear();
    
    // apply this update to the UI as well
    this.jpPreviewHistory.updateUIFromObjectCollection();
  }
  
  
  // FIXME
//  private void updateStatusOfCustomActionButtons(boolean previewIsCurrentlyLoading)
//  {
//    if (previewIsCurrentlyLoading) {
//      // disable all action buttons while loading is in progress
//      bOpenInBioCatalogue.setEnabled(false);
//    }
//    else {
//      // loading finished - conditionally enable buttons
//      
//      // all items can be previewed on the website
//      if (Resource.isValidType(this.rpcContent.getResource().getType())) {
//        // ..however only if the preview was successfully generated with no errors
//        bOpenInBioCatalogue.setEnabled(true);
//      }
//    }
//  }
  
  
  /**
   * Opens provided URL in the system specified default web browser.
   * 
   * @param urlToOpen The actual URL to open or action command starting with
   *                  <code>BioCataloguePluginConstants.ACTION_SHOW_IN_WEB_BROWSER</code>
   *                  followed by the URL to open.
   */
  public void openInWebBrowser(String urlToOpen)
  {
    String actualURL = urlToOpen;
    
    // if nothing provided - do nothing
    if (actualURL == null) return;
    
    if (actualURL.startsWith(BioCataloguePluginConstants.ACTION_SHOW_IN_WEB_BROWSER)) {
      // extract the 'true' URL from the action command
      actualURL = actualURL.substring(BioCataloguePluginConstants.ACTION_SHOW_IN_WEB_BROWSER.length());
    }
    
    // if the URL was empty or only the action command prefix - do nothing
    if (actualURL.length() == 0) return;
    
    // we've got the URL - attempt to open it
    try {
      BrowserLauncher launcher = new BrowserLauncher();
      launcher.openURLinBrowser(actualURL);
    }
    catch (Exception ex) {
      logger.error("Failed while trying to open the URL in a standard browser; URL was: " +
           actualURL + "\nException was: " + ex + "\n" + ex.getStackTrace());
    }
  }
  
  
  // *** Callback for ActionListener interface ***
  
  public void actionPerformed(ActionEvent e)
  {
    // FIXME
//    if(e.getSource().equals(this.bBack))
//    {
//      // "Back" button clicked
//      
//      // update position in the history
//      iCurrentHistoryIdx--;
//      
//      // enable or disable "back"/"forward" buttons as appropriate
//      bBack.setEnabled(iCurrentHistoryIdx > 0);
//      bForward.setEnabled(iCurrentHistoryIdx < alCurrentHistory.size() - 1);
//      
//      // open requested preview from the history
//      this.createPreview(alCurrentHistory.get(iCurrentHistoryIdx));
//    }
//    else if(e.getSource().equals(this.bForward))
//    {
//      // "Forward" button clicked
//      
//      // update position in the history
//      iCurrentHistoryIdx++;
//      
//      // enable or disable "back"/"forward" buttons as appropriate
//      bBack.setEnabled(iCurrentHistoryIdx > 0);
//      bForward.setEnabled(iCurrentHistoryIdx < alCurrentHistory.size() - 1);
//      
//      // open requested preview from the history
//      this.createPreview(alCurrentHistory.get(iCurrentHistoryIdx));
//    }
//    else if(e.getSource().equals(this.bRefresh))
//    {
//      // "Refresh" button clicked
//      
//      // simply reload the same preview
//      this.createPreview(alCurrentHistory.get(iCurrentHistoryIdx));
//    }
//    else if(e.getSource().equals(this.bOpenInBioCatalogue))
//    {
//      // "Open in BioCatalogue" button clicked
//      openInWebBrowser(this.rpcContent.getResource().getURL());
//    }
//    else if(e.getSource() instanceof JClickableLabel)
//    {
//      // click somewhere on a JClickableLabel
//      if(e.getActionCommand().startsWith(BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE))
//      {
//        // this is a 'preview' request - launch preview
//        this.preview(e.getActionCommand());
//      }
//      else if (e.getActionCommand().startsWith(BioCataloguePluginConstants.ACTION_SHOW_IN_WEB_BROWSER))
//      {
//        // action command requesting opening it in a web browser
//        openInWebBrowser(e.getActionCommand());
//      }
//      else if (e.getActionCommand().startsWith(BioCataloguePluginConstants.ACTION_PREVIEWED_SERVICE_HEALTH_CHECK))
//      {
//        // "health check" of previewed service is requested
//        ServiceHealthChecker.checkService(rpcContent.getResource());
//      }
//      else if (e.getActionCommand().startsWith(BioCataloguePluginConstants.ACTION_TAG_SEARCH_PREFIX))
//      {
//        // pass this event onto the Search tab
//        this.setVisible(false);
//        this.pluginMainComponent.getSearchTab().actionPerformed(new ActionEvent(this, 0, e.getActionCommand()));
//      }
//      else if (e.getActionCommand().startsWith(BioCataloguePluginConstants.ACTION_FILTER_BY_CATEGORY)) {
//        JOptionPane.showMessageDialog(null, "This would switch to \"Filtering\" tab " +
//        		                                "and filter by selected category:\n" + e.getActionCommand());
//      }
//      else if (e.getActionCommand().startsWith(BioCataloguePluginConstants.ACTION_SHOW_IN_WEB_BROWSER))
//      {
//        // open the link in a default web browser
//        openInWebBrowser(e.getActionCommand());
//      }
//      else
//      {
//        // if action command is blank - nothing needs to be done
//        if (e.getActionCommand().length() > 0) {
//          // TODO - decide what to do with unknown actions
//          JOptionPane.showMessageDialog(null, "unknown action:\n" + e.getActionCommand());
//        }
//      }
//    }
  }
  
  
  // *** Callback for HistoryOrFavoritesBlockEntryDetailsProvider interface ***
  
  
  public Entry provideEntryDetails(HistoryOrFavouritesBlock displayPanel,
      Object objectToProvideDetailsFor,
      int indexOfObjectInDisplayPanelDataCollection)
  {
    // FIXME
//    if (displayPanel.equals(jpPreviewHistory) && objectToProvideDetailsFor instanceof Resource) {
//      Resource resourceToDisplay = (Resource)objectToProvideDetailsFor;
//      JClickableLabel entryLabel = new JClickableLabel(resourceToDisplay.getTitle(),
//                           BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE + resourceToDisplay.getURL(),
//                           this, resourceToDisplay.getIcon(), JLabel.LEFT,
//                           "<html><b>" + resourceToDisplay.getTypeName() + "</b>: " + resourceToDisplay.getTitle());
//      JLabel entryDetails = new JLabel();
//      JClickableLabel actionIcon = new JClickableLabel("", "", this);
//      
//      return (new Entry(entryLabel, entryDetails, actionIcon));
//    }
//    else {
      return null;
//    }
  }
  
}
