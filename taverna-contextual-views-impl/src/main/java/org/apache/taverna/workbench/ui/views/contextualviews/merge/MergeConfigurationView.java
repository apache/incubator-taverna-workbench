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

package org.apache.taverna.workbench.ui.views.contextualviews.merge;

import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.lang.Math.max;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.LEFT;
import static javax.swing.SwingConstants.RIGHT;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.downArrowIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.upArrowIcon;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.helper.HelpEnabledDialog;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.DataLink;

@SuppressWarnings("serial")
public class MergeConfigurationView extends HelpEnabledDialog {
	private static final String TITLE = "<html><body><b>Order of incoming links</b></body></html>";

	private List<DataLink> dataLinks;
	private List<DataLink> reorderedDataLinks;
	/** Ordered list of labels for dataLinks to be displayed to the user */
	private DefaultListModel<String> labelListModel;
	/** JList that displays the labelListModel */
	JList<String> list;
	/** Button to push the dataLink up the list */
	private JButton upButton;
	/** Button to push the dataLink down the list */
	private JButton downButton;
	private final EditManager editManager;
	private final SelectionManager selectionManager;

	public MergeConfigurationView(List<DataLink> dataLinks, EditManager editManager,
			SelectionManager selectionManager) {
		super((Frame)null, "Merge Configuration", true);

		this.dataLinks = new ArrayList<>(dataLinks);
		reorderedDataLinks = new ArrayList<>(dataLinks);
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		labelListModel = new DefaultListModel<>();
		for (DataLink dataLink : dataLinks)
			labelListModel.addElement(dataLink.toString());

		initComponents();
	}

	private void initComponents() {
        getContentPane().setLayout(new BorderLayout());

		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		listPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10),
				new EtchedBorder()));

		JLabel title = new JLabel(TITLE);
		title.setBorder(new EmptyBorder(5, 5, 5, 5));
		listPanel.add(title, NORTH);

		list = new JList<>(labelListModel);
		list.setSelectionMode(SINGLE_SELECTION);
		list.setVisibleRowCount(-1);
		list.addListSelectionListener(new ListSelectionListener() {
			/**
			 * Enable and disable up and down buttons based on which item in the
			 * list is selected
			 */
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int index = list.getSelectedIndex();
				if ((index == -1) || (index == 0 && labelListModel.size() == 0)) {
					// nothing selected or only one item in the list
					upButton.setEnabled(false);
					downButton.setEnabled(false);
				} else {
					upButton.setEnabled(index > 0);
					downButton.setEnabled(index < labelListModel.size() - 1);
				}
			}
		});

		final JScrollPane listScroller = new JScrollPane(list);
		listScroller.setBorder(new EmptyBorder(5, 5, 5, 5));
		listScroller.setBackground(listPanel.getBackground());
		listScroller.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
		listScroller.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
		// Set the size of scroll pane to make all list items visible
		FontMetrics fm = listScroller.getFontMetrics(this.getFont());
		int listScrollerHeight = fm.getHeight() * labelListModel.size() + 75; //+75 just in case
		listScroller.setPreferredSize(new Dimension(listScroller
				.getPreferredSize().width, max(listScrollerHeight,
				listScroller.getPreferredSize().height)));
		listPanel.add(listScroller, BorderLayout.CENTER);

		JPanel upDownButtonPanel = new JPanel();
		upDownButtonPanel.setLayout(new BoxLayout(upDownButtonPanel, Y_AXIS));
		upDownButtonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		upButton = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				if (index != -1) {
					// Swap the labels
					String label = (String) labelListModel.elementAt(index);
					labelListModel.set(index, labelListModel.get(index - 1));
					labelListModel.set(index - 1, label);
					// Swap the dataLinks
					DataLink dataLink = reorderedDataLinks.get(index);
					reorderedDataLinks.set(index,
							reorderedDataLinks.get(index - 1));
					reorderedDataLinks.set(index - 1, dataLink);
					// Make the pushed item selected
					list.setSelectedIndex(index - 1);
					// Refresh the list
					listScroller.repaint();
					listScroller.revalidate();
				}
			}
		});
		upButton.setIcon(upArrowIcon);
		upButton.setText("Up");
	    // Place text to the right of icon, vertically centered
		upButton.setVerticalTextPosition(CENTER);
		upButton.setHorizontalTextPosition(RIGHT);
		// Set the horizontal alignment of the icon and text
		upButton.setHorizontalAlignment(LEFT);
		upButton.setEnabled(false);
		upDownButtonPanel.add(upButton);

		downButton = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				if (index != -1) {
					// Swap the labels
					String label = (String) labelListModel.elementAt(index);
					labelListModel.set(index, labelListModel.get(index + 1));
					labelListModel.set(index + 1, label);
					// Swap the dataLinks
					DataLink dataLink = reorderedDataLinks.get(index);
					reorderedDataLinks.set(index,
							reorderedDataLinks.get(index + 1));
					reorderedDataLinks.set(index + 1, dataLink);
					// Make the pushed item selected
					list.setSelectedIndex(index + 1);
					// Refresh the list
					list.repaint();
					listScroller.revalidate();
				}
			}
		});
		downButton.setIcon(downArrowIcon);
		downButton.setText("Down");
	    // Place text to the right of icon, vertically centered
		downButton.setVerticalTextPosition(CENTER);
		downButton.setHorizontalTextPosition(RIGHT);
		// Set the horizontal alignment of the icon and text
		downButton.setHorizontalAlignment(LEFT);
		downButton.setEnabled(false);
		// set the up button to be of the same size as down button
		upButton.setPreferredSize(downButton.getPreferredSize());
		upButton.setMaximumSize(downButton.getPreferredSize());
		upButton.setMinimumSize(downButton.getPreferredSize());
		upDownButtonPanel.add(downButton);

		listPanel.add(upDownButtonPanel, EAST);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton jbOK = new JButton("OK");
		jbOK.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new MergeConfigurationAction(dataLinks, reorderedDataLinks,
						editManager, selectionManager).actionPerformed(e);
				closeDialog();
			}
		});

		JButton jbCancel = new JButton("Cancel");
		jbCancel.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog();
			}
		});

        buttonPanel.add(jbOK);
        buttonPanel.add(jbCancel);

        getContentPane().add(listPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, SOUTH);
        pack();
	}

	/**
	 * Close the dialog.
	 */
	private void closeDialog() {
		setVisible(false);
		dispose();
	}
}
