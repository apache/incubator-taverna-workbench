package net.sf.taverna.t2.ui.perspectives.biocatalogue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.Util;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.ui.BioCatalogueExplorationTab;
import net.sf.taverna.biocatalogue.ui.BioCataloguePluginAbout;
import net.sf.taverna.biocatalogue.ui.HasDefaultFocusCapability;
import net.sf.taverna.biocatalogue.ui.previews.ResourcePreviewBrowser;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/*
 * @author Sergejs Aleksejevs
 */
public final class MainComponent extends JPanel implements UIComponentSPI, ChangeListener
{
  private static final String windowBaseTitle = "BioCatalogue API Demo";
  private HashMap<String, String> windowTitleMap;
  
  private MainComponent pluginPerspectiveMainComponent;
  private BioCatalogueClient client;
  private ResourcePreviewBrowser previewBrowser;
  private final Logger logger = Logger.getLogger(MainComponent.class);
  
  private JTabbedPane tpMainTabs;
  private BioCatalogueExplorationTab jpBioCatalogueExplorationTab;
  private BioCataloguePluginAbout jpAboutTab;
  
  public static JFrame dummyOwnerJFrame;
  static {
    // this is only to have a nice icon on all Dialog boxes - can be removed at any time
    dummyOwnerJFrame = new JFrame();
    dummyOwnerJFrame.setIconImage(ResourceManager.getImageIcon(ResourceManager.FAVICON).getImage());
  }
  
  
  /**
   * This constructor is protected, and so is only available to the classes in its package -
   * i.e. Taverna integration classes. Other parts of the plugin must use <code>MainComponentFactory.getSharedInstance()</code>
   * to get the shared instance of this class.
   */
	protected MainComponent()
	{
	  super();
	  initialiseEnvironment();
	  initialisePerspectiveUI();
	}
	
	
  // *** Methods implementing UIComponentSPI interface ***
	
	public ImageIcon getIcon() {
		return WorkbenchIcons.databaseIcon;
	}

	@Override
	public String getName() {
		return "myExperiment Perspective Main Component";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
	}

	public void onDispose() {
		// TODO Auto-generated method stub
	}
	
	// *** End of methods implementing UIComponentSPI interface ***
	
	
	private void initialiseEnvironment()
	{
	  // before anything else - store a reference to self for use during
	  // initialisation of other components
	  pluginPerspectiveMainComponent = this;
	  
	  
	  // pre-load classes for FlyingSaucer XHTML renderer - this will make sure
    // that the first time it is used, there will be no delay while classes
    // are loaded by Java
    new Thread("class pre-loading") {
      public void run() {
        try {
          Class.forName("org.xhtmlrenderer.simple.FSScrollPane");
          Class.forName("org.xhtmlrenderer.simple.XHTMLPanel");
        }
        catch (ClassNotFoundException e) {
          System.err.println("\nProblem while pre-loading classes for FlyingSaucer XHTML renderer... Error message below:");
          e.printStackTrace();
        }
      }
    }.start();
    
    
    // register the thread which will perform shutdown operations 
    if (!Util.isRunningInTaverna())
    {
      // NB! This only needs to be done when running outside Taverna, because
      // ShutdownSPI would automatically replace this in that case.
      Runtime.getRuntime().addShutdownHook(new MainComponentShutdownHook.MyExperimentClientShutdownThread());
    }
    
    
    // determine what folder is to be used for config files
    if (Util.isRunningInTaverna()) {
      // running inside Taverna - use its default folders for config files and log files
      BioCataloguePluginConstants.CONFIG_FILE_FOLDER =
        new java.io.File(ApplicationRuntime.getInstance().getApplicationHomeDir(), "conf");
      BioCataloguePluginConstants.LOG_FILE_FOLDER =
        new java.io.File(ApplicationRuntime.getInstance().getApplicationHomeDir(), "logs");
    }
    else {
      // running outside Taverna, place config file and log into the user's home directory
      BioCataloguePluginConstants.CONFIG_FILE_FOLDER = 
        new java.io.File(System.getProperty("user.home"), BioCataloguePluginConstants.CONFIG_FILE_FOLDER_WHEN_RUNNING_STANDALONE);
      BioCataloguePluginConstants.LOG_FILE_FOLDER = 
        new java.io.File(System.getProperty("user.home"), BioCataloguePluginConstants.CONFIG_FILE_FOLDER_WHEN_RUNNING_STANDALONE);
    }
    
    
    // this makes sure that tooltips will stay displayed for longer than default 
    ToolTipManager.sharedInstance().setDismissDelay(BioCataloguePluginConstants.DEFAULT_TOOLTIP_DURATION);
    
    // these components must be accessed by all other components, hence need
    // to be initialised before any other initialisation is done
    client = new BioCatalogueClient();
    previewBrowser = new ResourcePreviewBrowser(pluginPerspectiveMainComponent, client, logger);
    windowTitleMap = new HashMap<String,String>();
	}
	
	private void initialisePerspectiveUI()
	{
	  // set the loader icon to show that the perspective is loading
	  this.setLayout(new GridLayout(1,1));
	  this.add(new JLabel(ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_ORANGE)));
	  
	  new Thread("Initialise BioCatalogue Perspective UI")
	  {
	    public void run() {
	      // create all tabs prior to putting them inside the tabbed pane
	      jpBioCatalogueExplorationTab = new BioCatalogueExplorationTab();
//	      jpServiceFilteringTab = new ServiceFilteringTab(pluginPerspectiveMainComponent, client, logger);
//	      jpSearchTab = new SearchTab(pluginPerspectiveMainComponent, client, logger);
	      jpAboutTab = new BioCataloguePluginAbout(pluginPerspectiveMainComponent, client, logger);
	      
	      // create main tabs
	      tpMainTabs = new JTabbedPane();
	      tpMainTabs.add("Explore BioCatalogue", jpBioCatalogueExplorationTab);
//	      tpMainTabs.add("Search", jpSearchTab);
//	      tpMainTabs.add("Filter Services", jpServiceFilteringTab);
	      tpMainTabs.add("About", jpAboutTab);
	      
	      SwingUtilities.invokeLater(new Runnable() {
          public void run()
          {
            // add main tabs and the status bar into the perspective
            pluginPerspectiveMainComponent.removeAll();
            pluginPerspectiveMainComponent.setLayout(new BorderLayout());
            pluginPerspectiveMainComponent.add(tpMainTabs, BorderLayout.CENTER);
            
            // everything is prepared -- need to focus default component on the first tab
            // (artificially generate change event on the tabbed pane to perform focus request)
            tpMainTabs.setSelectedIndex(1);
            tpMainTabs.addChangeListener(pluginPerspectiveMainComponent);
            tpMainTabs.setSelectedIndex(0);
          }
        });
	    }
	  }.start();
	}
	
	/**
   * Determines whether the specified tab is currently active in the main tabbed pane.
   * @param strTabClassName Class name of the tab which is tested for being active.
   * @return True if specified tab is currently active.
   */
  private boolean isTabActive(String strTabClassName)
  {
    if (tpMainTabs == null) return (false);
    
    // if an anonymous thread within the main tab component class will call
    // this method, we want to store the main tab's class, rather than
    // the full class name of the anonymous worker thread
    String strCurSelectedTabClassName = tpMainTabs.getSelectedComponent().getClass().getName();
    
    // get the real class name to match
    String strBaseClassName = Util.getBaseClassName(strTabClassName);
    
    // compare the two class names
    return (strBaseClassName.equals(strCurSelectedTabClassName));
  }
  
  
  /**
   * This method "selects" the tab represented by the component provided as a parameter.
   * 
   * @param c Component that represents one of the tabs in the main tabbed pane.
   *          If <code>c</code> is not found within the components of the tabbed pane, nothing will happen.
   */
  public void setTabActive(Component c)
  {
    try {
      tpMainTabs.setSelectedComponent(c);
    }
    catch (IllegalArgumentException e) {
      // do nothing, can't activate component which is not in the tabbed pane
    }
  }
  
  
  /**
   * Sets title of the main perspective window for a specified tab,
   * thus supporting different window titles for different tabs;
   * new title will only be displayed immediately if the specified tab.
   * 
   * @param strTabClassName Class name of the tab, for which window title should be updated.
   * @param strTitle New title to set.
   */
  public void setWindowTitle(String strTabClassName, String strTitle)
  {
    // if an anonymous thread within the main tab component class will call
    // this method, we want to store the main tab's class, rather than
    // the full class name of the anonymous worker thread
    String strBaseClassName = Util.getBaseClassName(strTabClassName);
    
    // store the new title for for the specified tab
    windowTitleMap.put(strBaseClassName, strTitle);
    
    // if the specified tab is active, update window title immediately
    if (isTabActive(strBaseClassName)) displayWindowTitle(strBaseClassName);
  }
  
  
  /** 
   * Displays window title that corresponds to specified tab.
   * 
   * @param strTabClassName Class name of the tab for which the window title is to be set.
   */
  public void displayWindowTitle(String strTabClassName)
  {
    // if an anonymous thread within the main tab component class will call
    // this method, we want to store the main tab's class, rather than
    // the full class name of the anonymous worker thread
    String strBaseClassName = Util.getBaseClassName(strTabClassName);
    
    if (windowTitleMap.containsKey(strBaseClassName)) {
      // title for specified tab found - show it
      // TODO - disabled until this info will be shown in the status bar
      //this.setTitle(windowBaseTitle + " :: " + windowTitleMap.get(strBaseClassName));
    }
    else {
      // tab not found - display standard title
      // TODO - disabled until this info will be shown in the status bar
      //this.setTitle(windowBaseTitle);
    }
  }
  
  
  // *** Getters for various components ***
  
  /**
   * @return Reference to the component that represents the BioCatalogue Exploration
   *         tab in the BioCatalogue Perspective.
   */
  public BioCatalogueExplorationTab getBioCatalogueExplorationTab() {
    return (this.jpBioCatalogueExplorationTab);
  }
  
  
  /**
   * @return An initialised and ready to use instance of BioCatalogue client or null.
   */
  public BioCatalogueClient getBioCatalogueClient() {
    return (this.client);
  }
  
  /**
   * @return An initialised and ready to use instance of <code>ResourcePreviewBrowser</code>.
   */
  public ResourcePreviewBrowser getPreviewBrowser() {
    return (this.previewBrowser);
  }
  
  
  // *** Callbacks for ChangeListener interface ***
  
  public void stateChanged(ChangeEvent e)
  {
    if (e.getSource().equals(tpMainTabs)) {
      // will get called when selected tab changes - need to focus default component in the active tab
      // and also set a correct window title
      if (tpMainTabs.getSelectedComponent() instanceof HasDefaultFocusCapability) {
        ((HasDefaultFocusCapability)tpMainTabs.getSelectedComponent()).focusDefaultComponent();
        this.displayWindowTitle(tpMainTabs.getSelectedComponent().getClass().getName());
      }
    }
  }
	
}
