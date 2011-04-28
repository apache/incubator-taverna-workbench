package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.jdom.Document;
//import org.jdom.Element;
//import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.service_panel.BioCatalogueServiceProvider;


/**
 * 
 * @author Sergejs Aleksejevs
 */
@SuppressWarnings("serial")
public class BioCataloguePluginConfigurationPanel extends JPanel
{
	public static final String APPLICATION_XML_MIME_TYPE = "application/xml";

	public static String PROXY_HOST = "http.proxyHost";
	public static String PROXY_PORT = "http.proxyPort";
	public static String PROXY_USERNAME = "http.proxyUser";
	public static String PROXY_PASSWORD = "http.proxyPassword";
	
	private BioCataloguePluginConfiguration configuration = 
                          BioCataloguePluginConfiguration.getInstance();
  
  
	// UI elements
	JTextField tfBioCatalogueAPIBaseURL;

	private Logger logger = Logger.getLogger(BioCataloguePluginConfigurationPanel.class);
  
  
	public BioCataloguePluginConfigurationPanel() {
		initialiseUI();
		resetFields();
	}
  
  private void initialiseUI()
  {
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.weightx = 1.0;
    
    c.gridx = 0;
    c.gridy = 0;
    JTextArea taDescription = new JTextArea("Configure the BioCatalogue integration functionality");
    taDescription.setFont(taDescription.getFont().deriveFont(Font.PLAIN, 11));
    taDescription.setLineWrap(true);
    taDescription.setWrapStyleWord(true);
    taDescription.setEditable(false);
    taDescription.setFocusable(false);
    taDescription.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    this.add(taDescription, c);
    
    
    c.gridy++;
    c.insets = new Insets(20, 0, 0, 0);
    JLabel jlBioCatalogueAPIBaseURL = new JLabel("Base URL of BioCatalogue instance to connect to:");
    this.add(jlBioCatalogueAPIBaseURL, c);
    
    c.gridy++;
    c.insets = new Insets(0, 0, 0, 0);
    tfBioCatalogueAPIBaseURL = new JTextField();
    this.add(tfBioCatalogueAPIBaseURL, c);
    
    
    c.gridy++;
    c.insets = new Insets(30, 0, 0, 0);
    // We are not removing BioCatalogue services from its config panel any more - 
    // they are being handled by the Taverna's Service Registry
//    JButton bForgetStoredServices = new JButton("Forget services added to Service Panel by BioCatalogue Plugin");
//    bForgetStoredServices.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e)
//      {
//        int response = JOptionPane.showConfirmDialog(null, // no way T2ConfigurationFrame instance can be obtained to be used as a parent...
//                                       "Are you sure you want to clear all SOAP operations and REST methods\n" +
//                                       "that were added to the Service Panel by the BioCatalogue Plugin?\n\n" +
//                                       "This action is permanent is cannot be undone.\n\n" +
//                                       "Do you want to proceed?", "BioCatalogue Plugin", JOptionPane.YES_NO_OPTION);
//        
//        if (response == JOptionPane.YES_OPTION)
//        {
//          BioCatalogueServiceProvider.clearRegisteredServices();
//          JOptionPane.showMessageDialog(null,  // no way T2ConfigurationFrame instance can be obtained to be used as a parent...
//                          "Stored services have been successfully cleared, but will remain\n" +
//                          "being shown in Service Panel during this session.\n\n" +
//                          "They will not appear in the Service Panel after you restart Taverna.",
//                          "BioCatalogue Plugin", JOptionPane.INFORMATION_MESSAGE);
//        }
//      }
//    });
//    this.add(bForgetStoredServices, c);
    
    
    JButton bLoadDefaults = new JButton("Load Defaults");
    bLoadDefaults.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadDefaults();
      }
    });
    
    JButton bReset = new JButton("Reset");
    bReset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetFields();
      }
    });
    
    JButton bApply = new JButton("Apply");
    bApply.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyChanges();
      }
    });
    
    JPanel jpActionButtons = new JPanel();
    jpActionButtons.add(bLoadDefaults);
    jpActionButtons.add(bReset);
    jpActionButtons.add(bApply);
    c.insets = new Insets(30, 0, 0, 0);
    c.gridy++;
    c.weighty = 1.0;
    this.add(jpActionButtons, c);
  }
  
  
  /**
   * Resets all fields to the last saved configuration.
   */
  private void resetFields() {
    tfBioCatalogueAPIBaseURL.setText(configuration.getProperty(BioCataloguePluginConfiguration.BIOCATALOGUE_BASE_URL));
  }
  
  /**
   * Resets all fields to the default values.
   */
  private void loadDefaults() {
    tfBioCatalogueAPIBaseURL.setText(configuration.getDefaultProperty(BioCataloguePluginConfiguration.BIOCATALOGUE_BASE_URL));
  }
  
  /**
   * Saves recent changes to the configuration parameter map.
   * Some input validation is performed as well.
   */
	private void applyChanges() {
		// --- BioCatalogue BASE URL ---

		String candidateBaseURL = tfBioCatalogueAPIBaseURL.getText();
		if (candidateBaseURL.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"BioCatalogue base URL must not be blank",
					"BioCatalogue Plugin", JOptionPane.WARNING_MESSAGE);
			tfBioCatalogueAPIBaseURL.requestFocusInWindow();
			return;
		} else {
			try {
				new URL(candidateBaseURL);
			} catch (MalformedURLException e) {
				JOptionPane
						.showMessageDialog(
								this,
								"Currently set BioCatalogue instance URL is not valid\n." +
								"Please check the URL and try again.",
								"BioCatalogue Plugin",
								JOptionPane.WARNING_MESSAGE);
				tfBioCatalogueAPIBaseURL.selectAll();
				tfBioCatalogueAPIBaseURL.requestFocusInWindow();
				return;
			}

			// check if the base URL has changed from the last saved state
			if (!candidateBaseURL
					.equals(configuration
							.getProperty(BioCataloguePluginConfiguration.BIOCATALOGUE_BASE_URL))) {
					// Perform various checks on the new URL

				// Do a GET with "Accept" header set to "application/xml"
				// We are expecting a 200 OK and an XML doc in return that
				// contains the BioCataogue version number element.
				DefaultHttpClient httpClient = new DefaultHttpClient();
				
				// Set the proxy settings, if any
				if (System.getProperty(PROXY_HOST) != null
						&& !System.getProperty(PROXY_HOST).equals("")) {
					// Instruct HttpClient to use the standard
					// JRE proxy selector to obtain proxy information
					ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
							httpClient.getConnectionManager().getSchemeRegistry(), ProxySelector
									.getDefault());
					httpClient.setRoutePlanner(routePlanner);
					// Do we need to authenticate the user to the proxy?
					if (System.getProperty(PROXY_USERNAME) != null
							&& !System.getProperty(PROXY_USERNAME).equals("")) {
						// Add the proxy username and password to the list of credentials
						httpClient.getCredentialsProvider().setCredentials(
								new AuthScope(System.getProperty(PROXY_HOST),Integer.parseInt(System.getProperty(PROXY_PORT))),
								new UsernamePasswordCredentials(System.getProperty(PROXY_USERNAME), System.getProperty(PROXY_PASSWORD)));
					}
				}
				
				HttpGet httpGet = new HttpGet(candidateBaseURL);
				httpGet.setHeader("Accept", APPLICATION_XML_MIME_TYPE);

				// Execute the request
				HttpContext localContext = new BasicHttpContext();
				HttpResponse httpResponse;
				try {
					httpResponse = httpClient.execute(httpGet, localContext);
				} catch (Exception ex1) {
					logger.error("BioCatalogue preferences configuration: Failed to do "
							+ httpGet.getRequestLine(), ex1);
					// Warn the user
					JOptionPane.showMessageDialog(this,
							"Failed to connect to the URL of the BioCatalogue instance.\n"
									+ "Please check the URL and try again.",
							"BioCatalogue Plugin",
							JOptionPane.INFORMATION_MESSAGE);
					
					// Release resource
					httpClient.getConnectionManager().shutdown();
					
					tfBioCatalogueAPIBaseURL.requestFocusInWindow();
					return;
				}

				if (httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) { // HTTP/1.1 200 OK
					HttpEntity httpEntity = httpResponse.getEntity();
					String contentType = httpEntity.getContentType().getValue()
							.toLowerCase().trim();
					logger
							.info("BioCatalogue preferences configuration: Got 200 OK when testing BioCatalogue instance by doing "
									+ httpResponse.getStatusLine()
									+ ". Content type of response "
									+ contentType);
					if (contentType.startsWith(APPLICATION_XML_MIME_TYPE)) {
						String value = null;
						Document doc = null;
						try {
							value = readResponseBodyAsString(httpEntity)
									.trim();
							// Try to read this string into an XML document
							SAXBuilder builder = new SAXBuilder();
							byte[] bytes = value.getBytes("UTF-8");
							doc = builder.build(new ByteArrayInputStream(bytes));
						} catch (Exception ex2) {
							logger.error("BioCatalogue preferences configuration: Failed to build an XML document from the response.", ex2);
							// Warn the user
							JOptionPane.showMessageDialog(this,
									"Failed to get the expected response body when testing the BioCatalogue instance.\n"
											+ "The URL is probably wrong. Please check it and try again.",
									"BioCatalogue Plugin",
									JOptionPane.INFORMATION_MESSAGE);
							tfBioCatalogueAPIBaseURL.requestFocusInWindow();
							return;
						}
						finally{
							// Release resource
							httpClient.getConnectionManager().shutdown();
						}
						// Get the version element from the XML document
						/*String acceptedVersion = "...";
						Element versionElement = doc.getRootElement().getChild("version", Namespace.getNamespace("http://www.biocatalogue.org/2009/xml/rest"));
						if (versionElement.getText() <some_kind_of_comparison> acceptedVersion){
							// Warn the user
							JOptionPane.showMessageDialog(this,
									"The version of the BioCatalogue instance you are trying to connect to is not supported.\n"
											+ "Please change the URL and try again.",
									"BioCatalogue Plugin",
									JOptionPane.INFORMATION_MESSAGE);
							tfBioCatalogueAPIBaseURL.requestFocusInWindow();
							return;				
						}*/
					} else {
						logger
								.error("BioCatalogue preferences configuration: Failed to get the expected response content type when testing the BioCatalogue instance. "
										+ httpGet.getRequestLine()
										+ " returned content type '"
										+ contentType
										+ "'; expected response content type is 'application/xml'.");
						// Warn the user
						JOptionPane
								.showMessageDialog(
										this,
										"Failed to get the expected response content type when testing the BioCatalogue instance.\n"
										+ "The URL is probably wrong. Please check it and try again.",
										"BioCatalogue Plugin",
										JOptionPane.INFORMATION_MESSAGE);
						tfBioCatalogueAPIBaseURL.requestFocusInWindow();
						return;
					}
				}
				else{
					logger
							.error("BioCatalogue preferences configuration: Failed to get the expected response status code when testing the BioCatalogue instance. "
									+ httpGet.getRequestLine()
									+ " returned the status code "
									+ httpResponse.getStatusLine()
											.getStatusCode() + "; expected status code is 200 OK.");
					// Warn the user
					JOptionPane
							.showMessageDialog(
									this,
									"Failed to get the expected response status code when testing the BioCatalogue instance.\n"
									+ "The URL is probably wrong. Please check it and try again.",
									"BioCatalogue Plugin",
									JOptionPane.INFORMATION_MESSAGE);
					tfBioCatalogueAPIBaseURL.requestFocusInWindow();
					return;					
				}

				// Warn the user of the changes in the BioCatalogue base URL
				JOptionPane
						.showMessageDialog(
								this,
								"You have updated the BioCatalogue base URL.\n\n"
										+ "From now on the new one will be used, however it is advised\n"
										+ "to restart Taverna for this new setting to take the full effect.\n\n"
										+ "If you keep using Taverna, any previously made searches, filtering\n"
										+ "operations and tags in the tag cloud may still use the previous\n"
										+ "setting.", "BioCatalogue Plugin",
								JOptionPane.INFORMATION_MESSAGE);

			}

			// the new base URL seems to be valid - can save it into config
			// settings
			configuration.setProperty(
					BioCataloguePluginConfiguration.BIOCATALOGUE_BASE_URL,
					candidateBaseURL);

			// also update the base URL in the BioCatalogueClient
			MainComponentFactory.getSharedInstance().getBioCatalogueClient()
					.setBaseURL(candidateBaseURL);
		}

	}
  
  
  /**
   * For testing only.
   */
  public static void main(String[] args) {
    JFrame theFrame = new JFrame();
    theFrame.add(new BioCataloguePluginConfigurationPanel());
    theFrame.pack();
    theFrame.setLocationRelativeTo(null);
    theFrame.setVisible(true);
  }

	/**
	 * Worker method that extracts the content of the received HTTP message as a
	 * string. It also makes use of the charset that is specified in the
	 * Content-Type header of the received data to read it appropriately.
	 * 
	 * @param entity
	 * @return
	 * @throws IOException
	 */
	// Taken from HTTPRequestHandler in rest-activity by Sergejs Aleksejevs
	private static String readResponseBodyAsString(HttpEntity entity)
			throws IOException {
		// get charset name
		String charset = null;
		String contentType = entity.getContentType().getValue().toLowerCase();

		String[] contentTypeParts = contentType.split(";");
		for (String contentTypePart : contentTypeParts) {
			contentTypePart = contentTypePart.trim();
			if (contentTypePart.startsWith("charset=")) {
				charset = contentTypePart.substring("charset=".length());
			}
		}

		// read the data line by line
		StringBuilder responseBodyString = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(entity
				.getContent(), charset));

		String str;
		while ((str = reader.readLine()) != null) {
			responseBodyString.append(str + "\n");
		}

		return (responseBodyString.toString());
	}
  
}
