package org.apache.taverna.workbench.views.results.saveactions;

import static org.apache.taverna.workbench.icons.WorkbenchIcons.xmlNodeIcon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import javax.swing.AbstractAction;

import org.apache.taverna.results.BaclavaDocumentPathHandler;

/**
 * Stores the entire map of result objects to disk as a single XML data
 * document. For the most part, this class delegates to
 * {@link BaclavaDocumentPathHandler}
 * 
 * @author Tom Oinn
 * @author Alex Nenadic
 * @author Stuart Owen
 * @author David Withers
 */
public class SaveAllResultsAsXML extends SaveAllResultsSPI {
	private static final long serialVersionUID = 452360182978773176L;

	private BaclavaDocumentPathHandler baclavaDocumentHandler = new BaclavaDocumentPathHandler();

	public SaveAllResultsAsXML() {
		super();
		putValue(NAME, "Save in single XML document");
		putValue(SMALL_ICON, xmlNodeIcon);
	}

	@Override
	public AbstractAction getAction() {
		return new SaveAllResultsAsXML();
	}

	/**
	 * Saves the result data to an XML Baclava file.
	 * 
	 * @throws IOException
	 */
	@Override
	protected void saveData(File file) throws IOException {
		baclavaDocumentHandler.saveData(file);
	}

	@Override
	public void setChosenReferences(Map<String, Path> chosenReferences) {
		super.setChosenReferences(chosenReferences);
		baclavaDocumentHandler.setChosenReferences(chosenReferences);
	}

	@Override
	protected String getFilter() {
		return "xml";
	}
}
