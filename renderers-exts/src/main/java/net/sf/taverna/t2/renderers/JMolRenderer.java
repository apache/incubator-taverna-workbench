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
package net.sf.taverna.t2.renderers;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

import org.apache.log4j.Logger;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;
import org.jmol.api.JmolViewer;
import org.jmol.viewer.Viewer;

/**
 * Renders using the Jmol software for chemical structures
 * 
 * @author Tom Oinn
 * @author Ian Dunlop
 * @author Alex Nenadic
 */
public class JMolRenderer implements Renderer {

	private Logger logger = Logger.getLogger(JMolRenderer.class);

	private int MEGABYTE = 1024 * 1024;
	
	public JMolRenderer() {
	}

	public boolean isTerminal() {
		return true;
	}

	// public boolean canHandle(DataFacade facade,
	// EntityIdentifier entityIdentifier, String mimeType)
	// throws RendererException {
	// // Object resolve = null;
	// // try {
	// // resolve = facade.resolve(entityIdentifier, String.class);
	// // } catch (RetrievalException e) {
	// // throw new RendererException(
	// // "Could not resolve " + entityIdentifier + " (probably is not a JMOL
	// file");
	// // } catch (NotFoundException e) {
	// // throw new RendererException("Data Manager Could not find "
	// // + entityIdentifier, e);
	// // }
	// // if (resolve instanceof String) {
	// // return canHandle(mimeType);
	// // }
	//
	// return canHandle(mimeType);
	// }

	public boolean canHandle(String mimeType) {
		if (mimeType.matches(".*chemical/x-pdb.*")
				|| mimeType.matches(".*chemical/x-mdl-molfile.*")
				|| mimeType.matches(".*chemical/x-cml.*")) {
			return true;
		}

		return false;
	}

	static final String proteinScriptString = "wireframe off; spacefill off; select protein; cartoon; colour structure; select ligand; spacefill; colour cpk; select dna; spacefill 0.4; wireframe on; colour cpk;";

	static final String scriptString = "select *; spacefill 0.4; wireframe 0.2; colour cpk;";

	public String getType() {
		return "JMol";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		
		// Should be a ReferenceSet
		if (reference.getReferenceType() == T2ReferenceType.ReferenceSet) {
			try {

				long approximateSizeInBytes = 0;
				try {
					ReferenceSet refSet = referenceService
							.getReferenceSetService()
							.getReferenceSet(reference);
					approximateSizeInBytes = refSet.getApproximateSizeInBytes()
							.longValue();
				} catch (Exception ex) {
					logger
							.error(
									"Failed to get the size of the data from Reference Service",
									ex);
					return new JTextArea(
							"Failed to get the size of the data from Reference Service (see error log for more details): \n"
									+ ex.getMessage());
				}

				if (approximateSizeInBytes > MEGABYTE) {
					int response = JOptionPane
							.showConfirmDialog(
									null,
									"Result is approximately "
											+ bytesToMeg(approximateSizeInBytes)
											+ " MB in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
									"Render using JMol?", JOptionPane.YES_NO_OPTION);

					if (response != JOptionPane.YES_OPTION) {
						return new JTextArea(
								"Rendering cancelled due to size of data. Try saving and viewing in an external application.");
					}
				}

				String resolve = null;
				try {
					// Resolve it as a string
					resolve = (String) referenceService.renderIdentifier(
							reference, String.class, null);
				} catch (Exception e) {
					logger
							.error(
									"Reference Service failed to render data as string",
									e);
					return new JTextArea(
							"Reference Service failed to render data as string (see error log for more details): \n"
									+ e.getMessage());
				}
				JMolPanel panel = new JMolPanel();
				JmolSimpleViewer viewer = null;
				try {
					viewer = panel.getViewer();
					viewer.openStringInline(resolve);
					if (((JmolViewer) viewer).getAtomCount() > 300) {
						viewer.evalString(proteinScriptString);
					} else {
						viewer.evalString(scriptString);
					}
				} catch (Exception e) {
					logger.error("Failed to create JMol renderer", e);
					return new JTextArea("Failed to create JMol renderer (see error log for more details): \n"
							+ e.getMessage());
				}
				return panel;
			} catch (Exception e) {
				logger.error("Failed to create JMol renderer", e);
				return new JTextArea("Failed to create JMol renderer: \n"
						+ e.getMessage());
			}
		}
		else{
			// Else this is not a ReferenceSet so this is not good
			logger.error("JMol Renderer: expected data as ReferenceSet but received as "
					+ reference.getReferenceType().toString());
			return new JTextArea(
			"Reference Service failed to obtain the data to render: data is not a ReferenceSet");	
		}
	}

	class JMolPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JmolSimpleViewer viewer;
		JmolAdapter adapter;

		JMolPanel() {
			adapter = new SmarterJmolAdapter(null);
			viewer = Viewer.allocateJmolSimpleViewer(this, adapter);
			// viewer = JmolSimpleViewer.allocateSimpleViewer(this, adapter);
		}

		public JmolSimpleViewer getViewer() {
			return viewer;
		}

		final Dimension currentSize = new Dimension();
		final Rectangle rectClip = new Rectangle();

		@Override
		public void paint(Graphics g) {
			getSize(currentSize);
			g.getClipBounds(rectClip);
			viewer.renderScreenImage(g, currentSize, rectClip);
		}
	}
	
	/**
	 * Work out size of file in megabytes to 1 decimal place
	 * 
	 * @param bytes
	 * @return
	 */
	private int bytesToMeg(long bytes) {
		float f = bytes / MEGABYTE;
		return Math.round(f);
	}
}
