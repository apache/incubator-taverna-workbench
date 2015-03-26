/*******************************************************************************
 * Copyright (C) 2008-2010 The University of Manchester
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
package org.apache.taverna.workbench.report.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.lang.ui.DeselectingButton;
import org.apache.taverna.lang.ui.ReadOnlyTextArea;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.visit.VisitReport;
import org.apache.taverna.visit.VisitReport.Status;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.report.DataflowReportEvent;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.report.ReportManagerEvent;
import net.sf.taverna.t2.workbench.report.explainer.VisitExplainer;
import org.apache.taverna.workbench.selection.DataflowSelectionModel;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.DataflowSelectionMessage;
import org.apache.taverna.workbench.ui.Workbench;
import org.apache.taverna.workbench.ui.zaria.UIComponentSPI;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.Processor;
import org.apache.taverna.workflowmodel.health.RemoteHealthChecker;

import org.apache.log4j.Logger;

/**
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class ReportViewComponent extends JPanel implements UIComponentSPI {

	private static Logger logger = Logger.getLogger(ReportViewComponent.class);

	private static int TABLE_MARGIN = 5;

	private ReportManager reportManager;

	private List<VisitExplainer> visitExplainers;

	private JLabel dataflowName;

	// private static JTextArea solutionDescription;
	// private static JTextArea issueDescription;
	// private static JSplitPane subSplitPane = new JSplitPane();

	private JTable table;
	private static final JComponent defaultExplanation = new ReadOnlyTextArea(
			"No additional explanation available");
	private static final JComponent defaultSolution = new ReadOnlyTextArea("No suggested solutions");

	private static final JComponent okExplanation = new ReadOnlyTextArea("No problem found");
	private static final JComponent okSolution = new ReadOnlyTextArea("No change necessary");

	private static final JComponent nothingToExplain = new ReadOnlyTextArea("No message selected");
	private static final JComponent nothingToSolve = new ReadOnlyTextArea("No message selected");

	private JComponent explanation = okExplanation;
	private JComponent solution = okSolution;

	private JTabbedPane messagePane;
	private final JScrollPane explanationScrollPane = new JScrollPane();
	private final JScrollPane solutionScrollPane = new JScrollPane();

	private VisitReport lastSelectedReport = null;

	private ReportViewTableModel reportViewTableModel;
	private ReportViewConfigureAction reportViewConfigureAction = new ReportViewConfigureAction();
	private JComboBox shownReports = null;

	private TableListener tableListener = null;

	private VisitReportProxySet ignoredReports = new VisitReportProxySet();

	JButton ignoreReportButton;

	public static String ALL_INCLUDING_IGNORED = "All";
	public static String ALL_EXCEPT_IGNORED = "All except ignored";

	protected FileManager fileManager;
	protected FileManagerObserver fileManagerObserver = new FileManagerObserver();
	private SelectionManager openedWorkflowsManager;
	private Observer<DataflowSelectionMessage> workflowSelectionListener = new DataflowSelectionListener();

	private final Workbench workbench;

	private final EditManager editManager;

	private final MenuManager menuManager;

	public ReportViewComponent(EditManager editManager, FileManager fileManager, MenuManager menuManager,
			ReportManager reportManager, Workbench workbench,
			SelectionManager selectionManager, List<VisitExplainer> visitExplainers) {
		super();
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.menuManager = menuManager;
		this.reportManager = reportManager;
		this.workbench = workbench;
		openedWorkflowsManager = selectionManager;
		this.visitExplainers = visitExplainers;
		reportManager.addObserver(new ReportManagerObserver());
		fileManager.addObserver(fileManagerObserver);
		initialise();
	}

	private JScrollPane tableScrollPane;

	private void initialise() {
		shownReports = new JComboBox(new String[] { ALL_INCLUDING_IGNORED, ALL_EXCEPT_IGNORED,
				ReportViewTableModel.WARNINGS_AND_ERRORS, ReportViewTableModel.JUST_ERRORS });
		shownReports.setSelectedIndex(1);
		shownReports.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ex) {
				showReport(fileManager.getCurrentDataflow());
			}
		});
		this.setLayout(new BorderLayout());
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout());
		dataflowName = new JLabel();
		dataflowName.setText("No workflow");

		headerPanel.add(dataflowName, BorderLayout.WEST);

		JPanel shownReportsPanel = new JPanel();
		shownReportsPanel.setLayout(new BorderLayout());
		shownReportsPanel.add(new JLabel("Show messages:"), BorderLayout.WEST);
		shownReportsPanel.add(shownReports, BorderLayout.EAST);
		headerPanel.add(shownReportsPanel, BorderLayout.EAST);

		this.add(headerPanel, BorderLayout.NORTH);

		JPanel splitPanel = new JPanel();
		splitPanel.setLayout(new GridLayout(2, 1));
		tableScrollPane = new JScrollPane();
		splitPanel.add(tableScrollPane);

		messagePane = new JTabbedPane();
		messagePane.addTab("Explanation", explanationScrollPane);
		messagePane.addTab("Solution", solutionScrollPane);
		splitPanel.add(messagePane);

		this.add(splitPanel, BorderLayout.CENTER);
		ignoreReportButton = new DeselectingButton("Hide message", new AbstractAction() {
			public void actionPerformed(ActionEvent ex) {
				if (lastSelectedReport != null) {
					if (ignoredReports.contains(lastSelectedReport)) {
						ignoredReports.remove(lastSelectedReport);
					} else {
						ignoredReports.add(lastSelectedReport);
						if (shownReports.getSelectedItem().equals(ALL_INCLUDING_IGNORED)) {
							shownReports.setSelectedItem(ALL_EXCEPT_IGNORED);
						}
						showReport();
					}
				}
			}
		});
		// JButton quickCheckButton = new JButton(new
		// ReportOnWorkflowAction("Quick check", false, true));
		JButton fullCheckButton = new DeselectingButton("Validate workflow",
				new ReportOnWorkflowAction("Validate workflow", true, false, editManager,
						fileManager, reportManager, workbench) {
					@Override
					public void actionPerformed(ActionEvent e) {
						// Full check always starts from scratch
						RemoteHealthChecker.clearCachedEndpointStatus();
						super.actionPerformed(e);
					}
				});
		JPanel validateButtonPanel = new JPanel();
		validateButtonPanel.add(ignoreReportButton);
		// validateButtonPanel.add(quickCheckButton);
		validateButtonPanel.add(fullCheckButton);
		this.add(validateButtonPanel, BorderLayout.SOUTH);
		showReport(fileManager.getCurrentDataflow());
	}

	public void onDisplay() {
	}

	public ImageIcon getIcon() {
		return null;
	}

	public void onDispose() {

	}

	private void createTable(Dataflow dataflow, Map<Object, Set<VisitReport>> reportEntries) {
		reportViewTableModel = new ReportViewTableModel(dataflow, reportEntries,
				(String) shownReports.getSelectedItem(), ignoredReports, reportManager);
		if (table == null) {
			table = new JTable(reportViewTableModel);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setRowSelectionAllowed(true);
			tableListener = new TableListener();
			table.getSelectionModel().addListSelectionListener(tableListener);
			table.setSurrendersFocusOnKeystroke(false);
			table.getInputMap(JInternalFrame.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "configure");
			table.getInputMap(JInternalFrame.WHEN_FOCUSED).put(
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "configure");

			table.getActionMap().put("configure", reportViewConfigureAction);

			table.setDefaultRenderer(Status.class, new StatusRenderer());
			table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		} else {
			table.setModel(reportViewTableModel);
		}
		packColumn(table, 0, TABLE_MARGIN, true);
		packColumn(table, 1, TABLE_MARGIN, true);
		packColumn(table, 2, TABLE_MARGIN, true);
		packColumn(table, 3, TABLE_MARGIN, false);
		packColumn(table, 4, TABLE_MARGIN, false);
	}

	private void showReport() {
		showReport(fileManager.getCurrentDataflow());
	}

	private void showReport(final Dataflow dataflow) {
		if (dataflow != null) {
			String dfName = dataflow.getLocalName();
			if (dfName.length() > 20) {
				dfName = dfName.substring(0, 17) + "...";
			}
			dataflowName.setText(dfName);
		} else {
			dataflowName.setText("No workflow");
		}

		createTable(dataflow, reportManager.getReports(dataflow));
		tableScrollPane.setViewportView(table);
		boolean found = false;
		DataflowSelectionModel selectionModel = openedWorkflowsManager
				.getDataflowSelectionModel(fileManager.getCurrentDataflow());

		Set<Object> selection = selectionModel.getSelection();
		Object selectedObject = null;
		if (selection.size() == 1) {
			selectedObject = selection.iterator().next();
		}
		if ((lastSelectedReport != null)
				&& (lastSelectedReport.getSubject().equals(selectedObject))) {
			VisitReportProxy lastSelectedReportProxy = new VisitReportProxy(lastSelectedReport);
			for (int i = 0; i < table.getRowCount(); i++) {
				VisitReport vr = reportViewTableModel.getReport(i);
				VisitReportProxy vrProxy = new VisitReportProxy(vr);
				if (vrProxy.equals(lastSelectedReportProxy)) {
					table.setRowSelectionInterval(i, i);
					found = true;
					scrollToVisible(i);
					break;
				}
			}
			/*
			 * if (!found) { found =
			 * selectSubject(lastSelectedReport.getSubject()); }
			 */
		}
		if ((!found) && (selectedObject != null)) {
			found = selectSubject(selectedObject);
		}
		if (!found) {
			lastSelectedReport = null;
			table.clearSelection();
		}
		updateExplanation(lastSelectedReport);
		updateMessages();
		messagePane.revalidate();
		this.revalidate();
	}

	private boolean selectSubject(Object subject) {
		int currentlySelected = table.getSelectedRow();
		if (currentlySelected != -1) {
			Object currentSubject = reportViewTableModel.getReport(currentlySelected).getSubject();
			if (currentSubject == subject) {
				return true;
			}
		}
		for (int i = 0; i < table.getRowCount(); i++) {
			VisitReport vr = reportViewTableModel.getReport(i);
			if (vr.getSubject() == subject) {
				table.setRowSelectionInterval(i, i);
				scrollToVisible(i);
				return true;
			}
		}
		return false;
	}

	private void updateMessages() {
		JPanel explainPanel = wrapComponent(explanation);
		explanationScrollPane.setViewportView(explainPanel);
		solutionScrollPane.setViewportView(wrapComponent(solution));
		if (lastSelectedReport != null) {
			ignoreReportButton.setEnabled(true);
			if (ignoredReports.contains(lastSelectedReport)) {
				ignoreReportButton.setText("Include message");
			} else {
				ignoreReportButton.setText("Ignore message");
			}
		} else {
			ignoreReportButton.setEnabled(false);
			ignoreReportButton.setText("Ignore message");
		}
		messagePane.revalidate();
	}

	private final class ReportManagerObserver implements Observer<ReportManagerEvent> {
		public void notify(Observable<ReportManagerEvent> sender, ReportManagerEvent event)
				throws Exception {
			Dataflow currentDataflow = fileManager.getCurrentDataflow();

			if (event instanceof DataflowReportEvent) {
				DataflowReportEvent dre = (DataflowReportEvent) event;
				final Dataflow dataflow = dre.getDataflow();
				if (dataflow.equals(currentDataflow)) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							showReport(dataflow);
						}
					});
				}
			}
		}
	}

	private void scrollToVisible(int rowIndex) {
		Rectangle rect = table.getCellRect(rowIndex, 0, true);
		table.scrollRectToVisible(rect);
	}

	final class TableListener implements ListSelectionListener {

		public TableListener() {
		}

		public void valueChanged(ListSelectionEvent e) {
			int row = table.getSelectedRow();
			if (row >= 0) {
				DataflowSelectionModel dsm = openedWorkflowsManager
						.getDataflowSelectionModel(fileManager.getCurrentDataflow());
				dsm.clearSelection();
				VisitReport vr = reportViewTableModel.getReport(row);
				final Object subject = reportViewTableModel.getSubject(row);
				dsm.addSelection(subject);
				updateExplanation(vr);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateMessages();
						if (subject instanceof Processor) {
							reportViewConfigureAction.setConfiguredProcessor((Processor) subject, menuManager);
						}
					}
				});
			}
		}
	}

	private void updateExplanation(VisitReport vr) {
		lastSelectedReport = vr;
		if (vr == null) {
			explanation = nothingToExplain;
			solution = nothingToSolve;
			return;
		}
		if (vr.getStatus().equals(Status.OK)) {
			explanation = okExplanation;
			solution = okSolution;
			return;
		}
		for (VisitExplainer ve : visitExplainers) {
			if (ve.canExplain(vr.getKind(), vr.getResultId())) {
				try {
					explanation = ve.getExplanation(vr);
				} catch (Exception e) {
					logger.error("Error creating explanation", e);
					explanation = null;
				}
				if (explanation == null) {
					explanation = defaultExplanation;
				}
				try {
					solution = ve.getSolution(vr);
				} catch (Exception e) {
					logger.error("Error creating soluttion", e);
					solution = null;
				}
				if (solution == null) {
					solution = defaultSolution;
				}
				return;
			}
		}
		explanation = defaultExplanation;
		solution = defaultSolution;
	}

	// Sets the preferred width of the visible column specified by vColIndex.
	// The column
	// will be just wide enough to show the column head and the widest cell in
	// the column.
	// margin pixels are added to the left and right
	// (resulting in an additional width of 2*margin pixels).
	public void packColumn(JTable table, int vColIndex, int margin, boolean fixWidth) {
		// TableModel model = table.getModel();
		DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
		TableColumn col = colModel.getColumn(vColIndex);
		int width = 0;
		// Get width of column header
		TableCellRenderer renderer = col.getHeaderRenderer();
		if (renderer == null) {
			renderer = table.getTableHeader().getDefaultRenderer();
		}
		Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false,
				false, 0, 0);
		width = comp.getPreferredSize().width;
		// Get maximum width of column data
		for (int r = 0; r < table.getRowCount(); r++) {
			renderer = table.getCellRenderer(r, vColIndex);
			comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex),
					false, false, r, vColIndex);
			width = Math.max(width, comp.getPreferredSize().width);
		}
		// Add margin
		width += 2 * margin;
		// Set the width
		col.setPreferredWidth(width);
		if (fixWidth) {
			col.setMaxWidth(width);
			col.setMinWidth(width);
		}

	}

	private static Insets rightGap = new Insets(0, 0, 20, 0);

	private JPanel wrapComponent(JComponent c) {
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 0.9;
		gbc.insets = rightGap;
		result.add(c, gbc);
		c.setBackground(SystemColor.text);
		gbc.weightx = 0.9;
		gbc.weighty = 0.9;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		JPanel filler = new JPanel();
		filler.setBackground(SystemColor.text);
		result.setBackground(SystemColor.text);
		result.add(filler, gbc);
		return result;
	}

	/**
	 * Update workflow explorer when current dataflow changes or closes.
	 *
	 */
	public class FileManagerObserver implements Observer<FileManagerEvent> {

		public void notify(Observable<FileManagerEvent> sender, FileManagerEvent message)
				throws Exception {

			if (message instanceof SetCurrentDataflowEvent) { // switched the
																// current
																// workflow

				final Dataflow newWF = ((SetCurrentDataflowEvent) message).getDataflow(); // the
																							// newly
																							// switched
																							// to
																							// workflow
				if (newWF != null) {
					openedWorkflowsManager.getDataflowSelectionModel(newWF).addObserver(
							workflowSelectionListener);
				}
			}
		}
	}

	/**
	 * Observes events on workflow Selection Manager, i.e. when a workflow node
	 * is selected in the graph view.
	 */
	private final class DataflowSelectionListener implements Observer<DataflowSelectionMessage> {

		public void notify(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) throws Exception {

			DataflowSelectionModel selectionModel = openedWorkflowsManager
					.getDataflowSelectionModel(fileManager.getCurrentDataflow());

			Set<Object> selection = selectionModel.getSelection();
			if (selection.size() == 1) {
				if (!selectSubject(selection.iterator().next())) {
					lastSelectedReport = null;
					table.clearSelection();
				}

			}
		}
	}
}
