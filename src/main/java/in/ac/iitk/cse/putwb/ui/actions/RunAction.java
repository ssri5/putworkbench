package in.ac.iitk.cse.putwb.ui.actions;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import in.ac.iitk.cse.putwb.experiment.PUTExperiment;
import in.ac.iitk.cse.putwb.ui.PUTWb;
import in.ac.iitk.cse.putwb.ui.widgets.HyperlinkButton;
import in.ac.iitk.cse.putwb.ui.widgets.RunningStateDialog;

/**
 * The Execution Action executes starts a <code>PUTExperiment</code> based on the user preferences.
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class RunAction extends Action implements ActionListener { 
	
	/**
	 * The constant for "Tab Changed Request" property
	 */
	public static final String TAB_CHANGE_REQUEST_PROPERTY = "RunAction - tab change request";

	/**
	 * The constant for "All Tasks Completed" property
	 */
	public static final String TASKS_COMPLETED_PROPERTY = "RunAction - tasks completed";
	
	/**
	 * The constant for "Tasks Running" property
	 */
	public static final String TASKS_RUNNING_PROPERTY = "RunAction - error occured";
	
	/**
	 * The {@link SelectClassifierAction} object associated with current experiment
	 */
	private SelectClassifierAction classifierInfo;
	
	/**
	 * The label to show the name of the currently selected classifier
	 */
	private JLabel classifierLabel;
	
	/**
	 * The label to show the currently supplied classifier options
	 */
	private JLabel classifierOptionsLabel;
	
	/**
	 * The {@link UploadFileAction} object associated with current experiment
	 */
	private UploadFileAction datasetInfo;
	
	/**
	 * The label to show the full path of the currently selected dataset
	 */
	private JLabel datasetLabel;
	
	/**
	 * The {@link SetExpenseAction} object associated with current experiment
	 */
	private SetExpenseAction expenseInfo;
	
	/**
	 * The label to show the currently selected value of horizontal expense
	 */
	private JLabel horizontalExpenseLabel;
	
	/**
	 * The text field to supply the value of <i>k</i> to use while performing k-cross validation of models
	 */
	private JTextField kTextField;
	
	/**
	 * The file containing the preferences for the experiment just after completion
	 */
	private File preferencesFile = null;
	
	/**
	 * The label to show the currently added privacy exceptions
	 */
	private JLabel privacyExceptionsLabel;
	
	/**
	 * The {@link SelectPrivacySettingsAction} object associated with current experiment
	 */
	private SelectPrivacySettingsAction privacyInfo;
	
	/**
	 * The label to show the currently selected value of PUT Number
	 */
	private JLabel putLabel;
	
	/**
	 * A checkbox to take preference for attribute generation method
	 */
	private JCheckBox randomGenerationAdvise;
	
	/**
	 * The file to which the results are stored when the experiment completes
	 */
	private File resultFile = null;

	/**
	 * The button to start the tasks	
	 */
	private JButton startTasksButton;
	
	/**
	 * The label to show the currently added utility exceptions
	 */
	private JLabel utilityExceptionsLabel;
	
	/**
	 * The label to show the currently selected value of vertical expense
	 */
	private JLabel verticalExpenseLabel;
	
	/**
	 * Creates a new instance of Execution action, with links to the other required actions
	 * @param datasetInfo The action for dataset selection
	 * @param privacyInfo The action for privacy settings
	 * @param expenseInfo The action for expense settings
	 * @param classifierInfo The action for classifier selection
	 */
	public RunAction(UploadFileAction datasetInfo, SelectPrivacySettingsAction privacyInfo, 
			SetExpenseAction expenseInfo, SelectClassifierAction classifierInfo) {
		super();
		this.datasetInfo = datasetInfo;
		this.privacyInfo = privacyInfo;
		this.expenseInfo = expenseInfo;
		this.classifierInfo = classifierInfo;
				
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.8, 0.2, 0.0};
		setLayout(gridBagLayout);
		
		JLabel summaryLabel = new JLabel("<html><center><font size='5' color='#033e9e'>Summary</font></center></html>");
		summaryLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_summaryLabel = new GridBagConstraints();
		gbc_summaryLabel.insets = new Insets(5, 0, 5, 0);
		gbc_summaryLabel.fill = GridBagConstraints.BOTH;
		gbc_summaryLabel.gridx = 0;
		gbc_summaryLabel.gridy = 0;
		add(summaryLabel, gbc_summaryLabel);
		
		JPanel summaryPanel = new JPanel();
		summaryPanel.setOpaque(false);
		GridBagConstraints gbc_summaryPanel = new GridBagConstraints();
		gbc_summaryPanel.insets = new Insets(5, 5, 5, 0);
		gbc_summaryPanel.fill = GridBagConstraints.VERTICAL;
		gbc_summaryPanel.gridx = 0;
		gbc_summaryPanel.gridy = 1;
		add(summaryPanel, gbc_summaryPanel);
		GridBagLayout gbl_summaryPanel = new GridBagLayout();
		gbl_summaryPanel.columnWeights = new double[]{1.0, 0.0};
		gbl_summaryPanel.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0};
		summaryPanel.setLayout(gbl_summaryPanel);
		
		JPanel datasetSection = new JPanel();
		datasetSection.setOpaque(false);
		datasetSection.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		GridBagConstraints gbc_datasetSection = new GridBagConstraints();
		gbc_datasetSection.insets = new Insets(5, 5, 5, 5);
		gbc_datasetSection.fill = GridBagConstraints.BOTH;
		gbc_datasetSection.gridx = 0;
		gbc_datasetSection.gridy = 0;
		summaryPanel.add(datasetSection, gbc_datasetSection);
		GridBagLayout gbl_datasetSection = new GridBagLayout();
		gbl_datasetSection.columnWeights = new double[]{0.0, 1.0};
		gbl_datasetSection.rowWeights = new double[]{0.0};
		datasetSection.setLayout(gbl_datasetSection);
		
		JLabel infoLabel1 = new JLabel("<html><center><b><font size='4' color='#2d0c08'>Dataset File</font><br/></center></html>");
		GridBagConstraints gbc_infoLabel1 = new GridBagConstraints();
		gbc_infoLabel1.anchor = GridBagConstraints.WEST;
		gbc_infoLabel1.fill = GridBagConstraints.BOTH;
		gbc_infoLabel1.insets = new Insets(5, 50, 0, 130);
		gbc_infoLabel1.gridx = 0;
		gbc_infoLabel1.gridy = 0;
		datasetSection.add(infoLabel1, gbc_infoLabel1);
		
		datasetLabel = new JLabel();
		GridBagConstraints gbc_datasetLabel = new GridBagConstraints();
		gbc_datasetLabel.fill = GridBagConstraints.BOTH;
		gbc_datasetLabel.insets = new Insets(5, 5, 0, 50);
		gbc_datasetLabel.anchor = GridBagConstraints.WEST;
		gbc_datasetLabel.gridx = 1;
		gbc_datasetLabel.gridy = 0;
		datasetSection.add(datasetLabel, gbc_datasetLabel);
		
		HyperlinkButton modifyDatabaseSectionLink = new HyperlinkButton("Change");
		GridBagConstraints gbc_modifyDatabaseSectionLink = new GridBagConstraints();
		gbc_modifyDatabaseSectionLink.insets = new Insets(5, 10, 5, 10);
		gbc_modifyDatabaseSectionLink.anchor = GridBagConstraints.WEST;
		gbc_modifyDatabaseSectionLink.fill = GridBagConstraints.HORIZONTAL;
		gbc_modifyDatabaseSectionLink.gridx = 1;
		gbc_modifyDatabaseSectionLink.gridy = 0;
		summaryPanel.add(modifyDatabaseSectionLink, gbc_modifyDatabaseSectionLink);
		modifyDatabaseSectionLink.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				pcs.firePropertyChange(TAB_CHANGE_REQUEST_PROPERTY, null, PUTWb.LOAD_TAB);
			}
		});
		
		JPanel privacySection = new JPanel();
		privacySection.setOpaque(false);
		privacySection.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		GridBagConstraints gbc_privacySection = new GridBagConstraints();
		gbc_privacySection.insets = new Insets(0, 5, 5, 5);
		gbc_privacySection.fill = GridBagConstraints.BOTH;
		gbc_privacySection.gridx = 0;
		gbc_privacySection.gridy = 1;
		summaryPanel.add(privacySection, gbc_privacySection);
		GridBagLayout gbl_privacySection = new GridBagLayout();
		gbl_privacySection.columnWeights = new double[]{0.0, 1.0};
		gbl_privacySection.rowWeights = new double[]{1.0, 1.0, 1.0};
		privacySection.setLayout(gbl_privacySection);
		
		JLabel infoLabel2 = new JLabel("<html><center><b><font size='4' color='#2d0c08'>PUT Number</font><br/></center></html>");
		GridBagConstraints gbc_infoLabel2 = new GridBagConstraints();
		gbc_infoLabel2.insets = new Insets(5, 50, 5, 5);
		gbc_infoLabel2.fill = GridBagConstraints.BOTH;
		gbc_infoLabel2.anchor = GridBagConstraints.WEST;
		gbc_infoLabel2.gridx = 0;
		gbc_infoLabel2.gridy = 0;
		privacySection.add(infoLabel2, gbc_infoLabel2);
		
		putLabel = new JLabel();
		GridBagConstraints gbc_putLabel = new GridBagConstraints();
		gbc_putLabel.fill = GridBagConstraints.BOTH;
		gbc_putLabel.anchor = GridBagConstraints.WEST;
		gbc_putLabel.insets = new Insets(5, 5, 5, 50);
		gbc_putLabel.gridx = 1;
		gbc_putLabel.gridy = 0;
		privacySection.add(putLabel, gbc_putLabel);
		
		JLabel infoLabel3 = new JLabel("<html><center><b><font size='4' color='#2d0c08'>Privacy Exceptions</font><br/></center></html>");
		GridBagConstraints gbc_infoLabel3 = new GridBagConstraints();
		gbc_infoLabel3.fill = GridBagConstraints.BOTH;
		gbc_infoLabel3.anchor = GridBagConstraints.WEST;
		gbc_infoLabel3.insets = new Insets(0, 50, 5, 5);
		gbc_infoLabel3.gridx = 0;
		gbc_infoLabel3.gridy = 1;
		privacySection.add(infoLabel3, gbc_infoLabel3);
		
		privacyExceptionsLabel = new JLabel();
		GridBagConstraints gbc_privacyExceptionsLabel = new GridBagConstraints();
		gbc_privacyExceptionsLabel.insets = new Insets(0, 5, 5, 50);
		gbc_privacyExceptionsLabel.fill = GridBagConstraints.BOTH;
		gbc_privacyExceptionsLabel.anchor = GridBagConstraints.WEST;
		gbc_privacyExceptionsLabel.gridx = 1;
		gbc_privacyExceptionsLabel.gridy = 1;
		privacySection.add(privacyExceptionsLabel, gbc_privacyExceptionsLabel);
		
		JLabel infoLabel4 = new JLabel("<html><center><b><font size='4' color='#2d0c08'>Security Exceptions</font><br/></center></html>");
		GridBagConstraints gbc_infoLabel4 = new GridBagConstraints();
		gbc_infoLabel4.fill = GridBagConstraints.BOTH;
		gbc_infoLabel4.anchor = GridBagConstraints.WEST;
		gbc_infoLabel4.insets = new Insets(0, 50, 5, 65);
		gbc_infoLabel4.gridx = 0;
		gbc_infoLabel4.gridy = 2;
		privacySection.add(infoLabel4, gbc_infoLabel4);
		
		utilityExceptionsLabel = new JLabel();
		GridBagConstraints gbc_utilityExceptionsLabel = new GridBagConstraints();
		gbc_utilityExceptionsLabel.insets = new Insets(0, 5, 5, 50);
		gbc_utilityExceptionsLabel.fill = GridBagConstraints.BOTH;
		gbc_utilityExceptionsLabel.anchor = GridBagConstraints.WEST;
		gbc_utilityExceptionsLabel.gridx = 1;
		gbc_utilityExceptionsLabel.gridy = 2;
		privacySection.add(utilityExceptionsLabel, gbc_utilityExceptionsLabel);
		
		HyperlinkButton modifyPrivacySectionLink = new HyperlinkButton("Change");
		GridBagConstraints gbc_modifyPrivacySectionLink = new GridBagConstraints();
		gbc_modifyPrivacySectionLink.insets = new Insets(5, 10, 5, 10);
		gbc_modifyPrivacySectionLink.fill = GridBagConstraints.HORIZONTAL;
		gbc_modifyPrivacySectionLink.anchor = GridBagConstraints.WEST;
		gbc_modifyPrivacySectionLink.gridx = 1;
		gbc_modifyPrivacySectionLink.gridy = 1;
		summaryPanel.add(modifyPrivacySectionLink, gbc_modifyPrivacySectionLink);
		modifyPrivacySectionLink.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				pcs.firePropertyChange(TAB_CHANGE_REQUEST_PROPERTY, null, PUTWb.PUT_TAB);
			}
		});
		
		JPanel expensePanel = new JPanel();
		expensePanel.setOpaque(false);
		expensePanel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		GridBagConstraints gbc_expensePanel = new GridBagConstraints();
		gbc_expensePanel.insets = new Insets(0, 5, 5, 5);
		gbc_expensePanel.fill = GridBagConstraints.BOTH;
		gbc_expensePanel.gridx = 0;
		gbc_expensePanel.gridy = 2;
		summaryPanel.add(expensePanel, gbc_expensePanel);
		GridBagLayout gbl_expensePanel = new GridBagLayout();
		gbl_expensePanel.columnWeights = new double[]{0.0, 1.0};
		gbl_expensePanel.rowWeights = new double[]{0.0, 0.0};
		expensePanel.setLayout(gbl_expensePanel);
		
		JLabel infoLabel5 = new JLabel("<html><center><b><font size='4' color='#2d0c08'>Horizontal Expense</font><br/></center></html>");
		GridBagConstraints gbc_infoLabel5 = new GridBagConstraints();
		gbc_infoLabel5.insets = new Insets(5, 50, 5, 70);
		gbc_infoLabel5.fill = GridBagConstraints.BOTH;
		gbc_infoLabel5.anchor = GridBagConstraints.WEST;
		gbc_infoLabel5.gridx = 0;
		gbc_infoLabel5.gridy = 0;
		expensePanel.add(infoLabel5, gbc_infoLabel5);
		
		horizontalExpenseLabel = new JLabel();
		GridBagConstraints gbc_horizontalExpenseLabel = new GridBagConstraints();
		gbc_horizontalExpenseLabel.insets = new Insets(5, 5, 5, 50);
		gbc_horizontalExpenseLabel.fill = GridBagConstraints.BOTH;
		gbc_horizontalExpenseLabel.anchor = GridBagConstraints.WEST;
		gbc_horizontalExpenseLabel.gridx = 1;
		gbc_horizontalExpenseLabel.gridy = 0;
		expensePanel.add(horizontalExpenseLabel, gbc_horizontalExpenseLabel);
		
		JLabel infoLabel6 = new JLabel("<html><center><b><font size='4' color='#2d0c08'>Vertical Expense</font><br/></center></html>");
		GridBagConstraints gbc_infoLabel6 = new GridBagConstraints();
		gbc_infoLabel6.fill = GridBagConstraints.BOTH;
		gbc_infoLabel6.anchor = GridBagConstraints.WEST;
		gbc_infoLabel6.insets = new Insets(0, 50, 5, 5);
		gbc_infoLabel6.gridx = 0;
		gbc_infoLabel6.gridy = 1;
		expensePanel.add(infoLabel6, gbc_infoLabel6);
		
		verticalExpenseLabel = new JLabel();
		GridBagConstraints gbc_verticalExpenseLabel = new GridBagConstraints();
		gbc_verticalExpenseLabel.fill = GridBagConstraints.BOTH;
		gbc_verticalExpenseLabel.anchor = GridBagConstraints.WEST;
		gbc_verticalExpenseLabel.insets = new Insets(0, 5, 5, 50);
		gbc_verticalExpenseLabel.gridx = 1;
		gbc_verticalExpenseLabel.gridy = 1;
		expensePanel.add(verticalExpenseLabel, gbc_verticalExpenseLabel);
		
		HyperlinkButton modifyExpenseSectionLink = new HyperlinkButton("Change");
		GridBagConstraints gbc_modifyExpenseSectionLink = new GridBagConstraints();
		gbc_modifyExpenseSectionLink.anchor = GridBagConstraints.WEST;
		gbc_modifyExpenseSectionLink.fill = GridBagConstraints.HORIZONTAL;
		gbc_modifyExpenseSectionLink.insets = new Insets(5, 10, 5, 10);
		gbc_modifyExpenseSectionLink.gridx = 1;
		gbc_modifyExpenseSectionLink.gridy = 2;
		summaryPanel.add(modifyExpenseSectionLink, gbc_modifyExpenseSectionLink);
		modifyExpenseSectionLink.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				pcs.firePropertyChange(TAB_CHANGE_REQUEST_PROPERTY, null, PUTWb.EXPENSE_TAB);
			}
		});
		
		JPanel classifierSection = new JPanel();
		classifierSection.setOpaque(false);
		classifierSection.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		GridBagConstraints gbc_classifierSection = new GridBagConstraints();
		gbc_classifierSection.insets = new Insets(0, 5, 5, 5);
		gbc_classifierSection.fill = GridBagConstraints.BOTH;
		gbc_classifierSection.gridx = 0;
		gbc_classifierSection.gridy = 3;
		summaryPanel.add(classifierSection, gbc_classifierSection);
		GridBagLayout gbl_classifierSection = new GridBagLayout();
		gbl_classifierSection.columnWeights = new double[]{0.0, 1.0};
		gbl_classifierSection.rowWeights = new double[]{1.0, 0.0};
		classifierSection.setLayout(gbl_classifierSection);
		
		JLabel infoLabel7 = new JLabel("<html><center><b><font size='4' color='#2d0c08'>Classification Mechanism</font><br/></center></html>");
		GridBagConstraints gbc_infoLabel7 = new GridBagConstraints();
		gbc_infoLabel7.insets = new Insets(5, 50, 5, 25);
		gbc_infoLabel7.fill = GridBagConstraints.BOTH;
		gbc_infoLabel7.anchor = GridBagConstraints.WEST;
		gbc_infoLabel7.gridx = 0;
		gbc_infoLabel7.gridy = 0;
		classifierSection.add(infoLabel7, gbc_infoLabel7);
		
		classifierLabel = new JLabel();
		GridBagConstraints gbc_classifierLabel = new GridBagConstraints();
		gbc_classifierLabel.insets = new Insets(5, 5, 5, 50);
		gbc_classifierLabel.fill = GridBagConstraints.BOTH;
		gbc_classifierLabel.anchor = GridBagConstraints.WEST;
		gbc_classifierLabel.gridx = 1;
		gbc_classifierLabel.gridy = 0;
		classifierSection.add(classifierLabel, gbc_classifierLabel);
		
		JLabel infoLabel8 = new JLabel("<html><center><b><font size='4' color='#2d0c08'>Classifier Options</font><br/></center></html>");
		GridBagConstraints gbc_infoLabel8 = new GridBagConstraints();
		gbc_infoLabel8.fill = GridBagConstraints.BOTH;
		gbc_infoLabel8.anchor = GridBagConstraints.WEST;
		gbc_infoLabel8.insets = new Insets(0, 50, 5, 5);
		gbc_infoLabel8.gridx = 0;
		gbc_infoLabel8.gridy = 1;
		classifierSection.add(infoLabel8, gbc_infoLabel8);
		
		classifierOptionsLabel = new JLabel();
		GridBagConstraints gbc_classifierOptionsLabel = new GridBagConstraints();
		gbc_classifierOptionsLabel.fill = GridBagConstraints.BOTH;
		gbc_classifierOptionsLabel.anchor = GridBagConstraints.WEST;
		gbc_classifierOptionsLabel.insets = new Insets(0, 5, 5, 50);
		gbc_classifierOptionsLabel.gridx = 1;
		gbc_classifierOptionsLabel.gridy = 1;
		classifierSection.add(classifierOptionsLabel, gbc_classifierOptionsLabel);
		
		HyperlinkButton modifyClassifierSectionLink = new HyperlinkButton("Change");
		GridBagConstraints gbc_modifyClassifierSectionLink = new GridBagConstraints();
		gbc_modifyClassifierSectionLink.anchor = GridBagConstraints.WEST;
		gbc_modifyClassifierSectionLink.fill = GridBagConstraints.HORIZONTAL;
		gbc_modifyClassifierSectionLink.insets = new Insets(5, 10, 5, 10);
		gbc_modifyClassifierSectionLink.gridx = 1;
		gbc_modifyClassifierSectionLink.gridy = 3;
		summaryPanel.add(modifyClassifierSectionLink, gbc_modifyClassifierSectionLink);
		modifyClassifierSectionLink.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				pcs.firePropertyChange(TAB_CHANGE_REQUEST_PROPERTY, null, PUTWb.CLASSIFIER_TAB);
			}
		});
		
		JPanel otherInputsPanel = new JPanel();
		otherInputsPanel.setOpaque(false);
		GridBagConstraints gbc_otherInputsPanel = new GridBagConstraints();
		gbc_otherInputsPanel.insets = new Insets(0, 0, 10, 0);
		gbc_otherInputsPanel.fill = GridBagConstraints.VERTICAL;
		gbc_otherInputsPanel.gridx = 0;
		gbc_otherInputsPanel.gridy = 2;
		add(otherInputsPanel, gbc_otherInputsPanel);
		GridBagLayout gbl_crossValidationPanel = new GridBagLayout();
		gbl_crossValidationPanel.columnWeights = new double[]{0.0, 1.0};
		gbl_crossValidationPanel.rowWeights = new double[]{1.0, 1.0};
		otherInputsPanel.setLayout(gbl_crossValidationPanel);
		
		kTextField = new JTextField();
		kTextField.setHorizontalAlignment(SwingConstants.CENTER);
		kTextField.setColumns(2);
		kTextField.setText("5");
		kTextField.addActionListener(this);
		GridBagConstraints gbc_kTextField = new GridBagConstraints();
		gbc_kTextField.insets = new Insets(0, 5, 0, 0);
		gbc_kTextField.gridx = 1;
		gbc_kTextField.gridy = 0;
		otherInputsPanel.add(kTextField, gbc_kTextField);
		
		JLabel infoLabel10 = new JLabel("<html><center><font size='4' color='#9e1503'>The value of <b><i>k</i></b> to use for k-fold cross validation while collecting model statistics</font></center></html>");
		GridBagConstraints gbc_infoLabel10 = new GridBagConstraints();
		gbc_infoLabel10.anchor = GridBagConstraints.WEST;
		gbc_infoLabel10.fill = GridBagConstraints.HORIZONTAL;
		gbc_infoLabel10.insets = new Insets(0, 0, 0, 0);
		gbc_infoLabel10.gridx = 0;
		gbc_infoLabel10.gridy = 0;
		otherInputsPanel.add(infoLabel10, gbc_infoLabel10);
		
		randomGenerationAdvise = new JCheckBox();
		randomGenerationAdvise.setOpaque(false);
		GridBagConstraints gbc_randomGenerationAdvise = new GridBagConstraints();
		gbc_randomGenerationAdvise.anchor = GridBagConstraints.WEST;
		gbc_randomGenerationAdvise.fill = GridBagConstraints.HORIZONTAL;
		gbc_randomGenerationAdvise.insets = new Insets(0, 5, 0, 5);
		gbc_randomGenerationAdvise.gridx = 1;
		gbc_randomGenerationAdvise.gridy = 1;
		otherInputsPanel.add(randomGenerationAdvise, gbc_randomGenerationAdvise);
		
		JLabel infoLabel11 = new JLabel("<html><center><font size='4' color='#9e1503'>Advise using random generation method for generating attribute combinations</font></center></html>");
		GridBagConstraints gbc_infoLabel11 = new GridBagConstraints();
		gbc_infoLabel11.fill = GridBagConstraints.HORIZONTAL;
		gbc_infoLabel11.insets = new Insets(0, 0, 0, 0);
		gbc_infoLabel11.gridx = 0;
		gbc_infoLabel11.gridy = 1;
		otherInputsPanel.add(infoLabel11, gbc_infoLabel11);
		
		startTasksButton = new JButton(" Start Tasks ");
		GridBagConstraints gbc_startTasksButton = new GridBagConstraints();
		gbc_startTasksButton.fill = GridBagConstraints.VERTICAL;
		gbc_startTasksButton.insets = new Insets(5, 0, 10, 0);
		gbc_startTasksButton.gridx = 0;
		gbc_startTasksButton.gridy = 3;
		add(startTasksButton, gbc_startTasksButton);
		startTasksButton.addActionListener(this);
		
		setSummary();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String kText = kTextField.getText();
		int k = -1;
		String temporaryFile = null;
		try {
			k = Integer.parseInt(kText);
			temporaryFile = File.createTempFile("put", "" + System.currentTimeMillis()).getAbsolutePath();
		} catch(NumberFormatException | NullPointerException ex) {
			JOptionPane.showMessageDialog(null, "Please provide a valid value of \"k\" for cross validation", "Error", JOptionPane.ERROR_MESSAGE);
			kTextField.setText("5");
			return;
		} catch(IOException ex) {
			JOptionPane.showMessageDialog(null, "Problem in creating temporary file. Please check if you have sufficient disk space.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String dataset = datasetInfo.getSelectedFile().getAbsolutePath();
		char missingValuePreference = datasetInfo.isDeleteMissingValues() ? 'D' : 'R';
		char duplicateInstancesPreference = datasetInfo.isIgnoreDuplicateInstances() ? 'Y' : 'N';
		float putNumber = privacyInfo.getPUTNumber();
		String privacyExceptions = privacyInfo.getPrivacyExceptions().toString();
		privacyExceptions = "{" + privacyExceptions.substring(1, privacyExceptions.length()-1) + "}";
		String utilityExceptions = privacyInfo.getUtilityExceptions().toString();
		utilityExceptions = "{" + utilityExceptions.substring(1, utilityExceptions.length()-1) + "}";
		float vExpense = expenseInfo.getVerticalExpense();
		float hExpense = expenseInfo.getHorizontalExpense();
		String classifier = classifierInfo.getClassifier();
		String classifierOptions = classifierInfo.getCustomOptions();
		char adviseRandomGeneration = randomGenerationAdvise.isSelected() ? 'Y' : 'N';
		String[] params = new String[26];
		
		params[0] = PUTExperiment.DATA_FILE_SWITCH;
		params[1] = dataset;
		
		params[2] = PUTExperiment.PUT_NUMBER_SWITCH;
		params[3] = "" + putNumber;
		
		params[4] = PUTExperiment.V_EXPENSE_SWITCH;
		params[5] = "" + vExpense;
		
		params[6] = PUTExperiment.H_EXPENSE_SWITCH;
		params[7] = "" + hExpense;
		
		params[8] = PUTExperiment.PRIVACY_EXCEPTIONS_SWITCH;
		params[9] = privacyExceptions;
		
		params[10] = PUTExperiment.UTILITY_EXCEPTIONS_SWITCH;
		params[11] = utilityExceptions;
		
		params[12] = PUTExperiment.CLASSIFIER_SWITCH;
		params[13] = classifier;
		
		params[14] = PUTExperiment.CLASSIFIER_OPTIONS_SWITCH;
		params[15] = classifierOptions;
		
		params[16] = PUTExperiment.K_CROSS_SWITCH;
		params[17] = "" + k;
		
		params[18] = PUTExperiment.MISSING_VALUE_SWITCH;
		params[19] = "" + missingValuePreference;
		
		params[20] = PUTExperiment.DUPLICATE_ROWS_SWITCH;
		params[21] = "" + duplicateInstancesPreference;
		
		params[22] = PUTExperiment.OUTPUT_FILE_SWITCH;
		params[23] = temporaryFile;
		
		params[24] = PUTExperiment.GENERATION_METHOD_SWITCH;
		params[25] = "" + adviseRandomGeneration;
		
		PUTExperiment experiment = PUTExperiment.createExperiment(params);
		
		String error = experiment.runCompatibilityTests();
		if(error != null) {
			JOptionPane.showMessageDialog(null, "Could not start experiment !!\nReason: " + error, "Problem in stating experiment", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		pcs.firePropertyChange(TASKS_RUNNING_PROPERTY, null, true);
		
		RunningStateDialog dialog = new RunningStateDialog(experiment, this);
		
		pcs.firePropertyChange(TASKS_RUNNING_PROPERTY, null, false);
		
		if(dialog.isTasksCompleted()) {
			resultFile = experiment.getResultFile();
			/*
			 * Write preferences
			 */
			try {
				File tempFile = File.createTempFile("putPref", "" + System.nanoTime());
				PrintWriter pw = new PrintWriter(tempFile);
				for(int i = 0; i < params.length; i+=2) {
					if(params[i] == PUTExperiment.DATA_FILE_SWITCH || params[i] == PUTExperiment.OUTPUT_FILE_SWITCH)
						continue;
					pw.println(params[i] + " " + params[i+1]);
				}
				pw.flush();
				pw.close();
				preferencesFile = tempFile;
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Problem in creating/writing temporary file. You may not be able to save this experiment.", "Warning", JOptionPane.WARNING_MESSAGE);
			}
			pcs.firePropertyChange(TASKS_COMPLETED_PROPERTY, null, true);
		}
		else
			pcs.firePropertyChange(TASKS_COMPLETED_PROPERTY, null, false);
	}
	
	/**
	 * Return the preferences for last successfully completed experiment
	 * @return The preferences file
	 */
	public File getPreferencesFile() {
		return preferencesFile;
	}
	
	/**
	 * Returns the result file to which the experiment's results were stored
	 * @return The file containing the results of the experiment were stored if it completed successfully, or <code>null</code> otherwise 
	 */
	public File getResultFile() {
		return resultFile;
	}
	
	/**
	 * Returns the "Start Tasks" button. Used by the autopilot.
	 * @return the start tasks Button
	 */
	public JButton getStartTasksButton() {
		return startTasksButton;
	}

	@Override
	public void setInitialPreferences(Map<String, String> preferences) {
		if(preferences != null) {
			String kCrossValidationPreference = preferences.get(PUTExperiment.K_CROSS_SWITCH);
			if(kCrossValidationPreference != null)
				kTextField.setText(kCrossValidationPreference);
			String generationMethodPreference = preferences.get(PUTExperiment.GENERATION_METHOD_SWITCH);
			if(generationMethodPreference != null)
				randomGenerationAdvise.setSelected(generationMethodPreference.trim() == "Y");
		}
	}

	/**
	 * Sets the summary labels for all the inputs given by the user
	 */
	public void setSummary() {
		String dataset = datasetInfo.getSelectedFile().getAbsolutePath();
		datasetLabel.setText("<html><center><font size='4' color='#9e1503'>" + dataset + "</font></center></html>");
		
		float putNumber = privacyInfo.getPUTNumber();
		String privacyExceptions = privacyInfo.getPrivacyExceptions().toString();
		if(privacyExceptions == null || privacyExceptions.trim().length() == 0 || privacyExceptions.equals("[]"))
			privacyExceptions = "<b><i>None</i></b>";
		else
			privacyExceptions = "{" + privacyExceptions.substring(1, privacyExceptions.length()-1) + "}";
		String utilityExceptions = privacyInfo.getUtilityExceptions().toString();
		if(utilityExceptions == null || utilityExceptions.trim().length() == 0 || utilityExceptions.equals("[]"))
			utilityExceptions = "<b><i>None</i></b>";
		else
			utilityExceptions = "{" + utilityExceptions.substring(1, utilityExceptions.length()-1) + "}";
		
		putLabel.setText("<html><center><font size='4' color='#9e1503'>" + putNumber + "</font></center></html>");
		privacyExceptionsLabel.setText("<html><center><font size='4' color='#9e1503'>" + privacyExceptions + "</font></center></html>");
		utilityExceptionsLabel.setText("<html><center><font size='4' color='#9e1503'>" + utilityExceptions + "</font></center></html>");
		
		float vExpense = expenseInfo.getVerticalExpense();
		float hExpense = expenseInfo.getHorizontalExpense();
		
		verticalExpenseLabel.setText("<html><center><font size='4' color='#9e1503'>" + vExpense + "</font></center></html>");
		horizontalExpenseLabel.setText("<html><center><font size='4' color='#9e1503'>" + hExpense + "</font></center></html>");
		
		String classifier = classifierInfo.getClassifier();
		String classifierOptions = classifierInfo.getCustomOptions();
		if(classifierOptions == null || classifierOptions.trim().length() == 0 || classifierOptions.equals("{}"))
			classifierOptions = "<b><i>None</i></b>";
		
		classifierLabel.setText("<html><center><font size='4' color='#9e1503'>" + classifier + "</font></center></html>");
		classifierOptionsLabel.setText("<html><center><font size='4' color='#9e1503'>" + classifierOptions + "</font></center></html>");
	}
}
