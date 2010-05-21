/**
 * 
 */
package net.sf.taverna.t2.activities.disabled.views;

import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;
import net.sf.taverna.t2.workflowmodel.OutputPort;

import java.awt.Frame;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

import java.lang.reflect.Method;
import java.lang.IllegalAccessException;
import java.lang.IllegalArgumentException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Action;

/**
 * A DisabledContextualView displays information about a DisabledActivity
 * 
 * @author alanrw
 * 
 */
public class DisabledContextualView extends
		HTMLBasedActivityContextualView<Object> {

	public DisabledContextualView(DisabledActivity activity) {
		super(activity);
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
		return "Disabled service";
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
