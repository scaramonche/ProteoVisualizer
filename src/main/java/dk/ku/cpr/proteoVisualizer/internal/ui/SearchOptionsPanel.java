package dk.ku.cpr.proteoVisualizer.internal.ui;

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

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
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

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;
import dk.ku.cpr.proteoVisualizer.internal.model.NetworkType;
import dk.ku.cpr.proteoVisualizer.internal.model.SharedProperties;
import dk.ku.cpr.proteoVisualizer.internal.model.StringSpecies;

public class SearchOptionsPanel extends JPanel { 
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
	private JTextField netName;
	private JComboBox<String> delimiter;
	private JCheckBox keepCollapsed;
	
	NumberFormat formatter = new DecimalFormat("#0.00");
	NumberFormat intFormatter = new DecimalFormat("#0");
	private boolean ignore = false;

	private int confidence;
	private NetworkType networkType;
	private List<StringSpecies> speciesList;
	
	public SearchOptionsPanel(final AppManager manager) {
		super(new GridBagLayout());
		this.manager = manager;
		confidence = manager.getDefaultConfidence();
		this.networkType = manager.getDefaultNetworkType();
		initOptions();
		setPreferredSize(new Dimension(700,200));
	}

	private void initOptions() {
		// Add species combobox
		EasyGBC c = new EasyGBC();
		speciesList = StringSpecies.getModelSpecies();		
		if (speciesList == null)
			speciesList = new ArrayList<StringSpecies>();
		
		speciesBox = createSpeciesComboBox(speciesList);
		add(speciesBox, c.expandHoriz().insets(5,5,0,5));

		// Create the radio buttons for the network type
		JPanel networkTypeRadioButtons = createNetworkTypeRadioGroup();
		add(networkTypeRadioButtons, c.down().expandBoth().insets(5,5,0,5));

		// Create the slider for the confidence cutoff
		JPanel confidenceSlider = createConfidenceSlider();
		add(confidenceSlider, c.down().expandBoth().insets(5,5,0,5));
		
		// Add some "advanced" options
		advancedOptions = createAdvancedOptions();
		add(advancedOptions, c.down().expandBoth().insets(5,5,0,5));
	}

	
	JPanel createSpeciesComboBox(List<StringSpecies> speciesList) {
    // System.out.println("createSpeciesComboBox");
		JPanel speciesPanel = new JPanel(new GridBagLayout());
		EasyGBC c = new EasyGBC();
		JLabel speciesLabel = new JLabel("Species:");
		c.noExpand().insets(0,5,0,5);
		speciesPanel.add(speciesLabel, c);
		speciesCombo = new JComboBox<StringSpecies>(speciesList.toArray(new StringSpecies[1]));
		speciesCombo.setSelectedItem(manager.getDefaultSpecies());
		JComboBoxDecorator decorator = new JComboBoxDecorator(speciesCombo, true, true, speciesList);
		decorator.decorate(speciesList); 
		c.right().expandHoriz().insets(0,5,0,5);
		speciesPanel.add(speciesCombo, c);
		return speciesPanel;
	}

	public StringSpecies getSpecies() throws RuntimeException {
		Object sp = speciesCombo.getSelectedItem();
		if (sp instanceof StringSpecies)
			return (StringSpecies) sp;

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

	JPanel createAdvancedOptions() {
		JPanel advancedPanel = new JPanel(new GridBagLayout());
		advancedPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		EasyGBC c = new EasyGBC();
		
		JLabel optionsLabel = new JLabel("<html><b>Options:</b></html>");
		c.anchor("west").insets(0,5,0,5);
		advancedPanel.add(optionsLabel, c);
		
		c.right().noExpand().insets(0,10,0,5);		
		keepCollapsed = new JCheckBox("Collapse groups", true);
		keepCollapsed.setToolTipText("");
		advancedPanel.add(keepCollapsed, c);

		c.right().noExpand().insets(0,10,0,1);
		JLabel delimiterLabel = new JLabel("Delimiter:");
		advancedPanel.add(delimiterLabel, c);

		c.right().noExpand().insets(0,1,0,5);
		delimiter = new JComboBox<String>(SharedProperties.pg_delimiters);
		delimiter.setSelectedItem(SharedProperties.DEFAULT_PG_DELIMITER);
		advancedPanel.add(delimiter, c);

		c.right().noExpand().insets(0,10,0,1);
		JLabel netNameLabel = new JLabel("Network Name:");
		advancedPanel.add(netNameLabel, c);

		c.right().expandHoriz().insets(0,1,0,5);
		netName = new JTextField();
		advancedPanel.add(netName, c);

		// if we add an option to create or not a network view
		//c.right().expandHoriz().insets(0,10,0,5);
		//createNetView = new JCheckBox("Create network view", true);
		//advancedPanel.add(createNetView, c);
		
		return advancedPanel;
	}

	public boolean getKeepCollapsed() {
		return keepCollapsed.isSelected();
	}

	public void setKeepCollapsed(boolean selected) {
		keepCollapsed.setSelected(selected);
	}

	public String getDelimiter() {
		return (String)delimiter.getSelectedItem();
	}

	public void setDelimiter(String delim) {
		delimiter.setSelectedItem(delim);
	}

	public String getNetworkName() {
		return netName.getText();
	}

	public void setNetworkName(String name) {
		netName.setText(name);
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

}
