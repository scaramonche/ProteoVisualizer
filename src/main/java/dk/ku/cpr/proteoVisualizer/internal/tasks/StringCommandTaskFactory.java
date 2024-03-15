package dk.ku.cpr.proteoVisualizer.internal.tasks;

import java.util.Map;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;

public class StringCommandTaskFactory extends AbstractTaskFactory {

	private final AppManager manager;
	private final String command;
	private final Map<String, Object> args;
	private final TaskObserver taskObserver;

	public StringCommandTaskFactory(AppManager manager, String command, Map<String, Object> args,
			TaskObserver taskObserver) {
		super();
		this.manager = manager;
		this.command = command;
		this.args = args;
		this.taskObserver = taskObserver;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new StringCommandTask(manager, command, args, taskObserver));
	}
}
