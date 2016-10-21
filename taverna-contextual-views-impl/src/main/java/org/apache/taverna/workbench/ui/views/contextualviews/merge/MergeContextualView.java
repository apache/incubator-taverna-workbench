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
package org.apache.taverna.workbench.ui.views.contextualviews.merge;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.FlowLayout.LEFT;
import static org.apache.taverna.lang.ui.HtmlUtils.buildTableOpeningTag;
import static org.apache.taverna.lang.ui.HtmlUtils.createEditorPane;
import static org.apache.taverna.lang.ui.HtmlUtils.getHtmlHead;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workflowmodel.Merge;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.DataLink;

/**
 * Contextual view for a {@link Merge}.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
class MergeContextualView extends ContextualView {
	@SuppressWarnings("unused")
	private DataLink dataLink;
	private List<DataLink> datalinks;
	@SuppressWarnings("unused")
	private WorkflowBundle workflow;
	private JEditorPane editorPane;
	private final EditManager editManager;
	private final ColourManager colourManager;
	private final SelectionManager selectionManager;

	// TODO inject from Spring via factory?
	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public MergeContextualView(DataLink dataLink, EditManager editManager,
			SelectionManager selectionManager, ColourManager colourManager) {
		this.dataLink = dataLink;
		this.selectionManager = selectionManager;
		datalinks = scufl2Tools.datalinksTo(dataLink.getSendsTo());
		this.editManager = editManager;
		this.colourManager = colourManager;
		workflow = selectionManager.getSelectedWorkflowBundle();
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		editorPane = createEditorPane(buildHtml());
		return panelForHtml(editorPane);
	}

	@Override
	public String getViewTitle() {
		return "Merge Position";
	}

	/**
	 * Update the view with the latest information from the configuration bean.
	 */
	@Override
	public void refreshView() {
		editorPane.setText(buildHtml());
		repaint();
	}

	private String buildHtml() {
		StringBuilder html = new StringBuilder(
				getHtmlHead(getBackgroundColour()));
		html.append(buildTableOpeningTag())
				.append("<tr><td colspan=\"2\"><b>")
				.append(getViewTitle())
				.append("</b></td></tr>")
				.append("<tr><td colspan=\"2\"><b>Ordered incoming links</b></td></tr>");

		int counter = 1;
		for (DataLink datalink : datalinks)
			html.append("<tr><td>").append(counter++).append(".</td><td>")
					.append(datalink).append("</td></tr>");

		return html.append("</table>").append("</body></html>").toString();
	}

	protected JPanel panelForHtml(JEditorPane editorPane) {
		final JPanel panel = new JPanel();

		JPanel buttonPanel = new JPanel(new FlowLayout(LEFT));

		JButton configureButton = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MergeConfigurationView mergeConfigurationView = new MergeConfigurationView(
						datalinks, editManager, selectionManager);
				mergeConfigurationView.setLocationRelativeTo(panel);
				mergeConfigurationView.setVisible(true);
			}
		});
		configureButton.setText("Configure");
		buttonPanel.add(configureButton);

		panel.setLayout(new BorderLayout());
		panel.add(editorPane, CENTER);
		panel.add(buttonPanel, SOUTH);
		return panel;
	}

	public String getBackgroundColour() {
		return colourManager.getDefaultPropertyMap().get(
				"org.apache.taverna.workflowmodel.Merge");
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}
}
