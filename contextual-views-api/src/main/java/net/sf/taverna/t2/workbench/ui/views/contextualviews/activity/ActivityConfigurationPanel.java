/**
 *
 */
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.common.URITools;
import uk.org.taverna.scufl2.api.common.WorkflowBean;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.port.ActivityPort;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.property.PropertyException;
import uk.org.taverna.scufl2.api.property.PropertyLiteral;
import uk.org.taverna.scufl2.api.property.PropertyResource;
import uk.org.taverna.scufl2.api.property.PropertyResource.PropertyComparator;

/**
 * @author alanrw
 *
 */
public abstract class ActivityConfigurationPanel extends JPanel {

	private static final Logger logger = Logger.getLogger(ActivityConfigurationPanel.class);

	private final PropertyComparator propertyComparator = new PropertyComparator();
	protected final Scufl2Tools scufl2Tools = new Scufl2Tools();
	protected final URITools uriTools = new URITools();
	protected final Activity activity;
	protected Configuration configuration;
	private PropertyResource propertyResource;

	public ActivityConfigurationPanel(Activity activity) {
		this.activity = activity;
		configuration = scufl2Tools.configurationFor(activity, activity.getParent());
		propertyResource = configuration.getPropertyResource();
	}

	protected abstract void initialise();

	public abstract boolean checkValues();

	public abstract void noteConfiguration();

	public  boolean isConfigurationChanged() {
		return propertyComparator.compare(configuration.getPropertyResource(), propertyResource) != 0;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public PropertyResource getPropertyResource() {
		return propertyResource;
	}

	public void setPropertyResource(PropertyResource propertyResource) {
		this.propertyResource = propertyResource;
	}

	public void refreshConfiguration() {
		configuration = scufl2Tools.configurationFor(activity, activity.getParent());
		propertyResource = configuration.getPropertyResource();
		initialise();
	}

    public void whenOpened() {
    }

	public void whenClosed() {
	}

	protected String getProperty(String name) {
		try {
			return propertyResource.getPropertyAsString(configuration.getType().resolve("#" + name));
		} catch (PropertyException e) {
			return "";
		}
	}

	protected void setProperty(String name, String value) {
		try {
			propertyResource.getPropertyAsLiteral(configuration.getType().resolve("#" + name)).setLiteralValue(value);
		} catch (PropertyException e) {
			propertyResource.addPropertyAsString(configuration.getType().resolve("#" + name), value);
		}
	}

	protected List<PropertyResource> getPortDefinitions(Collection<? extends ActivityPort> activityPorts) {
		List<PropertyResource> portDefinitions = new ArrayList<PropertyResource>();
		for (ActivityPort activityPort : activityPorts) {
			try {
				PropertyResource portDefinition = scufl2Tools.portDefinitionFor(activityPort, activityPort.getParent().getParent());
				portDefinition.addProperty(Scufl2Tools.PORT_DEFINITION.resolve("#name"), new PropertyLiteral(activityPort.getName()));
				portDefinition.addProperty(Scufl2Tools.PORT_DEFINITION.resolve("#depth"), new PropertyLiteral(activityPort.getDepth()));
				portDefinitions.add(portDefinition);
			} catch (PropertyException e) {
				e.printStackTrace();
				logger.warn("No port definition for " + activityPort, e);
			}
		}
		return portDefinitions;
	}

}
