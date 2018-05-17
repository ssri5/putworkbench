/**
 * 
 */
package in.ac.iitk.cse.putwb.heuristic;

import java.util.Map;

import weka.core.Instances;

/**
 * An abstract class for all subclasses implementing dataset related heuristics.
 * @author Saurabh Srivastava
 *
 */
public abstract class DatasetRelatedHeuristics extends Heuristics {

	/**
	 * Key for providing the dataset
	 */
	public static final String DATASET = "dataset";
	
	/**
	 * Returns the dataset from the supplierd input map
	 * @param inputMap The input map supplied to the heuristic
	 * @return The dataset, if found in the map
	 * @throws IllegalStateException If the map doesn't contain any valid dataset
	 */
	protected Instances getDataset(Map<String, Object> inputMap) throws IllegalStateException {
		Object temp;
		
		temp = inputMap.get(DATASET);
		if(temp == null || !(temp instanceof Instances))
			throw new IllegalStateException("Cannot provide a decision without looking at the dataset");
		return (Instances)temp;
	}
}
