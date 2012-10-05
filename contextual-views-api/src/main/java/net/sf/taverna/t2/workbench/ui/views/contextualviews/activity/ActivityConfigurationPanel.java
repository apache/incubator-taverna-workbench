/**
 *
 */
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import javax.swing.JPanel;

import uk.org.taverna.scufl2.api.configurations.Configuration;

import com.thoughtworks.xstream.XStream;

/**
 * @author alanrw
 *
 */
public abstract class ActivityConfigurationPanel extends JPanel {

	public abstract boolean isConfigurationChanged();

	public abstract Configuration getConfiguration();

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

    public void whenOpened() {
    }

	public void whenClosed() {
	}
}
