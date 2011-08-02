/**
 *
 */
package net.sf.taverna.t2.workbench.report.view;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
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
	private DataflowSelectionManager dataflowSelectionManager;
	private MenuManager menuManager;

	public UIComponentSPI getComponent() {
		return new ReportViewComponent(editManager, fileManager, menuManager, reportManager, workbench, dataflowSelectionManager);
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

	public void setDataflowSelectionManager(DataflowSelectionManager dataflowSelectionManager) {
		this.dataflowSelectionManager = dataflowSelectionManager;
	}

}
