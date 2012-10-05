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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.ui.HtmlUtils;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.DataLink;

/**
 * Contextual view for a {@link Merge}.
 *
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class MergeContextualView extends ContextualView{

	private DataLink dataLink;
	private List<DataLink> datalinks;
	private WorkflowBundle workflow;
	private JEditorPane editorPane;
	private final EditManager editManager;
	private final FileManager fileManager;
	private final ColourManager colourManager;

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public MergeContextualView(DataLink dataLink, EditManager editManager, FileManager fileManager, ColourManager colourManager) {
		this.dataLink = dataLink;
		datalinks = scufl2Tools.datalinksTo(dataLink.getSendsTo());
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.colourManager = colourManager;
		workflow = fileManager.getCurrentDataflow();
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		editorPane = HtmlUtils.createEditorPane(buildHtml());
		return this.panelForHtml(editorPane);
	}

	@Override
	public String getViewTitle() {
		return "Merge Position";
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
		html += "<tr><td colspan=\"2\"><b>Ordered incoming links</b></td></tr>";

		int counter = 1;

		for (DataLink datalink : datalinks){
			html += "<tr><td>"+ (counter++) + ".</td><td>" + datalink + "</td></tr>";
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
				MergeConfigurationView	mergeConfigurationView = new MergeConfigurationView(datalinks, editManager, fileManager);
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
		return colourManager.getDefaultPropertyMap().get("net.sf.taverna.t2.workflowmodel.Merge");
	}


	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
