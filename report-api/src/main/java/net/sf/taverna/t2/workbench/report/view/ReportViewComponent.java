/**
 * 
 */
package net.sf.taverna.t2.workbench.report.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.StringReader;

import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.JInternalFrame;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumn;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.DataflowReportEvent;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.report.ReportManagerEvent;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.NamedWorkflowEntity;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPort;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.lang.ui.icons.Icons;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.report.explainer.VisitExplainer;

import net.sf.taverna.t2.lang.ui.TableSorter;
import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import net.sf.taverna.t2.lang.ui.JSplitPaneExt;


/**
 * @author alanrw
 *
 */
public class ReportViewComponent extends JPanel implements UIComponentSPI {
	
	private static int TABLE_MARGIN = 5;
	
	private ReportManager reportManager = ReportManager.getInstance();
	
	private static SPIRegistry<VisitExplainer> visitExplainerRegistry = new SPIRegistry<VisitExplainer>(VisitExplainer.class);

	private JLabel dataflowName;
	
	private static JTextArea solutionDescription;
	private static JTextArea issueDescription;
	private static JSplitPane subSplitPane = new JSplitPane();
	
	private JTable table;
	private JPanel subPanel = new JPanel();
	
	private JComponent explanation = okExplanation;
	private JComponent solution = okSolution;

	private static JTextArea defaultExplanation = new ReadOnlyTextArea("No additional explanation available");
	private static JTextArea defaultSolution = new JTextArea("No suggested solutions");
	
	private static JTextArea okExplanation = new JTextArea("No problem found");
	private static JTextArea okSolution = new JTextArea("No change necessary");
	
	private static JTextArea nothingToExplain = new JTextArea("No report selected");
	private static JTextArea nothingToSolve = new JTextArea("No report selected");
	
	private JTabbedPane messagePane;
	
	private VisitReport lastSelectedReport = null;
	
	private ReportViewTableModel reportViewTableModel;
	private TableSorter sorter;
	
	public ReportViewComponent() {
		super();
		reportManager.addObserver(new ReportManagerObserver());
		initialise();
		System.err.println("Initially defaultExplanation is editable " + defaultExplanation.isEditable());
	}
	
	private void initialise() {
		this.setLayout(new BorderLayout());
		messagePane = new JTabbedPane();
		showReport(FileManager.getInstance().getCurrentDataflow());
	}

	
	public void onDisplay() {
	}

	public ImageIcon getIcon() {
		return null;
	}
	
	public void onDispose() {

	}
	
	private JTable createTable(Map<Object, Set<VisitReport>> reportEntries) {
		reportViewTableModel = new ReportViewTableModel(reportEntries);
		sorter = new TableSorter(reportViewTableModel);
		JTable table = new JTable(sorter);
		sorter.sortByColumn(0, false); // sort by decreasing severity
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.getSelectionModel().addListSelectionListener(new TableListener(table, reportViewTableModel, sorter));
		table.setSurrendersFocusOnKeystroke(false);

		table.setDefaultRenderer(Status.class, new StatusRenderer());

		sorter.addMouseListenerToHeaderInTable(table);
		packColumn(table, 0, TABLE_MARGIN, true);
		packColumn(table, 1, TABLE_MARGIN, true);
		packColumn(table, 2, TABLE_MARGIN, true);
		packColumn(table, 3, TABLE_MARGIN, false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		return table;
	}

	private void showReport(final Dataflow dataflow) {
		this.removeAll();
		dataflowName = new JLabel();
		if (dataflow != null) {
			dataflowName.setText(dataflow.getLocalName());
		} else {
			dataflowName.setText("No workflow");
		}
		this.add(dataflowName, BorderLayout.NORTH);
		table = createTable(reportManager.getReports(dataflow));
		explanation = nothingToExplain;
		solution = nothingToSolve;
		updateMessages();
		getProblemPanel();

		this.add(subPanel, BorderLayout.CENTER);
		JButton validateButton = new JButton("Validate");
		validateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ex) {
				ReportManager.updateReport(dataflow, true);
			}
		});
		JPanel validateButtonPanel = new JPanel();
		validateButtonPanel.add(validateButton);
		this.add(validateButtonPanel, BorderLayout.SOUTH);
		messagePane.revalidate();
		this.revalidate();
		for (int i = 0; i < table.getRowCount(); i++) {
			VisitReport vr = reportViewTableModel.getReport(sorter.transposeRow(i));
			if (vr.equals(lastSelectedReport)) {
				table.setRowSelectionInterval(i, i);
				return;
			}
		}
	}
	
	private void getProblemPanel() {
		JSplitPane splitPane1 = new JSplitPaneExt(JSplitPane.VERTICAL_SPLIT);
		splitPane1.setDividerLocation(0.5);
		splitPane1.add(new JScrollPane(table));
		splitPane1.add(messagePane);
		subPanel.removeAll();
		subPanel.setLayout(new BorderLayout());
		subPanel.add(splitPane1, BorderLayout.CENTER);
		this.revalidate();
	}
	
	private void updateMessages() {
		int explanationIndex = messagePane.indexOfTab("Explanation");
		if (explanationIndex == -1) {
			messagePane.addTab("Explanation", explanation);
		} else {
			messagePane.setComponentAt(explanationIndex, explanation);
		}
		int solutionIndex = messagePane.indexOfTab("Solution");
		if (solutionIndex == -1) {
			messagePane.addTab("Solution", solution);
		} else {
			messagePane.setComponentAt(solutionIndex, solution);
		}
		messagePane.revalidate();
	}
	
	private final class ReportManagerObserver implements
			Observer<ReportManagerEvent> {
		public void notify(Observable<ReportManagerEvent> sender,
				ReportManagerEvent event) throws Exception {
			// System.err.println("Got an event");
			Dataflow currentDataflow = FileManager.getInstance()
					.getCurrentDataflow();

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

	final class TableListener implements ListSelectionListener {
		private JTable table;
		private ReportViewTableModel reportViewTableModel;
		private TableSorter sorter;
		private int lastSelectedRow = -1;
		
		public TableListener(JTable table, ReportViewTableModel reportViewTableModel, TableSorter sorter) {
			this.table = table;
			this.reportViewTableModel = reportViewTableModel;
			this.sorter = sorter;
		}
		
		public void valueChanged(ListSelectionEvent e) {
			int row = table.getSelectedRow();
			if ((row >= 0) && (row != lastSelectedRow)) {
				lastSelectedRow = row;
				DataflowSelectionModel dsm = DataflowSelectionManager.getInstance().getDataflowSelectionModel(FileManager.getInstance().getCurrentDataflow());
				dsm.clearSelection();
				VisitReport vr = reportViewTableModel.getReport(sorter.transposeRow(row));
				dsm.addSelection(reportViewTableModel.getSubject(sorter.transposeRow(row)));
				updateExplanation(vr);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateMessages();
					}
				});
			}
		}		
	}
	
	private void updateExplanation(VisitReport vr) {
		lastSelectedReport = vr;
		if (vr.getStatus().equals(Status.OK)) {
			explanation = okExplanation;
			solution = okSolution;
			return;
		}
		for (VisitExplainer ve : visitExplainerRegistry.getInstances()) {
			if (ve.canExplain(vr.getKind(), vr.getResultId())) {
				explanation = ve.getExplanation(vr);
				if (explanation == null) {
					explanation = defaultExplanation;
				}
				solution = ve.getSolution(vr);
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
		TableModel model = table.getModel();
		DefaultTableColumnModel colModel = (DefaultTableColumnModel) table
				.getColumnModel();
		TableColumn col = colModel.getColumn(vColIndex);
		int width = 0;
		// Get width of column header
		TableCellRenderer renderer = col.getHeaderRenderer();
		if (renderer == null) {
			renderer = table.getTableHeader().getDefaultRenderer();
		}
		Component comp = renderer.getTableCellRendererComponent(table, col
				.getHeaderValue(), false, false, 0, 0);
		width = comp.getPreferredSize().width;
		// Get maximum width of column data
		for (int r = 0; r < table.getRowCount(); r++) {
			renderer = table.getCellRenderer(r, vColIndex);
			comp = renderer.getTableCellRendererComponent(table, table
					.getValueAt(r, vColIndex), false, false, r, vColIndex);
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
}
