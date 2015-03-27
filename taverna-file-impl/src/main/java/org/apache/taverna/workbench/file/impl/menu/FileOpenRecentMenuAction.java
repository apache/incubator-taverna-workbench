/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.file.impl.menu;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_R;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.NAME;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.taverna.workbench.file.impl.menu.FileOpenMenuSection.FILE_OPEN_SECTION_URI;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.ui.menu.AbstractMenuCustom;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.FileType;
import org.apache.taverna.workbench.file.events.AbstractDataflowEvent;
import org.apache.taverna.workbench.file.events.ClosedDataflowEvent;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.file.events.OpenedDataflowEvent;
import org.apache.taverna.workbench.file.events.SavedDataflowEvent;
import org.apache.taverna.workbench.file.exceptions.OpenException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import uk.org.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

public class FileOpenRecentMenuAction extends AbstractMenuCustom implements
		Observer<FileManagerEvent> {
	public static final URI RECENT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpenRecent");
	private static final String CONF = "conf";
	private static Logger logger = Logger
			.getLogger(FileOpenRecentMenuAction.class);
	private static final String RECENT_WORKFLOWS_XML = "recentWorkflows.xml";
	private static final int MAX_ITEMS = 10;

	private FileManager fileManager;
	private ApplicationConfiguration applicationConfiguration;
	private JMenu menu;
	private List<Recent> recents = new ArrayList<>();
	private Thread loadRecentThread;

	public FileOpenRecentMenuAction(FileManager fileManager) {
		super(FILE_OPEN_SECTION_URI, 30, RECENT_URI);
		this.fileManager = fileManager;
		fileManager.addObserver(this);
	}

	@Override
	public void notify(Observable<FileManagerEvent> sender,
			FileManagerEvent message) throws Exception {
		FileManager fileManager = (FileManager) sender;
		if (message instanceof OpenedDataflowEvent
				|| message instanceof SavedDataflowEvent) {
			AbstractDataflowEvent dataflowEvent = (AbstractDataflowEvent) message;
			WorkflowBundle dataflow = dataflowEvent.getDataflow();
			Object dataflowSource = fileManager.getDataflowSource(dataflow);
			FileType dataflowType = fileManager.getDataflowType(dataflow);
			addRecent(dataflowSource, dataflowType);
		}
		if (message instanceof ClosedDataflowEvent)
			// Make sure enabled/disabled status is correct
			updateRecentMenu();
	}

	public void updateRecentMenu() {
		invokeLater(new Runnable() {
			@Override
			public void run() {
				updateRecentMenuGUI();
			}
		});
		saveRecent();
	}

	protected void addRecent(Object dataflowSource, FileType dataflowType) {
		if (dataflowSource == null)
			return;
		if (!(dataflowSource instanceof Serializable)) {
			logger.warn("Can't serialize workflow source for 'Recent workflows': "
					+ dataflowSource);
			return;
		}
		synchronized (recents) {
			Recent recent = new Recent((Serializable) dataflowSource, dataflowType);
			if (recents.contains(recent))
				recents.remove(recent);
			recents.add(0, recent); // Add to front
		}
		updateRecentMenu();
	}

	@Override
	protected Component createCustomComponent() {
		action = new DummyAction("Recent workflows");
		action.putValue(MNEMONIC_KEY, VK_R);
		menu = new JMenu(action);
		// Disabled until we have loaded the recent workflows
		menu.setEnabled(false);
		loadRecentThread = new Thread("Loading recent workflow menu") {
			// Avoid hanging GUI initialization while deserialising
			@Override
			public void run() {
				loadRecent();
				updateRecentMenu();
			}
		};
		loadRecentThread.start();
		return menu;
	}

	protected synchronized void loadRecent() {
		File confDir = new File(applicationConfiguration.getApplicationHomeDir(), CONF);
		confDir.mkdir();
		File recentFile = new File(confDir, RECENT_WORKFLOWS_XML);
		if (!recentFile.isFile())
			return;
		try {
			loadRecent(recentFile);
		} catch (JDOMException|IOException e) {
			logger.warn("Could not read recent workflows from file "
					+ recentFile, e);
		}
	}

	private void loadRecent(File recentFile) throws FileNotFoundException,
			IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder();
		@SuppressWarnings("unused")
		Document document;
		try (InputStream fileInputStream = new BufferedInputStream(
				new FileInputStream(recentFile))) {
			document = builder.build(fileInputStream);
		}
		synchronized (recents) {
			recents.clear();
			//RecentDeserializer deserialiser = new RecentDeserializer();
			try {
				// recents.addAll(deserialiser.deserializeRecent(document
				// .getRootElement()));
			} catch (Exception e) {
				logger.warn("Could not read recent workflows from file "
						+ recentFile, e);
			}
		}
	}

	protected synchronized void saveRecent() {
		File confDir = new File(applicationConfiguration.getApplicationHomeDir(), CONF);
		confDir.mkdir();
		File recentFile = new File(confDir, RECENT_WORKFLOWS_XML);

		try {
			saveRecent(recentFile);
//		} catch (JDOMException e) {
//			logger.warn("Could not generate XML for recent workflows to file "
//					+ recentFile, e);
		} catch (IOException e) {
			logger.warn("Could not write recent workflows to file "
					+ recentFile, e);
		}
	}

	private void saveRecent(File recentFile) throws FileNotFoundException,
			IOException {
		// RecentSerializer serializer = new RecentSerializer();
		// XMLOutputter outputter = new XMLOutputter();

		// Element serializedRecent;
		synchronized (recents) {
			if (recents.size() > MAX_ITEMS)
				// Remove excess entries
				recents.subList(MAX_ITEMS, recents.size()).clear();
			// serializedRecent = serializer.serializeRecent(recents);
		}
		try (OutputStream outputStream = new BufferedOutputStream(
				new FileOutputStream(recentFile))) {
			// outputter.output(serializedRecent, outputStream);
		}
	}

	protected void updateRecentMenuGUI() {
		int items = 0;
		menu.removeAll();
		synchronized (recents) {
			for (Recent recent : recents) {
				if (++items >= MAX_ITEMS)
					break;
				OpenRecentAction openRecentAction = new OpenRecentAction(
						recent, fileManager);
				if (fileManager.getDataflowBySource(recent.getDataflowSource()) != null)
					openRecentAction.setEnabled(false);
				// else setEnabled(true)
				JMenuItem menuItem = new JMenuItem(openRecentAction);
				if (items < 10) {
					openRecentAction.putValue(NAME, items + " "
							+ openRecentAction.getValue(NAME));
					menuItem.setMnemonic(VK_0 + items);
				}
				menu.add(menuItem);
			}
		}
		menu.setEnabled(items > 0);
		menu.revalidate();
	}

	@SuppressWarnings("serial")
	public static class OpenRecentAction extends AbstractAction implements
			Runnable {
		private final Recent recent;
		private Component component = null;
		private final FileManager fileManager;

		public OpenRecentAction(Recent recent, FileManager fileManager) {
			this.recent = recent;
			this.fileManager = fileManager;
			Serializable source = recent.getDataflowSource();
			String name;
			if (source instanceof File)
				name = ((File) source).getAbsolutePath();
			else
				name = source.toString();
			this.putValue(NAME, name);
			this.putValue(SHORT_DESCRIPTION, "Open the workflow " + name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			component = null;
			if (e.getSource() instanceof Component)
				component = (Component) e.getSource();
			setEnabled(false);
			new Thread(this, "Opening workflow from "
					+ recent.getDataflowSource()).start();
		}

		/**
		 * Opening workflow in separate thread
		 */
		@Override
		public void run() {
			final Serializable source = recent.getDataflowSource();
			try {
				fileManager.openDataflow(recent.makefileType(), source);
			} catch (OpenException ex) {
				logger.warn("Failed to open the workflow from  " + source
						+ " \n", ex);
				showMessageDialog(component,
						"Failed to open the workflow from url " + source
								+ " \n" + ex.getMessage(), "Error!",
						ERROR_MESSAGE);
			} finally {
				setEnabled(true);
			}
		}
	}

	@SuppressWarnings("serial")
	public static class Recent implements Serializable {
		private final class RecentFileType extends FileType {
			@Override
			public String getMimeType() {
				return mimeType;
			}

			@Override
			public String getExtension() {
				return extension;
			}

			@Override
			public String getDescription() {
				return "File type " + extension + " " + mimeType;
			}
		}

		private Serializable dataflowSource;
		private String mimeType;
		private String extension;

		public String getMimeType() {
			return mimeType;
		}

		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}

		public String getExtension() {
			return extension;
		}

		public void setExtension(String extension) {
			this.extension = extension;
		}

		public Recent() {
		}

		public FileType makefileType() {
			if (mimeType == null && extension == null)
				return null;
			return new RecentFileType();
		}

		public Recent(Serializable dataflowSource, FileType dataflowType) {
			setDataflowSource(dataflowSource);
			if (dataflowType != null) {
				setMimeType(dataflowType.getMimeType());
				setExtension(dataflowType.getExtension());
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((dataflowSource == null) ? 0 : dataflowSource.hashCode());
			result = prime * result
					+ ((extension == null) ? 0 : extension.hashCode());
			result = prime * result
					+ ((mimeType == null) ? 0 : mimeType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Recent))
				return false;
			Recent other = (Recent) obj;

			if (dataflowSource == null) {
				if (other.dataflowSource != null)
					return false;
			} else if (!dataflowSource.equals(other.dataflowSource))
				return false;

			if (extension == null) {
				if (other.extension != null)
					return false;
			} else if (!extension.equals(other.extension))
				return false;

			if (mimeType == null) {
				if (other.mimeType != null)
					return false;
			} else if (!mimeType.equals(other.mimeType))
				return false;

			return true;
		}

		public Serializable getDataflowSource() {
			return dataflowSource;
		}

		public void setDataflowSource(Serializable dataflowSource) {
			this.dataflowSource = dataflowSource;
		}

		@Override
		public String toString() {
			return getDataflowSource() + "";
		}
	}

	// TODO find new serialization
//	protected static class RecentDeserializer extends AbstractXMLDeserializer {
//		public Collection<Recent> deserializeRecent(Element el) {
//			return (Collection<Recent>) super.createBean(el, getClass()
//					.getClassLoader());
//		}
//	}
//
//	protected static class RecentSerializer extends AbstractXMLSerializer {
//		public Element serializeRecent(List<Recent> x) throws JDOMException,
//				IOException {
//			Element beanAsElement = super.beanAsElement(x);
//			return beanAsElement;
//		}
//	}

	public void setApplicationConfiguration(
			ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}
}
