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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.t2.workbench.configuration.mimetype.MimeTypeManager;

/**
 * Simple UI for adding mime types to "something". The "something" registers as
 * a listener using addMimeTypeListener() and is then called back when the OK
 * button is clicked to do whatever action it wants. The lists of Mime Types are
 * populated using {@link JList} with {@link DefaultListModel}s. There are two
 * lists, one for the default set of mimes populated from the
 * {@link MimeTypeManager} and the other for the ones that the user has selected
 * 
 * @author Ian Dunlop
 * 
 */
@SuppressWarnings("serial")
public class MimeTypeConfig extends JPanel {

	private JTextArea mimeTypes;
	private JTextArea newMimeType;
	private ActionListener listener;
	private Map<String, String> mimeMap;
	private DefaultListModel originalMimeListModel;
	private JList mimeList;
	protected String selectedMime;
	private JList userMimeList;
	private DefaultListModel userMimeListModel;
	private String mimeToRemove;
	private JButton addUserDefinedMimeButton;
	private JButton addOriginalMimeTypeButton;
	private JButton removeMimeButton;
	private JButton oKbutton;
	private JButton addToMimeConfigButton;

	public MimeTypeConfig() {
		init();
	}

	/**
	 * Create the Lists to hold the mime types, call {@link #createButtons()}
	 * and then create the display through {@link #setupDisplay()}
	 */
	private void init() {

		setLayout(new GridBagLayout());

		userMimeListModel = new DefaultListModel();
		userMimeList = new JList(userMimeListModel);
		userMimeList.setBorder(BorderFactory
				.createTitledBorder("Mime Types for Port"));
		userMimeList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				mimeToRemove = (String) userMimeList.getSelectedValue();
			}

		});

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		mimeTypes = new JTextArea();
		mimeTypes.setBorder(BorderFactory
				.createTitledBorder("Create new Mime Type"));
		newMimeType = new JTextArea();

		originalMimes();
		populateMimeList();
		userMimeList
				.setToolTipText("Define a Mime Type and click on \'add Mime Type\'");

		createButtons();
		setupDisplay();
	}

	/**
	 * 
	 */
	private void createButtons() {
		oKbutton = new JButton("OK");
		oKbutton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				listener.actionPerformed(e);
			}

		});
		oKbutton.setToolTipText("Add all of these mime types to the port");

		addOriginalMimeTypeButton = new JButton("Add mime type");
		addOriginalMimeTypeButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				userMimeListModel.addElement(selectedMime);
			}

		});

		removeMimeButton = new JButton("Remove Mime Type");
		removeMimeButton.addActionListener(new AbstractAction() {
			// find out what mime was clicked on and remove it from the list
			public void actionPerformed(ActionEvent e) {
				int indexOf = userMimeListModel.indexOf(mimeToRemove);
				userMimeListModel.remove(indexOf);
			}

		});

		addUserDefinedMimeButton = new JButton("Add your own mime type");
		addUserDefinedMimeButton.addActionListener(new AbstractAction() {
			// update the display and add the mime type to list of use selected
			// mime types
			public void actionPerformed(ActionEvent e) {
				String text = newMimeType.getText();
				System.out.println(text);
				// if there is a user added mime type then add it to the list
				// and also the clickable list
				if (text != "" || text != null) {
					// TODO regex to check for string/string
					userMimeListModel.addElement(text);
					originalMimeListModel.addElement(text);
				}
			}

		});
		addUserDefinedMimeButton
				.setToolTipText("Add this mime type to the list");

		addToMimeConfigButton = new JButton(
				"Add your new mime types to the default list");
		addToMimeConfigButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

			}

		});
		// FIXME add some annotations or something?
		addToMimeConfigButton.setEnabled(false);
	}

	private void setupDisplay() {

		// FIXME needs some work - grid bag bleuch

		GridBagConstraints constraint = new GridBagConstraints();
		constraint.anchor = GridBagConstraints.FIRST_LINE_START;
		constraint.gridx = 0;
		constraint.gridy = 0;
		constraint.weighty = 0;
		constraint.weightx = 0;
		constraint.fill = GridBagConstraints.NONE;

		// left side
		add(newMimeType, constraint);
		constraint.gridy = 1;
		add(addUserDefinedMimeButton, constraint);
		constraint.gridy = 2;
		add(mimeList, constraint);
		constraint.gridy = 3;
		add(addOriginalMimeTypeButton, constraint);

		constraint.anchor = GridBagConstraints.BOTH;
		constraint.gridx = 1;
		constraint.gridy = 0;
		constraint.weighty = 0;
		constraint.weightx = 0;
		constraint.fill = GridBagConstraints.NONE;

		// right side
		add(userMimeList, constraint);
		constraint.gridy = 1;
		add(removeMimeButton, constraint);
		constraint.gridy = 2;
		add(oKbutton, constraint);
		constraint.gridy = 3;
		add(addToMimeConfigButton, constraint);
	}

	private void populateMimeList() {
		originalMimeListModel = new DefaultListModel();
		for (String mime : mimeMap.keySet()) {
			originalMimeListModel.addElement(mime);
		}
		mimeList = new JList(originalMimeListModel);
		mimeList.setBorder(BorderFactory.createTitledBorder("All Mime Types"));
		mimeList.addListSelectionListener(new ListSelectionListener() {
			// when a mime type is selected remember what it is
			public void valueChanged(ListSelectionEvent e) {
				Object selectedValue = mimeList.getSelectedValue();
				selectedMime = (String) selectedValue;
			}

		});
	}

	/**
	 * All of the mime types which have been added
	 * 
	 * @return a List of all the mime types
	 */
	public List<String> getMimeTypeList() {
		List<String> returnedMimeList = new ArrayList<String>();
		Object[] mimeArray = new Object[userMimeListModel.size()];
		userMimeListModel.copyInto(mimeArray);
		for (int i = 0; i < mimeArray.length; i++) {
			returnedMimeList.add((String) mimeArray[i]);
		}
		return returnedMimeList;
	}

	/**
	 * Pass in an action which will happen when the OK button is clicked
	 * 
	 * @param listener
	 */
	public void addNewMimeListener(ActionListener listener) {
		this.listener = listener;
	}

	/**
	 * Set the initial list of mime types
	 * 
	 * @param mimeTypeList
	 */
	public void setMimeTypeList(List<String> mimeTypeList) {
		for (String mime : mimeTypeList) {
			userMimeListModel.addElement(mime);
		}
	}

	private void originalMimes() {
		mimeMap=new HashMap<String, String>();
		for (String key : MimeTypeManager.getInstance().getKeys()) {
			mimeMap.put(key, MimeTypeManager.getInstance().getProperty(key));
		}
	}

}
