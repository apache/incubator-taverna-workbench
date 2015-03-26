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

package org.apache.taverna.workbench.plugin.impl;

import static java.awt.Font.BOLD;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.NORTHWEST;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.tavernaCogs64x64Icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * @author David Withers
 */
@SuppressWarnings("serial")
public abstract class PluginPanel extends JPanel {
	@SuppressWarnings("unused")
	private static final int logoSize = 64;

	private JLabel descriptionLabel;
	private JLabel descriptionTitle;
	private JButton actionButton;

	public PluginPanel(String name, String organization, String version,
			String description) {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = NORTHWEST;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.insets.top = 10;
		gbc.insets.bottom = 10;

		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.gridheight = 4;
		JLabel logo = new JLabel(tavernaCogs64x64Icon);
		add(logo, gbc);

		gbc.gridx = 2;
		gbc.anchor = CENTER;
		actionButton = new JButton(getPluginAction());
		add(actionButton, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.gridheight = 1;
		gbc.insets.top = 7;
		gbc.insets.bottom = 0;
		gbc.anchor = NORTHWEST;
		JLabel nameLabel = new JLabel(name);
		nameLabel.setFont(getFont().deriveFont(BOLD));
		add(nameLabel, gbc);

		gbc.insets.top = 0;
		add(new JLabel(organization), gbc);

		add(new JLabel("Version " + version), gbc);

		JButton information = new JButton(new InfoAction());
		information.setFont(information.getFont().deriveFont(BOLD));
		information.setUI(new BasicButtonUI());
		information.setBorder(null);
		add(information, gbc);

		descriptionTitle = new JLabel("Description");
		descriptionTitle.setFont(getFont().deriveFont(BOLD));
		descriptionLabel = new JLabel("<html>" + description);

		setBorder(new PluginBorder());
	}

	private void showInformation() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = NORTHWEST;
		gbc.insets.left = 10;
		gbc.insets.right = 10;
		gbc.insets.bottom = 10;
		gbc.gridx = 0;
		gbc.gridwidth = 3;

		add(descriptionTitle, gbc);
		add(descriptionLabel, gbc);
		revalidate();
	}

	private void hideInformation() {
		remove(descriptionTitle);
		remove(descriptionLabel);
		revalidate();
	}

	public abstract Action getPluginAction();

	class InfoAction extends AbstractAction {
		private boolean showInformation = true;

		public InfoAction() {
			putValue(NAME, "Show information");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (showInformation) {
				showInformation();
				putValue(NAME, "Hide information");
				showInformation = false;
			} else {
				hideInformation();
				putValue(NAME, "Show information");
				showInformation = true;
			}
		}
	}

	class PluginBorder implements Border {
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			g.setColor(getBackground().darker());
			g.drawLine(x, y, x + width, y);
			g.drawLine(x, y + height - 1, x + width, y + height - 1);
		}

		@Override
		public boolean isBorderOpaque() {
			return false;
		}

		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(0, 0, 0, 0);
		}
	}
}
