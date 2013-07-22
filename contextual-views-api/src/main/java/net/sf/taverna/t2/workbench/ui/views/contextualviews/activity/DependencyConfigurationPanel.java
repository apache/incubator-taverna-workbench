/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Component for configuring activities that require dependencies.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class DependencyConfigurationPanel extends JPanel {

	private String classLoaderSharing;
	private List<String> localDependencies;
	private File libDir;

	public DependencyConfigurationPanel(String classLoaderSharing, List<String> localDependencies, File libDir) {
		this.classLoaderSharing = classLoaderSharing;
		this.localDependencies = localDependencies;
		this.libDir = libDir;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		// Create panel with classloading options
		JPanel classloadingPanel = new ClassloadingPanel();
		// Create panel for selecting jar files
		JPanel jarFilesPanel = new JarFilesPanel();

		add(classloadingPanel);
		add(Box.createRigidArea(new Dimension(0,10)));
		add(jarFilesPanel);
		add(Box.createRigidArea(new Dimension(0,10)));

	}

	public String getClassLoaderSharing() {
		return classLoaderSharing;
	}

	public List<String> getLocalDependencies() {
		return localDependencies;
	}

	// Panel containing classloading options
	private class ClassloadingPanel extends JPanel {

		// Classloading option 'workflow'
		private static final String WORKFLOW = "Shared for whole workflow";
		// Classloading option 'system'
		private static final String SYSTEM = "System classloader";

		// Combobox with classloading options
		private JComboBox<String> jcbClassloadingOption;
		// Classloading option descriptions
		private HashMap<String, String> classloadingDescriptions;
		// JLabel with classloading option description
		private JLabel jlClassloadingDescription;

		// Panel containing a list of possible classloading options which users can select from
		private ClassloadingPanel(){
			super(new GridBagLayout());
			jcbClassloadingOption = new JComboBox<>(new String[] {WORKFLOW,SYSTEM});
			// Set the current classlaoding option based on the configuration bean
			if ("workflow".equals(classLoaderSharing)) {
				jcbClassloadingOption.setSelectedItem(WORKFLOW);
			}
			else if ("system".equals(classLoaderSharing)) {
				jcbClassloadingOption.setSelectedItem(SYSTEM);
			}

			jcbClassloadingOption.addActionListener(new ActionListener(){
				// Fires up when combobox selection changes
				public void actionPerformed(ActionEvent e) {
					Object selectedItem = jcbClassloadingOption.getSelectedItem();
					jlClassloadingDescription.setText(classloadingDescriptions
							.get(selectedItem));
					if (selectedItem.equals(WORKFLOW)) {
						classLoaderSharing = "workflow";
					}
					else if (selectedItem.equals(SYSTEM)) {
						classLoaderSharing = "system";
					}
				}
			});
			//jcbClassloadingOption.setEnabled(false);

			classloadingDescriptions = new HashMap<String, String>();
			classloadingDescriptions.put(WORKFLOW, "<html><small>"
					+ "Classes are shared across the whole workflow (with any service<br>"
					+ "also selecting this option), but are reinitialised for each workflow run.<br>"
					+ "This might be needed if a service passes objects to another, or <br>"
					+ "state is shared within static members of loaded classes."
					+ "</small></html>");
			classloadingDescriptions.put(SYSTEM, "<html><small><p>"
					+ "The (global) system classloader is used, any dependencies defined here are<br>"
					+ "made available globally on the first run. Note that if you are NOT using<br>"
					+ "the defaulf Taverna BootstrapClassLoader, any settings here will be disregarded."
					+ "</p><p>"
					+ "This is mainly useful if you are using JNI-based libraries. Note that <br>"
					+ "for JNI you also have to specify <code>-Djava.library.path</code> and <br>"
					+ "probably your operating system's dynamic library search path<br>"
					+ "<code>LD_LIBRARY_PATH</code> / <code>DYLD_LIBRARY_PATH</code> / <code>PATH</code> </p></small></html>");

			// Set the current classlaoding description based on the item selected in the combobox
			jlClassloadingDescription = new JLabel(classloadingDescriptions
					.get(jcbClassloadingOption.getSelectedItem()));

			// Add components to the ClassloadingPanel
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.insets = new Insets(10,0,0,0);
			add(new JLabel("Classloader persistence"), c);
			c.insets = new Insets(0,0,0,0);
			add(jcbClassloadingOption, c);
			c.insets = new Insets(0,30,0,0);
			add(jlClassloadingDescription, c);
		}
	}

	// Panel for users to add local JAR dependencies (contains a list of jar files which users can select from)
	private class JarFilesPanel extends JPanel {
		private JLabel warning =
			new JLabel(
				"<html><center<font color='red'>"
					+ "Warning: Depending on local libraries makes this workflow<br>"
					+ "difficult or impossible to run for other users. Try depending<br>"
					+ "on artifacts from a public repository if possible.</font></center></html>");

		private JarFilesPanel() {
			super();
			setMinimumSize(new Dimension(400, 150));
			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(0,10,0,10));

			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
			JLabel label = new JLabel("Local JAR files");
			JLabel libLabel = new JLabel("<html><small>" + libDir.getAbsolutePath()
					+ "</small></html>");
			labelPanel.add(label);
			labelPanel.add(libLabel);

			add(labelPanel, BorderLayout.NORTH);
			add(new JScrollPane(jarFiles(),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

			warning.setVisible(false);
			// We'll skip the warning until we actually have support
			// for artifacts
			//add(warning);
			updateWarning();
		}

		private void updateWarning() {
			// Show warning if there is any local dependencies
			warning.setVisible(!localDependencies.isEmpty());
		}

		public JPanel jarFiles() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			// List of all jar files in the lib directory
			List<String> jarFiles =
				Arrays.asList(libDir.list(new FileExtFilter(".jar")));
			// We also add the list of jars that may have been configured sometime before
			// but are now not present in the lib directory for some reason
			Set<String> missingLocalDeps =
				new HashSet<String>(localDependencies);
			missingLocalDeps.removeAll(jarFiles);
			// jarFiles and missingLocalDeps now contain two sets of files that do not intersect
			List<String> jarFilesList = new ArrayList<String>();
			// Put them all together
			jarFilesList.addAll(jarFiles);
			jarFilesList.addAll(missingLocalDeps);
			Collections.sort(jarFilesList);

			if (jarFilesList.isEmpty()) {
				panel.add(new JLabel("<html><small>To depend on a JAR file, "
					+ "copy it to the above-mentioned folder.</small></html>"));
				return panel;
			}

			for (String jarFile : jarFilesList) {
				JCheckBox checkBox = new JCheckBox(jarFile);
				// Has it already been selected in some previous configuring?
				checkBox.setSelected(localDependencies.contains(jarFile));
				checkBox.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						JCheckBox box = (JCheckBox) e.getSource();
						if (e.getStateChange() == ItemEvent.SELECTED) {
							localDependencies.add(box.getText());
						} else if (e.getStateChange() == ItemEvent.DESELECTED) {
							localDependencies.remove(box.getText());
						}
						updateWarning();
					}
				});
				panel.add(checkBox);
				// The jar may not be in the lib directory, so warn the user
				if (!new File(libDir, jarFile).exists()) {
					checkBox.setForeground(Color.RED);
					checkBox.setText(checkBox.getText() + " (missing file!)");
				}
			}
			return panel;
		}
	}

	public static class FileExtFilter implements FilenameFilter {

		String ext = null;

		public FileExtFilter(String ext) {
			this.ext = ext;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(ext);
		}
	}

}
