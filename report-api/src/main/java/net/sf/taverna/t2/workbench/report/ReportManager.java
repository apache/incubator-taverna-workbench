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
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;

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
						updateReport(dataflow, false);
					} else if (onEdit.equals(ReportManagerConfiguration.FULL_CHECK)) {
						updateReport(dataflow, true);
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
	
	public static synchronized void updateReport(Dataflow d, boolean includeTimeConsuming) {
		Set<VisitReport> oldTimeConsumingReports = null;
		if (!includeTimeConsuming) {
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
		if (!includeTimeConsuming) {
			for (VisitReport vr : oldTimeConsumingReports) {
				addReport(reportsEntry, statusEntry, summaryEntry, vr);
			}
		}
		getInstance().multiCaster.notify(new DataflowReportEvent(d));
		if (includeTimeConsuming) {
			checkDisabledActivities(d, reportsEntry);
		}
	}
	
	private static void checkDisabledActivities(
			Dataflow d, Map<Object, Set<VisitReport>> reportsEntry) {
		EditManager editManager = EditManager.getInstance();
		Edits edits = editManager.getEdits();
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		for (Object o : reportsEntry.keySet()) {
			if (o instanceof Processor) {
				Set<VisitReport> reports = reportsEntry.get(o);
				boolean isAlreadyDisabled = false;
				Processor processor = (Processor) o;
				DisabledActivity disabledActivity = null;
				List<? extends Activity<?>> activityList = processor.getActivityList();
				for (Activity a : activityList) {
					if (a instanceof DisabledActivity) {
						isAlreadyDisabled = true;
						disabledActivity = (DisabledActivity) a;
						break;
					}
				}
				if (isAlreadyDisabled) {
					Set<VisitReport> nonDisabledReports = new HashSet<VisitReport>();
					for (VisitReport vr : reports) {
						if (vr.getResultId() != HealthCheck.DISABLED) {
							nonDisabledReports.add(vr);
						}
					}
					Status remainingStatus = VisitReport.getWorstStatus(nonDisabledReports);
					if (!remainingStatus.equals(Status.SEVERE)) {
						logger.info(processor.getLocalName() + " is no longer disabled");
						Activity replacementActivity = disabledActivity.getActivity();
						try {
							replacementActivity.configure(disabledActivity.getActivityConfiguration());
							editList.add(edits.getRemoveActivityEdit(processor, disabledActivity));
							replacementActivity.getInputPortMapping().clear();
							replacementActivity.getInputPortMapping().putAll(disabledActivity.getInputPortMapping());
							replacementActivity.getOutputPortMapping().clear();
							replacementActivity.getOutputPortMapping().putAll(disabledActivity.getOutputPortMapping());
							editList.add(edits.getAddActivityEdit(processor, replacementActivity));
						}
						catch (ActivityConfigurationException ex) {
							// ok
						}
						
					}
				}
//				else {
//					boolean nowDisabled = false;
//					for (VisitReport vr : reports) {
//						if (vr.getKind() instanceof HealthCheck) {
//							int resultId = vr.getResultId();
//							if ((resultId == HealthCheck.CONNECTION_PROBLEM) ||
//									(resultId == HealthCheck.IO_PROBLEM) ||
//									(resultId == HealthCheck.INVALID_URL) ||
//									(resultId == HealthCheck.TIME_OUT)) {
//								nowDisabled = true;
//								break;
//							}
//						}
//					}
//					if (nowDisabled) {
//						logger.info(processor.getLocalName() + " is now disabled");
//						Activity replacedActivity = processor.getActivityList().get(0);
//						Activity replacementActivity = new DisabledActivity(replacedActivity);
//							editList.add(edits.getRemoveActivityEdit(processor, replacedActivity));
//							editList.add(edits.getAddActivityEdit(processor, replacementActivity));											}
//				}
			}
		}
		if (!editList.isEmpty()) {
			CompoundEdit ce = new CompoundEdit(editList);
			try {
				editManager.doDataflowEdit(d, ce);
			} catch (EditException e) {
				logger.error(e);
			}
		}
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
			addReport(reportsEntry, statusEntry, summaryEntry, new VisitReport(InvalidDataflowKind.getInstance(), d, "Invalid dataflow", InvalidDataflowKind.INVALID_DATAFLOW, VisitReport.Status.SEVERE));
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
						updateReport(dataflow, onOpen.equals(ReportManagerConfiguration.FULL_CHECK));
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
