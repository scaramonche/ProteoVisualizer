package dk.ku.cpr.proteoVisualizer.internal.tasks;

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
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;
import dk.ku.cpr.proteoVisualizer.internal.model.NetworkType;
import dk.ku.cpr.proteoVisualizer.internal.model.SharedProperties;
import dk.ku.cpr.proteoVisualizer.internal.utils.SwingUtil;

public class RetrieveStringNetworkTask extends AbstractTask implements TaskObserver, ObservableTask {
	protected AppManager manager;

	protected String protected_query;
	protected String protected_netName;
	protected String protected_delim;
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
		this.protected_delim = SharedProperties.DEFAULT_PG_DELIMITER;
		this.protected_taxonID = 9606;
		this.protected_species = "Homo sapiens";
		this.protected_cutoff = (double) manager.getDefaultConfidence() / 100.0;
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

	public void setDelimiter(String delim) {
		this.protected_delim = delim;
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

		// process list of query terms and save them somewhere?
		String query = protected_query;
		Set<String> queryIDs = new HashSet<String>();
		
		//System.out.println("query initial: " + query);
		//System.out.println("is gui: " + isGUI);
		// Strip off any blank lines as well as trailing spaces
		if (isGUI) {
			queryIDs = new HashSet<String>(Arrays.asList(query.split("\n")));
		} else {
			query = query.replaceAll("(?m)^\\s*", "");
			query = query.replaceAll("(?m)\\s*$", "");
			queryIDs = new HashSet<String>(Arrays.asList(query.split(",")));
		}
		//System.out.println("query ids: " + queryIDs);
		//System.out.println("delim: " + this.protected_delim);
		Set<String> allProteins = new HashSet<String>();
		protected_pg2proteinsMap = new HashMap<String, List<String>>();
		protected_protein2pgsMap = new HashMap<String, List<String>>();
			for (String queryID : queryIDs) {
				// let the user choose the delimiter of the proteins within a group
				List<String> proteinIDs = Arrays.asList(queryID.split(this.protected_delim));
				protected_pg2proteinsMap.put(queryID, proteinIDs);
				for (String protein : proteinIDs) {
					List<String> pgs = new ArrayList<String>();
					if (protected_protein2pgsMap.containsKey(protein)) {
						pgs = protected_protein2pgsMap.get(protein);
					} 
					pgs.add(queryID);
					protected_protein2pgsMap.put(protein, pgs);
				}
				allProteins.addAll(proteinIDs);
		}
		this.protected_query = String.join(",", allProteins);
		//System.out.println("query formatted: " + this.protected_query);
		//System.out.println(protected_pg2proteinsMap);
		//System.out.println(protected_protein2pgsMap);		
		
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
		if (!task.getClass().getSimpleName().equals("ProteinQueryTask")) 
			return;
		
		// get network on which the task was executed
		retrievedNetwork = task.getResults(CyNetwork.class);

		// create a map of query term to node
		HashMap<String, CyNode> queryTerm2node = new HashMap<String, CyNode>();
		for (CyNode node : retrievedNetwork.getNodeList()) {
			String queryTerm = retrievedNetwork.getRow(node).get(SharedProperties.QUERYTERM, String.class);
			if (queryTerm != null) {
				queryTerm2node.put(queryTerm, node);
			}
		}

		// TODO: [Release] Remove columns that are not needed
		// add needed new columns
		manager.createBooleanColumnIfNeeded(retrievedNetwork.getDefaultNodeTable(), Boolean.class,
				SharedProperties.USE_ENRICHMENT, false);
		manager.createBooleanColumnIfNeeded(retrievedNetwork.getDefaultEdgeTable(), Boolean.class,
				SharedProperties.EDGEAGGREGATED, false);
		//manager.createDoubleColumnIfNeeded(retrievedNetwork.getDefaultEdgeTable(), Double.class,
		//		SharedProperties.EDGEPROB, null);
		manager.createIntegerColumnIfNeeded(retrievedNetwork.getDefaultEdgeTable(), Integer.class,
				SharedProperties.EDGEPOSSIBLE, null);
		manager.createDoubleColumnIfNeeded(retrievedNetwork.getDefaultEdgeTable(), Integer.class,
				SharedProperties.EDGEEXISTING, null);
		
		// duplicate nodes (and their adjacent edges) if they belong to more than one protein group
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
				retrievedNetwork.getRow(newIdentityEdge).set(CyEdge.INTERACTION, SharedProperties.EDGE_TYPE_IDENTITY);
				// TODO: why is this needed?
				retrievedNetwork.getRow(newIdentityEdge).set(CyNetwork.NAME,
						retrievedNetwork.getRow(proteinNode).get(CyNetwork.NAME, String.class) + " (" + SharedProperties.EDGE_TYPE_IDENTITY + ") "
								+ retrievedNetwork.getRow(newDuplNode).get(CyNetwork.NAME, String.class));
			}
			protein2dupProteinsMap.put(proteinNode, duplicatedNodes);
		}

		// find all PGs with more than one node and create group nodes for them
		// in addition, handle all node attribtues! 
		CyGroupFactory groupFactory = manager.getService(CyGroupFactory.class);
		// TODO: fix some cyGroup setting here before creating the nodes?
		List<CyGroup> groups = new ArrayList<CyGroup>();
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
					// if this is the first node in the list, set it as the representative node
					if (proteinCount == 0) {
						reprNode = proteinNode;
					} 
					// add to list with group nodes and count up
					nodesForGroup.add(proteinNode);							
					proteinCount += 1;
				}
			}
			// if group only had one protein, set this node to be used for enrichment and go to the next protein group
			if (nodesForGroup.size() <= 1) {
				// set the use for enrichment flag if there is at least one protein
				if (reprNode != null)
					retrievedNetwork.getRow(reprNode).set(SharedProperties.USE_ENRICHMENT, true);
				// continue with the next protein group
				continue;
			}
			// otherwise create and save the new cyGroup
			CyGroup pgGroup = groupFactory.createGroup(this.retrievedNetwork, nodesForGroup, null, true);
			groups.add(pgGroup);
			CyNode groupNode = pgGroup.getGroupNode();

			// Node attributes that are group-specific
			// set protein group query term to be the PD name such that import of data works properly
			retrievedNetwork.getRow(groupNode).set(SharedProperties.QUERYTERM, pg);
			retrievedNetwork.getRow(groupNode).set(SharedProperties.TYPE, SharedProperties.NODE_TYPE_PG);				
			// set group node to be used for enrichment with the string ID of the repr node
			// TODO: USE_ENRICHMENT might change eventually but ok like this for now
			retrievedNetwork.getRow(groupNode).set(SharedProperties.USE_ENRICHMENT, Boolean.valueOf(true));
			// style is getting fixed when we collapse/uncollapse
			retrievedNetwork.getRow(groupNode).set(SharedProperties.STYLE, SharedProperties.STYLE_STRING_EMPTY);
			
			// Node attributes that are copied from the representative node
			// name, database identifier (STRINGID), @id, namespace, species, enhanced label
			// NAME, STRINGID, ID, NAMESPACE, SPECIES, ELABEL_STYLE
			for (String attr : SharedProperties.nodeAttrinbutesToCopyString) {
				if (retrievedNetwork.getDefaultNodeTable().getColumn(attr) == null)
					continue;
				retrievedNetwork.getRow(groupNode).set(attr,
						retrievedNetwork.getRow(reprNode).get(attr, String.class));					
			}
			
			// Node attributes (string) that are concatenated
			// canonical, display name, full name, dev. level, family
			// CANONICAL, DISPLAY, FULLNAME, DEVLEVEL, FAMILY
			for (String attr : SharedProperties.nodeAttrinbutesToConcatString) {
				if (retrievedNetwork.getDefaultNodeTable().getColumn(attr) == null)
					continue;
				String concatValue = "";
				for (CyNode node : nodesForGroup) {
					String nodeValue = retrievedNetwork.getRow(node).get(attr, String.class);
					if (nodeValue == null) 
						continue;
					concatValue += SharedProperties.ATTRIBUTE_CONCAT_SYMBOL + nodeValue;
				}
				if (concatValue.startsWith(SharedProperties.ATTRIBUTE_CONCAT_SYMBOL))
					concatValue = concatValue.substring(1);
				retrievedNetwork.getRow(groupNode).set(attr, concatValue);
			}
			// Node attributes (list of strings) that are concatenated
			// structures
			if (retrievedNetwork.getDefaultNodeTable().getColumn(SharedProperties.STRUCTURES) != null) {
				Set<String> structures = new HashSet<String>();
				for (CyNode node : nodesForGroup) {
					List<String> nodeStructures = (List<String>) retrievedNetwork.getRow(node).get(SharedProperties.STRUCTURES, List.class);
					if (nodeStructures == null || nodeStructures.size() == 0 || nodeStructures.get(0).equals(""))
						continue;
					structures.addAll(nodeStructures);
				}
				retrievedNetwork.getRow(groupNode).set(SharedProperties.STRUCTURES, new ArrayList<String>(structures));
			}
			
			// Node attributes that are averaged
			// all compartment cols, all tissue cols, interactor score?
			Collection<CyColumn> colsToAverage = retrievedNetwork.getDefaultNodeTable().getColumns(SharedProperties.COMPARTMENT_NAMESPACE);
			colsToAverage.addAll(retrievedNetwork.getDefaultNodeTable().getColumns(SharedProperties.TISSUE_NAMESPACE));
			colsToAverage.add(retrievedNetwork.getDefaultNodeTable().getColumn(SharedProperties.INTERACTORSCORE));
			for (CyColumn col : colsToAverage) {
				double averagedValue = 0.0;
				//int numValues = 0;
				if (col == null) 
					continue;
				for (CyNode node : nodesForGroup) {
					Double nodeValue = retrievedNetwork.getRow(node).get(col.getName(), Double.class);
					if (nodeValue == null) 
						continue;
					averagedValue += nodeValue.doubleValue();
					//numValues += 1;
				}
				// for node attributes, we average based on number of group nodes, e.g. a node with a missing value is considered to have a value = 0 
				// TODO: shorten to 6 digits precision or not?
				retrievedNetwork.getRow(groupNode).set(col.getName(), Double.valueOf(averagedValue/nodesForGroup.size()));
			}
		}

		// TODO: decide how to handle networks with confidence 1.0, that might contain only identity edges!
		
		// collapse all the groups
		// TODO: aggregation is turned off in the cy activator, but do we need to check the preferences again?
		for (CyGroup group : groups) {
			// System.out.println("collapsing group " + retrievedNetwork.getRow(group.getGroupNode()).get(CyNetwork.NAME, String.class) + " (SUID: " + group.getGroupNode().getSUID() + ")");
			group.collapse(retrievedNetwork);				
		}
					
		// retrieve the stringdb namespace columns
		Collection<CyColumn> stringdbCols = retrievedNetwork.getDefaultEdgeTable().getColumns(SharedProperties.STRINGDB_NAMESPACE);
		List<String> edgeColsToAggregate = new ArrayList<String>();
		for (CyColumn col : stringdbCols) {
			if (col == null || !col.getType().equals(Double.class)) 
				continue;
			edgeColsToAggregate.add(col.getName());
		}
		
		// do edge attribute aggregation for the stringdb namespace columns 
		for (CyGroup group : groups) {
			//System.out.println("aggregating edge attributes for group "
			//		+ retrievedNetwork.getRow(group.getGroupNode()).get(CyNetwork.NAME, String.class) + " (SUID: "
			//		+ group.getGroupNode().getSUID() + ")");
			// group network is a network representation of the nodes in the group and the edges that connect them
			// CyNetwork groupNetwork = group.getGroupNetwork();
			// root network is the network that contains the group and the retrieved network?! NOT the same as the retrieved network above
			// CyNetwork rootNetwork = group.getRootNetwork();				
			// get all external edges, includes both meta edges and edges from any node in the group to any other node in the network 
			// Set<CyEdge> externalEdges = group.getExternalEdgeList();
			// the adjacent edges to the node representing the group are all meta edges between the group and other nodes, so they are not the same as the external edges
			List<CyEdge> groupNodeEdges = retrievedNetwork.getAdjacentEdgeList(group.getGroupNode(), Type.ANY);
			for (CyEdge newEdge : groupNodeEdges) {
				// find the neighbor
				CyNode neighbor = null;
				if (group.getGroupNode().equals(newEdge.getSource())) 
					neighbor = newEdge.getTarget();
				else 
					neighbor = newEdge.getSource();
				// aggregate edge attributes 
				manager.aggregateGroupEdgeAttributes(retrievedNetwork, group, newEdge, group.getNodeList(), neighbor, edgeColsToAggregate);
			}
		}
		
		// redo layout and set visual properties!
		VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
		VisualStyle currentVS = vmm.getCurrentVisualStyle();
		
		// add node transparency for protein group nodes
		VisualMappingFunctionFactory discreteFactory = 
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		DiscreteMapping<String,Integer> dMapping = (DiscreteMapping) currentVS.getVisualMappingFunction(BasicVisualLexicon.NODE_TRANSPARENCY);
		if (dMapping == null) {
			dMapping = (DiscreteMapping) discreteFactory.createVisualMappingFunction(SharedProperties.TYPE, String.class, BasicVisualLexicon.NODE_TRANSPARENCY);
		} 
		dMapping.putMapValue(SharedProperties.NODE_TYPE_PG, 50);
		currentVS.addVisualMappingFunction(dMapping);
		
		// TODO: add visual mapping for edge lines
		VisualMappingFunctionFactory continuousFactory = manager.getService(VisualMappingFunctionFactory.class,
				"(mapping.type=continuous)");
		ContinuousMapping<Double, LineType> cMapping = (ContinuousMapping) continuousFactory
				.createVisualMappingFunction(SharedProperties.SCORE, Double.class, BasicVisualLexicon.EDGE_LINE_TYPE);
		cMapping.addPoint(this.protected_cutoff, new BoundaryRangeValues<LineType>(LineTypeVisualProperty.EQUAL_DASH,
				LineTypeVisualProperty.SOLID, LineTypeVisualProperty.SOLID));
		currentVS.addVisualMappingFunction(cMapping);

		
		// lay out network after collapsing groups 
		CyNetworkView networkView = manager.getCurrentNetworkView();
		if (networkView != null && networkView.getModel().equals(retrievedNetwork)) {
			CyLayoutAlgorithm alg = manager.getService(CyLayoutAlgorithmManager.class).getLayout("force-directed");
			Object context = alg.createLayoutContext();
			TunableSetter setter = manager.getService(TunableSetter.class);
			Map<String, Object> layoutArgs = new HashMap<>();
			layoutArgs.put("defaultNodeMass", 10.0);
			setter.applyTunables(context, layoutArgs);
			Set<View<CyNode>> nodeViews = new HashSet<>(networkView.getNodeViews());
			insertTasksAfterCurrentTask(alg.createTaskIterator(networkView, context, nodeViews, SharedProperties.SCORE));
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
