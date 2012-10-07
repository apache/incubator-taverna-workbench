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
package net.sf.taverna.t2.workbench.views.graph;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.SavedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphController.PortStyle;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.dndhandler.ServiceTransferHandler;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workbench.views.graph.config.GraphViewConfiguration;
import net.sf.taverna.t2.workbench.views.graph.menu.ResetDiagramAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomInAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomOutAction;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;

import uk.org.taverna.platform.capability.api.ActivityService;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Workflow;

/**
 *
 * @author David Withers
 * @author Alex Nenadic
 * @author Tom Oinn
 *
 */
public class GraphViewComponent extends WorkflowView {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(GraphViewComponent.class);

	private SVGGraphController graphController;

	private JPanel diagramPanel;

	public static Map<WorkflowBundle, SVGGraphController> graphControllerMap = new HashMap<WorkflowBundle, SVGGraphController>();
	public static Map<WorkflowBundle, JPanel> diagramPanelMap = new HashMap<WorkflowBundle, JPanel>();
	public static Map<WorkflowBundle, Action[]> diagramActionsMap = new HashMap<WorkflowBundle, Action[]>();

	private WorkflowBundle workflowBundle;

	private Timer timer;

	private GVTTreeRendererAdapter gvtTreeBuilderAdapter;

	private CardLayout cardLayout;

    private TitledBorder border;

	private final EditManager editManager;
	private final FileManager fileManager;
	private final MenuManager menuManager;
	private final DataflowSelectionManager dataflowSelectionManager;
	private final ColourManager colourManager;
	private final WorkbenchConfiguration workbenchConfiguration;
	private final GraphViewConfiguration configuration;

	public GraphViewComponent(EditManager editManager, FileManager fileManager, MenuManager menuManager,
			DataflowSelectionManager dataflowSelectionManager, ColourManager colourManager,
			WorkbenchConfiguration workbenchConfiguration, GraphViewConfiguration configuration, ActivityService activityService) {
		super(editManager, dataflowSelectionManager, activityService);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.menuManager = menuManager;
		this.dataflowSelectionManager = dataflowSelectionManager;
		this.colourManager = colourManager;
		this.workbenchConfiguration = workbenchConfiguration;
		this.configuration = configuration;
		cardLayout = new CardLayout();
		setLayout(cardLayout);

		border = new TitledBorder("Workflow diagram");
		border.setTitleJustification(TitledBorder.CENTER);
		setBorder(border);

		fileManager.addObserver(new FileManagerObserver());

		editManager.addObserver(new EditManagerObserver());

		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (graphController != null) {
					graphController.redraw();
				}
				timer.stop();
			}
		};
		timer = new Timer(100, taskPerformer);

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				if (timer.isRunning()) {
					timer.restart();
				} else {
					timer.start();
				}
			}
		});

	}

	private JPanel createDiagramPanel(WorkflowBundle workflowBundle) {
		JPanel diagramPanel = new JPanel(new BorderLayout());

		// get the default diagram settings
		Alignment alignment = Alignment.valueOf(configuration
				.getProperty(GraphViewConfiguration.ALIGNMENT));
		PortStyle portStyle = PortStyle.valueOf(configuration
				.getProperty(GraphViewConfiguration.PORT_STYLE));
		boolean animationEnabled = Boolean.parseBoolean(configuration
				.getProperty(GraphViewConfiguration.ANIMATION_ENABLED));
		int animationSpeed = Integer.parseInt(configuration
				.getProperty(GraphViewConfiguration.ANIMATION_SPEED));

		// create an SVG canvas
		final JSVGCanvas svgCanvas = new JSVGCanvas(null, true, false);
		svgCanvas.setEnableZoomInteractor(false);
		svgCanvas.setEnableRotateInteractor(false);
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		svgCanvas.setTransferHandler(new ServiceTransferHandler(editManager, menuManager, dataflowSelectionManager));

		AutoScrollInteractor asi = new AutoScrollInteractor(svgCanvas);
		svgCanvas.addMouseListener(asi);
		svgCanvas.addMouseMotionListener(asi);

		final JSVGScrollPane svgScrollPane = new MySvgScrollPane(svgCanvas);

		gvtTreeBuilderAdapter = new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
				logger.info("Rendered svg");
				svgScrollPane.reset();
				GraphViewComponent.this.revalidate();
			}
		};
		svgCanvas.addGVTTreeRendererListener(gvtTreeBuilderAdapter);

		// create a graph controller
		SVGGraphController svgGraphController = new SVGGraphController(
				workflowBundle.getMainWorkflow(), false, svgCanvas, alignment, portStyle, editManager, menuManager, colourManager, workbenchConfiguration);
		svgGraphController.setDataflowSelectionModel(dataflowSelectionManager
				.getDataflowSelectionModel(workflowBundle));
		svgGraphController.setAnimationSpeed(animationEnabled ? animationSpeed
				: 0);

		graphControllerMap.put(workflowBundle, svgGraphController);

		// Toolbar with actions related to graph
		JToolBar graphActionsToolbar = graphActionsToolbar(workflowBundle, svgGraphController,
				svgCanvas, alignment, portStyle);
		graphActionsToolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
		graphActionsToolbar.setFloatable(false);

		// Panel to hold the toolbars
		JPanel toolbarPanel = new JPanel();
		toolbarPanel
				.setLayout(new BoxLayout(toolbarPanel, BoxLayout.PAGE_AXIS));
		toolbarPanel.add(graphActionsToolbar);

		diagramPanel.add(toolbarPanel, BorderLayout.NORTH);
		diagramPanel.add(svgScrollPane, BorderLayout.CENTER);
		// diagramPanel.add(new MySvgScrollPane(svgCanvas),
		// BorderLayout.CENTER);

		return diagramPanel;
	}

	@SuppressWarnings("serial")
	private JToolBar graphActionsToolbar(WorkflowBundle workflowBundle,
			final SVGGraphController graphController, JSVGCanvas svgCanvas,
			Alignment alignment, PortStyle portStyle) {
		JToolBar toolBar = new JToolBar();

		JButton resetDiagramButton = new JButton();
		resetDiagramButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		JButton zoomInButton = new JButton();
		zoomInButton.setBorder(new EmptyBorder(0, 2, 0, 2));
		JButton zoomOutButton = new JButton();
		zoomOutButton.setBorder(new EmptyBorder(0, 2, 0, 2));

		Action resetDiagramAction = svgCanvas.new ResetTransformAction();
		ResetDiagramAction.setDesignAction(resetDiagramAction);
		resetDiagramAction.putValue(Action.SHORT_DESCRIPTION, "Reset Diagram");
		resetDiagramAction.putValue(Action.SMALL_ICON,
				WorkbenchIcons.refreshIcon);
		resetDiagramButton.setAction(resetDiagramAction);

		Action zoomInAction = svgCanvas.new ZoomAction(1.2);
		ZoomInAction.setDesignAction(zoomInAction);
		zoomInAction.putValue(Action.SHORT_DESCRIPTION, "Zoom In");
		zoomInAction.putValue(Action.SMALL_ICON, WorkbenchIcons.zoomInIcon);
		zoomInButton.setAction(zoomInAction);

		Action zoomOutAction = svgCanvas.new ZoomAction(1 / 1.2);
		ZoomOutAction.setDesignAction(zoomOutAction);
		zoomOutAction.putValue(Action.SHORT_DESCRIPTION, "Zoom Out");
		zoomOutAction.putValue(Action.SMALL_ICON, WorkbenchIcons.zoomOutIcon);
		zoomOutButton.setAction(zoomOutAction);

		diagramActionsMap.put(workflowBundle, new Action[] {resetDiagramAction, zoomInAction, zoomOutAction});

		toolBar.add(resetDiagramButton);
		toolBar.add(zoomInButton);
		toolBar.add(zoomOutButton);

		toolBar.addSeparator();

		ButtonGroup nodeTypeGroup = new ButtonGroup();

		JToggleButton noPorts = new JToggleButton();
		JToggleButton allPorts = new JToggleButton();
		JToggleButton blobs = new JToggleButton();
		nodeTypeGroup.add(noPorts);
		nodeTypeGroup.add(allPorts);
		nodeTypeGroup.add(blobs);

		if (portStyle.equals(PortStyle.NONE)) {
			noPorts.setSelected(true);
		} else if (portStyle.equals(PortStyle.ALL)) {
			allPorts.setSelected(true);
		} else {
			blobs.setSelected(true);
		}

		noPorts.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(PortStyle.NONE);
				graphController.redraw();
			}

		});
		noPorts.getAction().putValue(Action.SHORT_DESCRIPTION,
				"Display no service ports");
		noPorts.getAction().putValue(Action.SMALL_ICON,
				WorkbenchIcons.noportIcon);
		noPorts.setFocusPainted(false);

		allPorts.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(PortStyle.ALL);
				graphController.redraw();
			}

		});
		allPorts.getAction().putValue(Action.SHORT_DESCRIPTION,
				"Display all service ports");
		allPorts.getAction().putValue(Action.SMALL_ICON,
				WorkbenchIcons.allportIcon);
		allPorts.setFocusPainted(false);

		blobs.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setPortStyle(PortStyle.BLOB);
				graphController.redraw();
			}

		});
		blobs.getAction().putValue(Action.SHORT_DESCRIPTION,
				"Display services as circles");
		blobs.getAction().putValue(Action.SMALL_ICON, WorkbenchIcons.blobIcon);
		blobs.setFocusPainted(false);

		toolBar.add(noPorts);
		toolBar.add(allPorts);
		toolBar.add(blobs);

		toolBar.addSeparator();

		ButtonGroup alignmentGroup = new ButtonGroup();

		JToggleButton vertical = new JToggleButton();
		JToggleButton horizontal = new JToggleButton();
		alignmentGroup.add(vertical);
		alignmentGroup.add(horizontal);

		if (alignment.equals(Alignment.VERTICAL)) {
			vertical.setSelected(true);
		} else {
			horizontal.setSelected(true);
		}

		vertical.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setAlignment(Alignment.VERTICAL);
				graphController.redraw();
			}

		});
		vertical.getAction().putValue(Action.SHORT_DESCRIPTION,
				"Align services vertically");
		vertical.getAction().putValue(Action.SMALL_ICON,
				WorkbenchIcons.verticalIcon);
		vertical.setFocusPainted(false);

		horizontal.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setAlignment(Alignment.HORIZONTAL);
				graphController.redraw();
			}

		});
		horizontal.getAction().putValue(Action.SHORT_DESCRIPTION,
				"Align services horizontally");
		horizontal.getAction().putValue(Action.SMALL_ICON,
				WorkbenchIcons.horizontalIcon);
		horizontal.setFocusPainted(false);

		toolBar.add(vertical);
		toolBar.add(horizontal);

		toolBar.addSeparator();

		JToggleButton expandNested = new JToggleButton();
		expandNested.setSelected(true);

		expandNested.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent arg0) {
				graphController.setExpandNestedDataflows(!graphController
						.expandNestedDataflows());
				graphController.redraw();
			}

		});
		expandNested.getAction().putValue(Action.SHORT_DESCRIPTION,
				"Expand Nested Workflows");
		expandNested.getAction().putValue(Action.SMALL_ICON,
				WorkbenchIcons.expandNestedIcon);
		expandNested.setFocusPainted(false);
		toolBar.add(expandNested);

		return toolBar;
	}

	private String getBorderTitle(final WorkflowBundle workflowBundle) {
		String localName = workflowBundle.getName();
		String sourceName = fileManager.getDataflowName(workflowBundle);
		String result = "";
		if (localName.equals(sourceName)) {
			result = localName;
		}
		else if (sourceName.startsWith(localName + " ")) {
			result = sourceName;
		}
		else {
			result = localName + " from " + sourceName;
		}
		if (result.length() > 60) {
			result = result.substring(0, 57) + "...";
		}
		return result;
	}

	/**
	 * Sets the Dataflow to display in the graph view.
	 *
	 * @param dataflow
	 */
	public void setDataflow(WorkflowBundle dataflow) {
		this.workflowBundle = dataflow;
		if (!diagramPanelMap.containsKey(dataflow)) {
			JPanel newDiagramPanel = createDiagramPanel(dataflow);
			add(newDiagramPanel, String.valueOf(newDiagramPanel.hashCode()));
			diagramPanelMap.put(dataflow, newDiagramPanel);
		}
		graphController = graphControllerMap.get(dataflow);
		diagramPanel = diagramPanelMap.get(dataflow);
		Action[] actions = diagramActionsMap.get(dataflow);
		if (actions != null && actions.length == 3) {
			ResetDiagramAction.setDesignAction(actions[0]);
			ZoomInAction.setDesignAction(actions[1]);
			ZoomOutAction.setDesignAction(actions[2]);
		}
		cardLayout.show(this, String.valueOf(diagramPanel.hashCode()));
		border.setTitle(getBorderTitle(dataflow));
		graphController.redraw();
		this.repaint();
	}

	/**
	 * Returns the dataflow.
	 *
	 * @return the dataflow
	 */
	public WorkflowBundle getDataflow() {
		return workflowBundle;
	}

	/**
	 * For testing only
	 */
//	public static void main(String[] args) throws Exception {
//		System.setProperty("raven.eclipse", "true");
//		System.setProperty("taverna.dotlocation",
//				"/Applications/Taverna-1.7.1.app/Contents/MacOS/dot");
//		// System.setProperty("taverna.dotlocation", "/opt/local/bin/dot");
//
//		GraphViewComponent graphView = new GraphViewComponent(null, null, null, null);
//
//		T2DataflowOpener t2DataflowOpener = new T2DataflowOpener();
//		InputStream stream = GraphViewComponent.class
//				.getResourceAsStream("/nested_iteration.t2flow");
//		Dataflow dataflow = t2DataflowOpener.openDataflow(new T2FlowFileType(),
//				stream).getDataflow();
//
//		JFrame frame = new JFrame();
//		frame.add(graphView);
//		frame.setPreferredSize(new Dimension(600, 800));
//		frame.pack();
//		graphView.setDataflow(dataflow);
//		frame.setVisible(true);
//
//	}

	@Override
	public String getName() {
		return "Graph View Component";
	}

	public ImageIcon getIcon() {
		return null;
	}

	public void onDisplay() {
	}

	public void onDispose() {
		if (timer != null) {
			timer.stop();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		onDispose();
	}

	protected class EditManagerObserver implements Observer<EditManagerEvent> {
		public void notify(Observable<EditManagerEvent> sender,
				final EditManagerEvent message) throws Exception {
			if (! (message instanceof AbstractDataflowEditEvent)) {
				return;
			}

			boolean animationEnabled = Boolean.parseBoolean(configuration
					.getProperty(GraphViewConfiguration.ANIMATION_ENABLED));
			final int animationSpeed = (animationEnabled ? Integer.parseInt(configuration
					.getProperty(GraphViewConfiguration.ANIMATION_SPEED)) : 0);

			final boolean animationSettingChanged = (animationEnabled != (graphController.getAnimationSpeed() != 0));

			Runnable redraw = new Runnable() {
				public void run() {
					AbstractDataflowEditEvent dataflowEditEvent = (AbstractDataflowEditEvent) message;

					if (dataflowEditEvent.getDataFlow() == workflowBundle) {

						if (graphController.isDotMissing() || animationSettingChanged) {
							diagramPanelMap.remove(workflowBundle);
							setDataflow(workflowBundle);
						} else {
							if (animationSpeed != graphController.getAnimationSpeed()) {
								graphController.setAnimationSpeed(animationSpeed);
							}
							graphController.redraw();
							String dataflowName = getBorderTitle(workflowBundle);
							if (!dataflowName.equals(border.getTitle())) {
							    border.setTitle(dataflowName);
							    GraphViewComponent.this.repaint();
							}
						}
					}
				}
			};
			if (SwingUtilities.isEventDispatchThread()) {
				redraw.run();
			} else {
				// T2-971
				SwingUtilities.invokeLater(redraw);
			}
		}
	}


	public class FileManagerObserverRunnable implements Runnable {
		private final FileManagerEvent message;

	    public FileManagerObserverRunnable(FileManagerEvent message) {
			this.message = message;
		}

		public void run() {
			if (message instanceof ClosedDataflowEvent) {
				ClosedDataflowEvent closedDataflowEvent = (ClosedDataflowEvent) message;
				WorkflowBundle dataflow = closedDataflowEvent.getDataflow();
				JPanel panel = diagramPanelMap.remove(dataflow);
				if (panel != null) {
					remove(panel);
				}
				SVGGraphController removedController = graphControllerMap
						.remove(dataflow);
				if (removedController != null) {
					removedController.getSVGCanvas()
							.removeGVTTreeRendererListener(
									gvtTreeBuilderAdapter);
					removedController.shutdown();
				}
				diagramActionsMap.remove(dataflow);
			} else if (message instanceof SetCurrentDataflowEvent) {
				SetCurrentDataflowEvent currentDataflowEvent = (SetCurrentDataflowEvent) message;
				WorkflowBundle dataflow = currentDataflowEvent.getDataflow();
				setDataflow(dataflow);
			} else if (message instanceof SavedDataflowEvent) {
				setDataflow(workflowBundle);
			}
		}
	}

	public class FileManagerObserver implements Observer<FileManagerEvent> {

		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) {
		    FileManagerObserverRunnable runnable = new FileManagerObserverRunnable(message);
			if (SwingUtilities.isEventDispatchThread()) {
				runnable.run();
			} else {
				// T2-971
				SwingUtilities.invokeLater(runnable);
			}
		}
	}

	private class MySvgScrollPane extends JSVGScrollPane {
		private static final long serialVersionUID = 1L;

		public MySvgScrollPane(JSVGCanvas canvas) {
			super(canvas);
		}

		public void reset() {
			super.resizeScrollBars();
			super.reset();
		}
	}

}
