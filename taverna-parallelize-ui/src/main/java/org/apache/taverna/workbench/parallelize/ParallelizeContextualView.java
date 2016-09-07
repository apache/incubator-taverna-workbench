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

package org.apache.taverna.workbench.parallelize;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.taverna.lang.ui.ReadOnlyTextArea;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.core.Processor;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 *
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class ParallelizeContextualView extends ContextualView {

	private final Scufl2Tools scufl2Tools = new Scufl2Tools();

	private Processor processor;

	private JPanel panel;

	private final EditManager editManager;

	private final SelectionManager selectionManager;

	public ParallelizeContextualView(Processor processor, EditManager editManager, SelectionManager selectionManager) {
		super();
		this.processor = processor;
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		initialise();
		initView();
	}

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
		String maxJobs = "1";
		for (Configuration configuration : scufl2Tools.configurationsFor(processor, selectionManager.getSelectedProfile())) {
			JsonNode processorConfig = configuration.getJson();
			if (processorConfig.has("parallelize")) {
				JsonNode parallelizeConfig = processorConfig.get("parallelize");
				if (parallelizeConfig.has("maximumJobs")) {
					maxJobs = parallelizeConfig.get("maximumJobs").asText();
				}
			}
		}
		textArea.setText("The maximum number of jobs is " + maxJobs);
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
	    return "Parallel jobs";
	}

	protected JPanel createPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());


		return result;
	}

	@Override
	public int getPreferredPosition() {
		return 400;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new ParallelizeConfigureAction(owner, this, processor, editManager, selectionManager);
	}


}
