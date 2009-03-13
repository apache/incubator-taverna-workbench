/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.help.CSH;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;

import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.partition.ActivityQueryFactory;
import net.sf.taverna.t2.partition.AddQueryActionHandler;
import net.sf.taverna.t2.partition.PartitionAlgorithm;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPI;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPIRegistry;
import net.sf.taverna.t2.partition.PropertyExtractorRegistry;
import net.sf.taverna.t2.partition.PropertyExtractorSPIRegistry;
import net.sf.taverna.t2.partition.Query;
import net.sf.taverna.t2.partition.QueryFactory;
import net.sf.taverna.t2.partition.QueryFactoryRegistry;
import net.sf.taverna.t2.partition.RootPartition;
import net.sf.taverna.t2.partition.SetModelChangeListener;
import net.sf.taverna.t2.partition.algorithms.CustomPartitionAlgorithm;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/**
 * Contains the {@link ActivityTree} which shows the available activities
 * partitioned by their properties using a {@link PartitionAlgorithm}. Contains
 * a {@link JMenu} which allows the user to filter the activities by their
 * properties
 * 
 * @author Ian Dunlop
 * @author Stuart Owen
 * @author Alex Nenadic
 * 
 */
@SuppressWarnings("serial")
public class ActivityPaletteComponent extends JPanel implements UIComponentSPI {
	/** All the available activities filtered by the selected properties */
	private JTree activityTree;
	/** The different properties for each type of activity */
	private PropertyExtractorRegistry propertyExtractorRegistry;
	/**
	 * Allows the user to filter the activities by selecting a property. This
	 * then reconfigures the tree model with a different set of
	 * {@link PartitionAlgorithm}s
	 */
	private RootPartition<?> rootPartition;
	private List<PartitionAlgorithm<?>> selectedPartitions = new ArrayList<PartitionAlgorithm<?>>(
			2);
	JMenu firstMenu;
	JMenu secondMenu;
	Map<PartitionAlgorithm<?>, JMenuItem> firstAlgorithmToMenuItemMap = new HashMap<PartitionAlgorithm<?>, JMenuItem>();
	Map<PartitionAlgorithm<?>, JMenuItem> secondAlgorithmToMenuItemMap = new HashMap<PartitionAlgorithm<?>, JMenuItem>();
	Map<String, LiteralValuePartitionAlgorithm> partitionAlgorithmMap = new HashMap<String, LiteralValuePartitionAlgorithm>();
	private static ActivityPaletteComponent instance = new ActivityPaletteComponent();
	private JComboBox firstPartitionComboBox;
	private JComboBox secondPartitionComboBox;
	private JTextField searchBox;
	private JButton searchButton;

	public static ActivityPaletteComponent getInstance() {
		return instance;
	}

	/**
	 * Provides access to the root partition of the tree.
	 * 
	 * @return the root partition
	 */
	public RootPartition<?> getRootPartition() {
		return rootPartition;
	}

	/**
	 * Sets the layout as {@link BorderLayout}. Then calls {@link #initialise()}
	 * to create the {@link ActivityTree}. Adds a {@link JMenuBar} to allow the
	 * user to filter the activities
	 */
	private ActivityPaletteComponent() {
		CSH
				.setHelpIDString(this,
						"net.sf.taverna.t2.workbench.ui.activitypalette.ActivityPaletteComponent");
		setLayout(new BorderLayout());
		initialise();

		// Create the partition comboboxes and search field and button
		createComboBoxes();
		
		// Lay everything out
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx =0.0;
		c.anchor = GridBagConstraints.LINE_START;
		topPanel.add(searchBox, c);

		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		topPanel.add(searchButton, c);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.0;
		topPanel.add(firstPartitionComboBox, c);

		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1.0;
		topPanel.add(secondPartitionComboBox, c);

		add(topPanel, BorderLayout.NORTH);
		add(new JScrollPane(activityTree), BorderLayout.CENTER);
	}

	/**
	 * Creates the {@link ActivityTree} which displays all the activities. Gets
	 * an instance of {@link PropertyExtractorSPIRegistry} which contains all
	 * the properties which the activities can be filtered on. Creates the
	 * {@link RootPartition}, basically a {@link TreeModel} which the
	 * {@link ActivityTree} needs. Then adds the initial queries (probably one
	 * for each activity)
	 */
	private void initialise() {

		propertyExtractorRegistry = PropertyExtractorSPIRegistry.getInstance();
		rootPartition = getPartition(propertyExtractorRegistry);
		initQueries(rootPartition);
		// createSorts();
		activityTree = new ActivityTree(rootPartition);
		activityTree.setCellRenderer(new ActivityTreeCellRenderer());
		((RootPartition) activityTree.getModel().getRoot())
				.setChildPartitionOrder(new Comparator<String>() {

					public int compare(String o1, String o2) {
						if (o1.compareTo(o2) > 0) {
							return 1;
						}
						return 0;
					}

				});
	}

	/**
	 * Create all the {@link Query}s for the activities and add them to the
	 * {@link SetModelChangeListener} which the original {@link RootPartition}
	 * has. This allows the {@link ActivityTree} to know about the queries and
	 * re-run them whenever the user selects a different filter. ie. when the
	 * {@link ActivityTree} is given a new model (remember that the model is a
	 * {@link RootPartition}. The actual queries do not change but the partition
	 * does
	 * 
	 * @param partition
	 */
	private void initQueries(RootPartition<?> partition) {

		List<Query<?>> queries = QueryFactoryRegistry.getInstance()
				.getQueries();

		for (Query<?> query : queries) {
			query.addSetModelChangeListener((SetModelChangeListener) partition
					.getSetModelChangeListener());
			partition.getSetModelChangeListener().addQuery(query);
		}
	}

	private void createComboBoxes() {

		// FIXME: currently acts a bit quirky if there are no activities added,
		// since there is an assumption
		// that there will be at least 1 partiion algorith for "type". There is
		// also an assumption there are 2
		// combo boxes.

		firstPartitionComboBox = new JComboBox();
		secondPartitionComboBox = new JComboBox();

		firstPartitionComboBox.setToolTipText("Select the first grouping");
		secondPartitionComboBox.setToolTipText("Select the second grouping");

		for (PartitionAlgorithm<?> algorithm : getAlgorithms()) {
			if (algorithm instanceof LiteralValuePartitionAlgorithm) {
				LiteralValuePartitionAlgorithm litAlg = (LiteralValuePartitionAlgorithm) algorithm;

				if (!(litAlg instanceof NoneSelectedPartitionAlgorithm)) { // dont
					// add
					// NoneSelected
					// to
					// the
					// first
					// combo
					// box
					firstPartitionComboBox.addItem(litAlg);
				}
				secondPartitionComboBox.addItem(litAlg);

				if (litAlg.getPropertyName().equalsIgnoreCase("type")) {
					selectedPartitions.add(0, litAlg);
					firstPartitionComboBox.setSelectedItem(litAlg);
				}

				if (litAlg instanceof NoneSelectedPartitionAlgorithm) {
					selectedPartitions.add(litAlg);
					secondPartitionComboBox.setSelectedItem(litAlg);
				}
			}
		}

		firstPartitionComboBox
				.setRenderer(new PartitionComboxBoxCellRenderer(0));
		secondPartitionComboBox.setRenderer(new PartitionComboxBoxCellRenderer(
				1));

		firstPartitionComboBox
				.addActionListener(new PartionComboBoxActionListener(0));
		secondPartitionComboBox
				.addActionListener(new PartionComboBoxActionListener(1));
	
		searchBox = new JTextField();
		searchBox.setText("Enter search here");
		searchBox.setMinimumSize(new Dimension(30,searchBox.getHeight()));
		searchBox.setToolTipText("Enter a search term and click the button");
		searchBox.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent e) {
				// When a user clicks on the box, the text 
				// "Enter search here" should disappear
				if (searchBox.getText().equals("Enter search here"))
					searchBox.setText("");
			}}
		);
		searchBox.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent e) {
				// If ENTER key is pressed, perform the search
		           if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		        	   doSearch();
		           }
			}
		});
		
		searchButton = new JButton("Search");
		searchButton.setFocusable(true);
		searchButton.setToolTipText("Search over all the activities and properties");
		searchButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				doSearch();				
			}
		});

	}

	class PartitionComboxBoxCellRenderer extends DefaultListCellRenderer {

		private final int index;

		public PartitionComboxBoxCellRenderer(int index) {
			this.index = index;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int i, boolean isSelected, boolean cellHasFocus) {

			JCheckBoxMenuItem result = new JCheckBoxMenuItem(value.toString());
			if (selectedPartitions.size() > 1) {
				if (value == selectedPartitions.get(index)) {
					result.setSelected(true);
				}
			}

			if (isSelected) {
				result.setBackground(Color.GRAY);
			}

			int otherSelectedIndex = index == 0 ? 1 : 0;
			if (selectedPartitions.size() > 1) {
				if (value == selectedPartitions.get(otherSelectedIndex)) {
					result.setEnabled(false);
					result.setFocusable(false);
				}
			}
			return result;
		}
	}

	class PartionComboBoxActionListener implements ActionListener {

		private final int index;

		public PartionComboBoxActionListener(int index) {
			this.index = index;
		}

		public void actionPerformed(ActionEvent ev) {
			JComboBox box = (JComboBox) ev.getSource();
			LiteralValuePartitionAlgorithm alg = (LiteralValuePartitionAlgorithm) box
					.getSelectedItem();
			if (!handleSelected(alg)) {
				box.setSelectedItem(selectedPartitions.get(index));
			}
		}

		private boolean handleSelected(PartitionAlgorithm<?> selected) {
			if (selected != selectedPartitions.get(0)
					&& selected != selectedPartitions.get(1)) {
				selectedPartitions.set(index, selected);

				final RootPartition<ActivityItem> root = (RootPartition<ActivityItem>) activityTree
						.getModel();
				final List<PartitionAlgorithm<?>> partitions = new ArrayList<PartitionAlgorithm<?>>();
				for (PartitionAlgorithm<?> selectedAlgorithm : selectedPartitions) {
					if (!(selectedAlgorithm instanceof NoneSelectedPartitionAlgorithm)) {
						partitions.add(selectedAlgorithm);
					}
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Cursor oldCursor = getCursor();
						Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
						setCursor(hourglassCursor);
						root.setPartitionAlgorithmList(partitions);
						setCursor(oldCursor);
					}
				});
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Searches over all the activities and each of the properties - "url",
	 * "operation" etc
	 * 
	 * @author Ian Dunlop
	 * 
	 */
/*	class SearchBoxActionListener implements ActionListener {

		private String text;

		public void actionPerformed(ActionEvent e) {
			text = searchBox.getText();
			final RootPartition<ActivityItem> root = (RootPartition<ActivityItem>) activityTree
					.getModel();
			if ((text != null) || !(text.equalsIgnoreCase("Enter Search here"))) {
				final List<PartitionAlgorithm<?>> partitions = new ArrayList<PartitionAlgorithm<?>>();
				CustomPartitionAlgorithm customAlg = new CustomPartitionAlgorithm();
				customAlg.setSearchValue(text);
				for (PartitionAlgorithm<?> algorithm : getAlgorithms()) {
					if (algorithm instanceof LiteralValuePartitionAlgorithm) {

						customAlg
								.addProperty(((LiteralValuePartitionAlgorithm) algorithm)
										.getPropertyName());
					}
				}
				partitions.add(customAlg);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Cursor oldCursor = getCursor();
						Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
						setCursor(hourglassCursor);
						root.setPartitionAlgorithmList(partitions);
						setCursor(oldCursor);
					}
				});
			}
		}
	}
	*/
	
	/**
	 * Searches over all the activities and each of the properties - "url",
	 * "operation" etc. Called when either the 'Search' button is clicked or 
	 * when ENTER is pressed when in the 'Search' text field.
	 */
	private void doSearch(){
		String text = searchBox.getText();
		final RootPartition<ActivityItem> root = (RootPartition<ActivityItem>) activityTree
				.getModel();
		if ((text != null) || !(text.equalsIgnoreCase("Enter Search here"))) {
			final List<PartitionAlgorithm<?>> partitions = new ArrayList<PartitionAlgorithm<?>>();
			CustomPartitionAlgorithm customAlg = new CustomPartitionAlgorithm();
			customAlg.setSearchValue(text);
			for (PartitionAlgorithm<?> algorithm : getAlgorithms()) {
				if (algorithm instanceof LiteralValuePartitionAlgorithm) {

					customAlg
							.addProperty(((LiteralValuePartitionAlgorithm) algorithm)
									.getPropertyName());
				}
			}
			partitions.add(customAlg);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Cursor oldCursor = getCursor();
					Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
					setCursor(hourglassCursor);
					root.setPartitionAlgorithmList(partitions);
					setCursor(oldCursor);
				}
			});
		}
	}

	/*
	private JMenu createAddActivityMenu() {
		JMenu addQueryMenu = new JMenu("Add...");
		addQueryMenu.setToolTipText("Open this menu to add a new Query");
		for (QueryFactory factory : QueryFactoryRegistry.getInstance()
				.getInstances()) {
			if (factory instanceof ActivityQueryFactory) {
				ActivityQueryFactory af = (ActivityQueryFactory) factory;
				if (af.hasAddQueryActionHandler()) {
					AddQueryActionHandler handler = af
							.getAddQueryActionHandler();
					handler
							.setSetModelChangeListener((SetModelChangeListener<ActivityItem>) rootPartition
									.getSetModelChangeListener());
					addQueryMenu.add(handler);
				}
			}
		}
		return addQueryMenu;
	}*/

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Activity Palette";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	/**
	 * The {@link RootPartition} represents the {@link TreeModel} in the
	 * {@link ActivityTree}. It has a list of all the ways that activities can
	 * be filtered (all the {@link PartitionAlgorithm}s) and also has a
	 * {@link PropertyExtractorRegistry} which knows all the different
	 * properties for each type of activity
	 * 
	 * @param <ActivityItem>
	 * @param reg
	 * @return {@link RootPartition} which is the {@link TreeModel} for the
	 *         {@link ActivityTree}
	 */
	private <ActivityItem extends Comparable> RootPartition<?> getPartition(
			PropertyExtractorRegistry reg) {
		return new RootPartition<ActivityItem>(DefaultPartitionAlgorithms
				.getPartitionAlgorithms(), reg);
	}

	/**
	 * Loop through all the available {@link PartitionAlgorithm}s and create a
	 * master set. This is all the possible ways that the activities can be
	 * filtered
	 * 
	 * @return A union of all the {@link PartitionAlgorithm} sets
	 */
	private List<PartitionAlgorithm<?>> getAlgorithms() {
		// TODO use the SPI instead of hard coding when there are algorithms
		// ready
		List<PartitionAlgorithmSetSPI> instances = PartitionAlgorithmSetSPIRegistry
				.getInstance().getInstances();
		Set<PartitionAlgorithm<?>> partitionAlgorithmSet = new HashSet<PartitionAlgorithm<?>>();

		for (PartitionAlgorithmSetSPI instance : instances) {
			Set<PartitionAlgorithm<?>> partitonAlgorithms = instance
					.getPartitionAlgorithms();
			partitionAlgorithmSet.addAll(partitonAlgorithms);
		}
		List<PartitionAlgorithm<?>> partitionAlgorithmList = new ArrayList<PartitionAlgorithm<?>>();
		partitionAlgorithmList.add(new NoneSelectedPartitionAlgorithm());
		for (PartitionAlgorithm<?> algorithm : partitionAlgorithmSet) {
			partitionAlgorithmList.add(algorithm);
		}

		return partitionAlgorithmList;
	}

}
