/**
 * 
 */
package net.sf.taverna.t2.workbench.report.view;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/**
 * @author alanrw
 *
 */
public class ReportViewComponentFactory implements UIComponentFactorySPI {


	public UIComponentSPI getComponent() {
		return new ReportViewComponent();
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Reports";
	}

}
