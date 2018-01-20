package in.ac.iitk.cse.putwb.partition;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

/**
 * A class that generates a subset of combinations, from a starting combination, before an ending combination, in dictionary order
 * @author Saurabh Srivastava
 *
 */
public class PartialCombinationsGenerator implements Callable<Set<Set<Integer>>> {

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
	 * @param n The value of <i>n</i> in <b>(n choose k)</b>
	 * @param k The value of <i>k</i> in <b>(n choose k)</b>
	 * @param ignoreTogether Any sets of attributes, which if appear together in a combination, should result in exclusion of that combination from the result
	 * @param startWith The combination to start with (including this one)
	 * @param stopBefore The combination to end before (excluding this one)
	 * 
	 */
	public PartialCombinationsGenerator(int n, int k, Set<Set<Integer>> ignoreTogether, Set<Integer> startWith, Set<Integer> stopBefore) {
		this.n = n;
		this.k = k;
		this.ignoreTogether = ignoreTogether;
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
