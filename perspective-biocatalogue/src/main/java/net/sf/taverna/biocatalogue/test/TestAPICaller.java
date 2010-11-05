package net.sf.taverna.biocatalogue.test;

import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Sergejs Aleksejevs
 */
public class TestAPICaller extends JFrame implements ActionListener {

	private JPanel jContentPane = null;
	private JTextField tfURL = null;
	private JButton bSubmitRequest = null;
	private JButton bClear = null;
	private JScrollPane spOutputPane = null;
	private JTextPane tpOutputPane = null;

	/**
	 * This method initializes 
	 * 
	 */
	public TestAPICaller() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(515, 321));
        this.setTitle("Test BioCatalogue API Caller");
        this.setContentPane(getJContentPane());
		
        this.bSubmitRequest.setDefaultCapable(true);
        this.getRootPane().setDefaultButton(bSubmitRequest);
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridwidth = 2;
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 2;
			gridBagConstraints3.ipadx = 459;
			gridBagConstraints3.ipady = 182;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.insets = new Insets(4, 12, 9, 12);
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.ipady = 0;
			gridBagConstraints2.insets = new Insets(0, 5, 7, 12);
			gridBagConstraints2.weightx = 0.5;
			gridBagConstraints2.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.ipadx = 0;
			gridBagConstraints1.ipady = 0;
			gridBagConstraints1.insets = new Insets(0, 12, 7, 5);
			gridBagConstraints1.weightx = 0.5;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.ipadx = 466;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.ipady = 3;
			gridBagConstraints.insets = new Insets(13, 12, 8, 12);
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getTfURL(), gridBagConstraints);
			jContentPane.add(getBSubmitRequest(), gridBagConstraints1);
			jContentPane.add(getBClear(), gridBagConstraints2);
			jContentPane.add(getSpOutputPane(), gridBagConstraints3);
		}
		return jContentPane;
	}

	/**
	 * This method initializes tfURL	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTfURL() {
		if (tfURL == null) {
			tfURL = new JTextField();
			tfURL.setText(BioCatalogueClient.DEFAULT_API_SANDBOX_BASE_URL);
		}
		return tfURL;
	}

	/**
	 * This method initializes tfSubmitRequest	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBSubmitRequest() {
		if (bSubmitRequest == null) {
			bSubmitRequest = new JButton();
			bSubmitRequest.setText("Submit Request");
			bSubmitRequest.addActionListener(this);
		}
		return bSubmitRequest;
	}
	
	
	/**
	 * This method initializes bClear	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBClear() {
		if (bClear == null) {
			bClear = new JButton();
			bClear.setText("Clear Output");
			bClear.addActionListener(this);
		}
		return bClear;
	}
	
	
	/**
	 * This method initializes tpOutputPane	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextPane getTpOutputPane() {
		if (tpOutputPane == null) {
			tpOutputPane = new JTextPane();
			tpOutputPane.setContentType("text/plain");
		}
		return tpOutputPane;
	}
	

	/**
	 * This method initializes spOutputPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getSpOutputPane() {
		if (spOutputPane == null) {
			spOutputPane = new JScrollPane();
			spOutputPane.setViewportView(getTpOutputPane());
		}
		return spOutputPane;
	}

	
	// ACTION LISTENER
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(bSubmitRequest)) {
			tfURL.selectAll();
			
			// call the actual test method
			runBioCatalogueAPITest(tfURL.getText());
		}
		else if (e.getSource().equals(bClear)) {
			this.tpOutputPane.setText("");
		}
		
	}


	// ACTUAL TEST CLASS
	
	private void runBioCatalogueAPITest(String url) {
		final String urlFinal = url;
		new Thread("making request") {
  		public void run() {
  		  tpOutputPane.setText("Initialising BioCatalogue client...");
  		  BioCatalogueClient client = null;
        try {
          client = new BioCatalogueClient();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        
    		final StringBuilder text = new StringBuilder();
    		try {
    		  tpOutputPane.setText("Sending request...");
    		  BufferedReader br = new BufferedReader(new InputStreamReader(client.doBioCatalogueGET(urlFinal).getResponseStream()));
    		  String str = "";
    		  
    		  while ((str = br.readLine()) != null) {
    		    text.append(str + "\n");
    		  }
    		  
    		  br.close();
    		}
    		catch (Exception e) {
    		  text.append(e);
    		}
    		
    		SwingUtilities.invokeLater(new Runnable() {
    		  public void run() {
    		    tpOutputPane.setText(text.toString());
    		  }
    		});
  		}
		}.start();
		
	}

	
}
