package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
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

	private ContextualView view;
	private JPanel configureButtonPanel = new JPanel();
	private JButton configureButton = new JButton("Configure",
			WorkbenchIcons.configureIcon);

	/** Keep list of views in case you want to go back or forward between them */
	private List<ContextualView> views = new ArrayList<ContextualView>();
	private JPanel panel;
	private JLabel title;

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

		setLayout(new BorderLayout());

		panel = new JPanel(new BorderLayout());
		add(panel, BorderLayout.CENTER);

		title = new JLabel("Contextual View"); // the actual title will be
												// changed later based on the
												// workflow object being
												// displayed
		title.setMinimumSize(new Dimension(0, 0));// so that the label can
													// shrink (and contextual
													// view together with it)
		title.setBorder(new EmptyBorder(0, 5, 5, 5));
		add(title, BorderLayout.NORTH);

		configureButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		configureButtonPanel.add(configureButton);
		configureButton.setEnabled(false);
		configureButton.setVisible(false);
		add(configureButtonPanel, BorderLayout.SOUTH);
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	private void updateContextualView(ContextualView view) {
		if (this.view != null) {
			panel.remove(this.view);
		}
		this.view = view;
		views.add(view);
		title.setText(view.getViewTitle());

		panel.add(view, BorderLayout.CENTER);

		// Show/hide the 'Configure' button and its panel as needed
		Action configureAction = view.getConfigureAction(Utils
				.getParentFrame(this));
		if (configureAction != null) {
			configureButtonPanel.setVisible(true);
			configureButton.setAction(configureAction);
			if ((configureButton.getText() == null || configureButton.getText()
					.equals(""))
					&& configureButton.getIcon() == null) {
				configureButton.setText("Configure");
				configureButton.setIcon(WorkbenchIcons.configureIcon);
			}
			configureButton.setEnabled(true);
			configureButton.setVisible(true);
		} else {
			configureButtonPanel.setVisible(false);
			configureButton.setEnabled(false);
			configureButton.setVisible(false);
		}
		revalidate();
		repaint();
	}

	private void clearContextualView() {

		// Remove the contextual view panel
		if (this.view != null) {
			panel.remove(this.view);
			title.setText("<html><i>No object selected.</i></html>");
		}

		// Hide the 'Configure' button and its panel
		configureButtonPanel.setVisible(false);
		configureButton.setVisible(false);

		revalidate();
		repaint();
	}

	public void updateSelection(Object selectedItem) {

		// if (selectedItem instanceof Processor) {
		// Processor processor = (Processor) selectedItem;
		// Activity<?> activity = processor.getActivityList().get(0);
		// findContextualView(activity);
		// } else {
		findContextualView(selectedItem);
		// }

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
		ContextualViewFactory viewFactoryForBeanType = reg
				.getViewFactoryForObject(selection);
		ContextualView viewType = viewFactoryForBeanType.getView(selection);
		updateContextualView(viewType);
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
		if (view != null) {
			view.refreshView();
			title.setText(view.getViewTitle());
			repaint();
		}
	}
}
