/**
 * 
 */
package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import in.ac.iitk.cse.putwb.heuristic.BasicSettingsSuggestor;
import in.ac.iitk.cse.putwb.ui.IconCreator;

/**
 * A dialog to collect user preferences relating to the autopilot
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class AutoPilotPreferencesDialog extends JDialog {
	
	/**
	 * A map to keep the user preferences
	 */
	private Map<String, Object> preferencesMap;
	
	/**
	 * A flag to indicate if the user chose to start the autopilot or not
	 */
	private boolean start;
	
	/**
	 * Creates a dialog to take user preferences and consequently start the autopilot
	 * @param parent The parent component w.r.t. which this dialog will be centred
	 */
	public AutoPilotPreferencesDialog(Component parent) {
		start = false;
		preferencesMap = new HashMap<String, Object>();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setUndecorated(true);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.2, 0.1, 0.1, 0.1, 0.1, 0.4};
		getContentPane().setLayout(gridBagLayout);
		setSize(350, 300);
		
		JLabel bannerLabel = new JLabel("<html><font size='5' color='#033e9e'>Autopilot Preferences</font></html>", IconCreator.getIcon(IconCreator.PLANE_ICON_FILE), JLabel.LEFT);
		GridBagConstraints gbc_bannerLabel = new GridBagConstraints();
		gbc_bannerLabel.insets = new Insets(0, 0, 5, 0);
		gbc_bannerLabel.gridx = 0;
		gbc_bannerLabel.gridy = 0;
		getContentPane().add(bannerLabel, gbc_bannerLabel);
		
		JLabel infoLabel1 = new JLabel("May I know your preferences?");
		GridBagConstraints gbc_infoLabel1 = new GridBagConstraints();
		gbc_infoLabel1.insets = new Insets(0, 0, 5, 0);
		gbc_infoLabel1.gridx = 0;
		gbc_infoLabel1.gridy = 1;
		getContentPane().add(infoLabel1, gbc_infoLabel1);
		
		JComboBox<String> approachCombobox = new JComboBox<String>(new String[]{"Prefer privacy", "Prefer utility"});
		GridBagConstraints gbc_approachCombobox = new GridBagConstraints();
		gbc_approachCombobox.insets = new Insets(0, 0, 5, 0);
		gbc_approachCombobox.gridx = 0;
		gbc_approachCombobox.gridy = 2;
		getContentPane().add(approachCombobox, gbc_approachCombobox);
		
		JLabel infoLabel2 = new JLabel("How much time do you have?");
		GridBagConstraints gbc_infoLabel2 = new GridBagConstraints();
		gbc_infoLabel2.insets = new Insets(0, 0, 5, 0);
		gbc_infoLabel2.gridx = 0;
		gbc_infoLabel2.gridy = 3;
		getContentPane().add(infoLabel2, gbc_infoLabel2);
		
		JComboBox<String> timeCombobox = new JComboBox<String>(new String[]{"Make it quick", "Let's see what you have", "I have time"});
		timeCombobox.setSelectedIndex(1);
		GridBagConstraints gbc_timeCombobox = new GridBagConstraints();
		gbc_timeCombobox.insets = new Insets(0, 0, 5, 0);
		gbc_timeCombobox.gridx = 0;
		gbc_timeCombobox.gridy = 4;
		getContentPane().add(timeCombobox, gbc_timeCombobox);
		
		JPanel buttonsPanel = new JPanel();
		GridBagConstraints gbc_buttonsPanel = new GridBagConstraints();
		gbc_buttonsPanel.fill = GridBagConstraints.BOTH;
		gbc_buttonsPanel.gridx = 0;
		gbc_buttonsPanel.gridy = 5;
		getContentPane().add(buttonsPanel, gbc_buttonsPanel);
		GridBagLayout gbl_buttonsPanel = new GridBagLayout();
		gbl_buttonsPanel.columnWidths = new int[]{70, 70};
		gbl_buttonsPanel.rowHeights = new int[]{50};
		gbl_buttonsPanel.columnWeights = new double[]{1.0, 1.0};
		gbl_buttonsPanel.rowWeights = new double[]{1.0};
		buttonsPanel.setLayout(gbl_buttonsPanel);
		
		JButton startButton = new JButton("Go for it");
		GridBagConstraints gbc_startButton = new GridBagConstraints();
		gbc_startButton.anchor = GridBagConstraints.SOUTHEAST;
		gbc_startButton.insets = new Insets(0, 0, 10, 15);
		gbc_startButton.gridx = 0;
		gbc_startButton.gridy = 0;
		buttonsPanel.add(startButton, gbc_startButton);
		startButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int approach = (approachCombobox.getSelectedIndex() + 1);	// See the values of constants in BasicSettingsSuggestor 
				int waitingTime = (timeCombobox.getSelectedIndex() + 11);	// to understand these assignments
				
				preferencesMap.put(BasicSettingsSuggestor.APPROACH, approach);
				preferencesMap.put(BasicSettingsSuggestor.WAITING_TIME, waitingTime);
				
				start = true;
				AutoPilotPreferencesDialog.this.setVisible(false);
				AutoPilotPreferencesDialog.this.dispose();
			}
		});
		
		JButton cancelButton = new JButton("Never mind");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.insets = new Insets(0, 15, 10, 0);
		gbc_cancelButton.anchor = GridBagConstraints.SOUTHWEST;
		gbc_cancelButton.gridx = 1;
		gbc_cancelButton.gridy = 0;
		buttonsPanel.add(cancelButton, gbc_cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				preferencesMap.clear();
				start = false;
				AutoPilotPreferencesDialog.this.setVisible(false);
				AutoPilotPreferencesDialog.this.dispose();
			}
		});
	
		setLocationRelativeTo(parent);
	}
	
	/**
	 * Get the preferences that user chose for running the autopilot
	 * @return A {@link Map} of user selected preferences
	 */
	public Map<String, Object> getPreferences() {
		return preferencesMap;
	}
	
	/**
	 * Returns whether the user has chosen to start the autopilot or not
	 * @return <code>true</code> if the user chose to start the autopilot, <code>false</code> otherwise
	 */
	public boolean startAutopilot() {
		return start;
	}
	
}
