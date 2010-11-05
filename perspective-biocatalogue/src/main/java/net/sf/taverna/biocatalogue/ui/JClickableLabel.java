package net.sf.taverna.biocatalogue.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * @author Sergejs Aleksejevs
 */
public class JClickableLabel extends JLabel implements MouseListener
{
  /**
   * Default height of the JClickableLabel - calculated based on 16-pixel
   * standard square icon and 3-pixel thick padding on top / bottom of element.
   */
  public static final int DEFAULT_HEIGHT = 22;
  
  public static final Color DEFAULT_REGULAR_FOREGROUND_COLOR = Color.BLUE;
  public static final Color DEFAULT_HOVER_FOREGROUND_COLOR = Color.RED;
  
  
  // This will hold the data which is relevant to processing the 'click' event on this label
  private final String strData;
  
  // This will hold a reference to ResourcePreviewBrowser instance that is supposed to process the clicks
  // on JClickableLabels
  private ActionListener clickHandler;
  
  
  private Color REGULAR_FOREGROUND_COLOR = DEFAULT_REGULAR_FOREGROUND_COLOR;
  private Color HOVER_FOREGROUND_COLOR = DEFAULT_HOVER_FOREGROUND_COLOR;
  
  
  public JClickableLabel(String strLabel, String strDataForAction, EventListener eventHandler)
  {
    this(strLabel, strDataForAction, eventHandler, null);
  }
  
  public JClickableLabel(String strLabel, String strDataForAction, EventListener eventHandler, Icon icon)
  {
    this(strLabel, strDataForAction, eventHandler, icon, SwingUtilities.LEFT);
  }
  
  public JClickableLabel(String strLabel, String strDataForAction, EventListener eventHandler, Icon icon, int horizontalAlignment)
  {
    this(strLabel, strDataForAction, eventHandler, icon, horizontalAlignment, null);
  }
  
  /**
   * 
   * @param strLabel Textual label that will be visible in the UI.
   * @param strDataForAction Data that will be passed to eventHandler when click on the label is made.
   * @param eventHandler ActionListener that will process clicks on this label.
   * @param icon Icon to display in the label.
   * @param horizontalAlignment This is one of SwingConstants: LEFT, CENTER, RIGHT, LEADING or TRAILING
   * @param strTooltip Tooltip to show over the label - if none is provided (e.g. null value), the strLabel will be used as a tooltip.
   */
  public JClickableLabel(String strLabel, String strDataForAction, EventListener eventHandler, Icon icon, int horizontalAlignment, String strTooltip)
  {
    super(strLabel, icon, horizontalAlignment);
    
    this.strData = strDataForAction;
    this.clickHandler = (ActionListener)eventHandler;
    
    // empty border at the top and bottom will simulate "line-spacing"
    // (this is only needed when an icon is displayed)
    if (icon != null) {
      this.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
    }
    
    // the tooltip for now only shows the full label text
    this.setToolTipText(strTooltip == null ? strLabel : strTooltip);
    this.setForeground(REGULAR_FOREGROUND_COLOR);
    this.addMouseListener(this);
  }
  
  
  public void setRegularForegroundColor(Color regularForegroundColor)
  {
    REGULAR_FOREGROUND_COLOR = regularForegroundColor;
    
    // apply the new foreground color immediately
    this.setForeground(REGULAR_FOREGROUND_COLOR);
  }

  public Color getRegularForegroundColor() {
    return REGULAR_FOREGROUND_COLOR;
  }
  
  
  public void setHoverForegroundColor(Color hoverForegroundColor)
  {
    // will be applied the next time mouse hovers over this label
    HOVER_FOREGROUND_COLOR = hoverForegroundColor;
  }

  public Color getHoverForegroundColor() {
    return HOVER_FOREGROUND_COLOR;
  }
  
  
  /**
   * @return The "hidden" string value that is normally sent as an <code>ActionCommand</code>
   *         within <code>ActionEvent</code> when JClickableLabel is clicked.
   */
  public String getData() {
    return (this.strData);
  }
  
  
  /**
   * @return String value of the label that this JClickableLabel would have in the UI.
   */
  public String toString() {
    return (this.getText());
  }
  
  
  /* This class extends JLabel, so it can't extend MouseAdapter;
   * therefore, empty methods will be added for not useful callbacks
   * from the MouseListener interface.
   */
  public void mouseClicked(MouseEvent e) 
  {
    // call 'actionPerformed' method on the clickHandler instance that was supplied
    // on creation of the JClickableLabel instance
    this.clickHandler.actionPerformed(new ActionEvent(this, e.getID(), this.strData));
  }
  
  public void mouseEntered(MouseEvent e) 
  {
    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) ;
    this.setForeground(HOVER_FOREGROUND_COLOR);
  }
  
  public void mouseExited(MouseEvent e) 
  {
    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
    this.setForeground(REGULAR_FOREGROUND_COLOR);
  }
  
  public void mousePressed(MouseEvent e) 
  {
    // do nothing
  }
  
  public void mouseReleased(MouseEvent e) 
  {
    // do nothing
  }
  
  
  /**
   * @return A dummy instance of JClickable label - only intended to
   *         represent an object of this class; doesn't have a click handler,
   *         so a click on it will result in a <code>NullPointerException</code>.
   */
  public static JClickableLabel getDummyInstance() {
    return (new JClickableLabel("dummy", "", null));
  }
  
}
