package dk.ku.cpr.proteoVisualizer.internal.tasks;

import javax.swing.JOptionPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;
import dk.ku.cpr.proteoVisualizer.internal.ui.RetrieveStringNetworkWindow;
import dk.ku.cpr.proteoVisualizer.internal.utils.SwingUtil;

public class ShowRetrieveWindowTask extends AbstractTask {
	
	private AppManager manager;

	public ShowRetrieveWindowTask(AppManager manager) {
		super();
		this.manager = manager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// First we make sure that enhancedGraphics is installed and enabled
		AvailableCommands availableCommands = (AvailableCommands) this.manager.getService(AvailableCommands.class);
		if (!availableCommands.getNamespaces().contains("string")) {
			JOptionPane.showMessageDialog(this.manager.getService(CySwingApplication.class).getJFrame(),
					"You need to install stringApp from the App Manager or Cytoscape App Store.",
					"Dependency error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		SwingUtil.invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				RetrieveStringNetworkWindow retrieveWindow =  new RetrieveStringNetworkWindow(manager);
				retrieveWindow.setVisible(true);
			}
		});
	}

}
