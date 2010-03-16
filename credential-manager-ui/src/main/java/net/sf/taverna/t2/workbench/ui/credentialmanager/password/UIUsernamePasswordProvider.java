package net.sf.taverna.t2.workbench.ui.credentialmanager.password;

import java.awt.GraphicsEnvironment;
import java.net.URI;

import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.security.credentialmanager.UsernamePasswordProviderSPI;

import org.apache.log4j.Logger;

public class UIUsernamePasswordProvider implements UsernamePasswordProviderSPI {

	private static Logger logger = Logger
			.getLogger(UIUsernamePasswordProvider.class);

	public boolean canProvideCredentialFor(URI serviceURI) {
		return ! GraphicsEnvironment.isHeadless();
	}

	public UsernamePassword getUsernamePassword(URI serviceURI,
			String requestingPrompt) {

		StringBuilder message = new StringBuilder();
		message.append("<html><body>Credential Manager could not find ");
		message.append("username and password for service at:");
		message.append("<br><br><code>");
		message.append(serviceURI);
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

	public int providerPriority() {
		return 100;
	}

}
