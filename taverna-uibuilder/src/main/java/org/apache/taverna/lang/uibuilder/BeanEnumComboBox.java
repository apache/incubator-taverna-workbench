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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JComboBox;

/**
 * Bean property editor for enumerated property types, rendering the enumeration
 * as a combo box
 * 
 * @author Tom Oinn
 * 
 */
public class BeanEnumComboBox extends BeanComponent implements
		AlignableComponent {

	private static final long serialVersionUID = -6892016525599793149L;

	private Object[] possibleValues;
	private static int height = 24;
	private JComboBox value;

	public BeanEnumComboBox(Object target, String propertyName, Properties props)
			throws NoSuchMethodException {
		this(target, propertyName, true, props);
	}

	public BeanEnumComboBox(Object target, String propertyName,
			boolean useLabel, Properties props) throws NoSuchMethodException {
		super(target, propertyName, useLabel, props);
		setLayout(new BorderLayout());
		// Check that this is actually an enumeration type
		if (!propertyType.isEnum()) {
			throw new IllegalArgumentException(
					"Can't use BeanEnumComboBox on a non Enumeration property");
		}
		possibleValues = propertyType.getEnumConstants();
		value = new JComboBox(possibleValues) {

			private static final long serialVersionUID = -7712225463703816146L;

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(super.getMinimumSize().width, height);
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().width, height);
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(super.getMaximumSize().width, height);
			}
		};
		value.setSelectedIndex(currentValueIndex());
		value.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchronized (this) {
					currentObjectValue = value.getSelectedItem();
					setProperty();
				}
			}
		});
		addLabel();
		add(value, BorderLayout.CENTER);
	}

	private int currentValueIndex() {
		Object currentValue = getProperty();
		for (int i = 0; i < possibleValues.length; i++) {
			if (currentValue.equals(possibleValues[i])) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void updateComponent() {
		value.setSelectedIndex(currentValueIndex());
	}

}
