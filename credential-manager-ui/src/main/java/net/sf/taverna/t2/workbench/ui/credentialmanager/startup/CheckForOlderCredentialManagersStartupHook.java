package net.sf.taverna.t2.workbench.ui.credentialmanager.startup;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.Security;
import java.security.KeyStore.Entry;
import java.util.Enumeration;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CMUtils;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workbench.StartupSPI;
import net.sf.taverna.t2.workbench.ui.credentialmanager.WarnUserAboutJCEPolicyDialog;

/**
 * 
 * Startup hook to check for previous versions of Keystore/Truststore files in 
 * older Taverna versions installed on the system. It will ask the user if they 
 * wanted to copy the content of the Keystore/Truststore over to the current version.
 * 
 * @author Alex Nenadic
 *
 */
public class CheckForOlderCredentialManagersStartupHook implements StartupSPI{
	private Logger logger = Logger.getLogger(CheckForOlderCredentialManagersStartupHook.class);

	private final String SECURITY_DIRECTORY_NAME = "security";
	
	public static final String DO_NOT_ASK_TO_IMPORT_OLD_CREDENTIAL_MANAGER = "do_not_ask_to_import_old_credential_manager";
	public static File doNotAskToImportOldCredentialManagerFile = new File(CMUtils.getCredentialManagerDefaultDirectory(),DO_NOT_ASK_TO_IMPORT_OLD_CREDENTIAL_MANAGER);

	
	@Override
	public int positionHint() {
		return 20; // run this before initialise SSL hook
	}

	@Override
	public boolean startup() {
		logger.info("Checking for previous versions of Credential Manager in older Taverna installations.");
				
		// Do not pop up a dialog if we are running headlessly.
		// If we have warned the user and they do not want us to remind them again - exit.
		if (GraphicsEnvironment.isHeadless() || doNotAskToImportOldCredentialManagerFile.exists()){
			return true;
		}
		
		// If there are already Keystore or Truststore files present - exit.
		File currentCredentialManagerDirectory = CMUtils.getCredentialManagerDefaultDirectory();
		File currentKeystoreFile = new File(currentCredentialManagerDirectory, CredentialManager.T2KEYSTORE_FILE);
		File currentTruststoreFile = new File(currentCredentialManagerDirectory, CredentialManager.T2TRUSTSTORE_FILE);
		// If the keystore and truststore files are there - the things have already been set up - exit
		if (currentKeystoreFile.exists() && currentTruststoreFile.exists()){
			return true;
		}
		
		// Check if there are previous Taverna versions installed and find the
		// latest one that contains the Credential Manager files. Ask user if
		// they want to copy that previous data.
		final File appHomeDirectory = ApplicationRuntime.getInstance().getApplicationHomeDir();
		File parentDirectory = appHomeDirectory.getParentFile();
	    FileFilter fileFilter = new FileFilter() {
	        public boolean accept(File file) {       	
				return (!file.getName().equals(appHomeDirectory.getName()) // Exclude Taverna home directory for this app
						&& file.isDirectory()
						&& file.getName().toLowerCase().startsWith("taverna-")
						&& !file.getName().toLowerCase().contains("snapshot") // exclude snapshot versions
						&& !file.getName().toLowerCase().contains("cmd") // exclude command line tool versions
						&& !file.getName().toLowerCase().contains("dataviewer")); // exclude dataviewer versions
	        }
	    };
		File[] tavernaDirectories = parentDirectory.listFiles(fileFilter);
		// Find the newest one among previous Taverna installations 
		File previousKeystoreFile = null;
		File previousTruststoreFile = null;
		File previousTavernaDirectory = null;
		String previousTavernaVersion = null;
		for (File tavernaDirectory : tavernaDirectories){
			logger.info("Checking for previous versions of Credential Manager: found taverna directory " + tavernaDirectory);
			previousTavernaVersion = tavernaDirectory.getName().substring(
					tavernaDirectory.getName().indexOf("-") + 1);
			previousTavernaDirectory = tavernaDirectory;

			File keystoreFile = new File(tavernaDirectory,
					SECURITY_DIRECTORY_NAME
					+ System.getProperty("file.separator")
					+ CredentialManager.T2KEYSTORE_FILE); // .ubr
			
			File truststoreFile = null;
			
			// For Taverna 2.2 and older
			if (previousTavernaVersion.contains("2.2")
					|| previousTavernaVersion.contains("2.1")) {
				truststoreFile = new File(tavernaDirectory,
						SECURITY_DIRECTORY_NAME
								+ System.getProperty("file.separator")
								+ CredentialManager.OLD_T2TRUSTSTORE_FILE); // .jks
			} else { // For Taverna 2.3+
				truststoreFile = new File(tavernaDirectory,
						SECURITY_DIRECTORY_NAME
								+ System.getProperty("file.separator")
								+ CredentialManager.T2TRUSTSTORE_FILE); // .ubr
			}
			
			if (keystoreFile.exists()) {
				if (previousKeystoreFile == null) {
					previousKeystoreFile = keystoreFile;
				} else if (previousKeystoreFile.lastModified() < keystoreFile
						.lastModified()) {
					previousKeystoreFile = keystoreFile;
				}
			}
			if (truststoreFile.exists()) {
				if (previousTruststoreFile == null) {
					previousTruststoreFile = truststoreFile;
				} else if (previousTruststoreFile.lastModified() < truststoreFile
						.lastModified()) {
					previousTruststoreFile = truststoreFile;
				}
			}
		}

		// Found previous keystore/truststore files - ask user if they want to copy them
		// but only if we are not in a headless environment
		if (previousTruststoreFile != null && previousKeystoreFile != null){ 
			// Pop up a warning about Java Cryptography Extension (JCE)
			// Unlimited Strength Jurisdiction Policy - this code is currently the
			// first thing to touch security and Credential Manager so 
			// make sure we warn the user early
			WarnUserAboutJCEPolicyDialog.warnUserAboutJCEPolicy();// no need for this any more as OpenJDK 7 includes the strong policy but just as well
			
			CopyOldCredentialManagerDialog copyDialog = new CopyOldCredentialManagerDialog(previousTavernaVersion);
			while(true){ //loop in case user enters wrong password
				copyDialog.setVisible(true);
				String password = copyDialog.getPassword(); 
				if (password != null){ // user wants us to copy their old credentials
					// Create the Cred Manager folder, if it does not exist
					if (!currentCredentialManagerDirectory.exists()){ // should exist by now as we have called warnUserAboutJCEPolicy() previously
						currentCredentialManagerDirectory.mkdir();						
					}
					
					// For Taverna 2.2 and older - load the old BC-type Keystore and store it again
					// but use the master password for each entry (not just to unlock the Keystore itself).
					// Then load the JKS-type Truststore with the default Truststore password
					// then store it as BC-type keystore using the entered (old) password. Then try
					// to instantiate the Credential Manager with the copied files and old password.
					if (previousTavernaVersion.contains("2.2") 
							|| previousTavernaVersion.contains("2.1")){		
						// Make sure we have added BouncyCastle provider
				        Security.addProvider(new BouncyCastleProvider()); 
				        
				        ///////////////////////////////////////////////

					    logger.info("Trying to copy over the old Keystore from "+previousKeystoreFile);
						KeyStore previousKeystore = null;
						KeyStore currentKeystore = null;
						try {
							previousKeystore = KeyStore.getInstance("UBER", "BC"); //.ubr file
						} catch (Exception ex) {
							// The requested keystore type is not available from the provider
							logger.error("Failed to instantiate old Bouncy Castle 'UBER'-type keystore when trying to copy the old Keystore from "
									+ previousKeystoreFile, ex);
							JOptionPane.showMessageDialog(null, "Failed to instantiate the old Keystore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}
						try {
							currentKeystore = KeyStore.getInstance("UBER", "BC"); //.ubr file
						} catch (Exception ex) {
							// The requested keystore type is not available from the provider
							logger.error("Failed to instantiate new Bouncy Castle 'UBER'-type keystore when trying to copy the old Keystore from "
									+ previousKeystoreFile, ex);
							JOptionPane.showMessageDialog(null, "Failed to instantiate the new Keystore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}
						// Read the old Keystore
						FileInputStream fis = null;
						try {
							// Get the file
							fis = new FileInputStream(previousKeystoreFile);
							// Load the old Keystore from the file
							previousKeystore.load(fis, password.toCharArray());
						} catch (IOException ex) {
							logger.error("Failed to load the old Keystore from "
									+ previousKeystoreFile +". Possible reason: incorrect password or corrupted file.", ex);
							JOptionPane.showMessageDialog(null, "Failed to load credentials from the old Keystore. Possible reason: incorrect password.", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							continue;
						} 
						catch(Exception ex2){
							logger.error("Failed to load the old Keystore from "
									+ previousKeystoreFile, ex2);
							JOptionPane.showMessageDialog(null, "Failed to load credentials from the old Keystore.", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}finally {
							if (fis != null) {
								try {
									fis.close();
								} catch (IOException e) {
									// ignore
								}
							}
						}
						// Create a new empty Keystore
						try {
							currentKeystore.load(null, null);
						} catch (Exception ex) {
							logger.error("Failed to create the new Keystore to copy old entries to from " + previousKeystoreFile, ex);
							JOptionPane.showMessageDialog(null, "Failed to load the new Keystore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}
						// Copy all old entries to the new Keystore 
						// but use the master password when storing the entries !!!
						try{
							Enumeration<String> aliases = previousKeystore.aliases();
							while (aliases.hasMoreElements()){
								String alias = aliases.nextElement();
								try {
									//logger.info("Copying over an entry (from the old Keystore) with alias: "+alias);
									Entry entry = previousKeystore.getEntry(alias,
											new KeyStore.PasswordProtection(null)); // previously entries did not have password
									currentKeystore.setEntry(alias, entry,
											new KeyStore.PasswordProtection(
													password.toCharArray())); // use master password for entry password as well
								} catch (Exception ex) {
									logger.error(
											"Failed to copy entry "+alias+" from the old Keystore in "
													+ previousKeystoreFile
													+ " to the new location in "
													+ currentKeystoreFile, ex);
								}
							}
						}
						catch(Exception ex){
							logger.error(
									"Failed to get Keystore entry aliases from the old Keystore in "
											+ previousKeystoreFile
											+ " in order to copy them to the new location in "
											+ currentKeystoreFile, ex);
							JOptionPane.showMessageDialog(null, "Failed to copy the old credentials to the new Keystore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}
						// Save the new Keystore to the new location (in the current Cred Manager directory)
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(currentKeystoreFile);
							currentKeystore.store(fos, password.toCharArray());
							// Also set the flag to use user-set master password
							FileUtils.touch(new File(currentCredentialManagerDirectory, CredentialManager.USER_SET_MASTER_PASSWORD_INDICATOR_FILE_NAME));
						} catch (Exception ex) {
							logger.error("Failed to save the new Keystore when copying from the old location in "+previousKeystoreFile + " to the new location in "+ currentKeystoreFile, ex);
							FileUtils.deleteQuietly(currentKeystoreFile);
							JOptionPane.showMessageDialog(null, "Failed to save the new Keystore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						} finally {
							if (fos != null) {
								try {
									fos.close();
								} catch (IOException e) {
									// ignore
								}
							}
						}
				        
				        ///////////////////////////////////////////////						
					    logger.info("Trying to load and store the old Truststore from " +previousTruststoreFile);
						KeyStore previousTruststore = null;
						KeyStore currentTruststore = null;
						try {
							previousTruststore = KeyStore.getInstance("JKS"); // .jks file
						} catch (Exception ex) {
							// The requested keystore type is not available from the  provider
							logger.error("Failed to instantiate a 'JKS'-type keystore when trying to copy the old Truststore from "
									+ previousTruststoreFile, ex);
							JOptionPane.showMessageDialog(null, "Failed to instantiate the old Truststore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}
						try {
							currentTruststore = KeyStore.getInstance("UBER", "BC"); // BC .ubr file
						} catch (Exception ex) {
							// The requested keystore type is not available from the
							// provider
							logger.error("Failed to instantiate a Bouncy Castle 'UBER'-type keystore when trying to copy the old Truststore from "
									+ previousKeystoreFile, ex);
							JOptionPane.showMessageDialog(null, "Failed to instantiate the new Truststore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}
						// Read the old Truststore
						FileInputStream fis2 = null;
						try {
							// Get the file
							fis2 = new FileInputStream(previousTruststoreFile);
							// Load the old Truststore from the file
							previousTruststore.load(fis2, CredentialManager.OLD_TRUSTSTORE_PASSWORD.toCharArray());
						} catch (Exception ex) {
							logger.error("Failed to load the old Truststore from "
									+ previousTruststoreFile +". Possible reason: incorrect password or corrupted file.", ex);
							JOptionPane.showMessageDialog(null, "Failed to load trusted certificates from the old Truststore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);	
							break;
						} finally {
							if (fis2 != null) {
								try {
									fis2.close();
								} catch (IOException e) {
									// ignore
								}
							}
						}
						// Create a new empty Truststore
						try {
							currentTruststore.load(null, null);
						} catch (Exception ex) {
							logger.error("Failed to create the new Truststore to copy old entries to from " + previousTruststoreFile, ex);
							JOptionPane.showMessageDialog(null, "Failed to create the new empty Truststore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}
						// Copy all old entries to the new Truststore		
						try{
							Enumeration<String> aliases = previousTruststore.aliases();
							while (aliases.hasMoreElements()){
								String alias= aliases.nextElement();
								try{
									Entry entry = previousTruststore.getEntry(alias, null); // use null password
									currentTruststore.setEntry(alias, entry, null);
								}
								catch(Exception ex){
									logger.error("Failed to copy over an entry with alias " +alias+ " from the old Truststore in "
										+ previousTruststoreFile +" to the new location in " + currentTruststoreFile, ex);
								}
							}
						}
						catch(Exception ex2){
							logger.error("Failed to get aliases for entries in the old Truststore in "
									+ previousTruststoreFile +" when copying them to the new location in " + currentTruststoreFile, ex2);
							JOptionPane.showMessageDialog(null, "Failed to copy the old trusted certificates to the new Truststore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}
						// Save the old Truststore to the new location (in the current Cred Manager directory)
						FileOutputStream fos2 = null;
						try {
							fos2 = new FileOutputStream(currentTruststoreFile);
							currentTruststore.store(fos2, password.toCharArray());
						} catch (Exception ex) {
							logger.error("Failed to saved the new Truststore when copying from the old location in "+previousTruststoreFile + " to the new location in "+ currentTruststoreFile, ex);
							FileUtils.deleteQuietly(currentTruststoreFile);
							JOptionPane.showMessageDialog(null, "Failed to save the new Truststore", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						} finally {
							if (fos2 != null) {
								try {
									fos2.close();
								} catch (IOException e) {
									// ignore
								}
							}
						}
						
						// Try to instantiate Credential Manager
						try{
							CredentialManager.getInstance(password);
						}
						catch (CMException cmex) {
							logger
							.error("Failed to instantiate Credential Manager's with the older files copied from "
									+ previousTavernaDirectory
									+ System.getProperty("file.separator") + SECURITY_DIRECTORY_NAME
									+ " to " + currentCredentialManagerDirectory, cmex);
							// Remove the files
							try{
								FileUtils.deleteDirectory(currentCredentialManagerDirectory);
							}
							catch(Exception ex2){
								// Ignore - nothing we can do
							}
							JOptionPane.showMessageDialog(null, "Failed to instantiate new Credential Manager with the old credentials and trusted certificates", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
					// For Taverna 2.3+
					else{
						// Just copy the files to the current Cred Manager folder (as they are all 
						// of the same Bouncy Castle UBER type) and try to instantiate Credential Manager with 
						// the same (old) password, which will hopefully pick up the new keystore files
						try{
					        logger.info("Trying to copy over the old Keystore from " + previousKeystoreFile);
							FileUtils.copyFile(previousKeystoreFile, currentKeystoreFile);
					        logger.info("Trying to copy over the old Truststore from " + previousTruststoreFile);
							FileUtils.copyFile(previousTruststoreFile, currentTruststoreFile);
							
							// Also set the flag to use user-set master password
							FileUtils.touch(new File(currentCredentialManagerDirectory, CredentialManager.USER_SET_MASTER_PASSWORD_INDICATOR_FILE_NAME));
							
							// Try to instantiate Credential Manager
							CredentialManager.getInstance(password);
						}
						catch(IOException ex){
							logger
									.error("Failed to copy Credential Manager's files from "
											+ previousTavernaDirectory
											+ System.getProperty("file.separator") + SECURITY_DIRECTORY_NAME
											+ " to " + currentCredentialManagerDirectory, ex);
							// Remove the files
							try{
								FileUtils.deleteDirectory(currentCredentialManagerDirectory);
							}
							catch(Exception ex2){
								// Ignore - nothing we can do
							}
							JOptionPane.showMessageDialog(null, "Failed to copy the old Keystore and Truststore with credentials and trusted certificates", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						} catch (CMException cmex) {
							logger
							.error("Failed to instantiate Credential Manager's with the older files copied from "
									+ previousTavernaDirectory
									+ System.getProperty("file.separator") + SECURITY_DIRECTORY_NAME
									+ " to " + currentCredentialManagerDirectory, cmex);
							// Remove the files
							try{
								FileUtils.deleteDirectory(currentCredentialManagerDirectory);
							}
							catch(Exception ex2){
								// Ignore - nothing we can do
							}
							JOptionPane.showMessageDialog(null, "Failed to instantiate new Credential Manager with the old credentials and trusted certificates", "Credential Manager copy failed", JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
				}
				else{
					break;
				}
				
				// If we are here - all is good - break from the loop
				break;
			}
		}
		return true;
	}

}
