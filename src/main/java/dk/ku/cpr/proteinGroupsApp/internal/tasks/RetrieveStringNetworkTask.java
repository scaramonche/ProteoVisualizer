package dk.ku.cpr.proteinGroupsApp.internal.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

import dk.ku.cpr.proteinGroupsApp.internal.model.AppManager;
import dk.ku.cpr.proteinGroupsApp.internal.model.SharedProperties;
import dk.ku.cpr.proteinGroupsApp.internal.utils.SwingUtil;

public class RetrieveStringNetworkTask extends AbstractTask implements TaskObserver, ObservableTask {
	protected AppManager manager;

	protected String protected_query;
	protected String protected_netName;
	protected Integer protected_taxonID;
	protected String protected_species;
	protected double protected_cutoff;
	protected ListSingleSelection<String> protected_netType;
	protected HashMap<String, List<String>> protected_pg2proteinsMap;
	protected HashMap<String, Set<String>> protected_protein2pgsMap;

	protected CyNetwork retrievedNetwork;

	private boolean isGUI;

	public RetrieveStringNetworkTask(AppManager manager) {
		super();
		this.manager = manager;

		this.protected_query = "";
		this.protected_netName = "";
		this.protected_taxonID = null;
		this.protected_species = null;
		this.protected_cutoff = 0.4;
		// TODO: move names to sharedproperties
		this.protected_netType = new ListSingleSelection<String>(
				Arrays.asList("full STRING network", "physical subnetwork"));
		this.protected_netType.setSelectedValue("full STRING network");
		this.protected_pg2proteinsMap = null;
		this.protected_protein2pgsMap = null;

		this.isGUI = false;
	}

	public void setQuery(String query) {
		this.protected_query = query;
	}

	public void setNetworkName(String name) {
		this.protected_netName = name;
	}

	public void setTaxonID(Integer taxonID) {
		this.protected_taxonID = taxonID;
	}

	public void setSpecies(String species) {
		this.protected_species = species;
	}

	public void setCutoff(double cutoff) {
		this.protected_cutoff = cutoff;
	}

	public void setNetType(String netType) {
		this.protected_netType.setSelectedValue(netType);
		;
	}

	public void setIsGUI(boolean isGUI) {
		this.isGUI = isGUI;
	}

	public void setPGMapping(HashMap<String, List<String>> pg2proteinsMap) {
		this.protected_pg2proteinsMap = pg2proteinsMap;
	}

	public void setProteinMapping(HashMap<String, Set<String>> protein2pgsMap) {
		this.protected_protein2pgsMap = protein2pgsMap;
	}

	@ProvidesTitle
	public String getName() {
		return "Retrieve STRING Network";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle(this.getName());

		taskMonitor.setStatusMessage("Query: " + this.protected_query);
		taskMonitor.setStatusMessage("New network name: " + this.protected_netName);
		taskMonitor.setStatusMessage("Taxon ID: " + this.protected_taxonID);
		taskMonitor.setStatusMessage("Species: " + this.protected_species);
		taskMonitor.setStatusMessage("Cut-off: " + this.protected_cutoff);
		taskMonitor.setStatusMessage("Network type: " + this.protected_netType.getSelectedValue());

		if ((this.protected_taxonID == null) && (this.protected_species == null)) {
			taskMonitor.setStatusMessage("You have to give either the Taxon ID or the Species name.");
			return;
		}

		// We set the arguments for the STRING command
		Map<String, Object> args = new HashMap<>();
		args.put("query", this.protected_query);
		if (this.protected_taxonID != null) {
			args.put("taxonID", this.protected_taxonID);
		}
		if (this.protected_species != null) {
			args.put("species", this.protected_species);
		}
		args.put("cutoff", String.valueOf(this.protected_cutoff));
		args.put("networkType", protected_netType.getSelectedValue());
		args.put("limit", "0");
		args.put("newNetName", this.protected_netName);

		// We call the STRING command
		StringCommandTaskFactory factory = new StringCommandTaskFactory(this.manager,
				SharedProperties.STRING_CMD_PROTEIN_QUERY, args, this);
		TaskIterator ti = factory.createTaskIterator();
		this.manager.executeSynchronousTask(ti, this);

		// the STRING command is executed synchronously, so we can check the result
		if (this.getResults(CyNetwork.class) == null) {
			// If there is not result: we display an error message.
			taskMonitor.showMessage(Level.ERROR, "No network was retrieved.");

			if (this.isGUI) {
				// We have to invoke it on another thread because showMessageDialog blocks the
				// main process
				SwingUtil.invokeOnEDT(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(manager.getService(CySwingApplication.class).getJFrame(),
								"No network was retrieved.\nThe stringApp could not retrieve the queried network.",
								"Error while retrieving STRING network", JOptionPane.ERROR_MESSAGE);
					}
				});
			}
		}
	}

	@Override
	public void taskFinished(ObservableTask task) {
		if (task.getClass().getSimpleName().equals("ProteinQueryTask")) {
			this.retrievedNetwork = task.getResults(CyNetwork.class);

			// find all the PGs, for which we need to create group nodes
			HashMap<String, CyNode> queryTerm2node = new HashMap<String, CyNode>();
			for (CyNode node : this.retrievedNetwork.getNodeList()) {
				String queryTerm = this.retrievedNetwork.getRow(node).get("query term", String.class);
				if (queryTerm != null) {
					queryTerm2node.put(queryTerm, node);
				}
			}

			// create the group nodes
			CyGroupFactory groupFactory = manager.getService(CyGroupFactory.class);
			List<CyGroup> groups = new ArrayList<CyGroup>();
			for (String pg : protected_pg2proteinsMap.keySet()) {
				List<String> proteins = protected_pg2proteinsMap.get(pg);
				if (proteins.size() == 1)
					continue;
				List<CyNode> nodes = new ArrayList<>();
				CyNode reprNode = null;
				for (String protein : proteins) {
					if (queryTerm2node.containsKey(protein)) {
						nodes.add(queryTerm2node.get(protein));
						// TODO: decide how to choose the repr node
						if (reprNode == null)
							reprNode = queryTerm2node.get(protein);
					}
				}
				// create and save group
				CyGroup pgGroup = groupFactory.createGroup(this.retrievedNetwork, nodes, null, true);
				groups.add(pgGroup);
				CyNode groupNode = pgGroup.getGroupNode();
				// TODO: set whatever attributes we need to set here...
				// set display name and query term to be the name of the pg
				retrievedNetwork.getRow(groupNode).set("display name", pg);
				retrievedNetwork.getRow(groupNode).set("query term", pg);
				
				// set some attributes to be the name of the representative node
				retrievedNetwork.getRow(groupNode).set(CyNetwork.NAME,
						retrievedNetwork.getRow(reprNode).get(CyNetwork.NAME, String.class));
				// TODO: add new attribute "use for enrichment"
			}
			// collapse groups...
			// TODO: figure out in which order to do that since it changes the results
			//for (CyGroup group : groups) {
			//	group.collapse(retrievedNetwork);
			//}
		}
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
		// Do nothing
	}

	@SuppressWarnings("unchecked")
	public <R> R getResults(Class<? extends R> clzz) {
		if (clzz.equals(CyNetwork.class)) {
			return (R) this.retrievedNetwork;
		} else if (clzz.equals(Long.class)) {
			if (this.retrievedNetwork == null)
				return null;
			return (R) this.retrievedNetwork.getSUID();
			// We need to use the actual class rather than the interface so that
			// CyREST can inspect it to find the annotations
		} else if (clzz.equals(JSONResult.class)) {
			return (R) ("{\"SUID\":" + this.retrievedNetwork.getSUID() + "}");
		} else if (clzz.equals(String.class)) {
			if (this.retrievedNetwork == null) {
				return (R) "No network was loaded";
			}
			return (R) this.retrievedNetwork.getSUID().toString();
		}
		return null;
	}

	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class, String.class, Long.class, CyNetwork.class);
	}
}
