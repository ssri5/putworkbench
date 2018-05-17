/**
 * 
 */
package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import in.ac.iitk.cse.putwb.ui.IconCreator;

/**
 * The Autopilot Button
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class AutopilotButton extends JPanel {

	/**
	 * Creates a new Autopilot button
	 */
	public AutopilotButton() {
		super();
		setOpaque(false);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setToolTipText("Select common settings and run the experiment");
		JLabel text = new JLabel("<html><font size='3' color='#033e9e'>Switch on<br/> Autopilot</font></html>");
		text.setOpaque(false);
		text.setHorizontalAlignment(SwingConstants.LEFT);
		text.setVerticalAlignment(SwingConstants.CENTER);
		
		JLabel icon = new JLabel(IconCreator.getIcon(IconCreator.PLANE_ICON_FILE));
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
	}

}
