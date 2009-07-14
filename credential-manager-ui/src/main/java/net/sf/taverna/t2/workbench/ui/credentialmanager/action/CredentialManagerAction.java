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
package net.sf.taverna.t2.workbench.ui.credentialmanager.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
//import javax.swing.SwingUtilities;

import net.sf.taverna.t2.workbench.ui.credentialmanager.CredentialManagerUI;

@SuppressWarnings("serial")
public class CredentialManagerAction extends AbstractAction {

	private static ImageIcon ICON = new ImageIcon(CredentialManagerAction.class.getResource("/images/cred_manager16x16.png"));
	
	public CredentialManagerAction() {
		super("Credential Manager",ICON);
	}
	
	public void actionPerformed(ActionEvent e) {
		CredentialManagerUI cmUI = CredentialManagerUI.getInstance();
		cmUI.setVisible(true);

//		Runnable createAndShowCredentialManagerUI = new Runnable(){
//
//		   public void run()
//		   	{
//			   CredentialManagerUI cmUI = new CredentialManagerUI();
//   				cmUI.setVisible(true);
//		   	}
//		};
//		SwingUtilities.invokeLater(createAndShowCredentialManagerUI);

	}
}
