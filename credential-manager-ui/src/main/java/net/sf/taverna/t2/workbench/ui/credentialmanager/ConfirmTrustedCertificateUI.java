package net.sf.taverna.t2.workbench.ui.credentialmanager;

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.security.cert.X509Certificate;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI;
import net.sf.taverna.t2.security.credentialmanager.TrustConfirmation;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

import org.apache.log4j.Logger;

public class ConfirmTrustedCertificateUI implements CredentialProviderSPI {

	private static Logger logger = Logger
			.getLogger(ConfirmTrustedCertificateUI.class);

	public boolean canHandleTrustConfirmation(X509Certificate[] chain) {
		return !GraphicsEnvironment.isHeadless();
	}

	public int getProviderPriority() {
		return 100;
	}

	public TrustConfirmation shouldTrust(X509Certificate[] chain) {
		TrustConfirmation trustConfirm = new TrustConfirmation();
		logger.info("Asking the user if they want to trust certificate.");
		// Ask user if they want to trust this service
		ConfirmTrustedCertificateDialog confirmCertTrustDialog = new ConfirmTrustedCertificateDialog(
				(Frame) null, "Untrusted HTTPS connection", true,
				(X509Certificate) chain[0]);
		confirmCertTrustDialog.setLocationRelativeTo(null);
		confirmCertTrustDialog.setVisible(true);
		trustConfirm.setShouldTrust(confirmCertTrustDialog.shouldTrust());
		trustConfirm.setShouldSave(confirmCertTrustDialog.shouldSave());
		if (!confirmCertTrustDialog.shouldTrust()) {
			JOptionPane
					.showMessageDialog(
							null,
							"As you refused to trust this host, you will not be able to its services from a workflow.",
							"Untrusted HTTPS connection",
							JOptionPane.INFORMATION_MESSAGE);
		}
		return trustConfirm;
	}

	public boolean canProvideUsernamePassword(URI serviceURI) {
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

	public UsernamePassword getUsernamePassword(URI serviceURI,
			String requestingPrompt) {
		return null;
	}

}
