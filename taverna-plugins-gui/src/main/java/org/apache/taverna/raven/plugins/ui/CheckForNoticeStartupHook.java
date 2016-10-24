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

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.download.DownloadException;
import org.apache.taverna.download.DownloadManager;
import org.apache.taverna.workbench.StartupSPI;
import org.apache.taverna.workbench.icons.WorkbenchIcons;

/**
 * 
 * This class looks for a notice on the Taverna website that is later than the
 * one (if any) in the application directory. It then displays the notice. This
 * is intended to allow simple messages to be sent to all users.
 * 
 */
public class CheckForNoticeStartupHook implements StartupSPI {

	private static final String UPDATES = "updates";
	private static final String LAST_NOTICE = "last_notice";
	private static final String NOTICE = "notice";

	private static Logger logger = Logger
			.getLogger(CheckForNoticeStartupHook.class);

	private DownloadManager downloadManager;

	private ApplicationConfiguration applicationConfiguration;
	
	public int positionHint() {
		return 95;
	}

	public boolean startup() {
		if (GraphicsEnvironment.isHeadless()) {
			return true; // if we are running without graphics we won't check for notices
		}

		FileTime previousLastModified = FileTime.fromMillis(0);		
		Path lastNoticeCheckFile = applicationConfiguration.getApplicationHomeDir().resolve(UPDATES).resolve(LAST_NOTICE);
		
		if (Files.exists(lastNoticeCheckFile)) {
			try {
				previousLastModified = Files.getLastModifiedTime(lastNoticeCheckFile);
			} catch (IOException e) {
				logger.error("Could not check " + lastNoticeCheckFile, e);
				return false;
			}
		} else {
			// Prepare the folder so we can download to it later
			Path parent = lastNoticeCheckFile.getParent();
			try {
				Files.createDirectories(parent);
			} catch (IOException e) {
				logger.error("Could not create folders " + parent, e);
				return false;
			}
		}

		
		URI noticeURI;
		try {
			// e.g. https://taverna.incubator.apache.org/updates/workbench/3.1.0.incubating/notice
			noticeURI = new URI(updateSite()).resolve(version() + "/").resolve(NOTICE);
			
		} catch (URISyntaxException e) {
			logger.error("Invalid plugin site URL: " + updateSite(), e);
			return true;
		}

		try {
			downloadManager.download(noticeURI, lastNoticeCheckFile);
		} catch (DownloadException e) {
			logger.error("Could not download from " + noticeURI, e);
			return true;
		}

		// After successful download the file should exist, so if we 
		// get an IOException below we bail out early (e.g. disk error)		
		try {
			FileTime newLastModified = Files.getLastModifiedTime(lastNoticeCheckFile);
			 if (hasMessage(lastNoticeCheckFile) && 
					 isNewer(newLastModified, previousLastModified)) {
				// Our "API" is that the file should always be in UTF8.. 
				String message = new String(Files.readAllBytes(lastNoticeCheckFile), StandardCharsets.UTF_8);
				// Show the notice dialog
				JOptionPane.showMessageDialog(null, message, product(),
							JOptionPane.INFORMATION_MESSAGE,
							WorkbenchIcons.tavernaCogs64x64Icon);
			}
			return true;
		} catch (HeadlessException e) {
			// but we already checked for GraphicsEnvironment.isHeadless above..!
			logger.error("Can't initialize GUI", e);
			return false;
		} catch (IOException e) {
			logger.error("Can't read " + lastNoticeCheckFile, e);
			return false;
		}
	}

	private boolean isNewer(FileTime newLastModified, FileTime previousLastModified) {
		return newLastModified.compareTo(previousLastModified) > 0;
	}

	private boolean hasMessage(Path lastNoticeCheckFile) throws IOException {
		return Files.size(lastNoticeCheckFile) > 0;
	}

	private String updateSite() {
		return applicationConfiguration.getApplicationProfile().getUpdates().getUpdateSite();
	}

	private String version() {
		return applicationConfiguration.getApplicationProfile().getVersion();
	}
	private String product() {
		return applicationConfiguration.getApplicationProfile().getName();
	}

	public void setDownloadManager(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
	}

	public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

}
