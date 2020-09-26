package in.ac.iitk.cse.putwb.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

/**
 * @author Saurabh Srivastava
 *
 */
public class CSVConverter {

	/**
	 * Creates an ARFF file, if it doesn't already exist (in which case, it is overwritten).
	 * The created ARFF file contains the data in the supplied input file, converted to ARFF.
	 * @param inputCSVFile The input file
	 * @param outputFile The converted data file in ARFF
	 * @throws IOException if something goes wrong during conversion
	 */
	public void saveAsARFF(File inputCSVFile, File outputFile) throws IOException {
		/*
		 * Sanity checks
		 */
		
		// Check if input file exists or not
		if(!inputCSVFile.exists())
			throw new FileNotFoundException(inputCSVFile.getAbsolutePath() + " does not exist.");
		
		// Check if the output file can be created or not
		if(!outputFile.exists() && !outputFile.createNewFile())
			throw new IOException("Could not create new file " + outputFile.getAbsolutePath());
		
		CSVLoader loader = new CSVLoader();
		loader.setSource(inputCSVFile);
		loader.setNominalAttributes("last");
		Instances data = loader.getDataSet();
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(outputFile);
		saver.setDestination(outputFile);
		saver.writeBatch();
	}

}
