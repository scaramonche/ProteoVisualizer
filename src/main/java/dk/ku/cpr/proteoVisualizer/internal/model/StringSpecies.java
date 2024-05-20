package dk.ku.cpr.proteoVisualizer.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;


public class StringSpecies implements Comparable<StringSpecies> {
	private static Map<String, StringSpecies> nameToSpeciesMap;
	private static List<StringSpecies> modelSpecies;
	private static List<StringSpecies> allSpecies;
	private static StringSpecies humanSpecies;

	private Integer taxonID;
	// TODO: check species names and sync with current version
	private String abbreviatedName;
	private String scientificName;
	
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
		for (String s: nameToSpeciesMap.keySet()) {
			if (s.regionMatches(true, 0, str, 0, str.length())) { 
				retValue.add(nameToSpeciesMap.get(s));
			}
		}
		return retValue;
	}

	public static List<StringSpecies> readSpecies(List<Map<String, String>> speciesFromTask) throws Exception {
		modelSpecies = new ArrayList<StringSpecies>();
		allSpecies = new ArrayList<StringSpecies>();
		nameToSpeciesMap = new TreeMap<String, StringSpecies>();

		for (Map<String, String> r : speciesFromTask) {
			StringSpecies species = new StringSpecies(r);
			allSpecies.add(species);
			nameToSpeciesMap.put(species.toString(), species);

			// TODO: Fix the way we set the model species
			String homoSapiensAbbrevName = "Homo sapiens";
			if (species.getAbbrevName().equals(homoSapiensAbbrevName)) {
				humanSpecies = species;
			}

			List<String> modelSpeciesAbbrevNames = List.of(homoSapiensAbbrevName, "Mus musculus", "Rattus norvegicus",
					"Saccharomyces cerevisiae", "Caenorhabditis elegans", "Escherichia coli str. K-12 substr. MG1655");
			if (modelSpeciesAbbrevNames.contains(species.getAbbrevName())) {
				modelSpecies.add(species);
			}
		}

		// sort all collections
		Collections.sort(allSpecies);
		Collections.sort(modelSpecies);

		return allSpecies;
	}

	public static StringSpecies getSpecies(String speciesName) {
		if (nameToSpeciesMap == null || speciesName == null) return null;
		if (nameToSpeciesMap.containsKey(speciesName))
			return nameToSpeciesMap.get(speciesName);

		if (allSpecies == null) return null;
		for (StringSpecies s: allSpecies) {
			if (s.getName().equalsIgnoreCase(speciesName))
				return s;
		}
		return null;
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
	
	public static void loadSpecies(final AppManager manager) {
		LoadSpecies ls = new LoadSpecies(manager);
		Thread t = new Thread(ls);
		t.start();
	}

	static class LoadSpecies implements Runnable, TaskObserver {
		final AppManager manager;
		LoadSpecies(final AppManager manager) { 
			this.manager = manager; 
		}

		@Override
		public void run() {
			while (!manager.haveCommand("string", "list species")) {
				try {
					Thread.sleep(500);
				} catch (Exception e) {}
			}
			if (manager.haveCommand("string", "list species"))
				manager.executeCommand("string", "list species", null, this, true);
		}

		@Override
		public void allFinished(FinishStatus status) {			
		}

		@Override
		public void taskFinished(ObservableTask task) {
			if (task.getClass().getSimpleName().equals("GetSpeciesTask")) {
				List<Map<String, String>> res = task.getResults(List.class);
				try {
					StringSpecies.readSpecies(res);
				} catch (Exception e) {
					throw new RuntimeException("Can't read species information");
				}
				// now that species are loaded, register the search factories 
				manager.registerSearchTaskFactories();
			}
		}
	}
}

