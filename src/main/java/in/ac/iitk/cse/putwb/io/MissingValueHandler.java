/**
 * 
 */
package in.ac.iitk.cse.putwb.io;

import java.util.ArrayList;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WekaException;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

/**
 * @author Saurabh Srivastava
 * Replaces a missing value in a dataset with mean or mode
 */
public class MissingValueHandler {

	/**
	 * The Weka {@link}Instances containing the data
	 */
	private Instances dataset;

	/**
	 * A flag to indicate whether instances with missing values are to be deleted
	 */
	private boolean deleteInstances;
	
	/**
	 * Create a <code>MissingValueHandler</code> for a given dataset
	 * @param dataset The dataset for which miising values are to be handled
	 */
	public MissingValueHandler(Instances dataset) {
		this(dataset, false);
	}

	/**
	 * Create a <code>MissingValueHandler</code> for a given dataset, and an indicator whether to delete such instances or attempt to fill appropriate values
	 * @param dataset The dataset for which miising values are to be handled
	 * @param deleteInstances <code>true</code> implies instances with missing values be deleted; <code>false</code> implies a replacement be tried
	 */
	public MissingValueHandler(Instances dataset, boolean deleteInstances) {
		this.dataset = dataset;
		this.deleteInstances = deleteInstances;
	}
	
	/**
	 * Handles missing values in the dataset. The rows with a missing class attribute is removed from the dataset. 
	 * The rows with missing values are replaced with mean or modes. 
	 * @throws Exception If something goes wrong while attempting to replace values or remove records
	 */ 
	public void handleMissingValues() throws Exception {

		/*
		 * Sanity check
		 */

		// Check if all attributes are nominal or numeric
		for(int i = 0; i < dataset.numAttributes(); i++) {
			Attribute attr = dataset.attribute(i);
			if(!attr.isNominal() && !attr.isNumeric())
				throw new WekaException("Cannot replace values for non-numeric, non nominal attributes");
		}
		
		if(dataset.classIndex() == -1)
			return;

		Attribute classAttribute = dataset.classAttribute();
		if(!classAttribute.isNominal())
			return;

		/*
		 * Remove any rows, where class is not set
		 */
		dataset.deleteWithMissingClass();

		// Check if instances are to be deleted or replacement is requested
		if(deleteInstances) {
			List<Integer> instanceIndicesToRemove = new ArrayList<Integer>();
			for(int i = 0; i < dataset.numInstances(); i++) {
				Instance instance = dataset.get(i);
				if(instance.hasMissingValue())
					instanceIndicesToRemove.add(i);
			}
			for(int i = instanceIndicesToRemove.size()-1; i >=0; i--)
				dataset.remove((int)instanceIndicesToRemove.get(i));
		} else {
			ReplaceMissingValues filter = new ReplaceMissingValues();
			filter.setInputFormat(dataset);
			dataset = Filter.useFilter(dataset, filter);

		}
	}

}
