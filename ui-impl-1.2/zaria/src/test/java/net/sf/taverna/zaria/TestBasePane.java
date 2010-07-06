/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester   
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
package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import junit.framework.TestCase;

public class TestBasePane extends TestCase {

	@SuppressWarnings("serial")
	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException, InterruptedException,
			ParseException {
		// Create the JFrame first as I want to keep the windows etc
		// decorations.
		JFrame myFrame = new JFrame("ZPane test");
		try {
			UIManager
					.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
		} catch (Exception ex) {
			// don't have synthetica installed, shame. It looks
			// a lot better.
		}
		myFrame.setSize(300, 300);
		ZBasePane pane = new ZBasePane() {

			public void discard() {
				// TODO Auto-generated method stub

			}

			@SuppressWarnings("unchecked")
			@Override
			public JComponent getComponent(Class theClass) {
				// TODO Auto-generated method stub
				return null;
			}

			@SuppressWarnings("unchecked")
			@Override
			public JMenuItem getMenuItem(Class theClass) {
				// TODO Auto-generated method stub
				return null;
			}

		};
		pane.setEditable(true);
		myFrame.getContentPane().add(pane, BorderLayout.CENTER);
		myFrame.setVisible(true);
		Thread.sleep(1000 * 20);
		pane.lockFrame();
		Thread.sleep(1000 * 30);
	}

	public void testBasePane() throws InterruptedException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		/* Does not run in head-less mode */
		/**
		 * UIManager.setLookAndFeel(UIManager .getSystemLookAndFeelClassName());
		 * JFrame myFrame = new JFrame("ZPane test"); myFrame.setSize(300,300);
		 * ZPane pane = new ZBasePane() {
		 * 
		 * @Override public JComponent getComponent(Class theClass) { // TODO
		 *           Auto-generated method stub return null; }
		 *  }; pane.setEditable(true);
		 * myFrame.getContentPane().add(pane, BorderLayout.CENTER);
		 * myFrame.setVisible(true); Thread.sleep(1000*30);
		 */
	}

}
