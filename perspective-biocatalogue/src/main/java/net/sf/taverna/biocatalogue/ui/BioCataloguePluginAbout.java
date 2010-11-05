package net.sf.taverna.biocatalogue.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;

import net.sf.taverna.biocatalogue.model.StringToInputStreamConverter;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;
import net.sf.taverna.t2.workbench.helper.HelpCollator;
import net.sf.taverna.t2.workbench.helper.Helper;

import org.apache.log4j.Logger;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;


/**
 * This class generates contents of the "About" tab in the perspective.
 * 
 * @author Sergejs Aleksejevs
 */
public class BioCataloguePluginAbout extends JPanel implements HasDefaultFocusCapability
{
  private static final String TAB_BASE_TITLE = "About";
  
  private final MainComponent pluginPerspectiveMainComponent;
  private final BioCatalogueClient client;
  private final Logger logger;
  
  private BioCataloguePluginAbout thisPanel = null;
  
  
  public BioCataloguePluginAbout(MainComponent pluginPerspectiveMainComponent, BioCatalogueClient client, Logger logger)
  {
    this.thisPanel = this;
    
    this.pluginPerspectiveMainComponent = pluginPerspectiveMainComponent;
    this.client = client;
    this.logger = logger;
    
    this.setLayout(new BorderLayout());
    try {
      initializeUI();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    // TODO -- enable the following block to focus required component properly on tab switch in the perspective
    // this is to make sure that search will get focused when this tab is opened
    // -- is a workaround to a bug in JVM
//    setFocusCycleRoot(true);
//    setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
//      public Component getDefaultComponent(Container cont) {
//          return (thisPanel.getDefaultComponent());
//      }
//    });
  }
  
  
  private void initializeUI() throws Exception
  {
    StringBuilder sbAbout = new StringBuilder();
    sbAbout.append("<html>" +
                     "<body>" +
                       "<h2>BioCatalogue Plugin - version: 0.1 (alpha)</h2>" +
                       "<p>" +
                         "The BioCatalogue plugin is intended to provide access to the data held in the " +
                         "BioCatalogue Web Services Registry directly from Taverna Workbench. For more information " +
                         "please see help pages by clicking the \"View Help\" button above. Also, please leave your " +
                         "comments and suggestions by clicking \"Leave Feedback\" button." +
                       "</p>" +
                       "<p>" +
                         "<b>Note:</b> This is an incomplete version of the BioCatalogue plugin. " +
                         "You may see notifications that certain pieces of functionality have not been " +
                         "implemented yet; some features are not yet fully stable, which means that " +
                         "occasionally you may see unexpected error messages." +
                       "</p>" +
                       "<p>" +
                         "This version of the plugin was developed by <b>Sergejs Aleksejevs</b> as part of his " +
                         "final year project on the undergraduate Computer Science course at the University of Manchester." +
                       "</p>" +
                       "<hr/>" +
                       "<p style=\"margin-top: 3em;\">" +
                         "The BioCatalogue plugin uses Silk icon set (v1.3) by Mark James:<br/>" +
                         "http://www.famfamfam.com/lab/icons/silk/" +
                     "</p>" +
                     "</body>" +
    		           "</html>");
    
    // About XHTML panel
    XHTMLPanel xhtmlAbout = new XHTMLPanel();
    xhtmlAbout.getSharedContext().getTextRenderer().setSmoothingThreshold(0); // Anti-aliasing for all font sizes
    for (Object o : xhtmlAbout.getMouseTrackingListeners()) {
      // remove all default link listeners, as we don't need the XHTMLPanel
      // to navigate to the 'new' page automatically via any clicked link
      if (o instanceof LinkListener) {
        xhtmlAbout.removeMouseTrackingListener((FSMouseListener)o);
      }
    }
    
    FSScrollPane xhtmlAboutScrollPane = new FSScrollPane(xhtmlAbout);
    xhtmlAboutScrollPane.setPreferredSize(new Dimension(400, 300));
    xhtmlAboutScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    xhtmlAboutScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    StringToInputStreamConverter converter = new StringToInputStreamConverter(sbAbout.toString());
    xhtmlAbout.setDocument(converter.getInputStream(), System.getProperty("user.dir"));
    xhtmlAbout.validate();
    converter.closeAllStreams();  // close all streams
    
    this.add(xhtmlAboutScrollPane, BorderLayout.CENTER);

    
    // create action buttons - Help and Feedback
    JButton bFeedback = new JButton("Leave Feedback");
    bFeedback.setToolTipText("<html>Clicking this button will open the web browser;<br>" +
    		                     "A web page with the feedback form will be displayed.</html>");
    bFeedback.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pluginPerspectiveMainComponent.getPreviewBrowser().
          openInWebBrowser("http://www.taverna.org.uk/about/contact-us/feedback?product=BioCataloguePlugin");
      }
    });
    
    final JButton bHelp = new JButton("View Help");
    bHelp.setPreferredSize(bFeedback.getPreferredSize());
    bHelp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Helper.showHelp(bHelp);
      }
    });
    HelpCollator.registerComponent(bHelp, "biocatalogue-plugin");
    
    JPanel jpActionButtons = new JPanel();
    jpActionButtons.add(bHelp);
    jpActionButtons.add(bFeedback);
    this.add(jpActionButtons, BorderLayout.NORTH);
    
    
    // set default title for the current tab
    pluginPerspectiveMainComponent.setWindowTitle(this.getClass().getName(), TAB_BASE_TITLE);
  }
  
  
  // *** Callbacks for HasDefaultFocusCapability interface ***
  
  public void focusDefaultComponent() {
    // TODO
  }
  
  public Component getDefaultComponent() {
    // TODO
    return null;
  }
  
}
