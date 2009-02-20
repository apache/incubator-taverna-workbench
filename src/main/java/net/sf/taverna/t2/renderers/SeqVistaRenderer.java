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

import javax.swing.JComponent;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import cht.svista.SeqVISTA;

/**
 * Uses the SeqVista renderer to draw an EMBL or SwissProt sequence
 * 
 * @author Ian Dunlop
 * @author anonymous from T1
 */
public class SeqVistaRenderer implements Renderer {

	private String seqType = "fasta";
	// 0 = auto, 1 = nucleotide, 2 = protein
	private int np = 0;

	public SeqVistaRenderer() {
	}

	public boolean canHandle(String mimeType) {
		if (mimeType.matches(".*chemical/x-swissprot.*")) {
			seqType = "embl";
			np = 2;
			return true;
		} else if (mimeType.matches(".*chemical/x-embl-dl-nucleotide.*")) {
			seqType = "embl";
			np = 1;
			return true;
		} else if (mimeType.matches(".*chemical/x-fasta.*")) {
			seqType = "fasta";
			np = 0;
			return true;
		} else if (mimeType.matches(".*chemical/x-ppd.*")) {
			seqType = "ppd";
			return true;
		} else if (mimeType.matches(".*chemical/seq-na-genbank.*")) {
			seqType = "auto";
			np = 1;
			return true;
		} else if (mimeType.matches(".*chemical/seq-aa-genpept.*")) {
			seqType = "auto";
			np = 2;
			return true;
		}
		return false;
	}

	public String getType() {
		return "Seq Vista";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		String resolve = null;
		try {
			resolve = (String) referenceService.renderIdentifier(reference,
					String.class, null);
		} catch (Exception e) {
			throw new RendererException("Could not resolve " + reference, e);
		}
		SeqVISTA vista = new SeqVISTA() {
			@Override
			public java.awt.Dimension getPreferredSize() {
				return new java.awt.Dimension(100, 100);
			}
		};
		try {
			vista.loadFromText(resolve, false, seqType, np);
		} catch (Exception e) {
			throw new RendererException(
					"Could not create Seq Vista renderer for " + reference, e);
		}
		return vista;
	}

}
