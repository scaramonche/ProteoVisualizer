package dk.ku.cpr.proteoVisualizer.internal.tasks;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;

public class RetrieveStringNetworkTunableTask extends RetrieveStringNetworkTask {

	@Tunable(description = "Protein query", required = true, 
	         longDescription="Comma separated list of protein names or identifiers.",
					 exampleStringValue="EGFR,BRCA1,BRCA2,TP53")
	public String query = null;

	@Tunable(description = "New network name", 
	         longDescription="Name for the network to be created",
					 exampleStringValue="String Network")
	public String newNetName = "";

	@Tunable(description="Identifier of the species to query. REQUIRED if 'species' is not used.",
			required=false,
			tooltip="You can put here the taxon identifier of the species you want to query.",
			exampleStringValue="9606",
			gravity=1.0)
	public Integer taxonID;

	@Tunable(description="Name of the species to query. REQUIRED if 'taxonID' is not used.",
			required=false,
			tooltip="You can put here the name of the species you want to query.",
			exampleStringValue="Homo sapiens",
			gravity=1.0)
	public String species;

	@Tunable(description="Confidence (score) cutoff.",
			required=false,
			tooltip="Enter a value between 0.0 and 1.0 defining the confidence score the STRING network should have. Default: 0.40.",
			exampleStringValue="0.4",
			gravity=1.0)
	public Double cutoff;

	@Tunable(description="Network type.",
			required=false,
			tooltip="Choose one of the two network types (full STRING network or physical subnetwork) the STRING network should have. Default: full STRING network.",
			exampleStringValue="full STRING network",
			gravity=1.0)
	public ListSingleSelection<String> netType;

	public RetrieveStringNetworkTunableTask(AppManager manager) {
		super(manager);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (query != null) {
			this.setQuery(query);
		}
		if (newNetName != null) {
			this.setNetworkName(newNetName);
		}
		if(taxonID != null) {
			this.setTaxonID(taxonID);
		}
		if(species != null) {
			this.setSpecies(species);
		}
		if(cutoff != null) {
			this.setCutoff(cutoff.doubleValue());
		}
		if(netType != null) {
			this.setNetType(netType.getSelectedValue());
		}
		
		super.run(taskMonitor);
	}

}
