package in.ac.iitk.cse.putwb.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import in.ac.iitk.cse.putwb.classify.Dataset;
import in.ac.iitk.cse.putwb.experiment.PUTExperiment;
import in.ac.iitk.cse.putwb.ui.actions.Action;
import in.ac.iitk.cse.putwb.ui.actions.AnalyzeAction;
import in.ac.iitk.cse.putwb.ui.actions.RunAction;
import in.ac.iitk.cse.putwb.ui.actions.SelectClassifierAction;
import in.ac.iitk.cse.putwb.ui.actions.SelectPrivacySettingsAction;
import in.ac.iitk.cse.putwb.ui.actions.SetExpenseAction;
import in.ac.iitk.cse.putwb.ui.actions.UploadFileAction;
import in.ac.iitk.cse.putwb.ui.widgets.IconButton;
import weka.core.Instances;

/**
 * The UI version of Privacy Utility Trade-off utility. 
 * It creates an instance of {@link PUTExperiment} from the user preferences, runs the experiment, and shows results. 
 * @author Saurabh Srivastava
 *
 */
public class PUTWb implements PropertyChangeListener {

	/**
	 * The constant to represent Results and Analysis action
	 */
	private static final short ANALYZE_ACTION = 6;
	
	/**
	 * The constant to represent Results and Analysis tab
	 */
	public static final short ANALYZE_TAB = 16;
	
	/**
	 * The constant to represent Classifier Selection action
	 */
	private static final short CLASSIFIER_ACTION = 4;

	/**
	 * The constant to represent Classifier Selection tab
	 */
	public static final short CLASSIFIER_TAB = 14;
	
	/**
	 * The constant to represent Expense Selection action
	 */
	private static final short EXPENSE_ACTION = 3;
	
	/**
	 * The constant to represent Expense Selection tab
	 */
	public static final short EXPENSE_TAB = 13;
	
	/**
	 * The constant to represent Dataset Selection action
	 */
	private static final short LOAD_ACTION = 1;
	
	/**
	 * The constant to represent Dataset Selection tab
	 */
	public static final short LOAD_TAB = 11;
	
	/**
	 * The constant to represent Privacy Settings Selection action
	 */
	private static final short PUT_ACTION = 2;
	
	/**
	 * The constant to represent Privacy Settings Selection tab
	 */
	public static final short PUT_TAB = 12;
	
	/**
	 * The constant to represent Execution action
	 */
	private static final short RUN_ACTION = 5;
	
	/**
	 * The constant to represent Execution tab
	 */
	public static final short RUN_TAB = 15;
	
	/**
	 * Launches the UI
	 * @param args Any arguments are ignored as of now
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PUTWb window = new PUTWb();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Keeps a map of different actions active in the tool at any given point
	 */
	private Map<Short, Action> actions;
	
	/**
	 * A panel to show every action's content in the form of a tab like setting
	 */
	private ActionsPanel actionsPanel;
	
	/**
	 * Points to the currently shown tab
	 */
	private short currentTab;
	
	/**
	 * The dataset over which the experiment is conducted
	 */
	private Instances dataset;
	
	/**
	 * The flag becomes <code>true</code> when the experiment completes successfully, and the results can now be shown
	 */
	private boolean enableAnalysis = false;
	
	/**
	 * The frame that displays the UI
	 */
	private JFrame frame;
	
	/**
	 * The color used to paint an overlay, which looks like the UI is inaccessible during the experiment's execution
	 */
	private final Color OVERLAY_COLOR = new Color(255, 255, 255, 200);
	
	/**
	 * The flag, if <code>true</code>, results in an overlay painted over the the UI
	 */
	private boolean paintOverlay = false;
	
	/**
	 * A Panel to show the Status Buttons - showing which tab is currently selected, and which other tabs are active and can be seen
	 */
	private StatusPanel statusPanel;

	/**
	 * Create the application.
	 */
	public PUTWb() {
		initialize();
		setup();
	}

	/**
	 * Attempts to navigate to a particular tab (if it is active), and take any custom actions required just before switching to the tab
	 * @param navigationRequest The tab to navigate to
	 */
	private void attemptNavigation(short navigationRequest) {
		try {
			String tabName = getTabName(navigationRequest);
			Action action = getActionForTab(tabName);
			actionsPanel.setContent(action);
			if(navigationRequest > currentTab) {
				actionsPanel.getBackButton().setDisabled(false);
				actionsPanel.getRewindButton().setDisabled(false);
			} else if(navigationRequest < currentTab) {
				actionsPanel.getForwardButton().setDisabled(false);
				actionsPanel.getFastForwardButton().setDisabled(false);
			}
			if(navigationRequest == LOAD_TAB) {
				actionsPanel.getBackButton().setDisabled(true);
				actionsPanel.getRewindButton().setDisabled(true);
			} else if(navigationRequest == ANALYZE_TAB || (!enableAnalysis && navigationRequest == RUN_TAB)) {
				actionsPanel.getForwardButton().setDisabled(true);
				actionsPanel.getFastForwardButton().setDisabled(true);
			}
			if(action instanceof RunAction) {
				((RunAction)action).setSummary();
			} else if(action instanceof SetExpenseAction) {
				((SetExpenseAction)action).resetVerticalExpenseInfo();
			}
			currentTab = navigationRequest;
			statusPanel.selectStatusButton(tabName.toLowerCase());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the Action corresponding to a tab's name. Uses Java Reflection APIs to find an appropriate Action constant to return
	 * @param tabName The name of the tab to search
	 * @return one of the Action constants that matches the request
	 * @throws Exception If the tab name is not valid
	 */
	private Action getActionForTab(String tabName) throws Exception {
		String actionName = tabName + "_ACTION";
		Field actionField = PUTWb.class.getDeclaredField(actionName);
		return actions.get(actionField.get(null));
	}
	
	/**
	 * Returns textual representation of a tab
	 * @param tabId The tab id for the tab
	 * @return a <code>String</code> representation of a given tab id
	 * @throws Exception If the tab id is invalid
	 */
	private String getTabName(short tabId) throws Exception {
		Field[] fields = PUTWb.class.getDeclaredFields();
		for(Field f : fields) {
			int modifiers = f.getModifiers();
			String name = f.getName();
			if(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && f.getType().equals(short.class)) {
				if(name.endsWith("TAB") && f.get(null).equals(tabId)) {
					return name.split("_")[0];
				}
			}
		}
		return null;
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		frame = new JFrame("Privacy Utility Tradeoff Workbench") {

			public void paint(Graphics g) {
				super.paint(g);
				if(paintOverlay) {
					Dimension size = getSize();
					Graphics2D g2 = (Graphics2D)(g.create());
					g2.setColor(OVERLAY_COLOR);
					g2.fillRect(0, 0, size.width, size.height);
					g2.dispose();
				}
			}
		};
		frame.setContentPane(new JDesktopPane());
		frame.setSize(new Dimension(1000, 800));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		
		gridBagLayout.rowHeights = new int[]{120, 0};
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0, 1.0};
		frame.getContentPane().setLayout(gridBagLayout);
		
		statusPanel = new StatusPanel();
		statusPanel.addPropertyChangeListener(this);
		GridBagConstraints gbc_statusPanel = new GridBagConstraints();
		gbc_statusPanel.weighty = 0.0;
		gbc_statusPanel.insets = new Insets(5, 5, 5, 5);
		gbc_statusPanel.fill = GridBagConstraints.BOTH;
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 0;
		frame.getContentPane().add(statusPanel, gbc_statusPanel);
		
		actionsPanel = new ActionsPanel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void navigate(IconButton buttonPressed) {
				short temp = currentTab;
				if(actionsPanel.getBackButton().equals(buttonPressed)) {
					temp--;
				} else if(actionsPanel.getRewindButton().equals(buttonPressed)) {
					temp = LOAD_TAB;
				} else if(actionsPanel.getForwardButton().equals(buttonPressed)) {
					temp++;
				} else if(actionsPanel.getFastForwardButton().equals(buttonPressed)) {
					if(enableAnalysis)
						temp = ANALYZE_TAB;
					else
						temp = RUN_TAB;
				}
				attemptNavigation(temp);
			}
		};
		GridBagConstraints gbc_actionsPanel = new GridBagConstraints();
		gbc_actionsPanel.insets = new Insets(5, 5, 5, 5);
		gbc_actionsPanel.anchor = GridBagConstraints.NORTH;
		gbc_actionsPanel.weighty = 0.0;
		gbc_actionsPanel.fill = GridBagConstraints.BOTH;
		gbc_actionsPanel.gridx = 0;
		gbc_actionsPanel.gridy = 1;
		frame.getContentPane().add(actionsPanel, gbc_actionsPanel);
		frame.setLocationRelativeTo(null);
		
		currentTab = LOAD_TAB;
	}
	
	/**
	 * Handles events of navigation, dataset loading, experiment completion etc.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(UploadFileAction.LOADED_DATASET_PROPERTY)) {
			dataset = (Instances)evt.getNewValue();
			if(dataset == null) {	// Reset everything
				Action action = actions.get(LOAD_ACTION);
				actions.clear();
				actions.put(LOAD_ACTION, action);
				
				statusPanel.setInitialState();
				
				actionsPanel.getRewindButton().setDisabled(true);
				actionsPanel.getBackButton().setDisabled(true);
				actionsPanel.getForwardButton().setDisabled(true);
				actionsPanel.getFastForwardButton().setDisabled(true);
			} else {
				Action action = new SelectPrivacySettingsAction(dataset);
				actions.put(PUT_ACTION, action);
				
				action = new SetExpenseAction(dataset, (SelectPrivacySettingsAction)actions.get(PUT_ACTION));
				actions.put(EXPENSE_ACTION, action);
				
				action = new SelectClassifierAction();
				actions.put(CLASSIFIER_ACTION, action);
				
				action = new RunAction((UploadFileAction)actions.get(LOAD_ACTION),
										(SelectPrivacySettingsAction)actions.get(PUT_ACTION),
										(SetExpenseAction)actions.get(EXPENSE_ACTION),
										(SelectClassifierAction)actions.get(CLASSIFIER_ACTION));
				action.addPropertyChangeListener(this);
				actions.put(RUN_ACTION, action);
				
				statusPanel.setReadyState();
				
				actionsPanel.getForwardButton().setDisabled(false);
				actionsPanel.getFastForwardButton().setDisabled(false);
			}
			enableAnalysis = false;
		} else if(evt.getPropertyName().equals(StatusPanel.TAB_CHANGED)) {
			short tab = ((Integer)evt.getNewValue()).shortValue();
			attemptNavigation(tab);
		} else if(evt.getPropertyName().equals(RunAction.TAB_CHANGE_REQUEST_PROPERTY)) {
			short tab = (Short)evt.getNewValue();
			attemptNavigation(tab);
		} else if(evt.getPropertyName().equals(RunAction.TASKS_RUNNING_PROPERTY)) {
			paintOverlay = (Boolean)evt.getNewValue();
			frame.repaint();
		} else if(evt.getPropertyName().equals(RunAction.TASKS_COMPLETED_PROPERTY)) {
			if((Boolean)evt.getNewValue()) {
				RunAction runAction = (RunAction)actions.get(RUN_ACTION);
				String[] attributeNames = new String[dataset.numAttributes() - 1];
				for(int i = 0; i < attributeNames.length; i++)
					attributeNames[i] = dataset.attribute(i).name();
				List<String> allClasses = Dataset.getAllClassesForDataset(dataset);
				AnalyzeAction action = new AnalyzeAction(runAction.getResultFile(), attributeNames, allClasses, 
						((UploadFileAction)actions.get(LOAD_ACTION)).getSelectedFile(), 
						((RunAction)actions.get(RUN_ACTION)).getPreferencesFile());
				actions.put(ANALYZE_ACTION, action);
				enableAnalysis = true;
				
				statusPanel.setFinishedState();
				actionsPanel.getForwardButton().setDisabled(false);
				actionsPanel.getFastForwardButton().setDisabled(false);
				attemptNavigation(ANALYZE_TAB);
			}
		} else if(evt.getPropertyName().equals(UploadFileAction.LOADED_EXPERIMENT_PROPERTY)) {
			UploadFileAction uploadAction = (UploadFileAction)actions.get(LOAD_ACTION);
			File resultFile = uploadAction.getResultsFile();
			File prefsFile = uploadAction.getPreferencesFile();
			String[] attributeNames = new String[dataset.numAttributes() - 1];
			for(int i = 0; i < attributeNames.length; i++)
				attributeNames[i] = dataset.attribute(i).name();
			List<String> allClasses = Dataset.getAllClassesForDataset(dataset);
			AnalyzeAction action = new AnalyzeAction(resultFile, attributeNames, allClasses, 
					((UploadFileAction)actions.get(LOAD_ACTION)).getSelectedFile(), 
					prefsFile);
			actions.put(ANALYZE_ACTION, action);
			enableAnalysis = true;
			statusPanel.setFinishedState();
			actionsPanel.getForwardButton().setDisabled(false);
			actionsPanel.getFastForwardButton().setDisabled(false);
			@SuppressWarnings("unchecked")
			Map<String, String> map = (Map<String, String>)evt.getNewValue();
			for(short key : actions.keySet())
				actions.get(key).setInitialPreferences(map);
			attemptNavigation(ANALYZE_TAB);
		}
	}
	
	/**
	 * Initial setup
	 */
	private void setup() {
		actions = new HashMap<Short, Action>();
		
		Action action = new UploadFileAction();
		actions.put(LOAD_ACTION, action);
		action.addPropertyChangeListener(this);
		actionsPanel.setContent(actions.get(LOAD_ACTION));
	}

}
