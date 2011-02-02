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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CMUtil;
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
	
	@Override
	public int positionHint() {
		return 20; // run this before initialise SSL hook
	}

	@Override
	public boolean startup() {
		logger.info("Checking for previous versions of Credential Manager in older Taverna installations.");
				
		// If there are already Keystore or Truststore files present - exit.
		File currentCredentialManagerDirectory = CMUtil.getCredentialManagerDefaultDirectory();
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
			File keystoreFile = null;
			File truststoreFile = null;
			// For Taverna 2.2 and older
			if (previousTavernaVersion.contains("2.2")
					|| previousTavernaVersion.contains("2.1")) {
				keystoreFile = new File(tavernaDirectory,
						SECURITY_DIRECTORY_NAME
								+ System.getProperty("file.separator")
								+ CredentialManager.OLD_T2KEYSTORE_FILE); // .ubr
				truststoreFile = new File(tavernaDirectory,
						SECURITY_DIRECTORY_NAME
								+ System.getProperty("file.separator")
								+ CredentialManager.OLD_T2TRUSTSTORE_FILE); // .jks
			} else { // For Taverna 2.3+
				keystoreFile = new File(tavernaDirectory,
						SECURITY_DIRECTORY_NAME
								+ System.getProperty("file.separator")
								+ CredentialManager.T2KEYSTORE_FILE); // .jceks
				truststoreFile = new File(tavernaDirectory,
						SECURITY_DIRECTORY_NAME
								+ System.getProperty("file.separator")
								+ CredentialManager.T2TRUSTSTORE_FILE); // .jceks
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
			if (!GraphicsEnvironment.isHeadless()){
				// Pop up a warning about Java Cryptography Extension (JCE)
				// Unlimited Strength Jurisdiction Policy - this code is currently the
				// first thing to touch security and Credential Manager so 
				// make sure we warn the user early
				WarnUserAboutJCEPolicyDialog.warnUserAboutJCEPolicy();
				
				CopyOldCredentialManagerDialog copyDialog = new CopyOldCredentialManagerDialog(previousTavernaVersion);
				copyDialog.setVisible(true);
				String password = copyDialog.getPassword(); 
				if (password != null){ // user wants us to copy their old credentials
					// Create the Cred Manager folder, if it does not exist
					if (!currentCredentialManagerDirectory.exists()){
						currentCredentialManagerDirectory.mkdir();						
					}
					// For Taverna 2.2 and older - load the BC-type Keystore with the entered password
					// and the JKS-type Truststore with the default Truststore password
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
							logger.error("Failed to instantiate a Bouncy Castle 'UBER'-type keystore when trying to copy the old Keystore from "
									+ previousKeystoreFile, ex);
							return true;
						}
						try {
							currentKeystore = KeyStore.getInstance("JCEKS"); // .jceks file
						} catch (Exception ex) {
							// The requested keystore type is not available from the provider
							logger.error("Failed to instantiate a 'JCEKS'-type keystore when trying to copy the old Keystore from "
									+ previousKeystoreFile, ex);
							return true;
						}
						// Read the old Keystore
						FileInputStream fis = null;
						try {
							// Get the file
							fis = new FileInputStream(previousKeystoreFile);
							// Load the old Keystore from the file
							previousKeystore.load(fis, password.toCharArray());
						} catch (Exception ex) {
							logger.error("Failed to load the old Keystore from "
									+ previousKeystoreFile +". Possible reason: incorrect password or corrupted file.", ex);
							return true;
						} finally {
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
							return true;
						}
						// Copy all old entries to the new keystore
						try{
							Enumeration<String> aliases = previousKeystore.aliases();
							while (aliases.hasMoreElements()){
								String alias = aliases.nextElement();
								try {
									//logger.info("Copying over an entry (from the old Keystore) with alias: "+alias);
									Entry entry = previousKeystore.getEntry(alias,
											new KeyStore.PasswordProtection(null));
									currentKeystore.setEntry(alias, entry,
											new KeyStore.PasswordProtection(
													password.toCharArray()));
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
							return true;
						}
						// Save the new Keystore to the new location (in the current Cred Manager directory)
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(currentKeystoreFile);
							currentKeystore.store(fos, password.toCharArray());
						} catch (Exception ex) {
							logger.error("Failed to saved the new Keystore when copying from the old location in "+previousKeystoreFile + " to the new location in "+ currentKeystoreFile, ex);
							try{
								FileUtils.deleteDirectory(currentCredentialManagerDirectory);
							}
							catch(Exception ex2){
								// Ignore - nothing we can do
							}
							return true;
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
					    logger.info("Trying to copy over the old Truststore from " +previousTruststoreFile);
						KeyStore previousTruststore = null;
						KeyStore currentTruststore = null;
						try {
							previousTruststore = KeyStore.getInstance("JKS"); // .jks file
						} catch (Exception ex) {
							// The requested keystore type is not available from the  provider
							logger.error("Failed to instantiate a 'JKS'-type keystore when trying to copy the old Truststore from "
									+ previousTruststoreFile, ex);
							return true;
						}
						try {
							currentTruststore = KeyStore.getInstance("JCEKS"); // .jceks file
						} catch (Exception ex) {
							// The requested keystore type is not available from the
							// provider
							logger.error("Failed to instantiate a 'JCEKS'-type keystore when trying to copy the old Truststore from "
									+ previousKeystoreFile, ex);
							return true;
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
							return true;
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
							return true;
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
							return true;
						}
						// Save the old Truststore to the new location (in the current Cred Manager directory)
						FileOutputStream fos2 = null;
						try {
							fos2 = new FileOutputStream(currentTruststoreFile);
							currentTruststore.store(fos2, password.toCharArray());
						} catch (Exception ex) {
							logger.error("Failed to saved the new Truststore when copying from the old location in "+previousTruststoreFile + " to the new location in "+ currentTruststoreFile, ex);
							try{
								FileUtils.deleteDirectory(currentCredentialManagerDirectory);
							}
							catch(Exception ex2){
								// Ignore - nothing we can do
							}
							return true;
						} finally {
							if (fos2 != null) {
								try {
									fos2.close();
								} catch (IOException e) {
									// ignore
								}
							}
						}
						
						// Try to instantiate the Credential Manager
						try{
							CredentialManager.getInstance(password);
						}
						catch (CMException cmex) {
							logger
							.error("Failed to instantiate Credential Manager's with the files copied from "
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
						}
					}
					// For Taverna 2.3+
					else{
						// Just copy the files to the current Cred Manager folder (as they are all 
						// of the same JCEKS type) and try to instantiate Credential Manager with 
						// the same (old) password, which will hopefully pick up the new keystore files
						try{
							FileUtils.copyFile(previousKeystoreFile, currentKeystoreFile);
							FileUtils.copyFile(previousTruststoreFile, currentTruststoreFile);
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
						} catch (CMException cmex) {
							logger
							.error("Failed to instantiate Credential Manager's with the older files copied from "
									+ previousTavernaDirectory
									+ System.getProperty("file.separator") + SECURITY_DIRECTORY_NAME
									+ " to " + currentCredentialManagerDirectory, cmex);
						}
					}
				}	
			}
		}		
		return true;
	}

}
