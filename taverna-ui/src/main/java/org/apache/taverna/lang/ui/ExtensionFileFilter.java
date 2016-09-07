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

package org.apache.taverna.lang.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * A FileFilter implementation that can be configured to show only specific file
 * suffixes.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 */
public class ExtensionFileFilter extends FileFilter {
	List<String> allowedExtensions;

	public ExtensionFileFilter(List<String> extensions) {
	    setAllowedExtensions(extensions);
	}

	public ExtensionFileFilter(String[] allowedExtensions) {
	    setAllowedExtensions(Arrays.asList(allowedExtensions));
	}

    private void setAllowedExtensions(List<String> extensions) {
	    this.allowedExtensions = new ArrayList<String>();
            for (String e : extensions) {
		if (e.startsWith(".")) {
                    if (e.length() > 1) {
			allowedExtensions.add(e.substring(1));
		    }
		}
		else {
		    allowedExtensions.add(e);
		}
	    }
    }

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String extension = getExtension(f);
		if (extension != null) {
			for (String allowedExtension : allowedExtensions) {
				if (extension.equalsIgnoreCase(allowedExtension)) {
					return true;
				}
			}
		}
		return false;
	}

	String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	@Override
	public String getDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append("Filter for extensions : " );
		for (int i = 0; i < allowedExtensions.size(); i++) {
			sb.append(allowedExtensions.get(i));
			if (i < allowedExtensions.size() - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}
}
