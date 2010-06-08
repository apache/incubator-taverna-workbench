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

    class VisitReportProxy {

	Status status;
	int subjectHashCode;
	VisitKind kind;
	String message;

	VisitReportProxy(VisitReport vr) {
	    this.status = vr.getStatus();
	    this.subjectHashCode = vr.getSubject().hashCode();
	    this.kind = vr.getKind();
	    this.message = vr.getMessage();
	}

	public boolean equals(Object o) {
	    if ((o == null) || !(o instanceof VisitReportProxy)) {
		return false;
	    }
	    VisitReportProxy vrp = (VisitReportProxy) o;
	    return (vrp.status.equals(this.status) &&
		    (vrp.subjectHashCode == this.subjectHashCode) &&
		    (vrp.kind.equals(this.kind)) &&
		    (vrp.message.equals(this.message)));
	}

	public int hashCode() {
	    return ((status.hashCode() >> 2) +
		    (subjectHashCode >> 2) +
		    (kind.hashCode() >> 2) +
		    (message.hashCode() >> 2));
	}
    }

}
