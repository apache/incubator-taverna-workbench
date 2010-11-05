package net.sf.taverna.biocatalogue.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.biocatalogue.model.Util;

/**
 * @author Sergejs Aleksejevs
 */
public class HistoryOrFavouritesBlock extends JPanel
{
  private final String panelTitle;
  private final String emptyCollectionMessage;
  private final int maximumObjectCollectionSize;
  private final HistoryOrFavoritesBlockEntryDetailsProvider detailsProvider;
  
  private LinkedList llObjectCollection;
  
  private Set<HistoryOrFavouritesBlockObjectCollectionChangeListener> collectionChangeListeners;
  
  
  public HistoryOrFavouritesBlock(String panelTitle, String emptyCollectionMessage, int maximumObjectCollectionSize,
                                  HistoryOrFavoritesBlockEntryDetailsProvider detailsProvider)
  {
    this.panelTitle = panelTitle;
    this.emptyCollectionMessage = emptyCollectionMessage;
    this.maximumObjectCollectionSize = maximumObjectCollectionSize;
    this.detailsProvider = detailsProvider;
    
    this.llObjectCollection = new LinkedList();
    
    this.collectionChangeListeners = new HashSet<HistoryOrFavouritesBlockObjectCollectionChangeListener>();
    
    initialiseUI();
  }
  
  
  /**
   * NB! Callers of this method must know that listeners of changes to
   *     object collection will not be notified if a change is made directly
   *     via collection reference.
   * 
   * Local methods must be used whenever possible.
   * 
   * @return
   */
  protected List getObjectCollection() {
    return (this.llObjectCollection);
  }
  
  
  public void setObjectCollection(LinkedList newList) {
    this.llObjectCollection = newList;
  }
  
  
  public void addObjectCollectionChangeListener(HistoryOrFavouritesBlockObjectCollectionChangeListener listener) {
    this.collectionChangeListeners.add(listener);
  }
  
  private void notifyObjectCollectionChangeListeners() {
    for (HistoryOrFavouritesBlockObjectCollectionChangeListener listener : this.collectionChangeListeners) {
      listener.objectCollectionChanged(this);
    }
  }
  
  
  private void initialiseUI()
  {
    this.setPreferredSize(new Dimension(0, 200));       // HACK: this is to make sure that the scroll pane which wraps
                                                        // this panel only acts as a vertical scroll bar; this makes the
                                                        // scroll pane think that the element is not requiring more space
                                                        // and hence no horizontal scrolling behaviour will be shown
    
    this.setMaximumSize(new Dimension(1024, 0));        // HACK: this is to make sure that this panel isn't stretched vertically
    this.setLayout(new GridBagLayout());
    this.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " " + this.panelTitle + " "),
        BorderFactory.createEmptyBorder(0, 5, 5, 5)
    ));
  }
  
  
  public void updateUIFromObjectCollection()
  {
    this.removeAll();
    
    if (this.llObjectCollection.size() == 0)
    {
      GridBagConstraints c = new GridBagConstraints();
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.WEST;
      this.add(Util.generateNoneTextLabel(this.emptyCollectionMessage), c);
    }
    else
    {
      for(int i = this.llObjectCollection.size() - 1; i >= 0; i--) {
        Entry entryToDisplay = detailsProvider.provideEntryDetails(this, this.llObjectCollection.get(i), i);
        addEntryToPanel(entryToDisplay);
      }
    }
    
    this.repaint();
    this.revalidate();
    
    updatePreferredSize();
  }
  
  
  /**
   * Adds entry to the object collection (but doesn't update the UI).
   * 
   * @param objectToAdd
   */
  public void addObjectToCollection(Object objectToAdd, boolean sortCollection)
  {
    // check if such entry is already in the list
    int iDuplicateIdx = llObjectCollection.indexOf(objectToAdd);
    
    // only do the following if we have new instance list OR current instance is not the same
    // as the last one in the list
    if (llObjectCollection.size() == 0 || iDuplicateIdx != llObjectCollection.size() - 1)
    {
      // if the current item is already in the list, remove it (then re-add at the end of the list)
      if (iDuplicateIdx >= 0) llObjectCollection.remove(iDuplicateIdx);
      
      // we want to keep the list size constant, therefore when it reaches a certain
      // size, oldest element needs to be removed
      if (llObjectCollection.size() >= this.maximumObjectCollectionSize) llObjectCollection.remove();
      
      // in either case, add the new element to the tail of the list;
      llObjectCollection.offer(objectToAdd);
      
      // sort the collection if necessary
      if (sortCollection) {
        Collections.sort(this.llObjectCollection);
      }
      
      // notify collection listeners
      notifyObjectCollectionChangeListeners();
    }
  }
  
  
  public Object removeObjectFromCollectionAt(int objectIndex)
  {
    Object returnValue = this.llObjectCollection.remove(objectIndex);
    
    // notify collection listeners
    notifyObjectCollectionChangeListeners();
    
    return (returnValue);
  }
  
  
  public void clearObjectCollection()
  {
    this.llObjectCollection.clear();
    
    // notify collection listeners
    notifyObjectCollectionChangeListeners();
  }
  
  
  /**
   * Adds entry to the JPanel.
   */
  private void addEntryToPanel(Entry entryToAdd)
  {
    // little padding on the between the entry label and details
    entryToAdd.entryDetails.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    
    // grouping search details and search settings together
    JPanel jpCurentEntryWithDetails = new JPanel();
    jpCurentEntryWithDetails.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    
    c.anchor = GridBagConstraints.WEST;
    jpCurentEntryWithDetails.add(entryToAdd.entryLabel, c);
    c.weightx = 1.0;
    jpCurentEntryWithDetails.add(entryToAdd.entryDetails, c);
            
    // padding between entry details and the action icon
    entryToAdd.actionIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    
    
    // *** putting all pieces of current item together ***
    JPanel jpCurrentEntry = new JPanel();
    jpCurrentEntry.setLayout(new GridBagLayout());
    
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 1.0;
    jpCurrentEntry.add(jpCurentEntryWithDetails, c);
    
    c.anchor = GridBagConstraints.WEST;
    c.weightx = 0;
    jpCurrentEntry.add(entryToAdd.actionIcon, c);
    
    // adding current item to the history list 
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1.0;
    c.gridx = 0;
    c.gridy = GridBagConstraints.RELATIVE;
    this.add(jpCurrentEntry, c);
  }
  
  
  /**
   * This helper provides a solution for the difficulty with the scroll pane
   * around both search history and favourite searches. For the scroll pane
   * to work correctly it should "know" the preferred size of the panel it
   * wraps.
   * 
   * Number of items in the two panes change, hence we have to dynamically
   * adjust the preferred size of the surrounding panel.
   */
  private void updatePreferredSize()
  {
    // width of zero indicates that the whole of the width of the panel
    // "fits" anyway - and hence no horizontal scroll will appear either
    // way, because the scroll pane would "think" that the wrapped panel
    // doesn't require "more" width than the preferred size which is zero
    int iWidth = 0;
    
    
    // --- Height is something which we actually need to calculate; it depends on
    // the current contents of the panels inside ---
    
    // 32 is the height of the empty panel with border and padding
    int iHeight = 32; 
    
    // now - if the list of item is empty, add 28 pixels to height to cover the status message;
    // otherwise add default height of the JClickableLabel for each item in each of two lists
    iHeight += (llObjectCollection != null && llObjectCollection.size() > 0 ? JClickableLabel.DEFAULT_HEIGHT * llObjectCollection.size() : 28);
    
    // apply the calculated size
    this.setPreferredSize(new Dimension(iWidth, iHeight));
    this.validate();
  }
  
  
  public static class Entry
  {
    private final JClickableLabel entryLabel;
    private final JComponent entryDetails;
    private final JClickableLabel actionIcon;

    public Entry(JClickableLabel entryLabel, JComponent entryDetails, JClickableLabel actionIcon)
    {
      this.entryLabel = entryLabel;
      this.entryDetails = entryDetails;
      this.actionIcon = actionIcon;
    }
  }
  
}
