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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JCheckBox;

/**
 * Bean field editor using a JCheckBox to handle boolean and Boolean field types
 * 
 * @author Tom Oinn
 */
public class BeanCheckBox extends BeanComponent implements AlignableComponent {

	private static final long serialVersionUID = -2842617445268734650L;
	private JCheckBox value;

	public BeanCheckBox(Object target, String propertyName, Properties props)
			throws NoSuchMethodException {
		this(target, propertyName, true, props);
	}

	public BeanCheckBox(Object target, String propertyName, boolean useLabel,
			Properties props) throws NoSuchMethodException {
		super(target, propertyName, useLabel, props);
		setLayout(new BorderLayout());
		value = new JCheckBox();
		value.setSelected(getBooleanProperty());
		value.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				boolean isSelected = value.isSelected();
				currentObjectValue = isSelected;
				setProperty();
			}
		});
		addLabel();
		value.setOpaque(false);
		add(Box.createHorizontalGlue(), BorderLayout.CENTER);
		add(value, BorderLayout.EAST);
	}

	@Override
	protected void updateComponent() {
		value.setSelected(getBooleanProperty());
	}

	private boolean getBooleanProperty() {
		return (Boolean) getProperty();
	}

}
