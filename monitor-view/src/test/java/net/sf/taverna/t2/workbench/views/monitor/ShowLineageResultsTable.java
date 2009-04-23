package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;

public class ShowLineageResultsTable {

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("TableToolTipsDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final List<LineageQueryResultRecord> lineageRecords = new ArrayList<LineageQueryResultRecord>();

		for (int i = 0; i < 4; i++) {
			LineageQueryResultRecord record = new LineageQueryResultRecord();
			record.setVname("a" + i);
			record.setIteration("b" + i);
			record.setType("c" + i);
			record.setValue("d" + i);
			lineageRecords.add(record);
		}
		final ProvenanceResultsPanel provenancePane = new ProvenanceResultsPanel();
		provenancePane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(provenancePane);
		JButton button = new JButton("click me");
		frame.add(button);
		button.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				provenancePane.setLineageRecords(lineageRecords);
			}

		});

		// Create and set up the content pane.


		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
