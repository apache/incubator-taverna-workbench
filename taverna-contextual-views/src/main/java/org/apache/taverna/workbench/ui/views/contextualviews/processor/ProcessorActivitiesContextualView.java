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

package org.apache.taverna.workbench.ui.views.contextualviews.processor;

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static org.apache.taverna.workbench.ui.Utils.getParentFrame;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 *
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public class ProcessorActivitiesContextualView extends ContextualView {
	private static final String ABSTRACT_PROCESSOR_MSG = "<strong>Abstract processor</strong><br>"
			+ "<i>No services. This will not execute.</i>";
	private Scufl2Tools scufl2Tools = new Scufl2Tools();
	protected JPanel mainPanel = new JPanel();
	protected Processor processor;
	private final ContextualViewFactoryRegistry contextualViewFactoryRegistry;
	private final SelectionManager selectionManager;

	public ProcessorActivitiesContextualView(Processor processor,
			ContextualViewFactoryRegistry contextualViewFactoryRegistry,
			SelectionManager selectionManager) {
		super();
		this.processor = processor;
		this.contextualViewFactoryRegistry = contextualViewFactoryRegistry;
		this.selectionManager = selectionManager;
		initialise();
		initView();
	}

	@Override
	public void refreshView() {
		initialise();
		this.revalidate();
	}

	private synchronized void initialise() {
		mainPanel.removeAll();
		mainPanel.setLayout(new GridBagLayout());

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.1;
		constraints.weighty = 0;

		List<ProcessorBinding> processorBindings = scufl2Tools
				.processorBindingsForProcessor(processor,
						selectionManager.getSelectedProfile());
		if (processorBindings.isEmpty()) {
			JLabel noActivitiesLabel = new JLabel("<html>"
					+ ABSTRACT_PROCESSOR_MSG + "</html>");
			constraints.fill = NONE;
			constraints.anchor = LINE_START;
			mainPanel.add(noActivitiesLabel, constraints);
		} else
			for (ProcessorBinding processorBinding : processorBindings)
				addViewForBinding(constraints, processorBinding);
		mainPanel.revalidate();
		mainPanel.repaint();
		this.revalidate();
		this.repaint();
	}

	private void addViewForBinding(GridBagConstraints constraints,
			ProcessorBinding processorBinding) {
		Activity activity = processorBinding.getBoundActivity();
		List<ContextualViewFactory<? super Activity>> viewFactoryForBeanType = contextualViewFactoryRegistry
				.getViewFactoriesForObject(activity);
		if (viewFactoryForBeanType.isEmpty())
			return;
		// TODO why a list when we only use the first, twice, and assume non-empty too?
		ContextualView view = (ContextualView) viewFactoryForBeanType.get(0)
				.getViews(activity).get(0);

		constraints.anchor = CENTER;
		constraints.fill = HORIZONTAL;
		mainPanel.add(view, constraints);
		Frame frame = getParentFrame(this);
		Action configureAction = view.getConfigureAction(frame);
		if (configureAction != null) {
			constraints.gridy++;
			constraints.fill = NONE;
			constraints.anchor = LINE_START;
			JButton configureButton = new JButton(configureAction);
			if (configureButton.getText() == null
					|| configureButton.getText().isEmpty())
				configureButton.setText("Configure");
			mainPanel.add(configureButton, constraints);
		}
		constraints.gridy++;
	}

	@Override
	public JComponent getMainFrame() {
		return mainPanel;
	}

	@Override
	public String getViewTitle() {
		return "Service";
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}
}
