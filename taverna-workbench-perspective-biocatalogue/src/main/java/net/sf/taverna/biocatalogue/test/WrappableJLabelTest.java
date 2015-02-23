package net.sf.taverna.biocatalogue.test;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class WrappableJLabelTest extends JFrame
{
  public WrappableJLabelTest() {
    
    // depending on the LayoutManager of the container, JLabel may
    // be resized or simply "cut off" on the edges - e.g. FlowLayout
    // cuts it off, BorderLayout does the resizing
    JPanel jpTestPanel = new JPanel(new BorderLayout());
    jpTestPanel.add(new JLabel("<html><span color=\"red\">a very long</span> text that <b>is just</b> " +
        "showing how the whole thing looks - will it wrap text or not; this " +
    "is the question</html>"), BorderLayout.CENTER);
    
    this.getContentPane().add(jpTestPanel);
    
    this.pack();
  }
  
  public static void main(String[] args)
  {
    WrappableJLabelTest f = new WrappableJLabelTest();
    f.setLocationRelativeTo(null);
    f.setPreferredSize(new Dimension(400, 300));
    f.setVisible(true);
  }

}
