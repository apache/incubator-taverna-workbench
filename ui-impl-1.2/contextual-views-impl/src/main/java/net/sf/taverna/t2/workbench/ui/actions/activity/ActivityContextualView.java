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
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * A contextual view specific to an Activity.
 * <p>
 * Through the generic type the activity is associated with a given ConfigBean that is used internally
 * to define the activity. This is the bean that is used to configure the Activity itself.
 * </p>
 * <p>
 * The implementation provides a view based upon the properties set in the ConfigBean
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 *
 * @param <ConfigBean> - the ConfigBean that the Activity for this view is associated with
 * @see Activity
 * @see ContextualView
 */
public abstract class ActivityContextualView<ConfigBean> extends ContextualView {

	private Activity<?> activity;
	
	/**
	 * Constructs an instance of the view, and initialises the view itself.
	 * <p>
	 * The constructor parameter for the implementation of this class should define the specific Activity type itself.
	 * </p>
	 * @param activity
	 */
	public ActivityContextualView(Activity<?> activity) {
		super();
		this.activity = activity;
		initView();
	}

	public Activity<?> getActivity() {
		return this.activity;
	}

	@SuppressWarnings("unchecked")
	public ConfigBean getConfigBean() {
		return (ConfigBean)activity.getConfiguration();
	}
	
	public abstract void refreshView();
}
