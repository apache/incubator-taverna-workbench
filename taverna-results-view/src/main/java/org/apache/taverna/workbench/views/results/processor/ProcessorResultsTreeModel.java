/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.views.results.processor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

import org.apache.taverna.databundle.DataBundles;

/**
 * Tree model for the results of a processor (workflow's intermediate results).
 *
 * @author Alex Nenadic
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ProcessorResultsTreeModel extends DefaultTreeModel {
	private static final Logger logger = Logger
			.getLogger(ProcessorResultsTreeModel.class);
	// Tree root
	private ProcessorResultTreeNode root;

	public ProcessorResultsTreeModel(Path path) {
		super(new ProcessorResultTreeNode());
		root = (ProcessorResultTreeNode) getRoot();
		createTree(path, root);
	}

	private void createTree(Path path, ProcessorResultTreeNode parentNode){
		// If reference contains a list of data references
		if (DataBundles.isList(path)) {
			try {
				List<Path> list = DataBundles.getList(path);
				ProcessorResultTreeNode listNode = new ProcessorResultTreeNode(
						list.size(), path); // list node
				parentNode.add(listNode);
				for (Path ref : list)
					createTree(ref, listNode);
			} catch (IOException e) {
				logger.error("Could not resolve list " + path + ", was run with in-memory storage?");
			}
		} else // reference to single data or an error
			insertDataNode(path, parentNode);
	}

	private void insertDataNode(Path path, ProcessorResultTreeNode parent) {
		ProcessorResultTreeNode dataNode = new ProcessorResultTreeNode(path); // data node
		parent.add(dataNode);
	}
}
