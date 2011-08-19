/**
 *
 */
package net.sf.taverna.t2.workbench.ui.views.contextualviews.dataflow;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import net.sf.taverna.t2.lang.ui.HtmlUtils;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.file.FileManager;
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
	private final FileManager fileManager;
	private final ColourManager colourManager;

	public DataflowContextualView(Dataflow dataflow, FileManager fileManager, ColourManager colourManager) {
		this.dataflow = dataflow;
		this.fileManager = fileManager;
		this.colourManager = colourManager;
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
		String source = "Newly created";
		if (fileManager.getDataflowSource(dataflow) != null) {
			source = fileManager.getDataflowName(dataflow);
		}

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
		return colourManager.getDefaultPropertyMap().get("net.sf.taverna.t2.workflowmodel.Dataflow");
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getPreferredPosition()
	 */
	@Override
	public int getPreferredPosition() {
		return 100;
	}

	private static int MAX_LENGTH = 50;

	private String limitName(String fullName) {
		if (fullName.length() > MAX_LENGTH) {
			return (fullName.substring(0, MAX_LENGTH - 3) + "...");
		}
		return fullName;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView#getViewTitle()
	 */
	@Override
	public String getViewTitle() {
		return "Workflow " + limitName(dataflow.getLocalName());
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
