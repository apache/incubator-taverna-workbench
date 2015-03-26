/**
 *
 */
package org.apache.taverna.workbench.report.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.taverna.lang.observer.MultiCaster;
import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.visit.HierarchyTraverser;
import org.apache.taverna.visit.VisitKind;
import org.apache.taverna.visit.VisitReport;
import org.apache.taverna.visit.VisitReport.Status;
import org.apache.taverna.visit.Visitor;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.EditManager.AbstractDataflowEditEvent;
import org.apache.taverna.workbench.edits.EditManager.EditManagerEvent;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.ClosedDataflowEvent;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.report.DataflowReportEvent;
import net.sf.taverna.t2.workbench.report.FailedEntityKind;
import net.sf.taverna.t2.workbench.report.IncompleteDataflowKind;
import net.sf.taverna.t2.workbench.report.InvalidDataflowKind;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.report.ReportManagerEvent;
import net.sf.taverna.t2.workbench.report.UnresolvedOutputKind;
import net.sf.taverna.t2.workbench.report.UnsatisfiedEntityKind;
import org.apache.taverna.workbench.report.config.ReportManagerConfiguration;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.DataflowValidationReport;
import org.apache.taverna.workflowmodel.Processor;

import org.apache.log4j.Logger;

/**
 * @author Alan R Williams
 *
 */
public class ReportManagerImpl implements Observable<ReportManagerEvent>, ReportManager {

	private static final long MAX_AGE_OUTDATED_MILLIS = 1 * 60 * 60 * 1000; // 1 hour
	private static Logger logger = Logger.getLogger(ReportManagerImpl.class);

	private ReportManagerConfiguration reportManagerConfiguration;
	private HierarchyTraverser traverser = null;
	private Map<Dataflow, Map<Object, Set<VisitReport>>> reportMap = Collections
			.synchronizedMap(new WeakHashMap<Dataflow, Map<Object, Set<VisitReport>>>());;
	private Map<Dataflow, Map<Object, Status>> statusMap = Collections
			.synchronizedMap(new WeakHashMap<Dataflow, Map<Object, Status>>());
	private Map<Dataflow, Map<Object, String>> summaryMap = Collections
			.synchronizedMap(new WeakHashMap<Dataflow, Map<Object, String>>());
	private Map<Dataflow, Long> lastCheckedMap = Collections
			.synchronizedMap(new WeakHashMap<Dataflow, Long>());
	private Map<Dataflow, Long> lastFullCheckedMap = Collections
			.synchronizedMap(new WeakHashMap<Dataflow, Long>());
	private Map<Dataflow, String> lastFullCheckedDataflowIdMap = Collections
			.synchronizedMap(new WeakHashMap<Dataflow, String>());

	private EditManager editManager;
	private FileManager fileManager;

	// private Set<Visitor<?>> visitors;

	protected ReportManagerImpl(EditManager editManager, FileManager fileManager,
			Set<Visitor<?>> visitors, ReportManagerConfiguration reportManagerConfiguration)
			throws IllegalStateException {
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.reportManagerConfiguration = reportManagerConfiguration;
		// this.visitors = visitors;
		traverser = new HierarchyTraverser(visitors);
		ReportManagerFileObserver fileObserver = new ReportManagerFileObserver();
		fileManager.addObserver(fileObserver);
		addEditObserver();
		reportManagerConfiguration.applySettings();
	}

	private void addEditObserver() {
		synchronized (editManager) {
			List<Observer<EditManagerEvent>> currentObservers = editManager.getObservers();
			for (Observer<EditManagerEvent> o : currentObservers) {
				editManager.removeObserver(o);
			}
			editManager.addObserver(new ReportManagerEditObserver());
			for (Observer<EditManagerEvent> o : currentObservers) {
				editManager.addObserver(o);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.workbench.report.ReportManagerI#updateReport(net.sf.taverna.t2.workflowmodel
	 * .Dataflow, boolean, boolean)
	 */
	@Override
	public void updateReport(Dataflow d, boolean includeTimeConsuming, boolean remember) {
		Set<VisitReport> oldTimeConsumingReports = null;
		long time = System.currentTimeMillis();
		long expiration = Integer.parseInt(reportManagerConfiguration
				.getProperty(ReportManagerConfiguration.REPORT_EXPIRATION)) * 60 * 1000;
		if (remember && !includeTimeConsuming
				&& ((expiration == 0) || ((time - getLastFullCheckedTime(d)) < expiration))) {
			oldTimeConsumingReports = getTimeConsumingReports(d);
		}
		Map<Object, Set<VisitReport>> reportsEntry = new HashMap<Object, Set<VisitReport>>();
		Map<Object, Status> statusEntry = new HashMap<Object, Status>();
		Map<Object, String> summaryEntry = new HashMap<Object, String>();
		reportMap.put(d, reportsEntry);
		statusMap.put(d, statusEntry);
		summaryMap.put(d, summaryEntry);
		validateDataflow(d, reportsEntry, statusEntry, summaryEntry);

		Set<VisitReport> newReports = new HashSet<VisitReport>();
		traverser.traverse(d, new ArrayList<Object>(), newReports, includeTimeConsuming);
		for (VisitReport vr : newReports) {
			addReport(reportsEntry, statusEntry, summaryEntry, vr);
		}
		if (oldTimeConsumingReports != null) {
			for (VisitReport vr : oldTimeConsumingReports) {
				addReport(reportsEntry, statusEntry, summaryEntry, vr);
			}
		}
		time = System.currentTimeMillis();
		lastCheckedMap.put(d, time);
		if (includeTimeConsuming) {
			lastFullCheckedMap.put(d, time);
			lastFullCheckedDataflowIdMap.put(d, d.getIdentifier());
		}
		multiCaster.notify(new DataflowReportEvent(d));
	}

	private void updateObjectReportInternal(Dataflow d, Object o) {
		Map<Object, Set<VisitReport>> reportsEntry = reportMap.get(d);
		Map<Object, Status> statusEntry = statusMap.get(d);
		Map<Object, String> summaryEntry = summaryMap.get(d);
		if (reportsEntry == null) {
			logger.error("Attempt to update reports on an object in a dataflow that has not been checked");
			reportsEntry = new HashMap<Object, Set<VisitReport>>();
			statusEntry = new HashMap<Object, Status>();
			summaryEntry = new HashMap<Object, String>();
			reportMap.put(d, reportsEntry);
			statusMap.put(d, statusEntry);
			summaryMap.put(d, summaryEntry);
		} else {
			reportsEntry.remove(o);
			statusEntry.remove(o);
			summaryEntry.remove(o);
		}
		// Assume o is directly inside d
		List<Object> ancestry = new ArrayList<Object>();
		ancestry.add(d);
		Set<VisitReport> newReports = new HashSet<VisitReport>();
		traverser.traverse(o, ancestry, newReports, true);
		for (VisitReport vr : newReports) {
			addReport(reportsEntry, statusEntry, summaryEntry, vr);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.workbench.report.ReportManagerI#updateObjectSetReport(net.sf.taverna.t2
	 * .workflowmodel.Dataflow, java.util.Set)
	 */
	@Override
	public void updateObjectSetReport(Dataflow d, Set<Object> objects) {
		for (Object o : objects) {
			updateObjectReportInternal(d, o);
		}
		multiCaster.notify(new DataflowReportEvent(d));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.sf.taverna.t2.workbench.report.ReportManagerI#updateObjectReport(net.sf.taverna.t2.
	 * workflowmodel.Dataflow, java.lang.Object)
	 */
	@Override
	public void updateObjectReport(Dataflow d, Object o) {
		updateObjectReportInternal(d, o);
		multiCaster.notify(new DataflowReportEvent(d));
	}

	private Set<VisitReport> getTimeConsumingReports(Dataflow d) {
		Set<VisitReport> result = new HashSet<VisitReport>();
		Map<Object, Set<VisitReport>> currentReports = getReports(d);
		if (currentReports != null) {
			for (Object o : currentReports.keySet()) {
				for (VisitReport vr : currentReports.get(o)) {
					if (vr.wasTimeConsuming()) {
						result.add(vr);
					}
				}
			}
		}
		return result;
	}

	private void removeReport(Dataflow d) {
		reportMap.remove(d);
		statusMap.remove(d);
		summaryMap.remove(d);
	}

	private void addReport(Map<Object, Set<VisitReport>> reports, Map<Object, Status> statusEntry,
			Map<Object, String> summaryEntry, VisitReport newReport) {
		if (newReport.getCheckTime() == 0) {
			newReport.setCheckTime(System.currentTimeMillis());
		}
		Object subject = newReport.getSubject();
		Set<VisitReport> currentReports = reports.get(subject);
		Status newReportStatus = newReport.getStatus();
		if (currentReports == null) {
			currentReports = new HashSet<VisitReport>();
			reports.put(subject, currentReports);
			statusEntry.put(subject, newReportStatus);
			summaryEntry.put(subject, newReport.getMessage());
		} else {
			Status currentStatus = statusEntry.get(subject);
			if (currentStatus.compareTo(newReportStatus) < 0) {
				statusEntry.put(subject, newReportStatus);
				summaryEntry.put(subject, newReport.getMessage());
			} else if (currentStatus.compareTo(newReportStatus) == 0) {
				if (currentStatus.equals(Status.WARNING)) {
					summaryEntry.put(subject, "Multiple warnings");
				} else if (currentStatus.equals(Status.SEVERE)) {
					summaryEntry.put(subject, "Multiple errors");
				}
			}
		}
		currentReports.add(newReport);
	}

	private void validateDataflow(Dataflow d, Map<Object, Set<VisitReport>> reportsEntry,
			Map<Object, Status> statusEntry, Map<Object, String> summaryEntry) {
		DataflowValidationReport validationReport = d.checkValidity();
		if (validationReport.isWorkflowIncomplete()) {
			addReport(reportsEntry, statusEntry, summaryEntry, new VisitReport(
					IncompleteDataflowKind.getInstance(), d, "Incomplete workflow",
					IncompleteDataflowKind.INCOMPLETE_DATAFLOW, VisitReport.Status.SEVERE));
		} else if (!validationReport.isValid()) {
			addReport(reportsEntry, statusEntry, summaryEntry,
					new VisitReport(InvalidDataflowKind.getInstance(), d, "Invalid workflow",
							InvalidDataflowKind.INVALID_DATAFLOW, VisitReport.Status.SEVERE));
		}
		fillInReport(reportsEntry, statusEntry, summaryEntry, validationReport);
	}

	private void fillInReport(Map<Object, Set<VisitReport>> reportsEntry,
			Map<Object, Status> statusEntry, Map<Object, String> summaryEntry,
			DataflowValidationReport report) {
		for (Object o : report.getUnresolvedOutputs()) {
			addReport(reportsEntry, statusEntry, summaryEntry,
					new VisitReport(UnresolvedOutputKind.getInstance(), o,
							"Invalid workflow output", UnresolvedOutputKind.OUTPUT,
							VisitReport.Status.SEVERE));
		}
		for (Object o : report.getFailedEntities()) {
			addReport(reportsEntry, statusEntry, summaryEntry,
					new VisitReport(FailedEntityKind.getInstance(), o,
							"Mismatch of input list depths", FailedEntityKind.FAILED_ENTITY,
							VisitReport.Status.SEVERE));
		}
		for (Object o : report.getUnsatisfiedEntities()) {
			addReport(reportsEntry, statusEntry, summaryEntry, new VisitReport(
					UnsatisfiedEntityKind.getInstance(), o, "Unknown prior list depth",
					UnsatisfiedEntityKind.UNSATISFIED_ENTITY, VisitReport.Status.SEVERE));
		}
		// for (DataflowValidationReport subReport : report.getInvalidDataflows().values()) {
		// fillInReport(descriptionMap, subReport);
		// }
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.workbench.report.ReportManagerI#getReports(net.sf.taverna.t2.workflowmodel
	 * .Dataflow, java.lang.Object)
	 */
	@Override
	public Set<VisitReport> getReports(Dataflow d, Object object) {
		Set<VisitReport> result = new HashSet<VisitReport>();
		Map<Object, Set<VisitReport>> objectReports = reportMap.get(d);
		if (objectReports != null) {
			Set<Object> objects = new HashSet<Object>();
			objects.add(object);
			if (object instanceof Processor) {
				objects.addAll(((Processor) object).getActivityList());
			}
			for (Object o : objects) {
				if (objectReports.containsKey(o)) {
					result.addAll(objectReports.get(o));
				}
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.workbench.report.ReportManagerI#getReports(net.sf.taverna.t2.workflowmodel
	 * .Dataflow)
	 */
	@Override
	public Map<Object, Set<VisitReport>> getReports(Dataflow d) {
		return reportMap.get(d);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.sf.taverna.t2.workbench.report.ReportManagerI#isStructurallySound(net.sf.taverna.t2.
	 * workflowmodel.Dataflow)
	 */
	@Override
	public boolean isStructurallySound(Dataflow d) {
		Map<Object, Set<VisitReport>> objectReports = reportMap.get(d);
		if (objectReports == null) {
			return false;
		}
		for (Set<VisitReport> visitReportSet : objectReports.values()) {
			for (VisitReport vr : visitReportSet) {
				if (vr.getStatus().equals(Status.SEVERE)) {
					VisitKind vk = vr.getKind();
					if ((vk instanceof IncompleteDataflowKind)
							|| (vk instanceof InvalidDataflowKind)
							|| (vk instanceof UnresolvedOutputKind)
							|| (vk instanceof FailedEntityKind)
							|| (vk instanceof UnsatisfiedEntityKind)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.workbench.report.ReportManagerI#getStatus(net.sf.taverna.t2.workflowmodel
	 * .Dataflow)
	 */
	@Override
	public Status getStatus(Dataflow d) {
		Map<Object, Set<VisitReport>> objectReports = reportMap.get(d);
		if (objectReports == null) {
			return Status.OK;
		}
		Status currentStatus = Status.OK;
		for (Set<VisitReport> visitReportSet : objectReports.values()) {
			for (VisitReport vr : visitReportSet) {
				Status status = vr.getStatus();
				if (status.compareTo(currentStatus) > 0) {
					currentStatus = status;
				}
				if (currentStatus.equals(Status.SEVERE)) {
					return currentStatus;
				}
			}
		}
		return currentStatus;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.workbench.report.ReportManagerI#getStatus(net.sf.taverna.t2.workflowmodel
	 * .Dataflow, java.lang.Object)
	 */
	@Override
	public Status getStatus(Dataflow d, Object object) {
		Status result = Status.OK;
		Map<Object, Status> statusEntry = statusMap.get(d);
		if (statusEntry != null) {
			Status value = statusEntry.get(object);
			if (value != null) {
				result = value;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.sf.taverna.t2.workbench.report.ReportManagerI#getSummaryMessage(net.sf.taverna.t2.
	 * workflowmodel.Dataflow, java.lang.Object)
	 */
	@Override
	public String getSummaryMessage(Dataflow d, Object object) {
		String result = null;
		if (!getStatus(d, object).equals(Status.OK)) {
			Map<Object, String> summaryEntry = summaryMap.get(d);
			if (summaryEntry != null) {
				result = summaryEntry.get(object);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.sf.taverna.t2.workbench.report.ReportManagerI#getLastCheckedTime(net.sf.taverna.t2.
	 * workflowmodel.Dataflow)
	 */
	@Override
	public long getLastCheckedTime(Dataflow d) {
		Long l = lastCheckedMap.get(d);
		if (l == null) {
			return 0;
		} else {
			return l.longValue();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.workbench.report.ReportManagerI#getLastFullCheckedTime(net.sf.taverna.t2
	 * .workflowmodel.Dataflow)
	 */
	@Override
	public long getLastFullCheckedTime(Dataflow d) {
		Long l = lastFullCheckedMap.get(d);
		if (l == null) {
			return 0;
		} else {
			return l.longValue();
		}
	}

	/**
	 * @author alanrw
	 *
	 */
	public class ReportManagerFileObserver implements Observer<FileManagerEvent> {

		public void notify(Observable<FileManagerEvent> sender, FileManagerEvent message)
				throws Exception {
			String onOpen = reportManagerConfiguration.getProperty(
					ReportManagerConfiguration.ON_OPEN);
			if (message instanceof ClosedDataflowEvent) {
				ReportManagerImpl.this.removeReport(((ClosedDataflowEvent) message).getDataflow());
			} else if (message instanceof SetCurrentDataflowEvent) {
				Dataflow dataflow = ((SetCurrentDataflowEvent) message).getDataflow();
				if (!reportMap.containsKey(dataflow)) {
					if (!onOpen.equals(ReportManagerConfiguration.NO_CHECK)) {
						updateReport(dataflow,
								onOpen.equals(ReportManagerConfiguration.FULL_CHECK), true);
					} else {
						ReportManagerImpl.this.multiCaster
								.notify(new DataflowReportEvent(dataflow));
					}
				} else {
					ReportManagerImpl.this.multiCaster.notify(new DataflowReportEvent(dataflow));
				}
			}
		}

	}

	private MultiCaster<ReportManagerEvent> multiCaster = new MultiCaster<ReportManagerEvent>(this);

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.workbench.report.ReportManagerI#addObserver(net.sf.taverna.t2.lang.observer
	 * .Observer)
	 */
	@Override
	public void addObserver(Observer<ReportManagerEvent> observer) {
		multiCaster.addObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.sf.taverna.t2.workbench.report.ReportManagerI#getObservers()
	 */
	@Override
	public List<Observer<ReportManagerEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.workbench.report.ReportManagerI#removeObserver(net.sf.taverna.t2.lang.observer
	 * .Observer)
	 */
	@Override
	public void removeObserver(Observer<ReportManagerEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.sf.taverna.t2.workbench.report.ReportManagerI#isReportOutdated(net.sf.taverna.t2.
	 * workflowmodel.Dataflow)
	 */
	@Override
	public boolean isReportOutdated(Dataflow dataflow) {
		String lastCheckedId = lastFullCheckedDataflowIdMap.get(dataflow);
		Long lastCheck = lastFullCheckedMap.get(dataflow);
		if (lastCheckedId == null || lastCheck == null) {
			// Unknown, so outdated
			return true;
		}
		if (!lastCheckedId.equals(dataflow.getIdentifier())) {
			// Workflow changed, so outdaeted
			return true;
		}
		long now = System.currentTimeMillis();
		long age = now - lastCheck;
		// Outdated if it is older than the maximum
		return age > MAX_AGE_OUTDATED_MILLIS;
	}

	public class ReportManagerEditObserver implements Observer<EditManagerEvent> {
		public void notify(Observable<EditManagerEvent> sender, EditManagerEvent message)
				throws Exception {
			String onEdit = reportManagerConfiguration
					.getProperty(ReportManagerConfiguration.ON_EDIT);
			Dataflow dataflow = fileManager.getCurrentDataflow();
			if (message instanceof AbstractDataflowEditEvent) {
				AbstractDataflowEditEvent adee = (AbstractDataflowEditEvent) message;
				if (adee.getDataFlow().equals(dataflow)) {
					if (onEdit.equals(ReportManagerConfiguration.QUICK_CHECK)) {
						updateReport(dataflow, false, true);
					} else if (onEdit.equals(ReportManagerConfiguration.FULL_CHECK)) {
						updateReport(dataflow, true, true);
					}
				}
			}
		}
	}

}
