package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * A widget that shows an icon 
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class IconButton extends JLabel {

	/**
	 * The cursor constant to show when the widget is disabled
	 */
	private static Cursor DISABLED_CURSOR = Cursor.getDefaultCursor();
	
	/**
	 * The cusror constant to show when the widget is not disabled
	 */
	private static Cursor HOVER_CURSOR = new Cursor(Cursor.HAND_CURSOR);
	
	/**
	 * The color constant to paint an overlay over the widget, if it is disabled
	 */
	private static Color OVERLAY_COLOR = new Color(255, 255, 255, 180);
	
	/**
	 * If <code>true</code>, the widget is in a disabled state
	 */
	private boolean disabled;
	
	/**
	 * Tooltip for this widget
	 */
	private String toolTip;
	
	/**
	 * Creates a new icon button with the given icon, tooltip text and initial state
	 * @param icon The icon to show
	 * @param toolTip Text to set as the tooltip for this button
	 * @param disabled If the button's initial state is disabled or not
	 */
	public IconButton(ImageIcon icon, String toolTip, boolean disabled) {
		super(icon);
		setOpaque(false);
		this.toolTip = toolTip;
		setToolTipText(toolTip);
		setDisabled(disabled);
	}
	
	/**
	 * Returns if this button is disabled or not 
	 * @return <code>true</code> if the button is in disabled state right now
	 */
	public boolean isDisabled() {
		return disabled;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(disabled) {
			Graphics g2 = g.create();
			g2.setColor(OVERLAY_COLOR);
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.dispose();
		}
	}
	
	/**
	 * Sets the disabled state of this button
	 * @param disabled The disabled state to set
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		if(disabled) {
			setCursor(DISABLED_CURSOR);
			setToolTipText(null);
		}
		else {
			setCursor(HOVER_CURSOR);
			setToolTipText(toolTip);
		}
		repaint();
	}
}
