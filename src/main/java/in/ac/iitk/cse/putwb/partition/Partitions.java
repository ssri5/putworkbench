package in.ac.iitk.cse.putwb.partition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import in.ac.iitk.cse.putwb.experiment.PUTExperiment;
import in.ac.iitk.cse.putwb.log.BasicLogger;

/**
 * This class represents a set of partitions. Each partition represents one set of attributes, that should be grouped together.
 * @author Saurabh Srivastava
 *
 */
public class Partitions {
	
	/**
	 * Generate partitions containing specified number of attributes, as per the given partition plan
	 * @param plan The partition plan to use while creating the partitions
	 * @param logger The logger object for logging
	 * @return A <code>Partition</code> instance, containing the attribute sets
	 * @throws Exception if something goes wrong while generating partitions
	 */
	public static Partitions generatePartitions(PartitionPlan plan, BasicLogger logger) throws Exception {
		Partitions d = new Partitions();
		
		int n = plan.getNumOfAttributes();
		
		int k = plan.getPartitionSize();
		
		float expense = plan.getExpense();
		BigInteger noOfPossibleCombinations = PUTExperiment.getNcKValue(n, k);
		BigDecimal numberOfCombinationsToGenerate = new BigDecimal(noOfPossibleCombinations);
		numberOfCombinationsToGenerate = numberOfCombinationsToGenerate.multiply(new BigDecimal("" + expense)).setScale(0, RoundingMode.CEILING);
		if(numberOfCombinationsToGenerate.compareTo(new BigDecimal(Integer.MAX_VALUE)) > 0) {
			logger.error("Too many partitons to generate: " + numberOfCombinationsToGenerate);
			System.exit(1);
		}
		if(plan.isGenerateRandomly() && expense < 0.95 && numberOfCombinationsToGenerate.intValue() > 100000) {	
			// If there are large number of partitions to generate, random generation is allowed, 
			// and the expense is at least a little less than 1, then use Random Combinations
			logger.outln("Generating attribute combinations (in random order)... ");
			d.partition = RandomCombinationGenerator.generateRandomCombinations(n, k, numberOfCombinationsToGenerate.intValue(), plan.getPrivacyExceptionsSet());
		} else {
			DictionaryCombinationsGenerator pc = new DictionaryCombinationsGenerator(n, k, expense, plan.getPrivacyExceptionsSet(), plan.getUtilityExceptions());
			logger.outln("Generating attribute combinations (in dictionary order)... ");
			d.partition = pc.getCombinations();
		}
		
		return d;
	}

	/**
	 * A set of attribute indices forming a partition of the unfragmented dataset
	 */
	private Set<Set<Integer>> partition;

	/**
	 * Just to make it out of bounds of other classes
	 */
	private Partitions() {
		// Makes it out of bounds for outside classes
		partition = new TreeSet<Set<Integer>>(PUTExperiment.ATTRIBUTE_SET_COMPARATOR);
	}
	
	/**
	 * Returns an <i>unmodifiable</i> set of partitions. Each partition represents one set of attributes, that should be grouped together.
	 * @return the partition as an <i>unmodifiable</i> set
	 */
	public Set<Set<Integer>> getPartitions() {
		return Collections.unmodifiableSet(partition);
	}
	
}
