package net.sf.taverna.t2.workbench.retry;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.RetryConfig;

public class RetryConfigurationPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6625235577980124442L;

	private static final double MIN_BACKOFF = 1.0;
	private static final int MIN_DELAY = 1;
	private static final int MIN_RETRIES = 0;

	private final RetryConfig defaultConfig = new RetryConfig();
	
	private final RetryConfig configuration;
	private JTextField maxRetriesField = new JTextField();
	private JTextField initialDelayField = new JTextField();
	private JTextField maximumDelayField = new JTextField();
	private JTextField backoffFactorField = new JTextField();
	private JPanel panel;

	public RetryConfigurationPanel(RetryConfig configuration) {
		this.configuration = configuration;
		panel = new JPanel(new GridLayout(4,2));
		add(panel);
		populate();
	}

	public void populate() {		
		readConfiguration();
		panel.removeAll();
		panel.add(new JLabel("Maximum number of retries"));
		panel.add(maxRetriesField);
		panel.add(new JLabel("Initial delay in ms"));		
		panel.add(initialDelayField);
		panel.add(new JLabel("Maximum delay in ms"));
		panel.add(maximumDelayField);
		panel.add(new JLabel("Delay increase factor"));
		panel.add(backoffFactorField);
	}

	private void readConfiguration() {
		int maxRetries = configuration.getMaxRetries();
		if (maxRetries < MIN_RETRIES) {
			maxRetries = defaultConfig.getMaxRetries();
		}
		
		int initialDelay = configuration.getInitialDelay();
		if (initialDelay < MIN_DELAY) {
			initialDelay = defaultConfig.getInitialDelay();
		}
		
		int maxDelay = configuration.getMaxDelay();
		if (maxDelay < MIN_DELAY) {
			maxDelay = defaultConfig.getMaxDelay();
		}
		if (maxDelay < initialDelay) {
			maxDelay = initialDelay;
		}
		float backoffFactor = configuration.getBackoffFactor();
		if (backoffFactor <= 1.0) {
			backoffFactor = defaultConfig.getBackoffFactor();
		}

		maxRetriesField.setText(Integer.toString(maxRetries));
		initialDelayField.setText(Integer.toString(initialDelay));
		maximumDelayField.setText(Integer.toString(maxDelay));
		backoffFactorField.setText(Float.toString(backoffFactor));
	}

	public boolean validateConfig() {
		String errorText = "";
		int maxRetries = -1;
		int initialDelay = -1;
		int maxDelay = -1;
		float backoffFactor = -1;
		
		try {
			maxRetries = Integer.parseInt(maxRetriesField.getText());
			if (maxRetries < MIN_RETRIES) {
			    errorText += "The number of retries must be non-negative.\n";
			}
		}
		catch (NumberFormatException e) {
			errorText += "The maximum number of retries must be an integer.\n";
		}
		
		try {
			initialDelay = Integer.parseInt(initialDelayField.getText());
			if (initialDelay < MIN_DELAY) {
				errorText += "The initial delay must be a positive integer.\n";
			}
		}
		catch (NumberFormatException e) {
			errorText += "The initial delay must be an integer.\n";
		}
		
		try {
			maxDelay = Integer.parseInt(maximumDelayField.getText());
			if (maxDelay < MIN_DELAY) {
				errorText += "The maximum delay must be a positive integer.\n";
			}
			else if (maxDelay < initialDelay) {
				errorText += "The maximum delay must be greater than the initial delay.\n";
				maxDelay = Math.max(MIN_DELAY, initialDelay);
				maximumDelayField.setText(Integer.toString(maxDelay));
			}
		}
		catch (NumberFormatException e) {
			errorText += "The maximum delay must be an integer.\n";
		}
		
		try {
			backoffFactor = Float.parseFloat(backoffFactorField.getText());
			if (backoffFactor < MIN_BACKOFF) {
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
