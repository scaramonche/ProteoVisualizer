package dk.ku.cpr.proteinGroupsApp.internal.model;

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


	/**
	 * Node or edge attribute names.
	 */
	public static String STRINGDB_NAMESPACE = "stringdb";
	public static String NAMESPACE_SEPARATOR = "::";
	public static String TISSUE_NAMESPACE = "tissue";
	public static String COMPARTMENT_NAMESPACE = "compartment";
	
	// Node information
	public static String CANONICAL = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "canonical name";
	public static String DISPLAY = "display name";
	public static String FULLNAME = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "full name";
	public static String CV_STYLE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "chemViz Passthrough";
	public static String ELABEL_STYLE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "enhancedLabel Passthrough";
	public static String ID = "@id";
	public static String DESCRIPTION = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "description";
	public static String USE_ENRICHMENT = "use for enrichment";
	public static String IMAGE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "imageurl";
	public static String NAMESPACE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "namespace";
	public static String QUERYTERM = "query term";
	public static String SEQUENCE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "sequence";
	public static String SMILES = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "smiles";
	public static String SPECIES = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "species";
	public static String STRINGID = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "database identifier";
	public static String STYLE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "STRING style";
	public static String TYPE = STRINGDB_NAMESPACE + NAMESPACE_SEPARATOR + "node type";

	// We forbid the class to have instances
	private SharedProperties() {
	}

}
