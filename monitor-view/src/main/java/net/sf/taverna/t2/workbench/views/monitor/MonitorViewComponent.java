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
import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.provenance.ProvenanceConnectorRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.provenance.ProvenanceConfiguration;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;

public class MonitorViewComponent extends JPanel implements UIComponentSPI {

	private static Logger logger = Logger.getLogger(MonitorViewComponent.class);

	private static final long serialVersionUID = 1L;

	private SVGGraphController graphController;

	private JSVGCanvas svgCanvas;

	private ProvenanceConnector provenanceConnector;

	private Dataflow dataflow;

	public MonitorViewComponent() {
		super(new BorderLayout());
		setBorder(LineBorder.createGrayLineBorder());

		svgCanvas = new JSVGCanvas();
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);

		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
				graphController.setUpdateManager(svgCanvas.getUpdateManager());
			}
		});
		add(svgCanvas, BorderLayout.CENTER);
		setProvenanceConnector();
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
				}
			}
		}
	}

	public Observer<MonitorMessage> setDataflow(Dataflow dataflow) {
		graphController = new SVGGraphController(dataflow,
				new MonitorGraphEventManager(provenanceConnector, dataflow),
				this) {
			public void redraw() {
				svgCanvas.setDocument(graphController
						.generateSVGDocument(getBounds()));
			}
		};
		svgCanvas.setDocument(graphController.generateSVGDocument(getBounds()));
		// revalidate();
		return new GraphMonitor(graphController);
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

}

class MonitorGraphEventManager implements GraphEventManager {

	private static Logger logger = Logger
			.getLogger(MonitorGraphEventManager.class);
	private final ProvenanceConnector provenanceConnector;
	private final Dataflow dataflow;
	private String localName;

	public MonitorGraphEventManager(ProvenanceConnector provenanceConnector,
			Dataflow dataflow) {
		this.provenanceConnector = provenanceConnector;
		this.dataflow = dataflow;
	}

	/**
	 * Retrieve the provenance for a dataflow object
	 */
	public void mouseClicked(final GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {

		Object dataflowObject = graphElement.getDataflowObject();
		// no popup if provenance is switched off
		if (provenanceConnector != null) {
			if (dataflowObject != null) {
				if (dataflowObject instanceof Processor) {
					localName = ((Processor) dataflowObject).getLocalName();
					JFrame frame = new JFrame();
					JPanel panel = new JPanel();
					panel.setLayout(new BorderLayout());
					JPanel topPanel = new JPanel();
					topPanel.setLayout(new BorderLayout());
					JLabel label = new JLabel();
					final JPanel provenancePanel = new JPanel();
					provenancePanel.setLayout(new BorderLayout());
					JButton retrieveProvenanceButton = new JButton(
							"Retrieve Intermediate Results");
					retrieveProvenanceButton
							.addActionListener(new AbstractAction() {

								private String intermediateValues;

								public void actionPerformed(ActionEvent e) {
									if (provenanceConnector != null) {
										String internalIdentier = dataflow
												.getInternalIdentier();
										final String dataflowInstanceID = provenanceConnector
												.getDataflowInstance(internalIdentier);
										new Thread(
												"Retrieve intermediate results for dataflow: "
														+ internalIdentier
														+ ", processor: "
														+ localName) {
											@Override
											public void run() {

												try {
													intermediateValues = provenanceConnector
															.getIntermediateValues(
																	dataflowInstanceID,
																	localName,
																	null, null);
													JEditorPane editorPane = new JEditorPane(
															"text/html",
															intermediateValues);
													editorPane
															.setEditable(false);
													JScrollPane scrollPane = new JScrollPane(
															editorPane);
													provenancePanel
															.add(
																	scrollPane,
																	BorderLayout.CENTER);
													provenancePanel
															.revalidate();
													provenancePanel
															.setVisible(true);

												} catch (SQLException e) {
													logger
															.warn("Could not retrieve intermediate results: "
																	+ e);
													JOptionPane
															.showMessageDialog(
																	null,
																	"Could not retrieve intermediate results:\n"
																	+ e,
																	"Problem retrieving results",
																	JOptionPane.ERROR_MESSAGE);
												}
											}
										}.start();

									}
								}
							});

					topPanel.add(label, BorderLayout.NORTH);
					topPanel.add(retrieveProvenanceButton, BorderLayout.SOUTH);
					panel.add(topPanel, BorderLayout.NORTH);
//					panel.add(label);
//					panel.add(retrieveProvenanceButton);
					panel.add(provenancePanel, BorderLayout.CENTER);
					provenancePanel.setVisible(false);
					frame.add(panel);
					label.setText("You selected processor " + localName);
					frame.setVisible(true);
					frame.setSize(300, 300);

				}
				// else if (dataflowObject instanceof OutputPort) {
				// String localName = ((OutputPort) dataflowObject).getName();
				// JFrame frame = new JFrame();
				// JPanel panel = new JPanel();
				// JLabel label = new JLabel();
				// final JPanel provenancePanel = new JPanel();
				// JButton retrieveProvenanceButton = new JButton(
				// "Retrieve Intermediate Results");
				// retrieveProvenanceButton
				// .addActionListener(new AbstractAction() {
				//
				// public void actionPerformed(ActionEvent e) {
				// if (provenanceConnector != null) {
				// provenancePanel.add(new JLabel(
				// "I would have retrieved some provenance from the "
				// + provenanceConnector
				// .getName()));
				// } else {
				// provenancePanel.add(new JLabel(
				// "Provenance is switched off"));
				// }
				// provenancePanel.revalidate();
				// }
				// });
				//
				// provenancePanel.setBorder(BorderFactory.createEtchedBorder());
				// panel.add(retrieveProvenanceButton);
				// panel.add(provenancePanel);
				// panel.add(label);
				// frame.add(panel);
				// label.setText("You clicked on output port " + localName);
				// frame.setVisible(true);
				// } else if (dataflowObject instanceof InputPort) {
				// String localName = ((InputPort) dataflowObject).getName();
				// JFrame frame = new JFrame();
				// JPanel panel = new JPanel();
				// JLabel label = new JLabel();
				// final JPanel provenancePanel = new JPanel();
				// JButton retrieveProvenanceButton = new JButton(
				// "Retrieve Intermediate Results");
				// retrieveProvenanceButton
				// .addActionListener(new AbstractAction() {
				//
				// public void actionPerformed(ActionEvent e) {
				// if (provenanceConnector != null) {
				// provenancePanel.add(new JLabel(
				// "I would have retrieved some provenance from the "
				// + provenanceConnector
				// .getName()));
				// } else {
				// provenancePanel.add(new JLabel(
				// "Provenance is switched off"));
				// }
				// provenancePanel.revalidate();
				// }
				// });
				//
				// provenancePanel.setBorder(BorderFactory.createEtchedBorder());
				// panel.add(retrieveProvenanceButton);
				// panel.add(provenancePanel);
				// panel.add(label);
				// frame.add(panel);
				// label.setText("You clicked on input port " + localName);
				// frame.setVisible(true);
				// } else if (dataflowObject instanceof Datalink) {
				// String outputName = ((Datalink) dataflowObject).getSink()
				// .getName();
				// String inputName = ((Datalink) dataflowObject).getSource()
				// .getName();
				// JFrame frame = new JFrame();
				// JPanel panel = new JPanel();
				// JLabel label = new JLabel();
				// final JPanel provenancePanel = new JPanel();
				// JButton retrieveProvenanceButton = new JButton(
				// "Retrieve Intermediate Results");
				// retrieveProvenanceButton
				// .addActionListener(new AbstractAction() {
				//
				// public void actionPerformed(ActionEvent e) {
				// if (provenanceConnector != null) {
				// provenancePanel.add(new JLabel(
				// "I would have retrieved some provenance from the "
				// + provenanceConnector
				// .getName()));
				// } else {
				// provenancePanel.add(new JLabel(
				// "Provenance is switched off"));
				// }
				// provenancePanel.revalidate();
				// }
				// });
				//
				// provenancePanel.setBorder(BorderFactory.createEtchedBorder());
				// panel.add(retrieveProvenanceButton);
				// panel.add(provenancePanel);
				// panel.add(label);
				// frame.add(panel);
				// label.setText("You clicked the link from " + inputName +
				// " to "
				// + outputName);
				// frame.setVisible(true);
				// }
			}
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
