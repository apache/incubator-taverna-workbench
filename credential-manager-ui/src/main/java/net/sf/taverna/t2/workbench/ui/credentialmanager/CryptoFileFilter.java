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
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.filechooser.FileFilter;

/**
 * File filter for filtering against various file extensions. Crypto files
 * normally contain a private key (and optionally its certificate chain)
 * or a public key certificate (and optionally its certificate chain).
 */
public class CryptoFileFilter extends FileFilter 
{
	// Description of the filter
	private String description;
	
	// Array of file extensions to filter against
	private ArrayList<String> exts = new ArrayList<String>();
	
    /**
     * Construct a CryptoFileFilter for a set of related file extensions.
     */
    public CryptoFileFilter(String [] extList, String desc)
    {
        for (int i = 0; i < extList.length; i++) {
            addType (extList[i]);
        }
        this.description = desc;
    }

	private void addType(String s) {
		exts.add(s);
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		} else if (f.isFile()) {
			Iterator<String> it = exts.iterator();
			while (it.hasNext()) {
				if (f.getName().toLowerCase().endsWith((String) it.next()))
					return true;
			}
		}
		return false;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	public String getDescription() {
		return this.description;
	}
}
