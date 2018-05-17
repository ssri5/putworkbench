package in.ac.iitk.cse.putwb.ui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.KeyStroke;

import in.ac.iitk.cse.putwb.classify.Dataset;
import in.ac.iitk.cse.putwb.experiment.PUTExperiment;
import in.ac.iitk.cse.putwb.heuristic.BasicSettingsSuggestor;
import in.ac.iitk.cse.putwb.heuristic.ClassifierSuggestor;
import in.ac.iitk.cse.putwb.ui.actions.Action;
import in.ac.iitk.cse.putwb.ui.actions.AnalyzeAction;
import in.ac.iitk.cse.putwb.ui.actions.RunAction;
import in.ac.iitk.cse.putwb.ui.actions.SelectClassifierAction;
import in.ac.iitk.cse.putwb.ui.actions.SelectPrivacySettingsAction;
import in.ac.iitk.cse.putwb.ui.actions.SetExpenseAction;
import in.ac.iitk.cse.putwb.ui.actions.UploadFileAction;
import in.ac.iitk.cse.putwb.ui.widgets.FloatingSliderPanel;
import in.ac.iitk.cse.putwb.ui.widgets.IconButton;
import in.ac.iitk.cse.putwb.ui.widgets.WaitDialog;
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
	 * A thread to run the autopilot
	 */
	private Thread autopilot;

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
	 * A robot for mouse and keyboard events. Used by the autopilot.
	 */
	private CustomizedRobot robot;

	/**
	 * A Panel to show the Status Buttons - showing which tab is currently selected, and which other tabs are active and can be seen
	 */
	private StatusPanel statusPanel;

	/**
	 * An instance of a waiting dialog box, shown while a dataset or experiment is being loaded
	 */
	private WaitDialog waitDialog;

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
	 * Hides the waiting dialog box
	 */
	private void hideWaitingDialog() {
		waitDialog.close();
		waitDialog.dispose();
		paintOverlay = false;
		frame.repaint();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		frame = new JFrame("Privacy Utility Tradeoff Workbench " + PUTExperiment.versionInfo) {

			public void paint(Graphics g) {
				super.paint(g);
				Graphics2D g2 = (Graphics2D)(g.create());
				if(paintOverlay) {
					Dimension size = getSize();
					g2.setColor(OVERLAY_COLOR);
					g2.fillRect(0, 0, size.width, size.height);
				}
				g2.dispose();
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

		AbstractAction interruptAutopilotAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(autopilot != null && autopilot.isAlive() && !autopilot.isInterrupted())
					autopilot.interrupt();
			}
		};

		frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "interruptAutopilot");
		frame.getRootPane().getActionMap().put("interruptAutopilot", interruptAutopilotAction);

		currentTab = LOAD_TAB;
	}

	/**
	 * Handles events of navigation, dataset loading, experiment completion etc.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(UploadFileAction.LOADING_DATASET_OR_EXPERIMENT_PROPERTY)) {
			showWaitingDialog();
		} else if(evt.getPropertyName().equals(UploadFileAction.LOADED_DATASET_PROPERTY)) {
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
				hideWaitingDialog();
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
		} else if(evt.getPropertyName().equals(UploadFileAction.START_AUTOPILOT_PROPERTY)) {
			if(dataset != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> inputs = (Map<String, Object>)evt.getNewValue();
				inputs.put(BasicSettingsSuggestor.DATASET, dataset);
				startAutopilot(inputs);
			}
		}
	}

	/**
	 * Selects the suggested classifier in the application window. Used by the autopilot.
	 * @param suggestedClassifier The suggested classifier
	 * @throws Exception If something goes wrong or the autopilot is interrupted
	 */
	private void setClassifierInAutopilot(String suggestedClassifier) throws Exception {
		// Find position to click
		JComboBox<String> comboBox = ((SelectClassifierAction)actions.get(CLASSIFIER_ACTION)).getClassifierDropdown();
		Point comboBoxLocation = comboBox.getLocationOnScreen();
		Dimension comboBoxSize = comboBox.getSize();
		Point clickPoint = new Point(comboBoxLocation.x + comboBoxSize.width/2, comboBoxLocation.y + comboBoxSize.height/2);
		robot.moveMouseToLocation(clickPoint);
		robot.click();
		comboBox.grabFocus();
		Thread.sleep(500);
		if(autopilot.isInterrupted())
			throw new InterruptedException();
		// Select the classifier required
		if(!comboBox.getSelectedItem().equals(suggestedClassifier)) {
			robot.typeString(suggestedClassifier);
			Thread.sleep(1000);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
		}
	}

	/**
	 * Selects the suggested horizontal expense in the application window. Used by the autopilot.
	 * @param suggestedH The suggested horizontal expense
	 * @throws Exception If something goes wrong or the autopilot is interrupted
	 */
	private void setHInAutopilot(float suggestedH) throws Exception {
		// Find out rough position to click
		FloatingSliderPanel sliderPanel = ((SetExpenseAction)actions.get(EXPENSE_ACTION)).getHorizontalExpenseSlider();
		JSlider slider = sliderPanel.getSlider();
		Point sliderLocation = slider.getLocationOnScreen();
		Dimension sliderSize = slider.getSize();
		int offset = (int)(sliderSize.width*(suggestedH));
		Point clickPoint = new Point(sliderLocation.x + offset, sliderLocation.y + sliderSize.height/2);
		robot.moveMouseToLocation(clickPoint);
		robot.click();
		// Fine tune, if required
		float selectedH = sliderPanel.getCurrentValue();
		if(selectedH != suggestedH) {
			// Get as close as possible with the mouse:
			float diff = selectedH - suggestedH;
			int moveOffset;
			if(diff < 0)
				moveOffset = 10;
			else
				moveOffset = -10;

			do {
				if(autopilot.isInterrupted())
					throw new InterruptedException();
				clickPoint.x += moveOffset;
				// Check if we've gone beyond the bounds of the slider, if so top the movement
				sliderLocation = slider.getLocationOnScreen();
				sliderSize = slider.getSize();
				Rectangle sliderBoundsOnScreen = new Rectangle(sliderLocation.x, sliderLocation.y, sliderSize.width, sliderSize.height);
				if(!sliderBoundsOnScreen.contains(clickPoint))
					break;
				// Move the mouse slightly to the left or right
				robot.moveMouseToLocation(clickPoint);
				robot.click();
				selectedH = sliderPanel.getCurrentValue();
				float newDiff = selectedH - suggestedH;
				if(Math.abs(newDiff) > Math.abs(diff)) // We are making it worse
					break;
			} while(selectedH != suggestedH);
			// Now use the keyboard to fine tune, if required
			if(selectedH != suggestedH) {
				// Get back to the last point we clicked that was good
				Thread.sleep(5000);
				clickPoint.x -= moveOffset;
				robot.moveMouseToLocation(clickPoint);
				robot.click();
				slider.grabFocus();
				diff = selectedH - suggestedH;
				int keyToPress;
				if(diff < 0)
					keyToPress = KeyEvent.VK_RIGHT;
				else
					keyToPress = KeyEvent.VK_LEFT;
				diff = selectedH - suggestedH;
				do {
					robot.pressKey(keyToPress);
					selectedH = sliderPanel.getCurrentValue();
					float newDiff = selectedH - suggestedH;
					if(Math.abs(newDiff) > Math.abs(diff)) { // That's the closest we could set to the suggested value
						// Just go to the last state and give up !!
						if(keyToPress == KeyEvent.VK_RIGHT)
							keyToPress = KeyEvent.VK_LEFT;
						else
							keyToPress = KeyEvent.VK_RIGHT;
						robot.pressKey(keyToPress);
						break;
					}
					diff = newDiff;
					if(autopilot.isInterrupted())
						throw new InterruptedException();
				} while(selectedH != suggestedH);
			}
		}
	}

	/**
	 * Selects the suggested partition size in the application window. Used by the autopilot.
	 * @param suggestedPartitionSize The suggested partition size
	 * @return <code>true</code> if the suggested partition size was set, <code>false</code> if the slider is too sensitive to set the suggested value
	 * @throws Exception If something goes wrong or the autopilot is interrupted
	 */
	private boolean setPartitionSizeInAutopilot(int suggestedPartitionSize) throws Exception {
		// Find out rough position to click
		int totalAttributes = dataset.numAttributes();
		FloatingSliderPanel sliderPanel = ((SelectPrivacySettingsAction)actions.get(PUT_ACTION)).getPutSlider();
		JSlider slider = sliderPanel.getSlider();
		Point sliderLocation = slider.getLocationOnScreen();
		Dimension sliderSize = slider.getSize();
		int offset = (int)(sliderSize.width*((float)suggestedPartitionSize/totalAttributes));
		Point clickPoint = new Point(sliderLocation.x + offset, sliderLocation.y + sliderSize.height/2);
		robot.moveMouseToLocation(clickPoint);
		robot.click();
		// Fine tune, if required
		int selectedPartitionSize = PUTExperiment.calculatePartitionSize(totalAttributes-1, sliderPanel.getCurrentValue());
		if(selectedPartitionSize != suggestedPartitionSize) {
			// Get as close as possible with the mouse:
			int diff = selectedPartitionSize - suggestedPartitionSize;
			int moveOffset;
			if(diff < 0)
				moveOffset = 10;
			else
				moveOffset = -10;

			do {
				if(autopilot.isInterrupted())
					throw new InterruptedException();
				clickPoint.x += moveOffset;
				// Check if we've gone beyond the bounds of the slider, if so top the movement
				sliderLocation = slider.getLocationOnScreen();
				sliderSize = slider.getSize();
				Rectangle sliderBoundsOnScreen = new Rectangle(sliderLocation.x, sliderLocation.y, sliderSize.width, sliderSize.height);
				if(!sliderBoundsOnScreen.contains(clickPoint))
					break;
				// Move the mouse slightly to the left or right
				robot.moveMouseToLocation(clickPoint);
				robot.click();
				selectedPartitionSize = PUTExperiment.calculatePartitionSize(totalAttributes-1, sliderPanel.getCurrentValue());
				int newDiff = selectedPartitionSize - suggestedPartitionSize;
				if(Math.abs(newDiff) > Math.abs(diff))
					break;
			} while(selectedPartitionSize != suggestedPartitionSize);
			// Now use the keyboard to fine tune, if required
			if(selectedPartitionSize != suggestedPartitionSize) {
				// Get back to the last point we clicked that was good
				clickPoint.x -= moveOffset;
				robot.moveMouseToLocation(clickPoint);
				robot.click();
				selectedPartitionSize = PUTExperiment.calculatePartitionSize(totalAttributes-1, sliderPanel.getCurrentValue());
				diff = selectedPartitionSize - suggestedPartitionSize;
				int keyToPress;
				if(diff < 0)
					keyToPress = KeyEvent.VK_RIGHT;
				else
					keyToPress = KeyEvent.VK_LEFT;
				slider.grabFocus();
				do {
					robot.pressKey(keyToPress);
					selectedPartitionSize = PUTExperiment.calculatePartitionSize(totalAttributes-1, sliderPanel.getCurrentValue());
					int newDiff = selectedPartitionSize - suggestedPartitionSize;
					if(Math.abs(newDiff) > Math.abs(diff)) {	// Give up
						// Just go to the last state and give up !!
						if(keyToPress == KeyEvent.VK_RIGHT)
							keyToPress = KeyEvent.VK_LEFT;
						else
							keyToPress = KeyEvent.VK_RIGHT;
						robot.pressKey(keyToPress);
						JOptionPane.showMessageDialog(null, "It seems that the slider is too sensitive for me to set the right value.. I am giving up !!", "Autopilot Problem", JOptionPane.ERROR_MESSAGE);						
						return false;
					}
					diff = newDiff;
					if(autopilot.isInterrupted())
						throw new InterruptedException();
				} while(selectedPartitionSize != suggestedPartitionSize);
			}
		}
		return true;
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

		try {
			robot = new CustomizedRobot();
		} catch (AWTException e) {
			robot = null;
			JOptionPane.showMessageDialog(null, "Problems in creating robot, you may not be able to use the Autopilot", "Autopilot Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Selects the suggested vertical expense in the application window. Used by the autopilot.
	 * @param suggestedV The suggested vertical expense
	 * @throws Exception If something goes wrong or the autopilot is interrupted
	 */
	private void setVInAutopilot(float suggestedV) throws Exception {
		// Find out rough position to click
		FloatingSliderPanel sliderPanel = ((SetExpenseAction)actions.get(EXPENSE_ACTION)).getVerticalExpenseSlider();
		JSlider slider = sliderPanel.getSlider();
		Point sliderLocation = slider.getLocationOnScreen();
		Dimension sliderSize = slider.getSize();
		int offset = (int)(sliderSize.width*(suggestedV));
		Point clickPoint = new Point(sliderLocation.x + offset, sliderLocation.y + sliderSize.height/2);
		robot.moveMouseToLocation(clickPoint);
		robot.click();
		// Fine tune, if required
		float selectedV = sliderPanel.getCurrentValue();
		if(selectedV != suggestedV) {
			// Get as close as possible with the mouse:
			float diff = selectedV - suggestedV;
			int moveOffset;
			if(diff < 0)
				moveOffset = 10;
			else
				moveOffset = -10;

			do {
				if(autopilot.isInterrupted())
					throw new InterruptedException();
				clickPoint.x += moveOffset;
				// Check if we've gone beyond the bounds of the slider, if so top the movement
				sliderLocation = slider.getLocationOnScreen();
				sliderSize = slider.getSize();
				Rectangle sliderBoundsOnScreen = new Rectangle(sliderLocation.x, sliderLocation.y, sliderSize.width, sliderSize.height);
				if(!sliderBoundsOnScreen.contains(clickPoint))
					break;
				// Move the mouse slightly to the left or right
				robot.moveMouseToLocation(clickPoint);
				robot.click();
				selectedV = sliderPanel.getCurrentValue();
				float newDiff = selectedV - suggestedV;
				if(Math.abs(newDiff) > Math.abs(diff))
					break;
			} while(selectedV != suggestedV);
			// Now use the keyboard to fine tune, if required
			if(selectedV != suggestedV) {
				// Get back to the last point we clicked that was good
				clickPoint.x -= moveOffset;
				robot.moveMouseToLocation(clickPoint);
				robot.click();
				slider.grabFocus();
				diff = selectedV - suggestedV;
				int keyToPress;
				if(diff < 0)
					keyToPress = KeyEvent.VK_RIGHT;
				else
					keyToPress = KeyEvent.VK_LEFT;
				diff = selectedV - suggestedV;
				do {
					robot.pressKey(keyToPress);
					selectedV = sliderPanel.getCurrentValue();
					float newDiff = selectedV - suggestedV;
					if(Math.abs(newDiff) > Math.abs(diff))  { // That's the closest we could set to the suggested value
						// Just go to the last state and give up !!
						if(keyToPress == KeyEvent.VK_RIGHT)
							keyToPress = KeyEvent.VK_LEFT;
						else
							keyToPress = KeyEvent.VK_RIGHT;
						robot.pressKey(keyToPress);
						break;
					}
					diff = newDiff;
					if(autopilot.isInterrupted())
						throw new InterruptedException();
				} while(selectedV != suggestedV);
			}
		}
	}

	/**
	 * Shows the waiting dialog box
	 */
	private void showWaitingDialog() {
		paintOverlay = true;
		frame.repaint();
		waitDialog = new WaitDialog(frame);
		waitDialog.open();
	}

	/**
	 * Starts the autopilot
	 * @param inputs The user preferences for the autopilot
	 */
	private void startAutopilot(Map<String, Object> inputs) {

		autopilot = new Thread() {
			@SuppressWarnings("unchecked")
			public void run() {
				PrintStream tempStream = null, originalStream = null;
				try {
					/*
					 * Switch off warnings and exceptions for a while
					 */
					tempStream = new PrintStream(File.createTempFile("autopilot", "" + System.nanoTime()));
					originalStream = System.err;
					System.setErr(tempStream);
					((SetExpenseAction)actions.get(EXPENSE_ACTION)).setSwitchOffWarnings(true);

					BasicSettingsSuggestor basicSettingsSuggestor = new BasicSettingsSuggestor();		
					Map<String, Object> decisions = (Map<String, Object>) basicSettingsSuggestor.getDecision(inputs);

					Thread.sleep(1000);

					if(isInterrupted())
						return;
					frame.requestFocus();

					/*
					 * Step 1: Go to "Privacy Settings" tab
					 */
					robot.moveMouseToComponent(actionsPanel.getForwardButton());
					robot.click();

					Thread.sleep(1000);

					if(isInterrupted())
						return;
					frame.requestFocus();

					/*
					 * Step 2: Set PUT Number
					 */

					if(!setPartitionSizeInAutopilot((int) decisions.get(PUTExperiment.PARTITION_SIZE_SWITCH)))
						return;
					Thread.sleep(1000);

					if(isInterrupted())
						return;
					frame.requestFocus();

					/*
					 * Step 3: Go to "Expense Settings" tab
					 */
					robot.moveMouseToComponent(actionsPanel.getForwardButton());
					robot.click();

					Thread.sleep(1000);

					if(isInterrupted())
						return;
					frame.requestFocus();

					/*
					 * Step 4: Set Vertical Expense
					 */
					setVInAutopilot((float) decisions.get(PUTExperiment.V_EXPENSE_SWITCH));	
					Thread.sleep(1000);

					if(isInterrupted())
						return;
					frame.requestFocus();

					/*
					 * Step 5: Set Horizontal Expense
					 */
					setHInAutopilot((float) decisions.get(PUTExperiment.H_EXPENSE_SWITCH));	
					Thread.sleep(1000);

					if(isInterrupted())
						return;
					frame.requestFocus();

					/*
					 * Step 6: Go to "Classifier Settings" tab
					 */
					robot.moveMouseToComponent(actionsPanel.getForwardButton());
					robot.click();

					Thread.sleep(1000);

					if(isInterrupted())
						return;
					frame.requestFocus();

					/*
					 * Step 7: Set the Classifier
					 */
					ClassifierSuggestor classifierSuggestor = new ClassifierSuggestor();
					decisions = (Map<String, Object>) classifierSuggestor.getDecision(inputs);
					setClassifierInAutopilot((String) decisions.get(PUTExperiment.CLASSIFIER_SWITCH));

					Thread.sleep(1000);

					if(isInterrupted())
						return;
					frame.requestFocus();

					/*
					 * Step 8: Go to "Run Experiment" tab
					 */
					robot.moveMouseToComponent(actionsPanel.getForwardButton());
					robot.click();

					Thread.sleep(1000);

					/*
					 * Last Step: Go to the "Start Tasks" Button
					 */
					robot.moveMouseToComponent(((RunAction)actions.get(RUN_ACTION)).getStartTasksButton());

					Thread.sleep(1000);

					stopAutopilot('N');
				} catch(Exception e) {
					if(!(e instanceof InterruptedException)) {
						stopAutopilot('E');
						e.printStackTrace(System.err);
					} else {
						stopAutopilot('D');
					}
				} finally {
					/*
					 * Restore the warnings
					 */
					((SetExpenseAction)actions.get(EXPENSE_ACTION)).setSwitchOffWarnings(false);
					if(originalStream != null)
						System.setErr(originalStream);
				}
			}
		};
		JOptionPane.showMessageDialog(null, "To disengage Autopilot any time, press \"Esc\".\nAvoid using your Mouse or Keyboard while the Autopilot is on.", "Before we take-off", JOptionPane.INFORMATION_MESSAGE);
		autopilot.start();
	}

	/**
	 * Stops the autopilot, with a reason - "Normal completion" ('N'), "Disengaged by user" ('D') or "Error" ('E')
	 * @param type One of the given types - 'N', 'D' or 'E'
	 */
	private void stopAutopilot(char type) {
		if(type == 'N')
			JOptionPane.showMessageDialog(null, "I've done my best. You can start the tasks in the experiment now. Bye !!", "Autopilot Disengaged", JOptionPane.INFORMATION_MESSAGE);
		else if(type == 'D')
			JOptionPane.showMessageDialog(null, "You can start the Autopilot again by going to to the \"Load a Dataset\" Section.", "Autopilot Disengaged", JOptionPane.INFORMATION_MESSAGE);
		else	// Error
			JOptionPane.showMessageDialog(null, "It seems something went wrong. I am not perfect. Try some other time.", "Autopilot Disengaged", JOptionPane.ERROR_MESSAGE);
	}

}
