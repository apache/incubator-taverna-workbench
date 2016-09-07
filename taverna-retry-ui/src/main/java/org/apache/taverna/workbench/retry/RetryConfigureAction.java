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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.helper.HelpEnabledDialog;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workflow.edits.AddChildEdit;
import org.apache.taverna.workflow.edits.ChangeJsonEdit;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public class RetryConfigureAction extends AbstractAction {

	private static Logger logger = Logger.getLogger(RetryConfigureAction.class);

	private final Frame owner;
	private final Processor processor;
	private final RetryContextualView retryContextualView;

	private final EditManager editManager;
	private final SelectionManager selectionManager;

	private final Scufl2Tools scufl2Tools = new Scufl2Tools();

	private Configuration configuration;

	public RetryConfigureAction(Frame owner, RetryContextualView retryContextualView,
			Processor processor, EditManager editManager, SelectionManager selectionManager) {
		super("Configure");
		this.owner = owner;
		this.retryContextualView = retryContextualView;
		this.processor = processor;
		this.editManager = editManager;
		this.selectionManager = selectionManager;
	}

	public void actionPerformed(ActionEvent e) {
		String title = "Retries for service " + processor.getName();
		final JDialog dialog = new HelpEnabledDialog(owner, title, true);
		Configuration configuration;
		try {
			configuration = scufl2Tools.configurationFor(processor, selectionManager.getSelectedProfile());
		} catch (IndexOutOfBoundsException ex) {
			configuration = new Configuration();
		}
		RetryConfigurationPanel retryConfigurationPanel = new RetryConfigurationPanel(configuration);
		dialog.add(retryConfigurationPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton okButton = new JButton(new OKAction(dialog, retryConfigurationPanel));
		buttonPanel.add(okButton);

		JButton resetButton = new JButton(new ResetAction(retryConfigurationPanel));
		buttonPanel.add(resetButton);

		JButton cancelButton = new JButton(new CancelAction(dialog));
		buttonPanel.add(cancelButton);

		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	public class ResetAction extends AbstractAction {

		private final RetryConfigurationPanel retryConfigurationPanel;

		public ResetAction(RetryConfigurationPanel retryConfigurationPanel) {
			super("Reset");
			this.retryConfigurationPanel = retryConfigurationPanel;
		}

		public void actionPerformed(ActionEvent e) {
			retryConfigurationPanel.populate();
		}

	}

	public class OKAction extends AbstractAction {

		private final RetryConfigurationPanel retryConfigurationPanel;
		private final JDialog dialog;

		public OKAction(JDialog dialog, RetryConfigurationPanel retryConfigurationPanel) {
			super("OK");
			this.dialog = dialog;
			this.retryConfigurationPanel = retryConfigurationPanel;
		}

		public void actionPerformed(ActionEvent e) {
			if (retryConfigurationPanel.validateConfig()) {
				try {
					try {
						Configuration configuration = scufl2Tools.configurationFor(processor, selectionManager.getSelectedProfile());
						ObjectNode json = configuration.getJsonAsObjectNode().deepCopy();
						ObjectNode parallelizeNode = null;
						if (json.has("retry")) {
							parallelizeNode = (ObjectNode) json.get("retry");
						} else {
							parallelizeNode = json.objectNode();
							json.put("retry", parallelizeNode);
						}
						JsonNode newParallelizeNode = retryConfigurationPanel.getJson();
						Iterator<Entry<String, JsonNode>> fields = newParallelizeNode.fields();
						while (fields.hasNext()) {
							Entry<String, JsonNode> entry = fields.next();
							parallelizeNode.set(entry.getKey(), entry.getValue());
						}
						Edit<Configuration> edit = new ChangeJsonEdit(configuration, json);
						editManager.doDataflowEdit(selectionManager.getSelectedWorkflowBundle(), edit);
					} catch (IndexOutOfBoundsException ex) {
						Configuration configuration = new Configuration();
						configuration.setConfigures(processor);
						ObjectNode json = configuration.getJsonAsObjectNode();
						json.put("retry", retryConfigurationPanel.getJson());
						Edit<Profile> edit = new AddChildEdit<Profile>(selectionManager.getSelectedProfile(), configuration);
						editManager.doDataflowEdit(selectionManager.getSelectedWorkflowBundle(), edit);
					}
					dialog.setVisible(false);
					if (retryContextualView != null) {
						retryContextualView.refreshView();
					}
				} catch (EditException e1) {
					logger.warn("Could not configure retries", e1);
					JOptionPane.showMessageDialog(owner, "Could not configure retries",
							"An error occured when configuring retries: " + e1.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}

	}

	public class CancelAction extends AbstractAction {

		private final JDialog dialog;

		public CancelAction(JDialog dialog) {
			super("Cancel");
			this.dialog = dialog;

		}

		public void actionPerformed(ActionEvent e) {
			dialog.setVisible(false);
		}

	}

}
