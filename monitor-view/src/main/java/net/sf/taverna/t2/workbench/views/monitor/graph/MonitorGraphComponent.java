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
package net.sf.taverna.t2.workbench.views.monitor.graph;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

//import net.sf.taverna.t2.lang.observer.Observer;
//import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.graph.menu.ResetDiagramAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomInAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomOutAction;
import net.sf.taverna.t2.workbench.views.monitor.WorkflowObjectSelectionMessage;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;

/**
 * Use to display the graph for fresh workflow runs and allow the user to
 * click on processors to see the intermediate results for processors pulled from provenance.
 *  
 */
public class MonitorGraphComponent extends JPanel implements UIComponentSPI, Observable<WorkflowObjectSelectionMessage> {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MonitorGraphComponent.class);

	// Multicaster used to notify all interested parties that a selection of 
	// a workflow object has occurred on the graph.
	private MultiCaster<WorkflowObjectSelectionMessage> multiCaster = new MultiCaster<WorkflowObjectSelectionMessage>(this);

	private static final long serialVersionUID = 1L;

	private SVGGraphController graphController;

	protected JSVGCanvas svgCanvas;
	protected JSVGScrollPane svgScrollPane;
	
	protected JLabel statusLabel;
	
	protected ProvenanceConnector provenanceConnector;	

	private String sessionId;

	protected GVTTreeRendererAdapter gvtTreeRendererAdapter;

	private GraphMonitor graphMonitor;

	protected Dataflow dataflow;

	private ReferenceService referenceService;

	public MonitorGraphComponent() {
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
		
		//add(statusLabel, BorderLayout.SOUTH);
		
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

//	public void setStatus(Status status) {
//		switch (status) {
//			case RUNNING :
//				statusLabel.setText("Workflow running");
//				statusLabel.setIcon(WorkbenchIcons.workingIcon);
//				if (dataflow != null){ // should not be null really, dataflow should be set before this method is called
//					dataflow.setIsRunning(true);
//				}
//			    break;
//			case FINISHED :
//				statusLabel.setText("Workflow finished");
//				statusLabel.setIcon(WorkbenchIcons.greentickIcon);
//				if (dataflow != null){// should not be null really, dataflow should be set before this method is called
//					dataflow.setIsRunning(false);
//				}
//			    break;		
//		}
//	}
	
	public void setProvenanceConnector(ProvenanceConnector connector) {
		if (connector != null) {
			provenanceConnector = connector;
			setSessionId(provenanceConnector.getSessionID());			
		}
	}
	
	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public GraphMonitor setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
		SVGGraphController svgGraphController = new SVGGraphController(dataflow, true, svgCanvas);
		svgGraphController.setGraphEventManager(new MonitorGraphEventManager(this, provenanceConnector, dataflow, getSessionId()));
		// For selections on the graph
		svgGraphController.setDataflowSelectionModel(DataflowSelectionManager
				.getInstance().getDataflowSelectionModel(dataflow));	
		setGraphController(svgGraphController);
		graphMonitor = new GraphMonitor(svgGraphController);
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
		if (graphController != null) {
			graphController.shutdown();
			graphController = null;
		}
		if (svgCanvas != null) {
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

	public ReferenceService getReferenceService() {
		return referenceService;
	}

	public void addObserver(Observer<WorkflowObjectSelectionMessage> observer) {
		multiCaster.addObserver(observer);
	}

	public void removeObserver(Observer<WorkflowObjectSelectionMessage> observer) {
		multiCaster.removeObserver(observer);
	}

	public void triggerWorkflowObjectSelectionEvent(Object workflowObject) {
		multiCaster.notify(new WorkflowObjectSelectionMessage(workflowObject));
	}

	public List<Observer<WorkflowObjectSelectionMessage>> getObservers() {
		return multiCaster.getObservers();
	}
	
	public void setSelectedGraphElementForWorkflowObject(Object workflowObject){
		// Only select processors, ignore links, ports etc.
		if (workflowObject instanceof Processor){
			// Clear previous selection
			graphController.getDataflowSelectionModel().clearSelection();
			graphController.getDataflowSelectionModel().addSelection(workflowObject);
		}
		else if (workflowObject instanceof Dataflow){
			// We cannot show dataflow object as selected of the graph so clear previous selection
			graphController.getDataflowSelectionModel().clearSelection();
		}
	}
}

class MonitorGraphEventManager implements GraphEventManager {

	private static Logger logger = Logger
			.getLogger(MonitorGraphEventManager.class);
	private final ProvenanceConnector provenanceConnector;
	private final Dataflow dataflow;
	private String localName;

	private Runnable runnable;
	private String sessionID;
	private String targetWorkflowID;

	static int MINIMUM_HEIGHT = 500;
	static int MINIMUM_WIDTH = 800;
	private MonitorGraphComponent monitorViewComponent;
	private GraphElement previouslySelectedProcessorGraphElement;

	public MonitorGraphEventManager(MonitorGraphComponent monitorViewComponent, ProvenanceConnector provenanceConnector,
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

		monitorViewComponent.setSelectedGraphElementForWorkflowObject(dataflowObject);
	
		// Notify anyone interested that a selection occurred on the graph
		monitorViewComponent.triggerWorkflowObjectSelectionEvent(dataflowObject);
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
