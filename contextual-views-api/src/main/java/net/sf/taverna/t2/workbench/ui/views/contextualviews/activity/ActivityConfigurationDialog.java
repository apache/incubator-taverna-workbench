package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.EditManager.DataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowUndoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowRedoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import org.apache.log4j.Logger;

public class ActivityConfigurationDialog<A extends Activity, B extends Object>
		extends JDialog {

	private A activity;
	private ActivityConfigurationPanel<A, B> panel;
	private Dataflow owningDataflow;
	private Processor owningProcessor;

	private Observer<EditManagerEvent> observer;

	private static Logger logger = Logger
			.getLogger(ActivityConfigurationDialog.class);

	public ActivityConfigurationDialog(A a, ActivityConfigurationPanel<A, B> p) {
		super((Frame) null, "", false, null);
		this.activity = a;
		this.panel = p;

		owningDataflow = FileManager.getInstance().getCurrentDataflow();
		owningProcessor = findProcessor(owningDataflow, a);

		this.setTitle(getRelativeName(owningDataflow, activity));
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());

		add(panel, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		JButton applyButton = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				// For the moment it always does an apply as what should be
				// happening is that the apply button only becomes available
				// when the configuration has changed. However, many
				// configuration panels are not set up to detected changes
//				if (panel.isConfigurationChanged()) {
				if (panel.checkValues()) {
					applyConfiguration();
				}
//				} else {
//					logger.info("Ignoring apply");
//				}
			}

		});
		applyButton.setText("Apply");

		buttonPanel.add(applyButton);
		JButton closeButton = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				closeDialog();
			}
		});
		closeButton.setText("Close");
		buttonPanel.add(closeButton);
		add(buttonPanel, BorderLayout.SOUTH);

		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				closeDialog();
			}
		});
		this.pack();

		observer = new Observer<EditManagerEvent>() {

			public void notify(Observable<EditManagerEvent> sender,
					EditManagerEvent message) throws Exception {
				logger.info("sender is a "
						+ sender.getClass().getCanonicalName());
				logger.info("message is a "
						+ message.getClass().getCanonicalName());
				Edit edit = message.getEdit();
				logger.info(edit.getClass().getCanonicalName());
				considerEdit(message, edit);
			}
		};
		EditManager.getInstance().addObserver(observer);
	}

	private void considerEdit(EditManagerEvent message, Edit edit) {
		boolean result = false;
		if (edit instanceof CompoundEdit) {
			for (Edit subEdit : ((CompoundEdit) edit).getChildEdits()) {
				considerEdit(message, subEdit);
			}
		} else {
			Object subject = edit.getSubject();
			if (subject == owningProcessor) {
				// panel.reevaluate();
				setTitle(getRelativeName(owningDataflow, activity));
			} else if (subject == owningDataflow) {
				if (!owningDataflow.getProcessors().contains(owningProcessor)) {
					ActivityConfigurationAction.clearDialog(activity);
				}
			} else if (subject == activity) {
				if (message instanceof DataFlowUndoEvent) {
					logger.info("undo of activity edit found");
					panel.refreshConfiguration();
				} else if (message instanceof DataFlowRedoEvent) {
					logger.info("redo of activity edit found");
					panel.refreshConfiguration();
				}
			}
		}
	}

	protected void configureActivity(B configurationBean) {
		configureActivity(owningDataflow, activity, configurationBean);
	}

	public static void configureActivity(Dataflow df, Activity a, Object bean) {
		Edits edits = EditsRegistry.getEdits();
		Edit<?> configureActivityEdit = edits.getConfigureActivityEdit(a, bean);
		try {
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			editList.add(configureActivityEdit);
			Processor p = findProcessor(df, a);
			if (p != null && p.getActivityList().size() == 1) {
				editList.add(edits.getMapProcessorPortsForActivityEdit(p));
			}
			EditManager.getInstance().doDataflowEdit(df,
					new CompoundEdit(editList));
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		} catch (EditException e) {
			logger.error(e);
		}
	}

	protected static Processor findProcessor(Dataflow df, Activity activity) {
		for (Processor processor : df.getProcessors()) {
			if (processor.getActivityList().contains(activity))
				return processor;
		}
		return null;
	}

	public static String getRelativeName(Dataflow df, Activity activity) {
		String result = "";
		if (df != null) {
			result += df.getLocalName();
			Processor p = findProcessor(df, activity);
			if (p != null) {
				result += (":" + p.getLocalName());
			}
		}
		return result;
	}

	public boolean closeDialog() {

		if (panel.isConfigurationChanged()) {
			String relativeName = getRelativeName(owningDataflow, activity);
			int answer = JOptionPane.showConfirmDialog(this,
					"Do you want to save the configuration of " + relativeName,
					relativeName, JOptionPane.YES_NO_CANCEL_OPTION);
			if (answer == JOptionPane.YES_OPTION) {
				applyConfiguration();
			} else if (answer == JOptionPane.CANCEL_OPTION) {
				return false;
			}
		}
		ActivityConfigurationAction.clearDialog(activity);

		return true;
	}

	private void applyConfiguration() {
		panel.noteConfiguration();
		configureActivity(panel.getConfiguration());
	}

	public void dispose() {
		super.dispose();
		EditManager.getInstance().removeObserver(observer);
	}
}
