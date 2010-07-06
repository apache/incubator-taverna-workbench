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
package net.sf.taverna.t2.workbench.ui.impl;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.zaria.ZBasePane;

import org.apache.log4j.Logger;

/**
 * The default ZBasePane used within the Taverna Workbench
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 */
@SuppressWarnings("serial")
public class WorkbenchZBasePane extends ZBasePane {

	private static Logger logger = Logger
	.getLogger(WorkbenchZBasePane.class);

	public WorkbenchZBasePane() {
		super();
		setKnownSPINames(new String[] { UIComponentFactorySPI.class
				.getCanonicalName() });
	}

	@SuppressWarnings("unchecked")
	@Override
	public JMenuItem getMenuItem(Class theClass) {
		try {
			UIComponentFactorySPI factory = (UIComponentFactorySPI) theClass
					.newInstance();
			Icon icon = factory.getIcon();
			if (icon != null) {
				return new JMenuItem(factory.getName(), factory.getIcon());
			} else {
				return new JMenuItem(factory.getName());
			}
		} catch (InstantiationException e) {
			return new JMenuItem("Instantiation exception!");
		} catch (IllegalAccessException e) {
			return new JMenuItem("Illegal access exception!");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JComponent getComponent(Class theClass) {
		UIComponentFactorySPI factory;
		try {
			factory = (UIComponentFactorySPI) theClass.newInstance();
			return (JComponent) factory.getComponent();
		} catch (InstantiationException e) {
			logger.error("Unable to create component", e);
		} catch (IllegalAccessException e) {
			logger.error("Unable to create component", e);
		}
		return new JPanel();
	}

	public void discard() {

	}

}
