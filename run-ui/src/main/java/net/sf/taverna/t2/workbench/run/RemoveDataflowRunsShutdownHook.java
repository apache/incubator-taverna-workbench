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
package net.sf.taverna.t2.workbench.run;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.Timer;

import net.sf.taverna.t2.workbench.ShutdownSPI;

/**
 * Shutdown hook that checks and waits until all previous workflow runs queued for deletion
 * have actually been deleted from the provenance and Reference Service's databases.
 * 
 * @author Alex Nenadic
 *
 */
public class RemoveDataflowRunsShutdownHook implements ShutdownSPI{

	public int positionHint() {
		// We need to finish before Reference Service is shutdown.
		return 202;
	}

	public boolean shutdown() {
		boolean shutdown = true;
		final LinkedList<DataflowRun> runsToBeDeletedQueue = DataflowRunsComponent.getRunsToBeDeletedQueue();		

		if (!runsToBeDeletedQueue.isEmpty()) {
				final RemoveDataflowRunsShutdownDialog dialog = new RemoveDataflowRunsShutdownDialog();
				dialog.setInitialQueueSize(runsToBeDeletedQueue.size());
				
				Timer timer = new Timer(500, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dialog.setCurrentQueueSize(runsToBeDeletedQueue.size());
						if (runsToBeDeletedQueue.isEmpty()) {
							dialog.setVisible(false);
						}
					}
				});
				timer.start();

				dialog.setVisible(true);
				
				timer.stop();
				shutdown = dialog.confirmShutdown();
				dialog.dispose();

		} 
		return shutdown;
		
	}

}
