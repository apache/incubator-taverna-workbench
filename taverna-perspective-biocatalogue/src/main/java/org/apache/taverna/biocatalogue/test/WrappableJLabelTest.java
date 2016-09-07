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
