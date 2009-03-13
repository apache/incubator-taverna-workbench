/**********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
 **********************************************************************/
package net.sf.taverna.t2.ui.menu.items;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.ContextualSelection;

/**
 * An {@link AbstractMenuAction} that is {@link ContextualMenuComponent} aware.
 * The contextual selection can be retrieved from
 * {@link #getContextualSelection()}.
 * <p>
 * The cached action will be flushed everytime the contextual selection changes,
 * forcing a new call to {@link #createAction()} - given that
 * {@link #isEnabled()} returns true.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AbstractContextualMenuAction extends AbstractMenuAction
		implements ContextualMenuComponent {

	private ContextualSelection contextualSelection;

	public AbstractContextualMenuAction(URI parentId, int positionHint) {
		super(parentId, positionHint);
	}

	public AbstractContextualMenuAction(URI parentId, int positionHint, URI id) {
		super(parentId, positionHint, id);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return contextualSelection != null;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		// Force new createAction() call
		action = null;
	}

}