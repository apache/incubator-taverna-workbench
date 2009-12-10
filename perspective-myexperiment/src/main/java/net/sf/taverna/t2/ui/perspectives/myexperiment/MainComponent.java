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
/**
 * 
 */
package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.perspectives.PerspectiveRegistry;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Workflow;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializationConstants;

import org.apache.log4j.Logger;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira, Jiten Bhagat
 */
public final class MainComponent extends JPanel implements UIComponentSPI, ChangeListener {
  // myExperiment client, logger and the stylesheet will be made available throughout the whole perspective
  private MyExperimentClient myExperimentClient;
  private final Logger logger = Logger.getLogger(MainComponent.class);
  private final StyleSheet css;
  private final ResourcePreviewFactory previewFactory;
  private final ResourcePreviewBrowser previewBrowser;

  // components of the perspective
  private JTabbedPane tpMainTabs;
  private MyStuffTabContentPanel pMyStuffContainer;
  private ExampleWorkflowsPanel pExampleWorkflows;
  private TagBrowserTabContentPanel pTagBrowser;
  private SearchTabContentPanel pSearchTab;
  private HistoryBrowserTabContentPanel pHistoryBrowserTab;
  private PluginStatusBar pStatusBar;
  private PluginPreferencesDialog jdPreferences;

  public static MainComponent MAIN_COMPONENT;
  public static MyExperimentClient MY_EXPERIMENT_CLIENT;
  public static Logger LOGGER;

  public MainComponent() {
	super();

	// create and initialise myExperiment client
	try {
	  this.myExperimentClient = new MyExperimentClient(logger);
	} catch (Exception e) {
	  this.logger.error("Couldn't initialise myExperiment client");
	}

	// x, y, z ARE NOT USED ANYWHERE ELSE
	// HACK TO BE ABLE TO GET THE REFS FROM TAVERNA'S PREFERENCE PANEL
	// TODO: refactor code for all the other classes to utilise the class vars
	MainComponent x = this;
	MAIN_COMPONENT = x;

	MyExperimentClient y = this.myExperimentClient;
	MY_EXPERIMENT_CLIENT = y;

	Logger z = this.logger;
	LOGGER = z;

	// components to generate and display previews
	previewFactory = new ResourcePreviewFactory(this, myExperimentClient, logger);
	previewBrowser = new ResourcePreviewBrowser(this, myExperimentClient, logger);

	this.css = new StyleSheet();
	this.css.importStyleSheet(MyExperimentPerspective.getLocalResourceURL("css_stylesheet"));
	logger.debug("Stylesheet loaded: \n" + this.css.toString());

	// check if values for default tabs are set, if not - set defaults;
	// NB! This has to be done before initialising UI
	if (myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_DEFAULT_ANONYMOUS_TAB) == null)
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_DEFAULT_ANONYMOUS_TAB, "2"); // DEFAULT IS SEARCH TAB
	if (myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_DEFAULT_LOGGED_IN_TAB) == null)
	  myExperimentClient.getSettings().put(MyExperimentClient.INI_DEFAULT_LOGGED_IN_TAB, "0"); // DEFAULT IS MY STUFF TAB

	initialisePerspectiveUI();

	// HACK for a weird stylesheet bug (where the first thing to use the stylesheet doesn't actually get the styles)
	// NB! This has to be located after all ShadedLabels were initialized to prevent bad layout in them
	HTMLEditorKit kit = new HTMLEditorKit();
	kit.setStyleSheet(this.css);

	// determine which shutdown operations to use
	if (Util.isRunningInTaverna()) {
	  // register the current instance of main component with the myExperiment perspective; this will be used later on when shutdown operation needs to be performed - e.g. this aids ShutdownSPI to find the running instance of the plugin
	  for (PerspectiveSPI perspective : PerspectiveRegistry.getInstance().getPerspectives()) {
		if (perspective.getText().equals(MyExperimentPerspective.PERSPECTIVE_NAME)) {
		  ((MyExperimentPerspective) perspective).setMainComponent(this);
		  break;
		}
	  }
	} else {
	  // this registers the thread which will perform shutdown operations;
	  // NB! this only needs to be done when running outside Taverna, because
	  // ShutdownSPI would replace this in that case.
	  MainComponentShutdownHook shutdownHook = new MainComponentShutdownHook();
	  shutdownHook.setLinks(this, this.myExperimentClient, this.logger);
	  Runtime.getRuntime().addShutdownHook(shutdownHook.new MyExperimentClientShutdownThread());
	}

	// Do the rest in a separate thread to avoid hanging the GUI.
	// Remember to use SwingUtilities.invokeLater to update the GUI directly.
	new Thread("Data initialisation for Taverna 2 - myExperiment plugin") {
	  @Override
	  public void run() {
		// load the data into the plugin
		initialiseData();
	  }
	}.start();

  }

  public ImageIcon getIcon() {
	return WorkbenchIcons.databaseIcon;
  }

  @Override
  public String getName() {
	return "myExperiment Perspective Main Component";
  }

  public void onDisplay() {
  }

  public void onDispose() {
  }

  public MyExperimentClient getMyExperimentClient() {
	return this.myExperimentClient;
  }

  public Logger getLogger() {
	return this.logger;
  }

  public StyleSheet getStyleSheet() {
	return this.css;
  }

  public PluginStatusBar getStatusBar() {
	return this.pStatusBar;
  }

  public PluginPreferencesDialog getPreferencesDialog() {
	return this.jdPreferences;
  }

  public ResourcePreviewFactory getPreviewFactory() {
	return this.previewFactory;
  }

  public ResourcePreviewBrowser getPreviewBrowser() {
	return this.previewBrowser;
  }

  public HistoryBrowserTabContentPanel getHistoryBrowser() {
	return this.pHistoryBrowserTab;
  }

  public JTabbedPane getMainTabs() {
	return (this.tpMainTabs);
  }

  public MyStuffTabContentPanel getMyStuffTab() {
	return (this.pMyStuffContainer);
  }

  public ExampleWorkflowsPanel getExampleWorkflowsTab() {
	return (this.pExampleWorkflows);
  }

  public TagBrowserTabContentPanel getTagBrowserTab() {
	return (this.pTagBrowser);
  }

  public SearchTabContentPanel getSearchTab() {
	return (this.pSearchTab);
  }

  private void initialisePerspectiveUI() {
	// HACK: this is required to prevent some labels from having white non-transparent background
	ShadedLabel testLabel = new ShadedLabel("test", ShadedLabel.BLUE);

	// create instances of individual components
	// (NB! Status bar needs to be initialised first, so that it is available to other components immediately!)
	this.pStatusBar = new PluginStatusBar(this, myExperimentClient, logger);
	this.pMyStuffContainer = new MyStuffTabContentPanel(this, myExperimentClient, logger);
	this.pExampleWorkflows = new ExampleWorkflowsPanel(this, myExperimentClient, logger);
	this.pTagBrowser = new TagBrowserTabContentPanel(this, myExperimentClient, logger);
	this.pSearchTab = new SearchTabContentPanel(this, myExperimentClient, logger);
	this.pHistoryBrowserTab = new HistoryBrowserTabContentPanel(this, myExperimentClient, logger);

	// add the required ones into the main tabs
	this.tpMainTabs = new JTabbedPane();
	this.tpMainTabs.add("My Stuff", this.pMyStuffContainer);
	// TODO: implement the starter pack
	this.tpMainTabs.add("Starter Pack", this.pExampleWorkflows);
	this.tpMainTabs.add("Tag Browser", this.pTagBrowser);
	this.tpMainTabs.add("Search", this.pSearchTab);
	this.tpMainTabs.add("Local History", this.pHistoryBrowserTab);

	// add main tabs and the status bar into the perspective
	this.setLayout(new BorderLayout());
	this.add(this.tpMainTabs, BorderLayout.CENTER);
	this.add(this.pStatusBar, BorderLayout.SOUTH);

	// add listener to TabbedPane, so that the app "knows" when some tab was
	// opened
	this.tpMainTabs.addChangeListener(this);

	// initialise the preferences dialog
	/*
	 * NB! this has to be done after all tabs were created (Preview Browser is
	 * used as an owner of the preferences dialog because Preview Browser is the
	 * only JFrame in the application - in Java 1.5 it is only possible to set
	 * an icon to a JFrame and all 'children' dialog of it get the same icon -
	 * so essentially, this is only to set the myExperiment logo as an icon of
	 * preferences dialog.)
	 */
	this.jdPreferences = new PluginPreferencesDialog(this.getPreviewBrowser(), this, myExperimentClient, logger);
  }

  private void initialiseData() {
	this.logger.debug("Initialising myExperiment Perspective data");

	// check if 'auto-login' is required (NB! This requires the BASE_URL to be
	// set correctly!)
	Object oAutoLogin = this.myExperimentClient.getSettings().get(MyExperimentClient.INI_AUTO_LOGIN);
	if (oAutoLogin != null && oAutoLogin.equals("true")) {
	  this.getStatusBar().setStatus(this.getMyStuffTab().getClass().getName(), "Performing autologin");
	  this.myExperimentClient.doLoginFromStoredCredentials();
	  this.getStatusBar().setStatus(this.getMyStuffTab().getClass().getName(), "Autologin finished. Fetching user data");
	}

	// NB! This should only be done if the user is logged in -
	// otherwise this component simply doesn't exist
	//    this.pMyStuffContainer.spMyStuff.setDividerLocation(0.3);

	// load data into all tabs
	this.pMyStuffContainer.createAndInitialiseInnerComponents();
	if (this.myExperimentClient.isLoggedIn()) {
	  // set the default tab for logged in user (e.g. as a consequence of auto-login)
	  tpMainTabs.setSelectedIndex(Integer.parseInt(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_DEFAULT_LOGGED_IN_TAB)));

	  // auto-login was successful - can display user tags
	  // (no need to refresh this cloud on its own, because the whole tab
	  // is refreshed immediately after)
	  this.pTagBrowser.setMyTagsShown(true);
	} else {
	  // set the default tab for anonymous user (auto-login failed or wasn't chosen)
	  tpMainTabs.setSelectedIndex(Integer.parseInt(myExperimentClient.getSettings().getProperty(MyExperimentClient.INI_DEFAULT_ANONYMOUS_TAB)));
	}

	this.pExampleWorkflows.refresh();
	this.pTagBrowser.refresh();
  }

  public void stateChanged(ChangeEvent e) {
	// invoked when a tab is opened
	if (e.getSource().equals(this.tpMainTabs)) {
	  this.getStatusBar().displayStatus(this.getMainTabs().getSelectedComponent().getClass().getName());
	}
  }

  // ************** ACTIONS ***************
  public class PreviewResourceAction extends AbstractAction {
	private int iResourceType = Resource.UNKNOWN;
	private String strResourceURI = "";

	public PreviewResourceAction(int iResourceType, String strResourceURI) {
	  putValue(SMALL_ICON, WorkbenchIcons.zoomIcon);
	  putValue(NAME, "Preview");
	  putValue(SHORT_DESCRIPTION, "Preview this "
		  + Resource.getResourceTypeName(iResourceType).toLowerCase()
		  + " in the Preview Browser window");

	  this.iResourceType = iResourceType;
	  this.strResourceURI = strResourceURI;
	}

	public void actionPerformed(ActionEvent actionEvent) {
	  getPreviewBrowser().preview("preview:" + this.iResourceType + ":"
		  + this.strResourceURI);
	}
  }

  public class DownloadResourceAction extends AbstractAction {
	private Resource resource = null;

	public DownloadResourceAction(Resource resource) {
	  this(resource, true);
	}

	public DownloadResourceAction(Resource resource, boolean bShowButtonLabel) {
	  this.resource = resource;
	  String strResourceType = resource.getItemTypeName().toLowerCase();

	  // in either case the icon is the same; label might be displayed - based
	  // on the parameter
	  putValue(SMALL_ICON, WorkbenchIcons.saveIcon);
	  if (bShowButtonLabel)
		putValue(NAME, "Download");

	  String strTooltip = "Downloading " + strResourceType
		  + "s is currently not possible";
	  boolean bDownloadAllowed = false;
	  if (resource.isDownloadable()) {
		if (resource.isDownloadAllowed()) {
		  strTooltip = "Download this " + strResourceType
			  + " and store it locally";
		  bDownloadAllowed = true;
		} else {
		  strTooltip = "You don't have permissions to download this "
			  + strResourceType;
		}
	  }

	  setEnabled(bDownloadAllowed);
	  putValue(SHORT_DESCRIPTION, strTooltip);
	}

	public void actionPerformed(ActionEvent actionEvent) {
	  try {
		BrowserLauncher launcher = new BrowserLauncher();
		launcher.openURLinBrowser(resource.getResource() + "/download");

		// update downloaded items history making sure that:
		// - there's only one occurrence of this item in the history;
		// - if this item was in the history before, it is moved to the 'top'
		// now;
		// - predefined history size is not exceeded
		getHistoryBrowser().getDownloadedItemsHistoryList().remove(resource);
		getHistoryBrowser().getDownloadedItemsHistoryList().add(resource);
		if (getHistoryBrowser().getDownloadedItemsHistoryList().size() > HistoryBrowserTabContentPanel.DOWNLOADED_ITEMS_HISTORY_LENGTH) {
		  getHistoryBrowser().getDownloadedItemsHistoryList().remove(0);
		}

		// now update the downloaded items history panel in 'History' tab
		if (getHistoryBrowser() != null) {
		  getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.DOWNLOADED_ITEMS_HISTORY);
		}
	  } catch (Exception ex) {
		logger.error("Failed while trying to open download URL in a standard browser; URL was: "
			+ resource.getURI() + "\nException was: " + ex);
	  }
	}
  }

  public class LoadResourceInTavernaAction extends AbstractAction {
	private final Resource resource;

	public LoadResourceInTavernaAction(Resource resource) {
	  this(resource, true);
	}

	public LoadResourceInTavernaAction(Resource resource, boolean bShowButtonLabel) {
	  this.resource = resource;
	  String strResourceType = resource.getItemTypeName().toLowerCase();

	  putValue(SMALL_ICON, WorkbenchIcons.openIcon);
	  if (bShowButtonLabel)
		putValue(NAME, "Open");

	  boolean bLoadingAllowed = false;
	  String strTooltip = "Loading " + strResourceType
		  + "s into Taverna Workbench is currently not possible";
	  if (resource.getItemType() == Resource.WORKFLOW) {
		if (((Workflow) resource).isTavernaWorkflow()) {
		  if (resource.isDownloadAllowed()) {
			// Taverna workflow and download allowed - can load in Taverna
			bLoadingAllowed = true;
			strTooltip = "Download and load this workflow in Design mode of Taverna Workbench";
		  } else {
			strTooltip = "You don't have permissions to download this workflow, and thus to load into Taverna Workbench";
		  }
		} else {
		  strTooltip = "Loading workflow of unsupported type into Taverna Workbench is not possible.";
		}
	  }

	  setEnabled(bLoadingAllowed);
	  putValue(SHORT_DESCRIPTION, strTooltip);

	}

	public void actionPerformed(ActionEvent actionEvent) {
	  // if the preview browser window is opened, hide it beneath the main window
	  if (getPreviewBrowser().isActive())
		getPreviewBrowser().toBack();

	  final String strCallerTabClassName = getMainTabs().getSelectedComponent().getClass().getName();
	  getStatusBar().setStatus(strCallerTabClassName, "Downloading and opening workflow...");
	  logger.debug("Downloading and opening workflow from URI: "
		  + resource.getURI());

	  new Thread("Download and open workflow") {
		@Override
		public void run() {
		  try {
			Workflow w = myExperimentClient.fetchWorkflowBinary(resource.getURI());
			ByteArrayInputStream workflowDataInputStream = new ByteArrayInputStream(w.getContent());

			FileManager fileManager = FileManager.getInstance();
			FileType fileTypeType = (w.isTaverna1Workflow() ? new ScuflFileType() : new T2FlowFileType());
			Dataflow openDataflow = fileManager.openDataflow(fileTypeType, workflowDataInputStream);
		  } catch (Exception e) {
			javax.swing.JOptionPane.showMessageDialog(null, "An error has occurred while trying to load a workflow from myExperiment.\n\n"
				+ e, "Error", JOptionPane.ERROR_MESSAGE);
			logger.error("Failed to open connection to URL to download and open workflow, from myExperiment.", e);
		  }

		  getStatusBar().setStatus(strCallerTabClassName, null);

		  // update opened items history making sure that:
		  // - there's only one occurrence of this item in the history;
		  // - if this item was in the history before, it is moved to the 'top' now; 
		  // - predefined history size is not exceeded
		  getHistoryBrowser().getOpenedItemsHistoryList().remove(resource);
		  getHistoryBrowser().getOpenedItemsHistoryList().add(resource);
		  if (getHistoryBrowser().getOpenedItemsHistoryList().size() > HistoryBrowserTabContentPanel.OPENED_ITEMS_HISTORY_LENGTH) {
			getHistoryBrowser().getOpenedItemsHistoryList().remove(0);
		  }

		  // now update the opened items history panel in 'History' tab
		  if (getHistoryBrowser() != null) {
			getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.OPENED_ITEMS_HISTORY);
		  }
		}
	  }.start();
	}
  }

  public class ImportIntoTavernaAction extends AbstractAction {
	private final Resource resource;
	private boolean importAsNesting;

	public ImportIntoTavernaAction(Resource r) {
	  this.resource = r;

	  String strResourceType = resource.getItemTypeName().toLowerCase();

	  putValue(SMALL_ICON, WorkbenchIcons.importIcon);
	  putValue(NAME, "Import");

	  boolean bLoadingAllowed = false;
	  String strTooltip = "Loading " + strResourceType
		  + "s into Taverna Workbench is currently not possible";
	  if (resource.getItemType() == Resource.WORKFLOW) {
		if (((Workflow) resource).isTavernaWorkflow()) {
		  if (resource.isDownloadAllowed()) {
			// Taverna workflow and download allowed - can load in Taverna
			bLoadingAllowed = true;
			strTooltip = "Import this workflow into one that is currently open in the Design mode of Taverna Workbench";
		  } else {
			strTooltip = "You don't have permissions to download this workflow, and thus to load into Taverna Workbench";
		  }
		} else {
		  strTooltip = "Loading workflow of unsupported type into Taverna Workbench is not possible.";
		}
	  }

	  setEnabled(bLoadingAllowed);
	  putValue(SHORT_DESCRIPTION, strTooltip);
	}

	private final FileManager fileManager = FileManager.getInstance();

	public void actionPerformed(ActionEvent actionEvent) {
	  // if the preview browser window is opened, hide it beneath the main window
	  if (getPreviewBrowser().isActive())
		getPreviewBrowser().toBack();

	  ImportWorkflowWizard importWorkflowDialog = new ImportWorkflowWizard(getPreviewBrowser());

	  Workflow w;
	  try {
		w = MY_EXPERIMENT_CLIENT.fetchWorkflowBinary(resource.getURI());
	  } catch (Exception e) {
		JOptionPane.showMessageDialog(null, "An error has occurred "
			+ "while trying to load a " + "workflow from myExperiment.\n\n" + e, "Error", JOptionPane.ERROR_MESSAGE);
		LOGGER.error("Failed to open connection to URL to "
			+ "download and open workflow, from myExperiment.", e);
		return;
	  }
	  ByteArrayInputStream workflowDataInputStream = new ByteArrayInputStream(w.getContent());
	  FileType fileTypeType = (w.isTaverna1Workflow() ? new MainComponent.ScuflFileType() : new MainComponent.T2FlowFileType());
	  Dataflow toBeImported;
	  try {
		toBeImported = fileManager.openDataflowSilently(fileTypeType, workflowDataInputStream).getDataflow();
	  } catch (OpenException e) {
		JOptionPane.showMessageDialog(null, "An error has occurred"
			+ " while trying to load a " + "workflow from myExperiment.\n\n"
			+ e, "Error", JOptionPane.ERROR_MESSAGE);
		LOGGER.error("Failed to" + " open connection to URL "
			+ "to download and open workflow, from myExperiment.", e);
		return;
	  }
	  importWorkflowDialog.setCustomSourceDataflow(toBeImported, "From myExperiment: "
		  + resource.getTitle());
	  importWorkflowDialog.setSourceEnabled(false);
	  importWorkflowDialog.setVisible(true);

	  // update opened items history making sure that:
	  // - there's only one occurrence of this item in the history;
	  // - if this item was in the history before, it is moved to the 'top' now; 
	  // - predefined history size is not exceeded
	  getHistoryBrowser().getOpenedItemsHistoryList().remove(resource);
	  getHistoryBrowser().getOpenedItemsHistoryList().add(resource);
	  if (getHistoryBrowser().getOpenedItemsHistoryList().size() > HistoryBrowserTabContentPanel.OPENED_ITEMS_HISTORY_LENGTH) {
		getHistoryBrowser().getOpenedItemsHistoryList().remove(0);
	  }

	  // now update the opened items history panel in 'History' tab
	  if (getHistoryBrowser() != null)
		getHistoryBrowser().refreshHistoryBox(HistoryBrowserTabContentPanel.OPENED_ITEMS_HISTORY);
	}

  }

  // *** FileTypes for opening workflows inside Taverna

  public static class ScuflFileType extends FileType {

	@Override
	public String getDescription() {
	  return "Taverna 1 SCUFL workflow";
	}

	@Override
	public String getExtension() {
	  return "xml";
	}

	@Override
	public String getMimeType() {
	  return "application/vnd.taverna.scufl+xml";
	}
  }

  public static class T2FlowFileType extends FileType {
	@Override
	public String getDescription() {
	  return "Taverna 2 workflow";
	}

	@Override
	public String getExtension() {
	  return "t2flow";
	}

	@Override
	public String getMimeType() {
	  // "application/vnd.taverna.t2flow+xml";
	  return XMLSerializationConstants.WORKFLOW_DOCUMENT_MIMETYPE;
	}
  }
}
