/**
 *
 */
package net.sf.taverna.t2.activities.unrecognized.views;

import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.port.InputActivityPort;
import org.apache.taverna.scufl2.api.port.OutputActivityPort;

/**
 * A UnrecognizedContextualView displays information about a UnrecognizedActivity
 *
 * @author alanrw
 * @author David Withers
 */
@SuppressWarnings("serial")
public class UnrecognizedContextualView extends HTMLBasedActivityContextualView {

	public UnrecognizedContextualView(Activity activity, ColourManager colourManager) {
		super(activity, colourManager);
	}

	/**
	 * The table for the UnrecognizedActivity shows its ports.
	 *
	 * @return
	 */
	@Override
	protected String getRawTableRowsHtml() {
		StringBuilder html = new StringBuilder();
		html.append("<tr><th>Input Port Name</th><th>Depth</th></tr>");
		for (InputActivityPort inputActivityPort : getActivity().getInputPorts()) {
			html.append("<tr><td>" + inputActivityPort.getName() + "</td><td>");
			html.append(inputActivityPort.getDepth() + "</td></tr>");
		}
		html.append("<tr><th>Output Port Name</th><th>Depth</th></tr>");
		for (OutputActivityPort outputActivityPort : getActivity().getOutputPorts()) {
			html.append("<tr><td>" + outputActivityPort.getName() + "</td><td>");
			html.append(outputActivityPort.getDepth() + "</td></tr>");
		}
		return html.toString();
	}

	@Override
	public String getViewTitle() {
		return "Unrecognized service";
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
