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
package org.apache.taverna.lang.ui;

import java.awt.Font;

import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * @author alanrw
 *
 */
public class DialogTextArea extends JTextArea {

	private static Font newFont = Font.decode("Dialog");
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2329063139827993252L;

	/**
	 * 
	 */
	public DialogTextArea() {
		updateFont();
	}

	/**
	 * @param text
	 */
	public DialogTextArea(String text) {
		super(text);
		updateFont();
	}

	/**
	 * @param doc
	 */
	public DialogTextArea(Document doc) {
		super(doc);
		updateFont();
	}

	/**
	 * @param rows
	 * @param columns
	 */
	public DialogTextArea(int rows, int columns) {
		super(rows, columns);
		updateFont();
	}

	/**
	 * @param text
	 * @param rows
	 * @param columns
	 */
	public DialogTextArea(String text, int rows, int columns) {
		super(text, rows, columns);
		updateFont();
	}

	/**
	 * @param doc
	 * @param text
	 * @param rows
	 * @param columns
	 */
	public DialogTextArea(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
		updateFont();
	}
	
	private void updateFont() {
		if (newFont != null) {
			this.setFont(newFont);
		}
	}

}
