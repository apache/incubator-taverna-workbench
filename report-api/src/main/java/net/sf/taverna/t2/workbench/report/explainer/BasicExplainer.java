/**
 * 
 */
package net.sf.taverna.t2.workbench.report.explainer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

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
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_SCRIPT)) {
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
		return null;
	}

	public JComponent getSolution(VisitReport vr) {
		VisitKind vk = vr.getKind();
		int resultId = vr.getResultId();
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_SCRIPT)) {
			return solutionBeanshellInvalidScript(vr);
		}
		return null;
	}
	
	private static JComponent explanationDataflowIncompleteComponent = new ReadOnlyTextArea("A workflow must contain at least one service or at least one output port");
	private static JComponent explanationDataflowIncomplete(VisitReport vr) {
		return explanationDataflowIncompleteComponent;
	}
	
	private static JComponent explanationBeanshellInvalidScriptComponent = new ReadOnlyTextArea("There are errors in the script of the service.  When the workflow runs, any calls of the service will fail.");
	private static JComponent explanationBeanshellInvalidScript(VisitReport vr) {
		return explanationBeanshellInvalidScriptComponent;
	}
	
	private static JComponent solutionBeanshellInvalidScriptComponent = new ReadOnlyTextArea("Configure the service by clicking the button below.  Check that the script is valid before saving it.");
	private static JComponent solutionBeanshellInvalidScript(VisitReport vr) {
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 0.9;
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
