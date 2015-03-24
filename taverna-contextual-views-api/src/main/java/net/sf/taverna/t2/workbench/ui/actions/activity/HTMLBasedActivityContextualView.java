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

import static net.sf.taverna.t2.lang.ui.HtmlUtils.buildTableOpeningTag;
import static net.sf.taverna.t2.lang.ui.HtmlUtils.createEditorPane;
import static net.sf.taverna.t2.lang.ui.HtmlUtils.getHtmlHead;
import static net.sf.taverna.t2.lang.ui.HtmlUtils.panelForHtml;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
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
