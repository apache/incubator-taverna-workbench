/**
 *
 */
package org.apache.taverna.workbench.report.view;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import javax.swing.table.DefaultTableModel;

import org.apache.taverna.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workflowmodel.Condition;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.DataflowInputPort;
import org.apache.taverna.workflowmodel.DataflowOutputPort;
import org.apache.taverna.workflowmodel.Datalink;
import org.apache.taverna.workflowmodel.Merge;
import org.apache.taverna.workflowmodel.NamedWorkflowEntity;
import org.apache.taverna.workflowmodel.Port;
import org.apache.taverna.workflowmodel.Processor;
import org.apache.taverna.workflowmodel.ProcessorInputPort;
import org.apache.taverna.workflowmodel.ProcessorOutputPort;
import org.apache.taverna.workflowmodel.ProcessorPort;
import org.apache.taverna.workflowmodel.processor.activity.Activity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityInputPort;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;
import org.apache.taverna.workflowmodel.processor.activity.ActivityPort;


/**
 * @author alanrw
 *
 */
public class ReportViewTableModel extends DefaultTableModel {

    public static String ALL_REPORTS = "All";
    public static String WARNINGS_AND_ERRORS = "Warnings and errors";
    public static String JUST_ERRORS = "Only errors";

	private ArrayList<VisitReport> reports;

    private Dataflow dataflow;
	private final ReportManager reportManager;

    private static Comparator<VisitReport> descriptionComparator = new Comparator<VisitReport>() {
	public int compare(VisitReport o1, VisitReport o2) {
	    int result = o1.getMessage().compareTo(o2.getMessage());
	    return result;
	}
    };

    private static Comparator<VisitReport> severityComparator = new Comparator<VisitReport>() {
	public int compare(VisitReport o1, VisitReport o2) {
	    Status o1Status = o1.getStatus();
	    Status o2Status = o2.getStatus();
	    if ((o1Status.equals(Status.SEVERE)) && (o2Status.equals(Status.SEVERE))) {
		return descriptionComparator.compare(o1, o2);
	    }
	    if (o1Status.equals(Status.SEVERE)) {
		return -1;
	    }
	    if (o2Status.equals(Status.SEVERE)) {
		return 1;
	    }
	    if (o1Status.equals(Status.WARNING) && o2Status.equals(Status.WARNING)) {
		return descriptionComparator.compare(o1, o2);
	    }
	    if (o1Status.equals(Status.WARNING)) {
		return -1;
	    }
	    if (o2Status.equals(Status.WARNING)) {
		return 1;
	    }
	    return descriptionComparator.compare(o1, o2);
	}
    };

    private static Comparator<VisitReport> nameComparator = new Comparator<VisitReport>() {
	public int compare(VisitReport o1, VisitReport o2) {
	    int nameComparison = getName(o1.getSubject()).compareTo(getName(o2.getSubject()));
	    if (nameComparison == 0) {
		return severityComparator.compare(o1, o2);
	    } else {
		return nameComparison;
	    }
	}
    };

    private static Comparator<VisitReport> comparator = new Comparator<VisitReport>() {
	public int compare(VisitReport o1, VisitReport o2) {
	    Object o1Subject = o1.getSubject();
	    Object o2Subject = o2.getSubject();

	    if (o1Subject == o2Subject) {
		return severityComparator.compare(o1,o2);
	    }
	    if ((o1Subject instanceof Dataflow) && (o2.getSubject() instanceof Dataflow)) {
		return (nameComparator.compare(o1,o2));
	    }
	    if (o1Subject instanceof Dataflow) {
		return -1;
	    }
	    if (o2Subject instanceof Dataflow) {
		return 1;
	    }
	    if ((o1Subject instanceof DataflowInputPort) && (o2Subject instanceof DataflowInputPort)) {
		return (nameComparator.compare(o1,o2));
	    }
	    if (o1Subject instanceof DataflowInputPort) {
		return -1;
	    }
	    if (o2Subject instanceof DataflowInputPort) {
		return 1;
	    }
	    if ((o1Subject instanceof DataflowOutputPort) && (o2Subject instanceof DataflowOutputPort)) {
		return (nameComparator.compare(o1,o2));
	    }
	    if (o1Subject instanceof DataflowOutputPort) {
		return -1;
	    }
	    if (o2Subject instanceof DataflowOutputPort) {
		return 1;
	    }
	    if ((o1Subject instanceof Processor) && (o2Subject instanceof Processor)) {
		return (nameComparator.compare(o1,o2));
	    }
	    if (o1Subject instanceof Processor) {
		return -1;
	    }
	    if (o2Subject instanceof Processor) {
		return 1;
	    }
	    return -1;
	}
    };

    public ReportViewTableModel(Dataflow df,
				Map<Object, Set<VisitReport>> reportEntries,
				String shownReports,
				VisitReportProxySet ignoredReports, ReportManager reportManager) {
		super(new String[] { "Severity", "Age", "Type",
				"Name", "Description" }, 0);
		this.dataflow = df;
		this.reportManager = reportManager;
		reports = new ArrayList();
		if (reportEntries != null) {
			for (Object o : reportEntries.keySet()) {
				for (VisitReport vr : reportEntries.get(o)) {
					if (!shownReports.equals(ReportViewComponent.ALL_INCLUDING_IGNORED) && (ignoredReports.contains(vr))){
							continue;
					}
					Status status = vr.getStatus();
					if (shownReports.equals(WARNINGS_AND_ERRORS) && status.equals(Status.OK)) {
					    continue;
					}
					if (shownReports.equals(JUST_ERRORS) && !status.equals(Status.SEVERE)) {
					    continue;
					}
					Object subject = vr.getSubject();
					reports.add(vr);
				}
			}
		}
		Collections.sort(reports, comparator);
		for (VisitReport vr : reports) {
		    this.addRow(new Object[] {
			    vr.getStatus(),
			    calculateTimeDifference(vr),
			    getType(vr.getSubject()),
			    getName(vr.getSubject()),
			    vr.getMessage() });
		}
	}

    private String calculateTimeDifference(VisitReport vr) {
	if (!vr.wasTimeConsuming()) {
	    return "-";
	}
	long time = vr.getCheckTime();
	long dataflowTime = reportManager.getLastCheckedTime(this.dataflow);
	long difference = dataflowTime - time;
	if (difference < 1000) {
	    return "-";
	}
	difference /= 1000; // time in seconds
	long seconds = difference % 60;
	long minutes = difference / 60;

	if (minutes != 0) {
	    return (Long.toString(minutes) + "m " + Long.toString(seconds) + "s");
	} else {
	    return (Long.toString(seconds) + "s");
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
