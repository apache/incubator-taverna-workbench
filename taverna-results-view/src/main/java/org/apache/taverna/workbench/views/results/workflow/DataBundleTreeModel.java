package org.apache.taverna.workbench.views.results.workflow;

import static javax.swing.SwingUtilities.invokeLater;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.taverna.workbench.ui.Updatable;

import org.apache.log4j.Logger;

import org.apache.taverna.databundle.DataBundles;

/**
 * TreeModel for displaying DataBundle Paths.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class DataBundleTreeModel extends DefaultTreeModel implements Updatable {
	private static final Logger logger = Logger.getLogger(DataBundleTreeModel.class);

	private Path path;

	/**
	 * Constructs a new TreeModel for displaying DataBundle Paths.
	 *
	 * @param root
	 *            the root path of the tree
	 */
	public DataBundleTreeModel(Path root) {
		super(createTree(root));
		path = root;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	private static DefaultMutableTreeNode createTree(Path path) {
		if (path == null || DataBundles.isMissing(path))
			return new DefaultMutableTreeNode(null);
		else if (!DataBundles.isList(path))
			return new DefaultMutableTreeNode(path);

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(path);
		try {
			for (Path element : DataBundles.getList(path))
				node.add(createTree(element));
		} catch (IOException e) {
			logger.error("Error resolving data entity list " + path, e);
		}
		return node;
	}

	@Override
	public void update() {
		invokeLater(new Runnable() {
			@Override
			public void run() {
				DefaultMutableTreeNode oldNode = (DefaultMutableTreeNode) root;
				if (oldNode.getUserObject() == null
						&& (path == null || DataBundles.isMissing(path)))
					return;
				compare(oldNode, createTree(path));
			}
		});
	}

	private void compare(DefaultMutableTreeNode oldNode,
			DefaultMutableTreeNode newNode) {
		if (oldNode.getUserObject() == null) {
			Path newPath = (Path) newNode.getUserObject();
			if (newPath != null) {
				oldNode.setUserObject(newPath);
				if (DataBundles.isList(newPath))
					nodeStructureChanged(oldNode);
				else
					nodeChanged(oldNode);
			}
		} else if (DataBundles.isList((Path) oldNode.getUserObject())) {
			@SuppressWarnings("unchecked")
			Enumeration<DefaultMutableTreeNode> oldChildren = oldNode
					.children();
			@SuppressWarnings("unchecked")
			Enumeration<DefaultMutableTreeNode> newChildren = newNode
					.children();
			int index = 0;
			while (oldChildren.hasMoreElements()) {
				index++;
				compare(oldChildren.nextElement(), newChildren.nextElement());
			}
			int newChildNodes = newNode.getChildCount()
					- oldNode.getChildCount();
			if (newChildNodes != 0) {
				List<DefaultMutableTreeNode> childrenToAdd = new ArrayList<>(
						newChildNodes);
				int[] childIndices = new int[newChildNodes];
				for (int i = 0; newChildren.hasMoreElements(); i++) {
					childrenToAdd.add(newChildren.nextElement());
					childIndices[i] = index++;
				}
				for (DefaultMutableTreeNode childToAdd : childrenToAdd)
					oldNode.add(childToAdd);
				nodesWereInserted(oldNode, childIndices);
				nodeChanged(oldNode);
			}
		}
	}
}
