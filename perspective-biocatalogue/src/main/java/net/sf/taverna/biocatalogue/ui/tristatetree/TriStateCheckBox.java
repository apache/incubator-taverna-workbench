package net.sf.taverna.biocatalogue.ui.tristatetree;

/*
 * Taken from: http://72.5.124.102/thread.jspa?threadID=721308&messageID=9955637
 * Data webpage accessed: 07/February/2010
 * 
 * Modified by Sergejs Aleksejevs
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
 
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
 
public class TriStateCheckBox extends JCheckBox
{
  private static final long serialVersionUID = 1L;
  
  public static enum State {
    CHECKED, UNCHECKED, PARTIAL
  };
  
  
  // these colors will be used for painting the 'partial' state of the checkbox -
  // a gradient painting will be made from top-left to bottom-right
  private Color partialStateTopLeftColor = Color.GREEN.darker().darker().darker();
  private Color partialStateBottomRightColor = Color.GREEN.brighter().brighter().brighter();
  
  
  /**
   * Creates an initially unselected check box button with no text, no icon.
   */
  public TriStateCheckBox() {
    this(null, State.UNCHECKED);
  }
 
  /**
   * Creates a check box with text and icon, and specifies whether or not it is initially
   * selected.
   * 
   * @param text
   *            The text of the check box.
   * @param initial
   *            The initial state
   */
  public TriStateCheckBox(String text, State initial) {
    super.setText(text);
    setModel(new TriStateModel(initial));
    setIcon(new TriStateIcon(this));
    // some UI settings
    setRolloverEnabled(true);
  }
 
  /**
   * Set the new state to either CHECKED, PARTIAL or UNCHECKED.
   */
  public void setState(State state) {
    ((TriStateModel) model).setState(state);
  }
 
  /**
   * Return the current state, which is determined by the selection status of the model.
   */
  public State getState() {
    return ((TriStateModel) model).getState();
  }
 
  public void setSelected(boolean selected) {
    ((TriStateModel) model).setSelected(selected);
  }
 
  /** The model for the button */
  private static class TriStateModel extends JToggleButton.ToggleButtonModel {
 
    private static final long serialVersionUID = 1L;
    protected State state;
 
    public TriStateModel(State state) {
      this.state = state;
    }
 
    public boolean isSelected() {
      return state == State.CHECKED;
    }
 
    public State getState() {
      return state;
    }
 
    public void setState(State state) {
      this.state = state;
      fireStateChanged();
    }
 
    public void setPressed(boolean pressed) {
      if (pressed) {
        switch (state) {
          case UNCHECKED:
            state = State.CHECKED;
            break;
          case PARTIAL:
            state = State.UNCHECKED;
            break;
          case CHECKED:
            state = State.PARTIAL;
            break;
        }
      }
 
    }
 
    public void setSelected(boolean selected) {
      if (selected) {
        this.state = State.CHECKED;
      } else {
        this.state = State.UNCHECKED;
      }
    }
  }
 
  private class TriStateIcon implements Icon
  {
    private Icon checkBoxIcon;
    private TriStateCheckBox triStateCheckBox;
    public TriStateIcon(TriStateCheckBox triStateCheckBox) {
      this.triStateCheckBox = triStateCheckBox;
      this.checkBoxIcon = UIManager.getIcon("CheckBox.icon");
      
      return;
    }
 
    public final int getIconHeight() {
      return this.checkBoxIcon.getIconHeight();
 
    }
 
    public final int getIconWidth() {
      return this.checkBoxIcon.getIconWidth();
 
    }
    
    
    public void paintIcon(Component c, Graphics g, int x, int y)
		{
			checkBoxIcon.paintIcon(triStateCheckBox, g, x, y);
			if (triStateCheckBox.getState().equals(TriStateCheckBox.State.PARTIAL))
			{
			  // this is changed to create the gradient paint dynamically every time;
			  // this makes sure that the gradient is relative to the actual position of the checkbox,
			  // rather than in the absolute coordinates of the parent component
			  GradientPaint gradient = new GradientPaint(x, y, partialStateTopLeftColor, x + 8, y + 8, partialStateBottomRightColor, false);
      
				Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(gradient);
				final int deltaX = 2;
				final int deltaY = 2;
				final int xNew = x + deltaX;
				final int yNew = y + deltaY;
				final int width = getIconWidth() - 2*deltaX;
				final int height = getIconHeight() - 2*deltaY;
				g2d.fillRect(xNew, yNew, width, height);
			}
		}

  }
}
