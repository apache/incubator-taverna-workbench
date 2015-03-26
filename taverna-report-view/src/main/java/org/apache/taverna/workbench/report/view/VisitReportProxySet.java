/**
 * 
 */
package org.apache.taverna.workbench.report.view;

import java.util.HashSet;
import java.util.Set;

import org.apache.taverna.visit.VisitReport;

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
