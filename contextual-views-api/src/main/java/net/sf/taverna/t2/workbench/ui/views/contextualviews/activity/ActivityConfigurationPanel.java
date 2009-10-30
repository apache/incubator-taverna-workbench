/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import javax.swing.JPanel;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import com.thoughtworks.xstream.XStream;

/**
 * @author alanrw
 *
 */
public abstract class ActivityConfigurationPanel<A extends Activity, B extends Object> extends JPanel {
	
	public abstract boolean isConfigurationChanged();

	public abstract B getConfiguration();
	
	public abstract void noteConfiguration();

	protected String convertBeanToString(Object bean) {
		XStream xstream = new XStream();
		xstream.setClassLoader(bean.getClass().getClassLoader());
		return xstream.toXML(bean);
	}
	
	protected Object cloneBean(Object bean) {
		XStream xstream = new XStream();
		xstream.setClassLoader(bean.getClass().getClassLoader());
		return (xstream.fromXML(xstream.toXML(bean)));
	}

	public abstract void refreshConfiguration();
	
	public abstract boolean checkValues();

}
