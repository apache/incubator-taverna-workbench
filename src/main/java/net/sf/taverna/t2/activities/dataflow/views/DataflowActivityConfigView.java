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
package net.sf.taverna.t2.activities.dataflow.views;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.DataflowXMLDeserializer;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class DataflowActivityConfigView extends JPanel {

	private static Logger logger = Logger
			.getLogger(DataflowActivityConfigView.class);

	private FileManager fileManager = FileManager.getInstance();

	private DataflowActivity dataflowActivity;

	private boolean configChanged = false;

	private ActionListener actionListener;

	private File selectedFile;

	private Processor createProcessor;

	public boolean isConfigChanged() {
		return configChanged;
	}

	public void setConfigChanged(boolean configChanged) {
		this.configChanged = configChanged;
	}

	public DataflowActivityConfigView(DataflowActivity dataflow) {
		this.dataflowActivity = dataflow;
		init();
	}

	private void init() {
		setLayout(new BorderLayout());
		JLabel dataflowSource = new JLabel(dataflowActivity.getConfiguration()
				.getLocalName());
		add(dataflowSource, BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel();
		JButton OKButton = new JButton("OK");
		OKButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				// config the activity with the dataflow and tell the
				// DataflowActivityConigurationAction that something happened
				if (isConfigChanged()) {
					// dataflow has been loaded so reconfig the activity and add
					// to the current dataflow
					Dataflow dataflow = deserialiseSelectedDataflow();
					try {
						dataflowActivity.configure(dataflow);
					} catch (ActivityConfigurationException e1) {
						
						logger.error("Unable to configure activity", e1);
					}
					addSelectedDataflowToCurrentDataflow(dataflow);
					// observe the file manager in case this dataflow gets
					// altered
					
					EditManager.getInstance().addObserver(new Observer<EditManagerEvent>(){

						public void notify(Observable<EditManagerEvent> sender,
								EditManagerEvent message) throws Exception {
							if (message instanceof AbstractDataflowEditEvent) {
								AbstractDataflowEditEvent dataflowEdit = (AbstractDataflowEditEvent) message;
								
								if (dataflowEdit.getDataFlow().equals(dataflowActivity.getConfiguration())) {
									// Reconfigure in case ports have changed
									dataflowActivity.configure(dataflowEdit.getDataFlow());
									EditsRegistry.getEdits()
									.getMapProcessorPortsForActivityEdit(
											createProcessor);
								}
							}
							
						}});
					FileManager.getInstance().openDataflow(dataflow);
					
				}
				actionListener.actionPerformed(e);
			}

		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				setConfigChanged(false);
				actionListener.actionPerformed(e);
			}

		});

		JButton loadDataflowButton = new JButton("Load");
		loadDataflowButton.addActionListener(new AbstractAction() {
			/**
			 * Configure the DataflowActivity with the dataflow and add the
			 * dataflow to the current workflow
			 */
			public void actionPerformed(ActionEvent e) {
				if (selectDataflow()) {
					setConfigChanged(true);
				}

			}

		});

		buttonPanel.add(OKButton);
		buttonPanel.add(loadDataflowButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public void setButtonClickedListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	/**
	 * Get the user to select a t2flow (ie a serialised T2 dataflow) to add as a
	 * nested dataflow
	 * 
	 * @return
	 */
	private boolean selectDataflow() {
		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle("Select dataflow.....");

		fileChooser.resetChoosableFileFilters();

		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".t2flow");
			}

			@Override
			public String getDescription() {
				return "T2 dataflow";
			}

		};

		fileChooser.addChoosableFileFilter(filter);

		fileChooser.setFileFilter(filter);

		fileChooser.setCurrentDirectory(new File(curDir));
		fileChooser.setMultiSelectionEnabled(false);

		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fileChooser.getCurrentDirectory()
					.toString());
			selectedFile = fileChooser.getSelectedFile();
			return true;
		}
		return false;

	}

	/**
	 * Use the T2 deserialiser to turn the {@link #selectedFile} dataflow File
	 * into a T2 dataflow object
	 * 
	 * @return
	 */
	private Dataflow deserialiseSelectedDataflow() {
		DataflowXMLDeserializer deserializer = DataflowXMLDeserializer
				.getInstance();
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(selectedFile);
		} catch (FileNotFoundException e) {
			logger.error("Unable to deserialize nested workflow", e);
		}
		SAXBuilder builder = new SAXBuilder();
		Element detachRootElement = null;
		try {
			detachRootElement = builder.build(inputStream).detachRootElement();
		} catch (JDOMException e) {
			logger.error("", e);
		} catch (IOException e) {
			logger.error("", e);
		}
		try {
			return deserializer.deserializeDataflow(detachRootElement,
					new HashMap<String, Element>());
		} catch (EditException e) {
			logger.error("Could not deserialize dataflow", e);
		} catch (DeserializationException e) {
			logger.error("Could not deserialize dataflow", e);
		} catch (ActivityConfigurationException e) {
			logger.error("Could not deserialize dataflow", e);
		} catch (ClassNotFoundException e) {
			logger.error("Could not deserialize dataflow", e);
		} catch (InstantiationException e) {
			logger.error("Could not deserialize dataflow", e);
		} catch (IllegalAccessException e) {
			logger.error("Could not deserialize dataflow", e);
		}
		return null;
	}

	/**
	 * Adds the selected dataflow to the currently opened one. Create a
	 * processor with the same name as the nested dataflow (ie. the one just
	 * opened). Add the configured dataflow activity to this processor. Then use
	 * the {@link EditManager} to add the processor to the main dataflow so that
	 * any GUI updates are forced
	 * 
	 * @param dataflow
	 */
	private void addSelectedDataflowToCurrentDataflow(Dataflow dataflow) {
		createProcessor = EditsRegistry.getEdits().createProcessor(
				dataflow.getLocalName());
		try {
			EditsRegistry.getEdits().getAddActivityEdit(createProcessor,
					dataflowActivity).doEdit();
			EditManager.getInstance().doDataflowEdit(
					(Dataflow) ModelMap.getInstance().getModel(
							ModelMapConstants.CURRENT_DATAFLOW),
					EditsRegistry.getEdits()
							.getMapProcessorPortsForActivityEdit(
									createProcessor));

		} catch (EditException e) {
			logger.error("Could not add nested workflow", e);
		}

	}

}
