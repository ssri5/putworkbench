package in.ac.iitk.cse.putwb.ui.actions;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import in.ac.iitk.cse.putwb.experiment.Stats;
import in.ac.iitk.cse.putwb.ui.ArchiveManager;
import in.ac.iitk.cse.putwb.ui.widgets.BarChartWidget;
import in.ac.iitk.cse.putwb.ui.widgets.HyperlinkButton;
import in.ac.iitk.cse.putwb.ui.widgets.RoundedLineBorder;

/**
 * The Results and Analysis action provides user with the results of a successfully completed experiment. 
 * It provides options to see the results of the learning tasks sorted by different parameters and ordering.
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class AnalyzeAction extends Action implements ItemListener {
	
	/**
	 * This array is used to provide different metrics for class specific sorting
	 */
	private static String[] classSpecificSortOptions = new String[]{"True Positive Rate", "False Positive Rate", "False Negative Rate", "Precision", "Recall", "Area under RO Curve", "Area under PR Curve"};
	
	/**
	 * The preference constant for setting ordering criteria
	 */
	public static final String ORDER_PREFERENCE = "Order_Preference";
	
	/**
	 * The preference constant for setting selected class
	 */
	public static final String SELECTED_CLASS_PREFERENCE = "Selected_Class";
	
	/**
	 * The preference constant for setting selected metric
	 */
	public static final String SELECTED_METRIC_PREFERENCE = "Selected_Metric";
	
	/**
	 * The preference constant for setting sort criteria
	 */
	public static final String SORT_CRITERIA_PREFERENCE = "Sort_Criteria";
	
	/**
	 * This array is used to provide different parameters for sorting results
	 */
	private static String[] sortOptions = new String[]{"Dictionary Sequence", "Accuracy", "Class Specific Metrics"};
	
	/**
	 * Returns a textual representation of a metric
	 * @param metricType The constant for the metric
	 * @return a textual representation for the metric, or null if no such metric is found
	 */
	public static String getDisplayNameForMetric(short metricType) {
		if(metricType >= 10 && metricType < 20)	// Class-specific metric
			return classSpecificSortOptions[metricType-10];
		else if(metricType < 10)
			return "Accuracy";
		else 
			return null;
	}
	
	/**
	 * A list that holds all the statistics related to the current experiment
	 */
	private List<Stats> allStats;
	
	/**
	 * A widget for performing attribute filtering
	 */
	private AttributeFilterAction attributeFilterAction;
	
	/**
	 * An array of the names of the attributes that the dataset has (minus the class attribute)
	 */
	private String[] attributeNames;
	
	/**
	 * The selection widget for selecting the class for class specific metric sorting
	 */
	private JComboBox<String> classSelection;
	
	/**
	 * A flag indicating that the class specifix metrics widgets be enabled
	 */
	private boolean enableClassSortingSection = false;
	
	/**
	 * The list of stats that are visible to the user at any point in time
	 */
	private List<Stats> liveStats;
	
	/**
	 * The selection widget for selecting the metric for class specific metric sorting
	 */
	private JComboBox<String> metricSelection;
	
	/**
	 * The selection widget for ordering (ascending or descending)
	 */
	private JComboBox<String> orderSelection;
	
	/**
	 * The color used to paint an overlay, which makes a widget look disabled
	 */
	private final Color OVERLAY_COLOR = new Color(255, 255, 255, 200);

	/**
	 * The widget that shows the results in the form of bar charts
	 */
	private BarChartWidget plotWidget;
	
	/**
	 * The selection widget for selecting the parameter for sorting the results
	 */
	private JComboBox<String> sortCriterionSelection;
	
	/**
	 * Creates a new results and analysis action panel for the given results file, attributes and classes
	 * @param resultFile The result file to parse
	 * @param attributeNames The names of the attributes in the results
	 * @param allClasses The {@link List} of classes
	 * @param datasetFile The dataset in use
	 * @param preferencesFile The preferences file for the current experiment
	 */
	public AnalyzeAction(File resultFile, String[] attributeNames, List<String> allClasses, File datasetFile, File preferencesFile) {
		this.attributeNames = attributeNames;
		allStats = null;
		try {
			allStats = Stats.readStatsFile(resultFile, allClasses.size());
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Cannot open file - " + resultFile.toString(), "Error...", JOptionPane.ERROR);
			e.printStackTrace();
			throw new IllegalStateException("Illegal Data file");
		}
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{100, 0};
		gridBagLayout.columnWeights = new double[]{0.2, 0.8};
		gridBagLayout.rowWeights = new double[]{0.1, 0.1, 0.8};
		setLayout(gridBagLayout);
		
		JPanel saveSection = new JPanel();
		saveSection.setBorder(new TitledBorder(new RoundedLineBorder(Color.LIGHT_GRAY, 2), "Save Experiment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		saveSection.setOpaque(false);
		GridBagConstraints gbc_saveSection = new GridBagConstraints();
		gbc_saveSection.insets = new Insets(5, 0, 5, 5);
		gbc_saveSection.fill = GridBagConstraints.BOTH;
		gbc_saveSection.gridx = 0;
		gbc_saveSection.gridy = 0;
		add(saveSection, gbc_saveSection);
		saveSection.setLayout(new GridLayout(1, 2, 5, 5));
		
		JPanel sortSection = new JPanel();
		sortSection.setBorder(new TitledBorder(new RoundedLineBorder(Color.LIGHT_GRAY, 2), "Sort Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		sortSection.setOpaque(false);
		GridBagConstraints gbc_sortSection = new GridBagConstraints();
		gbc_sortSection.insets = new Insets(0, 0, 5, 5);
		gbc_sortSection.fill = GridBagConstraints.BOTH;
		gbc_sortSection.gridx = 0;
		gbc_sortSection.gridy = 1;
		add(sortSection, gbc_sortSection);
		GridBagLayout gbl_sortSection = new GridBagLayout();
		gbl_sortSection.columnWeights = new double[]{0.0, 0.0};
		gbl_sortSection.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		sortSection.setLayout(gbl_sortSection);
		
		JLabel infoLabel1 = new JLabel("<html><b><font size='4' color='#033e9e'>Sort the results</font></b></html>");
		infoLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel1 = new GridBagConstraints();
		gbc_infoLabel1.gridwidth = 2;
		gbc_infoLabel1.fill = GridBagConstraints.HORIZONTAL;
		gbc_infoLabel1.anchor = GridBagConstraints.NORTH;
		gbc_infoLabel1.insets = new Insets(5, 0, 5, 0);
		gbc_infoLabel1.gridx = 0;
		gbc_infoLabel1.gridy = 0;
		sortSection.add(infoLabel1, gbc_infoLabel1);
		
		JLabel infoLabel2 = new JLabel("in");
		GridBagConstraints gbc_infoLabel2 = new GridBagConstraints();
		gbc_infoLabel2.fill = GridBagConstraints.HORIZONTAL;
		gbc_infoLabel2.anchor = GridBagConstraints.NORTHEAST;
		gbc_infoLabel2.insets = new Insets(10, 0, 0, 5);
		gbc_infoLabel2.gridx = 0;
		gbc_infoLabel2.gridy = 1;
		sortSection.add(infoLabel2, gbc_infoLabel2);
		
		orderSelection = new JComboBox<String>(new String[]{"ascending order", "descending order"});
		orderSelection.setSelectedIndex(1);
		GridBagConstraints gbc_orderSelection = new GridBagConstraints();
		gbc_orderSelection.insets = new Insets(5, 5, 5, 0);
		gbc_orderSelection.anchor = GridBagConstraints.NORTHWEST;
		gbc_orderSelection.gridx = 1;
		gbc_orderSelection.gridy = 1;
		sortSection.add(orderSelection, gbc_orderSelection);
		orderSelection.addItemListener(this);
		
		JLabel infoLabel3 = new JLabel("by");
		infoLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_infoLabel3 = new GridBagConstraints();
		gbc_infoLabel3.insets = new Insets(5, 0, 5, 5);
		gbc_infoLabel3.fill = GridBagConstraints.HORIZONTAL;
		gbc_infoLabel3.anchor = GridBagConstraints.NORTHEAST;
		gbc_infoLabel3.gridx = 0;
		gbc_infoLabel3.gridy = 2;
		sortSection.add(infoLabel3, gbc_infoLabel3);
		
		sortCriterionSelection = new JComboBox<String>(sortOptions);
		sortCriterionSelection.setSelectedItem(sortOptions[1]);
		GridBagConstraints gbc_sortCriterionSelection = new GridBagConstraints();
		gbc_sortCriterionSelection.insets = new Insets(0, 5, 5, 0);
		gbc_sortCriterionSelection.anchor = GridBagConstraints.NORTHWEST;
		gbc_sortCriterionSelection.gridx = 1;
		gbc_sortCriterionSelection.gridy = 2;
		sortSection.add(sortCriterionSelection, gbc_sortCriterionSelection);
		sortCriterionSelection.addItemListener(this);
		
		JLabel infoLabel4 = new JLabel("select class") {
			public void paint(Graphics g) {
				super.paint(g);
				drawOverlay(g, getSize());
			}
		};
		infoLabel4.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_infoLabel4 = new GridBagConstraints();
		gbc_infoLabel4.insets = new Insets(5, 5, 0, 5);
		gbc_infoLabel4.fill = GridBagConstraints.HORIZONTAL;
		gbc_infoLabel4.anchor = GridBagConstraints.NORTHWEST;
		gbc_infoLabel4.gridx = 1;
		gbc_infoLabel4.gridy = 3;
		sortSection.add(infoLabel4, gbc_infoLabel4);
		
		classSelection = new JComboBox<String>(allClasses.toArray(new String[allClasses.size()]));
		classSelection.setSelectedItem(attributeNames[0]);
		GridBagConstraints gbc_attributeSelection = new GridBagConstraints();
		gbc_attributeSelection.insets = new Insets(5, 5, 5, 5);
		gbc_attributeSelection.anchor = GridBagConstraints.NORTHWEST;
		gbc_attributeSelection.gridx = 1;
		gbc_attributeSelection.gridy = 4;
		sortSection.add(classSelection, gbc_attributeSelection);
		classSelection.addItemListener(this);
		classSelection.setEnabled(false);
		
		JLabel infoLabel5 = new JLabel("select metric") {
			public void paint(Graphics g) {
				super.paint(g);
				drawOverlay(g, getSize());
			}
		};
		infoLabel5.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_infoLabel5 = new GridBagConstraints();
		gbc_infoLabel5.insets = new Insets(5, 5, 0, 5);
		gbc_infoLabel5.fill = GridBagConstraints.HORIZONTAL;
		gbc_infoLabel5.anchor = GridBagConstraints.NORTHWEST;
		gbc_infoLabel5.gridx = 1;
		gbc_infoLabel5.gridy = 5;
		sortSection.add(infoLabel5, gbc_infoLabel5);
		
		metricSelection = new JComboBox<String>(classSpecificSortOptions);
		metricSelection.setSelectedItem(attributeNames[3]);
		GridBagConstraints gbc_metricSelection = new GridBagConstraints();
		gbc_metricSelection.insets = new Insets(5, 5, 5, 5);
		gbc_metricSelection.anchor = GridBagConstraints.NORTHWEST;
		gbc_metricSelection.gridx = 1;
		gbc_metricSelection.gridy = 6;
		sortSection.add(metricSelection, gbc_metricSelection);
		metricSelection.addItemListener(this);
		metricSelection.setEnabled(false);
		
		attributeFilterAction = new AttributeFilterAction(attributeNames);
		GridBagConstraints gbc_attributeFilterAction = new GridBagConstraints();
		gbc_attributeFilterAction.insets = new Insets(5, 0, 5, 0);
		gbc_attributeFilterAction.fill = GridBagConstraints.BOTH;
		gbc_attributeFilterAction.gridx = 1;
		gbc_attributeFilterAction.gridy = 0;
		gbc_attributeFilterAction.gridheight = 2;
		add(attributeFilterAction, gbc_attributeFilterAction);
		attributeFilterAction.addPropertyChangeListener(new PropertyChangeListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals(AttributeFilterAction.FILTERS_CHANGED)) {
					short value = (Short)evt.getNewValue();
					if(value == AttributeFilterAction.HAVING_TO_NOT_HAVING) {
						Set<Integer> toRemove = (Set<Integer>)evt.getOldValue();
						Iterator<Stats> it = liveStats.iterator();
						while(it.hasNext()) {
							Stats stat = it.next();
							Set<Integer> combination = stat.getPartition();
							for(int attribute : toRemove) {
								if(combination.contains(attribute)) {
									it.remove();
									break;
								}
							}
						}
						setupBarCharts();
					} else if(value == AttributeFilterAction.NOT_HAVING_TO_HAVING || value == AttributeFilterAction.INVERSION) {
						List<Stats> temp = new ArrayList<Stats>(allStats);
						Set<Integer> notInclude = attributeFilterAction.getNotHavingAttributesList();
						Iterator<Stats> it = temp.iterator();
						while(it.hasNext()) {
							Stats stat = it.next();
							Set<Integer> combination = stat.getPartition();
							for(int attribute : notInclude) {
								if(combination.contains(attribute)) {
									it.remove();
									break;
								}
							}
						}
						liveStats = temp;
						setupBarCharts();
					}
				}
			}
		});	
		
		liveStats = new ArrayList<Stats>(allStats);
		
		HyperlinkButton saveCompleteLabel = new HyperlinkButton("Complete");
		saveCompleteLabel.setHorizontalAlignment(SwingConstants.CENTER);
		saveSection.add(saveCompleteLabel);
		if(preferencesFile == null)
			saveCompleteLabel.setEnabled(false);
		saveCompleteLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				File selectedResultFile = new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "results.put");
				JFileChooser jf = new JFileChooser(selectedResultFile.getParentFile());
				jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jf.setFileFilter(new FileNameExtensionFilter("PUT files", "put"));
				jf.setSelectedFile(selectedResultFile);
				int closeOption = jf.showSaveDialog(null);
				if(closeOption == JFileChooser.APPROVE_OPTION) {
					selectedResultFile = jf.getSelectedFile();
					if(selectedResultFile.exists()) {
						closeOption = JOptionPane.showConfirmDialog(null, "Overwrite existing file?");
						if(closeOption == JOptionPane.NO_OPTION)
							return;
					}
					try {
						File tempFile = File.createTempFile("put-result", "" + System.nanoTime());
						// Add filter preferences to preference file
						PrintWriter pw = new PrintWriter(new FileWriter(preferencesFile, true));
						pw.println(AttributeFilterAction.FILTERED_ATTRIBUTES_LIST_PREFERENCE + " " + attributeFilterAction.getNotHavingAttributesList());
						pw.println(SELECTED_METRIC_PREFERENCE + " " + metricSelection.getSelectedItem());
						pw.println(SELECTED_CLASS_PREFERENCE + " " + classSelection.getSelectedItem());
						pw.println(SORT_CRITERIA_PREFERENCE + " " + sortCriterionSelection.getSelectedItem());
						pw.println(ORDER_PREFERENCE + " " + orderSelection.getSelectedItem());
						pw.close();
						if(ArchiveManager.saveAsCompressedFile(tempFile, new File[]{datasetFile, resultFile, preferencesFile}, new String[]{null, "results.csv", "prefs.txt"})) {
							Files.copy(tempFile.toPath(), selectedResultFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
							JOptionPane.showMessageDialog(null, "File saved !!");
						}
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Error in saving the results file. Please make sure the selected directory is writable.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
		});
		
		HyperlinkButton saveResultsOnlyLabel = new HyperlinkButton("Results only");
		saveResultsOnlyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		saveSection.add(saveResultsOnlyLabel);
		saveResultsOnlyLabel.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				File selectedResultFile = new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "results.csv");
				JFileChooser jf = new JFileChooser(selectedResultFile.getParentFile());
				jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jf.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
				jf.setSelectedFile(selectedResultFile);
				int closeOption = jf.showSaveDialog(null);
				if(closeOption == JFileChooser.APPROVE_OPTION) {
					selectedResultFile = jf.getSelectedFile();
					if(selectedResultFile.exists()) {
						closeOption = JOptionPane.showConfirmDialog(null, "Overwrite existing file?");
						if(closeOption == JOptionPane.NO_OPTION)
							return;
					}
					try {
						Files.copy(resultFile.toPath(), selectedResultFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
						JOptionPane.showMessageDialog(null, "File saved !!");
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Error in saving the results file. Please make sure the selected directory is writable.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
		});
		
		setupBarCharts();
	}
	
	/**
	 * Draws an overlay over a given area using the given graphics context
	 * @param g Graphics context
	 * @param size Overlay size (the overlay is drawn in the <code>Rectangle(0, 0, size.width, size.height)</code>
	 */
	private void drawOverlay(Graphics g, Dimension size) {
		if(enableClassSortingSection)
			return;
		Graphics2D g2 = (Graphics2D)(g.create());
		g2.setColor(OVERLAY_COLOR);
		g2.fillRect(0, 0, size.width, size.height);
		g2.dispose();
	}
	
	/**
	 * Enables or disables the class specific widgets
	 * @param enable If <code>true</code>, enables the widgets, or disables them otherwise 
	 */
	private void enableClassSpecificWidgets(boolean enable) {
		enableClassSortingSection = enable;
		classSelection.setEnabled(enable);
		metricSelection.setEnabled(enable);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED) {
			JComboBox<String> source = (JComboBox<String>)e.getSource();
			if(source.equals(sortCriterionSelection))
				enableClassSpecificWidgets(sortCriterionSelection.getSelectedItem().equals("Class Specific Metrics"));
			setupBarCharts();
		}
	}
	
	@Override
	public void setInitialPreferences(Map<String, String> preferences) {
		// Pass any filter preferences to Attribute Filter Action first
		attributeFilterAction.setInitialPreferences(preferences);
		if(preferences != null) {
			String selectedMetric = preferences.get(SELECTED_METRIC_PREFERENCE);
			if(selectedMetric != null)
				metricSelection.setSelectedItem(selectedMetric.trim());
			String selectedClass = preferences.get(SELECTED_CLASS_PREFERENCE);
			if(selectedClass != null)
				classSelection.setSelectedItem(selectedClass.trim());
			String selectedOrder = preferences.get(ORDER_PREFERENCE);
			if(selectedOrder != null)
				orderSelection.setSelectedItem(selectedOrder.trim());
			String sortCriteria = preferences.get(SORT_CRITERIA_PREFERENCE);
			if(sortCriteria != null) {
				sortCriterionSelection.setSelectedItem(sortCriteria);
			}
				
		}
	}

	/**
	 * Sets up or refreshes the bar chart widget
	 */
	private void setupBarCharts() {
		if(plotWidget != null) {
			plotWidget.dispose();
			remove(plotWidget);
		}
		/*
		 * The order in which sorting preferences are shown are critical for the code below to work.
		 * If a new sorting preference is to be added, the respective constant in Stats class should be added/changed accordingly.
		 */
		Integer classIndex = null;
		short selectedCriterion = (short)sortCriterionSelection.getSelectedIndex();
		if(sortCriterionSelection.getSelectedItem().equals("Class Specific Metrics")) {
			selectedCriterion = (short)(metricSelection.getSelectedIndex() + 10);
			classIndex = classSelection.getSelectedIndex();
		}
		Stats.sortList(liveStats, selectedCriterion, orderSelection.getSelectedIndex() == 0 ? false : true, classIndex);
		plotWidget = new BarChartWidget(liveStats, attributeNames, selectedCriterion, classIndex);
		GridBagConstraints gbc_plotWidget = new GridBagConstraints();
		gbc_plotWidget.gridwidth = 2;
		gbc_plotWidget.insets = new Insets(0, 5, 5, 5);
		gbc_plotWidget.fill = GridBagConstraints.BOTH;
		gbc_plotWidget.gridx = 0;
		gbc_plotWidget.gridy = 2;
		add(plotWidget, gbc_plotWidget);
		revalidate();
	}
}
