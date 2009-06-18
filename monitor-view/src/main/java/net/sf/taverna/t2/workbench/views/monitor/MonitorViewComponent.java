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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.provenance.ProvenanceConnectorRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.provenance.ProvenanceConfiguration;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.JSVGCanvas.ResetTransformAction;
import org.apache.batik.swing.JSVGCanvas.ZoomAction;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;

public class MonitorViewComponent extends JPanel implements UIComponentSPI {

	private static Logger logger = Logger.getLogger(MonitorViewComponent.class);

	private static final long serialVersionUID = 1L;

	private SVGGraphController graphController;

	private JSVGCanvas svgCanvas;
	private JSVGScrollPane svgScrollPane;
	
	private JLabel statusLabel;
	
	private ProvenanceConnector provenanceConnector;

	private Dataflow dataflow;
	
	public enum Status {RUNNING, COMPLETE};

	private String sessionId;
	

	public MonitorViewComponent() {
		super(new BorderLayout());
		setBorder(LineBorder.createGrayLineBorder());

		svgCanvas = new JSVGCanvas();
		svgCanvas.setEnableZoomInteractor(false);
		svgCanvas.setEnableRotateInteractor(false);
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);

		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
//				svgScrollPane.reset();
				graphController.setUpdateManager(svgCanvas.getUpdateManager());
//				MonitorViewComponent.this.revalidate();
			}
		});
		
		JPanel diagramAndControls = new JPanel();
		diagramAndControls.setLayout(new BorderLayout());
		
		svgScrollPane = new MySvgScrollPane(svgCanvas);
		diagramAndControls.add(graphActionsToolbar(), BorderLayout.NORTH);
		diagramAndControls.add(svgScrollPane, BorderLayout.CENTER);
		
		add(diagramAndControls, BorderLayout.CENTER);
		
		statusLabel = new JLabel();
		statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		add(statusLabel, BorderLayout.SOUTH);
		
		setProvenanceConnector();
	}
	
	private JToolBar graphActionsToolbar() {
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
		resetDiagramAction.putValue(Action.SHORT_DESCRIPTION, "Reset Diagram");
		resetDiagramAction.putValue(Action.SMALL_ICON, WorkbenchIcons.refreshIcon);
		resetDiagramButton.setAction(resetDiagramAction);

		Action zoomInAction = svgCanvas.new ZoomAction(1.2);
		zoomInAction.putValue(Action.SHORT_DESCRIPTION, "Zoom In");
		zoomInAction.putValue(Action.SMALL_ICON, WorkbenchIcons.zoomInIcon);
		zoomInButton.setAction(zoomInAction);

		Action zoomOutAction = svgCanvas.new ZoomAction(1/1.2);
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
	
	private void setProvenanceConnector() {
		if (ProvenanceConfiguration.getInstance().getProperty("enabled")
				.equalsIgnoreCase("yes")) {
			String connectorType = ProvenanceConfiguration.getInstance()
					.getProperty("connector");

			for (ProvenanceConnector connector : ProvenanceConnectorRegistry
					.getInstance().getInstances()) {
				if (connectorType.equalsIgnoreCase(connector.getName())) {
					provenanceConnector = connector;
					//ensure that this view has the correct session identifier to retrieve provenance
					this.setSessionId(provenanceConnector.getSessionID());
				}
			}
		}
	}

	public Observer<MonitorMessage> setDataflow(Dataflow dataflow) {
		graphController = new SVGGraphController(dataflow,
				new MonitorGraphEventManager(provenanceConnector, dataflow, getSessionId()),
				this) {
			public void redraw() {
				svgCanvas.setDocument(graphController
						.generateSVGDocument(getBounds()));
			}
		};
		svgCanvas.setDocument(graphController.generateSVGDocument(getBounds()));
		// revalidate();
		return new GraphMonitor(graphController, this);
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
		// TODO Auto-generated method stub

	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return sessionId;
	}

	private class MySvgScrollPane extends JSVGScrollPane {

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
	private Timer timer;
	private TimerTask timerTask;
	private String sessionID;

	static int MINIMUM_HEIGHT = 500;
	static int MINIMUM_WIDTH = 800;

	public MonitorGraphEventManager(ProvenanceConnector provenanceConnector,
			Dataflow dataflow, String sessionID) {
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
		// no popup if provenance is switched off
		final JFrame frame = new JFrame();
		
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());

		final JPanel provenancePanel = new JPanel();
		provenancePanel.setLayout(new BorderLayout());
		if (provenanceConnector != null) {
			if (dataflowObject != null) {
				if (dataflowObject instanceof Processor) {
					if (provenanceConnector != null) {
						localName = ((Processor) dataflowObject).getLocalName();
						frame.setTitle("Fetching intermediate results for " + localName);

						String internalIdentier = dataflow
								.getInternalIdentier();
//						final String sessionID = provenanceConnector
//								.getSessionID();

						final ProvenanceResultsPanel provResultsPanel = new ProvenanceResultsPanel();
						provResultsPanel.setContext(provenanceConnector
								.getInvocationContext());
						provenancePanel.add(provResultsPanel,
								BorderLayout.CENTER);

						timerTask = new TimerTask() {

							@Override
							public void run() {
								try {
									logger
											.info("Retrieving intermediate results for dataflow instance: "
													+ sessionID
													+ " processor: "
													+ localName);

									intermediateValues = provenanceConnector
											.getIntermediateValues(sessionID,
													localName, null, null);
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
												.info("Intermediate results retrieved for dataflow instance: "
														+ sessionID
														+ " processor: "
														+ localName);
									} else {
										frame.setTitle("No intermediate results for "
												+ localName);
										
									}

								} catch (Exception e) {
									logger
											.warn("Could not retrieve intermediate results: "
													+ e.getStackTrace());
									JOptionPane.showMessageDialog(null,
											"Could not retrieve intermediate results:\n"
													+ e,
											"Problem retrieving results",
											JOptionPane.ERROR_MESSAGE);
								}
							}

						};

						timer = new Timer(
								"Retrieve intermediate results for dataflow: "
										+ internalIdentier + ", processor: "
										+ localName);
						timer.schedule(timerTask, 1, 50000);
						//kill the timer when the user closes the frame
						frame.addWindowListener(new WindowClosingListener(timer, timerTask));

					}

					panel.add(topPanel, BorderLayout.NORTH);
					panel.add(provenancePanel, BorderLayout.CENTER);
					panel.setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT - 100));
					frame.add(new JScrollPane(panel));
					frame.setSize(new Dimension(800,500));
					frame.setVisible(true);
					
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
	
	private class WindowClosingListener extends WindowAdapter {
		private final Timer timer;
		private final TimerTask timerTask;

		public WindowClosingListener(Timer timer, TimerTask timerTask) {
			this.timer = timer;
			this.timerTask = timerTask;
		}

		@Override
		public void windowClosing(WindowEvent e) {
			if (timer != null) {
				timer.cancel();
			}
			if (timerTask != null) {
				timerTask.cancel();
			}
		}
	}
	


}
