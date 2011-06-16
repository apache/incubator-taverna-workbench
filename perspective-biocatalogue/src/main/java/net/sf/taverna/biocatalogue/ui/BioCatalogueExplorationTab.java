package net.sf.taverna.biocatalogue.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LayoutFocusTraversalPolicy;

import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.ui.search_results.SearchResultsMainPanel;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;

import org.apache.log4j.Logger;


/**
 * 
 * @author Sergejs Aleksejevs
 */
@SuppressWarnings("serial")
public class BioCatalogueExplorationTab extends JPanel implements HasDefaultFocusCapability
{
  private final MainComponent pluginPerspectiveMainComponent;
  private final BioCatalogueClient client;
  private final Logger logger;
  
  
  // COMPONENTS
  private BioCatalogueExplorationTab thisPanel;
  
  private SearchOptionsPanel searchOptionsPanel;
  private SearchResultsMainPanel tabbedSearchResultsPanel;
  
  
  public BioCatalogueExplorationTab()
  {
    this.thisPanel = this;
    
    this.pluginPerspectiveMainComponent = MainComponentFactory.getSharedInstance();
    this.client = BioCatalogueClient.getInstance();
    this.logger = Logger.getLogger(this.getClass());
    
    initialiseUI();
    
    // this is to make sure that search will get focused when this tab is opened
    // -- is a workaround to a bug in JVM
    setFocusCycleRoot(true);
    setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
      public Component getDefaultComponent(Container cont) {
          return (thisPanel.getDefaultComponent());
      }
    });
  }
  
  
  private void initialiseUI()
  {
    this.tabbedSearchResultsPanel = new SearchResultsMainPanel();
    this.searchOptionsPanel = new SearchOptionsPanel(tabbedSearchResultsPanel);
    
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.EAST;
    
    this.add(searchOptionsPanel, c);
    
    c.gridy++;
    c.weightx = c.weighty = 1.0;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    this.add(tabbedSearchResultsPanel, c);
    
    this.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
  }
  
  
  public SearchResultsMainPanel getTabbedSearchResultsPanel() {
    return tabbedSearchResultsPanel;
  }
  
  
  
  // *** Callbacks for HasDefaultFocusCapability interface ***
  
  public void focusDefaultComponent() {
    this.searchOptionsPanel.focusDefaultComponent();
  }
  
  public Component getDefaultComponent() {
    return (this.searchOptionsPanel.getDefaultComponent());
  }
  
  // *********************************************************
  
  
  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.getContentPane().add(new BioCatalogueExplorationTab());
    f.setSize(1000, 800);
    f.setLocationRelativeTo(null);
    
    f.setVisible(true);
  }
}
