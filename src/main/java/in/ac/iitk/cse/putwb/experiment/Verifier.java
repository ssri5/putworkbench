/**
 * 
 */
package in.ac.iitk.cse.putwb.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import in.ac.iitk.cse.putwb.classify.DataClassifier;
import in.ac.iitk.cse.putwb.classify.Dataset;
import in.ac.iitk.cse.putwb.io.DatasetLoader;
import in.ac.iitk.cse.putwb.log.BasicLogger;

/**
 * @author Saurabh Srivastava
 * Given a (sub)set of classification task results, verifies the variance in the results, 
 * with the case when the same task is applied over a fixed sized, similar dataset.
 */
public class Verifier extends PUTExperiment {

	/**
	 * Switch for providing the current results file
	 */
	public static final String CURR_RESULTS_FILE_SWITCH = "-res";

	/**
	 * Creates and returns a {@link Verifier} with the given arguments
	 * @param params The commandline arguments provided for this verifier
	 * @return A verifier built using the asked preferences
	 */
	private static Verifier createVerifier(String[] params) {
		String filePath = null;
		float hExpense = 1.0f;
		String outputFileName = null;
		String currentResultsFileName = null;
		String classifier = null;
		String classifierOptions = null;
		File stdout = null, stderr = null;
		BasicLogger logger = BasicLogger.getDefaultLogger();
		int k = 5;
		boolean deleteMissing = false;
		boolean removeDuplicates = true;
		try {
			for(int i = 0; i < params.length; i++) {
				if(params[i].compareToIgnoreCase(DATA_FILE_SWITCH) == 0)
					filePath = params[++i];
				else if(params[i].compareToIgnoreCase(H_EXPENSE_SWITCH) == 0)
					hExpense = Float.parseFloat(params[++i]);
				else if(params[i].compareToIgnoreCase(OUTPUT_FILE_SWITCH) == 0)
					outputFileName = params[++i];
				else if(params[i].compareToIgnoreCase(CURR_RESULTS_FILE_SWITCH) == 0)
					currentResultsFileName = params[++i];
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
				} else
					throw new RuntimeException("Invaid option - " + params[i]);
			}
			if(filePath == null)
				throw new RuntimeException("Data file required");
			if(currentResultsFileName == null)
				throw new RuntimeException("Current results file required");
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
			Verifier verifier = new Verifier(filePath, classifier, classifierOptions, outputFileName, currentResultsFileName,
					hExpense, deleteMissing, removeDuplicates, k, logger);
			return verifier;
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
	 * Returns a string representation for a pair of stats. 
	 * The string contains values of the various metrics, as well as differences between the current and new results. 
	 * @param s1 The newly computed set of stats 
	 * @param s2 The current set of stats
	 * @return A string representation for the pair of given stats, if the stats were compatible, <code>null</code> otherwise
	 */
	private static String getStatLine(Stats s1, Stats s2) {
		StringBuffer sb = new StringBuffer();
		Set<Integer> partition = s1.getPartition();
		if(partition != null) {
			sb.append("\"" + partition.toString() + "\"");
			sb.append(", " + s1.getTime()/1000000000f);
			sb.append(", " + s1.getAccuracy());
			sb.append(", " + (s2.getAccuracy() - s1.getAccuracy()));
			
			/*
			 * Order :-
			 * 1. True Positives
			 * 2. False Positives
			 * 3. False Negatives
			 * 4. Precision
			 * 5. Recall
			 * 6. Area under ROC Curve
			 * 7. Area under PR Curve
			 */
			double[] arr1, arr2;
			int i = 0;
			if(((arr1 = s1.getTp()) != null) && (arr2 = s2.getTp()) != null) {
				for(i = 0; i < arr1.length; i++)
					sb.append(", " + arr1[i] + ", " + (arr2[i] - arr1[i]));
			}
			if(((arr1 = s1.getFp()) != null) && (arr2 = s2.getFp()) != null) {
				for(i = 0; i < arr1.length; i++)
					sb.append(", " + arr1[i] + ", " + (arr2[i] - arr1[i]));
			}
			if(((arr1 = s1.getFn()) != null) && (arr2 = s2.getFn()) != null) {
				for(i = 0; i < arr1.length; i++)
					sb.append(", " + arr1[i] + ", " + (arr2[i] - arr1[i]));
			}
			if(((arr1 = s1.getPrecision()) != null) && (arr2 = s2.getPrecision()) != null) {
				for(i = 0; i < arr1.length; i++)
					sb.append(", " + arr1[i] + ", " + (arr2[i] - arr1[i]));
			}
			if(((arr1 = s1.getRecall()) != null) && (arr2 = s2.getRecall()) != null) {
				for(i = 0; i < arr1.length; i++)
					sb.append(", " + arr1[i] + ", " + (arr2[i] - arr1[i]));
			}
			if(((arr1 = s1.getRoc()) != null) && (arr2 = s2.getRoc()) != null) {
				for(i = 0; i < arr1.length; i++)
					sb.append(", " + arr1[i] + ", " + (arr2[i] - arr1[i]));
			}
			if(((arr1 = s1.getPr()) != null) && (arr2 = s2.getPr()) != null) {
				for(i = 0; i < arr1.length; i++)
					sb.append(", " + arr1[i] + ", " + (arr2[i] - arr1[i]));
			}
		}
		return sb.toString();
	}

	/**
	 * The main to run this verifier
	 * @param args The commandline arguments for the verifier
	 */
	public static void main(String[] args) {
		BasicLogger defaultLogger = BasicLogger.getDefaultLogger();
		if(args.length == 0) {
			printUsageDetails();
			System.exit(0);
		}
		Verifier verifier = createVerifier(args);
		if(verifier == null) {
			defaultLogger.errorln("Problems in creating verifier. To see usage details, invoke without any arguments");
			System.exit(-1);
		}
		defaultLogger.outln("Starting verification...");
		verifier.verify();
	}

	/**
	 * Prints a summary of the usage of the verifier class, with explanation of various switches
	 */
	public static void printUsageDetails() {
		System.out.println("---------------------------------------------------------------");
		System.out.println("  PUTWorkbench Verifier " + PUTExperiment.versionInfo);
		System.out.println("---------------------------------------------------------------");
		System.out.println("\nUsage options:");
		System.out.println(DATA_FILE_SWITCH + "\t (Required) Path to the (arff) data file, e.g. " + DATA_FILE_SWITCH + " /home/user/data.arff");
		System.out.println(CURR_RESULTS_FILE_SWITCH + "\t (Required) Path to the current results file, e.g. " + CURR_RESULTS_FILE_SWITCH + " ./results.csv");
		System.out.println(CLASSIFIER_SWITCH + "\t (Required) The classifier to use, e.g. "+ CLASSIFIER_SWITCH + " J48");
		System.out.println("\t Currently available options for classifier are:");
		System.out.print("\t");
		for(String option : DataClassifier.getClassifierOptions())
			System.out.print(" \"" + option + "\" ");
		System.out.println();
		System.out.println(H_EXPENSE_SWITCH + "\t (Default: 1.0) The horizontal expense budget, e.g. "+ H_EXPENSE_SWITCH + " 0.3");
		System.out.println(OUTPUT_FILE_SWITCH + "\t (Default: A file named \"verification_results.csv\" in the directory containing the current results file) The output file, e.g. " + OUTPUT_FILE_SWITCH + " out.csv");
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
	}

	/**
	 * The result file which has the current results
	 */
	private File currentResultsFile;

	/**
	 * A {@link List} to hold the current statistics
	 */
	private List<Stats> currentStats;

	/**
	 * Creates a new Verifier with given details
	 * @param filePath The path to the (arff) data file
	 * @param classifier The classifier type to use for the experiment
	 * @param classifierOptions Sets the options to be passed on to the Weka classifie, if any
	 * @param outputFileName The output file for this verification process
	 * @param currentResultsFileName The path to the file containing the current stats
	 * @param hExpense The horizontal to use for the verification process
	 * @param deleteMissing Indicates whether to delete instances with missing values, or fill values
	 * @param removeDuplicates Indicates whether to remove duplicate instances or pass them to the classifier
	 * @param k The value of <i>k</k> for k-cross validation
	 * @param logger A logger for this verification process
	 * @throws Exception If something goes wrong while creating the verifier
	 */
	private Verifier(String filePath, String classifier, String classifierOptions, String outputFileName, String currentResultsFileName,
			float hExpense, boolean deleteMissing, boolean removeDuplicates, int k, BasicLogger logger) throws Exception {
		super();
		dataset = DatasetLoader.loadAndCleanDataset(filePath, deleteMissing, removeDuplicates);
		classifierType = DataClassifier.findClassifierByName(classifier);
		this.hExpense = hExpense;
		this.logger = logger;
		this.k = k;

		if(classifierOptions != null)
			parseClassifierOptions(classifierOptions);
		
		currentResultsFile = new File(currentResultsFileName);
		if(!currentResultsFile.exists())
			throw new FileNotFoundException("The current results file " + currentResultsFileName + " not found.");
		if(outputFileName == null)
			resultFile = new File(currentResultsFile.getParent(), "verification_results.csv");
		else
			setOutput(outputFileName);
		if(currentResultsFile.equals(resultFile))
			throw new RuntimeException("The output file is the same as the current results file");
		currentStats = new ArrayList<Stats>();
		switchOffRecovery = true;
	}

	/**
	 * Starts the verification process
	 */
	public void verify() {
		/*
		 * Read the contents of the current results
		 */
		try {
			currentStats = Stats.readStatsFile(currentResultsFile, Dataset.getAllClassesForDataset(dataset).size());
		} catch (FileNotFoundException e) {
			logger.error("Fatal Error - Unable to locate the current results file. Exiting");
			logger.exception(e);
			System.exit(-1);
		}
		/*
		 * Collect the partitions to use
		 */
		attributePartitions = new LinkedHashSet<Set<Integer>>();
		for(Stats stats : currentStats) {
			attributePartitions.add(stats.getPartition());
		}
		/*
		 * Create datasets
		 */
		try {
			createDatasets();
		} catch (Exception e) {
			logger.errorln(
					"Fatal Error - problems in creating verifier, could not create partitioned datasets. Exiting.");
			logger.exception(e);
			System.exit(-1);
		}
		/*
		 * Create learning requests
		 */
		results = Collections.synchronizedList(new ArrayList<Future<Stats>>());
		createLearningRequests();
		/*
		 * Collect new results
		 */
		try {
			stats = new ArrayList<Stats>();
			collectStats();
		} catch (InterruptedException | ExecutionException e) {
			logger.errorln("Fatal Error - problem in collecting new learning statistics. Exiting.");
			logger.exception(e);
			System.exit(-1);
		}
		int noOfResultsWritten = writeVerificationResultsToFile();
		if(noOfResultsWritten != stats.size()) {
			if(noOfResultsWritten <= 0)
				logger.errorln("Verification process failed");
			else
				logger.errorln("Partial failure in the verification process");
		} else
			logger.outln("Verification succeeded. Results saved to " + resultFile.getAbsolutePath());
	}
	
	/**
	 * Writes the new collected stats, along with the observed differences with the old stats to the output file
	 */
	private int writeVerificationResultsToFile() {
		try {
			PrintWriter writer = new PrintWriter(new FileWriter(resultFile), true);
			// Write the header
			writer.print("Attribute set, ");
			writer.print("Time taken (in sec), ");
			writer.print("Accuracy, diff");
			List<String> allClasses = Dataset.getAllClassesForDataset(dataset);
			/*
			 * Order :-
			 * 1. True Positives
			 * 2. False Positives
			 * 3. False Negatives
			 * 4. Precision
			 * 5. Recall
			 * 6. Area under ROC Curve
			 * 7. Area under PR Curve
			 */
			for(Object classValue : allClasses)
				writer.print(", TP_" + classValue + ", diff");
			for(Object classValue : allClasses)
				writer.print(", FP_" + classValue + ", diff");
			for(Object classValue : allClasses)
				writer.print(", FN_" + classValue + ", diff");
			for(Object classValue : allClasses)
				writer.print(", Precision_" + classValue + ", diff");
			for(Object classValue : allClasses)
				writer.print(", Recall_" + classValue + ", diff");
			for(Object classValue : allClasses)
				writer.print(", aROC_" + classValue + ", diff");
			for(Object classValue : allClasses)
				writer.print(", aPR_" + classValue + ", diff");
			writer.println();

			Stats.sortList(currentStats, Stats.DICTIONARY_SEQUENCE, false, null);
			Stats.sortList(stats, Stats.DICTIONARY_SEQUENCE, false, null);
			int missedStats = 0;
			for(int i = 0; i < stats.size(); i++) {
				Stats s1 = stats.get(i);
				Stats s2 = currentStats.get(i);
				if(s1.getPartition().equals(s2.getPartition())) {
					writer.println(getStatLine(s1, s2));
				} else
					missedStats++;
			}
			if(missedStats > 0) {
				logger.errorln("Could not verify " + missedStats + " results out of " + stats.size());
			}
			writer.close();
			return stats.size() - missedStats;
		} catch (IOException e) {
			logger.errorln("Fatal Error - problem in writing output file");
			logger.exception(e);
			System.exit(-1);
		}
		return -1;
	}

}
