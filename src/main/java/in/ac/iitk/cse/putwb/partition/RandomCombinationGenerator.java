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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class to generate random combinations of a set of attributes.
 * @author Saurabh Srivastava
 *
 */
public class RandomCombinationGenerator implements Runnable { 
	
	/**
	 * Signals any worker threads that they must stop attempting generation of a new combination
	 */
	private static AtomicBoolean stopGeneration;
	
	/**
	 * A set of exceptions to honour
	 */
	private static Set<Set<Integer>> ignoreTogetherSet;
	
	/**
	 * If no new random combination could be generated in specified number of seconds, the generator gives up.
	 */
	private static final int TIMEOUT_IN_SECONDS = 60;
	
	/**
	 * The thread-safe wrapper set that contains the generated partitions at any point
	 */
	private static Set<Set<Integer>> combinationsWrapper;
	
	/**
	 * A list of numbers from 1 to n
	 */
	private static List<Integer> startingList;
	
	/**
	 * The last checked size of the combinations set, used to check progress
	 */
	private static int combinationsSize;
	
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
		ignoreTogetherSet = new HashSet<Set<Integer>>(ignoreTogether);
		Set<Set<Integer>> combinationsSet = new HashSet<Set<Integer>>();
		combinationsWrapper = Collections.synchronizedSet(combinationsSet);
		startingList = new ArrayList<Integer>();
		stopGeneration = new AtomicBoolean(false);
		combinationsSize = -1;
		for(int i = 1; i <= n; i++)
			startingList.add(i);
		int noOfCores = Runtime.getRuntime().availableProcessors();
		
		if(noOfCores > 2) {
			while(combinationsWrapper.size() < numberOfCombinationsToGenerate && !stopGeneration.get()) {
				ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(noOfCores);
				executor.setRemoveOnCancelPolicy(true);
				for(int i = 0; i < 100*noOfCores; i++)	//  Create workers in a batch of size (no. of cores X 100)
					executor.submit(new RandomCombinationGenerator(k));
				executor.shutdown();
				while(executor.isTerminating()) {
					if(!isProgressing()) {	// Problem !! no new combinations are getting generated, give up !!
						executor.shutdownNow();
						stopGeneration.set(true);
						// If other threads need cleanup time, hint the scheduler to prioritize them
						Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
						Thread.yield();
						// give some more time (5 seconds) for things to clear up !!
						Thread.sleep(5000);
						// check if everything is fine, if not, something is seriously wrong.. just exit !!
						if(!executor.isTerminated())
							System.exit(-1);
						else
							break;
					}
					if(combinationsWrapper.size() >= numberOfCombinationsToGenerate)
						stopGeneration.set(true);
					Thread.sleep(50);
				}
			}
			synchronized (combinationsWrapper) {
				Iterator<Set<Integer>> it = combinationsWrapper.iterator();
				while(combinationsWrapper.size() > numberOfCombinationsToGenerate) {
					it.next();
					it.remove();
				}
			}
		} else {
			RandomCombinationGenerator generator = new RandomCombinationGenerator(k);
			int temp = numberOfCombinationsToGenerate;
			while(temp-- != 0) {
				Set<Integer> generatedCombination = generator.generateRandomCombination();
				if(generatedCombination != null)
					combinationsWrapper.add(generatedCombination);
			}
			stopGeneration.set(true);
		}
		return combinationsSet;
	}
	
	/**
	 * Checks if the generation process is <i>progressing</i> or not (which essentially means that 
	 * the number of combinations generated so far is higher than that generated "some time ago").
	 * @return <code>true</code> if the process is progressing, <code>false</code> otherwise
	 */
	private static boolean isProgressing() {
		int currentSize = -1;
		int timeToWait = TIMEOUT_IN_SECONDS;
		synchronized (combinationsWrapper) {
			currentSize = combinationsWrapper.size();
		}
		while(currentSize == combinationsSize) {
			timeToWait--;
			try {
				Thread.sleep(1000);
				synchronized (combinationsWrapper) {
					currentSize = combinationsWrapper.size();
				}
			} catch (InterruptedException e) {
				// Shouldn't really be here, but if we are, just say there has been no progress !!
				return false;
			}
			if(timeToWait <= 0)
				return false;
		}
		combinationsSize = currentSize;
		return true;
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
		if(combinationsWrapper.contains(combination))
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
			if(stopGeneration.get() || Thread.currentThread().isInterrupted())
				return null;
			else {
				if(alreadyGenerated() || hasExceptions())
					continue;
				else
					return combination;
			}
		} while(true);
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
		Set<Integer> generatedCombination = generateRandomCombination();
		if(generatedCombination != null)
			combinationsWrapper.add(generatedCombination);
	}
	
}
