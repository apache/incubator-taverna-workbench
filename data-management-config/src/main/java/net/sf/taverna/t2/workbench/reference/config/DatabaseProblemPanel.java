package net.sf.taverna.t2.workbench.reference.config;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import net.sf.taverna.t2.workbench.helper.HelpCollator;
import net.sf.taverna.t2.workbench.helper.Helper;

class DatabaseProblemPanel extends JPanel {
	
	final static String messageString = "Taverna is unable to obtain a connection to its database.\n\nThis is normally because you have another Taverna already running.";
	
	private static Object[] options = { "Help", "OK" };
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8792040026805287603L;

	public DatabaseProblemPanel() {
		super(new BorderLayout());
		ReadOnlyTextArea textArea = new ReadOnlyTextArea(messageString);
		this.add(new JScrollPane(textArea), BorderLayout.CENTER);
		this.setPreferredSize(new Dimension(300,300));
	}
	
	void showDialog() {

		int n = JOptionPane.showOptionDialog(null, this,
				"Database problem", JOptionPane.YES_NO_OPTION,
				JOptionPane.ERROR_MESSAGE, null, options, options[1]);
		if (n == JOptionPane.YES_OPTION) {
			Helper.showHelp(this);
		}
	}

}