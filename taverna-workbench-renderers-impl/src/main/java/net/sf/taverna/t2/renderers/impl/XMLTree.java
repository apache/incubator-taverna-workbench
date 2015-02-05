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
package net.sf.taverna.t2.renderers.impl;

import static java.util.prefs.Preferences.userNodeForPackage;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;
import static org.jdom.output.Format.getPrettyFormat;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Parent;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * An extension of the {@link JTree} class, constructed with a String of XML and
 * used to display the XML structure as an interactive tree. Derived from <a
 * href="http://www.devx.com/gethelpon/10MinuteSolution/16694/0/page/1">original
 * code by Kyle Gabhart</a> and then subsequently heavily rewritten to move to
 * JDOM, and moved lots of the setup code to the renderer to cut down
 * initialisation time. Added text node size limit as well. Displaying large
 * gene sequences as base64 encoded text in a single node really, <i>really</i>
 * hurts performance.
 * 
 * @author Kyle Gabhart
 * @author Tom Oinn
 * @author Kevin Glover
 * @author Ian Dunlop
 */
@SuppressWarnings("serial")
class XMLTree extends JTree {
	private class XMLNode extends DefaultMutableTreeNode {
		public XMLNode(Content userObject) {
			super(userObject);
		}
	}

	int textSizeLimit = 1000;
	final JFileChooser fc = new JFileChooser();
	Element rootElement = null;

	/**
	 * Build a new XMLTree from the supplied String containing XML.
	 * 
	 * @param text
	 * @throws IOException
	 * @throws JDOMException
	 */
	public XMLTree(String text) throws IOException, JDOMException {
		super();
		Document document = new SAXBuilder(false).build(new StringReader(text));
		init(document.getRootElement());
		revalidate();
	}

	public String getText() {
		if (rootElement == null)
			return "";
		XMLOutputter xo = new XMLOutputter(getPrettyFormat());
		return xo.outputString(rootElement);
	}

	public XMLTree(String text, boolean limit) throws IOException,
			JDOMException {
		if (!limit)
			textSizeLimit = -1;
		Document document = new SAXBuilder(false).build(new StringReader(text));
		init(document.getRootElement());
		revalidate();
	}

	public XMLTree(Document document) {
		this(document.getRootElement());
	}
	
	public XMLTree(Element element) {
		super();
		init(element);
		revalidate();
	}

	private void init(Content content) {
		rootElement = (Element) content;
		/*
		 * Fix for platforms other than metal which can't otherwise cope with
		 * arbitrary size rows
		 */
		setRowHeight(0);
		getSelectionModel().setSelectionMode(SINGLE_TREE_SELECTION);
		setShowsRootHandles(true);
		setEditable(false);
		setModel(new DefaultTreeModel(createTreeNode(content)));
		setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Color getBackgroundNonSelectionColor() {
				return null;
			}

			@Override
			public Color getBackground() {
				return null;
			}

			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded,
						leaf, row, hasFocus);
				setOpaque(false);
				if (value instanceof XMLNode) {
					XMLNode node = (XMLNode) value;
					if (node.getUserObject() instanceof Element)
						renderElementNode((Element) node.getUserObject());
					else if (node.getUserObject() instanceof Text)
						renderTextNode((Text) node.getUserObject());
					// TODO what about other node types?
				}
				setBackground(new Color(0, 0, 0, 0));
				return this;
			}

			private void renderElementNode(Element element) {
				// setIcon(TavernaIcons.xmlNodeIcon);
				StringBuilder nameBuffer = new StringBuilder("<html>")
						.append(element.getQualifiedName());
				/*
				 * Bit of a quick and dirty hack here to try to ensure that the
				 * element namespace is shown. There appears no way to get the
				 * actual xmlns declarations that are part of an element through
				 * jdom. Also, please note, there's no namespace handling at all
				 * for attributes...
				 */
				if (element.getParent() instanceof Element) {
					Element parent = (Element) element.getParent();
					if (parent.getNamespace(element.getNamespacePrefix()) == null)
						nameBuffer
								.append(" <font color=\"purple\">xmlns:")
								.append(element.getNamespacePrefix())
								.append("</font>=\"<font color=\"green\">")
								.append(element.getNamespaceURI() + "</font>\"");
				} else
					nameBuffer.append(" <font color=\"purple\">xmlns:")
							.append(element.getNamespacePrefix())
							.append("</font>=\"<font color=\"green\">")
							.append(element.getNamespaceURI() + "</font>\"");

				String sep = "";
				for (Object a : element.getAttributes()) {
					Attribute attribute = (Attribute) a;
					String name = attribute.getName().trim();
					String value = attribute.getValue().trim();
					if (value != null && value.length() > 0) {
						// TODO xml-quote name and value
						nameBuffer.append(sep)
								.append(" <font color=\"purple\">")
								.append(name)
								.append("</font>=\"<font color=\"green\">")
								.append(value).append("</font>\"");
						sep = ",";
					}
				}

				nameBuffer.append("</html>");
				setText(nameBuffer.toString());
			}

			private void renderTextNode(Text text) {
				// setIcon(TavernaIcons.leafIcon);
				String name = text.getText();
				if (textSizeLimit > -1 && name.length() > textSizeLimit)
					name = name.substring(0, textSizeLimit) + "...";
				setText("<html><pre><font color=\"blue\">"
						+ name.replaceAll("<br>", "\n").replaceAll("<", "&lt;")
						+ "</font></pre></html>");
			}
		});
		setAllNodesExpanded();

		// Add a listener to present the 'save as text' option
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					doEvent(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					doEvent(e);
			}

			public void doEvent(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem item = new JMenuItem("Save as XML text");
				menu.add(item);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent ae) {
						saveTreeXML();
					}
				});
				menu.show(XMLTree.this, e.getX(), e.getY());
			}
		});
	}

	private void saveTreeXML() {
		try {
			Preferences prefs = userNodeForPackage(XMLTree.class);
			String curDir = prefs.get("currentDir",
					System.getProperty("user.home"));
			fc.resetChoosableFileFilters();
			fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
			fc.setCurrentDirectory(new File(curDir));
			if (fc.showSaveDialog(this) == APPROVE_OPTION) {
				prefs.put("currentDir", fc.getCurrentDirectory().toString());
				saveTreeXML(fc.getSelectedFile());
			}
		} catch (Exception ex) {
			showMessageDialog(this, "Problem saving XML:\n" + ex.getMessage(),
					"Error!", ERROR_MESSAGE);
		}
	}

	private void saveTreeXML(File file) throws IOException {
		try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
			out.print(this.getText());
		}
	}

	public void setAllNodesExpanded() {
		synchronized (this.getModel()) {
			expandAll(this, new TreePath(this.getModel().getRoot()), true);
		}
	}

	private void expandAll(JTree tree, TreePath parent, boolean expand) {
		synchronized (this.getModel()) {
			/*
			 * Traverse children
			 * 
			 * Ignores nodes who's userObject is a Processor type to avoid
			 * overloading the UI with nodes at startup.
			 */
			TreeNode node = (TreeNode) parent.getLastPathComponent();
			for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
				TreeNode n = (TreeNode) e.nextElement();
				expandAll(tree, parent.pathByAddingChild(n), expand);
			}
			// Expansion or collapse must be done bottom-up
			if (expand)
				tree.expandPath(parent);
			else
				tree.collapsePath(parent);
		}
	}

	public void setTextNodeSizeLimit(int sizeLimit) {
		textSizeLimit = sizeLimit;
	}

	private XMLNode createTreeNode(Content content) {
		XMLNode node = new XMLNode(content);
		if (content instanceof Parent) {
			Parent parent = (Parent) content;
			for (Object child : parent.getContent()) {
				if (child instanceof Element)
					node.add(createTreeNode((Content) child));
				else if (textSizeLimit != 0 && child instanceof Text) {
					Text text = (Text) child;
					if (!text.getTextNormalize().isEmpty())
						node.add(createTreeNode(text));
				}
			}
		}
		return node;
	}
}
