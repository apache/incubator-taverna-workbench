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

package org.apache.taverna.workbench.retry;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.taverna.lang.ui.ReadOnlyTextArea;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.core.Processor;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 *
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class RetryContextualView extends ContextualView {

	private final Processor processor;

	private final EditManager editManager;

	private final SelectionManager selectionManager;

	private final Scufl2Tools scufl2Tools = new Scufl2Tools();

	private JPanel panel;

	public RetryContextualView(Processor processor, EditManager editManager, SelectionManager selectionManager) {
		super();
		this.processor = processor;
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		initialise();
		initView();
	}

	/*
	 * @Override public Action getConfigureAction(Frame owner) { return new
	 * ConfigureAction(owner); }
	 */
	@Override
	public void refreshView() {
		initialise();
	}

	private void initialise() {
		if (panel == null) {
			panel = createPanel();
		} else {
			panel.removeAll();
		}

		JTextArea textArea = new ReadOnlyTextArea();
		textArea.setEditable(false);
		String text = "";
		int maxRetries = RetryConfigurationPanel.DEFAULT_RETRIES;
		int initialDelay = RetryConfigurationPanel.DEFAULT_INITIAL_DELAY;
		int maxDelay = RetryConfigurationPanel.DEFAULT_MAX_DELAY;
		double backoffFactor = RetryConfigurationPanel.DEFAULT_BACKOFF;

		for (Configuration configuration : scufl2Tools.configurationsFor(processor, selectionManager.getSelectedProfile())) {
			JsonNode processorConfig = configuration.getJson();
			if (processorConfig.has("retry")) {
				JsonNode retryConfig = processorConfig.get("retry");
				if (retryConfig.has("maxRetries")) {
					maxRetries = retryConfig.get("maxRetries").asInt();
				}
				if (retryConfig.has("initialDelay")) {
					initialDelay = retryConfig.get("initialDelay").asInt();
				}
				if (retryConfig.has("maxDelay")) {
					maxDelay = retryConfig.get("maxDelay").asInt();
				}
				if (retryConfig.has("backoffFactor")) {
					backoffFactor = retryConfig.get("backoffFactor").asDouble();;
				}
			}
		}

		if (maxRetries < 1) {
			text += "The service is not re-tried";
		} else if (maxRetries == 1) {
			text += "The service is re-tried once";
			text += " after " + initialDelay + "ms";
		} else {
			text += "The service is re-tried " + maxRetries + " times.  ";
			if (backoffFactor == 1.0) {
				text += "Each time after a delay of " + initialDelay + "ms.";
			} else {
				text += "The first delay is " + initialDelay + "ms";
				int noMaxDelay = (int) (initialDelay * Math.pow(backoffFactor, maxRetries - 1));
				if (noMaxDelay < maxDelay) {
					maxDelay = noMaxDelay;
				}
				text += " with a maximum delay of " + maxDelay + "ms.";
				text += "  Each delay is increased by a factor of " + backoffFactor;
			}
		}
		textArea.setText(text);
		textArea.setBackground(panel.getBackground());
		panel.add(textArea, BorderLayout.CENTER);
		revalidate();
	}

	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	@Override
	public String getViewTitle() {
		return "Retries";
	}

	protected JPanel createPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		result.setOpaque(false);

		return result;
	}

	@Override
	public int getPreferredPosition() {
		return 400;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new RetryConfigureAction(owner, this, processor, editManager, selectionManager);
	}

}
