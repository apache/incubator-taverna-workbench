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
package net.sf.taverna.t2.workbench.ui.credentialmanager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * File filter for filtering against various file extensions. Crypto files
 * normally contain a private key (and optionally its certificate chain) or a
 * public key certificate (and optionally its certificate chain).
 * 
 * .p12 or .pfx are PKCS #12 keystore files containing private key and its
 * public key (+cert chain); .pem are ASN.1 PEM-encoded files containing one (or
 * more concatenated) public key certificate(s); .der are ASN.1 DER-encoded
 * files containing one public key certificate; .cer are CER-encoded files
 * containing one ore more DER-encoded certificates; .crt files are either
 * encoded as binary DER or as ASCII PEM. .p7 and .p7c are PKCS #7 certificate
 * chain files (i.e. SignedData structure without data, just certificate(s)).
 */
public class CryptoFileFilter extends FileFilter {
	// Description of the filter
	private String description;

	// Array of file extensions to filter against
	private List<String> exts;

	public CryptoFileFilter(String[] extList, String desc) {
		exts = Arrays.asList(extList);
		this.description = desc;
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory())
			return true;
		if (file.isFile())
			for (String ext : exts)
				if (file.getName().toLowerCase().endsWith(ext))
					return true;
		return false;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	@Override
	public String getDescription() {
		return this.description;
	}
}
