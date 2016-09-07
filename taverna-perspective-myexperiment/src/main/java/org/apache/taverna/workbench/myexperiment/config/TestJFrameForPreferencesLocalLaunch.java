package org.apache.taverna.workbench.myexperiment.config;
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

import java.awt.Dimension;

import javax.swing.JFrame;

/**
 * This is a class to get a visual on what the preferences will look like when
 * integrated into the main taverna preferences.
 * 
 * @author Emmanuel Tagarira
 */
public class TestJFrameForPreferencesLocalLaunch {

  public static void main(String[] args) {
	JFrame frame = new JFrame("myExperiment Preferences Test");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	frame.setMinimumSize(new Dimension(500, 300));
	frame.setLocation(300, 150);
	frame.getContentPane().add(new MyExperimentConfigurationPanel());

	frame.pack();
	frame.setVisible(true);
  }
}
