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
