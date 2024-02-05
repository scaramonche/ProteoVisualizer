package dk.ku.cpr.proteinGroupsApp.internal.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CySubNetwork;
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
import dk.ku.cpr.proteinGroupsApp.internal.model.NetworkType;
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
	protected HashMap<String, List<String>> protected_protein2pgsMap;

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
		this.protected_netType = new ListSingleSelection<String>(
				Arrays.asList(NetworkType.FUNCTIONAL.toString(), NetworkType.PHYSICAL.toString()));
		this.protected_netType.setSelectedValue(NetworkType.FUNCTIONAL.toString());
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
	}

	public void setIsGUI(boolean isGUI) {
		this.isGUI = isGUI;
	}

	public void setPGMapping(HashMap<String, List<String>> pg2proteinsMap) {
		this.protected_pg2proteinsMap = pg2proteinsMap;
	}

	public void setProteinMapping(HashMap<String, List<String>> protein2pgsMap) {
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

	public void copyRow(CyTable fromTable, CyTable toTable, CyIdentifiable from, CyIdentifiable to) {
		for (CyColumn col : fromTable.getColumns()) {
			if (col.getName().equals(CyNetwork.SUID))
				continue;
			if (col.getName().equals(CyNetwork.SELECTED))
				continue;
			Object v = fromTable.getRow(from.getSUID()).getRaw(col.getName());
			toTable.getRow(to.getSUID()).set(col.getName(), v);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void taskFinished(ObservableTask task) {
		if (task.getClass().getSimpleName().equals("ProteinQueryTask")) {
			retrievedNetwork = task.getResults(CyNetwork.class);

			// create a map of query term to node
			HashMap<String, CyNode> queryTerm2node = new HashMap<String, CyNode>();
			for (CyNode node : retrievedNetwork.getNodeList()) {
				String queryTerm = retrievedNetwork.getRow(node).get("query term", String.class);
				if (queryTerm != null) {
					queryTerm2node.put(queryTerm, node);
				}
			}

			// add needed columns
			manager.createBooleanColumnIfNeeded(retrievedNetwork.getDefaultNodeTable(), Boolean.class,
					SharedProperties.USE_ENRICHMENT, false);
			manager.createDoubleColumnIfNeeded(retrievedNetwork.getDefaultEdgeTable(), Double.class,
					SharedProperties.EDGEPROB, 0.0);

			// duplicate nodes (and their adjacent edges) if they belong to more than one
			// protein group
			HashMap<CyNode, LinkedHashSet<CyNode>> protein2dupProteinsMap = new HashMap<CyNode, LinkedHashSet<CyNode>>();
			for (String protein : protected_protein2pgsMap.keySet()) {
				List<String> proteinGroups = protected_protein2pgsMap.get(protein);
				if (proteinGroups.size() == 1) // ignore if there is a 1-to-1 mapping between protein and protein group
					continue;
				if (!queryTerm2node.containsKey(protein)) // ignore if we did not get a node from STRING for this ID
					continue;
				CyNode proteinNode = queryTerm2node.get(protein);
				LinkedHashSet<CyNode> duplicatedNodes = new LinkedHashSet<CyNode>();
				duplicatedNodes.add(proteinNode);
				for (int i = 1; i < proteinGroups.size(); i++) {
					CyNode newDuplNode = retrievedNetwork.addNode();
					duplicatedNodes.add(newDuplNode);
					// copy node attributes
					copyRow(retrievedNetwork.getDefaultNodeTable(), retrievedNetwork.getDefaultNodeTable(), proteinNode,
							newDuplNode);
					// create edges and copy edge attributes
					List<CyEdge> proteinNodeEdges = retrievedNetwork.getAdjacentEdgeList(proteinNode, Type.ANY);
					for (CyEdge edge : proteinNodeEdges) {
						// we should not assume that the original protein node is the source in all edges
						CyNode sourceNode = edge.getSource();
						CyNode targetNode = edge.getTarget();
						boolean isDirected = edge.isDirected();
						// create a new edge 
						CyEdge newEdge = null;
						if (sourceNode.equals(proteinNode)) {
							newEdge = retrievedNetwork.addEdge(newDuplNode, targetNode, isDirected);
						} else if (targetNode.equals(proteinNode)) {
							newEdge = retrievedNetwork.addEdge(sourceNode, newDuplNode, isDirected);
						} else {
							System.out.println("something goes wrong with the edge copying");
							continue;
						}
						// copy edge attributes
						copyRow(retrievedNetwork.getDefaultEdgeTable(), retrievedNetwork.getDefaultEdgeTable(), edge,
								newEdge);
					}
					// add a new identity edge between the duplicated nodes
					CyEdge newIdentityEdge = retrievedNetwork.addEdge(proteinNode, newDuplNode, false);
					retrievedNetwork.getRow(newIdentityEdge).set(CyEdge.INTERACTION, "identity");
					// TODO: why is this needed?
					retrievedNetwork.getRow(newIdentityEdge).set(CyNetwork.NAME,
							retrievedNetwork.getRow(proteinNode).get(CyNetwork.NAME, String.class) + " (identity) "
									+ retrievedNetwork.getRow(newDuplNode).get(CyNetwork.NAME, String.class));
				}
				protein2dupProteinsMap.put(proteinNode, duplicatedNodes);
			}

			// find all PGs with more than one node and create group nodes for them
			CyGroupFactory groupFactory = manager.getService(CyGroupFactory.class);
			// TODO: fix some group setting here before creating the nodes?
			List<CyGroup> groups = new ArrayList<CyGroup>();
			Set<CyNode> groupsAsNodes = new HashSet<CyNode>();
			for (String pg : protected_pg2proteinsMap.keySet()) {
				List<String> proteins = protected_pg2proteinsMap.get(pg);
				List<CyNode> nodesForGroup = new ArrayList<>();
				CyNode reprNode = null;
				int proteinCount = 0;
				for (String protein : proteins) {
					if (queryTerm2node.containsKey(protein)) {
						CyNode proteinNode = queryTerm2node.get(protein);
						// check if we need to use one of the duplicates
						if (protein2dupProteinsMap.containsKey(proteinNode) && protein2dupProteinsMap.get(proteinNode).size() > 0) {
							CyNode copyNode = proteinNode;
							proteinNode = protein2dupProteinsMap.get(copyNode).iterator().next();
							protein2dupProteinsMap.get(copyNode).remove(proteinNode);
						}
						if (proteinCount == 0) {
							reprNode = proteinNode;
						} 
						nodesForGroup.add(proteinNode);							
					}
					proteinCount += 1;
				}
				// if group only had one protein, set this node to be used for enrichment and go to the next protein group
				if (nodesForGroup.size() <= 1) {
					// set the use for enrichment flag and continue with the others
					retrievedNetwork.getRow(reprNode).set(SharedProperties.USE_ENRICHMENT, true);
					continue;
				}
				// otherwise create and save the new cyGroup
				CyGroup pgGroup = groupFactory.createGroup(this.retrievedNetwork, nodesForGroup, null, true);
				groups.add(pgGroup);
				CyNode groupNode = pgGroup.getGroupNode();
				groupsAsNodes.add(groupNode);

				// Node attributes that are group-specific
				// set protein group query term to be the PD name such that import of data works properly
				retrievedNetwork.getRow(groupNode).set(SharedProperties.QUERYTERM, pg);
				retrievedNetwork.getRow(groupNode).set(SharedProperties.TYPE, "protein group");
				
				// set group node to be used for enrichment with the string ID of the repr node
				// TODO: this might change eventually but ok like this for now
				retrievedNetwork.getRow(groupNode).set(SharedProperties.USE_ENRICHMENT, true);

				// Node attributes that are copied from the representative node
				// name, canonical name, database identifier (STRINGID), @id, namespace, species, imageurl, enhanced label
				// TODO: what to do with style?
				// retrievedNetwork.getRow(groupNode).set(SharedProperties.STYLE, "string:");
				for (String attr : SharedProperties.nodeAttrinbutesToCopyString) {
					retrievedNetwork.getRow(groupNode).set(attr,
							retrievedNetwork.getRow(reprNode).get(attr, String.class));					
				}
				
				// Node attributes (string) that are concatenated
				// display name, full name, description, sequence, dev. level, family
				// TODO: what to do with canonical? --> fix in stringApp?
				for (String attr : SharedProperties.nodeAttrinbutesToConcatString) {
					String concatValue = "";
					for (CyNode node : nodesForGroup) {
						String nodeValue = retrievedNetwork.getRow(node).get(attr, String.class);
						if (nodeValue == null) 
							continue;
						concatValue += ";" + nodeValue;
					}
					if (concatValue.startsWith(";"))
						concatValue = concatValue.substring(1);
					retrievedNetwork.getRow(groupNode).set(attr, concatValue);
				}
				// Node attributes (list of strings) that are concatenated
				// structures
				Set<String> structures = new HashSet<String>();
				for (CyNode node : nodesForGroup) {
					List<String> nodeStructures = (List<String>) retrievedNetwork.getRow(node).get(SharedProperties.STRUCTURES, List.class);
					if (nodeStructures == null)
						continue;
					structures.addAll(nodeStructures);
				}
				retrievedNetwork.getRow(groupNode).set(SharedProperties.STRUCTURES, new ArrayList<String>(structures));
				
				// Node attributes that are averaged
				// all compartment cols, all tissue cols, interactor score?
				Collection<CyColumn> colsToAverage = retrievedNetwork.getDefaultNodeTable().getColumns(SharedProperties.COMPARTMENT_NAMESPACE);
				colsToAverage.addAll(retrievedNetwork.getDefaultNodeTable().getColumns(SharedProperties.TISSUE_NAMESPACE));
				colsToAverage.add(retrievedNetwork.getDefaultNodeTable().getColumn(SharedProperties.INTERACTORSCORE));
				for (CyColumn col : colsToAverage) {
					double averagedValue = 0.0;
					int numValues = 0;
					if (col == null) 
						continue;
					for (CyNode node : nodesForGroup) {
						Double nodeValue = retrievedNetwork.getRow(node).get(col.getName(), Double.class);
						if (nodeValue == null) 
							continue;
						averagedValue += nodeValue.doubleValue();
						numValues += 1;
					}
					// TODO: shorten to 6 digits precision or not?
					retrievedNetwork.getRow(groupNode).set(col.getName(), Double.valueOf(averagedValue/numValues));
				}

			}

			// collapse all the groups
			// TODO: aggregation is turned off in the cy activator, but do we need to check the preferences again?
			for (CyGroup group : groups) {
				System.out.println("collapsing group " + retrievedNetwork.getRow(group.getGroupNode()).get(CyNetwork.NAME, String.class) + " (SUID: " + group.getGroupNode().getSUID() + ")");
				group.collapse(retrievedNetwork);				
			}
						
			// do edge attribute aggregation for the stringdb namespace columns
			Collection<CyColumn> stringdbCols = retrievedNetwork.getDefaultEdgeTable().getColumns(SharedProperties.STRINGDB_NAMESPACE);
			List<String> edgeColsToAverage = new ArrayList<String>();
			for (CyColumn col : stringdbCols) {
				if (col == null || !col.getType().equals(Double.class)) 
					continue;
				edgeColsToAverage.add(col.getName());
			}
			
			for (CyGroup group : groups) {
				System.out.println("aggregating edges for group " + retrievedNetwork.getRow(group.getGroupNode()).get(CyNetwork.NAME, String.class) + " (SUID: " + group.getGroupNode().getSUID() + ")");
				// group network is a network representation of the nodes in the group and the edges that connect them
				// CyNetwork groupNetwork = group.getGroupNetwork(); 				
				// root network is the network that contains the group and the retrieved network?! NOT the same as the retrieved network above
				CyNetwork rootNetwork = group.getRootNetwork();				
				// get all nodes in the group
				List<CyNode> groupNodes = group.getNodeList();
				// get all external edges, includes both meta edges and edges from any node in the group to any other node in the network 
				// Set<CyEdge> externalEdges = group.getExternalEdgeList();
				// the adjacent edges to the node representing the group are all meta edges between the group and other nodes, so they are not the same as the external edges
				List<CyEdge> groupNodeEdges = retrievedNetwork.getAdjacentEdgeList(group.getGroupNode(), Type.ANY);
				for (CyEdge newEdge : groupNodeEdges) {
					// System.out.println("Group node edge from retrieved: " + newEdge);
					// CyRow row = retrievedNetwork.getRow(newEdge, CyNetwork.HIDDEN_ATTRS);
					// if (row != null && row.isSet("__isMetaEdge"))
					// 	System.out.println("ISMETA =  " + row.get("__isMetaEdge", Boolean.class));
					// find the neighbor
					CyNode neighbor = null;
					if (group.getGroupNode().equals(newEdge.getSource())) 
						neighbor = newEdge.getTarget();
					else 
						neighbor = newEdge.getSource();
					// System.out.println(retrievedNetwork.getRow(neighbor).get(CyNetwork.NAME, String.class));
					// find out which edges we need to average
					List<CyEdge> edgesToAverage = new ArrayList<CyEdge>();
					if (groupsAsNodes.contains(neighbor)) {
						// if the neighbor is a group node, get the edges between nodes of both groups
						//System.out.println("found a neighbor that is a group as well: " + neighbor);
						CySubNetwork groupSubNet = (CySubNetwork)neighbor.getNetworkPointer();
						for (CyNode group1Node : groupSubNet.getNodeList()) {
							for (CyNode group2Node : groupNodes) {
								edgesToAverage.addAll(rootNetwork.getConnectingEdgeList(group1Node, group2Node, Type.ANY));
							}
						}
						//System.out.println("edges to average: " + edgesToAverage.size());
					} else {
						//System.out.println("found a normal neighbor: " + neighbor);
						for (CyNode groupNode : groupNodes) {
							edgesToAverage.addAll(rootNetwork.getConnectingEdgeList(groupNode, neighbor, Type.ANY));
						}
						//System.out.println("with edges to average: " + edgesToAverage);
					}
					retrievedNetwork.getRow(newEdge).set(SharedProperties.EDGEPROB, Double.valueOf((double)edgesToAverage.size()/groupNodes.size()));
					// now get the average and set it for each column
					for (String col : edgeColsToAverage) {
						double averagedValue = 0.0;
						for (CyEdge edge : edgesToAverage) {
							Double edgeValue = rootNetwork.getRow(edge).get(col, Double.class);
							if (edgeValue != null) 
								averagedValue += edgeValue.doubleValue();
						}
						if (averagedValue != 0.0) {
							// TODO: shorten to 6 digits precision or not?
							retrievedNetwork.getRow(newEdge).set(col, Double.valueOf(averagedValue/edgesToAverage.size()));							
						}
					}
					// TODO: in theory we need to assign scores to the meta edges of the uncollapsed group... how to best do that?
				}
			}
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
