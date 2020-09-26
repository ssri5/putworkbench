package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import in.ac.iitk.cse.putwb.experiment.Stats;
import in.ac.iitk.cse.putwb.ui.IconCreator;
import in.ac.iitk.cse.putwb.ui.actions.AnalyzeAction;


/**
 * A widget that shows a set of Bar Charts for a given list of stats
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class BarChartWidget extends JPanel {
	
	/**
	 * A component that shows a bar, in a {@link BarChartWidget}
	 * @author Saurabh Srivastava
	 *
	 */
	private class Bar extends JComponent {

		/**
		 * The height proportion, between <code>0</code> and <code>1</code>, to which the bar goes in the parent space
		 */
		private double heightProportion; 

		/**
		 * The color with which the bar is painted
		 */
		private Color paintColor;
		
		/**
		 * The actual y-axis location (based on the {@link #heightProportion} value), within this widget, from where the painting starts
		 */
		private double y;
		
		/**
		 * Creates a bar component, to be placed within a {@link BarChartWidget}
		 * @param heightProportion The proportion of the overall height, that the component represents (depends on the y-axis value the respective stat represents)
		 * @param paintColor The color with which the bar is to be painted
		 * @param toolTipText The tooltip to show for this bar
		 * @param details The details (lines of text) that will be shown if the user clicks on this bar
		 */
		private Bar(double heightProportion, Color paintColor, String toolTipText, String[] details) {
			this.heightProportion = heightProportion;
			this.paintColor = paintColor;
			setToolTipText(toolTipText);
			setCursor(HAND_CURSOR);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					/*
					 * 1. Finds the relative location of this bar within the scroll pane.
					 * 2. Decides whether the detail box should be on the left or right, so that it shows within the scroll pane
					 * 3. Indicates that an overlay be painted over rest of the bars, so that this bar becomes prominent on screen
					 * 4. Shows the details box at a specified location - close to the bar
					 */
					selectedBar = Bar.this;
					Rectangle2D bounds = selectedBar.getBounds();
					Point barChartWidgetLocation = BarChartWidget.this.getLocationOnScreen();
					double x = e.getLocationOnScreen().getX() - barChartWidgetLocation.getX() - e.getPoint().getX();
					double y2 = y + barChartWidgetLocation.getY() - selectedBar.getParent().getLocationOnScreen().getY();
					int parentWidth = chartScrollPane.getBounds().width;
					char side = 'N';
					if(x < parentWidth/2)
						side = 'R';
					else
						side = 'L';
					
					Rectangle dialogBounds = new Rectangle();
					
					if(side == 'R') 
						dialogBounds.setRect(x + bounds.getWidth(), y2, DETAILS_BOX_WIDTH, DETAILS_BOX_HEIGHT);
					else
						dialogBounds.setRect(x - DETAILS_BOX_WIDTH, y2, DETAILS_BOX_WIDTH, DETAILS_BOX_HEIGHT);
					
					dialogBounds.setRect(dialogBounds.getX() + barChartWidgetLocation.getX(), dialogBounds.getY() + barChartWidgetLocation.getY(), DETAILS_BOX_WIDTH, DETAILS_BOX_HEIGHT);
					
					detailBox.setInfo(details);
					showDetailsBoxAt(dialogBounds);
					BarChartWidget.this.repaint();
				}
			});
		}

		public JToolTip createToolTip() {
			JToolTip tip = super.createToolTip();
			tip.setBackground(TOOLTIP_BACKGROUND);
			tip.setForeground(TOOLTIP_TEXT_COLOR);
			return tip;
		}

		public void paint(Graphics g) {
			Dimension size = getSize();
			double height = heightProportion * size.height;
			y = size.height - height;
			Rectangle2D.Double bar = new Rectangle2D.Double(0, y, size.width, height);
			Graphics2D g2 = (Graphics2D)(g.create());
			g2.setColor(paintColor);
			g2.fill(bar);
			g2.dispose();
		}
	}

	/**
	 * A dialog like widget to show details about a particular bar chart
	 * @author Saurabh Srivastava
	 *
	 */
	private class DetailsDialog extends JPanel {
		
		/**
		 * The panel that contains the detailed information
		 */
		private JPanel contentPanel;
		
		/**
		 * Creates a new details box, with blank fields
		 */
		private DetailsDialog() {
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWeights = new double[]{1.0, 0.0};
			gridBagLayout.rowWeights = new double[]{0.0, 1.0};
			setLayout(gridBagLayout);
			setBackground(Color.WHITE);
			contentPanel = new JPanel();
			
			contentPanel.setLayout(new GridLayout(0, 1, 2, 2));
			GridBagConstraints gbc_contentPanelPane = new GridBagConstraints();
			gbc_contentPanelPane.gridx = 0;
			gbc_contentPanelPane.gridy = 1;
			gbc_contentPanelPane.fill = GridBagConstraints.BOTH;
			JScrollPane contentPanelPane = new JScrollPane(contentPanel);
			contentPanel.setBackground(Color.WHITE);
			add(contentPanelPane,gbc_contentPanelPane);
			
			JPanel topPanel = new JPanel(new BorderLayout());
			topPanel.setBackground(new Color(66, 134, 244));
			IconButton closeButton = new IconButton(IconCreator.getIcon(IconCreator.CLOSE_ICON_FILE), "close", false);
			GridBagConstraints gbc_topPanel = new GridBagConstraints();
			gbc_topPanel.gridx = 0;
			gbc_topPanel.gridy = 0;
			gbc_topPanel.fill = GridBagConstraints.BOTH;
			gbc_topPanel.anchor = GridBagConstraints.NORTHEAST;
			topPanel.add(closeButton, BorderLayout.EAST);
			closeButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					hideDetailsBox();
				}
			});
			add(topPanel, gbc_topPanel);
		}
		
		/**
		 * Sets the information in details box
		 * @param lines One or more lines to set as the information in the details box
		 */
		private void setInfo(String... lines) {
			contentPanel.removeAll();
			for(String line : lines) {
				JEditorPane info = new JEditorPane();
				info.setContentType("text/html");
				info.setEditable(false);
				info.setOpaque(false);
				info.setText("<html><font size='3' color='#2d0c08'>" + line + "</font></html>");
				contentPanel.add(info);
			}
		}
		
	}
	
	/**
	 * A widget that shows a vertical scale, next to the bar charts
	 * @author Saurabh Srivastava
	 *
	 */
	private class YAxis extends JComponent {

		/**
		 * Creates a y-axis scale with the given low, high values and title
		 * @param start The lower end of the scale
		 * @param end The higher end of the scale
		 * @param title The title for this axis
		 */
		private YAxis(double start, double end, String title) {
			super();

			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[]{22, 20, 2};
			gridBagLayout.columnWeights = new double[]{0.0, 1, 0.0};
			gridBagLayout.rowWeights = new double[]{1.0};
			setLayout(gridBagLayout);
			final Color lineColor = new Color(45, 12, 8);
			JComponent line = new JComponent() {
				public void paint(Graphics g) {
					super.paint(g);
					Graphics2D g2 = (Graphics2D)(g.create());
					g2.setColor(lineColor);
					Dimension size = getSize();
					g2.fillRect(0, 0, size.width, size.height);
					g2.dispose();
				}
			};
			GridBagConstraints gbc_line = new GridBagConstraints();
			gbc_line.gridx = 2;
			gbc_line.gridy = 0;
			gbc_line.fill = GridBagConstraints.BOTH;
			add(line, gbc_line);
			line.setBackground(Color.RED);

			JPanel ticksPanel = buildTicksPanel(start, end);
			GridBagConstraints gbc_ticksPanel = new GridBagConstraints();
			gbc_ticksPanel.gridx = 1;
			gbc_ticksPanel.gridy = 0;
			gbc_ticksPanel.insets = new Insets(0, 0, 0, 2);
			gbc_ticksPanel.fill = GridBagConstraints.BOTH;
			add(ticksPanel, gbc_ticksPanel);

			JPanel titleLabel = new JPanel() {
				public void paint(Graphics g) {
					/*
					 * This is a quick and dirty hack.. we should ideally have a different widget for this
					 */
					super.paint(g);
					Graphics2D g2 = (Graphics2D)(g.create());
					Rectangle bounds = getBounds();
					g2.rotate(Math.toRadians(-90), bounds.x + bounds.width/2, bounds.y + bounds.height/2);
					int labelWidth = (int)(bounds.height*0.8);
					g2.translate(-labelWidth/2, 0);
					JLabel label = new JLabel("<html><font size='3' color='#9e1503'>" + title + "</font></html>");
					label.setHorizontalAlignment(JLabel.CENTER);
					label.setBounds(0, 0, bounds.width + labelWidth, bounds.height);
					label.paint(g2);
					g2.dispose();
				}
			};
			titleLabel.setOpaque(false);
			GridBagConstraints gbc_titleLabel = new GridBagConstraints();
			gbc_titleLabel.gridx = 0;
			gbc_titleLabel.gridy = 0;
			gbc_titleLabel.insets = new Insets(0, 0, 0, 0);
			gbc_titleLabel.fill = GridBagConstraints.BOTH;
			add(titleLabel, gbc_titleLabel);
		}

		/**
		 * Builds a panel that shows the high, low and mid values, and paints a tick next to them
		 * @param start The high value
		 * @param end The low value
		 * @return A panel containing the ticks and values
		 */
		private JPanel buildTicksPanel(double start, double end) {
			JPanel ticksPanel = new JPanel();
			ticksPanel.setOpaque(false);
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWeights = new double[]{1.0};
			gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0};
			ticksPanel.setLayout(gridBagLayout);

			double mid = start + (end - start)/2;

			JLabel startLabel = new JLabel("<html><font size='3' color='#033e9e'>" + start + "</font></html>");
			startLabel.setVerticalAlignment(SwingConstants.BOTTOM);
			startLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			GridBagConstraints gbc_startLabel = new GridBagConstraints();
			gbc_startLabel.gridx = 0;
			gbc_startLabel.gridy = 2;
			gbc_startLabel.fill = GridBagConstraints.BOTH;
			gbc_startLabel.anchor = GridBagConstraints.SOUTHEAST;
			ticksPanel.add(startLabel, gbc_startLabel);

			JLabel midLabel = new JLabel("<html><font size='3' color='#033e9e'>" + mid + "</font></html>");
			midLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			GridBagConstraints gbc_midLabel = new GridBagConstraints();
			gbc_midLabel.gridx = 0;
			gbc_midLabel.gridy = 1;
			gbc_midLabel.fill = GridBagConstraints.BOTH;
			gbc_midLabel.anchor = GridBagConstraints.EAST;
			ticksPanel.add(midLabel, gbc_midLabel);

			JLabel endLabel = new JLabel("<html><font size='3' color='#033e9e'>" + end + "</font></html>");
			endLabel.setVerticalAlignment(SwingConstants.TOP);
			endLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			GridBagConstraints gbc_endLabel = new GridBagConstraints();
			gbc_endLabel.gridx = 0;
			gbc_endLabel.gridy = 0;
			gbc_endLabel.fill = GridBagConstraints.BOTH;
			gbc_endLabel.anchor = GridBagConstraints.NORTHEAST;
			ticksPanel.add(endLabel, gbc_endLabel);

			return ticksPanel;
		}
	}
	
	/**
	 * A color pallete for bar charts - charts are painted in the colors defined in this array in a circular fashion
	 */
	private static Color[] colorPallete = new Color[] {new Color(244, 164, 244), new Color(159, 159, 252), Color.CYAN, Color.ORANGE, new Color(146, 252, 146)};
	
	/**
	 * The constant for the width of a details box
	 */
	private static final int DETAILS_BOX_HEIGHT = 150;
	
	/**
	 * The constant for the width of a details box
	 */
	private static final int DETAILS_BOX_WIDTH = 310;
	
	/**
	 * The cursor constant for bar charts
	 */
	private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
	
	/**
	 * The color constant for tooltip background
	 */
	private static final Color TOOLTIP_BACKGROUND = new Color(255,255,225);
	
	/**
	 * The color constant for tooltip text
	 */
	private static final Color TOOLTIP_TEXT_COLOR = new Color(158, 21, 3);
	
	/**
	 * The scroll pane in which the bar charts are created
	 */
	private JScrollPane chartScrollPane;
	
	/**
	 * A shared instance of details box, used to show details of any given bar chart
	 */
	private DetailsDialog detailBox = new DetailsDialog();
	
	/**
	 * A popup that shows details of a bar chart
	 */
	private Popup displayDialog;
	
	/**
	 * A custom mous events' handler. This is required for proper disposal of detail boxes.
	 */
	private AWTEventListener mouseEventsHandler;
	
	/**
	 * The color used to paint an overlay, which partially hides the non-selected bars
	 */
	private final Color OVERLAY_COLOR = new Color(255, 255, 255, 200);
	
	/**
	 * The flag, if <code>true</code>, results in an overlay painted over the the charts
	 */
	private boolean paintOverlay = false;

	/**
	 * The currently selected bar, if any
	 */
	private Bar selectedBar;
	
	/**
	 * The AWT toolkit in use
	 */
	private Toolkit toolkit;
	
	/**
	 * Creates a compound statistical display widget where the statistics are shown in the form of bar charts
	 * @param statsToDisplay The {@link List} of {@link Stats} to display
	 * @param attributeNames The name of attributes over which the stats are collected
	 * @param detailType The detail that this widget is plotting; defaults to "Accuracy" if no matching metric is found
	 * @param classIndex The class that is used, if it is plotting a class specific metric; <code>-1</code> implies it is not a class specific metric
	 */
	public BarChartWidget(List<Stats> statsToDisplay, String[] attributeNames, short detailType, Integer classIndex) {
		setOpaque(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0};
		setLayout(gridBagLayout);

		JLabel infoLabel = new JLabel("<html><font size='4' color='#033e9e'><b>Click on a bar for details</b></font></html>");
		GridBagConstraints gbc_infoLabel = new GridBagConstraints();
		gbc_infoLabel.insets = new Insets(0, 0, 3, 0);
		gbc_infoLabel.gridx = 1;
		gbc_infoLabel.gridy = 0;
		gbc_infoLabel.gridwidth = 1;
		gbc_infoLabel.fill = GridBagConstraints.HORIZONTAL;
		add(infoLabel, gbc_infoLabel);

		double start = Stats.getMinimumStatValue(detailType);
		double end = Stats.getMaximumStatValue(detailType);
		String title = AnalyzeAction.getDisplayNameForMetric(detailType);
		
		YAxis yAxis = new YAxis(start, end, title);
		GridBagConstraints gbc_yAxis = new GridBagConstraints();
		gbc_yAxis.gridx = 0;
		gbc_yAxis.gridy = 1;
		gbc_yAxis.fill = GridBagConstraints.BOTH;
		add(yAxis, gbc_yAxis);

		JPanel chartsContainer = new JPanel();
		chartsContainer.setOpaque(false);
		GridBagLayout gbl_chartsContainer = new GridBagLayout();
		int[] colWidths = new int[statsToDisplay.size() + 1];
		Arrays.fill(colWidths, 25);
		colWidths[colWidths.length-1] = 0;
		gbl_chartsContainer.columnWidths = colWidths;
		double[] colWeights = new double[statsToDisplay.size()+1];
		Arrays.fill(colWeights, 0);
		colWeights[colWeights.length-1] = 1.0;
		gbl_chartsContainer.rowHeights = new int[] {50};
		gbl_chartsContainer.columnWeights = colWeights;
		gbl_chartsContainer.rowWeights = new double[]{1.0};
		chartsContainer.setLayout(gbl_chartsContainer);

		int ctr = 0, len = colorPallete.length;
		double scale = end - start;
		for(Stats stat : statsToDisplay) {
			String tooltipLine = "<b>";
			for(int i : stat.getPartition()) {
				tooltipLine += attributeNames[i-1] + ", ";
			}
			double value = stat.getStatValue(detailType, classIndex);
			tooltipLine = tooltipLine.substring(0, tooltipLine.lastIndexOf(", ")) + "</b>";
			StringBuffer details = new StringBuffer(); 
			details.append("Attributes Used: <b>" + tooltipLine + "</b>");
			details.append("\n");
			details.append("Classification Accuracy: <b>" + stat.getAccuracy() + " </b>%");
			details.append("\n");
			if(detailType != Stats.ACCURACY && detailType != Stats.DICTIONARY_SEQUENCE) {
				details.append(title + ": <b>" + value + " </b>");
				details.append("\n");
			}
			details.append("Time taken: <b>" + stat.getTime()/1000000000d + " </b>secs");
			Bar bar = new Bar(value/scale, colorPallete[ctr%len], "<html>" + tooltipLine + "</html>", details.toString().split("\n"));

			GridBagConstraints gbc_bar = new GridBagConstraints();
			gbc_bar.gridx = ctr;
			gbc_bar.gridy = 0;
			gbc_bar.fill = GridBagConstraints.BOTH;
			gbc_bar.insets = new Insets(0, 5, 0, 0);
			gbc_bar.anchor = GridBagConstraints.WEST;
			chartsContainer.add(bar, gbc_bar);
			ctr++;
		}

		// Spacer, if required
		JLabel spacer = new JLabel();

		GridBagConstraints gbc_spacer = new GridBagConstraints();
		gbc_spacer.gridx = ctr;
		gbc_spacer.gridy = 0;
		gbc_spacer.fill = GridBagConstraints.BOTH;
		gbc_spacer.insets = new Insets(0, 3, 0, 0);
		gbc_spacer.anchor = GridBagConstraints.WEST;
		chartsContainer.add(spacer, gbc_spacer);

		chartScrollPane = new JScrollPane(chartsContainer);
		chartScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		chartScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_chartScrollPane = new GridBagConstraints();
		gbc_chartScrollPane.fill = GridBagConstraints.BOTH;
		gbc_chartScrollPane.gridx = 1;
		gbc_chartScrollPane.gridy = 1;
		add(chartScrollPane, gbc_chartScrollPane);
		chartScrollPane.setOpaque(false);
		chartScrollPane.getViewport().setOpaque(false);
		JScrollBar horizontalScrollBar = chartScrollPane.getHorizontalScrollBar();
		if(horizontalScrollBar != null)
			horizontalScrollBar.addAdjustmentListener(new AdjustmentListener() {
				
				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
					if(displayDialog != null) {
						hideDetailsBox();
					}
				}
			});
	}
	
	/**
	 * Disposes any created resources that should be released. As of now, it disposes the popup for details box.
	 */
	public void dispose() {
		hideDetailsBox();
	}
	
	/**
	 * Hides the details box, if it is visible
	 */
	private void hideDetailsBox() {
		if(displayDialog != null) {
			/*
			 * Hide the details box, if it is visible, when the user scrolls
			 */
			displayDialog.hide();
			paintOverlay = false;
			BarChartWidget.this.repaint();
			if(toolkit != null)
				toolkit.removeAWTEventListener(mouseEventsHandler);
		}
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if(paintOverlay && selectedBar != null) {
			Point widgetLocation = getLocationOnScreen();
			Rectangle scrollPaneArea = chartScrollPane.getBounds();
			Rectangle barArea = selectedBar.getBounds();
			Point barLocation = selectedBar.getLocationOnScreen();
			barArea.x = barLocation.x - widgetLocation.x;
			barArea.y = barLocation.y - widgetLocation.y;
			Graphics2D g2 = (Graphics2D)(g.create());
			g2.setColor(OVERLAY_COLOR);
			Area paintArea = new Area(scrollPaneArea);
			paintArea.subtract(new Area(barArea));
			g2.fill(paintArea);
			g2.dispose();
		}
	}

	/**
	 * Shows a detail box, at a particular location and size
	 * @param bounds A {@link Rectangle} object providing details of the location and size of the detail box
	 */
	private void showDetailsBoxAt(Rectangle bounds) {
		hideDetailsBox();
		paintOverlay = true;
		detailBox.setPreferredSize(new Dimension(bounds.width, bounds.height));
		displayDialog = PopupFactory.getSharedInstance().getPopup(null, detailBox, bounds.x, bounds.y);
		displayDialog.show();
		if(toolkit == null);
			toolkit = Toolkit.getDefaultToolkit();
		if(mouseEventsHandler == null) {
			mouseEventsHandler = new AWTEventListener() {
				
				@Override
				public void eventDispatched(AWTEvent event) {
					MouseEvent e = (MouseEvent)event;
					if(e.getClickCount() < 1)	// We care about clicks only, we reach here even for motion events
						return;
					Point location = BarChartWidget.this.getLocationOnScreen();
					Dimension size = BarChartWidget.this.getSize();
					Rectangle bounds = new Rectangle(location.x, location.y, size.width, size.height);	
					Point evenLocation = new Point(e.getXOnScreen(), e.getYOnScreen());
					if(!bounds.contains(evenLocation)) {
						hideDetailsBox();
					}
				}
			};
		}
		toolkit.addAWTEventListener((AWTEventListener) mouseEventsHandler, AWTEvent.MOUSE_EVENT_MASK);	
	}
}
