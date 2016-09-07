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

package org.apache.taverna.workbench.ui.views.contextualviews.dataflow;

import static org.apache.taverna.lang.ui.HtmlUtils.buildTableOpeningTag;
import static org.apache.taverna.lang.ui.HtmlUtils.createEditorPane;
import static org.apache.taverna.lang.ui.HtmlUtils.getHtmlHead;
import static org.apache.taverna.lang.ui.HtmlUtils.panelForHtml;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;

/**
 * @author alanrw
 */
@SuppressWarnings("serial")
class DataflowContextualView extends ContextualView {
	private static int MAX_LENGTH = 50;
	private static final String ELLIPSIS = "...";

	private Workflow dataflow;
	private JEditorPane editorPane;
	private final FileManager fileManager;
	private final ColourManager colourManager;

	public DataflowContextualView(Workflow dataflow, FileManager fileManager,
			ColourManager colourManager) {
		this.dataflow = dataflow;
		this.fileManager = fileManager;
		this.colourManager = colourManager;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		editorPane = createEditorPane(buildHtml());
		return panelForHtml(editorPane);
	}

	private String buildHtml() {
		StringBuilder html = new StringBuilder(getHtmlHead(getBackgroundColour()));
		html.append(buildTableOpeningTag());

		html.append("<tr><td colspan=\"2\" align=\"center\"><b>Source</b></td></tr>");
		String source = "Newly created";
		if (fileManager.getDataflowSource(dataflow.getParent()) != null)
			source = fileManager.getDataflowName(dataflow.getParent());

		html.append("<tr><td colspan=\"2\" align=\"center\">").append(source)
				.append("</td></tr>");
		if (!dataflow.getInputPorts().isEmpty()) {
			html.append("<tr><th>Input Port Name</th><th>Depth</th></tr>");
			for (InputWorkflowPort dip : dataflow.getInputPorts())
				html.append("<tr><td>")
						.append(dip.getName())
						.append("</td><td>")
						.append(dip.getDepth() < 0 ? "invalid/unpredicted"
								: dip.getDepth()).append("</td></tr>");
		}
		if (!dataflow.getOutputPorts().isEmpty()) {
			html.append("<tr><th>Output Port Name</th><th>Depth</th></tr>");
			for (OutputWorkflowPort dop : dataflow.getOutputPorts())
				html.append("<tr><td>")
						.append(dop.getName())
						.append("</td><td>")
						.append(/*(dop.getDepth() < 0 ?*/ "invalid/unpredicted" /*: dop.getDepth())*/)
						.append("</td>" + "</tr>");
		}

		return html.append("</table>").append("</body></html>").toString();
	}

	public String getBackgroundColour() {
		return colourManager.getDefaultPropertyMap().get(
				Dataflow.class.toString());
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

	private String limitName(String fullName) {
		if (fullName.length() <= MAX_LENGTH)
			return fullName;
		return fullName.substring(0, MAX_LENGTH - ELLIPSIS.length()) + ELLIPSIS;
	}

	@Override
	public String getViewTitle() {
		return "Workflow " + limitName(dataflow.getName());
	}

	@Override
	public void refreshView() {
		editorPane.setText(buildHtml());
		repaint();
	}
}
