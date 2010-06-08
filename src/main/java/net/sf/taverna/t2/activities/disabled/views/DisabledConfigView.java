/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.activities.disabled.views;

import javax.help.CSH;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;

import net.sf.taverna.t2.lang.uibuilder.UIBuilder;

import com.thoughtworks.xstream.XStream;

public class DisabledConfigView extends ActivityConfigurationPanel<DisabledActivity, ActivityAndBeanWrapper> {

	private ActivityAndBeanWrapper configuration;
    private DisabledActivity activity;
    private List<String> fieldNames;

    private Object clonedConfig = null;
    String origConfigXML = "";

	public DisabledConfigView(DisabledActivity activity) {
		this.activity = activity;
		setLayout(new BorderLayout());
		fieldNames = null;
		initialise();
	}

	private void initialise() {
		CSH.setHelpIDString(
				    this,
				    "net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.DisabledConfigView");
		configuration = activity.getConfiguration();
		XStream xstream = new XStream();
		Activity a = configuration.getActivity();
		xstream.setClassLoader(a.getClass().getClassLoader());
		Object origConfig = configuration.getBean();
		if (fieldNames == null) {
		    fieldNames = getFieldNames(origConfig);
		}
		origConfigXML = xstream.toXML(origConfig);
		clonedConfig  = xstream.fromXML(origConfigXML);
		JPanel panel = UIBuilder.buildEditor(clonedConfig, (String[]) fieldNames.toArray(new String[0]));
		this.add(panel, BorderLayout.CENTER);
		this.revalidate();
	}

	@Override
	public void refreshConfiguration() {
	    this.removeAll();
	    initialise();
	}

	public boolean checkValues() {
	    boolean result = false;
		result = activity.configurationWouldWork(clonedConfig);
		if (!result) {
		    JOptionPane.showMessageDialog(
						  this,
						  "The new properties are invalid or not consistent with the workflow",
						  "Invalid properties", JOptionPane.WARNING_MESSAGE);
		}
	    return result;
	}

    public void noteConfiguration() {
	if (isConfigurationChanged()) {
	    ActivityAndBeanWrapper newConfig = new ActivityAndBeanWrapper();
	    newConfig.setActivity(configuration.getActivity());
	    newConfig.setBean(clonedConfig);
	    configuration = newConfig;

	    XStream xstream = new XStream();
	    xstream.setClassLoader(configuration.getActivity().getClass().getClassLoader());
	    
	    origConfigXML = xstream.toXML(clonedConfig);
	}
    }

    @Override
	public ActivityAndBeanWrapper getConfiguration() {
	return configuration;
    }

    public boolean isConfigurationChanged() {
	XStream xstream = new XStream();
	xstream.setClassLoader(configuration.getActivity().getClass().getClassLoader());
	return (!xstream.toXML(clonedConfig).equals(origConfigXML));
    }

    private List<String> getFieldNames(Object config) {
	List<String> result = new ArrayList<String>();
	try {
	    BeanInfo beanInfo = Introspector.getBeanInfo(config.getClass());
	    for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
		Method readMethod = pd.getReadMethod();
		if ((readMethod != null) && !(pd.getName().equals("class"))) {
		    try {
			result.add(pd.getName());
		    } catch (IllegalArgumentException ex) {
			// ignore
		    }
		}
	    }
	} catch (IntrospectionException e) {
	    // ignore
	}
	return result;
    }
}
