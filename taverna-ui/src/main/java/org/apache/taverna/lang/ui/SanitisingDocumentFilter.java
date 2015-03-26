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

import java.util.regex.Pattern;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

/**
 * @author alanrw
 *
 */
public class SanitisingDocumentFilter extends DocumentFilter {
	
	private static SanitisingDocumentFilter INSTANCE = new SanitisingDocumentFilter();
	
	private SanitisingDocumentFilter () {
		super();
	}
	
	public static void addFilterToComponent(JTextComponent c) {
		Document d = c.getDocument();
		if (d instanceof AbstractDocument) {
			((AbstractDocument) d).setDocumentFilter(INSTANCE);
		} 		
	}
	
	public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
		
		fb.insertString(offset, sanitiseString(string), attr);
	}
	
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
		      String text, javax.swing.text.AttributeSet attr)

		      throws BadLocationException {
		           fb.replace(offset, length, sanitiseString(text), attr);   
		 }
	
	private static String sanitiseString(String text) {
		String result = text;
		if (Pattern.matches("\\w++", text) == false) {
			result = "";
			for (char c : text.toCharArray()) {
				if (Character.isLetterOrDigit(c) || c == '_') {
					result += c;
				} else {
					result += "_";
				}
			}
		}
		return result;		
	}
}
