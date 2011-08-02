/**
 *
 */
package net.sf.taverna.t2.activities.disabled.views;

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

import net.sf.taverna.t2.activities.disabled.actions.DisabledActivityConfigurationAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;

import org.apache.log4j.Logger;

/**
 * A DisabledContextualView displays information about a DisabledActivity
 *
 * @author alanrw
 *
 */
public class DisabledContextualView extends
		HTMLBasedActivityContextualView<Object> {

	private static Logger logger = Logger
			.getLogger(DisabledContextualView.class);

    private List<String> fieldNames;

	private final EditManager editManager;
	private final FileManager fileManager;
	private final ReportManager reportManager;

	public DisabledContextualView(DisabledActivity activity, EditManager editManager, FileManager fileManager, ReportManager reportManager) {
		super(activity);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.reportManager = reportManager;
		init();
	}

	private void init() {
	}

	/**
	 * The table for the DisabledActivity shows its ports and the information
	 * within the offline Activity's configuration.
	 *
	 * @return
	 */
	@Override
	protected String getRawTableRowsHtml() {
		String html = "";
		html = html + "<tr><th>Input Port Name</th><th>Port depth</th>"
				+ "</tr>";
		for (ActivityInputPort aip : getActivity().getInputPorts()) {
			html = html + "<tr><td>" + aip.getName() + "</td><td>"
					+ aip.getDepth() + "</td></tr>";
		}
		html = html + "<tr><th>Output Port Name</th><th>Port depth</th>"
				+ "</tr>";
		for (OutputPort aop : getActivity().getOutputPorts()) {
			html = html + "<tr><td>" + aop.getName() + "</td><td>"
					+ aop.getDepth() + "</td></tr>";
		}

		Object config = ((DisabledActivity) getActivity()).getConfiguration()
				.getBean();
		try {
			html += "<tr><th>Property Name</th><th>Property Value</th></tr>";
			BeanInfo beanInfo = Introspector.getBeanInfo(config.getClass());
			for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
				Method readMethod = pd.getReadMethod();
				if ((readMethod != null) && !(pd.getName().equals("class"))) {
					try {
						html += "<tr><td>" + pd.getName() + "</td><td>"
								+ readMethod.invoke(config) + "</td></tr>";
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
		return html;
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
		return new DisabledActivityConfigurationAction(
				(DisabledActivity) getActivity(), owner, editManager, fileManager, reportManager);
	}

}
