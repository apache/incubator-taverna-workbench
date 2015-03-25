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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.FlowLayout.LEFT;
import static net.sf.taverna.t2.lang.ui.HtmlUtils.buildTableOpeningTag;
import static net.sf.taverna.t2.lang.ui.HtmlUtils.createEditorPane;
import static net.sf.taverna.t2.lang.ui.HtmlUtils.getHtmlHead;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workflowmodel.Merge;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.DataLink;

/**
 * Contextual view for a {@link Merge}.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
class MergeContextualView extends ContextualView {
	@SuppressWarnings("unused")
	private DataLink dataLink;
	private List<DataLink> datalinks;
	@SuppressWarnings("unused")
	private WorkflowBundle workflow;
	private JEditorPane editorPane;
	private final EditManager editManager;
	private final ColourManager colourManager;
	private final SelectionManager selectionManager;

	// TODO inject from Spring via factory?
	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public MergeContextualView(DataLink dataLink, EditManager editManager,
			SelectionManager selectionManager, ColourManager colourManager) {
		this.dataLink = dataLink;
		this.selectionManager = selectionManager;
		datalinks = scufl2Tools.datalinksTo(dataLink.getSendsTo());
		this.editManager = editManager;
		this.colourManager = colourManager;
		workflow = selectionManager.getSelectedWorkflowBundle();
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		editorPane = createEditorPane(buildHtml());
		return panelForHtml(editorPane);
	}

	@Override
	public String getViewTitle() {
		return "Merge Position";
	}

	/**
	 * Update the view with the latest information from the configuration bean.
	 */
	@Override
	public void refreshView() {
		editorPane.setText(buildHtml());
		repaint();
	}

	private String buildHtml() {
		StringBuilder html = new StringBuilder(
				getHtmlHead(getBackgroundColour()));
		html.append(buildTableOpeningTag())
				.append("<tr><td colspan=\"2\"><b>")
				.append(getViewTitle())
				.append("</b></td></tr>")
				.append("<tr><td colspan=\"2\"><b>Ordered incoming links</b></td></tr>");

		int counter = 1;
		for (DataLink datalink : datalinks)
			html.append("<tr><td>").append(counter++).append(".</td><td>")
					.append(datalink).append("</td></tr>");

		return html.append("</table>").append("</body></html>").toString();
	}

	protected JPanel panelForHtml(JEditorPane editorPane) {
		final JPanel panel = new JPanel();

		JPanel buttonPanel = new JPanel(new FlowLayout(LEFT));

		JButton configureButton = new JButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MergeConfigurationView mergeConfigurationView = new MergeConfigurationView(
						datalinks, editManager, selectionManager);
				mergeConfigurationView.setLocationRelativeTo(panel);
				mergeConfigurationView.setVisible(true);
			}
		});
		configureButton.setText("Configure");
		buttonPanel.add(configureButton);

		panel.setLayout(new BorderLayout());
		panel.add(editorPane, CENTER);
		panel.add(buttonPanel, SOUTH);
		return panel;
	}

	public String getBackgroundColour() {
		return colourManager.getDefaultPropertyMap().get(
				"net.sf.taverna.t2.workflowmodel.Merge");
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}
}
