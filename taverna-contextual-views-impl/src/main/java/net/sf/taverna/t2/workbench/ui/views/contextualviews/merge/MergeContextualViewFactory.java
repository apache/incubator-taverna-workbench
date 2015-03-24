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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.merge;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import org.apache.taverna.scufl2.api.core.DataLink;

/**
 * A factory of contextual views for dataflow's merges.
 *
 * @author Alex Nenadic
 */
public class MergeContextualViewFactory implements ContextualViewFactory<DataLink> {
	private EditManager editManager;
	private SelectionManager selectionManager;
	private ColourManager colourManager;

	@Override
	public boolean canHandle(Object object) {
		return object instanceof DataLink
				&& ((DataLink) object).getMergePosition() != null;
	}

	@Override
	public List<ContextualView> getViews(DataLink merge) {
		return Arrays.asList(new ContextualView[] {
				new MergeContextualView(merge, editManager, selectionManager, colourManager)});
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}
}
