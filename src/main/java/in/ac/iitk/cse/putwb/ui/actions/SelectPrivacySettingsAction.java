package in.ac.iitk.cse.putwb.ui.actions;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import in.ac.iitk.cse.putwb.experiment.PUTExperiment;
import in.ac.iitk.cse.putwb.ui.widgets.ExceptionsEditor;
import in.ac.iitk.cse.putwb.ui.widgets.FloatingSliderPanel;
import weka.core.Instances;

/**
 * This action takes inputs that are central to the working of the experiment.
 * It allows user to choose a value of PUT number, and add any Privacy or Utility exceptions. 
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class SelectPrivacySettingsAction extends Action {
	
	/**
	 * The default value of PUT number to start with
	 */
	private static final float DEFAULT_PUTNUMBER = -0.8f;
	
	/**
	 * Holds the name of all the attributes in the selected dataset (minus the class attribute)
	 */
	private String[] attributeNames;
	
	/**
	 * The container to display options for adding exceptions - for privacy or utility
	 */
	private JPanel exceptionsContainer;
	
	/**
	 * An instance of the {@link ExceptionsEditor} to add privacy exceptions
	 */
	private ExceptionsEditor privacyExceptionEditor;
	
	/**
	 * A slider to select the value of PUT number to use for the experiment
	 */
	private FloatingSliderPanel putSlider;
	
	/**
	 * An instance of the {@link ExceptionsEditor} to add utility exceptions
	 */
	private ExceptionsEditor utilityExceptionEditor;
	
	/**
	 * Creates a privacy settings action with the given selected dataset
	 * @param dataset The dataset for the experiment
	 */
	public SelectPrivacySettingsAction(Instances dataset) {
		this(dataset, DEFAULT_PUTNUMBER);
	}
	
	/**
	 * Creates a privacy settings action with the given selected dataset and initial PUT Number
	 * @param dataset The dataset for the experiment
	 * @param initialPUTNumber The initial PUT Number
	 */
	public SelectPrivacySettingsAction(Instances dataset, float initialPUTNumber) {
		super();
		attributeNames = new String[dataset.numAttributes() - 1];
		for(int i = 0; i < attributeNames.length; i++)
			attributeNames[i] = dataset.attribute(i).name();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0};
		setLayout(gridBagLayout);
		
		JPanel putNumberContainer = new JPanel();
		putNumberContainer.setOpaque(false);
		GridBagConstraints gbc_putNumberContainer = new GridBagConstraints();
		gbc_putNumberContainer.insets = new Insets(0, 0, 5, 0);
		gbc_putNumberContainer.fill = GridBagConstraints.BOTH;
		gbc_putNumberContainer.gridx = 0;
		gbc_putNumberContainer.gridy = 0;
		add(putNumberContainer, gbc_putNumberContainer);
		GridBagLayout gbl_putNumberContainer = new GridBagLayout();
		gbl_putNumberContainer.columnWeights = new double[]{1.0};
		gbl_putNumberContainer.rowWeights = new double[]{0.0, 1.0};
		putNumberContainer.setLayout(gbl_putNumberContainer);
		
		JLabel choosePUTLabel = new JLabel("<html><center><font size='4' color='#033e9e'>Choose Privacy-Utility Tradeoff number</font><br/><font size='3' color='#033e9e'><b>-1</b> implies maximum Privacy whereas <b>1</b> implies maximum Utility</font></center></html>");
		choosePUTLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_choosePUTLabel = new GridBagConstraints();
		gbc_choosePUTLabel.anchor = GridBagConstraints.NORTH;
		gbc_choosePUTLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_choosePUTLabel.insets = new Insets(5, 0, 10, 0);
		gbc_choosePUTLabel.gridx = 0;
		gbc_choosePUTLabel.gridy = 0;
		putNumberContainer.add(choosePUTLabel, gbc_choosePUTLabel);
		
		putSlider = new FloatingSliderPanel(-1f, 1f, initialPUTNumber) {
			protected void setDescriptionLabelText() {
				float currentValue = getCurrentValue();
				int numOfColumns = PUTExperiment.calculatePartitionSize(attributeNames.length, currentValue);
				String text = "Select " + numOfColumns + " attributes out of " + attributeNames.length;
				infoLabel.setText("<html><center><font size='4' color='#2d0c08'>" + text + "</font></center></html>");
			}

			@Override
			public void stateChanged(ChangeEvent e) {
				super.stateChanged(e);
				setupExceptionEditors();
			}
			
		};
		GridBagConstraints gbc_putSlider = new GridBagConstraints();
		gbc_putSlider.insets = new Insets(0, 0, 5, 0);
		gbc_putSlider.anchor = GridBagConstraints.NORTH;
		gbc_putSlider.fill = GridBagConstraints.BOTH;
		gbc_putSlider.gridx = 0;
		gbc_putSlider.gridy = 1;
		putNumberContainer.add(putSlider, gbc_putSlider);
		
		JSeparator horizontalSeparator = new JSeparator();
		GridBagConstraints gbc_horizontalSeparator = new GridBagConstraints();
		gbc_horizontalSeparator.fill = GridBagConstraints.HORIZONTAL;
		gbc_horizontalSeparator.gridx = 0;
		gbc_horizontalSeparator.gridy = 1;
		add(horizontalSeparator, gbc_horizontalSeparator);
		
		exceptionsContainer = new JPanel();
		exceptionsContainer.setOpaque(false);
		GridBagConstraints gbc_exceptionsContainer = new GridBagConstraints();
		gbc_exceptionsContainer.insets = new Insets(0, 0, 5, 0);
		gbc_exceptionsContainer.fill = GridBagConstraints.BOTH;
		gbc_exceptionsContainer.gridx = 0;
		gbc_exceptionsContainer.gridy = 2;
		add(exceptionsContainer, gbc_exceptionsContainer);
		GridBagLayout gbl_exceptionsContainer = new GridBagLayout();
		gbl_exceptionsContainer.columnWeights = new double[]{1.0, 0.0, 1.0};
		gbl_exceptionsContainer.rowWeights = new double[]{0.0, 1.0};
		exceptionsContainer.setLayout(gbl_exceptionsContainer);
		
		JLabel choosePrivacyExceptionLabel = new JLabel("<html><center><font size='4' color='#033e9e'>Select Privacy Exceptions</font><br/><font size='3' color='#033e9e'>Any combinations containing these attributes are ignored</font></center></html>");
		choosePrivacyExceptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_choosePrivacyExceptionLabel = new GridBagConstraints();
		gbc_choosePrivacyExceptionLabel.insets = new Insets(5, 0, 5, 0);
		gbc_choosePrivacyExceptionLabel.anchor = GridBagConstraints.NORTH;
		gbc_choosePrivacyExceptionLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_choosePrivacyExceptionLabel.gridx = 0;
		gbc_choosePrivacyExceptionLabel.gridy = 0;
		exceptionsContainer.add(choosePrivacyExceptionLabel, gbc_choosePrivacyExceptionLabel);
		
		JSeparator verticalSeparator = new JSeparator(SwingConstants.VERTICAL);
		GridBagConstraints gbc_verticalSeparator = new GridBagConstraints();
		gbc_verticalSeparator.fill = GridBagConstraints.VERTICAL;
		gbc_verticalSeparator.gridx = 1;
		gbc_verticalSeparator.gridy = 0;
		gbc_verticalSeparator.gridheight = 2;
		exceptionsContainer.add(verticalSeparator, gbc_verticalSeparator);		
		
		JLabel chooseUtilityExceptionsLabel = new JLabel("<html><center><font size='4' color='#033e9e'>Select Utility Exceptions</font><br/><font size='3' color='#033e9e'>Any combinations containing these attributes are explored</font></center></html>");
		chooseUtilityExceptionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_chooseUtilityExceptionsLabel = new GridBagConstraints();
		gbc_chooseUtilityExceptionsLabel.anchor = GridBagConstraints.NORTH;
		gbc_chooseUtilityExceptionsLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_chooseUtilityExceptionsLabel.insets = new Insets(5, 0, 5, 0);
		gbc_chooseUtilityExceptionsLabel.gridx = 2;
		gbc_chooseUtilityExceptionsLabel.gridy = 0;
		exceptionsContainer.add(chooseUtilityExceptionsLabel, gbc_chooseUtilityExceptionsLabel);
		
		setupExceptionEditors();
	}
	
	/**
	 * Returns a (possibly empty) set of privacy exceptions
	 * @return A {@link Set} of exceptions, each being a set itself of integer indices
	 */
	public Set<Set<Integer>> getPrivacyExceptions() {
		return privacyExceptionEditor.getExceptions();
	}
	
	/**
	 * Returns the currently selected value of PUT number
	 * @return The selected value for PUT number
	 */
	public float getPUTNumber() {
		return putSlider.getCurrentValue();
	}
	
	/**
	 * Returns a (possibly empty) set of utility exceptions
	 * @return A {@link Set} of exceptions, each being a set itself of integer indices
	 */
	public Set<Set<Integer>> getUtilityExceptions() {
		return utilityExceptionEditor.getExceptions();
	}
	
	@Override
	public void setInitialPreferences(Map<String, String> preferences) {
		if(preferences != null) {
			String putNumber = preferences.get(PUTExperiment.PUT_NUMBER_SWITCH);
			if(putNumber != null)
				putSlider.setCurrentValue(Float.parseFloat(putNumber.trim()));
			String privacyExceptions = preferences.get(PUTExperiment.PRIVACY_EXCEPTIONS_SWITCH);
			if(privacyExceptions != null) {
				Pattern p = Pattern.compile("\\[[^\\]]*\\]");
				Matcher m = p.matcher(privacyExceptions);
				while(m.find()) {
					String exceptionStr = m.group();
					if(exceptionStr != null) {
						exceptionStr = exceptionStr.replaceAll("\\s", "");	// Remove any extra spaces, if any
						Pattern p2 = Pattern.compile("(\\d)+");
						Matcher m2 = p2.matcher(exceptionStr);
						Set<Integer> exception = new TreeSet<Integer>();
						while(m2.find()) {
							Integer attribute = Integer.parseInt(m2.group());
							exception.add(attribute);
						}
						if(exception.size() > 0)
							privacyExceptionEditor.addException(exception);
					}
				}
			}
			String utilityExceptions = preferences.get(PUTExperiment.UTILITY_EXCEPTIONS_SWITCH);
			if(utilityExceptions != null) {
				Pattern p = Pattern.compile("\\[[^\\]]*\\]");
				Matcher m = p.matcher(utilityExceptions);
				while(m.find()) {
					String exceptionStr = m.group();
					if(exceptionStr != null) {
						exceptionStr = exceptionStr.replaceAll("\\s", "");	// Remove any extra spaces, if any
						Pattern p2 = Pattern.compile("(\\d)+");
						Matcher m2 = p2.matcher(exceptionStr);
						Set<Integer> exception = new TreeSet<Integer>();
						while(m2.find()) {
							Integer attribute = Integer.parseInt(m2.group());
							exception.add(attribute);
						}
						if(exception.size() > 0)
							utilityExceptionEditor.addException(exception);
					}
				}
			}
		}
	}

	/**
	 * Sets up the exception editors
	 */
	private void setupExceptionEditors() {
		if(privacyExceptionEditor != null)
			exceptionsContainer.remove(privacyExceptionEditor);
		privacyExceptionEditor = new ExceptionsEditor(attributeNames, PUTExperiment.calculatePartitionSize(attributeNames.length, putSlider.getCurrentValue()));
		GridBagConstraints gbc_privacyExceptionEditor = new GridBagConstraints();
		gbc_privacyExceptionEditor.anchor = GridBagConstraints.NORTH;
		gbc_privacyExceptionEditor.insets = new Insets(0, 0, 5, 0);
		gbc_privacyExceptionEditor.fill = GridBagConstraints.VERTICAL;
		gbc_privacyExceptionEditor.gridx = 0;
		gbc_privacyExceptionEditor.gridy = 1;
		exceptionsContainer.add(privacyExceptionEditor, gbc_privacyExceptionEditor);
		
		if(utilityExceptionEditor != null)
			exceptionsContainer.remove(utilityExceptionEditor);
		utilityExceptionEditor = new ExceptionsEditor(attributeNames, PUTExperiment.calculatePartitionSize(attributeNames.length, putSlider.getCurrentValue()));
		GridBagConstraints gbc_utilityExceptionEditor = new GridBagConstraints();
		gbc_utilityExceptionEditor.insets = new Insets(0, 0, 5, 0);
		gbc_utilityExceptionEditor.anchor = GridBagConstraints.NORTH;
		gbc_utilityExceptionEditor.fill = GridBagConstraints.VERTICAL;
		gbc_utilityExceptionEditor.gridx = 2;
		gbc_utilityExceptionEditor.gridy = 1;
		exceptionsContainer.add(utilityExceptionEditor, gbc_utilityExceptionEditor);
		
		revalidate();
	}
}
