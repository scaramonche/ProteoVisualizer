package dk.ku.cpr.proteoVisualizer.internal.tasks;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.json.JSONResult;

import dk.ku.cpr.proteoVisualizer.internal.model.SharedProperties;

public class AboutTask extends AbstractTask implements ObservableTask {

	final String version;
	CyServiceRegistrar reg;
	
	public AboutTask(final String version, CyServiceRegistrar reg) {
		this.version = version;
		this.reg = reg;
	}

	public void run(TaskMonitor monitor) {
		monitor.setTitle("Proteo Visualizer About page");
		monitor.showMessage(Level.INFO, SharedProperties.ABOUT_URI);
		OpenBrowser openBrowser = reg.getService(OpenBrowser.class);
		if (openBrowser != null)
			openBrowser.openURL(SharedProperties.ABOUT_URI);
	}

	@SuppressWarnings("unchecked")
	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			String response = "About URI: "+SharedProperties.ABOUT_URI+"\n";
			return (R)response;
		} else if (type.equals(JSONResult.class)) {
			return (R)new JSONResult() {
				public String getJSON() { return "{\"aboutURI\":\""+SharedProperties.ABOUT_URI+"\"}"; }
			};
		}
		return (R)SharedProperties.ABOUT_URI;
	}

	@SuppressWarnings("unchecked")
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class, String.class);
	}
}
