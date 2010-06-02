/**
 * 
 */
package net.sf.taverna.t2.workbench.views.monitor;

import java.util.Map;

import javax.swing.table.AbstractTableModel;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

public class LineageResultsTableModel extends AbstractTableModel {

	Logger logger = Logger.getLogger(LineageResultsTableModel.class);

	private Map<String, T2Reference> lineageRecords;

//	private String[] columnNames = { "Port Name", "Iteration"};

	private InvocationContext context;

	public LineageResultsTableModel(
			Map<String, T2Reference> lineageRecords,
			InvocationContext context) {
		this.setLineageRecords(lineageRecords);
		this.setContext(context);
	}

	public LineageResultsTableModel() {
	}

//	public int getColumnCount() {
//		return columnNames.length;
//	}

//	public String getColumnName(int columnIndex) {
//		return columnNames[columnIndex];
//	}

	public int getRowCount() {
		if (getLineageRecords() != null) {
			return getLineageRecords().size();
		} else {
			return 0;
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
//		if (getLineageRecords() != null) {
//			LineageQueryResultRecord lineageQueryResultRecord = getLineageRecords()
//					.get(rowIndex);
////			Map<Integer, LineageQueryResultRecord> map = new HashMap<Integer, LineageQueryResultRecord>();
//			switch (columnIndex) {
//			case 0:
//				return lineageQueryResultRecord.getVname();
//			case 1:
//				return lineageQueryResultRecord.getIteration();
////			case 2:
////				return getContext().getReferenceService()
////						.referenceFromString(
////								lineageQueryResultRecord.getValue());
//				// case 3: return lineageQueryResultRecord.getValue();
//			}
//		}

		return null;
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then the last column would
	 * contain plain text rather than a background colour
	 */
	public Class getColumnClass(int c) {
		Class<? extends Object> class1 = getValueAt(0, c).getClass();
		return class1;
	}

	private String getType(String value) {
		T2Reference referenceFromString = getContext().getReferenceService()
				.referenceFromString(value);
		referenceFromString.getReferenceType();
		return referenceFromString.getReferenceType().toString();
	}

	public void setLineageRecords(Map<String, T2Reference> lineageRecords) {
		this.lineageRecords = lineageRecords;
		fireTableDataChanged();
	}

	public Map<String, T2Reference> getLineageRecords() {
		return lineageRecords;
	}

	public void setContext(InvocationContext context) {
		this.context = context;
	}

	public InvocationContext getContext() {
		return context;
	}

	public int getColumnCount() {
		return 0;
	}

}