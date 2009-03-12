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
/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: PluginRepositoryListener.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/04 14:51:52 $
 *               by   $Author: sowen70 $
 * Created on 7 Dec 2006
 *****************************************************************/
package net.sf.taverna.raven.plugins.ui;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import net.sf.taverna.raven.RavenException;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.DownloadStatus;
import net.sf.taverna.raven.repository.RepositoryListener;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class PluginRepositoryListener implements
		RepositoryListener {
	
	private final JProgressBar bar = new JProgressBar();

	private static Logger logger = Logger
			.getLogger(PluginRepositoryListener.class);

	public PluginRepositoryListener() {
		bar.setMaximum(100);
		bar.setMinimum(0);
		bar.setStringPainted(true);
	}

	public void statusChanged(final Artifact a, ArtifactStatus oldStatus,
			ArtifactStatus newStatus) {

		bar.setString(a.getArtifactId() + "-" + a.getVersion() + " : "
				+ newStatus.toString());

		if (newStatus.equals(ArtifactStatus.JarFetching)) {

			final DownloadStatus dls;
			try {
				dls = PluginManager.getInstance().getRepository()
						.getDownloadStatus(a);
			} catch (RavenException ex) {
				logger.warn("Could not get download status for: " + a, ex);
				return;
			}

			Thread progressThread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							logger.warn("Progress thread interrupted", e);
							return;
						}
						int progress = Math.min(100, (dls.getReadBytes() * 100)
								/ dls.getTotalBytes());						
						setProgress(progress);
						if (dls.isFinished()) {
							return;
						}
					}
				}
			}, "Plugin repository progress bar");
			progressThread.start();
		}
	}
	

	public JProgressBar getProgressBar() {
		return bar;
	}
	
	public void setVisible(final boolean val) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {				
				bar.setVisible(val);
			}			
		});
		
	}
	
	public void setProgress(final int percentage) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {				
				if (percentage > 0) {
					bar.setValue(percentage);
				} else {
					bar.setValue(0);
				}	
			}			
		});
					
	}
}
