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

package org.apache.taverna.lang.ui;

import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * @author alanrw
 *
 */
public class ReadOnlyTextArea extends JTextArea {
	
	public ReadOnlyTextArea () {
		super();
		setFields();
	}
	
	public ReadOnlyTextArea(Document doc) {
		super(doc);
		setFields();
	}
	
	public ReadOnlyTextArea (Document doc, String text, int rows, int columns) {
		super(doc,text,rows,columns);
		setFields();
	}
	
	public ReadOnlyTextArea(int rows, int columns) {
		super(rows, columns);
		setFields();
	}
	
	public ReadOnlyTextArea(String text) {
		super(text);
		setFields();
	}
	
	public ReadOnlyTextArea(String text, int rows, int columns) {
		super(text, rows, columns);
		setFields();
	}

	private void setFields() {
		super.setEditable(false);
		super.setLineWrap(true);
		super.setWrapStyleWord(true);
	}
}
