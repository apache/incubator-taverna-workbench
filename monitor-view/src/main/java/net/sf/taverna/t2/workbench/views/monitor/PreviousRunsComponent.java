package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.ui.dndhandler.ServiceTransferHandler;
import net.sf.taverna.t2.workbench.views.graph.AutoScrollInteractor;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;

public class PreviousRunsComponent extends MonitorViewComponent{
	
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(PreviousRunsComponent.class);
	
	private JLabel statusLabel;
	
	private GVTTreeRendererAdapter gvtTreeBuilderAdapter;

	private JSVGCanvas svgCanvas;
	
	public PreviousRunsComponent(){
		setLayout(new BorderLayout());
		
	}
	
	
	
	@Override
	public Observer<MonitorMessage> setDataflow(Dataflow dataflow) {
		svgCanvas = new JSVGCanvas(null, true, false);
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
				svgScrollPane.reset();
				PreviousRunsComponent.this.revalidate();
			}
		};
		svgCanvas.addGVTTreeRendererListener(gvtTreeBuilderAdapter);

		// create a graph controller
		SVGGraphController svgGraphController = new SVGGraphController(dataflow, true, svgCanvas);		
		svgGraphController.setAnimationSpeed(0);
		svgGraphController.setGraphEventManager(new MonitorGraphEventManager(this, provenanceConnector, dataflow, getSessionId()));
		svgGraphController.redraw();
		JPanel diagramAndControls = new JPanel();
		diagramAndControls.setLayout(new BorderLayout());
		setGraphController(svgGraphController);
		diagramAndControls.add(graphActionsToolbar(), BorderLayout.NORTH);
		diagramAndControls.add(svgScrollPane, BorderLayout.CENTER);
		
		add(diagramAndControls, BorderLayout.CENTER);
		
		statusLabel = new JLabel();
		statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		add(statusLabel, BorderLayout.SOUTH);
		setStatus(MonitorViewComponent.Status.COMPLETE);
		revalidate();
//		setProvenanceConnector();
		return null;
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
