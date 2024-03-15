package dk.ku.cpr.proteoVisualizer.internal.ui;

import javax.swing.JDialog;

import org.cytoscape.application.swing.CySwingApplication;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;

public abstract class AppWindow extends JDialog {
	
	private static final long serialVersionUID = 7970396803455866978L;

	protected AppManager manager;
	
	public AppWindow(AppManager manager, String title) {
		super(manager.getService(CySwingApplication.class).getJFrame(), title);
		
		this.manager = manager;
		
		this.setModal(true);
		this.setResizable(false);
	}
	
	public AppWindow(AppManager manager) {
		this(manager, "");
	}
}
