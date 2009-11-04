package net.sf.taverna.t2.workbench.file.impl.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.AbstractAction;
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

	private Set<Recent> recents = new LinkedHashSet<Recent>();

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
			logger
					.warn("Can't serialize dataflow source for 'Recent workflows': "
							+ dataflowSource);
			return;
		}
		synchronized (recents) {
			recents
					.add(new Recent((Serializable) dataflowSource, dataflowType));
		}
		updateRecentMenu();
	}

	@Override
	protected Component createCustomComponent() {
		menu = new JMenu("Recent workflows");
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
			recents.addAll(deserialiser.deserializeRecent(document
					.getRootElement()));
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
				synchronized (recents) {
					if (recents.size() > MAX_ITEMS) {
						// Shrink if needed
						Recent[] copy = recents.toArray(new Recent[0]);
						for (int i = MAX_ITEMS - 1; i < copy.length; i++) {
							recents.remove(copy[i]);
						}
					}
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
					// Only keystrokes 1-9
					menuItem.setAccelerator(KeyStroke.getKeyStroke(String
							.valueOf(items)));
				}
				menu.add(menuItem);
			}
		}
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
				fileManager.openDataflow(recent.getDataflowType(), source);
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

		private Serializable dataflowSource;

		private FileType dataflowType;

		public Recent() {
		}

		public Recent(Serializable dataflowSource, FileType dataflowType) {
			this.setDataflowSource(dataflowSource);
			this.setDataflowType(dataflowType);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Recent other = (Recent) obj;
			if (getDataflowSource() == null) {
				if (other.getDataflowSource() != null)
					return false;
			} else if (!getDataflowSource().equals(other.getDataflowSource()))
				return false;
			if (getDataflowType() == null) {
				if (other.getDataflowType() != null)
					return false;
			} else if (!getDataflowType().equals(other.getDataflowType()))
				return false;
			return true;
		}

		public Serializable getDataflowSource() {
			return dataflowSource;
		}

		public FileType getDataflowType() {
			return dataflowType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((getDataflowSource() == null) ? 0 : getDataflowSource()
							.hashCode());
			result = prime
					* result
					+ ((getDataflowType() == null) ? 0 : getDataflowType()
							.hashCode());
			return result;
		}

		public void setDataflowSource(Serializable dataflowSource) {
			this.dataflowSource = dataflowSource;
		}

		public void setDataflowType(FileType dataflowType) {
			this.dataflowType = dataflowType;
		}

		@Override
		public String toString() {
			return getDataflowSource() + "";
		}

	}

	protected static class RecentDeserializer extends AbstractXMLDeserializer {
		public Set<Recent> deserializeRecent(Element el) {
			return (Set<Recent>) super.createBean(el, getClass()
					.getClassLoader());
		}
	}

	protected static class RecentSerializer extends AbstractXMLSerializer {
		public Element serializeRecent(Set<Recent> x) throws JDOMException,
				IOException {
			Element beanAsElement = super.beanAsElement(x);
			return beanAsElement;
		}
	}

}
