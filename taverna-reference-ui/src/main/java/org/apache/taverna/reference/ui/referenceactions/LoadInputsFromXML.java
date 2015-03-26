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
package org.apache.taverna.reference.ui.referenceactions;

import static javax.swing.JFileChooser.APPROVE_OPTION;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.xmlNodeIcon;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.taverna.lang.ui.ExtensionFileFilter;
import org.apache.taverna.reference.ui.RegistrationPanel;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * Loads a set of input values from an XML document
 */
public class LoadInputsFromXML extends AbstractAction implements
		ReferenceActionSPI {
	private static final long serialVersionUID = -5031867688853589341L;
	private static final String INPUT_DATA_DIR_PROPERTY = "inputDataValuesDir";

	private Map<String, RegistrationPanel> inputPanelMap;

	public LoadInputsFromXML() {
		super();
		putValue(NAME, "Load previous values");
		putValue(SMALL_ICON, xmlNodeIcon);
	}

	@Override
	public AbstractAction getAction() {
		return new LoadInputsFromXML();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs.get(INPUT_DATA_DIR_PROPERTY, System.getProperty("user.home"));

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select file to load input values from");

		chooser.resetChoosableFileFilters();
		chooser.setFileFilter(new ExtensionFileFilter(new String[]{"xml"}));
		chooser.setCurrentDirectory(new File(curDir));

		if (chooser.showOpenDialog(null) != APPROVE_OPTION)
			return;
		prefs.put(INPUT_DATA_DIR_PROPERTY, chooser.getCurrentDirectory()
				.toString());
		try {
			File file = chooser.getSelectedFile();
			InputStreamReader stream;
			stream = new InputStreamReader(new FileInputStream(file),
					Charset.forName("UTF-8"));
			Document inputDoc = new SAXBuilder(false).build(stream);
			Map<String, DataThing> inputMap = DataThingXMLFactory.parseDataDocument(inputDoc);
			for (String portName : inputMap.keySet()) {
				RegistrationPanel panel = inputPanelMap.get(portName);
				Object o = inputMap.get(portName).getDataObject();
				if (o != null) {
					int objectDepth = getObjectDepth(o);
					if ((panel != null) && (objectDepth <= panel.getDepth()))
						panel.setValue(o, objectDepth);
				}
			}
		} catch (Exception ex) {
			// Nothing
		}
	}

	@Override
	public void setInputPanelMap(Map<String, RegistrationPanel> inputPanelMap) {
		this.inputPanelMap = inputPanelMap;
	}

	private int getObjectDepth(Object o) {
		int result = 0;
		if (o instanceof Iterable) {
			result++;
			@SuppressWarnings("unchecked")
			Iterator<Object> i = ((Iterable<Object>) o).iterator();
			if (i.hasNext())
				result = result + getObjectDepth(i.next());
		}
		return result;
	}
}
