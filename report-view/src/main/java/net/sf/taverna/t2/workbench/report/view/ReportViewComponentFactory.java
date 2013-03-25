/**
 *
 */
package net.sf.taverna.t2.workbench.report.view;

import java.util.List;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.report.explainer.VisitExplainer;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

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
