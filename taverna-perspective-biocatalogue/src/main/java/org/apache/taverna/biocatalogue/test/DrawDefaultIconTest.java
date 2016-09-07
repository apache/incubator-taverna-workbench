package org.apache.taverna.biocatalogue.test;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
