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
package net.sf.taverna.t2.workbench.file.impl.actions;

import java.awt.Component;
import java.io.File;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * An action for opening a nested workflow from a file.
 * 
 * @author Alex Nenadic
 * 
 */
public class OpenNestedWorkflowAction extends OpenWorkflowAction{

	private static final long serialVersionUID = -5398423684000142379L;

	private static Logger logger = Logger.getLogger(OpenNestedWorkflowAction.class);

	private FileManager fileManager = FileManager.getInstance();
	
	public OpenNestedWorkflowAction(){
		super();
	}
	
	/**
	 * Opens a nested workflow from a file (should be one file even though
	 * the method takes a list of files - this is because it overrides the 
	 * #{@link net.sf.taverna.t2.workbench.file.impl.actions.OpenWorkflowAction.java#openWorkflows(Component, File[], FileType, OpenCallback)}).
	 */
	@Override
	public void openWorkflows(final Component parentComponent, File[] files,
			FileType fileType, OpenCallback openCallback) {
		
		ErrorLoggingOpenCallbackWrapper callback = new ErrorLoggingOpenCallbackWrapper(
				openCallback);
		for (final File file : files) {

			try {
				callback.aboutToOpenDataflow(file);
				Dataflow dataflow = fileManager.openDataflow(fileType, file);
				callback.openedDataflow(file, dataflow);
			} catch (final RuntimeException ex) {
				logger.warn("Could not open workflow from " + file, ex);
				if (!callback.couldNotOpenDataflow(file, ex)) {
					showErrorMessage(parentComponent, file, ex);
				}
			} catch (final OpenException ex) {
				logger.warn("Could not open workflow from " + file, ex);
				if (!callback.couldNotOpenDataflow(file, ex)) {
					showErrorMessage(parentComponent, file, ex);
				}
				return;
			}
		}
	}
}
