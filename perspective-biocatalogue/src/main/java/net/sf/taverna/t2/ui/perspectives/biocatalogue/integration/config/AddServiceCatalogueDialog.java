package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class AddServiceCatalogueDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JTextField catalogueNameField;

	private JTextField catalogueURLField;

	private String catalogueName;

	private String catalogueURL;

	public AddServiceCatalogueDialog() {
		super((Frame) null, "Add Service Catalogue", true);
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());

		JLabel catalogueNameLabel = new JLabel("Service Catalogue name");
		catalogueNameLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JLabel catalogueURLLabel = new JLabel("Service Catalogue URL");
		catalogueURLLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

		catalogueNameField = new JTextField(15);
		catalogueURLField = new JTextField(15);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				okPressed();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cancelPressed();
			}
		});

		JPanel mainPanel = new JPanel(new BorderLayout());

		JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
		panel.add(catalogueNameLabel);
		panel.add(catalogueNameField);
		panel.add(catalogueURLLabel);
		panel.add(catalogueURLField);
		panel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10,
				10), new EtchedBorder()));

		mainPanel.add(panel, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);

		panel.setMinimumSize(new Dimension(300, 100));

		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

		setResizable(false);

		getRootPane().setDefaultButton(okButton);

		pack();
	}

	private boolean checkControls() {
		catalogueName = catalogueNameField.getText();
		if (catalogueName.length() == 0) {
			JOptionPane.showMessageDialog(this, "Service Catalogue name must not be blank",
					"Service Catalogue Configuration", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		catalogueURL = new String(catalogueURLField.getText());
		if (catalogueURL.length() == 0) { 
			JOptionPane.showMessageDialog(this, "Service Catalogue base URL must not be blank",
					"Warning", JOptionPane.WARNING_MESSAGE);

			return false;
		}

		return BioCataloguePluginConfigurationPanel.checkServiceCatalogueBaseURL(catalogueURL);
	}

	private void okPressed() {
		if (checkControls()) {
			closeDialog();
		}
	}

	private void cancelPressed() {
		// Set all fields to null to indicate that cancel button was pressed
		catalogueName = null;
		catalogueURL = null;
		closeDialog();
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}
    
	public String getCatalogueName()
    {
        return catalogueName;
    }
    
    public String getCatalogueURL()
    {
    	return catalogueURL;
    }
    
	public void setCatalogueName(String username) {
		this.catalogueName = username;
		catalogueNameField.setText(username);
	}

	public void setCatalogueURL(String password) {
		this.catalogueURL = password;
		catalogueURLField.setText(password);
	}
}
