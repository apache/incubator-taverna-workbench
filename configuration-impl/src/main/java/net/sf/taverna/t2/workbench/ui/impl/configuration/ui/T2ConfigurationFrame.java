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
package net.sf.taverna.t2.workbench.ui.impl.configuration.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIRegistry;
import net.sf.taverna.t2.workbench.helper.HelpCollator;
import net.sf.taverna.t2.workbench.helper.Helper;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class T2ConfigurationFrame extends JFrame {
	
	private static Logger logger = Logger.getLogger(T2ConfigurationFrame.class);
	
	private static final int FRAME_WIDTH = 700;
	private static final int FRAME_HEIGHT = 400;
	
	private static JSplitPane splitPane;
		
	public static T2ConfigurationFrame showFrame() {
		return new T2ConfigurationFrame();
	}
	
	public T2ConfigurationFrame () {
		Helper.setKeyCatcher(this);
		HelpCollator.registerComponent(this);
		
		setLayout(new BorderLayout());
		
		// Split pane to hold list of properties (on the left) and their configurable options (on the right)
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new EtchedBorder()));
	
		JList list = getConfigurationList();
		JScrollPane jspList = new JScrollPane(list);
		jspList.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(5,5,5,5)));
		jspList.setMinimumSize(new Dimension(120, 380));

		splitPane.setLeftComponent(jspList);
		splitPane.setRightComponent(new JPanel());
		splitPane.setDividerSize(0);
		
		//select first item if one exists
		if (list.getModel().getSize()>0) {
			list.setSelectedValue(list.getModel().getElementAt(0), true);
		}

		add(splitPane);
		
		pack();
		setSize(new Dimension(FRAME_WIDTH,FRAME_HEIGHT));
		setVisible(true);
	}
	
	private JList getConfigurationList() {
		final JList list = new JList();
			
		DefaultListModel listModel = new DefaultListModel();
		list.setModel(listModel);
		list.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (list.getSelectedValue() instanceof ConfigurableItem) {
					ConfigurableItem item = (ConfigurableItem)list.getSelectedValue();
					setMainPanel(item.getPanel());
				}
				
				// Keep the split pane's divider at its current position - but looks ugly
				// The problem with divider moving from its current position after selecting an 
				// item from the list on the left is that the right hand side panels are loaded dynamically
				// and it seems there is nothing we can do about it - it's just the JSplitPane's behaviour
				//splitPane.setDividerLocation(splitPane.getLastDividerLocation());
			
			}
		});
		
		for (ConfigurationUIFactory fac : ConfigurationUIRegistry.getInstance().getConfigurationUIFactories()) {
			String name=fac.getConfigurable().getName();
			if (name!=null) {
				logger.info("Adding configurable for name:"+name);
				listModel.addElement(new ConfigurableItem(fac));
			}
			else {
				logger.warn("The configurable "+fac.getConfigurable().getClass()+" has a null name");
			}
		}
		
		return list;
	}
	
	private void setMainPanel(JPanel panel) {
		panel.setBorder(new EmptyBorder(15,15,15,15));
		splitPane.setRightComponent(panel);
	}
	
	class ConfigurableItem {
		

		private final ConfigurationUIFactory factory;

		public ConfigurableItem(ConfigurationUIFactory factory) {
			this.factory = factory;
			
		}

		public JPanel getPanel() {
			return factory.getConfigurationPanel();
		}
		
		@Override
		public String toString() {
			return factory.getConfigurable().getName();
		}
		
	}
}
