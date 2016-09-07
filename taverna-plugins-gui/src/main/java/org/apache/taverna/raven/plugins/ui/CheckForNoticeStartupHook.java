/**
 * 
 */
package org.apache.taverna.raven.plugins.ui;
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

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;

import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;
import org.apache.taverna.workbench.StartupSPI;
import org.apache.taverna.workbench.icons.WorkbenchIcons;

/**
 * 
 * This class looks for a notice on the myGrid website that is later than the
 * one (if any) in the application directory. It then displays the notice. This
 * is intended to allow simple messages to be sent to all users.
 * 
 * @author alanrw
 * 
 */
public class CheckForNoticeStartupHook implements StartupSPI {

	private static Logger logger = Logger
			.getLogger(CheckForNoticeStartupHook.class);

	private static final String LAST_NOTICE_CHECK_FILE_NAME = "last_notice";


	private static File checkForUpdatesDirectory = CheckForUpdatesStartupHook
			.getCheckForUpdatesDirectory();
	private static File lastNoticeCheckFile = new File(checkForUpdatesDirectory,
			LAST_NOTICE_CHECK_FILE_NAME);

	private static String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
	private static SimpleDateFormat format = new SimpleDateFormat(pattern);

	private static Profile profile = ProfileFactory.getInstance().getProfile();
	private static String version = profile.getVersion();

	private static String BASE_URL = "http://www.mygrid.org.uk/taverna/updates";
	private static String SUFFIX = "notice";
	
	private static int TIMEOUT = 5000;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.workbench.StartupSPI#positionHint()
	 */
	public int positionHint() {
		return 95;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.workbench.StartupSPI#startup()
	 */
	public boolean startup() {

		if (GraphicsEnvironment.isHeadless()) {
			return true; // if we are running headlessly just return
		}

		long noticeTime = -1;
		long lastCheckedTime = -1;

		HttpClient client = new HttpClient();
		client.setConnectionTimeout(TIMEOUT);
		client.setTimeout(TIMEOUT);
		PluginManager.setProxy(client);
		String message = null;

		try {
			URI noticeURI = new URI(BASE_URL + "/" + version + "/" + SUFFIX);
			HttpMethod method = new GetMethod(noticeURI.toString());
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				logger.warn("HTTP status " + statusCode + " while getting "
						+ noticeURI);
				return true;
			}
			String noticeTimeString = null;
			Header h = method.getResponseHeader("Last-Modified");
			message = method.getResponseBodyAsString();
			if (h != null) {
				noticeTimeString = h.getValue();
				noticeTime = format.parse(noticeTimeString).getTime();
				logger.info("NoticeTime is " + noticeTime);
			}

		} catch (URISyntaxException e) {
			logger.error("URI problem", e);
			return true;
		} catch (IOException e) {
			logger.info("Could not read notice", e);
		} catch (ParseException e) {
			logger.error("Could not parse last-modified time", e);
		}

		if (lastNoticeCheckFile.exists()) {
			lastCheckedTime = lastNoticeCheckFile.lastModified();
		}

		if ((message != null) && (noticeTime != -1)) {
			if (noticeTime > lastCheckedTime) {
				// Show the notice dialog
				JOptionPane.showMessageDialog(null, message, "Taverna notice",
						JOptionPane.INFORMATION_MESSAGE,
						WorkbenchIcons.tavernaCogs64x64Icon);
				try {
					FileUtils.touch(lastNoticeCheckFile);
				} catch (IOException e) {
					logger.error("Unable to touch file", e);
				}
			}
		}
		return true;
	}

}
