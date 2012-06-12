/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.dndhandler.ServiceTransferHandler;
import net.sf.taverna.t2.workbench.views.graph.AutoScrollInteractor;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;

/**
 * Use to display the graph for previous workflow runs and allow the user to
 * click on processors to see the intermediate results pulled from provenance.
 *
 * @author Ian Dunlop
 *
 */
public class MonitorGraphPreviousRunComponent extends MonitorGraphComponent {

	private static final long serialVersionUID = 1L;

	private GVTTreeRendererAdapter gvtTreeBuilderAdapter;

	public MonitorGraphPreviousRunComponent(EditManager editManager, MenuManager menuManager, DataflowSelectionManager dataflowSelectionManager,
			ColourManager colourManager, WorkbenchConfiguration workbenchConfiguration) {
		super(editManager, menuManager, dataflowSelectionManager, colourManager, workbenchConfiguration);
		setLayout(new BorderLayout());

	}

	/**
	 * No need to monitor what is going on since this is a previous run. Just
	 * show the graph and use the {@link MonitorGraphComponent} mouse click to
	 * listen for user interaction
	 */
	@Override
	public GraphMonitor setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
		svgCanvas = new JSVGCanvas(null, true, false);
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
				svgScrollPane.reset();
				MonitorGraphPreviousRunComponent.this.revalidate();
			}
		};
		svgCanvas.addGVTTreeRendererListener(gvtTreeBuilderAdapter);

		// create a graph controller
		final SVGGraphController svgGraphController = new SVGGraphController(
				dataflow, true, svgCanvas, editManager, menuManager, colourManager, workbenchConfiguration);
		// For selections on the graph
		svgGraphController.setDataflowSelectionModel(dataflowSelectionManager.getDataflowSelectionModel(dataflow));
		svgGraphController.setAnimationSpeed(0);
		svgGraphController.setGraphEventManager(new MonitorGraphEventManager(
				this, provenanceConnector, dataflow, getSessionId()));
		svgGraphController.redraw();
		// Previous runs are passive
		JPanel diagramAndControls = new JPanel();
		diagramAndControls.setLayout(new BorderLayout());
		setGraphController(svgGraphController);
		diagramAndControls.add(graphActionsToolbar(), BorderLayout.NORTH);
		diagramAndControls.add(svgScrollPane, BorderLayout.CENTER);

		add(diagramAndControls, BorderLayout.CENTER);

		statusLabel = new JLabel();
		statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

		//add(statusLabel, BorderLayout.SOUTH);
		//setStatus(MonitorGraphComponent.Status.FINISHED);
		revalidate();

		// setProvenanceConnector();
		return null;
	}

	@SuppressWarnings("serial")
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
