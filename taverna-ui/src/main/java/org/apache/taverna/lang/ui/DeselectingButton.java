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

package org.apache.taverna.lang.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;

/**
 * @author alanrw
 *
 */
public class DeselectingButton extends JButton {
	
	public DeselectingButton(String name, final ActionListener action, String toolTip) {
		super();
		this.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				Component parent = DeselectingButton.this.getParent();
				action.actionPerformed(e);
				parent.requestFocusInWindow();
			}		
		});
		this.setText(name);
		this.setToolTipText(toolTip);
	}

	public DeselectingButton(String name, final ActionListener action) {
		this(name, action, null);
	}
	
	public DeselectingButton(final Action action, String toolTip) {
		this((String) action.getValue(Action.NAME), action, toolTip);
	}

	public DeselectingButton(final Action action) {
		this(action, null);
	}
}
