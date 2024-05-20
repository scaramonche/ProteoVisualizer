package dk.ku.cpr.proteoVisualizer.internal;

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
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.group.events.GroupEdgesAddedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;
import dk.ku.cpr.proteoVisualizer.internal.model.SharedProperties;
import dk.ku.cpr.proteoVisualizer.internal.model.StringSpecies;
import dk.ku.cpr.proteoVisualizer.internal.tasks.AboutTaskFactory;
import dk.ku.cpr.proteoVisualizer.internal.tasks.ChangeGroupReprTaskFactory;
import dk.ku.cpr.proteoVisualizer.internal.tasks.CollapseGroupsTaskFactory;
import dk.ku.cpr.proteoVisualizer.internal.tasks.RetrieveStringNetworkTaskFactory;
import dk.ku.cpr.proteoVisualizer.internal.tasks.ShowRetrieveWindowTaskFactory;


public class CyActivator extends AbstractCyActivator {
	//String JSON_EXAMPLE = "{\"SUID\":1234}";

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		System.out.println("Starting Proteo Visualizer!");

		// Get a handle on the CyServiceRegistrar
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		final Logger logger = Logger.getLogger(CyUserLog.NAME);

		AppManager manager = new AppManager(registrar);
		manager.setGroupSettings();
		
		// Get our version number
		Version v = bc.getBundle().getVersion();
		String version = v.toString(); // The full version

		// load species AND registering the search task factory
		StringSpecies.loadSpecies(manager);
		
		{
			// Register our listeners
			registerService(bc, manager, GroupAboutToCollapseListener.class, new Properties());
			registerService(bc, manager, GroupCollapsedListener.class, new Properties());			
			registerService(bc, manager, GroupEdgesAddedListener.class, new Properties());
			// registerService(bc, manager, SelectedNodesAndEdgesListener.class, new Properties());
		}		
		
		{
			ShowRetrieveWindowTaskFactory retrieveFactory = new ShowRetrieveWindowTaskFactory(manager);
			Properties retrieveProps = new Properties();
			// menu properties
			retrieveProps.setProperty(PREFERRED_MENU, SharedProperties.APP_PREFERRED_MENU);
			retrieveProps.setProperty(TITLE, "Retrieve STRING network");
			retrieveProps.setProperty(MENU_GRAVITY, "1.0");
			retrieveProps.setProperty(IN_MENU_BAR, "true");
			registerService(bc, retrieveFactory, TaskFactory.class, retrieveProps);
		}

		
		{
			RetrieveStringNetworkTaskFactory retrieveFactory = new RetrieveStringNetworkTaskFactory(manager);
			Properties retrieveProps = new Properties();
			// command properties
			retrieveProps.setProperty(COMMAND_NAMESPACE, SharedProperties.APP_COMMAND_NAMESPACE);
			retrieveProps.setProperty(COMMAND, "retrieve string");
			retrieveProps.setProperty(COMMAND_DESCRIPTION, "Retrieve STRING network with given settings and input.");
			retrieveProps.setProperty(COMMAND_LONG_DESCRIPTION, "Retrieve STRING network with given settings and input.");
			retrieveProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			// versionProps.setProperty(COMMAND_EXAMPLE_JSON, "{\"version\":\"1.0.0\"}");
			registerService(bc, retrieveFactory, TaskFactory.class, retrieveProps);
		}
		
		{
			CollapseGroupsTaskFactory collapseFactory = new CollapseGroupsTaskFactory(manager);
			Properties collapseProps = new Properties();
			// menu properties
			collapseProps.setProperty(PREFERRED_MENU, SharedProperties.APP_PREFERRED_MENU);
			collapseProps.setProperty(TITLE, "Collapse all groups");
			collapseProps.setProperty(MENU_GRAVITY, "3.0");
			collapseProps.setProperty(IN_MENU_BAR, "true");
			registerService(bc, collapseFactory, NetworkTaskFactory.class, collapseProps);
		}

		{
			// Register the Change repr factory as a node view task factory (appears on right click)
			ChangeGroupReprTaskFactory changeRrpr = new ChangeGroupReprTaskFactory(manager);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, SharedProperties.APP_PREFERRED_MENU);
			props.setProperty(TITLE, "Change group representative");
			props.setProperty(MENU_GRAVITY, "1.0");
			props.setProperty(IN_MENU_BAR, "false");
			registerService(bc, changeRrpr, NodeViewTaskFactory.class, props);

			// Don't think this one is needed
			//Properties props = new Properties();
			//props.setProperty(PREFERRED_MENU, SharedProperties.APP_PREFERRED_MENU);
			//props.setProperty(TITLE, "Change group representative");
			//props.setProperty(MENU_GRAVITY, "1.0");
			//props.setProperty(IN_MENU_BAR, "true");
			//registerService(bc, changeRrpr, NetworkTaskFactory.class, props);

			// TODO: can we have the change repr as command?
			// it probably needs to be a different task
			//Properties changeReprProps = new Properties();
			//changeReprProps.setProperty(COMMAND_NAMESPACE, SharedProperties.APP_COMMAND_NAMESPACE);
			//changeReprProps.setProperty(COMMAND, "change repr");
			//changeReprProps.setProperty(COMMAND_DESCRIPTION, "Change group representative");
			//changeReprProps.setProperty(COMMAND_LONG_DESCRIPTION, "");
			//changeReprProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			//changeReprProps.setProperty(COMMAND_EXAMPLE_JSON, JSON_EXAMPLE);			
			//registerService(bc, changeRrpr, TaskFactory.class, changeReprProps);			
		}

		{
			AboutTaskFactory aboutFactory = new AboutTaskFactory(version, registrar);
			Properties aboutProps = new Properties();
			// menu properties
			aboutProps.setProperty(PREFERRED_MENU, SharedProperties.APP_PREFERRED_MENU);
			aboutProps.setProperty(TITLE, "About");
			aboutProps.setProperty(MENU_GRAVITY, "10.0");
			aboutProps.setProperty(IN_MENU_BAR, "true");
			// command properties
			aboutProps.setProperty(COMMAND_NAMESPACE, SharedProperties.APP_COMMAND_NAMESPACE);
			aboutProps.setProperty(COMMAND, "about");
			aboutProps.setProperty(COMMAND_DESCRIPTION, "Return the about URL of Proteo Visualizer.");
			aboutProps.setProperty(COMMAND_LONG_DESCRIPTION, "Returns the about URL of Proteo Visualizer.");
			aboutProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			// versionProps.setProperty(COMMAND_EXAMPLE_JSON, "{\"version\":\"1.0.0\"}");
			registerService(bc, aboutFactory, TaskFactory.class, aboutProps);
		}

		logger.info("Proteo Visualizer " + version + " initialized.");
		System.out.println("Proteo Visualizer " + version + " initialized.");
	}
}
