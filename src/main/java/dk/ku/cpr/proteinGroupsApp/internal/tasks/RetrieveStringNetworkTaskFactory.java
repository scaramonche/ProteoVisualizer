package dk.ku.cpr.proteinGroupsApp.internal.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import dk.ku.cpr.proteinGroupsApp.internal.model.AppManager;

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

	public TaskIterator createTaskIterator(String query, Integer taxonID, String species, double cutoff, String netType,
			String netName, HashMap<String, List<String>> pg2proteins, HashMap<String, List<String>> protein2pgs,
			boolean isGUI) {
		RetrieveStringNetworkTask task = new RetrieveStringNetworkTask(this.manager);

		task.setQuery(query);
		task.setTaxonID(taxonID);
		task.setSpecies(species);
		task.setCutoff(cutoff);
		task.setNetType(netType);
		task.setNetworkName(netName);
		task.setPGMapping(pg2proteins);
		task.setProteinMapping(protein2pgs);
		task.setIsGUI(isGUI);

		return new TaskIterator(task);
	}

}
