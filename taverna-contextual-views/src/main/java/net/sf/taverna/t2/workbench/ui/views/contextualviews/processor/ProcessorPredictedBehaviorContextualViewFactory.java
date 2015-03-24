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

import static java.util.Collections.singletonList;
import static javax.swing.BoxLayout.Y_AXIS;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.scufl2.api.common.NamedSet;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.OutputProcessorPort;

/**
 * How to get a panel describing what Taverna predicts the depth of the ports of
 * a processor to be.
 * 
 * @author Stian Soiland-Reyes
 */
public class ProcessorPredictedBehaviorContextualViewFactory implements
		ContextualViewFactory<Processor> {
	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	@Override
	public boolean canHandle(Object selection) {
		return selection instanceof Processor;
	}

	@Override
	@SuppressWarnings("serial")
	public List<ContextualView> getViews(final Processor selection) {
		class ProcessorPredictedBehaviorContextualView extends ContextualView {
			protected JPanel mainPanel = new JPanel();
			protected Processor processor;

			public ProcessorPredictedBehaviorContextualView() {
				super();
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
				mainPanel.setLayout(new BoxLayout(mainPanel, Y_AXIS));

				StringBuilder html = new StringBuilder("<html><head>");
				addStyle(html);
				html.append("</head><body>");

				NamedSet<InputProcessorPort> inputs = processor.getInputPorts();
				if (!inputs.isEmpty()) {
					html.append("<table border=1><tr><th>Input Port Name</th>")
							.append("<th>Size of data</th>").append("</tr>");
					for (InputProcessorPort ip : inputs) {
						html.append("<tr><td>").append(ip.getName())
								.append("</td><td>");
						List<DataLink> incomingDataLinks = scufl2Tools
								.datalinksTo(ip);
						if (incomingDataLinks.isEmpty())
							html.append("No value");
						else {
							int depth = getDepth(incomingDataLinks.get(0));
							if (depth == -1)
								html.append("Invalid");
							else if (depth == 0)
								html.append("Single value");
							else
								html.append("List of depth ").append(depth);
						}
						html.append("</td></tr>");
					}
					html.append("</table>");
				}
				NamedSet<OutputProcessorPort> outputs = processor
						.getOutputPorts();
				if (!outputs.isEmpty()) {
					html.append("<table border=1><tr><th>Output Port Name</th>")
							.append("<th>Size of data</th>").append("</tr>");
					for (OutputProcessorPort op : outputs) {
						html.append("<tr><td>").append(op.getName())
								.append("</td><td>");
						List<DataLink> outgoingDataLinks = scufl2Tools
								.datalinksFrom(op);
						if (outgoingDataLinks.isEmpty())
							html.append("No value");
						else {
							int depth = getDepth(outgoingDataLinks.get(0));
							if (depth == -1)
								html.append("Invalid/unpredicted");
							else if (depth == 0)
								html.append("Single value");
							else
								html.append("List of depth ").append(depth);
						}
						html.append("</td></tr>");
					}
					html.append("</table>");
				}
				if (inputs.isEmpty() && outputs.isEmpty())
					html.append("<p>No port behavior predicted</p>");
				html.append("</body></html>");
				JEditorPane editorPane = new JEditorPane("text/html",
						html.toString());
				editorPane.setEditable(false);
				mainPanel.add(editorPane);

				mainPanel.revalidate();
				mainPanel.repaint();
				this.revalidate();
				this.repaint();
			}

			protected void addStyle(StringBuilder html) {
				html.append("<style type='text/css'>")
						.append("table {align:center; border:solid black 1px;")
						.append("width:100%; height:100%; overflow:auto;}")
						.append("</style>");
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

		return singletonList((ContextualView) new ProcessorPredictedBehaviorContextualView());
	}

	private int getDepth(DataLink datalink) {
		// TODO calculate actual depth
		return -1;
	}
}
