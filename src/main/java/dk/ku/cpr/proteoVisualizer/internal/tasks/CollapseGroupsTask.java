package dk.ku.cpr.proteoVisualizer.internal.tasks;

import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;
import dk.ku.cpr.proteoVisualizer.internal.model.SharedProperties;

public class CollapseGroupsTask extends AbstractNetworkTask {

	private AppManager manager;
	private CyNetwork network;
	
	public CollapseGroupsTask(AppManager manager, CyNetwork network) {
		super(network);
		this.manager = manager;
		this.network = network;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(this.getName());

		if (network.getDefaultNetworkTable().getColumn(SharedProperties.COLLAPSED) == null
				|| network.getRow(network).get(SharedProperties.COLLAPSED, Boolean.class) == null)
			return;
		
		boolean collapsed = network.getRow(network).get(SharedProperties.COLLAPSED, Boolean.class).booleanValue();
		CyGroupManager groupManager = manager.getService(CyGroupManager.class);
		Set<CyGroup> groups = groupManager.getGroupSet(network);	
		
		if (collapsed) {
			System.out.println("Expand all groups");
			for (CyGroup group : groups) {
				group.expand(network);
			}
			network.getRow(network).set(SharedProperties.COLLAPSED, false);
		} else {
			System.out.println("Collapse all groups");
			for (CyGroup group : groups) {
				group.collapse(network);
			}
			network.getRow(network).set(SharedProperties.COLLAPSED, true);
		}
	}

	@ProvidesTitle
	public String getName() {
		return "Collapse/expand all groups";
	}

}
