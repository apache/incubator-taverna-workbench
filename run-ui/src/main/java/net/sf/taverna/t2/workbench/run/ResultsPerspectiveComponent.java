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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade.State;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.run.cleanup.DatabaseCleanup;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.graph.menu.ResetDiagramAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomInAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomOutAction;
import net.sf.taverna.t2.workbench.views.monitor.MonitorViewComponent;
import net.sf.taverna.t2.workbench.views.monitor.graph.MonitorGraphComponent;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.WorkflowRunProgressMonitor;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;

import org.apache.log4j.Logger;

/**
 * Component for keeping and showing workflow runs and workflow
 * final/intermediate results as well as workflow run progress report.
 * <p>
 * <b> FIXME: </b> This class performs a double-role as the GUI component and
 * for keeping and tidying up in the actual runs. Running, keeping runs, results
 * and previous runs should be done in a separate non-GUI module.
 */
public class ResultsPerspectiveComponent extends JSplitPane implements UIComponentSPI {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static Logger logger = Logger.getLogger(ResultsPerspectiveComponent.class);

	// Current Reference Service
	private ReferenceService referenceService;

	// Reference Service using the database storage,
	// needed for previous runs that are always store in the database
	private ReferenceService referenceServiceWithDatabase;

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

	private EditManager editManager;

	private FileManager fileManager;

	private MenuManager menuManager;

	private DataflowSelectionManager dataflowSelectionManager;

	private XMLDeserializer xmlDeserializer;

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
		workflowRunsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// workflowRunList.setSelectedIndex(0);
		workflowRunsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object selection = workflowRunsList.getSelectedValue();

					int location = getDividerLocation();

					if (selection == null) {
						// there is no workflow items in the list
						ResetDiagramAction.setResultsAction(null);
						ZoomInAction.setResultsAction(null);
						ZoomOutAction.setResultsAction(null);

						JPanel tempMonitorPanel = new JPanel(new BorderLayout());
						tempMonitorPanel.setBorder(LineBorder.createGrayLineBorder());
						tempMonitorPanel.setBackground(Color.WHITE);
						tempMonitorPanel.add(new JLabel("No workflows runs available",
								JLabel.CENTER), BorderLayout.CENTER);

						JPanel tempMonitorPanel2 = new JPanel(new BorderLayout());
						tempMonitorPanel2.setBorder(LineBorder.createGrayLineBorder());
						tempMonitorPanel2.setBackground(Color.WHITE);
						tempMonitorPanel2.add(new JLabel("No workflows runs available",
								JLabel.CENTER), BorderLayout.CENTER);

						JTabbedPane monitorComponent = new JTabbedPane();
						monitorComponent.add("Graph", tempMonitorPanel); // graph
						monitorComponent.add("Progress report", tempMonitorPanel2); // progress
																					// report
						topPanel.setBottomComponent(monitorComponent);

						JPanel tempResultsPanel = new JPanel(new BorderLayout());
						tempResultsPanel.setBackground(Color.WHITE);
						tempResultsPanel.add(new JLabel("Results"), BorderLayout.NORTH);
						tempResultsPanel.add(new JLabel("No results available", JLabel.CENTER),
								BorderLayout.CENTER);
						setBottomComponent(tempResultsPanel);

						removeWorkflowRunsButton.setEnabled(false);
						setDividerLocation(location);
						revalidate();
					} else if (selection instanceof WorkflowRun) {
						final WorkflowRun workflowRun = (WorkflowRun) selection;
						if (!workflowRun.isDataflowLoaded()) {
							JTabbedPane monitorComponent = new JTabbedPane();
							String loadingText = "<html><body>Loading workflow diagram and results for <b>"
									+ workflowRun.getWorkflowName()
									+ "</b> for run "
									+ ISO_8601.format(workflowRun.getDate()) + "</body></html>";
							monitorComponent.add("Graph", new JLabel(loadingText)); // graph
							monitorComponent.add("Progress report", new JLabel(loadingText)); // progress
																								// report
							topPanel.setBottomComponent(monitorComponent);
							setBottomComponent(new JPanel());
							synchronized (this) {
								if (loadPreviousWorkflowRunThread != null) {
									loadPreviousWorkflowRunThread.interrupt();
								}
								loadPreviousWorkflowRunThread = new LoadPreviousWorkflowRunThread(
										workflowRun);
								loadPreviousWorkflowRunThread.start();
							}
						} else if (workflowRun.getDataflow() == null) {
							ResetDiagramAction.setResultsAction(null);
							ZoomInAction.setResultsAction(null);
							ZoomOutAction.setResultsAction(null);

							JTabbedPane monitorComponent = new JTabbedPane();
							String errorText = "Could not load workflow for run "
									+ workflowRun.getRunId();
							monitorComponent.add("Graph", new JLabel(errorText)); // graph
							monitorComponent.add("Progress report", new JLabel(errorText)); // progress
																							// report
							topPanel.setBottomComponent(monitorComponent);
							setBottomComponent(new JPanel());
						} else {
							// Everything OK, dataflow already loaded
							/*
							 * JTabbedPane monitorComponent = new JTabbedPane();
							 * monitorComponent.add("Graph", workflowRun
							 * .getOrCreateMonitorViewComponent()); // graph
							 * JScrollPane scrollPane = new
							 * JScrollPane(workflowRun
							 * .getOrCreateProgressReportComponent(),
							 * JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
							 * JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
							 * monitorComponent.add("Progress report",
							 * scrollPane); // progress report
							 */
							MonitorViewComponent monitorComponent = workflowRun
									.getOrCreateMonitorViewComponent();
							topPanel.setBottomComponent(monitorComponent);
							setBottomComponent(workflowRun.getResultsComponent());

							MonitorGraphComponent monitorGraph = monitorComponent.getMonitorGraph();
							if (monitorGraph != null) {
								ResetDiagramAction.setResultsAction(monitorGraph
										.getResetDiagramAction());
								ZoomInAction.setResultsAction(monitorGraph.getZoomInAction());
								ZoomOutAction.setResultsAction(monitorGraph.getZoomOutAction());
							}
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
								"Confirm workflow run deletion", JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.CANCEL_OPTION) {
					return;
				}

				int[] selectedRunsToDelete = workflowRunsList.getSelectedIndices();
				WorkflowRun nextToSelect = null;
				if (selectedRunsToDelete.length > 0) {
					int lastSelectedIndex = selectedRunsToDelete[selectedRunsToDelete.length - 1];
					if (lastSelectedIndex != workflowRunsListModel.size() - 1) {
						nextToSelect = (WorkflowRun) workflowRunsListModel
								.get(lastSelectedIndex + 1);
					}
				}
				for (int i = 0; i < selectedRunsToDelete.length; i++) {

					WorkflowRun wfRun = ((WorkflowRun) workflowRunsListModel.get(i));

					if (wfRun.getFacade() != null
							&& wfRun.getFacade().getState().equals(State.running)) {
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
					WorkflowInstanceFacade facade = workflowRunToBeDeleted.getFacade();
					if (facade != null) {
						synchronized (facade) {
							if (facade.getState().equals(State.running)) {
								facade.cancelWorkflowRun();
							}
						}
					}
					MonitorGraphComponent mvc = workflowRunToBeDeleted.getMonitorGraphComponent();
					if (mvc != null) {
						mvc.onDispose();
					}
					WorkflowRunProgressMonitor progressRunMonitor = workflowRunToBeDeleted
							.getWorkflowRunProgressMonitor();
					if (progressRunMonitor != null) {
						progressRunMonitor.onDispose();
					}
					DatabaseCleanup.getInstance().scheduleDeleteDataflowRun(workflowRunToBeDeleted,
							true);
				}
				// Set the first item as selected - if there is one
				if (workflowRunsListModel.size() > 0) {
					int selectedIndex = 0;
					if (nextToSelect != null) {
						selectedIndex = workflowRunsListModel.indexOf(nextToSelect);
						if (selectedIndex < 0) {
							selectedIndex = 0;
						}
					}
					workflowRunsList.setSelectedIndex(selectedIndex);
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
		hintsPanel.add(new JLabel("Click on a run to see its values"), BorderLayout.NORTH);
		hintsPanel.add(new JLabel("Click on a service in the diagram"), BorderLayout.CENTER);
		hintsPanel.add(new JLabel("to see intermediate values (if available)"), BorderLayout.SOUTH);
		workflowRunListWithHintTopPanel.add(hintsPanel, BorderLayout.SOUTH);

		workflowRunsListPanel.add(workflowRunListWithHintTopPanel, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(workflowRunsList);
		scrollPane.setBorder(null);
		workflowRunsListPanel.add(scrollPane, BorderLayout.CENTER);

		topPanel.setTopComponent(workflowRunsListPanel);

		JPanel tempMonitorPanel = new JPanel(new BorderLayout());
		tempMonitorPanel.setBorder(LineBorder.createGrayLineBorder());
		tempMonitorPanel.setBackground(Color.WHITE);
		tempMonitorPanel.add(new JLabel("No workflow run selected", JLabel.CENTER),
				BorderLayout.CENTER);
		topPanel.setBottomComponent(tempMonitorPanel);

		JPanel tempResultsPanel = new JPanel(new BorderLayout());
		tempResultsPanel.setBackground(Color.WHITE);
		tempResultsPanel.add(new JLabel("Values"), BorderLayout.NORTH);
		tempResultsPanel.add(new JLabel("No values yet", JLabel.CENTER), BorderLayout.CENTER);
		setBottomComponent(tempResultsPanel);

		// revalidate();
		// setDividerLocation(.3);

		Thread thread = new RetrievePreviousWorkflowRunsThread();
		thread.start();

	}

	@SuppressWarnings("unchecked")
	public ArrayList<WorkflowRun> getPreviousWorkflowRuns() {
		return (ArrayList<WorkflowRun>) Collections.list(workflowRunsListModel.elements());
	}

	protected void retrievePreviousWorkflowRunsFromDatabase() {
		String connectorType = DataManagementConfiguration.getInstance().getConnectorType();
		ProvenanceAccess provenanceAccess = new ProvenanceAccess(connectorType);

		List<net.sf.taverna.t2.provenance.lineageservice.utils.WorkflowRun> allWorkflowRunIDs = provenanceAccess
				.listRuns(null, null);
		// List<WorkflowInstance> allWorkflowRunIDs =
		// provenanceAccess.getAllWorkflowIDs();
		// Collections.reverse(allWorkflowRunIDs);

		for (net.sf.taverna.t2.provenance.lineageservice.utils.WorkflowRun workflowRun : allWorkflowRunIDs) {
			DatabaseCleanup databaseCleanup = DatabaseCleanup.getInstance();
			if (databaseCleanup.isDeletedOrScheduledForDeletion(workflowRun.getWorkflowRunId())) {
				continue;
			}
			if (provenanceAccess.isTopLevelDataflow(workflowRun.getWorkflowId(),
					workflowRun.getWorkflowRunId())) {
				logger.info("retrieved previous run, workflow run id: "
						+ workflowRun.getWorkflowRunId() + " date: " + workflowRun.getTimestamp());
				Timestamp time = Timestamp.valueOf(workflowRun.getTimestamp());
				Date date = new Date(time.getTime());

				// Do Dataflow parsing on selection, simply pass
				// wf-bytes and wf id
				WorkflowRun runComponent = new WorkflowRun(workflowRun.getDataflowBlob(),
						workflowRun.getWorkflowId(), workflowRun.getWorkflowExternalName(), date,
						workflowRun.getWorkflowRunId(), referenceServiceWithDatabase, editManager,
						fileManager, menuManager, dataflowSelectionManager, xmlDeserializer);
				runComponent.setDataSavedInDatabase(true);
				runComponent.setProvenanceEnabledForRun(true);
				workflowRunsListModel.add(workflowRunsListModel.getSize(), runComponent);
			}
		}
	}

	public long getRunListCount() {
		return workflowRunsListModel.size();
	}

	public ReferenceService getReferenceService() {
		return referenceService;
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public synchronized ReferenceService getReferenceServiceWithDatabase() {
		// Force creation of a Ref. Service that uses database regardless of
		// what current context is
		// This Ref. Service will be used for previous wf runs to get
		// intermediate results even if
		// current Ref. Manager uses in-memory store.
		if (referenceServiceWithDatabase == null) {
			String databasecontext = DataManagementConfiguration.HIBERNATE_CONTEXT;
//			ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
//					databasecontext);
//			referenceServiceWithDatabase = (ReferenceService) appContext
//					.getBean("t2reference.service.referenceService");
		}
		return referenceServiceWithDatabase;
	}

	public void runWorkflow(WorkflowInstanceFacade facade, Map<String, T2Reference> inputs) {
		WorkflowRun workflowRun = new WorkflowRun(facade, inputs, new Date(), referenceService, editManager,
				fileManager, menuManager, dataflowSelectionManager, xmlDeserializer);
		workflowRun.setProvenanceEnabledForRun(DataManagementConfiguration.getInstance()
				.isProvenanceEnabled());
		workflowRun.setDataSavedInDatabase(DataManagementConfiguration.getInstance()
				.getProperty(DataManagementConfiguration.IN_MEMORY).equalsIgnoreCase("false"));
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

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setDataflowSelectionManager(DataflowSelectionManager dataflowSelectionManager) {
		this.dataflowSelectionManager = dataflowSelectionManager;
	}

	public void setXmlDeserializer(XMLDeserializer xmlDeserializer) {
		this.xmlDeserializer = xmlDeserializer;
	}

	protected class RetrievePreviousWorkflowRunsThread extends Thread {
		private RetrievePreviousWorkflowRunsThread() {
			super("Retrieving previous workflow runs");
		}

		@Override
		public void run() {
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
				MonitorViewComponent monitorComponent = workflowRun
						.getOrCreateMonitorViewComponent();
				topPanel.setBottomComponent(monitorComponent);
				setBottomComponent(workflowRun.getResultsComponent());
			} else {
				topPanel.setBottomComponent(new JLabel("Could not load workflow for run "
						+ workflowRun.getRunId()));
				setBottomComponent(new JPanel());
			}
		}
	}

	public void setBottomComponent(Component comp) {
		int dividerLocation = this.getDividerLocation();
		int height = this.getHeight();
		if (height < 1) {
			dividerLocation = 400;
		} else {
			int minLocation = height / 20;
			int maxLocation = minLocation * 19;
			if (dividerLocation < minLocation) {
				dividerLocation = minLocation;
			}
			if (dividerLocation > maxLocation) {
				dividerLocation = maxLocation;
			}
		}
		super.setBottomComponent(comp);
		super.setDividerLocation(dividerLocation);
	}
}
