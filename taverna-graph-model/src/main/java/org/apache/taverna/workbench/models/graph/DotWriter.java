/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.models.graph;

import static java.lang.String.format;
import static org.apache.taverna.workbench.models.graph.Graph.Alignment.HORIZONTAL;
import static org.apache.taverna.workbench.models.graph.Graph.Alignment.VERTICAL;
import static org.apache.taverna.workbench.models.graph.GraphElement.LineStyle.NONE;
import static org.apache.taverna.workbench.models.graph.GraphShapeElement.Shape.RECORD;

import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.taverna.workbench.models.graph.Graph.Alignment;

/**
 * Writer for creating a graphical representation of a Graph in the DOT language.
 * 
 * @author David Withers
 */
public class DotWriter {
	private static final String EOL = System.getProperty("line.separator");

	private Writer writer;

	/**
	 * Constructs a new instance of DotWriter.
	 *
	 * @param writer
	 */
	public DotWriter(Writer writer) {
		this.writer = writer;
	}

	/**
	 * Writes a graphical representation of a Graph in the DOT language to a Writer.
	 * 
	 * @param graph
	 * @throws IOException
	 */
	public void writeGraph(Graph graph) throws IOException {
		writeLine("digraph \"" + graph.getId() + "\" {");

		// Overall graph style
		writeLine(" graph [");
		writeLine("  bgcolor=\"" + getHexValue(graph.getFillColor()) + "\"");
		writeLine("  color=\"black\"");
		writeLine("  fontsize=\"10\"");
		writeLine("  labeljust=\"left\"");
		writeLine("  clusterrank=\"local\"");
		writeLine("  ranksep=\"0.22\"");
		writeLine("  nodesep=\"0.05\"");
		// Set left to right view if alignment is horizontal
		if (graph.getAlignment().equals(HORIZONTAL))
			writeLine("  rankdir=\"LR\"");
		writeLine(" ]");

		// Overall node style
		writeLine(" node [");
		writeLine("  fontname=\"Helvetica\"");
		writeLine("  fontsize=\"10\"");
		writeLine("  fontcolor=\"black\"");
		writeLine("  shape=\"record\"");
		writeLine("  height=\"0\"");
		writeLine("  width=\"0\"");
		writeLine("  color=\"black\"");
		writeLine("  fillcolor=\"lightgoldenrodyellow\"");
		writeLine("  style=\"filled\"");
		writeLine(" ];");

		// Overall edge style
		writeLine(" edge [");
		writeLine("  fontname=\"Helvetica\"");
		writeLine("  fontsize=\"8\"");
		writeLine("  fontcolor=\"black\"");
		writeLine("  color=\"black\"");
		writeLine(" ];");

		for (GraphNode node : graph.getNodes()) {
			if (node.isExpanded())
				writeSubGraph(node.getGraph(), " ");
			else
				writeNode(node, graph.getAlignment(), " ");
		}

		for (Graph subGraph : graph.getSubgraphs())
			writeSubGraph(subGraph, " ");

		for (GraphEdge edge : graph.getEdges())
			writeEdges(edge, graph.getAlignment(), " ");

		writeLine("}");
	}

	private void writeSubGraph(Graph graph, String indent) throws IOException {
		writeLine(format("%ssubgraph \"cluster_%s\" {", indent, graph.getId()));
		writeLine(format("%s rank=\"same\"", indent));

		StringBuilder style = new StringBuilder();
		if (graph.getFillColor() != null) {
			writeLine(format("%s fillcolor=\"%s\"", indent,
					getHexValue(graph.getFillColor())));
			style.append("filled");
		}
		if (graph.getLineStyle() != null) {
			style.append(style.length() == 0 ? "" : ",");
			if (graph.getLineStyle().equals(NONE))
				style.append("invis");
			else
				style.append(graph.getLineStyle().toString().toLowerCase());
		}
		writeLine(format("%s style=\"%s\"", indent, style));

		if (graph.getLabel() != null)
			writeLine(format("%s label=\"%s\"", indent, graph.getLabel()));

		for(GraphNode node : graph.getNodes()) {
			if (node.isExpanded())
				writeSubGraph(node.getGraph(), indent + " ");
			else
				writeNode(node, graph.getAlignment(), indent + " ");
		}

		for (Graph subGraph : graph.getSubgraphs())
			writeSubGraph(subGraph, indent + " ");

		for (GraphEdge edge : graph.getEdges())
			writeEdges(edge, graph.getAlignment(), indent + " ");

		writeLine(indent + "}");
	}

	private void writeEdges(GraphEdge edge, Alignment alignment, String indent) throws IOException {
		GraphNode source = edge.getSource();
		GraphNode sink = edge.getSink();
		String sourceId = "\"" + source.getId() + "\"";
		String sinkId = "\"" + sink.getId() + "\"";
		
		if (source.getParent() instanceof GraphNode) {
			GraphNode parent = (GraphNode) source.getParent();
			sourceId = "\"" + parent.getId() + "\":" + sourceId;
		}
		if (sink.getParent() instanceof GraphNode) {
			GraphNode parent = (GraphNode) sink.getParent();
			sinkId = "\"" + parent.getId() + "\":" + sinkId;
		}
		/*
		 * the compass point is required with newer versions of dot (e.g.
		 * 2.26.3) but is not compatible with older versions (e.g. 1.3)
		 */
		if (alignment.equals(HORIZONTAL)) {
			sourceId = sourceId + ":e";
			sinkId = sinkId + ":w";
		} else {
			sourceId = sourceId + ":s";
			sinkId = sinkId + ":n";			
		}
		writeLine(format("%s%s -> %s [", indent, sourceId, sinkId));
		writeLine(format("%s arrowhead=\"%s\"", indent, edge
				.getArrowHeadStyle().toString().toLowerCase()));
		writeLine(format("%s, arrowtail=\"%s\"", indent, edge
				.getArrowTailStyle().toString().toLowerCase()));
		if (edge.getColor() != null)
			writeLine(format("%s color=\"%s\"", indent,
					getHexValue(edge.getColor())));
		writeLine(format("%s]", indent));
	}

	private void writeNode(GraphNode node, Alignment alignment, String indent) throws IOException {
		writeLine(format("%s\"%s\" [", indent, node.getId()));

		StringBuilder style = new StringBuilder();
		if (node.getFillColor() != null) {
			writeLine(format("%s fillcolor=\"%s\"", indent,
					getHexValue(node.getFillColor())));
			style.append("filled");
		}
		if (node.getLineStyle() != null) {
			style.append(style.length() == 0 ? "" : ",");
			style.append(node.getLineStyle().toString().toLowerCase());
		}
		writeLine(format("%s style=\"%s\"", indent, style));

		writeLine(format("%s shape=\"%s\"", indent, node.getShape().toString().toLowerCase()));
		writeLine(format("%s width=\"%s\"", indent, node.getWidth() / 72f));
		writeLine(format("%s height=\"%s\"", indent, node.getHeight() / 72f));

		if (node.getShape().equals(RECORD)) {
			StringBuilder labelString = new StringBuilder();
			if (alignment.equals(VERTICAL)) {
				labelString.append("{{");
				addNodeLabels(node.getSinkNodes(), labelString);
				labelString.append("}|").append(node.getLabel()).append("|{");
				addNodeLabels(node.getSourceNodes(), labelString);
				labelString.append("}}");
			} else {
				labelString.append(node.getLabel()).append("|{{");
				addNodeLabels(node.getSinkNodes(), labelString);
				labelString.append("}|{");
				addNodeLabels(node.getSourceNodes(), labelString);
				labelString.append("}}");
			}
			writeLine(format("%s label=\"%s\"", indent, labelString));
		} else {
			writeLine(format("%s label=\"%s\"", indent, node.getLabel()));
		}

		writeLine(format("%s];", indent));
	}

	private void addNodeLabels(List<GraphNode> nodes, StringBuilder labelString) {
		String sep = "";
		for (GraphNode node : nodes)
			if (node.getLabel() != null) {
				labelString.append(sep);
				labelString.append("<");
				labelString.append(node.getId());
				labelString.append(">");
				labelString.append(node.getLabel());
				sep = "|";
			}
	}

	private String getHexValue(Color color) {
		return format("#%02x%02x%02x", color.getRed(), color.getGreen(),
				color.getBlue());
	}

	private void writeLine(String line) throws IOException {
		writer.write(line);
		writer.write(EOL);
	}
}
