package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JComponent;

/**
 * A widget that shown a "work in progress" animation - similar to a ticking clock
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class ProgressAnimation extends JComponent {
	
	/**
	 * A variable that decides which tick of the clock hand is shown at any point in time
	 */
	private volatile int turn;
	
	/**
	 * The color with which the animation is painted
	 */
	private Color paintColor;
	
	/**
	 * Creates a new progress animation with the given refresh time and color
	 * @param refreshTime The time in milliseconds, after which the animation goes to next state
	 * @param paintColor The color with which the animation is painted
	 */
	public ProgressAnimation(long refreshTime, Color paintColor) {
		this.paintColor = paintColor;
		setOpaque(false);
		turn = 0;
		Thread refreshThread = new Thread() {
			public void run() {
				try {
					while(true) {
						Thread.sleep(refreshTime);
						ProgressAnimation.this.repaint();
						turn = (turn + 1)%8;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		refreshThread.start();
	}
	
	/**
	 * Creates a progress animation with a refresh time of 500 milliseconds, painted in Black color
	 */
	public ProgressAnimation() {
		this(500, Color.BLACK);
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
		
		int radius = squareSide/2;
		
		Point endPoint = null;
		
		switch(turn) {
		case 0:
			endPoint = new Point(centre.x + radius + 2, centre.y);
			break;
		case 1:
			endPoint = new Point(centre.x + radius + 2, centre.y + radius + 2);
			break;
		case 2:
			endPoint = new Point(centre.x, centre.y + radius + 2);
			break;
		case 3:
			endPoint = new Point(centre.x - radius - 2, centre.y + radius + 2);
			break;
		case 4:
			endPoint = new Point(centre.x - radius - 2, centre.y);
			break;
		case 5:
			endPoint = new Point(centre.x - radius - 2, centre.y - radius - 2);
			break;
		case 6:
			endPoint = new Point(centre.x, centre.y - radius - 2);
			break;
		case 7:
			endPoint = new Point(centre.x + radius + 2, centre.y - radius - 2);
			break;
		}

		Ellipse2D outerCircle = new Ellipse2D.Float(squareBounds.x, squareBounds.y, squareBounds.width, squareBounds.height);
		Ellipse2D intermediateCircle = new Ellipse2D.Float(squareBounds.x+1, squareBounds.y+1, squareBounds.width-2, squareBounds.height-2);
		Ellipse2D innerCircle = new Ellipse2D.Float(squareBounds.x+2, squareBounds.y+2, squareBounds.width-4, squareBounds.height-4);
		Area ring = new Area(outerCircle);
		ring.subtract(new Area(innerCircle));
		
		Line2D line = new Line2D.Float(centre.x, centre.y, endPoint.x, endPoint.y);
		
		g2.setColor(paintColor);
		
		g2.setClip(intermediateCircle);
		g2.draw(line);
		g2.setClip(null);
		
		g2.fill(ring);
		
		g2.dispose();
	}
	
}
