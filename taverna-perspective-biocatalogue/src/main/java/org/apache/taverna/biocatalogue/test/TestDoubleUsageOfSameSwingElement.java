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
