/**
 * 
 */
package in.ac.iitk.cse.putwb.io;

import java.io.File;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveDuplicates;

/**
 * Loads a dataset for processing
 * @author Saurabh Srivastava
 *
 */
public class DatasetLoader {
	/**
	 * Converts a CSV file to ARFF and then loads the dataset
	 * @param csvFile The input CSV file
	 * @return The loaded {@link Instances}
	 * @throws Exception if something goes wrong while converting or loading the dataset
	 */
	public static Instances convertAndLoadCSV(String csvFile) throws Exception {
		File inputFile = new File(csvFile);
		File tempFile = File.createTempFile("temp", ".arff");
		CSVConverter converter = new CSVConverter();
		converter.saveAsARFF(inputFile, tempFile);
		return loadARFF(tempFile.getAbsolutePath());
	}
	
	/**
	 * Loads and cleans (removes/replaces missing values) a dataset from a given ARFF file
	 * @param arffFile The data file
	 * @return The loaded {@link Instances}
	 * @throws Exception if something goes wrong while loading or cleaning the dataset
	 */
	public static Instances loadAndCleanDataset(String arffFile) throws Exception {
		return loadAndCleanDataset(arffFile, false, true);
	}
	
	/**
	 * Loads and cleans (removes/replaces missing values) a dataset from a given ARFF file
	 * @param arffFile The data file
	 * @param ignoreInstancesWithMissingValues Indicates whether the rows with missing values be deleted or cleaned (filled with appropriate values)
	 * @param removeDuplcates Indicates whether to remove duplicate rows or not
	 * @return The loaded {@link Instances}
	 * @throws Exception if something goes wrong while loading or cleaning the dataset
	 */
	public static Instances loadAndCleanDataset(String arffFile, boolean ignoreInstancesWithMissingValues, boolean removeDuplcates) throws Exception {
		Instances temp = loadARFF(arffFile);
		if(removeDuplcates) {
			RemoveDuplicates filter = new RemoveDuplicates();
			filter.setInputFormat(temp);
			temp = Filter.useFilter(temp, filter);
		}
		MissingValueHandler mvh = new MissingValueHandler(temp, ignoreInstancesWithMissingValues);
		mvh.handleMissingValues();
		return temp;
	}
	
	/**
	 * Loads a dataset from a given ARFF file
	 * @param arffFile The input ARFF file
	 * @return The loaded {@link Instances}
	 * @throws Exception if something goes wrong while loading the dataset
	 */
	public static Instances loadARFF(String arffFile) throws Exception {
		DataSource source = new DataSource(arffFile);
		Instances dataset = source.getDataSet();
		if (dataset.classIndex() == -1) {
			int classIndex = dataset.numAttributes() - 1;
			Attribute classAttribute = dataset.attribute(classIndex);
			if(classAttribute.isNominal())
				dataset.setClassIndex(classIndex);
			else
				throw new IllegalStateException("The last attribute of the dataset must be nominal class attribute");
		}
		return dataset;
	}

}
