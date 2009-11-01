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
package net.sf.taverna.t2.workbench.ui.impl;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.workbench.StartupSPI;

public class UserRegistrationHook implements StartupSPI{

	public static final String REGISTRATION_DIRECTORY_NAME = "registration";
	public static final String REGISTRATION_DATA_FILE_NAME = "registration_data.properties";
	public static final String REMIND_ME_FILE_NAME = "remind_me";
	public static final String DO_NOT_REGISTER_ME_FILE_NAME = "do_not_register_me";

	public static String appName = ApplicationConfig.getInstance().getName();
	
	public static File registrationDirectory = getRegistrationDirectory();
	public static File registrationDataFile = new File(registrationDirectory,REGISTRATION_DATA_FILE_NAME);
	public static File doNotRegisterMeFile = new File(registrationDirectory,DO_NOT_REGISTER_ME_FILE_NAME);
	public static File remindMeFile = new File(registrationDirectory,REMIND_ME_FILE_NAME);


	public int positionHint() {
		return 500;
	}

	public boolean startup() {
		
		 // For Taverna snapshots - do not ask user to register
		if (appName.toLowerCase().contains("snapshot")){
			return true;
		}
		
		// If there is already user's registration data present - exit.
		if (registrationDataFile.exists()){
			return true;
		}
		
		// If user did not want to register - exit.
		if (doNotRegisterMeFile.exists()){
			return true;
		}
		
		// If user said to remind them - check if more than 2 weeks passed since we asked previously.
		if (remindMeFile.exists()){
			long lastModified  = remindMeFile.lastModified();
			long now = new Date().getTime();
			if (now - lastModified < 2*14*24*3600*1000){ // 2 weeks have not passed since we last asked
				return true;
			}
			else{ // Ask user again if they want to register 
				UserRegistrationForm form = new UserRegistrationForm();
				form.setVisible(true);
				return true;
			}
		}
		
		// Check if there are previous Taverna versions installed and find the
		// latest one that contains user registration data, if any. Ask user if
		// they want to upload that previous data.
		final File appHomeDirectory = ApplicationRuntime.getInstance().getApplicationHomeDir();
		File parentDirectory = appHomeDirectory.getParentFile();
	    FileFilter fileFilter = new FileFilter() {
	        public boolean accept(File file) {
	        	
				return (!file.getName().equals(appHomeDirectory.getName())) // Exclude Taverna home directory for this app
						&& file.isDirectory()
						&& file.getName().toLowerCase().startsWith("taverna")
						&& (!file.getName().toLowerCase().contains("snapshot"));
	        }
	    };
		File[] tavernaDirectories = parentDirectory.listFiles(fileFilter);
		// Find the latest previous registration data file, if any
		File previousRegistrationDataFile = null;
		for (File tavernaDirectory : tavernaDirectories){
			File regFile = new File (tavernaDirectory, REGISTRATION_DIRECTORY_NAME + System.getProperty("file.separator") + REGISTRATION_DATA_FILE_NAME);
			if (regFile.exists()){
				if (previousRegistrationDataFile == null){
					previousRegistrationDataFile = regFile;
				}
				else if (previousRegistrationDataFile.lastModified() < regFile.lastModified()){
					previousRegistrationDataFile = regFile;
				}
			}
		}

		if (previousRegistrationDataFile == null){ // No previous registration file - ask user to register
			UserRegistrationForm form = new UserRegistrationForm();
			form.setVisible(true);
			return true;
		}
		else{ // Fill in user's old registration data in the form and ask them to register
			UserRegistrationForm form = new UserRegistrationForm(previousRegistrationDataFile);
			form.setVisible(true);
			return true;	
		}
	}
	
	/**
	 * Gets the registration directory where info about registration will be saved to.
	 */
	public static File getRegistrationDirectory() {
		
		File home = ApplicationRuntime.getInstance().getApplicationHomeDir();

		File registrationDirectory = new File(home,"registration");
		if (!registrationDirectory.exists()) {
			registrationDirectory.mkdir();
		}
		return registrationDirectory;
	}
	
}
