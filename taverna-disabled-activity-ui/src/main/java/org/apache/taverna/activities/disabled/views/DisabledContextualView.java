/**
 *
 */
package org.apache.taverna.activities.disabled.views;

import java.awt.Frame;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import org.apache.taverna.activities.disabled.actions.DisabledActivityConfigurationAction;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.port.InputActivityPort;
import org.apache.taverna.scufl2.api.port.OutputActivityPort;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A DisabledContextualView displays information about a DisabledActivity
 *
 * @author alanrw
 * @author David Withers
 */
@SuppressWarnings("serial")
public class DisabledContextualView extends HTMLBasedActivityContextualView {

	private List<String> fieldNames;

	private final EditManager editManager;
	private final FileManager fileManager;
	private final ReportManager reportManager;
	private final ActivityIconManager activityIconManager;
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	public DisabledContextualView(Activity activity, EditManager editManager,
			FileManager fileManager, ReportManager reportManager, ColourManager colourManager,
			ActivityIconManager activityIconManager, ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super(activity, colourManager);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.reportManager = reportManager;
		this.activityIconManager = activityIconManager;
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	/**
	 * The table for the DisabledActivity shows its ports and the information within the offline
	 * Activity's configuration.
	 *
	 * @return
	 */
	@Override
	protected String getRawTableRowsHtml() {
		StringBuilder html = new StringBuilder();
		html.append("<tr><th>Input Port Name</th><th>Depth</th></tr>");
		for (InputActivityPort inputActivityPort : getActivity().getInputPorts()) {
			html.append("<tr><td>" + inputActivityPort.getName() + "</td><td>");
			html.append(inputActivityPort.getDepth() + "</td></tr>");
		}
		html.append("<tr><th>Output Port Name</th><th>Depth</th></tr>");
		for (OutputActivityPort outputActivityPort : getActivity().getOutputPorts()) {
			html.append("<tr><td>" + outputActivityPort.getName() + "</td><td>");
			html.append(outputActivityPort.getDepth() + "</td></tr>");
		}

		JsonNode config = getConfigBean().getJson();
		try {
			html.append("<tr><th>Property Name</th><th>Property Value</th></tr>");
			BeanInfo beanInfo = Introspector.getBeanInfo(config.getClass());
			for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
				Method readMethod = pd.getReadMethod();
				if ((readMethod != null) && !(pd.getName().equals("class"))) {
					try {
						html.append("<tr><td>");
						html.append(pd.getName());
						html.append("</td><td>");
						html.append(readMethod.invoke(config));
						html.append("</td></tr>");
						if (fieldNames == null) {
							fieldNames = new ArrayList<String>();
						}
						fieldNames.add(pd.getName());
					} catch (IllegalAccessException ex) {
						// ignore
					} catch (IllegalArgumentException ex) {
						// ignore
					} catch (InvocationTargetException ex) {
						// ignore
					}
				}
			}
		} catch (IntrospectionException e) {
			// ignore
		}
		return html.toString();
	}

	@Override
	public String getViewTitle() {
		return "Unavailable service";
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new DisabledActivityConfigurationAction(getActivity(), owner,
				editManager, fileManager, reportManager, activityIconManager, serviceDescriptionRegistry);
	}

}
