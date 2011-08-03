package net.sf.taverna.t2.workbench.report;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public interface ReportManager {

	public void updateReport(Dataflow d, boolean includeTimeConsuming, boolean remember);

	public void updateObjectSetReport(Dataflow d, Set<Object> objects);

	public void updateObjectReport(Dataflow d, Object o);

	public Set<VisitReport> getReports(Dataflow d, Object object);

	public Map<Object, Set<VisitReport>> getReports(Dataflow d);

	public boolean isStructurallySound(Dataflow d);

	public Status getStatus(Dataflow d);

	public Status getStatus(Dataflow d, Object object);

	public String getSummaryMessage(Dataflow d, Object object);

	public long getLastCheckedTime(Dataflow d);

	public long getLastFullCheckedTime(Dataflow d);

	public void addObserver(Observer<ReportManagerEvent> observer);

	public List<Observer<ReportManagerEvent>> getObservers();

	public void removeObserver(Observer<ReportManagerEvent> observer);

	public boolean isReportOutdated(Dataflow dataflow);

}