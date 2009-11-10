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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.AddLayerFactorySPI;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;

import org.apache.log4j.Logger;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 * 
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 * 
 */
public class ProcessorPredictedBehaviorContextualView extends ContextualView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7675243000874724197L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(ProcessorPredictedBehaviorContextualView.class);
	
	private FileManager fileManager = FileManager.getInstance();

	protected SPIRegistry<AddLayerFactorySPI> addLayerFactories = new SPIRegistry<AddLayerFactorySPI>(
			AddLayerFactorySPI.class);
	
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
		
		List<? extends ProcessorInputPort> inputs = processor.getInputPorts();
		if (!inputs.isEmpty()) {
			html += "<table border=1><tr><th>Input Port Name</th>"
				+ "<th>Size of data</th>" + "</tr>";
			for (ProcessorInputPort ip : inputs) {
				html += "<tr><td>" + ip.getName() + "</td><td>";
				Datalink incoming = ip.getIncomingLink();
				if (incoming == null) {
					html += "No value";
				} else {
					int depth = incoming.getResolvedDepth();
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
		List<? extends ProcessorOutputPort> outputs = processor.getOutputPorts();
		if (!outputs.isEmpty()) {
			html += "<table border=1><tr><th>Output Port Name</th>"
				+ "<th>Size of data</th>" + "</tr>";
			for (ProcessorOutputPort op : outputs) {
				html += "<tr><td>" + op.getName() + "</td><td>";
				Set<? extends Datalink> outgoingSet = op.getOutgoingLinks();
				if (outgoingSet.isEmpty()) {
					html += "No value";
				} else {
					Datalink outgoing = outgoingSet.iterator().next();
					int depth = outgoing.getResolvedDepth();
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
	public Action getConfigureAction(Frame owner) {
		return new AbstractAction("Update prediction") {

			public void actionPerformed(ActionEvent e) {
				fileManager.getCurrentDataflow().checkValidity();
				refreshView();
			}};
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
