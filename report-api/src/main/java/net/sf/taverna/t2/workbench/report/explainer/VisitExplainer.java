/**
 * 
 */
package net.sf.taverna.t2.workbench.report.explainer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;

/**
 * @author alanrw
 *
 */
public interface VisitExplainer {

		boolean canExplain(VisitKind vk, int resultId);
		
		JComponent getExplanation(VisitReport vr);
		
		JComponent getSolution(VisitReport vr);
}
