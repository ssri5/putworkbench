package in.ac.iitk.cse.putwb.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * A utility class for taking care of all icons and their respective picture files
 * @author Saurabh Srivastava
 *
 */
public abstract class IconCreator {

	/**
	 * Icon File for ANALYZE Status Button
	 */
	public static final String ANALYZE_ICON_FILE = "Analyze.png";
	
	/**
	 * Icon File for Back Button
	 */
	public static final String BACK_ICON_FILE = "Back.png";
	
	/**
	 * Icon File for Classification Diagram
	 */
	public static final String CLASSIFICATION_ICON_FILE = "Classification.png";
	
	/**
	 * Icon File for Classifier Status Button
	 */
	public static final String CLASSIFIER_ICON_FILE = "Classifier.png";
	
	/**
	 * Icon File for Close Button
	 */
	public static final String CLOSE_ICON_FILE = "Close.png";
	
	/**
	 * Icon File for Data Source Status Button
	 */
	public static final String DATA_SOURCE_ICON_FILE = "DS.png";
	
	/**
	 * Icon File for Analyze Status Button
	 */
	public static final String EXPENSE_ICON_FILE = "Expense.png";
	
	/**
	 * Icon File for Fast Forward Button
	 */
	public static final String FAST_FORWARD_ICON_FILE = "FastForward.png";
	
	/**
	 * Icon File for Forward Button
	 */
	public static final String FORWARD_ICON_FILE = "Forward.png";
	
	/**
	 * Icon File for Autopilot Button
	 */
	public static final String PLANE_ICON_FILE = "Plane.png";
	
	/**
	 * Icon File for Privacy Status Button
	 */
	public static final String PRIVACY_ICON_FILE = "Privacy.png";
	
	/**
	 * Icon File for Rewind Button
	 */
	public static final String REWIND_ICON_FILE = "Rewind.png";
	
	/**
	 * Icon File for Run Status Button
	 */
	public static final String RUN_ICON_FILE = "Run.png";
	
	/**
	 * Icon File for Upload Dataset Diagram
	 */
	public static final String UPLOAD_DATASET_ICON_FILE = "UploadDataset.png";
	
	/**
	 * Icon File for Upload Experiment Diagram
	 */
	public static final String UPLOAD_EXPERIMENT_ICON_FILE = "UploadExperiment.png";
	
	/**
	 * Returns an Icon in object form, for the given file
	 * @param pathToImageFile path to image file
	 * @return an <code>ImageIcon</code> object corresponding to the image
	 * @throws IllegalArgumentException if the specified file does not point to an image file
	 */
	public static ImageIcon getIcon(String pathToImageFile) throws IllegalArgumentException {
		return getIcon(pathToImageFile, "");
	}
	
	/**
	 * Returns an Icon in object form, for the given file and alternative text
	 * @param pathToImageFile pathToImageFile path to image file
	 * @param altText alternative text for the image
	 * @return an <code>ImageIcon</code> object corresponding to the image
	 * @throws IllegalArgumentException if the specified file does not point to an image file
	 */
	public static ImageIcon getIcon(String pathToImageFile, String altText) throws IllegalArgumentException {
		ClassLoader loader = IconCreator.class.getClassLoader();
		try {
			BufferedImage icon = ImageIO.read(loader.getResourceAsStream(pathToImageFile));
			return new ImageIcon(icon, altText);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot find the specified icon file: " + pathToImageFile);
		}
	}
}
