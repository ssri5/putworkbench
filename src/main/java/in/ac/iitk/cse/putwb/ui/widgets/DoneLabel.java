package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * A widget used to show successful completion - sign of a '✔' 
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class DoneLabel extends JComponent {
	
	/**
	 * The color constant for painting the check sign
	 */
	private static Color CORRECT_SIGN_COLOR = new Color(62, 130, 54);
	
	/**
	 * Creates a Done Label
	 */
	public DoneLabel() {
		setOpaque(false);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)(g.create());
		
		Dimension size = getSize();
		int squareSide = 0;
		Rectangle squareBounds = null;
		if(size.width > size.height) {
			squareSide = size.height;
			squareBounds = new Rectangle((size.width - squareSide)/2, 0, squareSide, squareSide);
		} else {
			squareSide = size.width;
			squareBounds = new Rectangle(1, (size.height - squareSide)/2, squareSide, squareSide);
		}
		
		// Reduce square bounds slightly
		squareBounds.x += 2;
		squareBounds.y += 2;
		squareBounds.width -= 4;
		squareBounds.height -= 4;
		squareSide -= 4;
		
		Point centre = new Point(squareBounds.x + squareSide/2, squareBounds.y + squareSide/2);
		
		Ellipse2D outerCircle = new Ellipse2D.Float(squareBounds.x, squareBounds.y, squareBounds.width, squareBounds.height);
		Ellipse2D innerCircle = new Ellipse2D.Float(squareBounds.x+2, squareBounds.y+2, squareBounds.width-4, squareBounds.height-4);
		
		Area ring = new Area(outerCircle);
		ring.subtract(new Area(innerCircle));
		
		Rectangle2D outerRectangle = new Rectangle2D.Float(squareBounds.x+7, squareBounds.y+10, squareBounds.width-13, squareBounds.height-20);
		Rectangle2D innerRectangle = new Rectangle2D.Float(squareBounds.x+10, squareBounds.y+10, squareBounds.width-15, squareBounds.height-23);
		
		Area correctSign = new Area(outerRectangle);
		correctSign.subtract(new Area(innerRectangle));	
		g2.setColor(Color.BLACK);
		g2.fill(ring);
		
		g2.setColor(CORRECT_SIGN_COLOR);
		g2.rotate(Math.toRadians(-45), centre.x, centre.y);
		g2.fill(correctSign);
		
		g2.dispose();
	}
	
}
