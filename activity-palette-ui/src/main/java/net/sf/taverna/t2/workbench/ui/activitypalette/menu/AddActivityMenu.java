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
package net.sf.taverna.t2.workbench.ui.activitypalette.menu;

import java.awt.Component;
import java.net.URI;
import java.util.List;

import javax.swing.JMenu;

import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.partition.ActivityQueryFactory;
import net.sf.taverna.t2.partition.AddQueryActionHandler;
import net.sf.taverna.t2.partition.QueryFactory;
import net.sf.taverna.t2.partition.QueryFactoryRegistry;
import net.sf.taverna.t2.partition.RootPartition;
import net.sf.taverna.t2.partition.SetModelChangeListener;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.workbench.ui.activitypalette.ActivityPaletteComponent;

/**
 * A menu that provides a set up menu actions for adding new Activity queries
 * to the Activity Palette.
 * <br>
 * The Actions are discovered from the ActivityQueryFactory's found through
 * the QueryFactory SPI.
 * 
 * @author Stuart Owen
 * 
 * @see ActivityQueryFactory
 * @see QueryFactory
 *
 */
public class AddActivityMenu extends AbstractMenuCustom {

	public AddActivityMenu() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#activity"),
				30,
				URI.create("http://taverna.sf.net/2008/t2workbench/menu#addActivity"));
	}
	
	@SuppressWarnings("unchecked")
	protected Component createCustomComponent() {
		JMenu addQueryMenu = new JMenu("New activity");
		addQueryMenu.setToolTipText("Open this menu to add a new activity");
		List<QueryFactory> factories = QueryFactoryRegistry.getInstance()
				.getInstances();
		boolean isEmpty = true;
		for (QueryFactory factory : factories) {
			if (factory instanceof ActivityQueryFactory) {
				ActivityQueryFactory af = (ActivityQueryFactory) factory;
				if (af.hasAddQueryActionHandler()) {
					AddQueryActionHandler handler = af
							.getAddQueryActionHandler();
					RootPartition<?> root = ActivityPaletteComponent.getInstance().getRootPartition();
					SetModelChangeListener<ActivityItem> listener = (SetModelChangeListener<ActivityItem>)root.getSetModelChangeListener();
					handler
							.setSetModelChangeListener(listener);
					addQueryMenu.add(handler);
					isEmpty = false;
				}
			}
		}
		if (isEmpty) {
			addQueryMenu.setEnabled(false);
		}
		return addQueryMenu;
	}

}
