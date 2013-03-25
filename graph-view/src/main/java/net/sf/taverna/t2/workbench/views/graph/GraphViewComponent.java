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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphController.PortStyle;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowBundleSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowSelectionEvent;
import net.sf.taverna.t2.workbench.ui.dndhandler.ServiceTransferHandler;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.graph.config.GraphViewConfiguration;
import net.sf.taverna.t2.workbench.views.graph.menu.ResetDiagramAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomInAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomOutAction;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Workflow;

/**
 *
 * @author David Withers
 * @author Alex Nenadic
 * @author Tom Oinn
 *
 */
public class GraphViewComponent extends JPanel implements UIComponentSPI {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(GraphViewComponent.class);

	private Workflow workflow;
	private SVGGraphController graphController;
	private JPanel diagramPanel;

	private Map<WorkflowBundle, Set<Workflow>> workflowsMap = new IdentityHashMap<WorkflowBundle, Set<Workflow>>();

	private Map<Workflow, SVGGraphController> graphControllerMap = new IdentityHashMap<Workflow, SVGGraphController>();
	private Map<Workflow, JPanel> diagramPanelMap = new IdentityHashMap<Workflow, JPanel>();
	private Map<Workflow, Action[]> diagramActionsMap = new IdentityHashMap<Workflow, Action[]>();


	private Timer timer;

	private GVTTreeRendererAdapter gvtTreeBuilderAdapter;

	private CardLayout cardLayout;

	private final ColourManager colourManager;
	private final EditManager editManager;
	private final MenuManager menuManager;
	private final GraphViewConfiguration graphViewConfiguration;
	private final WorkbenchConfiguration workbenchConfiguration;
	private final SelectionManager selectionManager;

	public GraphViewComponent(ColourManager colourManager, EditManager editManager, FileManager fileManager, MenuManager menuManager,
			GraphViewConfiguration graphViewConfiguration, WorkbenchConfiguration workbenchConfiguration,
			SelectionManager selectionManager) {
		this.colourManager = colourManager;
		this.editManager = editManager;
		this.menuManager = menuManager;
		this.graphViewConfiguration = graphViewConfiguration;
		this.workbenchConfiguration = workbenchConfiguration;
		this.selectionManager = selectionManager;

		cardLayout = new CardLayout();
		setLayout(cardLayout);

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

		editManager.addObserver(new EditManagerObserver());
		selectionManager.addObserver(new SelectionManagerObserver());
		fileManager.addObserver(new FileManagerObserver());
	}

	@Override
	protected void finalize() throws Throwable {
		if (timer != null) {
			timer.stop();
		}
	}

	private JPanel createDiagramPanel(Workflow workflow) {
		JPanel diagramPanel = new JPanel(new BorderLayout());

		// get the default diagram settings
		Alignment alignment = Alignment.valueOf(graphViewConfiguration
				.getProperty(GraphViewConfiguration.ALIGNMENT));
		PortStyle portStyle = PortStyle.valueOf(graphViewConfiguration
				.getProperty(GraphViewConfiguration.PORT_STYLE));
		boolean animationEnabled = Boolean.parseBoolean(graphViewConfiguration
				.getProperty(GraphViewConfiguration.ANIMATION_ENABLED));
		int animationSpeed = Integer.parseInt(graphViewConfiguration
				.getProperty(GraphViewConfiguration.ANIMATION_SPEED));

		// create an SVG canvas
		final JSVGCanvas svgCanvas = new JSVGCanvas(null, true, false);
		svgCanvas.setEnableZoomInteractor(false);
		svgCanvas.setEnableRotateInteractor(false);
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		svgCanvas.setTransferHandler(new ServiceTransferHandler(editManager, menuManager, selectionManager));

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
				workflow, selectionManager.getSelectedProfile(), false, svgCanvas, alignment, portStyle, editManager, menuManager, colourManager, workbenchConfiguration);
		svgGraphController.setDataflowSelectionModel(selectionManager
				.getDataflowSelectionModel(workflow.getParent()));
		svgGraphController.setAnimationSpeed(animationEnabled ? animationSpeed
				: 0);

		graphControllerMap.put(workflow, svgGraphController);

		// Toolbar with actions related to graph
		JToolBar graphActionsToolbar = graphActionsToolbar(workflow, svgGraphController,
				svgCanvas, alignment, portStyle);
		graphActionsToolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
		graphActionsToolbar.setFloatable(false);

		// Panel to hold the toolbars
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.PAGE_AXIS));
		toolbarPanel.add(graphActionsToolbar);

		diagramPanel.add(toolbarPanel, BorderLayout.NORTH);
		diagramPanel.add(svgScrollPane, BorderLayout.CENTER);

//		JTextField workflowHierarchy  = new JTextField(workflow.getName());
//		diagramPanel.add(workflowHierarchy, BorderLayout.SOUTH);

		return diagramPanel;
	}

	@SuppressWarnings("serial")
	private JToolBar graphActionsToolbar(Workflow workflow,
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

		diagramActionsMap.put(workflow, new Action[] {resetDiagramAction, zoomInAction, zoomOutAction});

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

	/**
	 * Sets the Workflow to display in the graph view.
	 *
	 * @param workflow
	 */
	private void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
		if (!diagramPanelMap.containsKey(workflow)) {
			addWorkflow(workflow);
		}
		graphController = graphControllerMap.get(workflow);
		diagramPanel = diagramPanelMap.get(workflow);
		Action[] actions = diagramActionsMap.get(workflow);
		if (actions != null && actions.length == 3) {
			ResetDiagramAction.setDesignAction(actions[0]);
			ZoomInAction.setDesignAction(actions[1]);
			ZoomOutAction.setDesignAction(actions[2]);
		}
		cardLayout.show(this, String.valueOf(diagramPanel.hashCode()));
		graphController.redraw();
	}

	private void addWorkflow(Workflow workflow) {
		JPanel newDiagramPanel = createDiagramPanel(workflow);
		add(newDiagramPanel, String.valueOf(newDiagramPanel.hashCode()));
		diagramPanelMap.put(workflow, newDiagramPanel);
		if (!workflowsMap.containsKey(workflow.getParent())) {
			workflowsMap.put(workflow.getParent(), new HashSet<Workflow>());
		}
		workflowsMap.get(workflow.getParent()).add(workflow);
	}

	private void removeWorkflow(Workflow workflow) {
		JPanel panel = diagramPanelMap.remove(workflow);
		if (panel != null) {
			remove(panel);
		}
		SVGGraphController removedController = graphControllerMap.remove(workflow);
		if (removedController != null) {
			removedController.getSVGCanvas().removeGVTTreeRendererListener(gvtTreeBuilderAdapter);
			removedController.shutdown();
		}
		diagramActionsMap.remove(workflow);
		Set<Workflow> workflows = workflowsMap.get(workflow.getParent());
		if (workflows != null) {
			workflows.remove(workflow);
		}
	}

	public GraphController getGraphController(Workflow workflow) {
		return graphControllerMap.get(workflow);
	}

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

	protected class EditManagerObserver extends SwingAwareObserver<EditManagerEvent> {
		@Override
		public void notifySwing(Observable<EditManagerEvent> sender, EditManagerEvent message) {
			if (message instanceof AbstractDataflowEditEvent) {
				System.out.println("EditManagerEvent");
				AbstractDataflowEditEvent dataflowEditEvent = (AbstractDataflowEditEvent) message;

				boolean animationEnabled = Boolean.parseBoolean(graphViewConfiguration
						.getProperty(GraphViewConfiguration.ANIMATION_ENABLED));
				int animationSpeed = (animationEnabled ? Integer.parseInt(graphViewConfiguration
						.getProperty(GraphViewConfiguration.ANIMATION_SPEED)) : 0);

				boolean animationSettingChanged = (animationEnabled != (graphController.getAnimationSpeed() != 0));

				if (dataflowEditEvent.getDataFlow() == workflow.getParent()) {
					if (graphController.isDotMissing() || animationSettingChanged) {
						removeWorkflow(workflow);
						setWorkflow(workflow);
					} else {
						if (animationSpeed != graphController.getAnimationSpeed()) {
							graphController.setAnimationSpeed(animationSpeed);
						}
						graphController.redraw();
					}
				}
			}
		}
	}

	public class FileManagerObserver extends SwingAwareObserver<FileManagerEvent> {
		@Override
		public void notifySwing(Observable<FileManagerEvent> sender, final FileManagerEvent message) {
			if (message instanceof ClosedDataflowEvent) {
				ClosedDataflowEvent closedDataflowEvent = (ClosedDataflowEvent) message;
				WorkflowBundle workflowBundle = closedDataflowEvent.getDataflow();
				if (workflowsMap.containsKey(workflowBundle)) {
					Set<Workflow> workflows = workflowsMap.remove(workflowBundle);
					for (Workflow workflow : workflows) {
						removeWorkflow(workflow);
					}
				}
			}
		}
	}

	public class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender, SelectionManagerEvent message)  {
			if (message instanceof WorkflowSelectionEvent) {
				System.out.println("GraphViewComponent.WorkflowSelectionEvent");
				setWorkflow(selectionManager.getSelectedWorkflow());
			} else if (message instanceof WorkflowBundleSelectionEvent) {
				System.out.println("GraphViewComponent.WorkflowBundleSelectionEvent");
				setWorkflow(selectionManager.getSelectedWorkflow());
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
