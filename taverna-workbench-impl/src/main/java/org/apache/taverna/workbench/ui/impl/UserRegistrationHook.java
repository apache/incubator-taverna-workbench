/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.impl;

import static java.awt.GraphicsEnvironment.isHeadless;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import org.apache.taverna.workbench.StartupSPI;
import uk.org.taverna.configuration.app.ApplicationConfiguration;

public class UserRegistrationHook implements StartupSPI {
	/** Delay between when we ask the user about registration, in milliseconds */
	private static final int TWO_WEEKS = 14 * 24 * 3600 * 1000;
	public static final String REGISTRATION_DIRECTORY_NAME = "registration";
	public static final String REGISTRATION_DATA_FILE_NAME = "registration_data.properties";
	public static final String REMIND_ME_LATER_FILE_NAME = "remind_me_later";
	public static final String DO_NOT_REGISTER_ME_FILE_NAME = "do_not_register_me";

	private ApplicationConfiguration applicationConfiguration;

	@Override
	public int positionHint() {
		return 50;
	}

	@Override
	public boolean startup() {
		File registrationDirectory = getRegistrationDirectory();
		File registrationDataFile = new File(registrationDirectory,
				REGISTRATION_DATA_FILE_NAME);
		File doNotRegisterMeFile = new File(registrationDirectory,
				DO_NOT_REGISTER_ME_FILE_NAME);
		File remindMeLaterFile = new File(registrationDirectory,
				REMIND_ME_LATER_FILE_NAME);

		// if we are running headlessly just return
		if (isHeadless())
			return true;
		// For Taverna snapshots - do not ask user to register
		if (applicationConfiguration.getName().toLowerCase().contains("snapshot"))
			return true;

		// If there is already user's registration data present - exit.
		if (registrationDataFile.exists())
			return true;

		// If user did not want to register - exit.
		if (doNotRegisterMeFile.exists())
			return true;

		/*
		 * If user said to remind them - check if more than 2 weeks passed since
		 * we asked previously.
		 */
		if (remindMeLaterFile.exists()) {
			long lastModified = remindMeLaterFile.lastModified();
			long now = new Date().getTime();
			if (now - lastModified < TWO_WEEKS)
				// 2 weeks have not passed since we last asked
				return true;

			// Ask user again if they want to register
			UserRegistrationForm form = new UserRegistrationForm(
					applicationConfiguration.getName(), registrationDataFile,
					doNotRegisterMeFile, remindMeLaterFile);
			form.setVisible(true);
			return true;
		}

		/*
		 * Check if there are previous Taverna versions installed and find the
		 * latest one that contains user registration data, if any. Ask user if
		 * they want to upload that previous data.
		 */
		final File appHomeDirectory = applicationConfiguration.getApplicationHomeDir();
		File parentDirectory = appHomeDirectory.getParentFile();
		FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return !(file.getName().equals(appHomeDirectory.getName())
						// Exclude Taverna home directory for this app
						&& file.isDirectory()
						&& file.getName().toLowerCase().startsWith("taverna-")
						// exclude snapshots
						&& !file.getName().toLowerCase().contains("snapshot")
						// exclude command line tool
						&& !file.getName().toLowerCase().contains("cmd")
						// exclude dataviewer
						&& !file.getName().toLowerCase().contains("dataviewer"));
			}
		};
		File[] tavernaDirectories = parentDirectory.listFiles(fileFilter);
		// Find the latest previous registration data file, if any
		File previousRegistrationDataFile = null;
		for (File tavernaDirectory : tavernaDirectories) {
			File regFile = new File(tavernaDirectory, REGISTRATION_DIRECTORY_NAME
					+ System.getProperty("file.separator") + REGISTRATION_DATA_FILE_NAME);
			if (!regFile.exists())
				continue;
			if (previousRegistrationDataFile == null)
				previousRegistrationDataFile = regFile;
			else if (previousRegistrationDataFile.lastModified() < regFile
					.lastModified())
				previousRegistrationDataFile = regFile;
		}

		UserRegistrationForm form;
		if (previousRegistrationDataFile == null)
			// No previous registration file - ask user to register
			form = new UserRegistrationForm(applicationConfiguration.getName(),
					registrationDataFile, doNotRegisterMeFile,
					remindMeLaterFile);
		else
			/*
			 * Fill in user's old registration data in the form and ask them to
			 * register
			 */
			form = new UserRegistrationForm(applicationConfiguration.getName(),
					previousRegistrationDataFile, registrationDataFile,
					doNotRegisterMeFile, remindMeLaterFile);
		form.setVisible(true);
		return true;
	}

	/**
	 * Gets the registration directory where info about registration will be
	 * saved to.
	 */
	public File getRegistrationDirectory() {
		File home = applicationConfiguration.getApplicationHomeDir();

		File registrationDirectory = new File(home, REGISTRATION_DIRECTORY_NAME);
		if (!registrationDirectory.exists())
			registrationDirectory.mkdir();
		return registrationDirectory;
	}

	public void setApplicationConfiguration(
			ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}
}
