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

	// We forbid the class to have instances
	private SharedProperties() {
	}

}
