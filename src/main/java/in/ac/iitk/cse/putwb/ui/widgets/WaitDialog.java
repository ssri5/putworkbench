/**
 * 
 */
package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * A dialog that show a waiting message while some processing occurs in the background
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class WaitDialog extends JDialog {
	
	/**
	 * A different thread (other than the invoking thread, that could be the UI thread as well) to open this dialog
	 */
	private Thread displayThread;
	
	/**
	 * A semaphore to avoid situations where a call to open and close the dialog are temporally very close, and are processed out of order
	 */
	private transient int semaphore;
	
	/**
	 * Creates a waiting dialog, centred w.r.t. the screem
	 */
	public WaitDialog() {
		this(null);
	}
	
	/**
	 * Creates a waiting dialog, centred w.r.t. a given parent component
	 * @param parent
	 */
	public WaitDialog(Component parent) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setUndecorated(true);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{40, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0};
		gridBagLayout.rowWeights = new double[]{1.0};
		getContentPane().setLayout(gridBagLayout);
		setSize(200, 50);
		
		ProgressAnimation analyzingAnimation = new ProgressAnimation(250, new Color(158, 21, 3));
		GridBagConstraints gbc_analyzingAnimation = new GridBagConstraints();
		gbc_analyzingAnimation.gridx = 0;
		gbc_analyzingAnimation.gridy = 0;
		gbc_analyzingAnimation.fill = GridBagConstraints.BOTH;
		getContentPane().add(analyzingAnimation, gbc_analyzingAnimation);

		JLabel waitingLabel = new JLabel("<html><font size='4' color='#033e9e'>Just a moment...</font></html>");
		GridBagConstraints gbc_waitingLabel = new GridBagConstraints();
		gbc_waitingLabel.gridx = 1;
		gbc_waitingLabel.gridy = 0;
		gbc_waitingLabel.insets = new Insets(0, 5, 0, 0);
		gbc_waitingLabel.anchor = GridBagConstraints.WEST;
		gbc_waitingLabel.fill = GridBagConstraints.BOTH;
		getContentPane().add(waitingLabel, gbc_waitingLabel);
		setLocationRelativeTo(parent);
		
		semaphore = 0;
	}
	
	/**
	 * Requests to close the current instance of the dialog
	 */
	public void close() {
		semaphore--;
		if(semaphore == 0) {
			semaphore = 0;
			setVisible(false);
		}
	}
	
	/**
	 * Requests to open the current instance of the dialog
	 */
	public void open() {
		semaphore++;
		if(semaphore > 0) {
			displayThread =	new Thread() {
				public void run() {
					setVisible(true);
				}
			};
			displayThread.setPriority(Thread.MAX_PRIORITY);
			displayThread.start();
			Thread.yield();
		}
	}
}
