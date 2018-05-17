package in.ac.iitk.cse.putwb.ui.actions;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import in.ac.iitk.cse.putwb.classify.DataClassifier;
import in.ac.iitk.cse.putwb.experiment.PUTExperiment;
import in.ac.iitk.cse.putwb.ui.IconCreator;
import in.ac.iitk.cse.putwb.ui.widgets.RoundedLineBorder;
import weka.classifiers.AbstractClassifier;

/**
 * The Classifier Selection action allows user to choose a particular (weka) classifier for classification, and provide any custom options, if required.
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class SelectClassifierAction extends Action implements ActionListener {

	/**
	 * The constant for the default classifier to use
	 */
	private static final String DEFAULT_CLASSIFIER = "J48";

	/**
	 * Shows a description of the selected classifier
	 */
	private JEditorPane classifierDescription;

	/**
	 * The selection widget for selecting a classifier 
	 */
	private JComboBox<String> classifierDropdown;

	/**
	 * Holds the currently selected classifier
	 */
	private String currentClassifier;

	/**
	 * The textbox for taking any custom classifier options 
	 */
	private JTextField customOptionsTextbox;

	/**
	 * Creates a new Classifier Selection action
	 */
	public SelectClassifierAction() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0};

		setLayout(gridBagLayout);

		JLabel classifierLabelIcon = new JLabel(IconCreator.getIcon(IconCreator.CLASSIFICATION_ICON_FILE));
		classifierLabelIcon.setOpaque(false);
		GridBagConstraints gbc_classifierLabelIcon = new GridBagConstraints();
		gbc_classifierLabelIcon.insets = new Insets(2, 0, 5, 0);
		gbc_classifierLabelIcon.fill = GridBagConstraints.HORIZONTAL;
		gbc_classifierLabelIcon.gridx = 0;
		gbc_classifierLabelIcon.gridy = 0;
		add(classifierLabelIcon, gbc_classifierLabelIcon);

		JPanel classifierPanel = new JPanel();
		classifierPanel.setOpaque(false);
		GridBagConstraints gbc_classifierPanel = new GridBagConstraints();
		gbc_classifierPanel.insets = new Insets(5, 5, 10, 5);
		gbc_classifierPanel.fill = GridBagConstraints.BOTH;
		gbc_classifierPanel.gridx = 0;
		gbc_classifierPanel.gridy = 1;
		add(classifierPanel, gbc_classifierPanel);
		GridBagLayout gbl_classifierPanel = new GridBagLayout();
		gbl_classifierPanel.columnWeights = new double[]{0.0, 1.0};
		gbl_classifierPanel.rowWeights = new double[]{0.0, 1.0, 0.0};
		classifierPanel.setLayout(gbl_classifierPanel);

		JLabel infoLabel1 = new JLabel("<html><center><font size='5' color='#033e9e'>Select the classification mechanism to use</font></center></html>");
		infoLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel1 = new GridBagConstraints();
		gbc_infoLabel1.gridwidth = 2;
		gbc_infoLabel1.insets = new Insets(5, 0, 5, 0);
		gbc_infoLabel1.anchor = GridBagConstraints.NORTH;
		gbc_infoLabel1.fill = GridBagConstraints.HORIZONTAL;
		gbc_infoLabel1.gridx = 0;
		gbc_infoLabel1.gridy = 0;
		classifierPanel.add(infoLabel1, gbc_infoLabel1);

		List<String> allClassifiers = DataClassifier.getClassifierOptions();
		classifierDropdown = new JComboBox<String>(allClassifiers.toArray(new String[allClassifiers.size()]));
		classifierDropdown.setSelectedItem(DEFAULT_CLASSIFIER);
		currentClassifier = DEFAULT_CLASSIFIER;
		GridBagConstraints gbc_classifierDropdown = new GridBagConstraints();
		gbc_classifierDropdown.anchor = GridBagConstraints.NORTHWEST;
		gbc_classifierDropdown.insets = new Insets(8, 20, 0, 0);
		gbc_classifierDropdown.gridx = 0;
		gbc_classifierDropdown.gridy = 1;
		classifierPanel.add(classifierDropdown, gbc_classifierDropdown);

		classifierDescription = new JEditorPane();
		classifierDescription.setEditable(false);
		classifierDescription.setContentType("text/html");
		classifierDropdown.addActionListener(this);
		JScrollPane editorScrollPane = new JScrollPane(classifierDescription);
		editorScrollPane.setOpaque(false);
		TitledBorder border = BorderFactory.createTitledBorder(new RoundedLineBorder(Color.GRAY, 2), "Weka Documentation");
		border.setTitleJustification(TitledBorder.CENTER);
		editorScrollPane.setBorder(border);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(250, 145));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));
		GridBagConstraints gbc_editorScrollPane = new GridBagConstraints();
		gbc_editorScrollPane.insets = new Insets(0, 5, 10, 0);
		gbc_editorScrollPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_editorScrollPane.fill = GridBagConstraints.BOTH;
		gbc_editorScrollPane.gridx = 1;
		gbc_editorScrollPane.gridy = 1;
		classifierPanel.add(editorScrollPane, gbc_editorScrollPane);

		JLabel infoLabel2 = new JLabel("<html><center><font size='4' color='#033e9e'>Custom Options (see weka docs)</font></center></html>");
		GridBagConstraints gbc_infoLabel2 = new GridBagConstraints();
		gbc_infoLabel2.anchor = GridBagConstraints.NORTHWEST;
		gbc_infoLabel2.insets = new Insets(0, 20, 5, 0);
		gbc_infoLabel2.gridx = 0;
		gbc_infoLabel2.gridy = 2;
		classifierPanel.add(infoLabel2, gbc_infoLabel2);

		customOptionsTextbox = new JTextField();
		GridBagConstraints gbc_cutomOptionsTextbox = new GridBagConstraints();
		gbc_cutomOptionsTextbox.insets = new Insets(0, 5, 5, 0);
		gbc_cutomOptionsTextbox.fill = GridBagConstraints.HORIZONTAL;
		gbc_cutomOptionsTextbox.anchor = GridBagConstraints.NORTHWEST;
		gbc_cutomOptionsTextbox.gridx = 1;
		gbc_cutomOptionsTextbox.gridy = 2;
		classifierPanel.add(customOptionsTextbox, gbc_cutomOptionsTextbox);

		setDocumentationText();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setDocumentationText();
	}

	/**
	 * Returns the selected classifier name
	 * @return The classifier name
	 */
	public String getClassifier() {
		return classifierDropdown.getSelectedItem().toString();
	}

	/**
	 * Returns the Classifier combobox. Used by the autopilot.
	 * @return the classifierDropdown
	 */
	public JComboBox<String> getClassifierDropdown() {
		return classifierDropdown;
	}

	/**
	 * Returns any custom classifier options, if provided
	 * @return The classifier options (or an empty string), enclosed between parentheses 
	 */
	public String getCustomOptions() {
		return "{" + customOptionsTextbox.getText() + "}";
	}
	
	/**
	 * Fetches and loads the weka documentation for selected classifier
	 */
	private void setDocumentationText() {
		try {
			String classifier = classifierDropdown.getSelectedItem().toString();
			if(classifier != currentClassifier) {
				Class<? extends AbstractClassifier> classifierClass = DataClassifier.findClassifierByName(classifier);
				String fullName = classifierClass.getCanonicalName();
				fullName = "/doc.dev/" + fullName.replaceAll("\\.", "/").concat(".html");
				classifierDescription.setText("<html><font size='4' color='#2d0c08'>Attempting to load documentation</font></html>");
				URL docURL = new URL("http", "weka.sourceforge.net", fullName);

				Thread loadThread = new Thread() {
					public void run() {
						try {
							Scanner s = new Scanner(docURL.openStream());
							StringBuffer sb = new StringBuffer();
							boolean start = false;
							while(s.hasNextLine()) {
								String line = s.nextLine();
								if(line.contains("<!-- globalinfo-start -->"))
									start = true;
								if(start)
									sb.append(line);
								if(line.contains("<!-- options-end -->"))
									break;
							}
							classifierDescription.setText("<html>" + sb.toString() + "</html>");
							s.close();
						} catch (IOException e) {
							classifierDescription.setText("<html><font size='4' color='#2d0c08'>Loading failed</font></html>");
						}
					}
				};
				loadThread.start();
			}
			currentClassifier = classifier;
		} catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setInitialPreferences(Map<String, String> preferences) {
		if(preferences != null) {
			String classifier = preferences.get(PUTExperiment.CLASSIFIER_SWITCH);
			if(classifier != null) {
				classifierDropdown.setSelectedItem(classifier);
				setDocumentationText();
			}
			String classifierOptions = preferences.get(PUTExperiment.CLASSIFIER_OPTIONS_SWITCH);
			if(classifierOptions != null)
				customOptionsTextbox.setText(classifierOptions.substring(1, classifierOptions.length()-1));
		}
	}
}
