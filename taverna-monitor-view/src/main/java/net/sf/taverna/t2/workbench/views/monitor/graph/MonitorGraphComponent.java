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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;
import static javax.swing.BoxLayout.PAGE_AXIS;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.refreshIcon;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.zoomInIcon;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.zoomOutIcon;
import static org.apache.batik.swing.svg.AbstractJSVGComponent.ALWAYS_DYNAMIC;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.graph.AutoScrollInteractor;
import net.sf.taverna.t2.workbench.views.graph.menu.ResetDiagramAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomInAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomOutAction;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;

import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunService;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.WorkflowPort;
import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * Use to display the graph for fresh workflow runs and allow the user to click
 * on processors to see the intermediate results for processors pulled from
 * provenance.
 */
@SuppressWarnings("serial")
public class MonitorGraphComponent extends JPanel implements Updatable {
	private static Logger logger = Logger.getLogger(MonitorGraphComponent.class);

	private SVGGraphController graphController;
	private JPanel diagramPanel;
	private GraphMonitor graphMonitor;

	private Map<String, SVGGraphController> graphControllerMap = new HashMap<>();
	private Map<String, GraphMonitor> graphMonitorMap = new HashMap<>();
	private Map<String, JPanel> diagramPanelMap = new HashMap<>();
	private Map<String, Action[]> diagramActionsMap = new HashMap<>();

	@SuppressWarnings("unused")
	private Timer timer;
	private CardLayout cardLayout;
	@SuppressWarnings("unused")
	private JLabel statusLabel;

	private final RunService runService;
	private final ColourManager colourManager;
	private final WorkbenchConfiguration workbenchConfiguration;
	private final SelectionManager selectionManager;

	public MonitorGraphComponent(RunService runService, ColourManager colourManager,
			WorkbenchConfiguration workbenchConfiguration, SelectionManager selectionManager) {
		this.runService = runService;
		this.colourManager = colourManager;
		this.workbenchConfiguration = workbenchConfiguration;
		this.selectionManager = selectionManager;

		cardLayout = new CardLayout();
		setLayout(cardLayout);

//		ActionListener taskPerformer = new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				if (graphController != null) {
//					graphController.redraw();
//					graphMonitor.redraw();
//				}
//				timer.stop();
//			}
//		};
//		timer = new Timer(100, taskPerformer);
//
//		addComponentListener(new ComponentAdapter() {
//			public void componentResized(ComponentEvent e) {
//				if (timer.isRunning()) {
//					timer.restart();
//				} else {
//					timer.start();
//				}
//			}
//		});

	}

	@Override
	protected void finalize() throws Throwable {
		if (graphController != null)
			graphController.shutdown();
	}

	@Override
	public void update() {
		if (graphMonitor != null)
			graphMonitor.update();
	}

	private JPanel createDiagramPanel(String workflowRun) {
		final JPanel diagramPanel = new JPanel(new BorderLayout());

		try {
			Workflow workflow = runService.getWorkflow(workflowRun);
			Profile profile = runService.getProfile(workflowRun);

			// get the default diagram settings
			// Alignment alignment = Alignment.valueOf(graphViewConfiguration
			// .getProperty(GraphViewConfiguration.ALIGNMENT));
			// PortStyle portStyle = PortStyle.valueOf(graphViewConfiguration
			// .getProperty(GraphViewConfiguration.PORT_STYLE));

			// create an SVG canvas
			final JSVGCanvas svgCanvas = new JSVGCanvas(null, true, false);
			svgCanvas.setEnableZoomInteractor(false);
			svgCanvas.setEnableRotateInteractor(false);
			svgCanvas.setDocumentState(ALWAYS_DYNAMIC);

			AutoScrollInteractor asi = new AutoScrollInteractor(svgCanvas);
			svgCanvas.addMouseListener(asi);
			svgCanvas.addMouseMotionListener(asi);

			final JSVGScrollPane svgScrollPane = new MySvgScrollPane(svgCanvas);

			GVTTreeRendererAdapter gvtTreeRendererAdapter = new GVTTreeRendererAdapter() {
				@Override
				public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
					logger.info("Rendered svg");
//					svgScrollPane.reset();
//					diagramPanel.revalidate();
				}
			};
			svgCanvas.addGVTTreeRendererListener(gvtTreeRendererAdapter);

			// create a graph controller
			SVGGraphController svgGraphController = new SVGGraphController(
					workflow, profile, true, svgCanvas, null, null,
					colourManager, workbenchConfiguration);
			DataflowSelectionModel selectionModel = selectionManager
					.getWorkflowRunSelectionModel(workflowRun);
			svgGraphController.setDataflowSelectionModel(selectionModel);
			svgGraphController
					.setGraphEventManager(new MonitorGraphEventManager(
							selectionModel));

			graphControllerMap.put(workflowRun, svgGraphController);

			// Toolbar with actions related to graph
			JToolBar graphActionsToolbar = graphActionsToolbar(workflowRun, svgCanvas);
			graphActionsToolbar.setAlignmentX(LEFT_ALIGNMENT);
			graphActionsToolbar.setFloatable(false);

			// Panel to hold the toolbars
			JPanel toolbarPanel = new JPanel();
			toolbarPanel.setLayout(new BoxLayout(toolbarPanel, PAGE_AXIS));
			toolbarPanel.add(graphActionsToolbar);

			diagramPanel.add(toolbarPanel, NORTH);
			diagramPanel.add(svgScrollPane, CENTER);

			// JTextField workflowHierarchy = new JTextField(workflow.getName());
			// diagramPanel.add(workflowHierarchy, BorderLayout.SOUTH);
		} catch (InvalidRunIdException e) {
			diagramPanel.add(new JLabel("Workflow run ID invalid", JLabel.CENTER),
					CENTER);
		}
		return diagramPanel;
	}

	protected JToolBar graphActionsToolbar(String workflowRun, JSVGCanvas svgCanvas) {
		JToolBar toolBar = new JToolBar();
		toolBar.setAlignmentX(LEFT_ALIGNMENT);
		toolBar.setFloatable(false);

		JButton resetDiagramButton = new JButton();
		resetDiagramButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		JButton zoomInButton = new JButton();
		zoomInButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		JButton zoomOutButton = new JButton();
		zoomOutButton.setBorder(new EmptyBorder(0, 2, 0, 2));

		Action resetDiagramAction = svgCanvas.new ResetTransformAction();
		ResetDiagramAction.setResultsAction(resetDiagramAction);
		resetDiagramAction.putValue(SHORT_DESCRIPTION, "Reset Diagram");
		resetDiagramAction.putValue(SMALL_ICON, refreshIcon);
		resetDiagramButton.setAction(resetDiagramAction);

		Action zoomInAction = svgCanvas.new ZoomAction(1.2);
		ZoomInAction.setResultsAction(zoomInAction);
		zoomInAction.putValue(SHORT_DESCRIPTION, "Zoom In");
		zoomInAction.putValue(SMALL_ICON, zoomInIcon);
		zoomInButton.setAction(zoomInAction);

		Action zoomOutAction = svgCanvas.new ZoomAction(1 / 1.2);
		ZoomOutAction.setResultsAction(zoomOutAction);
		zoomOutAction.putValue(SHORT_DESCRIPTION, "Zoom Out");
		zoomOutAction.putValue(SMALL_ICON, zoomOutIcon);
		zoomOutButton.setAction(zoomOutAction);

		// diagramActionsMap.put(workflowRun, new Action[] { resetDiagramAction, zoomInAction,
		// zoomOutAction });

		toolBar.add(resetDiagramButton);
		toolBar.add(zoomInButton);
		toolBar.add(zoomOutButton);

		return toolBar;
	}

	// public void setStatus(Status status) {
	// switch (status) {
	// case RUNNING :
	// statusLabel.setText("Workflow running");
	// statusLabel.setIcon(WorkbenchIcons.workingIcon);
	// if (workflow != null){ // should not be null really, workflow should be set before this
	// method is called
	// workflow.setIsRunning(true);
	// }
	// break;
	// case FINISHED :
	// statusLabel.setText("Workflow finished");
	// statusLabel.setIcon(WorkbenchIcons.tickIcon);
	// if (workflow != null){// should not be null really, workflow should be set before this method
	// is called
	// workflow.setIsRunning(false);
	// }
	// break;
	// }
	// }

	public void setWorkflowRun(String workflowRun) throws InvalidRunIdException {
		if (workflowRun != null) {
			if (!diagramPanelMap.containsKey(workflowRun))
				addWorkflowRun(workflowRun);
			graphController = graphControllerMap.get(workflowRun);
			diagramPanel = diagramPanelMap.get(workflowRun);
			graphMonitor = graphMonitorMap.get(workflowRun);
			Action[] actions = diagramActionsMap.get(workflowRun);
			if (actions != null && actions.length == 3) {
				ResetDiagramAction.setDesignAction(actions[0]);
				ZoomInAction.setDesignAction(actions[1]);
				ZoomOutAction.setDesignAction(actions[2]);
			}
			cardLayout.show(this, String.valueOf(diagramPanel.hashCode()));
			// graphController.redraw();
		}
	}

	public void addWorkflowRun(String workflowRun) throws InvalidRunIdException {
		JPanel newDiagramPanel = createDiagramPanel(workflowRun);
		add(newDiagramPanel, String.valueOf(newDiagramPanel.hashCode()));
		diagramPanelMap.put(workflowRun, newDiagramPanel);
		graphMonitorMap.put(workflowRun,
				new GraphMonitor(graphControllerMap.get(workflowRun),
						runService.getWorkflowReport(workflowRun)));
	}

	public void removeWorkflowRun(String workflowRun) {
		JPanel removedDiagramPanel = diagramPanelMap.remove(workflowRun);
		if (removedDiagramPanel != null)
			remove(removedDiagramPanel);
		SVGGraphController removedController = graphControllerMap
				.remove(workflowRun);
		if (removedController != null)
			removedController.shutdown();
		graphMonitorMap.remove(workflowRun);
		diagramActionsMap.remove(workflowRun);
	}

	private class MonitorGraphEventManager implements GraphEventManager {
		private final DataflowSelectionModel selectionModel;

		public MonitorGraphEventManager(DataflowSelectionModel selectionModel) {
			this.selectionModel = selectionModel;
		}

		@Override
		public void mouseClicked(final GraphElement graphElement, short button,
				boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
				int screenX, int screenY) {
			Object workflowObject = graphElement.getWorkflowBean();
			if (workflowObject instanceof Processor
					|| workflowObject instanceof WorkflowPort)
				selectionModel.addSelection(workflowObject);
		}

		@Override
		public void mouseDown(GraphElement graphElement, short button,
				boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
				int screenX, int screenY) {
		}

		@Override
		public void mouseMoved(GraphElement graphElement, short button,
				boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
				int screenX, int screenY) {
		}

		@Override
		public void mouseUp(GraphElement graphElement, short button,
				boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
				int screenX, int screenY) {
		}

		@Override
		public void mouseOut(GraphElement graphElement, short button,
				boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
				int screenX, int screenY) {
		}

		@Override
		public void mouseOver(GraphElement graphElement, short button,
				boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
				int screenX, int screenY) {
		}
	}

	private class MySvgScrollPane extends JSVGScrollPane {
		private static final long serialVersionUID = 6890422410714378543L;

		public MySvgScrollPane(JSVGCanvas canvas) {
			super(canvas);
		}

		@Override
		public void reset() {
			super.resizeScrollBars();
			super.reset();
		}
	}
}
