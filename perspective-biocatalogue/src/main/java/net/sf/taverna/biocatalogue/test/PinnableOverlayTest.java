package net.sf.taverna.biocatalogue.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import net.sf.taverna.biocatalogue.ui.JPanelWithOverlay;

public class PinnableOverlayTest extends JPanel
{
  private PinnableOverlayTest thisPanel;
  
  private JPanel jpMain;
  private JPanel jpOverlay;
  private JPanelWithOverlay panelWithOverlay;
  private JToggleButton bToggle;
  
  
  public PinnableOverlayTest()
  {
    thisPanel = this;
    
    jpMain = new JPanel(new GridLayout(1,1));
    jpMain.add(new JLabel("this is the main panel"));
    
    jpOverlay = new JPanel(new GridLayout(0,1));
    jpOverlay.add(new JLabel("overlay"));
    jpOverlay.setPreferredSize(new Dimension(150, 100));
    
    bToggle = new JToggleButton("Toggle");
    bToggle.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        panelWithOverlay.setOverlayVisible(bToggle.isSelected());
      }
    });
    
    panelWithOverlay = new JPanelWithOverlay(jpMain, jpOverlay, JSplitPane.HORIZONTAL_SPLIT, false, true, false);
    panelWithOverlay.registerOverlayActivationToggleButton(bToggle);
    
    this.setLayout(new BorderLayout());
    this.add(panelWithOverlay, BorderLayout.CENTER);
    this.add(bToggle, BorderLayout.EAST);
    
    this.add(new JLabel(" | empty label | "), BorderLayout.WEST);
  }
  
  
  /**
   * @param args
   */
  public static void main(String[] args)
  {
    JFrame f = new JFrame();
    f.getContentPane().add(new PinnableOverlayTest());
    f.setSize(1000, 800);
    f.setLocationRelativeTo(null);
    f.setVisible(true);
  }

}
