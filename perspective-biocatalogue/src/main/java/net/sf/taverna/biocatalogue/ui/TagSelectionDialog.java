package net.sf.taverna.biocatalogue.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.Tag;
import net.sf.taverna.biocatalogue.model.search.SearchOptions;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;
import net.sf.taverna.t2.workbench.MainWindow;


/**
 * 
 * @author Sergejs Aleksejevs
 */
public class TagSelectionDialog extends JDialog
{
  private JDialog thisDialog;
  
  private TagCloudPanel tagCloudPanel;
  
  private JButton bSearch;
  private JButton bCancel;
  
  
  public TagSelectionDialog(final List<TYPE> resourceTypesToSearchFor)
  {
    super(MainComponent.dummyOwnerJFrame, true);
    this.setTitle("BioCatalogue Plugin");
    
    this.thisDialog = this;
    
    tagCloudPanel = new TagCloudPanel("Tag Cloud", TagCloudPanel.TAGCLOUD_TYPE_GENERAL, 
        TagCloudPanel.TAGCLOUD_MULTIPLE_SELECTION, new ActionListener() {
                                                      public void actionPerformed(ActionEvent e) {
                                                        /* This is known to work, no need to do anything on tag selection/deselection;
                                                         * To test: System.out.println(tagCloudPanel.getCurrentlySelectedTags()); */
                                                      }
                                                    });
    
    bSearch = new JButton("Search");
    bSearch.setPreferredSize(new Dimension(bSearch.getPreferredSize().width * 2, 
                                           bSearch.getPreferredSize().height));
    bSearch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        List<Tag> selectedTags = tagCloudPanel.getCurrentlySelectedTags();
        if (selectedTags.size() == 0) {
          JOptionPane.showMessageDialog(thisDialog, "Please select at least one tag to proceed",
              "BioCatalogue Plugin", JOptionPane.WARNING_MESSAGE);
        }
        else {
          // start the tag search and close the dialog
          MainComponentFactory.getSharedInstance().getBioCatalogueExplorationTab().getTabbedSearchResultsPanel().
              startNewSearch(new SearchOptions(tagCloudPanel.getCurrentlySelectedTags(), resourceTypesToSearchFor));
          thisDialog.dispose();
        }
      }
    });
    
    bCancel = new JButton("Cancel");
    bCancel.setPreferredSize(bSearch.getPreferredSize());
    bCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        thisDialog.setVisible(false);
        thisDialog.dispose();
      }
    });
    
    JPanel jpButtons = new JPanel();
    jpButtons.add(bSearch);
    jpButtons.add(bCancel);
    
    
    this.setLayout(new BorderLayout());
    this.add(tagCloudPanel, BorderLayout.CENTER);
    this.add(jpButtons, BorderLayout.SOUTH);
    
    
    this.setSize(new Dimension(800,600));
    this.setLocationRelativeTo(MainWindow.getMainWindow());  // important for multiple screens!!
    
    initialiseData();
  }


  private void initialiseData()
  {
    tagCloudPanel.refresh();
  }
}
