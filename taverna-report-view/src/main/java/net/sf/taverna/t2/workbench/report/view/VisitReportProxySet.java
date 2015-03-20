/**
 * 
 */
package net.sf.taverna.t2.workbench.report.view;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;

public class VisitReportProxySet {

    private Set<VisitReportProxy> elements;

    public VisitReportProxySet() {
	elements = new HashSet<VisitReportProxy>();
    }

    public boolean add(VisitReport newElement) {
	VisitReportProxy proxy = new VisitReportProxy(newElement);
	return elements.add(proxy);
    }

    public boolean remove(VisitReport removedElement) {
	VisitReportProxy proxy = new VisitReportProxy(removedElement);
	return elements.remove(proxy);
    }

    public boolean contains(VisitReport vr) {
	VisitReportProxy proxy = new VisitReportProxy(vr);
	return elements.contains(proxy);
    }

}
