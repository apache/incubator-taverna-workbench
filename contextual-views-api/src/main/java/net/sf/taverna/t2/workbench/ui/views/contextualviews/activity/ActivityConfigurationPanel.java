/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import javax.swing.JPanel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

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
		return xstream.toXML(bean);
	}
	
	protected Object cloneBean(Object bean) {
		XStream xstream = new XStream();
		return (xstream.fromXML(xstream.toXML(bean)));
	}

	public abstract void refreshConfiguration();
	
	public abstract boolean checkValues();

}
