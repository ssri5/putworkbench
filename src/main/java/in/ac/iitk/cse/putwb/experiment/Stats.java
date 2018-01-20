package in.ac.iitk.cse.putwb.experiment;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * A wrapper class to collect results of a classification task
 * @author Saurabh Srivastava
 *
 */
public class Stats {
	
	/**
	 * Constant for Accuracy metric
	 */
	public static final short ACCURACY = 1;
	
	/**
	 * Constant for (Class specific) False Negative Rate
	 */
	public static final short CS_FALSE_NEGATIVE = 12;
	
	/**
	 * Constant for (Class specific) False Positive Rate
	 */
	public static final short CS_FALSE_POSITIVE = 11;
	
	/**
	 * Constant for (Class specific) Area under PRC
	 */
	public static final short CS_PRC = 16;
	
	/**
	 * Constant for (Class specific) Precision
	 */
	public static final short CS_PRECISION = 13;
	
	/**
	 * Constant for (Class specific) Recall
	 */
	public static final short CS_RECALL = 14;
	
	/**
	 * Constant for (Class specific) Area under ROC
	 */
	public static final short CS_ROC = 15;
	
	/**
	 * Constant for (Class specific) True Positive Rate
	 */
	public static final short CS_TRUE_POSITIVE = 10;
	
	/**
	 * Constant for Dictionary ordering. This is not a metric, and for all purposes other than sorting, it is considered equivalent to the Accuracy metric.
	 */
	public static final short DICTIONARY_SEQUENCE = 0;
	
	/**
	 * This method returns the maximum possible value for a given field
	 * @param statType The field
	 * @return the maximum value the given field can take
	 */
	public static double getMaximumStatValue(short statType) {
		switch(statType) {
			case ACCURACY:
			case DICTIONARY_SEQUENCE:
				return 100;
			case CS_TRUE_POSITIVE:
			case CS_FALSE_POSITIVE:
			case CS_FALSE_NEGATIVE:
			case CS_PRECISION:
			case CS_RECALL:
			case CS_ROC:
			case CS_PRC:
				return 1;
		}
		return Double.MAX_VALUE;
	}
	
	/**
	 * This method returns the minimum possible value for a given field
	 * @param statType The field
	 * @return the minimum value the given field can take
	 */
	public static double getMinimumStatValue(short statType) {
		// If there is any metric type whose minimum value is other than 0, then a check needs to be placed here
		return 0;
	}
	
	/**
	 * Sorts a <code>List<code> of <code>Stats</code> objects, as per the given metric/field, in either ascending or descending order
	 * @param originalList The <code>List</code> to sort
	 * @param byCriteria The sort metric/field; should be one of defined constants in <code>Stats</code> class
	 * @param descending If <code>true</code>, sorts the <code>List</code> in decreasing order
	 * @param classIndex The class index, in case the metric is class specific (otherwise ignored) 
	 */
	public static void sortList(List<Stats> originalList, short byCriteria, boolean descending, Integer classIndex) {
		Comparator<Stats> c = null;
		final int sign = descending ? -1 : 1;
		if(byCriteria == ACCURACY) {
			c = new Comparator<Stats>() {
				@Override
				public int compare(Stats o1, Stats o2) {
					return sign*compareStats(o1, o2);
				}
				
				private int compareStats(Stats o1, Stats o2) {
					if(o1.accuracy > o2.accuracy)
						return 1;
					else if(o2.accuracy > o1.accuracy)
						return -1;
					else
						return 0;
				}
			};
		} else if(byCriteria == DICTIONARY_SEQUENCE) {
			c = new Comparator<Stats>() {
				@Override
				public int compare(Stats o1, Stats o2) {
					Comparator<Set<Integer>> c = PUTExperiment.ATTRIBUTE_SET_COMPARATOR;
					return sign*c.compare(o1.partition, o2.partition);
				}
			};
		} else if(byCriteria == CS_TRUE_POSITIVE) {
			c = new Comparator<Stats>() {
				@Override
				public int compare(Stats o1, Stats o2) {
					double d1 = o1.getTp()[classIndex];
					double d2 = o2.getTp()[classIndex];
					return sign * (d1 > d2 ? 1 : (d1 < d2 ? -1 : 0));
				}			
			};
		} else if(byCriteria == CS_FALSE_POSITIVE) {
			c = new Comparator<Stats>() {
				@Override
				public int compare(Stats o1, Stats o2) {
					double d1 = o1.getFp()[classIndex];
					double d2 = o2.getFp()[classIndex];
					return sign * (d1 > d2 ? 1 : (d1 < d2 ? -1 : 0));
				}				
			};	
		} else if(byCriteria == CS_FALSE_NEGATIVE) {
			c = new Comparator<Stats>() {
				@Override
				public int compare(Stats o1, Stats o2) {
					double d1 = o1.getFn()[classIndex];
					double d2 = o2.getFn()[classIndex];
					return sign * (d1 > d2 ? 1 : (d1 < d2 ? -1 : 0));
				}	
			};
		} else if(byCriteria == CS_PRECISION) {
			c = new Comparator<Stats>() {
				@Override
				public int compare(Stats o1, Stats o2) {
					double d1 = o1.getPrecision()[classIndex];
					double d2 = o2.getPrecision()[classIndex];
					return sign * (d1 > d2 ? 1 : (d1 < d2 ? -1 : 0));
				}
			};
		} else if(byCriteria == CS_RECALL) {
			c = new Comparator<Stats>() {
				@Override
				public int compare(Stats o1, Stats o2) {
					double d1 = o1.getRecall()[classIndex];
					double d2 = o2.getRecall()[classIndex];
					return sign * (d1 > d2 ? 1 : (d1 < d2 ? -1 : 0));
				}
			};
		} else if(byCriteria == CS_ROC) {
			c = new Comparator<Stats>() {

				@Override
				public int compare(Stats o1, Stats o2) {
					double d1 = o1.getRoc()[classIndex];
					double d2 = o2.getRoc()[classIndex];
					return sign * (d1 > d2 ? 1 : (d1 < d2 ? -1 : 0));
				}
				
			};
		} else if(byCriteria == CS_PRC) {
			c = new Comparator<Stats>() {
				@Override
				public int compare(Stats o1, Stats o2) {
					double d1 = o1.getPrc()[classIndex];
					double d2 = o2.getPrc()[classIndex];
					return sign * (d1 > d2 ? 1 : (d1 < d2 ? -1 : 0));
				}
			};
		}
		if(c != null)
			Collections.sort(originalList, c);
	}
	
	/**
	 * The accuracy achieved by the classification task during k-cross validation
	 */
	private double accuracy;
	
	/**
	 * The array of false negative rates achieved by the classification task during k-cross validation
	 */
	private double[] fn;
	
	/**
	 * The array of false positive rates achieved by the classification task during k-cross validation
	 */
	private double[] fp;
	
	/**
	 * The {@link Set} of attribute indices used for this task 
	 */
	private Set<Integer> partition;
	
	/**
	 * The array of area under PRC values achieved by the classification task during k-cross validation
	 */
	private double[] prc;
	
	/**
	 * The array of precision values achieved by the classification task during k-cross validation
	 */
	private double[] precision;

	/**
	 * The array of recall values achieved by the classification task during k-cross validation
	 */
	private double[] recall;
	
	/**
	 * The array of area under ROC values achieved by the classification task during k-cross validation
	 */
	private double[] roc;

	/**
	 * The amount of time it took for the classification and validation to complete
	 */
	private long time;

	/**
	 * The array of true positive rates achieved by the classification task during k-cross validation
	 */
	private double[] tp;

	/**
	 * Creates a <code>Stats</code> instance, with default values
	 */
	public Stats() {
		partition = null;
		accuracy = Float.NaN;
		time = 0;
	}

	/**
	 * Returns the accuracy of the classification task
	 * @return the accuracy
	 */
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * Returns the false negative rates of the classification task
	 * @return an array of false negative rates
	 */
	public double[] getFn() {
		return fn;
	}

	/**
	 * Returns the false positive rates of the classification task
	 * @return an array of false positive rates
	 */
	public double[] getFp() {
		return fp;
	}

	/**
	 * Returns the set containing indices of attributes used for classification
	 * @return the partition
	 */
	public Set<Integer> getPartition() {
		return partition;
	}

	/**
	 * Returns the area under the PRC values of the classification task
	 * @return an array of area under the PRC values
	 */
	public double[] getPrc() {
		return prc;
	}

	/**
	 * Returns the precision values of the classification task
	 * @return an array of precision values
	 */
	public double[] getPrecision() {
		return precision;
	}

	/**
	 * Returns the recall values of the classification task
	 * @return an array of recall values
	 */
	public double[] getRecall() {
		return recall;
	}

	/**
	 * Returns the area under the ROC values of the classification task
	 * @return an array of area under the ROC values
	 */
	public double[] getRoc() {
		return roc;
	}

	/**
	 * Returns the value for a given field within this instance
	 * @param statType The field whose value is required
	 * @param classIndex For class-specific values, the class index, ignored if the field is not class specific
	 * @return the required value
	 */
	public double getStatValue(short statType, Integer classIndex) {
		if(statType == ACCURACY || statType == DICTIONARY_SEQUENCE)
			return accuracy;
		else if(statType == CS_TRUE_POSITIVE)
			return tp[classIndex];
		else if(statType == CS_FALSE_POSITIVE)
			return fp[classIndex];
		else if(statType == CS_FALSE_NEGATIVE)
			return fn[classIndex];
		else if(statType == CS_PRECISION)
			return precision[classIndex];
		else if(statType == CS_RECALL)
			return recall[classIndex];
		else if(statType == CS_ROC)
			return roc[classIndex];
		else if(statType == CS_PRC)
			return prc[classIndex];
		else
			return -1;
	}

	/**
	 * Returns the time in nano seconds, for the classification task to complete
	 * @return the time in nano seconds
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Returns the true positive rates of the classification task
	 * @return an array of true positive rates to set
	 */
	public double[] getTp() {
		return tp;
	}

	/**
	 * Sets the accuracy of the classification task
	 * @param accuracy the accuracy to set
	 */
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	/**
	 * Sets the false negative rates of the classification task
	 * @param fn an array of false negative rates to set
	 */
	public void setFn(double[] fn) {
		this.fn = fn;
	}

	/**
	 * Sets the false positive rates of the classification task
	 * @param fp an array of false positive rates to set
	 */
	public void setFp(double[] fp) {
		this.fp = fp;
	}

	/**
	 * Sets the set of attributes over which the learning was performed
	 * @param partition the partition to set
	 */
	public void setPartition(Set<Integer> partition) {
		this.partition = partition;
	}

	/**
	 * Sets the area under the PRC values of the classification task
	 * @param recall an array of area under the PRC values to set
	 */
	public void setPrc(double[] prc) {
		this.prc = prc;
	}

	/**
	 * Sets the precision values of the classification task
	 * @param precision an array of precision values to set
	 */
	public void setPrecision(double[] precision) {
		this.precision = precision;
	}

	/**
	 * Sets the recall values of the classification task
	 * @param recall an array of recall values to set
	 */
	public void setRecall(double[] recall) {
		this.recall = recall;
	}
	
	/**
	 * Sets the area under the ROC values of the classification task
	 * @param recall an array of area under the ROC values to set
	 */
	public void setRoc(double[] roc) {
		this.roc = roc;
	}
	
	/**
	 * Sets the time in nano seconds, for the classification task to complete
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}
	
	/**
	 * Sets the true positive rates of the classification task
	 * @param tp an array of true positive rates to set
	 */
	public void setTp(double[] tp) {
		this.tp = tp;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(partition != null) {
			sb.append("\"" + partition.toString() + "\"");
			sb.append(", " + time/1000000000f);
			sb.append(", " + accuracy);
			
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
			if(tp != null)
				for(double val : tp)
					sb.append(", " + val);
			if(fp != null)
				for(double val : fp)
					sb.append(", " + val);
			if(fn != null)
				for(double val : fn)
					sb.append(", " + val);
			if(precision != null)
				for(double val : precision)
					sb.append(", " + val);
			if(recall != null)
				for(double val : recall)
					sb.append(", " + val);
			if(roc != null)
				for(double val : roc)
					sb.append(", " + val);
			if(prc != null)
				for(double val : prc)
					sb.append(", " + val);
		}
		return sb.toString();
	}
	
}
