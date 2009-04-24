/**
 * 
 */
package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

public class ReferenceRenderer extends JLabel implements TableCellRenderer {
	
	private Border unselectedBorder = null;
    private Border selectedBorder = null;

	public ReferenceRenderer() {
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		 if (isSelected) {
             if (selectedBorder == null) {
                 selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                           table.getSelectionBackground());
             }
             setBorder(selectedBorder);
         } else {
             if (unselectedBorder == null) {
                 unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                           table.getBackground());
             }
             setBorder(unselectedBorder);
         }
		 
		setText(value.toString());
		
		if (((T2Reference)value).getReferenceType().equals((T2ReferenceType.ErrorDocument))) {
			setBackground(new Color(0xff0000));
			return this;
		} else if (((T2Reference)value).getReferenceType().equals((T2ReferenceType.ReferenceSet))) {
			setBackground(new Color(0x33ff00));
			return this;
		}
		
		return this;
	}

}