/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester   
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
package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdom.Element;

/**
 * ZPane implementation which holds its children within a tabbed pane container.
 * Can allow addition, removal, ordering and renaming of tabs as well as
 * conversion to a split pane view if and only if there are exactly two tabs
 * present.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 */
@SuppressWarnings("serial")
public class ZTabbedPane extends ZPane {

	private JTabbedPane tabs;
	private List<Action> actions = new ArrayList<Action>();
	private Action removeTabAction = new RemoveCurrentTabAction();
	private Action demoteTabAction = new DemoteTabAction();
	private Action promoteTabAction = new PromoteTabAction();
	private Action colourTabAction = new ColourTabAction();
	private JTextField tabName = new JTextField();

	public ZTabbedPane() {
		super();
		tabName.setMaximumSize(new Dimension(150, 20));
		tabName.setPreferredSize(new Dimension(150, 20));
		tabs = new JTabbedPane();
		add(tabs, BorderLayout.CENTER);
		actions.add(new AddTabAction());
		actions.add(removeTabAction);
		actions.add(colourTabAction);
		actions.add(demoteTabAction);
		actions.add(promoteTabAction);
		actions.add(new ReplaceWithBlankAction());
		tabs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				checkValidity();
			}
		});
		tabName.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) {
				doName();
			}

			public void insertUpdate(DocumentEvent arg0) {
				doName();
			}

			public void removeUpdate(DocumentEvent arg0) {
				doName();
			}

			private void doName() {
				int i = tabs.getSelectedIndex();
				if (i >= 0) {
					tabs.setTitleAt(i, tabName.getText());
				}
			}
		});
		checkValidity();
	}

	public void configure(Element e) {
		Element tabsElement = e.getChild("tabs");
		if (tabsElement != null) {
			for (Object tabElement : tabsElement.getChildren("tab")) {
				Element t = (Element) tabElement;
				String tabName = t.getChildTextTrim("title");
				ZTreeNode znode = componentFor(t.getChild("znode"));
				// Add to tabs then configure so we have a valid
				// component heirarchy in place to find the base pane etc
				tabs.addTab(tabName, (JComponent) znode);
				znode.configure(t.getChild("znode"));
			}
		}
	}

	public void discard() {
		for (ZTreeNode child : getZChildren()) {
			child.discard();
		}
	}

	public List<Action> getActions() {
		return actions;
	}

	public Element getElement() {
		Element tabsElement = new Element("tabs");
		for (int i = 0; i < tabs.getComponentCount(); i++) {
			ZTreeNode child = (ZTreeNode) tabs.getComponentAt(i);
			String tabName = tabs.getTitleAt(i);
			Element tabElement = new Element("tab");
			tabsElement.addContent(tabElement);
			Element tabNameElement = new Element("title");
			tabNameElement.setText(tabName);
			tabElement.addContent(tabNameElement);
			tabElement.addContent(elementFor(child));
		}
		return tabsElement;
	}

	/**
	 * Get the tab name editor
	 */
	@Override
	public List<Component> getToolbarComponents() {
		List<Component> components = new ArrayList<Component>();
		components.add(Box.createRigidArea(new Dimension(5, 5)));
		components.add(tabName);
		return components;
	}

	public List<ZTreeNode> getZChildren() {
		List<ZTreeNode> children = new ArrayList<ZTreeNode>();
		for (int i = 0; i < tabs.getComponentCount(); i++) {
			children.add((ZPane) tabs.getComponentAt(i));
		}
		return children;
	}


	@Override
	public boolean makeVisible(JComponent node) {
		if (node == this) {
			return true;
		}
		for (ZTreeNode child: getZChildren()) {
			if (child.makeVisible(node)) {
				tabs.setSelectedComponent((Component) child);
				return true;
			}
		}
		return false;
	}
	
	public void newTab() {
		newTab("Tab " + tabs.getComponentCount());
	}

	public void newTab(String name) {
		ZBlankComponent c = new ZBlankComponent();
		c.setEditable(editable);
		tabs.add(name, c);
		tabs.setSelectedComponent(c);
		checkValidity();
	}

	/**
	 * Call superclass method to show or hide toolbar and recursively call on
	 * all child elements.
	 */
	@Override
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		for (ZTreeNode child : getZChildren()) {
			child.setEditable(editable);
		}
	}

	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		// Find the tab index, if any, of the old component
		int componentIndex = -1;
		for (int i = 0; i < tabs.getComponentCount() && componentIndex < 0; i++) {
			if (tabs.getComponentAt(i) == oldComponent) {
				componentIndex = i;
			}
		}
		if (componentIndex == -1) {
			// Give up, couldn't find the old component
			return;
		}
		oldComponent.discard();
		newComponent.setEditable(editable);
		tabs.setComponentAt(componentIndex, (JComponent) newComponent);
	}

	private void checkValidity() {
		int index = tabs.getSelectedIndex();
		if (index >= 0) {
			removeTabAction.setEnabled(true);
			colourTabAction.setEnabled(true);
			tabName.setEnabled(true);
			if (tabName.getText().equals(tabs.getTitleAt(index)) == false) {
				tabName.setText(tabs.getTitleAt(index));
			}
			if (index > 0) {
				demoteTabAction.setEnabled(true);
			} else {
				demoteTabAction.setEnabled(false);
			}
			if (index < tabs.getComponentCount() - 1) {
				promoteTabAction.setEnabled(true);
			} else {
				promoteTabAction.setEnabled(false);
			}
		} else {
			tabName.setEnabled(false);
			removeTabAction.setEnabled(false);
			promoteTabAction.setEnabled(false);
			demoteTabAction.setEnabled(false);
			colourTabAction.setEnabled(false);
		}
		// Set up state for other tab controls
		// here, will do later when we actually
		// have the other controls...
	}

	/**
	 * Swap the tab at index i with that at index i+1
	 */
	private void swapTabs(int i) {
		Component c = tabs.getComponentAt(i);
		String text = tabs.getTitleAt(i);
		Icon icon = tabs.getIconAt(i);
		tabs.remove(i);
		tabs.add(c, text, i + 1);
		if (icon != null) {
			tabs.setIconAt(i + 1, icon);
		}
	}

	private class AddTabAction extends AbstractAction {

		public AddTabAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION, "Add tab");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("addtab"));
		}

		public void actionPerformed(ActionEvent arg0) {
			newTab();
		}

	}

	private class ColourTabAction extends AbstractAction {

		public ColourTabAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION, "Colour...");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("colourwheel"));
		}

		public void actionPerformed(ActionEvent arg0) {
			int i = tabs.getSelectedIndex();
			Color c = tabs.getBackgroundAt(i);
			Color newColour = JColorChooser.showDialog(ZTabbedPane.this,
					"Pick tab colour", c);
			if (newColour != null) {
				tabs.setBackgroundAt(i, newColour);
			}
		}

	}

	private class DemoteTabAction extends AbstractAction {

		public DemoteTabAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION, "Shift tab left");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("demotetab"));
		}

		public void actionPerformed(ActionEvent e) {
			int selectedIndex = tabs.getSelectedIndex();
			swapTabs(selectedIndex - 1);
			tabs.setSelectedIndex(selectedIndex - 1);
		}

	}

	private class PromoteTabAction extends AbstractAction {

		public PromoteTabAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION, "Shift tab right");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("promotetab"));
		}

		public void actionPerformed(ActionEvent e) {
			int selectedIndex = tabs.getSelectedIndex();
			swapTabs(selectedIndex);
			tabs.setSelectedIndex(selectedIndex + 1);
		}

	}

	private class RemoveCurrentTabAction extends AbstractAction {

		public RemoveCurrentTabAction() {
			super();
			putValue(Action.SHORT_DESCRIPTION, "Remove tab");
			putValue(Action.SMALL_ICON, ZIcons.iconFor("deletetab"));
		}

		public void actionPerformed(ActionEvent arg0) {
			int index = tabs.getSelectedIndex();
			tabs.remove(index);
			checkValidity();
		}

	}

}
