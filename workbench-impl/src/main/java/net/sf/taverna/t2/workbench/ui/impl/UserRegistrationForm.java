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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.WHITE;
import static java.awt.FlowLayout.LEFT;
import static java.awt.Font.BOLD;
import static java.awt.GridBagConstraints.FIRST_LINE_START;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_TAB;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingConstants.BOTTOM;
import static javax.swing.SwingConstants.TOP;
import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.tavernaCogs32x32Icon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
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
import java.awt.event.KeyAdapter;
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
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
/**
 * User registration form.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class UserRegistrationForm extends HelpEnabledDialog {
	private static final String FAILED = "User registration failed: ";

	private static final String REGISTRATION_FAILED_MSG = "User registration failed. Please try again later.";

	private static final String REGISTRATION_URL = "http://www.mygrid.org.uk/taverna/registration/";

	public static final String TAVERNA_VERSION_PROPERTY_NAME = "Taverna version";
	public static final String FIRST_NAME_PROPERTY_NAME = "First name";
	public static final String LAST_NAME_PROPERTY_NAME = "Last name";
	public static final String EMAIL_ADDRESS_PROPERTY_NAME = "Email address";
	public static final String INSTITUTION_OR_COMPANY_PROPERTY_NAME = "Institution or company name";
	public static final String INDUSTRY_PROPERTY_NAME = "Industry";
	public static final String FIELD_PROPERTY_NAME = "Field of investigation";
	public static final String PURPOSE_PROPERTY_NAME = "Purpose of using Taverna";
	public static final String KEEP_ME_INFORMED_PROPERTY_NAME = "Keep me informed by email";

	public static final String TAVERNA_REGISTRATION_POST_PARAMETER_NAME = "taverna_registration";
	public static final String TAVERNA_VERSION_POST_PARAMETER_NAME = "taverna_version";
	public static final String FIRST_NAME_POST_PARAMETER_NAME = "first_name";
	public static final String LAST_NAME_POST_PARAMETER_NAME = "last_name";
	public static final String EMAIL_ADDRESS_POST_PARAMETER_NAME = "email";
	public static final String INSTITUTION_OR_COMPANY_POST_PARAMETER_NAME = "institution_or_company";
	public static final String INDUSTRY_TYPE_POST_PARAMETER_NAME = "industry_type";
	public static final String FIELD_POST_PARAMETER_NAME = "field";
	public static final String PURPOSE_POST_PARAMETER_NAME = "purpose";
	public static final String KEEP_ME_INFORMED_POST_PARAMETER_PROPERTY_NAME = "keep_me_informed";

	private static String TRUE = Boolean.TRUE.toString();
	private static String FALSE = Boolean.FALSE.toString();

	private static final String WELCOME = "Welcome to the Taverna User Registration Form";
	private static final String PLEASE_FILL_IN_THIS_REGISTRATION_FORM = "Please fill in this registration form to let us know that you are using Taverna";

	private static final String WE_DO = "Note that by registering:\n"
			+ "   \u25CF We do not have access to your data\n"
			+ "   \u25CF We do not have access to your service usage\n"
			+ "   \u25CF You will not be monitored\n"
			+ "   \u25CF We do record the information you provide\n"
			+ "     at registration time";

	private static final String WHY_REGISTER = "By registering you will:\n"
			+ "   \u25CF Allow us to support you better; future plans will be\n"
			+ "     directed towards solutions Taverna users require\n"
			+ "   \u25CF Help sustain Taverna development; our continued\n"
			+ "     funding relies on us showing usage\n"
			+ "   \u25CF (Optionally) Hear about news and product updates";

	private static final String FIRST_NAME = "*First name:";
	private static final String LAST_NAME = "*Last name:";
	private static final String EMAIL_ADDRESS = "*Email address:";
	private static final String KEEP_ME_INFORMED = "Keep me informed of news and product updates via email";
	private static final String INSTITUTION_COMPANY_NAME = "*Institution/Company name:";
	private static final String FIELD_OF_INVESTIGATION = " Field of investigation:\n"
			+ " (e.g. bioinformatics)";
	private static final String WHY_YOU_INTEND_TO_USE_TAVERNA = " A brief description of how you intend\n"
			+ " to use Taverna: (e.g. genome analysis\n"
			+ " for bacterial strain identification)";

	private static String[] industryTypes = { "", "Academia - Life Sciences",
			"Academia - Social Sciences", "Academia - Physical Sciences",
			"Academia - Environmental Sciences", "Academia - Other",
			"Industry - Biotechnology", "Industry - Pharmaceutical",
			"Industry - Engineering", "Industry - Other",
			"Healthcare Services", "Goverment and Public Sector", "Other" };

	private static final String I_AGREE_TO_THE_TERMS_AND_CONDITIONS = "I agree to the terms and conditions of registration at";
	private static final String TERMS_AND_CONDITIONS_URL = "http://www.taverna.org.uk/legal/terms";

	private Logger logger = Logger.getLogger(UserRegistrationForm.class);
	private UserRegistrationData previousRegistrationData;
	private JTextField firstNameTextField;
	private JTextField lastNameTextField;
	private JTextField emailTextField;
	private JCheckBox keepMeInformedCheckBox;
	private JTextField institutionOrCompanyTextField;
	private JComboBox<String> industryTypeTextField;
	private JTextField fieldTextField;
	private JTextArea purposeTextArea;
	private JCheckBox termsAndConditionsCheckBox;

	private final File registrationDataFile;
	private final File remindMeLaterFile;
	private final File doNotRegisterMeFile;
	private final String appName;

	public UserRegistrationForm(String appName, File registrationDataFile,
			File doNotRegisterMeFile, File remindMeLaterFile) {
		super((Frame) null, "Taverna User Registration", true);
		this.appName = appName;
		this.registrationDataFile = registrationDataFile;
		this.doNotRegisterMeFile = doNotRegisterMeFile;
		this.remindMeLaterFile = remindMeLaterFile;
		initComponents();
	}

	public UserRegistrationForm(String appName,
			File previousRegistrationDataFile, File registrationDataFile,
			File doNotRegisterMeFile, File remindMeLaterFile) {
		super((Frame) null, "Taverna User Registration", true);
		this.appName = appName;
		this.registrationDataFile = registrationDataFile;
		this.doNotRegisterMeFile = doNotRegisterMeFile;
		this.remindMeLaterFile = remindMeLaterFile;
		previousRegistrationData = loadUserRegistrationData(previousRegistrationDataFile);
		initComponents();
	}

	// For testing only
	// public static void main(String[] args) throws ClassNotFoundException,
	// InstantiationException, IllegalAccessException,
	// UnsupportedLookAndFeelException{
	// WorkbenchImpl.setLookAndFeel();
	// UserRegistrationForm form = new UserRegistrationForm();
	// form.setVisible(true);
	// }

	private void initComponents() {
		JPanel mainPanel = new JPanel(new GridBagLayout());

		// Base font for all components on the form
		Font baseFont = new JLabel("base font").getFont().deriveFont(11f);

		// Title panel
		JPanel titlePanel = new JPanel(new FlowLayout(LEFT));
		titlePanel.setBackground(WHITE);
		// titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		JLabel titleLabel = new JLabel(WELCOME);
		titleLabel.setFont(baseFont.deriveFont(BOLD, 13.5f));
		// titleLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
		JLabel titleIcon = new JLabel(tavernaCogs32x32Icon);
		// titleIcon.setBorder(new EmptyBorder(10, 10, 10, 10));
		DialogTextArea titleMessage = new DialogTextArea(
				PLEASE_FILL_IN_THIS_REGISTRATION_FORM);
		titleMessage.setMargin(new Insets(0, 20, 0, 10));
		titleMessage.setFont(baseFont);
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);
		// titlePanel.setBorder( new EmptyBorder(10, 10, 0, 10));
		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.add(titleLabel, NORTH);
		messagePanel.add(titleMessage, CENTER);
		messagePanel.setBackground(WHITE);
		titlePanel.add(titleIcon);
		titlePanel.add(messagePanel);
		addDivider(titlePanel, BOTTOM, true);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.gridwidth = 2;
		// gbc.insets = new Insets(5, 10, 0, 0);
		mainPanel.add(titlePanel, gbc);

		// Registration messages
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.gridwidth = 2;
		// gbc.insets = new Insets(5, 0, 0, 10);
		DialogTextArea registrationMessage1 = new DialogTextArea(WHY_REGISTER);
		registrationMessage1.setMargin(new Insets(0, 10, 0, 0));
		registrationMessage1.setFont(baseFont);
		registrationMessage1.setEditable(false);
		registrationMessage1.setFocusable(false);
		registrationMessage1.setBackground(getBackground());

		DialogTextArea registrationMessage2 = new DialogTextArea(WE_DO);
		registrationMessage2.setMargin(new Insets(0, 10, 0, 10));
		registrationMessage2.setFont(baseFont);
		registrationMessage2.setEditable(false);
		registrationMessage2.setFocusable(false);
		registrationMessage2.setBackground(getBackground());
		JPanel registrationMessagePanel = new JPanel(new FlowLayout(
				FlowLayout.CENTER));
		registrationMessagePanel.add(registrationMessage1);
		registrationMessagePanel.add(registrationMessage2);
		addDivider(registrationMessagePanel, BOTTOM, true);
		mainPanel.add(registrationMessagePanel, gbc);

		// Mandatory label
		// JLabel mandatoryLabel = new JLabel("* Mandatory fields");
		// mandatoryLabel.setFont(baseFont);
		// gbc.weightx = 0.0;
		// gbc.weighty = 0.0;
		// gbc.gridx = 0;
		// gbc.gridy = 3;
		// gbc.fill = NONE;
		// gbc.anchor = GridBagConstraints.EAST;
		// gbc.gridwidth = 2;
		// gbc.insets = new Insets(0, 10, 0, 20);
		// mainPanel.add(mandatoryLabel, gbc);

		// First name
		JLabel firstNameLabel = new JLabel(FIRST_NAME);
		firstNameLabel.setFont(baseFont);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 10, 0, 10);
		mainPanel.add(firstNameLabel, gbc);

		firstNameTextField = new JTextField();
		firstNameTextField.setFont(baseFont);
		if (previousRegistrationData != null)
			firstNameTextField.setText(previousRegistrationData.getFirstName());
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(firstNameTextField, gbc);

		// Last name
		JLabel lastNameLabel = new JLabel(LAST_NAME);
		lastNameLabel.setFont(baseFont);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 10, 0, 10);
		mainPanel.add(lastNameLabel, gbc);

		lastNameTextField = new JTextField();
		lastNameTextField.setFont(baseFont);
		if (previousRegistrationData != null)
			lastNameTextField.setText(previousRegistrationData.getLastName());
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(lastNameTextField, gbc);

		// Email address
		JLabel emailLabel = new JLabel(EMAIL_ADDRESS);
		emailLabel.setFont(baseFont);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(emailLabel, gbc);

		emailTextField = new JTextField();
		emailTextField.setFont(baseFont);
		if (previousRegistrationData != null)
			emailTextField.setText(previousRegistrationData.getEmailAddress());
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(emailTextField, gbc);

		// Keep me informed
		keepMeInformedCheckBox = new JCheckBox(KEEP_ME_INFORMED);
		keepMeInformedCheckBox.setFont(baseFont);
		if (previousRegistrationData != null)
			keepMeInformedCheckBox.setSelected(previousRegistrationData
					.getKeepMeInformed());
		keepMeInformedCheckBox.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == VK_ENTER) {
					evt.consume();
					keepMeInformedCheckBox.setSelected(!keepMeInformedCheckBox
							.isSelected());
				}
			}
		});
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(keepMeInformedCheckBox, gbc);

		// Institution name
		JLabel institutionLabel = new JLabel(INSTITUTION_COMPANY_NAME);
		institutionLabel.setFont(baseFont);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(institutionLabel, gbc);

		institutionOrCompanyTextField = new JTextField();
		institutionOrCompanyTextField.setFont(baseFont);
		if (previousRegistrationData != null)
			institutionOrCompanyTextField.setText(previousRegistrationData
					.getInstitutionOrCompanyName());
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 8;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(institutionOrCompanyTextField, gbc);

		// Industry type
		JLabel industryLabel = new JLabel(" Industry type:");
		industryLabel.setFont(baseFont);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 9;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(industryLabel, gbc);

		industryTypeTextField = new JComboBox<>(industryTypes);
		industryTypeTextField.setFont(baseFont);
		if (previousRegistrationData != null)
			industryTypeTextField.setSelectedItem(previousRegistrationData
					.getIndustry());
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 9;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
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
		gbc.fill = NONE;
		gbc.anchor = LINE_START;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(fieldLabel, gbc);

		fieldTextField = new JTextField();
		fieldTextField.setFont(baseFont);
		if (previousRegistrationData != null)
			fieldTextField.setText(previousRegistrationData.getField());
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridx = 1;
		gbc.gridy = 10;
		gbc.fill = HORIZONTAL;
		gbc.anchor = FIRST_LINE_START;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
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
		gbc.fill = NONE;
		gbc.anchor = LINE_START;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(purposeLabel, gbc);

		purposeTextArea = new JTextArea(4, 30);
		purposeTextArea.setFont(baseFont);
		purposeTextArea.setLineWrap(true);
		purposeTextArea.setAutoscrolls(true);
		if (previousRegistrationData != null)
			purposeTextArea.setText(previousRegistrationData
					.getPurposeOfUsingTaverna());
		purposeTextArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == VK_TAB) {
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
		gbc.fill = HORIZONTAL;
		gbc.anchor = FIRST_LINE_START;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(5, 10, 0, 10);
		mainPanel.add(purposeScrollPane, gbc);

		// Terms and conditions
		termsAndConditionsCheckBox = new JCheckBox(
				I_AGREE_TO_THE_TERMS_AND_CONDITIONS);
		termsAndConditionsCheckBox.setFont(baseFont);
		termsAndConditionsCheckBox.setBorder(null);
		termsAndConditionsCheckBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == VK_ENTER) {
					evt.consume();
					termsAndConditionsCheckBox
							.setSelected(!termsAndConditionsCheckBox
									.isSelected());
				}
			}
		});
		// gbc.weightx = 0.0;
		// gbc.weighty = 0.0;
		// gbc.gridx = 0;
		// gbc.gridy = 12;
		// gbc.fill = NONE;
		// gbc.anchor = WEST;
		// gbc.gridwidth = 2;
		// gbc.insets = new Insets(10, 10, 0, 0);
		// mainPanel.add(termsAndConditionsCheckBox, gbc);

		// Terms and conditions link
		JEditorPane termsAndConditionsURL = new JEditorPane();
		termsAndConditionsURL.setEditable(false);
		termsAndConditionsURL.setBackground(this.getBackground());
		termsAndConditionsURL.setFocusable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		termsAndConditionsURL.setEditorKit(kit);
		StyleSheet styleSheet = kit.getStyleSheet();
		// styleSheet.addRule("body {font-family:"+baseFont.getFamily()+"; font-size:"+baseFont.getSize()+";}");
		// // base font looks bigger when rendered as HTML
		styleSheet.addRule("body {font-family:" + baseFont.getFamily()
				+ "; font-size:9px;}");
		Document doc = kit.createDefaultDocument();
		termsAndConditionsURL.setDocument(doc);
		termsAndConditionsURL.setText("<html><body><a href=\""
				+ TERMS_AND_CONDITIONS_URL + "\">" + TERMS_AND_CONDITIONS_URL
				+ "</a></body></html>");
		termsAndConditionsURL.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent he) {
				if (he.getEventType() == ACTIVATED)
					followHyperlinkToTandCs();
			}
		});
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 13;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5, 10, 0, 10);
		JPanel termsAndConditionsPanel = new JPanel(new FlowLayout(
				FlowLayout.LEFT));
		termsAndConditionsPanel.add(termsAndConditionsCheckBox);
		termsAndConditionsPanel.add(termsAndConditionsURL);
		mainPanel.add(termsAndConditionsPanel, gbc);

		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton registerButton = new JButton("Register");
		registerButton.setFont(baseFont);
		registerButton.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == VK_ENTER) {
					evt.consume();
					register();
				}
			}
		});
		registerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				register();
			}
		});
		JButton doNotRegisterButton = new JButton("Do not ask me again");
		doNotRegisterButton.setFont(baseFont);
		doNotRegisterButton.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == VK_ENTER) {
					evt.consume();
					doNotRegister();
				}
			}
		});
		doNotRegisterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doNotRegister();
			}
		});
		JButton remindMeLaterButton = new JButton("Remind me later"); // in 2 weeks
		remindMeLaterButton.setFont(baseFont);
		remindMeLaterButton.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == VK_ENTER) {
					evt.consume();
					remindMeLater();
				}
			}
		});
		remindMeLaterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				remindMeLater();
			}
		});
		buttonPanel.add(registerButton);
		buttonPanel.add(remindMeLaterButton);
		buttonPanel.add(doNotRegisterButton);
		addDivider(buttonPanel, TOP, true);
		gbc.gridx = 0;
		gbc.gridy = 14;
		gbc.fill = HORIZONTAL;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 10, 0, 10);
		gbc.gridwidth = 2;
		mainPanel.add(buttonPanel, gbc);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel, CENTER);

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
			FileUtils.touch(remindMeLaterFile);
		} catch (IOException ioex) {
			logger.error(
					"Failed to touch the 'Remind me later' file at user registration.",
					ioex);
		}
		closeDialog();
	}

	protected void doNotRegister() {
		try {
			FileUtils.touch(doNotRegisterMeFile);
			if (remindMeLaterFile.exists()) {
				remindMeLaterFile.delete();
			}
		} catch (IOException ioex) {
			logger.error(
					"Failed to touch the 'Do not register me' file at user registration.",
					ioex);
		}
		closeDialog();
	}

	private void register() {
		if (validateForm()) {
			UserRegistrationData regData = new UserRegistrationData();
			regData.setTavernaVersion(appName);
			regData.setFirstName(firstNameTextField.getText());
			regData.setLastName(lastNameTextField.getText());
			regData.setEmailAddress(emailTextField.getText());
			regData.setKeepMeInformed(keepMeInformedCheckBox.isSelected());
			regData.setInstitutionOrCompanyName(institutionOrCompanyTextField
					.getText());
			regData.setIndustry(industryTypeTextField.getSelectedItem()
					.toString());
			regData.setField(fieldTextField.getText());
			regData.setPurposeOfUsingTaverna(purposeTextArea.getText());

			if (postUserRegistrationDataToServer(regData)) {
				saveUserRegistrationData(regData, registrationDataFile);
				if (remindMeLaterFile.exists())
					remindMeLaterFile.delete();
				closeDialog();
			}
		}
	}

	private boolean validateForm() {
		String errorMessage = "";
		if (firstNameTextField.getText().isEmpty())
			errorMessage += "Please provide your first name.<br>";
		if (lastNameTextField.getText().isEmpty())
			errorMessage += "Please provide your last name.<br>";
		if (emailTextField.getText().isEmpty())
			errorMessage += "Please provide your email address.<br>";
		if (institutionOrCompanyTextField.getText().isEmpty())
			errorMessage += "Please provide your institution or company name.";
		if (!errorMessage.isEmpty()) {
			showMessageDialog(this, new JLabel("<html><body>"
					+ errorMessage + "</body></html>"), "Error in form",
					ERROR_MESSAGE);
			return false;
		}
		if (!termsAndConditionsCheckBox.isSelected()) {
			showMessageDialog(this, new JLabel(
					"You must agree to the terms and conditions."),
					"Error in form", ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public UserRegistrationData loadUserRegistrationData(File propertiesFile) {
		UserRegistrationData regData = new UserRegistrationData();
		Properties props = new Properties();

		// Try to retrieve data from file
		try {
			props.load(new FileInputStream(propertiesFile));
			regData.setTavernaVersion(props
					.getProperty(TAVERNA_VERSION_PROPERTY_NAME));
			regData.setFirstName(props.getProperty(FIRST_NAME_PROPERTY_NAME));
			regData.setLastName(props.getProperty(LAST_NAME_PROPERTY_NAME));
			regData.setEmailAddress(props
					.getProperty(EMAIL_ADDRESS_PROPERTY_NAME));
			regData.setKeepMeInformed((props.getProperty(
					KEEP_ME_INFORMED_PROPERTY_NAME).equals(TRUE) ? true : false));
			regData.setInstitutionOrCompanyName(props
					.getProperty(INSTITUTION_OR_COMPANY_PROPERTY_NAME));
			regData.setIndustry(props.getProperty(INDUSTRY_PROPERTY_NAME));
			regData.setField(props.getProperty(FIELD_PROPERTY_NAME));
			regData.setPurposeOfUsingTaverna(props
					.getProperty(PURPOSE_PROPERTY_NAME));
		} catch (IOException e) {
			logger.error("Failed to load old user registration data from "
					+ propertiesFile.getAbsolutePath(), e);
			return null;
		}
		return regData;
	}

	private void enc(StringBuilder buffer, String name, Object value)
			throws UnsupportedEncodingException {
		if (buffer.length() != 0)
			buffer.append('&');
		buffer.append(URLEncoder.encode(name, "UTF-8"));
		buffer.append('=');
		buffer.append(URLEncoder.encode(value.toString(), "UTF-8"));
	}

	/**
	 * Post registration data to our server.
	 */
	private boolean postUserRegistrationDataToServer(
			UserRegistrationData regData) {
		StringBuilder parameters = new StringBuilder();

		/*
		 * The 'submit' parameter - to let the server-side script know we are
		 * submitting the user's registration form - all other requests will be
		 * silently ignored
		 */
		try {
			// value does not matter
			enc(parameters, TAVERNA_REGISTRATION_POST_PARAMETER_NAME, "submit");

			enc(parameters, TAVERNA_VERSION_POST_PARAMETER_NAME,
					regData.getTavernaVersion());
			enc(parameters, FIRST_NAME_POST_PARAMETER_NAME,
					regData.getFirstName());
			enc(parameters, LAST_NAME_POST_PARAMETER_NAME,
					regData.getLastName());
			enc(parameters, EMAIL_ADDRESS_POST_PARAMETER_NAME,
					regData.getEmailAddress());
			enc(parameters, KEEP_ME_INFORMED_POST_PARAMETER_PROPERTY_NAME,
					regData.getKeepMeInformed());
			enc(parameters, INSTITUTION_OR_COMPANY_POST_PARAMETER_NAME,
					regData.getInstitutionOrCompanyName());
			enc(parameters, INDUSTRY_TYPE_POST_PARAMETER_NAME,
					regData.getIndustry());
			enc(parameters, FIELD_POST_PARAMETER_NAME, regData.getField());
			enc(parameters, PURPOSE_POST_PARAMETER_NAME,
					regData.getPurposeOfUsingTaverna());
		} catch (UnsupportedEncodingException ueex) {
			logger.error(FAILED + "Could not url encode post parameters", ueex);
			showMessageDialog(null, REGISTRATION_FAILED_MSG,
					"Error encoding registration data", ERROR_MESSAGE);
			return false;
		}
		String server = REGISTRATION_URL;
		logger.info("Posting user registartion to " + server
				+ " with parameters: " + parameters);
		String response = "";
		String failure;
		try {
			URL url = new URL(server);
			URLConnection conn = url.openConnection();
			/*
			 * Set timeout to e.g. 7 seconds, otherwise we might hang too long
			 * if server is not responding and it will block Taverna
			 */
			conn.setConnectTimeout(7000);
			// Set connection parameters
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			// Make server believe we are HTML form data...
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			// Write out the bytes of the content string to the stream.
			try (DataOutputStream out = new DataOutputStream(
					conn.getOutputStream())) {
				out.writeBytes(parameters.toString());
				out.flush();
			}
			// Read response from the input stream.
			try (BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()))) {
				String temp;
				while ((temp = in.readLine()) != null)
					response += temp + "\n";
				// Remove the last \n character
				if (!response.isEmpty())
					response = response.substring(0, response.length() - 1);
			}
			if (response.equals("Registration successful!"))
				return true;
			logger.error(FAILED + "Response form server was: " + response);
			failure = "Error saving registration data on the server";
		} catch (ConnectException ceex) {
			/*
			 * the connection was refused remotely (e.g. no process is listening
			 * on the remote address/port).
			 */
			logger.error(
					FAILED
							+ "Registration server is not listening of the specified url.",
					ceex);
			failure = "Registration server is not listening at the specified url";
		} catch (SocketTimeoutException stex) {
			// timeout has occurred on a socket read or accept.
			logger.error(FAILED + "Socket timeout occurred.", stex);
			failure = "Registration server timeout";
		} catch (MalformedURLException muex) {
			logger.error(FAILED + "Registartion server's url is malformed.",
					muex);
			failure = "Error with registration server's url";
		} catch (IOException ioex) {
			logger.error(
					FAILED
							+ "Failed to open url connection to registration server or writing/reading to/from it.",
					ioex);
			failure = "Error opening connection to the registration server";
		}
		showMessageDialog(null, REGISTRATION_FAILED_MSG, failure, ERROR_MESSAGE);
		return false;
	}

	private void saveUserRegistrationData(UserRegistrationData regData,
			File propertiesFile) {
		Properties props = new Properties();
		props.setProperty(TAVERNA_VERSION_PROPERTY_NAME,
				regData.getTavernaVersion());
		props.setProperty(FIRST_NAME_PROPERTY_NAME, regData.getFirstName());
		props.setProperty(LAST_NAME_PROPERTY_NAME, regData.getLastName());
		props.setProperty(EMAIL_ADDRESS_PROPERTY_NAME,
				regData.getEmailAddress());
		props.setProperty(KEEP_ME_INFORMED_PROPERTY_NAME,
				regData.getKeepMeInformed() ? TRUE : FALSE);
		props.setProperty(INSTITUTION_OR_COMPANY_PROPERTY_NAME,
				regData.getInstitutionOrCompanyName());
		props.setProperty(INDUSTRY_PROPERTY_NAME, regData.getIndustry());
		props.setProperty(FIELD_PROPERTY_NAME,
				regData.getPurposeOfUsingTaverna());
		props.setProperty(PURPOSE_PROPERTY_NAME,
				regData.getPurposeOfUsingTaverna());

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
	protected void addDivider(JComponent component, final int position,
			final boolean etched) {
		component.setBorder(new Border() {
			private final Color borderColor = new Color(.6f, .6f, .6f);

			@Override
			public Insets getBorderInsets(Component c) {
				if (position == TOP) {
					return new Insets(5, 0, 0, 0);
				} else {
					return new Insets(0, 0, 5, 0);
				}
			}

			@Override
			public boolean isBorderOpaque() {
				return false;
			}

			@Override
			public void paintBorder(Component c, Graphics g, int x, int y,
					int width, int height) {
				if (position == TOP) {
					if (etched) {
						g.setColor(borderColor);
						g.drawLine(x, y, x + width, y);
						g.setColor(WHITE);
						g.drawLine(x, y + 1, x + width, y + 1);
					} else {
						g.setColor(LIGHT_GRAY);
						g.drawLine(x, y, x + width, y);
					}
				} else {
					if (etched) {
						g.setColor(borderColor);
						g.drawLine(x, y + height - 2, x + width, y + height - 2);
						g.setColor(WHITE);
						g.drawLine(x, y + height - 1, x + width, y + height - 1);
					} else {
						g.setColor(LIGHT_GRAY);
						g.drawLine(x, y + height - 1, x + width, y + height - 1);
					}
				}
			}
		});
	}

	private void followHyperlinkToTandCs() {
		// Open a Web browser
		try {
			Desktop.getDesktop().browse(new URI(TERMS_AND_CONDITIONS_URL));
		} catch (Exception ex) {
			logger.error("User registration: Failed to launch browser to show terms and conditions at "
					+ TERMS_AND_CONDITIONS_URL);
		}
	}
}
