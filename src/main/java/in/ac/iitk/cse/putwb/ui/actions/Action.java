package in.ac.iitk.cse.putwb.ui.actions;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;

import javax.swing.JPanel;

/**
 * An abstract class for creating content that can be shown in an <code>ActionPanel</code>
 * @author Saurabh Srivastava
 *
 */
@SuppressWarnings("serial")
public abstract class Action extends JPanel {

	/**
	 * Used for providing support for changes in properties
	 */
	protected final PropertyChangeSupport pcs;
	
	/**
	 * Creates an empty Action without any content
	 */
	public Action() {
		super();
		setOpaque(false);
		pcs = new PropertyChangeSupport(this);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	/**
	 * Used for setting any initial preferences for this action
	 * @param preferences A {@link Map} of preference to value
	 */
	public abstract void setInitialPreferences(Map<String, String> preferences);
}