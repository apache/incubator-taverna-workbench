package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.Utils;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;

@SuppressWarnings("serial")
public class ContextualViewComponent extends JPanel implements UIComponentSPI {

	private Observer<DataflowSelectionMessage> dataflowSelectionListener = new DataflowSelectionListener();

	private FileManager fileManager = FileManager.getInstance();
	private EditManager editManager = EditManager.getInstance();
	private EditManagerObserver editManagerObserver = new EditManagerObserver();
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();
	private DataflowSelectionManager dataflowSelectionManager = DataflowSelectionManager
	.getInstance();

	/** Keep list of views in case you want to go back or forward between them */
//	private List<ContextualView> views = new ArrayList<ContextualView>();
	
	GridBagConstraints gbc;

	protected Map<JPanel, SectionLabel> closeables = new HashMap<JPanel, SectionLabel>();
	
	private int openChild = -1;

	private JPanel mainPanel;
	
	private List<JPanel> shownComponents = null;
	
	private Object lastSelection = null;
	
	private static Comparator<ContextualView> viewComparator = new Comparator<ContextualView> () {

		public int compare(ContextualView o1, ContextualView o2) {
			return (o1.getPreferredPosition() - o2.getPreferredPosition());
		}};
		
	private Color[] colors = new Color[] {ShadedLabel.BLUE, ShadedLabel.GREEN, ShadedLabel.ORANGE};
	int colorIndex = 0;

	public ContextualViewComponent() {
		Dataflow currentDataflow = fileManager.getCurrentDataflow();

		DataflowSelectionModel selectionModel = dataflowSelectionManager
				.getDataflowSelectionModel(currentDataflow);
		selectionModel.addObserver(dataflowSelectionListener);

		editManager.addObserver(editManagerObserver);
		fileManager.addObserver(fileManagerObserver);
		initialise();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Contextual View";
	}

	private void initialise() {
		this.setLayout(new BorderLayout());
		mainPanel = new JPanel(new GridBagLayout());
		this.add(mainPanel, BorderLayout.NORTH);
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	private void updateContextualView(List<ContextualViewFactory> viewFactoriesForBeanType, Object selection) {
		mainPanel.removeAll();
		closeables.clear();

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		gbc.gridy = 0;
		JPanel firstPanel = null;
		shownComponents = new ArrayList<JPanel>();
		List<ContextualView> views = new ArrayList<ContextualView>();
		for (ContextualViewFactory cvf: viewFactoriesForBeanType) {
			views.addAll(cvf.getViews(selection));
		}
		Collections.sort(views, viewComparator);
		colorIndex = 0;
		if (!views.isEmpty()) {
			for (ContextualView view : views) {
				SectionLabel label = new SectionLabel(view.getViewTitle(),
						nextColor());
				mainPanel.add(label, gbc);
				gbc.gridy++;
				JPanel subPanel = new JPanel();
				subPanel.setLayout(new GridBagLayout());

				GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 0;
				constraints.gridy = 0;
				constraints.weightx = 0.1;
				constraints.weighty = 0;
				constraints.anchor = GridBagConstraints.CENTER;
				constraints.fill = GridBagConstraints.HORIZONTAL;

				subPanel.add(view, constraints);
				Frame frame = Utils.getParentFrame(this);
				Action configureAction = view.getConfigureAction(frame);
				if (configureAction != null) {
					JButton configureButton = new JButton(configureAction);
					if (configureButton.getText() == null
							|| configureButton.getText().equals("")) {
						configureButton.setText("Configure");
					}
					constraints.gridy++;
					constraints.fill = GridBagConstraints.NONE;
					constraints.anchor = GridBagConstraints.LINE_START;
					subPanel.add(configureButton, constraints);
				}
				if (firstPanel == null) {
					firstPanel = subPanel;
				}
				mainPanel.add(subPanel, gbc);
				shownComponents.add(subPanel);
				gbc.gridy++;
				if (viewFactoriesForBeanType.size() != 1) {
					makeCloseable(subPanel, label);
				}
			}
			if ((lastSelection != selection)
					|| ((openChild == -1) && (firstPanel != null))) {
				openSection(firstPanel);
			} else {
				openSection(shownComponents.get(openChild));
			}
		} else {
			mainPanel.add(new JLabel("No details available"));
		}
		gbc.weighty = 0.1;
		gbc.fill = GridBagConstraints.BOTH;
		mainPanel.add(new JPanel(), gbc);
		lastSelection = selection;
		mainPanel.revalidate();
		mainPanel.repaint();
		this.revalidate();
		this.repaint();
	}

	private void clearContextualView() {

		// Remove the contextual view panel
		if (this.mainPanel != null) {
			mainPanel.removeAll();
			mainPanel.add(new JLabel("No details available"));
		}
		revalidate();
		repaint();
	}

	public void updateSelection(Object selectedItem) {

		findContextualView(selectedItem);

	}

	public void updateSelection() {

		Dataflow dataflow = fileManager.getCurrentDataflow();

		// If there is no currently opened dataflow,
		// clear the contextual view panel
		if (dataflow == null) {
			clearContextualView();
		} else {
			DataflowSelectionModel selectionModel = dataflowSelectionManager
					.getDataflowSelectionModel(dataflow);
			Set<Object> selection = selectionModel.getSelection();

			// If the dataflow is opened but no component of the dataflow is
			// selected, clear the contextual view panel
			if (selection.isEmpty()) {
				clearContextualView();
			} else {
				Iterator<Object> iterator = selection.iterator();
				// TODO multiple selections, dataflow contextual view, datalink
				// contextual view
				updateSelection(iterator.next());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void findContextualView(Object selection) {
		ContextualViewFactoryRegistry reg = ContextualViewFactoryRegistry
				.getInstance();
		List<ContextualViewFactory> viewFactoriesForBeanType = reg
				.getViewFactoriesForObject(selection);
		updateContextualView(viewFactoriesForBeanType, selection);
	}

	private final class FileManagerObserver implements
			Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent event) throws Exception {
			if (event instanceof SetCurrentDataflowEvent) {
				Dataflow dataflow = ((SetCurrentDataflowEvent) event)
						.getDataflow();
				if (dataflow != null) {
					dataflowSelectionManager
							.getDataflowSelectionModel(dataflow).addObserver(
									dataflowSelectionListener);
				}
				updateSelection();
			}
		}
	}

	private final class DataflowSelectionListener implements
			Observer<DataflowSelectionMessage> {

		public void notify(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) throws Exception {
			updateSelection();
		}

	}

	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {

		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			refreshView();
		}

	}

	public void refreshView() {
		if (mainPanel != null) {
			updateSelection();
		}
	}
	
	private final class SectionLabel extends ShadedLabel {
		private JLabel expand;

		private SectionLabel(String text, Color colour) {
			super(text, colour);
			expand = new JLabel(WorkbenchIcons.minusIcon);
			add(expand, 0);
			setExpanded(true);
		}
		public void setExpanded(boolean expanded) {
			if (expanded) {
				expand.setIcon(WorkbenchIcons.minusIcon);
			} else {
				expand.setIcon(WorkbenchIcons.plusIcon);
			}
		}
	}

	private void makeCloseable(JPanel panel, SectionLabel label) {
		panel.setVisible(false);
		if (closeables.get(panel) != label) {
			closeables.put(panel, label);
			// Only add mouse listener once
			label.addMouseListener(new SectionOpener(panel));
		}
	}
	
	protected class SectionOpener extends MouseAdapter {

		private final JPanel sectionToOpen;

		public SectionOpener(JPanel sectionToOpen) {
			this.sectionToOpen = sectionToOpen;
		}

		public void mouseClicked(MouseEvent e) {
			openSection(sectionToOpen);
		}
	}
	
	public synchronized void openSection(JPanel sectionToOpen) {
		openChild = -1;
		for (Entry<JPanel, SectionLabel> entry : closeables.entrySet()) {
			JPanel section = entry.getKey();
			SectionLabel sectionLabel = entry.getValue();
			
			if (section != sectionToOpen) {
				section.setVisible(false);
			} else {
				section.setVisible(! section.isVisible());
				if (section.isVisible()) {
					openChild = shownComponents.indexOf(sectionToOpen);
				}
			}
			sectionLabel.setExpanded(section.isVisible());
		}
	}
	
	private Color nextColor () {
		if (colorIndex >= colors.length) {
			colorIndex = 0;
		}
		return colors[colorIndex++];
	}
}
