package net.sf.taverna.biocatalogue.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.search.SearchOptions;
import net.sf.taverna.biocatalogue.ui.search_results.SearchResultsMainPanel;
import net.sf.taverna.t2.lang.ui.DeselectingButton;


/**
 * 
 * @author Sergejs Aleksejevs
 */
@SuppressWarnings("serial")
public class SearchOptionsPanel extends JPanel implements HasDefaultFocusCapability
{
  // COMPONENTS
  private SearchOptionsPanel thisPanel;
  
private JTextField tfSearchQuery;
  private JButton bSearch;
  
  private final SearchResultsMainPanel tabbedSearchResultsPanel;
  
  
  public SearchOptionsPanel(SearchResultsMainPanel tabbedSearchResultsPanel)
  {
    super();
    this.thisPanel = this;
    this.tabbedSearchResultsPanel = tabbedSearchResultsPanel;
    
    this.initialiseUI();
  }
  
  
  private void initialiseUI()
  {
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    
    c.gridx = 0;
    c.gridy = 0;
       
    c.gridx++;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    this.tfSearchQuery = new JTextField(30);
    this.tfSearchQuery.setToolTipText(
        "<html>&nbsp;Tips for creating search queries:<br>" +
        "&nbsp;1) Use wildcards to make more flexible queries. Asterisk (<b>*</b>) matches any zero or more<br>" +
        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;characters (e.g. <b><i>Seq*</i></b> would match <b><i>Sequence</i></b>), question mark (<b>?</b>) matches any single<br>" +
        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;character (e.g. <b><i>Bla?t</i></b> would match <b><i>Blast</i></b>).<br>" +
        "&nbsp;2) Enclose the <b><i>\"search query\"</i></b> in double quotes to make exact phrase matching, otherwise<br>" +
        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;items that contain any (or all) words in the <b><i>search query</i></b> will be found.</html>");
    this.tfSearchQuery.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        tfSearchQuery.selectAll();
      }
      public void focusLost(FocusEvent e) { /* do nothing */ }
    });
    this.tfSearchQuery.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        // ENTER pressed - start search by simulating "search" button click
        // (only do this if the "search" button was active at that moment)
        if (e.getKeyCode() == KeyEvent.VK_ENTER && bSearch.isEnabled()) {    
          bSearch.doClick();
        }
      }
    });
    this.tfSearchQuery.addCaretListener(new CaretListener() {
      public void caretUpdate(CaretEvent e) {
        // enable search button if search query is present; disable otherwise
        bSearch.setEnabled(getSearchQuery().length() > 0);
      }
    });
    this.add(tfSearchQuery, c);
    
    
    // --- Search button ---
    
    c.gridx++;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    this.bSearch = new JButton("Search");
    this.bSearch.setEnabled(false);      // will be enabled automatically when search query is typed in
    this.bSearch.setToolTipText(tfSearchQuery.getToolTipText());
    this.bSearch.setPreferredSize(new Dimension(bSearch.getPreferredSize().width * 2, bSearch.getPreferredSize().height));
    this.bSearch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (getSearchQuery().length() == 0) {
          JOptionPane.showMessageDialog(null, "Please specify your search query", "Search - No search query", JOptionPane.WARNING_MESSAGE);
          thisPanel.focusDefaultComponent();
        }
        else {
          // search query available - collect data about the current search and execute it
          tabbedSearchResultsPanel.startNewSearch(thisPanel.getState());
        }
      }
    });
    this.add(bSearch, c);
    
}
   
  
  /**
   * Saves the current state of the search options into a single {@link SearchOptions} object.
   */
  public SearchOptions getState() {
    return (new SearchOptions(getSearchQuery(), Arrays.asList(TYPE.values())));
  }
  
  
  // *** GETTERS AND SETTERS ***
  
  public String getSearchQuery() {
    return (this.tfSearchQuery.getText().trim());
  }
  public void setSearchQuery(String strSearchQuery) {
    this.tfSearchQuery.setText(strSearchQuery);
  }
   
  
  // *** Callbacks for HasDefaultFocusCapability interface ***
  
  public void focusDefaultComponent() {
    this.tfSearchQuery.selectAll();
    this.tfSearchQuery.requestFocusInWindow();
  }
  
  public Component getDefaultComponent() {
    return(this.tfSearchQuery);
  }
  
}
