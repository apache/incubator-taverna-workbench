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
package net.sf.taverna.t2.renderers;

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
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Parent;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * An extension of the javax.swing.JTree class, constructed with a String of XML
 * and used to display the XML structure as an interactive tree. Derived from
 * original code by Kyle Gabhart from
 * http://www.devx.com/gethelpon/10MinuteSolution/16694/0/page/1
 * 
 * And then subsequently heavily rewritten to move to JDOM, and moved lots of
 * the setup code to the renderer to cut down initialisation time. Added text
 * node size limit as well. Displaying large gene sequences as base64 encoded
 * text in a single node really, really hurts performance.
 * 
 * @author Kyle Gabhart
 * @author Tom Oinn
 * @author Kevin Glover
 * @author Ian Dunlop
 */
public class XMLTree extends JTree {
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
		if (rootElement != null) {
			XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
			return xo.outputString(rootElement);
		} else {
			return "";
		}
	}

	public XMLTree(String text, boolean limit) throws IOException,
			JDOMException {
		if (!limit) {
			textSizeLimit = -1;
		}
		Document document = new SAXBuilder(false).build(new StringReader(text));
		init(document.getRootElement());
		revalidate();
	}

	public XMLTree(Document document) {
		super();
		init(document.getRootElement());
		revalidate();
	}

	private void init(Content content) {
		rootElement = (Element) content;
		// Fix for platforms other than metal which can't otherwise
		// cope with arbitrary size rows
		setRowHeight(0);
		getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
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

			/*
			 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
			 *      java.lang.Object, boolean, boolean, boolean, int, boolean)
			 */
			@Override
			@SuppressWarnings("unchecked")
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded,
						leaf, row, hasFocus);
				setOpaque(false);
				if (value instanceof XMLNode) {
					XMLNode node = (XMLNode) value;
					if (node.getUserObject() instanceof Element) {
//						setIcon(TavernaIcons.xmlNodeIcon);
						Element element = (Element) node.getUserObject();
						StringBuffer nameBuffer = new StringBuffer("<html>"
								+ element.getQualifiedName());
						boolean addedAnAttribute = false;
						// Bit of a quick and dirty hack here to try to ensure
						// that the element namespace is shown. There appears no
						// way to get the actual xmlns declarations that are
						// part of an element through jdom. Also, please note,
						// theres no namespace handling at all for attributes...
						if (element.getParent() instanceof Element) {
							Element parent = (Element) element.getParent();
							if (parent.getNamespace(element
									.getNamespacePrefix()) == null) {
								nameBuffer
										.append(" <font color=\"purple\">xmlns:"
												+ element.getNamespacePrefix()
												+ "</font>=\"<font color=\"green\">"
												+ element.getNamespaceURI()
												+ "</font>\"");
							}
						} else {
							nameBuffer.append(" <font color=\"purple\">xmlns:"
									+ element.getNamespacePrefix()
									+ "</font>=\"<font color=\"green\">"
									+ element.getNamespaceURI() + "</font>\"");
						}

						Iterator attributes = element.getAttributes()
								.iterator();
						while (attributes.hasNext()) {
							Attribute attribute = (Attribute) attributes.next();
							String name = attribute.getName().trim();
							String attributeValue = attribute.getValue().trim();
							if (attributeValue != null) {
								if (attributeValue.length() > 0) {
									if (addedAnAttribute) {
										nameBuffer.append(",");
									}
									addedAnAttribute = true;
									nameBuffer
											.append(" <font color=\"purple\">"
													+ name
													+ "</font>=\"<font color=\"green\">"
													+ attributeValue
													+ "</font>\"");
								}
							}
						}

						nameBuffer.append("</html>");
						setText(nameBuffer.toString());
					} else if (node.getUserObject() instanceof Text) {
//						setIcon(TavernaIcons.leafIcon);
						Text text = (Text) node.getUserObject();
						String name = text.getText();
						if (textSizeLimit > -1 && name.length() > textSizeLimit) {
							name = name.substring(0, textSizeLimit) + "...";
						}
						setText("<html><pre><font color=\"blue\">"
								+ name.replaceAll("<br>", "\n").replaceAll("<",
										"&lt;") + "</font></pre></html>");
					}
				}
				setBackground(new Color(0, 0, 0, 0));
				return this;
			}
		});
		setAllNodesExpanded();

		// Add a listener to present the 'save as text' option
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					doEvent(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					doEvent(e);
				}
			}

			public void doEvent(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem item = new JMenuItem("Save as XML text");
				menu.add(item);
				item.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						try {
							Preferences prefs = Preferences
									.userNodeForPackage(XMLTree.class);
							String curDir = prefs.get("currentDir", System
									.getProperty("user.home"));
							fc.resetChoosableFileFilters();
							fc.setFileFilter(new ExtensionFileFilter(
									new String[] { "xml" }));
							fc.setCurrentDirectory(new File(curDir));
							int returnVal = fc.showSaveDialog(XMLTree.this);
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								prefs.put("currentDir", fc
										.getCurrentDirectory().toString());
								File file = fc.getSelectedFile();
								PrintWriter out = new PrintWriter(
										new FileWriter(file));
								out.print(XMLTree.this.getText());
								out.flush();
								out.close();
							}
						} catch (Exception ex) {
							JOptionPane
									.showMessageDialog(XMLTree.this,
											"Problem saving XML : \n"
													+ ex.getMessage(),
											"Error!", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				menu.show(XMLTree.this, e.getX(), e.getY());
			}
		});

	}

	public void setAllNodesExpanded() {
		synchronized (this.getModel()) {
			expandAll(this, new TreePath(this.getModel().getRoot()), true);
		}
	}

	@SuppressWarnings("unchecked")
	private void expandAll(JTree tree, TreePath parent, boolean expand) {
		synchronized (this.getModel()) {
			// Traverse children
			// Ignores nodes who's userObject is a Processor type to
			// avoid overloading the UI with nodes at startup.
			TreeNode node = (TreeNode) parent.getLastPathComponent();
			if (node.getChildCount() >= 0) {
				for (Enumeration e = node.children(); e.hasMoreElements();) {
					TreeNode n = (TreeNode) e.nextElement();
					TreePath path = parent.pathByAddingChild(n);
					expandAll(tree, path, expand);
				}
			}
			// Expansion or collapse must be done bottom-up
			if (expand) {
				tree.expandPath(parent);
			} else {
				tree.collapsePath(parent);
			}
		}
	}

	public void setTextNodeSizeLimit(int sizeLimit) {
		textSizeLimit = sizeLimit;
	}

	@SuppressWarnings("unchecked")
	private XMLNode createTreeNode(Content content) {
		XMLNode node = new XMLNode(content);
		if (content instanceof Parent) {
			Parent parent = (Parent) content;
			Iterator children = parent.getContent().iterator();
			while (children.hasNext()) {
				Object child = children.next();
				if (child instanceof Element) {
					node.add(createTreeNode((Content) child));
				} else if (textSizeLimit != 0 && child instanceof Text) {
					Text text = (Text) child;
					if (!text.getTextNormalize().equals("")) {
						node.add(createTreeNode(text));
					}
				}
			}
		}
		return node;
	}
}
