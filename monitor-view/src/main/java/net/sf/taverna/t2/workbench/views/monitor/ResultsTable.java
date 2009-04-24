/**
 * 
 */
package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

public class ResultsTable extends JTable {

	public ResultsTable(LineageResultsTableModel lineageResultsTableModel) {
		super(lineageResultsTableModel);
	}

	// Implement table header tool tips.
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
			public String getToolTipText(MouseEvent e) {
				// String tip = null;
				// java.awt.Point p = e.getPoint();
				// int index = columnModel.getColumnIndexAtX(p.x);
				// int realIndex = columnModel.getColumn(index).getModelIndex();
				// return columnToolTips[realIndex];
				return "would be a tool tip here";
			}
		};

	}

}