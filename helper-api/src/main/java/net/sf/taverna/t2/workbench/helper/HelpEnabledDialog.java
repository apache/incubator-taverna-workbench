/**
 * 
 */
package net.sf.taverna.t2.workbench.helper;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.ui.DialogTextArea;

/**
 * 
 * This class extends JDialog to register the dialog and also attach a key
 * catcher so that F1 is interpreted as help
 * 
 * @author alanrw
 * 
 */
public class HelpEnabledDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5068807887477419800L;

	/**
	 * The settings used by the static dialogs.
	 */
	private static int areaWidth = 40;
	private static int fieldWidth = 40;

	private static Logger logger = Logger.getLogger(HelpEnabledDialog.class);

	/**
	 * The icons used by the static dialogs
	 */
	private static Icon informationIcon = (Icon) UIManager
			.get("OptionPane.informationIcon");
	private static Icon errorIcon = (Icon) UIManager
			.get("OptionPane.errorIcon");
	private static Icon questionIcon = (Icon) UIManager
			.get("OptionPane.questionIcon");
	private static Icon warningIcon = (Icon) UIManager
			.get("OptionPane.warningIcon");

	/**
	 * Prevent information-free creation of a HelpEnabledDialog.
	 */
	private HelpEnabledDialog() {

	}

	/**
	 * Create a HelpEnabledDialog, register it (if possible) with the
	 * HelpCollator and attach a keycatcher.
	 * 
	 * @param owner
	 * @param title
	 * @param modal
	 * @param id
	 * @throws HeadlessException
	 */
	public HelpEnabledDialog(Frame owner, String title, boolean modal, String id)
			throws HeadlessException {
		super(owner, title, modal);
		if (id != null) {
			HelpCollator.registerComponent(this, id);
		} else if (owner != null) {
			HelpCollator.registerComponent(this, owner.getClass()
					.getCanonicalName()
					+ "-dialog");
		} else if ((title != null) && !title.equals("")) {
			HelpCollator.registerComponent(this, title);
		}
		Helper.setKeyCatcher(this);
	}

	/**
	 * Create a HelpEnabledDialog, register it (if possible) with the
	 * HelpCollator and attach a keycatcher.
	 * 
	 * @param owner
	 * @param title
	 * @param modal
	 * @param id
	 * @throws HeadlessException
	 */
	public HelpEnabledDialog(Dialog owner, String title, boolean modal,
			String id) throws HeadlessException {
		super(owner, title, modal);
		if (id != null) {
			HelpCollator.registerComponent(this, id);
		} else if (owner != null) {
			HelpCollator.registerComponent(this, owner.getClass()
					.getCanonicalName()
					+ "-dialog");
		}
		Helper.setKeyCatcher(this);
	}

	/**
	 * Create a panel containing a message and the specified icon.
	 * 
	 * @param message
	 * @param icon
	 * @return
	 */
	private static JPanel createMessagePanel(String message, Icon icon) {
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new FlowLayout());
		JLabel iconLabel = new JLabel(icon);
		messagePanel.add(iconLabel);

		JPanel messageAreaPanel = new JPanel();
		messageAreaPanel.setLayout(new BorderLayout());
		int areaRows = (message.length() / areaWidth) + 1;
		int areaColumns = Math.min(areaWidth, message.length());
		DialogTextArea messageArea = new DialogTextArea(message);
		logger.info("areaRows is " + areaRows + " - columns is " + areaColumns);
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		messageArea.setEditable(false);
		messageArea.setBackground(null);
		messageArea.setRows(areaRows);
		messageArea.setColumns(areaColumns);
		messageAreaPanel.add(messageArea, BorderLayout.CENTER);
		messagePanel.add(messageAreaPanel);

		return messagePanel;
	}

	/**
	 * Show a message dialog in a similar manner to JOptionPane. This is not yet
	 * used by the rest of Taverna.
	 * 
	 * @param owner
	 * @param title
	 * @param message
	 * @param id
	 * @param icon
	 */
	private static void showMessage(Frame owner, String title, String message,
			String id, Icon icon) {
		final HelpEnabledDialog dialog = new HelpEnabledDialog(owner, title,
				true, id);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(createMessagePanel(message, icon));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(new JButton(new AbstractAction("OK") {

			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}
		}));
		buttonPanel.add(new JButton(new AbstractAction("Help") {
			public void actionPerformed(ActionEvent arg0) {
				Helper.showHelpWithinContainer(dialog, dialog);
			}
		}));
		mainPanel.add(buttonPanel);

		dialog.add(mainPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setResizable(false);
		dialog.setVisible(true);
	}

	/**
	 * Show an informative dialog in a similar manner to JOptionPane. This is not yet
	 * used by the rest of Taverna.
	 * 
	 * @param owner
	 * @param message
	 * @param id
	 */
	public static void showInformation(Frame owner, String message, String id) {
		showMessage(owner, "Information", message, id, informationIcon);
	}

	/**
	 * Show an error dialog in a similar manner to JOptionPane. This is not yet
	 * used by the rest of Taverna.
	 * 
	 * @param owner
	 * @param message
	 * @param id
	 */
	public static void showError(Frame owner, String message, String id) {
		showMessage(owner, "Error", message, id, errorIcon);
	}

	/**
	 * Show a warning dialog in a similar manner to JOptionPane. This is not yet
	 * used by the rest of Taverna.
	 * 
	 * @param owner
	 * @param message
	 * @param id
	 */
	public static void showWarning(Frame owner, String message, String id) {
		showMessage(owner, "Warning", message, id, warningIcon);
	}

	/**
	 * Show an input dialog in a similar manner to JOptionPane. This is not yet
	 * used by the rest of Taverna.
	 * 
	 * @param owner
	 * @param message
	 * @param initialValue
	 * @param id
	 * @return
	 */
	public static String showQuestion(Frame owner, String message,
			String initialValue, String id) {
		final List<String> result = new ArrayList<String>();
		result.add(initialValue);

		final HelpEnabledDialog dialog = new HelpEnabledDialog(owner,
				"Question", true, id);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(createMessagePanel(message, questionIcon));

		int width;
		if (initialValue != null) {
			width = Math.max(initialValue.length(), fieldWidth);
		} else {
			width = fieldWidth;
		}
		final JTextField textField = new JTextField(initialValue);

		AbstractAction okAction = new AbstractAction("OK") {

			public void actionPerformed(ActionEvent e) {
				result.clear();
				result.add(textField.getText());
				dialog.setVisible(false);
			}
		};
		textField.addActionListener(okAction);
		textField.setColumns(20);
		mainPanel.add(textField);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(new JButton(okAction));

		buttonPanel.add(new JButton(new AbstractAction("Cancel") {

			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}
		}));

		buttonPanel.add(new JButton(new AbstractAction("Help") {
			public void actionPerformed(ActionEvent arg0) {
				Helper.showHelpWithinContainer(dialog, dialog);
			}
		}));

		mainPanel.add(buttonPanel);

		dialog.add(mainPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setResizable(false);
		textField.requestFocusInWindow();
		textField.selectAll();
		dialog.setVisible(true);
		if (owner != null) {
			owner.requestFocus();
		}
		return result.get(0);
	}

}
