package net.sf.taverna.t2.workbench.file.impl.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.events.AbstractDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.OpenedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SavedDataflowEvent;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.xml.AbstractXMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.AbstractXMLSerializer;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class FileOpenRecentMenuAction extends AbstractMenuCustom implements
		Observer<FileManagerEvent> {

	public static final URI RECENT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpenRecent");

	private static final String CONF = "conf";

	private static FileManager fileManager = FileManager.getInstance();

	private static Logger logger = Logger
			.getLogger(FileOpenRecentMenuAction.class);

	private static final String RECENT_WORKFLOWS_XML = "recentWorkflows.xml";

	private final int MAX_ITEMS = 10;

	private JMenu menu;

	private List<Recent> recents = new ArrayList<Recent>();

	public FileOpenRecentMenuAction() {
		super(FileOpenMenuSection.FILE_OPEN_SECTION_URI, 30, RECENT_URI);
		fileManager.addObserver(this);
	}

	public void notify(Observable<FileManagerEvent> sender,
			FileManagerEvent message) throws Exception {
		FileManager fileManager = (FileManager) sender;
		if ((message instanceof OpenedDataflowEvent)
				|| (message instanceof SavedDataflowEvent)) {
			AbstractDataflowEvent dataflowEvent = (AbstractDataflowEvent) message;
			Dataflow dataflow = dataflowEvent.getDataflow();
			Object dataflowSource = fileManager.getDataflowSource(dataflow);
			FileType dataflowType = fileManager.getDataflowType(dataflow);
			addRecent(dataflowSource, dataflowType);
		}
		if (message instanceof ClosedDataflowEvent) {
			// Make sure enabled/disabled status is correct
			updateRecentMenu();
		}
	}

	public void updateRecentMenu() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateRecentMenuGUI();
			}
		});

		saveRecent();
	}

	protected void addRecent(Object dataflowSource, FileType dataflowType) {
		if (dataflowSource == null) {
			return;
		}
		if (!(dataflowSource instanceof Serializable)) {
			logger.warn("Can't serialize dataflow source for 'Recent workflows': "
							+ dataflowSource);
			return;
		}
		synchronized (recents) {			
			Recent recent = new Recent((Serializable) dataflowSource, dataflowType);
			if (recents.contains(recent)) {
				recents.remove(recent);
			}
			recents.add(0, recent); // Add to front
		}
		updateRecentMenu();
	}

	@Override
	protected Component createCustomComponent() {
		action = new DummyAction("Recent workflows");
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		menu = new JMenu(action);
		loadRecent();
		updateRecentMenu();
		return menu;
	}

	protected synchronized void loadRecent() {
		File confDir = new File(ApplicationRuntime.getInstance()
				.getApplicationHomeDir(), CONF);
		confDir.mkdir();
		File recentFile = new File(confDir, RECENT_WORKFLOWS_XML);
		if (!recentFile.isFile()) {
			return;
		}
		SAXBuilder builder = new SAXBuilder();
		Document document;
		try {
			InputStream fileInputStream;
			fileInputStream = new BufferedInputStream(new FileInputStream(
					recentFile));
			document = builder.build(fileInputStream);
		} catch (JDOMException e) {
			logger.warn("Could not read recent workflows from file "
					+ recentFile, e);
			return;
		} catch (IOException e) {
			logger.warn("Could not read recent workflows from file "
					+ recentFile, e);
			return;
		}
		synchronized (recents) {
			recents.clear();
			RecentDeserializer deserialiser = new RecentDeserializer();
			try { 
				recents.addAll(deserialiser.deserializeRecent(document
						.getRootElement()));
			} catch (Exception e) {
				logger.warn("Could not read recent workflows from file "
						+ recentFile, e);
				return;
			}
		}
	}

	protected synchronized void saveRecent() {
		File confDir = new File(ApplicationRuntime.getInstance()
				.getApplicationHomeDir(), CONF);
		confDir.mkdir();
		File recentFile = new File(confDir, RECENT_WORKFLOWS_XML);

		RecentSerializer serializer = new RecentSerializer();
		XMLOutputter outputter = new XMLOutputter();

		OutputStream outputStream = null;
		try {
			Element serializedRecent;
			synchronized (recents) {
				if (recents.size() > MAX_ITEMS) {
					// Remove excess entries
					recents.subList(MAX_ITEMS, recents.size()).clear();
				}
				serializedRecent = serializer.serializeRecent(recents);				
			}
			outputStream = new BufferedOutputStream(new FileOutputStream(
					recentFile));
			outputter.output(serializedRecent, outputStream);
		} catch (JDOMException e) {
			logger.warn("Could not generate XML for recent workflows to file "
					+ recentFile, e);
			return;
		} catch (IOException e) {
			logger.warn("Could not write recent workflows to file "
					+ recentFile, e);
			return;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.warn(
							"Could not close file writing recent workflows to file "
									+ recentFile, e);
				}
			}
		}
	}

	protected void updateRecentMenuGUI() {
		int items = 0;
		menu.removeAll();
		synchronized (recents) {
			for (Recent recent : recents) {
				if (++items >= MAX_ITEMS) {
					break;
				}				
				OpenRecentAction openRecentAction = new OpenRecentAction(recent);
				if (fileManager.getDataflowBySource(recent.getDataflowSource()) != null) {
					openRecentAction.setEnabled(false);
				} // else setEnabled(true)
				JMenuItem menuItem = new JMenuItem(openRecentAction);
				if (items < 10) {
					openRecentAction.putValue(Action.NAME, items + " " + openRecentAction.getValue(Action.NAME));
					menuItem.setMnemonic(KeyEvent.VK_0 + items);
				}
				menu.add(menuItem);
			}
		}
		menu.setEnabled(items > 0);
		menu.revalidate();
	}

	public static class OpenRecentAction extends AbstractAction implements
			Runnable {
		private final Recent recent;
		private Component component = null;

		public OpenRecentAction(Recent recent) {
			this.recent = recent;
			Serializable source = recent.getDataflowSource();
			String name;
			if (source instanceof File) {
				name = ((File) source).getAbsolutePath();
			} else {
				name = source.toString();
			}
			this.putValue(NAME, name);
			this.putValue(SHORT_DESCRIPTION, "Open the workflow " + name);
		}

		public void actionPerformed(ActionEvent e) {
			component = null;
			if (e.getSource() instanceof Component) {
				component = (Component) e.getSource();
			}
			setEnabled(false);
			new Thread(this, "Opening workflow from "
					+ recent.getDataflowSource()).start();
		}

		/**
		 * Opening workflow in separate thread
		 */
		public void run() {
			final Serializable source = recent.getDataflowSource();
			try {
				fileManager.openDataflow(recent.makefileType(), source);
			} catch (OpenException ex) {
				logger.warn("Failed to open the workflow from  " + source
						+ " \n", ex);
				JOptionPane.showMessageDialog(component,
						"Failed to open the workflow from url " + source
								+ " \n" + ex.getMessage(), "Error!",
						JOptionPane.ERROR_MESSAGE);
			} finally {
				setEnabled(true);
			}
		}
	}

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
		private String extension;

		public Recent() {
		}

		public FileType makefileType() {
			if (mimeType == null && extension == null) {
				return null;
			}
			return new RecentFileType();
		}
		
		public Recent(Serializable dataflowSource, FileType dataflowType) {
			this.setDataflowSource(dataflowSource);
			if (dataflowType != null) {
				this.setMimeType(dataflowType.getMimeType());
				this.setExtension(dataflowType.getExtension());
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
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Recent)) {
				return false;
			}
			Recent other = (Recent) obj;
			if (dataflowSource == null) {
				if (other.dataflowSource != null) {
					return false;
				}
			} else if (!dataflowSource.equals(other.dataflowSource)) {
				return false;
			}
			if (extension == null) {
				if (other.extension != null) {
					return false;
				}
			} else if (!extension.equals(other.extension)) {
				return false;
			}
			if (mimeType == null) {
				if (other.mimeType != null) {
					return false;
				}
			} else if (!mimeType.equals(other.mimeType)) {
				return false;
			}
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

	protected static class RecentDeserializer extends AbstractXMLDeserializer {
		public Collection<Recent> deserializeRecent(Element el) {
			return (Collection<Recent>) super.createBean(el, getClass()
					.getClassLoader());
		}
	}

	protected static class RecentSerializer extends AbstractXMLSerializer {
		public Element serializeRecent(List<Recent> x) throws JDOMException,
				IOException {
			Element beanAsElement = super.beanAsElement(x);
			return beanAsElement;
		}
	}

}
