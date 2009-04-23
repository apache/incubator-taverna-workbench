/**
 * 
 */
package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ResultsTableRenderer extends DefaultTableCellRenderer {



	public ResultsTableRenderer() {
	}

	public Component getTableCellRendererComponent(JTable table,
			final Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		JLabel label = new JLabel();
		if (value != null) {
			label.setText(value.toString());			
		} else {
			label.setText("N/A");
		}
		return label;
	}
	
}