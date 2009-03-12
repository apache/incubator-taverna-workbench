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
package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * An abstract class defining the base container to hold a contextual view over
 * Dataflow element.
 * <p>
 * The specific implementation of this class to support a given dataflow element
 * needs to implement the getMainFrame() and getViewTitle().
 * </p>
 * <p>
 * If a view is associated with an action handler to configure this component,
 * then the getConfigureAction handler must be over-ridden. If this returns null
 * then the configure button is left disabled and it is not possible to
 * configure the element.
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * 
 */
public abstract class ContextualView extends JPanel {

	/**
	 * When implemented, this method should define the main frame that is placed
	 * in this container, and provides a static view of the Dataflow element.
	 * 
	 * @return a JComponent that represents the dataflow element.
	 */
	public abstract JComponent getMainFrame();

	/**
	 * @return a String providing a title for the view
	 */
	public abstract String getViewTitle();

	/**
	 * Allows the item to be configured, but returning an action handler that
	 * will be invoked when selecting to configure. By default this is provided
	 * by a button.
	 * <p>
	 * If there is no ability to configure the given item, then this should
	 * return null.
	 * </p>
	 * 
	 * @param owner - the owning dialog to be used when displaying dialogues for configuration options
	 * @return an action that allows the element being viewed to be configured.
	 */
	public Action getConfigureAction(Frame owner) {
		return null;
	}

	/**
	 * This MUST be called by any sub classes after they have initialised their
	 * own view since it gets their main panel and adds it to the main
	 * contextual view. If you don't do this you will get a very empty frame
	 * popping up!
	 */
	public void initView() {
		setLayout(new BorderLayout());
		add(getMainFrame(), BorderLayout.CENTER);
		setName(getViewTitle());
	}
	
	public abstract void refreshView();

}
