/**
 * 
 */
package in.ac.iitk.cse.putwb.heuristic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import in.ac.iitk.cse.putwb.experiment.PUTExperiment;
import weka.core.Instances;

/**
 * Provides suggestions for basic settings (partition size, vertical expense and horizontal expense) for a given dataset,
 * using an approach - "Prefer Privacy" or "Prefer Utility" 
 * and waiting time - "Quick", "Normal" or "Thorough". <br>
 * By default, the approach is "Prefer Privacy". By default, the waiting time is "Normal".
 * @author Saurabh Srivastava
 *
 */
public class BasicSettingsSuggestor extends DatasetRelatedHeuristics {
	
	/**
	 * Key for providing the approach
	 */
	public static final String APPROACH = "approach";
	
	/**
	 * Constant for partitions limit to be followed for "NORMAL" preference
	 */
	private static final int N_PART_LIMIT = 10000;
	
	/**
	 * Constant for rows limit to be followed for "NORMAL" preference
	 */
	private static final int N_ROW_LIMIT = 5000;
	
	/**
	 * A constant to suggest the "Normal" preference for waiting time
	 */
	public static final int NORMAL = 12;
	
	/**
	 * A constant to suggest the "Prefer Privacy" approach
	 */
	public static final int PREFER_PRIVACY = 1;
	
	/**
	 * A constant to suggest the "Prefer Utility" approach
	 */
	public static final int PREFER_UTILITY = 2;
	
	/**
	 * Constant for partitions limit to be followed for "QUICK" preference
	 */
	private static final int Q_PART_LIMIT = 1000;
	
	/**
	 * Constant for rows limit to be followed for "QUICK" preference
	 */
	private static final int Q_ROW_LIMIT = 500;

	/**
	 * A constant to suggest the "Quick" preference for waiting time
	 */
	public static final int QUICK = 11;
	
	/**
	 * Constant for partitions limit to be followed for "THOROUGH" preference
	 */
	private static final int T_PART_LIMIT = 100000;
	
	/**
	 * Constant for rows limit to be followed for "THOROUGH" preference
	 */
	private static final int T_ROW_LIMIT = 50000;
	
	/**
	 * A constant to suggest the "Thorough" preference for waiting time
	 */
	public static final int THOROUGH = 13;

	/**
	 * Key for providing the waiting time preference
	 */
	public static final String WAITING_TIME = "waiting time";
	
	/* (non-Javadoc)
	 * @see in.ac.iitk.cse.putwb.heuristic.Heuristic#getDecision(java.util.Map)
	 */
	@Override
	public Object getDecision(Map<String, Object> inputs) throws Exception {
		Instances dataset = getDataset(inputs);
		
		Object temp;
		
		boolean preferPrivacy = true;
		temp = inputs.get(APPROACH);
		if(temp != null && (temp instanceof Integer) && ((Integer)temp == PREFER_UTILITY))
			preferPrivacy = false;
		
		char time = 'N';
		temp = inputs.get(WAITING_TIME);
		if(temp != null && (temp instanceof Integer)) {
			int waiting_preference = (Integer)temp;
			if(waiting_preference == QUICK)
				time = 'Q';
			else if(waiting_preference == THOROUGH)
				time = 'T';
		}
		
		/*
		 * Heuristic:
		 * 1. Keep the number of partitions to try below N_ATT_LIMIT for "Normal", below Q_ATT_LIMIT for "Quick" and below T_ATT_LIMIT for "Thorough"
		 * 2. If the number of partitions are upto 25% more of the above limit, use v expense to bring it down
		 */
		
		BigDecimal limit = new BigDecimal("" + N_PART_LIMIT);
		if(time == 'Q')
			limit = new BigDecimal("" + Q_PART_LIMIT);
		else if(time == 'T')
			limit = new BigDecimal("" + T_PART_LIMIT);
		
		BigDecimal leeway = limit.multiply(new BigDecimal("1.25")).setScale(0, RoundingMode.FLOOR).subtract(limit);
		
		float vExpense = 1f;
		
		int numberOfAttributes = dataset.numAttributes() - 1;
		
		int start = (int) Math.ceil(numberOfAttributes/2);
		int incr = preferPrivacy ? -1 : 1;
		int partitionSize;
		for(partitionSize = start; partitionSize > 0 && partitionSize <= numberOfAttributes; partitionSize += incr) {
			BigDecimal noOfPossibleCombinations = new BigDecimal(PUTExperiment.getNcKValue(numberOfAttributes, partitionSize));
			if(limit.compareTo(noOfPossibleCombinations) >= 0)
				break;
			else {
				BigDecimal difference = noOfPossibleCombinations.subtract(limit);
				if(difference.compareTo(leeway) <= 0) {	// Use vertical expense
					// Reduce vertical expense in multiple of 0.05
					do {
						vExpense -= 0.05f;
						noOfPossibleCombinations = noOfPossibleCombinations.multiply(new BigDecimal("" + vExpense)).setScale(0, RoundingMode.FLOOR).subtract(limit);
					} while(limit.compareTo(noOfPossibleCombinations) < 0);
					break;
				}
			}
		}
		
		/*
		 * Heuristic:
		 * Keep the number of rows in the dataset to roughly below Q_ROW_LIMIT for "Quick", below N_ROW_LIMIT for "Normal" and below T_ROW_LIMIT for "Thorough" 
		 */
		
		float hExpense = 1.0f;
		
		int numberOfRows = dataset.numInstances();
		if(time == 'Q' && numberOfRows > Q_ROW_LIMIT)
			hExpense = (float)Q_ROW_LIMIT/numberOfRows;
		else if(time == 'N' && numberOfRows > N_ROW_LIMIT)
			hExpense = (float)N_ROW_LIMIT/numberOfRows;
		else if(numberOfRows > T_ROW_LIMIT)
			hExpense = (float)T_ROW_LIMIT/numberOfRows;
		
		Map<String, Number> decisions = new HashMap<String, Number>();
		decisions.put(PUTExperiment.PARTITION_SIZE_SWITCH, partitionSize);
		decisions.put(PUTExperiment.V_EXPENSE_SWITCH, vExpense);
		decisions.put(PUTExperiment.H_EXPENSE_SWITCH, hExpense);
		
		return decisions;
	}
	
}
