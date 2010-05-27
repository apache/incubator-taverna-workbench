/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester   
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.WriteQueueAspect;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.run.cleanup.DatabaseCleanup;
import net.sf.taverna.t2.workbench.run.cleanup.ReferenceServiceShutdownHook;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.monitor.MonitorViewComponent;
import net.sf.taverna.t2.workbench.views.monitor.graph.MonitorGraphComponent;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.WorkflowRunProgressMonitor;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * Component for keeping and showing workflow runs and 
 * workflow final/intermediate results as well as workflow run progress report.
 * <p>
 * <b> FIXME: </b> This class performs a double-role as the GUI component and for
 * keeping and tidying up in the actual runs. Running, keeping runs, results and
 * previous runs should be done in a separate non-GUI module.
 */
public class ResultsPerspectiveComponent extends JSplitPane implements UIComponentSPI {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger
			.getLogger(ResultsPerspectiveComponent.class);

	// Current Reference Service
	private ReferenceService referenceService;

	// Reference Service using the database storage, 
	// needed for previous runs that are always store in the database
	private ReferenceService referenceServiceWithDatabase;

	// Current ReferenceContext
	private String referenceContext;

	// List model for previous workflow runs
	private DefaultListModel workflowRunsListModel;

	// Multi-selection list of previous workflow runs
	private JList workflowRunsList;

	// A button to remove previous runs
	private JButton removeWorkflowRunsButton;

	// A split pane containing previous runs and workflow run progress 
	// components (graph and progress report) 
	private JSplitPane topPanel;
	
	// Background thread for loading a previous workflow run
	protected LoadPreviousWorkflowRunThread loadPreviousWorkflowRunThread;



	private static class Singleton {
		private static ResultsPerspectiveComponent INSTANCE = new ResultsPerspectiveComponent();
	}
	
	public static ResultsPerspectiveComponent getInstance() {
		return Singleton.INSTANCE;
	}
	
	protected ResultsPerspectiveComponent() {
		super(JSplitPane.VERTICAL_SPLIT);
		setDividerLocation(400);

		topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		topPanel.setDividerLocation(275);
		topPanel.setBorder(null);
		setTopComponent(topPanel);

		workflowRunsListModel = new DefaultListModel();
		workflowRunsList = new JList(workflowRunsListModel);
		workflowRunsList.setBorder(new EmptyBorder(5, 5, 5, 5));
		workflowRunsList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// workflowRunList.setSelectedIndex(0);
		workflowRunsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object selection = workflowRunsList.getSelectedValue();

					int location = getDividerLocation();

					if (selection == null) { // there is no workflow items in
						// the list

						JPanel tempMonitorPanel = new JPanel(new BorderLayout());
						tempMonitorPanel.setBorder(LineBorder
								.createGrayLineBorder());
						tempMonitorPanel.setBackground(Color.WHITE);
						tempMonitorPanel.add(new JLabel(
								"No workflows runs available", JLabel.CENTER),
								BorderLayout.CENTER);
						
						JPanel tempMonitorPanel2 = new JPanel(new BorderLayout());
						tempMonitorPanel2.setBorder(LineBorder
								.createGrayLineBorder());
						tempMonitorPanel2.setBackground(Color.WHITE);
						tempMonitorPanel2.add(new JLabel(
								"No workflows runs available", JLabel.CENTER),
								BorderLayout.CENTER);
						
						JTabbedPane monitorComponent = new JTabbedPane();
						monitorComponent.add("Graph",tempMonitorPanel); // graph
						monitorComponent.add("Progress report", tempMonitorPanel2); // progress report	
						topPanel.setBottomComponent(monitorComponent);
						
						JPanel tempResultsPanel = new JPanel(new BorderLayout());
						tempResultsPanel.setBackground(Color.WHITE);
						tempResultsPanel.add(new JLabel("Results"),
								BorderLayout.NORTH);
						tempResultsPanel.add(new JLabel("No results available",
								JLabel.CENTER), BorderLayout.CENTER);
						setBottomComponent(tempResultsPanel);

						removeWorkflowRunsButton.setEnabled(false);
						setDividerLocation(location);
						revalidate();
					} else if (selection instanceof WorkflowRun) {
						final WorkflowRun workflowRun = (WorkflowRun) selection;
						if (!workflowRun.isDataflowLoaded()) {
							JTabbedPane monitorComponent = new JTabbedPane();
							monitorComponent.add("Graph", new JLabel(
									"<html>Loading workflow diagram and results for <i>" + workflowRun.getWorkflowName() +  "</i> for run "
									+ workflowRun.getDate() + "</html>")); // graph
							monitorComponent.add("Progress report", new JLabel(
									"<html>Loading workflow diagram and results for <i>" + workflowRun.getWorkflowName() +  "</i> for run "
									+ workflowRun.getDate() + "</html>")); // progress report	
							topPanel.setBottomComponent(monitorComponent);
							setBottomComponent(new JPanel());
							synchronized(this) {
								if (loadPreviousWorkflowRunThread != null) {
									loadPreviousWorkflowRunThread.interrupt();
								}
								loadPreviousWorkflowRunThread = new LoadPreviousWorkflowRunThread(
										workflowRun);
								loadPreviousWorkflowRunThread.start();
							}
						} else if (workflowRun.getDataflow() == null) {					
							JTabbedPane monitorComponent = new JTabbedPane();
							monitorComponent.add("Graph", new JLabel(
									"Could not load workflow for run "
									+ workflowRun.getRunId())); // graph
							monitorComponent.add("Progress report", new JLabel(
									"Could not load workflow for run "
									+ workflowRun.getRunId())); // progress report	
							topPanel.setBottomComponent(monitorComponent);	
							setBottomComponent(new JPanel());
						} else {
							// Everything OK, dataflow already loaded
							/*JTabbedPane monitorComponent = new JTabbedPane();
							monitorComponent.add("Graph", workflowRun
									.getOrCreateMonitorViewComponent()); // graph
							JScrollPane scrollPane = new JScrollPane(workflowRun
									.getOrCreateProgressReportComponent(),
									JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
							monitorComponent.add("Progress report", scrollPane); // progress report	*/
							MonitorViewComponent monitorComponent = workflowRun.getOrCreateMonitorViewComponent();
							topPanel.setBottomComponent(monitorComponent);	
							setBottomComponent(workflowRun
									.getResultsComponent());
						}
						setDividerLocation(location);
						removeWorkflowRunsButton.setEnabled(true);
						revalidate();
					}
				}
			}
		});

		// Panel with previous workflow runs
		JPanel workflowRunsListPanel = new JPanel(new BorderLayout());
		workflowRunsListPanel.setBorder(LineBorder.createGrayLineBorder());

		JLabel worklflowRunsLabel = new JLabel("Workflow runs");
		worklflowRunsLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		worklflowRunsLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		// button to remove previous workflow runs
		removeWorkflowRunsButton = new JButton("Remove");
		removeWorkflowRunsButton.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
		removeWorkflowRunsButton.setEnabled(false);
		removeWorkflowRunsButton.setToolTipText("Remove workflow run(s)");
		removeWorkflowRunsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// Warn user that removing workflow run will
				// cause all provenance data for that run to be deleted
				int option = JOptionPane
						.showConfirmDialog(
								null,
								new JLabel(
										"<html><body>Are you sure you want to delete the selected workflow run(s)?<br>"
												+ "Deleting them will remove all provenance data related to the run(s).</body></html>"),
								"Confirm workflow run deletion",
								JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.CANCEL_OPTION) {
					return;
				}

				int[] selectedRunsToDelete = workflowRunsList.getSelectedIndices();
				for (int i = 0; i < selectedRunsToDelete.length; i++) {
					
					WorkflowRun wfRun = ((WorkflowRun) workflowRunsListModel.get(i));
					
					if (wfRun.getFacade() != null && wfRun.getFacade().isRunning()){
						option = JOptionPane
								.showConfirmDialog(
										null,
										new JLabel(
												"<html><body>Some of the workflow runs you are trying to delete appear not to have finished.<br>Are you sure you want to continue and delete them as well (this is not recommended)?</body></html>"),
										"Confirm unfinished workflow run deletion",
										JOptionPane.WARNING_MESSAGE);
						if (option == JOptionPane.CANCEL_OPTION) {
							return;
						} else {
							// Don't ask again
							break;
						}
					}
				}

				for (int i = selectedRunsToDelete.length - 1; i >= 0; i--) {
					final WorkflowRun workflowRunToBeDeleted = (WorkflowRun) workflowRunsListModel
							.remove(selectedRunsToDelete[i]);
					
					// Stop the workflow run if it still active
					if (workflowRunToBeDeleted.getFacade() != null
							&& workflowRunToBeDeleted.getFacade().isRunning()) {
						workflowRunToBeDeleted.getFacade().cancelWorkflowRun();
					}
					MonitorGraphComponent mvc = workflowRunToBeDeleted
							.getMonitorGraphComponent();
					if (mvc != null) {
						mvc.onDispose();
					}
					WorkflowRunProgressMonitor progressRunMonitor = workflowRunToBeDeleted.getWorkflowRunProgressMonitor();
					if (progressRunMonitor != null) {
						progressRunMonitor.onDispose();
					}
					DatabaseCleanup.getInstance().scheduleDeleteDataflowRun(workflowRunToBeDeleted, true);					
				}
				// Set the first item as selected - if there is one
				if (workflowRunsListModel.size() > 0) {
					workflowRunsList.setSelectedIndex(0);
				}
				System.gc();
			}
		});
		
		JPanel workflowRunListTopPanel = new JPanel();
		workflowRunListTopPanel.setLayout(new BorderLayout());
		workflowRunListTopPanel.add(worklflowRunsLabel, BorderLayout.WEST);
		workflowRunListTopPanel.add(removeWorkflowRunsButton, BorderLayout.EAST);

		JPanel workflowRunListWithHintTopPanel = new JPanel();
		workflowRunListWithHintTopPanel.setLayout(new BorderLayout());
		workflowRunListWithHintTopPanel.add(workflowRunListTopPanel, BorderLayout.NORTH);

		JPanel hintsPanel = new JPanel();
		hintsPanel.setLayout(new BorderLayout());
		hintsPanel.add(new JLabel("Click on a run to see its values"),
				BorderLayout.NORTH);
		hintsPanel.add(new JLabel("Click on a service in the diagram"),
				BorderLayout.CENTER);
		hintsPanel.add(new JLabel("to see intermediate values (if available)"),
				BorderLayout.SOUTH);
		workflowRunListWithHintTopPanel.add(hintsPanel, BorderLayout.SOUTH);

		workflowRunsListPanel.add(workflowRunListWithHintTopPanel, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(workflowRunsList);
		scrollPane.setBorder(null);
		workflowRunsListPanel.add(scrollPane, BorderLayout.CENTER);

		topPanel.setTopComponent(workflowRunsListPanel);

		JPanel tempMonitorPanel = new JPanel(new BorderLayout());
		tempMonitorPanel.setBorder(LineBorder.createGrayLineBorder());
		tempMonitorPanel.setBackground(Color.WHITE);
		tempMonitorPanel.add(new JLabel("No workflow run selected",
				JLabel.CENTER), BorderLayout.CENTER);
		topPanel.setBottomComponent(tempMonitorPanel);

		JPanel tempResultsPanel = new JPanel(new BorderLayout());
		tempResultsPanel.setBackground(Color.WHITE);
		tempResultsPanel.add(new JLabel("Values"), BorderLayout.NORTH);
		tempResultsPanel.add(new JLabel("No values yet", JLabel.CENTER),
				BorderLayout.CENTER);
		setBottomComponent(tempResultsPanel);

		// revalidate();
		// setDividerLocation(.3);

		Thread thread = new RetrievePreviousWorkflowRunsThread();
		thread.start();

	}

	@SuppressWarnings("unchecked")
	public ArrayList<WorkflowRun> getPreviousWorkflowRuns() {
		return (ArrayList<WorkflowRun>) Collections.list(workflowRunsListModel
				.elements());
	}

	protected void retrievePreviousWorkflowRunsFromDatabase() {
		String connectorType = DataManagementConfiguration.getInstance()
				.getConnectorType();
		ProvenanceAccess provenanceAccess = new ProvenanceAccess(connectorType);

		List<net.sf.taverna.t2.provenance.lineageservice.utils.WorkflowRun> allWorkflowRunIDs = provenanceAccess.listRuns(
				null, null);
		// List<WorkflowInstance> allWorkflowRunIDs =
		// provenanceAccess.getAllWorkflowIDs();
		// Collections.reverse(allWorkflowRunIDs);

		for (net.sf.taverna.t2.provenance.lineageservice.utils.WorkflowRun workflowInstance : allWorkflowRunIDs) {
			DatabaseCleanup databaseCleanup = DatabaseCleanup.getInstance();
			if (databaseCleanup.isDeletedOrScheduledForDeletion(
					workflowInstance.getInstanceID())) {
				continue;
			}
			if (provenanceAccess.isTopLevelDataflow(workflowInstance
					.getWorkflowIdentifier())) {
				logger.info("retrieved previous run, workflow id: "
						+ workflowInstance.getInstanceID() + " date: "
						+ workflowInstance.getTimestamp());
				Timestamp time = Timestamp.valueOf(workflowInstance
						.getTimestamp());
				Date date = new Date(time.getTime());

				// Do Dataflow parsing on selection, simply pass
				// wf-bytes and wf id
				WorkflowRun runComponent = new WorkflowRun(workflowInstance
						.getDataflowBlob(), workflowInstance
						.getWorkflowIdentifier(), workflowInstance.getWorkflowExternalName(), 
						date, workflowInstance.getInstanceID(), referenceServiceWithDatabase);
				runComponent.setDataSavedInDatabase(true);
				runComponent.setProvenanceEnabledForRun(true);
				workflowRunsListModel.add(workflowRunsListModel.getSize(), runComponent);
			}
		}
	}

	public long getRunListCount() {
		return workflowRunsListModel.size();
	}

	public synchronized ReferenceService getReferenceService() {
		String context = DataManagementConfiguration.getInstance()
				.getDatabaseContext();
		if (!context.equals(referenceContext)) {
			referenceContext = context;
			ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
					context);
			referenceService = (ReferenceService) appContext
					.getBean("t2reference.service.referenceService");
			try {
				WriteQueueAspect cache = (WriteQueueAspect) appContext
						.getBean("t2reference.cache.cacheAspect");
				ReferenceServiceShutdownHook.setReferenceServiceCache(cache);
			} catch (NoSuchBeanDefinitionException e) {
				// ReferenceServiceShutdown.setReferenceServiceCache(null);
			} catch (ClassCastException e) {
				// ReferenceServiceShutdown.setReferenceServiceCache(null);
			}
		}
		return referenceService;

	}

	public synchronized ReferenceService getReferenceServiceWithDatabase() {
		// Force creation of a Ref. Service that uses database regardless of
		// what current context is
		// This Ref. Service will be used for previous wf runs to get
		// intermediate results even if
		// current Ref. Manager uses in-memory store.
		if (referenceServiceWithDatabase == null) {
			String databasecontext = DataManagementConfiguration.HIBERNATE_CONTEXT;
			ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
					databasecontext);
			referenceServiceWithDatabase = (ReferenceService) appContext
					.getBean("t2reference.service.referenceService");
		}
		return referenceServiceWithDatabase;
	}
	
	public void runWorkflow(WorkflowInstanceFacade facade,
			Map<String, T2Reference> inputs) {
		WorkflowRun workflowRun = new WorkflowRun(facade, inputs, new Date(),
				referenceService);
		workflowRun.setProvenanceEnabledForRun(DataManagementConfiguration
				.getInstance().isProvenanceEnabled());
		workflowRun.setDataSavedInDatabase(DataManagementConfiguration
				.getInstance().getProperty(
						DataManagementConfiguration.IN_MEMORY)
				.equalsIgnoreCase("false"));
		workflowRunsListModel.add(0, workflowRun);
		workflowRunsList.setSelectedIndex(0);
		workflowRun.run();
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

	protected class RetrievePreviousWorkflowRunsThread extends Thread {
		private RetrievePreviousWorkflowRunsThread() {
			super("Retrieving previous workflow runs");
		}

		@Override
		public void run() {
			// force reference service to be constructed now rather than at
			// first
			// workflow run
			getReferenceService();
			getReferenceServiceWithDatabase(); // get the Reference Service
			// with database for
			// previous runs
			retrievePreviousWorkflowRunsFromDatabase();
		}
	}

	/**
	 * Load a workflow run from database (in separate thread, as this involves
	 * parsing the workflow definition).
	 * 
	 */
	protected class LoadPreviousWorkflowRunThread extends Thread {
		private final WorkflowRun workflowRun;

		private LoadPreviousWorkflowRunThread(WorkflowRun dataflowRun) {
			super("Loading workflow " + dataflowRun.getRunId());
			this.workflowRun = dataflowRun;
		}

		public void run() {
			if (isInterrupted()) {
				return;
			}
			// Load Dataflow
			workflowRun.getDataflow();
			if (isInterrupted()) {
				return;
			}
			// Prepare GUI
			workflowRun.getOrCreateMonitorViewComponent();
			
			if (isInterrupted()) {
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateSelection();
				}
			});
		}

		protected void updateSelection() {
			if (workflowRunsList.getSelectedValue() != workflowRun) {
				// User changed selection meanwhile we loaded
				return;
			}
			if (workflowRun.getDataflow() != null) {
				MonitorViewComponent monitorComponent = workflowRun.getOrCreateMonitorViewComponent();
				topPanel.setBottomComponent(monitorComponent);
				setBottomComponent(workflowRun.getResultsComponent());
			} else {
				topPanel.setBottomComponent(new JLabel(
						"Could not load workflow for run "
								+ workflowRun.getRunId()));
				setBottomComponent(new JPanel());
			}
		}
	}
}
