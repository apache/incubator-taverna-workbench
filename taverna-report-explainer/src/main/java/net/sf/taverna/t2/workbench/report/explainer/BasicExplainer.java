/*******************************************************************************
 * Copyright (C) 2008-2010 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.report.explainer;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.actions.EditNestedDataflowAction;
import net.sf.taverna.t2.activities.disabled.actions.DisabledActivityConfigurationAction;
import net.sf.taverna.t2.activities.wsdl.InputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.AddXMLSplitterEdit;
import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import org.apache.taverna.visit.DataflowCollation;
import org.apache.taverna.visit.VisitKind;
import org.apache.taverna.visit.VisitReport;
import net.sf.taverna.t2.visit.fragility.FragilityCheck;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowOutputAction;
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
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.impl.configuration.ui.T2ConfigurationFrame;
import org.apache.taverna.workflowmodel.CompoundEdit;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.DataflowOutputPort;
import org.apache.taverna.workflowmodel.Datalink;
import org.apache.taverna.workflowmodel.Edit;
import org.apache.taverna.workflowmodel.EditException;
import org.apache.taverna.workflowmodel.Merge;
import org.apache.taverna.workflowmodel.Processor;
import org.apache.taverna.workflowmodel.ProcessorInputPort;
import org.apache.taverna.workflowmodel.ProcessorOutputPort;
import org.apache.taverna.workflowmodel.TokenProcessingEntity;
import org.apache.taverna.workflowmodel.health.HealthCheck;
import org.apache.taverna.workflowmodel.processor.activity.Activity;
import org.apache.taverna.workflowmodel.processor.activity.DisabledActivity;
import org.apache.taverna.workflowmodel.processor.dispatch.DispatchLayer;
import org.apache.taverna.workflowmodel.processor.dispatch.layers.Retry;
import org.apache.taverna.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.ConfigurationUIFactory;

/**
 * @author alanrw
 *
 */
public class BasicExplainer implements VisitExplainer {

	private static Logger logger = Logger.getLogger(BasicExplainer.class);

	private static String PLEASE_CONTACT = "Please contact the service provider or workflow creator.";

	private EditManager editManager;

	private FileManager fileManager;

	private ReportManager reportManager;

	private SelectionManager selectionManager;

	private List<ConfigurationUIFactory> configurationUIFactories;

	private ActivityIconManager activityIconManager;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.taverna.t2.workbench.report.explainer.VisitExplainer#canExplain
	 * (net.sf.taverna.t2.visit.VisitKind, int)
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
		if ((vk instanceof HealthCheck)
				&& ((resultId == HealthCheck.INVALID_SCRIPT)
						|| (resultId == HealthCheck.CONNECTION_PROBLEM)
						|| (resultId == HealthCheck.INVALID_URL)
						|| (resultId == HealthCheck.IO_PROBLEM)
						|| (resultId == HealthCheck.TIME_OUT)
						|| (resultId == HealthCheck.MISSING_DEPENDENCY)
						|| (resultId == HealthCheck.DEFAULT_VALUE)
						|| (resultId == HealthCheck.BAD_WSDL)
						|| (resultId == HealthCheck.NOT_HTTP)
						|| (resultId == HealthCheck.UNSUPPORTED_STYLE)
						|| (resultId == HealthCheck.UNKNOWN_OPERATION)
						|| (resultId == HealthCheck.NO_ENDPOINTS)
						|| (resultId == HealthCheck.INVALID_CONFIGURATION)
						|| (resultId == HealthCheck.NULL_DATATYPE)
						|| (resultId == HealthCheck.DISABLED)
						|| (resultId == HealthCheck.DATATYPE_SOURCE)
						|| (resultId == HealthCheck.UNRECOGNIZED)
						|| (resultId == HealthCheck.LOOP_CONNECTION)
						|| (resultId == HealthCheck.UNMANAGED_LOCATION) || (resultId == HealthCheck.INCOMPATIBLE_MIMETYPES))) {
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
		if ((vk instanceof IncompleteDataflowKind)
				&& (resultId == IncompleteDataflowKind.INCOMPLETE_DATAFLOW)) {
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

		if ((vk instanceof FragilityCheck)
				&& (resultId == FragilityCheck.SOURCE_FRAGILE)) {
			return explanationSourceFragile(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.INVALID_SCRIPT)) {
			return explanationBeanshellInvalidScript(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.CONNECTION_PROBLEM)) {
			return explanationConnectionProblem(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.INVALID_URL)) {
			return explanationInvalidUrl(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.TIME_OUT)) {
			return explanationTimeOut(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.IO_PROBLEM)) {
			return explanationIoProblem(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.MISSING_DEPENDENCY)) {
			return explanationMissingDependency(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.DEFAULT_VALUE)) {
			return explanationDefaultValue(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.BAD_WSDL)) {
			return explanationBadWSDL(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.NOT_HTTP)) {
			return explanationNotHTTP(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.UNSUPPORTED_STYLE)) {
			return explanationUnsupportedStyle(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.UNKNOWN_OPERATION)) {
			return explanationUnknownOperation(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.NO_ENDPOINTS)) {
			return explanationNoEndpoints(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.INVALID_CONFIGURATION)) {
			return explanationInvalidConfiguration(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.NULL_DATATYPE)) {
			return explanationNullDatatype(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.DISABLED)) {
			return explanationDisabled(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.DATATYPE_SOURCE)) {
			return explanationDatatypeSource(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.UNRECOGNIZED)) {
			return explanationUnrecognized(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.LOOP_CONNECTION)) {
			return explanationLoopConnection(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.UNMANAGED_LOCATION)) {
			return explanationUnmanagedLocation(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.INCOMPATIBLE_MIMETYPES)) {
			return explanationIncompatibleMimetypes(vr);
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
		if ((vk instanceof IncompleteDataflowKind)
				&& (resultId == IncompleteDataflowKind.INCOMPLETE_DATAFLOW)) {
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

		if ((vk instanceof FragilityCheck)
				&& (resultId == FragilityCheck.SOURCE_FRAGILE)) {
			return solutionSourceFragile(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.INVALID_SCRIPT)) {
			return solutionBeanshellInvalidScript(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.CONNECTION_PROBLEM)) {
			return solutionConnectionProblem(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.INVALID_URL)) {
			return solutionInvalidUrl(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.TIME_OUT)) {
			return solutionTimeOut(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.IO_PROBLEM)) {
			return solutionIoProblem(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.MISSING_DEPENDENCY)) {
			return solutionMissingDependency(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.DEFAULT_VALUE)) {
			return solutionDefaultValue(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.BAD_WSDL)) {
			return solutionBadWSDL(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.NOT_HTTP)) {
			return solutionNotHTTP(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.UNSUPPORTED_STYLE)) {
			return solutionUnsupportedStyle(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.UNKNOWN_OPERATION)) {
			return solutionUnknownOperation(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.NO_ENDPOINTS)) {
			return solutionNoEndpoints(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.INVALID_CONFIGURATION)) {
			return solutionInvalidConfiguration(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.NULL_DATATYPE)) {
			return solutionNullDatatype(vr);
		}
		if ((vk instanceof HealthCheck) && (resultId == HealthCheck.DISABLED)) {
			return solutionDisabled(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.DATATYPE_SOURCE)) {
			return solutionDatatypeSource(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.UNRECOGNIZED)) {
			return solutionUnrecognized(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.LOOP_CONNECTION)) {
			return solutionLoopConnection(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.UNMANAGED_LOCATION)) {
			return solutionUnmanagedLocation(vr);
		}
		if ((vk instanceof HealthCheck)
				&& (resultId == HealthCheck.INCOMPATIBLE_MIMETYPES)) {
			return solutionIncompatibleMimetypes(vr);
		}
		return null;
	}

	private static JComponent explanationFailedEntity(VisitReport vr) {
		if (vr.getSubject() instanceof Processor) {
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
			return createPanel(new Object[] { message + "." });
		} else if (vr.getSubject() instanceof Merge) {
			return createPanel(new Object[] { "The merge is combining data of different depths." });
		}
		return null;
	}

	private static JComponent explanationInvalidDataflow(VisitReport vr) {
		return createPanel(new Object[] { "The workflow contains errors - see other reports." });
	}

	private static JComponent explanationUnresolvedOutput(VisitReport vr) {
		DataflowOutputPort dop = (DataflowOutputPort) vr.getSubject();
		Datalink incomingLink = dop.getInternalInputPort().getIncomingLink();
		String message;
		if (incomingLink == null) {
			message = "The workflow output port is not connected.";
		} else {
			message = "The workflow output port is connected to a service or port that has errors.";
		}
		return createPanel(new Object[] { message });
	}

	private static JComponent explanationUnsatisfiedEntity(VisitReport vr) {
		return createPanel(new Object[] { "The service could not be properly checked." });
	}

	private static JComponent explanationDataflowCollation(VisitReport vr) {
		if (vr.getStatus().equals(VisitReport.Status.SEVERE)) {
			return createPanel(new Object[] { "There are errors in the nested workflow." });
		} else {
			return createPanel(new Object[] { "There are warnings in the nested workflow." });
		}
	}

	private static JComponent explanationDataflowIncomplete(VisitReport vr) {
		return createPanel(new Object[] { "A workflow must contain at least one service or at least one output port." });
	}

	private static JComponent explanationSourceFragile(VisitReport vr) {
		ProcessorInputPort pip = (ProcessorInputPort) vr
				.getProperty("sinkPort");
		Processor sourceProcessor = (Processor) vr
				.getProperty("sourceProcessor");
		String message = "A single error input into ";
		if (pip == null) {
			message += "an input port ";
		} else {
			message += "\"" + pip.getName() + "\" ";
		}
		if (sourceProcessor != null) {
			message += "from \"" + sourceProcessor.getLocalName() + "\"";
		}
		message += " will cause this service to fail.  ";
		message += "If "
				+ (sourceProcessor == null ? "the source" : sourceProcessor
						.getLocalName())
				+ " is unlikely to fail (for example if it is a StringConstant) then this warning can be ignored.";
		return createPanel(new Object[] { message });
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
		return createPanel(new Object[] { "Taverna connected to \"" + endpoint
				+ "\" but received " + responseCode
				+ ".  The service may still work." });
	}

	private static JComponent explanationIoProblem(VisitReport vr) {
		String message = "Reading from ";
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			message += "the endpoint";
		} else {
			message += "\"" + endpoint + "\"";
		}
		message += " caused ";
		Exception e = (Exception) (vr.getProperty("exception"));
		if (e == null) {
			message += "an error";
		} else {
			message += e.getClass().getCanonicalName() + ": \""
					+ e.getMessage() + "\"";
		}
		return createPanel(new Object[] { message + "." });
	}

	private static JComponent explanationInvalidUrl(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "the endpoint";
		}
		return createPanel(new Object[] { "Taverna was unable to connect to \""
				+ endpoint + "\" because it is not a valid URL." });
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
			try {
				Integer timeOut = Integer.parseInt(timeOutString);
				if (timeOut > 1000) {
					timeOutString = Float.toString(timeOut / 1000) + "s";
				} else {
					timeOutString += "ms";
				}
			} catch (NumberFormatException ex) {
				timeOutString = " the timeout limit";
			}
		}
		return createPanel(new Object[] { "Taverna was unable to connect to \""
				+ endpoint + "\" within " + timeOutString + "." });
	}

	private static JComponent explanationMissingDependency(VisitReport vr) {
		Set<String> dependencies = (Set<String>) (vr
				.getProperty("dependencies"));
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
			} catch (IOException e) {
				logger.error("Could not get path", e);
			}
		}
		return createPanel(new Object[] { message + "." });
	}

	private static JComponent explanationDefaultValue(VisitReport vr) {
		String value = (String) (vr.getProperty("value"));
		if (value == null) {
			value = "the default value";
		}
		return createPanel(new Object[] { "The service still has its value set to \""
				+ value + "\"" });
	}

	private static JComponent explanationBadWSDL(VisitReport vr) {
		Exception e = (Exception) (vr.getProperty("exception"));
		String message = "Parsing the WSDL caused ";
		if (e == null) {
			message += " an exception";
		} else {
			message += "\"" + e.getMessage() + "\"";
		}
		return createPanel(new Object[] { message + "." });
	}

	private static JComponent explanationNotHTTP(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "The endpoint";
		} else {
			endpoint = "\"" + endpoint + "\"";
		}
		return createPanel(new Object[] { endpoint
				+ " might not be accessible if you run the workflow on a different machine." });
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
			message += " the kind of message the service uses.";
		} else {
			message += " the \"" + kind + "\" messages that the service uses.";
		}
		return createPanel(new Object[] { message });
	}

	private static JComponent explanationUnknownOperation(VisitReport vr) {
		String message = "Taverna could not find the operation ";
		String operationName = (String) vr.getProperty("operationName");
		if (operationName == null) {
			operationName = "called by the service";
		} else {
			operationName = "\"" + operationName + "\"";
		}
		return createPanel(new Object[] { message + operationName + "." });
	}

	private static JComponent explanationNoEndpoints(VisitReport vr) {
		String message = "Taverna found the operation ";
		String operationName = (String) vr.getProperty("operationName");
		if (operationName == null) {
			operationName = "called by the service";
		} else {
			operationName = "\"" + operationName + "\"";
		}
		message += operationName;
		message += " but is unable to call it due to lack of location information.";
		return createPanel(new Object[] { message + operationName });
	}

	private static JComponent explanationInvalidConfiguration(VisitReport vr) {
		Exception e = (Exception) (vr.getProperty("exception"));
		String message = "Trying to understand the XML splitter caused ";
		if (e == null) {
			message += " an exception";
		} else {
			message += "\"" + e.getMessage() + "\"";
		}
		return createPanel(new Object[] { message });
	}

	private static JComponent explanationNullDatatype(VisitReport vr) {
		String message = "The XML splitter appears to have a NULL datatype.";
		return createPanel(new Object[] { message });
	}

	private static JComponent explanationDisabled(VisitReport vr) {
		String message = "Taverna could not contact the service when the workflow was opened.";
		return createPanel(new Object[] { message });
	}

	private static JComponent explanationDatatypeSource(VisitReport vr) {
		String message = "The data going into ";
		String sinkPortName = (String) vr.getProperty("sinkPortName");
		if (sinkPortName == null) {
			sinkPortName = "a port";
		} else {
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
		message += " may not be XML.  The service requires XML as input.";
		return createPanel(new Object[] { message });
	}

	private static JComponent explanationBeanshellInvalidScript(VisitReport vr) {
		Exception e = (Exception) vr.getProperty("exception");
		String exceptionMessage = null;
		if (e != null) {
			exceptionMessage = e.getMessage();
		}
		return createPanel(new Object[] {
				"There are errors in the script of the service.\nWhen the workflow runs, any calls of the service will fail with error: ",
				exceptionMessage + "." });
	}

	private static JComponent explanationUnrecognized(VisitReport vr) {
		String message = "Taverna could not recognize the service when the workflow was opened.";
		return createPanel(new Object[] { message });
	}

	private static JComponent explanationLoopConnection(VisitReport vr) {
		return createPanel(new Object[] { "Port \""
				+ vr.getProperty("portname") + "\" must be connected" });
	}

	private static JComponent explanationUnmanagedLocation(VisitReport vr) {
		return createPanel(new Object[] { "The external tool service is configured to run on a location that is not currently known to the Location Manager. It is a good idea to change it to a known location" });
	}

	private static JComponent explanationIncompatibleMimetypes(VisitReport vr) {
		ProcessorInputPort pip = (ProcessorInputPort) vr
				.getProperty("sinkPort");
		ProcessorOutputPort pop = (ProcessorOutputPort) vr
				.getProperty("sourcePort");
		Processor sourceProcessor = (Processor) vr
				.getProperty("sourceProcessor");
		String message = "The data";
		if (pop != null) {
			message += " from port \"" + pop.getName() + "\"";
			if (sourceProcessor != null) {
				message += " of service \"" + sourceProcessor.getLocalName()
						+ "\"";
			}
		}
		if (pip != null) {
			message += " into port \"" + pip.getName() + "\"";
		}
		message += " does not have a compatible mime type";
		return createPanel(new Object[] { message });
	}

	private JComponent solutionFailedEntity(VisitReport vr) {
		if (vr.getSubject() instanceof Processor) {
			Processor p = (Processor) (vr.getSubject());
			DataflowActivity da = null;
			for (Activity a : p.getActivityList()) {
				if (a instanceof DataflowActivity) {
					da = (DataflowActivity) a;
					break;
				}
			}
			String message = "Check the list handling of the service, including the predicted behavior of the service's inputs and outputs";
			JButton button = null;
			if (da != null) {
				message += ", or edit the nested workflow";
				button = new JButton();
				button.setAction(new EditNestedDataflowAction(da, fileManager));
				button.setText("Edit \"" + p.getLocalName() + "\"");
			}
			return createPanel(new Object[] { message + ".", button });
		} else if (vr.getSubject() instanceof Merge) {
			return createPanel(new Object[] { "Check the predicted behaviour of the data being merged." });
		}
		return null;
	}

	private static JComponent solutionInvalidDataflow(VisitReport vr) {
		String message = "Fix the errors within the workflow.";
		return createPanel(new Object[] { message });
	}

	private JComponent solutionUnresolvedOutput(VisitReport vr) {
		JButton deleteButton = null;

		DataflowOutputPort port = (DataflowOutputPort) vr.getSubject();
		DataflowOutputPort dop = (DataflowOutputPort) vr.getSubject();
		Datalink incomingLink = dop.getInternalInputPort().getIncomingLink();
		String message;
		if (incomingLink == null) {
			message = "Connect the workflow output port to a service or a workflow input port.  Alternatively,";
			final Dataflow d = fileManager.getCurrentDataflow();
			final DataflowOutputPort p = port;
			deleteButton = new JButton(new AbstractAction("Remove port") {
				public void actionPerformed(ActionEvent e) {
					Edit removeEdit = editManager.getEdits()
							.getRemoveDataflowOutputPortEdit(d, p);
					try {
						editManager.doDataflowEdit(d, removeEdit);
					} catch (EditException ex) {
						logger.error("Could not perform edit", ex);
					}
				}
			});
		} else {
			message = "Fix the errors of the service that the output port is connected to.";
		}
		return createPanel(new Object[] { message, deleteButton });
	}

	private JComponent solutionUnsatisfiedEntity(VisitReport vr) {
		String message = "";
		Dataflow currentDataflow = fileManager.getCurrentDataflow();
		Tools.ProcessorSplit ps = Tools.splitProcessors(
				currentDataflow.getProcessors(),
				(TokenProcessingEntity) (vr.getSubject()));
		Set<Processor> upStream = ps.getUpStream();
		boolean plural = false;
		for (Processor p : upStream) {
			Set<VisitReport> reports = reportManager.getReports(currentDataflow, p);
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
			message = "The underlying error is caused by" + message;
		} else {
			message = "The underlying errors are caused by" + message;
		}
		return createPanel(new Object[] { message + "." });
	}

	private JComponent solutionDataflowCollation(VisitReport vr) {
		String message = "Edit the nested workflow to fix its problems.";
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
			button.setAction(new EditNestedDataflowAction(da, fileManager));
			button.setText("Edit \"" + p.getLocalName() + "\"");
		}
		String reminder = "Remember to save the nested workflow.";
		return createPanel(new Object[] { message, button, reminder });
	}

	private JComponent solutionDataflowIncomplete(VisitReport vr) {
		String message = "Add a service from the service panel to the workflow, or";
		JButton button = new JButton();
		button.setAction(new AddDataflowOutputAction(
				(Dataflow) vr.getSubject(), null, editManager, selectionManager));
		button.setText("Add an output port");
		return createPanel(new Object[] { message, button });
	}

	private JComponent solutionSourceFragile(VisitReport vr) {
		Processor sourceProcessor = (Processor) vr
				.getProperty("sourceProcessor");
		String labelText = "Make ";
		if (sourceProcessor == null) {
			labelText += "the source service ";
		} else {
			labelText += "\"" + sourceProcessor.getLocalName() + "\" ";
		}
		labelText += "more robust to failure by adding service retries.";
		JButton button = null;
		if (sourceProcessor != null) {
			Retry retryLayer = null;
			for (DispatchLayer dl : sourceProcessor.getDispatchStack()
					.getLayers()) {
				if (dl instanceof Retry) {
					retryLayer = (Retry) dl;
					break;
				}
			}
			if (retryLayer != null) {
				button = new JButton();
				button.setAction(new RetryConfigureAction(null, null,
						retryLayer, editManager, fileManager));
				button.setText("Set retry");
			}
		}
		return createPanel(new Object[] { labelText, button });
	}

	private static JComponent solutionBeanshellInvalidScript(VisitReport vr) {
		JButton button = new JButton();
		Processor p = (Processor) (vr.getSubject());
		button.setAction(new ReportViewConfigureAction(p));
		button.setText("Configure " + p.getLocalName());
		return createPanel(new Object[] {
				"Edit the service script, checking that the script is valid before saving it.",
				button });
	}

	private JComponent solutionConnectionProblem(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		String connectMessage = "";
		JButton connectButton = null;
		if (endpoint == null) {
			endpoint = "the endpoint";
			connectMessage = "Try to connect to the endpoint.";
		} else {
			connectMessage = "Try to connect to " + endpoint + " in a browser.";
			final String end = endpoint;
			connectButton = new JButton(new AbstractAction("Open in browser") {
				public void actionPerformed(ActionEvent e) {
					try {
						Desktop.getDesktop().browse(new URI(end));
					} catch (Exception ex) {
						logger.error("Failed to open endpoint", ex);
					}
				}
			});
		}
		String workedMessage = "If the connection did not work, please contact the service provider or workflow creator.  Alternatively, check if you are using an HTTP Proxy, and edit Taverna's proxy settings.";
		JButton preferencesButton = null;
		if (endpoint != null) {
			preferencesButton = new JButton(new AbstractAction(
					"Change HTTP proxy") {
				public void actionPerformed(ActionEvent e) {
					T2ConfigurationFrame.showConfiguration("HTTP proxy", configurationUIFactories);
				}
			});
		}
		String editMessage = null;
		JButton editButton = null;
		DisabledActivity da = null;
		for (Activity a : ((Processor) vr.getSubject()).getActivityList()) {
			if (a instanceof DisabledActivity) {
				da = (DisabledActivity) a;
				break;
			}
		}
		if (da != null) {
			editMessage = "If the service has moved, change the service's properties to its new location.";
			editButton = new JButton(new DisabledActivityConfigurationAction(
					da, null, editManager, fileManager, reportManager, activityIconManager));
		}
		return createPanel(new Object[] { connectMessage, connectButton,
				workedMessage, preferencesButton, editMessage, editButton });

	}

	private JComponent solutionInvalidUrl(VisitReport vr) {
		String message = "Contact the service provider or workflow creator.";
		String editMessage = "If the service has moved, change the service's properties to its new location.";
		JButton editButton = null;
		DisabledActivity da = null;
		for (Activity a : ((Processor) vr.getSubject()).getActivityList()) {
			if (a instanceof DisabledActivity) {
				da = (DisabledActivity) a;
				break;
			}
		}
		if (da != null) {
			editButton = new JButton(new DisabledActivityConfigurationAction(
					da, null, editManager, fileManager, reportManager, activityIconManager));
		}
		return createPanel(new Object[] { message, editMessage, editButton });
	}

	private JComponent solutionTimeOut(VisitReport vr) {
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
						Desktop.getDesktop().browse(new URI(end));
					} catch (Exception ex) {
						logger.error("Failed to open endpoint", ex);
					}
				}
			});
		}
		message += "in a file, or web, browser.";
		String workedMessage = "If the browser opened the address, then alter the validation timeout in the preferences";
		JButton preferencesButton = new JButton(new AbstractAction(
				"Change timeout") {
			public void actionPerformed(ActionEvent e) {
				T2ConfigurationFrame.showConfiguration("Validation report", configurationUIFactories);
			}
		});
		String didNotWorkMessage = "Alternatively, if the browser did not open the address, try later as the service may be temporarily offline.  If the service remains offline, please contact the service provider or workflow creator.";
		String editMessage = null;
		JButton editButton = null;
		DisabledActivity da = null;
		for (Activity a : ((Processor) vr.getSubject()).getActivityList()) {
			if (a instanceof DisabledActivity) {
				da = (DisabledActivity) a;
				break;
			}
		}
		if (da != null) {
			editMessage = "If the service has moved, change the service's properties to its new location.";
			editButton = new JButton(new DisabledActivityConfigurationAction(
					da, null, editManager, fileManager, reportManager, activityIconManager));
		}
		return createPanel(new Object[] { message, connectButton,
				workedMessage, preferencesButton, didNotWorkMessage,
				editMessage, editButton });
	}

	private JComponent solutionIoProblem(VisitReport vr) {
		String message = "";
		Exception e = (Exception) (vr.getProperty("exception"));
		if (e != null && e instanceof SSLException) {
			message += "There was a problem with establishing a HTTPS connection to the service. ";
			if (e.getMessage().toLowerCase()
					.contains("no trusted certificate found")) {
				message += "Looks like the authenticity of the service could not be confirmed. Check that you have imported the service's certificate under 'Trusted Certificates' in Credential Manager. "
						+ "If this is a WSDL service, try restarting Taverna to refresh certificates used in HTTPS connections.\n\n";
			} else if (e.getMessage().toLowerCase()
					.contains("received fatal alert: bad_certificate")) {
				message += "Looks like you could not be authenticated to the service. Check that you have imported your certificate under 'Your certificates' in Credential Manager. "
						+ "If this is a WSDL service, try restarting Taverna to refresh certificates used in HTTPS connections.\n\n";
			} else {
				message += "Check that you have imported the service's certificate under 'Trusted Certificates' in Credential Manager. "
						+ "If user authentication is required, also check that you have imported your certificate under 'Your certificates' in Credential Manager. "
						+ "If this is a WSDL service, try restarting Taverna to refresh certificates used in HTTPS connections.\n\n";
			}
		}
		message += "Try to open ";
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
						Desktop.getDesktop().browse(new URI(end));
					} catch (Exception ex) {
						logger.error("Failed to open endpoint", ex);
					}
				}
			});
		}
		message += "in a file, or web, browser.";
		String elseMessage = message.startsWith("Try to open") ? "If that does not work, please contact the service provider or workflow creator."
				: null;
		String editMessage = message.startsWith("Try to open") ? "If the service has moved, change the service's properties to its new location."
				: null;
		JButton editButton = null;
		DisabledActivity da = null;
		for (Activity a : ((Processor) vr.getSubject()).getActivityList()) {
			if (a instanceof DisabledActivity) {
				da = (DisabledActivity) a;
				break;
			}
		}
		if (da != null) {
			editButton = new JButton(new DisabledActivityConfigurationAction(
					da, null, editManager, fileManager, reportManager, activityIconManager));
		}
		return createPanel(new Object[] { message, connectButton, elseMessage,
				editMessage, editButton });
	}

	private static JComponent solutionMissingDependency(VisitReport vr) {
		String message = "Put ";
		Set<String> dependencies = (Set<String>) (vr
				.getProperty("dependencies"));
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
			} catch (IOException e) {
				logger.error("Could not get path", e);
			}
		} else {
			message += " in the application directory.";
		}
		String elseMessage = "If you do not have the files, please contact the workflow creator.";
		return createPanel(new Object[] { message, elseMessage });
	}

	private static JComponent solutionDefaultValue(VisitReport vr) {
		String message = "Change the value of the service by clicking the \"Set value\" button";
		JButton button = new JButton();
		Processor p = (Processor) (vr.getSubject());
		button.setAction(new ReportViewConfigureAction(p));
		button.setText("Set value");
		return createPanel(new Object[] { message, button });
	}

	private static JComponent solutionBadWSDL(VisitReport vr) {
		return createPanel(new Object[] { PLEASE_CONTACT });
	}

	private static JComponent solutionNotHTTP(VisitReport vr) {
		String endpoint = (String) (vr.getProperty("endpoint"));
		if (endpoint == null) {
			endpoint = "the endpoint";
		} else {
			endpoint = "\"" + endpoint + "\"";
		}
		String message = "Move the file at " + endpoint + " to a web server.";
		return createPanel(new Object[] { message });
	}

	private static JComponent solutionUnsupportedStyle(VisitReport vr) {
		String message = "Contact the service provider to see if there is an alternative style of service available.";
		return createPanel(new Object[] { message });
	}

	private JComponent solutionUnknownOperation(VisitReport vr) {
		String message = "Contact the service provider to see if the operation has been renamed.";
		String editMessage = "If you know its new name, then please edit the service's properties.";
		JButton editButton = null;
		DisabledActivity da = null;
		for (Activity a : ((Processor) vr.getSubject()).getActivityList()) {
			if (a instanceof DisabledActivity) {
				da = (DisabledActivity) a;
				break;
			}
		}
		if (da != null) {
			editButton = new JButton(new DisabledActivityConfigurationAction(
					da, null, editManager, fileManager, reportManager, activityIconManager));
		}
		return createPanel(new Object[] { message, editMessage, editButton });
	}

	private static JComponent solutionNoEndpoints(VisitReport vr) {
		return createPanel(new Object[] { PLEASE_CONTACT });
	}

	private static JComponent solutionInvalidConfiguration(VisitReport vr) {
		return createPanel(new Object[] { PLEASE_CONTACT });
	}

	private static JComponent solutionNullDatatype(VisitReport vr) {
		return createPanel(new Object[] { PLEASE_CONTACT });
	}

	private static JComponent solutionDisabled(VisitReport vr) {
		String message = "Validate the workflow and fix any errors on the service.";
		return createPanel(new Object[] { message });
	}

	private JComponent solutionDatatypeSource(VisitReport vr) {
		String sinkPortName = (String) vr.getProperty("sinkPortName");
		if (sinkPortName == null) {
			return null;
		}
		String removeMessage = "1. Remove the link to " + "port \""
				+ sinkPortName + "\"";
		ProcessorInputPort pip = (ProcessorInputPort) vr
				.getProperty("sinkPort");
		final InputPortTypeDescriptorActivity a = (InputPortTypeDescriptorActivity) vr
				.getProperty("activity");
		String addSplitterMessage = "2. Add an XML splitter for " + "port \""
				+ sinkPortName + "\"";
		JButton button = null;
		if (pip != null) {
			Datalink incomingLink = pip.getIncomingLink();
			if (incomingLink != null) {
				button = new JButton();
				final Dataflow d = fileManager.getCurrentDataflow();
				final String portName = sinkPortName;
				final Datalink link = incomingLink;
				button.setAction(new AbstractAction(
						"Remove link and add XML splitter") {
					public void actionPerformed(ActionEvent e) {
						Edit removeLinkEdit = Tools
								.getDisconnectDatalinkAndRemovePortsEdit(link, editManager.getEdits());
						Edit addXMLEdit = new AddXMLSplitterEdit(d,
								(Activity<?>) a, portName, true, editManager.getEdits());
						List<Edit<?>> editList = Arrays.asList(new Edit<?>[] {
								removeLinkEdit, addXMLEdit });
						CompoundEdit ce = new CompoundEdit(editList);
						try {
							editManager.doDataflowEdit(d, ce);
						} catch (EditException ex) {
							logger.error("Could not perform edit", ex);
						}
					}
				});
			}
		}
		String addConnectionMessage = "3. Make a connection to the relevant port of the new XML splitter.";
		return createPanel(new Object[] { removeMessage, addSplitterMessage,
				button, addConnectionMessage });
	}

	private static JComponent solutionUnrecognized(VisitReport vr) {
		String message = "Please contact the workflow creator to find out what additional plugins, if any, need to be installed in Taverna.";
		return createPanel(new Object[] { message });
	}

	private static JComponent solutionLoopConnection(VisitReport vr) {
		return createPanel(new Object[] { "Connect port \""
				+ vr.getProperty("portname") + "\"" });
	}

	private static JComponent solutionUnmanagedLocation(VisitReport vr) {
		JButton button = new JButton();
		Processor p = (Processor) (vr.getSubject());
		button.setAction(new ReportViewConfigureAction(p));
		button.setText("Configure " + p.getLocalName());
		return createPanel(new Object[] {
				"Change the run locaton of the service", button });
	}

	private JComponent solutionIncompatibleMimetypes(VisitReport vr) {
		JButton sinkButton = new JButton();
		Processor sinkProcessor = (Processor) (vr.getProperty("sinkProcessor"));
		Processor sourceProcessor = (Processor) vr
				.getProperty("sourceProcessor");
		sinkButton.setAction(new ReportViewConfigureAction(sinkProcessor));
		sinkButton.setText("Configure " + sinkProcessor.getLocalName());
		JButton sourceButton = new JButton();
		sourceButton.setAction(new ReportViewConfigureAction(sourceProcessor));
		sourceButton.setText("Configure " + sourceProcessor.getLocalName());

		JButton deleteLinkButton = new JButton();
		final Datalink link = (Datalink) vr.getProperty("link");
		final Dataflow d = fileManager.getCurrentDataflow();
		deleteLinkButton.setAction(new AbstractAction("Remove link") {
			public void actionPerformed(ActionEvent e) {
				Edit removeLinkEdit = Tools
						.getDisconnectDatalinkAndRemovePortsEdit(link, editManager.getEdits());
				try {
					editManager.doDataflowEdit(d, removeLinkEdit);
				} catch (EditException ex) {
					logger.error("Could not perform edit", ex);
				}
			}
		});

		return createPanel(new Object[] {
				"Change the source or destination mimetype or remove the link",
				sourceButton, sinkButton, deleteLinkButton });
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
		// gbc.weightx = 0.9;
		// gbc.weighty = 0.9;
		// gbc.gridx = 0;
		// gbc.gridy++;
		// gbc.gridwidth = 2;
		// gbc.fill = GridBagConstraints.BOTH;
		// result.add(new JPanel(), gbc);
		result.setBackground(SystemColor.text);
		return result;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setReportManager(ReportManager reportManager) {
		this.reportManager = reportManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setConfigurationUIFactories(List<ConfigurationUIFactory> configurationUIFactories) {
		this.configurationUIFactories = configurationUIFactories;
	}

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

}
