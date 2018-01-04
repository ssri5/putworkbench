/**
 * 
 */
package in.ac.iitk.cse.putwb.partition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import in.ac.iitk.cse.putwb.experiment.PUTExperiment;

/**
 * This class generates the combinations of a set of integer in the dictionary order.<br/>
 * If there are multiple cores on the machine being used to run the code, it can parallelise the task to some extent, in order to save time.<br/>
 * The class can also automatically remove any combinations, that contain certain numbers together (this is handy while finding partitions of attributes,
 * honouring privacy exceptions). 
 * @author Saurabh Srivastava
 *
 */
public class ParallelCombinations {

	/**
	 * A class that generates a subset of combinations, from a starting combination, before an ending combination, in dictionary order
	 * @author Saurabh Srivastava
	 *
	 */
	class PartialCombinations implements Callable<Set<Set<Integer>>> {

		/**
		 * The depth to which the current combination being generated has reached, where it needs an increment 
		 */
		private int depth = 0;

		/**
		 * The combination to start with (including this one)
		 */
		private Set<Integer> startWith;

		/**
		 * The combination to end before (excluding this one)
		 */
		private Set<Integer> stopBefore;

		/**
		 * Create a partial combinations generator with given specifications
		 * @param startWith The combination to start with (including this one)
		 * @param stopBefore The combination to end before (excluding this one)
		 */
		private PartialCombinations(Set<Integer> startWith, Set<Integer> stopBefore) {
			this.startWith = startWith;
			this.stopBefore = stopBefore;
		}

		/* (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public Set<Set<Integer>> call() throws Exception {
			return generateCombinations();
		}

		/**
		 * Removes any combinations, that are not required, as per ignore requests
		 * @param originalSet The original set of combinations
		 * @return The set after expected filtering 
		 */
		private Set<Set<Integer>> filter(Set<Set<Integer>> originalSet) {
			if(ignoreTogether != null) {
				Iterator<Set<Integer>> exceptionsIterator = ignoreTogether.iterator();
				while(exceptionsIterator.hasNext()) {
					Set<Integer> exception = exceptionsIterator.next();
					Iterator<Set<Integer>> partitionsIterator = originalSet.iterator();
					while(partitionsIterator.hasNext()) {
						Set<Integer> chunk = partitionsIterator.next();
						if(chunk.containsAll(exception))
							partitionsIterator.remove();
					}
				}
			}
			return originalSet;
		}

		/**
		 * Flips a set's digit, when the digit next to it, has reached the maximum possible value
		 * @param set The current set of integers
		 * @param n The value of <b>n</b> in use
		 * @param k The value of <b>k</b> in use
		 * @return The set which succeeds the provided set in dictionary order
		 */
		private TreeSet<Integer> flipMajor(TreeSet<Integer> set, int n, int k) {
			depth++;
			int high = set.pollLast();
			if(high == n-depth) {
				set = flipMajor(set, n, k);
				high = set.last();
			}
			set.add(++high);
			depth--;
			return set;
		}

		/**
		 * Generates (serially) and returns the required set of combinations
		 * @return A {@link Set} of combinations
		 */
		public Set<Set<Integer>> generateCombinations() {
			Set<Set<Integer>> set = new LinkedHashSet<Set<Integer>>();
			TreeSet<Integer> temp = new TreeSet<Integer>(startWith);

			int high;
			// flip and add

			do {
				set.add(temp);
				if(temp.first() == (n-k+1) && temp.last() == n)	// works when stopBefore is null !
					break;
				temp = new TreeSet<Integer>(temp);
				high = temp.pollLast();
				if(high == n) {
					temp = flipMajor(temp, n, k);
					high = temp.last();
				}
				high++;
				temp.add(high);
			} while(!temp.equals(stopBefore));
			return filter(set);
		}
	}

	/**
	 * Any sets of attributes, which if appear together in a combination, should result in exclusion of that combination from the result
	 */
	private Set<Set<Integer>> ignoreTogether;

	/**
	 * The value of <i>k</i> in <b>(n choose k)</b>
	 */
	private int k;

	/**
	 * The value of <i>n</i> in <b>(n choose k)</b>
	 */
	private int n;

	/**
	 * Creates a new Combinations Generator, with given values of <i>n</i>, <i>k</i> and any sets of numbers that are to be ignored 
	 * if they appear together in a combination
	 * @param n The value of <i>n</i> in <b>(n choose k)</b>
	 * @param k The value of <i>k</i> in <b>(n choose k)</b>
	 * @param ignoreTogether Any sets of attributes, which if appear together in a combination, should result in exclusion of that combination from the result
	 */
	public ParallelCombinations(int n, int k, Set<Set<Integer>> ignoreTogether) {
		super();
		this.n = n;
		this.k = k;
		this.ignoreTogether = ignoreTogether;
	}

	/**
	 * Returns the {@link Set} of combinations as required, in dictionary order
	 * @return A {@link Set} of combinations, each combination itself represented by a {@link Set} of integers in increasing order
	 * @throws Exception If something goes wrong while generating the combinations (usually when parallelism is used)
	 */
	public Set<Set<Integer>> getCombinations() throws Exception {
		// Check the number of cores available - try parallelizing only if they are more than 2  (and k is not n) !
		int noOfCores = Runtime.getRuntime().availableProcessors();
		Set<Set<Integer>> finalSet = new TreeSet<Set<Integer>>(PUTExperiment.ATTRIBUTE_SET_COMPARATOR);
		if(noOfCores > 2 && k < n) {
			ExecutorService executor = Executors.newFixedThreadPool(noOfCores);
			// Create n-k-1 Parallel Combinations generators
			Set<Integer> startWith = getSetWithElements(1, k);
			Set<Integer> stopBefore = getSetWithElements(2, k+1);
			int ctr = 1;
			List<Future<Set<Set<Integer>>>> results = new ArrayList<Future<Set<Set<Integer>>>>();
			do {
				Future<Set<Set<Integer>>> result = executor.submit(new PartialCombinations(startWith, stopBefore));
				results.add(result);
				startWith = stopBefore;
				ctr++;
				stopBefore = getSetWithElements(ctr+1, k+ctr);
			} while(ctr <= (n-k));
			// Add the last element
			finalSet.add(getSetWithElements(n-k+1, n));
			// Merge the sets from the last to the first thread
			for(int i = results.size()-1; i >= 0; i--) {
				Future<Set<Set<Integer>>> result = results.get(i);
				Set<Set<Integer>> resultSet = result.get(); 
				finalSet.addAll(resultSet);
			}
			executor.shutdown();
		} else {
			PartialCombinations allCombinations = new PartialCombinations(getSetWithElements(1, k), null);
			finalSet = allCombinations.generateCombinations();
		}
		return finalSet;
	}

	/**
	 * Produces a {@link Set} of elements, containing all integers from from the starting number to the ending number (both included)	
	 * @param startElement The starting integer
	 * @param endElement The edning integer
	 * @return A {@link Set} containing the required integers in increasing order
	 */
	private Set<Integer> getSetWithElements(int startElement, int endElement) {
		Set<Integer> set = new TreeSet<Integer>();
		for(int i = startElement; i <= endElement; i++)
			set.add(i);
		return set;
	}
}
