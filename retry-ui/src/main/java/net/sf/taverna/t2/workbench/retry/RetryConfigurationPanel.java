package net.sf.taverna.t2.workbench.retry;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.RetryConfig;

public class RetryConfigurationPanel extends JPanel {

	private final RetryConfig configuration;
	private JTextField maxRetriesField = new JTextField();
	private JTextField initialDelayField = new JTextField();
	private JTextField maximumDelayField = new JTextField();
	private JTextField backoffFactorField = new JTextField();

	public RetryConfigurationPanel(RetryConfig configuration) {
		this.configuration = configuration;
		this.setLayout(new GridLayout(4,2));
		
		populate();
	}

	public void populate() {
		this.removeAll();
		this.add(new JLabel("Maximum number of retries"));
		maxRetriesField.setText(Integer.toString(configuration.getMaxRetries()));
		this.add(maxRetriesField);
		
		this.add(new JLabel("Initial delay in ms"));
		initialDelayField.setText(Integer.toString(configuration.getInitialDelay()));
		this.add(initialDelayField);
		
		this.add(new JLabel("Maximum delay in ms"));
		maximumDelayField.setText(Integer.toString(configuration.getMaxDelay()));
		this.add(maximumDelayField);
		
		this.add(new JLabel("Delay increase factor"));
		backoffFactorField.setText(Float.toString(configuration.getBackoffFactor()));
		this.add(backoffFactorField);
	}

	public boolean validateConfig() {
		String errorText = "";
		int maxRetries = -1;
		int initialDelay = -1;
		int maxDelay = -1;
		float backoffFactor = -1;
		try {
			maxRetries = Integer.parseInt(maxRetriesField.getText());
		}
		catch (NumberFormatException e) {
			errorText += "The maximum number of retries must be an integer.\n";
		}
		try {
			initialDelay = Integer.parseInt(initialDelayField.getText());
			if (initialDelay < 1) {
				errorText += "The initial delay must be a positive integer.\n";
			}
		}
		catch (NumberFormatException e) {
			errorText += "The initial delay must be an integer.\n";
		}
		try {
			maxDelay = Integer.parseInt(maximumDelayField.getText());
			if (maxDelay < 1) {
				errorText += "The maximum delay must be a positive integer.\n";
			}
			else if (maxDelay < initialDelay) {
				errorText += "The maximum delay must be greater than the initial delay.\n";
			}
		}
		catch (NumberFormatException e) {
			errorText += "The maximum delay must be an integer.\n";
		}
		try {
			backoffFactor = Float.parseFloat(backoffFactorField.getText());
			if (backoffFactor < 1.0) {
				errorText += "The backoff factor must be greater than one.\n";
			}
		}
		catch (NumberFormatException e) {
			errorText += "The backoff factor must be a number.\n";
		}
		if (errorText.length() > 0) {
			JOptionPane.showMessageDialog(this, errorText, "", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public RetryConfig getConfiguration() {
		RetryConfig newConfig = new RetryConfig();
		newConfig.setMaxRetries(Integer.parseInt(maxRetriesField.getText()));
		newConfig.setInitialDelay(Integer.parseInt(initialDelayField.getText()));
		newConfig.setMaxDelay(Integer.parseInt(maximumDelayField.getText()));
		newConfig.setBackoffFactor(Float.parseFloat(backoffFactorField.getText()));
		return newConfig;
	}

}
