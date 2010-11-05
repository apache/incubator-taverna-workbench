package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.service_panel.BioCatalogueServiceProvider;


/**
 * 
 * @author Sergejs Aleksejevs
 */
public class BioCataloguePluginConfigurationPanel extends JPanel
{
  private BioCataloguePluginConfiguration configuration = 
                          BioCataloguePluginConfiguration.getInstance();
  
  
  // UI elements
  JTextField tfBioCatalogueAPIBaseURL;
  
  
  public BioCataloguePluginConfigurationPanel()
  {
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
    JButton bForgetStoredServices = new JButton("Forget services added to Service Panel by BioCatalogue Plugin");
    bForgetStoredServices.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        int response = JOptionPane.showConfirmDialog(null, // no way T2ConfigurationFrame instance can be obtained to be used as a parent...
                                       "Are you sure you want to clear all SOAP operations and REST methods\n" +
                                       "that were added to the Service Panel by the BioCatalogue Plugin?\n\n" +
                                       "This action is permanent is cannot be undone.\n\n" +
                                       "Do you want to proceed?", "BioCatalogue Plugin", JOptionPane.YES_NO_OPTION);
        
        if (response == JOptionPane.YES_OPTION)
        {
          BioCatalogueServiceProvider.clearRegisteredServices();
          JOptionPane.showMessageDialog(null,  // no way T2ConfigurationFrame instance can be obtained to be used as a parent...
                          "Stored services have been successfully cleared, but will remain\n" +
                          "being shown in Service Panel during this session.\n\n" +
                          "They will not appear in the Service Panel after you restart Taverna.",
                          "BioCatalogue Plugin", JOptionPane.INFORMATION_MESSAGE);
        }
      }
    });
    this.add(bForgetStoredServices, c);
    
    
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
  private void applyChanges()
  {
    // --- BioCatalogue BASE URL ---
    
    String candidateBaseURL = tfBioCatalogueAPIBaseURL.getText(); 
    if (candidateBaseURL.length() == 0) {
      JOptionPane.showMessageDialog(this, 
          "BioCatalogue base URL must not be blank", "BioCatalogue Plugin", JOptionPane.WARNING_MESSAGE);
      tfBioCatalogueAPIBaseURL.requestFocusInWindow();
      return;
    }
    else {
      try {
        new URL(candidateBaseURL);
      }
      catch (MalformedURLException e) {
        JOptionPane.showMessageDialog(this, 
            "Currently set BioCatalogue base URL is not valid - please check your input",
            "BioCatalogue Plugin", JOptionPane.WARNING_MESSAGE);
        tfBioCatalogueAPIBaseURL.selectAll();
        tfBioCatalogueAPIBaseURL.requestFocusInWindow();
        return;
      }
      
      
      // check if the base URL has changed from the last saved state
      if (!candidateBaseURL.equals(configuration.getProperty(BioCataloguePluginConfiguration.BIOCATALOGUE_BASE_URL))) {
        JOptionPane.showMessageDialog(this, "You have updated the BioCatalogue base URL.\n\n" +
        		"From now on the new one will be used, however it is advised\n" +
        		"to restart Taverna for this new setting to take the full effect.\n\n" +
        		"If you keep using Taverna, any previously made searches, filtering\n" +
        		"operations and tags in the tag cloud may still use the previous\n" +
        		"setting.",
            "BioCatalogue Plugin", JOptionPane.INFORMATION_MESSAGE);
      }
      
      
      // TODO - implement a test request (e.g. to the base URL where it has some
      //        basic API details; check the version there or something similar
      //        this way if it's not the BioCatalogue endpoint, can reject at
      //        config stage, not usage stage).
      
      
      // the new base URL seems to be valid - can save it into config settings
      configuration.setProperty(BioCataloguePluginConfiguration.BIOCATALOGUE_BASE_URL, candidateBaseURL);
      
      // also update the base URL in the BioCatalogueClient
      MainComponentFactory.getSharedInstance().getBioCatalogueClient().setBaseURL(candidateBaseURL);
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
}
