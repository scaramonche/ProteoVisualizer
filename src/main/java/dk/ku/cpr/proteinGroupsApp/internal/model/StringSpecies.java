package dk.ku.cpr.proteinGroupsApp.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class StringSpecies implements Comparable<StringSpecies> {
	private Integer taxonID;
	// TODO: check species names and sync with current version
	private String abbreviatedName;
	private String scientificName;
	private static Map<String, StringSpecies> nameSpecies;
	private static List<StringSpecies> modelSpecies;
	private static List<StringSpecies> allSpecies;
	private static StringSpecies humanSpecies;
	
	public StringSpecies(Map<String,String> data) {
		super();
		this.taxonID = Integer.valueOf(data.get("taxonomyId"));
		this.abbreviatedName = data.get("scientificName"); 
		this.scientificName =  data.get("abbreviatedName"); 
	}

	public int compareTo(StringSpecies t) {
		if (t.toString() == null) return 1;
		return this.toString().compareTo(t.toString());
	}

	public static List<StringSpecies> search(String str) {
		List<StringSpecies> retValue = new ArrayList<StringSpecies>();
		for (String s: nameSpecies.keySet()) {
			if (s.regionMatches(true, 0, str, 0, str.length())) { 
				retValue.add(nameSpecies.get(s));
			}
		}
		return retValue;
	}

	public static List<StringSpecies> readSpecies(List<Map<String, String>> speciesFromTask) throws Exception {
		modelSpecies = new ArrayList<StringSpecies>();
		allSpecies = new ArrayList<StringSpecies>();
		nameSpecies = new TreeMap<String, StringSpecies>();

		for (Map<String, String> r : speciesFromTask) {
			StringSpecies species = new StringSpecies(r);
			allSpecies.add(species);
			nameSpecies.put(species.toString(), species);

			// TODO: Fix the way we set the model species
			if (species.getAbbrevName().equals("Homo sapiens")) {
				modelSpecies.add(species);
				humanSpecies = species;
			} else if (species.getAbbrevName().equals("Mus musculus")
					|| species.getAbbrevName().equals("Rattus norvegicus")
					|| species.getAbbrevName().equals("Saccharomyces cerevisiae")
					|| species.getAbbrevName().equals("Caenorhabditis elegans")
					|| species.getAbbrevName().equals("Escherichia coli str. K-12 substr. MG1655")) {
				modelSpecies.add(species);
			}
		}

		// sort all collections
		Collections.sort(allSpecies);
		Collections.sort(modelSpecies);

		return allSpecies;
	}

	public static StringSpecies getHumanSpecies() {
		return humanSpecies;
	}

	public static List<StringSpecies> getAllSpecies() {
		return allSpecies;
	}

	public static List<StringSpecies> getModelSpecies() {
		return modelSpecies;
	}

	public Integer getTaxonID() {
		return this.taxonID;
	}
	
	public String getName() {
		return this.scientificName; 
	}

	public String getAbbrevName() {
		return this.abbreviatedName; 
	}

	public String getQueryString() {
		return (this.abbreviatedName + " " + this.scientificName).toLowerCase();
	}

	public String toString() {
		return this.scientificName;
	}
}
