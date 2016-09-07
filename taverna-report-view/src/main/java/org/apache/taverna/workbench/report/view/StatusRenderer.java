/**
 * 
 */
package org.apache.taverna.workbench.report.view;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.taverna.lang.ui.icons.Icons;
import org.apache.taverna.visit.VisitReport.Status;

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
