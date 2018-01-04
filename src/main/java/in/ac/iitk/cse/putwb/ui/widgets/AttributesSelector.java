package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A widget that can select one or more attributes in a list 
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class AttributesSelector extends JPanel implements ListSelectionListener {
	
	/**
	 * Holds the set of all currently selected attributes
	 */
	private Set<Integer> allSelectedAttributes;
	
	/**
	 * A list to show the attributes
	 */
	private JList<String> attributeList;
	
	/**
	 * Creates an attribute selection widget for the given set of attribute names
	 * @param attributeNames The names of the attributes
	 */
	public AttributesSelector(String[] attributeNames) {
		allSelectedAttributes = new TreeSet<Integer>();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0};
		setLayout(gridBagLayout);
		
		JLabel infoLabel = new JLabel("Select one or more attributes");
		infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_infoLabel = new GridBagConstraints();
		gbc_infoLabel.insets = new Insets(5, 5, 5, 0);
		gbc_infoLabel.gridx = 0;
		gbc_infoLabel.gridy = 0;
		add(infoLabel, gbc_infoLabel);
		
		attributeList = new JList<String>(attributeNames);
		attributeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		attributeList.addListSelectionListener(this);
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.insets = new Insets(0, 5, 5, 5);
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 0;
		gbc_list.gridy = 1;
		add(attributeList, gbc_list);
	}

	/**
	 * Returns the set of selected attributes
	 * @return a {@link Set} of attribute indices; the first attribute has the index 1, and so on
	 */
	public Set<Integer> getAllSelectedAttributes() {
		return allSelectedAttributes;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int[] selectedIndices = attributeList.getSelectedIndices();
		allSelectedAttributes.clear();
		for(int i : selectedIndices)
			allSelectedAttributes.add(i+1);
	}
	
}
