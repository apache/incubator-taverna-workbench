/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workbench.ui.credentialmanager;

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
