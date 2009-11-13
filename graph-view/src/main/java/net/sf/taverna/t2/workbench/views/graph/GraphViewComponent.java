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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelDestroyedEvent;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowOpener;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.GraphController.PortStyle;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.ui.dndhandler.ServiceTransferHandler;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workbench.views.graph.config.GraphViewConfiguration;
import net.sf.taverna.t2.workbench.views.graph.menu.ResetDiagramAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomInAction;
import net.sf.taverna.t2.workbench.views.graph.menu.ZoomOutAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;

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

	private static GraphViewConfiguration configuration = GraphViewConfiguration
			.getInstance();

	private SVGGraphController graphController;

	private JPanel diagramPanel;

	public static Map<Dataflow, SVGGraphController> graphControllerMap = new HashMap<Dataflow, SVGGraphController>();

	public static Map<Dataflow, JPanel> diagramPanelMap = new HashMap<Dataflow, JPanel>();

	private Dataflow dataflow;

	private Timer timer;

	private GVTTreeRendererAdapter gvtTreeBuilderAdapter;

	private CardLayout cardLayout;

	public GraphViewComponent() {
		cardLayout = new CardLayout();
		setLayout(cardLayout);

		TitledBorder border = new TitledBorder("Workflow diagram");
		border.setTitleJustification(TitledBorder.CENTER);
		setBorder(border);

		ModelMap.getInstance().addObserver(new ModelMapObserver());

		EditManager.getInstance().addObserver(new EditManagerObserver());

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

	private JPanel createDiagramPanel(Dataflow dataflow) {
		JPanel diagramPanel = new JPanel(new BorderLayout());

		// get the default diagram settings
		Alignment alignment = Alignment.valueOf(configuration
				.getProperty(GraphViewConfiguration.ALIGNMENT));
		PortStyle portStyle = PortStyle.valueOf(configuration
				.getProperty(GraphViewConfiguration.PORT_STYLE));
		boolean animationEnabled = Boolean.parseBoolean(configuration
				.getProperty(GraphViewConfiguration.ANIMATION_ENABLED));
		int animationSpeed = Integer.valueOf(configuration
				.getProperty(GraphViewConfiguration.ANIMATION_SPEED));

		// create an SVG canvas
		final JSVGCanvas svgCanvas = new JSVGCanvas(null, true, false);
		svgCanvas.setEnableZoomInteractor(false);
		svgCanvas.setEnableRotateInteractor(false);
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		svgCanvas.setTransferHandler(new ServiceTransferHandler());

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
				dataflow, false, svgCanvas, alignment, portStyle);
		svgGraphController.setDataflowSelectionModel(DataflowSelectionManager
				.getInstance().getDataflowSelectionModel(dataflow));
		svgGraphController.setAnimationSpeed(animationEnabled ? animationSpeed
				: 0);

		graphControllerMap.put(dataflow, svgGraphController);

		// Toolbar with actions related to graph
		JToolBar graphActionsToolbar = graphActionsToolbar(svgGraphController,
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
	private JToolBar graphActionsToolbar(
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
				graphController.setPortStyle(GraphController.PortStyle.NONE);
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
				graphController.setPortStyle(GraphController.PortStyle.ALL);
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
				graphController.setPortStyle(GraphController.PortStyle.BLOB);
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
	 * Sets the Dataflow to display in the graph view.
	 * 
	 * @param dataflow
	 */
	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
		if (!diagramPanelMap.containsKey(dataflow)) {
			JPanel newDiagramPanel = createDiagramPanel(dataflow);
			add(newDiagramPanel, String.valueOf(newDiagramPanel.hashCode()));
			diagramPanelMap.put(dataflow, newDiagramPanel);
		}
		graphController = graphControllerMap.get(dataflow);
		diagramPanel = diagramPanelMap.get(dataflow);
		cardLayout.show(this, String.valueOf(diagramPanel.hashCode()));
		graphController.redraw();
	}

	/**
	 * Returns the dataflow.
	 * 
	 * @return the dataflow
	 */
	public Dataflow getDataflow() {
		return dataflow;
	}

	/**
	 * For testing only
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("raven.eclipse", "true");
		System.setProperty("taverna.dotlocation",
				"/Applications/Taverna-1.7.1.app/Contents/MacOS/dot");
		// System.setProperty("taverna.dotlocation", "/opt/local/bin/dot");

		GraphViewComponent graphView = new GraphViewComponent();

		T2DataflowOpener t2DataflowOpener = new T2DataflowOpener();
		InputStream stream = GraphViewComponent.class
				.getResourceAsStream("/nested_iteration.t2flow");
		Dataflow dataflow = t2DataflowOpener.openDataflow(new T2FlowFileType(),
				stream).getDataflow();

		JFrame frame = new JFrame();
		frame.add(graphView);
		frame.setPreferredSize(new Dimension(600, 800));
		frame.pack();
		graphView.setDataflow(dataflow);
		frame.setVisible(true);

	}

	@Override
	public String getName() {
		return "Graph View Component";
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
	}

	public void onDispose() {
		if (timer != null) {
			timer.stop();
		}
		// if (svgScrollPane != null) {
		// svgScrollPane.removeAll();
		// svgScrollPane = null;
		// }
		// if (svgCanvas != null) {
		// svgCanvas.stopProcessing();
		// svgCanvas.removeGVTTreeRendererListener(gvtTreeBuilderAdapter);
		// svgCanvas = null;
		// }
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
			Runnable doRun = new Runnable() {
				public void run() {
					AbstractDataflowEditEvent dataflowEditEvent = (AbstractDataflowEditEvent) message;
					if (dataflowEditEvent.getDataFlow() == dataflow) {
						graphController.redraw();
					}
				}
			};
			Runnable redraw = new Runnable() {
				public void run() {
					AbstractDataflowEditEvent dataflowEditEvent = (AbstractDataflowEditEvent) message;
					if (dataflowEditEvent.getDataFlow() == dataflow) {
						graphController.redraw();
					}
				}
			};
			// Seems to hang diagram at times.. things not happening in right order?
			//SwingUtilities.invokeLater(redraw);	
			// Run it directly for now
			redraw.run();
		}
	}

	public class ModelMapObserverRunnable implements Runnable {
		private final ModelMapEvent message;

		public ModelMapObserverRunnable(ModelMapEvent message) {
			this.message = message;
		}

		public void run() {
			if (message instanceof ModelDestroyedEvent) {
				if (message.getOldModel() instanceof Dataflow) {
					JPanel panel = diagramPanelMap.remove((Dataflow) message
							.getOldModel());
					if (panel != null) {
						remove(panel);
					}
					graphControllerMap.remove((Dataflow) message.getOldModel());
				}
			} else {
				if (message.getModelName().equals(
						ModelMapConstants.CURRENT_DATAFLOW)) {
					if (message.getNewModel() instanceof Dataflow) {
						setDataflow((Dataflow) message.getNewModel());
					}
				}
			}
		}
	}

	public class ModelMapObserver implements Observer<ModelMap.ModelMapEvent> {

		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) {
			ModelMapObserverRunnable runnable = new ModelMapObserverRunnable(message);
			// Seems to hang diagram at times.. things not happening in right order?		
			//SwingUtilities.invokeLater(runnable);
			// instead - run directly
			runnable.run();
		}
		

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
