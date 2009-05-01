/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/

package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.provenance.reporter.ProvenanceReporter;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

import org.springframework.context.ApplicationContext;

/**
 * Test harness for the {@link ProvenanceResultsPanel}. Creates a new set of
 * pseudo provenance results when a button is pressed and pushes them to the
 * {@link ResultsTable} and its {@link LineageResultsTableModel}.
 * 
 * @author Ian Dunlop
 * 
 */
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

		final InvocationContext context = new InvocationContextImplementation(
				referenceService, null);

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
		frame.setSize(600, 300);
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

	private List<LineageQueryResultRecord> getRecords(InvocationContext context) {
		List<LineageQueryResultRecord> lineageRecords = new ArrayList<LineageQueryResultRecord>();
		for (int i = 0; i < 4; i++) {

			LineageQueryResultRecord record = new LineageQueryResultRecord();
			record.setVname("a" + i);
			record.setIteration("[" + i + "]");
			record.setType("c" + i);
			record.setInput(true);

			String string = UUID.randomUUID().toString();
			T2Reference register = context.getReferenceService().register(
					string, 0, true, context);

			record.setValue(register.toUri().toString());
			lineageRecords.add(record);

		}
		Exception exception = new Exception("broken");
		T2Reference register = context.getReferenceService().register(
				exception, 0, true, context);
		LineageQueryResultRecord recorda = new LineageQueryResultRecord();
		recorda.setVname("e1");
		recorda.setIteration("[0]");
		recorda.setValue(register.toString());
		recorda.setInput(false);
		lineageRecords.add(recorda);
		for (int ij = 5; ij < 8; ij++) {

			LineageQueryResultRecord record2 = new LineageQueryResultRecord();
			record2.setVname("a" + ij);
			record2.setIteration("[" + ij +"]");
			record2.setType("c" + ij);

			String string = UUID.randomUUID().toString();
			T2Reference register2 = context.getReferenceService().register(
					string, 0, true, context);

			record2.setValue(register2.toUri().toString());
			lineageRecords.add(record2);

		}
		
		for (int i = 0; i < 4; i++) {

			LineageQueryResultRecord record = new LineageQueryResultRecord();
			record.setVname("a" + i);
			record.setIteration("[" + i + ", 1]");
			record.setType("c" + i);
			record.setInput(true);

			String string = UUID.randomUUID().toString();
			T2Reference register1 = context.getReferenceService().register(
					string, 0, true, context);

			record.setValue(register1.toUri().toString());
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
