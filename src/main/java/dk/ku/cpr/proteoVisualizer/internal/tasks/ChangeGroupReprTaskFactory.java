package dk.ku.cpr.proteoVisualizer.internal.tasks;

import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;

public class ChangeGroupReprTaskFactory extends AbstractTaskFactory implements NodeViewTaskFactory, TaskFactory {

	final AppManager manager;
	final CyGroupManager groupManager;
	
	public ChangeGroupReprTaskFactory(AppManager manager) {
		this.manager = manager;
		this.groupManager = manager.getService(CyGroupManager.class);
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ChangeGroupReprTask(manager));
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new ChangeGroupReprTask(manager, networkView, nodeView));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		CyNode groupNode = nodeView.getModel();
		// TODO: check if proteovis network?
		if (groupManager.getGroup(groupNode, networkView.getModel()) != null)
			return true;
		return false;
	}

}
