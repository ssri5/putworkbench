package in.ac.iitk.cse.putwb.ui.actions;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import in.ac.iitk.cse.putwb.experiment.PUTExperiment;
import in.ac.iitk.cse.putwb.io.DatasetLoader;
import in.ac.iitk.cse.putwb.ui.ArchiveManager;
import in.ac.iitk.cse.putwb.ui.IconCreator;
import in.ac.iitk.cse.putwb.ui.widgets.AutoPilotPreferencesDialog;
import in.ac.iitk.cse.putwb.ui.widgets.AutopilotButton;
import weka.core.Instances;

/**
 * The dataset selection action allows user to select or reset a dataset to use for the experiment
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class UploadFileAction extends Action {

	/**
	 * The constant for "dataset loaded/reset" property
	 */
	public static final String LOADED_DATASET_PROPERTY = "UploadFileAction - loaded dataset";

	/**
	 * The constant for "experiment loaded/rest" property
	 */
	public static final String LOADED_EXPERIMENT_PROPERTY = "UploadFileAction - loaded experiment";

	/**
	 * The constant for "dataset/experiment loading" property
	 */
	public static final String LOADING_DATASET_OR_EXPERIMENT_PROPERTY = "UploadFileAction - loading dataset/experiment";

	/**
	 * The constant for "starting autopilot" property
	 */
	public static final String START_AUTOPILOT_PROPERTY = "UploadFileAction - start autopilot";

	/**
	 * The button to invoke the autopilot
	 */
	private AutopilotButton autoPilotButton;

	/**
	 * Holds the selected dataset
	 */
	private Instances dataset;

	/**
	 * Holds the preference - whether to delete instance with missing values or not
	 */
	private boolean deleteMissingValues = false;

	/**
	 * The widget to take duplicate rows preferences
	 */
	private JCheckBox duplicateInstancesPreference;

	/**
	 * A label to show the full file path of currently selected dataset
	 */
	private JLabel fileLabel;

	/**
	 * Holds the preference - whether to ignore duplicate instances or not
	 */
	private boolean ignoreDuplicateInstances = true;

	/**
	 * The widget to take missing value preferences
	 */
	private JCheckBox missingValuePreference;

	/**
	 * Points to the currently selected preferences for the last experiment that successfully completed stored in a file, if any
	 */
	private File preferencesFile = null;

	/**
	 * Points to the loaded results file, if there is any
	 */
	private File resultsFile;

	/**
	 * The panel that contain preferences related to data sanitization
	 */
	private JPanel sanitizationOptionsPanel;

	/**
	 * Points to the selected dataset file
	 */
	private File selectedFile;

	/**
	 * Creates an instance of dataset selection action
	 */
	public UploadFileAction() {
		super();
		selectedFile = null;
		resultsFile = null;
		dataset = null;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.5, 0.5, 0};
		setLayout(gridBagLayout);

		JLabel uploadDatasetIconLabel = new JLabel(IconCreator.getIcon(IconCreator.UPLOAD_DATASET_ICON_FILE));
		uploadDatasetIconLabel.setOpaque(false);
		GridBagConstraints gbc_uploadDatasetIconLabel = new GridBagConstraints();
		gbc_uploadDatasetIconLabel.insets = new Insets(0, 0, 5, 0);
		gbc_uploadDatasetIconLabel.gridx = 0;
		gbc_uploadDatasetIconLabel.gridy = 0;
		uploadDatasetIconLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		add(uploadDatasetIconLabel, gbc_uploadDatasetIconLabel);
		uploadDatasetIconLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				File startingDir = selectedFile == null ? null : selectedFile.getParentFile();
				JFileChooser jf = new JFileChooser(startingDir);
				jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jf.setFileFilter(new FileNameExtensionFilter("ARFF files", "arff"));
				int closeOption = jf.showOpenDialog(null);
				if(closeOption == JFileChooser.APPROVE_OPTION) {
					setSelectedDatasetFile(jf.getSelectedFile());
					// Reset preferences
					missingValuePreference.setSelected(false);
					duplicateInstancesPreference.setSelected(true);
				}
			}
		});
		uploadDatasetIconLabel.setToolTipText("Upload a new ARFF dataset to start an experiment");

		JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets(5, 0, 5, 0);
		gbc_separator.gridx = 1;
		gbc_separator.gridy = 0;
		gbc_separator.gridheight = 2;
		gbc_separator.fill = GridBagConstraints.VERTICAL;
		add(separator, gbc_separator);

		JLabel uploadExperimentIconLabel = new JLabel(IconCreator.getIcon(IconCreator.UPLOAD_EXPERIMENT_ICON_FILE));
		uploadExperimentIconLabel.setOpaque(false);
		GridBagConstraints gbc_uploadExperimentIconLabel = new GridBagConstraints();
		gbc_uploadExperimentIconLabel.insets = new Insets(0, 0, 5, 0);
		gbc_uploadExperimentIconLabel.gridx = 2;
		gbc_uploadExperimentIconLabel.gridy = 0;
		uploadExperimentIconLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		add(uploadExperimentIconLabel, gbc_uploadExperimentIconLabel);
		uploadExperimentIconLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				File startingDir = selectedFile == null ? null : selectedFile.getParentFile();
				JFileChooser jf = new JFileChooser(startingDir);
				jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jf.setFileFilter(new FileNameExtensionFilter("PUT files", "put"));
				int closeOption = jf.showOpenDialog(null);
				if(closeOption == JFileChooser.APPROVE_OPTION)
					setSelectedExperimentFile(jf.getSelectedFile());
			}
		});
		uploadExperimentIconLabel.setToolTipText("Select a saved experiment file and load it to analyze");

		JLabel displayDatasetTextLabel = new JLabel("<html><center><font size='5' color='#033e9e'>Select a data file for experiment</font></center></html>");
		GridBagConstraints gbc_displayDatasetTextLabel = new GridBagConstraints();
		gbc_displayDatasetTextLabel.insets = new Insets(0, 0, 5, 0);
		gbc_displayDatasetTextLabel.anchor = GridBagConstraints.NORTH;
		gbc_displayDatasetTextLabel.gridx = 0;
		gbc_displayDatasetTextLabel.gridy = 1;
		add(displayDatasetTextLabel, gbc_displayDatasetTextLabel);

		JLabel displayExperimentTextLabel = new JLabel("<html><center><font size='5' color='#033e9e'>Load an existing experiment</font></center></html>");
		GridBagConstraints gbc_displayExperimentTextLabel = new GridBagConstraints();
		gbc_displayExperimentTextLabel.insets = new Insets(0, 0, 5, 0);
		gbc_displayExperimentTextLabel.anchor = GridBagConstraints.NORTH;
		gbc_displayExperimentTextLabel.gridx = 2;
		gbc_displayExperimentTextLabel.gridy = 1;
		add(displayExperimentTextLabel, gbc_displayExperimentTextLabel);

		fileLabel = new JLabel("");
		GridBagConstraints gbc_fileLabel = new GridBagConstraints();
		gbc_fileLabel.insets = new Insets(0, 0, 10, 0);
		gbc_fileLabel.gridx = 0;
		gbc_fileLabel.gridy = 2;
		gbc_fileLabel.gridwidth = 3;
		add(fileLabel, gbc_fileLabel);

		sanitizationOptionsPanel = new JPanel();
		sanitizationOptionsPanel.setOpaque(false);
		GridBagConstraints gbc_sanitizationOptionsPanel = new GridBagConstraints();
		gbc_sanitizationOptionsPanel.anchor = GridBagConstraints.NORTH;
		gbc_sanitizationOptionsPanel.gridx = 0;
		gbc_sanitizationOptionsPanel.gridy = 3;
		gbc_sanitizationOptionsPanel.gridwidth = 3;
		add(sanitizationOptionsPanel, gbc_sanitizationOptionsPanel);
		GridBagLayout gbl_sanitizationOptionsPanel = new GridBagLayout();
		gbl_sanitizationOptionsPanel.columnWeights = new double[]{0.0};
		gbl_sanitizationOptionsPanel.rowWeights = new double[]{0.0, 0.0};
		sanitizationOptionsPanel.setLayout(gbl_sanitizationOptionsPanel);

		missingValuePreference = new JCheckBox("Delete instances with missing values, instead of replacing with Mean or Mode");
		missingValuePreference.setOpaque(false);
		GridBagConstraints gbc_missingValuePreference = new GridBagConstraints();
		gbc_missingValuePreference.anchor = GridBagConstraints.WEST;
		gbc_missingValuePreference.insets = new Insets(0, 0, 5, 0);
		gbc_missingValuePreference.gridx = 0;
		gbc_missingValuePreference.gridy = 0;
		sanitizationOptionsPanel.add(missingValuePreference, gbc_missingValuePreference);
		missingValuePreference.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteMissingValues = missingValuePreference.isSelected();
				attemptDatasetLoad();
			}
		});

		duplicateInstancesPreference = new JCheckBox("Delete duplicate instances");
		duplicateInstancesPreference.setSelected(true);
		duplicateInstancesPreference.setOpaque(false);
		GridBagConstraints gbc_duplicateInstancesPreference = new GridBagConstraints();
		gbc_duplicateInstancesPreference.anchor = GridBagConstraints.WEST;
		gbc_duplicateInstancesPreference.gridx = 0;
		gbc_duplicateInstancesPreference.gridy = 1;
		sanitizationOptionsPanel.add(duplicateInstancesPreference, gbc_duplicateInstancesPreference);
		duplicateInstancesPreference.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ignoreDuplicateInstances = duplicateInstancesPreference.isSelected();
				attemptDatasetLoad();
			}
		});

		JLabel spacer1 = new JLabel();
		spacer1.setOpaque(false);
		GridBagConstraints gbc_spacer1 = new GridBagConstraints();
		gbc_spacer1.gridx = 4;
		gbc_spacer1.gridy = 0;
		gbc_spacer1.fill = GridBagConstraints.BOTH;
		add(spacer1, gbc_spacer1);

		autoPilotButton = new AutopilotButton();
		GridBagConstraints gbc_autoPilotButton = new GridBagConstraints();
		gbc_autoPilotButton.insets = new Insets(10, 0, 10, 0);
		gbc_autoPilotButton.gridx = 4;
		gbc_autoPilotButton.gridx = 1;
		gbc_autoPilotButton.fill = GridBagConstraints.BOTH;
		gbc_autoPilotButton.anchor = GridBagConstraints.NORTH;
		add(autoPilotButton, gbc_autoPilotButton);
		autoPilotButton.addMouseListener(new MouseAdapter() {

			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				AutoPilotPreferencesDialog dialog = new AutoPilotPreferencesDialog(UploadFileAction.this);
				dialog.setVisible(true);
				if(dialog.startAutopilot()) {
					pcs.firePropertyChange(START_AUTOPILOT_PROPERTY, null, dialog.getPreferences());
				}
			}
		});

		JLabel spacer2 = new JLabel();
		spacer2.setOpaque(false);
		GridBagConstraints gbc_spacer2 = new GridBagConstraints();
		gbc_spacer2.gridx = 4;
		gbc_spacer2.gridy = 2;
		gbc_spacer2.fill = GridBagConstraints.BOTH;
		add(spacer2, gbc_spacer2);

		sanitizationOptionsPanel.setVisible(false);
		autoPilotButton.setVisible(false);
	}

	/**
	 * Attempts to load the selected dataset, with set preferences
	 */
	private void attemptDatasetLoad() {
		pcs.firePropertyChange(LOADING_DATASET_OR_EXPERIMENT_PROPERTY, null, null);

		new Thread() {
			public void run() {
				try {
					setDataset(DatasetLoader.loadAndCleanDataset(selectedFile.getAbsolutePath(), deleteMissingValues, ignoreDuplicateInstances));
				} catch (Exception e) {

				}
			}
		}.start();

	}

	/**
	 * Returns the preferences file for the last experiment that successfully completed
	 * @return The preferences file
	 */
	public File getPreferencesFile() {
		return preferencesFile;
	}

	/**
	 * Returns the loaded experiment's result file, if any
	 * @return a {@link File} object pointing to the result file of the loaded experiment, or <code>null</code> otherwise 
	 */
	public File getResultsFile() {
		return resultsFile;
	}

	/**
	 * Returns the currently selected dataset file, or <code>null</code> if no dataset is seleceted currently
	 * @return a {@link File} object pointing to any selected dataset file, or <code>null</code> otherwise 
	 */
	public File getSelectedFile() {
		return selectedFile;
	}

	/**
	 * Returns the missing value handling preference
	 * @return <code>true</code> if the instances with missing values are to be deleted, <code>false</code> if the missing values are to be replaced with mean or mode
	 */
	public boolean isDeleteMissingValues() {
		return deleteMissingValues;
	}

	/**
	 * Returns the duplicate instances handling preference
	 * @return <code>true</code> if the duplicate instances are to be ignored, <code>false</code> otherwise
	 */
	public boolean isIgnoreDuplicateInstances() {
		return ignoreDuplicateInstances;
	}

	/**
	 * Attempts to set a new dataset, and acknowledge any listeners of the event
	 * @param dataset The dataset to set
	 */
	private void setDataset(Instances dataset) {
		pcs.firePropertyChange(LOADED_DATASET_PROPERTY, this.dataset, dataset);
		this.dataset = dataset;
		if(dataset == null)
			JOptionPane.showMessageDialog(null, "Problems in loading dataset", "Loading failed", JOptionPane.ERROR_MESSAGE);
		if(selectedFile != null)
			fileLabel.setText("<html><center><b><font size='4' color='#2d0c08'>Source Loaded</font></b><br/><font size='3' color='#9e1503'>" + selectedFile.getName() + "</font></center></html>");
	}

	@Override
	public void setInitialPreferences(Map<String, String> preferences) {
		// Do nothing
	}

	/**
	 * Attempts to set a new dataset file; if successful, proceeds to set the new value of the dataset
	 * @param selectedDatasetFile The selected dataset file
	 */
	private void setSelectedDatasetFile(File selectedDatasetFile) {
		if(selectedDatasetFile != null && selectedDatasetFile.exists()) {
			this.selectedFile = selectedDatasetFile;
			fileLabel.setText("<html><center><b><font size='4' color='#2d0c08'>Selected source</font></b><br/><font size='3' color='#9e1503'>" + selectedDatasetFile.getAbsolutePath() + "</font></center></html>");
			sanitizationOptionsPanel.setVisible(true);
			autoPilotButton.setVisible(true);
			attemptDatasetLoad();
		}
		else if(selectedDatasetFile == null)
			fileLabel.setText("<html><center><b><font size='4' color='#2d0c08'>Select a Data source</font></b></center></html>");
		else
			fileLabel.setText("<html><center><b><font size='4' color='#2d0c08'>Source file not found</font></b><br/><font size='3' color='#9e1503'>" + selectedDatasetFile.getAbsolutePath() + "</font></center></html>");
	}

	/**
	 * Attempts to load an existing experiment; if successful, proceeds to set the new value of the dataset and sets initiates setting other preferences
	 * @param selectedExperimentFile The experiment file to load
	 */
	private void setSelectedExperimentFile(File selectedExperimentFile) {
		if(selectedExperimentFile != null && selectedExperimentFile.exists()) {
			if(selectedExperimentFile.getName().toLowerCase().endsWith(".put")) {
				File tempDir = null;
				try {
					tempDir = Files.createTempDirectory("put" + System.nanoTime()).toFile();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Problem in creating temporary directory. Please check if you have sufficient disk space.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				if(ArchiveManager.extractCompressedFileToDirectory(tempDir, selectedExperimentFile)) {
					File[] list = tempDir.listFiles();
					File dataFile = null, csvFile = null, prefFile = null;
					for(File file : list) {
						String fileName = file.getName().toLowerCase();
						if(fileName.endsWith(".arff"))
							dataFile = file;
						else if(fileName.equals("results.csv"))
							csvFile = file;
						else if(fileName.equals("prefs.txt"))
							prefFile = file;
					}
					if(dataFile == null || csvFile == null || prefFile == null) {
						JOptionPane.showMessageDialog(null, "Illegal experiment file", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					setSelectedDatasetFile(dataFile);
					Map<String, String> map = new HashMap<String, String>();
					try {
						Scanner sc = new Scanner(prefFile);
						String line = null;
						while(sc.hasNextLine()) {
							line = sc.nextLine();
							String[] tokens = line.trim().split(" ", 2);
							if(tokens.length == 2)
								map.put(tokens[0], tokens[1]);
						}
						sc.close();
						this.resultsFile = csvFile;
						this.preferencesFile = prefFile;
						String missingValPref = map.get(PUTExperiment.MISSING_VALUE_SWITCH);
						if(missingValPref != null) {
							deleteMissingValues = missingValPref.trim().equals("D") ? true : false;
							missingValuePreference.setSelected(deleteMissingValues);
						}
						String duplicateValPref = map.get(PUTExperiment.DUPLICATE_ROWS_SWITCH);
						if(duplicateValPref != null) {
							ignoreDuplicateInstances = duplicateValPref.trim().equals("Y") ? true : false;
							duplicateInstancesPreference.setSelected(ignoreDuplicateInstances);
						}
						attemptDatasetLoad();
						pcs.firePropertyChange(LOADED_EXPERIMENT_PROPERTY, null, map);
					} catch (FileNotFoundException e) {
						JOptionPane.showMessageDialog(null, "Problem in reading experiment data. Unable to load experiment.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else
				JOptionPane.showMessageDialog(null, "Please provide a valid experiment file", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
