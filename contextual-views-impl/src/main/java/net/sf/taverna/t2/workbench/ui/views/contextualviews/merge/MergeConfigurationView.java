package net.sf.taverna.t2.workbench.ui.views.contextualviews.merge;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

@SuppressWarnings("serial")
public class MergeConfigurationView extends JDialog{

	private Merge merge;
	
	// Ordered list of merge's input ports
	private List<MergeInputPort> inputPortsList;
	
	// Ordered list of labels for links towards merge's ports to be displayed to the user
	private DefaultListModel labelListModel;
	
	// JList that displays the labelListModel
	JList list;

	// Button to push the link up the list
	private JButton upButton;

	// Button to push the link down the list
	private JButton downButton;

	public MergeConfigurationView(Merge merge){
		
		super();
		setTitle("Merge Configuration");
		setModal(true);
		
		this.merge = merge;
		// Ordered list of merge's input ports
		inputPortsList = new ArrayList<MergeInputPort>(merge.getInputPorts());
		// Generate labels for the input ports (label displays a link from a workflow entity 
		// towards the merge's input port)
		labelListModel = new DefaultListModel();
		String maxLabel = "Order of incoming links (entity.port -> merge):"+"Push";
		for (MergeInputPort mergeInputPort : inputPortsList){	
			EventForwardingOutputPort sourcePort = mergeInputPort.getIncomingLink().getSource();
			// Get the name TokenProcessingEntity (Processor or another Merge or Dataflow) and 
			// its port that contains the source EventForwardingOutputPort
			Dataflow workflow = FileManager.getInstance().getCurrentDataflow();
			TokenProcessingEntity entity = Tools.getTokenProcessingEntityWithEventForwardingOutputPort(sourcePort, workflow);
			if (entity != null){
				String link = entity.getLocalName() + "."
						+ sourcePort.getName() + " -> " + merge.getLocalName();
				if (link.length() > maxLabel.length())
					maxLabel = link;
				labelListModel.addElement(link);
			}
		}
			
		initComponents();
	}
	
	private void initComponents() {
		
        getContentPane().setLayout(new BorderLayout());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new EtchedBorder()));
        
        JLabel title = new JLabel("<html><body><b>Order of incoming links (entity.port -> merge):</b></body></html>");
        title.setBorder(new EmptyBorder(5,5,5,5));
        listPanel.add(title, BorderLayout.NORTH);

        list = new JList(labelListModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(-1);
		list.addListSelectionListener(new ListSelectionListener(){

			// Enable and disable up and down buttons based on which item in the list is selected
			public void valueChanged(ListSelectionEvent e) {
				int index = list.getSelectedIndex();
				if ((index == -1) || (index == 0 && labelListModel.size() == 0) ){ //nothing selected or only one item in the list
					upButton.setEnabled(false);
					downButton.setEnabled(false);
				}
				else if (index == 0){ // first element in the list
					upButton.setEnabled(false);
					downButton.setEnabled(true);
				}
				else if (index == labelListModel.size() - 1){ //last element in the list
					upButton.setEnabled(true);
					downButton.setEnabled(false);
				}
				else {
					upButton.setEnabled(true); // any other element in the list
					downButton.setEnabled(true);			
				}
			}});

        final JScrollPane listScroller = new JScrollPane(list);
        listScroller.setBorder(new EmptyBorder(5,5,5,5));
        listScroller.setBackground(listPanel.getBackground());
        listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // Set the size of scroll pane to make all list items visible
		FontMetrics fm = listScroller.getFontMetrics(this.getFont());  
		int listScrollerHeight = fm.getHeight()*(labelListModel.size()) + 75; //+75 just in case
        listScroller.setPreferredSize(new Dimension(listScroller
				.getPreferredSize().width, Math.max(listScrollerHeight,
				listScroller.getPreferredSize().height)));  
		listPanel.add(listScroller, BorderLayout.CENTER);
		
		JPanel upDownButtonPanel = new JPanel(); 
		upDownButtonPanel.setLayout(new BoxLayout(upDownButtonPanel, BoxLayout.Y_AXIS));
		upDownButtonPanel.setBorder(new EmptyBorder(5,5,5,5));
		
		upButton = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				if (index != -1){
					// Swap the labels
					String label = (String) labelListModel.elementAt(index);
					labelListModel.set(index, labelListModel.get(index - 1));
					labelListModel.set(index - 1, label);
					// Swap the merge's ports
					MergeInputPort port = inputPortsList.get(index);
					inputPortsList.set(index, inputPortsList.get(index - 1));
					inputPortsList.set(index - 1, port);
					// Make the pushed item selected 
					list.setSelectedIndex(index - 1);
					// Refresh the list
					listScroller.repaint();
					listScroller.revalidate();
				}
			}});
		upButton.setIcon(WorkbenchIcons.upArrowIcon);
		upButton.setText("Up");
	    // Place text to the right of icon, vertically centered
		upButton.setVerticalTextPosition(SwingConstants.CENTER);
		upButton.setHorizontalTextPosition(SwingConstants.RIGHT);
		// Set the horizontal alignment of the icon and text
		upButton.setHorizontalAlignment(SwingConstants.LEFT);
		upButton.setEnabled(false);
		upDownButtonPanel.add(upButton);
		
		downButton = new JButton(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				if (index != -1){
					// Swap the labels
					String label = (String) labelListModel.elementAt(index);
					labelListModel.set(index, labelListModel.get(index + 1));
					labelListModel.set(index + 1, label);
					// Swap the merge's ports
					MergeInputPort port = inputPortsList.get(index);
					inputPortsList.set(index, inputPortsList.get(index + 1));
					inputPortsList.set(index + 1, port);
					// Make the pushed item selected 
					list.setSelectedIndex(index + 1);
					// Refresh the list
					list.repaint();	
					listScroller.revalidate();
				}					
			}});
		downButton.setIcon(WorkbenchIcons.downArrowIcon);
		downButton.setText("Down");
	    // Place text to the right of icon, vertically centered
		downButton.setVerticalTextPosition(SwingConstants.CENTER);
		downButton.setHorizontalTextPosition(SwingConstants.RIGHT);
		// Set the horizontal alignment of the icon and text
		downButton.setHorizontalAlignment(SwingConstants.LEFT);
		downButton.setEnabled(false);
		upButton.setPreferredSize(downButton.getPreferredSize()); // set the up button to be of the same size as down button
		upButton.setMaximumSize(downButton.getPreferredSize()); 
		upButton.setMinimumSize(downButton.getPreferredSize());
		upDownButtonPanel.add(downButton);
		
		listPanel.add(upDownButtonPanel, BorderLayout.EAST);

		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
       
		JButton jbOK = new JButton("OK");
        jbOK.addActionListener(new AbstractAction()
        {
			public void actionPerformed(ActionEvent e) {
				
				new MergeConfigurationAction(merge, inputPortsList).actionPerformed(e);
		        closeDialog();			
			}

        });

        JButton jbCancel = new JButton("Cancel");
        jbCancel.addActionListener(new AbstractAction()
        {

			public void actionPerformed(ActionEvent e) {
		        closeDialog();			
			}

        });
        
        buttonPanel.add(jbOK);
        buttonPanel.add(jbCancel);

        getContentPane().add(listPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
	}

	/**
     * Close the dialog.
     */
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
}
