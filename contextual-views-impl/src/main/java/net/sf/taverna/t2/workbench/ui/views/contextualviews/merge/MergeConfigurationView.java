package net.sf.taverna.t2.workbench.ui.views.contextualviews.merge;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

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
	
	// Ordered list of labels for ports to be displayed to the user
	private List<String> labelList;
	
	// Width of the dialog calculated to be bigger width of the longest label string in the dialog
	private int dialogWidth;


	public MergeConfigurationView(Merge merge){
		
		super();
		setTitle("Merge Configuration");
		setModal(true);
		setResizable(false); // make it not resizeable
		
		this.merge = merge;
		// Ordered list of merge's input ports
		inputPortsList = new ArrayList<MergeInputPort>(merge.getInputPorts());
		// Generate labels for the input ports (label displays a link from a workflow entity 
		// towards the merge's input port)
		labelList = new ArrayList<String>();
		String maxLabel = "";
		for (MergeInputPort mergeInputPort : inputPortsList){	
			EventForwardingOutputPort sourcePort = mergeInputPort.getIncomingLink().getSource();
			// Get the name TokenProcessingEntity (Processor or another Merge or Dataflow) and 
			// its port that contains the source EventForwardingOutputPort
			Dataflow workflow = FileManager.getInstance().getCurrentDataflow();
			TokenProcessingEntity entity = Tools.getTokenProcessingEntityWithEventForwardingOutputPort(sourcePort, workflow);
			if (entity != null){
				String link = entity.getLocalName() + "."
						+ sourcePort.getName() + " -> " + merge.getLocalName()
						+ "." + mergeInputPort.getName();
				if (link.length() > maxLabel.length())
					maxLabel = link;
				labelList.add(link);
			}
		}
		// Calculate how many pixels will maximum label take
		FontMetrics fm = this.getFontMetrics(this.getFont());  
		// +32 to allow for arrow icons; +10 for borders; +15 for space between GridBagLayout cells
		// and +13 just in case to add up to a nice number +75
		dialogWidth = fm.stringWidth(maxLabel) + 75;  

		initComponents();
	}
	
	private void initComponents() {
		
        getContentPane().removeAll(); // clear old components, if any (for refreshing purposes)

        getContentPane().setLayout(new BorderLayout());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new GridBagLayout());
        listPanel.setBorder(new CompoundBorder(new EmptyBorder(10,10,10,10), new EtchedBorder()));
        
        JLabel title = new JLabel("<html><body><b>Order of incoming links:</b></body></html>");
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 1.0;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(5,5,0,0);
		c.fill = GridBagConstraints.HORIZONTAL;
		listPanel.add(title, c);
		
		// Display the list of (links to) ports to be reordered
		c.gridwidth = 1;
		for (int i = 0; i < labelList.size(); i++){	
						
			final int index = i;
			
			// Label showing the merge's link
			c.gridy = i+1;
			c.gridx = 0;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			JLabel link = new JLabel((i+1) + ". " + labelList.get(i));
			listPanel.add(link, c);

			// Push up button, if any
			c.gridx = 1;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.NONE;
			if (i != 0){ // not the first one - add the button to push up
				JButton upButton = new JButton(new AbstractAction(){
					public void actionPerformed(ActionEvent e) {
						// Swap the labels
						String label = labelList.get(index);
						labelList.set(index, labelList.get(index - 1));
						labelList.set(index - 1, label);
						// Swap the merge's ports
						MergeInputPort port = inputPortsList.get(index);
						inputPortsList.set(index, inputPortsList.get(index - 1));
						inputPortsList.set(index - 1, port);
						// Refresh the dialog
						initComponents();
					}});
				upButton.setIcon(WorkbenchIcons.upArrowIcon);
				listPanel.add(upButton, c);
			}

			// Push down button, if any
			c.gridx = 2;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.NONE;
			if (i != labelList.size()-1){ // not the last one - add the button to push down
				JButton downButton = new JButton(new AbstractAction(){
					public void actionPerformed(ActionEvent e) {
						// Swap the labels
						String label = labelList.get(index);
						labelList.set(index, labelList.get(index + 1));
						labelList.set(index + 1, label);
						// Swap the merge's ports
						MergeInputPort port = inputPortsList.get(index);
						inputPortsList.set(index, inputPortsList.get(index + 1));
						inputPortsList.set(index + 1, port);
						// Refresh the dialog
						initComponents();						
					}});
				downButton.setIcon(WorkbenchIcons.downArrowIcon);
				listPanel.add(downButton, c);
			}
		}
		
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
        
        // For some reason setMinimumSize, setPreferredSize and setMaximumSize did not work
		setSize(new Dimension(Math.max(dialogWidth, this.getPreferredSize().width), getPreferredSize().height));  

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
