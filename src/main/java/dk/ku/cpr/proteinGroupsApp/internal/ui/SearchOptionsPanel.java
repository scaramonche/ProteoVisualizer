package dk.ku.cpr.proteinGroupsApp.internal.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import dk.ku.cpr.proteinGroupsApp.internal.model.AppManager;
import dk.ku.cpr.proteinGroupsApp.internal.model.NetworkType;
import dk.ku.cpr.proteinGroupsApp.internal.model.SharedProperties;
import dk.ku.cpr.proteinGroupsApp.internal.model.StringSpecies;
import dk.ku.cpr.proteinGroupsApp.internal.tasks.StringCommandTaskFactory;

public class SearchOptionsPanel extends JPanel implements TaskObserver { 
	//StringNetwork stringNetwork = null;
	//StringNetwork initialStringNetwork = null;
	final AppManager manager;

	JPanel speciesBox;
	JComboBox<StringSpecies> speciesCombo;
	JSlider confidenceSlider;
	JTextField confidenceValue;
	JRadioButton physicalNetwork;
	JRadioButton functionalNetwork;
	JPanel advancedOptions;
	NumberFormat formatter = new DecimalFormat("#0.00");
	NumberFormat intFormatter = new DecimalFormat("#0");
	private boolean ignore = false;

	private String defaultSpecies;
	private int confidence;
	private NetworkType networkType;
	private List<StringSpecies> speciesList;
	
	public SearchOptionsPanel(final AppManager manager) {
		super(new GridBagLayout());
		this.manager = manager;
		confidence = (int)(manager.getDefaultConfidence());
		this.networkType = manager.getDefaultNetworkType();
		StringCommandTaskFactory factory = new StringCommandTaskFactory(this.manager,
				SharedProperties.STRING_CMD_LIST_SPECIES, null, this);
		TaskIterator ti = factory.createTaskIterator();
		this.manager.executeSynchronousTask(ti);
		initOptions();
		setPreferredSize(new Dimension(700,125));
	}

	private void initOptions() {
		// Add species combobox
		EasyGBC c = new EasyGBC();
		speciesList = new ArrayList<StringSpecies>();

		speciesList = getSpeciesList();
		speciesBox = createSpeciesComboBox(speciesList);
		add(speciesBox, c.expandHoriz().insets(5,5,0,5));

		// Create the radio buttons for the network type
		JPanel networkTypeRadioButtons = createNetworkTypeRadioGroup();
		add(networkTypeRadioButtons, c.down().expandBoth().insets(5,5,0,5));

		// Create the slider for the confidence cutoff
		JPanel confidenceSlider = createConfidenceSlider();
		add(confidenceSlider, c.down().expandBoth().insets(5,5,0,5));
	}

	
	List<StringSpecies> getSpeciesList() {
		// Create the species panel
		// Retrieve only the list of main species for now, otherwise the dialogs are very slow
		List<StringSpecies> speciesList = StringSpecies.getModelSpecies();
		return speciesList;
	}

	JPanel createSpeciesComboBox(List<StringSpecies> speciesList) {
    // System.out.println("createSpeciesComboBox");
		JPanel speciesPanel = new JPanel(new GridBagLayout());
		EasyGBC c = new EasyGBC();
		JLabel speciesLabel = new JLabel("Species:");
		c.noExpand().insets(0,5,0,5);
		speciesPanel.add(speciesLabel, c);
		speciesCombo = new JComboBox<StringSpecies>(speciesList.toArray(new StringSpecies[1]));
		
		if (defaultSpecies == null ) {
			speciesCombo.setSelectedItem(manager.getDefaultSpecies());
		} 
		else {
			speciesCombo.setSelectedItem(StringSpecies.getSpecies(defaultSpecies));
		}
		JComboBoxDecorator decorator = new JComboBoxDecorator(speciesCombo, true, true, speciesList);
		decorator.decorate(speciesList); 
		c.right().expandHoriz().insets(0,5,0,5);
		speciesPanel.add(speciesCombo, c);
		return speciesPanel;
	}

	public StringSpecies getSpecies() throws RuntimeException {
    Object sp = speciesCombo.getSelectedItem();
    if (sp instanceof StringSpecies)
      return (StringSpecies)sp;

    // OK, we haven't gotten a real species, so let's see if we can convert it
    String spText = sp.toString();
    if (spText.length() == 0)
      return null;

    // See if we know about this species
    StringSpecies species = StringSpecies.getSpecies(spText);
    if (species == null) {
      throw new RuntimeException("No such species");
    }
    return species;
	}

	public String getSpeciesText() {
		if (speciesCombo != null)
			return speciesCombo.getSelectedItem().toString();
		return null;
	}

	public void setSpeciesText(String speciesText) {
		if (speciesCombo != null)
			speciesCombo.setSelectedItem(StringSpecies.getSpecies(speciesText));
	}

	public void setSpecies(StringSpecies species) {
		if (speciesCombo != null)
			speciesCombo.setSelectedItem(species);
	}


	JPanel createNetworkTypeRadioGroup() {

		JPanel netTypePanel = new JPanel(new GridBagLayout());
		netTypePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		EasyGBC c = new EasyGBC();
		
		c.anchor("west").noExpand().insets(0,5,0,5);
		JLabel netTypeLabel = new JLabel("Network type:");
		Font labelFont = netTypeLabel.getFont();
		netTypeLabel.setFont(new Font(labelFont.getFontName(), Font.BOLD, labelFont.getSize()));
		netTypePanel.add(netTypeLabel, c);
		
		c.right().noExpand().insets(0,5,0,5);
		functionalNetwork = new JRadioButton(NetworkType.FUNCTIONAL.toString(), true);
		netTypePanel.add(functionalNetwork, c);

		c.right().expandHoriz().insets(0,5,0,5);
		physicalNetwork = new JRadioButton(NetworkType.PHYSICAL.toString(), false);
		netTypePanel.add(physicalNetwork, c);
		
		ButtonGroup group = new ButtonGroup();
		group.add(physicalNetwork);
		group.add(functionalNetwork);
		
		if (networkType.equals(NetworkType.PHYSICAL)) 
			physicalNetwork.setSelected(true);
		
		return netTypePanel;
	}
	
	public NetworkType getNetworkType() {
		if (physicalNetwork.isSelected())
			return NetworkType.PHYSICAL;
		else 
			return NetworkType.FUNCTIONAL;
	}

	public void setNetworkType(NetworkType type) {
		if (type.equals(NetworkType.FUNCTIONAL)) 
			functionalNetwork.setSelected(true);
		else 
			physicalNetwork.setSelected(true);
	}
	
	JPanel createConfidenceSlider() {
		JPanel confidencePanel = new JPanel(new GridBagLayout());
		confidencePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		EasyGBC c = new EasyGBC();

		Font labelFont;
		{
			c.anchor("west").noExpand().insets(0,5,0,5);
			JLabel confidenceLabel = new JLabel("Confidence (score) cutoff:");
			labelFont = confidenceLabel.getFont();
			confidenceLabel.setFont(new Font(labelFont.getFontName(), Font.BOLD, labelFont.getSize()));
			confidencePanel.add(confidenceLabel, c);
		}

		{
			confidenceSlider = new JSlider();
			Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
			Font valueFont = new Font(labelFont.getFontName(), Font.BOLD, labelFont.getSize()-4);
			for (int value = 0; value <= 100; value += 10) {
				double labelValue = (double)value/100.0;
				JLabel label = new JLabel(formatter.format(labelValue));
				label.setFont(valueFont);
				labels.put(value, label);
			}
			confidenceSlider.setLabelTable(labels);
			confidenceSlider.setPaintLabels(true);
			confidenceSlider.setValue(confidence);

			confidenceSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (ignore) return;
					ignore = true;
					int value = confidenceSlider.getValue();
					confidenceValue.setText(formatter.format(((double)value)/100.0));
					ignore = false;
				}
			});
			// c.anchor("southwest").expandHoriz().insets(0,5,0,5);
			c.right().expandHoriz().insets(0,5,0,5);
			confidencePanel.add(confidenceSlider, c);
		}

		{
			confidenceValue = new JTextField(4);
			confidenceValue.setHorizontalAlignment(JTextField.RIGHT);
			confidenceValue.setText(formatter.format(((double)confidence)/100.0));
			c.right().noExpand().insets(0,5,0,5);
			confidencePanel.add(confidenceValue, c);

			confidenceValue.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					textFieldValueChanged();
				}
			});

			confidenceValue.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					textFieldValueChanged();
				}
			});

		}
		return confidencePanel;
	}

	public int getConfidence() {
		return confidenceSlider.getValue();
	}

	public void setConfidence(int conf) {
		confidenceSlider.setValue(conf);
	}
	
	private void textFieldValueChanged() {
		if (ignore) return;
		ignore = true;
		String text = confidenceValue.getText();
		Number n = formatter.parse(text, new ParsePosition(0));
		double val = 0.0;
		if (n == null) {
			try {
				val = Double.valueOf(confidenceValue.getText());
			} catch (NumberFormatException nfe) {
				val = inputError();
			}
		} else if (n.doubleValue() > 1.0 || n.doubleValue() < 0.0) {
			val = inputError();
		} else {
			val = n.doubleValue();
		}

		val = val*100.0;
		confidenceSlider.setValue((int)val);
		ignore = false;
	}

	private double inputError() {
		confidenceValue.setBackground(Color.RED);
		JOptionPane.showMessageDialog(null, 
				                          "Please enter a confence cutoff between 0.0 and 1.0", 
											            "Alert", JOptionPane.ERROR_MESSAGE);
		confidenceValue.setBackground(UIManager.getColor("TextField.background"));

		// Reset the value to correspond to the current slider setting
		double val = ((double)confidenceSlider.getValue())/100.0;
		confidenceValue.setText(formatter.format(val));
		return val;
	}

	public void cancel() {
		((Window)getRootPane().getParent()).dispose();
	}

	@Override
	public void taskFinished(ObservableTask task) {
		// TODO: move this code to a utility class 
		if (task.getClass().getSimpleName().equals("GetSpeciesTask")) {
			List<Map<String, String>> res = task.getResults(List.class);

			try {
				StringSpecies.readSpecies(res);
				this.speciesList = StringSpecies.getModelSpecies();
			} catch (Exception e) {
				throw new RuntimeException("Can't read species information");
			}

			speciesCombo = new JComboBox<StringSpecies>(speciesList.toArray(new StringSpecies[1]));
			// We select Human as default
			speciesCombo.setSelectedItem(manager.getDefaultSpecies());

			JComboBoxDecorator decorator = new JComboBoxDecorator(this.speciesCombo, true, true, speciesList);
			decorator.decorate(speciesList);
		}		
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
		// TODO Auto-generated method stub
		
	}


}
