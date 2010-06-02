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

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Used so that tool tips can be displayed for the cells in the provenance
 * results table
 * 
 * @author Ian Dunlop
 * 
 */
public class ResultsTable extends JTable {

	@Override
	public Object getValueAt(int row, int column) {
		return super.getValueAt(row, column);
	}

	private String[] toolTips = {
			"Name of the input/output port"};
//			"The iteration that produced this value",
//			"The internal data reference. A red colour represents an error. Click on the value to render the result" };

	public ResultsTable() {
		super();
	}
	public ResultsTable(TableModel resultsTableModel) {
		super(resultsTableModel);
	}
	

	public ResultsTable(TableModel resultsTableModel,
			TableColumnModel columnModel) {
		super(resultsTableModel, columnModel);
	}

	// Implement table header tool tips.
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
			public String getToolTipText(MouseEvent e) {
				String tip = null;
				java.awt.Point p = e.getPoint();
				int index = columnModel.getColumnIndexAtX(p.x);
				int realIndex = columnModel.getColumn(index).getModelIndex();
				return toolTips[realIndex];
			}
		};

	}

}