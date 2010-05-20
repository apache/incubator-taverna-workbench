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
package net.sf.taverna.t2.workbench.views.results.saveactions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.api.ProvenanceQueryParser;
import net.sf.taverna.t2.provenance.api.QueryAnswer;
import net.sf.taverna.t2.provenance.api.Query;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * Saves results in OPM (Open Provenance Model) format.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class SaveAllResultsAsOPM extends SaveAllResultsSPI{

	private boolean isProvenanceEnabledForRun = false;
	private String runId = null;
	private Dataflow dataflow = null;

	public SaveAllResultsAsOPM(){
		super();
		putValue(NAME, "Save as OPM");
		putValue(SMALL_ICON, WorkbenchIcons.opmIcon);
	}
	
	@Override
	public AbstractAction getAction() {
		// Action should only be enabled if provenance was enabled for this run
		// isProvenanceEnabledForRun should be set before this method is called
		this.setEnabled(isProvenanceEnabledForRun);
		return this;
	}

	@Override
	protected String getFilter() {
		return "opm";
	}

	/**
	 * Save as XML file in the OPM format.
	 */
	@Override
	protected void saveData(File file) throws Exception {
		// Build the Object map from the chosenReferences
		// First convert map of references to objects into a map of real result objects
		/*Map<String, Object> resultMap = new HashMap<String, Object>();
		for (Iterator<String> i = chosenReferences.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
			T2Reference reference = chosenReferences.get(portName);
			Object obj = convertReferencesToObjects(reference);
			resultMap.put(portName, obj);
		}	*/
		
		Query query = new Query();
		QueryAnswer answer;

		String connectorType = DataManagementConfiguration.getInstance().getConnectorType();
		ProvenanceAccess provenanceAccess = new ProvenanceAccess(connectorType);
		
		String querySpec = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
		"<pquery xmlns=\"http://taverna.org.uk/2009/provenance/pquery/\"\n"+
		"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"+
		"xsi:schemaLocation=\"http://taverna.org.uk/2009/provenance/pquery/pquery.xsd\">\n"+
		"<scope workflow=\""+ dataflow.getInternalIdentier() +"\"/>\n"+
		"<focus>\n"+
		"<workflow name=\""+ runId +"\">\n"+
		"</workflow>\n"+
		"</focus>\n" +
		"</pquery>";

		logger.info("Query to get the OPM graph:\n" + querySpec);
		ProvenanceQueryParser provenanceQueryParser = new ProvenanceQueryParser();
		provenanceQueryParser.setPAccess(provenanceAccess);
		
		InputStream stream = new ByteArrayInputStream(querySpec.getBytes("UTF-8"));
		query = provenanceQueryParser.parseProvenanceQuery(stream);
		try{
			stream.close();
		}
		catch(Exception ex){
			logger.error("Failed to close the stream from which a query to get the OPM graph was read when saving results as OPM.", ex);
		}

		if (query == null) {
			logger.error("Query processing failed when saving results as OPM graph: could not parse the query.");
			JOptionPane.showMessageDialog(null, "Failed to parse the query needed to get the OPM graph", "Failed to save OPM graph", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {		
			answer = provenanceAccess.executeQuery(query);
		} catch (SQLException e) {
			logger.error("Failed while executing query when saving results as OPM graph.", e);
			JOptionPane.showMessageDialog(null, "Failed to execute the query needed to get the OPM graph", "Failed to save OPM graph", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (answer.getOPMAnswer_AsRDF() == null) {
			logger.error("Failed to generate the OPM graph when saving results as OPM graph.");
			JOptionPane.showMessageDialog(null, "Failed to generate the OPM graph for the result data", "Failed to save OPM graph", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			FileWriter fw= new FileWriter(file);
			fw.write(answer.getOPMAnswer_AsRDF());
			fw.close();
		} catch (IOException e) {
			logger.error("Failed to save the OPM graph to a file.", e);
			JOptionPane.showMessageDialog(null, "Failed to save the OPM graph to a file", "Failed to save OPM graph", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setIsProvenanceEnabledForRun(boolean isProvenanceEnabledForRun) {
		this.isProvenanceEnabledForRun = isProvenanceEnabledForRun;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}

	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

}
