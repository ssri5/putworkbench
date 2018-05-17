/**
 * 
 */
package in.ac.iitk.cse.putwb.heuristic;

import java.util.Map;

import in.ac.iitk.cse.putwb.experiment.PUTExperiment;
import weka.core.Instances;

/**
 * Suggests a classifier to use for a given dataset.
 * @author Saurabh Srivastava
 *
 */
public class ClassifierSuggestor extends DatasetRelatedHeuristics {

	/* (non-Javadoc)
	 * @see in.ac.iitk.cse.putwb.heuristic.Heuristics#getDecision(java.util.Map)
	 */
	@Override
	public Object getDecision(Map<String, Object> inputs) throws Exception {
		/*
		 * Heuristic:
		 * If it is a binary classification problem, suggest SGD, otherwise suggest J48
		 */
		Instances dataset = getDataset(inputs);
		if(dataset.classAttribute().numValues() > 2)
			return buildMap(PUTExperiment.CLASSIFIER_SWITCH, "J48");
		else
			return buildMap(PUTExperiment.CLASSIFIER_SWITCH, "SGD");
	}

}
