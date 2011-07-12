package net.sf.taverna.biocatalogue.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class DrawDefaultIconTest {

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    int w = 16;
    int h = 16;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration gc = gd.getDefaultConfiguration();
    
    BufferedImage image = gc.createCompatibleImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.setColor(Color.RED);
    g.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
    g.drawLine(4, 4, 12, 12);
    g.drawLine(12, 4, 4, 12);
    g.dispose();
    
    JOptionPane.showMessageDialog(null, new ImageIcon(image)); 
  }

}
