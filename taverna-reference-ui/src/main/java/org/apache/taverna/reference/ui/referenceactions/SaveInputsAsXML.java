/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.reference.ui.referenceactions;

import static java.lang.System.getProperty;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.xmlNodeIcon;
import static org.jdom.Namespace.getNamespace;
import static org.jdom.output.Format.getPrettyFormat;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.taverna.invocation.InvocationContext;
import org.apache.taverna.lang.ui.ExtensionFileFilter;
import org.apache.taverna.reference.ui.RegistrationPanel;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

/**
 * Stores the entire map of result objects to disk
 * as a single XML data document.
 *
 * @author Tom Oinn
 * @author Alex Nenadic
 */
public class SaveInputsAsXML extends AbstractAction implements ReferenceActionSPI {
	private static final long serialVersionUID = 452360182978773176L;
	private static final String INPUT_DATA_DIR_PROPERTY = "inputDataValuesDir";
	private static final Logger logger = Logger
			.getLogger(SaveInputsAsXML.class);
	public static final String BACLAVA_NAMESPACE = "http://org.embl.ebi.escience/baclava/0.1alpha";
	/** {@value #BACLAVA_NAMESPACE} */
	private static final Namespace namespace = getNamespace("b",
			BACLAVA_NAMESPACE);

	@SuppressWarnings("unused")
	private InvocationContext context = null;

	private Map<String, RegistrationPanel> inputPanelMap;

	public SaveInputsAsXML(){
		super();
		putValue(NAME, "Save values");
		putValue(SMALL_ICON, xmlNodeIcon);
	}

	@Override
	public AbstractAction getAction() {
		return new SaveInputsAsXML();
	}

	// Must be called before actionPerformed()
	public void setInvocationContext(InvocationContext context) {
		this.context = context;
	}

	/**
	 * Shows a standard save dialog and dumps the entire input set to the
	 * specified XML file.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs.get(INPUT_DATA_DIR_PROPERTY, getProperty("user.home"));

		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Select file to save input values to");

		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
		fc.setCurrentDirectory(new File(curDir));
		fc.setFileSelectionMode(FILES_ONLY);

		File file;
		do {
			if (fc.showSaveDialog(null) != APPROVE_OPTION)
				return;
			prefs.put(INPUT_DATA_DIR_PROPERTY, fc.getCurrentDirectory().toString());
			file = fc.getSelectedFile();

			/*
			 * If the user did not use the .xml extension for the file - append
			 * it to the file name now
			 */
			if (!file.getName().toLowerCase().endsWith(".xml"))
				file = new File(file.getParentFile(), file.getName() + ".xml");

			// If the file exists, ask the user if they want to overwrite the file
		} while (file.exists()
				&& showConfirmDialog(null, file.getAbsolutePath()
						+ " already exists. Do you want to overwrite it?",
						"File already exists", YES_NO_OPTION) != YES_OPTION);
		doSave(file);
	}

	private void doSave(final File file) {
		// Do this in separate thread to avoid hanging UI
		new Thread("Save(InputsAsXML: Saving inputs to " + file) {
			@Override
			public void run() {
				try {
					synchronized (inputPanelMap) {
						saveData(file);
					}
				} catch (Exception ex) {
					showMessageDialog(null, "Problem saving input data",
							"Save Inputs Error", ERROR_MESSAGE);
					logger.error("Problem saving input data as XML", ex);
				}
			}
		}.start();
	}

	/**
	 * Saves the input data to an XML Baclava file.
	 */
	private void saveData(File file) throws Exception {
		// Build the DataThing map from the inputPanelMap
		Map<String, Object> valueMap = new HashMap<>();
		for (String portName : inputPanelMap.keySet()) {
			RegistrationPanel panel = inputPanelMap.get(portName);
			Object obj = panel.getValue();
			if (obj != null)
				valueMap.put(portName, obj);
		}
		Map<String, DataThing> dataThings = bakeDataThingMap(valueMap);

		// Build the string containing the XML document from the panel map
		String xmlString = new XMLOutputter(getPrettyFormat())
				.outputString(getDataDocument(dataThings));
		try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
			out.print(xmlString);
		}
	}

	/**
	 * Returns a map of port names to DataThings from a map of port names to a
	 * list of (lists of ...) result objects.
	 */
	Map<String, DataThing> bakeDataThingMap(Map<String, Object> resultMap) {
		Map<String, DataThing> dataThingMap = new HashMap<>();
		for (String portName : resultMap.keySet())
			dataThingMap.put(portName, bake(resultMap.get(portName)));
		return dataThingMap;
	}

	/**
	 * Returns a org.jdom.Document from a map of port named to DataThingS containing
	 * the port's results.
	 */
	public static Document getDataDocument(Map<String, DataThing> dataThings) {
		Element rootElement = new Element("dataThingMap", namespace);
		Document theDocument = new Document(rootElement);
		for (String key : dataThings.keySet()) {
			DataThing value = (DataThing) dataThings.get(key);
			Element dataThingElement = new Element("dataThing", namespace);
			dataThingElement.setAttribute("key", key);
			dataThingElement.addContent(value.getElement());
			rootElement.addContent(dataThingElement);
		}
		return theDocument;
	}

	@Override
	public void setInputPanelMap(Map<String, RegistrationPanel> inputPanelMap) {
		this.inputPanelMap = inputPanelMap;
	}
}
