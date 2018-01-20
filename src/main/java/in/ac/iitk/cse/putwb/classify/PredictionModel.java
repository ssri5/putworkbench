package in.ac.iitk.cse.putwb.classify;

import weka.classifiers.AbstractClassifier;
import weka.core.Instance;

/**
 * This class represents a prediction model that can be used to classify instances
 * @author Saurabh Srivastava
 *
 */
public class PredictionModel {
	
	/**
	 * The classification model
	 */
	private AbstractClassifier model;

	/**
	 * Create a prediction model with the given classifier model
	 * @param model The classifier model to use for predictions
	 */
	public PredictionModel(AbstractClassifier model) {
		super();
		if(model == null)
			throw new NullPointerException("model cannot be null");
		this.model = model;
	}

	/**
	 * Classifies an instance according to the current model
	 * @param instanceToClassify The instance to classify
	 * @return The value for the class
	 * @throws Exception if something goes wrong during th classification process
	 */
	public double predict(Instance instanceToClassify) throws Exception {
		return model.classifyInstance(instanceToClassify);
	}
	
}
