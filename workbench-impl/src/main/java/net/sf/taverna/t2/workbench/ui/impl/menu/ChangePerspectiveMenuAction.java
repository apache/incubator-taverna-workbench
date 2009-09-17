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
package net.sf.taverna.t2.workbench.ui.impl.menu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.impl.WorkbenchPerspectives;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

import org.apache.log4j.Logger;

/**
 * An action that zooms a diagram image
 * 
 * @author Alex Nenadic
 * @author Tom Oinn
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class ChangePerspectiveMenuAction extends AbstractMenuAction{

	private static Logger logger = Logger.getLogger(ChangePerspectiveMenuAction.class);
	
	private static ModelMap modelMap = ModelMap.getInstance();
	
	public static final URI CHANGE_PERSPECTIVE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#viewMenuChangePerspective");
	
	public ChangePerspectiveMenuAction(){
		super(ViewShowMenuSection.VIEW_SHOW_MENU_SECTION, 10, CHANGE_PERSPECTIVE_URI);
	}

	@Override
	protected Action createAction() {
		return new ChangePerspectiveAction();
	}

	private class ChangePerspectiveAction extends AbstractAction {
		
		ChangePerspectiveAction() {
			super();
			putValue(NAME, "Switch perspective");	
			putValue(SHORT_DESCRIPTION, "Switch perspective");
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
							InputEvent.CTRL_DOWN_MASK));
			
		}

		public void actionPerformed(ActionEvent e) {
			WorkbenchPerspectives perspectives = Workbench.getInstance().getPerspectives();
			PerspectiveSPI current = (PerspectiveSPI) modelMap
			.getModel(ModelMapConstants.CURRENT_PERSPECTIVE);
			if (current != null) {
			List<PerspectiveSPI> allPerspectives = perspectives.getPerspectives();
			int position = allPerspectives.indexOf(current);
			if (position != -1) {
			PerspectiveSPI nextPerspective = null;
					int nextPosition = (position + 1) % allPerspectives.size();
					nextPerspective = allPerspectives.get(nextPosition);
					while (!nextPerspective.isVisible()
							&& !nextPerspective.equals(current)) {
						nextPosition = (nextPosition + 1)
								% allPerspectives.size();
						nextPerspective = allPerspectives.get(nextPosition);
					}
					if (!nextPerspective.equals(current)) {
						modelMap.setModel(
								ModelMapConstants.CURRENT_PERSPECTIVE,
								nextPerspective);
					}
			}
			}
		}
		
	}
}
