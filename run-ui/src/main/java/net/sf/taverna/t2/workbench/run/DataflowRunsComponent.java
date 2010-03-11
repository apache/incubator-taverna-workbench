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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.lineageservice.utils.WorkflowInstance;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.WriteQueueAspect;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.run.cleanup.DatabaseCleanup;
import net.sf.taverna.t2.workbench.run.cleanup.ReferenceServiceShutdownHook;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.monitor.MonitorViewComponent;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * Component for keeping and showing dataflow runs.
 * <p>
 * <b> FIXME: </b> This class performs a double-role as the GUI component and for
 * keeping and tidying up in the actual runs. Running, keeping runs, results and
 * previous runs should be done in a separate non-GUI module.
 */
public class DataflowRunsComponent extends JSplitPane implements UIComponentSPI {

	private static final long serialVersionUID = 1L;

	static Logger logger = Logger
			.getLogger(DataflowRunsComponent.class);

	private static DataflowRunsComponent singletonInstance;

	private ReferenceService referenceService;

	private ReferenceService referenceServiceWithDatabase; // for previous runs,
	// we always need
	// the one using
	// database

	private String referenceContext;

	private DefaultListModel runListModel;

	private JList runList;

	private JButton removeWorkflowRunsButton;

	private JSplitPane topPanel;

	protected LoadPreviousWorkflow loadWorkflowRunThread;

	private DataflowRunsComponent() {
		super(JSplitPane.VERTICAL_SPLIT);
		setDividerLocation(400);

		topPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		topPanel.setDividerLocation(275);
		topPanel.setBorder(null);
		setTopComponent(topPanel);

		runListModel = new DefaultListModel();
		runList = new JList(runListModel);
		runList.setBorder(new EmptyBorder(5, 5, 5, 5));
		runList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// runList.setSelectedIndex(0);
		runList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object selection = runList.getSelectedValue();

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
						topPanel.setBottomComponent(tempMonitorPanel);

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
					} else if (selection instanceof DataflowRun) {
						final DataflowRun dataflowRun = (DataflowRun) selection;
						if (!dataflowRun.isDataflowLoaded()) {
							topPanel.setBottomComponent(new JLabel(
									"<html>Loading workflow diagram and results for <i>" + dataflowRun.getWorkflowName() +  "</i> for run "
											+ dataflowRun.getDate() + "</html>"));
							setBottomComponent(new JPanel());
							if (loadWorkflowRunThread != null) {
								loadWorkflowRunThread.interrupt();
							}
							loadWorkflowRunThread = new LoadPreviousWorkflow(
									dataflowRun);
							loadWorkflowRunThread.start();
						} else if (dataflowRun.getDataflow() == null) {
							topPanel.setBottomComponent(new JLabel(
									"Could not load workflow for run "
											+ dataflowRun.getRunId()));
							setBottomComponent(new JPanel());
						} else {
							// Everything OK, dataflow already loaded
							topPanel.setBottomComponent(dataflowRun
									.getOrCreateMonitorViewComponent());
							setBottomComponent(dataflowRun
									.getResultsComponent());
						}
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

				int[] selectedRunsToDelete = runList.getSelectedIndices();
				for (int i = 0; i < selectedRunsToDelete.length; i++) {
					if (((DataflowRun) runListModel.get(i)).getDataflow()
							.isRunning()) {
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

				int[] selected = runList.getSelectedIndices();
				for (int i = selected.length - 1; i >= 0; i--) {
					final DataflowRun dataflowRunToBeDeleted = (DataflowRun) runListModel
							.remove(selected[i]);
					MonitorViewComponent mvc = dataflowRunToBeDeleted
							.getMonitorViewComponent();
					if (mvc != null) {
						mvc.onDispose();
					}
					DatabaseCleanup.getInstance().scheduleDeleteDataflowRun(dataflowRunToBeDeleted, true);					
				}
				// Set the first item as selected - if there is one
				if (runListModel.size() > 0) {
					runList.setSelectedIndex(0);
				}
				System.gc();
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
		hintsPanel.add(new JLabel("Click on a run to see its values"),
				BorderLayout.NORTH);
		hintsPanel.add(new JLabel("Click on a service in the diagram"),
				BorderLayout.CENTER);
		hintsPanel.add(new JLabel("to see intermediate values (if available)"),
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

		Thread thread = new RetrievePreviousRunsThread();
		thread.start();

	}

	@SuppressWarnings("unchecked")
	public ArrayList<DataflowRun> getPreviousWFRuns() {
		return (ArrayList<DataflowRun>) Collections.list(runListModel
				.elements());
	}

	protected void retrievePreviousRuns() {
		String connectorType = DataManagementConfiguration.getInstance()
				.getConnectorType();
		ProvenanceAccess provenanceAccess = new ProvenanceAccess(connectorType);

		List<WorkflowInstance> allWorkflowRunIDs = provenanceAccess.listRuns(
				null, null);
		// List<WorkflowInstance> allWorkflowRunIDs =
		// provenanceAccess.getAllWorkflowIDs();
		// Collections.reverse(allWorkflowRunIDs);

		for (WorkflowInstance workflowInstance : allWorkflowRunIDs) {
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
				DataflowRun runComponent = new DataflowRun(workflowInstance
						.getDataflowBlob(), workflowInstance
						.getWorkflowIdentifier(), workflowInstance.getWorkflowExternalName(), 
						date, workflowInstance.getInstanceID(), referenceServiceWithDatabase);
				runComponent.setDataSavedInDatabase(true);
				runComponent.setProvenanceEnabledForRun(true);
				runListModel.add(runListModel.getSize(), runComponent);
			}
		}
	}

	public static DataflowRunsComponent getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DataflowRunsComponent();
		}
		return singletonInstance;
	}

	public long getRunListCount() {
		return runListModel.size();
	}

	public ReferenceService getReferenceService() {
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

	public ReferenceService getReferenceServiceWithDatabase() {
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

	public void runDataflow(WorkflowInstanceFacade facade,
			Map<String, T2Reference> inputs) {
		DataflowRun runComponent = new DataflowRun(facade, inputs, new Date(),
				referenceService);
		runComponent.setProvenanceEnabledForRun(DataManagementConfiguration
				.getInstance().isProvenanceEnabled());
		runComponent.setDataSavedInDatabase(DataManagementConfiguration
				.getInstance().getProperty(
						DataManagementConfiguration.IN_MEMORY)
				.equalsIgnoreCase("false"));
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

	protected class RetrievePreviousRunsThread extends Thread {
		private RetrievePreviousRunsThread() {
			super("Retrieving previous runs");
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
			retrievePreviousRuns();
		}
	}

	/**
	 * Load a workflow from database (in separate thread, as this involves
	 * parsing the workflow definition)
	 * 
	 */
	protected class LoadPreviousWorkflow extends Thread {
		private final DataflowRun dataflowRun;

		private LoadPreviousWorkflow(DataflowRun dataflowRun) {
			super("Loading workflow " + dataflowRun.getRunId());
			this.dataflowRun = dataflowRun;
		}

		public void run() {
			if (isInterrupted()) {
				return;
			}
			// Load Dataflow
			dataflowRun.getDataflow();
			if (isInterrupted()) {
				return;
			}
			// Prepare GUI
			dataflowRun.getOrCreateMonitorViewComponent();
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
			if (runList.getSelectedValue() != dataflowRun) {
				// User changed selection meanwhile we loaded
				return;
			}
			if (dataflowRun.getDataflow() != null) {
				topPanel.setBottomComponent(dataflowRun
						.getOrCreateMonitorViewComponent());
				setBottomComponent(dataflowRun.getResultsComponent());
			} else {
				topPanel.setBottomComponent(new JLabel(
						"Could not load workflow for run "
								+ dataflowRun.getRunId()));
				setBottomComponent(new JPanel());
			}
		}
	}
}
