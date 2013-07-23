/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.impl.configuration.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.t2.workbench.configuration.workbench.ui.T2ConfigurationFrame;
import net.sf.taverna.t2.workbench.helper.HelpCollator;
import net.sf.taverna.t2.workbench.helper.Helper;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.ConfigurationUIFactory;

public class T2ConfigurationFrameImpl implements T2ConfigurationFrame {

	private static Logger logger = Logger.getLogger(T2ConfigurationFrameImpl.class);

	private static final int FRAME_WIDTH = 700;
	private static final int FRAME_HEIGHT = 450;

	private List<ConfigurationUIFactory> configurationUIFactories = new ArrayList<>();

	private JFrame frame;
	private JSplitPane splitPane;
	private JList<ConfigurableItem> list;

	public T2ConfigurationFrameImpl() {
	}

	public void showFrame() {
		getFrame().setVisible(true);
	}

	public void showConfiguration(String name) {
		showFrame();
		ListModel<ConfigurableItem> lm = list.getModel();
		for (int i = 0; i < lm.getSize(); i++) {
			Object o = lm.getElementAt(i);
			if (o.toString().equals(name)) {
				list.setSelectedIndex(i);
				break;
			}
		}
	}

	private JFrame getFrame() {
		if (frame == null) {
			frame = new JFrame();
			Helper.setKeyCatcher(frame);
			HelpCollator.registerComponent(frame);

			frame.setLayout(new BorderLayout());

			// Split pane to hold list of properties (on the left) and their
			// configurable options (on the right)
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setBorder(null);

			list = getConfigurationList();
			JScrollPane jspList = new JScrollPane(list);
			jspList.setBorder(new EmptyBorder(5, 5, 5, 5));
			jspList.setMinimumSize(new Dimension(150, jspList.getPreferredSize().height));

			splitPane.setLeftComponent(jspList);
			splitPane.setRightComponent(new JPanel());
			splitPane.setDividerSize(1);

			// select first item if one exists
			if (list.getModel().getSize() > 0) {
				list.setSelectedValue(list.getModel().getElementAt(0), true);
			}

			frame.add(splitPane);

			frame.setSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		}
		return frame;
	}

	private JList<ConfigurableItem> getConfigurationList() {
		if (list == null) {
			list = new JList<>();
			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (list.getSelectedValue() instanceof ConfigurableItem) {
						ConfigurableItem item = (ConfigurableItem) list.getSelectedValue();
						setMainPanel(item.getPanel());
					}

					// Keep the split pane's divider at its current position - but
					// looks ugly
					// The problem with divider moving from its current position
					// after selecting an
					// item from the list on the left is that the right hand side
					// panels are loaded dynamically
					// and it seems there is nothing we can do about it - it's just
					// the JSplitPane's behaviour
					// splitPane.setDividerLocation(splitPane.getLastDividerLocation());

				}
			});
			list.setListData(getListItems());
		}
		return list;
	}

	private void setMainPanel(JPanel panel) {
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		splitPane.setRightComponent(panel);
	}

	public void setConfigurationUIFactories(List<ConfigurationUIFactory> configurationUIFactories) {
		this.configurationUIFactories = configurationUIFactories;
	}

	private ConfigurableItem[] getListItems() {
		List<ConfigurableItem> arrayList = new ArrayList<ConfigurableItem>();
		for (ConfigurationUIFactory fac : configurationUIFactories) {
			String name = fac.getConfigurable().getDisplayName();
			if (name != null) {
				logger.info("Adding configurable for name: " + name);
				arrayList.add(new ConfigurableItem(fac));
			} else {
				logger.warn("The configurable " + fac.getConfigurable().getClass()
						+ " has a null name");
			}
		}
		// Sort the list alphabetically
		ConfigurableItem[] array = (ConfigurableItem[]) arrayList.toArray(new ConfigurableItem[0]);
		Arrays.sort(array, new Comparator<ConfigurableItem>() {
			public int compare(ConfigurableItem item1, ConfigurableItem item2) {
				return item1.toString().compareToIgnoreCase(item2.toString());
			}
		});
		return array;
	}

	public void update(Object service, Map<?, ?> properties) {
		getConfigurationList().setListData(getListItems());
		if (frame != null) {
			frame.revalidate();
			frame.repaint();
		}
		// select first item if one exists
		if (list.getModel().getSize() > 0) {
			list.setSelectedValue(list.getModel().getElementAt(0), true);
		}
	}

	class ConfigurableItem {

		private final ConfigurationUIFactory factory;

		public ConfigurableItem(ConfigurationUIFactory factory) {
			this.factory = factory;

		}

		public JPanel getPanel() {
			return factory.getConfigurationPanel();
		}

		@Override
		public String toString() {
			return factory.getConfigurable().getDisplayName();
		}

	}

}
