package dk.ku.cpr.proteinGroupsApp.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import dk.ku.cpr.proteinGroupsApp.internal.tasks.AboutTaskFactory;

public class CyActivator extends AbstractCyActivator {
	String JSON_EXAMPLE = "{\"SUID\":1234}";

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		// Get a handle on the CyServiceRegistrar
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		final Logger logger = Logger.getLogger(CyUserLog.NAME);

		// Get our version number
		Version v = bc.getBundle().getVersion();
		String version = v.toString(); // The full version

		{
			AboutTaskFactory aboutFactory = new AboutTaskFactory(version, registrar);
			Properties aboutProps = new Properties();
			// menu properties
			aboutProps.setProperty(PREFERRED_MENU, "Apps.ProteinGroups");
			aboutProps.setProperty(TITLE, "About");
			aboutProps.setProperty(MENU_GRAVITY, "3.0");
			aboutProps.setProperty(IN_MENU_BAR, "true");
			// command properties
			aboutProps.setProperty(COMMAND_NAMESPACE, "proteinGroups");
			aboutProps.setProperty(COMMAND, "about");
			aboutProps.setProperty(COMMAND_DESCRIPTION, "Return the about URL of ProteinGroupsApp.");
			aboutProps.setProperty(COMMAND_LONG_DESCRIPTION, "Returns the about URL of ProteinGroupsApp.");
			aboutProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			// versionProps.setProperty(COMMAND_EXAMPLE_JSON, "{\"version\":\"1.0.0\"}");
			registerService(bc, aboutFactory, TaskFactory.class, aboutProps);
		}


		logger.info("ProteinGroupsApp " + version + " initialized.");
		System.out.println("ProteinGroupsApp " + version + " initialized.");
	
	}
}