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
package net.sf.taverna.t2.reference.ui.tree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sf.taverna.t2.reference.ui.PreRegistrationPanel;

/**
 * Show a PreRegistrationPanel with a collection depth of 4 to play around with.
 * 
 * @author Tom Oinn
 */
public class PreRegistrationPanelTestApp {

	/**
	 * @param args
	 * @throws UnsupportedLookAndFeelException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		final PreRegistrationTreeModel model = new PreRegistrationTreeModel(4);
		model.addPojoStructure(null, "Hello world!", 0);
		model.addPojoStructure(null, "Hello world2!", 0);
		List<String> stringList = new ArrayList<String>();
		stringList.add("Foo");
		stringList.add("Bar");
		model.addPojoStructure(null, stringList, 1);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI3();
			}
		});
	}

	@SuppressWarnings("serial")
	private static void createAndShowGUI3() {
		// Create and set up the window.
		JFrame frame = new JFrame("PreRegistrationTreeModel Demo 3");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		JPanel newContentPane = new PreRegistrationPanel(4) {
			@Override
			public void handleRegistration(Object pojo) {
				System.out.println(pojo);
			}
		};
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

}
