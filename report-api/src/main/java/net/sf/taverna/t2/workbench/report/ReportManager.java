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
//import net.sf.taverna.t2.workflowmodel.Dataflow;

public interface ReportManager {

	public void updateReport(Profile p, boolean includeTimeConsuming, boolean remember);

	public void updateObjectSetReport(Profile p, Set<WorkflowBean> objects);

	public void updateObjectReport(Profile p, WorkflowBean o);

	public Set<WorkflowBeanReport> getReports(Profile p, WorkflowBean object);

	public Map<WorkflowBean, Set<WorkflowBeanReport>> getReports(Profile p);

	public boolean isStructurallySound(Profile p);

	public Status getStatus(Profile p);

	public Status getStatus(Profile p, WorkflowBean object);

	public String getSummaryMessage(Profile p, WorkflowBean object);

	public long getLastCheckedTime(Profile p);

	public long getLastFullCheckedTime(Profile p);

	public void addObserver(Observer<ReportManagerEvent> observer);

	public List<Observer<ReportManagerEvent>> getObservers();

	public void removeObserver(Observer<ReportManagerEvent> observer);

}