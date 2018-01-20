package in.ac.iitk.cse.putwb.ui.actions;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import in.ac.iitk.cse.putwb.experiment.PUTExperiment;
import in.ac.iitk.cse.putwb.ui.widgets.FloatingSliderPanel;
import weka.core.Instances;

/**
 * This action allows user to curtail the computational expense of the experiment, by setting values for horizontal and vertical expenses.
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class SetExpenseAction extends Action {
	
	/**
	 * The default value of horizontal expense
	 */
	private float DEFAULT_HORIZONTAL_EXPENSE = 1f;
	
	/**
	 * The default value of vertical expense
	 */
	private float DEFAULT_VERTICAL_EXPENSE = 1f;
	
	/**
	 * A slider to select horizontal expense
	 */
	private FloatingSliderPanel horizontalExpenseSlider;
	
	/**
	 * The number of data instances in the dataset
	 */
	private long numberOfRows;
	
	/**
	 * The number of attributes in the dataset (minus the class attribute)
	 */
	private int numOfAttributes;
	
	/**
	 * A link to the current privacy settings action; used to fetch the current value of PUT number
	 */
	private SelectPrivacySettingsAction privacyAction;
	
	/**
	 * The current value of vertical expense
	 */
	private JPanel verticalExpensePanel;
	
	/**
	 * A slider to select vertical expense
	 */
	private FloatingSliderPanel verticalExpenseSlider;
	
	/**
	 * Creates an instance of expense action for the given selected dataset and a link to the current privacy settings action
	 * @param dataset The selected dataset
	 * @param privacyAction The current privacy settings action
	 */
	public SetExpenseAction(Instances dataset, SelectPrivacySettingsAction privacyAction) {
		this.privacyAction = privacyAction;
		numOfAttributes = dataset.numAttributes() - 1;
		numberOfRows = dataset.numInstances();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 1.0};
		setLayout(gridBagLayout);
		
		verticalExpensePanel = new JPanel();
		verticalExpensePanel.setOpaque(false);
		GridBagConstraints gbc_verticalExpensePanel = new GridBagConstraints();
		gbc_verticalExpensePanel.fill = GridBagConstraints.BOTH;
		gbc_verticalExpensePanel.gridx = 0;
		gbc_verticalExpensePanel.gridy = 0;
		add(verticalExpensePanel, gbc_verticalExpensePanel);
		GridBagLayout gbl_verticalExpensePanel = new GridBagLayout();
		gbl_verticalExpensePanel.columnWeights = new double[]{1.0};
		gbl_verticalExpensePanel.rowWeights = new double[]{0.0, 0.0, 0.0};
		verticalExpensePanel.setLayout(gbl_verticalExpensePanel);
		
		setupVExpenseSlider();
		
		JLabel infoLabel1 = new JLabel("<html><center><font size='4' color='#033e9e'>Choose Vertical expense budget</font></center></html>");
		infoLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel1 = new GridBagConstraints();
		gbc_infoLabel1.fill = GridBagConstraints.BOTH;
		gbc_infoLabel1.anchor = GridBagConstraints.SOUTH;
		gbc_infoLabel1.insets = new Insets(0, 0, 10, 0);
		gbc_infoLabel1.gridx = 0;
		gbc_infoLabel1.gridy = 0;
		verticalExpensePanel.add(infoLabel1, gbc_infoLabel1);
		
		JLabel infoLabel2 = new JLabel("<html><center><font size='3' color='#033e9e'>Use this parameter to restrict the number of attribute combinations to try out</font></center></html>");
		infoLabel2.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel2 = new GridBagConstraints();
		gbc_infoLabel2.fill = GridBagConstraints.BOTH;
		gbc_infoLabel2.anchor = GridBagConstraints.NORTH;
		gbc_infoLabel2.insets = new Insets(10, 0, 0, 0);
		gbc_infoLabel2.gridx = 0;
		gbc_infoLabel2.gridy = 2;
		verticalExpensePanel.add(infoLabel2, gbc_infoLabel2);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 1;
		add(separator, gbc_separator);
		
		JPanel horizontalExpensePanel = new JPanel();
		horizontalExpensePanel.setOpaque(false);
		GridBagConstraints gbc_horizontalExpensePanel = new GridBagConstraints();
		gbc_horizontalExpensePanel.fill = GridBagConstraints.BOTH;
		gbc_horizontalExpensePanel.gridx = 0;
		gbc_horizontalExpensePanel.gridy = 2;
		add(horizontalExpensePanel, gbc_horizontalExpensePanel);
		GridBagLayout gbl_horizontalExpensePanel = new GridBagLayout();
		gbl_horizontalExpensePanel.columnWeights = new double[]{1.0};
		gbl_horizontalExpensePanel.rowWeights = new double[]{0.0, 0.0, 0.0};
		horizontalExpensePanel.setLayout(gbl_horizontalExpensePanel);
		
		JLabel infoLabel3 = new JLabel("<html><center><font size='4' color='#033e9e'>Choose Horizontal expense budget</font></center></html>");
		infoLabel3.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel3 = new GridBagConstraints();
		gbc_infoLabel3.fill = GridBagConstraints.BOTH;
		gbc_infoLabel3.anchor = GridBagConstraints.SOUTH;
		gbc_infoLabel3.insets = new Insets(0, 0, 10, 0);
		gbc_infoLabel3.gridx = 0;
		gbc_infoLabel3.gridy = 0;
		horizontalExpensePanel.add(infoLabel3, gbc_infoLabel3);
		
		horizontalExpenseSlider = new FloatingSliderPanel(0.0f, 1f, DEFAULT_HORIZONTAL_EXPENSE) {
			protected void setDescriptionLabelText() {
				float currentValue = getCurrentValue();
				long numOfRowsInFragmentedDataset = (long) (numberOfRows * currentValue);
				if(numOfRowsInFragmentedDataset == 0) {
					JOptionPane.showMessageDialog(null, "The number of rows cannot be 0. Resetting to default !!", "Error (0 rows)", JOptionPane.ERROR_MESSAGE);
					horizontalExpenseSlider.setCurrentValue(DEFAULT_HORIZONTAL_EXPENSE);
					return;
				}
				String text = "Use " + numOfRowsInFragmentedDataset + " rows out of " + numberOfRows;
				infoLabel.setText("<html><center><font size='4' color='#2d0c08'>" + text + "</font></center></html>");
			}
		};
		GridBagConstraints gbc_horizontalExpenseSlider = new GridBagConstraints();
		gbc_horizontalExpenseSlider.insets = new Insets(0, 10, 0, 10);
		gbc_horizontalExpenseSlider.fill = GridBagConstraints.BOTH;
		gbc_horizontalExpenseSlider.gridx = 0;
		gbc_horizontalExpenseSlider.gridy = 1;
		horizontalExpensePanel.add(horizontalExpenseSlider, gbc_horizontalExpenseSlider);
		
		JLabel infoLabel4 = new JLabel("<html><center><font size='3' color='#033e9e'>Use this parameter to restrict the number of tuples in each fragmented dataset</font></center></html>");
		infoLabel4.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel4 = new GridBagConstraints();
		gbc_infoLabel4.insets = new Insets(10, 0, 0, 0);
		gbc_infoLabel4.fill = GridBagConstraints.BOTH;
		gbc_infoLabel4.anchor = GridBagConstraints.NORTH;
		gbc_infoLabel4.gridx = 0;
		gbc_infoLabel4.gridy = 2;
		horizontalExpensePanel.add(infoLabel4, gbc_infoLabel4);
	}
	
	/**
	 * Returns the currently selected value of horizontal expense
	 * @return the horizontal expense
	 */
	public float getHorizontalExpense() {
		return horizontalExpenseSlider.getCurrentValue();
	}
	
	/**
	 * Returns the currently selected value of vertical expense
	 * @return the vertical expense
	 */
	public float getVerticalExpense() {
		return verticalExpenseSlider.getCurrentValue();
	}
	
	/**
	 * Resets the panel to reflect the correct current state
	 */
	public void resetVerticalExpenseInfo() {
		verticalExpenseSlider.setCurrentValue(verticalExpenseSlider.getCurrentValue());
	}
	
	@Override
	public void setInitialPreferences(Map<String, String> preferences) {
		if(preferences != null) {
			String vExpenseStr = preferences.get(PUTExperiment.V_EXPENSE_SWITCH);
			if(vExpenseStr != null)
				verticalExpenseSlider.setCurrentValue(Float.parseFloat(vExpenseStr.trim()));
			String hExpenseStr = preferences.get(PUTExperiment.H_EXPENSE_SWITCH);
			if(hExpenseStr != null)
				horizontalExpenseSlider.setCurrentValue(Float.parseFloat(hExpenseStr.trim()));
		}
	}
	
	/**
	 * Sets up the vertical expense slider
	 */
	private void setupVExpenseSlider() {
		if(verticalExpenseSlider != null)
			verticalExpensePanel.remove(verticalExpenseSlider);
		verticalExpenseSlider =  new FloatingSliderPanel(0.0f, 1f, DEFAULT_VERTICAL_EXPENSE) {
			protected void setDescriptionLabelText() {
				float currentValue = getCurrentValue();
				infoLabel.setText(setVerticalExpenseInfoLabel(currentValue));
			}
		};
		
		GridBagConstraints gbc_verticalExpenseSlider = new GridBagConstraints();
		gbc_verticalExpenseSlider.insets = new Insets(0, 10, 0, 10);
		gbc_verticalExpenseSlider.fill = GridBagConstraints.BOTH;
		gbc_verticalExpenseSlider.gridx = 0;
		gbc_verticalExpenseSlider.gridy = 1;
		verticalExpensePanel.add(verticalExpenseSlider, gbc_verticalExpenseSlider);
		
		revalidate();
	}

	/**
	 * Set the information label for vertical expense slider - the number of attribute combinations that will be tried for the selected value of vertical expense
	 * @param currentValue The current value of vertical expense
	 * @return a string that shows details of the number of attribute combinations that will be tried for the given vertical expense value
	 */
	private String setVerticalExpenseInfoLabel(float currentValue) {
		float putNumber = privacyAction.getPUTNumber();
		BigInteger totalCombinations = PUTExperiment.getNcKValue(numOfAttributes, PUTExperiment.calculatePartitionSize(numOfAttributes, putNumber));
		BigInteger numberOfCombinations = new BigDecimal(totalCombinations).multiply(new BigDecimal("" + currentValue)).toBigInteger();
		if(numberOfCombinations.compareTo(BigInteger.ZERO) == 0) {
			JOptionPane.showMessageDialog(null, "The number of attribute combinations cannot be 0. Resetting to default !!", "Error (0 combinations)", JOptionPane.ERROR_MESSAGE);
			verticalExpenseSlider.setCurrentValue(DEFAULT_VERTICAL_EXPENSE);
			numberOfCombinations = new BigDecimal(totalCombinations).multiply(new BigDecimal("" + DEFAULT_VERTICAL_EXPENSE)).toBigInteger();
		}
		String text = "Select " + numberOfCombinations + " attribute combinations out of " + totalCombinations + " (without considering any Privacy Exceptions)";
		return "<html><center><font size='4' color='#2d0c08'>" + text + "</font></center></html>";
	}

}
