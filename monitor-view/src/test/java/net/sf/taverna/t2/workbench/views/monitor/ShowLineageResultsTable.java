package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.springframework.context.ApplicationContext;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.provenance.reporter.ProvenanceReporter;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

public class ShowLineageResultsTable {

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private void createAndShowGUI() {
		// Create and set up the window.

		ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceContext.xml");
		final ReferenceService referenceService = (ReferenceService) appContext
				.getBean("t2reference.service.referenceService");

		final InvocationContext context = new InvocationContextImplementation(referenceService, null);

		JFrame frame = new JFrame("TableToolTipsDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		List<LineageQueryResultRecord> records = getRecords(context);

		final ProvenanceResultsPanel provenancePane = new ProvenanceResultsPanel();
		provenancePane.setContext(context);  
		// provenancePane.setOpaque(true); // content panes must be opaque
		JPanel panel = new JPanel(new FlowLayout());
		JButton button = new JButton("click me"); 
		panel.add(button);
		panel.add(provenancePane);
		frame.add(panel);
		button.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				provenancePane.setLineageRecords(getRecords(context));
				provenancePane.revalidate();
			}

		});

		// Create and set up the content pane.

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private List<LineageQueryResultRecord> getRecords(
			InvocationContext context) {
		List<LineageQueryResultRecord> lineageRecords = new ArrayList<LineageQueryResultRecord>();

		for (int i = 0; i < 4; i++) {
			LineageQueryResultRecord record = new LineageQueryResultRecord();
			record.setVname("a" + i);
			record.setIteration("b" + i);
			record.setType("c" + i);

			String string = UUID.randomUUID().toString();
			T2Reference register = context.getReferenceService().register(string, 0, true,
					context);

			record.setValue(register.toUri().toString());
			lineageRecords.add(record);
		}
		return lineageRecords;
	}

	private class InvocationContextImplementation implements InvocationContext {
		private final ReferenceService referenceService;

		private final ProvenanceReporter provenanceReporter;

		private InvocationContextImplementation(
				ReferenceService referenceService,
				ProvenanceReporter provenanceReporter) {
			this.referenceService = referenceService;
			this.provenanceReporter = provenanceReporter;
		}

		public ReferenceService getReferenceService() {
			return referenceService;
		}

		public <T> List<? extends T> getEntities(Class<T> entityType) {
			// TODO Auto-generated method stub
			return null;
		}

		public ProvenanceReporter getProvenanceReporter() {
			return provenanceReporter;
		}
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ShowLineageResultsTable showTable = new ShowLineageResultsTable();

				showTable.createAndShowGUI();
			}
		});
	}

}
