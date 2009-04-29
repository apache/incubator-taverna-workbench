/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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

import java.awt.BorderLayout;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.T2ReferenceImpl;
import net.sf.taverna.t2.workbench.views.results.RenderedResultComponent;
import net.sf.taverna.t2.workbench.views.results.ResultTreeNode;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * Designed to be used in conjunction with the provenance system in Taverna.
 * Displays a table with T2 data references and renders the results when clicked
 * on The results are passed in within a {@link List} of
 * {@link LineageQueryResultRecord}s. Uses a {@link LineageResultsTableModel} as
 * its internal model. This handles the dereferencing of the {@link T2Reference}
 * it receives as a string by turning it into an actual {@link T2Reference}. A
 * {@link ReferenceRenderer} is used to show whether the {@link T2Reference} is
 * an error by colouring the cell red or green
 * 
 * @author Ian Dunlop
 * 
 */
public class ProvenanceResultsPanel extends JPanel implements
		TableModelListener {

	static Logger logger = Logger.getLogger(ProvenanceResultsPanel.class);

	private List<LineageQueryResultRecord> lineageRecords;

	private JTable resultsTable;

	private InvocationContext context;

	private LineageResultsTableModel lineageResultsTableModel;

	private RenderedResultComponent renderedResultsComponent = new RenderedResultComponent();

	public ProvenanceResultsPanel() {
	}

	public ProvenanceResultsPanel(
			List<LineageQueryResultRecord> lineageRecords,
			InvocationContext invocationContext) {
		super();
		this.lineageRecords = lineageRecords;
		this.setContext(invocationContext);
		initView();
	}

	private void initView() {

		removeAll();

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createRaisedBevelBorder());

		// setLineageResultsTableModel(new LineageResultsTableModel());
		resultsTable = new ResultsTable(getLineageResultsTableModel());
		// resultsTable.setPreferredScrollableViewportSize(new Dimension(500,
		// 70));
		resultsTable.getSelectionModel().addListSelectionListener(
				new RowListener());
		resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultsTable.setDefaultRenderer(HashMap.class,
				new ReferenceRenderer(getContext()));
		resultsTable.getModel().addTableModelListener(this);

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(resultsTable.getTableHeader(), BorderLayout.PAGE_START);
		tablePanel.add(resultsTable, BorderLayout.CENTER);

		// Java 6 only - do it by introspection
		// resultsTable.setFillsViewportHeight(true);
		try {
			BeanUtils.setProperty(resultsTable, "fillsViewportHeight", true);
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
			// expected - Java 6 only
		}

		resultsTable.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.RAISED));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(resultsTable), renderedResultsComponent);
		splitPane.setDividerLocation(0.5);
		//		add(new JScrollPane(resultsTable));
//		add(renderedResultsComponent);
		add(splitPane);
		renderedResultsComponent.setBorder(BorderFactory
				.createRaisedBevelBorder());
		// JPanel panel = new JPanel(new FlowLayout());
		// JScrollPane scrollPane = new JScrollPane(resultsTable);
		// panel.add(tablePanel);
		// panel.add(renderedResultsComponent);
		// JScrollPane scrollPane = new JScrollPane();
		// scrollPane.add(panel);
		// add(scrollPane);
		// add(panel);
		revalidate();

	}

	private class RowListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			int rowSelectionIndex = resultsTable.getSelectionModel()
					.getLeadSelectionIndex();
			int columnSelectionIndex = resultsTable.getColumnModel()
					.getSelectionModel().getLeadSelectionIndex();
			if (rowSelectionIndex == -1 || columnSelectionIndex == -1) {
				return;
			}
			String valueAt = lineageRecords.get(rowSelectionIndex).getValue();
			logger.info("trying to construct: " + valueAt);

			T2Reference referenceFromString = getContext()
					.getReferenceService().referenceFromString(valueAt);
			ResultTreeNode node = new ResultTreeNode(referenceFromString,
					getContext());
			try {
				renderedResultsComponent.setNode(node);
			} catch (Exception e) {
				logger.warn("Could not render intermediate results for "
						+ referenceFromString + "due to:\n" + e);
				JOptionPane.showMessageDialog(null,
						"Could not render intermediate results for "
								+ referenceFromString + "due to:\n" + e,
						"Problem rendering results", JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	public void setLineageRecords(List<LineageQueryResultRecord> lineageRecords) {
		this.lineageRecords = lineageRecords;
		// FIXME this bit is a hack, need a smarter way of instantiating this
		// group of prov results objects. this way assumes that context is set -
		// not clever
		if (lineageResultsTableModel == null) {
			lineageResultsTableModel = new LineageResultsTableModel(
					lineageRecords, context);
			initView();
		} else {
			lineageResultsTableModel.setLineageRecords(lineageRecords);
		}

	}

	public List<LineageQueryResultRecord> getLineageRecords() {
		return lineageRecords;
	}

	public void setContext(InvocationContext context) {
		this.context = context;
	}

	public InvocationContext getContext() {
		return context;
	}

	public void setLineageResultsTableModel(
			LineageResultsTableModel lineageResultsTableModel) {
		this.lineageResultsTableModel = lineageResultsTableModel;
	}

	public LineageResultsTableModel getLineageResultsTableModel() {
		return lineageResultsTableModel;
	}

	public void tableChanged(TableModelEvent e) {
		resultsTable.revalidate();
	}

}
