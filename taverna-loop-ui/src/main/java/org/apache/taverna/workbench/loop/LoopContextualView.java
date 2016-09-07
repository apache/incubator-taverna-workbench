package org.apache.taverna.workbench.loop;
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

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.loop.comparisons.Comparison;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.Processor;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 *
 * @author Stian Soiland-Reyes
 *
 */
public class LoopContextualView extends ContextualView {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(LoopContextualView.class);

	private EditManager editManager;
	private FileManager fileManager;

	private Loop loopLayer;

	private JPanel panel;

	private Processor processor;

	public LoopContextualView(Processor processor, EditManager editManager, FileManager fileManager) {
		super();
		this.loopLayer = loopLayer;
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.processor = processor;
		initialise();
		initView();
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new LoopConfigureAction(owner, this, processor, editManager, fileManager);
	}

	@Override
	public void refreshView() {
		initialise();
	}

	private void initialise() {
		if (panel == null) {
			panel = new JPanel();
		} else {
			panel.removeAll();
		}
		panel.setLayout(new GridBagLayout());
		updateUIByConfig();
	}

	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	@Override
	public String getViewTitle() {
		return "Loop of " + processor.getLocalName();
	}

	protected void updateUIByConfig() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		StringBuilder description = new StringBuilder("<html><body>");
		Properties properties = loopLayer.getConfiguration().getProperties();
		if (properties.getProperty(ActivityGenerator.COMPARISON,
				ActivityGenerator.CUSTOM_COMPARISON).equals(
				ActivityGenerator.CUSTOM_COMPARISON)) {
			Activity<?> condition = loopLayer.getConfiguration().getCondition();
			if (condition != null) {
				description.append("Looping using custom conditional ");
				if (condition instanceof BeanshellActivity) {
					String script = ((BeanshellActivity)condition).getConfiguration().getScript();
					if (script != null) {
						if (script.length() <= 100) {
							description.append("<pre>\n");
							description.append(script);
							description.append("</pre>\n");
						}
					}
				}
			} else {
				description.append("<i>Unconfigured, will not loop</i>");
			}
		} else {
			description.append("The service will be invoked repeatedly ");
			description.append("until<br> its output <strong>");
			description.append(properties
					.getProperty(ActivityGenerator.COMPARE_PORT));
			description.append("</strong> ");

			Comparison comparison = ActivityGenerator
					.getComparisonById(properties
							.getProperty(ActivityGenerator.COMPARISON));
			description.append(comparison.getName());

			description.append(" the " + comparison.getValueType() + ": <pre>");
			description.append(properties
					.getProperty(ActivityGenerator.COMPARE_VALUE));
			description.append("</pre>");

			String delay = properties.getProperty(ActivityGenerator.DELAY, "");
			try {
				if (Double.parseDouble(delay) > 0) {
					description.append("adding a delay of " + delay
							+ " seconds between loops.");
				}
			} catch (NumberFormatException ex) {
			}
		}
		description.append("</body></html>");

		panel.add(new JLabel(description.toString()), gbc);
		gbc.gridy++;

		revalidate();
	}



	@Override
	public int getPreferredPosition() {
		return 400;
	}

}
