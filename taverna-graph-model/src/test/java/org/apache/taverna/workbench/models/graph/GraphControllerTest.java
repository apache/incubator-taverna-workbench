package org.apache.taverna.workbench.models.graph;

import org.apache.taverna.workbench.models.graph.GraphNode;
import org.apache.taverna.workbench.models.graph.Graph;
import org.apache.taverna.workbench.models.graph.GraphEdge;
import org.apache.taverna.workbench.models.graph.GraphController;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.taverna.workbench.models.graph.GraphController.PortStyle;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.taverna.scufl2.api.core.Workflow;

public class GraphControllerTest {

	Workflow dataflow;

	GraphController graphController;

	@Before
	public void setUp() throws Exception {
//		System.setProperty("raven.eclipse", "true");
//		setUpRavenRepository();
//		dataflow = WorkflowModelTranslator.doTranslation(loadScufl("nested_iteration.xml"));
		graphController = new GraphController(dataflow, null, false, null, null, null, null) {

			@Override
			public GraphEdge createGraphEdge() {
				return new GraphEdge(this);
			}

			@Override
			public Graph createGraph() {
				return new Graph(this);
			}

			@Override
			public GraphNode createGraphNode() {
				return new GraphNode(this);
			}

			@Override
			public void redraw() {

			}

		};
		graphController.setPortStyle(PortStyle.NONE);
	}

	@Test
	@Ignore
	public void testGenerateGraph() throws IOException, InterruptedException {
		Graph graph = graphController.generateGraph();
		assertEquals(5, graph.getNodes().size());
		assertEquals(9, graph.getEdges().size());
		assertEquals(1, graph.getSubgraphs().size());
	}

}
