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
package org.apache.taverna.raven.plugins.ui;

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
			}, "Update repository progress bar");
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
