package in.ac.iitk.cse.putwb.ui.widgets;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

/**
 * A widget that can let users add a set of exceptions - group of attribute indices.
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public class ExceptionsEditor extends JPanel implements ActionListener {
	
	/**
	 * The button for adding a new exception
	 */
	private JButton addButton;
	
	/**
	 * The set of attributes which can be included in an exception
	 */
	private String[] attributeNames;
	
	/**
	 * The list widget containing the exceptions
	 */
	private JList<Set<Integer>> list;
	
	/**
	 * The model for the exception's list widget
	 */
	private DefaultListModel<Set<Integer>> listModel;
	
	/**
	 * The maximum length of an exception
	 */
	private int maxExceptionLength;
	
	/**
	 * The button for removing an existing exception
	 */
	private JButton removeButton;
	
	/**
	 * Creates an exception editor with the given attribute names and maximum exception length
	 * @param attributeNames The names of the attributes
	 * @param maxExceptionLength The maximum length possible for an exception
	 */
	public ExceptionsEditor(String[] attributeNames, int maxExceptionLength) {
		this.maxExceptionLength = maxExceptionLength;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0};
		setLayout(gridBagLayout);
		
		this.attributeNames = new String[attributeNames.length];
		for(int i = 0; i < attributeNames.length; i++)
			this.attributeNames[i] = (i+1) + ".  " + attributeNames[i];
		
		listModel = new DefaultListModel<Set<Integer>>();
		list = new JList<Set<Integer>>(listModel);
		list.setVisibleRowCount(5);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.insets = new Insets(0, 2, 5, 2);
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 0;
		gbc_list.gridy = 0;
		add(list, gbc_list);
		
		JPanel buttonsPanel = new JPanel();
		GridBagConstraints gbc_buttonsPanel = new GridBagConstraints();
		gbc_buttonsPanel.fill = GridBagConstraints.BOTH;
		gbc_buttonsPanel.insets = new Insets(0, 5, 5, 5);
		gbc_buttonsPanel.gridx = 0;
		gbc_buttonsPanel.gridy = 1;
		add(buttonsPanel, gbc_buttonsPanel);
		GridBagLayout gbl_buttonsPanel = new GridBagLayout();
		gbl_buttonsPanel.columnWeights = new double[]{1.0, 1.0};
		gbl_buttonsPanel.rowWeights = new double[]{1.0};
		buttonsPanel.setLayout(gbl_buttonsPanel);
		
		addButton = new JButton("Add New");
		addButton.addActionListener(this);
		GridBagConstraints gbc_addButton = new GridBagConstraints();
		gbc_addButton.anchor = GridBagConstraints.EAST;
		gbc_addButton.insets = new Insets(0, 0, 0, 5);
		gbc_addButton.gridx = 0;
		gbc_addButton.gridy = 0;
		buttonsPanel.add(addButton, gbc_addButton);
		
		removeButton = new JButton("Remove Selected");
		removeButton.addActionListener(this);
		GridBagConstraints gbc_removeButton = new GridBagConstraints();
		gbc_removeButton.anchor = GridBagConstraints.WEST;
		gbc_removeButton.insets = new Insets(0, 5, 0, 0);
		gbc_removeButton.gridx = 1;
		gbc_removeButton.gridy = 0;
		buttonsPanel.add(removeButton, gbc_removeButton);
		
		setBorder(new RoundedLineBorder(Color.GRAY, 3));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(addButton)) {
			AttributesSelector as = new AttributesSelector(attributeNames);
			int option = JOptionPane.showConfirmDialog(null, as, "Create a new exception",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if(option == JOptionPane.OK_OPTION) {
				Set<Integer> exception = as.getAllSelectedAttributes();
				addException(exception);
			}
		} else if(e.getSource().equals(removeButton)) {
			int selectedIndex = list.getSelectedIndex();
			if(selectedIndex == -1) {
				JOptionPane.showMessageDialog(null, "Select an exception to remove");
				return;
			}
			listModel.remove(selectedIndex);
		}
	}

	/**
	 * Adds a given exception to the editor
	 * @param exception A {@link Set} of integers representing the exception
	 */
	public void addException(Set<Integer> exception) {
		if(exception.size() > maxExceptionLength) {
			JOptionPane.showMessageDialog(null, "The exception can contain a maximum of " + maxExceptionLength + " attribute(s)");
			return;
		} else if(listModel.contains(exception)) {
			JOptionPane.showMessageDialog(null, "Exception already exists");
			return;
		} else {
			Set<Integer> temp = checkSubsetException(exception);
			if(temp != null) {
				JOptionPane.showMessageDialog(null, "Exception " + exception.toString() + " is a subset of exception " + temp.toString() + " which already exists");
				return;
			}
		}
		listModel.addElement(exception);
	}
	
	/**
	 * Checks if an exception is already covered by an existing exception
	 * @param exceptionsToCheck The exception to check
	 * @return an existing exception, that covers this exception, <code>null</code> otherwise
	 */
	private Set<Integer> checkSubsetException(Set<Integer> exceptionsToCheck) {
		Set<Set<Integer>> allExceptions = getExceptions();
		for(Set<Integer> exception : allExceptions) {
			if(exceptionsToCheck.containsAll(exception))
				return exception;
		}
		return null;
	}
	
	/**
	 * Returns the (possibly empty) currently added set of exceptions
	 * @return the {@link Set} of exceptions added
	 */
	public Set<Set<Integer>> getExceptions() {
		Set<Set<Integer>> exceptions = new LinkedHashSet<Set<Integer>>();
		for(int i = 0; i < listModel.size(); i++)
			exceptions.add(listModel.getElementAt(i));
		return exceptions;
	}
	
}
