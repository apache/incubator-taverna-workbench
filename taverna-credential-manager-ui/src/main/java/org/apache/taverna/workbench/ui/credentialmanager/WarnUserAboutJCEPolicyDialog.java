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
/*

package org.apache.taverna.workbench.ui.credentialmanager;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Desktop.getDesktop;
import static javax.swing.border.EtchedBorder.LOWERED;
import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;
import static org.apache.commons.io.FileUtils.touch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.taverna.security.credentialmanager.DistinguishedNameParser;
import org.apache.taverna.workbench.helper.NonBlockedHelpEnabledDialog;

import org.apache.log4j.Logger;
import org.apache.taverna.configuration.app.ApplicationConfiguration;

/**
 * Dialog that warns user that they need to install unlimited cryptography
 * strength policy for Java.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class WarnUserAboutJCEPolicyDialog extends NonBlockedHelpEnabledDialog {
	private static final Logger logger = Logger
			.getLogger(WarnUserAboutJCEPolicyDialog.class);

	private JCheckBox doNotWarnMeAgainCheckBox;
	private final ApplicationConfiguration applicationConfiguration;
	private final DistinguishedNameParser dnParser;

	public WarnUserAboutJCEPolicyDialog(
			ApplicationConfiguration applicationConfiguration,
			DistinguishedNameParser dnParser) {
		super((Frame) null,
				"Java Unlimited Strength Cryptography Policy Warning", true);
		this.applicationConfiguration = applicationConfiguration;
		this.dnParser = dnParser;
		initComponents();
	}

	// For testing
	public static void main(String[] args) {
		WarnUserAboutJCEPolicyDialog dialog = new WarnUserAboutJCEPolicyDialog(
				null, null);
		dialog.setVisible(true);
	}

	private void initComponents() {
		// Base font for all components on the form
		Font baseFont = new JLabel("base font").getFont().deriveFont(11f);

		// Message saying that updates are available
		JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10,
				10), new EtchedBorder(LOWERED)));

		JEditorPane message = new JEditorPane();
		message.setEditable(false);
		message.setBackground(this.getBackground());
		message.setFocusable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		message.setEditorKit(kit);
		StyleSheet styleSheet = kit.getStyleSheet();
		//styleSheet.addRule("body {font-family:"+baseFont.getFamily()+"; font-size:"+baseFont.getSize()+";}"); // base font looks bigger when rendered as HTML
		styleSheet.addRule("body {font-family:" + baseFont.getFamily()
				+ "; font-size:10px;}");
		Document doc = kit.createDefaultDocument();
		message.setDocument(doc);
		message.setText("<html><body>In order for Taverna's security features to function properly - you need to install<br>"
				+ "'Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy'. <br><br>"
				+ "If you do not already have it, for <b>Java 6</b> you can get it from:<br>"
				+ "<a href=\"http://www.oracle.com/technetwork/java/javase/downloads/index.html\">http://www.oracle.com/technetwork/java/javase/downloads/index.html</a><br<br>"
				+ "Installation instructions are contained in the bundle you download."
				+ "</body><html>");
		message.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent he) {
				HyperlinkEvent.EventType type = he.getEventType();
				if (type == ACTIVATED)
					// Open a Web browser
					try {
						getDesktop().browse(he.getURL().toURI());
//						BrowserLauncher launcher = new BrowserLauncher();
//						launcher.openURLinBrowser(he.getURL().toString());
					} catch (Exception ex) {
						logger.error("Failed to launch browser to fetch JCE "
								+ he.getURL());
					}
			}
		});
		message.setBorder(new EmptyBorder(5, 5, 5, 5));
		messagePanel.add(message, CENTER);

		doNotWarnMeAgainCheckBox = new JCheckBox("Do not warn me again");
		doNotWarnMeAgainCheckBox.setFont(baseFont.deriveFont(12f));
		messagePanel.add(doNotWarnMeAgainCheckBox, SOUTH);

		// Buttons
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okButton = new JButton("OK");
		okButton.setFont(baseFont);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed();
			}
		});

		buttonsPanel.add(okButton);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(messagePanel, CENTER);
		getContentPane().add(buttonsPanel, SOUTH);

		pack();
		setResizable(false);
		// Center the dialog on the screen (we do not have the parent)
		Dimension dimension = getToolkit().getScreenSize();
		Rectangle abounds = getBounds();
		setLocation((dimension.width - abounds.width) / 2,
				(dimension.height - abounds.height) / 2);
		setSize(getPreferredSize());
	}

	private static final String DO_NOT_WARN_ABOUT_JCE_POLICY = "do_not_warn_about_JCE_policy";
	public static boolean warnedUser = false; // have we already warned user for
												// this run

	/**
	 * Warn user that they need to install Java Cryptography Extension (JCE)
	 * Unlimited Strength Jurisdiction Policy if they want Credential Manager to
	 * function properly.
	 */
	public static void warnUserAboutJCEPolicy(
			ApplicationConfiguration applicationConfiguration,
			DistinguishedNameParser dnParser) {
		/*
		 * Do not pop up a dialog if we are running headlessly. If we have
		 * warned the user and they do not want us to remind them again - exit.
		 */
		if (warnedUser || GraphicsEnvironment.isHeadless()
				|| doNotWarnFile(applicationConfiguration, dnParser).exists())
			return;

		WarnUserAboutJCEPolicyDialog warnDialog = new WarnUserAboutJCEPolicyDialog(
				applicationConfiguration, dnParser);
		warnDialog.setVisible(true);
		warnedUser = true;
	}

	private static File doNotWarnFile(
			ApplicationConfiguration applicationConfiguration,
			DistinguishedNameParser dnParser) {
		return new File(
				dnParser.getCredentialManagerDefaultDirectory(applicationConfiguration).toFile(),
				DO_NOT_WARN_ABOUT_JCE_POLICY);
	}

	protected void okPressed() {
		try {
			if (doNotWarnMeAgainCheckBox.isSelected())
				touch(doNotWarnFile(applicationConfiguration, dnParser));
		} catch (IOException e) {
			logger.error(
					"Failed to touch the 'Do not want me about JCE unilimited security policy' file.",
					e);
		}
		closeDialog();
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}

}
