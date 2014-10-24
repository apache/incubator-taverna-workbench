package net.sf.taverna.t2.workbench.report;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.org.taverna.scufl2.api.common.WorkflowBean;
import uk.org.taverna.scufl2.api.profiles.Profile;
import uk.org.taverna.scufl2.validation.WorkflowBeanReport;
import uk.org.taverna.scufl2.validation.Status;

import net.sf.taverna.t2.lang.observer.Observer;
//import net.sf.taverna.t2.visit.VisitReport;
//import net.sf.taverna.t2.visit.VisitReport.Status;

public interface ReportManager {
	void updateReport(Profile p, boolean includeTimeConsuming, boolean remember);

	void updateObjectSetReport(Profile p, Set<WorkflowBean> objects);

	void updateObjectReport(Profile p, WorkflowBean o);

	Set<WorkflowBeanReport> getReports(Profile p, WorkflowBean object);

	Map<WorkflowBean, Set<WorkflowBeanReport>> getReports(Profile p);

	boolean isStructurallySound(Profile p);

	Status getStatus(Profile p);

	Status getStatus(Profile p, WorkflowBean object);

	String getSummaryMessage(Profile p, WorkflowBean object);

	long getLastCheckedTime(Profile p);

	long getLastFullCheckedTime(Profile p);

	void addObserver(Observer<ReportManagerEvent> observer);

	List<Observer<ReportManagerEvent>> getObservers();

	void removeObserver(Observer<ReportManagerEvent> observer);
}
