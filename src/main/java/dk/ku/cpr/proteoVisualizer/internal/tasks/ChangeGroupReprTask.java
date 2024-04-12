package dk.ku.cpr.proteoVisualizer.internal.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.command.StringToModel;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;
import dk.ku.cpr.proteoVisualizer.internal.model.SharedProperties;

public class ChangeGroupReprTask extends AbstractTask {

	private AppManager manager;
	private CyNetworkView netView;
	private View<CyNode> nodeView;

	private CyGroupManager groupManager;
	private CyGroup group;
	private CyNode groupNode;
	
	private Map<String, CyNode> groupNodesMap;

	@Tunable(description = "Network to expand", 
			longDescription = StringToModel.CY_NETWORK_LONG_DESCRIPTION, 
			exampleStringValue = StringToModel.CY_NETWORK_EXAMPLE_STRING, 
			context = "nogui", required=true)
	public CyNetwork network;

	//@Tunable (description="Group node for which node repreesentative will be changed", 
	//		longDescription = "", 
	//		exampleStringValue = "0", 
	//		context = "nogui", required=true)
	//public Long groupNodeSUID = null;

	@Tunable (description="Group nodes to choose from", 
			longDescription = "", 
			exampleStringValue = "CDK1", 
			gravity=1.0)
	public ListSingleSelection<String> groupNodesList = new ListSingleSelection<String>();

	
	public ChangeGroupReprTask(AppManager aManager) {
		manager = aManager;
		groupManager = manager.getService(CyGroupManager.class);
		
		// Make sure we have a network.  This should only happen at this point if we're coming in via a command
		if (this.network == null)
			this.network = manager.getCurrentNetwork();

		this.netView = manager.getCurrentNetworkView();
		this.nodeView = null;

		//if (this.network != null && groupNodeSUID != null) {
			// initiate tunables if needed
		//	groupNode = this.network.getNode(groupNodeSUID);
		//	if (groupNode != null) {
		//		group = groupManager.getGroup(groupNode, network);
		//		groupNodes = new ListSingleSelection<CyNode>(group.getNodeList());
		//		// TODO: set selected to current node representative
		//	}
		//}
	}

	public ChangeGroupReprTask(AppManager aManager, CyNetworkView aNetView, View<CyNode> aNodeView) {
		manager = aManager;
		netView = aNetView;
		nodeView = aNodeView;
		network = netView.getModel();
		groupManager = manager.getService(CyGroupManager.class);
		// initiate tunables if needed
		if (network != null && nodeView != null) {
			groupNode = nodeView.getModel();
			// groupNodeSUID = groupNode.getSUID();
			group = groupManager.getGroup(groupNode, network);
			initGroupNodesList();
			// TODO: set selected to current node representative
		}
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Change group representative");
		System.out.println("change to: " + groupNodesList.getSelectedValue());
		// TODO: change group node attributes to new repr node
		
	}

	
	private void initGroupNodesList() {
		groupNodesMap = new HashMap<String, CyNode>();
		String currentRepr = null;
		List<String> nodes = new ArrayList<String>();
		List<CyNode> groupNodes = group.getNodeList();
		for (CyNode cyNode : groupNodes) {
			String nodeName = network.getRow(cyNode).get(CyNetwork.NAME, String.class);
			if (network.getDefaultNodeTable().getColumn(SharedProperties.DISPLAY) != null)
				nodeName = network.getRow(cyNode).get(SharedProperties.DISPLAY, String.class);
			groupNodesMap.put(nodeName, cyNode);
			nodes.add(nodeName);
			if (network.getDefaultNodeTable().getColumn(SharedProperties.ID) != null
					&& network.getRow(cyNode).get(SharedProperties.ID, String.class) != null 
					&& network.getRow(groupNode).get(SharedProperties.ID, String.class) != null) {
				if (network.getRow(cyNode).get(SharedProperties.ID, String.class)
						.equals(network.getRow(groupNode).get(SharedProperties.ID, String.class)))
					currentRepr = nodeName;
			}
		}
		Collections.sort(nodes);
		groupNodesList = new ListSingleSelection<String>(nodes);
		groupNodesList.setSelectedValue(currentRepr);
	}
	
}
