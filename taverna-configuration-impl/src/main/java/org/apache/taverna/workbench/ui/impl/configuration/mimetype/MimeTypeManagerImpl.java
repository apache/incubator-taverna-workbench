/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.impl.configuration.mimetype;

import java.util.HashMap;
import java.util.Map;

import org.apache.taverna.workbench.configuration.mimetype.MimeTypeManager;
import uk.org.taverna.configuration.AbstractConfigurable;
import uk.org.taverna.configuration.ConfigurationManager;

public class MimeTypeManagerImpl extends AbstractConfigurable implements
		MimeTypeManager {
	/**
	 * Constructs a new <code>MimeTypeManagerImpl</code>.
	 * 
	 * @param configurationManager
	 */
	public MimeTypeManagerImpl(ConfigurationManager configurationManager) {
		super(configurationManager);
	}

	@Override
	public String getCategory() {
		return "Mime Type";
	}

	@Override
	public Map<String, String> getDefaultPropertyMap() {
		HashMap<String, String> map = new HashMap<>();
		map.put("text/plain", "Plain Text");
		map.put("text/xml", "XML Text");
		map.put("text/html", "HTML Text");
		map.put("text/rtf", "Rich Text Format");
		map.put("text/x-graphviz", "Graphviz Dot File");
		map.put("image/png", "PNG Image");
		map.put("image/jpeg", "JPEG Image");
		map.put("image/gif", "GIF Image");
		map.put("application/octet-stream", "Binary Data");
		map.put("application/zip", "Zip File");
		map.put("chemical/x-swissprot", "SWISSPROT Flat File");
		map.put("chemical/x-embl-dl-nucleotide", "EMBL Flat File");
		map.put("chemical/x-ppd", "PPD File");
		map.put("chemical/seq-aa-genpept", "Genpept Protein");
		map.put("chemical/seq-na-genbank", "Genbank Nucleotide");
		map.put("chemical/x-pdb", "PDB 3D Structure File");
		return map;
	}

	@Override
	public String getUUID() {
		return "b9277fa0-5967-11dd-ae16-0800200c9a66";
	}

	@Override
	public String getDisplayName() {
		return "Mime Type Manager";
	}

	@Override
	public String getFilePrefix() {
		return "MimeTypeManagerImpl";
	}
}
