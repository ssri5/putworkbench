package in.ac.iitk.cse.putwb.classify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instances;

/**
 * A Dataset represents a weka {@link Instances} object, along with any String descriptions.<br/>
 * The string descriptions come handy, since we break up one dataset into multiple smaller datasets in course of our experiment.
 * @author Saurabh Srivastava
 *
 */
public class Dataset {
	
	/**
	 * A utility method which enumerates and collects all the possible values for the class attribute in a given dataset
	 * @param dataset The dataset whose classes are to be returned
	 * @return a {@link List} of classes
	 */
	public static List<String> getAllClassesForDataset(Instances dataset) {
		Attribute classAttribute = dataset.classAttribute();
		Enumeration<Object> classEnumerator = classAttribute.enumerateValues();
		List<String> allClasses = new ArrayList<String>();
		while(classEnumerator.hasMoreElements()) {
			allClasses.add(classEnumerator.nextElement().toString());
		}
		return allClasses;
	}
	
	/**
	 * Stores the possible values that the class attribute can take
	 */
	private List<String> classes;

	/**
	 * A string description for the dataset (for fragmented datasets, this is set to the set of attribute indices from the unfragmented dataset)
	 */
	private String description;
	
	/**
	 * The set of rows comprising this dataset
	 */
	private Instances instances;
	
	/**
	 * The number of classes into which instances can be classified
	 */
	private int numOfClasses;

	/**
	 * Creates a new dataset, with given instances and description
	 * @param instances The set of rows in this dataset
	 * @param description A string description of the dataset
	 */
	public Dataset(Instances instances, String description) {
		super();
		this.instances = instances;
		this.description = description;
		classes = getAllClassesForDataset(instances);
		numOfClasses = classes.size();
	}

	/**
	 * Returns a "read-only" list of the values that the class attribute of this dataset can take
	 * @return a {@link List} of class attribute values
	 */
	public List<Object> getClasses() {
		return Collections.unmodifiableList(classes);
	}

	/**
	 * Returns a string description of this dataset
	 * @return the description of this dataset 
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the rows in this dataset 
	 * @return the rows in this dataset in the form of Weka {@link Instances}
	 */
	public Instances getInstances() {
		return instances;
	}
	
	/**
	 * Returns the number of classes into which instances in this dataset can be classified
	 * @return the number of classes
	 */
	public int getNumOfClasses() {
		return numOfClasses;
	}
}
