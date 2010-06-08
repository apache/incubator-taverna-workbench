/**
 * 
 */
package net.sf.taverna.t2.workbench.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.visit.HierarchyTraverser;
import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowUndoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.OpenedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.report.config.ReportManagerConfiguration;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;

import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * @author alanrw
 *
 */
public class ReportManager implements Observable<ReportManagerEvent> {
	
	private static Logger logger = Logger
	.getLogger(ReportManager.class);
	

	public static class ReportManagerEditObserver implements Observer<EditManagerEvent> {

		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			String onEdit = ReportManagerConfiguration.getInstance().getProperty(ReportManagerConfiguration.ON_EDIT);
			Dataflow dataflow = FileManager.getInstance().getCurrentDataflow();
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

	private static ReportManager instance = null;
	
	private static List<VisitKind> visitorDescriptions = null;
	
	private static Map<Dataflow, Map<Object, Set<VisitReport>>> reportMap = null;
	
	private static HierarchyTraverser traverser = null;

	private static Map<Dataflow, Map<Object, Status>> statusMap;
	private static Map<Dataflow, Map<Object, String>> summaryMap;

	private static Map<Dataflow, Long> lastCheckedMap;
	private static Map<Dataflow, Long> lastFullCheckedMap;

	private ReportManager() {
		
	}

	public static synchronized ReportManager getInstance()
			throws IllegalStateException {
		if (instance == null) {
//			SPIRegistry<ReportManager> registry = new SPIRegistry<ReportManager>(
//					ReportManager.class);
			try {
//				instance = registry.getInstances().get(0);
				instance = new ReportManager();
				SPIRegistry<VisitKind> visitorDescriptionRegistry = new SPIRegistry<VisitKind>(VisitKind.class);
				visitorDescriptions = visitorDescriptionRegistry.getInstances();
				traverser = new HierarchyTraverser(visitorDescriptions);
				
				ReportManagerFileObserver fileObserver = new ReportManagerFileObserver();
				FileManager.getInstance().addObserver(fileObserver);
				addEditObserver();

				reportMap = new WeakHashMap<Dataflow, Map<Object, Set<VisitReport>>> ();
				statusMap = new WeakHashMap<Dataflow, Map<Object, Status>>();
				summaryMap = new WeakHashMap<Dataflow, Map<Object, String>>();
				lastCheckedMap = new WeakHashMap<Dataflow, Long>();
				lastFullCheckedMap = new WeakHashMap<Dataflow, Long>();
				
				ReportManagerConfiguration.getInstance().applySettings();
			} catch (IndexOutOfBoundsException ex) {
				throw new IllegalStateException(
						"Could not find implementation of "
								+ ReportManager.class);
			}
		}
		return instance;
	}
	
	private static void addEditObserver() {
		EditManager editManager = EditManager.getInstance();
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
	
	public static synchronized void updateReport(Dataflow d, boolean includeTimeConsuming, boolean remember) {
		Set<VisitReport> oldTimeConsumingReports = null;
		long time = System.currentTimeMillis();
		long expiration = Integer.parseInt(ReportManagerConfiguration.getInstance().getProperty(ReportManagerConfiguration.REPORT_EXPIRATION)) * 60 * 1000;
		if (remember && !includeTimeConsuming && ((expiration == 0) || ((time - getLastFullCheckedTime(d)) < expiration))) {
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
		}
		getInstance().multiCaster.notify(new DataflowReportEvent(d));
	}
	
	private static synchronized void updateObjectReportInternal(Dataflow d, Object o) {
		Map<Object, Set<VisitReport>> reportsEntry = reportMap.get(d);
		Map<Object, Status> statusEntry = statusMap.get(d);
		Map<Object, String> summaryEntry = summaryMap.get(d);
		if (reportsEntry == null) {
		    logger.error("Attempt to update reports on an object in a dataflow that has not been checked");
		}
		reportsEntry.remove(o);
		statusEntry.remove(o);
		summaryEntry.remove(o);

		// Assume o is directly inside d
		List<Object> ancestry = new ArrayList<Object>();
		ancestry.add(d);
		Set<VisitReport> newReports = new HashSet<VisitReport>();
		traverser.traverse(o, ancestry, newReports, true);
		for (VisitReport vr : newReports) {
			addReport(reportsEntry, statusEntry, summaryEntry, vr);
		}
		long time = System.currentTimeMillis();
		lastCheckedMap.put(d, time);
		lastFullCheckedMap.put(d, time);
	    }

	public static synchronized void updateObjectSetReport(Dataflow d, Set<Object> objects) {
		for (Object o : objects) {
		    updateObjectReportInternal(d, o);
		}
		getInstance().multiCaster.notify(new DataflowReportEvent(d));
	    }

	public static synchronized void updateObjectReport(Dataflow d, Object o) {
		updateObjectReportInternal(d, o);
		getInstance().multiCaster.notify(new DataflowReportEvent(d));
	    }

	private static Set<VisitReport> getTimeConsumingReports(Dataflow d) {
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
	
	private synchronized void removeReport(Dataflow d) {
		reportMap.remove(d);
		statusMap.remove(d);
		summaryMap.remove(d);
	}
	
	private static synchronized void addReport(Map<Object, Set<VisitReport>> reports, Map<Object, Status> statusEntry, Map<Object, String> summaryEntry, VisitReport newReport) {
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
			}
			else if (currentStatus.compareTo(newReportStatus) == 0) {
				if (currentStatus.equals(Status.WARNING)) {
					summaryEntry.put(subject, "Multiple warnings");
				} else if (currentStatus.equals(Status.SEVERE)) {
					summaryEntry.put(subject, "Multiple errors");
				}
			}
		}
		currentReports.add(newReport);
	}
	
	private static synchronized void validateDataflow(Dataflow d, Map<Object, Set<VisitReport>> reportsEntry, Map<Object, Status> statusEntry, Map<Object, String> summaryEntry) {
		DataflowValidationReport validationReport = d.checkValidity();
		if (validationReport.isWorkflowIncomplete()) {
			addReport(reportsEntry, statusEntry, summaryEntry, new VisitReport(IncompleteDataflowKind.getInstance(), d, "Incomplete dataflow", IncompleteDataflowKind.INCOMPLETE_DATAFLOW, VisitReport.Status.SEVERE));
		}
		else if (!validationReport.isValid()) {
			addReport(reportsEntry, statusEntry, summaryEntry, new VisitReport(InvalidDataflowKind.getInstance(), d, "Invalid workflow", InvalidDataflowKind.INVALID_DATAFLOW, VisitReport.Status.SEVERE));
		}
		fillInReport(reportsEntry, statusEntry, summaryEntry, validationReport);
	}

	private static synchronized void fillInReport(
			Map<Object, Set<VisitReport>> reportsEntry,
			Map<Object, Status> statusEntry,
			Map<Object, String> summaryEntry, DataflowValidationReport report) {
		for (Object o : report.getUnresolvedOutputs()) {
			addReport(reportsEntry, statusEntry, summaryEntry, new VisitReport(UnresolvedOutputKind.getInstance(), o, "Unresolved output", UnresolvedOutputKind.OUTPUT, VisitReport.Status.SEVERE));
		}
		for (Object o : report.getFailedEntities()) {
			addReport(reportsEntry, statusEntry, summaryEntry, new VisitReport(FailedEntityKind.getInstance(), o, "Failed entity", FailedEntityKind.FAILED_ENTITY, VisitReport.Status.SEVERE));
		}
		for (Object o : report.getUnsatisfiedEntities()) {
			addReport(reportsEntry, statusEntry, summaryEntry, new VisitReport(UnsatisfiedEntityKind.getInstance(), o, "Unsatisfied", UnsatisfiedEntityKind.UNSATISFIED_ENTITY, VisitReport.Status.SEVERE));
		}
//		for (DataflowValidationReport subReport : report.getInvalidDataflows().values()) {
//			fillInReport(descriptionMap, subReport);
//		}
	}
	
	public synchronized Set<VisitReport> getReports(Dataflow d, Object object) {
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
	
	public static synchronized Map<Object, Set<VisitReport>> getReports(Dataflow d) {
		return reportMap.get(d);
	}
	
	public static boolean isStructurallySound(Dataflow d) {
		Map<Object, Set<VisitReport>> objectReports = reportMap.get(d);
		if (objectReports == null) {
			return false;
		}
		for (Set<VisitReport> visitReportSet : objectReports.values()) {
			for (VisitReport vr : visitReportSet) {
				if (vr.getStatus().equals(Status.SEVERE)) {
					VisitKind vk = vr.getKind();
					if ((vk instanceof IncompleteDataflowKind) ||
							(vk instanceof InvalidDataflowKind) ||
							(vk instanceof UnresolvedOutputKind) ||
							(vk instanceof FailedEntityKind) ||
							(vk instanceof UnsatisfiedEntityKind)) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static Status getStatus(Dataflow d) {
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
	
	public synchronized Status getStatus(Dataflow d, Object object) {
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
	
	public synchronized String getSummaryMessage(Dataflow d, Object object) {
		String result = null;
		if (!getStatus(d, object).equals(Status.OK)) {
			Map<Object, String> summaryEntry = summaryMap.get(d);
			if (summaryEntry != null) {
				result = summaryEntry.get(object);
			}
		}
		return result;
	}
	
	public static synchronized long getLastCheckedTime(Dataflow d) {
		Long l = lastCheckedMap.get(d);
		if (l == null) {
		    return 0;
		} else {
		    return l.longValue();
		}
	    }

	public static synchronized long getLastFullCheckedTime(Dataflow d) {
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
	public static class ReportManagerFileObserver implements Observer<FileManagerEvent> {

		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			String onOpen = ReportManagerConfiguration.getInstance().getProperty(ReportManagerConfiguration.ON_OPEN);
			if (message instanceof ClosedDataflowEvent) {
				ReportManager.getInstance().removeReport(((ClosedDataflowEvent) message).getDataflow());
			} else if (message instanceof SetCurrentDataflowEvent) {
				Dataflow dataflow = ((SetCurrentDataflowEvent) message)
				.getDataflow();
				if (!reportMap.containsKey(dataflow)) {
					if (!onOpen.equals(ReportManagerConfiguration.NO_CHECK)) {
						updateReport(dataflow, onOpen.equals(ReportManagerConfiguration.FULL_CHECK), true);
					} else {
						getInstance().multiCaster.notify(new DataflowReportEvent(dataflow));
					}
				} else {
					getInstance().multiCaster.notify(new DataflowReportEvent(dataflow));
				}				
			}
		}

	}

	private MultiCaster<ReportManagerEvent> multiCaster = new MultiCaster<ReportManagerEvent>(
			this);
	
	public void addObserver(Observer<ReportManagerEvent> observer) {
		multiCaster.addObserver(observer);
	}

	public List<Observer<ReportManagerEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	public void removeObserver(Observer<ReportManagerEvent> observer) {
		multiCaster.removeObserver(observer);
	}
	
}
