package dk.ku.cpr.proteoVisualizer.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;

public class RetrieveStringNetworkTaskFactory extends AbstractTaskFactory {
	private AppManager manager;

	public RetrieveStringNetworkTaskFactory(AppManager manager) {
		super();
		this.manager = manager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RetrieveStringNetworkTunableTask(this.manager));
	}

	public TaskIterator createTaskIterator(String query, String delim, Integer taxonID, String species, double cutoff, String netType,
			String netName, boolean isGUI) {
		RetrieveStringNetworkTask task = new RetrieveStringNetworkTask(this.manager);

		task.setQuery(query);
		task.setDelimiter(delim);
		task.setTaxonID(taxonID);
		task.setSpecies(species);
		task.setCutoff(cutoff);
		task.setNetType(netType);
		task.setNetworkName(netName);
		task.setIsGUI(isGUI);

		return new TaskIterator(task);
	}

}
