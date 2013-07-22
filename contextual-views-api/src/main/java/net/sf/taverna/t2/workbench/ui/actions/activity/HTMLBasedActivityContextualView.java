/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.ui.actions.activity;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import net.sf.taverna.t2.lang.ui.HtmlUtils;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.configurations.Configuration;

@SuppressWarnings("serial")
public abstract class HTMLBasedActivityContextualView extends ActivityContextualView {

	private JEditorPane editorPane;
	private final ColourManager colourManager;

	public HTMLBasedActivityContextualView(Activity activity, ColourManager colourManager) {
		super(activity);
		this.colourManager = colourManager;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		editorPane = HtmlUtils.createEditorPane(buildHtml());
		return HtmlUtils.panelForHtml(editorPane);
	}

	private String buildHtml() {
		String html = HtmlUtils.getHtmlHead(getBackgroundColour());
		html += HtmlUtils.buildTableOpeningTag();
		html += "<tr><th colspan=\"2\">" + getViewTitle() + "</th></tr>";
		html += getRawTableRowsHtml() + "</table>";
		html += "</body></html>";
		return html;
	}

	protected abstract String getRawTableRowsHtml();

	public String getBackgroundColour() {
		String activityType = getActivity().getType().toString();
		if ("http://ns.taverna.org.uk/2010/activity/localworker".equals(activityType)) {
			Configuration configuration = getConfigBean();
			if (configuration.getJson().get("isAltered").booleanValue()) {
				String colour = (String) colourManager
						.getProperty("http://ns.taverna.org.uk/2010/activity/beanshell");
				return colour;
			}
		}
		String colour = (String) colourManager.getProperty(activityType);
		return colour == null ? "#ffffff" : colour;
	}

	/**
	 * Update the html view with the latest information in the configuration
	 * bean
	 */
	public void refreshView() {
		editorPane.setText(buildHtml());
	}

}
