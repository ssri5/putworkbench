/**
 * 
 */
package in.ac.iitk.cse.putwb.experiment;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.ac.iitk.cse.putwb.classify.Dataset;
import in.ac.iitk.cse.putwb.io.DatasetLoader;

/**
 * This class provides the implementation for a recovery tool, part of the CLI of PUTWorkbench. 
 * The class provides methods to write essential recovery information to a recovery file.
 * If a {@link PUTExperiment} fails to complete, a recovery file associated with it could be used to resume the experiment from the last saved state.
 * @author Saurabh Srivastava
 *
 */
public class RecoveryManager {

	/**
	 * Switch for providing the (arff) data file
	 */
	public static final String RECOVER_STATS = "-rs";

	/**
	 * The main to run the recovery manager
	 * @param arr The commandline arguments for running the experiment
	 */
	public static void main(String[] arr) {
		if(arr.length < 1 || arr.length > 2) {
			printUsageDetails();
			System.exit(-1);
		}
		// Parse arguments
		boolean recoverStatsOnly = false;
		String recoveryFileName = null;
		if(arr.length == 2) {
			if(arr[0].equals(RECOVER_STATS)) {
				recoverStatsOnly = true;
				recoveryFileName = arr[1];
			}
			else {
				printUsageDetails();
				System.exit(-1);
			}
		} else
			recoveryFileName = arr[0];
		File recoveryFile = new File(recoveryFileName);
		if(!recoveryFile.exists()) {
			System.out.println("Recovery File not found: " + recoveryFile.getAbsolutePath());
			System.exit(-1);
		}
		try {
			RecoveryManager rm = new RecoveryManager(recoveryFile, true);
			if(recoverStatsOnly)
				rm.recoverStats();
			else
				rm.restoreExperiment();
		} catch (IOException | IllegalStateException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints a summary of the usage of the Recovery Manager
	 */
	public static void printUsageDetails() {
		System.out.println("---------------------------------------------------------------");
		System.out.println("  PUTWorkbench RecoveryManager " + PUTExperiment.versionInfo);
		System.out.println("---------------------------------------------------------------");
		System.out.println("\nUsage options:");
		System.out.println(RECOVER_STATS + "\t Instructs to only recover the saved stats without resuming the exeriment, e.g. " + RECOVER_STATS + " /home/user/result_recovery.putr");
		System.out.println();
	}

	/**
	 * A stream to write to a recovery file during the execution of an experiment
	 */
	private ObjectInputStream in;

	/**
	 * A stream to read a recovery file when recovery is being sought
	 */
	private ObjectOutputStream out;

	/**
	 * The recovery file to which recovery information is written or from which recovery is sought
	 */
	private File recoveryFile;

	/**
	 * Equivalent to {@code RecoveryManager(File,true)}
	 * @param recoveryFile The recovery file to use
	 * @throws FileNotFoundException If the recovery file is not found and could not be created
	 * @throws IOException If an error occurs while trying to read/write to the recovery file
	 * @see #RecoveryManager(File, boolean)
	 */
	public RecoveryManager(File recoveryFile) throws FileNotFoundException, IOException {
		this(recoveryFile, false);
	}

	/**
	 * Creates a recovery manager which can be used to either print recovery information for a running {@link PUTExperiment},
	 * or recover an interrupted {@link PUTExperiment} after interruption.
	 * @param recoveryFile The recovery file to use
	 * @param recoverFrom <code>true</code>, indicates that this recovery manager will be used for resuming an interrupted experiment, 
	 * <code>false</code> indicates that the recovery manager will be used for printing recovery information to a recovery file during the execution of an experiment
	 * @throws FileNotFoundException If the recovery file is not found and could not be created
	 * @throws IOException If an error occurs while trying to read/write to the recovery file
	 */
	public RecoveryManager(File recoveryFile, boolean recoverFrom) throws FileNotFoundException, IOException {
		this.recoveryFile = recoveryFile;
		if(recoverFrom)
			in = new ObjectInputStream(new FileInputStream(recoveryFile));
		else
			out = new ObjectOutputStream(new FileOutputStream(recoveryFile));
	}

	/**
	 * Deletes the recovery file, if the manager was configured
	 * @throws IOException If an error while closing the opened streams to the file
	 */
	public void deleteRecoveryFile() throws IOException {
		if(out != null) {
			out.close();
		} else if(in != null)
			in.close();
		recoveryFile.delete();
	}

	/**
	 * Prints header information of the experiment to the recovery file.
	 * @param headerInfo The header information to print
	 * @throws IllegalStateException If this recovery manager is not configured to print recovery information
	 * @throws IllegalArgumentException If the number of elements in the header array are odd
	 * @throws IOException If an error occurs while writing the recovery information
	 */
	public void printHeader(String[] headerInfo) throws IllegalStateException, IllegalArgumentException, IOException {
		if(out != null) {
			if(headerInfo.length%2 != 0)	// The array size must be an even number
				throw new IllegalArgumentException("The number of items in header must be even (key-value pairs).");
			Map<String, String> map = new HashMap<String, String>();
			for(int i = 0; i < headerInfo.length; i+=2)
				map.put(headerInfo[i], headerInfo[i+1]);
			out.writeObject(map);
		} else
			throw new IllegalStateException("The recovery manager is not configured to prepare recovery file");
	}

	/**
	 * Prints a partition to the recovery file
	 * @param partition The partition to print
	 * @throws IllegalStateException If this recovery manager is not configured to print recovery information
	 * @throws IOException If an error occurs while writing the recovery information
	 */
	public void printPartition(Set<Integer> partition) throws IllegalStateException, IOException {
		if(out != null) {
			out.writeObject(partition);
		} else
			throw new IllegalStateException("The recovery manager is not configured to prepare recovery file");
	}

	/**
	 * Prints partitions related information of the experiment to the recovery file.
	 * @param numberOfPartitions The number of partitions being used for the experiment
	 * @param randomGeneration <code>true</code> signifies that the random generation method has been used for generating partitions,
	 * <code>false</code> signifies that the generation is done in dictionary order
	 * @throws IllegalStateException If this recovery manager is not configured to print recovery information
	 * @throws IOException If an error occurs while writing the recovery information
	 */
	public void printPartitionsMetadata(int numberOfPartitions, boolean randomGeneration) throws IllegalStateException, IOException {
		if(out != null) {
			out.writeInt(numberOfPartitions);
			out.writeBoolean(randomGeneration);
		} else
			throw new IllegalStateException("The recovery manager is not configured to prepare recovery file");
	}

	/**
	 * Print a collected stat to the recovery file
	 * @param stat The stat to print
	 * @throws IllegalStateException If this recovery manager is not configured to print recovery information
	 * @throws IOException If an error occurs while writing the recovery information
	 */
	public void printStats(Stats stat) throws IllegalStateException, IOException {
		if(out != null) {
			out.writeObject(stat);
		} else
			throw new IllegalStateException("The recovery manager is not configured to prepare recovery file");
	}

	/**
	 * Recovers the stats stored in a recovery file, and save them to an output file.
	 * The output file name is the same as the experiment was configured to use (or "results.csv" if no name could be determined), and is created in the current directory.
	 * The original dataset is also required for the results to be interpreted. If the dataset is not found at the specified location, another attempt is made to find it in the current directory.
	 * @throws IllegalStateException If this recovery manager is not configured to print recovery information
	 * @throws ClassNotFoundException If a problem occurs while interpreting the data stored in the recovery file
	 * @throws IOException If a problem occurs while reading the recovery file
	 */
	@SuppressWarnings("unchecked")
	public void recoverStats() throws IllegalStateException, ClassNotFoundException, IOException {
		if(in != null) {
			File resultFile = null;
			// Read experiment header
			Map<String, String> headerInfo = new HashMap<String, String>();
			headerInfo = (Map<String, String>) in.readObject();
			String dataFileName = headerInfo.get(PUTExperiment.DATA_FILE_SWITCH);
			if(dataFileName == null) {
				System.err.println("Could not find dataset details. The recovery file may be corrupted.");
				System.exit(1);
			}
			File dataFile = new File(dataFileName);
			if(!dataFile.exists()) {
				System.out.println("Could not locate the original dataset, checking current directory for the file named \"" + dataFile.getName() + "\"");
				dataFile = new File(".", dataFile.getName());
				if(!dataFile.exists()) {
					System.err.println("Could not find the original dataset.");
					System.exit(1);
				}

			}
			List<String> allClasses = null;
			try {
				allClasses = Dataset.getAllClassesForDataset(DatasetLoader.loadARFF(dataFile.getAbsolutePath()));
			} catch (Exception e) {
				System.err.println("Something went wrong trying to load the dataset");
				e.printStackTrace(System.err);
				System.exit(-1);
			}
			String resultFileName = headerInfo.get(PUTExperiment.OUTPUT_FILE_SWITCH);
			if(resultFileName != null)
				resultFileName = PUTExperiment.DEFAULT_OUTPUT_FILE_NAME;

			resultFile = new File(".", resultFileName);
			if(!resultFile.exists() && !resultFile.createNewFile()) {
				System.out.println("Could not create a file called \"" + resultFile.getAbsolutePath() + "\"");
				System.exit(-1);
			}

			// Read the partition information
			int numberOfPartitions = in.readInt();
			in.readBoolean();	// We don't care about what the generation method was !
			int i = 0;
			try {
				for(i = 0; i < numberOfPartitions; i++)
					in.readObject();	// We don't care about what partition is it !
			} catch(EOFException e) {
				// There are no stats in the file to recover !!
				System.out.println("No stats in the file to recover");
				return;
			}
			// Now read whatever number of stats we could !
			List<Stats> allRecoveredStats = new ArrayList<Stats>();
			try {
				for(i = 0; i < numberOfPartitions; i++)
					allRecoveredStats.add((Stats) in.readObject());
			} catch(EOFException e) {
				// Not all stats were written to the file !
				// Do nothing !
			}
			in.close();
			System.out.println("Recovered " + allRecoveredStats.size() + " stats from the recovery file");
			// Write them to the results file
			PrintWriter writer = new PrintWriter(new FileWriter(resultFile), true);
			// Write the header
			writer.print("Attribute set, ");
			writer.print("Time taken (in sec), ");
			writer.print("Accuracy");
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
			/*
			 * Write the recovered stats
			 */
			for(Stats stat : allRecoveredStats)
				writer.println(stat);
			writer.close();
			System.out.println("Recovered stats saved to " + resultFile.getAbsolutePath());
		} else
			throw new IllegalStateException("The recovery manager is not configured to recover an experiment");
	}

	/**
	 * Restores the experiment linked to this recovery manager.
	 * The recovery can either be done from the partition generation stage, or after the partition generation stage, depending upon the information available in the recovery file.
	 * If the partition information was not present in the recovery file, the recovery would fail. In this case, it is better to restart the experiment from the scratch.
	 * @throws IllegalStateException If this recovery manager is not configured to print recovery information
	 * @throws ClassNotFoundException If a problem occurs while interpreting the data stored in the recovery file
	 * @throws IOException If a problem occurs while reading the recovery file
	 */
	@SuppressWarnings("unchecked")
	public void restoreExperiment() throws IllegalStateException, ClassNotFoundException, IOException {
		if(in != null) {
			// Read experiment header and create a new experiment instance
			Map<String, String> headerInfo = new HashMap<String, String>();
			headerInfo = (Map<String, String>) in.readObject();
			String[] params = new String[2*headerInfo.size()];
			Iterator<String> it = headerInfo.keySet().iterator();
			int i = 0;
			while(it.hasNext()) {
				String key = it.next();
				String val = headerInfo.get(key);
				params[i++] = key;
				params[i++] = val;
			}
			PUTExperiment experiment = PUTExperiment.createExperiment(params);

			// Read partition metadata and see if all the partitions generation completed before the interruption
			int numberOfPartitions = in.readInt();
			boolean randomGeneration = in.readBoolean();
			Set<Set<Integer>> partitions = new LinkedHashSet<Set<Integer>>();
			try {
				for(i = 0; i < numberOfPartitions; i++)
					partitions.add((Set<Integer>) in.readObject());
			} catch(EOFException e) {
				// Couldn't read all the partitions
				in.close();
				experiment.resumeExperimentFromGenerationStage(numberOfPartitions, partitions, randomGeneration);
				System.out.println("Done !!");
				System.out.println("Results saved to - " + experiment.getResultFile().getAbsolutePath());
				return;
			}
			// Just to be sure, check once again !
			if(partitions.size() != numberOfPartitions) {
				experiment.resumeExperimentFromGenerationStage(numberOfPartitions, partitions, randomGeneration);
				System.out.println("Done !!");
				System.out.println("Results saved to - " + experiment.getResultFile().getAbsolutePath());
				return;
			}
			// Read whatever results were written to the file
			List<Stats> allRecoveredStats = new ArrayList<Stats>();
			try {
				for(i = 0; i < numberOfPartitions; i++)
					allRecoveredStats.add((Stats) in.readObject());
			} catch(EOFException e) {
				// Couldn't read all results
				in.close();
				experiment.resumeExperimentAfterGenerationStage(partitions, allRecoveredStats, randomGeneration);
				System.out.println("Done !!");
				System.out.println("Results saved to - " + experiment.getResultFile().getAbsolutePath());
				return;
			}
		} else
			throw new IllegalStateException("The recovery manager is not configured to recover an experiment");
	}
}
