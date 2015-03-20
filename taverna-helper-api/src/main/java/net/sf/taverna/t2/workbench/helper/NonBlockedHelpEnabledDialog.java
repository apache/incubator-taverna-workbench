/**
 * 
 */
package net.sf.taverna.t2.workbench.helper;

import static java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;

/**
 * @author alanrw
 */
public class NonBlockedHelpEnabledDialog extends HelpEnabledDialog {
	private static final long serialVersionUID = -2455471377333940417L;

	public NonBlockedHelpEnabledDialog(Dialog owner, String title,
			boolean modal, String id) throws HeadlessException {
		super(owner, title, modal, id);
		this.setModalExclusionType(APPLICATION_EXCLUDE);
	}

	public NonBlockedHelpEnabledDialog(Frame owner, String title,
			boolean modal, String id) throws HeadlessException {
		super(owner, title, modal, id);
		this.setModalExclusionType(APPLICATION_EXCLUDE);
	}

	public NonBlockedHelpEnabledDialog(Frame parent, String title, boolean modal) {
		super(parent, title, modal, null);
		this.setModalExclusionType(APPLICATION_EXCLUDE);
	}

	public NonBlockedHelpEnabledDialog(Dialog parent, String title,
			boolean modal) {
		super(parent, title, modal, null);
		this.setModalExclusionType(APPLICATION_EXCLUDE);
	}
}
