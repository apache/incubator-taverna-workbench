package net.sf.taverna.biocatalogue.test;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class TestDoubleUsageOfSameSwingElement extends JFrame
{
  private String text = "abc";
  boolean bLowerCase = true;
  
  public TestDoubleUsageOfSameSwingElement()
  {
    final JButton bBtn = new JButton(text);
    bBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        bLowerCase = !bLowerCase;
        bBtn.setText(bLowerCase ? text.toLowerCase() : text.toUpperCase());
      }
    });
    
    this.setLayout(new BorderLayout());
    this.add(bBtn, BorderLayout.WEST);
    this.add(bBtn, BorderLayout.EAST);
    
    this.pack();
  }

}
