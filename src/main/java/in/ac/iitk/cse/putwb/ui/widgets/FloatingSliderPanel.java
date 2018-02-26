package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A slider widget that can select floating point values
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class FloatingSliderPanel extends JPanel implements ChangeListener {
	
	/**
	 * The maximum precision (number of digits after the decimal point) that the slider can provide
	 */
	public static final int MAX_PRECISION = 6;
	
	/**
	 * Holds the current value of the slider at any point in time
	 */
	private float currentValue;
	
	/**
	 * Shows any additional custom information, if required
	 */
	protected JLabel infoLabel;
	
	/**
	 * The maximum value that the slider can take
	 */
	private float max;
	
	/**
	 * The minimum value that the slider can take
	 */
	private float min;
	
	/**
	 * The multiplier (10 raised to power precision) which is used to convert the floating value to map it to a corresponding integer range
	 */
	private int multiplier;

	/**
	 * The bare slider widget - can choose integer values only
	 */
	private JSlider slider;
	
	/**
	 * Shows the currently selected value
	 */
	private JLabel valueLabel;
	
	/**
	 * Creates a floating slider panel, with given low and high limits, with initial value set to the mid point of the range,
	 * and precision set to {@link #MAX_PRECISION}
	 * @param min The minimum selectable value on this slider
	 * @param max The maximum selectable value on this slider
	 */
	public FloatingSliderPanel(float min, float max) {
		this(min, max, (min + (max - min)/2));
	}

	/**
	 * Creates a floating slider panel, with given low and high limits, as well as the initial value of the slider,
	 * and precision set to {@link #MAX_PRECISION}
	 * @param min The minimum selectable value on this slider
	 * @param max The maximum selectable value on this slider
	 * @param initialValue The initial value
	 */
	public FloatingSliderPanel(float min, float max, float initialValue) {
		this(min, max, initialValue, MAX_PRECISION);
	}
	
	/**
	 * Creates a floating slider panel, with given low and high limits, the initial value of the slider as well as the required precision
	 * @param min The minimum selectable value on this slider
	 * @param max The maximum selectable value on this slider
	 * @param initialValue The initial value
	 * @param precision The precision (number of digits after the decimal point) that this slider provides. Must be between 0 and {@link #MAX_PRECISION}
	 */
	public FloatingSliderPanel(float min, float max, float initialValue, int precision) {
		super();
		if(precision < 0 || precision > MAX_PRECISION)
			throw new IllegalArgumentException("Precision must be set to a value between 0 and " + MAX_PRECISION);
		multiplier = (int)Math.pow(10, precision);
		this.min = min;
		this.max = max;
		setOpaque(false);
		currentValue = initialValue;
		float[] midValues = new float[]{(min + (max - min)/4), (min + (max - min)/2), (min + 3*(max - min)/4)};
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0};
		setLayout(gridBagLayout);
		
		slider = new JSlider(JSlider.HORIZONTAL, (int)(min * multiplier), (int)(max * multiplier), (int)(initialValue * multiplier));
		Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		labels.put((int)(min * multiplier), new JLabel("" + min));
		labels.put((int)(max * multiplier), new JLabel("" + max));
		for(float d : midValues)
			labels.put((int)(d*multiplier), new JLabel("" + d));
		slider.setLabelTable(labels);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		if(multiplier > 10)
			slider.setMajorTickSpacing(multiplier/10);
		if(multiplier > 100)
			slider.setMinorTickSpacing(multiplier/100);
		slider.addChangeListener(this);
		
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.fill = GridBagConstraints.HORIZONTAL;
		gbc_slider.insets = new Insets(5, 10, 0, 10);
		gbc_slider.gridx = 0;
		gbc_slider.gridy = 0;
		add(slider, gbc_slider);
		
		valueLabel = new JLabel();
		GridBagConstraints gbc_valueLabel = new GridBagConstraints();
		gbc_valueLabel.insets = new Insets(5, 0, 5, 0);
		gbc_valueLabel.gridx = 0;
		gbc_valueLabel.gridy = 1;
		add(valueLabel, gbc_valueLabel);
		
		infoLabel = new JLabel();
		infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel = new GridBagConstraints();
		gbc_infoLabel.fill = GridBagConstraints.BOTH;
		gbc_infoLabel.anchor = GridBagConstraints.NORTH;
		gbc_infoLabel.insets = new Insets(5, 10, 5, 10);
		gbc_infoLabel.gridx = 0;
		gbc_infoLabel.gridy = 2;
		add(infoLabel, gbc_infoLabel);
		
		setValueLabelText(currentValue);
	}
	
	/**
	 * Returns the current value of the slider
	 * @return the slider value
	 */
	public float getCurrentValue() {
		return currentValue;
	}

	/**
	 * Sets a value explicitly for this slider
	 * @param value The value to set
	 * @throws IllegalArgumentException if the value is out of range for this slider
	 */
	public void setCurrentValue(float value) {
		if(value < min || value > max)
			throw new IllegalArgumentException("The value must be between " + min + " and " + max);
		slider.setValue((int)(value*multiplier));
		setValueLabelText(value);
		slider.updateUI();
	}
	
	/**
	 * A method that can be overridden by sub-classes to set any message as the slider value changes
	 */
	protected void setDescriptionLabelText() {
		// Leave it for implementing custom behaviour
	}
	
	/**
	 * Sets the specified value in the value label
	 * @param value The value to set
	 */
	protected void setValueLabelText(float value) {
		String text = "" + value;
		valueLabel.setText("<html><center><b><font size='4' color='#9e1503'>" + text + "</font><center></html>");
		setDescriptionLabelText();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider)e.getSource();
		currentValue = slider.getValue() / (float)multiplier;
		setValueLabelText(currentValue);
	}
}
