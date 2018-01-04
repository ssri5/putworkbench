package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

/**
 * A widget that shows text similar to a hyperlink on a webpage
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class HyperlinkButton extends JLabel {
	
	/**
	 * The color constant for displaying the text when the mouse is over the widget
	 */
	private static final Color HOVER_COLOR = new Color(51, 102, 255);
	
	/**
	 * The color constant for displaying the text
	 */
	private static final Color NORMAL_COLOR = new Color(0, 0, 255);
	
	/**
	 * Creates a new hyperlink button with the given text
	 * @param text The text to show
	 */
	public HyperlinkButton(String text) {
		super();
		setOpaque(false);
		setText("<html>" + text + "</html>");
		setForeground(NORMAL_COLOR);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				setText("<html><u>" + text + "</u></html>");
				setForeground(HOVER_COLOR);
			}
			
			public void mouseExited(MouseEvent e) {
				setText("<html>" + text + "</html>");
				setForeground(NORMAL_COLOR);
			}
		});
	}
}
