/**
 * 
 */
package net.sf.taverna.t2.workbench.report.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.helper.Helper;


/**
 * @author alanrw
 *
 */
public class ReportManagerConfigurationPanel extends JPanel {
	
    private static final String RESET = "Reset";
	private static final String APPLY = "Apply";
	private static final String HELP = "Help";
	private static final String ASK_ON_ERRORS_OR_WARNINGS = "Ask on errors or warnings";
	private static final String ASK_ON_ERRORS = "Ask on errors";
	private static final String DESCRIPTION = "Configure if and how the validation report is generated";
	private static final String NEVER_ASK = "Never ask";
	private static final String FULL_CHECKS = "Full checks";
	private static final String QUICK_CHECKS = "Quick checks";
	private static final String NO_CHECKS = "No checks";
	private static final String DEFAULT_TIMEOUT_STRING = "Reporting timeout in seconds (per service)";
    private static final String REPORT_EXPIRATION_STRING = "Minutes before reports expire - 0 means never";
    private static final String CHECKS_ON_OPEN = "Checks when opening a workflow";
    private static final String CHECKS_ON_EDIT = "Checks after each edit";
    private static final String CHECKS_BEFORE_RUN = "Checks before running a workflow";
    private static final String QUERY_USER_BEFORE_RUN = "Ask before run";

	private static ReportManagerConfiguration configuration = ReportManagerConfiguration.getInstance();
	
    
	/**
	 * The size of the field for the JTextFields.
	 */
	private static int TEXTFIELD_SIZE = 25;

	private JTextField timeoutField;
	private JTextField expirationField;
	private JComboBox openCombo;
	private JComboBox editCombo;
	private JComboBox runCombo;
	private JComboBox queryBeforeRunCombo;
    
    public ReportManagerConfigurationPanel() {
    	super();
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		// Title describing what kind of settings we are configuring here
        JTextArea descriptionText = new JTextArea(DESCRIPTION);
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setEditable(false);
        descriptionText.setFocusable(false);
        descriptionText.setBorder(new EmptyBorder(10, 10, 10, 10));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(descriptionText, gbc);
        
        openCombo = new JComboBox(new Object[] {NO_CHECKS, QUICK_CHECKS, FULL_CHECKS});
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10,0,0,0);
        this.add(new JLabel(CHECKS_ON_OPEN), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(openCombo, gbc);
        
        editCombo = new JComboBox(new Object[] {NO_CHECKS, QUICK_CHECKS});
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10,0,0,0);
        this.add(new JLabel(CHECKS_ON_EDIT), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(editCombo, gbc);
        
        runCombo = new JComboBox(new Object[] {QUICK_CHECKS, FULL_CHECKS});
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10,0,0,0);
        this.add(new JLabel(CHECKS_BEFORE_RUN), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(runCombo, gbc);
        
        timeoutField = new JTextField(TEXTFIELD_SIZE);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10,0,0,0);
        this.add(new JLabel(DEFAULT_TIMEOUT_STRING), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(timeoutField, gbc);
        
        expirationField = new JTextField(TEXTFIELD_SIZE);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10,0,0,0);
        this.add(new JLabel(REPORT_EXPIRATION_STRING), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(expirationField, gbc);
        
        queryBeforeRunCombo = new JComboBox(new Object[] {NEVER_ASK, ASK_ON_ERRORS, ASK_ON_ERRORS_OR_WARNINGS});
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10,0,0,0);
        this.add(new JLabel(QUERY_USER_BEFORE_RUN), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(queryBeforeRunCombo, gbc);
        
		// Add buttons panel
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(10, 0, 0, 0);
        this.add(createButtonPanel(), gbc);

	 	setFields();
    }

	/**
	 * Create the panel to contain the buttons
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	private JPanel createButtonPanel() {
		final JPanel panel = new JPanel();

		/**
		 * The helpButton shows help about the current component
		 */
		JButton helpButton = new JButton(new AbstractAction(HELP) {
			public void actionPerformed(ActionEvent arg0) {
				Helper.showHelp(panel);
			}
		});
		panel.add(helpButton);

		/**
		 * The resetButton changes the property values shown to those
		 * corresponding to the configuration currently applied.
		 */
		JButton resetButton = new JButton(new AbstractAction(RESET) {
			public void actionPerformed(ActionEvent arg0) {
				setFields();
			}
		});
		panel.add(resetButton);

		/**
		 * The applyButton applies the shown field values to the
		 * {@link HttpProxyConfiguration} and saves them for future.
		 */
		JButton applyButton = new JButton(new AbstractAction(APPLY) {
			public void actionPerformed(ActionEvent arg0) {
				applySettings();
				setFields();
			}
		});
		panel.add(applyButton);

		return panel;
	}
	
	/**
	 * Set the shown field values to those currently in use 
	 * (i.e. last saved configuration).
	 */
	private void setFields() {
		timeoutField.setText(Integer.toString(Integer.parseInt(configuration.getProperty(ReportManagerConfiguration.TIMEOUT))));
		expirationField.setText(Integer.toString(Integer.parseInt(configuration.getProperty(ReportManagerConfiguration.REPORT_EXPIRATION))));
		
		String openSetting = configuration.getProperty(ReportManagerConfiguration.ON_OPEN);
		if (openSetting.equals(ReportManagerConfiguration.NO_CHECK)) {
			openCombo.setSelectedIndex(0);
		} else if (openSetting.equals(ReportManagerConfiguration.QUICK_CHECK)) {
			openCombo.setSelectedIndex(1);
		} else {
			openCombo.setSelectedIndex(2);
		}
		
		String editSetting = configuration.getProperty(ReportManagerConfiguration.ON_EDIT);
		if (editSetting.equals(ReportManagerConfiguration.NO_CHECK)) {
			editCombo.setSelectedIndex(0);
		} else if (editSetting.equals(ReportManagerConfiguration.QUICK_CHECK)) {
			editCombo.setSelectedIndex(1);
		} else {
			editCombo.setSelectedIndex(2);
		}
		
		String runSetting = configuration.getProperty(ReportManagerConfiguration.BEFORE_RUN);
		if (runSetting.equals(ReportManagerConfiguration.QUICK_CHECK)) {
			runCombo.setSelectedIndex(0);
		} else {
			runCombo.setSelectedIndex(1);
		}
		
		String queryBeforeRunSetting = configuration.getProperty(ReportManagerConfiguration.QUERY_BEFORE_RUN);
		if (queryBeforeRunSetting.equals(ReportManagerConfiguration.NONE)) {
			queryBeforeRunCombo.setSelectedIndex(0);
		} else if (queryBeforeRunSetting.equals(ReportManagerConfiguration.ERRORS)) {
			queryBeforeRunCombo.setSelectedIndex(1);
		} else {
			queryBeforeRunCombo.setSelectedIndex(2);
		}
	}
	
	/**
	 * Save the currently set field values (if valid) to the
	 * configuration. Also applies those values to the
	 * currently running Taverna.
	 */
	private void applySettings() {
		if (validateFields()) {
			saveSettings();
		}
	}
	
	private boolean validateFields() {
	    return (validateTimeoutField() && validateExpirationField());
	}

	private boolean validateTimeoutField() {
		String timeoutText = timeoutField.getText();
		String errorText = "";
		int newTimeout = -1;
		try {
			newTimeout = Integer.parseInt(timeoutText);
			if (newTimeout <= 0) {
				errorText += "The timeout must be greater than zero";
			}
		} catch (NumberFormatException e) {
			errorText += "The timeout must be an integer value";
		}
		if (errorText.length() > 0) {
			JOptionPane.showMessageDialog(this, errorText, "", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private boolean validateExpirationField() {
		String expirationText = expirationField.getText();
		String errorText = "";
		int newExpiration = -1;
		try {
			newExpiration = Integer.parseInt(expirationText);
			if (newExpiration < 0) {
				errorText += "The expiration delay must be zero or greater";
			}
		} catch (NumberFormatException e) {
			errorText += "The expiration delay must be an integer value";
		}
		if (errorText.length() > 0) {
			JOptionPane.showMessageDialog(this, errorText, "", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * saveSettings saves the specified values for future use.
	 */
	private void saveSettings() {
		configuration.setProperty(ReportManagerConfiguration.TIMEOUT, Integer
				.toString(Integer.parseInt(timeoutField.getText())));

		configuration.setProperty(ReportManagerConfiguration.REPORT_EXPIRATION, Integer
				.toString(Integer.parseInt(expirationField.getText())));

		int openSetting = openCombo.getSelectedIndex();
		if (openSetting == 0) {
			configuration.setProperty(ReportManagerConfiguration.ON_OPEN,
					ReportManagerConfiguration.NO_CHECK);
		} else if (openSetting == 1) {
			configuration.setProperty(ReportManagerConfiguration.ON_OPEN,
					ReportManagerConfiguration.QUICK_CHECK);
		} else {
			configuration.setProperty(ReportManagerConfiguration.ON_OPEN,
					ReportManagerConfiguration.FULL_CHECK);
		}

		int editSetting = editCombo.getSelectedIndex();
		if (editSetting == 0) {
			configuration.setProperty(ReportManagerConfiguration.ON_EDIT,
					ReportManagerConfiguration.NO_CHECK);
		} else {
			configuration.setProperty(ReportManagerConfiguration.ON_EDIT,
					ReportManagerConfiguration.QUICK_CHECK);
		}

		int runSetting = runCombo.getSelectedIndex();
		if (runSetting == 0) {
			configuration.setProperty(ReportManagerConfiguration.BEFORE_RUN,
					ReportManagerConfiguration.QUICK_CHECK);
		} else {
			configuration.setProperty(ReportManagerConfiguration.BEFORE_RUN,
					ReportManagerConfiguration.FULL_CHECK);
		}
		
		int queryBeforeRunSetting = queryBeforeRunCombo.getSelectedIndex();
		if (queryBeforeRunSetting == 0) {
			configuration.setProperty(ReportManagerConfiguration.QUERY_BEFORE_RUN,  ReportManagerConfiguration.NONE);
		} else if (queryBeforeRunSetting == 1) {
			configuration.setProperty(ReportManagerConfiguration.QUERY_BEFORE_RUN, ReportManagerConfiguration.ERRORS);
		} else {
			configuration.setProperty(ReportManagerConfiguration.QUERY_BEFORE_RUN, ReportManagerConfiguration.ERRORS_OR_WARNINGS);		
		}
	}

}
