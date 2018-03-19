package in.ac.iitk.cse.putwb.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.ac.iitk.cse.putwb.classify.DataClassifier;
import in.ac.iitk.cse.putwb.classify.Dataset;
import in.ac.iitk.cse.putwb.io.DatasetLoader;
import in.ac.iitk.cse.putwb.log.BasicLogger;
import in.ac.iitk.cse.putwb.partition.PartitionPlan;
import in.ac.iitk.cse.putwb.partition.Partitions;
import in.ac.iitk.cse.putwb.partition.RandomCombinationGenerator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.SGD;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.UnassignedClassException;

/**
 * This class represents a particular Privacy-Utility tradeoff experiment. It involves providing a dataset, setting a number of parameters,
 * running the learning tasks, collecting stats and saving them in an output file.<br/>
 * The usage details can be known by running the class, without any parameters.
 * @author Saurabh Srivastava
 *
 */
public class PUTExperiment {

	/**
	 * A Custom comparator, to keep the partitions in order. Assumes that the underlying sets are {@link TreeSet}
	 */
	public static final Comparator<Set<Integer>> ATTRIBUTE_SET_COMPARATOR = new Comparator<Set<Integer>>() {

		@Override
		public int compare(Set<Integer> o1, Set<Integer> o2) {
			if(o1.size() < o2.size())
				return -1;
			else if(o1.size() > o2.size())
				return 1;
			else if(o1.equals(o2))
				return 0;
			else {
				Iterator<Integer> it1 = o1.iterator();
				Iterator<Integer> it2 = o2.iterator();
				while(it1.hasNext() && it2.hasNext()) {
					int i1 = it1.next();
					int i2 = it2.next();
					if(i1 < i2)
						return -1;
					else if(i1 > i2)
						return 1;
				}
			}
			return 0;
		}
	};

	/**
	 * Switch for providing any custom options for the classifier
	 */
	public static final String CLASSIFIER_OPTIONS_SWITCH = "-co";

	/**
	 * Switch for providing the classifier to use
	 */
	public static final String CLASSIFIER_SWITCH = "-c";

	/**
	 * Switch for providing the (arff) data file
	 */
	public static final String DATA_FILE_SWITCH = "-f";

	/**
	 * Maximum number of datasets that can wait in the queue.<br/>
	 * This is important as the memory available may no longer be enough to hold too many datasets simultaneously.<br/>
	 * If the memory available to the JVM is significantly restricted, reduce this number.
	 */
	private static final int DATASET_READY_QUEUE_SIZE = 1000;

	/**
	 * Switch for the method to handle duplicate rows
	 */
	public static final String DUPLICATE_ROWS_SWITCH = "-dr";

	/**
	 * Switch for providing the combinations generation method
	 */
	public static final String GENERATION_METHOD_SWITCH = "-rand";

	/**
	 * Switch for providing the horizontal expense
	 */
	public static final String H_EXPENSE_SWITCH = "-h";

	/**
	 * Switch for providing the value of <i>k</i> for k-cross validation 
	 */
	public static final String K_CROSS_SWITCH = "-k";

	/**
	 * Switch for the method to handle missing values
	 */
	public static final String MISSING_VALUE_SWITCH = "-mv";

	/**
	 * Switch for providing the put number
	 */
	public static final String OUTPUT_FILE_SWITCH = "-out";

	/**
	 * Switch for providing the partition size
	 */
	public static final String PARTITION_SIZE_SWITCH = "-ps";

	/**
	 * Switch for providing the privacy exceptions
	 */
	public static final String PRIVACY_EXCEPTIONS_SWITCH = "-pex";

	/**
	 * Switch for providing the put number
	 */
	public static final String PUT_NUMBER_SWITCH = "-put";

	/**
	 * A seed parameter for randomization
	 */
	private static long seed;

	/**
	 * Switch for providing the standard error stream to use
	 */
	public static final String STDERR_SWITCH = "-stderr";

	/**
	 * Switch for providing the standard output stream to use
	 */
	public static final String STDOUT_SWITCH = "-stdout";

	/**
	 * Switch for providing the utility exceptions
	 */
	public static final String UTILITY_EXCEPTIONS_SWITCH = "-uex";

	/**
	 * Switch for providing the vertical expense
	 */
	public static final String V_EXPENSE_SWITCH = "-v";

	/**
	 * Contains the version information of the tool
	 */
	public static String versionInfo = "";

	/**
	 * Collects any information from the .properties file
	 */
	static {
		Properties prop = new Properties();
		try {
			prop.load(PUTExperiment.class.getClassLoader().getResourceAsStream(".properties"));
			String version = prop.getProperty("build.version");
			if(version == null || version.trim().length() == 0)
				version = "Unknown";
			String date = prop.getProperty("build.date");
			if(date == null || date.trim().length() == 0)
				date = "Unknown";
			date = "Build Date: " + date;
			versionInfo = version + " (" + date + ")";
		} catch (Exception e) {
			System.err.println("Could not read the properties file. Version Unknonwn.");
		}
	}

	/**
	 * A utility method that maps put number, to number of attributes to choose at a time
	 * @param numOfAttributes Total number of attributes in the dataset
	 * @param putNumber The put number to consider
	 * @return Number of attributes to choose at a time
	 */
	public static int calculatePartitionSize(int numOfAttributes, float putNumber) {
		/*
		 * putNumber = -1   ==>  partition size = 1
		 * putNumber = 1    ==>  partition size = number of attributes
		 */
		float ratio = (putNumber + 1) / 2;
		return (int) Math.floor(1 + ratio * (numOfAttributes - 1));
	}

	/**
	 * Creates a Privacy-Utility tradeoff experiment with the given parameters and logging options 
	 * @param params The commandline arguments provided for this experiment
	 * @return Returns an instance of the Privacy Utility tradeoff experiment
	 */
	public static PUTExperiment createExperiment(String[] params) {
		String filePath = null;
		float putNumber = Float.MIN_VALUE;
		int partitionSize = 0;
		float vExpense = 1.0f;
		float hExpense = 1.0f;
		String privacyExceptions = null;
		String utilityExceptions = null;
		String outputFile = null;
		String classifier = null;
		String classifierOptions = null;
		File stdout = null, stderr = null;
		BasicLogger logger = BasicLogger.getDefaultLogger();
		int k = 5;
		boolean deleteMissing = false;
		boolean removeDuplicates = true;
		boolean useRandomGeneration = false;
		try {
			for(int i = 0; i < params.length; i++) {
				if(params[i].compareToIgnoreCase(DATA_FILE_SWITCH) == 0)
					filePath = params[++i];
				else if(params[i].compareToIgnoreCase(PUT_NUMBER_SWITCH) == 0)
					putNumber = Float.parseFloat(params[++i]);
				else if(params[i].compareToIgnoreCase(PARTITION_SIZE_SWITCH) == 0)
					partitionSize = Integer.parseInt(params[++i]);
				else if(params[i].compareToIgnoreCase(V_EXPENSE_SWITCH) == 0)
					vExpense = Float.parseFloat(params[++i]);
				else if(params[i].compareToIgnoreCase(H_EXPENSE_SWITCH) == 0)
					hExpense = Float.parseFloat(params[++i]);
				else if(params[i].compareToIgnoreCase(PRIVACY_EXCEPTIONS_SWITCH) == 0)
					privacyExceptions = params[++i];
				else if(params[i].compareToIgnoreCase(UTILITY_EXCEPTIONS_SWITCH) == 0)
					utilityExceptions = params[++i];
				else if(params[i].compareToIgnoreCase(OUTPUT_FILE_SWITCH) == 0)
					outputFile = params[++i];
				else if(params[i].compareToIgnoreCase(CLASSIFIER_SWITCH) == 0)
					classifier = params[++i];
				else if(params[i].compareToIgnoreCase(CLASSIFIER_OPTIONS_SWITCH) == 0)
					classifierOptions = params[++i];
				else if(params[i].compareToIgnoreCase(K_CROSS_SWITCH) == 0)
					k = Integer.parseInt(params[++i]);
				else if(params[i].compareToIgnoreCase(MISSING_VALUE_SWITCH) == 0) {
					String missingValueHandlingMethod = params[++i];
					if(missingValueHandlingMethod.compareToIgnoreCase("D") == 0)
						deleteMissing = true;
					else if(missingValueHandlingMethod.compareToIgnoreCase("R") != 0)
						throw new IllegalArgumentException("Illegal option for handling missing values - " + missingValueHandlingMethod);
				} else if(params[i].compareToIgnoreCase(DUPLICATE_ROWS_SWITCH) == 0) {
					String duplicatesHandlingMethod = params[++i];
					if(duplicatesHandlingMethod.compareToIgnoreCase("N") == 0)
						removeDuplicates = false;
					else if(duplicatesHandlingMethod.compareToIgnoreCase("Y") != 0)
						throw new IllegalArgumentException("Illegal option for handling duplicate rows - " + duplicatesHandlingMethod);
				} else if(params[i].compareToIgnoreCase(STDOUT_SWITCH) == 0) {
					stdout = new File(params[++i]);
					if(!(stdout.createNewFile() || stdout.canWrite()))
						throw new IOException("Cannot create/write the standard output file - " + stdout.getAbsolutePath());
				} else if(params[i].compareToIgnoreCase(STDERR_SWITCH) == 0) {
					stderr = new File(params[++i]);
					if(!(stderr.createNewFile() || stderr.canWrite()))
						throw new IOException("Cannot create/write the standard error file - " + stderr.getAbsolutePath());
				} else if(params[i].compareToIgnoreCase(GENERATION_METHOD_SWITCH) == 0) {
					String generationMethodPreference = params[++i];
					if(generationMethodPreference.compareToIgnoreCase("Y") == 0)
						useRandomGeneration = true;
					else if(generationMethodPreference.compareToIgnoreCase("N") != 0)
						throw new IllegalArgumentException("Illegal option for attribute combinations generation method - " + generationMethodPreference);
				} else
					throw new RuntimeException("Invaid option - " + params[i]);
			}
			if(filePath == null)
				throw new RuntimeException("Data file required");
			if(putNumber == Float.MIN_VALUE && partitionSize == 0)
				throw new RuntimeException("Either PUT Number or partition size is required");
			if(classifier == null)
				throw new RuntimeException("Classifier type required");

			if(stdout == null && stderr == null)
				logger = BasicLogger.getDefaultLogger();
			else if(stdout != null && stderr == null)
				logger = BasicLogger.getLogger(stdout);
			else if(stdout == null && stderr != null)
				logger = BasicLogger.getLogger(System.out, new PrintStream(stderr));
			else
				logger = BasicLogger.getLogger(stdout, stderr);
			PUTExperiment experiment;
			if(putNumber != Float.MIN_VALUE)
				experiment = new PUTExperiment(filePath, putNumber, vExpense, hExpense, classifier, k, deleteMissing, removeDuplicates, logger);
			else
				experiment = new PUTExperiment(filePath, partitionSize, vExpense, hExpense, classifier, k, deleteMissing, removeDuplicates, logger);
			if(classifierOptions != null)
				experiment.parseClassifierOptions(classifierOptions);
			if(privacyExceptions != null)
				experiment.parsePrivacyExceptions(privacyExceptions);
			if(utilityExceptions != null)
				experiment.parseUtilityExceptions(utilityExceptions);
			if(outputFile != null)
				experiment.setOutput(outputFile);
			if(useRandomGeneration)
				experiment.setGenerateRandomCombinations(true);
			experiment.setRecoveryInformation(params);
			logger.outln("Created experiment...");
			return experiment;
		} catch (NumberFormatException e) {
			logger.errorln("Problem in parsing numerical value - " + e.getMessage());
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.errorln("Invalid number of arguments. Please provide matching arguments for all the switches.");
		} catch (RuntimeException e) {
			logger.errorln(e.getMessage());
		} catch (Exception e) {
			logger.exception(e);
		}
		return null;
	}

	/**
	 * A utility method, that takes an unfragemented dataset, a partition of its attribute indices, and returns the dataset, 
	 * with reduced number of rows, if required, according to a given proportion
	 * @param original The original dataset to fragment
	 * @param partition A set of attribute indices from the original dataset to include in the partition
	 * @param trimToProportion The proportion of rows to put in the fragmented dataset (a minimum of one row is included for sure from the original dataset)
	 * @return The fragmented dataset
	 */
	private static Instances getFragmentedDataset(Instances original, Set<Integer> partition, float trimToProportion) {
		Instances copy = new Instances(original);
		for(int i = 0, j = 0; i < original.numAttributes() - 1; i++)
			if(!partition.contains(i))
				copy.deleteAttributeAt(i - j++);
		if(trimToProportion < 1.0f) {
			Random r = new Random(seed++);
			int n = original.numInstances();
			int numberOfInstances = (int) Math.ceil(n * trimToProportion);
			// Just a safety net to avoid any surprises because of floating point operations
			if(numberOfInstances == 0)
				numberOfInstances = 1;
			copy.randomize(r);
			for(int i = numberOfInstances; i < n; i++)
				copy.delete(numberOfInstances);
		}
		return copy;
	}

	/**
	 * This utility method calculates the value of the function <i>C(n, k)</i> or <i>"n choose k"</i> for given values of <i>n</i> and <i>k</i>
	 * @param n The value of <i>n</i>
	 * @param k The value of <i>k</i>
	 * @return <i>C(n, k)</i> for given <i>n</i> and <i>k</i>
	 */
	public static BigInteger getNcKValue(int n, int k) {
		if(k == 0)
			return BigInteger.ZERO;
		// Calculate two big integers: n * (n-1) * ... * (n-k+1) and k * (k-1) * ... * 2 * 1
		BigInteger numerator = new BigInteger("" + n);
		int temp = n;
		while(--temp >= (n-k+1))
			numerator = numerator.multiply(new BigInteger("" + temp));
		BigInteger denominator = new BigInteger("" + k);
		temp = k;
		while(--temp > 1)
			denominator = denominator.multiply(new BigInteger("" + temp));
		// Return numerator / denominator
		return (numerator.divide(denominator));
	}

	/**
	 * The main to run this experiment
	 * @param args The commandline arguments for the experiment
	 */
	public static void main(String[] args) {
		BasicLogger defaultLogger = BasicLogger.getDefaultLogger();
		if(args.length == 0) {
			printUsageDetails();
			System.exit(0);
		}
		PUTExperiment experiment = createExperiment(args);
		if(experiment == null) {
			defaultLogger.errorln("Problems in creating experiment. Printing usage details:");
			printUsageDetails();
			System.exit(-1);
		}
		defaultLogger.outln("Running compatibility tests");
		String error = experiment.runCompatibilityTests();
		if(error != null) {
			defaultLogger.errorln(error);
			defaultLogger.outln("Problems in running experiment... aborting");
			System.exit(1);
		}
		defaultLogger.outln("Starting experiment...");
		experiment.startExperimentSync();
		defaultLogger.outln("Done !!");
		defaultLogger.outln("Results saved to - " + experiment.getResultFile().getAbsolutePath());
	}

	/**
	 * Prints a summary of the usage of the experiment class, with explanation of various switches
	 */
	public static void printUsageDetails() {
		System.out.println("-----------------------------------------------");
		System.out.println("  PUTWorkbench " + versionInfo);
		System.out.println("-----------------------------------------------");
		System.out.println("\nUsage options:");
		System.out.println(DATA_FILE_SWITCH + "\t (Required) Path to the (arff) data file, e.g. " + DATA_FILE_SWITCH + " /home/user/data.arff");
		System.out.println(PUT_NUMBER_SWITCH + "\t (Required, if " + PARTITION_SIZE_SWITCH + " is not provided) The privacy -utility tradeoff number, e.g. "+ PUT_NUMBER_SWITCH + " -0.7");
		System.out.println(PARTITION_SIZE_SWITCH + "\t (Required, if " + PUT_NUMBER_SWITCH + " is not provided) The partition size or the number of attributes to choose, e.g. "+ PARTITION_SIZE_SWITCH + " 3");
		System.out.println(CLASSIFIER_SWITCH + "\t (Required) The classifier to use, e.g. "+ CLASSIFIER_SWITCH + " J48");
		System.out.println("\t Currently available options for classifier are:");
		System.out.print("\t");
		for(String option : DataClassifier.getClassifierOptions())
			System.out.print(" \"" + option + "\" ");
		System.out.println();
		System.out.println(V_EXPENSE_SWITCH + "\t (Default: 1.0) The vertical expense budget, e.g. "+ V_EXPENSE_SWITCH + " 0.8");
		System.out.println(GENERATION_METHOD_SWITCH + "\t (Default: 'N') Advise usage of \"random generation of combinations\", instead of conventional \"dictionary order generation\"." +
				"\n\t 'Y' implies \"attempt to generate attribute combinations randomly\" (useful for datasets with large number of attributes);" + 
				"\n\t 'N' implies \"generate attribute combinations in dictionary order\"." + 
				"\n\t e.g. "+ GENERATION_METHOD_SWITCH + " Y");
		System.out.println(H_EXPENSE_SWITCH + "\t (Default: 1.0) The horizontal expense budget, e.g. "+ H_EXPENSE_SWITCH + " 0.3");
		System.out.println(PRIVACY_EXCEPTIONS_SWITCH + "\t A set of privacy exceptions in the form {exception1,exception2...}." + 
				"\n\t Each exception in turn, is expressed in the form [attribute1,attribute2...]. Avoid spaces in between, or include the full string within quotes." + 
				"\n\t Examples:" + 
				" \n\t " + PRIVACY_EXCEPTIONS_SWITCH + " {[1,4,6],[2,4,5],[3,1]}" +
				" \n\t " + PRIVACY_EXCEPTIONS_SWITCH + " \"{[1, 4], [2, 5]}\"");
		System.out.println(UTILITY_EXCEPTIONS_SWITCH + "\t A set of utility exceptions in the form {exception1,exception2...}." + 
				"\n\t Each exception in turn, is expressed in the form [attribute1,attribute2...]. Avoid spaces in between, or include the full string within quotes." + 
				"\n\t Examples:" + 
				" \n\t " + UTILITY_EXCEPTIONS_SWITCH + " {[1,4,6],[2,4,5],[3,1]}" +
				" \n\t " + UTILITY_EXCEPTIONS_SWITCH + " \"{[1, 4], [2, 5]}\"");
		System.out.println(OUTPUT_FILE_SWITCH + "\t (Default: A file named \"results.csv\" in the folder of the data file) The output file, e.g. " + OUTPUT_FILE_SWITCH + " out.txt");
		System.out.println(CLASSIFIER_OPTIONS_SWITCH + "\t Any custom options for the Weka Classifier to use in the form {option1,option2...}." + 
				"\n\t These options are passed \"as provided\" to the classifier's setOptions() method. Refer to the documentation of respective classifers for details." + 
				"\n\t To avoid problems in setting options with space, include the full string within quotes." + 
				"\n\t e.g. (for J48 classifier):" + 
				" \n\t " + CLASSIFIER_OPTIONS_SWITCH + " \"{-U, -C 0.25}\"");
		System.out.println(K_CROSS_SWITCH + "\t (Default: 5) Use this value of \"k\" for k-cross validation, e.g. "+ K_CROSS_SWITCH + " 10");
		System.out.println(MISSING_VALUE_SWITCH + "\t (Default: R) 'R' implies \"replace missing values with mean/mode\"; 'D' implies \"delete rows with missing values\", e.g. "+ MISSING_VALUE_SWITCH + " R");
		System.out.println(DUPLICATE_ROWS_SWITCH + "\t (Default: Y) 'Y' implies \"duplicate rows be removed\"; 'N' implies \"duplicate rows be left as such\", e.g. "+ DUPLICATE_ROWS_SWITCH + " Y");
		System.out.println(STDOUT_SWITCH + "\t (Default: console) Use this for printing standard output messages, e.g. "+ STDOUT_SWITCH + " /tmp/stdout");
		System.out.println(STDERR_SWITCH + "\t (Default: console) Use this for printing standard error messages, e.g. "+ STDERR_SWITCH + " /tmp/stderr");
		System.out.println("\nAdditional notes:");
		System.out.println("1. In case of any conflicts between privacy and utility exceptions, privacy exceptions take precedence.");
		System.out.println("2. If both, PUT number and partition size are provided, partition size is ignored.");
		System.out.println("3. The \"random generation of combinations\" is not used when the number of combinations are low (less than 100,000), or the vertical expense is very high (>0.95)");
		System.out.println("4. Utility exceptions are considered only when attributes are generated in dictionary order, otherwise they are ignored");
	}

	/**
	 * A flag that tells the results collection thread to start waiting for results
	 */
	private boolean allLearningRequestsInQueue;

	/**
	 * A flag that tells if the current experiment is running in asynchronous mode or not (default: <code>false</code>)
	 */
	private boolean asyncExecution = false;

	/**
	 * If the experiment is running in the asynchronous mode, it runs as a part of this thread (default: <code>null</code>)
	 */
	private Thread asyncThread = null;
	
	/**
	 * Contains the partitions being used by this experiment
	 */
	private Set<Set<Integer>> attributePartitions = null;

	/**
	 * The number of processors available for use on the machine over which the experiment is being run
	 */
	private int availableProcessors;

	/**
	 * Any custom options to be set for the use of the Weka classifier.
	 * These are passed on directly to the <code>setOptions(String[])</code> method of the classifier for processing.
	 * @see AbstractClassifier#setOptions(String[]) 
	 */
	private String[] classifierOptions;

	/**
	 * The classifier type to use for this experiment.<br/> 
	 * The allowed types could be one of the pre-defined constants.
	 */
	private Class<? extends AbstractClassifier> classifierType;

	/**
	 * The original, unfragmented dataset, over which the experiment is designed
	 */
	private Instances dataset;

	/**
	 * A queue to keep fragmented datasets, ready to be used for classification tasks
	 */
	private BlockingQueue<Dataset> datasetsReadyQueue;

	/**
	 * Use random combinations instead of systematic generation and pruning
	 */
	private boolean generateRandomCombinations;

	/**
	 * The horizontal expense for this experiment
	 */
	private float hExpense;

	/**
	 * The value of <i>k</i> to use for k-cross validation
	 */
	private int k;

	/**
	 * The thread pool executor for learning requests
	 */
	private ThreadPoolExecutor learningExecutor;

	/**
	 * The thread that does progress monitoring of learning tasks completion
	 */
	private Thread learningProgressMonitor;

	/**
	 * The thread that does progress monitoring of learning requests creation
	 */
	private Thread learningRequestCreator;

	/**
	 * The logger for this experiment
	 */
	private BasicLogger logger;

	/**
	 * The number of attributes (except the class attribute) in the unfragmented dataset
	 */
	private int numOfAttributes;

	/**
	 * The number of datasets in queue waiting to be processed
	 */
	private volatile long numOfDatasetsInQueue;

	/**
	 * The number of partitioned datasets ready for creating learning requests
	 */
	private volatile long numOfPartitionedDatasets;

	/**
	 * The number of result lines already written to the results file
	 */
	private volatile long numOfResultsWrittenToFile;

	/**
	 * The number of learning requests completed till now
	 */
	private volatile long numOfTasksCompleted;

	/**
	 * The number of learning requests waiting in queue to be processed
	 */
	private volatile long numOfTasksInLearningQueue;

	/**
	 * The thread pool executor for partitioning the dataset
	 */
	private ThreadPoolExecutor partitioningExecutor;

	/**
	 * A flag that tells the learning task manager thread whether more fragmented datasets will be added to the queue or not
	 */
	private boolean partitioningOn;

	/**
	 * The thread that does progress monitoring of dataset partitioning
	 */
	private Thread partitioningProgressMonitor;

	/**
	 * The size of a partition (number of attributes to group together for a learning task)
	 */
	private int partitionSize;

	/**
	 * A set of privcacy exceptions for the experiment
	 */
	private Set<Set<Integer>> privacyExceptions;

	/**
	 * An array of strings representing this experiment's initial state used for recovering from interruptions.
	 * For CLI invocations, this array contains the passed parameters <i>in order</i> they were provided.
	 * By default, creating the experiment via {@link #createExperiment(String[])} method makes sure that this is set to a proper value.
	 * However, if the experiment is created by directly invoking the constructors, the responsibility of setting this lies with the invoker.
	 * If this is not set before starting the experiment, this experiment cannot be recovered in case of an abrupt failure.
	 */
	private String[] recoveryInformation;

	/**
	 * The result file to which the final statistics will be saved
	 */
	private File resultFile;

	/**
	 * A {@link List} to hold {@link Future} results of the learning tasks
	 */
	private List<Future<Stats>> results;

	/**
	 * A recovery manager for this experiment
	 */
	private RecoveryManager rm;

	/**
	 * A {@link List} to hold the statistics related to all the learning tasks
	 */
	private List<Stats> stats;

	/**
	 * Total number of learning tasks this experiment spawns
	 */
	private volatile long totalTasks;

	/**
	 * A set of utility exceptions for the experiment
	 */
	private Set<Set<Integer>> utilityExceptions;

	/**
	 * The vertical expense for this experiment
	 */
	private float vExpense;

	/**
	 * Create a new instance of the Privacy-Utility tradeoff experiment
	 * @param filePath The path to the (arff) data file
	 * @param putNumber The put number to use for the experiment
	 * @param vExpense The vertical expense to use for the experiment
	 * @param hExpense The horizontal to use for the experiment
	 * @param classifierName The classifier type to use for the experiment
	 * @param k The value of <i>k</k> for k-cross validation
	 * @param deleteMissing Indicates whether to delete instances with missing values, or fill values
	 * @throws Exception If something goes wrong while creating the experiment
	 */
	public PUTExperiment(String filePath, float putNumber, float vExpense, float hExpense, String classifierName, int k, boolean deleteMissing, boolean removeDuplicates, BasicLogger logger) throws Exception {
		File dataFile = new File(filePath);
		if(!dataFile.exists())
			throw new FileNotFoundException("Data file not found - " + filePath);
		dataset = DatasetLoader.loadAndCleanDataset(filePath, deleteMissing, removeDuplicates);
		numOfAttributes = dataset.numAttributes() - 1;	// Counting out the class attribute
		classifierType = DataClassifier.findClassifierByName(classifierName);
		if(putNumber < -1 || putNumber > 1)
			throw new IllegalArgumentException("PUT number must be between -1 and 1");
		partitionSize = calculatePartitionSize(numOfAttributes, putNumber);
		if(vExpense <= 0 || vExpense > 1)
			throw new IllegalArgumentException("Vertical expense must be >0 and <=1");
		this.vExpense = vExpense;
		if(hExpense <= 0 || hExpense > 1)
			throw new IllegalArgumentException("Horizontal expense must be >0 and <=1");
		this.hExpense = hExpense;
		this.logger = logger;
		privacyExceptions = new TreeSet<Set<Integer>>(ATTRIBUTE_SET_COMPARATOR);
		utilityExceptions = new TreeSet<Set<Integer>>(ATTRIBUTE_SET_COMPARATOR);
		resultFile = new File(dataFile.getParent(), "results.csv");
		this.k = k;
		availableProcessors = Runtime.getRuntime().availableProcessors();
		generateRandomCombinations = false;
		totalTasks = numOfPartitionedDatasets = numOfDatasetsInQueue = numOfTasksInLearningQueue = numOfTasksCompleted = numOfResultsWrittenToFile = Long.MIN_VALUE;
		datasetsReadyQueue = new ArrayBlockingQueue<Dataset>(DATASET_READY_QUEUE_SIZE);
		stats = new ArrayList<Stats>();
	}

	/**
	 * Create a new instance of the Privacy-Utility tradeoff experiment
	 * @param filePath The path to the (arff) data file
	 * @param partitionSize The partition size to use for the experiment
	 * @param vExpense The vertical expense to use for the experiment
	 * @param hExpense The horizontal to use for the experiment
	 * @param classifierName The classifier type to use for the experiment
	 * @param k The value of <i>k</k> for k-cross validation
	 * @param deleteMissing Indicates whether to delete instances with missing values, or fill values
	 * @throws Exception If something goes wrong while creating the experiment
	 */
	public PUTExperiment(String filePath, int partitionSize, float vExpense, float hExpense, String classifierName, int k, boolean deleteMissing, boolean removeDuplicates, BasicLogger logger) throws Exception {
		File dataFile = new File(filePath);
		if(!dataFile.exists())
			throw new FileNotFoundException("Data file not found - " + filePath);
		dataset = DatasetLoader.loadAndCleanDataset(filePath, deleteMissing, removeDuplicates);
		numOfAttributes = dataset.numAttributes() - 1;	// Counting out the class attribute
		classifierType = DataClassifier.findClassifierByName(classifierName);
		if(partitionSize < 1 || partitionSize > numOfAttributes)
			throw new IllegalArgumentException("Partition size must be between 1 and " + numOfAttributes);
		this.partitionSize = partitionSize;
		if(vExpense <= 0 || vExpense > 1)
			throw new IllegalArgumentException("Vertical expense must be >0 and <=1");
		this.vExpense = vExpense;
		if(hExpense <= 0 || hExpense > 1)
			throw new IllegalArgumentException("Horizontal expense must be >0 and <=1");
		this.hExpense = hExpense;
		this.logger = logger;
		privacyExceptions = new TreeSet<Set<Integer>>(ATTRIBUTE_SET_COMPARATOR);
		utilityExceptions = new TreeSet<Set<Integer>>(ATTRIBUTE_SET_COMPARATOR);
		resultFile = new File(dataFile.getParent(), "results.csv");
		this.k = k;
		availableProcessors = Runtime.getRuntime().availableProcessors();
		totalTasks = numOfPartitionedDatasets = numOfDatasetsInQueue = numOfTasksInLearningQueue = numOfTasksCompleted = numOfResultsWrittenToFile = Long.MIN_VALUE;
		datasetsReadyQueue = new ArrayBlockingQueue<Dataset>(DATASET_READY_QUEUE_SIZE);
		stats = new ArrayList<Stats>();
	}

	/**
	 * Add a privacy exception for this experiment
	 * @param exception A {@link Set} of attribute indices from the original dataset
	 */
	public void addPrivacyException(Set<Integer> exception) {
		if(exception.size() > 0)
			privacyExceptions.add(exception);
	}

	/**
	 * Add a privacy exception for this experiment from a string representation in the form [attribute1,attribute2...],
	 * where each attribute is an attribute index from the original dataset
	 * @param exceptionStr The exception string to parse
	 */
	public void addPrivacyException(String exceptionStr) {
		exceptionStr = exceptionStr.replaceAll("\\s", "");	// Remove any extra spaces, if any
		Pattern p = Pattern.compile("\\[([(\\d)+,]*[(\\d)+])?\\]");
		Matcher m = p.matcher(exceptionStr);
		if(!m.matches())
			throw new IllegalArgumentException("Invalid exception string - " + exceptionStr);
		p = Pattern.compile("(\\d)+");
		// Keep finding integers, till there are more
		m = p.matcher(exceptionStr);
		Set<Integer> exception = new TreeSet<Integer>();
		while(m.find()) {
			Integer attribute = Integer.parseInt(m.group());
			exception.add(attribute);
		}
		addPrivacyException(exception);
	}

	/**
	 * Add a utility exception for this experiment<br/>
	 * NOTE: In case of any conflicts between privacy and utility exceptions, privacy exceptions take precedence
	 * @param exception A {@link Set} of attribute indices from the original dataset
	 */
	public void addUtilityException(Set<Integer> exception) {
		if(exception.size() > 0)
			utilityExceptions.add(exception);
	}

	/**
	 * Add a utility exception for this experiment from a string representation in the form [attribute1,attribute2...],
	 * where each attribute is an attribute index from the original dataset<br/>
	 * NOTE: In case of any conflicts between privacy and utility exceptions, privacy exceptions take precedence
	 * @param exceptionStr The exception string to parse
	 */
	public void addUtilityException(String exceptionStr) {
		exceptionStr = exceptionStr.replaceAll("\\s", "");	// Remove any extra spaces, if any
		Pattern p = Pattern.compile("\\[([(\\d)+,]*[(\\d)+])?\\]");
		Matcher m = p.matcher(exceptionStr);
		if(!m.matches())
			throw new IllegalArgumentException("Invalid exception string - " + exceptionStr);
		p = Pattern.compile("(\\d)+");
		// Keep finding integers, till there are more
		m = p.matcher(exceptionStr);
		Set<Integer> exception = new TreeSet<Integer>();
		while(m.find()) {
			Integer attribute = Integer.parseInt(m.group());
			exception.add(attribute);
		}
		addUtilityException(exception);
	}

	/**
	 * Waits for all learning tasks to finish or move into learning queue, and then collects (waiting if required) the stats for all the learning tasks
	 * @throws InterruptedException If the stats collection thread is interrupted
	 * @throws ExecutionException If a problem is encountered while retrieving the results of a learning task
	 */
	private void collectStats() throws InterruptedException, ExecutionException {
		// Wait for all learning requests to go in queue
		int index = 0;
		do {
			Thread.sleep(500);
			if(allLearningRequestsInQueue)
				break;
			int size = results.size();
			while(index < size) {
				Stats stat = results.get(index++).get();
				stats.add(stat);
				try {
					rm.printStats(stat);
				} catch (IllegalStateException | IOException e) {
					logger.exception(e);
					logger.errorln("Error in writing recovery information");
				}
			}
		} while(true);

		while(index < results.size()) {
			Stats stat = results.get(index++).get();
			stats.add(stat);
			try {
				rm.printStats(stat);
			} catch (IllegalStateException | IOException e) {
				logger.exception(e);
				logger.errorln("Error in writing recovery information");
			}
		}
	}
	
	/**
	 * Creates a thread which manages fragmentation of the original dataset into a number of smaller datasets, which are then used for learning
	 * @throws Exception If the thread managing the fragmentation process runs into a fault during execution
	 */
	private void createDatasets() throws Exception {
		partitioningOn = true;
		partitioningExecutor = new ThreadPoolExecutor(availableProcessors+1, availableProcessors+1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		for(Set<Integer> partition : attributePartitions) {
			Runnable datasetPartitioner = new Runnable() {
				@Override
				public void run() {
					try {
						Instances data = getFragmentedDataset(dataset, partition, hExpense);
						Dataset dataset = new Dataset(data, partition.toString());
						datasetsReadyQueue.put(dataset);
					} catch (InterruptedException e) {
						if(!asyncExecution) {
							logger.errorln("Fatal Error - problem in creating partitioned datasets. Exiting.");
							logger.exception(e);
							System.exit(-1);
						}
					}
				}
			};
			partitioningExecutor.submit(datasetPartitioner);
		}
		partitioningExecutor.shutdown();

		partitioningProgressMonitor = new Thread("Partioning Progress Monitor") {
			@Override
			public void run() {
				try {
					numOfPartitionedDatasets = numOfDatasetsInQueue = 0;
					do {
						numOfPartitionedDatasets = partitioningExecutor.getCompletedTaskCount();
						numOfDatasetsInQueue = datasetsReadyQueue.size();
						String msg1 = "Dataset partitioning tasks - " + numOfPartitionedDatasets + " completed out of " + partitioningExecutor.getTaskCount();
						String msg2 = "Dataset Ready Queue - " + numOfDatasetsInQueue + " datasets waiting in queue to be processed";
						logger.outln("------------------------------------------------------\n### " + msg1 + "\n### " + msg2  + "\n------------------------------------------------------");
					} while(!partitioningExecutor.awaitTermination(1500, TimeUnit.MILLISECONDS));
					partitioningOn = false;
					logger.outln("Dataset partitioning completed !!");
					numOfPartitionedDatasets = totalTasks;
				} catch (InterruptedException e) {
					if(!asyncExecution) {
						logger.errorln("Something went wrong with the " + this.getName());
						logger.exception(e);
					}
					// The progress monitor crashed.. we'll have to signal the learning thread as well to not wait indefinitely
					partitioningOn = false;
				}
			}
		};
		partitioningProgressMonitor.start();
	}
	
	/**
	 * Creates a thread which manages learning tasks for this experiment
	 */
	private void createLearningRequests() {
		allLearningRequestsInQueue = false;
		learningExecutor = new ThreadPoolExecutor(availableProcessors+1, availableProcessors+1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		learningRequestCreator = new Thread("Learning Requests Creator") {
			@Override
			public void run() {
				do {
					try {
						Dataset dataset = datasetsReadyQueue.poll(2500, TimeUnit.MILLISECONDS);
						if(dataset == null && !partitioningOn)
							break;
						else if(dataset != null) {
							DataClassifier classifier = new DataClassifier(classifierType, dataset, k);
							classifier.setOptions(classifierOptions);
							LearningPod pod = new LearningPod(classifier);
							results.add(learningExecutor.submit(pod));
						}
					} catch (InterruptedException e) {
						if(!asyncExecution) {
							logger.errorln("Fatal Error - problem in creating learning requests. Exiting.");
							logger.exception(e);
							System.exit(-1);
						}
					}
				} while(true);
				learningExecutor.shutdown();
			}
		};
		learningRequestCreator.start();

		learningProgressMonitor = new Thread("Learning Progress Monitor") {
			@Override
			public void run() {
				try {
					numOfTasksCompleted = numOfTasksInLearningQueue = 0;
					do {
						numOfTasksCompleted = learningExecutor.getCompletedTaskCount();
						numOfTasksInLearningQueue = learningExecutor.getQueue().size();

						String msg1 = "Learning tasks - " + numOfTasksCompleted + " completed till now";
						String msg2 = "Learning tasks waiting in queue - " + numOfTasksInLearningQueue;
						logger.outln("------------------------------------------------------\n*** " + msg1 + "\n*** " + msg2 + "\n------------------------------------------------------");
					} while(!learningExecutor.awaitTermination(1500, TimeUnit.MILLISECONDS));
					allLearningRequestsInQueue = true;
					do {
						numOfTasksCompleted = learningExecutor.getCompletedTaskCount();
						String msg1 = "Learning tasks - " + numOfTasksCompleted + " completed till now";
						logger.outln("------------------------------------------------------\n*** " + msg1 + "\n------------------------------------------------------");
					} while(!learningExecutor.awaitTermination(1500, TimeUnit.MILLISECONDS));
				} catch (InterruptedException e) {
					if(!asyncExecution) {
						logger.errorln("Something went wrong with the " + this.getName());
						logger.exception(e);
					}
				}
			}
		};
		learningProgressMonitor.start();
	}
	
	/**
	 * Generates the paritions according to a given {@link PartitionPlan}
	 * @param plan The partition plan to use
	 * @throws Exception If something goes wrong while generating the partitions or writing them to the recovery file
	 */
	private void generatePartitions(PartitionPlan plan) throws Exception {
		attributePartitions = Partitions.generatePartitions(plan, logger).getPartitions();
		totalTasks = attributePartitions.size();
		logger.outln("Number of partitions to generate - " + totalTasks);
		writePartitionsForRecovery(plan.isGenerateRandomly());
//		writePartitionsForRecoveryTest(plan.isGenerateRandomly());
	}

	/**
	 * A <i>status indicator</i> while the experiment is running - The number of datasets in queue waiting to be processed
	 * @return number of processed datasets
	 */
	public long getNumOfDatasetsInQueue() {
		if(numOfDatasetsInQueue == Long.MIN_VALUE)
			throw new IllegalStateException("The experiment is either not running or this data is currently not available");
		return numOfDatasetsInQueue;
	}
	
	/**
	 * A <i>status indicator</i> while the experiment is running - The number of partitioned datasets ready for creating learning requests
	 * @return number of partitioned datasets
	 */
	public long getNumOfPartitionedDatasets() {
		if(numOfPartitionedDatasets == Long.MIN_VALUE)
			throw new IllegalStateException("The experiment is either not running or this data is currently not available");
		return numOfPartitionedDatasets;
	}
	
	/**
	 * A <i>status indicator</i> while the results of the experiment are being written to the result file - The number of data items already written
	 * @return number of lines written in the result file
	 */
	public long getNumOfResultsWrittenToFile() {
		if(numOfResultsWrittenToFile == Long.MIN_VALUE)
			throw new IllegalStateException("The experiment is either not running or this data is currently not available");
		return numOfResultsWrittenToFile;
	}
	
	/**
	 * A <i>status indicator</i> while the experiment is running - The number of learning requests completed till now
	 * @return number of learning requests completed
	 */
	public long getNumOfTasksCompleted() {
		if(numOfTasksCompleted == Long.MIN_VALUE)
			throw new IllegalStateException("The experiment is either not running or this data is currently not available");
		return numOfTasksCompleted;
	}
	
	/**
	 * A <i>status indicator</i> while the experiment is running - The number of learning requests waiting in queue to be processed
	 * @return number of learning requests waiting in queue
	 */
	public long getNumOfTasksInLearningQueue() {
		if(numOfTasksInLearningQueue == Long.MIN_VALUE)
			throw new IllegalStateException("The experiment is either not running or this data is currently not available");
		return numOfTasksInLearningQueue;
	}

	/**
	 *  If set, returns the required recovery information for the initial state of this experiment. Otherwise, returns <code>null</code>
	 * @return the recoveryInformation An array of recovery information
	 */
	public String[] getRecoveryInformation() {
		return recoveryInformation;
	}

	/**
	 * Returns the result file to use for this experiment<br/>
	 * By default, it is a file called "results.csv" in the same folder as the data file.
	 * @return the result file 
	 */
	public File getResultFile() {
		return resultFile;
	}

	/**
	 * Returns the number of learning tasks this experiment spawns
	 * @return number of learning tasks
	 */
	public long getTotalTasks() {
		if(totalTasks == Long.MIN_VALUE)
			throw new IllegalStateException("The experiment is either not running or this data is currently not available");
		return totalTasks;
	}

	/**
	 * Initiates recovery tasks for this experiment. This includes creation of a recovery file and printing header (recovery) information.
	 */
	private void initiateRecoveryTasks() {
		// Recovery Tasks
		try {
			String recoveryFileName = resultFile.getName();
			if(recoveryFileName.contains("."))
				recoveryFileName = recoveryFileName.substring(0, recoveryFileName.lastIndexOf(".")) + "_recovery.putr";
			else
				recoveryFileName = recoveryFileName + "_recovery.putr";
			File recoveryFile = new File(resultFile.getParent(), recoveryFileName);
			rm = new RecoveryManager(recoveryFile);
			logger.outln("Recovery File: " + recoveryFile.getAbsolutePath());
			logger.outln("If the experiment gets terminated prematurely, use this recovery file to resume from the last saved state.");
			rm.printHeader(recoveryInformation);
		} catch (FileNotFoundException e) {
			logger.exception(e);
			logger.outln("Could not create a recovery file for this experiment");
			rm = null;
		} catch (IOException e) {
			logger.exception(e);
			logger.outln("Error in writing recovery file for this experiment");
			rm = null;
		}
	}

	/**
	 * parse the classifier options provided at the commandline in the form {option1,option2...}, to be passed on to the Weka classifier
	 * @param classifierOptions The options string to parse
	 */
	private void parseClassifierOptions(String classifierOptions) {
		Pattern p = Pattern.compile("\\{.*\\}");
		Matcher m = p.matcher(classifierOptions);
		if(!m.matches())
			throw new IllegalArgumentException("Invalid format for classifier options - " + classifierOptions);
		// Remove braces
		classifierOptions = classifierOptions.substring(1, classifierOptions.length()-1);
		String[] options = classifierOptions.split(",");
		for(int i = 0; i < options.length; i++)
			options[i] = options[i].trim();
		setClassifierOptions(options);
	}

	/**
	 * Parses the set of privacy exceptions, in the format {exception1,exception2...}
	 * @param exceptionsStr The exception string to parse
	 */
	private void parsePrivacyExceptions(String exceptionsStr) {
		Pattern p = Pattern.compile("\\{.*\\}");
		Matcher m = p.matcher(exceptionsStr);
		if(!m.matches())
			throw new IllegalArgumentException("Invalid format for privacy exceptions - " + exceptionsStr);
		p = Pattern.compile("\\[[^\\]]*\\]");
		// Keep finding exceptions, till there are more
		m = p.matcher(exceptionsStr);
		while(m.find()) {
			try {
				String exception = m.group();
				addPrivacyException(exception);
				logger.outln("Added privacy exception - " + exception);
			} catch(IllegalArgumentException ex) {
				System.err.println(ex.getMessage());
			}
		}
	}

	/**
	 * Parses the set of utility exceptions, in the format {exception1,exception2...}<br/>
	 * NOTE: In case of any conflicts between privacy and utility exceptions, privacy exceptions take precedence
	 * @param exceptionsStr The exception string to parse
	 */
	private void parseUtilityExceptions(String exceptionsStr) {
		Pattern p = Pattern.compile("\\{.*\\}");
		Matcher m = p.matcher(exceptionsStr);
		if(!m.matches())
			throw new IllegalArgumentException("Invalid format for utility exceptions - " + exceptionsStr);
		p = Pattern.compile("\\[[^\\]]*\\]");
		// Keep finding exceptions, till there are more
		m = p.matcher(exceptionsStr);
		while(m.find()) {
			try {
				String exception = m.group();
				addUtilityException(exception);
				logger.outln("Added utility exception - " + exception);
			} catch(IllegalArgumentException ex) {
				logger.errorln(ex.getMessage());
			}
		}
	}

	/**
	 * Resumes the current experiment after the partition generation stage.
	 * This method is supposed to be invoked from the {@link RecoveryManager}.
	 * @param partitions The generated partitions to use
	 * @param existingStats Any stats that are already collected
	 * @param isRandomlyGenerated If <code>true</code>, signifies that the partitions were generated in random order, 
	 * <code>false</code> signifies that the partitions were generated in dictionary order 
	 */
	public void resumeExperimentAfterGenerationStage(Set<Set<Integer>> partitions, List<Stats> existingStats, boolean isRandomlyGenerated) {
		seed = 1;

		totalTasks = numOfPartitionedDatasets = numOfDatasetsInQueue = numOfTasksInLearningQueue = numOfTasksCompleted = Long.MIN_VALUE;
		
		initiateRecoveryTasks();
		
		if (partitions.size() > existingStats.size()) {
			logger.outln("Resuming the experiment after the partition generation stage");
			logger.outln("Total tasks in the experiment: " + partitions.size());
			logger.outln("Number of completed tasks recovered: " + existingStats.size());
			logger.outln("Number of tasks remaining: " + (partitions.size() - existingStats.size()));
			try {
				// Remove the partitions whose results are already collected,
				// write these stats to the new recovery file and
				// add them to the collected stats
				
				for(Stats s : existingStats)
					partitions.remove(s.getPartition());
				attributePartitions = partitions;
				writePartitionsForRecovery(isRandomlyGenerated);
				for(Stats s : existingStats) {
					rm.printStats(s);
					stats.add(s);
				}
				createDatasets();
			} catch (Exception e) {
				logger.errorln(
						"Fatal Error - problem in resuming experiment, could not create partitioned datasets. Exiting.");
				logger.exception(e);
				System.exit(-1);
			}
			// Create learning requests
			results = Collections.synchronizedList(new ArrayList<Future<Stats>>());
			createLearningRequests();
			// Collect results and stats
			try {
				stats = new ArrayList<Stats>();
				collectStats();
			} catch (InterruptedException | ExecutionException e) {
				logger.errorln("Fatal Error - problem in collecting learning statistics");
				logger.exception(e);
				System.exit(-1);
			} 
		} else {
			// Just write the stats properly to the result file
			logger.outln("All learning tasks recovered, saving results !!");
			stats = existingStats;
		}
		
		// Sort the results according to accuracy
		Stats.sortList(stats, Stats.ACCURACY, true, null);

		writeResultsToFile();
	}

	/**
	 * Resumes the current experiment from the partition generation stage.
	 * If the partitions were generated via random generation method, any partitions that were recovered can be reused.
	 * @param numberOfCombinationsToGenerate
	 * @param recoveredPartitions The set of partitions recovered from the recovery file
	 * @param isRandomlyGenerated If <code>true</code>, signifies that the partitions were generated in random order, 
	 * <code>false</code> signifies that the partitions were generated in dictionary order 
	 */
	public void resumeExperimentFromGenerationStage(int numberOfCombinationsToGenerate, Set<Set<Integer>> recoveredPartitions, boolean isRandomlyGenerated) {
		seed = 1;

		totalTasks = numOfPartitionedDatasets = numOfDatasetsInQueue = numOfTasksInLearningQueue = numOfTasksCompleted = Long.MIN_VALUE;
		
		initiateRecoveryTasks();
		
		logger.outln("Resuming the experiment from the partition generation stage");
		
		try {
			if(isRandomlyGenerated) {	// Use the already generated partitions
				logger.outln("The experiment was using random generation method");
				logger.outln("Reusing " + recoveredPartitions.size() + " recovered partitions");
				logger.outln("Generating remaining " + (numberOfCombinationsToGenerate - recoveredPartitions.size()) + " partitions");
				attributePartitions = RandomCombinationGenerator.generateRandomCombinations(numOfAttributes, partitionSize, numberOfCombinationsToGenerate, privacyExceptions, recoveredPartitions);
				logger.outln("Total number of partitions generated:" + attributePartitions.size());
				totalTasks = attributePartitions.size();
				writePartitionsForRecovery(true);
				createDatasets();
			} else {	// Just restart it.. nothing much can be done !
				logger.outln("The experiment was using dictionary order generation method, restarting the experiment !!");
				startExperiment();
				return;
			}
		} catch (Exception e) {
			logger.errorln("Fatal Error - problem in resuming experiment, could not create partitioned datasets. Exiting.");
			logger.exception(e);
			System.exit(-1);
		}
		
		// Create learning requests
		results = Collections.synchronizedList(new ArrayList<Future<Stats>>());
		createLearningRequests();

		// Collect results and stats
		try {
			stats = new ArrayList<Stats>();
			collectStats();
		} catch (InterruptedException | ExecutionException e) {
			logger.errorln("Fatal Error - problem in collecting learning statistics");
			logger.exception(e);
			System.exit(-1);
		}

		// Sort the results according to accuracy
		Stats.sortList(stats, Stats.ACCURACY, true, null);

		writeResultsToFile();
	}

	/**
	 * Runs a set of compatibility tests to check current experiment configurations.<br/>
	 * This method can be used to perform any checks over the dataset and parameters, before starting the experiment.<br/>
	 * The method is <b>not</b> invoked implicitly, and must be called explicitly before starting the experiment.
	 * Currently the following tests are performed in the method:
	 * <ol>
	 * 	<li>Check if the dataset's class attribute is set or not.</li>
	 * 	<li>Check if the class attribute is nominal or not.</li>
	 *	<li>Check that the number of attribute combinations to generate (without privacy exceptions) is not larger than the
	 *		maximum value of integer - {@link Integer#MAX_VALUE}
	 * 	<li>If the selected Classifier is <i>SGD</i>, the dataset must not have more than two classes.
	 * </ol>
	 * @return An error message and/or warning indicating a problem (the first one detected in the sequence), 
	 * <code>null</code> if no problems occurred while running the tests
	 */
	public String runCompatibilityTests() {
		/*
		 * 1. Check for class attribute's presence
		 */
		Attribute classAttribute = null;
		try {
			classAttribute = dataset.classAttribute();
		} catch(UnassignedClassException e) {
			return "No class attribute in the dataset";
		}

		/*
		 * 2. Check if the class is nominal or not
		 */
		if(!classAttribute.isNominal())
			return "The class attribute must be nominal";


		/*
		 * ##########################################################
		 * 				Parameter specific tests below
		 * ##########################################################
		 */

		/*
		 * 3. Check that the number of combinations to generate are not "too many"
		 */
		BigInteger noOfPossibleCombinations = PUTExperiment.getNcKValue(numOfAttributes, partitionSize);
		BigDecimal numberOfCombinationsToGenerate = new BigDecimal(noOfPossibleCombinations);
		numberOfCombinationsToGenerate = numberOfCombinationsToGenerate.multiply(new BigDecimal("" + vExpense)).setScale(0, RoundingMode.FLOOR);
		if(numberOfCombinationsToGenerate.compareTo(new BigDecimal(Integer.MAX_VALUE)) > 0) {
			return "Too many partitons to generate: " + numberOfCombinationsToGenerate;
		}

		/*
		 * 4. If the selected classifier is SGD, make sure that the dataset is not multi-class
		 */
		if(classifierType.equals(SGD.class) && classAttribute.numValues() > 2)
			return "The selected classifier SGD, does not support multi-class datasets";

		return null;
	}

	/**
	 * Sets the options to be passed on to the Weka classifier
	 * @see AbstractClassifier#setOptions(String[])
	 * @param classifierOptions The options for the classifier
	 */
	public void setClassifierOptions(String[] classifierOptions) {
		this.classifierOptions = classifierOptions;
	}

	/**
	 * Sets whether to use random combinations instead of systematic generation and pruning
	 * @param generateRandomCombinations <code>true</code> implies usage of random combinations, <code>false</code> implies usage of systematic generation and pruning 
	 */
	public void setGenerateRandomCombinations(boolean generateRandomCombinations) {
		this.generateRandomCombinations = generateRandomCombinations;
	}

	/**
	 * Sets the output file for this experiment
	 * @param fileName The file name
	 */
	private void setOutput(String fileName) {
		File resultFile = new File(fileName);
		if(!resultFile.exists()) {
			try {
				resultFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Cannot create output file - " + resultFile);
			}
			this.resultFile = resultFile;
		}
		else if(resultFile.exists() && resultFile.canWrite()) {
			logger.errorln("Output file already exists. Overwriting file " + resultFile.getAbsolutePath());
			this.resultFile = resultFile; 
		} 
	}

	/**
	 * Sets the recovery information for the intial state of this experiment.
	 * @param recoveryInformation An array of recovery information to set
	 */
	public void setRecoveryInformation(String[] recoveryInformation) {
		this.recoveryInformation = recoveryInformation;
	}

	/**
	 * Starts the experiment with the set parameters.<br/>
	 * All important activities are logged by a logger (by default, to standard output and error streams).<br/>
	 * On successful completion, the results of the learning tasks are stored in decreasing order of the accuracies in the result file
	 * (by default, a file called "results.csv" in the same folder as the data file).<br/>
	 * If any progress monitoring is to be done externally, preferably call this method in a new thread.
	 */
	private void startExperiment() {

		seed = 1;

		totalTasks = numOfPartitionedDatasets = numOfDatasetsInQueue = numOfTasksInLearningQueue = numOfTasksCompleted = Long.MIN_VALUE;

		initiateRecoveryTasks();

		// Create partitioned datasets
		try {
			PartitionPlan plan = new PartitionPlan(numOfAttributes, partitionSize, vExpense);
			plan.setGenerateRandomly(generateRandomCombinations);
			plan.setPrivacyExceptions(privacyExceptions);
			plan.setUtilityExceptions(utilityExceptions);
			generatePartitions(plan);
			createDatasets();
		} catch (Exception e) {
			if(!asyncExecution) {
				logger.errorln("Fatal Error - problem in creating partitioned datasets. Exiting.");
				logger.exception(e);
				System.exit(-1);
			}
		}

		// Create learning requests
		results = Collections.synchronizedList(new ArrayList<Future<Stats>>());
		createLearningRequests();

		// Collect results and stats
		try {
			collectStats();
		} catch (InterruptedException | ExecutionException e) {
			if(!asyncExecution) {
				logger.errorln("Fatal Error - problem in collecting learning statistics");
				logger.exception(e);
				System.exit(-1);
			}
		}

		// Sort the results according to accuracy
		Stats.sortList(stats, Stats.ACCURACY, true, null);

		writeResultsToFile();
	}

	/**
	 * Starts this experiment in asynchronous mode (in a different thread) and returns immediately
	 */
	public void startExperimentAsync() {
		asyncThread = new Thread() {
			public void run() {
				startExperiment();
			}
		};
		asyncExecution = true;
		asyncThread.start();
	}

	/**
	 * Starts this experiment in synchronous mode (in the same thread) and blocks till the experiment completes
	 */
	public void startExperimentSync() {
		asyncExecution = false;
		asyncThread = null;
		startExperiment();
	}
	
	/**
	 * Aborts this experiment, if it is running
	 */
	public void stopExperiment() {
		if(learningExecutor != null)
			learningExecutor.shutdownNow();

		if(partitioningExecutor != null)
			partitioningExecutor.shutdownNow();

		if(partitioningProgressMonitor != null)
			partitioningProgressMonitor.interrupt();

		if(learningRequestCreator != null)
			learningRequestCreator.interrupt();

		if(learningProgressMonitor != null)
			learningProgressMonitor.interrupt();

		if(asyncThread != null)
			asyncThread.interrupt();
	}

	/**
	 * Write the generated partitions to the recovery file
	 * @param isRandomlyGenerated <code>true</code> if the random generation method was used, <code>flase</code> otherwise
	 * @throws IOException If something goes wrong while trying to write the partitions
	 */
	private void writePartitionsForRecovery(boolean isRandomlyGenerated) throws IOException {
		// Recovery Tasks
		if(rm != null) {
			rm.printPartitionsMetadata(attributePartitions.size(), isRandomlyGenerated);
			for(Set<Integer> partition : attributePartitions) {
				rm.printPartition(partition);
			}
		}
	}

	/**
	 * Writes the collected stats to the result file and deletes the recovery file
	 */
	private void writeResultsToFile() {
		numOfResultsWrittenToFile = 0;
		// Write it to results file
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(resultFile), true);
			// Write the header
			writer.print("Attribute set, ");
			writer.print("Time taken (in sec), ");
			writer.print("Accuracy");
			List<String> allClasses = Dataset.getAllClassesForDataset(dataset);
			/*
			 * Order :-
			 * 1. True Positives
			 * 2. False Positives
			 * 3. False Negatives
			 * 4. Precision
			 * 5. Recall
			 * 6. Area under ROC
			 * 7. Area under PRC
			 */
			for(Object classValue : allClasses)
				writer.print(", TP_" + classValue);
			for(Object classValue : allClasses)
				writer.print(", FP_" + classValue);
			for(Object classValue : allClasses)
				writer.print(", FN_" + classValue);
			for(Object classValue : allClasses)
				writer.print(", Precision_" + classValue);
			for(Object classValue : allClasses)
				writer.print(", Recall_" + classValue);
			for(Object classValue : allClasses)
				writer.print(", aROC_" + classValue);
			for(Object classValue : allClasses)
				writer.print(", aPRC_" + classValue);
			writer.println();

			for(Stats stat : stats) {
				writer.println(stat);
				numOfResultsWrittenToFile++;
			}
			writer.close();
			if(rm != null)
				rm.deleteRecoveryFile();
		} catch (IOException e) {
			if(!asyncExecution) {
				logger.errorln("Fatal Error - problem in writing output file");
				logger.exception(e);
				System.exit(-1);
			}

		}
	}

}
