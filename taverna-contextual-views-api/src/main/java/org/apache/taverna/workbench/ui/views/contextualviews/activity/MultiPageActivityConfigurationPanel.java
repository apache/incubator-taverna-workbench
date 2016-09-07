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

package org.apache.taverna.workbench.ui.views.contextualviews.activity;

import static java.awt.BorderLayout.CENTER;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;

import org.apache.taverna.scufl2.api.activity.Activity;

/**
 * Component for configuring activities that have multiple configuration pages.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public abstract class MultiPageActivityConfigurationPanel extends
		ActivityConfigurationPanel {
	private JTabbedPane tabbedPane;

	/**
	 * Constructs a new <code>MultiPageActivityConfigurationPanel</code>.
	 * 
	 * @param activity
	 */
	public MultiPageActivityConfigurationPanel(Activity activity) {
		super(activity);
		setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane();
		add(tabbedPane, CENTER);
	}

	public void addPage(String name, Component component) {
		tabbedPane.addTab(name, component);
	}

	public void removePage(String name) {
		tabbedPane.removeTabAt(tabbedPane.indexOfTab(name));
	}

	public void removeAllPages() {
		tabbedPane.removeAll();
	}
}
