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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.io.StringReader;

import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.JComboBox;
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
import org.jdesktop.swingworker.SwingWorkerCompletionWaiter;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ReportViewComponent extends JPanel implements UIComponentSPI {
	
	private static Logger logger = Logger
			.getLogger(ReportViewComponent.class);

	private static int TABLE_MARGIN = 5;
	
	private ReportManager reportManager = ReportManager.getInstance();
	
	private static SPIRegistry<VisitExplainer> visitExplainerRegistry = new SPIRegistry<VisitExplainer>(VisitExplainer.class);

	private JLabel dataflowName;
	
	private static JTextArea solutionDescription;
	private static JTextArea issueDescription;
	private static JSplitPane subSplitPane = new JSplitPane();
	
	private JTable table;
	private static final JComponent defaultExplanation = new ReadOnlyTextArea("No additional explanation available");
	private static final JComponent defaultSolution = new ReadOnlyTextArea("No suggested solutions");
	
	private static final JComponent okExplanation = new ReadOnlyTextArea("No problem found");
	private static final JComponent okSolution = new ReadOnlyTextArea("No change necessary");
	
	private static final JComponent nothingToExplain = new ReadOnlyTextArea("No report selected");
	private static final JComponent nothingToSolve = new ReadOnlyTextArea("No report selected");
	
	private JComponent explanation = okExplanation;
	private JComponent solution = okSolution;

	private JTabbedPane messagePane;
	private final JScrollPane explanationScrollPane = new JScrollPane();
	private final JScrollPane solutionScrollPane = new JScrollPane();
	
	private VisitReport lastSelectedReport = null;
	
	private ReportViewTableModel reportViewTableModel;
	private TableSorter sorter;
	private ReportViewConfigureAction reportViewConfigureAction = new ReportViewConfigureAction();
    private JComboBox shownReports = null;
    
    private TableListener tableListener = null;
	public ReportViewComponent() {
		super();
		reportManager.addObserver(new ReportManagerObserver());
		initialise();
	}
	
    private JScrollPane tableScrollPane;

	private void initialise() {
	    shownReports = new JComboBox(new String[] {ReportViewTableModel.ALL_REPORTS,
						       ReportViewTableModel.WARNINGS_AND_ERRORS,
						       ReportViewTableModel.JUST_ERRORS});
	    shownReports.setSelectedIndex(0);
	    shownReports.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ex) {
			showReport(FileManager.getInstance()
					.getCurrentDataflow());
		    }
		});
		this.setLayout(new BorderLayout());
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout());
		dataflowName = new JLabel();
		dataflowName.setText("No workflow");

		headerPanel.add(dataflowName, BorderLayout.WEST);
		headerPanel.add(shownReports, BorderLayout.EAST);
		this.add(headerPanel, BorderLayout.NORTH);
		
		JSplitPane splitPane = new JSplitPaneExt(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(0.5);
		tableScrollPane = new JScrollPane();
		splitPane.add(tableScrollPane);

		messagePane = new JTabbedPane();
		messagePane.addTab("Explanation", explanationScrollPane);
		messagePane.addTab("Solution", solutionScrollPane);
		splitPane.add(messagePane);

		this.add(splitPane, BorderLayout.CENTER);
		JButton quickCheckButton = new JButton("Quick check");
		quickCheckButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ex) {
			    Dataflow dataflow = FileManager.getInstance()
				.getCurrentDataflow();
				ValidateSwingWorker validateSwingWorker = new ValidateSwingWorker(dataflow, false);
				ValidateInProgressDialog dialog = new ValidateInProgressDialog();
				validateSwingWorker.addPropertyChangeListener(
					     new SwingWorkerCompletionWaiter(dialog));
				validateSwingWorker.execute();
				
				// Give a chance to the SwingWorker to finish so we do not have to display 
				// the dialog if copying of the workflow is quick (so it won't flicker on the screen)
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// do nothing
				}
				if (!validateSwingWorker.isDone()){
					dialog.setVisible(true); // this will block the GUI
				}
			}
		});
		JButton fullCheckButton = new JButton("Full check");
		fullCheckButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ex) {
			    Dataflow dataflow = FileManager.getInstance()
				.getCurrentDataflow();
				ValidateSwingWorker validateSwingWorker = new ValidateSwingWorker(dataflow, true);
				ValidateInProgressDialog dialog = new ValidateInProgressDialog();
				validateSwingWorker.addPropertyChangeListener(
					     new SwingWorkerCompletionWaiter(dialog));
				validateSwingWorker.execute();
				
				// Give a chance to the SwingWorker to finish so we do not have to display 
				// the dialog if copying of the workflow is quick (so it won't flicker on the screen)
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// do nothing
				}
				if (!validateSwingWorker.isDone()){
					dialog.setVisible(true); // this will block the GUI
				}
			}
		});
		JPanel validateButtonPanel = new JPanel();
		validateButtonPanel.add(quickCheckButton);
		validateButtonPanel.add(fullCheckButton);
		this.add(validateButtonPanel, BorderLayout.SOUTH);

		explanationScrollPane.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent e) {
			}
			public void componentMoved(ComponentEvent e) {
			}
			public void componentResized(ComponentEvent e) {
			    if (lastSelectedReport != null) {
				updateExplanation(lastSelectedReport);
				updateMessages();
			    }
			}
			public void componentShown(ComponentEvent e) {
			}
		    });
		solutionScrollPane.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent e) {
			}
			public void componentMoved(ComponentEvent e) {
			}
			public void componentResized(ComponentEvent e) {
			    if (lastSelectedReport != null) {
				updateExplanation(lastSelectedReport);
				updateMessages();
			    }
			}
			public void componentShown(ComponentEvent e) {
			}
		    });
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
		reportViewTableModel = new ReportViewTableModel(reportEntries, (String) shownReports.getSelectedItem());
		if (sorter != null) {
		    sorter.setModel(reportViewTableModel);
		} else {
		    sorter = new TableSorter(reportViewTableModel);
		}
		sorter.sortByColumn(0, false); // sort by decreasing severity
		if (table == null) {
		    table = new JTable(sorter);
		    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		    table.setRowSelectionAllowed(true);
		    tableListener = new TableListener();
		    table.getSelectionModel().addListSelectionListener(
					tableListener);
		    table.setSurrendersFocusOnKeystroke(false);
		    table.getInputMap(JInternalFrame.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "configure");
		    table.getInputMap(JInternalFrame.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "configure");

		    table.getActionMap().put("configure", reportViewConfigureAction);

		    table.setDefaultRenderer(Status.class, new StatusRenderer());
		    sorter.addMouseListenerToHeaderInTable(table);
		    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		}
		packColumn(table, 0, TABLE_MARGIN, true);
		packColumn(table, 1, TABLE_MARGIN, true);
		packColumn(table, 2, TABLE_MARGIN, true);
		packColumn(table, 3, TABLE_MARGIN, true);
		packColumn(table, 4, TABLE_MARGIN, false);

		return table;
	}

	private void showReport(final Dataflow dataflow) {
		if (dataflow != null) {
			dataflowName.setText(dataflow.getLocalName());
		} else {
			dataflowName.setText("No workflow");
		}

		table = createTable(reportManager.getReports(dataflow));
		tableScrollPane.setViewportView(table);
		boolean found = false;
		for (int i = 0; i < table.getRowCount(); i++) {
			VisitReport vr = reportViewTableModel.getReport(sorter.transposeRow(i));
			if (vr.equals(lastSelectedReport)) {
				table.setRowSelectionInterval(i, i);
				found = true;
				break;
			}
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
	
	private void updateMessages() {
	    JPanel explainPanel = wrapComponent(explanation);
		explanationScrollPane.setViewportView(explainPanel);
		solutionScrollPane.setViewportView(wrapComponent(solution));
		messagePane.revalidate();
	}
	
	private final class ReportManagerObserver implements
			Observer<ReportManagerEvent> {
		public void notify(Observable<ReportManagerEvent> sender,
				ReportManagerEvent event) throws Exception {
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
				} else {
					System.err.println("Ignoring dataflow event");
				}
			}
		}
	}

	final class TableListener implements ListSelectionListener {
		
		public TableListener() {
		}
		
		public void valueChanged(ListSelectionEvent e) {
			int row = table.getSelectedRow();
			if (row >= 0) {
				DataflowSelectionModel dsm = DataflowSelectionManager.getInstance().getDataflowSelectionModel(FileManager.getInstance().getCurrentDataflow());
				dsm.clearSelection();
				VisitReport vr = reportViewTableModel.getReport(sorter.transposeRow(row));
				final Object subject = reportViewTableModel.getSubject(sorter.transposeRow(row));
				dsm.addSelection(subject);
				updateExplanation(vr);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateMessages();
						if (subject instanceof Processor) {
							reportViewConfigureAction.setConfiguredProcessor((Processor) subject);
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
		for (VisitExplainer ve : visitExplainerRegistry.getInstances()) {
			if (ve.canExplain(vr.getKind(), vr.getResultId())) {
			    try {
				explanation = ve.getExplanation(vr);
			    }
			    catch (ClassCastException e) {
				logger.error("Error creating explanation", e);
				explanation = null;
			    }
			    catch (NullPointerException e) {
				logger.error("Error creating explanation", e);
				explanation = null;
			    }
				if (explanation == null) {
					explanation = defaultExplanation;
				}
				try {
				    solution = ve.getSolution(vr);
				} catch (ClassCastException e) {
				    logger.error("Error creating soluttion", e);
				    solution = null;
				} catch (NullPointerException e) {
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
	
	private JPanel wrapComponent(JComponent c) {
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 0.9;
		result.add(c, gbc);
		gbc.weightx = 0.9;
		gbc.weighty = 0.9;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		result.add(new JPanel(), gbc);
		return result;
	}
}
