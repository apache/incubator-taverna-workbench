/*******************************************************************************
 * Copyright (C) 2007-2008 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.processor;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import uk.org.taverna.scufl2.api.common.NamedSet;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 *
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class ProcessorPredictedBehaviorContextualView extends ContextualView {

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	protected JPanel mainPanel = new JPanel();

	protected Processor processor;

	public ProcessorPredictedBehaviorContextualView(Processor processor) {
		super();
		this.processor = processor;
		refreshView();
		initView();
	}

	@Override
	public void refreshView() {
		initialise();
		this.revalidate();
	}


	private synchronized void initialise() {
		mainPanel.removeAll();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		String html ="<html><head>" + getStyle() + "</head><body>";

		NamedSet<InputProcessorPort> inputs = processor.getInputPorts();
		if (!inputs.isEmpty()) {
			html += "<table border=1><tr><th>Input Port Name</th>"
				+ "<th>Size of data</th>" + "</tr>";
			for (InputProcessorPort ip : inputs) {
				html += "<tr><td>" + ip.getName() + "</td><td>";
				List<DataLink> incomingDataLinks = scufl2Tools.datalinksTo(ip);
				if (incomingDataLinks.isEmpty()) {
					html += "No value";
				} else {
					DataLink incoming = incomingDataLinks.get(0);
					int depth = -1;// TODO calculate actual depth
					if (depth == -1) {
						html += "Invalid";
					} else if (depth == 0) {
						html += "Single value";
					} else {
						html += "List of depth " + depth;
					}
				}
				html += "</td></tr>";
			}
			html += "</table>";
		}
		NamedSet<OutputProcessorPort> outputs = processor.getOutputPorts();
		if (!outputs.isEmpty()) {
			html += "<table border=1><tr><th>Output Port Name</th>"
				+ "<th>Size of data</th>" + "</tr>";
			for (OutputProcessorPort op : outputs) {
				html += "<tr><td>" + op.getName() + "</td><td>";
				List<DataLink> outgoingDataLinks = scufl2Tools.datalinksFrom(op);
				if (outgoingDataLinks.isEmpty()) {
					html += "No value";
				} else {
					DataLink outgoing = outgoingDataLinks.get(0);
					int depth = -1;// TODO calculate actual depth
					if (depth == -1) {
						html += "Invalid/unpredicted";
					} else if (depth == 0) {
						html += "Single value";
					} else {
						html += "List of depth " + depth;
					}
				}
				html += "</td></tr>";
			}
			html += "</table>";
		}
		if (inputs.isEmpty() && outputs.isEmpty()) {
			html += "<p>No port behavior predicted</p>";
		}
		html +="</body></html>";
		JEditorPane editorPane = new JEditorPane("text/html", html);
		editorPane.setEditable(false);
		mainPanel.add(editorPane);

		mainPanel.revalidate();
		mainPanel.repaint();
		this.revalidate();
		this.repaint();
	}

	protected String getStyle() {
		String style = "<style type='text/css'>";
		style += "table {align:center; border:solid black 1px;"
				+ "width:100%; height:100%; overflow:auto;}";
		style += "</style>";
		return style;
	}

	@Override
	public JComponent getMainFrame() {
		return mainPanel;
	}

	@Override
	public String getViewTitle() {
		return "Predicted behavior";
	}

	@Override
	public int getPreferredPosition() {
		return 300;
	}
}
