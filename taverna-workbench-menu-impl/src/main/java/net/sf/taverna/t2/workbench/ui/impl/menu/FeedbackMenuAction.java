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

import static java.awt.Desktop.getDesktop;
import static net.sf.taverna.t2.workbench.ui.impl.menu.HelpMenu.HELP_URI;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

import org.apache.log4j.Logger;

/**
 * MenuItem for feedback
 * 
 * @author alanrw
 */
public class FeedbackMenuAction extends AbstractMenuAction {
	private static Logger logger = Logger.getLogger(FeedbackMenuAction.class);

	private static String FEEDBACK_URL = "http://www.taverna.org.uk/about/contact-us/feedback/";

	public FeedbackMenuAction() {
		super(HELP_URI, 20);
	}

	@Override
	protected Action createAction() {
		return new FeedbackAction();
	}

	@SuppressWarnings("serial")
	private final class FeedbackAction extends AbstractAction {
		private FeedbackAction() {
			super("Contact us");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				getDesktop().browse(new URI(FEEDBACK_URL));
			} catch (IOException e1) {
				logger.error("Unable to open URL", e1);
			} catch (URISyntaxException e1) {
				logger.error("Invalid URL syntax", e1);
			}
		}
	}

}
