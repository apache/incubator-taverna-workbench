/**
 *
 */
package org.apache.taverna.workbench.report.view;

import java.util.List;

import javax.swing.ImageIcon;

import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.report.explainer.VisitExplainer;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.Workbench;
import org.apache.taverna.workbench.ui.zaria.UIComponentFactorySPI;
import org.apache.taverna.workbench.ui.zaria.UIComponentSPI;

/**
 * @author alanrw
 *
 */
public class ReportViewComponentFactory implements UIComponentFactorySPI {

	private EditManager editManager;
	private FileManager fileManager;
	private ReportManager reportManager;
	private Workbench workbench;
	private SelectionManager selectionManager;
	private MenuManager menuManager;
	private List<VisitExplainer> visitExplainers;

	public UIComponentSPI getComponent() {
		return new ReportViewComponent(editManager, fileManager, menuManager, reportManager,
				workbench, selectionManager, visitExplainers);
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Reports";
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setReportManager(ReportManager reportManager) {
		this.reportManager = reportManager;
	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setVisitExplainers(List<VisitExplainer> visitExplainers) {
		this.visitExplainers = visitExplainers;
	}

}
