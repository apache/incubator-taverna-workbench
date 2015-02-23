/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.plugin.impl;

import static java.awt.Font.BOLD;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.NORTHWEST;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.tavernaCogs64x64Icon;

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
