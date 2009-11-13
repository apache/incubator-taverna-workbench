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
package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.lineageservice.Dependencies;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.graph.menu.ResetDiagramAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomInAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomOutAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;

public class MonitorViewComponent extends JPanel implements UIComponentSPI {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MonitorViewComponent.class);

	private static final long serialVersionUID = 1L;

	private SVGGraphController graphController;

	protected JSVGCanvas svgCanvas;
	protected JSVGScrollPane svgScrollPane;
	
	protected JLabel statusLabel;
	
	protected ProvenanceConnector provenanceConnector;	

	public enum Status {
		RUNNING, COMPLETE
	}

	private String sessionId;

	protected GVTTreeRendererAdapter gvtTreeRendererAdapter;

	private GraphMonitor graphMonitor;

	public MonitorViewComponent() {
		super(new BorderLayout());
		setBorder(LineBorder.createGrayLineBorder());

		svgCanvas = new JSVGCanvas();
		svgCanvas.setEnableZoomInteractor(false);
		svgCanvas.setEnableRotateInteractor(false);
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);

		gvtTreeRendererAdapter = new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
//				svgScrollPane.reset();
//				MonitorViewComponent.this.revalidate();
			}
		};
		svgCanvas.addGVTTreeRendererListener(gvtTreeRendererAdapter);
		
		JPanel diagramAndControls = new JPanel();
		diagramAndControls.setLayout(new BorderLayout());
		
		svgScrollPane = new MySvgScrollPane(svgCanvas);
		diagramAndControls.add(graphActionsToolbar(), BorderLayout.NORTH);
		diagramAndControls.add(svgScrollPane, BorderLayout.CENTER);
		
		add(diagramAndControls, BorderLayout.CENTER);
		
		statusLabel = new JLabel();
		statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		add(statusLabel, BorderLayout.SOUTH);
		
//		setProvenanceConnector();
	}
	
	protected JToolBar graphActionsToolbar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		toolBar.setFloatable(false);
		
		JButton resetDiagramButton = new JButton();
		resetDiagramButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		JButton zoomInButton = new JButton();
		zoomInButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		JButton zoomOutButton = new JButton();
		zoomOutButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		
		Action resetDiagramAction = svgCanvas.new ResetTransformAction();
		ResetDiagramAction.setResultsAction(resetDiagramAction);
		resetDiagramAction.putValue(Action.SHORT_DESCRIPTION, "Reset Diagram");
		resetDiagramAction.putValue(Action.SMALL_ICON, WorkbenchIcons.refreshIcon);
		resetDiagramButton.setAction(resetDiagramAction);

		Action zoomInAction = svgCanvas.new ZoomAction(1.2);
		ZoomInAction.setResultsAction(zoomInAction);
		zoomInAction.putValue(Action.SHORT_DESCRIPTION, "Zoom In");
		zoomInAction.putValue(Action.SMALL_ICON, WorkbenchIcons.zoomInIcon);
		zoomInButton.setAction(zoomInAction);

		Action zoomOutAction = svgCanvas.new ZoomAction(1/1.2);
		ZoomOutAction.setResultsAction(zoomOutAction);
		zoomOutAction.putValue(Action.SHORT_DESCRIPTION, "Zoom Out");
		zoomOutAction.putValue(Action.SMALL_ICON, WorkbenchIcons.zoomOutIcon);
		zoomOutButton.setAction(zoomOutAction);

		toolBar.add(resetDiagramButton);
		toolBar.add(zoomInButton);
		toolBar.add(zoomOutButton);

		return toolBar;
	}

	public void setStatus(Status status) {
		switch (status) {
			case RUNNING :
				statusLabel.setText("Workflow running");
				statusLabel.setIcon(WorkbenchIcons.workingIcon);
			    break;
			case COMPLETE :
				statusLabel.setText("Workflow complete");
				statusLabel.setIcon(WorkbenchIcons.greentickIcon);
			    break;		
		}
	}
	
	public void setProvenanceConnector(ProvenanceConnector connector) {
		if (connector != null) {
			provenanceConnector = connector;
			setSessionId(provenanceConnector.getSessionID());			
		}
	}

	public Observer<MonitorMessage> setDataflow(Dataflow dataflow) {
		SVGGraphController svgGraphController = new SVGGraphController(dataflow, true, svgCanvas);
		svgGraphController.setGraphEventManager(new MonitorGraphEventManager(this, provenanceConnector, dataflow, getSessionId()));
		setGraphController(svgGraphController);
		graphMonitor = new GraphMonitor(svgGraphController, this);
		return graphMonitor;
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Monitor View Component";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		if (graphMonitor != null) {
			graphMonitor.onDispose();
		}
		if (svgScrollPane != null) {
			svgScrollPane.removeAll();
			svgScrollPane = null;
		}
		if (svgCanvas != null) {
			svgCanvas.stopProcessing();
			svgCanvas.removeGVTTreeRendererListener(gvtTreeRendererAdapter);
			svgCanvas = null;
		}
		
	}

	@Override
	protected void finalize() throws Throwable {
		onDispose();
	}



	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setGraphController(SVGGraphController graphController) {
		this.graphController = graphController;
	}

	public SVGGraphController getGraphController() {
		return graphController;
	}

	class MySvgScrollPane extends JSVGScrollPane {
		private static final long serialVersionUID = 6890422410714378543L;

		public MySvgScrollPane(JSVGCanvas canvas) {
			super(canvas);
		}
		
		public void reset() {
			super.resizeScrollBars();
			super.reset();
		}
	}

}

class MonitorGraphEventManager implements GraphEventManager {

	private static Logger logger = Logger
			.getLogger(MonitorGraphEventManager.class);
	private final ProvenanceConnector provenanceConnector;
	private final Dataflow dataflow;
	private String localName;
	private List<LineageQueryResultRecord> intermediateValues;

	private Runnable runnable;
	private String sessionID;
	private String targetWorkflowID;

	static int MINIMUM_HEIGHT = 500;
	static int MINIMUM_WIDTH = 800;
	private MonitorViewComponent monitorViewComponent;
	private ProvenanceResultsPanel provResultsPanel;

	public MonitorGraphEventManager(MonitorViewComponent monitorViewComponent, ProvenanceConnector provenanceConnector,
			Dataflow dataflow, String sessionID) {
		this.monitorViewComponent = monitorViewComponent;
		this.provenanceConnector = provenanceConnector;
		this.dataflow = dataflow;
		this.sessionID = sessionID;
	}

	/**
	 * Retrieve the provenance for a dataflow object
	 */
	public void mouseClicked(final GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		

		Object dataflowObject = graphElement.getDataflowObject();
		GraphElement parent = graphElement.getParent();
		if (parent instanceof GraphNode) {
			   parent = parent.getParent();
			}

		if (monitorViewComponent.getGraphController().getDataflowSelectionModel() != null) {
			monitorViewComponent.getGraphController().getDataflowSelectionModel().addSelection(dataflowObject);
		}

		// no popup if provenance is switched off
		final JFrame frame = new JFrame();
		
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		JPanel fetchButtonPanel = new JPanel();
		fetchButtonPanel.setLayout(new BorderLayout());
		JButton fetchResults = new JButton("Fetch Results");
		fetchResults.setToolTipText("Retrieve provenance again - in case you need to get any new results");
		fetchButtonPanel.add(fetchResults, BorderLayout.WEST);
		topPanel.add(fetchButtonPanel);
		
		fetchResults.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				ProvenanceAccess provenanceAccess = new ProvenanceAccess(DataManagementConfiguration.getInstance().getConnectorType());
				//TODO use the new provenance access API with the nested workflow if required to get the results
				Dependencies fetchPortData = provenanceAccess.fetchPortData(sessionID, targetWorkflowID, localName, null, null);
				intermediateValues = fetchPortData.getRecords();
				if (intermediateValues.size() > 0) {
					frame.setTitle("Intermediate results for "
									+ localName);
					for (LineageQueryResultRecord record : intermediateValues) {
						logger.info("LQRR: "
								+ record.toString());
					}
					provResultsPanel
							.setLineageRecords(intermediateValues);
					logger
							.info("Intermediate results retrieved for workflow instance: "
									+ sessionID
									+ " processor: "
									+ localName
									+ " nested: " + targetWorkflowID);
				} else {
					frame.setTitle("Currently no intermediate results for service "
							+ localName  + ". Click \'Fetch Results\' to try again.");
					
				}
			}
		});

		final JPanel provenancePanel = new JPanel();
		provenancePanel.setLayout(new BorderLayout());
		if (provenanceConnector != null) {
			if (dataflowObject != null) {
				if (dataflowObject instanceof Processor) {
					if (provenanceConnector != null) {
						//Is it a nested workflow that has been clicked on or a processor inside it?
//						if (((Processor) dataflowObject).getActivityList().get(0) instanceof DataflowActivity) {
//							localName = null;
//						} else {
							localName = ((Processor) dataflowObject).getLocalName();
//						}
						frame.setTitle("Fetching intermediate results for service " + localName);
						//if the processor is inside a nested workflow then get the nested workflow id.  There
						//no parent on a top level workflow
//						if (parent == null) {
//							nestedWorkflowID = null;
//						} else {
						
							//is it inside a nested workflow?
							if (parent != null && parent.getDataflowObject() instanceof Processor) {
								if (((Processor)parent.getDataflowObject()).getActivityList().get(0) instanceof DataflowActivity) {
									Activity<?> activity = ((Processor)parent.getDataflowObject()).getActivityList().get(0);
									targetWorkflowID = ((DataflowActivity)activity).getNestedDataflow().getInternalIdentier();
								}
							} else {
								targetWorkflowID = dataflow.getInternalIdentier();
							}
//						}
							
//						String internalIdentier = dataflow.getInternalIdentier();
						provResultsPanel = new ProvenanceResultsPanel();
						provResultsPanel.setContext(provenanceConnector
								.getInvocationContext());
						provenancePanel.add(provResultsPanel,
								BorderLayout.CENTER);

						runnable = new Runnable() {


							public void run() {
								try {
									logger.info("Retrieving intermediate results for workflow instance: "
													+ sessionID
													+ " processor: "
													+ localName
													+ " nested: " + targetWorkflowID);																
									
									ProvenanceAccess provenanceAccess = new ProvenanceAccess(DataManagementConfiguration.getInstance().getConnectorType());
									//TODO use the new provenance access API with the nested workflow if required to get the results
									Dependencies fetchPortData = provenanceAccess.fetchPortData(sessionID, targetWorkflowID, localName, null, null);
									intermediateValues = fetchPortData.getRecords();
									
//									intermediateValues = provenanceConnector
//											.getIntermediateValues(sessionID,
//													localName, null, null);
									if (intermediateValues.size() > 0) {
										frame.setTitle("Intermediate results for "
														+ localName);
										for (LineageQueryResultRecord record : intermediateValues) {
											logger.info("LQRR: "
													+ record.toString());
										}
										provResultsPanel
												.setLineageRecords(intermediateValues);
										frame.setVisible(true);
//										frame.setVisible(true);
										logger
												.info("Intermediate results retrieved for workflow instance: "
														+ sessionID
														+ " processor: "
														+ localName
														+ " nested: " + targetWorkflowID);										
									} else {
//										JOptionPane.showMessageDialog(null,
//												"Currently no intermediate results available for service " + localName + "\nData may still be being processed",
//												"No results yet",
//												JOptionPane.INFORMATION_MESSAGE);
//										frame.setVisible(false);
										frame.setTitle("Currently no intermediate results for service "
												+ localName + ". Click \'Fetch Results\' to try again.");
										frame.setVisible(true);
										
									}

								} catch (Exception e) {
									logger
											.warn("Could not retrieve intermediate results: "
													+ e.getStackTrace());
									frame.setVisible(false);
									JOptionPane.showMessageDialog(null,
											"Could not retrieve intermediate results:\n"
													+ e,
											"Problem retrieving results",
											JOptionPane.ERROR_MESSAGE);
								}
							}

						};
						runnable.run();
//						timer = new Timer(
//								"Retrieve intermediate results for workflow: "
//										+ internalIdentier + ", processor: "
//										+ localName);
//						
//						timer.schedule(timerTask, 1, 50000);
						//kill the timer when the user closes the frame
//						frame.addWindowListener(new WindowClosingListener(timer, timerTask));

					}

					panel.add(topPanel, BorderLayout.NORTH);
					panel.add(provenancePanel, BorderLayout.CENTER);
					panel.setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT - 100));
					frame.add(new JScrollPane(panel));
					frame.setSize(new Dimension(800,500));
					
					
					frame.addComponentListener(new ComponentAdapter() {
						public void componentResized(ComponentEvent e) {
							Dimension resizedSize = frame.getSize();
							int newWidth = resizedSize.width < MINIMUM_WIDTH ? MINIMUM_WIDTH : resizedSize.width;
							int newHeight = resizedSize.height < MINIMUM_HEIGHT ? MINIMUM_HEIGHT : resizedSize.height;
							frame.setSize(new Dimension(newWidth, newHeight));
						}
					});

				}

			}
		} else {
			//tell the user that provenance is switched off
		}

	}

	public void mouseDown(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		// TODO Auto-generated method stub

	}

	public void mouseMoved(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		// TODO Auto-generated method stub

	}

	public void mouseUp(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		// TODO Auto-generated method stub

	}

	public void mouseOut(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		// TODO Auto-generated method stub

	}

	public void mouseOver(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		// TODO Auto-generated method stub

	}
	
	


}
