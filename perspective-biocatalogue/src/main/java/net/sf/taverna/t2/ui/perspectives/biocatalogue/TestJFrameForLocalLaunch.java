package net.sf.taverna.t2.ui.perspectives.biocatalogue;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;

import net.sf.taverna.biocatalogue.model.ResourceManager;


/**
 * @author Sergejs Aleksejevs
 */
public class TestJFrameForLocalLaunch extends JFrame implements ComponentListener
{
  private static final int DEFAULT_POSITION_X = 225;
  private static final int DEFAULT_POSITION_Y = 150;
  
  private static final int DEFAULT_WIDTH = 800;
  private static final int DEFAULT_HEIGHT = 500;
  
  
	private TestJFrameForLocalLaunch()
  {
    // set window title and icon
	  this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  this.setTitle("BioCatalogue API Demo"/* TODO - windowBaseTitle */);
    this.setIconImage(ResourceManager.getImageIcon(ResourceManager.FAVICON).getImage());
    this.addComponentListener(this);
    
    // get content pane
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new BorderLayout());
    
    // put main tabs into the content pane
    contentPane.add(MainComponentFactory.getSharedInstance(), BorderLayout.CENTER);
    
    this.pack();
  }
	
	
  // *** Callbacks for ComponentListener interface ***
  
  public void componentResized(ComponentEvent e) { /* do nothing */ }
  public void componentMoved(ComponentEvent e) { /* do nothing */ }
  public void componentHidden(ComponentEvent e) { /* do nothing */ }
  public void componentShown(ComponentEvent e) {
    this.setLocation(DEFAULT_POSITION_X, DEFAULT_POSITION_Y);
  }
	
  
  /**
   * This is a simple test class for launching BioCatalogue perspective
   * from outside Taverna. At some point it will be not usable anymore,
   * when proper integration of BioCatalogue plugin is made.
   * 
   * @author Sergejs Aleksejevs
   */
  public static void main(String[] args)
  {
    TestJFrameForLocalLaunch standaloneFrame = new TestJFrameForLocalLaunch();
    standaloneFrame.setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
    standaloneFrame.setVisible(true);
  }
}
