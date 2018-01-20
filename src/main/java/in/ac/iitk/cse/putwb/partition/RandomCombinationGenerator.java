package in.ac.iitk.cse.putwb.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A class to generate random combinations of a set of attributes.
 * @author Saurabh Srivastava
 *
 */
public class RandomCombinationGenerator implements Runnable { 
	
	/**
	 * A set of exceptions to honour
	 */
	private static Set<Set<Integer>> ignoreTogetherSet;
	
	/**
	 * The set that contains the generated partitions at any point
	 */
	private static Set<Set<Integer>> combinations;
	
	/**
	 * A list of numbers from 1 to n
	 */
	private static List<Integer> startingList;
	
	static {
		ignoreTogetherSet = new HashSet<Set<Integer>>();
		combinations = Collections.synchronizedSet(new HashSet<Set<Integer>>());
		startingList = new ArrayList<Integer>();
	}
	
	/**
	 * Generates a fixed number of random combinations for given values of n and k, honouring a given set of privacy exceptions.
	 * @param n The value of <i>n</i> in <i>C(n, k)</i>
	 * @param k The value of <i>k</i> in <i>C(n, k)</i>
	 * @param noOfCombinationsToGenerate The number of combinations to generate
	 * @param ignoreTogether A {@link Set} of privacy exceptions
	 * @return A {@link Set} of random combinations according to requested parameters
	 * @throws Exception If something goes wrong while generating the combinations
	 */
	public static Set<Set<Integer>> generateRandomCombinations(int n, int k, int numberOfCombinationsToGenerate, Set<Set<Integer>> ignoreTogether) throws Exception {
		ignoreTogetherSet.clear();
		ignoreTogetherSet.addAll(ignoreTogether);
		combinations.clear();
		startingList.clear();
		for(int i = 1; i <= n; i++)
			startingList.add(i);
		int noOfCores = Runtime.getRuntime().availableProcessors();
		
		if(noOfCores > 2) {
			ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(noOfCores);
			executor.setRemoveOnCancelPolicy(true);
			while(combinations.size() < numberOfCombinationsToGenerate) {
				executor.submit(new RandomCombinationGenerator(k));
			}
			executor.shutdownNow();
			while(!executor.isTerminating())
				Thread.sleep(500);
			while(combinations.size() > numberOfCombinationsToGenerate) {
				Iterator<Set<Integer>> it = combinations.iterator();
				it.next();
				it.remove();
			}
		} else {
			RandomCombinationGenerator generator = new RandomCombinationGenerator(k);
			int temp = numberOfCombinationsToGenerate;
			while(temp-- != 0) {
				combinations.add(generator.generateRandomCombination());
			}
		}
		return combinations;
	}
	
	/**
	 * Contains a set of attributes while the generation is going on
	 */
	private Set<Integer> combination;

	/**
	 * The value of <i>k</i> in <b>(n choose k)</b>
	 */
	private int k;

	/**
	 * Creates a new instance of a random combination generator, with the given value of <i>k</i>.
	 * @param k The number of attributes in a particular combination
	 */
	private RandomCombinationGenerator(int k) {
		this.k = k;
		combination = new TreeSet<Integer>();
	}
	
	/**
	 * Returns if the currently generated combination already exists or not.
	 * It is possible that the method returns <code>false</code>, but before the call ends, another thread may have added the same combination to the pool. 
	 * @return <code>false</code> if the combination hasn't already been generated, <code>true</code> otherwise 
	 */
	private boolean alreadyGenerated() {
		if(combinations.contains(combination))
			return true;
		return false;
	}
	
	/**
	 * Generates a random combination of <i>k</i> attributes, with an attempt to not repeat an existing combination
	 * @return A random combination
	 */
	private Set<Integer> generateRandomCombination() {
		do {
			combination.clear();
			List<Integer> integerList = new ArrayList<Integer>(startingList);
			int ctr = k;
			while(ctr > 0) {
				int randomNum = ThreadLocalRandom.current().nextInt(0, integerList.size());
				combination.add(integerList.remove(randomNum));
				ctr--;
			}
		} while(!Thread.currentThread().isInterrupted() && (hasExceptions() || alreadyGenerated()));
		return combination;
	}
	
	/**
	 * Returns if the currently generated combination violates any privacy exceptions or not
	 * @return <code>true</code> if the combination doesn't have any exceptions, <code>false</code> otherwise
	 */
	private boolean hasExceptions() {
		for(Set<Integer> exception : ignoreTogetherSet) {
			if(combination.containsAll(exception))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		combinations.add(generateRandomCombination());
	}
	
}
