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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.client.ProvenanceExporter;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;

/**
 * Saves results in Janus format.
 * 
 * @author Alex Nenadic
 * 
 */
@SuppressWarnings("serial")
public class SaveAllResultsAsJanus extends SaveAllResultsSPI {

	public SaveAllResultsAsJanus() {
		super();
		putValue(NAME, "Save as Janus (experimental)");
		//putValue(SMALL_ICON, WorkbenchIcons.janusIcon);
	}

	@Override
	public AbstractAction getAction() {
		// Action should only be enabled if provenance was enabled for this run
		// isProvenanceEnabledForRun should be set before this method is called
		this.setEnabled(isProvenanceEnabledForRun());
		return this;
	}

	@Override
	protected String getFilter() {
		return "rdf";
	}

	/**
	 * Save as XML file in the OPM format.
	 */
	@Override
	protected void saveData(File file) throws IOException {

		String connectorType = DataManagementConfiguration.getInstance()
				.getConnectorType();
		ProvenanceAccess provenanceAccess = new ProvenanceAccess(connectorType);
		ProvenanceExporter export = new ProvenanceExporter(provenanceAccess);
		BufferedOutputStream outStream = new BufferedOutputStream(
				new FileOutputStream(file));		
		try {
			export.exportAsJanusRDF(getRunId(), outStream);
		} catch (Exception e) {
			logger.error("Failed to save the Janus graph to " + file, e);
			JOptionPane.showMessageDialog(null,
					"Failed to save the Janus graph to " + file,
					"Failed to save Janus graph", JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				outStream.close();
			} catch (IOException e) {
			}
		}

	}
}
