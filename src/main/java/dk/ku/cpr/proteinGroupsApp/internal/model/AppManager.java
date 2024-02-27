package dk.ku.cpr.proteinGroupsApp.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.events.GroupAboutToCollapseEvent;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedEvent;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.group.events.GroupEdgesAddedEvent;
import org.cytoscape.group.events.GroupEdgesAddedListener;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

public class AppManager implements GroupAboutToCollapseListener, GroupCollapsedListener, GroupEdgesAddedListener, SelectedNodesAndEdgesListener {

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

	public void createIntegerColumnIfNeeded(CyTable table, Class<?> clazz, String columnName, Integer defaultValue) {
		if (table.getColumn(columnName) != null)
			return;

		table.createColumn(columnName, clazz, false, defaultValue);
	}

	public void createListColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
		if (table.getColumn(columnName) != null)
			return;

		table.createListColumn(columnName, clazz, false);
	}

	public CyNetworkView getCurrentNetworkView() {
		return getService(CyApplicationManager.class).getCurrentNetworkView();
	}

	@Override
	public void handleEvent(GroupCollapsedEvent e) {
		CyGroup group = e.getSource();
		CyNode groupNode = group.getGroupNode();
		CyNetwork network = e.getNetwork();
		// CyNetworkView view = getCurrentNetworkView();
		// View<CyNode> nodeView = view.getNodeView(groupNode);
		if (e.collapsed()) {
			// System.out.println("group collapsed");
			// change style of string node
			network.getRow(groupNode).set(SharedProperties.STYLE, "string:");
			//nodeView.setVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY, 255);
		} else {
			// System.out.println("group expanded");
			// change style of string node
			network.getRow(groupNode).set(SharedProperties.STYLE, "");
			//nodeView.setVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY, 50);
			
			// aggregate edge attributes if not already done 
			// do edge attribute aggregation for the stringdb namespace columns
			Collection<CyColumn> stringdbCols = network.getDefaultEdgeTable().getColumns(SharedProperties.STRINGDB_NAMESPACE);
			List<String> edgeColsToAverage = new ArrayList<String>();
			for (CyColumn col : stringdbCols) {
				if (col == null || !col.getType().equals(Double.class)) 
					continue;
				edgeColsToAverage.add(col.getName());
			}
			// get all external edges, includes both meta edges and edges from any node in the group to any other node in the network
			// not sure why we cannot get the neighbors using network.getAdjacentEdgeList(group.getGroupNode(), Type.ANY);
			Set<CyEdge> externalEdges = group.getExternalEdgeList();
			for (CyEdge newEdge : externalEdges) {
				Boolean edgeTypeMeta = group.getRootNetwork().getRow(newEdge, CyNetwork.HIDDEN_ATTRS).get("__isMetaEdge", Boolean.class);
				Boolean edgeAggregated = group.getRootNetwork().getRow(newEdge).get(SharedProperties.EDGEAGGREGATED, Boolean.class);
				// ignore edge if it is NOT a meta edge or if we already aggregated the attributes for it
				if (edgeTypeMeta == null || !edgeTypeMeta || (edgeAggregated != null && edgeAggregated))
					continue;
				System.out.println("aggregate edge attributes for edge with SUID " + newEdge.getSUID());
				CyNode source = newEdge.getSource();
				CyNode target = newEdge.getTarget();
				if (group.getNodeList().contains(source)) {
					aggregateGroupEdgeAttributes(network, group, newEdge, new ArrayList<CyNode>(Arrays.asList(source)), target, edgeColsToAverage);					
				} else if (group.getNodeList().contains(target)) {
					aggregateGroupEdgeAttributes(network, group, newEdge, new ArrayList<CyNode>(Arrays.asList(target)), source, edgeColsToAverage);
				} else {
					System.out.println("neither source nor target is a node in the group that was uncollapsed");
				}
					
			}
		}
	}

	
	public void aggregateGroupEdgeAttributes(CyNetwork retrievedNetwork, CyGroup group, CyEdge newEdge, List<CyNode> groupNodes, CyNode neighbor, List<String> edgeColsToAggregate) {
		CyNetwork rootNetwork = group.getRootNetwork(); 
		CyGroupManager groupManager = getService(CyGroupManager.class);
		// System.out.println(retrievedNetwork.getRow(neighbor).get(CyNetwork.NAME, String.class));
		// find out which edges we need to average
		List<CyEdge> edgesToAggregate = new ArrayList<CyEdge>();
		int numPossibleEdges = groupNodes.size();
		if (groupManager.isGroup(neighbor, retrievedNetwork)) {
			// if the neighbor is a group node, get the edges between nodes of both groups
			//System.out.println("found a neighbor that is a group as well: " + neighbor);
			CySubNetwork groupSubNet = (CySubNetwork)neighbor.getNetworkPointer();
			numPossibleEdges *= groupSubNet.getNodeCount();
			for (CyNode group1Node : groupSubNet.getNodeList()) {
				for (CyNode group2Node : groupNodes) {
					edgesToAggregate.addAll(rootNetwork.getConnectingEdgeList(group1Node, group2Node, Type.ANY));
				}
			}
			//System.out.println("edges to average: " + edgesToAverage.size());
		} else {
			//System.out.println("found a normal neighbor: " + neighbor);
			for (CyNode groupNode : groupNodes) {
				edgesToAggregate.addAll(rootNetwork.getConnectingEdgeList(groupNode, neighbor, Type.ANY));
			}
		}
		retrievedNetwork.getRow(newEdge).set(SharedProperties.EDGEPOSSIBLE, Integer.valueOf(numPossibleEdges));
		retrievedNetwork.getRow(newEdge).set(SharedProperties.EDGEEXISTING, Integer.valueOf(edgesToAggregate.size()));
		// retrievedNetwork.getRow(newEdge).set(SharedProperties.EDGEPROB, Double.valueOf((double)edgesToAggregate.size()/numPossibleEdges));
		// now get the average and set it for each column
		for (String col : edgeColsToAggregate) {
			double averagedValue = 0.0;
			for (CyEdge edge : edgesToAggregate) {
				Double edgeValue = rootNetwork.getRow(edge).get(col, Double.class);
				if (edgeValue != null) 
					averagedValue += edgeValue.doubleValue();
			}
			if (averagedValue != 0.0) {
				// TODO: shorten to 6 digits precision or not?
				// for edge attributes, we sum the values from all existing edges and divide by the number of possible edges
				retrievedNetwork.getRow(newEdge).set(col, Double.valueOf(averagedValue/numPossibleEdges));							
			}
		}
		// TODO: [Release] Remove this attribute
		retrievedNetwork.getRow(newEdge).set(SharedProperties.EDGEAGGREGATED, Boolean.valueOf(true));
	}

	@Override
	public void handleEvent(SelectedNodesAndEdgesEvent event) {
		// TODO: figure out what to do when group nodes are selected
		// needs to add the listener to app manager in CyActivator
		Collection<CyNode> nodes = event.getSelectedNodes();
		CyNetwork network = event.getNetwork();
		CyGroupManager groupManager = getService(CyGroupManager.class);
		for (CyNode node : nodes) {
			if (groupManager.isGroup(node, network)) {
				CyGroup group = groupManager.getGroup(node, network);
				for (CyNode groupNode : group.getNodeList()) {
					if (network.getRow(groupNode) != null)
						network.getRow(groupNode).set(CyNetwork.SELECTED, true);
				}
			}
		}
	}
	

	@Override
	public void handleEvent(GroupAboutToCollapseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("group about to collapse "
		//		+ e.getNetwork().getRow(e.getSource().getGroupNode()).get(CyNetwork.NAME, String.class));
	}


	@Override
	public void handleEvent(GroupEdgesAddedEvent e) {
		// TODO Auto-generated method stub
		// System.out.println("added edges + " + e.getEdges());
	}

	
	
}
