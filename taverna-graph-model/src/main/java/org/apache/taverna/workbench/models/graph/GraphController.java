/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.models.graph;

import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.edits.CompoundEdit;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.models.graph.Graph.Alignment;
import org.apache.taverna.workbench.models.graph.GraphEdge.ArrowStyle;
import org.apache.taverna.workbench.models.graph.GraphElement.LineStyle;
import org.apache.taverna.workbench.models.graph.GraphShapeElement.Shape;
import org.apache.taverna.workbench.selection.DataflowSelectionModel;
import org.apache.taverna.workbench.selection.events.DataflowSelectionMessage;
import org.apache.taverna.workflow.edits.AddDataLinkEdit;
import org.apache.taverna.workflow.edits.RemoveDataLinkEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.NamedSet;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.common.WorkflowBean;
import org.apache.taverna.scufl2.api.core.BlockingControlLink;
import org.apache.taverna.scufl2.api.core.ControlLink;
import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputActivityPort;
import org.apache.taverna.scufl2.api.port.InputPort;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.port.OutputActivityPort;
import org.apache.taverna.scufl2.api.port.OutputPort;
import org.apache.taverna.scufl2.api.port.OutputProcessorPort;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;
import org.apache.taverna.scufl2.api.port.Port;
import org.apache.taverna.scufl2.api.port.ProcessorPort;
import org.apache.taverna.scufl2.api.port.ReceiverPort;
import org.apache.taverna.scufl2.api.port.SenderPort;
import org.apache.taverna.scufl2.api.port.WorkflowPort;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * @author David Withers
 */
public abstract class GraphController implements
		Observer<DataflowSelectionMessage> {
	public enum PortStyle {
		ALL {
			@Override
			Shape inputShape() {
				return Shape.INVHOUSE;
			}

			@Override
			Shape outputShape() {
				return Shape.HOUSE;
			}

			@Override
			Shape processorShape() {
				return Shape.RECORD;
			}
		},
		BOUND {
			@Override
			Shape inputShape() {
				return Shape.INVHOUSE;
			}

			@Override
			Shape outputShape() {
				return Shape.HOUSE;
			}

			@Override
			Shape processorShape() {
				return Shape.RECORD;
			}
		},
		NONE {
			@Override
			Shape inputShape() {
				return Shape.BOX;
			}

			@Override
			Shape outputShape() {
				return Shape.BOX;
			}

			@Override
			Shape processorShape() {
				return Shape.BOX;
			}
		},
		BLOB {
			@Override
			Shape inputShape() {
				return Shape.CIRCLE;
			}

			@Override
			Shape outputShape() {
				return Shape.CIRCLE;
			}

			@Override
			Shape processorShape() {
				return Shape.CIRCLE;
			}
		};

		abstract Shape inputShape();

		abstract Shape outputShape();

		abstract Shape processorShape();

		Shape mergeShape() {
			return Shape.CIRCLE;
		}
	}

	private static Logger logger = Logger.getLogger(GraphController.class);

	private Map<String, GraphElement> idToElement = new HashMap<>();
	private Map<WorkflowBean, GraphElement> workflowToGraph = new HashMap<>();
	private Map<Port, GraphNode> ports = new HashMap<>();
	private Map<Graph, GraphNode> inputControls = new HashMap<>();
	private Map<Graph, GraphNode> outputControls = new HashMap<>();
	private Map<Port, Port> nestedWorkflowPorts = new HashMap<>();
	private Map<WorkflowPort, ProcessorPort> workflowPortToProcessorPort = new HashMap<>();
	private Map<Port, Processor> portToProcessor = new HashMap<>();

	private EditManager editManager;
	private final Workflow workflow;
	private final Profile profile;
	private DataflowSelectionModel dataflowSelectionModel;
	private GraphEventManager graphEventManager;
	private Component componentForPopups;

	// graph settings
	private PortStyle portStyle = PortStyle.NONE;
	private Map<Processor, PortStyle> processorPortStyle = new HashMap<>();
	private Alignment alignment = Alignment.VERTICAL;
	private boolean expandNestedDataflows = true;
	private Map<Activity, Boolean> dataflowExpansion = new HashMap<>();
	protected Map<String, GraphElement> graphElementMap = new HashMap<>();
	protected GraphElement edgeCreationSource, edgeCreationSink;
	protected GraphEdge edgeMoveElement;
	protected boolean edgeCreationFromSource = false;
	protected boolean edgeCreationFromSink = false;
	private Graph graph;
	private boolean interactive;
	private final ColourManager colourManager;

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public GraphController(Workflow workflow, Profile profile,
			boolean interactive, Component componentForPopups,
			EditManager editManager, MenuManager menuManager,
			ColourManager colourManager) {
		this(workflow, profile, interactive, componentForPopups,
				Alignment.VERTICAL, PortStyle.NONE, editManager, menuManager,
				colourManager);
	}

	public GraphController(Workflow workflow, Profile profile,
			boolean interactive, Component componentForPopups,
			Alignment alignment, PortStyle portStyle, EditManager editManager,
			MenuManager menuManager, ColourManager colourManager) {
		this.workflow = workflow;
		this.profile = profile;
		this.interactive = interactive;
		this.componentForPopups = componentForPopups;
		this.alignment = alignment;
		this.portStyle = portStyle;
		this.editManager = editManager;
		this.colourManager = colourManager;
		this.graphEventManager = new DefaultGraphEventManager(this,
				componentForPopups, menuManager);
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
		workflowToGraph.clear();
		ports.clear();
		inputControls.clear();
		outputControls.clear();
		nestedWorkflowPorts.clear();
		workflowPortToProcessorPort.clear();
		graphElementMap.clear();
		portToProcessor.clear();
		return generateGraph(workflow, "", workflow.getName(), 0);
	}

	private Graph generateGraph(Workflow dataflow, String prefix, String name,
			int depth) {
		Graph graph = createGraph();
		graph.setId(prefix + name);
		graph.setAlignment(getAlignment());
		if (getPortStyle().equals(PortStyle.BLOB) || depth == 0)
			graph.setLabel("");
		else
			graph.setLabel(name);
		graph.setFillColor(GraphColorManager.getSubGraphFillColor(depth));
		if (depth == 0)
			graph.setLineStyle(LineStyle.NONE);
		else
			graph.setLineStyle(LineStyle.SOLID);
		graph.setColor(Color.BLACK);
		graph.setShape(Shape.BOX);

		if (depth == 0)
			graph.setWorkflowBean(dataflow);
		if (interactive)
			graph.setWorkflowBean(dataflow);

		// processors
		for (Processor processor : dataflow.getProcessors())
			graph.addNode(generateProcessorNode(processor, graph.getId(), depth));

		// dataflow outputs
		NamedSet<OutputWorkflowPort> outputPorts = dataflow.getOutputPorts();
		if (outputPorts.size() > 0 || depth > 0)
			graph.addSubgraph(generateOutputsGraph(outputPorts, graph.getId(),
					graph, depth));

		// dataflow inputs
		NamedSet<InputWorkflowPort> inputPorts = dataflow.getInputPorts();
		if (inputPorts.size() > 0 || depth > 0)
			graph.addSubgraph(generateInputsGraph(inputPorts, graph.getId(),
					graph, depth));

		// datalinks
		for (DataLink datalink : dataflow.getDataLinks()) {
			GraphEdge edge = generateDataLinkEdge(datalink, depth);
			if (edge != null)
				graph.addEdge(edge);
		}

		// controlLinks
		for (ControlLink controlLink : dataflow.getControlLinks())
			if (controlLink instanceof BlockingControlLink) {
				GraphEdge edge = generateControlLinkEdge(
						(BlockingControlLink) controlLink, depth);
				if (edge != null)
					graph.addEdge(edge);
			}

		graphElementMap.put(graph.getId(), graph);
		return graph;
	}

	public void transformGraph(Graph oldGraph, Graph newGraph) {
		oldGraph.setAlignment(newGraph.getAlignment());
		transformGraphElement(oldGraph, newGraph);
		List<GraphEdge> oldEdges = new ArrayList<>(oldGraph.getEdges());
		List<GraphEdge> newEdges = new ArrayList<>(newGraph.getEdges());
		for (GraphEdge oldEdge : oldEdges) {
			int index = newEdges.indexOf(oldEdge);
			if (index >= 0) {
				GraphEdge newEdge = newEdges.remove(index);
				oldEdge.setPath(newEdge.getPath());
				workflowToGraph.put(oldEdge.getWorkflowBean(), oldEdge);
			} else
				oldGraph.removeEdge(oldEdge);
		}
		List<GraphNode> newNodes = new ArrayList<>(newGraph.getNodes());
		List<GraphNode> oldNodes = new ArrayList<>(oldGraph.getNodes());
		for (GraphNode oldNode : oldNodes) {
			int index = newNodes.indexOf(oldNode);
			if (index >= 0) {
				GraphNode newNode = newNodes.remove(index);
				oldNode.setExpanded(newNode.isExpanded());
				List<GraphNode> newSourceNodes = new ArrayList<>(
						newNode.getSourceNodes());
				List<GraphNode> oldSourceNodes = new ArrayList<>(
						oldNode.getSourceNodes());
				for (GraphNode oldSourceNode : oldSourceNodes) {
					int sourceNodeIndex = newSourceNodes.indexOf(oldSourceNode);
					if (sourceNodeIndex >= 0) {
						GraphNode newSourceNode = newSourceNodes
								.remove(sourceNodeIndex);
						transformGraphElement(oldSourceNode, newSourceNode);
					} else
						oldNode.removeSourceNode(oldSourceNode);
				}
				for (GraphNode sourceNode : newSourceNodes)
					oldNode.addSourceNode(sourceNode);
				List<GraphNode> newSinkNodes = new ArrayList<>(
						newNode.getSinkNodes());
				List<GraphNode> oldSinkNodes = new ArrayList<>(
						oldNode.getSinkNodes());
				for (GraphNode oldSinkNode : oldSinkNodes) {
					int sinkNodeIndex = newSinkNodes.indexOf(oldSinkNode);
					if (sinkNodeIndex >= 0) {
						GraphNode newSinkNode = newSinkNodes
								.remove(sinkNodeIndex);
						transformGraphElement(oldSinkNode, newSinkNode);
					} else
						oldNode.removeSinkNode(oldSinkNode);
				}
				for (GraphNode sinkNode : newSinkNodes)
					oldNode.addSinkNode(sinkNode);
				Graph oldSubGraph = oldNode.getGraph();
				Graph newSubGraph = newNode.getGraph();
				if (oldSubGraph != null && newSubGraph != null)
					transformGraph(oldSubGraph, newSubGraph);
				transformGraphElement(oldNode, newNode);
			} else
				oldGraph.removeNode(oldNode);
		}
		List<Graph> newSubGraphs = new ArrayList<>(newGraph.getSubgraphs());
		List<Graph> oldSubGraphs = new ArrayList<>(oldGraph.getSubgraphs());
		for (Graph oldSubGraph : oldSubGraphs) {
			int index = newSubGraphs.indexOf(oldSubGraph);
			if (index >= 0) {
				Graph newSubGraph = newSubGraphs.remove(index);
				transformGraph(oldSubGraph, newSubGraph);
			} else
				oldGraph.removeSubgraph(oldSubGraph);
		}
		for (GraphNode node : newNodes)
			oldGraph.addNode(node);
		for (Graph graph : newSubGraphs)
			oldGraph.addSubgraph(graph);
		for (GraphEdge newEdge : newEdges)
			oldGraph.addEdge(newEdge);
	}

	public void transformGraphElement(GraphShapeElement oldGraphElement,
			GraphShapeElement newGraphElement) {
		oldGraphElement.setWorkflowBean(newGraphElement.getWorkflowBean());
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
		workflowToGraph.put(oldGraphElement.getWorkflowBean(), oldGraphElement);
	}

	public void filterGraph(Set<?> dataflowEntities) {
		Set<GraphElement> graphElements = new HashSet<>();
		for (Entry<WorkflowBean, GraphElement> entry : workflowToGraph
				.entrySet())
			if (!dataflowEntities.contains(entry.getKey()))
				graphElements.add(entry.getValue());
		filterGraph(getGraph(), graphElements);
	}

	private void filterGraph(Graph graph, Set<GraphElement> graphElements) {
		for (GraphNode node : graph.getNodes()) {
			node.setFiltered(graphElements.contains(node));
			Graph subgraph = node.getGraph();
			if (subgraph != null)
				if (graphElements.contains(subgraph)) {
					removeFilter(subgraph);
					subgraph.setFiltered(true);
				} else {
					subgraph.setFiltered(false);
					filterGraph(subgraph, graphElements);
				}
		}
		for (GraphEdge edge : graph.getEdges())
			edge.setFiltered(graphElements.contains(edge));
		for (Graph subgraph : graph.getSubgraphs())
			if (graphElements.contains(subgraph)) {
				removeFilter(subgraph);
				subgraph.setFiltered(true);
			} else {
				subgraph.setFiltered(false);
				filterGraph(subgraph, graphElements);
			}
	}

	public void removeFilter() {
		for (Entry<WorkflowBean, GraphElement> entry : workflowToGraph
				.entrySet())
			entry.getValue().setFiltered(false);
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
		for (GraphEdge edge : graph.getEdges())
			edge.setFiltered(false);
		for (Graph subgraph : graph.getSubgraphs()) {
			subgraph.setFiltered(false);
			removeFilter(subgraph);
		}
	}

	private GraphEdge generateControlLinkEdge(BlockingControlLink condition,
			int depth) {
		GraphEdge edge = null;
		GraphElement source = workflowToGraph.get(condition.getUntilFinished());
		GraphElement sink = workflowToGraph.get(condition.getBlock());
		if (source != null && sink != null) {
			edge = createGraphEdge();
			if (source instanceof Graph)
				edge.setSource(outputControls.get(source));
			else if (source instanceof GraphNode)
				edge.setSource((GraphNode) source);
			if (sink instanceof Graph)
				edge.setSink(inputControls.get(sink));
			else if (sink instanceof GraphNode)
				edge.setSink((GraphNode) sink);
			String sourceId = edge.getSource().getId();
			String sinkId = edge.getSink().getId();
			edge.setId(sourceId + "->" + sinkId);
			edge.setLineStyle(LineStyle.SOLID);
			edge.setColor(Color.decode("#505050"));
			edge.setFillColor(null);
			edge.setArrowHeadStyle(ArrowStyle.DOT);
			if (depth == 0)
				edge.setWorkflowBean(condition);
			if (interactive)
				edge.setWorkflowBean(condition);
			workflowToGraph.put(condition, edge);
			graphElementMap.put(edge.getId(), edge);
		}
		return edge;
	}

	private GraphEdge generateDataLinkEdge(DataLink datalink, int depth) {
		GraphEdge edge = null;
		Port sourcePort = datalink.getReceivesFrom();
		Port sinkPort = datalink.getSendsTo();
		if (nestedWorkflowPorts.containsKey(sourcePort))
			sourcePort = nestedWorkflowPorts.get(sourcePort);
		if (nestedWorkflowPorts.containsKey(sinkPort))
			sinkPort = nestedWorkflowPorts.get(sinkPort);
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
			} else
				id.append(sourceNode.getId());
			id.append("->");
			if (sinkNode.getParent() instanceof GraphNode) {
				id.append(sinkNode.getParent().getId());
				id.append(":");
				id.append(sinkNode.getId());
			} else
				id.append(sinkNode.getId());
			edge.setId(id.toString());
			edge.setLineStyle(LineStyle.SOLID);
			edge.setColor(Color.BLACK);
			edge.setFillColor(Color.BLACK);
			if (depth == 0)
				edge.setWorkflowBean(datalink);
			if (interactive)
				edge.setWorkflowBean(datalink);
			workflowToGraph.put(datalink, edge);
			graphElementMap.put(edge.getId(), edge);
		}
		return edge;
	}

	private Graph generateInputsGraph(NamedSet<InputWorkflowPort> inputPorts,
			String prefix, Graph graph, int depth) {
		Graph inputs = createGraph();
		inputs.setId(prefix + "sources");
		inputs.setColor(Color.BLACK);
		inputs.setFillColor(null);
		inputs.setShape(Shape.BOX);
		inputs.setLineStyle(LineStyle.DOTTED);
		if (getPortStyle().equals(PortStyle.BLOB))
			inputs.setLabel("");
		else
			inputs.setLabel("Workflow input ports");

		GraphNode triangle = createGraphNode();
		triangle.setId(prefix + "WORKFLOWINTERNALSOURCECONTROL");
		triangle.setLabel("");
		triangle.setShape(Shape.TRIANGLE);
		triangle.setSize(new Dimension((int) (0.2f * 72), (int) ((Math.sin(Math
				.toRadians(60)) * 0.2) * 72)));
		triangle.setFillColor(Color.decode("#ff4040"));
		triangle.setColor(Color.BLACK);
		triangle.setLineStyle(LineStyle.SOLID);
		inputs.addNode(triangle);
		inputControls.put(graph, triangle);

		for (InputWorkflowPort inputWorkflowPort : inputPorts) {
			GraphNode inputNode = createGraphNode();
			inputNode.setId(prefix + "WORKFLOWINTERNALSOURCE_"
					+ inputWorkflowPort.getName());
			if (getPortStyle().equals(PortStyle.BLOB)) {
				inputNode.setLabel("");
				inputNode.setSize(new Dimension((int) (0.3f * 72),
						(int) (0.3f * 72)));
			} else
				inputNode.setLabel(inputWorkflowPort.getName());
			inputNode.setShape(getPortStyle().inputShape());
			inputNode.setColor(Color.BLACK);
			inputNode.setLineStyle(LineStyle.SOLID);
			inputNode.setFillColor(Color.decode("#8ed6f0"));
			if (depth == 0)
				inputNode.setInteractive(true);
			if (interactive)
				inputNode.setInteractive(true);
			if (depth < 2) {
				inputNode.setWorkflowBean(inputWorkflowPort);
				if (workflowPortToProcessorPort.containsKey(inputWorkflowPort)) {
					ProcessorPort port = workflowPortToProcessorPort
							.get(inputWorkflowPort);
					inputNode.setWorkflowBean(port);
					workflowToGraph.put(port, inputNode);
				} else {
					inputNode.setWorkflowBean(inputWorkflowPort);
					workflowToGraph.put(inputWorkflowPort, inputNode);
				}
			}
			ports.put(inputWorkflowPort, inputNode);
			inputs.addNode(inputNode);
			graphElementMap.put(inputNode.getId(), inputNode);
		}
		return inputs;
	}

	private Graph generateOutputsGraph(
			NamedSet<OutputWorkflowPort> outputPorts, String prefix,
			Graph graph, int depth) {
		Graph outputs = createGraph();
		outputs.setId(prefix + "sinks");
		outputs.setColor(Color.BLACK);
		outputs.setFillColor(null);
		outputs.setShape(Shape.BOX);
		outputs.setLineStyle(LineStyle.DOTTED);
		if (getPortStyle().equals(PortStyle.BLOB))
			outputs.setLabel("");
		else
			outputs.setLabel("Workflow output ports");

		GraphNode triangle = createGraphNode();
		triangle.setId(prefix + "WORKFLOWINTERNALSINKCONTROL");
		triangle.setLabel("");
		triangle.setShape(Shape.INVTRIANGLE);
		triangle.setSize(new Dimension((int) (0.2f * 72), (int) ((Math.sin(Math
				.toRadians(60)) * 0.2) * 72)));
		triangle.setFillColor(Color.decode("#66cd00"));
		triangle.setColor(Color.BLACK);
		triangle.setLineStyle(LineStyle.SOLID);
		outputs.addNode(triangle);
		outputControls.put(graph, triangle);

		for (OutputWorkflowPort outputWorkflowPort : outputPorts) {
			GraphNode outputNode = createGraphNode();
			outputNode.setId(prefix + "WORKFLOWINTERNALSINK_"
					+ outputWorkflowPort.getName());
			if (getPortStyle().equals(PortStyle.BLOB)) {
				outputNode.setLabel("");
				outputNode.setSize(new Dimension((int) (0.3f * 72),
						(int) (0.3f * 72)));
			} else
				outputNode.setLabel(outputWorkflowPort.getName());
			outputNode.setShape(getPortStyle().outputShape());
			outputNode.setColor(Color.BLACK);
			outputNode.setLineStyle(LineStyle.SOLID);
			outputNode.setFillColor(Color.decode("#8ed6f0"));
			if (depth == 0)
				outputNode.setInteractive(true);
			if (interactive)
				outputNode.setInteractive(true);
			if (depth < 2) {
				if (workflowPortToProcessorPort.containsKey(outputWorkflowPort)) {
					ProcessorPort port = workflowPortToProcessorPort
							.get(outputWorkflowPort);
					outputNode.setWorkflowBean(port);
					workflowToGraph.put(port, outputNode);
				} else {
					outputNode.setWorkflowBean(outputWorkflowPort);
					workflowToGraph.put(outputWorkflowPort, outputNode);
				}
			}
			ports.put(outputWorkflowPort, outputNode);
			outputs.addNode(outputNode);
			graphElementMap.put(outputNode.getId(), outputNode);
		}
		return outputs;
	}

	private GraphNode generateProcessorNode(Processor processor, String prefix,
			int depth) {
		// Blatantly ignoring any other activities for now
		ProcessorBinding processorBinding = scufl2Tools
				.processorBindingForProcessor(processor, profile);
		Activity activity = processorBinding.getBoundActivity();
		@SuppressWarnings("unused")
		URI activityType = activity.getType();

		GraphNode node = createGraphNode();
		node.setId(prefix + processor.getName());
		if (getPortStyle().equals(PortStyle.BLOB)) {
			node.setLabel("");
			node.setSize(new Dimension((int) (0.3f * 72), (int) (0.3f * 72)));
		} else
			node.setLabel(processor.getName());
		node.setShape(getPortStyle(processor).processorShape());
		node.setColor(Color.BLACK);
		node.setLineStyle(LineStyle.SOLID);
		// if (activityType.equals(URI.create(NonExecutableActivity.URI))) {
		// if (activityType.equals(URI.create(DisabledActivity.URI))) {
		// node.setFillColor(GraphColorManager
		// .getFillColor(((DisabledActivity) activity)
		// .getActivity(), colourManager));
		// } else {
		// node.setFillColor(GraphColorManager
		// .getFillColor(activityType, colourManager));
		// }
		// node.setOpacity(0.3f);
		// } else
		node.setFillColor(GraphColorManager.getFillColor(activity,
				colourManager));

		// check whether the nested workflow processors should be clickable or
		// not, if top level workflow then should be clickable regardless
		if (depth == 0) {
			node.setInteractive(true);
			node.setWorkflowBean(processor);
		}
		if (interactive) {
			node.setInteractive(true);
			node.setWorkflowBean(processor);
		}

		if (scufl2Tools.containsNestedWorkflow(processor, profile)
				&& expandNestedDataflow(activity)) {
			Workflow subDataflow = scufl2Tools.nestedWorkflowForProcessor(
					processor, profile);

			NamedSet<InputWorkflowPort> inputWorkflowPorts = subDataflow
					.getInputPorts();
			for (InputActivityPort inputActivityPort : activity.getInputPorts()) {
				InputWorkflowPort inputWorkflowPort = inputWorkflowPorts
						.getByName(inputActivityPort.getName());
				InputProcessorPort inputProcessorPort = scufl2Tools
						.processorPortBindingForPort(inputActivityPort, profile)
						.getBoundProcessorPort();
				nestedWorkflowPorts.put(inputProcessorPort, inputWorkflowPort);
				workflowPortToProcessorPort.put(inputWorkflowPort,
						inputProcessorPort);
				processorBinding.getInputPortBindings();
			}

			NamedSet<OutputWorkflowPort> outputWorkflowPorts = subDataflow
					.getOutputPorts();
			for (OutputActivityPort outputActivityPort : activity
					.getOutputPorts()) {
				OutputWorkflowPort outputWorkflowPort = outputWorkflowPorts
						.getByName(outputActivityPort.getName());
				OutputProcessorPort outputProcessorPort = scufl2Tools
						.processorPortBindingForPort(outputActivityPort,
								profile).getBoundProcessorPort();
				nestedWorkflowPorts
						.put(outputProcessorPort, outputWorkflowPort);
				workflowPortToProcessorPort.put(outputWorkflowPort,
						outputProcessorPort);
			}

			Graph subGraph = generateGraph(subDataflow, prefix,
					processor.getName(), depth + 1);
			// TODO why does this depth matter?
			if (depth == 0)
				subGraph.setWorkflowBean(processor);
			if (interactive)
				subGraph.setWorkflowBean(processor);
			node.setGraph(subGraph);
			node.setExpanded(true);

			workflowToGraph.put(processor, subGraph);
		} else {
			graphElementMap.put(node.getId(), node);
			workflowToGraph.put(processor, node);
		}

		NamedSet<InputProcessorPort> inputPorts = processor.getInputPorts();
		if (inputPorts.size() == 0) {
			GraphNode portNode = createGraphNode();
			portNode.setShape(Shape.BOX);
			portNode.setColor(Color.BLACK);
			portNode.setFillColor(node.getFillColor());
			portNode.setLineStyle(LineStyle.SOLID);
			node.addSinkNode(portNode);
		} else
			for (InputPort inputPort : inputPorts) {
				GraphNode portNode = createGraphNode();
				portNode.setId("i" + inputPort.getName().replaceAll("\\.", ""));
				portNode.setLabel(inputPort.getName());
				portNode.setShape(Shape.BOX);
				portNode.setColor(Color.BLACK);
				portNode.setFillColor(node.getFillColor());
				portNode.setLineStyle(LineStyle.SOLID);
				if (depth == 0)
					portNode.setWorkflowBean(inputPort);
				if (interactive)
					portNode.setWorkflowBean(inputPort);
				if (!node.isExpanded())
					workflowToGraph.put(inputPort, portNode);
				ports.put(inputPort, portNode);
				node.addSinkNode(portNode);
				graphElementMap.put(portNode.getId(), portNode);
				// portToActivity.put(inputPort, activity);
				portToProcessor.put(inputPort, processor);
			}

		NamedSet<OutputProcessorPort> outputPorts = processor.getOutputPorts();
		if (outputPorts.size() == 0) {
			GraphNode portNode = createGraphNode();
			portNode.setShape(Shape.BOX);
			portNode.setColor(Color.BLACK);
			portNode.setFillColor(node.getFillColor());
			portNode.setLineStyle(LineStyle.SOLID);
			node.addSourceNode(portNode);
		} else
			for (OutputPort outputPort : outputPorts) {
				GraphNode portNode = createGraphNode();
				portNode.setId("o" + outputPort.getName().replaceAll("\\.", ""));
				portNode.setLabel(outputPort.getName());
				portNode.setShape(Shape.BOX);
				portNode.setColor(Color.BLACK);
				portNode.setFillColor(node.getFillColor());
				portNode.setLineStyle(LineStyle.SOLID);
				if (depth == 0)
					portNode.setWorkflowBean(outputPort);
				if (interactive)
					portNode.setWorkflowBean(outputPort);
				if (!node.isExpanded())
					workflowToGraph.put(outputPort, portNode);
				ports.put(outputPort, portNode);
				node.addSourceNode(portNode);
				graphElementMap.put(portNode.getId(), portNode);
				// portToActivity.put(outputPort, activity);
				portToProcessor.put(outputPort, processor);
			}

		return node;
	}

	/**
	 * Returns the dataflow.
	 * 
	 * @return the dataflow
	 */
	public Workflow getWorkflow() {
		return workflow;
	}

	public Profile getProfile() {
		return profile;
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
	 * @param dataflowSelectionModel
	 *            the new dataflowSelectionModel
	 */
	public void setDataflowSelectionModel(
			DataflowSelectionModel dataflowSelectionModel) {
		if (this.dataflowSelectionModel != null)
			this.dataflowSelectionModel.removeObserver(this);
		this.dataflowSelectionModel = dataflowSelectionModel;
		this.dataflowSelectionModel.addObserver(this);
	}

	/**
	 * Sets the proportion of the node's jobs that have been completed.
	 * 
	 * @param nodeId
	 *            the id of the node
	 * @param complete
	 *            the proportion of the nodes's jobs that have been completed, a
	 *            value between 0.0 and 1.0
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
		if (processorPortStyle.containsKey(processor))
			return processorPortStyle.get(processor);
		return portStyle;
	}

	/**
	 * Sets the alignment.
	 * 
	 * @param alignment
	 *            the new alignment
	 */
	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}

	/**
	 * Sets the portStyle.
	 * 
	 * @param style
	 *            the new portStyle
	 */
	public void setPortStyle(PortStyle portStyle) {
		this.portStyle = portStyle;
		processorPortStyle.clear();
	}

	/**
	 * Sets the portStyle for a processor.
	 * 
	 * @param style
	 *            the new portStyle for the processor
	 */
	public void setPortStyle(Processor processor, PortStyle portStyle) {
		processorPortStyle.put(processor, portStyle);
	}

	/**
	 * Shut down any processing and update threads related to this controller.
	 * 
	 */
	public void shutdown() {
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
	public boolean expandNestedDataflow(Activity dataflow) {
		if (dataflowExpansion.containsKey(dataflow))
			return dataflowExpansion.get(dataflow);
		return expandNestedDataflows;
	}

	/**
	 * Sets the default for expanding nested workflows.
	 * 
	 * @param expand
	 *            the default for expanding nested workflows
	 */
	public void setExpandNestedDataflows(boolean expand) {
		dataflowExpansion.clear();
		this.expandNestedDataflows = expand;
	}

	/**
	 * Sets whether the nested dataflow should be expanded.
	 * 
	 * @param expand
	 *            whether the nested dataflow should be expanded
	 * @param dataflow
	 *            the nested dataflow
	 */
	public void setExpandNestedDataflow(Activity dataflow, boolean expand) {
		dataflowExpansion.put(dataflow, expand);
	}

	private boolean isSingleOutputProcessor(Object dataflowObject) {
		boolean result = false;
		if (dataflowObject instanceof Processor) {
			Processor processor = (Processor) dataflowObject;
			result = processor.getOutputPorts().size() == 1;
		}
		return result;
	}

	public boolean startEdgeCreation(GraphElement graphElement, Point point) {
		if (!edgeCreationFromSource && !edgeCreationFromSink) {
			Object dataflowObject = graphElement.getWorkflowBean();
			if (dataflowObject instanceof ReceiverPort) {
				edgeCreationSink = graphElement;
				edgeCreationFromSink = true;
			} else if (dataflowObject instanceof SenderPort
					|| isSingleOutputProcessor(dataflowObject)) {
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
		Object dataflowObject = graphElement.getWorkflowBean();
		if (edgeCreationFromSink) {
			if (graphElement instanceof GraphNode) {
				Object sinkObject = edgeCreationSink.getWorkflowBean();
				if (dataflowObject instanceof OutputPort) {
					Processor sourceProcessor = portToProcessor
							.get(dataflowObject);
					if (sourceProcessor != null) {
						Processor sinkProcessor = null;
						if (sinkObject instanceof Processor)
							sinkProcessor = (Processor) sinkObject;
						else if (portToProcessor.containsKey(sinkObject))
							sinkProcessor = portToProcessor.get(sinkObject);
						if (sinkProcessor != null) {
							Set<Processor> possibleSinkProcessors = scufl2Tools
									.possibleDownStreamProcessors(workflow,
											sourceProcessor);
							if (possibleSinkProcessors.contains(sinkProcessor)) {
								edgeCreationSource = graphElement;
								edgeValid = true;
							}
						}
						if (sinkObject instanceof OutputWorkflowPort) {
							edgeCreationSource = graphElement;
							edgeValid = true;
						}
					}
				} else if (dataflowObject instanceof InputWorkflowPort) {
					edgeCreationSource = graphElement;
					edgeValid = true;
				} else if (dataflowObject instanceof Processor) {
					Processor sourceProcessor = (Processor) dataflowObject;
					Processor sinkProcessor = null;
					if (sinkObject instanceof Processor)
						sinkProcessor = (Processor) sinkObject;
					else if (portToProcessor.containsKey(sinkObject))
						sinkProcessor = portToProcessor.get(sinkObject);
					if (sinkProcessor != null) {
						Set<Processor> possibleSinkProcessors = scufl2Tools
								.possibleDownStreamProcessors(workflow,
										sourceProcessor);
						if (possibleSinkProcessors.contains(sinkProcessor)) {
							edgeCreationSource = graphElement;
							edgeValid = true;
						}
					}
					if (sinkObject instanceof OutputWorkflowPort) {
						edgeCreationSource = graphElement;
						edgeValid = true;
					}
				}
			}
			if (!edgeValid)
				edgeCreationSource = null;
		} else if (edgeCreationFromSource) {
			if (graphElement instanceof GraphNode) {
				Object sourceObject = edgeCreationSource.getWorkflowBean();
				if (dataflowObject instanceof InputPort) {
					Processor sinkProcessor = portToProcessor
							.get(dataflowObject);
					if (sinkProcessor != null) {
						Processor sourceProcessor = null;
						if (sourceObject instanceof Processor)
							sourceProcessor = (Processor) sourceObject;
						else if (portToProcessor.containsKey(sourceObject))
							sourceProcessor = portToProcessor.get(sourceObject);
						if (sourceProcessor != null) {
							Set<Processor> possibleSourceProcessors = scufl2Tools
									.possibleUpStreamProcessors(workflow,
											sinkProcessor);
							if (possibleSourceProcessors
									.contains(sourceProcessor)) {
								edgeCreationSink = graphElement;
								edgeValid = true;
							}
						}
						if (sourceObject instanceof InputWorkflowPort) {
							edgeCreationSink = graphElement;
							edgeValid = true;
						}
					}
				} else if (dataflowObject instanceof OutputWorkflowPort) {
					if (sourceObject != null) {
						edgeCreationSink = graphElement;
						edgeValid = true;
					}
				} else if (dataflowObject instanceof Processor) {
					Processor sinkProcessor = (Processor) dataflowObject;
					Processor sourceProcessor = null;
					if (sourceObject instanceof Processor)
						sourceProcessor = (Processor) sourceObject;
					else if (portToProcessor.containsKey(sourceObject))
						sourceProcessor = portToProcessor.get(sourceObject);
					if (sourceProcessor != null) {
						Set<Processor> possibleSourceProcessors = scufl2Tools
								.possibleUpStreamProcessors(workflow,
										sinkProcessor);
						if (possibleSourceProcessors.contains(sourceProcessor)) {
							edgeCreationSink = graphElement;
							edgeValid = true;
						}
					}
					if (sourceObject instanceof InputWorkflowPort) {
						edgeCreationSink = graphElement;
						edgeValid = true;
					}
				}
			}
			if (!edgeValid)
				edgeCreationSink = null;
		}
		return edgeValid;
	}

	public boolean stopEdgeCreation(GraphElement graphElement, Point point) {
		boolean edgeCreated = false;
		if (edgeCreationSource != null && edgeCreationSink != null) {
			SenderPort source = null;
			ReceiverPort sink = null;
			Object sourceDataflowObject = edgeCreationSource.getWorkflowBean();
			Object sinkDataflowObject = edgeCreationSink.getWorkflowBean();
			if (sourceDataflowObject instanceof SenderPort)
				source = (SenderPort) sourceDataflowObject;
			else if (sourceDataflowObject instanceof Processor) {
				Processor processor = (Processor) sourceDataflowObject;
				source = showPortOptions(processor.getOutputPorts(), "output",
						componentForPopups, point);
			}
			if (sinkDataflowObject instanceof ReceiverPort)
				sink = (ReceiverPort) sinkDataflowObject;
			else if (sinkDataflowObject instanceof Processor) {
				Processor processor = (Processor) sinkDataflowObject;
				sink = showPortOptions(processor.getInputPorts(), "input",
						componentForPopups, point);
			}
			if (source != null && sink != null) {
				Edit<?> edit = null;
				if (edgeMoveElement == null) {
					DataLink dataLink = new DataLink();
					dataLink.setReceivesFrom(source);
					dataLink.setSendsTo(sink);
					edit = new AddDataLinkEdit(workflow, dataLink);
				} else {
					Object existingSink = edgeMoveElement.getSink()
							.getWorkflowBean();
					if (existingSink != sink) {
						List<Edit<?>> editList = new ArrayList<Edit<?>>();
						DataLink existingDataLink = (DataLink) edgeMoveElement
								.getWorkflowBean();
						DataLink newDataLink = new DataLink();
						newDataLink.setReceivesFrom(existingDataLink
								.getReceivesFrom());
						newDataLink.setSendsTo(sink);
						editList.add(new RemoveDataLinkEdit(workflow,
								existingDataLink));
						editList.add(new AddDataLinkEdit(workflow, newDataLink));
						edit = new CompoundEdit(editList);
					}
				}
				try {
					if (edit != null) {
						editManager.doDataflowEdit(workflow.getParent(), edit);
						edgeCreated = true;
					}
				} catch (EditException e) {
					logger.debug("Failed to create datalink from '"
							+ source.getName() + "' to '" + sink.getName()
							+ "'");
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

	private <T extends Port> T showPortOptions(NamedSet<T> ports,
			String portType, Component component, Point point) {
		T result = null;
		if (ports.size() == 0) {
			showMessageDialog(component, "Service has no " + portType
					+ " ports to connect to");
		} else if (ports.size() == 1)
			result = ports.first();
		else {
			Object[] portNames = ports.getNames().toArray();
			String portName = (String) showInputDialog(component, "Select an "
					+ portType + " port", "Port Chooser", PLAIN_MESSAGE, null,
					portNames, portNames[0]);
			if (portName != null)
				result = ports.getByName(portName);
		}
		return result;

	}

	public void resetSelection() {
		if (dataflowSelectionModel != null)
			for (Object dataflowElement : dataflowSelectionModel.getSelection()) {
				GraphElement graphElement = workflowToGraph
						.get(dataflowElement);
				if (graphElement != null)
					graphElement.setSelected(true);
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

	@Override
	public void notify(Observable<DataflowSelectionMessage> sender,
			DataflowSelectionMessage message) throws Exception {
		GraphElement graphElement = workflowToGraph.get(message.getElement());
		if (graphElement != null)
			graphElement.setSelected(message.getType().equals(
					DataflowSelectionMessage.Type.ADDED));
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
	 * @param graphEventManager
	 *            the new GraphEventManager
	 */
	public void setGraphEventManager(GraphEventManager graphEventManager) {
		this.graphEventManager = graphEventManager;
	}
}
