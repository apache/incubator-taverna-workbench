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
package net.sf.taverna.t2.workbench.ui.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class UserRegistrationForm extends JDialog{

	private static final String REGISTRATION_URL = "http://www.mygrid.org.uk/taverna/registration/";
	
	public static String TAVERNA_VERSION_PROPERTY_NAME = "Taverna version";
	public static String FIRST_NAME_PROPERTY_NAME = "First name";
	public static String LAST_NAME_PROPERTY_NAME = "Last name";
	public static String EMAIL_ADDRESS_PROPERTY_NAME = "Email address";
	public static String INSTITUTION_OR_COMPANY_PROPERTY_NAME = "Institution or company name";
	public static String INDUSTRY_PROPERTY_NAME = "Industry";
	public static String FIELD_PROPERTY_NAME = "Field of investigation";
	public static String PURPOSE_PROPERTY_NAME = "Purpose of using Taverna";
	public static String KEEP_ME_INFORMED_PROPERTY_NAME = "Keep me informed by email";
	
	public static String TAVERNA_REGISTRATION_POST_PARAMETER_NAME = "taverna_registration";
	public static String TAVERNA_VERSION_POST_PARAMETER_NAME = "taverna_version";
	public static String FIRST_NAME_POST_PARAMETER_NAME = "first_name";
	public static String LAST_NAME_POST_PARAMETER_NAME = "last_name";
	public static String EMAIL_ADDRESS_POST_PARAMETER_NAME = "email";
	public static String INSTITUTION_OR_COMPANY_POST_PARAMETER_NAME = "institution_or_company";
	public static String INDUSTRY_TYPE_POST_PARAMETER_NAME = "industry_type";
	public static String FIELD_POST_PARAMETER_NAME = "field";
	public static String PURPOSE_POST_PARAMETER_NAME = "purpose";
	public static String KEEP_ME_INFORMED_POST_PARAMETER_PROPERTY_NAME = "keep_me_informed";
	
	private static String TRUE = Boolean.TRUE.toString();
	private static String FALSE = Boolean.FALSE.toString();

	private static final String WELCOME = "Welcome to the Taverna User Registration Form";
	private static final String PLEASE_FILL_IN_THIS_REGISTRATION_FORM = "Please fill in this registration form to let us know that you are using Taverna";
	
	private static final String WE_DO = "Note that by registering:\n\n" +
					"   \u25CF We do not have access to your data\n" +
					"   \u25CF We do not have access to your service usage\n" +
					"   \u25CF You will not be monitored\n" +
					"   \u25CF We do record the information you provide\n     at registration time";
	
	private static final String WHY_REGISTER = "By registering you will:\n\n"
			+ "   \u25CF Allow us to support you better; future plans will be\n     directed towards solutions Taverna users require\n"
			+ "   \u25CF Help sustain Taverna development; our continued\n     funding relies on us showing usage\n"
			+ "   \u25CF (Optionally) Hear about news and product updates";

	private static final String FIRST_NAME = "*First name:";

	private static final String LAST_NAME = "*Last name:";

	private static final String EMAIL_ADDRESS = "*Email address:";
	
	private static final String KEEP_ME_INFORMED = "Keep me informed of news and product updates via email";
	
	private static final String INSTITUTION_COMPANY_NAME = "*Institution/Company name:";

	private static final String FIELD_OF_INVESTIGATION = " Field of investigation:\n (e.g. bioinformatics, chemistry,\n sociology, physics)";

	private static final String WHY_YOU_INTEND_TO_USE_TAVERNA = " Please give a brief description of\n why you intend to use Taverna:\n(e.g. genome analysis for\n bacterial strain identification)";

	private static String[] industryTypes = { "",
			"Academia - Life Sciences", "Academia - Social Sciences",
			"Academia - Physical Sciences", "Academia - Environmental Sciences",
			"Academia - Other", "Industry - Biotechnology",
			"Industry - Pharmaceutical", "Industry - Engineering", 
			"Industry - Other", "Healthcare Services",
			"Goverment and Public Sector", "Other" };
	
	private static final String I_AGREE_TO_THE_TERMS_AND_CONDITIONS = "I agree to the terms and conditions of registration http://www.taverna.org.uk/legal/terms";
	
	private Logger logger = Logger.getLogger(UserRegistrationForm.class);
	private UserRegistrationData previousRegistrationData;
	private JTextField firstNameTextField;
	private JTextField lastNameTextField;
	private JTextField emailTextField;
	private JCheckBox keepMeInformedCheckBox;
	private JTextField institutionOrCompanyTextField;
	private JComboBox industryTypeTextField;
	private JTextField fieldTextField;
	private JTextArea purposeTextArea;
	private JCheckBox termsAndConditionsCheckBox;

	
	public UserRegistrationForm(){
		super((Frame)null,"Taverna User Registration", true);
    	initComponents();
	}
	
	public UserRegistrationForm(File previousRegistrationDataFile) {
		super((Frame)null,"Taverna User Registration", true);
		previousRegistrationData = loadUserRegistrationData(previousRegistrationDataFile);
		initComponents();
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		UIManager.setLookAndFeel(UIManager
				.getSystemLookAndFeelClassName());
		UserRegistrationForm form = new UserRegistrationForm();
		form.setVisible(true);
	}
	
	private void initComponents() {

		JPanel mainPanel = new JPanel((new GridBagLayout()));	
		
		// Base font for all components on the form
		Font baseFont = new JLabel("base font").getFont().deriveFont(11f);
		
		// Title panel
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		titlePanel.setBackground(Color.WHITE);
		//titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		JLabel titleLabel = new JLabel(WELCOME);
		titleLabel.setFont(baseFont.deriveFont(Font.BOLD, 13.5f));
		//titleLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
		JLabel titleIcon = new JLabel(WorkbenchIcons.tavernaCogs64x64Icon);
		//titleIcon.setBorder(new EmptyBorder(10, 10, 10, 10));
		DialogTextArea titleMessage = new DialogTextArea(PLEASE_FILL_IN_THIS_REGISTRATION_FORM);
		titleMessage.setMargin(new Insets(5, 20, 10, 10));
		titleMessage.setFont(baseFont);
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);
		//titlePanel.setBorder( new EmptyBorder(10, 10, 0, 10));
		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.add(titleLabel, BorderLayout.NORTH);
		messagePanel.add(titleMessage, BorderLayout.CENTER);
		messagePanel.setBackground(Color.WHITE);
		titlePanel.add(titleIcon);
		titlePanel.add(messagePanel);
		addDivider(titlePanel, SwingConstants.BOTTOM, true);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 2;
		//gbc.insets = new Insets(5, 10, 0, 0);
		mainPanel.add(titlePanel, gbc);
		
		// Registration messages
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(10, 0, 10, 10);
		DialogTextArea registrationMessage1 = new DialogTextArea(
				WHY_REGISTER);
		registrationMessage1.setMargin(new Insets(0, 10, 0, 0));
		registrationMessage1.setFont(baseFont);
		registrationMessage1.setEditable(false);
		registrationMessage1.setFocusable(false);
		registrationMessage1.setBackground(getBackground());

		DialogTextArea registrationMessage2 = new DialogTextArea(
				WE_DO);
		registrationMessage2.setMargin(new Insets(0, 10, 0, 10));
		registrationMessage2.setFont(baseFont);
		registrationMessage2.setEditable(false);
		registrationMessage2.setFocusable(false);
		registrationMessage2.setBackground(getBackground());
		JPanel registrationMessagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		registrationMessagePanel.add(registrationMessage1);
		registrationMessagePanel.add(registrationMessage2);
		addDivider(registrationMessagePanel, SwingConstants.BOTTOM, true);
		mainPanel.add(registrationMessagePanel, gbc);
		
		// Mandatory label
//		JLabel mandatoryLabel = new JLabel("* Mandatory fields");
//		mandatoryLabel.setFont(baseFont);
//		gbc.weightx = 0.0;
//		gbc.weighty = 0.0;
//		gbc.gridx = 0;
//		gbc.gridy = 3;
//		gbc.fill = GridBagConstraints.NONE;
//		gbc.anchor = GridBagConstraints.EAST;
//		gbc.gridwidth = 2;
//		gbc.insets = new Insets(0, 10, 0, 20);
//		mainPanel.add(mandatoryLabel, gbc);
		
		// First name
		JLabel firstNameLabel = new JLabel(FIRST_NAME);		
		firstNameLabel.setFont(baseFont);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 10, 0, 10);
		mainPanel.add(firstNameLabel, gbc);
		
		firstNameTextField = new JTextField();
		firstNameTextField.setFont(baseFont);
		if (previousRegistrationData!=null){
			firstNameTextField.setText(previousRegistrationData.getFirstName());
		}
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(firstNameTextField, gbc);
		
		// Last name
		JLabel lastNameLabel = new JLabel(LAST_NAME);
		lastNameLabel.setFont(baseFont);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 10, 0, 10);
		mainPanel.add(lastNameLabel, gbc);
		
		lastNameTextField = new JTextField();
		lastNameTextField.setFont(baseFont);
		if (previousRegistrationData!=null){
			lastNameTextField.setText(previousRegistrationData.getLastName());
		}
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(lastNameTextField, gbc);
		
		// Email address
		JLabel emailLabel = new JLabel(EMAIL_ADDRESS);
		emailLabel.setFont(baseFont);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(emailLabel, gbc);
		
		emailTextField = new JTextField();
		emailTextField.setFont(baseFont);
		if (previousRegistrationData!=null){
			emailTextField.setText(previousRegistrationData.getEmailAddress());
		}
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(emailTextField, gbc);
		
		// Keep me informed
		keepMeInformedCheckBox = new JCheckBox(KEEP_ME_INFORMED);
		keepMeInformedCheckBox.setFont(baseFont);
		if (previousRegistrationData!=null){
			keepMeInformedCheckBox.setSelected(previousRegistrationData.getKeepMeInformed());
		}
		keepMeInformedCheckBox.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					evt.consume();
					if (keepMeInformedCheckBox.isSelected()){
						keepMeInformedCheckBox.setSelected(false);
					}
					else{
						keepMeInformedCheckBox.setSelected(true);
					}
				}
			}
		});
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(keepMeInformedCheckBox, gbc);
		
		// Institution name
		JLabel institutionLabel = new JLabel(INSTITUTION_COMPANY_NAME);
		institutionLabel.setFont(baseFont);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(institutionLabel, gbc);
		
		institutionOrCompanyTextField = new JTextField();
		institutionOrCompanyTextField.setFont(baseFont);
		if (previousRegistrationData!=null){
			institutionOrCompanyTextField.setText(previousRegistrationData.getInstitutionOrCompanyName());
		}
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 8;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(institutionOrCompanyTextField, gbc);
		
		// Industry type
		JLabel industryLabel = new JLabel(" Industry type:");
		industryLabel.setFont(baseFont);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 9;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(industryLabel, gbc);
		
		industryTypeTextField = new JComboBox(industryTypes);
		industryTypeTextField.setFont(baseFont);
		if (previousRegistrationData!=null){
			industryTypeTextField.setSelectedItem(previousRegistrationData.getIndustry());
		}
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 9;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(industryTypeTextField, gbc);
		
		// Field of investigation
		JTextArea fieldLabel = new JTextArea(FIELD_OF_INVESTIGATION);
		fieldLabel.setFont(baseFont);
		fieldLabel.setEditable(false);
		fieldLabel.setFocusable(false);
		fieldLabel.setBackground(getBackground());
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 10;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(fieldLabel, gbc);
		
		fieldTextField = new JTextField();
		fieldTextField.setFont(baseFont);
		if (previousRegistrationData!=null){
			fieldTextField.setText(previousRegistrationData.getField());
		}
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 10;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(fieldTextField, gbc);
		
		// Purpose of using Taverna
		JTextArea purposeLabel = new JTextArea(WHY_YOU_INTEND_TO_USE_TAVERNA);
		purposeLabel.setFont(baseFont);
		purposeLabel.setEditable(false);
		purposeLabel.setFocusable(false);
		purposeLabel.setBackground(getBackground());
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 11;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(purposeLabel, gbc);
		
		purposeTextArea = new JTextArea(5,30);
		purposeTextArea.setFont(baseFont);
		purposeTextArea.setLineWrap(true);
		purposeTextArea.setAutoscrolls(true);
		if (previousRegistrationData!=null){
			purposeTextArea.setText(previousRegistrationData.getPurposeOfUsingTaverna());
		}
		purposeTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_TAB) {
					if (evt.getModifiers() > 0)
						purposeTextArea.transferFocusBackward();
					else
						purposeTextArea.transferFocus();
					evt.consume();
				}
			}
		});
		JScrollPane purposeScrollPane = new JScrollPane(purposeTextArea); 
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 11;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(purposeScrollPane, gbc);
		
		// Terms and conditions
		termsAndConditionsCheckBox = new JCheckBox(I_AGREE_TO_THE_TERMS_AND_CONDITIONS);
		termsAndConditionsCheckBox.setFont(baseFont);
		termsAndConditionsCheckBox.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					evt.consume();
					if (termsAndConditionsCheckBox.isSelected()){
						termsAndConditionsCheckBox.setSelected(false);
					}
					else{
						termsAndConditionsCheckBox.setSelected(true);
					}
				}
			}
		});
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 12;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(10, 10, 0, 10);
		mainPanel.add(termsAndConditionsCheckBox, gbc);
		
		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton registerButton = new JButton("Register");
		registerButton.setFont(baseFont);
		registerButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					evt.consume();
					register();
				}
			}
		});
		registerButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				register();
			}
		});
		JButton doNotRegisterButton = new JButton("Do not ask me again");
		doNotRegisterButton.setFont(baseFont);
		doNotRegisterButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					evt.consume();
					doNotRegister();
				}
			}
		});
		doNotRegisterButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				doNotRegister();
			}
		});
		JButton remindMeLaterButton = new JButton("Remind me later"); // in 2 weeks
		remindMeLaterButton.setFont(baseFont);
		remindMeLaterButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					evt.consume();
					remindMeLater();
				}
			}
		});
		remindMeLaterButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				remindMeLater();
			}
		});
		buttonPanel.add(registerButton);
		buttonPanel.add(remindMeLaterButton);
		buttonPanel.add(doNotRegisterButton);
		addDivider(buttonPanel, SwingConstants.TOP, true);		
		gbc.gridx = 0;
		gbc.gridy = 14;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 10, 0, 10);
		gbc.gridwidth = 2;
		mainPanel.add(buttonPanel, gbc);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		pack();
		setResizable(false);
		// Center the dialog on the screen (we do not have the parent)
		Dimension dimension = getToolkit().getScreenSize();
		Rectangle abounds = getBounds();
		setLocation((dimension.width - abounds.width) / 2,
				(dimension.height - abounds.height) / 2);
		setSize(getPreferredSize());
	}
	
	protected void remindMeLater() {
	       try {
	            FileUtils.touch(UserRegistrationHook.remindMeLaterFile);
	        } catch (IOException ioex) {
	        	logger.error("Failed to touch the 'Remind me later' file at user registration.", ioex);
	        }
		closeDialog();		
	}

	protected void doNotRegister() {
	       try {
	            FileUtils.touch(UserRegistrationHook.doNotRegisterMeFile);
				if (UserRegistrationHook.remindMeLaterFile.exists()){
					UserRegistrationHook.remindMeLaterFile.delete();
				}
	        } catch (IOException ioex) {
	        	logger.error("Failed to touch the 'Do not register me' file at user registration.", ioex);
	        }
		closeDialog();		
	}

	protected void register() {
		if (validateForm()){
			UserRegistrationData regData = new UserRegistrationData();
			regData.setTavernaVersion(UserRegistrationHook.appName);
			regData.setFirstName(firstNameTextField.getText());
			regData.setLastName(lastNameTextField.getText());
			regData.setEmailAddress(emailTextField.getText());
			regData.setKeepMeInformed(keepMeInformedCheckBox.isSelected());
			regData.setInstitutionOrCompanyName(institutionOrCompanyTextField.getText());
			regData.setIndustry(industryTypeTextField.getSelectedItem().toString());
			regData.setField(fieldTextField.getText());
			regData.setPurposeOfUsingTaverna(fieldTextField.getText());
			
			if (postUserRegistrationDataToServer(regData)){
				saveUserRegistrationData(regData, UserRegistrationHook.registrationDataFile);
				if (UserRegistrationHook.remindMeLaterFile.exists()){
					UserRegistrationHook.remindMeLaterFile.delete();
				}
		    	closeDialog();
			}
		}		
	}

	protected boolean validateForm() {
		
		String errorMessage = "";
		if (firstNameTextField.getText().length()==0){ // Empty first name field
			errorMessage += "Please provide your first name.<br>";
		}
		if (lastNameTextField.getText().length()==0){ // Empty last name field
			errorMessage += "Please provide your last name.<br>";
		}
		if (emailTextField.getText().length()==0){ // Empty email field empty
			errorMessage += "Please provide your email address.<br>";
		}
		if (institutionOrCompanyTextField.getText().length()==0){ // Institution/company field empty
			errorMessage += "Please provide your institution or company name.";
		}
		if (errorMessage.equals("")){
			if (!termsAndConditionsCheckBox.isSelected()){
				JOptionPane.showMessageDialog(this, new JLabel("You must agree to the terms and conditions."), "Error in form",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}
		else{
			JOptionPane.showMessageDialog(this, new JLabel("<html><body>"
					+ errorMessage + "</body></html>"), "Error in form",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	public UserRegistrationData loadUserRegistrationData(File propertiesFile){
		
		UserRegistrationData regData = new UserRegistrationData();  
		Properties props = new Properties();

		// Try to retrieve data from file
		try {
			props.load(new FileInputStream(propertiesFile));
			regData.setTavernaVersion(props.getProperty(TAVERNA_VERSION_PROPERTY_NAME));
			regData.setFirstName(props.getProperty(FIRST_NAME_PROPERTY_NAME));
			regData.setLastName(props.getProperty(LAST_NAME_PROPERTY_NAME));
			regData.setEmailAddress(props.getProperty(EMAIL_ADDRESS_PROPERTY_NAME));
			regData.setKeepMeInformed((props.getProperty(KEEP_ME_INFORMED_PROPERTY_NAME).equals(TRUE) ? true : false));
			regData.setInstitutionOrCompanyName(props.getProperty(INSTITUTION_OR_COMPANY_PROPERTY_NAME));
			regData.setIndustry(props.getProperty(INDUSTRY_PROPERTY_NAME));
			regData.setField(props.getProperty(FIELD_PROPERTY_NAME));
			regData.setPurposeOfUsingTaverna(props.getProperty(PURPOSE_PROPERTY_NAME));

		} catch (IOException e) {
			logger.error("Failed to load old user registration data from " + propertiesFile.getAbsolutePath(), e);
			return null;
		}
        return regData;
	}
	
	/**
	 * Post registration data to our server.
	 */
	private boolean postUserRegistrationDataToServer(UserRegistrationData regData) {

		String parameters = "";
		
		// The 'submit' parameter - to let the server-side script know we are submitting 
        // the user's registration form - all other requests will be silently ignored
		try{
			parameters = URLEncoder.encode(TAVERNA_REGISTRATION_POST_PARAMETER_NAME, "UTF-8") + "=" + URLEncoder.encode("submit", "UTF-8"); // value does not matter

			parameters += "&" + URLEncoder.encode(TAVERNA_VERSION_POST_PARAMETER_NAME, "UTF-8") + "=" + URLEncoder.encode(regData.getTavernaVersion(), "UTF-8");
			parameters += "&" + URLEncoder.encode(FIRST_NAME_POST_PARAMETER_NAME, "UTF-8") + "=" + URLEncoder.encode(regData.getFirstName(), "UTF-8");
			parameters += "&" + URLEncoder.encode(LAST_NAME_POST_PARAMETER_NAME, "UTF-8") + "=" + URLEncoder.encode(regData.getLastName(), "UTF-8");
			parameters += "&" + URLEncoder.encode(EMAIL_ADDRESS_POST_PARAMETER_NAME, "UTF-8") + "=" + URLEncoder.encode(regData.getEmailAddress(), "UTF-8");
			parameters += "&" + URLEncoder.encode(KEEP_ME_INFORMED_POST_PARAMETER_PROPERTY_NAME, "UTF-8") + "=" + URLEncoder.encode(regData.getKeepMeInformed()? TRUE : FALSE, "UTF-8");
			parameters += "&" + URLEncoder.encode(INSTITUTION_OR_COMPANY_POST_PARAMETER_NAME, "UTF-8") + "=" + URLEncoder.encode(regData.getInstitutionOrCompanyName(), "UTF-8");
			parameters += "&" + URLEncoder.encode(INDUSTRY_TYPE_POST_PARAMETER_NAME, "UTF-8") + "=" + URLEncoder.encode(regData.getIndustry(), "UTF-8");
			parameters += "&" + URLEncoder.encode(FIELD_POST_PARAMETER_NAME, "UTF-8") + "=" + URLEncoder.encode(regData.getField(), "UTF-8");
			parameters += "&" + URLEncoder.encode(PURPOSE_POST_PARAMETER_NAME, "UTF-8") + "=" + URLEncoder.encode(regData.getPurposeOfUsingTaverna(), "UTF-8");
		}
		catch(UnsupportedEncodingException ueex){
			logger.error("Failed to url encode post parameters when sending user registration data.", ueex);
			JOptionPane
			.showMessageDialog(
					null,
					"User registration failed. Please try again later.",
					"Error encoding registration data",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
        String server = REGISTRATION_URL;
        //server = "http://localhost/~alex/taverna_registration/registration.php";
		logger.info("Posting user registartion to " + server + " with parameters: "
				+ parameters);
		String response = "";
		try{
			URL url = new URL(server);
			URLConnection conn = url.openConnection();
			// Set timeout to e.g. 7 seconds, otherwise we might hang too long 
			// if server is not responding and it will block Taverna
			conn.setConnectTimeout(7000);
			// Set connection parameters
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			// Make server believe we are HTML form data...
			conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			// Write out the bytes of the content string to the stream.
			out.writeBytes(parameters);
			out.flush();
			out.close();
			// Read response from the input stream.
			BufferedReader in = new BufferedReader(new InputStreamReader(conn
				.getInputStream()));
			String temp;
			while ((temp = in.readLine()) != null) {
				response += temp + "\n";
			}
			// Remove the last \n character
			if (!response.equals("")){
				response = response.substring(0, response.length() - 1);
			}
			in.close();
			if (!response.equals("Registration successful!")){
				logger.error("Registration failed. Response form server was: " + response);
				JOptionPane
				.showMessageDialog(
						null,
						"User registration failed. Please try again later.",
						"Error saving registration data on the server",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}
		// Catch some runtime exceptions
		catch(ConnectException ceex){ //the connection was refused remotely (e.g. no process is listening on the remote address/port). 
			logger.error("User registration failed: Registration server is not listening of the specified url.", ceex);
			JOptionPane
			.showMessageDialog(
					null,
					"User registration failed. Please try again later.",
					"Registration server is not listening at the specified url",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// Catch some runtime exceptions
		catch(SocketTimeoutException stex){ //timeout has occurred on a socket read or accept. 
			logger.error("User registration failed: Socket timeout occurred.", stex);
			JOptionPane
			.showMessageDialog(
					null,
					"User registration failed. Please try again later.",
					"Registration server timeout",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		catch(MalformedURLException muex){
			logger.error("User registration failed: Registartion server's url is malformed.", muex);
			JOptionPane
			.showMessageDialog(
					null,
					"User registration failed. Please try again later.",
					"Error with registration server's url",
					JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException ioex) {
			logger.error("User registration failed: Failed to open url connection to registration server or writing/reading to/from it.", ioex);
			JOptionPane
			.showMessageDialog(
					null,
					"User registration failed. Please try again later.",
					"Error opening connection to the registration server",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	private void saveUserRegistrationData(UserRegistrationData regData, File propertiesFile){
		Properties props = new Properties();
		props.setProperty(TAVERNA_VERSION_PROPERTY_NAME, regData.getTavernaVersion());
		props.setProperty(FIRST_NAME_PROPERTY_NAME, regData.getFirstName());
		props.setProperty(LAST_NAME_PROPERTY_NAME, regData.getLastName());
		props.setProperty(EMAIL_ADDRESS_PROPERTY_NAME, regData.getEmailAddress());
		props.setProperty(KEEP_ME_INFORMED_PROPERTY_NAME, regData.getKeepMeInformed()? TRUE : FALSE);
		props.setProperty(INSTITUTION_OR_COMPANY_PROPERTY_NAME, regData.getInstitutionOrCompanyName());
		props.setProperty(INDUSTRY_PROPERTY_NAME, regData.getIndustry());
		props.setProperty(FIELD_PROPERTY_NAME, regData.getPurposeOfUsingTaverna());
		props.setProperty(PURPOSE_PROPERTY_NAME, regData.getPurposeOfUsingTaverna());

		// Write the properties file.
	    try {
	        props.store(new FileOutputStream(propertiesFile), null);
	    } catch (Exception e) {
			logger.error("Failed to save user registration data locally on disk.");
	    }

	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}


	/**
	 * Adds a light gray or etched border to the top or bottom of a JComponent.
	 * 
	 * @author David Withers
	 * @param component
	 */
	protected void addDivider(JComponent component, final int position, final boolean etched) {
		component.setBorder(new Border() {
			private final Color borderColor = new Color(.6f, .6f, .6f);
			
			public Insets getBorderInsets(Component c) {
				if (position == SwingConstants.TOP) {
					return new Insets(5, 0, 0, 0);
				} else {
					return new Insets(0, 0, 5, 0);
				}
			}

			public boolean isBorderOpaque() {
				return false;
			}

			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				if (position == SwingConstants.TOP) {
					if (etched) {
						g.setColor(borderColor);
						g.drawLine(x, y, x + width, y);
						g.setColor(Color.WHITE);
						g.drawLine(x, y + 1, x + width, y + 1);
					} else {
						g.setColor(Color.LIGHT_GRAY);
						g.drawLine(x, y, x + width, y);
					}
				} else {
					if (etched) {
						g.setColor(borderColor);
						g.drawLine(x, y + height - 2, x + width, y + height - 2);
						g.setColor(Color.WHITE);
						g.drawLine(x, y + height - 1, x + width, y + height - 1);
					} else {
						g.setColor(Color.LIGHT_GRAY);
						g.drawLine(x, y + height - 1, x + width, y + height - 1);
					}
				}
			}

		});
	}

}
