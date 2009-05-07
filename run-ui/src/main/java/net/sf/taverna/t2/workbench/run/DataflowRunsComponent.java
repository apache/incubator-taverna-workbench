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
package net.sf.taverna.t2.workbench.run;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.WriteQueueAspect;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.provenance.ProvenanceConfiguration;
import net.sf.taverna.t2.workbench.reference.config.ReferenceConfiguration;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class DataflowRunsComponent extends JSplitPane implements UIComponentSPI {

	private static final long serialVersionUID = 1L;
	
	private static DataflowRunsComponent singletonInstance;
	
	private ReferenceService referenceService;
	
	private String referenceContext;
	
	private DefaultListModel runListModel;
	
	private JList runList;
	
	private JButton removeWorkflowRunsButton;
	
	private JSplitPane topPanel;
	
	private DataflowRunsComponent() {
		super(JSplitPane.VERTICAL_SPLIT);
		setDividerLocation(400);
		
		topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		topPanel.setDividerLocation(240);
		topPanel.setBorder(null);
		setTopComponent(topPanel);
		
		runListModel = new DefaultListModel();
		runList = new JList(runListModel);
		runList.setBorder(new EmptyBorder(5,5,5,5));
		runList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		runList.setSelectedIndex(0);
		runList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object selection = runList.getSelectedValue();
					
					int location = getDividerLocation();

					if (selection == null){ // there is no workflow items in the list

						JPanel tempMonitorPanel = new JPanel(new BorderLayout());
						tempMonitorPanel.setBorder(LineBorder.createGrayLineBorder());
						tempMonitorPanel.setBackground(Color.WHITE);
						tempMonitorPanel.add(new JLabel("No workflows runs available", JLabel.CENTER), BorderLayout.CENTER);
						topPanel.setBottomComponent(tempMonitorPanel);
						
						JPanel tempResultsPanel = new JPanel(new BorderLayout());
						tempResultsPanel.setBackground(Color.WHITE);
						tempResultsPanel.add(new JLabel("Results"), BorderLayout.NORTH);
						tempResultsPanel.add(new JLabel("No results available", JLabel.CENTER), BorderLayout.CENTER);
						setBottomComponent(tempResultsPanel);
						
						removeWorkflowRunsButton.setEnabled(false);
						setDividerLocation(location);
						revalidate();
					}
					else if (selection instanceof DataflowRun) {

						DataflowRun dataflowRun = (DataflowRun) selection;
						topPanel.setBottomComponent(dataflowRun.getMonitorViewComponent());
						setBottomComponent(dataflowRun.getResultsComponent());
						setDividerLocation(location);
						removeWorkflowRunsButton.setEnabled(true);
						revalidate();
					}
				}
			}
		});
		
		JPanel runListPanel = new JPanel(new BorderLayout());
		runListPanel.setBorder(LineBorder.createGrayLineBorder());
		
		JLabel worklflowRunsLabel = new JLabel("Workflow Runs");
		worklflowRunsLabel.setBorder(new EmptyBorder(5,5,5,5));
		worklflowRunsLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		removeWorkflowRunsButton = new JButton("Remove"); // button to remove previous workflow runs
		removeWorkflowRunsButton.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
		removeWorkflowRunsButton.setEnabled(false);
		removeWorkflowRunsButton.setToolTipText("Remove workflow run(s)");
		removeWorkflowRunsButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int[] selected = runList.getSelectedIndices();
				for (int i = selected.length - 1; i >=0; i--){
					runListModel.remove(selected[i]);
				}
				// Set the first item as selected - if there is one
				if (runListModel.size() > 0){
					runList.setSelectedIndex(0);
				}
			}
		});
		JPanel runListTopPanel = new JPanel();
		runListTopPanel.setLayout(new BorderLayout());
		runListTopPanel.add(worklflowRunsLabel, BorderLayout.WEST);
		runListTopPanel.add(removeWorkflowRunsButton, BorderLayout.EAST);
		
		JPanel runListWithHintTopPanel = new JPanel();
		runListWithHintTopPanel.setLayout(new BorderLayout());
		runListWithHintTopPanel.add(runListTopPanel, BorderLayout.NORTH);
		
		JPanel hintsPanel = new JPanel();
		hintsPanel.setLayout(new BorderLayout());
		hintsPanel.add(new JLabel("Click on a run to see its results"), BorderLayout.NORTH);
		if (ProvenanceConfiguration.getInstance().getProperty("enabled")
				.equalsIgnoreCase("yes")) {
			hintsPanel.add(new JLabel("Click on a service in the diagram"),
					BorderLayout.CENTER);
		}
		else {
			hintsPanel.add(new JLabel("Enable provenance under preferences"), BorderLayout.CENTER);
		}
		hintsPanel.add(new JLabel("to see intermediate results"),
				BorderLayout.SOUTH);
		runListWithHintTopPanel.add(hintsPanel, BorderLayout.SOUTH);
		
		runListPanel.add(runListWithHintTopPanel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(runList);
		scrollPane.setBorder(null);
		runListPanel.add(scrollPane, BorderLayout.CENTER);		

		topPanel.setTopComponent(runListPanel);
		
		JPanel tempMonitorPanel = new JPanel(new BorderLayout());
		tempMonitorPanel.setBorder(LineBorder.createGrayLineBorder());
		tempMonitorPanel.setBackground(Color.WHITE);
		tempMonitorPanel.add(new JLabel("No workflows have been run yet", JLabel.CENTER), BorderLayout.CENTER);
		topPanel.setBottomComponent(tempMonitorPanel);
				
		JPanel tempResultsPanel = new JPanel(new BorderLayout());
		tempResultsPanel.setBackground(Color.WHITE);
		tempResultsPanel.add(new JLabel("Results"), BorderLayout.NORTH);
		tempResultsPanel.add(new JLabel("No results yet", JLabel.CENTER), BorderLayout.CENTER);
		setBottomComponent(tempResultsPanel);
		
//		revalidate();
//		setDividerLocation(.3);
		
		//force reference service to be constructed now rather than at first workflow run
		getReferenceService();
	}
	
	public static DataflowRunsComponent getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DataflowRunsComponent();
		}
		return singletonInstance;
	}
	
	public ReferenceService getReferenceService() {
		String context = ReferenceConfiguration.getInstance().getProperty(
				ReferenceConfiguration.REFERENCE_SERVICE_CONTEXT);
		if (!context.equals(referenceContext)) {
			referenceContext = context;
			ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
					context);
			referenceService = (ReferenceService) appContext
					.getBean("t2reference.service.referenceService");
			try {
				WriteQueueAspect cache = (WriteQueueAspect) appContext
				.getBean("t2reference.cache.cacheAspect");
				ReferenceServiceShutdown.setReferenceServiceCache(cache);
			} catch (NoSuchBeanDefinitionException e) {
//				ReferenceServiceShutdown.setReferenceServiceCache(null);
			} catch (ClassCastException e) {
//				ReferenceServiceShutdown.setReferenceServiceCache(null);
			}
		}
		return referenceService;

	}
	
	public void runDataflow(WorkflowInstanceFacade facade, Map<String, T2Reference> inputs) {
		DataflowRun runComponent = new DataflowRun(facade, inputs, new Date());
		runListModel.add(0, runComponent);
		runList.setSelectedIndex(0);
		runComponent.run();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		// TODO Auto-generated method stub
		
	}

}
