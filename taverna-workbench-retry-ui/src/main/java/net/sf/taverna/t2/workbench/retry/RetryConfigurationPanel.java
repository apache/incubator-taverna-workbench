package net.sf.taverna.t2.workbench.retry;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import uk.org.taverna.scufl2.api.configurations.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("serial")
public class RetryConfigurationPanel extends JPanel {

	public static final double DEFAULT_BACKOFF = 1.0;
	public static final int DEFAULT_INITIAL_DELAY = 1000;
	public static final int DEFAULT_MAX_DELAY = 5000;
	public static final int DEFAULT_RETRIES = 0;

	private static final double MIN_BACKOFF = 1.0;
	private static final int MIN_DELAY = 1;
	private static final int MIN_RETRIES = 0;

	private final ObjectNode json;

	private JTextField maxRetriesField = new JTextField();
	private JTextField initialDelayField = new JTextField();
	private JTextField maximumDelayField = new JTextField();
	private JTextField backoffFactorField = new JTextField();

	public RetryConfigurationPanel(Configuration configuration) {
		if (configuration.getJson().has("retry")) {
			json = (ObjectNode) configuration.getJson().get("retry").deepCopy();
		} else {
			json = configuration.getJsonAsObjectNode().objectNode();
		}
		this.setLayout(new GridLayout(4,2));
		this.setBorder(new EmptyBorder(10,10,10,10));
		populate();
	}

	public void populate() {
		readConfiguration();
		this.removeAll();

		JLabel maxRetriesLabel = new JLabel("Maximum number of retries");
		maxRetriesLabel.setBorder(new EmptyBorder(0,0,0,10)); // give some right border to this label
		this.add(maxRetriesLabel);
		this.add(maxRetriesField);

		this.add(new JLabel("Initial delay in ms"));
		this.add(initialDelayField);

		this.add(new JLabel("Maximum delay in ms"));
		this.add(maximumDelayField);

		this.add(new JLabel("Delay increase factor"));
		this.add(backoffFactorField);
	}

	private void readConfiguration() {
		int maxRetries = DEFAULT_RETRIES;
		int initialDelay = DEFAULT_INITIAL_DELAY;
		int maxDelay = DEFAULT_MAX_DELAY;
		double backoffFactor = DEFAULT_BACKOFF;

		if (json.has("maxRetries")) {
			maxRetries = json.get("maxRetries").asInt();
		}
		if (json.has("initialDelay")) {
			initialDelay = json.get("initialDelay").asInt();
		}
		if (json.has("maxDelay")) {
			maxDelay = json.get("maxDelay").asInt();
		}
		if (json.has("backoffFactor")) {
			backoffFactor = json.get("backoffFactor").asDouble();;
		}

		if (maxRetries < MIN_RETRIES) {
			maxRetries = DEFAULT_RETRIES;
		}
		if (initialDelay < MIN_DELAY) {
			initialDelay = DEFAULT_INITIAL_DELAY;
		}
		if (maxDelay < MIN_DELAY) {
			maxDelay = DEFAULT_MAX_DELAY;
		}
		if (maxDelay < initialDelay) {
			maxDelay = initialDelay;
		}
		if (backoffFactor < MIN_BACKOFF) {
			backoffFactor = DEFAULT_BACKOFF;
		}

		maxRetriesField.setText(Integer.toString(maxRetries));
		initialDelayField.setText(Integer.toString(initialDelay));
		maximumDelayField.setText(Integer.toString(maxDelay));
		backoffFactorField.setText(Double.toString(backoffFactor));
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

	public JsonNode getJson() {
		json.put("backoffFactor", backoffFactorField.getText());
		json.put("initialDelay", initialDelayField.getText());
		json.put("maxDelay", maximumDelayField.getText());
		json.put("maxRetries", maxRetriesField.getText());
		return json;
	}

}
