/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.ui.perspectives.results;

import java.awt.Component;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import uk.org.taverna.platform.report.WorkflowReport;
import uk.org.taverna.platform.run.api.InvalidRunIdException;
import uk.org.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
public class WorkflowRunListCellRenderer extends JLabel implements ListCellRenderer<String> {
	private static final long serialVersionUID = -4954451814016664554L;
	private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final RunService runService;

	public WorkflowRunListCellRenderer(RunService runService) {
		this.runService = runService;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends String> list, String value,
			int index, boolean isSelected, boolean cellHasFocus) {
		try {
			WorkflowReport workflowReport = runService.getWorkflowReport(value);
			setText(workflowReport.getSubject().getName() + " "
					+ ISO_8601.format(workflowReport.getCreatedDate()));
		} catch (InvalidRunIdException e) {
			setText(value);
		}
		return this;
	}
}
