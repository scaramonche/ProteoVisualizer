package dk.ku.cpr.proteinGroupsApp.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
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
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.CyGroupSettingsManager.DoubleClickAction;
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.group.events.GroupEdgesAddedListener;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import dk.ku.cpr.proteinGroupsApp.internal.model.AppManager;
import dk.ku.cpr.proteinGroupsApp.internal.model.SharedProperties;
import dk.ku.cpr.proteinGroupsApp.internal.tasks.AboutTaskFactory;
import dk.ku.cpr.proteinGroupsApp.internal.tasks.ShowRetrieveWindowTaskFactory;

public class CyActivator extends AbstractCyActivator {
	String JSON_EXAMPLE = "{\"SUID\":1234}";

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		System.out.println("Starting Protein Groups App!");

		// Get a handle on the CyServiceRegistrar
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		final Logger logger = Logger.getLogger(CyUserLog.NAME);

		AppManager manager = new AppManager(registrar);

		
		// Get our version number
		Version v = bc.getBundle().getVersion();
		String version = v.toString(); // The full version

		{
			// Register our listeners
			registerService(bc, manager, GroupAboutToCollapseListener.class, new Properties());
			registerService(bc, manager, GroupCollapsedListener.class, new Properties());			
			registerService(bc, manager, GroupEdgesAddedListener.class, new Properties());
			registerService(bc, manager, SelectedNodesAndEdgesListener.class, new Properties());
		}

		{
			// Set some properties to the groups app
			CyGroupSettingsManager groupSettingsManager = getService(bc, CyGroupSettingsManager.class);
			groupSettingsManager.setGroupViewType(GroupViewType.COMPOUND);
			groupSettingsManager.setEnableAttributeAggregation(false);
			groupSettingsManager.setDoubleClickAction(DoubleClickAction.EXPANDCONTRACT);
		}
		
		
		{
			ShowRetrieveWindowTaskFactory retrieveFactory = new ShowRetrieveWindowTaskFactory(manager);
			Properties retrieveProps = new Properties();
			// menu properties
			retrieveProps.setProperty(PREFERRED_MENU, SharedProperties.APP_PREFERRED_MENU);
			retrieveProps.setProperty(TITLE, "Retrieve STRING network");
			retrieveProps.setProperty(MENU_GRAVITY, "1.0");
			retrieveProps.setProperty(IN_MENU_BAR, "true");

			// TODO: use a different task factory in this case
			// command properties
			//retrieveProps.setProperty(COMMAND_NAMESPACE, SharedProperties.APP_COMMAND_NAMESPACE);
			//retrieveProps.setProperty(COMMAND, "retrieve string");
			//retrieveProps.setProperty(COMMAND_DESCRIPTION, "Retrieve STRING network with given settings and input.");
			//retrieveProps.setProperty(COMMAND_LONG_DESCRIPTION, "Retrieve STRING network with given settings and input.");
			//retrieveProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			// versionProps.setProperty(COMMAND_EXAMPLE_JSON, "{\"version\":\"1.0.0\"}");
			registerService(bc, retrieveFactory, TaskFactory.class, retrieveProps);
		}

		{
			AboutTaskFactory aboutFactory = new AboutTaskFactory(version, registrar);
			Properties aboutProps = new Properties();
			// menu properties
			aboutProps.setProperty(PREFERRED_MENU, SharedProperties.APP_PREFERRED_MENU);
			aboutProps.setProperty(TITLE, "About");
			aboutProps.setProperty(MENU_GRAVITY, "3.0");
			aboutProps.setProperty(IN_MENU_BAR, "true");
			// command properties
			aboutProps.setProperty(COMMAND_NAMESPACE, SharedProperties.APP_COMMAND_NAMESPACE);
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