package dk.ku.cpr.proteinGroupsApp.internal.tasks;

import java.awt.Color;
import java.awt.Font;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.search.AbstractNetworkSearchTaskFactory;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.proteinGroupsApp.internal.model.AppManager;
import dk.ku.cpr.proteinGroupsApp.internal.model.StringSpecies;
import dk.ku.cpr.proteinGroupsApp.internal.ui.SearchOptionsPanel;
import dk.ku.cpr.proteinGroupsApp.internal.ui.SearchQueryComponent;
import dk.ku.cpr.proteinGroupsApp.internal.utils.TextIcon;

public class StringPGSearchTaskFactory extends AbstractNetworkSearchTaskFactory implements TaskObserver {
	AppManager manager;
	static String STRING_ID = "dk.ku.cpr.pg.string";
	static String STRING_URL = "http://string-db.org";
	static String STRING_NAME = "STRING: protein groups query";
	static String STRING_DESC = "Search STRING for protein-protein interactions";
	static String STRING_DESC_LONG = "<html>The protein query retrieves a STRING network for one or more proteins. <br />"
										+ "STRING is a database of known and predicted protein interactions for <br />"
										+ "thousands of organisms, which are integrated from several sources, <br />"
										+ "scored, and transferred across orthologs. The network includes both <br />"
										+ "physical interactions and functional associations.</html>";

	//private StringNetwork stringNetwork = null;
	private SearchOptionsPanel optionsPanel = null;
	private SearchQueryComponent queryComponent = null;
	private final Logger logger = Logger.getLogger(CyUserLog.NAME);
	
	private static final Font iconFont = new Font("Monospaced", Font.PLAIN, 6);
	private static final Icon icon = new TextIcon("Proteo Visualizer", iconFont, Color.BLACK, 36, 36);
	// private static final Icon icon = new TextIcon(IconUtils.STRING_LAYERS, IconUtils.getIconFont(32.0f), IconUtils.STRING_COLORS, 36, 36);
	
	
	private static URL stringURL() {
		try {
			return new URL(STRING_URL);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public StringPGSearchTaskFactory(AppManager manager) {
		super(STRING_ID, STRING_NAME, STRING_DESC, icon, StringPGSearchTaskFactory.stringURL());
		this.manager = manager;
	}

	public boolean isReady() { 
		if (queryComponent.getQueryText() != null && queryComponent.getQueryText().length() > 0 && getTaxId() != -1)
			return true; 
		return false;
	}

	public TaskIterator createTaskIterator() {
		StringSpecies species = getSpecies();
		String query = queryComponent.getQueryText();
		// Strip off any blank lines as well as trailing spaces
		query = query.replaceAll("(?m)^\\s*", "");
		query = query.replaceAll("(?m)\\s*$", "");
		// TODO: move this to a utility class later on
		Set<String> queryIDs = new HashSet<String>(Arrays.asList(query.split("\n")));
		Set<String> allProteins = new HashSet<String>();
		HashMap<String, List<String>> pg2proteins = new HashMap<String, List<String>>();
		HashMap<String, List<String>> protein2pgs = new HashMap<String, List<String>>();
			for (String queryID : queryIDs) {
				// TODO: let the user choose the delimiter of the proteins within a group
				List<String> proteinIDs = Arrays.asList(queryID.split(";"));
				pg2proteins.put(queryID, proteinIDs);
				for (String protein : proteinIDs) {
					List<String> pgs = new ArrayList<String>();
					if (protein2pgs.containsKey(protein)) {
						pgs = protein2pgs.get(protein);
					} 
					pgs.add(queryID);
					protein2pgs.put(protein, pgs);
				}
				allProteins.addAll(proteinIDs);
		}
		String proteinQueryInput = String.join(",", allProteins);
		System.out.println(proteinQueryInput);
		//System.out.println(pg2proteins);
		//System.out.println(protein2pgs);
		
		// TODO: provide network name as part of the options?
		RetrieveStringNetworkTaskFactory factory = new RetrieveStringNetworkTaskFactory(this.manager);
		return factory.createTaskIterator(proteinQueryInput, species.getTaxonID(), species.getName(),
				getConfidence()/100.0, getNetworkType(), "", pg2proteins, protein2pgs, true);
	}

	@Override
	public String getName() { return STRING_NAME; }

	@Override
	public String getId() { return STRING_ID; }

	@Override
	public String getDescription() {
		return STRING_DESC_LONG;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public URL getWebsite() { 
		return StringPGSearchTaskFactory.stringURL();
	}

	// Create a JPanel that provides the species, confidence interval, and number of interactions
	// NOTE: we need to use reasonable defaults since it's likely the user won't actually change it...
	@Override
	public JComponent getOptionsComponent() {
		optionsPanel = new SearchOptionsPanel(manager);
		return optionsPanel;
	}

	@Override
	public JComponent getQueryComponent() {
		if (queryComponent == null)
			queryComponent = new SearchQueryComponent();
		return queryComponent;
	}

	public StringSpecies getSpecies() {
		// This will eventually come from the OptionsComponent...
		if (optionsPanel.getSpecies() != null)
			return optionsPanel.getSpecies();
		return StringSpecies.getHumanSpecies(); // Homo sapiens
	}

	public int getTaxId() {
		try {
			if (optionsPanel.getSpecies() != null) {
				return optionsPanel.getSpecies().getTaxonID();
			}
			return 9606; // Homo sapiens
		} catch (RuntimeException e) {
			// The user might not have given us a full species name
			String name = optionsPanel.getSpeciesText();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null, "Unknown species: '" + name + "'", "Unknown species",
							JOptionPane.ERROR_MESSAGE);
				}
			});
			// Reset
			optionsPanel.setSpeciesText(manager.getDefaultSpecies().toString());
			return -1;
		}
	}

	public int getConfidence() {
		// This will eventually come from the OptionsComponent...
		return optionsPanel.getConfidence();
	}

	public String getNetworkType() {
		// This will eventually come from the OptionsComponent...
		return optionsPanel.getNetworkType().toString();
	}

	@Override
	public void taskFinished(ObservableTask task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
		// TODO Auto-generated method stub
		
	}


}
