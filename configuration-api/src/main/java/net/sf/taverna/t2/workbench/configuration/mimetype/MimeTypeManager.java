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
package net.sf.taverna.t2.workbench.configuration.mimetype;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

public class MimeTypeManager extends AbstractConfigurable {
	
	private static MimeTypeManager instance = new MimeTypeManager();
	
	public static MimeTypeManager getInstance() {
		return instance;
	}

	public String getCategory() {
		return "Mime Type";
	}

	public Map<String, String> getDefaultPropertyMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("text/plain", "Plain Text");
		map.put("text/xml", "XML Text");
		map.put("text/html", "HTML Text");
		map.put("text/rtf", "Rich Text Format");
		map.put("text/x-graphviz", "Graphviz Dot File");
		map.put("image/png","PNG Image");
		map.put("image/jpeg","JPEG Image");
		map.put("image/gif","GIF Image");
		map.put("application/octet-stream","Binary Data");
		map.put("application/zip","Zip File");
		map.put("chemical/x-swissprot","SWISSPROT Flat File");
		map.put("chemical/x-embl-dl-nucleotide", "EMBL Flat File");
		map.put("chemical/x-ppd","PPD File");
		map.put("chemical/seq-aa-genpept","Genpept Protein");
		map.put("chemical/seq-na-genbank", "Genbank Nucleotide");
		map.put("chemical/x-pdb", "PDB 3D Structure File");
		return map;
	}

	public String getName() {
		return "Mime Type Manager";
	}

	public String getUUID() {
		return "b9277fa0-5967-11dd-ae16-0800200c9a66";
	}

}
