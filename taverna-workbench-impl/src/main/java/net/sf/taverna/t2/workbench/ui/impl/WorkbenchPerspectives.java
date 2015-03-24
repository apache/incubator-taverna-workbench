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
package net.sf.taverna.t2.workbench.ui.impl;

import static java.awt.Image.SCALE_SMOOTH;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.PerspectiveSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class WorkbenchPerspectives {
	private static Logger logger = Logger
			.getLogger(WorkbenchPerspectives.class);

	private PerspectiveSPI currentPerspective;
	private ButtonGroup perspectiveButtonGroup = new ButtonGroup();
	private Map<String, JToggleButton> perspectiveButtonMap = new HashMap<>();
	private JToolBar toolBar;
	private JPanel panel;
	private CardLayout cardLayout;
	private List<PerspectiveSPI> perspectives = new ArrayList<>();
	private boolean refreshing;
	private final SelectionManager selectionManager;

	public WorkbenchPerspectives(JToolBar toolBar, JPanel panel,
			CardLayout cardLayout, SelectionManager selectionManager) {
		this.panel = panel;
		this.toolBar = toolBar;
		this.cardLayout = cardLayout;
		this.selectionManager = selectionManager;
		refreshing = true;
		selectionManager.addObserver(new SelectionManagerObserver());
		refreshing = false;
	}

	public List<PerspectiveSPI> getPerspectives() {
		return this.perspectives;
	}

	public void setPerspectives(List<PerspectiveSPI> perspectives) {
		this.perspectives = perspectives;
		initialisePerspectives();
	}

	private void initialisePerspectives() {
		for (final PerspectiveSPI perspective : perspectives)
			addPerspective(perspective, false);
		selectFirstPerspective();
	}

	private void setPerspective(PerspectiveSPI perspective) {
		if (perspective != currentPerspective) {
			if (!perspectiveButtonMap.containsKey(perspective.getID()))
				addPerspective(perspective, true);
			if (!(perspective instanceof BlankPerspective))
				perspectiveButtonMap.get(perspective.getID()).setSelected(true);
			cardLayout.show(panel, perspective.getID());
			currentPerspective = perspective;
		}
	}

	private void addPerspective(final PerspectiveSPI perspective,
			boolean makeActive) {
		// ensure icon image is always 16x16
		ImageIcon buttonIcon = null;
		if (perspective.getButtonIcon() != null) {
			Image buttonImage = perspective.getButtonIcon().getImage();
			buttonIcon = new ImageIcon(buttonImage.getScaledInstance(16, 16,
					SCALE_SMOOTH));
		}

		final JToggleButton toolbarButton = new JToggleButton(
				perspective.getText(), buttonIcon);
		toolbarButton.setToolTipText(perspective.getText() + " perspective");
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionManager.setSelectedPerspective(perspective);
			}
		};
		action.putValue(NAME, perspective.getText());
		action.putValue(SMALL_ICON, buttonIcon);

		toolbarButton.setAction(action);
		toolBar.add(toolbarButton);
		perspectiveButtonGroup.add(toolbarButton);
		perspectiveButtonMap.put(perspective.getID(), toolbarButton);

		panel.add(perspective.getPanel(), perspective.getID());
		if (makeActive)
			toolbarButton.doClick();
	}

	/**
	 * Recreates the toolbar buttons. Useful if a perspective has been removed.
	 */
	public void refreshPerspectives() {
		invokeLater(new RefreshRunner());
	}

	/** selects the first visible perspective by clicking on the toolbar button */
	private void selectFirstPerspective() {
		boolean set = false;
		for (Component c : toolBar.getComponents())
			if (c instanceof AbstractButton && c.isVisible()) {
				((AbstractButton) c).doClick();
				set = true;
				break;
			}

		if (!set) {
			// no visible perspectives were found
			logger.info("No visible perspectives.");
			selectionManager.setSelectedPerspective(new BlankPerspective());
		}
	}

	private final class RefreshRunner implements Runnable {
		@Override
		public void run() {
			synchronized (WorkbenchPerspectives.this) {
				if (refreshing)
					// We only need one run
					return;
				refreshing = true;
			}
			try {
				toolBar.removeAll();
				toolBar.repaint();
				initialisePerspectives();
			} finally {
				synchronized (WorkbenchPerspectives.this) {
					refreshing = false;
				}
			}
		}
	}

	private final class SelectionManagerObserver extends
			SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (message instanceof PerspectiveSelectionEvent) {
				PerspectiveSPI selectedPerspective = ((PerspectiveSelectionEvent) message)
						.getSelectedPerspective();
				setPerspective(selectedPerspective);
			}
		}
	}

	/**
	 * A dummy blank perspective for when there are no visible perspectives
	 * available
	 *
	 * @author Stuart Owen
	 */
	private class BlankPerspective implements PerspectiveSPI {
		@Override
		public String getID() {
			return BlankPerspective.class.getName();
		}

		@Override
		public JPanel getPanel() {
			return new JPanel();
		}

		@Override
		public ImageIcon getButtonIcon() {
			return null;
		}

		@Override
		public String getText() {
			return null;
		}

		@Override
		public int positionHint() {
			return 0;
		}
	}
}
