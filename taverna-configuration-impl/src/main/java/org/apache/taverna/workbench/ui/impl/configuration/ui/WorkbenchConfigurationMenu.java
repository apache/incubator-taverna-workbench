/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.impl.configuration.ui;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.workbench.configuration.workbench.ui.T2ConfigurationFrame;

public class WorkbenchConfigurationMenu extends AbstractMenuAction {
	private static final String MAC_OS_X = "Mac OS X";

	private T2ConfigurationFrame t2ConfigurationFrame;

	public WorkbenchConfigurationMenu() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#preferences"),
				100);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Preferences") {
			@Override
			public void actionPerformed(ActionEvent event) {
				t2ConfigurationFrame.showFrame();
			}
		};
	}

	@Override
	public boolean isEnabled() {
		return !MAC_OS_X.equalsIgnoreCase(System.getProperty("os.name"));
	}

	public void setT2ConfigurationFrame(T2ConfigurationFrame t2ConfigurationFrame) {
		this.t2ConfigurationFrame = t2ConfigurationFrame;
	}
}
