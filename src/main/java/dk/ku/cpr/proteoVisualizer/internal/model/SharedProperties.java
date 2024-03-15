package dk.ku.cpr.proteoVisualizer.internal.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ServiceProperties;

/**
 * Class used to store shared properties. The class also provides some useful
 * functions.
 */
public class SharedProperties {

	/** The prefix form hidden attributes. */
	public static final String APP_PREFIX = "dk.ku.cpr.proteinGroupsApp.";

	/** The menu where we store the actions. */
	public static final String APP_PREFERRED_MENU = ServiceProperties.APPS_MENU + ".Protein Groups";

	/** The namespace of the commands. */
	public static final String APP_COMMAND_NAMESPACE = "proteinGroups";

	/**
	 * Name of the stringApp command to query proteins. This is used to retrieve a
	 * STRING network.
	 */
	public static final String STRING_CMD_PROTEIN_QUERY = "protein query";
	/** Name of the stringApp command to list species. */
	public static final String STRING_CMD_LIST_SPECIES = "list species";


	public static final int defaultConfidence = 70;
	
	// App specific terms
	public static String NODE_TYPE_PG = "protein group";
	public static String STYLE_STRING_EMPTY = "string:";
	public static String ATTRIBUTE_CONCAT_SYMBOL = ";";
	public static String EDGE_TYPE_IDENTITY = "identity";
	
	// Node or edge attribute namesspaces
	public static String STRINGDB_NAMESPACE = "stringdb";
	public static String NAMESPACE_SEPARATOR = "::";
	public static String TISSUE_NAMESPACE = "tissue";
	public static String COMPARTMENT_NAMESPACE = "compartment";
	public static String TARGET_NAMESPACE = "target";
	
	// Node information for proteins
	public static String CANONICAL = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "canonical name";
	public static String DISPLAY = "display name";
	public static String FULLNAME = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "full name";
	public static String ELABEL_STYLE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "enhancedLabel Passthrough";
	public static String ID = "@id";
	public static String DESCRIPTION = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "description";
	public static String USE_ENRICHMENT = "use for enrichment";
	public static String IMAGE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "imageurl";
	public static String INTERACTORSCORE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "interactor score";
	public static String NAMESPACE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "namespace";
	public static String QUERYTERM = "query term";
	public static String SEQUENCE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "sequence";
	public static String SPECIES = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "species";
	public static String STRUCTURES = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "structures";
	public static String STRINGID = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "database identifier";
	public static String STYLE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "STRING style";
	public static String TYPE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "node type";

	// attributes from Pharos
	public static String DEVLEVEL = TARGET_NAMESPACE + NAMESPACE_SEPARATOR + "development level";
	public static String FAMILY = TARGET_NAMESPACE + NAMESPACE_SEPARATOR + "family";

	// Node information for compounds -> not relevant for PGs
	// public static String CV_STYLE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "chemViz Passthrough";
	// public static String SMILES = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "smiles";

	// Handling of attributes
	public static List<String> nodeAttrinbutesToCopyString = new ArrayList<String>(Arrays.asList(CyNetwork.NAME, STRINGID, ID, NAMESPACE, SPECIES, ELABEL_STYLE));	
	public static List<String> nodeAttrinbutesNotToCopyString = new ArrayList<String>(Arrays.asList(DESCRIPTION, SEQUENCE));
	public static List<String> nodeAttrinbutesToSetManually = new ArrayList<String>(Arrays.asList(QUERYTERM, STYLE, TYPE, USE_ENRICHMENT));
	public static List<String> nodeAttrinbutesToConcatString = new ArrayList<String>(Arrays.asList(CANONICAL, DISPLAY, FULLNAME, DEVLEVEL, FAMILY));

	// Edge information
	public static String SCORE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "score";
	public static String EDGEPROB = "edge prob";
	public static String EDGEPOSSIBLE = "# possible edges";
	public static String EDGEEXISTING = "# actual edges";
	public static String EDGEAGGREGATED = "aggregated";

	// We forbid the class to have instances
	private SharedProperties() {
	}

}
