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
package net.sf.taverna.t2.ui.menu.items.annotated;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.ui.menu.items.contextualviews.ConfigureSection;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.annotated.AnnotatedContextualView;

public class AnnotatedConfigureMenuAction extends AbstractContextualMenuAction {
	private static final String ANNOTATE = "Annotate...";
	private String namedComponent = "contextualView";

	private static Log logger = Log.getLogger(AnnotatedConfigureMenuAction.class);

	public AnnotatedConfigureMenuAction() {
		super(ConfigureSection.configureSection, 40);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && (getContextualSelection().getSelection() instanceof Annotated);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction(ANNOTATE) {
			public void actionPerformed(ActionEvent e) {
				AnnotatedContextualView view = new AnnotatedContextualView((Annotated) getContextualSelection().getSelection(), true);
				JOptionPane.showMessageDialog(null, view.getMainFrame(), "Annotation", JOptionPane.PLAIN_MESSAGE);
			}
		};
	}

}
