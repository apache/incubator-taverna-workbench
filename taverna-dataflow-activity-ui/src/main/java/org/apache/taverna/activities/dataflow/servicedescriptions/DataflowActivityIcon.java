/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.activities.dataflow.servicedescriptions;

import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.taverna.workbench.activityicons.ActivityIconSPI;

/**
 *
 * @author Alex Nenadic
 * @author alanrw
 *
 */
public class DataflowActivityIcon implements ActivityIconSPI{

	private static Icon icon;

	public int canProvideIconScore(URI activityType) {
		if (DataflowTemplateService.ACTIVITY_TYPE.equals(activityType))
			return DEFAULT_ICON + 1;
		else
			return NO_ICON;
	}

	public Icon getIcon(URI activityType) {
		return getDataflowIcon();
	}

	public static Icon getDataflowIcon() {
		if (icon == null) {
			icon = new ImageIcon(DataflowActivityIcon.class.getResource("/dataflow.png"));
		}
		return icon;
	}
}
