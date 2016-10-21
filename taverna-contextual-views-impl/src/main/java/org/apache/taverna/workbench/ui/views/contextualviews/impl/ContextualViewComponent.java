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
package org.apache.taverna.workbench.ui.views.contextualviews.impl;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static org.apache.taverna.lang.ui.ShadedLabel.BLUE;
import static org.apache.taverna.lang.ui.ShadedLabel.GREEN;
import static org.apache.taverna.lang.ui.ShadedLabel.ORANGE;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.minusIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.plusIcon;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.lang.ui.ShadedLabel;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.EditManager.EditManagerEvent;
import org.apache.taverna.workbench.selection.DataflowSelectionModel;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.DataflowSelectionMessage;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.selection.events.WorkflowBundleSelectionEvent;
import org.apache.taverna.workbench.ui.Utils;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import org.apache.taverna.workbench.ui.zaria.UIComponentSPI;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

@SuppressWarnings("serial")
public class ContextualViewComponent extends JScrollPane implements UIComponentSPI {
	/** delay before contextual view is redrawn */
	private static final int DELAY = 250;
	private static final Color[] colors = new Color[] { BLUE, GREEN, ORANGE };
	// HACK ALERT!
	public static boolean selfGenerated = false;

	private Observer<DataflowSelectionMessage> dataflowSelectionListener = new DataflowSelectionListener();
	private SelectionManager selectionManager;
	private ContextualViewFactoryRegistry contextualViewFactoryRegistry;
	GridBagConstraints gbc;
	protected Map<JPanel, SectionLabel> panelToLabelMap = new HashMap<>();
	private String lastOpenedSectionName = "";
	private JPanel mainPanel;
	private List<JPanel> shownComponents = null;
	int colorIndex = 0;
	private Timer updateSelectionTimer = null;
	private Object lastSelectedObject = null;

	private static final Comparator<ContextualView> viewComparator = new Comparator<ContextualView>() {
		@Override
		public int compare(ContextualView o1, ContextualView o2) {
			return o1.getPreferredPosition() - o2.getPreferredPosition();
		}
	};

	public ContextualViewComponent(EditManager editManager,
			SelectionManager selectionManager,
			ContextualViewFactoryRegistry contextualViewFactoryRegistry) {
		this.selectionManager = selectionManager;
		this.contextualViewFactoryRegistry = contextualViewFactoryRegistry;
		updateSelectionTimer = new Timer(DELAY, updateSelectionListener);
		updateSelectionTimer.setRepeats(false);

		initialise();

		editManager.addObserver(new EditManagerObserver());
		selectionManager.addObserver(new SelectionManagerObserver());
	}

	@Override
	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "Details";
	}

	private void initialise() {
		mainPanel = new JPanel(new GridBagLayout());
		this.setViewportView(mainPanel);
	}

	@Override
	public void onDisplay() {
	}

	@Override
	public void onDispose() {
		updateSelectionTimer.stop();
	}

	@SuppressWarnings("unchecked")
	private void updateContextualView(List<ContextualViewFactory<? super Object>> viewFactories,
			Object selection) {
		if (selection == lastSelectedObject)
			return;
		lastSelectedObject = selection;
		mainPanel = new JPanel(new GridBagLayout());
		panelToLabelMap.clear();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 0.1;
		gbc.fill = HORIZONTAL;

		gbc.gridy = 0;
		shownComponents = new ArrayList<>();
		List<ContextualView> views = new ArrayList<>();
		for (ContextualViewFactory<?> cvf : viewFactories)
			views.addAll(((ContextualViewFactory<Object>) cvf)
					.getViews(selection));
		Collections.sort(views, viewComparator);
		colorIndex = 0;
		if (views.isEmpty())
			mainPanel.add(new JLabel("No details available"));
		else
			populateContextualView(viewFactories, gbc, views);
		gbc.weighty = 0.1;
		gbc.fill = BOTH;
		mainPanel.add(new JPanel(), gbc);
		// mainPanel.revalidate();
		// mainPanel.repaint();
		this.setViewportView(mainPanel);
		// this.revalidate();
		// this.repaint();
	}

	private void populateContextualView(
			List<ContextualViewFactory<? super Object>> viewFactories,
			GridBagConstraints gbc, List<ContextualView> views) {
		JPanel firstPanel = null;
		JPanel lastOpenedSection = null;
		for (ContextualView view : views) {
			SectionLabel label = new SectionLabel(view.getViewTitle(), nextColor());
			mainPanel.add(label, gbc);
			gbc.gridy++;
			JPanel subPanel = new JPanel();
			if (view.getViewTitle().equals(lastOpenedSectionName))
				lastOpenedSection = subPanel;
			subPanel.setLayout(new GridBagLayout());

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 0.1;
			constraints.weighty = 0;
			constraints.anchor = CENTER;
			constraints.fill = HORIZONTAL;

			subPanel.add(view, constraints);
			Frame frame = Utils.getParentFrame(this);
			Action configureAction = view.getConfigureAction(frame);
			if (configureAction != null) {
				JButton configButton = new JButton(configureAction);
				if (configButton.getText() == null
						|| configButton.getText().isEmpty())
					configButton.setText("Configure");
				constraints.gridy++;
				constraints.fill = NONE;
				constraints.anchor = LINE_START;
				subPanel.add(configButton, constraints);
			}
			if (firstPanel == null)
				firstPanel = subPanel;
			mainPanel.add(subPanel, gbc);
			shownComponents.add(subPanel);
			gbc.gridy++;
			if (viewFactories.size() != 1)
				makeCloseable(subPanel, label);
			else {
				lastOpenedSectionName = label.getText();
				lastOpenedSection = subPanel;
				panelToLabelMap.put(subPanel, label);
				subPanel.setVisible(false);
			}
		}
		if (lastOpenedSection != null)
			openSection(lastOpenedSection);
		else if (firstPanel != null)
			openSection(firstPanel);
	}

	private void clearContextualView() {
		lastSelectedObject = null;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.add(new JLabel("No details available"));
		this.setViewportView(mainPanel);
		this.revalidate();
	}

	public void updateSelection(Object selectedItem) {
		findContextualView(selectedItem);
	}

	private Runnable updateSelectionRunnable = new Runnable() {
		@Override
		public void run() {
			Object selection = getSelection();
			if (selection == null)
				clearContextualView();
			else
				updateSelection(selection);
		}
	};

	private ActionListener updateSelectionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(updateSelectionRunnable);
		}
	};

	public void updateSelection() {
		updateSelectionTimer.restart();
	}

	private Object getSelection() {
		WorkflowBundle workflowBundle = selectionManager.getSelectedWorkflowBundle();

		/*
		 * If there is no currently opened dataflow, clear the contextual view
		 * panel
		 */
		if (workflowBundle == null) {
			return null;
		}
		DataflowSelectionModel selectionModel = selectionManager
				.getDataflowSelectionModel(workflowBundle);
		Set<Object> selection = selectionModel.getSelection();

		/*
		 * If the dataflow is opened but no component of the dataflow is
		 * selected, clear the contextual view panel
		 */
		if (selection.isEmpty())
			return null;
		return selection.iterator().next();
	}

	private void findContextualView(Object selection) {
		List<ContextualViewFactory<? super Object>> viewFactoriesForBeanType = contextualViewFactoryRegistry
				.getViewFactoriesForObject(selection);
		updateContextualView(viewFactoriesForBeanType, selection);
	}

	private final class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender, SelectionManagerEvent message) {
			if (message instanceof WorkflowBundleSelectionEvent)
				bundleSelected((WorkflowBundleSelectionEvent) message);
		}

		private void bundleSelected(WorkflowBundleSelectionEvent event) {
			WorkflowBundle oldBundle = event
					.getPreviouslySelectedWorkflowBundle();
			WorkflowBundle newBundle = event.getSelectedWorkflowBundle();

			if (oldBundle != null)
				selectionManager.getDataflowSelectionModel(oldBundle)
						.removeObserver(dataflowSelectionListener);
			if (newBundle != null)
				selectionManager.getDataflowSelectionModel(newBundle)
						.addObserver(dataflowSelectionListener);
			lastSelectedObject = null;
			updateSelection();
		}
	}

	private final class DataflowSelectionListener extends SwingAwareObserver<DataflowSelectionMessage> {
		@Override
		public void notifySwing(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) {
			updateSelection();
		}
	}

	private final class EditManagerObserver extends SwingAwareObserver<EditManagerEvent> {
		@Override
		public void notifySwing(Observable<EditManagerEvent> sender, EditManagerEvent message) {
			Object selection = getSelection();
			if ((selection != lastSelectedObject) && !selfGenerated) {
				lastSelectedObject = null;
				refreshView();
			}
		}
	}

	public void refreshView() {
		if (mainPanel != null)
			updateSelection();
	}

	private final class SectionLabel extends ShadedLabel {
		private JLabel expand;

		private SectionLabel(String text, Color colour) {
			super(text, colour);
			expand = new JLabel(minusIcon);
			add(expand, 0);
			setExpanded(true);
		}

		public void setExpanded(boolean expanded) {
			if (expanded)
				expand.setIcon(minusIcon);
			else
				expand.setIcon(plusIcon);
		}
	}

	private void makeCloseable(JPanel panel, SectionLabel label) {
		panel.setVisible(false);
		if (panelToLabelMap.get(panel) != label) {
			panelToLabelMap.put(panel, label);
			// Only add mouse listener once
			label.addMouseListener(new SectionOpener(panel));
		}
	}

	protected class SectionOpener extends MouseAdapter {
		private final JPanel sectionToOpen;

		public SectionOpener(JPanel sectionToOpen) {
			this.sectionToOpen = sectionToOpen;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			openSection(sectionToOpen);
		}
	}

	public synchronized void openSection(JPanel sectionToOpen) {
		lastOpenedSectionName = "";
		for (Entry<JPanel, SectionLabel> entry : panelToLabelMap.entrySet()) {
			JPanel section = entry.getKey();
			SectionLabel sectionLabel = entry.getValue();

			if (section != sectionToOpen)
				section.setVisible(false);
			else {
				section.setVisible(!section.isVisible());
				if (section.isVisible())
					lastOpenedSectionName = sectionLabel.getText();
			}
			sectionLabel.setExpanded(section.isVisible());
		}
		this.revalidate();
		this.repaint();
	}

	private Color nextColor() {
		if (colorIndex >= colors.length)
			colorIndex = 0;
		return colors[colorIndex++];
	}
}
