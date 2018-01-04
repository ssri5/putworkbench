package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.LineBorder;

/**
 * A line border with extra round corners
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class RoundedLineBorder extends LineBorder {

	/**
	 * Creates a rounded line border with the given color and thickness
	 * @param color The border color
	 * @param thickness The border thickness
	 */
	public RoundedLineBorder(Color color, int thickness) {
		super(color, thickness, true);
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		if ((this.thickness > 0) && (g instanceof Graphics2D)) {
			Graphics2D g2d = (Graphics2D) g;

			Color oldColor = g2d.getColor();
			g2d.setColor(this.lineColor);

			Shape outer;
			Shape inner;

			int offs = this.thickness;
			int size = offs + offs;
			if (this.roundedCorners) {
				float arc = 5f * offs;
				outer = new RoundRectangle2D.Float(x, y, width, height, arc, arc);
				inner = new RoundRectangle2D.Float(x + offs, y + offs, width - size, height - size, arc, arc);
			}
			else {
				outer = new Rectangle2D.Float(x, y, width, height);
				inner = new Rectangle2D.Float(x + offs, y + offs, width - size, height - size);
			}
			Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
			path.append(outer, false);
			path.append(inner, false);
			g2d.fill(path);
			g2d.setColor(oldColor);
		}
	}

}
