/**
 * 
 */
package net.sf.taverna.t2.workbench.report.explainer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;

import net.sf.taverna.t2.activities.disabled.actions.DisabledActivityConfigurationAction;
import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.actions.EditNestedDataflowAction;
import net.sf.taverna.t2.activities.wsdl.actions.AddXMLInputSplitterAction;
import net.sf.taverna.t2.activities.wsdl.InputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.AddXMLSplitterEdit;
import net.sf.taverna.t2.visit.DataflowCollation;
import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.fragility.FragilityCheck;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowOutputAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDatalinkAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.FailedEntityKind;
import net.sf.taverna.t2.workbench.report.IncompleteDataflowKind;
import net.sf.taverna.t2.workbench.report.InvalidDataflowKind;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.report.UnresolvedOutputKind;
import net.sf.taverna.t2.workbench.report.UnsatisfiedEntityKind;
import net.sf.taverna.t2.workbench.report.view.ReportViewConfigureAction;
import net.sf.taverna.t2.workbench.retry.RetryConfigureAction;
import net.sf.taverna.t2.workbench.ui.impl.configuration.ui.T2ConfigurationFrame;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;

import org.apache.log4j.Logger;

import edu.stanford.ejalbert.BrowserLauncher;

/**
 * @author alanrw
 *
 */
public class BasicExplainer implements VisitExplainer {

	private static Logger logger = Logger
			.getLogger(BasicExplainer.class);

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.report.explainer.VisitExplainer#canExplain(net.sf.taverna.t2.visit.VisitKind, int)
	 */
	public boolean canExplain(VisitKind vk, int resultId) {
		if (vk instanceof DataflowCollation) {
			return true;
		}
		if (vk instanceof FailedEntityKind) {
			return true;
		}
		if (vk instanceof IncompleteDataflowKind) {
			return true;
		}
		if (vk instanceof InvalidDataflowKind) {
			return true;
		}
		if (vk instanceof UnresolvedOutputKind) {
			return true;
		}
		if (vk instanceof UnsatisfiedEntityKind) {
			return true;
		}
		if (vk instanceof FragilityCheck) {
			return true;
		}
		if ((vk instanceof HealthCheck) &&
				((resultId == HealthCheck.INVALID_SCRIPT) ||
				(resultId == HealthCheck.CONNECTION_PROBLEM) ||
				(resultId == HealthCheck.INVALID_URL) ||
				(resultId == HealthCheck.IO_PROBLEM) ||
				(resultId == HealthCheck.TIME_OUT) ||
				(resultId == HealthCheck.MISSING_DEPENDENCY) ||
				(resultId == HealthCheck.DEFAULT_VALUE) ||
				(resultId == HealthCheck.BAD_WSDL) ||
				(resultId == HealthCheck.NOT_HTTP) ||
				(resultId == HealthCheck.UNSUPPORTED_STYLE) ||
				(resultId == HealthCheck.UNKNOWN_OPERATION) ||
				(resultId == HealthCheck.NO_ENDPOINTS) ||
				(resultId == HealthCheck.INVALID_CONFIGURATION) ||
				(resultId == HealthCheck.NULL_DATATYPE) ||
				(resultId == HealthCheck.DISABLED) ||
				(resultId == HealthCheck.DATATYPE_SOURCE) ||
				(resultId == HealthCheck.UNRECOGNIZED))) {
			return true;
		}
		return false;
	}

	public JComponent getExplanation(VisitReport vr) {
		VisitKind vk = vr.getKind();
		int resultId = vr.getResultId();
		if (vk instanceof DataflowCollation) {
			return explanationDataflowCollation(vr);
		}
		if (vk instanceof FailedEntityKind) {
		    return explanationFailedEntity(vr);
		}
		if ((vk instanceof IncompleteDataflowKind) && (resultId == IncompleteDataflowKind.INCOMPLETE_DATAFLOW)) {
			return explanationDataflowIncomplete(vr);
		}
		if (vk instanceof InvalidDataflowKind) {
		    return explanationInvalidDataflow(vr);
		}
		if (vk instanceof UnresolvedOutputKind) {
		    return explanationUnresolvedOutput(vr);
		}
		if (vk instanceof UnsatisfiedEntityKind) {
		    return explanationUnsatisfiedEntity(vr);
		}

		if ((vk instanceof FragilityCheck) && (resultId == FragilityCheck.SOURCE_FRAGILE)) {
			return explanationSourceFragile(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_SCRIPT)) {
			return explanationBeanshellInvalidScript(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.CONNECTION_PROBLEM)) {
			return explanationConnectionProblem(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_URL)) {
			return explanationInvalidUrl(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.TIME_OUT)) {
			return explanationTimeOut(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.IO_PROBLEM)) {
			return explanationIoProblem(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.MISSING_DEPENDENCY)) {
			return explanationMissingDependency(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.DEFAULT_VALUE)) {
			return explanationDefaultValue(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.BAD_WSDL)) {
			return explanationBadWSDL(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.NOT_HTTP)) {
			return explanationNotHTTP(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.UNSUPPORTED_STYLE)) {
			return explanationUnsupportedStyle(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.UNKNOWN_OPERATION)) {
			return explanationUnknownOperation(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.NO_ENDPOINTS)) {
			return explanationNoEndpoints(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_CONFIGURATION)) {
			return explanationInvalidConfiguration(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.NULL_DATATYPE)) {
			return explanationNullDatatype(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.DISABLED)) {
			return explanationDisabled(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.DATATYPE_SOURCE)) {
			return explanationDatatypeSource(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.UNRECOGNIZED)) {
			return explanationUnrecognized(vr);
		}
		return null;
	}

	public JComponent getSolution(VisitReport vr) {
		VisitKind vk = vr.getKind();
		int resultId = vr.getResultId();
		if (vk instanceof DataflowCollation) {
			return solutionDataflowCollation(vr);
		}
		if (vk instanceof FailedEntityKind) {
		    return solutionFailedEntity(vr);
		}
		if ((vk instanceof IncompleteDataflowKind) && (resultId == IncompleteDataflowKind.INCOMPLETE_DATAFLOW)) {
			return solutionDataflowIncomplete(vr);
		}
		if (vk instanceof InvalidDataflowKind) {
		    return solutionInvalidDataflow(vr);
		}
		if (vk instanceof UnresolvedOutputKind) {
		    return solutionUnresolvedOutput(vr);
		}
		if (vk instanceof UnsatisfiedEntityKind) {
		    return solutionUnsatisfiedEntity(vr);
		}

		if ((vk instanceof FragilityCheck) && (resultId == FragilityCheck.SOURCE_FRAGILE)) {
			return solutionSourceFragile(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_SCRIPT)) {
			return solutionBeanshellInvalidScript(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.CONNECTION_PROBLEM)) {
			return solutionConnectionProblem(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_URL)) {
			return solutionInvalidUrl(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.TIME_OUT)) {
			return solutionTimeOut(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.IO_PROBLEM)) {
			return solutionIoProblem(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.MISSING_DEPENDENCY)) {
			return solutionMissingDependency(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.DEFAULT_VALUE)) {
			return solutionDefaultValue(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.BAD_WSDL)) {
			return solutionBadWSDL(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.NOT_HTTP)) {
			return solutionNotHTTP(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.UNSUPPORTED_STYLE)) {
			return solutionUnsupportedStyle(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.UNKNOWN_OPERATION)) {
			return solutionUnknownOperation(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.NO_ENDPOINTS)) {
			return solutionNoEndpoints(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.INVALID_CONFIGURATION)) {
			return solutionInvalidConfiguration(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.NULL_DATATYPE)) {
			return solutionNullDatatype(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.DISABLED)) {
			return solutionDisabled(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.DATATYPE_SOURCE)) {
			return solutionDatatypeSource(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.UNRECOGNIZED)) {
			return solutionUnrecognized(vr);
		}
		return null;
	}
	
    private static JComponent explanationFailedEntity(VisitReport vr) {
	Processor p = (Processor) (vr.getSubject());
	DataflowActivity da = null;
	for (Activity a : p.getActivityList()) {
	    if (a instanceof DataflowActivity) {
		da = (DataflowActivity) a;
		break;
	    }
	}
	String message = "There is possibly a problem with the service's list handling";
	if (da != null) {
	    message += ", or a problem with the nested workflow";
	}
	return createPanel(new Object[] {message});
    }

    private static JComponent explanationInvalidDataflow(VisitReport vr) {
	return createPanel(new Object[] {"The workflow has problems - see other reports"});
    }

    private static JComponent explanationUnresolvedOutput(VisitReport vr) {
	DataflowOutputPort dop = (DataflowOutputPort) vr.getSubject();
	Datalink incomingLink = dop.getInternalInputPort().getIncomingLink();
	String message;
	if (incomingLink == null) {
	    message = "The workflow output port is not unconnected";
	} else {
	    message = "The workflow output port is connected to a service or port with problems";
	}
	return createPanel(new Object[] {message});
    }

    private static JComponent explanationUnsatisfiedEntity(VisitReport vr) {
	return createPanel(new Object[] {"The service could not be properly checked"});
    }

	private static JComponent explanationDataflowCollation(VisitReport vr) {
	    return createPanel(new Object[] {"There are problems in the nested workflow"});
	}
	
	private static JComponent explanationDataflowIncomplete(VisitReport vr) {
	    return createPanel(new Object[] {"A workflow must contain at least one service or at least one output port"});
	}
	
	private static JComponent explanationSourceFragile(VisitReport vr) {
	    ProcessorInputPort pip = (ProcessorInputPort) vr.getProperty("sinkPort");
	    Processor sourceProcessor = (Processor) vr.getProperty("sourceProcessor");
	    String message = "A single error into ";
	    if (pip == null) {
		message += "an input port ";
	    } else {
		message += "\"" + pip.getName() + "\" ";
	    }
	    if (sourceProcessor != null) {
		message += "from \"" + sourceProcessor.getLocalName() + "\"";
	    }
	    message += " can cause the service call to fail";
	    return createPanel(new Object[] {message});
	}
	
	private static JComponent explanationConnectionProblem(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "the endpoint";
		}
		String responseCode = (String) (vr.getProperty("responseCode"));
		if (responseCode == null) {
			responseCode = "an unexpected response code";
		} else {
			responseCode = "response code: " + responseCode;
		}
		return createPanel(new Object[]{"Taverna connected to \"" + endpoint + "\" but got back " + responseCode});
	}
	
	private static JComponent explanationIoProblem(VisitReport vr) {
		String message = "Connecting to ";
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			message += "the endpoint";
		} else {
			message += "\"" + endpoint + "\"";
		}
		message += " caused ";
		Exception e = (Exception) (vr.getProperty("exception"));
		if (e == null) {
			message += "an exception";
		} else {
			message += "\"" + e.getMessage() + "\"";
		}
		return createPanel(new Object[] {message});
	}
	
	private static JComponent explanationInvalidUrl(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "the endpoint";
		}
		return createPanel(new Object[] {"Taverna was unable to connect to \"" + endpoint + "\" because it is not a valid URL"});
	}
	
	private static JComponent explanationTimeOut(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "the endpoint";
		}
		String timeOutString = (String) (vr.getProperty("timeOut"));
		if (timeOutString == null) {
			timeOutString = " the timeout limit";
		} else {
			timeOutString += "ms";
		}
		return createPanel(new Object[] {"Taverna was unable to connect to \"" + endpoint + "\" within " + timeOutString});
	}
	
	private static JComponent explanationMissingDependency(VisitReport vr) {
		Set<String> dependencies = (Set<String>) (vr.getProperty("dependencies"));
		String message = "Taverna could not find ";
		if (dependencies == null) {
			message += "some dependencies";
		} else {
		    for (Iterator i = dependencies.iterator(); i.hasNext();) {
			String s = (String) i.next();
			message += s;
			if (i.hasNext()) {
			    message += " and ";
			}
		    }
		}
		File directory = (File) vr.getProperty("directory");
		if (directory != null) {
		    try {
			message += " in directory " + directory.getCanonicalPath();
		    }
		    catch (IOException e) {
			logger.error("Could not get path", e);
		    }
		}
		return createPanel(new Object[] {message});
	}
	
	private static JComponent explanationDefaultValue(VisitReport vr) {
		String value = (String) (vr.getProperty("value"));
		if (value == null) {
			value = "the default value";
		}
		return createPanel(new Object[] {"The service still has its value set to \"" + value + "\""});
	}
	
	private static JComponent explanationBadWSDL(VisitReport vr) {
		Exception e = (Exception) (vr.getProperty("exception"));
		String message = "Parsing the WSDL caused ";
		if (e == null) {
			message += " an exception";
		} else {
			message += "\"" + e.getMessage() + "\"";
		}
		return createPanel(new Object[] {message});
	}
	
	private static JComponent explanationNotHTTP(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "The endpoint";
		} else {
			endpoint = "\"" + endpoint + "\"";
		}
		return createPanel(new Object[] {endpoint + " may not be accessible if you run the workflow elsewhere"});
	}
	
	private static JComponent explanationUnsupportedStyle(VisitReport vr) {
		String message = "Taverna does not support ";
		String style = (String) (vr.getProperty("style"));
		String use = (String) (vr.getProperty("use"));
		String kind = null;
		if ((style != null) && (use != null)) {
			kind = style + "/" + use;
		}
		if (kind == null) {
			message += " the kind of message the service uses";
		} else {
			message += " the \"" + kind + "\" messages the service uses";
		}
		return createPanel(new Object[] {message});
	}
	
	private static JComponent explanationUnknownOperation(VisitReport vr) {
		String message = "Taverna could not find the operation ";
		String operationName = (String) vr.getProperty("operationName");
		if (operationName == null) {
		    operationName = "called by the service";
		} else {
		    operationName = "\"" + operationName + "\"";
		}
		return createPanel(new Object[] {message + operationName});
	}
	
	private static JComponent explanationNoEndpoints(VisitReport vr) {
		String message = "Taverna could not find where to call the operation";
		String operationName = (String) vr.getProperty("operationName");
		if (operationName == null) {
		    operationName = "called by the service";
		} else {
		    operationName = "\"" + operationName + "\"";
		}
		return createPanel(new Object[] {message + operationName});
	}
	
	private static JComponent explanationInvalidConfiguration(VisitReport vr) {
		Exception e = (Exception) (vr.getProperty("exception"));
		String message = "Trying to understand the splitter configuration caused ";
		if (e == null) {
			message += " an exception";
		} else {
			message += "\"" + e.getMessage() + "\"";
		}
		return createPanel(new Object[] {message});
	}
	
	private static JComponent explanationNullDatatype(VisitReport vr) {
		String message = "The XML splitter appears to have a NULL datatype";
		return createPanel(new Object[] {message});
	}
	
	private static JComponent explanationDisabled(VisitReport vr) {
		String message = "Taverna could not contact the service when the workflow was opened";
		return createPanel(new Object[] {message});
	}
	
	private static JComponent explanationDatatypeSource(VisitReport vr) {
	    String message = "The data going into ";
	    String sinkPortName = (String) vr.getProperty("sinkPortName");
	    if (sinkPortName == null) {
		sinkPortName = "a port";
	    }
	    else {
		sinkPortName = "port \"" + sinkPortName + "\"";
	    }
	    message += sinkPortName;
	    String sourceName = (String) vr.getProperty("sourceName");
	    String isProcessorSource = (String) vr.getProperty("isProcessorSource");
	    if (sourceName != null) {
		message += " from ";
		if (isProcessorSource != null) {
		    if (isProcessorSource.equals("true")) {
			message += "service ";
		    } else {
			message += "port ";
		    }
		}
		message += "\"" + sourceName + "\"";
	    }
	    message += " may not be XML as required";
		return createPanel(new Object[] {message});
	}
	
	private static JComponent explanationBeanshellInvalidScript(VisitReport vr) {
	    return createPanel(new Object[] {"There are errors in the script of the service.\nWhen the workflow runs, any calls of the service will fail"});
	}
	
	private static JComponent explanationUnrecognized(VisitReport vr) {
		String message = "Taverna could not recognize the service when the workflow was opened";
		return createPanel(new Object[] {message});
	}
	
    private static JComponent solutionFailedEntity(VisitReport vr) {
	Processor p = (Processor) (vr.getSubject());
	DataflowActivity da = null;
	for (Activity a : p.getActivityList()) {
	    if (a instanceof DataflowActivity) {
		da = (DataflowActivity) a;
		break;
	    }
	}
	String message = "Check the list handling of the service and the predicted behavior of the connections into it";
	JButton button = null;
	if (da != null) {
	    message += ", or change the nested workflow";
	    button = new JButton();
	    button.setAction(new EditNestedDataflowAction(da));
	    button.setText("Edit \"" + p.getLocalName() + "\"");
	}
	return createPanel(new Object[] {message, button});
    }

    private static JComponent solutionInvalidDataflow(VisitReport vr) {
	String message = "Fix other problens within the workflow";
	return createPanel(new Object[] {message});
    }

    private static JComponent solutionUnresolvedOutput(VisitReport vr) {
	JButton deleteButton = null;

	DataflowOutputPort port = (DataflowOutputPort) vr.getSubject();
	DataflowOutputPort dop = (DataflowOutputPort) vr.getSubject();
	Datalink incomingLink = dop.getInternalInputPort().getIncomingLink();
	String message;
	if (incomingLink == null) {
	    message = "Connect the workflow output port to a service or a workflow input port.  Alternatively,";
	    final Dataflow d = FileManager.getInstance().getCurrentDataflow();
	    final DataflowOutputPort p = port;
	    deleteButton = new JButton(new AbstractAction("Remove port") {
		    public void actionPerformed(ActionEvent e) {
			Edit removeEdit = EditManager.getInstance().getEdits().getRemoveDataflowOutputPortEdit(d, p);
			try {
			    EditManager.getInstance().doDataflowEdit(d, removeEdit);
			} catch (EditException ex) {
			    logger.error("Could not perform edit", ex);
			}
		    }
		});
	} else {
	    message = "Fix the problems with the service the workflow output port is connectd to";
	}
	return createPanel(new Object[] {message, deleteButton});
    }

    private static JComponent solutionUnsatisfiedEntity(VisitReport vr) {
	String message = "";
	Dataflow currentDataflow = FileManager.getInstance().getCurrentDataflow();
	Tools.ProcessorSplit ps = Tools.splitProcessors(currentDataflow.getProcessors(),
						 (Processor) (vr.getSubject()));
	Set<Processor> upStream = ps.getUpStream();
	ReportManager rm = ReportManager.getInstance();
	boolean plural = false;
	for (Processor p : upStream) {
	    Set<VisitReport> reports = rm.getReports(currentDataflow, p);
	    for (VisitReport report : reports) {
		if (report.getKind() instanceof FailedEntityKind) {
		    if (!message.equals("")) {
			message += " and";
			plural = true;
		    }
		    message += " " + p.getLocalName();
		}
	    }
	}
	if (message.equals("")) {
	    return null;
	}
	if (!plural) {
	    message = "The underlying problem is with" + message;
	} else {
	    message = "The underlying problems are with" + message;
	}
	return createPanel(new Object[] {message});
    }

    private static JComponent solutionDataflowCollation(VisitReport vr) {
	String message = "Edit the nested workflow to fix its problems";
	JButton button = null;
		Processor p = (Processor) (vr.getSubject());
		DataflowActivity da = null;
		for (Activity a : p.getActivityList()) {
		    if (a instanceof DataflowActivity) {
			da = (DataflowActivity) a;
			break;
		    }
		}
		if (da != null) {
		    button = new JButton();
		    button.setAction(new EditNestedDataflowAction(da));
		    button.setText("Edit \"" + p.getLocalName() + "\"");
		}
		String reminder = "Remember to save the nested workflow";
		return createPanel(new Object[] {message, button, reminder});
    }

    private static JComponent solutionDataflowIncomplete(VisitReport vr) {
	String message = "Add a service from the service panel to the workflow, or";
	JButton button = new JButton();
	button.setAction(new AddDataflowOutputAction((Dataflow) vr.getSubject(), null));
	button.setText("Add an output port");
	return createPanel(new Object[] {message, button});
    }

    private static JComponent solutionSourceFragile(VisitReport vr) {
	Processor sourceProcessor = (Processor) vr.getProperty("sourceProcessor");
	String labelText = "Make ";
	if (sourceProcessor == null) {
	    labelText += "the source service ";
	} else {
	    labelText += "\"" + sourceProcessor.getLocalName() + "\" ";
	}
	labelText += "more robust by setting it to retry on failure";
	JButton button = null;
	if (sourceProcessor != null) {
	    Retry retryLayer = null;
	    for (DispatchLayer dl : sourceProcessor.getDispatchStack().getLayers()) {
		if (dl instanceof Retry) {
		    retryLayer = (Retry) dl;
		    break;
		}
	    }
	    if (retryLayer != null) {
		button = new JButton();
		button.setAction(new RetryConfigureAction(null, null, retryLayer));
		button.setText("Set retry");
	    }
	}
	return createPanel(new Object[] {labelText, button});
    }

	private static JComponent solutionBeanshellInvalidScript(VisitReport vr) {
		JButton button = new JButton();
		Processor p = (Processor) (vr.getSubject());
		button.setAction(new ReportViewConfigureAction(p));
		button.setText("Configure " + p.getLocalName());
		return createPanel(new Object[] {"Configure the service by clicking the button below.\nCheck that the script is valid before saving it.",
						     button});
	}

    private static JComponent solutionConnectionProblem(VisitReport vr) {
	String endpoint = (String) (vr.getProperty("endpoint"));
	String connectMessage = "";
	JButton connectButton = null;
	if (endpoint == null) {
	    endpoint = "the endpoint";
	    connectMessage = "Try to connect to the endpoint";
	} else {
	    connectMessage = "Try to connect to " + endpoint + " in a browser";
	    final String end = endpoint;
	    connectButton = new JButton(new AbstractAction("Open in browser") {
		    public void actionPerformed(ActionEvent e) {
			try {
			    BrowserLauncher launcher = new BrowserLauncher();
			    launcher.openURLinBrowser(end);
			}
			catch (Exception ex) {
			    logger.error("Failed to open endpoint", ex);
			}
		    }
		});
	}
	String workedMessage = "If the connection did not work then contact the service provider or workflow creator, else check if you are using a HTTP Proxy and set Taverna's proxy settings";
	JButton preferencesButton = null;
	if (endpoint != null) {
	    preferencesButton = new JButton(new AbstractAction("Change HTTP proxy") {
		    public void actionPerformed(ActionEvent e) {
			T2ConfigurationFrame.showFrame();
		    }
		});
	}
	String editMessage = null;
	JButton editButton = null;
	DisabledActivity da = null;
	for (Activity a : ((Processor)vr.getSubject()).getActivityList()) {
	    if (a instanceof DisabledActivity) {
	    	da = (DisabledActivity) a;
	    	break;
	    }
	}
	if (da != null) {
    	editMessage = "If the service has moved then try to edit its properties";
	    editButton = new JButton(new DisabledActivityConfigurationAction(da, null));
	}
	return createPanel(new Object[] {connectMessage,
					 connectButton,
					 workedMessage,
					 preferencesButton,
					 editMessage,
					 editButton});

    }

    private static JComponent solutionInvalidUrl(VisitReport vr) {
	String message = "Contact the service provider or workflow creator";
	String editMessage = "If the service has moved then try to edit its properties";
	JButton editButton = null;
	DisabledActivity da = null;
	for (Activity a : ((Processor)vr.getSubject()).getActivityList()) {
	    if (a instanceof DisabledActivity) {
		da = (DisabledActivity) a;
		break;
	    }
	}
	if (da != null) {
	    editButton = new JButton(new DisabledActivityConfigurationAction(da, null));
	}
	return createPanel(new Object[] {message, editMessage, editButton});
    }

    private static JComponent solutionTimeOut(VisitReport vr) {
	return createPanel(new Object[] {"Try the service later, if it still does not work then contact the service provider or workflow creator"});
    }

    private static JComponent solutionIoProblem(VisitReport vr) {
	String message = "Try to open ";
	String endpoint = (String) (vr.getProperty("endpoint"));
	JButton connectButton = null;
	if (endpoint == null) {
	    message += "the endpoint ";
	} else {
	    message += "\"" + endpoint + "\" ";
	    final String end = endpoint;
	    connectButton = new JButton(new AbstractAction("Open in browser") {
		    public void actionPerformed(ActionEvent e) {
			try {
			    BrowserLauncher launcher = new BrowserLauncher();
			    launcher.openURLinBrowser(end);
			}
			catch (Exception ex) {
			    logger.error("Failed to open endpoint", ex);
			}
		    }
		});
	}
	message += "in a file or web browser";
	String elseMessage = "If that does not work then contact the service provider ot workflow creator";
	String editMessage = "If the service has moved then try to edit its properties";
	JButton editButton = null;
	DisabledActivity da = null;
	for (Activity a : ((Processor)vr.getSubject()).getActivityList()) {
	    if (a instanceof DisabledActivity) {
		da = (DisabledActivity) a;
		break;
	    }
	}
	if (da != null) {
	    editButton = new JButton(new DisabledActivityConfigurationAction(da, null));
	}
	return createPanel(new Object[] {message,
					     connectButton,
					     elseMessage,
					 editMessage,
					 editButton});
    }

    private static JComponent solutionMissingDependency(VisitReport vr) {
	String message = "Put ";
	Set<String> dependencies = (Set<String>) (vr.getProperty("dependencies"));
	if (dependencies == null) {
	    message += "the dependencies";
	} else {
	    for (Iterator i = dependencies.iterator(); i.hasNext();) {
		String s = (String) i.next();
		message += s;
		if (i.hasNext()) {
		    message += " and ";
		}
	    }
	}
	File directory = (File) vr.getProperty("directory");
	if (directory != null) {
	    try {
		message += " in directory " + directory.getCanonicalPath();
	    }
	    catch (IOException e) {
		logger.error("Could not get path", e);
	    }
	} else {
	    message += " in the application directory";
	}
	String elseMessage = "If you do not have the files then contact the workflow creator";
	return createPanel(new Object[] {message, elseMessage});
    }

    private static JComponent solutionDefaultValue(VisitReport vr) {
	String message = "Set default value of the service by clicking the button";
	JButton button = new JButton();
	Processor p = (Processor) (vr.getSubject());
	button.setAction(new ReportViewConfigureAction(p));
	button.setText("Set value");
	return createPanel(new Object[] {message, button});
    }

    private static JComponent solutionBadWSDL(VisitReport vr) {
	String message = "Contact the service provider or workflow creator";
	return createPanel(new Object[] {message});
    }

    private static JComponent solutionNotHTTP(VisitReport vr) {
	String endpoint = (String) (vr.getProperty("endpoint"));
	if (endpoint == null) {
	    endpoint = "the endpoint";
	} else {
	    endpoint = "\"" + endpoint + "\"";
	}
	String message = "Move the file at " + endpoint + " to a web server";
	return createPanel(new Object[] {message});
    }

    private static JComponent solutionUnsupportedStyle(VisitReport vr) {
	String message = "Contact the service provider to see if there is an alternative style of service available";
	return createPanel(new Object[] {message});
    }

    private static JComponent solutionUnknownOperation(VisitReport vr) {
	String message = "Contact the service provider to see if the operation has been renamed";
	String editMessage = "If you know the new name then try to edit the service's properties";
	JButton editButton = null;
	DisabledActivity da = null;
	for (Activity a : ((Processor)vr.getSubject()).getActivityList()) {
	    if (a instanceof DisabledActivity) {
		da = (DisabledActivity) a;
		break;
	    }
	}
	if (da != null) {
	    editButton = new JButton(new DisabledActivityConfigurationAction(da, null));
	}
	return createPanel(new Object[] {message, editMessage, editButton});
    }

    private static JComponent solutionNoEndpoints(VisitReport vr) {
	String message = "Contact the service provider or workflow creator";
	return createPanel(new Object[] {message});
    }

    private static JComponent solutionInvalidConfiguration(VisitReport vr) {
	String message = "Contact the service provider or workflow creator";
	return createPanel(new Object[] {message});
    }

    private static JComponent solutionNullDatatype(VisitReport vr) {
	String message = "Contact the service provider or workflow creator";
	return createPanel(new Object[] {message});
    }

    private static JComponent solutionDisabled(VisitReport vr) {
	String message = "Perform a full check and fix any connection problems with the service";
	return createPanel(new Object[] {message});
    }

    private static JComponent solutionDatatypeSource(VisitReport vr) {
	    String sinkPortName = (String) vr.getProperty("sinkPortName");
	    if (sinkPortName == null) {
		return null;
	    }
	    String removeMessage = "Remove the link to " + "port \"" + sinkPortName + "\"";
	    ProcessorInputPort pip = (ProcessorInputPort) vr.getProperty("sinkPort");
	    final InputPortTypeDescriptorActivity a = (InputPortTypeDescriptorActivity) vr.getProperty("activity");
	    String addSplitterMessage = "Add an XML splitter for " + "port \"" + sinkPortName + "\"";
	    JButton button = null;
	    if (pip != null) {
		Datalink incomingLink = pip.getIncomingLink();
		if (incomingLink != null) {
		    button = new JButton();
		    final Dataflow d = FileManager.getInstance().getCurrentDataflow();
		    final String portName = sinkPortName;
		    final Datalink link = incomingLink;
		    button.setAction(new AbstractAction ("Remove link and add XML splitter") {
			    public void actionPerformed(ActionEvent e) {
				Edit removeLinkEdit = Tools.getDisconnectDatalinkAndRemovePortsEdit(link);
				Edit addXMLEdit = new AddXMLSplitterEdit(d, (Activity<?>)a, portName, true);
				List<Edit<?>> editList = Arrays.asList(new Edit<?>[] {removeLinkEdit, addXMLEdit});
				CompoundEdit ce = new CompoundEdit(editList);
				try {
				    EditManager.getInstance().doDataflowEdit(d, ce);
				} catch (EditException ex) {
				    logger.error("Could not perform edit", ex);
			}
			    }
});
		}
	    }
	    String addConnectionMessage = "Make a connection to a port of the XML splitter";
	    return createPanel(new Object[] {removeMessage, addSplitterMessage, button, addConnectionMessage});
    }

    private static JComponent solutionUnrecognized(VisitReport vr) {
	String message = "Contact the workflow creator to find out what plugins need to be installed";
	return createPanel(new Object[] {message});
    }

	private static JPanel createPanel(Object[] components) {
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridwidth = 1;
		gbc.weightx = 0.9;
		for (Object o : components) {
			if (o == null) {
				continue;
			}
			JComponent component = null;
			if (o instanceof String) {
				component = new ReadOnlyTextArea((String) o);
			} else if (o instanceof JComponent) {
				component = (JComponent) o;
			} else {
				logger.error("Unrecognized component " + o.getClass());
				continue;
			}
			gbc.gridy++;
			if (component instanceof JButton) {
				gbc.weightx = 0;
				gbc.gridwidth = 1;
				gbc.fill = GridBagConstraints.NONE;
			} else {
				gbc.weightx = 0.9;
				gbc.gridwidth = 2;
				gbc.fill = GridBagConstraints.HORIZONTAL;
			}
			result.add(component, gbc);
		}
//		gbc.weightx = 0.9;
//		gbc.weighty = 0.9;
//		gbc.gridx = 0;
//		gbc.gridy++;
//		gbc.gridwidth = 2;
//		gbc.fill = GridBagConstraints.BOTH;
//		result.add(new JPanel(), gbc);
		result.setBackground(SystemColor.text);
		return result;
	}
}
