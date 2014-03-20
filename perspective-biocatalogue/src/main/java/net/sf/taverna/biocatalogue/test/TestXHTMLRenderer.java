package net.sf.taverna.biocatalogue.test;

import java.awt.Container;
import java.io.File;

import javax.swing.JFrame;

import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;

public class TestXHTMLRenderer extends JFrame {
  public TestXHTMLRenderer() {
    try {
      init();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void init() throws Exception {
    Container contentPane = this.getContentPane();
    
    XHTMLPanel panel = new XHTMLPanel();
    panel.getSharedContext().getTextRenderer().setSmoothingThreshold(0); // Anti-aliasing for all font sizes
    panel.setDocument(new File("c:\\Temp\\MyExperiment\\T2 BioCatalogue Plugin\\BioCatalogue Plugin\\resources\\test.html"));
    
    FSScrollPane scroll = new FSScrollPane(panel);
    contentPane.add(scroll);
    
    this.setTitle("XHTML rendered test");
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.pack();
    this.setSize(1024, 768);
  }
  
  
  public static void main(String[] args) {
    JFrame f = new TestXHTMLRenderer();
    f.setVisible(true);
  }
  
}
