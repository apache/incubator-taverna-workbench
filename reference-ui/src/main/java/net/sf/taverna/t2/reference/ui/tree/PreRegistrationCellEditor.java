/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.reference.ui.tree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;

/**
 * Cell editor that uses JTextArea.
 * 
 * @author David Withers
 */
public class PreRegistrationCellEditor extends AbstractCellEditor implements
		TreeCellEditor {

	private static final long serialVersionUID = 1L;

	protected JTextArea textArea;

	protected JScrollPane textScrollPane;

	public PreRegistrationCellEditor() {
		textArea = new JTextArea();
		textArea.setBackground(Color.WHITE);
		textArea.addFocusListener(new FocusAdapter () {
			public void focusLost(FocusEvent e) {
				stopCellEditing();
			}
		});
		textScrollPane = new JScrollPane(textArea);
		textScrollPane.setPreferredSize(new Dimension(350, 80));
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row) {
		String textValue = tree.convertValueToText(value, isSelected,
				expanded, leaf, row, false);
		textArea.setText(textValue);
		return textScrollPane;
	}

	public Object getCellEditorValue() {
		return textArea.getText();
	}

}
