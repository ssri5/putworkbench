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

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import in.ac.iitk.cse.putwb.io.DatasetLoader;
import in.ac.iitk.cse.putwb.ui.IconCreator;
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
	public static String LOADED_DATASET_PROPERTY = "UploadFileAction - loaded dataset";
	
	/**
	 * Holds the selected dataset
	 */
	private Instances dataset;
	
	/**
	 * Holds the preference - whether to delete instance with missing values or not
	 */
	private boolean deleteMissingValues = false;
	
	/**
	 * A label to show the full file path of currently selected dataset
	 */
	private JLabel fileLabel;
	
	/**
	 * Holds the preference - whether to ignore duplicate instances or not
	 */
	private boolean ignoreDuplicateInstances = true;
	
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
		dataset = null;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.5, 0.5};
		setLayout(gridBagLayout);
		
		JLabel uploadIconLabel = new JLabel(IconCreator.getIcon(IconCreator.UPLOAD_ICON_FILE));
		uploadIconLabel.setOpaque(false);
		GridBagConstraints gbc_uploadIconLabel = new GridBagConstraints();
		gbc_uploadIconLabel.insets = new Insets(0, 0, 5, 0);
		gbc_uploadIconLabel.gridx = 0;
		gbc_uploadIconLabel.gridy = 0;
		uploadIconLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		add(uploadIconLabel, gbc_uploadIconLabel);
		uploadIconLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				File startingDir = selectedFile == null ? null : selectedFile.getParentFile();
				JFileChooser jf = new JFileChooser(startingDir);
				jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jf.setFileFilter(new FileNameExtensionFilter("ARFF files", "arff"));
				int closeOption = jf.showOpenDialog(null);
				if(closeOption == JFileChooser.APPROVE_OPTION)
					setSelectedFile(jf.getSelectedFile());
			}
		});
		
		JLabel displayTextLabel = new JLabel("<html><center><font size='5' color='#033e9e'>Select the sample data file</font><br/><font size='3' color='#033e9e'>Data must be in Weka's Attribute-Relation File Format (arff)</font></center></html>");
		GridBagConstraints gbc_displayTextLabel = new GridBagConstraints();
		gbc_displayTextLabel.insets = new Insets(0, 0, 5, 0);
		gbc_displayTextLabel.anchor = GridBagConstraints.NORTH;
		gbc_displayTextLabel.gridx = 0;
		gbc_displayTextLabel.gridy = 1;
		add(displayTextLabel, gbc_displayTextLabel);
		
		fileLabel = new JLabel("");
		GridBagConstraints gbc_fileLabel = new GridBagConstraints();
		gbc_fileLabel.insets = new Insets(0, 0, 10, 0);
		gbc_fileLabel.gridx = 0;
		gbc_fileLabel.gridy = 2;
		add(fileLabel, gbc_fileLabel);
		
		sanitizationOptionsPanel = new JPanel();
		sanitizationOptionsPanel.setOpaque(false);
		GridBagConstraints gbc_sanitizationOptionsPanel = new GridBagConstraints();
		gbc_sanitizationOptionsPanel.anchor = GridBagConstraints.NORTH;
		gbc_sanitizationOptionsPanel.gridx = 0;
		gbc_sanitizationOptionsPanel.gridy = 3;
		add(sanitizationOptionsPanel, gbc_sanitizationOptionsPanel);
		GridBagLayout gbl_sanitizationOptionsPanel = new GridBagLayout();
		gbl_sanitizationOptionsPanel.columnWeights = new double[]{0.0};
		gbl_sanitizationOptionsPanel.rowWeights = new double[]{0.0, 0.0};
		sanitizationOptionsPanel.setLayout(gbl_sanitizationOptionsPanel);
		
		JCheckBox missingValuePreference = new JCheckBox("Delete instances with missing values, instead of replacing with Mean or Mode");
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
		
		JCheckBox duplicateInstancesPreference = new JCheckBox("Delete duplicate instances");
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
		sanitizationOptionsPanel.setVisible(false);
	}

	/**
	 * Attempts to load the selected dataset, with set preferences
	 */
	private void attemptDatasetLoad() {
		try {
			setDataset(DatasetLoader.loadAndCleanDataset(selectedFile.getAbsolutePath(), deleteMissingValues, ignoreDuplicateInstances));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Problems in loading dataset", "Loading failed", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
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
			fileLabel.setText("<html><center><b><font size='4' color='#2d0c08'>Source Loaded</font></b><br/><font size='3' color='#9e1503'>" + selectedFile.getAbsolutePath() + "</font></center></html>");
	}
	
	/**
	 * Attempts to set a new dataset file; if successful, proceeds to set the new value of the dataset
	 * @param selectedFile The selected dataset file
	 */
	private void setSelectedFile(File selectedFile) {
		if(selectedFile != null && selectedFile.exists()) {
			this.selectedFile = selectedFile;
			fileLabel.setText("<html><center><b><font size='4' color='#2d0c08'>Selected source</font></b><br/><font size='3' color='#9e1503'>" + selectedFile.getAbsolutePath() + "</font></center></html>");
			sanitizationOptionsPanel.setVisible(true);
			attemptDatasetLoad();
		}
		else if(selectedFile == null)
			fileLabel.setText("<html><center><b><font size='4' color='#2d0c08'>Select a Data source</font></b></center></html>");
		else
			fileLabel.setText("<html><center><b><font size='4' color='#2d0c08'>Source file not found</font></b><br/><font size='3' color='#9e1503'>" + selectedFile.getAbsolutePath() + "</font></center></html>");
	}
	
}
