package dk.ku.cpr.proteoVisualizer.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Togglable;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;
import dk.ku.cpr.proteoVisualizer.internal.model.SharedProperties;

public class CollapseGroupsTaskFactory extends AbstractNetworkTaskFactory implements Togglable {

	private AppManager manager;

	public CollapseGroupsTaskFactory(AppManager manager) {
		super();
		this.manager = manager;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new CollapseGroupsTask(manager, network));
	}

	public boolean isReady(CyNetwork network) {
		return super.isReady(network);
	}

	public boolean isOn(CyNetwork network) {
		if (isReady(network) && network.getDefaultNetworkTable().getColumn(SharedProperties.COLLAPSED) != null
				&& network.getRow(network).get(SharedProperties.COLLAPSED, Boolean.class) != null)
			return network.getRow(network).get(SharedProperties.COLLAPSED, Boolean.class).booleanValue();
		return false;
	}
	
}
