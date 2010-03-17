package net.sf.taverna.t2.workbench.ui.credentialmanager.password;

import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;

import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI;
import net.sf.taverna.t2.security.credentialmanager.TrustConfirmation;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

import org.apache.log4j.Logger;

public class UIUsernamePasswordProvider implements CredentialProviderSPI {

	private static Logger logger = Logger
			.getLogger(UIUsernamePasswordProvider.class);

	public boolean canProvideUsernamePassword(URI serviceURI) {
		return ! GraphicsEnvironment.isHeadless();
	}

	public UsernamePassword getUsernamePassword(URI serviceURI,
			String requestingPrompt) {

		URI displayURI = serviceURI;
		
		try {
			displayURI = CredentialManager.setFragmentForURI(displayURI, null);
			displayURI = CredentialManager.setUserInfoForURI(displayURI, null);
		} catch (URISyntaxException e) {
			logger.warn("Could not strip fragment/userinfo from " + serviceURI, e);
		}
		
		StringBuilder message = new StringBuilder();
		message.append("<html><body>Credential Manager could not find ");
		message.append("username and password for service at:");
		message.append("<br><br><code>");
		message.append(displayURI);
		message.append("</code>");
		if (requestingPrompt != null && ! requestingPrompt.equals("")) {
			message.append("<p><i>");
			message.append(requestingPrompt);
			message.append("</i>");
		}
		message.append("<br><br>Please provide username and password.</body></html>");
		
		
		
		GetPasswordDialog getPasswordDialog = new GetPasswordDialog(message.toString(),
				true);
		getPasswordDialog.setLocationRelativeTo(null);
		if (serviceURI.getRawUserInfo() != null && serviceURI.getRawUserInfo().length() > 1) {
			String userInfo = serviceURI.getRawUserInfo();
			String[] userPassword = userInfo.split(":", 2);
			if (userPassword.length == 2) {
				getPasswordDialog.setUsername(userPassword[0]);
				getPasswordDialog.setPassword(userPassword[1]);
			}
		}
		getPasswordDialog.setVisible(true);

		String username = getPasswordDialog.getUsername(); // get username
		String password = getPasswordDialog.getPassword(); // get password
		boolean shouldSaveUsernameAndPassword = getPasswordDialog
				.shouldSaveUsernameAndPassword();
		if (username == null || password == null) {
			/*
			 * user cancelled - any of the above two variables is null
			 */
			return null;
		}

		UsernamePassword username_password = new UsernamePassword();
		username_password.setUsername(username);
		username_password.setPassword(password.toCharArray());
		username_password.setShouldSave(shouldSaveUsernameAndPassword);
		return username_password;

	}

	public int getProviderPriority() {
		return 100;
	}

	public boolean canHandleTrustConfirmation(X509Certificate[] chain) {
		return false;
	}

	public boolean canProvideMasterPassword() {
		return false;
	}

	public boolean canProvideJavaTruststorePassword() {
		return false;
	}

	public String getJavaTruststorePassword() {
		return null;
	}

	public String getMasterPassword(boolean firstTime) {
		return null;
	}

	public TrustConfirmation shouldTrust(X509Certificate[] chain) {
		return null;
	}

}
