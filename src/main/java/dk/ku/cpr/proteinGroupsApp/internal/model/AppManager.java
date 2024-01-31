package dk.ku.cpr.proteinGroupsApp.internal.model;

import java.util.Properties;

import org.cytoscape.group.events.GroupAboutToCollapseEvent;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedEvent;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.group.events.GroupEdgesAddedEvent;
import org.cytoscape.group.events.GroupEdgesAddedListener;
import org.cytoscape.model.CyTable;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

public class AppManager implements GroupAboutToCollapseListener, GroupCollapsedListener, GroupEdgesAddedListener {

	private CyServiceRegistrar serviceRegistrar;

	public AppManager(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar=serviceRegistrar;

	}

	/**
	 * Returns the specific queried service.
	 * @param clazz The class defining the type of service desired.
	 * @return A reference to a service of type <code>clazz</code>.
	 * 
	 * @throws RuntimeException If the requested service can't be found.
	 */
	public <T> T getService(Class<? extends T> clazz) {
		return this.serviceRegistrar.getService(clazz);
	}

	/**
	 * A method that attempts to get a service of the specified type and that passes the specified filter.
	 * If an appropriate service is not found, an exception will be thrown.
	 * @param clazz The class defining the type of service desired.
	 * @param filter The string defining the filter the service must pass. See OSGi's service filtering syntax for more detail.
	 * @return A reference to a service of type <code>serviceClass</code> that passes the specified filter.
	 * 
	 * @throws RuntimeException If the requested service can't be found.
	 */
	public <T> T getService(Class<? extends T> clazz, String filter) {
		return this.serviceRegistrar.getService(clazz, filter);
	}

	/**
	 * Registers an object as an OSGi service with the specified service interface and properties.
	 * @param service The object to be registered as a service.
	 * @param clazz The service interface the object should be registered as.
	 * @param props The service properties.
	 */
	public void registerService(Object service, Class<?> clazz, Properties props) {
		this.serviceRegistrar.registerService(service, clazz, props);
	}

	/**
	 * This method registers an object as an OSGi service for all interfaces that the object implements and with the specified properties.
	 * Note that this method will NOT register services for any packages with names that begin with "java", which is an effort to avoid registering meaningless services for core Java APIs.
	 * @param service The object to be registered as a service for all interfaces that the object implements.
	 * @param props The service properties.
	 */
	public void registerAllServices(CyProperty<Properties> service, Properties props) {
		this.serviceRegistrar.registerAllServices(service, props);
	}

	/**
	 * This method unregisters an object as an OSGi service for the specified service interface.
	 * @param service The object to be unregistered as a service.
	 * @param clazz The service interface the object should be unregistered as.
	 */
	public void unregisterService(Object service, Class<?> clazz) {
		this.serviceRegistrar.unregisterService(service, clazz);
	}

	/**
	 * This method unregisters an object as all OSGi service interfaces that the object implements.
	 * @param service The object to be unregistered for services it provides.
	 */
	public void unregisterAllServices(Object service) {
		this.serviceRegistrar.unregisterAllServices(service);
	}

	/**
	 * Executes a list of tasks in a synchronous way.
	 * @param ti The list of tasks to execute.
	 * @param to The class that listens to the result of the tasks.
	 */
	public void executeSynchronousTask(TaskIterator ti, TaskObserver to) {
		SynchronousTaskManager<?> taskM = this.serviceRegistrar.getService(SynchronousTaskManager.class);
		taskM.execute(ti, to);
	}

	/**
	 * Executes a list of tasks in a synchronous way.
	 * @param ti The list of tasks to execute.
	 */
	public void executeSynchronousTask(TaskIterator ti) {
		this.executeSynchronousTask(ti, null);
	}

	/**
	 * Executes a list of tasks in an asynchronous way.
	 * @param ti The list of tasks to execute.
	 * @param to The class that listens to the result of the tasks.
	 */
	public void executeTask(TaskIterator ti, TaskObserver to) {
		TaskManager<?, ?> taskM = this.serviceRegistrar.getService(TaskManager.class);
		taskM.execute(ti, to);
	}
	/**
	 * Executes a list of tasks in an asynchronous way.
	 * @param ti The list of tasks to execute.
	 */
	public void executeTask(TaskIterator ti) {
		this.executeTask(ti, null);
	}

	public void createBooleanColumnIfNeeded(CyTable table, Class<?> clazz, String columnName, Boolean defaultValue) {
		if (table.getColumn(columnName) != null)
			return;

		table.createColumn(columnName, clazz, false, defaultValue);
	}

	public void createDoubleColumnIfNeeded(CyTable table, Class<?> clazz, String columnName, Double defaultValue) {
		if (table.getColumn(columnName) != null)
			return;

		table.createColumn(columnName, clazz, false, defaultValue);
	}

	public void createListColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
		if (table.getColumn(columnName) != null)
			return;

		table.createListColumn(columnName, clazz, false);
	}

	@Override
	public void handleEvent(GroupAboutToCollapseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("group about to collapse "
		//		+ e.getNetwork().getRow(e.getSource().getGroupNode()).get(CyNetwork.NAME, String.class));
	}

	@Override
	public void handleEvent(GroupCollapsedEvent e) {
		// TODO Auto-generated method stub
		// e.getSource().getGroupNode();
	}

	@Override
	public void handleEvent(GroupEdgesAddedEvent e) {
		// TODO Auto-generated method stub
	}

	
}
