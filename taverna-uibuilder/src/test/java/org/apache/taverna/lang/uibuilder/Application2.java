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
package org.apache.taverna.lang.uibuilder;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.taverna.lang.uibuilder.UIBuilder;

/**
 * Torture test for the UIBuilder, run this as an application
 * 
 * @author Tom Oinn
 * 
 */
public class Application2 {

	private static final String NUMBUS = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		try {
			UIManager.setLookAndFeel(NUMBUS);
		} catch (ClassNotFoundException ex) {
			// ignore
		}
		Object bean = new PrimitiveTypeBean();
		JFrame win = new JFrame();
		JPanel contents = UIBuilder.buildEditor(bean, new String[] {
				"intvalue", "shortValue", "longValue", "doubleValue",
				"booleanValue", "byteValue", "floatValue", "charValue" });
		contents.setBackground(new Color(240, 230, 200));
		win.setContentPane(new JScrollPane(contents));
		win.setTitle("Primitive type test");
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.pack();
		win.setVisible(true);
	}

}
