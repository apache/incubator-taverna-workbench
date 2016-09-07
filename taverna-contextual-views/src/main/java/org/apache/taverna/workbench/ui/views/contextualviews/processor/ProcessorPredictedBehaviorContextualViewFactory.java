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

package org.apache.taverna.workbench.ui.views.contextualviews.processor;

import static java.util.Collections.singletonList;
import static javax.swing.BoxLayout.Y_AXIS;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
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
