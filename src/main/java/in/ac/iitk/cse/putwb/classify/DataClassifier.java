package in.ac.iitk.cse.putwb.classify;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import in.ac.iitk.cse.putwb.log.BasicLogger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SGD;
import weka.classifiers.functions.SMO;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

/**
 * This class is responsible for performing classification tasks over a given {@link Dataset}, with a chosen classification mechanism.
 * It can also report model accuracies, using <i>k-fold cross validation</i>.
 * @author Saurabh Srivastava
 *
 */
public class DataClassifier {

	/**
	 * Constant to represent the Weka {@link J48} classifier
	 */
	public static final Class<J48> C_J48 = J48.class;

	/**
	 * Constant to represent the Weka {@link MultilayerPerceptron} classifier
	 */
	public static final Class<MultilayerPerceptron> C_MultilayerPerceptron = MultilayerPerceptron.class;

	/**
	 * Constant to represent the Weka {@link NaiveBayes} classifier
	 */
	public static final Class<NaiveBayes> C_NaiveBayes = NaiveBayes.class;

	/**
	 * Constant to represent the Weka {@link RandomForest} classifier
	 */
	public static final Class<RandomForest> C_RandomForest = RandomForest.class;

	/**
	 * Constant to represent the Weka {@link SGD} classifier
	 */
	public static final Class<SGD> C_SGD = SGD.class;

	/**
	 * Constant to represent the Weka {@link SMO} classifier
	 */
	public static final Class<SMO> C_SMO = SMO.class;
	
	/**
	 * Constant to represent the Weka {@link ZeroR} classifier
	 */
	public static final Class<ZeroR> C_ZeroR = ZeroR.class;

	/**
	 * Attempts to find a classifier type, with the name. The classifier must be corresponding to one of the constants defined in {@link DataClassifier}
	 * @param classifierName The name of the classifier
	 * @return The classifier type corresponding to the provided name
	 * @throws IllegalArgumentException If the name doesn't match any of the corresponding classifier constants
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends AbstractClassifier> findClassifierByName(String classifierName) throws IllegalArgumentException {
		Class<? extends DataClassifier> c = DataClassifier.class;
		List<Field> allFields = getAllFields(c);
		for(Field f : allFields) {
			int modifiers = f.getModifiers();
			try {
				if(f.getType().equals(Class.class) && Modifier.isStatic(modifiers) && 
						Modifier.isFinal(modifiers) && f.getName().equals("C_" + classifierName)) {
					return (Class<? extends AbstractClassifier>)f.get(null);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				BasicLogger.getDefaultLogger().exception(e);	// Ideally we shouldn't land here !
			}
		}
		throw new IllegalArgumentException("No matching classifier found for name - " + classifierName);
	}

	/**
	 * A utility method to find the list of fields of a given class, and its super classes
	 * @param type The class
	 * @return A {@link List} of all visible fields in the class and its super classes
	 */
	private synchronized static List<Field> getAllFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();
		for (Class<?> c = type; c != null; c = c.getSuperclass()) {
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
		}
		return fields;
	}

	/**
	 * Returns a {@link List} of available classifier types, based on the defined constants in the {@link Dataset} class
	 * @return a {@link List} of available classifier types valid for usage
	 */
	public static List<String> getClassifierOptions() {
		List<String> options = new ArrayList<String>();
		Class<? extends DataClassifier> c = DataClassifier.class;
		List<Field> allFields = getAllFields(c);
		for(Field f : allFields) {
			int modifiers = f.getModifiers();
			String fName = f.getName();
			if(f.getType().equals(Class.class) && Modifier.isStatic(modifiers) && 
					Modifier.isFinal(modifiers) && fName.startsWith("C_")) {
				options.add(fName.substring(2));
			}
		}
		return options;
	}

	/**
	 * The classifier type to use for classification tasks
	 */
	private Class<? extends AbstractClassifier> classifierType;

	/**
	 * The dataset over which the classification tasks are to be performed
	 */
	private Dataset dataset;

	/**
	 * The Weka model evaluator
	 */
	private Evaluation eval;

	/**
	 * The value of <i>k</i> for k-cross validation
	 */
	private int k;

	/**
	 * The model that is built by the classification task, using all of the rows in the dataset
	 */
	private AbstractClassifier model;

	/**
	 * Any classifier options. They are passed on to the respective weka classifiers
	 * @see AbstractClassifier#setOptions(String[])
	 */
	private String[] options;

	/**
	 * Creates a <code>DataClassifier</code> for given dataset with chosen classifier and 5-fold validation.
	 * @param classifierType The classifier to use (must be one of the options defined as a constant in the class)
	 * @param dataset The dataset to classify
	 */
	public DataClassifier(Class<? extends AbstractClassifier> classifierType, Dataset dataset) {
		this(classifierType, dataset, 5);
	}

	/**
	 * Creates a <code>DataClassifier</code> for given dataset with chosen classification options.
	 * @param classifierType The classifier to use (must be one of the options defined as a constant in the class)
	 * @param dataset The dataset to classify
	 * @param k The value of k for <i>k-fold cross validation</i> 
	 */
	public DataClassifier(Class<? extends AbstractClassifier> classifierType, Dataset dataset, int k) {
		super();
		this.classifierType = classifierType;
		this.dataset = dataset;
		this.k = k;
		model = null;
		options = null;
		eval = null;
		if(!isClassifierValid())
			throw new IllegalArgumentException("Classifier must be of a predefined type");
	}

	/**
	 * Builds the classification model over the dataset with the selected classification mechanism
	 */
	private void buildModel() {

		try {
			setModelType();
			if(options != null)
				model.setOptions(options);
			model.buildClassifier(dataset.getInstances());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Attempts to perform a <i>k-fold cross validation</i> over the dataset for the chosen classifier
	 * @throws Exception if the cross-validation process runs into a glitch
	 */
	public void evaluateModel() throws Exception {
		eval = new Evaluation(dataset.getInstances());
		setModelType();
		eval.crossValidateModel(model, dataset.getInstances(), k, new Random());
	}

	/**
	 * Returns the accuracy of the given model upon <i>k-fold cross validation</i> evaluation
	 * @return the overall accuracy in percentage if the model has been evaluated, -1 otherwise
	 */
	public double getAccuracy() {
		if(eval == null)
			return -1;
		else
			return eval.pctCorrect();
	}

	/**
	 * Returns the classifier type being used by this classifer
	 * @return the classifier type
	 */
	public Class<? extends AbstractClassifier> getClassifierType() {
		return classifierType;
	}
	
	/**
	 * Returns a string describing this dataset
	 * @return A textual description of the dataset
	 */
	public String getDatasetDescription() {
		return dataset.getDescription();
	}
	
	/**
	 * Returns the false negative rates (for every class individually) of the given model upon <i>k-fold cross validation</i>  evaluation
	 * @return an array of false negative rates, corresponding to all classes in the dataset
	 */
	public double[] getFN() {
		double[] results = new double[dataset.getNumOfClasses()];
		if(eval != null) {
			for(int i = 0; i < results.length; i++)
				results[i] = eval.falseNegativeRate(i);
			return results;
		}
		return null;
	}
	
	/**
	 * Returns the false positive rates (for every class individually) of the given model upon <i>k-fold cross validation</i> evaluation
	 * @return an array of false positive rates, corresponding to all classes in the dataset
	 */
	public double[] getFP() {
		double[] results = new double[dataset.getNumOfClasses()];
		if(eval != null) {
			for(int i = 0; i < results.length; i++)
				results[i] = eval.falsePositiveRate(i);
			return results;
		}
		return null;
	}
	
	/**
	 * Build (if not already built) and return the prediction model
	 * @return The <code>PredictionModel</code> representing this classifier
	 */
	public PredictionModel getModel() {
		if(model == null)
			buildModel();
		return new PredictionModel(model);
	}
	
	/**
	 * Get the appropriate options set for the classifier
	 * @return the options, or <code>null</code> if no options have been set for the classifier as yet
	 */
	public String[] getOptions() {
		return options;
	}
	
	/**
	 * Returns the area under the PRC values (for every class individually) of the given model upon <i>k-fold cross validation</i> evaluation
	 * @return an array of area under the PRC values, corresponding to all classes in the dataset
	 */
	public double[] getPRC() {
		double[] results = new double[dataset.getNumOfClasses()];
		if(eval != null) {
			for(int i = 0; i < results.length; i++)
				results[i] = eval.areaUnderPRC(i);
			return results;
		}
		return null;
	}
	
	/**
	 * Returns the precision values (for every class individually) of the given model upon <i>k-fold cross validation</i> evaluation
	 * @return an array of precision values, corresponding to all classes in the dataset
	 */
	public double[] getPrecision() {
		double[] results = new double[dataset.getNumOfClasses()];
		if(eval != null) {
			for(int i = 0; i < results.length; i++)
				results[i] = eval.precision(i);
			return results;
		}
		return null;
	}

	/**
	 * Returns the recall values (for every class individually) of the given model upon <i>k-fold cross validation</i> evaluation
	 * @return an array of recall values, corresponding to all classes in the dataset
	 */
	public double[] getRecall() {
		double[] results = new double[dataset.getNumOfClasses()];
		if(eval != null) {
			for(int i = 0; i < results.length; i++)
				results[i] = eval.recall(i);
			return results;
		}
		return null;
	}

	/**
	 * Returns the area under the ROC values (for every class individually) of the given model upon <i>k-fold cross validation</i> evaluation
	 * @return an array of area under the ROC values, corresponding to all classes in the dataset
	 */
	public double[] getROC() {
		double[] results = new double[dataset.getNumOfClasses()];
		if(eval != null) {
			for(int i = 0; i < results.length; i++)
				results[i] = eval.areaUnderROC(i);
			return results;
		}
		return null;
	}

	/**
	 * Returns the true positive rates (for every class individually) of the given model upon <i>k-fold cross validation</i> evaluation
	 * @return an array of true positive rates, corresponding to all classes in the dataset
	 */
	public double[] getTP() {
		double[] results = new double[dataset.getNumOfClasses()];
		if(eval != null) {
			for(int i = 0; i < results.length; i++)
				results[i] = eval.truePositiveRate(i);
			return results;
		}
		return null;
	}

	/**
	 * Checks if the currently set classifier is one of the allowed types (defined constants)
	 * @return <code>true</code> if the classifier type matches with one of the constants, <code>false</code> otherwise
	 */
	private boolean isClassifierValid() {
		Class<? extends DataClassifier> c = this.getClass();
		List<Field> allFields = getAllFields(c);
		for(Field f : allFields) {
			int modifiers = f.getModifiers();
			try {
				if(f.getType().equals(Class.class) && Modifier.isStatic(modifiers) && 
						Modifier.isFinal(modifiers) && f.getName().startsWith("C_") &&
						f.get(null) == classifierType)
					return true;
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}	
		}
		return false;
	}

	/**
	 * Creates a new instance of the selected classifier to proceed for learning
	 * @throws Exception if something goes wrong while creating the instance of the required classifer
	 */
	private void setModelType() throws Exception {
		model = null;
		if(!isClassifierValid())
			throw new IllegalArgumentException("Classifier must be of a predefined type");
		model = classifierType.getConstructor().newInstance();
	}
	
	/**
	 * Sets the appropriate options for the classifier
	 * @see {@link AbstractClassifier#setOptions(String[])}
	 * @param options the options to set
	 */
	public void setOptions(String[] options) {
		this.options = options;
	}
}
