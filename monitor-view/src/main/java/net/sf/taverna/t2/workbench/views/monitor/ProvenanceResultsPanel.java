/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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

package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.views.results.RenderedResultComponent;

import org.apache.log4j.Logger;

/**
 * Designed to be used in conjunction with the provenance system in Taverna.
 * Displays a {@link JTabbedPane} with a tab for each port. Each of the tabs has
 * a table which shows the iterations. Each of the iterations has an associated
 * {@link T2Reference}. The results are rendered when clicked on. The results
 * are passed in within a {@link List} of {@link LineageQueryResultRecord}s.
 * Uses a {@link LineageResultsTableModel} as its internal model. A
 * {@link ReferenceRenderer} is used to show whether the {@link T2Reference} is
 * an error by colouring the cell red.
 * 
 * @author Ian Dunlop
 * 
 */
public class ProvenanceResultsPanel extends JPanel implements
		TableModelListener {

	private static Logger logger = Logger
			.getLogger(ProvenanceResultsPanel.class);

	private PortTab oldTab;

	private Map<String, PortTab> portTabMap = new HashMap<String, PortTab>();

	private Map<String, Map<String, T2Reference>> portMap = new HashMap<String, Map<String, T2Reference>>();

	private Map<String, Boolean> inputOutputMap = new HashMap<String, Boolean>();
	
	private List<LineageQueryResultRecord> lineageRecords;

	private JTable resultsTable;

	private InvocationContext context;

	private LineageResultsTableModel lineageResultsTableModel;

	private RenderedResultComponent renderedResultsComponent = new RenderedResultComponent();

	private JTabbedPane tabbedPane;

	public ProvenanceResultsPanel() {
	}

	public ProvenanceResultsPanel(
			List<LineageQueryResultRecord> lineageRecords,
			InvocationContext invocationContext) {
		super();
		this.lineageRecords = lineageRecords;
		this.setContext(invocationContext);
		initView();
		this.setMinimumSize(new Dimension(800,600));
	}

	private void initView() {

		removeAll();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createRaisedBevelBorder());
		// set up maps

		tabbedPane = new JTabbedPane();

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent
						.getSource();
				int index = sourceTabbedPane.getSelectedIndex();
				String titleAt = tabbedPane.getTitleAt(index);
				PortTab portTab = portTabMap.get(titleAt);
				if (oldTab != null) {

					oldTab.getResultsTable().clearSelection();
					oldTab.getResultsTable().getSelectionModel()
							.clearSelection();
					oldTab = portTab;
					oldTab.getResultsTable().clearSelection();
					oldTab.getResultsTable().getSelectionModel()
							.clearSelection();

				}
			}
		};
		tabbedPane.addChangeListener(changeListener);

		Set<Entry<String, Map<String, T2Reference>>> entrySet = portMap
				.entrySet();

		for (Entry<String, Map<String, T2Reference>> entry : entrySet) {

			Map<String, T2Reference> value = entry.getValue();
			String key = entry.getKey();
			createPortTab(key, value);
		}

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				tabbedPane, getRenderedResultsComponent());
		tabbedPane.setMinimumSize(new Dimension(300,300));
//		splitPane.setResizeWeight(1);
		add(new JLabel("Click on an iteration to view the result"));
		add(splitPane);
		getRenderedResultsComponent().setBorder(
				BorderFactory.createRaisedBevelBorder());

		revalidate();
//		splitPane.setDividerLocation(0.5);
		// remember what tab is currently selected - at init it is the first one
		String titleAt = tabbedPane.getTitleAt(0);
		PortTab portTab = portTabMap.get(titleAt);
		oldTab = portTab;

	}

	public void setLineageRecords(List<LineageQueryResultRecord> lineageRecords) {
		this.lineageRecords = lineageRecords;
		portMap.clear();
		inputOutputMap.clear();
		for (LineageQueryResultRecord record : lineageRecords) {
			boolean input = record.isInput();
			String vname = record.getVname();
			inputOutputMap.put(vname, input);
			String iteration = record.getIteration();
			String value = record.getValue();
			Map<String, T2Reference> map = portMap.get(vname);
			if (map == null) {
				map = new HashMap<String, T2Reference>();
				portMap.put(vname, map);
			}
			T2Reference referenceValue = getContext().getReferenceService()
					.referenceFromString(value);
			map.put(iteration, referenceValue);
		}

		if (tabbedPane == null) {
			initView();
		} else {
			Set<Entry<String, Map<String, T2Reference>>> entrySet = portMap
					.entrySet();

			for (Entry<String, Map<String, T2Reference>> entry : entrySet) {

				PortTab portTab = getPortTabMap().get(entry.getKey());
				if (portTab == null) {
					createPortTab(entry.getKey(), entry.getValue());
				} else {
					portTab.setPortMap(entry.getValue());
				}

			}
			// reset the selection since the results may have changed
			oldTab.getResultsTable().clearSelection();
			oldTab.getResultsTable().getSelectionModel().clearSelection();

		}

	}

	private void createPortTab(String key, Map<String, T2Reference> value) {
		PortTab portTab = new PortTab(key, value,
				getRenderedResultsComponent(), getContext());
		Boolean input = inputOutputMap.get(key);
		if (input) {
			ImageIcon inputPortIcon = WorkbenchIcons.inputIcon;
			tabbedPane.addTab(key, inputPortIcon, portTab, "Input port");		
		} else {
			ImageIcon outputPortIcon = WorkbenchIcons.outputIcon;
			tabbedPane.addTab(key, outputPortIcon, portTab, "Output port");	
		}
		portTabMap.put(key, portTab);

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

	public void setLineageResultsTableModel(
			LineageResultsTableModel lineageResultsTableModel) {
		this.lineageResultsTableModel = lineageResultsTableModel;
	}

	public LineageResultsTableModel getLineageResultsTableModel() {
		return lineageResultsTableModel;
	}

	public void tableChanged(TableModelEvent e) {
		resultsTable.revalidate();
	}

	public void setRenderedResultsComponent(
			RenderedResultComponent renderedResultsComponent) {
		this.renderedResultsComponent = renderedResultsComponent;
	}

	public RenderedResultComponent getRenderedResultsComponent() {
		return renderedResultsComponent;
	}

	public void setPortTabMap(Map<String, PortTab> portTabMap) {
		this.portTabMap = portTabMap;
	}

	public Map<String, PortTab> getPortTabMap() {
		return portTabMap;
	}

}
