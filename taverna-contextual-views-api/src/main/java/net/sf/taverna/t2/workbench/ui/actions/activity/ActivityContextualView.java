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
package net.sf.taverna.t2.workbench.ui.actions.activity;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;

/**
 * A contextual view specific to an Activity. Concrete subclasses must
 * initialise the view by calling {@link #initView()}.
 * <p>
 * The implementation provides a view based upon the properties set in the
 * Configuration
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * 
 * @see Activity
 * @see ContextualView
 */
@SuppressWarnings("serial")
public abstract class ActivityContextualView extends ContextualView {
	private Activity activity;
	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	/**
	 * Constructs an instance of the view.
	 * <p>
	 * The constructor parameter for the implementation of this class should
	 * define the specific Activity type itself.
	 * 
	 * @param activity
	 */
	protected ActivityContextualView(Activity activity) {
		super();
		this.activity = activity;
	}

	public Activity getActivity() {
		return activity;
	}

	public Configuration getConfigBean() {
		return scufl2Tools.configurationFor(activity, activity.getParent());
	}

	@Override
	public abstract void refreshView();
}
