package net.sf.taverna.t2.workbench.report.view;

import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;

public class VisitReportProxy {

	Status status;
	int subjectHashCode;
	VisitKind kind;
	String message;

	public VisitReportProxy(VisitReport vr) {
	    this.status = vr.getStatus();
	    this.subjectHashCode = vr.getSubject().hashCode();
	    this.kind = vr.getKind();
	    this.message = vr.getMessage();
	}

	public boolean equals(Object o) {
	    if ((o == null) || !((o instanceof VisitReportProxy) || (o instanceof VisitReport))) {
		return false;
	    }
	    if (o instanceof VisitReport) {
		return this.equals(new VisitReportProxy((VisitReport) o));
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

