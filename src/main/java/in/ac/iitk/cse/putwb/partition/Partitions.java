/**
 * 
 */
package in.ac.iitk.cse.putwb.partition;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
	 * @return A <code>Partition</code> instance, containing the attribute sets
	 * @throws Exception 
	 */
	public static Partitions generatePartitions(PartitionPlan plan, BasicLogger logger) throws Exception {
		Partitions d = new Partitions();
		
		int n = plan.getNumOfAttributes();
		
		int k = plan.getPartitionSize();
		
		ParallelCombinations pc = new ParallelCombinations(n, k, plan.getPrivacyExceptionsSet());
		logger.outln("Generating attribute combinations... ");
		Iterator<Set<Integer>> permutationGenerator = pc.getCombinations().iterator();
		
		// Add all possible partitions to the set
		while(permutationGenerator.hasNext()) {
			d.partition.add(permutationGenerator.next());
		}
		
		logger.outln("Reducing size of dataset, if required... ");
		trimToBudget(d, plan.getExpense(), plan.getUtilityExceptionsSet());
		
		return d;
	}
	
	/**
	 * If the expense is less than <code>1.0</code>, purges some attribute combinations to trim the overall set to budget.<br/>
	 * Guarantees not purge any utility exception, if the budget is big enough. Otherwise, includes only a subset of utility exceptions, 
	 * trimmed to budget. 
	 * @param d The unpruned set of attribute combinations
	 * @param expense The proportion of combinations to retain
	 * @param utilityExceptions The set of utility exceptions to honour
	 */
	private static void trimToBudget(Partitions d, float expense, Set<Set<Integer>> utilityExceptions) {
		if(expense == 1.0f)
			return;
		int size = d.partition.size();
		int newSize = (int) Math.floor(size * expense);
		if(newSize == 0)
			throw new IllegalArgumentException("There is no way to satisfy the given vertical expense request as the number of prospective partitions become 0");
		int trimSize = size - newSize;
		
		if(trimSize > 0) {
			// Keep the utility exceptions aside first
			Set<Set<Integer>> mustKeep = new HashSet<Set<Integer>>();
			Iterator<Set<Integer>> partitionsIterator = d.partition.iterator();
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
				d.partition = mustKeep;
			} else {	// take some partitions from the remaining partition set, while purge the others
				size = d.partition.size();
				trimSize = size - (newSize - mustKeep.size());
				int interval = size/trimSize;
				int count = trimSize;
				int ctr = 0;
				partitionsIterator = d.partition.iterator();
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
				d.partition.addAll(mustKeep);
			}
		}
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
