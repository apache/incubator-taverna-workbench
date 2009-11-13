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
/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: AddPluginSiteFrame.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/04 14:51:52 $
 *               by   $Author: sowen70 $
 * Created on 8 Dec 2006
 *****************************************************************/
package net.sf.taverna.raven.plugins.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class AddPluginSiteFrame extends JDialog {

	private JPanel jContentPane = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JTextField urlText = null;
	private JTextField nameText = null;
	
	private String name = null;
	private String url = null;

	/**
	 * This method initializes 
	 * 
	 */
	public AddPluginSiteFrame(JDialog parent) {
		super(parent,true);
		initialize();
	}
		

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(350, 140));
        this.setContentPane(getJContentPane());
			
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();			
			
			GridBagConstraints gridBagContraintHeading = new GridBagConstraints();
			gridBagContraintHeading.ipadx = 10;
			gridBagContraintHeading.ipady = 5;	
			gridBagContraintHeading.gridx = 0;
			gridBagContraintHeading.gridy = 0;	        
	        gridBagContraintHeading.gridwidth = 2;
	        gridBagContraintHeading.anchor = GridBagConstraints.CENTER;
	        gridBagContraintHeading.fill = GridBagConstraints.BOTH;	   
			
			GridBagConstraints gridBagContraintNameLabel = new GridBagConstraints();

			gridBagContraintNameLabel.ipadx = 10;
			gridBagContraintNameLabel.ipady = 5;	       
			gridBagContraintNameLabel.gridx = 0;
			gridBagContraintNameLabel.gridy = 1;	        
	        gridBagContraintNameLabel.gridwidth = 1;
	        gridBagContraintNameLabel.anchor = GridBagConstraints.FIRST_LINE_START;
	        gridBagContraintNameLabel.fill = GridBagConstraints.NONE;	   
	        
	        GridBagConstraints gridBagContraintURLLabel = new GridBagConstraints();

			gridBagContraintURLLabel.ipadx = 10;
			gridBagContraintURLLabel.ipady = 5;	       
			gridBagContraintURLLabel.gridx = 0;
			gridBagContraintURLLabel.gridy = 2;	        
	        gridBagContraintURLLabel.gridwidth = 1;
	        gridBagContraintURLLabel.anchor = GridBagConstraints.FIRST_LINE_START;
	        gridBagContraintURLLabel.fill = GridBagConstraints.NONE;	   
	       
	        
	        

	        GridBagConstraints gridBagContraintNameText = new GridBagConstraints();
	        gridBagContraintNameText.ipadx = 10;
	        gridBagContraintNameText.ipady = 5;
	        gridBagContraintNameText.anchor = GridBagConstraints.FIRST_LINE_START;
	        gridBagContraintNameText.fill = GridBagConstraints.HORIZONTAL;
	        gridBagContraintNameText.gridx = 1;
	        gridBagContraintNameText.gridy = 1;
	        gridBagContraintNameText.weightx = 0.1;	        
	       
	        GridBagConstraints gridBagContraintURLText = new GridBagConstraints();
	        gridBagContraintURLText.ipadx = 10;
	        gridBagContraintURLText.ipady = 5;
	        gridBagContraintURLText.anchor = GridBagConstraints.FIRST_LINE_START;	        	        
	        gridBagContraintURLText.fill = GridBagConstraints.HORIZONTAL;
	        gridBagContraintURLText.gridx = 1;
	        gridBagContraintURLText.gridy = 2;
	        gridBagContraintURLText.weightx = 0.1;
	       	        
	       
	        GridBagConstraints gridBagContraintButtons = new GridBagConstraints();
	        gridBagContraintButtons.gridwidth=2;
	        gridBagContraintButtons.ipadx = 10;
	        gridBagContraintButtons.ipady = 5;
	        gridBagContraintButtons.anchor = GridBagConstraints.SOUTH;
	        gridBagContraintButtons.fill = GridBagConstraints.BOTH;
	        gridBagContraintButtons.weightx = 0;
	        gridBagContraintButtons.weighty = 0.2;
	        gridBagContraintButtons.gridy = 3;
	        gridBagContraintButtons.gridx = 0;	        
	        
	        JLabel name = new JLabel("Site Name:");
	        name.setHorizontalAlignment(SwingConstants.RIGHT);	        
	        JLabel url = new JLabel("Site URL:");
	        url.setHorizontalAlignment(SwingConstants.RIGHT);
	        
	        urlText=new JTextField("http://");
	        nameText=new JTextField();	        
	        
	        
	        jContentPane.setLayout(new GridBagLayout());	 
	        jContentPane.add(new JLabel("Enter update site name and url"),gridBagContraintHeading);
	        jContentPane.add(name, gridBagContraintNameLabel);
	        jContentPane.add(url, gridBagContraintURLLabel);
	        jContentPane.add(nameText, gridBagContraintNameText);
	        jContentPane.add(urlText, gridBagContraintURLText);
	        jContentPane.add(getButtonPanel(), gridBagContraintButtons);
	        
		}
		return jContentPane;
	} 
	
	public JPanel getButtonPanel() {		
		return new ButtonPanel(getOKButton(),getCancelButton());
	}
	
	public JButton getOKButton() {
		if (okButton==null) {
			okButton=new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					name=nameText.getText();
					url=urlText.getText();
					setVisible(false);
					dispose();
				}				
			});
			
		}
		return okButton;
	}
	
	public JButton getCancelButton() {
		if (cancelButton==null) {
			cancelButton=new JButton("Cancel");	
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
					dispose();
				}
			});
		}
		return cancelButton;
	}


	public String getName() {
		if (name!=null) name=name.trim();
		return name;
	}


	public String getUrl() {
		if (url!=null) url=url.trim();
		if (!url.endsWith("/")) url+="/";
		return url;
	}	

}  //  @jve:decl-index=0:visual-constraint="73,21"


@SuppressWarnings("serial")
class ButtonPanel extends JPanel {
    public ButtonPanel(JButton ok, JButton cancel) {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.ipadx = 5;
        c.gridy = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        add(ok);
        add(cancel);
    }
}
