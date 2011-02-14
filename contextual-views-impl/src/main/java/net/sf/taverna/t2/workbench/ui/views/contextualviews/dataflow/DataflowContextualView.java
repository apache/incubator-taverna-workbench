/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.views.contextualviews.dataflow;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.ui.HtmlUtils;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;

/**
 * @author alanrw
 *
 */
public class DataflowContextualView extends ContextualView {
	
	private Dataflow dataflow;
	private JEditorPane editorPane;
	
	public DataflowContextualView(Dataflow dataflow) {
		this.dataflow = dataflow;
		initView();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getMainFrame()
	 */
	@Override
	public JComponent getMainFrame() {
		editorPane = HtmlUtils.createEditorPane(buildHtml());
		return HtmlUtils.panelForHtml(editorPane);
	}

	private String buildHtml() {
		String html = HtmlUtils.getHtmlHead(getBackgroundColour());
		html += HtmlUtils.buildTableOpeningTag();
				
		html += "<tr><td colspan=\"2\" align=\"center\"><b>Source</b></td></tr>";
		String source = FileManager.getInstance().getDataflowName(dataflow);
		
		html += "<tr><td colspan=\"2\" align=\"center\">" + source
					+ "</td></tr>";
		if (!dataflow.getInputPorts().isEmpty()) {
			html = html
			+ "<tr><th>Input Port Name</th>" 
				+	"<th>Depth</th>" 
				+"</tr>";
			for (DataflowInputPort dip : dataflow.getInputPorts()) {
				html = html + "<tr><td>" + dip.getName() + "</td><td>"
						+ (dip.getDepth() < 0 ? "invalid/unpredicted" : dip.getDepth()) + "</td></tr>";
			}
		}
		if (!dataflow.getOutputPorts().isEmpty()) {
			html = html
					+ "<tr><th>Output Port Name</th>" 
						+	"<th>Depth</th>" 
						+"</tr>";
			for (DataflowOutputPort dop : dataflow.getOutputPorts()) {
				html = html + "<tr><td>" + dop.getName() + "</td><td>"
						+ (dop.getDepth() < 0 ? "invalid/unpredicted" : dop.getDepth()) + "</td>" 
						+ "</tr>";
			}
		}
		
		html += "</table>";
		html += "</body></html>";
		return html;
	}
	
	public String getBackgroundColour() {
		return ColourManager
		.getInstance()
		.getDefaultPropertyMap().get("net.sf.taverna.t2.workflowmodel.Dataflow");
	}
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getPreferredPosition()
	 */
	@Override
	public int getPreferredPosition() {
		return 100;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getViewTitle()
	 */
	@Override
	public String getViewTitle() {
		return "Workflow " + dataflow.getLocalName();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#refreshView()
	 */
	@Override
	public void refreshView() {
		editorPane.setText(buildHtml());
		repaint();
	}

}
