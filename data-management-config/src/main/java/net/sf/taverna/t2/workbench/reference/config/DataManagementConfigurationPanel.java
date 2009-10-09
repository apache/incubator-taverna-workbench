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
package net.sf.taverna.t2.workbench.reference.config;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import net.sf.taverna.t2.workbench.helper.Helper;

public class DataManagementConfigurationPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private DataManagementConfiguration configuration = DataManagementConfiguration.getInstance();
    JCheckBox enableProvenance;
    JCheckBox enableInMemory;

    public DataManagementConfigurationPanel() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        

        enableProvenance = new JCheckBox("Enable provenance capture");
        JTextArea enableProvenanceText = new JTextArea("Disabling provenance will prevent you from being able to view intermediate results, but does give a performance benefit.");
        enableProvenanceText.setLineWrap(true);
        enableProvenanceText.setWrapStyleWord(true);
        enableProvenanceText.setEditable(false);
        enableProvenanceText.setOpaque(false);
        enableProvenanceText.setFont(enableProvenanceText.getFont().deriveFont(Font.PLAIN, 10));

        enableInMemory = new JCheckBox("In-memory storage");
        JTextArea enableInMemoryText = new JTextArea("Data will not be stored between workbench sessions. This option is intended for testing only. Only use if your workflows have a low memory requirement. Provenance information is still recorded to a database.");
        enableInMemoryText.setLineWrap(true);
        enableInMemoryText.setWrapStyleWord(true);
        enableInMemoryText.setEditable(false);
        enableInMemoryText.setOpaque(false);
        enableInMemoryText.setFont(enableProvenanceText.getFont().deriveFont(Font.PLAIN, 10));       

        JTextArea storageText = new JTextArea(
                "Select how Taverna stores the data and provenance produced when a workflow is run. This includes workflow results and intermediate results.");
        storageText.setLineWrap(true);
        storageText.setWrapStyleWord(true);
        storageText.setEditable(false);
        storageText.setBorder(new EmptyBorder(10, 10, 10, 10));

        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, 10, 0);
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.weightx = 1d;
        c.weighty = 0d;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(storageText, c);
        add(storageText);

        c.ipady = 0;
        c.insets = new Insets(0, 0, 5, 0);
        gridbag.setConstraints(enableProvenance, c);
        add(enableProvenance);

        c.insets = new Insets(0, 20, 15, 20);
        gridbag.setConstraints(enableProvenanceText, c);
        add(enableProvenanceText);

        c.insets = new Insets(0, 0, 5, 0);
        gridbag.setConstraints(enableInMemory, c);
        add(enableInMemory);

        c.insets = new Insets(0, 20, 15, 20);
        gridbag.setConstraints(enableInMemoryText, c);
        add(enableInMemoryText);

        JPanel buttonPanel = createButtonPanel();
        c.insets = new Insets(0, 0, 5, 0);
        gridbag.setConstraints(buttonPanel, c);
        add(buttonPanel);

        resetFields(configuration);

    }

    // for testing only
    public static void main(String[] args) {
        JDialog dialog = new JDialog();
        dialog.add(new DataManagementConfigurationPanel());
        dialog.setModal(true);
        dialog.setSize(500, 300);
        dialog.setVisible(true);
        System.exit(0);
    }

    private void resetFields(DataManagementConfiguration instance) {
        System.out.println("IN MEM="+configuration.getProperty(DataManagementConfiguration.IN_MEMORY));
        enableInMemory.setSelected(configuration.getProperty(DataManagementConfiguration.IN_MEMORY).equalsIgnoreCase("true"));
        enableProvenance.setSelected(configuration.getProperty(DataManagementConfiguration.ENABLE_PROVENANCE).equalsIgnoreCase("true"));
    }

    private void applySettings() {
        configuration.setProperty(DataManagementConfiguration.ENABLE_PROVENANCE, String.valueOf(enableProvenance.isSelected()));
        configuration.setProperty(DataManagementConfiguration.IN_MEMORY, String.valueOf(enableInMemory.isSelected()));
    }

    private JPanel createButtonPanel() {
        final JPanel panel = new JPanel();

        /**
         * The helpButton shows help about the current component
         */
        JButton helpButton = new JButton(new AbstractAction("Help") {

            public void actionPerformed(ActionEvent arg0) {
                Helper.showHelp(panel);
            }
        });
        panel.add(helpButton);

        /**
         * The resetButton changes the property values shown to those
         * corresponding to the configuration currently applied.
         */
        JButton resetButton = new JButton(new AbstractAction("Reset") {

            public void actionPerformed(ActionEvent arg0) {
                resetFields(configuration);
            }
        });
        panel.add(resetButton);

        /**
         * The applyButton applies the shown field values to the
         * {@link HttpProxyConfiguration} and saves them for future.
         */
        JButton applyButton = new JButton(new AbstractAction("Apply") {

            public void actionPerformed(ActionEvent arg0) {
                applySettings();
                resetFields(configuration);
            }
        });
        panel.add(applyButton);

        return panel;
    }
}
