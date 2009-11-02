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
package net.sf.taverna.t2.workbench.models.graph;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.models.graph.GraphController.PortStyle;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;

/**
 * Manager for handling UI events on GraphElements.
 * 
 * @author David Withers
 */
public class DefaultGraphEventManager implements GraphEventManager {

	private GraphController graphController;

	private Component component;

	private JPopupMenu menu;
	
	private MenuManager menuManager = MenuManager.getInstance();

	/**
	 * Constructs a new instance of GraphEventManager.
	 *
	 * @param graphController
	 * @param component component to use when displaying popup menus
	 */
	public DefaultGraphEventManager(GraphController graphController, Component component) {
		this.graphController = graphController;
		this.component = component;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.EventGraphManager#mouseClicked(net.sf.taverna.t2.workbench.models.graph.GraphElement, short, boolean, boolean, boolean, int, int, int, int)
	 */
	public void mouseClicked(final GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, final int x, final int y, int screenX, int screenY) {
		Object dataflowObject = graphElement.getDataflowObject();
		
		// For both left and right click - add to selection model
		if (graphController.getDataflowSelectionModel() != null) {
			graphController.getDataflowSelectionModel().addSelection(dataflowObject);
		}
		
		// If this was a right click - show a pop-up as well
		if (button == 2) { 
			menu = null;
			if (dataflowObject instanceof Processor) {
				final Processor processor = (Processor) dataflowObject;
				menu = menuManager.createContextMenu(graphController.getDataflow(), processor, component);
				if (menu == null) {
					menu = new JPopupMenu();
				}
				if (graphElement instanceof GraphNode) {
					boolean expanded = false;
					if (graphController.getPortStyle(processor).equals(PortStyle.NONE)) {
						menu.addSeparator();
						menu.add(new JMenuItem(new AbstractAction("Show ports") {
							public void actionPerformed(ActionEvent arg0) {
								graphController.setPortStyle(processor, PortStyle.ALL);
								graphController.redraw();
							}
						}));
					} else if (graphController.getPortStyle(processor).equals(PortStyle.ALL)) {
						menu.addSeparator();
						menu.add(new JMenuItem(new AbstractAction("Hide ports") {
							public void actionPerformed(ActionEvent arg0) {
								graphController.setPortStyle(processor, PortStyle.NONE);
								graphController.redraw();
							}
						}));
					}
					if (!processor.getActivityList().isEmpty() && 
							processor.getActivityList().get(0) instanceof NestedDataflow) {						
						final NestedDataflow nestedDataflow = (NestedDataflow) processor.getActivityList().get(0);
						menu.addSeparator();
						menu.add(new JMenuItem(new AbstractAction("Show nested workflow") {
							public void actionPerformed(ActionEvent arg0) {
								graphController.setExpandNestedDataflow(nestedDataflow.getNestedDataflow(), true);
								graphController.redraw();
							}
						}));
					}
					menu.addSeparator();
					GraphNode graphNode = (GraphNode) graphElement;
					List<GraphNode> sourceNodes = graphNode.getSourceNodes();
					if (sourceNodes.size() > 0) {
						if (sourceNodes.size() == 1) {
							final GraphNode sourceNode = sourceNodes.get(0);
							if (sourceNode.getLabel() != null) {
								menu.add(new JMenuItem(new AbstractAction(
										"Link from output '"
												+ sourceNode.getLabel() + "'") {
									public void actionPerformed(ActionEvent arg0) {
										graphController.startEdgeCreation(
												sourceNode, new Point(x, y));
									}

								}));
							}
						} else {
							JMenu linkMenu = new JMenu("Link from output...");
							menu.add(linkMenu);
							for (final GraphNode sourceNode : sourceNodes) {
								linkMenu.add(new JMenuItem(new AbstractAction(sourceNode.getLabel()) {
									public void actionPerformed(ActionEvent arg0) {
										graphController.startEdgeCreation(sourceNode, new Point(x, y));
									}

								}));	
							}
						}
					}
					List<GraphNode> sinkNodes = graphNode.getSinkNodes();
					if (sinkNodes.size() > 0) {
						if (sinkNodes.size() == 1) {
							final GraphNode sinkNode = sinkNodes.get(0);
							if (sinkNode.getLabel() != null) {
								menu.add(new JMenuItem(new AbstractAction(
										"Link to input '" + sinkNode.getLabel()
												+ "'") {
									public void actionPerformed(ActionEvent arg0) {
										graphController.startEdgeCreation(
												sinkNode, new Point(x, y));
									}

								}));
							}
						} else {
							JMenu linkMenu = new JMenu("Link to input...");
							menu.add(linkMenu);
							for (final GraphNode sinkNode : sinkNodes) {
								linkMenu.add(new JMenuItem(new AbstractAction(sinkNode.getLabel()) {

									public void actionPerformed(ActionEvent arg0) {
										graphController.startEdgeCreation(sinkNode, new Point(x, y));
									}

								}));
							}
						}
					}
				} else if (graphElement instanceof Graph) {
					if (!processor.getActivityList().isEmpty() && 
							processor.getActivityList().get(0) instanceof NestedDataflow) {						
						final NestedDataflow nestedDataflow = (NestedDataflow) processor.getActivityList().get(0);
						menu.addSeparator();
						menu.add(new JMenuItem(new AbstractAction("Hide nested workflow") {
							public void actionPerformed(ActionEvent arg0) {
								graphController.setExpandNestedDataflow(nestedDataflow.getNestedDataflow(), false);
								graphController.redraw();
							}
						}));
					}
				}
			} else if (dataflowObject instanceof Merge) {
				Dataflow dataflow = graphController.getDataflow();
				menu = menuManager.createContextMenu(dataflow, dataflowObject, component);
			} else if (dataflowObject instanceof DataflowInputPort) {
				Dataflow dataflow = graphController.getDataflow();
				menu = menuManager.createContextMenu(dataflow, dataflowObject, component);
			} else if (dataflowObject instanceof DataflowOutputPort) {
				Dataflow dataflow = graphController.getDataflow();
				menu = menuManager.createContextMenu(dataflow, dataflowObject, component);
			} else if (dataflowObject instanceof ActivityInputPort) {
				Dataflow dataflow = graphController.getDataflow();
				menu = menuManager.createContextMenu(dataflow, dataflowObject, component);
			} else if (dataflowObject instanceof ActivityOutputPort) {
				Dataflow dataflow = graphController.getDataflow();
				menu = menuManager.createContextMenu(dataflow, dataflowObject, component);
			} else if (dataflowObject instanceof Datalink) {
				Dataflow dataflow = graphController.getDataflow();
				menu = menuManager.createContextMenu(dataflow, dataflowObject, component);
			} else if (dataflowObject instanceof Condition) {
				Dataflow dataflow = graphController.getDataflow();
				menu = menuManager.createContextMenu(dataflow, dataflowObject, component);
			} else if (dataflowObject instanceof Dataflow || dataflowObject == null) {
				Dataflow dataflow = graphController.getDataflow();
				menu = menuManager.createContextMenu(dataflow, dataflow, component);
			}
			if (menu != null) {
				final Point p = new Point(screenX, screenY);
				SwingUtilities.convertPointFromScreen(p, component);
				SwingUtilities.invokeLater(new Runnable() {
		            public void run(){
		            	menu.show(component, p.x, p.y);
		            }
		        });
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.EventGraphManager#mouseDown(net.sf.taverna.t2.workbench.models.graph.GraphElement, short, boolean, boolean, boolean, int, int, int, int)
	 */
	public void mouseDown(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x,
			int y, int screenX, int screenY) {
		if (button == 0) {
			graphController.startEdgeCreation(graphElement, new Point(x, y));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.EventGraphManager#mouseUp(net.sf.taverna.t2.workbench.models.graph.GraphElement, short, boolean, boolean, boolean, int, int, int, int)
	 */
	public void mouseUp(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, final int x,
			final int y, int screenX, int screenY) {
		if (button == 0) {
			graphController.stopEdgeCreation(graphElement, new Point(screenX, screenY));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.EventGraphManager#mouseMoved(net.sf.taverna.t2.workbench.models.graph.GraphElement, short, boolean, boolean, boolean, int, int, int, int)
	 */
	public void mouseMoved(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x,
			int y, int screenX, int screenY) {
		graphController.moveEdgeCreationTarget(graphElement, new Point(x, y));
	}

	public void mouseOver(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		if (graphElement.getDataflowObject() != null) {
			graphElement.setActive(true);
		}
	}

	public void mouseOut(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		if (graphElement.getDataflowObject() != null) {
			graphElement.setActive(false);
		}
	}

}
