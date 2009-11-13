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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge.ArrowStyle;
import net.sf.taverna.t2.workbench.models.graph.GraphElement.LineStyle;
import net.sf.taverna.t2.workbench.models.graph.GraphShapeElement.Shape;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.utils.PortComparator;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

/**
 * 
 * 
 * @author David Withers
 */
public abstract class GraphController implements Observer<DataflowSelectionMessage> {

	public enum PortStyle {
		ALL {
			Shape inputShape() {return Shape.INVHOUSE;}
			Shape outputShape() {return Shape.HOUSE;}
			Shape processorShape() {return Shape.RECORD;}
		},
		BOUND {
			Shape inputShape() {return Shape.INVHOUSE;}
			Shape outputShape() {return Shape.HOUSE;}
			Shape processorShape() {return Shape.RECORD;}
		},
		NONE {
			Shape inputShape() {return Shape.BOX;}
			Shape outputShape() {return Shape.BOX;}
			Shape processorShape() {return Shape.BOX;}
		},
		BLOB {
			Shape inputShape() {return Shape.CIRCLE;}
			Shape outputShape() {return Shape.CIRCLE;}
			Shape processorShape() {return Shape.CIRCLE;}
		};
		
		abstract Shape inputShape();

		abstract Shape outputShape();

		abstract Shape processorShape();

		Shape mergeShape() {return Shape.CIRCLE;}

	}
	
	private static Logger logger = Logger.getLogger(GraphController.class);
	
	private Map<String, GraphElement> idToElement = new HashMap<String, GraphElement>();

	private Map<Object, GraphElement> dataflowToGraph = new HashMap<Object, GraphElement>();

	private Map<Port, GraphNode> ports = new HashMap<Port, GraphNode>();

	private Map<Graph, GraphNode> inputControls = new HashMap<Graph, GraphNode>();
	
	private Map<Graph, GraphNode> outputControls = new HashMap<Graph, GraphNode>();

	private Map<Port, Port> nestedDataflowPorts = new HashMap<Port, Port>();
	
	private Map<DataflowPort, Port> dataflowToActivityPort = new HashMap<DataflowPort, Port>();
	
	private Map<Port, Activity<?>> portToActivity = new HashMap<Port, Activity<?>>();
	
	private Map<Port, Processor> portToProcessor = new HashMap<Port, Processor>();
	
	private Edits edits = EditsRegistry.getEdits();
	
	private EditManager editManager = EditManager.getInstance();

	private Dataflow dataflow;
	
	private DataflowSelectionModel dataflowSelectionModel;
	
	private GraphEventManager graphEventManager;
	
	private Component componentForPopups;
	
	//graph settings	
	private PortStyle portStyle = PortStyle.NONE;
	
	private Map<Processor, PortStyle> processorPortStyle = new HashMap<Processor, PortStyle>();
	
	private Alignment alignment = Alignment.VERTICAL;
	
	private boolean expandNestedDataflows = true;

	private Map<Dataflow, Boolean> dataflowExpansion = new HashMap<Dataflow, Boolean>();
	
	private boolean showMerges = true;

	protected Map<String, GraphElement> graphElementMap = new HashMap<String, GraphElement>();

	protected GraphElement edgeCreationSource, edgeCreationSink;
	
	protected GraphEdge edgeMoveElement;
	
	protected boolean edgeCreationFromSource = false;
	
	protected boolean edgeCreationFromSink = false;

	private Graph graph;

	private boolean interactive;
	
	private final PortComparator portComparator = new PortComparator();
	
	public GraphController(Dataflow dataflow, boolean interactive, Component componentForPopups) {
		this(dataflow, interactive, componentForPopups, Alignment.VERTICAL, PortStyle.NONE);
	}
	
	public GraphController(Dataflow dataflow, boolean interactive, Component componentForPopups, Alignment alignment, PortStyle portStyle) {
		this.dataflow = dataflow;
		this.interactive = interactive;
		this.componentForPopups = componentForPopups;
		this.alignment = alignment;
		this.portStyle = portStyle;
		this.graphEventManager = new DefaultGraphEventManager(this, componentForPopups);
		graph = generateGraph();
	}

	public abstract Graph createGraph();
	
	public abstract GraphNode createGraphNode();
	
	public abstract GraphEdge createGraphEdge();
	
	public void mapElement(String id, GraphElement element) {
		idToElement.put(id, element);
	}
	
	public GraphElement getElement(String id) {
		return idToElement.get(id);
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public abstract void redraw();
	
	/**
	 * Generates a graph model of a dataflow.
	 * 
	 * @return
	 */
	public Graph generateGraph() {
		dataflowToGraph.clear();
		ports.clear();
		inputControls.clear();
		outputControls.clear();
		nestedDataflowPorts.clear();
		dataflowToActivityPort.clear();
		graphElementMap.clear();
		portToActivity.clear();
		return generateGraph(dataflow, "", dataflow.getLocalName(), 0);
	}
	
	private Graph generateGraph(Dataflow dataflow, String prefix, String name, int depth) {
		Graph graph = createGraph();
		graph.setId(prefix + name);
		graph.setAlignment(getAlignment());
		if (getPortStyle().equals(PortStyle.BLOB) || depth == 0) {
			graph.setLabel("");
		} else {
			graph.setLabel(name);
		}
		graph.setFillColor(GraphColorManager.getSubGraphFillColor(depth));
		if (depth == 0) {
			graph.setLineStyle(LineStyle.NONE);
		} else {
			graph.setLineStyle(LineStyle.SOLID);
		}
		graph.setColor(Color.BLACK);
		graph.setShape(Shape.BOX);

		if (depth == 0) {
			graph.setDataflowObject(dataflow);
		}
		if (interactive) {
			graph.setDataflowObject(dataflow);
		}
		
		//processors
		for (Processor processor : dataflow.getProcessors()) {
			graph.addNode(generateProcessorNode(processor, graph.getId(), depth));
		}

		//merges
		for (Merge merge : dataflow.getMerges()) {
			if (showMerges) {
				graph.addNode(generateMergeNode(merge, graph.getId(), depth));				
			} else {
				Port sinkPort = null;
				for (Datalink datalink : merge.getOutputPort().getOutgoingLinks()) {
					sinkPort = datalink.getSink();
					break;
				}
				if (sinkPort != null) {
					if (nestedDataflowPorts.containsKey(sinkPort)) {
						sinkPort = nestedDataflowPorts.get(sinkPort);
					}
					GraphNode sinkNode = ports.get(sinkPort);
					for (MergeInputPort inputPort : merge.getInputPorts()) {
						ports.put(inputPort, sinkNode);
					}					
				}
			}
		}
		
		//dataflow outputs
		List<? extends DataflowOutputPort> outputPorts = dataflow.getOutputPorts();
		if (outputPorts.size() > 0 || depth > 0) {
			graph.addSubgraph(generateOutputsGraph(outputPorts, graph.getId(), graph, depth));
		}

		//dataflow inputs
		List<? extends DataflowInputPort> inputPorts = dataflow.getInputPorts();
		if (inputPorts.size() > 0 || depth > 0) {
			graph.addSubgraph(generateInputsGraph(inputPorts, graph.getId(), graph, depth));
		}

		//datalinks
		for (Datalink datalink : dataflow.getLinks()) {
			GraphEdge edge = generateDatalinkEdge(datalink, depth);
			if (edge != null) {
				graph.addEdge(edge);
			}
		}

		//conditions
		for (Processor processor : dataflow.getProcessors()) {
			GraphElement sink = dataflowToGraph.get(processor);
			for (Condition condition : processor.getPreconditionList()) {
				GraphEdge edge = generateControlEdge(condition, sink, depth);
				if (edge != null) {
					graph.addEdge(edge);
				}
			}
		}
		
		graphElementMap.put(graph.getId(), graph);
		return graph;
	}

	public void transformGraph(Graph oldGraph, Graph newGraph) {
		oldGraph.setAlignment(newGraph.getAlignment());
		transformGraphElement(oldGraph, newGraph);
		List<GraphEdge> oldEdges = new ArrayList<GraphEdge>(oldGraph.getEdges());
		List<GraphEdge> newEdges = new ArrayList<GraphEdge>(newGraph.getEdges());
		for (GraphEdge oldEdge : oldEdges) {
			int index = newEdges.indexOf(oldEdge);
			if (index >= 0) {
				GraphEdge newEdge = newEdges.remove(index);
				oldEdge.setPath(newEdge.getPath());
				dataflowToGraph.put(oldEdge.getDataflowObject(), oldEdge);
			} else {
				oldGraph.removeEdge(oldEdge);
			}
		}
		List<GraphNode> newNodes = new ArrayList<GraphNode>(newGraph.getNodes());
		List<GraphNode> oldNodes = new ArrayList<GraphNode>(oldGraph.getNodes());
		for (GraphNode oldNode : oldNodes) {
			int index = newNodes.indexOf(oldNode);
			if (index >= 0) {
				GraphNode newNode = newNodes.remove(index);
				oldNode.setExpanded(newNode.isExpanded());
				List<GraphNode> newSourceNodes = new ArrayList<GraphNode>(newNode.getSourceNodes());
				List<GraphNode> oldSourceNodes = new ArrayList<GraphNode>(oldNode.getSourceNodes());
				for (GraphNode oldSourceNode : oldSourceNodes) {
					int sourceNodeIndex = newSourceNodes.indexOf(oldSourceNode);
					if (sourceNodeIndex >= 0) {
						GraphNode newSourceNode = newSourceNodes.remove(sourceNodeIndex);
						transformGraphElement(oldSourceNode, newSourceNode);
					} else {
						oldNode.removeSourceNode(oldSourceNode);
					}
				}
				for (GraphNode sourceNode : newSourceNodes) {
					oldNode.addSourceNode(sourceNode);
				}
				List<GraphNode> newSinkNodes = new ArrayList<GraphNode>(newNode.getSinkNodes());
				List<GraphNode> oldSinkNodes = new ArrayList<GraphNode>(oldNode.getSinkNodes());
				for (GraphNode oldSinkNode : oldSinkNodes) {
					int sinkNodeIndex = newSinkNodes.indexOf(oldSinkNode);
					if (sinkNodeIndex >= 0) {
						GraphNode newSinkNode = newSinkNodes.remove(sinkNodeIndex);
						transformGraphElement(oldSinkNode, newSinkNode);
					} else {
						oldNode.removeSinkNode(oldSinkNode);
					}
				}
				for (GraphNode sinkNode : newSinkNodes) {
					oldNode.addSinkNode(sinkNode);
				}
				Graph oldSubGraph = oldNode.getGraph();
				Graph newSubGraph = newNode.getGraph();
				if (oldSubGraph != null && newSubGraph != null) {
					transformGraph(oldSubGraph, newSubGraph);
				}
				transformGraphElement(oldNode, newNode);
			} else {
				oldGraph.removeNode(oldNode);
			}
		}
		List<Graph> newSubGraphs = new ArrayList<Graph>(newGraph.getSubgraphs());
		List<Graph> oldSubGraphs = new ArrayList<Graph>(oldGraph.getSubgraphs());
		for (Graph oldSubGraph : oldSubGraphs) {
			int index = newSubGraphs.indexOf(oldSubGraph);
			if (index >= 0) {
				Graph newSubGraph = newSubGraphs.remove(index);
				transformGraph(oldSubGraph, newSubGraph);
			} else {
				oldGraph.removeSubgraph(oldSubGraph);
			}
		}
		for (GraphNode node : newNodes) {
			oldGraph.addNode(node);
		}
		for (Graph graph : newSubGraphs) {
			oldGraph.addSubgraph(graph);
		}
		for (GraphEdge newEdge : newEdges) {
			oldGraph.addEdge(newEdge);
		}
	}
	
	public void transformGraphElement(GraphShapeElement oldGraphElement, GraphShapeElement newGraphElement) {
		oldGraphElement.setDataflowObject(newGraphElement.getDataflowObject());
		oldGraphElement.setShape(newGraphElement.getShape());
		oldGraphElement.setSize(newGraphElement.getSize());
		oldGraphElement.setPosition(newGraphElement.getPosition());
		oldGraphElement.setLabel(newGraphElement.getLabel());
		oldGraphElement.setLabelPosition(newGraphElement.getLabelPosition());
		oldGraphElement.setLineStyle(newGraphElement.getLineStyle());
		oldGraphElement.setOpacity(newGraphElement.getOpacity());
		oldGraphElement.setVisible(newGraphElement.isVisible());
		oldGraphElement.setColor(newGraphElement.getColor());
		oldGraphElement.setFillColor(newGraphElement.getFillColor());
		dataflowToGraph.put(oldGraphElement.getDataflowObject(), oldGraphElement);
	}
	
	public void filterGraph(Set<?> dataflowEntities) {
		Set<GraphElement> graphElements = new HashSet<GraphElement>();
		for (Entry<Object, GraphElement> entry : dataflowToGraph.entrySet()) {
			if (!dataflowEntities.contains(entry.getKey())) {
				graphElements.add(entry.getValue());
			}
		}
		filterGraph(getGraph(), graphElements);
	}
	
	private void filterGraph(Graph graph, Set<GraphElement> graphElements) {
		for (GraphNode node : graph.getNodes()) {
			node.setFiltered(graphElements.contains(node));
			Graph subgraph = node.getGraph();
			if (subgraph != null) {
				if (graphElements.contains(subgraph)) {
					removeFilter(subgraph);
					subgraph.setFiltered(true);
				} else {
					subgraph.setFiltered(false);
					filterGraph(subgraph, graphElements);
				}
			}
		}
		for (GraphEdge edge : graph.getEdges()) {
				edge.setFiltered(graphElements.contains(edge));
		}
		for (Graph subgraph : graph.getSubgraphs()) {
			if (graphElements.contains(subgraph)) {
				removeFilter(subgraph);
				subgraph.setFiltered(true);
			} else {
				subgraph.setFiltered(false);
				filterGraph(subgraph, graphElements);
			}
		}
	}
	
	public void removeFilter() {
		for (Entry<Object, GraphElement> entry : dataflowToGraph.entrySet()) {
			entry.getValue().setFiltered(false);
		}
	}
	
	private void removeFilter(Graph graph) {
		for (GraphNode node : graph.getNodes()) {
			node.setOpacity(1f);
			Graph subgraph = node.getGraph();
			if (subgraph != null) {
				subgraph.setFiltered(false);
				removeFilter(subgraph);
			}
		}
		for (GraphEdge edge : graph.getEdges()) {
			edge.setFiltered(false);
		}
		for (Graph subgraph : graph.getSubgraphs()) {
			subgraph.setFiltered(false);
			removeFilter(subgraph);
		}
	}
	
	private GraphEdge generateControlEdge(Condition condition, GraphElement sink, int depth) {
		GraphEdge edge = null;
		GraphElement source = dataflowToGraph.get(condition.getControl());
		if (source != null && sink != null) {
			edge = createGraphEdge();
			if (source instanceof Graph) {
				edge.setSource(outputControls.get(source));
			} else if (source instanceof GraphNode) {
				edge.setSource((GraphNode) source);
			}
			if (sink instanceof Graph) {
				edge.setSink(inputControls.get(sink));
			} else if (sink instanceof GraphNode) {
				edge.setSink((GraphNode) sink);
			}
			String sourceId = edge.getSource().getId();
			String sinkId = edge.getSink().getId();
			edge.setId(sourceId + "->" + sinkId);
			edge.setLineStyle(LineStyle.SOLID);
			edge.setColor(Color.decode("#505050"));
			edge.setFillColor(null);
			edge.setArrowHeadStyle(ArrowStyle.DOT);
			if (depth == 0) {
				edge.setDataflowObject(condition);
			}
			if (interactive) {
				edge.setDataflowObject(condition);
			}
			dataflowToGraph.put(condition, edge);
			graphElementMap.put(edge.getId(), edge);
		}
		return edge;
	}

	private GraphEdge generateDatalinkEdge(Datalink datalink, int depth) {
		GraphEdge edge = null;
		Port sourcePort = datalink.getSource();
		Port sinkPort = datalink.getSink();
		if (sourcePort instanceof ProcessorOutputPort) {
			ProcessorOutputPort processorOutputPort = (ProcessorOutputPort) sourcePort;
			List<? extends Activity<?>> activities = processorOutputPort.getProcessor().getActivityList();
			if (activities.size() > 0) {
				String activityPortName = activities.get(0).getOutputPortMapping().get(sourcePort.getName());
				for (Port port : activities.get(0).getOutputPorts()) {
					if (port.getName().equals(activityPortName)) {
						sourcePort = port;
						break;
					}
				}
			}
		}
		if (sinkPort instanceof ProcessorInputPort) {
			ProcessorInputPort processorInputPort = (ProcessorInputPort) sinkPort;
			List<? extends Activity<?>> activities = processorInputPort.getProcessor().getActivityList();
			if (activities.size() > 0) {
				String activityPortName = activities.get(0).getInputPortMapping().get(sinkPort.getName());
				for (Port port : activities.get(0).getInputPorts()) {
					if (port.getName().equals(activityPortName)) {
						sinkPort = port;
						break;
					}
				}
			}
		}
		if (nestedDataflowPorts.containsKey(sourcePort)) {
			sourcePort = nestedDataflowPorts.get(sourcePort);
		}
		if (nestedDataflowPorts.containsKey(sinkPort)) {
			sinkPort = nestedDataflowPorts.get(sinkPort);
		}
		GraphNode sourceNode = ports.get(sourcePort);
		GraphNode sinkNode = ports.get(sinkPort);
		if (sourceNode != null && sinkNode != null) {
			edge = createGraphEdge();
			edge.setSource(sourceNode);
			edge.setSink(sinkNode);

			StringBuilder id = new StringBuilder();
			if (sourceNode.getParent() instanceof GraphNode) {
				id.append(sourceNode.getParent().getId());
				id.append(":");
				id.append(sourceNode.getId());
			} else {
				id.append(sourceNode.getId());
			}
			id.append("->");
			if (sinkNode.getParent() instanceof GraphNode) {
				id.append(sinkNode.getParent().getId());
				id.append(":");
				id.append(sinkNode.getId());
			} else {
				id.append(sinkNode.getId());
			}
			edge.setId(id.toString());
			edge.setLineStyle(LineStyle.SOLID);
			edge.setColor(Color.BLACK);
			edge.setFillColor(Color.BLACK);
			if (depth == 0) {
				edge.setDataflowObject(datalink);
			}
			if (interactive) {
				edge.setDataflowObject(datalink);
			}
			dataflowToGraph.put(datalink, edge);
			graphElementMap.put(edge.getId(), edge);
		}
		return edge;
	}

	private Graph generateInputsGraph(List<? extends DataflowInputPort> inputPorts, String prefix, Graph graph, int depth) {
		Graph inputs = createGraph();
		inputs.setId(prefix + "sources");
		inputs.setColor(Color.BLACK);
		inputs.setFillColor(null);
		inputs.setShape(Shape.BOX);
		inputs.setLineStyle(LineStyle.DOTTED);
		if (getPortStyle().equals(PortStyle.BLOB)) {
			inputs.setLabel("");
		} else {
			inputs.setLabel("Workflow input ports");
		}

		GraphNode triangle = createGraphNode();
		triangle.setId(prefix + "WORKFLOWINTERNALSOURCECONTROL");
		triangle.setLabel("");
		triangle.setShape(Shape.TRIANGLE);
		triangle.setSize(new Dimension((int) (0.2f * 72), (int) ((Math.sin(Math.toRadians(60)) * 0.2) * 72)));
		triangle.setFillColor(Color.decode("#ff4040"));
		triangle.setColor(Color.BLACK);
		triangle.setLineStyle(LineStyle.SOLID);
		inputs.addNode(triangle);
		inputControls.put(graph, triangle);

		for (DataflowInputPort inputPort : inputPorts) {
			GraphNode inputNode = createGraphNode();
			inputNode.setId(prefix + "WORKFLOWINTERNALSOURCE_"+ inputPort.getName());
			if (getPortStyle().equals(PortStyle.BLOB)) {
				inputNode.setLabel("");
				inputNode.setSize(new Dimension((int) (0.3f * 72), (int) (0.3f * 72)));
			} else {
				inputNode.setLabel(inputPort.getName());
			}
			inputNode.setShape(getPortStyle().inputShape());
			inputNode.setColor(Color.BLACK);
			inputNode.setLineStyle(LineStyle.SOLID);
			inputNode.setFillColor(Color.decode("#8ed6f0"));
			if (depth == 0) {
				inputNode.setInteractive(true);
			}
			if (interactive) {
				inputNode.setInteractive(true);
			}
			if (depth < 2) {
				inputNode.setDataflowObject(inputPort);
				if (dataflowToActivityPort.containsKey(inputPort)) {
					Port port = dataflowToActivityPort.get(inputPort);
					inputNode.setDataflowObject(port);
					dataflowToGraph.put(port, inputNode);
				} else {
					inputNode.setDataflowObject(inputPort);
					dataflowToGraph.put(inputPort, inputNode);
				}
			}
			ports.put(inputPort.getInternalOutputPort(), inputNode);
			inputs.addNode(inputNode);
			graphElementMap.put(inputNode.getId(), inputNode);
		}
		return inputs;
	}

	private Graph generateOutputsGraph(List<? extends DataflowOutputPort> outputPorts, String prefix, Graph graph, int depth) {
		Graph outputs = createGraph();
		outputs.setId(prefix + "sinks");
		outputs.setColor(Color.BLACK);
		outputs.setFillColor(null);
		outputs.setShape(Shape.BOX);
		outputs.setLineStyle(LineStyle.DOTTED);
		if (getPortStyle().equals(PortStyle.BLOB)) {
			outputs.setLabel("");
		} else {
			outputs.setLabel("Workflow output ports");
		}

		GraphNode triangle = createGraphNode();
		triangle.setId(prefix + "WORKFLOWINTERNALSINKCONTROL");
		triangle.setLabel("");
		triangle.setShape(Shape.INVTRIANGLE);
		triangle.setSize(new Dimension((int) (0.2f * 72), (int) ((Math.sin(Math.toRadians(60)) * 0.2) * 72)));
		triangle.setFillColor(Color.decode("#66cd00"));
		triangle.setColor(Color.BLACK);
		triangle.setLineStyle(LineStyle.SOLID);
		outputs.addNode(triangle);
		outputControls.put(graph, triangle);

		for (DataflowOutputPort outputPort : outputPorts) {
			GraphNode outputNode = createGraphNode();
			outputNode.setId(prefix + "WORKFLOWINTERNALSINK_"+ outputPort.getName());
			if (getPortStyle().equals(PortStyle.BLOB)) {
				outputNode.setLabel("");
				outputNode.setSize(new Dimension((int) (0.3f * 72), (int) (0.3f * 72)));
			} else {
				outputNode.setLabel(outputPort.getName());
			}
			outputNode.setShape(getPortStyle().outputShape());
			outputNode.setColor(Color.BLACK);
			outputNode.setLineStyle(LineStyle.SOLID);
			outputNode.setFillColor(Color.decode("#8ed6f0"));
			if (depth == 0) {
				outputNode.setInteractive(true);
			}
			if (interactive) {
				outputNode.setInteractive(true);
			}
			if (depth < 2) {
				if (dataflowToActivityPort.containsKey(outputPort)) {
					outputNode.setDataflowObject(dataflowToActivityPort.get(outputPort));
					Port port = dataflowToActivityPort.get(outputPort);
					outputNode.setDataflowObject(port);
					dataflowToGraph.put(port, outputNode);
				} else {
					outputNode.setDataflowObject(outputPort);
					dataflowToGraph.put(outputPort, outputNode);
				}
			}
			ports.put(outputPort.getInternalInputPort(), outputNode);
			outputs.addNode(outputNode);
			graphElementMap.put(outputNode.getId(), outputNode);
		}
		return outputs;
	}

	private GraphNode generateMergeNode(Merge merge, String prefix, int depth) {
		GraphNode node = createGraphNode();
		node.setId(prefix + merge.getLocalName());
		node.setLabel("");
		node.setShape(getPortStyle().mergeShape());
		node.setSize(new Dimension((int) (0.2f * 72), (int) (0.2f * 72)));
		node.setColor(Color.BLACK);
		node.setLineStyle(LineStyle.SOLID);
		node.setFillColor(Color.decode("#4f94cd"));
		if (depth == 0) {
			node.setDataflowObject(merge);
		}
		if (interactive) {
			node.setDataflowObject(merge);
		}

		dataflowToGraph.put(merge, node);

		for (MergeInputPort inputPort : merge.getInputPorts()) {
			GraphNode portNode = createGraphNode();
			portNode.setId("i" + inputPort.getName());
			portNode.setLabel(inputPort.getName());
			ports.put(inputPort, portNode);
			node.addSinkNode(portNode);
		}

		OutputPort outputPort = merge.getOutputPort();
		GraphNode portNode = createGraphNode();
		portNode.setId("o" + outputPort.getName());
		portNode.setLabel(outputPort.getName());
		ports.put(outputPort, portNode);
		node.addSourceNode(portNode);

		graphElementMap.put(node.getId(), node);
		return node;
	}

	private GraphNode generateProcessorNode(Processor processor, String prefix, int depth) {
		//Blatantly ignoring any other activities for now
		Activity<?> firstActivity = null;
		if (! processor.getActivityList().isEmpty()) {
			firstActivity = processor.getActivityList().get(0);
		}

		GraphNode node = createGraphNode();
		node.setId(prefix + processor.getLocalName());
		if (getPortStyle().equals(PortStyle.BLOB)) {
			node.setLabel("");
			node.setSize(new Dimension((int) (0.3f * 72), (int) (0.3f * 72)));
		} else {
			node.setLabel(processor.getLocalName());
		}
		node.setShape(getPortStyle(processor).processorShape());
		node.setColor(Color.BLACK);
		node.setLineStyle(LineStyle.SOLID);
		if (firstActivity != null) {
			node.setFillColor(GraphColorManager.getFillColor(firstActivity));
		}
//check whether the nested workflow processors should be clickable or not, if top level workflow then should be clickable regardless
		if (depth == 0) {
			node.setInteractive(true);
			node.setDataflowObject(processor);
		}
		if (interactive) {
			node.setInteractive(true);				
			node.setDataflowObject(processor);
		}

		
		
		if (firstActivity instanceof NestedDataflow && expandNestedDataflow(((NestedDataflow) firstActivity).getNestedDataflow())) {
			Dataflow subDataflow = ((NestedDataflow) firstActivity).getNestedDataflow();
			for (InputPort inputPort : firstActivity.getInputPorts()) {
				for (DataflowInputPort dataflowPort : subDataflow.getInputPorts()) {
					if (inputPort.getName().equals(dataflowPort.getName())) {
						nestedDataflowPorts.put(inputPort, dataflowPort.getInternalOutputPort());
						dataflowToActivityPort.put(dataflowPort, inputPort);
						break;
					}
				}
			}

			for (OutputPort outputPort : firstActivity.getOutputPorts()) {
				for (DataflowOutputPort dataflowPort : subDataflow.getOutputPorts()) {
					if (outputPort.getName().equals(dataflowPort.getName())) {
						nestedDataflowPorts.put(outputPort, dataflowPort.getInternalInputPort());
						dataflowToActivityPort.put(dataflowPort, outputPort);
						break;
					}
				}
			}

			Graph subGraph = generateGraph(subDataflow, prefix, processor.getLocalName(), depth + 1);
			//TODO why does this depth matter?
			if (depth == 0) {
				subGraph.setDataflowObject(processor);
			}
			if (interactive) {
				subGraph.setDataflowObject(processor);
			}
			node.setGraph(subGraph);
			node.setExpanded(true);

			dataflowToGraph.put(processor, subGraph);
		} else {
			graphElementMap.put(node.getId(), node);
			dataflowToGraph.put(processor, node);
		}

		List<InputPort> inputPorts;
		if (firstActivity != null) {
			inputPorts = new ArrayList<InputPort>(firstActivity.getInputPorts());
		} else {
			inputPorts = new ArrayList<InputPort>(processor.getInputPorts());
		}
		Collections.sort(inputPorts, portComparator);
		if (inputPorts.size() == 0) {
			GraphNode portNode = createGraphNode();
			portNode.setShape(Shape.BOX);
			portNode.setColor(Color.BLACK);
			portNode.setFillColor(node.getFillColor());
			portNode.setLineStyle(LineStyle.SOLID);
			node.addSinkNode(portNode);
		} else {
			for (InputPort inputPort : inputPorts) {
				GraphNode portNode = createGraphNode();
				portNode.setId("i" + inputPort.getName().replaceAll("\\.", ""));
				portNode.setLabel(inputPort.getName()/*.replaceAll("\\.", "\\\\n")*/);
				portNode.setShape(Shape.BOX);
				portNode.setColor(Color.BLACK);
				portNode.setFillColor(node.getFillColor());
				portNode.setLineStyle(LineStyle.SOLID);
				if (depth == 0) {
					portNode.setDataflowObject(inputPort);
				}
				if (interactive) {
					portNode.setDataflowObject(inputPort);
				}
				if (!node.isExpanded()) {
					dataflowToGraph.put(inputPort, portNode);
				}
				ports.put(inputPort, portNode);
				node.addSinkNode(portNode);
				graphElementMap.put(portNode.getId(), portNode);
				portToActivity.put(inputPort, firstActivity);
				portToProcessor.put(inputPort, processor);
			}
		}

		List<OutputPort> outputPorts;
		if (firstActivity != null) {
			outputPorts = new ArrayList<OutputPort>(firstActivity
					.getOutputPorts());
		} else {
			outputPorts = new ArrayList<OutputPort>(processor.getOutputPorts());
		}
		Collections.sort(outputPorts, portComparator);
		if (outputPorts.size() == 0) {
			GraphNode portNode = createGraphNode();
			portNode.setShape(Shape.BOX);
			portNode.setColor(Color.BLACK);
			portNode.setFillColor(node.getFillColor());
			portNode.setLineStyle(LineStyle.SOLID);
			node.addSourceNode(portNode);
		} else {
			for (OutputPort outputPort : outputPorts) {
				GraphNode portNode = createGraphNode();
				portNode.setId("o" + outputPort.getName().replaceAll("\\.", ""));
				portNode.setLabel(outputPort.getName()/*.replaceAll("\\.", "\\\\n")*/);
				portNode.setShape(Shape.BOX);
				portNode.setColor(Color.BLACK);
				portNode.setFillColor(node.getFillColor());
				portNode.setLineStyle(LineStyle.SOLID);
				if (depth == 0) {
					portNode.setDataflowObject(outputPort);
				}
				if (interactive) {
					portNode.setDataflowObject(outputPort);
				}
				if (!node.isExpanded()) {
					dataflowToGraph.put(outputPort, portNode);
				}
				ports.put(outputPort, portNode);
				node.addSourceNode(portNode);
				graphElementMap.put(portNode.getId(), portNode);
				portToActivity.put(outputPort, firstActivity);
				portToProcessor.put(outputPort, processor);
			}
		}

		return node;
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
	 * Returns the dataflowSelectionModel.
	 *
	 * @return the dataflowSelectionModel
	 */
	public DataflowSelectionModel getDataflowSelectionModel() {
		return dataflowSelectionModel;
	}

	/**
	 * Sets the dataflowSelectionModel.
	 *
	 * @param dataflowSelectionModel the new dataflowSelectionModel
	 */
	public void setDataflowSelectionModel(
			DataflowSelectionModel dataflowSelectionModel) {
		if (this.dataflowSelectionModel != null) {
			this.dataflowSelectionModel.removeObserver(this);
		}
		this.dataflowSelectionModel = dataflowSelectionModel;
		this.dataflowSelectionModel.addObserver(this);
	}

	/**
	 * Sets the proportion of the node's jobs that have been completed.
	 * 
	 * @param nodeId
	 *            the id of the node
	 * @param complete
	 *            the proportion of the nodes's jobs that have been
	 *            completed, a value between 0.0 and 1.0
	 */
	public void setNodeCompleted(String nodeId, float complete) {
		if (graphElementMap.containsKey(nodeId)) {
			GraphElement graphElement = graphElementMap.get(nodeId);
			graphElement.setCompleted(complete);
		}
	}

	public void setEdgeActive(String edgeId, boolean active) {
	}
	
	/**
	 * Returns the alignment.
	 *
	 * @return the alignment
	 */
	public Alignment getAlignment() {
		return alignment;
	}

	/**
	 * Returns the portStyle.
	 *
	 * @return the portStyle
	 */
	public PortStyle getPortStyle() {
		return portStyle;
	}
	
	/**
	 * Returns the portStyle for a processor.
	 *
	 * @return the portStyle for a processor
	 */
	public PortStyle getPortStyle(Processor processor) {
		if (processorPortStyle.containsKey(processor)) {
			return processorPortStyle.get(processor);
		} else {
			return portStyle;
		}
	}
	
	/**
	 * Sets the alignment.
	 *
	 * @param alignment the new alignment
	 */
	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}
	
	/**
	 * Sets the portStyle.
	 *
	 * @param style the new portStyle
	 */
	public void setPortStyle(PortStyle portStyle) {
		this.portStyle = portStyle;
		processorPortStyle.clear();
	}

	/**
	 * Sets the portStyle for a processor.
	 *
	 * @param style the new portStyle for the processor
	 */
	public void setPortStyle(Processor processor, PortStyle portStyle) {
		processorPortStyle.put(processor, portStyle);
	}
	
	/**
	 * Returns true if the default is to expand nested workflows.
	 *
	 * @return true if the default is to expand nested workflows
	 */
	public boolean expandNestedDataflows() {
		return expandNestedDataflows;
	}

	/**
	 * Returns true if the nested dataflow should be expanded.
	 *
	 * @param dataflow
	 * @return true if the nested dataflow should be expanded
	 */
	public boolean expandNestedDataflow(Dataflow dataflow) {
		if (dataflowExpansion.containsKey(dataflow)) {
			return dataflowExpansion.get(dataflow);
		} else {
			return expandNestedDataflows;
		}
	}

	/**
	 * Sets the default for expanding nested workflows.
	 *
	 * @param expand the default for expanding nested workflows
	 */
	public void setExpandNestedDataflows(boolean expand) {
		dataflowExpansion.clear();
		this.expandNestedDataflows = expand;
	}

	/**
	 * Sets whether the nested dataflow should be expanded.
	 *
	 * @param expand whether the nested dataflow should be expanded
	 * @param dataflow he nested dataflow
	 */
	public void setExpandNestedDataflow(Dataflow dataflow, boolean expand) {
		dataflowExpansion.put(dataflow, expand);
	}

	public boolean startEdgeCreation(GraphElement graphElement, Point point) {
		if (!edgeCreationFromSource && !edgeCreationFromSink) {
			Object dataflowObject = graphElement.getDataflowObject();
			if (dataflowObject instanceof ActivityInputPort || dataflowObject instanceof DataflowOutputPort) {
				edgeCreationSink = graphElement;
				edgeCreationFromSink = true;
			} else if (dataflowObject instanceof OutputPort || dataflowObject instanceof DataflowInputPort) {
				edgeCreationSource = graphElement;
				edgeCreationFromSource = true;
			} else if (graphElement instanceof GraphEdge) {
				GraphEdge edge = (GraphEdge) graphElement;
				edgeCreationSource = edge.getSource();
				edgeCreationFromSource = true;
				edgeMoveElement = edge;
			}
		}
		return edgeCreationFromSource || edgeCreationFromSink;
	}
	
	public boolean moveEdgeCreationTarget(GraphElement graphElement, Point point) {
		boolean edgeValid = false;
		Object dataflowObject = graphElement.getDataflowObject();
		if (edgeCreationFromSink) {
			if (graphElement instanceof GraphNode) {
				if (dataflowObject instanceof OutputPort) {
					OutputPort outputPort = (OutputPort) dataflowObject;
					Activity<?> activity = portToActivity.get(outputPort);
					if (activity != null) {
						//can't connect to same processor
						if (!activity.getInputPorts().contains(edgeCreationSink.getDataflowObject())) {
							edgeCreationSource = graphElement;
							edgeValid = true;
						}
					}
				} else if (dataflowObject instanceof DataflowInputPort) {
					edgeCreationSource = graphElement;
					edgeValid = true;
				} else if (dataflowObject instanceof Processor) {
					Processor processor = (Processor) dataflowObject;
					List<? extends Activity<?>> activities = processor.getActivityList();
					if (activities.size() > 0) {
						Activity<?> activity = activities.get(0);
						if (activity.getOutputPorts().size() > 0 && !activity.getInputPorts().contains(edgeCreationSink.getDataflowObject())) {
							edgeCreationSource = graphElement;
							edgeValid = true;
						}
					}
				}
			}
			if (!edgeValid) {
				edgeCreationSource = null;
			}
		} else if (edgeCreationFromSource) {
			if (graphElement instanceof GraphNode) {
				if (dataflowObject instanceof ActivityInputPort) {
					ActivityInputPort activityInputPort = (ActivityInputPort) dataflowObject;
					Activity<?> activity = portToActivity.get(activityInputPort);
					if (activity != null) {
						//can't connect to same processor
						if (!activity.getOutputPorts().contains(edgeCreationSource.getDataflowObject())) {
							edgeCreationSink = graphElement;
							edgeValid = true;
						}
					}
				} else if (dataflowObject instanceof DataflowOutputPort) {
					edgeCreationSink = graphElement;
					edgeValid = true;
				} else if (dataflowObject instanceof Processor) {
					Processor processor = (Processor) dataflowObject;
					List<? extends Activity<?>> activities = processor.getActivityList();
					if (activities.size() > 0) {
						Activity<?> activity = activities.get(0);
						if (activity.getInputPorts().size() > 0 && !activity.getOutputPorts().contains(edgeCreationSource.getDataflowObject())) {
							edgeCreationSink = graphElement;
							edgeValid = true;
						}
					}
				}
			} 
			if (!edgeValid) {
				edgeCreationSink = null;
			}
		}
		return edgeValid;
	}

	public boolean stopEdgeCreation(GraphElement graphElement, Point point) {
		boolean edgeCreated = false;
		if (edgeCreationSource != null && edgeCreationSink != null) {
			OutputPort source = null;
			InputPort sink = null;
			Object sourceDataflowObject = edgeCreationSource.getDataflowObject();
			Object sinkDataflowObject = edgeCreationSink.getDataflowObject();
			if (sourceDataflowObject instanceof OutputPort) {
				source = (OutputPort) sourceDataflowObject;				
			} else if (sourceDataflowObject instanceof DataflowInputPort) {
				DataflowInputPort dataflowInputPort = (DataflowInputPort) sourceDataflowObject;
				source = dataflowInputPort.getInternalOutputPort();
			} else if (sourceDataflowObject instanceof Processor) {
				List<? extends Activity<?>> activities = ((Processor) sourceDataflowObject).getActivityList();
				if (activities.size() > 0) {
					Set<OutputPort> ports = activities.get(0).getOutputPorts();
					source = (OutputPort) showPortOptions(new ArrayList<Port>(ports), "output", componentForPopups, point);
				}
			}
			if (sinkDataflowObject instanceof InputPort) {
				sink = (InputPort) sinkDataflowObject;				
			} else if (sinkDataflowObject instanceof DataflowOutputPort) {
				DataflowOutputPort dataflowOutputPort = (DataflowOutputPort) sinkDataflowObject;
				sink = dataflowOutputPort.getInternalInputPort();
			} else if (sinkDataflowObject instanceof Processor) {
				List<? extends Activity<?>> activities = ((Processor) sinkDataflowObject).getActivityList();
				if (activities.size() > 0) {
					Set<ActivityInputPort> ports = activities.get(0).getInputPorts();
					sink = (InputPort) showPortOptions(new ArrayList<Port>(ports), "input", componentForPopups, point);
				}
			}
			if (source != null && sink != null) {
				Edit<?> edit = null;
				if (edgeMoveElement == null) {
					EventForwardingOutputPort output = null;
					Edit<?> addProcessorOutputEdit = null;
					if (source instanceof EventForwardingOutputPort) {
						output = (EventForwardingOutputPort) source;
					} else {
						//must be an activity port
						Activity<?> activity = portToActivity.get(source);
						Processor processor = portToProcessor.get(source);
						if (activity != null && processor != null) {
							//check if processor port exists
							output = Tools.getProcessorOutputPort(processor, activity, source);
							if (output == null) {
								//port doesn't exist so create a processor port and map it
								ProcessorOutputPort processorOutputPort =
									edits.createProcessorOutputPort(processor, source.getName(),
											source.getDepth(), source.getGranularDepth());
								List<Edit<?>> editList = new ArrayList<Edit<?>>();
								editList.add(edits.getAddProcessorOutputPortEdit(processor, processorOutputPort));
								editList.add(edits.getAddActivityOutputPortMappingEdit(activity, source.getName(), source.getName()));
								output = processorOutputPort;
								addProcessorOutputEdit = new CompoundEdit(editList);
							}
						}
					}
					EventHandlingInputPort input = null;
					Edit<?> addProcessorInputEdit = null;
					if (sink instanceof EventHandlingInputPort) {
						input = (EventHandlingInputPort) sink;
					} else {
						//must be an activity port
						Activity<?> activity = portToActivity.get(sink);
						Processor processor = portToProcessor.get(sink);
						if (activity != null && processor != null) {
							//check if processor port exists
							input = Tools.getProcessorInputPort(processor, activity, sink);
							if (input == null) {
								//port doesn't exist so create a processor port and map it
								ProcessorInputPort processorInputPort =
									edits.createProcessorInputPort(processor, sink.getName(), sink.getDepth());
								List<Edit<?>> editList = new ArrayList<Edit<?>>();
								editList.add(edits.getAddProcessorInputPortEdit(processor, processorInputPort));
								editList.add(edits.getAddActivityInputPortMappingEdit(activity, sink.getName(), sink.getName()));
								input = processorInputPort;
								addProcessorInputEdit = new CompoundEdit(editList);
							}
						}
					}
					if (output != null && input != null) {
						List<Edit<?>> editList = new ArrayList<Edit<?>>();
						if (addProcessorOutputEdit != null) {
							editList.add(addProcessorOutputEdit);
						}
						if (addProcessorInputEdit != null) {
							editList.add(addProcessorInputEdit);
						}
						editList.add(Tools.getCreateAndConnectDatalinkEdit(dataflow, output, input));
						edit = new CompoundEdit(editList);
					}
				} else {
					Object sinkObject = edgeMoveElement.getSink().getDataflowObject();
					if (sinkObject instanceof DataflowOutputPort) {
						sinkObject = ((DataflowOutputPort) sinkObject).getInternalInputPort();						
					}
					EventHandlingInputPort input = null;
					Edit<?> addProcessorInputEdit = null;
					if (sink instanceof EventHandlingInputPort) {
						input = (EventHandlingInputPort) sink;
					} else {
						//must be an activity port
						Activity<?> activity = portToActivity.get(sink);
						Processor processor = portToProcessor.get(sink);
						if (activity != null && processor != null) {
							//check if processor port exists
							input = Tools.getProcessorInputPort(processor, activity, sink);
							if (input == null) {
								//port doesn't exist so create a processor port and map it
								ProcessorInputPort processorInputPort =
									edits.createProcessorInputPort(processor, sink.getName(), sink.getDepth());
								List<Edit<?>> editList = new ArrayList<Edit<?>>();
								editList.add(edits.getAddProcessorInputPortEdit(processor, processorInputPort));
								editList.add(edits.getAddActivityInputPortMappingEdit(activity, sink.getName(), sink.getName()));
								input = processorInputPort;
								addProcessorInputEdit = new CompoundEdit(editList);
							}
						}
					}
					if (sinkObject != sink) {
						List<Edit<?>> editList = new ArrayList<Edit<?>>();
						if (addProcessorInputEdit != null) {
							editList.add(addProcessorInputEdit);
						}
						editList.add(Tools.getMoveDatalinkSinkEdit(dataflow, (Datalink) edgeMoveElement.getDataflowObject(), input));
						edit = new CompoundEdit(editList);
					}
				}
				if (edit != null) {
					try {
						editManager.doDataflowEdit(dataflow, edit);
						edgeCreated = true;
					} catch (EditException e) {
						logger.debug("Failed to create datalink from '" + source.getName() + "' to '" + sink.getName() + "'");
					}
				}
			}
		}
		edgeCreationSource = null;
		edgeCreationSink = null;
		edgeMoveElement = null;
		edgeCreationFromSource = false;
		edgeCreationFromSink = false;
		
		return edgeCreated;
	}
	
	private Object showPortOptions(List<? extends Port> ports, String portType, Component component, Point point) {
		Object result = null;
		if (ports.size() == 0) {
			JOptionPane.showMessageDialog(component, "Service has no "+portType+" ports to connect to");
		} else if (ports.size() == 1) {
			result = ports.get(0);
		} else {
			List<String> portNames = new ArrayList<String>();
			for (Port port : ports) {
				portNames.add(port.getName());
			}
			String portName = (String)JOptionPane.showInputDialog(
					component,
					"Select an "+portType+" port",
					"Port Chooser",
					JOptionPane.PLAIN_MESSAGE,
					null,
					portNames.toArray(), portNames.get(0));
			if ((portName != null) && (portName.length() > 0)) {
				int index = portNames.indexOf(portName);
				if (index >= 0 && index < ports.size()) {
					result = ports.get(index);
				}
			}
		}
		return result;

	}

	public void resetSelection() {
		if (dataflowSelectionModel != null) {
			for (Object dataflowElement : dataflowSelectionModel.getSelection()) {
				GraphElement graphElement = dataflowToGraph.get(dataflowElement);
				if (graphElement != null) {
						graphElement.setSelected(true);
				}		
			}
		}
	}

	public void setIteration(String nodeId, int iteration) {
		if (graphElementMap.containsKey(nodeId)) {
			GraphElement graphElement = graphElementMap.get(nodeId);
			graphElement.setIteration(iteration);
		}		
	}
	
	public void setErrors(String nodeId, int errors) {
		if (graphElementMap.containsKey(nodeId)) {
			GraphElement graphElement = graphElementMap.get(nodeId);
			graphElement.setErrors(errors);
		}		
	}
	
	public void notify(Observable<DataflowSelectionMessage> sender,
			DataflowSelectionMessage message) throws Exception {
		GraphElement graphElement = dataflowToGraph.get(message.getElement());
		if (graphElement != null) {
				graphElement.setSelected(message.getType().equals(DataflowSelectionMessage.Type.ADDED));
		}		
	}

	/**
	 * Returns the GraphEventManager.
	 *
	 * @return the GraphEventManager
	 */
	public GraphEventManager getGraphEventManager() {
		return graphEventManager;
	}

	/**
	 * Sets the GraphEventManager.
	 *
	 * @param graphEventManager the new GraphEventManager
	 */
	public void setGraphEventManager(GraphEventManager graphEventManager) {
		this.graphEventManager = graphEventManager;
	}

}

