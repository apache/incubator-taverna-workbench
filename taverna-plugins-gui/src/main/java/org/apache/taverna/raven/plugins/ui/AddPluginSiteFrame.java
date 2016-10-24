/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.raven.plugins.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.taverna.workbench.helper.HelpEnabledDialog;

@SuppressWarnings("serial")
public class AddPluginSiteFrame extends HelpEnabledDialog {

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
		super(parent,"Add plugin site", true);
		initialize();
		this.getRootPane().setDefaultButton(okButton);
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
			final ActionListener okAction = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					name=nameText.getText();
					url=urlText.getText();
					setVisible(false);
					dispose();
				}	
			};	
			okButton.addActionListener(okAction);
		    okButton.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
						okAction.actionPerformed(null);
					}
				}
			});
		}
		return okButton;
	}
	
	public JButton getCancelButton() {
		if (cancelButton==null) {
			cancelButton=new JButton("Cancel");	
			final ActionListener cancelAction  = new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
					dispose();
				}
			};
			cancelButton.addActionListener(cancelAction);
			cancelButton.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyPressed(java.awt.event.KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
						cancelAction.actionPerformed(null);
					}
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
