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
/*

package org.apache.taverna.lang.uibuilder;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 * Bean field editor using a JTextField for short values
 * 
 * @author Tom Oinn
 */
public class BeanTextField extends BeanTextComponent implements
		AlignableComponent {

	private static final long serialVersionUID = 5968203948656812060L;

	public BeanTextField(Object target, String propertyName, boolean useLabel,
			Properties props) throws NoSuchMethodException {
		super(target, propertyName, useLabel, props);
	}

	public BeanTextField(Object target, String propertyName, Properties props)
			throws NoSuchMethodException {
		this(target, propertyName, true, props);
	}

	@SuppressWarnings("serial")
	@Override
	protected JTextComponent getTextComponent() {
		final JTextField result = new JTextField() {

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(0, height);
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(0, height);
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(super.getMaximumSize().width, height);
			}
		};
		result.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setProperty();
				result.transferFocus();
			}
		});
		return new JTextField();
	}

}
