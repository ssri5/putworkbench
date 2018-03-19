package in.ac.iitk.cse.putwb.partition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
public class DictionaryCombinationsGenerator {

	/**
	 * If the expense is less than <code>1.0</code>, purges some attribute combinations to trim the overall set to budget.<br/>
	 * Guarantees not purge any utility exception, if the budget is big enough. Otherwise, includes only a subset of utility exceptions, 
	 * trimmed to budget. 
	 * @param combinations The unpruned set of attribute combinations
	 * @param expense The proportion of combinations to retain
	 * @param utilityExceptions The set of utility exceptions to honour
	 */
	private static void trimToBudget(Set<Set<Integer>> combinations, float expense, Set<Set<Integer>> utilityExceptions) {
		if(expense == 1.0f)
			return;
		int size = combinations.size();
		int newSize = (int) Math.floor(size * expense);
		if(newSize == 0)
			throw new IllegalArgumentException("There is no way to satisfy the given vertical expense request as the number of prospective partitions become 0");
		int trimSize = size - newSize;
		
		if(trimSize > 0) {
			// Keep the utility exceptions aside first
			Set<Set<Integer>> mustKeep = new HashSet<Set<Integer>>();
			Iterator<Set<Integer>> partitionsIterator = combinations.iterator();
			while(partitionsIterator.hasNext()) {
				Set<Integer> partition = partitionsIterator.next();
				for(Set<Integer> exception : utilityExceptions)
					if(partition.containsAll(exception)) {
						mustKeep.add(partition);
						partitionsIterator.remove();
						break;
					}
			}
			
			// Check the size of must keep set, if it is already more than or equal to the required size, just operate on this
			if(mustKeep.size() >= newSize) {
				size = mustKeep.size();
				trimSize = size - newSize;
				int interval = size/trimSize;
				int count = trimSize;
				int ctr = 0;
				partitionsIterator = mustKeep.iterator();
				while(partitionsIterator.hasNext()) {
					ctr++;
					partitionsIterator.next();
					if(ctr == interval) {
						partitionsIterator.remove();
						ctr = 0;
						if(--count == 0)
							break;
					}
				}
				combinations.clear();
				combinations.addAll(mustKeep);
			} else {	// take some partitions from the remaining partition set, while purge the others
				size = combinations.size();
				trimSize = size - (newSize - mustKeep.size());
				int interval = size/trimSize;
				int count = trimSize;
				int ctr = 0;
				partitionsIterator = combinations.iterator();
				while(partitionsIterator.hasNext()) {
					ctr++;
					partitionsIterator.next();
					if(ctr == interval) {
						partitionsIterator.remove();
						ctr = 0;
						if(--count == 0)
							break;
					}
				}
				combinations.addAll(mustKeep);
			}
		}
	}

	/**
	 * Any sets of attributes appearing together in a combination, should result in exclusion of that combination from the result
	 */
	private Set<Set<Integer>> ignoreTogether;
	
	/**
	 * Any sets of attributes appearing together in a combination, should result in the combination being "relatively" immune from exclusion, if any trimming occurs
	 */
	private Set<Set<Integer>> mustContain;

	/**
	 * The value of <i>k</i> in <b>(n choose k)</b>
	 */
	private int k;

	/**
	 * The value of <i>n</i> in <b>(n choose k)</b>
	 */
	private int n;
	
	/**
	 * The value of vertical expense to use
	 */
	private float vExpense;

	/**
	 * Creates a new Dictionary Combinations Generator, with given values of <i>n</i>, <i>k</i>, privacy expense (vertical expense), and exceptions 
	 * if they appear together in a combination
	 * @param n The value of <i>n</i> in <b>(n choose k)</b>
	 * @param k The value of <i>k</i> in <b>(n choose k)</b>
	 * @param vExpense The value of vertical expense to use
	 * @param ignoreTogether Any sets of attributes appearing together in a combination, should result in exclusion of that combination from the result
	 * @param mustContain Any sets of attributes appearing together in a combination, should result in the combination being "relatively" immune from exclusion, if any trimming occurs
	 */
	public DictionaryCombinationsGenerator(int n, int k, float vExpense, Set<Set<Integer>> ignoreTogether, Set<Set<Integer>> mustContain) {
		super();
		this.n = n;
		this.k = k;
		this.vExpense = vExpense;
		this.ignoreTogether = ignoreTogether;
		this.mustContain = mustContain;
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
				Future<Set<Set<Integer>>> result = executor.submit(new PartialCombinationsGenerator(n, k, ignoreTogether, startWith, stopBefore));
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
			PartialCombinationsGenerator allCombinations = new PartialCombinationsGenerator(n, k, ignoreTogether, getSetWithElements(1, k), null);
			finalSet = allCombinations.generateCombinations();
		}
		trimToBudget(finalSet, vExpense, mustContain);
		return finalSet;
	}

	/**
	 * Produces a {@link Set} of elements, containing all integers from from the starting number to the ending number (both included)	
	 * @param startElement The starting integer
	 * @param endElement The ending integer
	 * @return A {@link Set} containing the required integers in increasing order
	 */
	private Set<Integer> getSetWithElements(int startElement, int endElement) {
		Set<Integer> set = new TreeSet<Integer>();
		for(int i = startElement; i <= endElement; i++)
			set.add(i);
		return set;
	}
}
