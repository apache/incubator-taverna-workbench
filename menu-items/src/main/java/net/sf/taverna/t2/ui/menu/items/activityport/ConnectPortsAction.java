package net.sf.taverna.t2.ui.menu.items.activityport;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

public class ConnectPortsAction extends AbstractAction {
	private final Dataflow dataflow;
	private final ActivityInputPort inputPort;
	private final OutputPort outputPort;

	private static Logger logger = Logger.getLogger(ConnectPortsAction.class);

	public ConnectPortsAction(Dataflow dataflow, ActivityInputPort inputPort,
			OutputPort outputPort) {
		super("Connect " + inputPort.getName() + " to " + outputPort.getName());
		this.dataflow = dataflow;
		this.inputPort = inputPort;
		this.outputPort = outputPort;
	}

	public void actionPerformed(ActionEvent e) {
		Edit<?> edit = Tools.getCreateAndConnectDatalinkEdit(dataflow,
				outputPort, inputPort);
		try {
			EditManager.getInstance().doDataflowEdit(dataflow, edit);
		} catch (EditException ex) {
			logger.warn("Can't create connection between " + inputPort
					+ " and " + outputPort, ex);
		}
	}
}
