package in.ac.iitk.cse.putwb.partition;

import java.util.Set;
import java.util.TreeSet;

import in.ac.iitk.cse.putwb.experiment.PUTExperiment;

/**
 * Represents a plan for distributing attributes among learning pods.
 * @author Saurabh Srivastava
 *
 */
public class PartitionPlan {

	/**
	 * A flag to signify usage of random combinations, instead of generating them in dictionary order
	 */
	private boolean generateRandomly;
	
	/**
	 * The proportion of attribute combinations to retain
	 */
	private float expense;
	
	/**
	 * The number of attributes in unfragmented dataset
	 */
	private int numOfAttributes;
	
	/**
	 * The size of a partition
	 */
	private int partitionSize;
	
	/**
	 * The set of privacy exceptions to honour
	 */
	private Set<Set<Integer>> privacyExceptions;
	
	/**
	 * The set of utility exceptions to honour
	 */
	private Set<Set<Integer>> utilityExceptions;
	
	/**
	 * Initiates a new partition plan for the given number of attributes, partition size and expense weight
	 * @param numOfAttributes The number of attributes in the dataset
	 * @param partitionSize The size of partitions to create
	 * @param expense An expense weight to consider while creating a plan
	 */
	public PartitionPlan(int numOfAttributes, int partitionSize, float expense) {
		this.numOfAttributes = numOfAttributes;
		this.privacyExceptions = null;
		setPartitionSize(partitionSize);
		setExpense(expense);
		generateRandomly = false;
	}

	/**
	 * Adds a privacy exception for a set of attributes. All attributes must be in the range [0, number of attributes)
	 * @param attributes A list of attributes
	 * @throws IllegalArgumentException if any of the attributes are not in range or the number of attributes are more than the partition size
	 */
	public void addPrivacyException(int... attributes) throws IllegalArgumentException {
		if(attributes.length > partitionSize)
			throw new IllegalArgumentException("Number of attributes in the exception cannot be more than the partition size");
		if(attributes.length > 0) {
			for(int attr : attributes)
				if(attr <  0 || attr >= numOfAttributes)
					throw new IllegalArgumentException("Attributes must be in the range [0, number of attributes)");
			if(privacyExceptions == null)
				privacyExceptions = new TreeSet<Set<Integer>>(PUTExperiment.ATTRIBUTE_SET_COMPARATOR);
			Set<Integer> exception = new TreeSet<Integer>();
			for(int attr : attributes)
				exception.add(attr);
			privacyExceptions.add(exception);
		}
	}

	/**
	 * Adds a utility exception for a set of attributes. All attributes must be in the range [0, number of attributes)<br/>
	 * NOTE: If a utility exception conflicts with a privacy exception, the privacy exception takes precedence
	 * @param attributes A list of attributes
	 * @throws IllegalArgumentException if any of the attributes are not in range or the number of attributes are more than the partition size
	 */
	public void addUtilityException(int... attributes) throws IllegalArgumentException {
		if(attributes.length > partitionSize)
			throw new IllegalArgumentException("Number of attributes in the exception cannot be more than the partition size");
		if(attributes.length > 0) {
			for(int attr : attributes)
				if(attr <  0 || attr >= numOfAttributes)
					throw new IllegalArgumentException("Attributes must be in the range [0, number of attributes)");
			if(utilityExceptions == null)
				utilityExceptions = new TreeSet<Set<Integer>>(PUTExperiment.ATTRIBUTE_SET_COMPARATOR);
			Set<Integer> exception = new TreeSet<Integer>();
			for(int attr : attributes)
				exception.add(attr);
			utilityExceptions.add(exception);
		}
	}

	/**
	 * Returns the expense weight for this partition plan
	 * @return the expense weight
	 */
	public float getExpense() throws IllegalArgumentException {
		return expense;
	}

	/**
	 * Returns the number of attributes in this partition plan
	 * @return the number of attributes
	 */
	public int getNumOfAttributes() {
		return numOfAttributes;
	}

	/**
	 * Returns the size of a single partition for this partition plan
	 * @return the partition size
	 */
	public int getPartitionSize() {
		return partitionSize;
	}
	
	/**
	 * Returns the set of privacy exceptions to honour
	 * @return the privacy exceptions
	 */
	public Set<Set<Integer>> getPrivacyExceptions() {
		return privacyExceptions;
	}

	/**
	 * Returns the privacy exceptions set for this partition plan
	 * @return the exception map
	 */
	public Set<Set<Integer>> getPrivacyExceptionsSet() {
		return privacyExceptions;
	}

	/**
	 * Returns the set of utility exceptions to honour
	 * @return the utility exceptions
	 */
	public Set<Set<Integer>> getUtilityExceptions() {
		return utilityExceptions;
	}
	
	/**
	 * Returns the utility exceptions set for this partition plan<br/>
	 * NOTE: If any utility exceptions conflict with privacy exceptions, the privacy exceptions take precedence
	 * @return the exception map
	 */
	public Set<Set<Integer>> getUtilityExceptionsSet() {
		return utilityExceptions;
	}

	/**
	 * Returns whether the combinations should be generated randomly (or in dictionary order). By default, returns <code>true</code>.
	 * @return the generateRandomly <code>true</code> if combinations are to be generated randomly, <code>false</code> otherwise
	 */
	public boolean isGenerateRandomly() {
		return generateRandomly;
	}
	
	/**
	 * Sets the expense weight for this partition plan. Must be in the range (0,1]
	 * @param expense the expense to set
	 * @throws IllegalArgumentException if the number is not in the range (0,1]
	 */
	public void setExpense(float expense) throws IllegalArgumentException {
		if(expense <= 0 || expense > 1)
			throw new IllegalArgumentException("The weight should be in (0,1] only");
		this.expense = expense;
	}
	
	/**
	 * Sets whether combinations should be generated randomly (or in dictionary order)
	 * @param generateRandomly <code>true</code> means generate combinations randomly, <code>false</code> means combinations shall be generated in dictionary order
	 * 
	 */
	public void setGenerateRandomly(boolean generateRandomly) {
		this.generateRandomly = generateRandomly;
	}

	/**
	 * Sets the number of attributes in this partition plan
	 * @param numOfAttributes the numOfAttributes to set
	 * @throws IllegalArgumentException if the number of attributes are 0 or less
	 */
	public void setNumOfAttributes(int numOfAttributes) throws IllegalArgumentException {
		if(numOfAttributes < 1)
			throw new IllegalArgumentException("The number of attributes cannot be less than 1");
		this.numOfAttributes = numOfAttributes;
	}

	/**
	 * Sets the size of a single partition for this partition plan. It must be in the range [1, number of attributes]
	 * @param partitionSize the partition size to set
	 * @throws IllegalArgumentException if the partition size is out of the range [1, number of attributes]
	 */
	public void setPartitionSize(int partitionSize) throws IllegalArgumentException {
		if(partitionSize < 1 || partitionSize > numOfAttributes)
			throw new IllegalArgumentException("The partition size must be between 1 and number of attributes");
		this.partitionSize = partitionSize;
	}

	/**
	 * Sets the set of privacy exceptions to honour
	 * @param The set of privacy exceptions
	 */
	public void setPrivacyExceptions(Set<Set<Integer>> privacyExceptions) {
		this.privacyExceptions = privacyExceptions;
	}

	/**
	 * Sets the set of utility exceptions to honour
	 * @param The set of utility exceptions
	 */
	public void setUtilityExceptions(Set<Set<Integer>> utilityExceptions) {
		this.utilityExceptions = utilityExceptions;
	}
}
