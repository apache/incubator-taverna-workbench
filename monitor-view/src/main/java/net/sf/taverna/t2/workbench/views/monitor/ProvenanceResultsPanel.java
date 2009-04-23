package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.views.results.RenderedResultComponent;
import net.sf.taverna.t2.workbench.views.results.ResultTreeNode;

import org.apache.log4j.Logger;

public class ProvenanceResultsPanel extends JPanel {

	static Logger logger = Logger.getLogger(ProvenanceResultsPanel.class);

	private List<LineageQueryResultRecord> lineageRecords;

	private JTable resultsTable;

	private InvocationContext context;

	private LineageResultsTableModel lineageResultsTableModel;

	private RenderedResultComponent renderedResutlsComponent = new RenderedResultComponent();

	public ProvenanceResultsPanel() {
	}

	public ProvenanceResultsPanel(
			List<LineageQueryResultRecord> lineageRecords,
			InvocationContext invocationContext) {
		super();
		this.lineageRecords = lineageRecords;
		this.setContext(invocationContext);
		initView();
	}

	private void initView() {

		removeAll();

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

//		setLineageResultsTableModel(new LineageResultsTableModel());
		resultsTable = new JTable(getLineageResultsTableModel());
//		resultsTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
		resultsTable.getSelectionModel().addListSelectionListener(
				new RowListener());
		// add(new JScrollPane(resultsTable));
		// add(renderedResutlsComponent);
		JPanel panel = new JPanel(new FlowLayout());

		panel.add(resultsTable);
		panel.add(renderedResutlsComponent);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.add(panel);
//		add(scrollPane);
		add(panel);
		revalidate();

	}

	private class RowListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			int rowSelectionIndex = resultsTable.getSelectionModel()
					.getLeadSelectionIndex();
			int columnSelectionIndex = resultsTable.getColumnModel()
					.getSelectionModel().getLeadSelectionIndex();

			String valueAt = lineageRecords.get(rowSelectionIndex).getValue();
			logger.info("trying to construct: " + valueAt);

			T2Reference referenceFromString = getContext()
					.getReferenceService().referenceFromString(valueAt);
			ResultTreeNode node = new ResultTreeNode(referenceFromString,
					getContext());
			renderedResutlsComponent.setNode(node);

		}
	}

	public void setLineageRecords(List<LineageQueryResultRecord> lineageRecords) {
		this.lineageRecords = lineageRecords;
		// FIXME this bit is a hack, need a smarter way of instantiating this
		// group of prov results objects.  this way assumes that context is set - not clever
		if (lineageResultsTableModel == null) {
			lineageResultsTableModel = new LineageResultsTableModel(
					lineageRecords, context);
			initView();
		} else {
			lineageResultsTableModel.setLineageRecords(lineageRecords);			
		}
		
	}

	public List<LineageQueryResultRecord> getLineageRecords() {
		return lineageRecords;
	}

	public void setContext(InvocationContext context) {
		this.context = context;
	}

	public InvocationContext getContext() {
		return context;
	}

	public void setLineageResultsTableModel(LineageResultsTableModel lineageResultsTableModel) {
		this.lineageResultsTableModel = lineageResultsTableModel;
	}

	public LineageResultsTableModel getLineageResultsTableModel() {
		return lineageResultsTableModel;
	}

}
