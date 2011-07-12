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
import net.sf.taverna.t2.workbench.MainWindow;

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

	private static Logger logger = Logger.getLogger(HelpEnabledDialog.class);

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
		super(owner == null ? MainWindow.getMainWindow() : owner, title, modal);

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
	 * Create a HelpEnabledDialog, register it (if possible) with the
	 * HelpCollator and attach a keycatcher.
	 * 
	 * @param owner
	 * @param title
	 * @param modal
	 * @throws HeadlessException
	 */
	public HelpEnabledDialog(Frame parent, String title, boolean modal) {
		this(parent, title, modal, null);
	}

	/**
	 * Create a HelpEnabledDialog, register it (if possible) with the
	 * HelpCollator and attach a keycatcher.
	 * 
	 * @param owner
	 * @param title
	 * @param modal
	 * @throws HeadlessException
	 */
	public HelpEnabledDialog(Dialog parent, String title, boolean modal) {
		this(parent, title, modal, null);
	}
	
    public void setVisible(boolean b) {
	this.setLocationRelativeTo(this.getParent());
	super.setVisible(b);
    }
}
