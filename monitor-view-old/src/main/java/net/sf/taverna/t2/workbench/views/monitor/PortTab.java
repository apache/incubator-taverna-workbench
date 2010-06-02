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

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.views.results.RenderedResultComponent;
import net.sf.taverna.t2.workbench.views.results.ResultTreeNode;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * Displays the iterations for an input or output port in a {@link JTable}.
 * Listens for row selections and tells the {@link RenderedResultComponent} to
 * render the {@link T2Reference} associated with the selected iteration
 * 
 * @author Ian Dunlop
 * 
 * 
 */
public class PortTab extends JPanel {

	private static Logger logger = Logger.getLogger(PortTab.class);

	private boolean userUnselected = false;

	private String name;

	private Map<String, T2Reference> portMap;

	private LineageResultsTableModel resultsTableModel;

	private List<LineageQueryResultRecord> lineageRecords;

	private JTable resultsTable;
	
	private JScrollPane scrollPane;

	private RenderedResultComponent resultsComponent;

	private InvocationContext context;

	private RowListener rowListener;

	private ReferenceRenderer iterationRenderer;

	/**
	 * Using the supplied {@link Map} of iterations to {@link T2Reference}s for
	 * the port with 'name', populate a table and listen for selections on it
	 * 
	 * @param name
	 * @param iterationMap
	 * @param resultsComponent
	 * @param context
	 */
	public PortTab(String name, Map<String, T2Reference> iterationMap,
			RenderedResultComponent resultsComponent, InvocationContext context) {
		this.name = name;
		this.setContext(context);
		this.setResultsComponent(resultsComponent);
		this.setPortMap(iterationMap);
		initView();
	}

	private void initView() {

		Set<Entry<String, T2Reference>> entrySet = portMap.entrySet();
		List<Map<String, T2Reference>> iterationList = new ArrayList<Map<String, T2Reference>>();
		Map<String, T2Reference> iterationMap = new HashMap<String, T2Reference>();
		
		for (Entry<String, T2Reference> entry2 : entrySet) {
			iterationMap.put(entry2.getKey(), entry2.getValue());
			logger.info("For port map insert is " + entry2.getKey() + " -> " + entry2.getValue());
		}
		List<String> sortedIndexes = sortIteration(iterationMap.keySet());
	
		resultsTableModel = new LineageResultsTableModel();
		resultsTableModel.setLineageRecords(iterationMap);
		TableColumn iterationColumn = new TableColumn();
		iterationColumn.setHeaderValue("Iteration");
		iterationColumn.setModelIndex(0);
			setIterationRenderer(new ReferenceRenderer(
					iterationMap, sortedIndexes));			
		iterationColumn.setCellRenderer(getIterationRenderer());
		TableColumnModel columnModel = new DefaultTableColumnModel();

		columnModel.addColumn(iterationColumn);
		setResultsTable(new ResultsTable(resultsTableModel, columnModel));
		setRowListener(new RowListener());
		getResultsTable().getSelectionModel().addListSelectionListener(
				getRowListener());
		getResultsTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		try {
			BeanUtils.setProperty(getResultsTable(), "fillsViewportHeight",
					true);
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
			// expected - Java 6 only
		}
		getResultsTable().setBorder(
				BorderFactory.createBevelBorder(BevelBorder.RAISED));
		scrollPane = new JScrollPane(getResultsTable());
		scrollPane.setPreferredSize(new Dimension(250,350));
		add(scrollPane);
	}
	
	
	private List<String> sortIteration(Set<String> keySet) {
		List<String> sortedIndexes = new ArrayList<String>(keySet);
		Collections.sort(sortedIndexes, new Comparator<String>(){
			public int compare(String o1, String o2) {
				Iterator<Integer> o1Iter = indexIterator(o1);
				Iterator<Integer> o2Iter = indexIterator(o2);
				while (o1Iter.hasNext() && o2Iter.hasNext()) {
					int compareTo = o1Iter.next().compareTo(o2Iter.next());
					if (compareTo != 0) {
						return compareTo;
					}
				} 
				if (o1Iter.hasNext()) {
					return 1;
				}
				if (o2Iter.hasNext()) {
					return -1;
				}
				return 0;
			}

			private Iterator<Integer> indexIterator(String o1) {
				o1 = o1.replace("[", "");
				o1 = o1.replace("]", "");
				List<Integer> indexes = new ArrayList<Integer>();
				for (String index : o1.split(",", -1)) {
					indexes.add(Integer.valueOf(index.trim()));
				}
				return indexes.iterator();
			}

			});
		return sortedIndexes;
	}

	public void setPortMap(Map<String, T2Reference> portMap) {
		this.portMap = portMap;
		List<String> sortIteration = sortIteration(portMap.keySet());
		if (iterationRenderer == null) {
			iterationRenderer = new ReferenceRenderer(
					portMap, sortIteration);
		}
		this.iterationRenderer.setSortedIndexes(sortIteration);
	}

	public Map<String, T2Reference> getPortMap() {
		return portMap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLineageRecords(List<LineageQueryResultRecord> lineageRecords) {
		this.lineageRecords = lineageRecords;
	}

	public List<LineageQueryResultRecord> getLineageRecords() {
		return lineageRecords;
	}

	public void setResultsComponent(RenderedResultComponent resultsComponent) {
		this.resultsComponent = resultsComponent;
	}

	public RenderedResultComponent getResultsComponent() {
		return resultsComponent;
	}

	public void setContext(InvocationContext context) {
		this.context = context;
	}

	public InvocationContext getContext() {
		return context;
	}

	public void setResultsTable(JTable resultsTable) {
		this.resultsTable = resultsTable;
	}

	public JTable getResultsTable() {
		return resultsTable;
	}

	public void setRowListener(RowListener rowListener) {
		this.rowListener = rowListener;
	}

	public RowListener getRowListener() {
		return rowListener;
	}

	public void setUserUnselected(boolean userUnselected) {
		this.userUnselected = userUnselected;
	}

	public boolean isUserUnselected() {
		return userUnselected;
	}

	public void setIterationRenderer(ReferenceRenderer iterationRenderer) {
		this.iterationRenderer = iterationRenderer;
	}

	public TableCellRenderer getIterationRenderer() {
		return iterationRenderer;
	}

	/**
	 * Listen for selections on the table and get the renderer to show the
	 * result for the selected iteration
	 * 
	 * @author Ian Dunlop
	 * 
	 */
	public class RowListener implements ListSelectionListener {
		private int rowSelectionIndex;
		private int columnSelectionIndex;

		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}

			setRowSelectionIndex(getResultsTable().getSelectionModel()
					.getLeadSelectionIndex());
			setColumnSelectionIndex(getResultsTable().getColumnModel()
					.getSelectionModel().getLeadSelectionIndex());
			if (getRowSelectionIndex() == -1 || getColumnSelectionIndex() == -1) {
				return;
			}
			logger.info(getRowSelectionIndex());
			logger.info(iterationRenderer);
			String selectedIteration = iterationRenderer.getSortedIndexes().get(getRowSelectionIndex());

			Object object = getPortMap().get(selectedIteration);

			ResultTreeNode node = new ResultTreeNode((T2Reference) object,
					getContext());
			try {
				getResultsComponent().setNode(node);
			} catch (Exception e) {
				logger.warn("Could not render intermediate results for "
						+ object + "due to:\n" + e);
				JOptionPane.showMessageDialog(null,
						"Could not render intermediate results for " + object
								+ "due to:\n" + e, "Problem rendering results",
						JOptionPane.ERROR_MESSAGE);
			}

		}

		public void setRowSelectionIndex(int rowSelectionIndex) {
			this.rowSelectionIndex = rowSelectionIndex;
		}

		public int getRowSelectionIndex() {
			return rowSelectionIndex;
		}

		public void setColumnSelectionIndex(int columnSelectionIndex) {
			this.columnSelectionIndex = columnSelectionIndex;
		}

		public int getColumnSelectionIndex() {
			return columnSelectionIndex;
		}
	}
}
