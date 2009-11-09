/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.views.graph.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Hashtable;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.GraphController.PortStyle;

/**
 * UI for GraphViewConfiguration.
 *
 * @author David Withers
 */
public class GraphViewConfigurationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int ANIMATION_SPEED_MIN = 100;
    private static final int ANIMATION_SPEED_MAX = 3100;

    private static GraphViewConfiguration configuration = GraphViewConfiguration.getInstance();

    public GraphViewConfigurationPanel() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        JTextArea descriptionText = new JTextArea(
                "Default settings for the workflow diagram.");
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setEditable(false);
        descriptionText.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel defaultLayoutLabel = new JLabel("Service display");

        final JRadioButton noPorts = new JRadioButton();
        JRadioButton allPorts = new JRadioButton();
        JRadioButton blobs = new JRadioButton();

        JLabel noPortsLabel = new JLabel("Name only", WorkbenchIcons.noportIcon, JLabel.LEFT);
        JLabel allPortsLabel = new JLabel("Name and ports", WorkbenchIcons.allportIcon, JLabel.LEFT);
        JLabel blobsLabel = new JLabel("No text", WorkbenchIcons.blobIcon, JLabel.LEFT);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(noPorts);
        buttonGroup.add(allPorts);
        buttonGroup.add(blobs);
        
        PortStyle portStyle = PortStyle.valueOf(configuration.getProperty(GraphViewConfiguration.PORT_STYLE));
        if (portStyle.equals(PortStyle.NONE)) {
        	noPorts.setSelected(true);
        } else if (portStyle.equals(PortStyle.ALL)) {
        	allPorts.setSelected(true);
        } else {
        	blobs.setSelected(true);
        }

        JLabel defaultAlignmentLabel = new JLabel("Diagram alignment");

        JRadioButton vertical = new JRadioButton();
        JRadioButton horizontal = new JRadioButton();

        JLabel verticalLabel = new JLabel("Vertical", WorkbenchIcons.verticalIcon, JLabel.LEFT);
        JLabel horizontalLabel = new JLabel("Horizontal", WorkbenchIcons.horizontalIcon, JLabel.LEFT);

        ButtonGroup alignmentButtonGroup = new ButtonGroup();
        alignmentButtonGroup.add(horizontal);
        alignmentButtonGroup.add(vertical);

        Alignment alignment = Alignment.valueOf(configuration.getProperty(GraphViewConfiguration.ALIGNMENT));
        if (alignment.equals(Alignment.VERTICAL)) {
        	vertical.setSelected(true);
        } else {
        	horizontal.setSelected(true);
        }

        final JCheckBox animation = new JCheckBox("Enable animation");
 
        boolean animationEnabled = Boolean.parseBoolean(configuration.getProperty(GraphViewConfiguration.ANIMATION_ENABLED));
        animation.setSelected(animationEnabled);

        final JLabel animationSpeedLabel = new JLabel("Animation speed");
        animationSpeedLabel.setEnabled(animationEnabled);
        
        final JSlider animationSpeedSlider = new JSlider(ANIMATION_SPEED_MIN, ANIMATION_SPEED_MAX);
        animationSpeedSlider.setEnabled(animationEnabled);
        animationSpeedSlider.setMajorTickSpacing(500);
        animationSpeedSlider.setMinorTickSpacing(100);
        animationSpeedSlider.setPaintTicks(true);
        animationSpeedSlider.setPaintLabels(true);
        animationSpeedSlider.setInverted(true);
        animationSpeedSlider.setSnapToTicks(true);
 
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(new Integer(ANIMATION_SPEED_MIN), new JLabel("Fast"));
        labelTable.put(new Integer(((ANIMATION_SPEED_MAX - ANIMATION_SPEED_MIN) / 2) + ANIMATION_SPEED_MIN), new JLabel("Medium"));
        labelTable.put(new Integer(ANIMATION_SPEED_MAX), new JLabel("Slow"));
        animationSpeedSlider.setLabelTable(labelTable);

        int animationSpeed = Integer.valueOf(configuration.getProperty(GraphViewConfiguration.ANIMATION_SPEED));
        if (animationSpeed > ANIMATION_SPEED_MAX) {
        	animationSpeed = ANIMATION_SPEED_MAX;
        } else if (animationSpeed < ANIMATION_SPEED_MIN) {
        	animationSpeed = ANIMATION_SPEED_MIN;
        }
        animationSpeedSlider.setValue(animationSpeed);

        noPorts.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	configuration.setProperty(GraphViewConfiguration.PORT_STYLE, PortStyle.NONE.toString());
            }
        });

        allPorts.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	configuration.setProperty(GraphViewConfiguration.PORT_STYLE, PortStyle.ALL.toString());
            }
        });

        blobs.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	configuration.setProperty(GraphViewConfiguration.PORT_STYLE, PortStyle.BLOB.toString());
            }
        });

        vertical.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	configuration.setProperty(GraphViewConfiguration.ALIGNMENT, Alignment.VERTICAL.toString());
            }
        });

        horizontal.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	configuration.setProperty(GraphViewConfiguration.ALIGNMENT, Alignment.HORIZONTAL.toString());
            }
        });

        animation.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
            	boolean animationEnabled = animation.isSelected();
            	configuration.setProperty(GraphViewConfiguration.ANIMATION_ENABLED,
            			String.valueOf(animationEnabled));
         		animationSpeedLabel.setEnabled(animationEnabled);
         		animationSpeedSlider.setEnabled(animationEnabled);
            }
        });

        animationSpeedSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			    if (!animationSpeedSlider.getValueIsAdjusting()) {
			        int speed = animationSpeedSlider.getValue();
	            	configuration.setProperty(GraphViewConfiguration.ANIMATION_SPEED, String.valueOf(speed));
			    }
			}
		});
        
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1d;
        c.weighty = 0d;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        add(descriptionText, c);

        c.insets = new Insets(10, 0, 10, 0);
        add(defaultLayoutLabel, c);
        
        c.insets = new Insets(0, 20, 0, 0);
        c.gridwidth = 1;
        c.weightx = 0d;
        add(noPorts, c);
        c.insets = new Insets(0, 5, 0, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        add(noPortsLabel, c);

        c.insets = new Insets(0, 10, 0, 0);
        add(allPorts, c);
        c.insets = new Insets(0, 5, 0, 0);
        add(allPortsLabel, c);

        c.insets = new Insets(0, 10, 0, 0);
        add(blobs, c);
        c.insets = new Insets(0, 5, 0, 0);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1d;
        add(blobsLabel, c);

        // alignment
        c.insets = new Insets(20, 0, 10, 0);
        c.gridx = 0;
        add(defaultAlignmentLabel, c);
        
        c.insets = new Insets(0, 20, 0, 0);
        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0d;
        add(vertical, c);
        c.insets = new Insets(0, 5, 0, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        add(verticalLabel, c);

        c.insets = new Insets(0, 10, 0, 0);
        add(horizontal, c);
        c.insets = new Insets(0, 5, 0, 0);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1d;
        add(horizontalLabel, c);

        // animation
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(20, 0, 10, 0);
        add(animation, c);

        c.insets = new Insets(0, 20, 0, 0);
        add(animationSpeedLabel, c);
        
        c.insets = new Insets(0, 20, 10, 30);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weighty = 1d;
        add(animationSpeedSlider, c);

    }

    // for testing only
    public static void main(String[] args) {
        JDialog dialog = new JDialog();
        dialog.add(new GraphViewConfigurationPanel());
        dialog.setModal(true);
        dialog.setSize(500, 400);
        dialog.setVisible(true);
        System.exit(0);
    }
    
}
