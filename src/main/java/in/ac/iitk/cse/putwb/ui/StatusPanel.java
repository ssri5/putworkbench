package in.ac.iitk.cse.putwb.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import in.ac.iitk.cse.putwb.ui.widgets.StatusButton;


/**
 * A panel that shows the various steps or actions in the workflow on the top, and can be used for navigation.
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class StatusPanel extends JPanel {

	/**
	 * The constant for "Tab Changed" property
	 */
	public static final String TAB_CHANGED = "StatusPanel - Tab selection changed";
	
	/**
	 * The Status Button for Results and Analysis action
	 */
	private StatusButton analyzeButton;
	
	/**
	 * The Status Button for Classifier Selection action
	 */
	private StatusButton classifierButton;
	
	/**
	 * The Status Button that points to currently selected action
	 */
	private short currentSelection;
	
	/**
	 * The Status Button for Expense Selection action
	 */
	private StatusButton expenseButton;
	
	/**
	 * The Status Button for Datast Selection action
	 */
	private StatusButton loadButton;
	
	/**
	 * Used for providing support for changes in properties
	 */
	protected final PropertyChangeSupport pcs;
	
	/**
	 * The Status Button for Privacy Settings action
	 */
	private StatusButton putButton;
	
	/**
	 * The Status Button for Experiment Execution action
	 */
	private StatusButton runButton;
	
	/**
	 * Creates a new Status Button Panel
	 */
	public StatusPanel() {
		pcs = new PropertyChangeSupport(this);
		
		MouseAdapter eventHandler = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				StatusButton source = (StatusButton)e.getSource();
				if(source.equals(loadButton) && currentSelection != PUTWb.LOAD_TAB && !loadButton.isDisabled()) {
					selectStatusButton(loadButton);
					pcs.firePropertyChange(TAB_CHANGED, currentSelection, PUTWb.LOAD_TAB);
					currentSelection = PUTWb.LOAD_TAB;
				} else if(source.equals(putButton) && currentSelection != PUTWb.PUT_TAB && !putButton.isDisabled()) {
					selectStatusButton(putButton);
					pcs.firePropertyChange(TAB_CHANGED, currentSelection, PUTWb.PUT_TAB);
					currentSelection = PUTWb.PUT_TAB;
				} else if(source.equals(expenseButton) && currentSelection != PUTWb.EXPENSE_TAB && !expenseButton.isDisabled()) {
					selectStatusButton(expenseButton);
					pcs.firePropertyChange(TAB_CHANGED, currentSelection, PUTWb.EXPENSE_TAB);
					currentSelection = PUTWb.EXPENSE_TAB;
				} else if(source.equals(classifierButton) && currentSelection != PUTWb.CLASSIFIER_TAB && !classifierButton.isDisabled()) {
					selectStatusButton(classifierButton);
					pcs.firePropertyChange(TAB_CHANGED, currentSelection, PUTWb.CLASSIFIER_TAB);
					currentSelection = PUTWb.CLASSIFIER_TAB;
				} else if(source.equals(runButton) && currentSelection != PUTWb.RUN_TAB && !runButton.isDisabled()) {
					selectStatusButton(runButton);
					pcs.firePropertyChange(TAB_CHANGED, currentSelection, PUTWb.RUN_TAB);
					currentSelection = PUTWb.RUN_TAB;
				} else if(source.equals(analyzeButton) && currentSelection != PUTWb.ANALYZE_TAB && !analyzeButton.isDisabled()) {
					selectStatusButton(analyzeButton);
					pcs.firePropertyChange(TAB_CHANGED, currentSelection, PUTWb.ANALYZE_TAB);
					currentSelection = PUTWb.ANALYZE_TAB;
				}
			}
		};
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
		gridBagLayout.rowWeights = new double[]{1.0};
		setLayout(gridBagLayout);
		
		loadButton = new StatusButton("Load a\nDataset", IconCreator.getIcon(IconCreator.DATA_SOURCE_ICON_FILE, "Select an ARFF file to load"), true, false);
		GridBagConstraints gbc_loadDataButton = new GridBagConstraints();
		gbc_loadDataButton.insets = new Insets(5, 5, 5, 0);
		gbc_loadDataButton.fill = GridBagConstraints.BOTH;
		gbc_loadDataButton.gridx = 0;
		gbc_loadDataButton.gridy = 0;
		add(loadButton, gbc_loadDataButton);
		loadButton.addMouseListener(eventHandler);
		
		putButton = new StatusButton("Choose Privacy\nSettings", IconCreator.getIcon(IconCreator.PRIVACY_ICON_FILE, "Choose a tradeoff point between Privacy and Utility"), true, false);
		GridBagConstraints gbc_putSettingsButton = new GridBagConstraints();
		gbc_putSettingsButton.insets = new Insets(5, 5, 5, 0);
		gbc_putSettingsButton.fill = GridBagConstraints.BOTH;
		gbc_putSettingsButton.gridx = 1;
		gbc_putSettingsButton.gridy = 0;
		add(putButton, gbc_putSettingsButton);
		putButton.addMouseListener(eventHandler);
		
		expenseButton = new StatusButton("Set Computation\nBudgets", IconCreator.getIcon(IconCreator.EXPENSE_ICON_FILE, "Fine tune the amount of computation performed"), true, false);
		GridBagConstraints gbc_expenseSettingsButton = new GridBagConstraints();
		gbc_expenseSettingsButton.insets = new Insets(5, 5, 5, 0);
		gbc_expenseSettingsButton.fill = GridBagConstraints.BOTH;
		gbc_expenseSettingsButton.gridx = 2;
		gbc_expenseSettingsButton.gridy = 0;
		add(expenseButton, gbc_expenseSettingsButton);
		expenseButton.addMouseListener(eventHandler);
		
		classifierButton = new StatusButton("Choose a\nClassifier", IconCreator.getIcon(IconCreator.CLASSIFIER_ICON_FILE, "Select a classification technique to try"), true, false);
		GridBagConstraints gbc_selectClassifierButton = new GridBagConstraints();
		gbc_selectClassifierButton.insets = new Insets(5, 5, 5, 0);
		gbc_selectClassifierButton.fill = GridBagConstraints.BOTH;
		gbc_selectClassifierButton.gridx = 3;
		gbc_selectClassifierButton.gridy = 0;
		add(classifierButton, gbc_selectClassifierButton);
		classifierButton.addMouseListener(eventHandler);
		
		runButton = new StatusButton("Run Tasks", IconCreator.getIcon(IconCreator.RUN_ICON_FILE, "Run tests to find suggestions"), true, false);
		GridBagConstraints gbc_runButton = new GridBagConstraints();
		gbc_runButton.insets = new Insets(5, 5, 5, 0);
		gbc_runButton.fill = GridBagConstraints.BOTH;
		gbc_runButton.gridx = 4;
		gbc_runButton.gridy = 0;
		add(runButton, gbc_runButton);
		runButton.addMouseListener(eventHandler);
		
		analyzeButton = new StatusButton("View Results", IconCreator.getIcon(IconCreator.ANALYZE_ICON_FILE, "Analyze the results and suggestions"), true, false);
		GridBagConstraints gbc_analyzeButton = new GridBagConstraints();
		gbc_analyzeButton.insets = new Insets(5, 5, 5, 5);
		gbc_analyzeButton.fill = GridBagConstraints.BOTH;
		gbc_analyzeButton.gridx = 5;
		gbc_analyzeButton.gridy = 0;
		add(analyzeButton, gbc_analyzeButton);
		analyzeButton.addMouseListener(eventHandler);
		
		setBackground(Color.WHITE);
		setBorder(new EtchedBorder(EtchedBorder.RAISED, new Color(255, 218, 185), new Color(255, 228, 196)));
		setPreferredSize(new Dimension(900, 100));
		
		setInitialState();
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void disableAll() {
		Field[] fields = StatusPanel.class.getDeclaredFields();
		for(Field field : fields) {
			if(field.getType().equals(StatusButton.class)) {
				try {
					((StatusButton)(field.get(this))).setDisabled(true);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Enables all Status Buttons in the panel
	 */
	public void enableAll() {
		Field[] fields = StatusPanel.class.getDeclaredFields();
		for(Field field : fields) {
			if(field.getType().equals(StatusButton.class)) {
				try {
					((StatusButton)(field.get(this))).setDisabled(false);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	/**
	 * Sets a requested button in the panel as "selected"
	 * @param sb The button to select 
	 */
	private void selectStatusButton(StatusButton sb) {
		Field[] fields = StatusPanel.class.getDeclaredFields();
		for(Field field : fields) {
			if(field.getType().equals(StatusButton.class)) {
				try {
					StatusButton sb2 = ((StatusButton)(field.get(this)));
					if(sb2.equals(sb)) {
						sb2.setDisabled(false);
						sb2.setSelected(true);
					}
					else
						sb2.setSelected(false);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Sets a button in the panel as "selected" based on the tab associated with it 
	 * @param tabName The name of the tab
	 */
	public void selectStatusButton(String tabName) {
		try {
			Field f = StatusPanel.class.getDeclaredField(tabName + "Button");
			StatusButton buttonToSelect = ((StatusButton)(f.get(this)));
			selectStatusButton(buttonToSelect);
			f = PUTWb.class.getDeclaredField(tabName.toUpperCase() + "_TAB");
			currentSelection = (Short)(f.get(null));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the panel in finished state - i.e. all buttons are enabled
	 */
	public void setFinishedState() {
		putButton.setDisabled(false);
		expenseButton.setDisabled(false);
		classifierButton.setDisabled(false);
		runButton.setDisabled(false);
		analyzeButton.setDisabled(false);
	}
	
	/**
	 * Sets the panel in the initial state - i.e. all but the status button for dataset selection are disabled
	 */
	public void setInitialState() {
		disableAll();
		loadButton.setDisabled(false);
		loadButton.setSelected(true);
		currentSelection = PUTWb.LOAD_TAB;
	}
	
	/**
	 * Sets the panel in the ready state before experiment can be stated - i.e. all but the status button for the results and analysis action are enabled
	 */
	public void setReadyState() {
		putButton.setDisabled(false);
		expenseButton.setDisabled(false);
		classifierButton.setDisabled(false);
		runButton.setDisabled(false);
		analyzeButton.setDisabled(true);
		selectStatusButton(loadButton);
	}
}
