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
package net.sf.taverna.t2.workbench.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;

import org.apache.log4j.Logger;

/**
 * An action that pastes a graph component
 * 
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class PasteGraphComponentAction extends AbstractAction{

	private static ModelMap modelMap = ModelMap.getInstance();
	
	private static PasteGraphComponentAction instance = null;
	
	private static Logger logger = Logger.getLogger(PasteGraphComponentAction.class);
	public PasteGraphComponentAction(){
		super();
		putValue(SMALL_ICON, WorkbenchIcons.pasteIcon);
		putValue(NAME, "Paste");	
		putValue(SHORT_DESCRIPTION, "Paste");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);

		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		setEnabled(false);
	}
	
	public void actionPerformed(ActionEvent e) {
		WorkflowView.pasteTransferable();
	}
	
	public static Action getInstance() {
		if (instance == null) {
			instance = new PasteGraphComponentAction();
		}
		return instance;
	}
}
