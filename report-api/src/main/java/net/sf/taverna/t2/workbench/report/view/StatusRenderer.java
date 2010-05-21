/**
 * 
 */
package net.sf.taverna.t2.workbench.report.view;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import net.sf.taverna.t2.visit.VisitReport.Status;

import net.sf.taverna.t2.lang.ui.icons.Icons;

/**
 * @author alanrw
 *
 */
public class StatusRenderer extends DefaultTableCellRenderer {
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component result = null;
		if (value instanceof Status) {
			result = chooseLabel((Status)value);
		} else {
			result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
		return result;
	}

	private static JLabel okLabel = new JLabel(Icons.okIcon);
	private static JLabel warningLabel = new JLabel(Icons.warningIcon);
	private static JLabel severeLabel = new JLabel(Icons.severeIcon);
	
	private static JLabel chooseLabel (Status status) {
		if (status == Status.OK) {
			return okLabel;
		}
		else if (status == Status.WARNING) {
			return warningLabel;
		} else if (status == Status.SEVERE) {
			return severeLabel;
		}
		return null;
	}
}
