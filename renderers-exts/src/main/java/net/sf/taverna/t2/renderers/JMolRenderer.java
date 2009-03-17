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
import javax.swing.JPanel;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

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
 */
public class JMolRenderer implements Renderer {

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
		JMolPanel panel = new JMolPanel();
		String coordinateText = null;
		try {
			coordinateText = (String) referenceService.renderIdentifier(
					reference, String.class, null);
		} catch (Exception e) {
			throw new RendererException("Could not resolve " + reference, e);
		}
		JmolSimpleViewer viewer = null;
		try {
			viewer = panel.getViewer();
			viewer.openStringInline(coordinateText);
			if (((JmolViewer) viewer).getAtomCount() > 300) {
				viewer.evalString(proteinScriptString);
			} else {
				viewer.evalString(scriptString);
			}
		} catch (Exception e) {
			throw new RendererException("could not create JMOL Renderer for "
					+ reference, e);
		}
		return panel;
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
}
