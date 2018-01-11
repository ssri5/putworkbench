package in.ac.iitk.cse.putwb.ui.actions;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import in.ac.iitk.cse.putwb.ui.widgets.RoundedLineBorder;


/**
 * Attribute filtering action allows users to select a set of attributes to mask from the results being displayed inside a results and analysis action.
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class AttributeFilterAction extends Action implements ActionListener {

	/**
	 * The preference constant for setting filtered attributes
	 */
	public static final String FILTERED_ATTRIBUTES_LIST_PREFERENCE = "Filter_Attributes";
	
	/**
	 * The constant for "Filters Changed" property
	 */
	public static final String FILTERS_CHANGED = "AttributeFilterAction - filters changed";

	/**
	 * The constant for "Attributes Masked" property
	 */
	public static short HAVING_TO_NOT_HAVING = 0;

	/**
	 * The constant for "Attributes in Masked and Unmasked categories switched" property
	 */
	public static short INVERSION = 2;

	/**
	 * The constant for "Attributes Unmasked" property
	 */
	public static short NOT_HAVING_TO_HAVING = 1;

	/**
	 * The text color in the list showing unmasked attributes
	 */
	private Color HAVING_TEXT_COLOR = new Color(45, 130, 2);

	/**
	 * The list widget containing the unmasked attributes
	 */
	private JList<String> havingList;

	/**
	 * The button to interchange masked and unmasked sets of attributes
	 */
	private JButton invertButton;

	/**
	 * The model for the unmasked attributes list
	 */
	private DefaultListModel<String> leftListModel;

	/**
	 * The button that masks one or more attributes
	 */
	private JButton leftToRightButton;

	/**
	 * The text color in the list showing masked attributes
	 */
	private Color NOT_HAVING_TEXT_COLOR = new Color(219, 58, 4);

	/**
	 * The list widget containing the masked attributes
	 */
	private JList<String> notHavingList;

	/**
	 * The model for the masked attributes list
	 */
	private DefaultListModel<String> rightListModel;

	/**
	 * The button that unmasks one or more attributes
	 */
	private JButton rightToLeftButton;

	/**
	 * The color to show any selected row(s) in either masked or unmasked attributes lists
	 */
	private Color SELECTED_COLOR = new Color(247, 247, 183);

	/**
	 * Creates a new Attribute Filter action panel with the given set of attribute names
	 * @param attributeNames An array of attribute names
	 */
	public AttributeFilterAction(String[] attributeNames) {
		setBorder(new TitledBorder(new RoundedLineBorder(Color.LIGHT_GRAY, 2), "Filter Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setOpaque(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0};
		setLayout(gridBagLayout);

		JLabel infoLabel1 = new JLabel("<html><b><font size='4' color='#033e9e'>Results of combinations with masked attributes are filtered out</font></b></html>");
		infoLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel1 = new GridBagConstraints();
		gbc_infoLabel1.insets = new Insets(5, 0, 5, 0);
		gbc_infoLabel1.gridwidth = 3;
		gbc_infoLabel1.gridx = 0;
		gbc_infoLabel1.gridy = 0;
		add(infoLabel1, gbc_infoLabel1);

		JPanel infoPanel = new JPanel();
		infoPanel.setOpaque(false);
		GridBagConstraints gbc_infoPanel = new GridBagConstraints();
		gbc_infoPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_infoPanel.insets = new Insets(0, 0, 5, 0);
		gbc_infoPanel.gridx = 0;
		gbc_infoPanel.gridy = 1;
		gbc_infoPanel.gridwidth = 3;
		add(infoPanel, gbc_infoPanel);
		GridBagLayout gbl_infoPanel = new GridBagLayout();
		gbl_infoPanel.columnWeights = new double[]{1.0, 1.0};
		gbl_infoPanel.rowWeights = new double[]{1.0};
		infoPanel.setLayout(gbl_infoPanel);

		JLabel infoLabel2 = new JLabel("Attributes shown");
		infoLabel2.setForeground(HAVING_TEXT_COLOR);
		infoLabel2.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel2 = new GridBagConstraints();
		gbc_infoLabel2.fill = GridBagConstraints.HORIZONTAL;
		gbc_infoLabel2.insets = new Insets(0, 0, 0, 0);
		gbc_infoLabel2.gridx = 0;
		gbc_infoLabel2.gridy = 1;
		infoPanel.add(infoLabel2, gbc_infoLabel2);

		JLabel infoLabel3 = new JLabel("Attributes masked");
		infoLabel3.setBackground(NOT_HAVING_TEXT_COLOR);
		infoLabel3.setForeground(Color.RED);
		infoLabel3.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel3 = new GridBagConstraints();
		gbc_infoLabel3.insets = new Insets(0, 0, 0, 0);
		gbc_infoLabel3.gridx = 1;
		gbc_infoLabel3.gridy = 1;
		infoPanel.add(infoLabel3, gbc_infoLabel3);

		leftListModel = new DefaultListModel<String>();
		for(int i = 0; i < attributeNames.length; i++)
			leftListModel.addElement((i+1) + ".  " + attributeNames[i]);
		havingList = new JList<String>(leftListModel);
		havingList.setVisibleRowCount(8);
		havingList.setBorder(new RoundedLineBorder(Color.LIGHT_GRAY, 1));
		havingList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		GridBagConstraints gbc_havngList = new GridBagConstraints();
		gbc_havngList.insets = new Insets(5, 5, 5, 5);
		gbc_havngList.fill = GridBagConstraints.BOTH;
		gbc_havngList.gridx = 0;
		gbc_havngList.gridy = 2;
		JScrollPane havingListScroller = new JScrollPane(havingList);
		havingListScroller.setPreferredSize(new Dimension(0, 100));
		havingListScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(havingListScroller, gbc_havngList);
		havingList.setCellRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				c.setForeground(HAVING_TEXT_COLOR);
				if(isSelected)
					c.setBackground(SELECTED_COLOR);
				return c;
			}

		});

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setOpaque(false);
		GridBagConstraints gbc_buttonsPanel = new GridBagConstraints();
		gbc_buttonsPanel.insets = new Insets(5, 5, 5, 5);
		gbc_buttonsPanel.fill = GridBagConstraints.BOTH;
		gbc_buttonsPanel.gridx = 1;
		gbc_buttonsPanel.gridy = 2;
		add(buttonsPanel, gbc_buttonsPanel);
		GridBagLayout gbl_buttonsPanel = new GridBagLayout();
		gbl_buttonsPanel.columnWeights = new double[]{0.0};
		gbl_buttonsPanel.rowWeights = new double[]{0.0, 0.0};
		buttonsPanel.setLayout(gbl_buttonsPanel);

		leftToRightButton = new JButton(">>");
		GridBagConstraints gbc_leftToRightButton = new GridBagConstraints();
		gbc_leftToRightButton.insets = new Insets(0, 0, 5, 0);
		gbc_leftToRightButton.gridx = 0;
		gbc_leftToRightButton.gridy = 0;
		buttonsPanel.add(leftToRightButton, gbc_leftToRightButton);
		leftToRightButton.addActionListener(this);
		leftToRightButton.setToolTipText("Mask");

		rightToLeftButton = new JButton("<<");
		GridBagConstraints gbc_rightToLeftButton = new GridBagConstraints();
		gbc_rightToLeftButton.insets = new Insets(5, 0, 0, 0);
		gbc_rightToLeftButton.gridx = 0;
		gbc_rightToLeftButton.gridy = 1;
		buttonsPanel.add(rightToLeftButton, gbc_rightToLeftButton);
		rightToLeftButton.addActionListener(this);
		rightToLeftButton.setToolTipText("Unmask");

		rightListModel = new DefaultListModel<String>();
		notHavingList = new JList<String>(rightListModel);
		notHavingList.setVisibleRowCount(8);
		notHavingList.setBorder(new RoundedLineBorder(Color.LIGHT_GRAY, 1));
		notHavingList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		GridBagConstraints gbc_notHavingList = new GridBagConstraints();
		gbc_notHavingList.insets = new Insets(5, 5, 5, 5);
		gbc_notHavingList.fill = GridBagConstraints.BOTH;
		gbc_notHavingList.gridx = 2;
		gbc_notHavingList.gridy = 2;
		JScrollPane notHavingListScroller = new JScrollPane(notHavingList);
		notHavingListScroller.setPreferredSize(new Dimension(0, 100));
		notHavingListScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(notHavingListScroller, gbc_notHavingList);
		notHavingList.setCellRenderer(new DefaultListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				c.setForeground(NOT_HAVING_TEXT_COLOR);
				if(isSelected)
					c.setBackground(SELECTED_COLOR);
				return c;
			}

		});

		invertButton = new JButton("Invert");
		GridBagConstraints gbc_invertButton = new GridBagConstraints();
		gbc_invertButton.gridwidth = 3;
		gbc_invertButton.insets = new Insets(5, 0, 0, 0);
		gbc_invertButton.gridx = 0;
		gbc_invertButton.gridy = 3;
		add(invertButton, gbc_invertButton);
		invertButton.addActionListener(this);
	}

	/**
	 * Handles events of button presses
	 */
	public void actionPerformed(ActionEvent e) {
		JButton source = (JButton)e.getSource();
		if(source.equals(leftToRightButton)) {
			int[] selectedIndices = havingList.getSelectedIndices();

			if(selectedIndices.length == 0) {
				JOptionPane.showMessageDialog(null, "Select one or more attributes to mask", "Select attribute(s)", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String[] selectedAttributes = new String[selectedIndices.length];
			int j = 0;
			int size = rightListModel.getSize();
			for(int i = 0; i < size && j < selectedIndices.length; i++) {
				String attr1 = leftListModel.get(selectedIndices[j]);
				String attr2 = rightListModel.get(i);
				if(compareAttributes(attr1, attr2) < 0) {
					rightListModel.add(i, attr1);
					selectedAttributes[j] = attr1;
					j++;
					size++;
				}
			}
			while(j < selectedIndices.length) {
				String attr1 = leftListModel.get(selectedIndices[j]);
				rightListModel.add(size++, attr1);
				selectedAttributes[j++] = attr1;
			}
			Set<Integer> attributeIndices = new TreeSet<Integer>();
			for(String attr : selectedAttributes) {
				leftListModel.removeElement(attr);
				attributeIndices.add(Integer.parseInt(attr.split("\\.")[0]));
			}
			pcs.firePropertyChange(FILTERS_CHANGED, attributeIndices, HAVING_TO_NOT_HAVING);
		} else if(source.equals(rightToLeftButton)) {
			int[] selectedIndices = notHavingList.getSelectedIndices();
			if(selectedIndices.length == 0) {
				JOptionPane.showMessageDialog(null, "Select one or more attributes to unmask", "Select attribute(s)", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String[] selectedAttributes = new String[selectedIndices.length];
			int j = 0;
			int size = leftListModel.getSize();
			for(int i = 0; i < size && j < selectedIndices.length; i++) {
				String attr1 = rightListModel.get(selectedIndices[j]);
				String attr2 = leftListModel.get(i);
				if(compareAttributes(attr1, attr2) < 0) {
					leftListModel.add(i, attr1);
					selectedAttributes[j] = attr1;
					j++;
					size++;
				}
			}
			while(j < selectedIndices.length) {
				String attr1 = rightListModel.get(selectedIndices[j]);
				leftListModel.add(size++, attr1);
				selectedAttributes[j++] = attr1;
			}
			Set<Integer> attributeIndices = new TreeSet<Integer>();
			for(String attr : selectedAttributes) {
				rightListModel.removeElement(attr);
				attributeIndices.add(Integer.parseInt(attr.split("\\.")[0]));
			}
			pcs.firePropertyChange(FILTERS_CHANGED, attributeIndices, NOT_HAVING_TO_HAVING);
		} else if(source.equals(invertButton)) {
			DefaultListModel<String> temp = rightListModel;
			rightListModel = leftListModel;
			leftListModel = temp;
			havingList.setModel(leftListModel);
			notHavingList.setModel(rightListModel);
			pcs.firePropertyChange(FILTERS_CHANGED, null, INVERSION);
		}
	}

	/**
	 * Compares two attributes as per the order information in the begining of the text (e.g. "1. AttributeI", "2. AttributeJ" etc.)
	 * @param attr1
	 * @param attr2
	 * @return <code>&lt; 0</code>, if <code>attr1</code> comes earlier in the sequence; 
	 * <code>attr2</code> otherwise
	 */
	private int compareAttributes(String attr1, String attr2) {
		int a1 = Integer.parseInt(attr1.substring(0, attr1.indexOf('.')));
		int a2 = Integer.parseInt(attr2.substring(0, attr2.indexOf('.')));
		return (a1 - a2);
	}

	/**
	 * Get the set of masked attributes
	 * @return A {@link Set} of attribute indices which are masked
	 */
	public Set<Integer> getHavingAttributesList() {
		Set<Integer> attributeList = new TreeSet<Integer>();
		for(int i = 0; i < leftListModel.getSize(); i++) {
			String attributeString = leftListModel.getElementAt(i);
			int attributeIndex = Integer.parseInt(attributeString.substring(0, attributeString.indexOf('.')));
			attributeList.add(attributeIndex);
		}
		return attributeList;
	}

	/**
	 * Get the set of unmasked attributes
	 * @return A {@link Set} of attribute indices which are unmasked
	 */
	public Set<Integer> getNotHavingAttributesList() {
		Set<Integer> attributeList = new TreeSet<Integer>();
		for(int i = 0; i < rightListModel.getSize(); i++) {
			String attributeString = rightListModel.getElementAt(i);
			int attributeIndex = Integer.parseInt(attributeString.substring(0, attributeString.indexOf('.')));
			attributeList.add(attributeIndex);
		}
		return attributeList;
	}

	@Override
	public void setInitialPreferences(Map<String, String> preferences) {
		if(preferences != null) {
			String list = preferences.get(FILTERED_ATTRIBUTES_LIST_PREFERENCE);
			if(list != null && list.trim().length() > 2) {
				String[] attributes = list.substring(1, list.length()-1).split(",");
				if(attributes.length > 0) {
					int[] selectedIndices = new int[attributes.length];
					int i = 0;
					for(String attributeIndex : attributes)
						selectedIndices[i++] = Integer.parseInt(attributeIndex.trim()) - 1;
					
					// Imitate the filtering UI action
					havingList.setSelectedIndices(selectedIndices);
					ActionEvent event;
					long when;

					when  = System.currentTimeMillis();
					event = new ActionEvent(leftToRightButton, ActionEvent.ACTION_PERFORMED, null, when, 0);

					for (ActionListener listener : leftToRightButton.getActionListeners()) {
						listener.actionPerformed(event);
					}
				}
			}
		}
	}

}
