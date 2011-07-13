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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.merge;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.ui.HtmlUtils;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * Contextual view for a {@link Merge}.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class MergeContextualView extends ContextualView{
	
	private Merge merge;
	private Dataflow workflow;
	private JEditorPane editorPane;

	public MergeContextualView(Merge merge) {
		this.merge = merge;
		workflow = FileManager.getInstance().getCurrentDataflow();
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		editorPane = HtmlUtils.createEditorPane(buildHtml());
		return this.panelForHtml(editorPane);
	}
	
	@Override
	public String getViewTitle() {
		return "Merge " + merge.getLocalName();
	}
	

	/**
	 * Update the view with the latest information 
	 * from the configuration bean.
	 */
	@Override
	public void refreshView() {
		editorPane.setText(buildHtml());
		repaint();
	}

	private String buildHtml() {
		String html = HtmlUtils.getHtmlHead(getBackgroundColour());
		html += HtmlUtils.buildTableOpeningTag();
		html += "<tr><td colspan=\"2\"><b>" + getViewTitle() + "</b></td></tr>";
		html += "<tr><td colspan=\"2\"><b>Ordered incoming links (entity.port -> merge)</b></td></tr>";

		int counter = 1;
		for (MergeInputPort mergeInputPort : merge.getInputPorts()){	
			EventForwardingOutputPort sourcePort = mergeInputPort.getIncomingLink().getSource();
			// Get the name TokenProcessingEntity (Processor or another Merge or Dataflow) and 
			// its port that contains the source EventForwardingOutputPort
			TokenProcessingEntity entity = Tools.getTokenProcessingEntityWithEventForwardingOutputPort(sourcePort, workflow);
			if (entity != null){
				html += "<tr><td>"+ (counter++) + ".</td><td>" + entity.getLocalName() + "."
						+ sourcePort.getName() + " -> " + merge.getLocalName() 
						+ "</td></tr>";
			}
			
		}
				
		html += "<tr><td colspan=\"2\"><b>Outgoing link (merge -> entity.port)</b></td></tr>";
		Object[] links = merge.getOutputPort().getOutgoingLinks().toArray();	
		// There will be only one link in the set
		EventHandlingInputPort targetPort = ((Datalink) links[0]).getSink();
		TokenProcessingEntity entity = Tools.getTokenProcessingEntityWithEventHandlingInputPort(targetPort,workflow);
		// Find the other part of the link (if any - could have been deleted)
		if (entity != null){
			html += "<tr><td>1.</td><td>" + merge.getLocalName() + " -> "
					+ entity.getLocalName() + "." + targetPort.getName()
					+ "</td></tr>";
		}
		
		html += "</table>";
		html += "</body></html>";
		return html;
	}

	protected JPanel panelForHtml(JEditorPane editorPane) {
		final JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(editorPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JButton configureButton = new JButton(new AbstractAction(){

			public void actionPerformed(ActionEvent e) {
				MergeConfigurationView	mergeConfigurationView = new MergeConfigurationView(merge);
				mergeConfigurationView.setLocationRelativeTo(panel);
				mergeConfigurationView.setVisible(true);
			}
			
		});
		configureButton.setText("Configure");
		buttonPanel.add(configureButton);
		
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		return panel;
	}

	public String getBackgroundColour() {
		return ColourManager
		.getInstance()
		.getDefaultPropertyMap().get("net.sf.taverna.t2.workflowmodel.Merge");
	}
	

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
