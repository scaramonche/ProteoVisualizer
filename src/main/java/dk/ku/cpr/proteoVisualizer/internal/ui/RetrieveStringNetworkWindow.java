package dk.ku.cpr.proteoVisualizer.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.LookAndFeelUtil;

import dk.ku.cpr.proteoVisualizer.internal.model.AppManager;
import dk.ku.cpr.proteoVisualizer.internal.model.NetworkType;
import dk.ku.cpr.proteoVisualizer.internal.model.SharedProperties;
import dk.ku.cpr.proteoVisualizer.internal.model.StringSpecies;
import dk.ku.cpr.proteoVisualizer.internal.tasks.RetrieveStringNetworkTaskFactory;

public class RetrieveStringNetworkWindow extends AppWindow implements ActionListener {

	private static final long serialVersionUID = -6267319220376028418L;

	private JTextArea queryInput;
	private JTextField netName;
	private JComboBox<String> delimiter;
	private JCheckBox keepCollapsed;
	
	private List<StringSpecies> speciesList;
	private JComboBox<StringSpecies> selectSpecies;

	private JRadioButton physicalNetwork;
	private JRadioButton functionalNetwork;
	private NetworkType networkType = null;

	private JSlider confidenceSlider;
	private JTextField confidenceValue;
	private boolean ignore = false;
	NumberFormat formatter;

	private JButton closeButton;
	private JButton retrieveButton;

	public RetrieveStringNetworkWindow(AppManager manager) {
		super(manager, "Retrieve STRING network for protein groups");

		this.queryInput = new JTextArea();
		this.netName = new JTextField();
		this.delimiter = new JComboBox<String>(SharedProperties.pg_delimiters);
		this.delimiter.setSelectedItem(SharedProperties.DEFAULT_PG_DELIMITER);
		this.keepCollapsed = new JCheckBox();
		this.keepCollapsed.setSelected(true);
		
		this.speciesList = StringSpecies.getModelSpecies();
		if (speciesList == null)
			speciesList = new ArrayList<StringSpecies>();
		this.selectSpecies = new JComboBox<StringSpecies>(speciesList.toArray(new StringSpecies[1]));		
		this.selectSpecies.setSelectedItem(manager.getDefaultSpecies());

		JComboBoxDecorator decorator = new JComboBoxDecorator(this.selectSpecies, true, true, speciesList);
		decorator.decorate(speciesList);

		this.networkType = manager.getDefaultNetworkType();
		if (this.networkType.equals(NetworkType.FUNCTIONAL)) {
			this.functionalNetwork = new JRadioButton(NetworkType.FUNCTIONAL.toString(), true);
			this.physicalNetwork = new JRadioButton(NetworkType.PHYSICAL.toString(), false);
		} else {
			this.functionalNetwork = new JRadioButton(NetworkType.FUNCTIONAL.toString(), false);
			this.physicalNetwork = new JRadioButton(NetworkType.PHYSICAL.toString(), true);			
		}

		this.confidenceSlider = new JSlider();
		this.confidenceValue = new JTextField();

		this.closeButton = new JButton("Close");
		this.closeButton.addActionListener(this);

		this.retrieveButton = new JButton("Retrieve network");
		this.retrieveButton.addActionListener(this);

		// Init the NumberFormat to be 0.00 (with a dot)
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		this.formatter = new DecimalFormat("#0.00", dfs);

		LookAndFeelUtil.equalizeSize(this.closeButton, this.retrieveButton);
	}

	private JPanel createNetTypeRadioButtons() {
		JPanel netTypePanel = new JPanel(new GridBagLayout());
		netTypePanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		MyGridBagConstraints c = new MyGridBagConstraints();
		netTypePanel.add(functionalNetwork, c);
		netTypePanel.add(physicalNetwork, c.nextCol());

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

	private JPanel createConfidenceSlider() {
		JPanel confidencePanel = new JPanel(new GridBagLayout());
		confidencePanel.setOpaque(!LookAndFeelUtil.isAquaLAF());
		MyGridBagConstraints c = new MyGridBagConstraints();

		Font labelFont;
		{
			c.setAnchor("W").expandVertical();// .setInsets(0,5,0,5);
			JLabel confidenceLabel = new JLabel("Confidence (score) cutoff:");
			labelFont = confidenceLabel.getFont();
			confidencePanel.add(confidenceLabel, c);
		}

		{
			this.confidenceSlider = new JSlider();
			Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
			Font valueFont = new Font(labelFont.getFontName(), Font.BOLD, labelFont.getSize() - 4);
			for (int value = 0; value <= 100; value += 10) {
				double labelValue = (double) value / 100.0;
				JLabel label = new JLabel(formatter.format(labelValue));
				label.setFont(valueFont);
				labels.put(value, label);
			}
			confidenceSlider.setLabelTable(labels);
			confidenceSlider.setPaintLabels(true);
			confidenceSlider.setValue(manager.getDefaultConfidence());

			confidenceSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (ignore)
						return;
					ignore = true;
					int value = confidenceSlider.getValue();
					confidenceValue.setText(formatter.format(((double) value) / 100.0));
					ignore = false;
				}
			});
			c.nextCol().expandHorizontal();// .setInsets(0,5,0,5);
			confidencePanel.add(confidenceSlider, c);
		}

		{
			confidenceValue = new JTextField(4);
			confidenceValue.setHorizontalAlignment(JTextField.RIGHT);
			confidenceValue
					.setText(this.formatter.format(((double) manager.getDefaultConfidence()) / 100.0));
			c.nextCol().noExpand().setAnchor("C");// .setInsets(0,5,0,5);
			confidencePanel.add(confidenceValue, c);

			confidenceValue.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = 6284825408676836773L;

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

	private void textFieldValueChanged() {
		if (ignore)
			return;
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

		val = val * 100.0;
		confidenceSlider.setValue((int) val);
		ignore = false;
	}

	private double inputError() {
		confidenceValue.setBackground(Color.RED);
		JOptionPane.showMessageDialog(this, "Please enter a confidence cutoff between 0.0 and 1.0", "Alert",
				JOptionPane.ERROR_MESSAGE);
		confidenceValue.setBackground(UIManager.getColor("TextField.background"));

		// Reset the value to correspond to the current slider setting
		double val = ((double) confidenceSlider.getValue()) / 100.0;
		confidenceValue.setText(formatter.format(val));
		return val;
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			this.confidenceSlider.setValue(manager.getDefaultConfidence());
			init();
		}

		super.setVisible(b);
	}

	public void init() {
		setPreferredSize(new Dimension(600,500));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new GridBagLayout());
		selectPanel.setBorder(LookAndFeelUtil.createPanelBorder());
		MyGridBagConstraints c = new MyGridBagConstraints();

		c.expandHorizontal();
		selectPanel.add(new JLabel("Protein groups:"), c);
		JScrollPane jsp = new JScrollPane(this.queryInput);
		// jsp.setPreferredSize(new Dimension(400,200));
		c.expandBoth();
		selectPanel.add(jsp, c.nextCol());
		
		c.expandHorizontal();
		selectPanel.add(new JLabel("Protein group delimiter:"), c.nextRow());
		selectPanel.add(this.delimiter, c.nextCol());

		selectPanel.add(new JLabel("Collapse groups:"), c.nextRow());
		selectPanel.add(this.keepCollapsed, c.nextCol());

		selectPanel.add(new JLabel("Network name:"), c.nextRow());
		selectPanel.add(this.netName, c.nextCol());

		selectPanel.add(new JLabel("Species:"), c.nextRow());
		selectPanel.add(this.selectSpecies, c.nextCol());

		selectPanel.add(new JLabel("Network type:"), c.nextRow());
		selectPanel.add(this.createNetTypeRadioButtons(), c.nextCol());

		selectPanel.add(this.createConfidenceSlider(), c.nextRow().useNCols(2)
				.setInsets(MyGridBagConstraints.DEFAULT_INSET, 0, MyGridBagConstraints.DEFAULT_INSET, 0));
		c.useNCols(1).setInsets(MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET,
				MyGridBagConstraints.DEFAULT_INSET, MyGridBagConstraints.DEFAULT_INSET);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(this.closeButton);
		buttonPanel.add(this.retrieveButton);

		mainPanel.add(selectPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		this.setContentPane(mainPanel);

		this.pack();
		// set location relative to the Cytoscape main window
		this.setLocationRelativeTo(manager.getService(CySwingApplication.class).getJFrame());

	}
 	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.retrieveButton) {
			// We check if the species is OK
			Object selectedSpecies = this.selectSpecies.getSelectedItem();
			StringSpecies species = null;
			if (selectedSpecies instanceof StringSpecies) {
				species = (StringSpecies) selectedSpecies;
			} else if (selectedSpecies instanceof String && selectedSpecies.equals("Homo sapiens")) {
				species = StringSpecies.getHumanSpecies();
			} else {
				JOptionPane.showMessageDialog(this, "Error: Unknown species \"" + selectedSpecies + "\".", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			String query = queryInput.getText();
			query = query.replaceAll("(?m)^\\s*", "");
			query = query.replaceAll("(?m)\\s*$", "");

			RetrieveStringNetworkTaskFactory factory = new RetrieveStringNetworkTaskFactory(this.manager);
			try {
				this.manager.executeTask(factory.createTaskIterator(query, (String)delimiter.getSelectedItem(), keepCollapsed.isSelected(), species.getTaxonID(), species.getName(),
						formatter.parse(this.confidenceValue.getText()).doubleValue(), this.getNetworkType().toString(),
						this.netName.getText(), true));
			} catch (ParseException e1) {
				inputError();
				return;
			}

			// The task is executed in background, we don't want the window to be displayed
			this.setVisible(false);
		} else if (e.getSource() == this.closeButton) {
			this.setVisible(false);
		}
	}

}
