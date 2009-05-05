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

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

/**
 * Used for rendering table cells for {@link T2Reference}s. If they are for
 * successful results then the cell is the normal background colour, if they are
 * for errors then red is used. A border is placed around the result to show if
 * the cell is selected or not
 * 
 * @author Ian Dunlop
 * 
 */
public class ReferenceRenderer extends JLabel implements TableCellRenderer {

	private Border unselectedBorder = null;
	private Border selectedBorder = null;
	private List<LineageQueryResultRecord> lineageRecords;
	private InvocationContext context;
	private Map<String, T2Reference> valueList;
	private List<String> sortedIndexes;

	public ReferenceRenderer(Map<String, T2Reference> valueList, List<String> sortedIndexes) {
		this.setSortedIndexes(sortedIndexes);
		this.setValueList(valueList);
		setOpaque(true);
	}

	/**
	 * Return a cell with the iteration inside
	 */
	@SuppressWarnings("unchecked")
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (isSelected) {
			if (selectedBorder == null) {

				selectedBorder = BorderFactory.createMatteBorder(2, 0, 2, 0,
						table.getSelectionBackground());

			}
			setBorder(selectedBorder);
		} else {
			if (unselectedBorder == null) {

				unselectedBorder = BorderFactory.createMatteBorder(2, 0, 2, 0,
						table.getBackground());

			}
			setBorder(unselectedBorder);		
		}

		String index = getSortedIndexes().get(row);
		
		T2Reference referenceValue = (T2Reference) valueList.get(index);

		setText(" " + index);

		if (referenceValue.getReferenceType().equals(
				(T2ReferenceType.ErrorDocument))) {
			setBackground(new Color(0xff0000));
			return this;

		} else if (referenceValue.getReferenceType().equals(
				(T2ReferenceType.ReferenceSet))) {
			setBackground(new Color(0xffffff));
			return this;
		}

		return this;
	}

	public void setValueList(Map<String, T2Reference> valueList2) {
		this.valueList = valueList2;
	}

	public Map<String, T2Reference> getValueList() {
		return valueList;
	}

	public List<String> getSortedIndexes() {
		return sortedIndexes;
	}

	public void setSortedIndexes(List<String> sortedIndexes) {
		this.sortedIndexes = sortedIndexes;
	}

}