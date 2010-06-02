/**
 * 
 */
package net.sf.taverna.t2.workbench.report.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPort;


/**
 * @author alanrw
 *
 */
public class ReportViewTableModel extends DefaultTableModel {
	
    public static String ALL_REPORTS = "All";
    public static String WARNINGS_AND_ERRORS = "Warnings and errors";
    public static String JUST_ERRORS = "Only errors";

	private ArrayList<VisitReport> reports;
	
	public ReportViewTableModel(Map<Object, Set<VisitReport>> reportEntries,
				String shownReports,
				HashSet<VisitReport> ignoredReports) {
		super(new String[] { "Severity", "Speed", "Type",
				"Name", "Description" }, 0);
		reports = new ArrayList();
		if (reportEntries != null) {
			for (Object o : reportEntries.keySet()) {
				for (VisitReport vr : reportEntries.get(o)) {
					if (!shownReports.equals(ReportViewComponent.ALL_INCLUDING_IGNORED) && (ignoredReports.contains(vr))){
							continue;
					}
//					if (!vr.getStatus().equals(Status.OK)) {
				    Status status = vr.getStatus();
				    if (shownReports.equals(WARNINGS_AND_ERRORS) && status.equals(Status.OK)) {
					continue;
				    }
				    if (shownReports.equals(JUST_ERRORS) && !status.equals(Status.SEVERE)) {
					continue;
				    }
						Object subject = vr.getSubject();
						this.addRow(new Object[] {
								vr.getStatus(),
								(vr.wasTimeConsuming() ? "Full" : "Quick"),
								getType(subject),
								getName(subject),
								vr.getMessage() });
						reports.add(vr);
//					}
				}
			}
		}
	}
	
	public Object getSubject(int rowIndex) {
		return reports.get(rowIndex).getSubject();
	}
	
	public VisitReport getReport(int rowIndex) {
		return reports.get(rowIndex);
	}
	
	public Class getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Status.class;
		}
		return String.class;
	}
	
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	

	
	private static String getType(Object o) {
		if (o instanceof Dataflow) {
			return "Workflow";
		}
		if (o instanceof DataflowInputPort) {
			return "Workflow input port";
		}
		if (o instanceof DataflowOutputPort) {
			return "Workflow output port";
		}
		if ((o instanceof Processor) || (o instanceof Activity)) {
			return "Service";
		}
		if ((o instanceof ProcessorInputPort) || (o instanceof ActivityInputPort)) {
			return "Service input port";
		}
		if ((o instanceof ProcessorOutputPort) || (o instanceof ActivityOutputPort)) {
			return "Service output port";
		}
		if (o instanceof Datalink) {
			return "Datalink";
		}
		if (o instanceof Condition) {
			return "Control link";
		}
		if (o instanceof Merge) {
			return "Merge";
		}
		return "?";
		}
	
	private static String getName(Object o) {
		if (o instanceof NamedWorkflowEntity) {
			return ((NamedWorkflowEntity) o).getLocalName();
		}
		if (o instanceof Port) {
			String prefix = "";
			if (o instanceof ProcessorPort) {
				prefix = ((ProcessorPort) o).getProcessor().getLocalName();
			}
			if (!(prefix.length()==0)) {
				prefix += " : ";
			}
			return (prefix + ((Port) o).getName());
		}
		if (o instanceof Activity) {
			return "?";
		}
		if (o instanceof ActivityPort) {
			return "?";
		}
		if (o instanceof Datalink) {
			return "?";
		}
		if (o instanceof Condition) {
			return "?";
		}
		if (o instanceof Merge) {
			return "?";
		}
		return "?";
		}


}
