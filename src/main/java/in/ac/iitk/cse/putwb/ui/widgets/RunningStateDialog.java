package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import in.ac.iitk.cse.putwb.experiment.PUTExperiment;


/**
 * This dialog shows the progress of various stages when the experiment is running.
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class RunningStateDialog extends JDialog {
	
	/**
	 * The progress animation widget for attribute combinations generation stage
	 */
	private ProgressAnimation attributeCombinationsAnimation;
	
	/**
	 * The progress animation widget for result export stage
	 */
	private ProgressAnimation dataWritingAnimation;
	
	/**
	 * 	The progress bar for result export stage
	 */
	private JProgressBar dataWritingProgressBar;
	
	/**
	 * The progress animation widget for learning stage
	 */
	private ProgressAnimation learningAnimation;

	/**
	 * The progress bar for learning stage
	 */
	private JProgressBar learningProgressBar;
	
	/**
	 * The color used to paint an overlay, which makes a widget look disabled
	 */
	private final Color OVERLAY_COLOR = new Color(255, 255, 255, 200);
	
	/**
	 * The progress bar for dataset partitions generation stage
	 */
	private JProgressBar partitioningProgressBar;
	
	/**
	 * The progress animation widget for dataset partitions generation stage
	 */
	private ProgressAnimation partitionsAnimation;
	
	/**
	 * A flag to signal that all the stages completed successfully
	 */
	private boolean tasksCompleted = false;
	
	/**
	 * A flag to signal that the attributes combinations generation stage has completed, and now the dataset partitioning and learning widgets can be enabled
	 */
	private boolean tasksInfoEnabled;
	
	/**
	 * A flag to signal that the learning stage has completed, and now the result export widgets can be enabled
	 */
	private boolean writingInfoEnabled; 
	
	/**
	 * Creates a new running state depiction dialog with a given {@link PUTExperiment} instance and the parent frame 
	 * @param experiment The {@link PUTExperiment} that this dialog is associated with
	 * @param parent The parent workbench window
	 */
	public RunningStateDialog(PUTExperiment experiment, Component parent) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setUndecorated(true);
		experiment.startExperimentAsync();
		setTitle("Running experiments...");
		tasksInfoEnabled = false;
		writingInfoEnabled = false;
		setSize(500, 350);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 15, 5, 5));
		contentPanel.setOpaque(false);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{40, 0};
		gbl_contentPanel.rowHeights = new int[]{40, 0, 40, 0, 40, 0, 40, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		contentPanel.setLayout(gbl_contentPanel);
		
		attributeCombinationsAnimation = new ProgressAnimation(500, new Color(158, 21, 3));
		GridBagConstraints gbc_attributeCombinationsAnimation = new GridBagConstraints();
		gbc_attributeCombinationsAnimation.gridheight = 2;
		gbc_attributeCombinationsAnimation.insets = new Insets(0, 0, 0, 5);
		gbc_attributeCombinationsAnimation.fill = GridBagConstraints.BOTH;
		gbc_attributeCombinationsAnimation.gridx = 0;
		gbc_attributeCombinationsAnimation.gridy = 0;
		contentPanel.add(attributeCombinationsAnimation, gbc_attributeCombinationsAnimation);
		
		JLabel infoLabel1 = new JLabel("<html><font size='4' color='#033e9e'>Generating attribute combinations</font></html>");
		infoLabel1.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_infoLabel1 = new GridBagConstraints();
		gbc_infoLabel1.insets = new Insets(0, 5, 0, 0);
		gbc_infoLabel1.fill = GridBagConstraints.BOTH;
		gbc_infoLabel1.anchor = GridBagConstraints.WEST;
		gbc_infoLabel1.gridx = 1;
		gbc_infoLabel1.gridy = 0;
		contentPanel.add(infoLabel1, gbc_infoLabel1);
		
		JLabel numberOfDatasetsLabel = new JLabel("<html><font size='3' color='#2d0c08'>Number of datasets:</font>&nbsp;<i>Calculating</i></html>");
		numberOfDatasetsLabel.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_numberOfDatasetsLabel = new GridBagConstraints();
		gbc_numberOfDatasetsLabel.fill = GridBagConstraints.BOTH;
		gbc_numberOfDatasetsLabel.anchor = GridBagConstraints.WEST;
		gbc_numberOfDatasetsLabel.insets = new Insets(0, 5, 0, 0);
		gbc_numberOfDatasetsLabel.gridx = 1;
		gbc_numberOfDatasetsLabel.gridy = 1;
		contentPanel.add(numberOfDatasetsLabel, gbc_numberOfDatasetsLabel);
		
		partitionsAnimation = new ProgressAnimation(500, new Color(158, 21, 3));
		GridBagConstraints gbc_partitionsAnimation = new GridBagConstraints();
		gbc_partitionsAnimation.gridheight = 2;
		gbc_partitionsAnimation.insets = new Insets(0, 0, 0, 5);
		gbc_partitionsAnimation.fill = GridBagConstraints.BOTH;
		gbc_partitionsAnimation.gridx = 0;
		gbc_partitionsAnimation.gridy = 2;
		contentPanel.add(partitionsAnimation, gbc_partitionsAnimation);
		
		JLabel infoLabel2 = new JLabel("<html><font size='4' color='#dee9fc'>Creating partitions of the dataset for learning</font></html>");
		infoLabel2.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_infoLabel2 = new GridBagConstraints();
		gbc_infoLabel2.fill = GridBagConstraints.BOTH;
		gbc_infoLabel2.anchor = GridBagConstraints.WEST;
		gbc_infoLabel2.insets = new Insets(0, 5, 0, 0);
		gbc_infoLabel2.gridx = 1;
		gbc_infoLabel2.gridy = 2;
		contentPanel.add(infoLabel2, gbc_infoLabel2);
		
		partitioningProgressBar = new JProgressBar() {

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				drawOverlay(g, getSize());
			}
		};
		GridBagConstraints gbc_partitioningProgressBar = new GridBagConstraints();
		gbc_partitioningProgressBar.anchor = GridBagConstraints.WEST;
		gbc_partitioningProgressBar.fill = GridBagConstraints.BOTH;
		gbc_partitioningProgressBar.insets = new Insets(0, 5, 0, 30);
		gbc_partitioningProgressBar.gridx = 1;
		gbc_partitioningProgressBar.gridy = 3;
		contentPanel.add(partitioningProgressBar, gbc_partitioningProgressBar);
		
		learningAnimation = new ProgressAnimation(500, new Color(158, 21, 3));
		GridBagConstraints gbc_learningAnimation = new GridBagConstraints();
		gbc_learningAnimation.gridheight = 2;
		gbc_learningAnimation.insets = new Insets(0, 0, 0, 5);
		gbc_learningAnimation.fill = GridBagConstraints.BOTH;
		gbc_learningAnimation.gridx = 0;
		gbc_learningAnimation.gridy = 4;
		contentPanel.add(learningAnimation, gbc_learningAnimation);
		
		JLabel infoLabel3 = new JLabel("<html><font size='4' color='#dee9fc'>Running learning tasks over partitioned datasets</font></html>");
		infoLabel3.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_infoLabel3 = new GridBagConstraints();
		gbc_infoLabel3.fill = GridBagConstraints.BOTH;
		gbc_infoLabel3.anchor = GridBagConstraints.WEST;
		gbc_infoLabel3.insets = new Insets(0, 5, 0, 0);
		gbc_infoLabel3.gridx = 1;
		gbc_infoLabel3.gridy = 4;
		contentPanel.add(infoLabel3, gbc_infoLabel3);
		
		learningProgressBar = new JProgressBar() {

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				drawOverlay(g, getSize());
			}
		};
		GridBagConstraints gbc_learningProgressBar = new GridBagConstraints();
		gbc_learningProgressBar.insets = new Insets(0, 5, 0, 30);
		gbc_learningProgressBar.fill = GridBagConstraints.BOTH;
		gbc_learningProgressBar.anchor = GridBagConstraints.WEST;
		gbc_learningProgressBar.gridx = 1;
		gbc_learningProgressBar.gridy = 5;
		contentPanel.add(learningProgressBar, gbc_learningProgressBar);
		
		dataWritingAnimation = new ProgressAnimation(500, new Color(158, 21, 3));
		GridBagConstraints gbc_dataWritingAnimation = new GridBagConstraints();
		gbc_dataWritingAnimation.gridheight = 2;
		gbc_dataWritingAnimation.insets = new Insets(0, 0, 0, 5);
		gbc_dataWritingAnimation.fill = GridBagConstraints.BOTH;
		gbc_dataWritingAnimation.gridx = 0;
		gbc_dataWritingAnimation.gridy = 6;
		contentPanel.add(dataWritingAnimation, gbc_dataWritingAnimation);
		
		JLabel infoLabel4 = new JLabel("<html><font size='4' color='#dee9fc'>Writing results to output file</font></html>");
		infoLabel3.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_infoLabel4 = new GridBagConstraints();
		gbc_infoLabel4.fill = GridBagConstraints.BOTH;
		gbc_infoLabel4.anchor = GridBagConstraints.WEST;
		gbc_infoLabel4.insets = new Insets(0, 5, 0, 0);
		gbc_infoLabel4.gridx = 1;
		gbc_infoLabel4.gridy = 6;
		contentPanel.add(infoLabel4, gbc_infoLabel4);
		
		dataWritingProgressBar = new JProgressBar() {

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				drawOverlay(g, getSize());
			}
		};
		GridBagConstraints gbc_dataWritingProgressBar = new GridBagConstraints();
		gbc_dataWritingProgressBar.insets = new Insets(0, 5, 0, 30);
		gbc_dataWritingProgressBar.fill = GridBagConstraints.BOTH;
		gbc_dataWritingProgressBar.anchor = GridBagConstraints.WEST;
		gbc_dataWritingProgressBar.gridx = 1;
		gbc_dataWritingProgressBar.gridy = 7;
		contentPanel.add(dataWritingProgressBar, gbc_dataWritingProgressBar);
		
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		GridBagLayout gbl_buttonPane = new GridBagLayout();
		gbl_buttonPane.columnWeights = new double[]{0.0};
		gbl_buttonPane.rowWeights = new double[]{0.0};
		buttonPane.setLayout(gbl_buttonPane);
		buttonPane.setOpaque(false);
		
		JButton abortButton = new JButton("Abort");
		GridBagConstraints gbc_stopButton = new GridBagConstraints();
		gbc_stopButton.insets = new Insets(0, 0, 20, 0);
		gbc_stopButton.gridx = 0;
		gbc_stopButton.gridy = 0;
		buttonPane.add(abortButton, gbc_stopButton);
		
		partitionsAnimation.setVisible(false);
		partitioningProgressBar.setStringPainted(true);
	
		learningAnimation.setVisible(false);
		learningProgressBar.setStringPainted(true);
	
		dataWritingAnimation.setVisible(false);
		dataWritingProgressBar.setStringPainted(true);
		
		Thread monitoringThread = new Thread() {

			@Override
			public void run() {
				long totalTasks = -1;
				while(true) {
					try {
						Thread.sleep(200);
						totalTasks = experiment.getTotalTasks();
						break;
					} catch (IllegalStateException e) {
						// Try again
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				numberOfDatasetsLabel.setText("<html><font size='3' color='#2d0c08'>Number of datasets:</font>&nbsp;<i>" + totalTasks + "</i></html>");
				
				while(true) {
					try {
						Thread.sleep(1000);
						long datasetsPartitioned = experiment.getNumOfPartitionedDatasets();
						// If we are here, enable the task indicators
						if(!tasksInfoEnabled) {
							tasksInfoEnabled = true;
							contentPanel.remove(attributeCombinationsAnimation);
							contentPanel.add(new DoneLabel(), gbc_attributeCombinationsAnimation);
							partitionsAnimation.setVisible(true);
							learningAnimation.setVisible(true);
							infoLabel2.setText("<html><font size='4' color='#033e9e'>Creating partitions of the dataset for learning</font></html>");
							infoLabel3.setText("<html><font size='4' color='#033e9e'>Running learning tasks over partitioned datasets</font></html>");
							
							partitioningProgressBar.setMinimum(0);
							learningProgressBar.setMinimum(0);
							partitioningProgressBar.setMaximum((int)totalTasks);
							
							learningProgressBar.setMaximum((int)totalTasks);
							partitioningProgressBar.setStringPainted(true);
							learningProgressBar.setStringPainted(true);
							
							contentPanel.revalidate();
						}
						long learningTasksCompleted = experiment.getNumOfTasksCompleted();
						long dataWritingTasksCompleted = 0;
						
						partitioningProgressBar.setValue((int)datasetsPartitioned);
						learningProgressBar.setValue((int)learningTasksCompleted);
	
						if(datasetsPartitioned == totalTasks) {
							contentPanel.remove(partitionsAnimation);
							contentPanel.add(new DoneLabel(), gbc_partitionsAnimation);
							contentPanel.revalidate();
						}
						
						if(learningTasksCompleted == totalTasks) {
							contentPanel.remove(learningAnimation);
							contentPanel.add(new DoneLabel(), gbc_learningAnimation);
							writingInfoEnabled = true;
							dataWritingProgressBar.setMaximum((int)totalTasks);
							dataWritingProgressBar.setStringPainted(true);
							contentPanel.revalidate();
						}
						
						if(writingInfoEnabled) {
							dataWritingTasksCompleted = experiment.getNumOfResultsWrittenToFile();
							dataWritingProgressBar.setValue((int)dataWritingTasksCompleted);
							infoLabel4.setText("<html><font size='4' color='#033e9e'>Writing results to output file</font></html>");
							contentPanel.revalidate();
						}
						
						if(dataWritingTasksCompleted == totalTasks) {
							dataWritingProgressBar.setValue((int)totalTasks);
							contentPanel.remove(dataWritingAnimation);
							contentPanel.add(new DoneLabel(), gbc_dataWritingAnimation);
							contentPanel.revalidate();
							// Just wait for half a second for any data flush
							Thread.sleep(500);
							tasksCompleted = true;
							RunningStateDialog.this.setVisible(false);
							RunningStateDialog.this.dispose();
							System.out.println("Learning tasks finished");
							contentPanel.revalidate();
							break;
						}
						
					} catch (IllegalStateException e) {
						// Try again
					} catch (InterruptedException e) {
						// Do nothing
					}
				}
			}
		};
		monitoringThread.start();
		
		abortButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to cancel the learning tasks?", "Confirm cancel", JOptionPane.YES_NO_OPTION);
				if(choice == JOptionPane.YES_OPTION) {
					monitoringThread.interrupt();
					experiment.stopExperiment();
					RunningStateDialog.this.setVisible(false);
					RunningStateDialog.this.dispose();
					System.out.println("Learning tasks aborted");
				}
			}
		});
		
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	/**
	 * Draws an overlay over a given area using the given graphics context
	 * @param g Graphics context
	 * @param size Overlay size (the overlay is drawn in the <code>Rectangle(0, 0, size.width, size.height)</code>
	 */
	private void drawOverlay(Graphics g, Dimension size) {
		if(tasksInfoEnabled)
			return;
		Graphics2D g2 = (Graphics2D)(g.create());
		g2.setColor(OVERLAY_COLOR);
		g2.fillRect(0, 0, size.width, size.height);
		g2.dispose();
	}
	
	/**
	 * Returns if the all the stages completed without interruption or not
	 * @return <code>true</code> if all the stages completed successfully, <code>false</code> otherwise
	 */
	public boolean isTasksCompleted() {
		return tasksCompleted;
	}
}
