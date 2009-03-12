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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.iterationstrategy;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;

public class IterationStrategyContextualView extends ContextualView{
	
	private IterationStrategy strategy;
	private JPanel panel;
	private JEditorPane editorPane;

	public IterationStrategyContextualView(IterationStrategy strategy) {
		this.strategy = strategy;
//		initialise();
		initView();
	}

//	private void initialise() {
//		panel = new JPanel();
//		panel.setLayout(new GridBagLayout());
//		Map<String, Integer> desiredCardinalities = strategy.getDesiredCardinalities();
//		
//	}

	@Override
	public JComponent getMainFrame() {
		String html = buildHtml();
		String style = getStyle();
		return panelForHtml(style + html);
	}

	private String buildHtml() {
		String html = buildTableOpeningTag();
		html += "<tr><td colspan=2>" + getViewTitle() + "</td></tr>";
		html += getRawTableRowsHtml() + "</table>";
		return html;
	}

	private String buildTableOpeningTag() {
		String result = "<table ";
		Map<String, String> props = getTableProperties();
		for (String key : props.keySet()) {
			result += key + "=\"" + props.get(key) + "\" ";
		}
		result += ">";
		return result;
	}

	private String getRawTableRowsHtml() {
		String html = "<tr><th>Port Name</th><th>Desired Cardinality</th></tr>";
		Map<String, Integer> desiredCardinalities = strategy.getDesiredCardinalities();
		Set<Entry<String, Integer>> entrySet = desiredCardinalities.entrySet();
		Iterator<Entry<String, Integer>> iterator = entrySet.iterator();
		while(iterator.hasNext()){
			Entry<String, Integer> next = iterator.next();
			html= html + "<tr><td>" + next.getKey() +"</td><td>" + next.getValue() +"</td></tr>";
		}
		return html;
	}

	private Map<String, String> getTableProperties() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("width", "100%");
		result.put("bgcolor", getBackgroundColour());
		result.put("border", "1");
		result.put("align", "center");
		return result;
	}

	public String getBackgroundColour() {
		return "gray";
	}

	private String getStyle() {
		String style = "<style type='text/css'>";
		style += "table {align:center}";
		style += "</style>";
		return style;
	}

	private JPanel panelForHtml(String html) {
		JPanel result = new JPanel();
		result.setLayout(new BorderLayout());
		editorPane = new JEditorPane("text/html", html);
		editorPane.setEditable(false);
		result.add(editorPane, BorderLayout.CENTER);
		return result;
	}

	/**
	 * Update the html view with the latest information in the configuration
	 * bean
	 */
	public void refreshView() {
		String html = buildHtml();
		String style = getStyle();
		editorPane.setText(style + html);
	}

	@Override
	public String getViewTitle() {
		return "Iteration Strategy Contextual View";
	}

}
