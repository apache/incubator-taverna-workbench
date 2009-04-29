/**
 * 
 */
package net.sf.taverna.t2.workbench.views.monitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.reference.T2Reference;

public class LineageResultsTableModel extends AbstractTableModel {

	Logger logger = Logger.getLogger(LineageResultsTableModel.class);

	private List<LineageQueryResultRecord> lineageRecords;

	private String[] columnNames = { "Port Name", "Iteration"};

	private InvocationContext context;

	public LineageResultsTableModel(
			List<LineageQueryResultRecord> lineageRecords,
			InvocationContext context) {
		this.setLineageRecords(lineageRecords);
		this.setContext(context);
	}

	public LineageResultsTableModel() {
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	public int getRowCount() {
		if (getLineageRecords() != null) {
			return getLineageRecords().size();
		} else {
			return 0;
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (getLineageRecords() != null) {
			LineageQueryResultRecord lineageQueryResultRecord = getLineageRecords()
					.get(rowIndex);
			Map<Integer, LineageQueryResultRecord> map = new HashMap<Integer, LineageQueryResultRecord>();
			switch (columnIndex) {
			case 0:
				map.put(1, lineageQueryResultRecord);
				return map;
			case 1:
				map.put(2, lineageQueryResultRecord);
				return map;
//			case 2:
//				return getContext().getReferenceService()
//						.referenceFromString(
//								lineageQueryResultRecord.getValue());
				// case 3: return lineageQueryResultRecord.getValue();
			}
		}

		return null;
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then the last column would
	 * contain plain text rather than a background colour
	 */
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	private String getType(String value) {
		logger.info("ref for splitting is: " + value);
		T2Reference referenceFromString = getContext().getReferenceService()
				.referenceFromString(value);
		referenceFromString.getReferenceType();
		return referenceFromString.getReferenceType().toString();
	}

	public void setLineageRecords(List<LineageQueryResultRecord> lineageRecords) {
		this.lineageRecords = lineageRecords;
		fireTableDataChanged();
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

}