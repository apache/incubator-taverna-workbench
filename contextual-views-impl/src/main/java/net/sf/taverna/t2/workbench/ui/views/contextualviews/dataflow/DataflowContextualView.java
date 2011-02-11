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
		return panelForHtml(buildHtml());
	}

	private JComponent panelForHtml(String html) {
		final JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		editorPane = new JEditorPane("text/html", html);
		editorPane.setEditable(false);
		panel.add(editorPane, BorderLayout.CENTER);
		
		return panel;
	}

	private String buildHtml() {
		String html = "<html><head>" + getStyle() + "</head><body>";
		html += buildTableOpeningTag();
				
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
	
	private String buildTableOpeningTag() {
		String result = "<table ";
		Map<String, String> props = getTableProperties();
		for (String key : props.keySet()) {
			result += key + "=\"" + props.get(key) + "\" ";
		}
		result += ">";
		return result;
	}

	protected Map<String, String> getTableProperties() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("border", "1");
		return result;
	}

	protected String getStyle() {
		String backgroundColour = ColourManager
		.getInstance()
		.getDefaultPropertyMap().get("net.sf.taverna.t2.workflowmodel.Dataflow"); 
		String style = "<style type='text/css'>";
		style += "table {align:center; border:solid black 1px; background-color:\""+backgroundColour+"\";width:100%; height:100%; overflow:auto;}";
		style += "</style>";
		return style;
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
