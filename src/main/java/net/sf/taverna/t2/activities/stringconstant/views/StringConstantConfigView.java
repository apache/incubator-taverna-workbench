/**
 *
 */
package net.sf.taverna.t2.activities.stringconstant.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.sf.taverna.t2.activities.stringconstant.servicedescriptions.StringConstantTemplateService;
import net.sf.taverna.t2.lang.ui.FileTools;
import net.sf.taverna.t2.lang.ui.LineEnabledTextPanel;
import net.sf.taverna.t2.lang.ui.LinePainter;
import net.sf.taverna.t2.lang.ui.NoWrapEditorKit;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.apache.log4j.Logger;

import uk.org.taverna.commons.services.ServiceRegistry;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.configurations.Configuration;

/**
 * @author alanrw
 * @author David Withers
 */
@SuppressWarnings("serial")
public class StringConstantConfigView extends ActivityConfigurationPanel {

	public static Logger logger = Logger.getLogger(StringConstantConfigView.class);

	/** The text */
	private JEditorPane scriptTextArea;

	private static final Color LINE_COLOR = Color.WHITE;

	private final ServiceRegistry serviceRegistry;

	public StringConstantConfigView(Activity activity, Configuration configuration,
			ServiceRegistry serviceRegistry) {
		super(activity, configuration);
		this.serviceRegistry = serviceRegistry;
		setLayout(new GridBagLayout());
		initialise();
		this.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				StringConstantConfigView.this.whenOpened();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}
		});
	}

	public StringConstantConfigView(Activity activity, ServiceRegistry serviceRegistry) {
		super(activity);
		this.serviceRegistry = serviceRegistry;
		setLayout(new GridBagLayout());
		initialise();
		this.addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				StringConstantConfigView.this.whenOpened();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}
		});
	}

	public void whenOpened() {
		scriptTextArea.requestFocus();
		if (scriptTextArea.getText().equals(StringConstantTemplateService.DEFAULT_VALUE)) {
			scriptTextArea.selectAll();
		}
	}

	protected void initialise() {
		super.initialise();
		// CSH
		// .setHelpIDString(
		// this,
		// "net.sf.taverna.t2.activities.stringconstant.views.StringConstantConfigView");

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font(
						"Lucida Grande", 1, 12)));

		JPanel scriptEditPanel = new JPanel(new BorderLayout());

		scriptTextArea = new JTextPane();
		new LinePainter(scriptTextArea, LINE_COLOR);

		// NOTE: Due to T2-1145 - always set editor kit BEFORE setDocument
		scriptTextArea.setEditorKit(new NoWrapEditorKit());
		scriptTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
		scriptTextArea.setText(getProperty("string"));
		scriptTextArea.setCaretPosition(0);
		scriptTextArea.setPreferredSize(new Dimension(200, 100));

		scriptEditPanel.add(new LineEnabledTextPanel(scriptTextArea), BorderLayout.CENTER);

		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;

		outerConstraint.fill = GridBagConstraints.BOTH;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		add(scriptEditPanel, outerConstraint);

		JButton loadScriptButton = new JButton("Load text");
		loadScriptButton.setToolTipText("Load text from a file");
		loadScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newScript = FileTools.readStringFromFile(StringConstantConfigView.this,
						"Load text", ".txt");
				if (newScript != null) {
					scriptTextArea.setText(newScript);
					scriptTextArea.setCaretPosition(0);
				}
			}
		});

		JButton saveRScriptButton = new JButton("Save text");
		saveRScriptButton.setToolTipText("Save the text to a file");
		saveRScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTools.saveStringToFile(StringConstantConfigView.this, "Save text", ".txt",
						scriptTextArea.getText());
			}
		});

		JButton clearScriptButton = new JButton("Clear text");
		clearScriptButton.setToolTipText("Clear current text from the edit area");
		clearScriptButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				cleaText();
			}

		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(loadScriptButton);
		buttonPanel.add(saveRScriptButton);
		buttonPanel.add(clearScriptButton);

		scriptEditPanel.add(buttonPanel, BorderLayout.SOUTH);
		setPreferredSize(new Dimension(600, 500));
		this.validate();

	}

	/**
	 * Method for clearing the script
	 */
	private void cleaText() {
		if (JOptionPane.showConfirmDialog(this, "Do you really want to clear the text?",
				"Clearing the script", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			scriptTextArea.setText("");
		}
	}

	@Override
	public boolean checkValues() {
		return true;
	}

	@Override
	public boolean isConfigurationChanged() {
		return !scriptTextArea.getText().equals(getProperty("string"));
	}

	@Override
	public void noteConfiguration() {
		setProperty("string", scriptTextArea.getText());
		configureInputPorts(serviceRegistry);
		configureOutputPorts(serviceRegistry);
	}

}
