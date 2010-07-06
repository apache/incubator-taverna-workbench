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
package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;

import org.jdom.Element;

/**
 * The blank component used when there isn't anything else, contains actions to
 * create the other components (which will therefore be created empty by
 * default).
 * 
 * @author Tom Oinn
 */
@SuppressWarnings("serial")
public class ZBlankComponent extends ZPane implements ZTreeNode {

	private List<Action> actions;

	private Action createSplitPaneAction = new AbstractAction() {
		public void actionPerformed(ActionEvent arg0) {
			replaceWith(new ZSplitPane());
		}
	};

	private Action createTabbedPaneAction = new AbstractAction() {
		public void actionPerformed(ActionEvent arg0) {
			ZTabbedPane tabbedPane = new ZTabbedPane();
			// Make an initial tab inside
			tabbedPane.newTab();
			replaceWith(tabbedPane);
		}
	};

	private Action createRavenPaneAction = new AbstractAction() {
		public void actionPerformed(ActionEvent arg0) {
			replaceWith(new ZRavenComponent());
		}
	};

	public ZBlankComponent() {
		super();
		actions = new ArrayList<Action>();
		actions.add(createSplitPaneAction);
		createSplitPaneAction.putValue(Action.SHORT_DESCRIPTION,
				"Create split pane");
		createSplitPaneAction.putValue(Action.SMALL_ICON, ZIcons
				.iconFor("addsplit"));
		actions.add(createTabbedPaneAction);
		createTabbedPaneAction.putValue(Action.SHORT_DESCRIPTION, "New tabs");
		createTabbedPaneAction.putValue(Action.SMALL_ICON, ZIcons
				.iconFor("addtab"));
		actions.add(createRavenPaneAction);
		createRavenPaneAction.putValue(Action.SHORT_DESCRIPTION,
				"Add component from Raven");
		createRavenPaneAction.putValue(Action.SMALL_ICON, ZIcons
				.iconFor("addraven"));
		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.setBackground(Color.WHITE);
		add(panel, BorderLayout.CENTER);
		setPreferredSize(new Dimension(100, 100));
		revalidate();
	}

	public void configure(Element e) {
		// No configuration for blank component
	}

	public void discard() {
		// Do nothing

	}

	public List<Action> getActions() {
		return actions;
	}

	public Element getElement() {
		return new Element("blank");
	}

	public List<ZTreeNode> getZChildren() {
		return new ArrayList<ZTreeNode>();
	}

	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent) {
		// Do nothing, this has no children
	}

}
