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

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.annotationbeans.HostInstitution;
import net.sf.taverna.t2.lang.ui.HtmlUtils;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

@SuppressWarnings("serial")
public abstract class HTMLBasedActivityContextualView<ConfigBean> extends
		ActivityContextualView<ConfigBean> {
	private JEditorPane editorPane;

	public HTMLBasedActivityContextualView(Activity<?> activity) {
		super(activity);
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
		// FIXME would prefer instanceof but no class def found error was thrown
		// even though the pom had the activity in it - spring peoblem?
		if (getActivity().getClass().getName().equalsIgnoreCase(
				"net.sf.taverna.t2.activities.localworker.LocalworkerActivity")) {
			if (checkAnnotations()) {
				String colour = (String) ColourManager
						.getInstance()
						.getProperty(
								"net.sf.taverna.t2.activities.beanshell.BeanshellActivity");
				return colour;
			}
		}
		String colour = (String) ColourManager.getInstance().getProperty(getActivity().getClass().getName());
		return colour == null ? "#ffffff" : colour;
	}

	private boolean checkAnnotations() {
		for (AnnotationChain chain : getActivity().getAnnotations()) {
			for (AnnotationAssertion<?> assertion : chain.getAssertions()) {
				Object detail = assertion.getDetail();
				if (detail instanceof HostInstitution) {
					// this is a user defined localworker so use the beanshell
					// colour!
					return true;
				}
			}
		}
		return false;
	}



	/**
	 * Update the html view with the latest information in the configuration
	 * bean
	 */
	public void refreshView() {
		editorPane.setText(buildHtml());
	}
}
