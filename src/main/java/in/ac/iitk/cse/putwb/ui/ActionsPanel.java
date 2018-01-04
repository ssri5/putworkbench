package in.ac.iitk.cse.putwb.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import in.ac.iitk.cse.putwb.ui.actions.Action;
import in.ac.iitk.cse.putwb.ui.widgets.IconButton;
import in.ac.iitk.cse.putwb.ui.widgets.RoundedLineBorder;

/**
 * The content panel that displays various actions or steps involved (such as selecting inputs, running the tasks and seeing results etc.).
 * The panel also displays a set of buttons at the bottom for navigation.
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class ActionsPanel extends JPanel {

	/**
	 * The Back Button
	 */
	private IconButton backButton;
	
	/**
	 * Contains the current content shown in the panel area
	 */
	private Action content;
	
	/**
	 * The Fast Forward Button
	 */
	private IconButton fastForwardButton;
	
	/**
	 * The Forward Button
	 */
	private IconButton forwardButton;
	
	/**
	 * The Rewind Button
	 */
	private IconButton rewindButton;
	
	/**
	 * Creates a new Panel for showing actions
	 */
	public ActionsPanel() {
		super();
		MouseAdapter eventHandler = new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				IconButton source = (IconButton)e.getSource();
				if(!source.isDisabled())
					navigate(source);
			}
		};
		setBackground(Color.WHITE);
		setBorder(new EtchedBorder(EtchedBorder.RAISED, new Color(255, 218, 185), new Color(255, 228, 196)));
		setLayout(new BorderLayout());
		
		JPanel navigationPanel = new JPanel();
		navigationPanel.setOpaque(false);
		add(navigationPanel, BorderLayout.SOUTH);
		GridBagLayout gbl_navigationPanel = new GridBagLayout();
		gbl_navigationPanel.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0};
		gbl_navigationPanel.rowWeights = new double[]{0.0};
		navigationPanel.setLayout(gbl_navigationPanel);
		navigationPanel.setBorder(new RoundedLineBorder(Color.LIGHT_GRAY, 5));
		
		rewindButton = new IconButton(IconCreator.getIcon(IconCreator.REWIND_ICON_FILE), "Load a new dataset", true);
		GridBagConstraints gbc_rewindButton = new GridBagConstraints();
		gbc_rewindButton.insets = new Insets(5, 0, 5, 0);
		gbc_rewindButton.gridx = 0;
		gbc_rewindButton.gridy = 0;
		navigationPanel.add(rewindButton, gbc_rewindButton);
		rewindButton.addMouseListener(eventHandler);
		
		backButton = new IconButton(IconCreator.getIcon(IconCreator.BACK_ICON_FILE), "Back", true);
		GridBagConstraints gbc_backButton = new GridBagConstraints();
		gbc_backButton.insets = new Insets(5, 0, 5, 0);
		gbc_backButton.gridx = 1;
		gbc_backButton.gridy = 0;
		navigationPanel.add(backButton, gbc_backButton);
		backButton.addMouseListener(eventHandler);
		
		forwardButton = new IconButton(IconCreator.getIcon(IconCreator.FORWARD_ICON_FILE), "Forward", true);
		GridBagConstraints gbc_forwardButton = new GridBagConstraints();
		gbc_forwardButton.insets = new Insets(5, 0, 5, 0);
		gbc_forwardButton.gridx = 2;
		gbc_forwardButton.gridy = 0;
		navigationPanel.add(forwardButton, gbc_forwardButton);
		forwardButton.addMouseListener(eventHandler);
		
		fastForwardButton = new IconButton(IconCreator.getIcon(IconCreator.FAST_FORWARD_ICON_FILE), "Run experiments", true);
		GridBagConstraints gbc_fastForwardButton = new GridBagConstraints();
		gbc_fastForwardButton.insets = new Insets(5, 0, 0, 5);
		gbc_fastForwardButton.gridx = 3;
		gbc_fastForwardButton.gridy = 0;
		navigationPanel.add(fastForwardButton, gbc_fastForwardButton);
		fastForwardButton.addMouseListener(eventHandler);
	
		content = null;
	}

	/**
	 * Disables all the Navigation Buttons
	 */
	public void disableAll() {
		Field[] fields = IconButton.class.getDeclaredFields();
		for(Field field : fields) {
			if(field.getType().equals(IconButton.class)) {
				try {
					((IconButton)(field.get(this))).setDisabled(true);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns the Back button in this panel
	 * @return the back button
	 */
	public IconButton getBackButton() {
		return backButton;
	}

	/**
	 * Returns the Back button in this panel
	 * @return
	 */
	public JPanel getContent() {
		return content;
	}

	/**
	 * Returns the Fast Forward button in this panel
	 * @return the fast forward button
	 */
	public IconButton getFastForwardButton() {
		return fastForwardButton;
	}

	/**
	 * Returns the Forward button in this panel
	 * @return the forward button
	 */
	public IconButton getForwardButton() {
		return forwardButton;
	}

	/**
	 * Returns the Rewind button in this panel
	 * @return the rewind button
	 */
	public IconButton getRewindButton() {
		return rewindButton;
	}
	
	/**
	 * A callback methods that can be overridden for implementing custom behaviour 
	 * @param buttonPressed The button that was pressed
	 */
	protected void navigate(IconButton buttonPressed) {
		// Leave it for implementing custom behaviour
	}
	
	/**
	 * Sets the content to be shown in this panel. If there was any previous content, it is removed first, and then the new content is added.
	 * @param newContent The <code>Action<code> to show in this panel. 
	 */
	public void setContent(Action newContent) {
		if(content != null)
			remove(content);
		if(newContent != null) {
			content = newContent;
			add(newContent, BorderLayout.CENTER);
		}
		content.setMaximumSize(new Dimension(980, 700));
		updateUI();
	}
}
