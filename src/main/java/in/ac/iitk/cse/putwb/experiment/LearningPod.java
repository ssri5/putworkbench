package in.ac.iitk.cse.putwb.experiment;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import in.ac.iitk.cse.putwb.classify.DataClassifier;

/**
 * This class represents a lightweight learning component, that attempts classification task over a given dataset, with a given Classifier
 * @author Saurabh Srivastava
 *
 */
public class LearningPod implements Callable<Stats> {
	
	/**
	 * The {@link DataClassifier} instance to use for classification
	 */
	private DataClassifier classifier;
	
	/**
	 * A String representation of the dataset (the list of attributes from the unfragmented dataset that are included for learning)
	 */
	private String description;
	
	/**
	 * The time taken by the classifier to build and cross validate the model
	 */
	private long time;
	
	/**
	 * Creates a new Learning pod with given dataset and description of the attributes (from the original dataset)
	 * @param classifier The <code>DataClassifier</code> to use
	 * @param description A string description of the attribute indices from the original dataset
	 */
	public LearningPod(DataClassifier classifier) {
		this.classifier = classifier;
		this.description = classifier.getDatasetDescription();
		time = 0;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Stats call() throws Exception {
		long tick = System.nanoTime();
		classifier.evaluateModel();
		time = (System.nanoTime() - tick);
		Stats stat = new Stats();
		stat.setAccuracy(classifier.getAccuracy());
		stat.setTime(time);
		stat.setPartition(getSetFromString());
		stat.setTp(classifier.getTP());
		stat.setFp(classifier.getFP());
		stat.setFn(classifier.getFN());
		stat.setPrecision(classifier.getPrecision());
		stat.setRecall(classifier.getRecall());
		stat.setRoc(classifier.getROC());
		stat.setPr(classifier.getPR());
		return stat;
	}

	/**
	 * A utility method to convert the string representation (a set of attributes) of the fragmented dataset, to an actual set 
	 * @return A {@link Set} of attribute indices
	 */
	private Set<Integer> getSetFromString() {
		Set<Integer> result = new TreeSet<Integer>();
		description = description.replaceAll("\\[", "").replaceAll("\\]", "");
		String[] tokens = description.trim().split(",");
		for(String token : tokens)
			result.add(Integer.parseInt(token.trim()));
		return result;
	}
	
}
