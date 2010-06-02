/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.report.view;

import net.sf.taverna.t2.workflowmodel.Dataflow;

import net.sf.taverna.t2.workbench.report.ReportManager;

import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;

public class ValidateSwingWorker extends SwingWorker<Dataflow, String>{

	private Logger logger = Logger.getLogger(ValidateSwingWorker.class);
	private Dataflow dataflow;
	private final boolean full;
	private final boolean remember;

	public ValidateSwingWorker(Dataflow dataflow, boolean full, boolean remember){
		this.dataflow = dataflow;
		this.full = full;
		this.remember = remember;
	}
	
	@Override
	protected Dataflow doInBackground() throws Exception {

		ReportManager.updateReport(dataflow, full, remember);
		return dataflow;
	}

}
