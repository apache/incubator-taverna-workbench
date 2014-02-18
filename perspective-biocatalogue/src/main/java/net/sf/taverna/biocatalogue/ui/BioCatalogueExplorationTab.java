package net.sf.taverna.biocatalogue.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutFocusTraversalPolicy;

import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.ui.search_results.SearchResultsMainPanel;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config.BaseURLChangedEvent;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config.BioCataloguePluginConfiguration;
import net.sf.taverna.t2.workbench.ui.impl.configuration.ui.T2ConfigurationFrame;

import org.apache.log4j.Logger;

/**
 * 
 * @author Sergejs Aleksejevs
 */
@SuppressWarnings("serial")
public class BioCatalogueExplorationTab extends JPanel implements
		HasDefaultFocusCapability, Observer<BaseURLChangedEvent> {
	private final MainComponent pluginPerspectiveMainComponent;
	private final BioCatalogueClient client;
	private final Logger logger;

	// COMPONENTS
	private BioCatalogueExplorationTab thisPanel;

	private SearchOptionsPanel searchOptionsPanel;
	private SearchResultsMainPanel tabbedSearchResultsPanel;
	private JLabel baseLabel;
	private String baseURL;

	public BioCatalogueExplorationTab() {
		
		BioCataloguePluginConfiguration.getInstance().addObserver(this);
		
		this.thisPanel = this;

		this.pluginPerspectiveMainComponent = MainComponentFactory
				.getSharedInstance();
		this.client = BioCatalogueClient.getInstance();
		this.logger = Logger.getLogger(this.getClass());

		initialiseUI();

		// this is to make sure that search will get focused when this tab is
		// opened
		// -- is a workaround to a bug in JVM
		setFocusCycleRoot(true);
		setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
			public Component getDefaultComponent(Container cont) {
				return (thisPanel.getDefaultComponent());
			}
		});
	}

	private void initialiseUI() {
		this.tabbedSearchResultsPanel = new SearchResultsMainPanel();
		this.searchOptionsPanel = new SearchOptionsPanel(
				tabbedSearchResultsPanel);

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(3, 10, 3, 10);
		baseURL = client.getBaseURL();
		String baseURLString = getBaseURLLabelString(baseURL);
		baseLabel = new JLabel(baseURLString);
		this.add(baseLabel, c);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 2;
		c.insets = new Insets(0, 5, 0, 0);
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		JButton changeServiceCatalogueButton = new JButton("Change");
		JPanel changeServiceCataloguePanel = new JPanel(new BorderLayout());
		changeServiceCataloguePanel.add(changeServiceCatalogueButton,
				BorderLayout.WEST);
		changeServiceCatalogueButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				T2ConfigurationFrame
						.showConfiguration(BioCataloguePluginConfiguration
								.getInstance().getDisplayName());
			}
		});
		c.fill = GridBagConstraints.BOTH;
		this.add(changeServiceCataloguePanel, c);

		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.1;
		c.weighty = 0.0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(23, 30, 3, 10);
		this.add(searchOptionsPanel, c);

		c.insets = new Insets(0, 0, 0, 0);
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 2;
		c.weightx = c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		this.add(tabbedSearchResultsPanel, c);

		this.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
	}

	private String getBaseURLLabelString(String url) {
		return "<html>Using " + BioCataloguePluginConfiguration.getInstance().getServiceCatalogueFriendlyName() + " at: " + url + "</html>";
	}

	public SearchResultsMainPanel getTabbedSearchResultsPanel() {
		return tabbedSearchResultsPanel;
	}

	// *** Callbacks for HasDefaultFocusCapability interface ***

	public void focusDefaultComponent() {
		this.searchOptionsPanel.focusDefaultComponent();
	}

	public Component getDefaultComponent() {
		return (this.searchOptionsPanel.getDefaultComponent());
	}

	// *********************************************************

	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.getContentPane().add(new BioCatalogueExplorationTab());
		f.setSize(1000, 800);
		f.setLocationRelativeTo(null);

		f.setVisible(true);
	}

	@Override
	public void notify(Observable<BaseURLChangedEvent> sender,
			BaseURLChangedEvent message) throws Exception {
		BioCataloguePluginConfiguration configuration = BioCataloguePluginConfiguration
				.getInstance();
		if (!baseURL.equals(configuration
				.getProperty(BioCataloguePluginConfiguration.SERVICE_CATALOGUE_BASE_URL))) {
			baseURL = configuration
					.getProperty(BioCataloguePluginConfiguration.SERVICE_CATALOGUE_BASE_URL);
			baseLabel.setText(getBaseURLLabelString(baseURL));
			client.setBaseURL(baseURL);
		}
	}
 
}
