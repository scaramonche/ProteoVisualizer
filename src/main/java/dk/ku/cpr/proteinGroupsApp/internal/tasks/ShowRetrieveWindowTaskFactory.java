package dk.ku.cpr.proteinGroupsApp.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.proteinGroupsApp.internal.model.AppManager;

public class ShowRetrieveWindowTaskFactory extends AbstractTaskFactory {
	
	private AppManager manager;

	public ShowRetrieveWindowTaskFactory(AppManager manager) {
		super();
		this.manager = manager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowRetrieveWindowTask(manager));
	}

	@Override
	public boolean isReady() {
		// TODO: return true when?
		return super.isReady();
	}
}
