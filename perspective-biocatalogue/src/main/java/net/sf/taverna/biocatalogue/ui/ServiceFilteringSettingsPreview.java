package net.sf.taverna.biocatalogue.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.Util;
import net.sf.taverna.biocatalogue.model.search.ServiceFilteringSettings;
import net.sf.taverna.biocatalogue.ui.filtertree.FilterTreeNode;
import net.sf.taverna.biocatalogue.ui.tristatetree.JTriStateTree;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;

/**
 * @author Sergejs Aleksejevs
 */
public class ServiceFilteringSettingsPreview extends JDialog
{
  // CONSTANTS
  private static final int DIALOG_WIDTH = 400;
  private static final int DIALOG_HEIGHT = 300;
  
  private final ServiceFilteringSettingsPreview instanceOfSelf;
  private final MainComponent pluginPerspectiveMainComponent;
  private final ServiceFilteringSettings filteringSettings;
  
  
  public ServiceFilteringSettingsPreview(ServiceFilteringSettings filteringSettings)
  {
    super(MainComponent.dummyOwnerJFrame);
    
    this.instanceOfSelf = this;
    this.pluginPerspectiveMainComponent = MainComponentFactory.getSharedInstance();
    this.filteringSettings = filteringSettings;
    
    initialiseUI(this.getContentPane());
    
    // make sure that this window appears centered within the main app window
    // (NB! This must be done after the size of the window was calculated!)
    this.setLocationRelativeTo(pluginPerspectiveMainComponent);
  }
  
  
  private void initialiseUI(Container contentPane)
  {
    this.setModal(true);
    this.setTitle("Filter Preview");
    
    // prepare layout
    contentPane.setLayout(new BorderLayout());
    
    // OK button will close the dialog window
    JButton bOK = new JButton("OK");
    bOK.setPreferredSize(new Dimension((int)(bOK.getPreferredSize().width * 1.5), bOK.getPreferredSize().height));
    bOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        instanceOfSelf.dispose();
      }
    });
    bOK.setDefaultCapable(true);
    this.getRootPane().setDefaultButton(bOK);
    
    // action button panel
    JPanel jpButtonPanel = new JPanel(new GridBagLayout());
    jpButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0, 2, 0, 2);
    
    
    // if there are any filtering settings at all
    if (this.filteringSettings != null)
    {
      this.setTitle(this.getTitle() + " - " + this.filteringSettings.getFilterName());
      
      // display selected filters from the filter tree
      if (this.filteringSettings.getNumberOfFilteringCriteria() > 0)
      {
        // don't take the root node from the filtering settings directly - otherwise any changes
        // (namely removing all unchecked nodes) will also be applied to that instance; need the
        // shown tree to be completely independent of any other instance of this filter tree
        FilterTreeNode root = (FilterTreeNode)Util.deepCopy(this.filteringSettings.getFilterTreeRootsOfCheckedPaths().get(0).getPathComponent(0));
        
        // Create the tree view with the populated root
        JTriStateTree filterTree = new JTriStateTree(root);
        filterTree.setRootVisible(false);        // don't want the root to be visible; not a standard thing, so not implemented within JTriStateTree
        filterTree.setCheckingEnabled(false);    // only show this tree as read-only
        filterTree.removeAllUncheckedNodes();    // only show checked paths
        filterTree.expandAll();                  // there will be only a small number of nodes, expand all for convenience
        
        JScrollPane spFilters = new JScrollPane(filterTree);
        spFilters.setPreferredSize(new Dimension(300,300));
        spFilters.getVerticalScrollBar().setUnitIncrement(BioCataloguePluginConstants.DEFAULT_SCROLL);
        contentPane.add(spFilters, BorderLayout.CENTER);
      }
      else {
        // no selections in the tree - show a warning message
        JLabel jlMsg = new JLabel("There are no selected filtering criteria in the current filter.", JLabel.CENTER);
        jlMsg.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        contentPane.add(jlMsg, BorderLayout.CENTER);
      }
      
      if (this.filteringSettings.getNumberOfFilteringCriteria() > 0) {
        JButton bRestoreFilterSettings = new JButton("Load");
        bRestoreFilterSettings.setPreferredSize(bOK.getPreferredSize());
        bRestoreFilterSettings.setToolTipText("<html>Load settings of the previewed filter into the filter tree.<br>" +
        		                                  "Any previous selections in the tree will be lost.</html>");
        bRestoreFilterSettings.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e) {
            System.err.println("This only loads filtering settings, but not search term.");
            
            // FIXME - this must be changed in such a way that in re-loads filter tree on each
            //         active resource type tab in SearchResultsMainPanel  
//            pluginPerspectiveMainComponent.getServiceFilteringTab().getFilterTree().
//                restoreFilterCheckingSettings(filteringSettings.getFilterTreeRootsOfCheckedPaths());
            // FIXME - end of problem
          }
        });
        jpButtonPanel.add(bRestoreFilterSettings, c);
      }
    }
    else
    {
      // no filtering settings - just show the error message
      JLabel jlMsg = new JLabel("There are no filtering settings to preview", JLabel.CENTER);
      jlMsg.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
      contentPane.add(jlMsg, BorderLayout.CENTER);
    }
    
    
    jpButtonPanel.add(bOK, c);
    contentPane.add(jpButtonPanel, BorderLayout.SOUTH);
    
    this.pack();
    this.setMinimumSize(this.getPreferredSize());
  }
}
