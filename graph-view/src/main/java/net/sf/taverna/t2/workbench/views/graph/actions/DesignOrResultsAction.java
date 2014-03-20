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
package net.sf.taverna.t2.workbench.views.graph.actions;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.ui.zaria.WorkflowPerspective;
import net.sf.taverna.t2.ui.perspectives.results.ResultsPerspective;

/**
 * @author alanrw
 *
 */
public abstract class DesignOrResultsAction extends AbstractAction {

	private static ModelMap modelMap = ModelMap.getInstance();

	/* Perspective switch observer */
	private CurrentPerspectiveObserver perspectiveObserver = new CurrentPerspectiveObserver();
	
	private Object lastPerspective = null;
	
	public DesignOrResultsAction() {
		super();
		
		modelMap.addObserver(perspectiveObserver);
	}
	
	/**
	 * Modify the enabled/disabled state of the action when ModelMapConstants.CURRENT_PERSPECTIVE has been
	 * modified (i.e. when perspective has been switched).
	 */
	public class CurrentPerspectiveObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(
					ModelMapConstants.CURRENT_PERSPECTIVE)) {
				lastPerspective = message.getNewModel();
				if ((lastPerspective instanceof WorkflowPerspective) || (lastPerspective instanceof ResultsPerspective)) {
					setEnabled(true);
				}
				else{
					setEnabled(false);
				}
			}
		}
	}
	
	protected boolean isWorkflowPerspective() {
		return (!isShowingPerspective() || (lastPerspective instanceof WorkflowPerspective));
	}
	
	protected boolean isResultsPerspective() {
		return (isShowingPerspective() && (lastPerspective instanceof ResultsPerspective));
		
	}
	protected boolean isShowingPerspective() {
		return (lastPerspective != null);
	}
}
