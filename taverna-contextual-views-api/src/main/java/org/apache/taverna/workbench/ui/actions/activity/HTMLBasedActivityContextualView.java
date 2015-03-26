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

package org.apache.taverna.workbench.ui.actions.activity;

import static org.apache.taverna.lang.ui.HtmlUtils.buildTableOpeningTag;
import static org.apache.taverna.lang.ui.HtmlUtils.createEditorPane;
import static org.apache.taverna.lang.ui.HtmlUtils.getHtmlHead;
import static org.apache.taverna.lang.ui.HtmlUtils.panelForHtml;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.scufl2.api.activity.Activity;

@SuppressWarnings("serial")
public abstract class HTMLBasedActivityContextualView extends ActivityContextualView {
	private static final String BEANSHELL_URI = "http://ns.taverna.org.uk/2010/activity/beanshell";
	private static final String LOCALWORKER_URI = "http://ns.taverna.org.uk/2010/activity/localworker";
	private JEditorPane editorPane;
	private final ColourManager colourManager;

	public HTMLBasedActivityContextualView(Activity activity, ColourManager colourManager) {
		super(activity);
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
		html.append("<tr><th colspan=\"2\">").append(getViewTitle()).append("</th></tr>");
		html.append(getRawTableRowsHtml()).append("</table>");
		html.append("</body></html>");
		return html.toString();
	}

	protected abstract String getRawTableRowsHtml();

	public String getBackgroundColour() {
		String activityType = getActivity().getType().toString();
		if (LOCALWORKER_URI.equals(activityType))
			if (getConfigBean().getJson().get("isAltered").booleanValue())
				return (String) colourManager.getProperty(BEANSHELL_URI);
		String colour = (String) colourManager.getProperty(activityType);
		return colour == null ? "#ffffff" : colour;
	}

	/**
	 * Update the html view with the latest information in the configuration
	 * bean
	 */
	@Override
	public void refreshView() {
		editorPane.setText(buildHtml());
	}
}
