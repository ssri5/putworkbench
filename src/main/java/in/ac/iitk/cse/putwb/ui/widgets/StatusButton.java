package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A widget to represent a particular tab. It can show an icon, and some text next to it.
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class StatusButton extends JPanel {

	/**
	 * The cursor to show when mouse is over the widget, and the widget is disabled
	 */
	private static Cursor DISABLED_CURSOR = Cursor.getDefaultCursor();
	
	/**
	 * The cursor to show when mouse is over the widget, and the widget is not disabled
	 */
	private static Cursor HOVER_CURSOR = new Cursor(Cursor.HAND_CURSOR);
	
	/**
	 * The color used to paint an overlay, which makes the status button look disabled
	 */
	private static Color OVERLAY_COLOR = new Color(255, 255, 255, 180);
	
	/**
	 * The usual background color of the widget
	 */
	private static Color REGULAR_BCK = new Color(245, 255, 250);
	
	/**
	 * The border to show if the button is not in the selected state
	 */
	private static RoundedLineBorder REGULAR_BORDER = new RoundedLineBorder(new Color(244, 182, 66), 3);
	
	/**
	 * The background color of the widget if it is in the selected state
	 */
	private static Color SELECTED_BCK = new Color(247, 247, 183);
	
	/**
	 * The border to show if the button is in the selected state
	 */
	private static RoundedLineBorder SELECTED_BORDER = new RoundedLineBorder(new Color(204, 76, 44), 3);
	
	/**
	 * A flag to tell if the widget is in disabled state or not
	 */
	private boolean disabled;
	
	/**
	 * A flag to tell if the widget is in selected state or not
	 */
	private boolean selected;
	
	/**
	 * The tooltip to show for this widget
	 */
	private String toolTip;
	
	/**
	 * Creates a status button with the given text and icon. The button is neither selected nor disabled
	 * @param displayText The text to display
	 * @param iconPicture The icon to display
	 */
	public StatusButton(String displayText, ImageIcon iconPicture) {
		this(displayText, iconPicture, false, false);
	}
	
	/**
	 * Creates a status button with the given text, icon and states (disabled and selected)
	 * @param displayText The text to display
	 * @param iconPicture The icon to display
	 * @param disabled If the widget is disabled from the start or not
	 * @param selected If the widget is selected from the start or not
	 */
	public StatusButton(String displayText, ImageIcon iconPicture, boolean disabled, boolean selected) {
	
		super();
		displayText = displayText.replaceAll("\n", "<br/>");
		JLabel text = new JLabel("<html><font size='3'>" + displayText + "</font></html>");
		text.setOpaque(false);
		text.setHorizontalAlignment(SwingConstants.LEFT);
		text.setVerticalAlignment(SwingConstants.CENTER);
		text.setForeground(Color.MAGENTA);
		
		JLabel icon = new JLabel(iconPicture);
		icon.setHorizontalAlignment(SwingConstants.RIGHT);
		icon.setOpaque(false);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{0.0, 1.0};
		gridBagLayout.rowWeights = new double[]{1.0};
		setLayout(gridBagLayout);
		
		GridBagConstraints gbc_lblIcon = new GridBagConstraints();
		gbc_lblIcon.insets = new Insets(2, 2, 2, 5);
		gbc_lblIcon.gridx = 0;
		gbc_lblIcon.gridy = 0;
		add(icon, gbc_lblIcon);
		
		GridBagConstraints gbc_lblText = new GridBagConstraints();
		gbc_lblText.insets = new Insets(2, 0, 2, 2);
		gbc_lblText.anchor = GridBagConstraints.WEST;
		gbc_lblText.gridx = 1;
		gbc_lblText.gridy = 0;
		add(text, gbc_lblText);

		toolTip = iconPicture.getDescription();
		
		setToolTipText(toolTip);
		setDisabled(disabled);
		setSelected(selected);
	}
	
	/**
	 * Returns whether the widget is in disabled state or not
	 * @return <code>true</code> if the widget is currently disabled, <code>false</code> otherwise
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Returns whether the widget is in selected state or not
	 * @return <code>true</code> if the widget is currently selected, <code>false</code> otherwise
	 */
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void paint(Graphics g) {
		if(selected) {
			setBackground(SELECTED_BCK);
		} else {
			setBackground(REGULAR_BCK);
		}
		super.paint(g);
		if(disabled) {
			Graphics g2 = g.create();
			g2.setColor(OVERLAY_COLOR);
			Insets borderInsets = getInsets();
			g2.fillRect(borderInsets.left+1, borderInsets.top+1, getWidth() - (borderInsets.left + borderInsets.right + 2),getHeight() - (borderInsets.top + borderInsets.bottom + 2));
			g2.dispose();
		}
	}

	/**
	 * Sets the disabled state of this widget
	 * @param disabled The disabled state to set
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		if(disabled) {
			setCursor(DISABLED_CURSOR);
			setSelected(false);
			setToolTipText(null);
		}
		else {
			setCursor(HOVER_CURSOR);
			setToolTipText(toolTip);
		}
		repaint();
	}

	/**
	 * Sets the selected state of this widget
	 * @param selected The selected state to set
	 */
	public void setSelected(boolean selected) {
		// if it is disabled, ignore !
		if(disabled)
			return;
		this.selected = selected;
		if(selected)
			setBorder(SELECTED_BORDER);
		else
			setBorder(REGULAR_BORDER);
		repaint();
	}
	
}
