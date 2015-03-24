/**
 *
 */
package net.sf.taverna.t2.activities.stringconstant.views;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Color.WHITE;
import static java.awt.Font.PLAIN;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.FIRST_LINE_START;
import static java.lang.String.format;
import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION;
import static javax.swing.border.TitledBorder.DEFAULT_POSITION;
import static net.sf.taverna.t2.activities.stringconstant.servicedescriptions.StringConstantTemplateService.DEFAULT_VALUE;
import static net.sf.taverna.t2.lang.ui.FileTools.readStringFromFile;
import static net.sf.taverna.t2.lang.ui.FileTools.saveStringToFile;

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
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.sf.taverna.t2.lang.ui.LineEnabledTextPanel;
import net.sf.taverna.t2.lang.ui.LinePainter;
import net.sf.taverna.t2.lang.ui.NoWrapEditorKit;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import org.apache.log4j.Logger;

import uk.org.taverna.commons.services.ServiceRegistry;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.configurations.Configuration;

/**
 * @author alanrw
 * @author David Withers
 */
@SuppressWarnings("serial")
public class StringConstantConfigView extends ActivityConfigurationPanel {
	private static final String CONTENT_PROPERTY = "string";
	private static final String TEXT_FILE_EXTENSION = ".txt";
	public static Logger logger = Logger.getLogger(StringConstantConfigView.class);
	private static final Color LINE_COLOR = WHITE;
	@SuppressWarnings("unused")
	private static final String HELP_TOKEN = "net.sf.taverna.t2.activities.stringconstant.views.StringConstantConfigView";

	/** The text */
	private JEditorPane scriptTextArea;
	private final ServiceRegistry serviceRegistry;

	public StringConstantConfigView(Activity activity,
			Configuration configuration, ServiceRegistry serviceRegistry) {
		super(activity, configuration);
		this.serviceRegistry = serviceRegistry;
		setLayout(new GridBagLayout());
		initialise();
		addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				whenOpened();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}
		});
	}

	public StringConstantConfigView(Activity activity,
			ServiceRegistry serviceRegistry) {
		super(activity);
		this.serviceRegistry = serviceRegistry;
		setLayout(new GridBagLayout());
		initialise();
		addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				whenOpened();
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
			}
		});
	}

	@Override
	public void whenOpened() {
		scriptTextArea.requestFocus();
		if (scriptTextArea.getText().equals(DEFAULT_VALUE))
			scriptTextArea.selectAll();
	}

	/** The name of the thing we are working with. */
	protected String entityName() {
		return "text";
	}

	@Override
	protected void initialise() {
		super.initialise();
		// CSH.setHelpIDString(this, HELP_TOKEN);

		setBorder(createTitledBorder(null, null, DEFAULT_JUSTIFICATION,
				DEFAULT_POSITION, new Font("Lucida Grande", 1, 12)));

		JPanel scriptEditPanel = new JPanel(new BorderLayout());

		scriptTextArea = new JTextPane();
		new LinePainter(scriptTextArea, LINE_COLOR);

		// NOTE: Due to T2-1145 - always set editor kit BEFORE setDocument
		scriptTextArea.setEditorKit(new NoWrapEditorKit());
		scriptTextArea.setFont(new Font("Monospaced", PLAIN, 14));
		scriptTextArea.setText(getProperty(CONTENT_PROPERTY));
		scriptTextArea.setCaretPosition(0);
		scriptTextArea.setPreferredSize(new Dimension(200, 100));

		scriptEditPanel.add(new LineEnabledTextPanel(scriptTextArea), CENTER);

		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;

		outerConstraint.fill = BOTH;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		add(scriptEditPanel, outerConstraint);

		JButton loadScriptButton = new JButton("Load " + entityName());
		loadScriptButton.setToolTipText(format("Load %s from a file",
				entityName()));
		loadScriptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadText();
			}
		});

		JButton saveRScriptButton = new JButton("Save " + entityName());
		saveRScriptButton.setToolTipText(format("Save the %s to a file",
				entityName()));
		saveRScriptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveText();
			}
		});

		JButton clearScriptButton = new JButton("Clear " + entityName());
		clearScriptButton.setToolTipText(format(
				"Clear current %s from the edit area", entityName()));
		clearScriptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearText();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(loadScriptButton);
		buttonPanel.add(saveRScriptButton);
		buttonPanel.add(clearScriptButton);

		scriptEditPanel.add(buttonPanel, SOUTH);
		setPreferredSize(new Dimension(600, 500));
		this.validate();
	}

	/**
	 * Method for loading the value
	 */
	private void loadText() {
		String newScript = readStringFromFile(this, "Load " + entityName(),
				TEXT_FILE_EXTENSION);
		if (newScript != null) {
			scriptTextArea.setText(newScript);
			scriptTextArea.setCaretPosition(0);
		}
	}

	/**
	 * Method for saving the value
	 */
	private void saveText() {
		saveStringToFile(this, "Save " + entityName(), TEXT_FILE_EXTENSION,
				scriptTextArea.getText());
	}

	/**
	 * Method for clearing the value
	 */
	private void clearText() {
		if (showConfirmDialog(this,
				format("Do you really want to clear the %s?", entityName()),
				"Clearing the " + entityName(), YES_NO_OPTION) == YES_OPTION)
			scriptTextArea.setText("");
	}

	@Override
	public boolean checkValues() {
		return true;
	}

	@Override
	public boolean isConfigurationChanged() {
		return !scriptTextArea.getText().equals(getProperty(CONTENT_PROPERTY));
	}

	@Override
	public void noteConfiguration() {
		setProperty(CONTENT_PROPERTY, scriptTextArea.getText());
		configureInputPorts(serviceRegistry);
		configureOutputPorts(serviceRegistry);
	}
}
