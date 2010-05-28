/**
 * 
 */
package net.sf.taverna.t2.workbench.report.explainer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;

import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workbench.report.IncompleteDataflowKind;
import net.sf.taverna.t2.workbench.report.view.ReportViewConfigureAction;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;

import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;

/**
 * @author alanrw
 *
 */
public class BasicExplainer implements VisitExplainer {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.report.explainer.VisitExplainer#canExplain(net.sf.taverna.t2.visit.VisitKind, int)
	 */
	public boolean canExplain(VisitKind vk, int resultId) {
		if (vk instanceof IncompleteDataflowKind) {
			return true;
		}
		if ((vk instanceof HealthCheck) &&
				((resultId == HealthCheck.INVALID_SCRIPT) ||
				(resultId == HealthCheck.CONNECTION_PROBLEM) ||
				(resultId == HealthCheck.INVALID_URL) ||
				(resultId == HealthCheck.IO_PROBLEM) ||
				(resultId == HealthCheck.TIME_OUT) ||
				(resultId == HealthCheck.MISSING_DEPENDENCY) ||
				(resultId == HealthCheck.DEFAULT_VALUE) ||
				(resultId == HealthCheck.BAD_WSDL) ||
				(resultId == HealthCheck.NOT_HTTP) ||
				(resultId == HealthCheck.UNSUPPORTED_STYLE))) {
			return true;
		}
		return false;
	}

	public JComponent getExplanation(VisitReport vr) {
		VisitKind vk = vr.getKind();
		int resultId = vr.getResultId();
		if ((vk instanceof IncompleteDataflowKind) && (resultId == IncompleteDataflowKind.INCOMPLETE_DATAFLOW)) {
			return explanationDataflowIncomplete(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_SCRIPT)) {
			return explanationBeanshellInvalidScript(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.CONNECTION_PROBLEM)) {
			return explanationConnectionProblem(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_URL)) {
			return explanationInvalidUrl(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.TIME_OUT)) {
			return explanationTimeOut(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.IO_PROBLEM)) {
			return explanationIoProblem(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.MISSING_DEPENDENCY)) {
			return explanationMissingDependency(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.DEFAULT_VALUE)) {
			return explanationDefaultValue(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.BAD_WSDL)) {
			return explanationBadWSDL(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.NOT_HTTP)) {
			return explanationNotHTTP(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.UNSUPPORTED_STYLE)) {
			return explanationUnsupportedStyle(vr);
		}
		return null;
	}

	public JComponent getSolution(VisitReport vr) {
		VisitKind vk = vr.getKind();
		int resultId = vr.getResultId();
//		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.CONNECTION_PROBLEM)) {
//			return solutionConnectionProblem(vr);
//		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_SCRIPT)) {
			return solutionBeanshellInvalidScript(vr);
		}
		return null;
	}
	
	private static JComponent explanationDataflowIncompleteComponent = new JLabel("A workflow must contain at least one service or at least one output port");
	private static JComponent explanationDataflowIncomplete(VisitReport vr) {
		return explanationDataflowIncompleteComponent;
	}
	
	private static JComponent explanationConnectionProblem(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "the endpoint";
		}
		String responseCode = (String) (vr.getProperty("responseCode"));
		if (responseCode == null) {
			responseCode = "an unexpected response code";
		}
		return new JLabel("Taverna connected to \"" + endpoint + "\" but got back " + responseCode);
	}
	
	private static JComponent explanationIoProblem(VisitReport vr) {
		String message = "Connecting to ";
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			message += "the endpoint";
		} else {
			message += "\"" + endpoint + "\" caused ";
		}
		Exception e = (Exception) (vr.getProperty("exception"));
		if (e == null) {
			message += "an exception";
		} else {
			message += "\"" + e.getMessage() + "\"";
		}
		
		return new JLabel(message);
	}
	
	private static JComponent explanationInvalidUrl(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "the endpoint";
		}
		return new JLabel("Taverna was unable to connect to \"" + endpoint + "\" because it is not a valid URL");
	}
	
	private static JComponent explanationTimeOut(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "the endpoint";
		}
		String timeOutString = (String) (vr.getProperty("timeOut"));
		if (timeOutString == null) {
			timeOutString = " the timeout limit";
		} else {
			timeOutString += "ms";
		}
		return new JLabel("Taverna was unable to connect to \"" + endpoint + "\" within " + timeOutString);
	}
	
	private static JComponent explanationMissingDependency(VisitReport vr) {
		Set<String> dependencies = (Set<String>) (vr.getProperty("dependencies"));
		String message = "Taverna could not find ";
		if (dependencies == null) {
			message += "some dependencies";
		} else {
			for (String s : dependencies) {
				message += s;
				message += " ";
			}
		}
		return new JLabel(message);
	}
	
	private static JComponent explanationDefaultValue(VisitReport vr) {
		String value = (String) (vr.getProperty("value"));
		if (value == null) {
			value = "the default value";
		}
		return new JLabel("The service still has its value set to \"" + value + "\"");
	}
	
	private static JComponent explanationBadWSDL(VisitReport vr) {
		Exception e = (Exception) (vr.getProperty("exception"));
		String message = "Parsing the WSDL caused ";
		if (e == null) {
			message += " an exception";
		} else {
			message += "\"" + e.getMessage() + "\"";
		}
		return new JLabel(message);
	}
	
	private static JComponent explanationNotHTTP(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "the endpoint";
		} else {
			endpoint = "\"" + endpoint + "\"";
		}
		return new JLabel("Taverna was unable to check " + endpoint + " as it is not a HTTP URL");
	}
	
	private static JComponent explanationUnsupportedStyle(VisitReport vr) {
		String message = "Taverna does not support ";
		String style = (String) (vr.getProperty("style"));
		String use = (String) (vr.getProperty("use"));
		String kind = null;
		if ((style != null) && (use != null)) {
			kind = style + "/" + use;
		}
		if (kind == null) {
			message += " the kind of message the service uses";
		} else {
			message += " the \"" + kind + "\" messages the service uses";
		}
		return new JLabel(message);
	}
	
	private static JComponent explanationBeanshellInvalidScriptComponent = new JLabel("<html>There are errors in the script of the service.<br/>When the workflow runs, any calls of the service will fail.</html>");
	private static JComponent explanationBeanshellInvalidScript(VisitReport vr) {
		return explanationBeanshellInvalidScriptComponent;
	}
	
	private static JComponent solutionBeanshellInvalidScriptComponent = new JLabel("<html>Configure the service by clicking the button below.<br/>Check that the script is valid before saving it.</html>");
	private static JComponent solutionBeanshellInvalidScript(VisitReport vr) {
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 0.9;
		solutionBeanshellInvalidScriptComponent.setMinimumSize(new Dimension(100,100));
		solutionBeanshellInvalidScriptComponent.setMaximumSize(new Dimension(100,100));
		result.add(solutionBeanshellInvalidScriptComponent, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		JButton button = new JButton();
		Processor p = (Processor) (vr.getSubject());
		button.setAction(new ReportViewConfigureAction(p));
		button.setText("Configure " + p.getLocalName());
		result.add(button, gbc);
		gbc.weightx = 0.9;
		gbc.weighty = 0.9;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		result.add(new JPanel(), gbc);
		result.setBackground(solutionBeanshellInvalidScriptComponent.getBackground());
		return result;
		
	}

}
