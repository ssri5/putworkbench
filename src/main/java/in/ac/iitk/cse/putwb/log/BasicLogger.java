package in.ac.iitk.cse.putwb.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Just a basic logger for our use. In future versions, we may choose a more sophisticated logging option like <i>log4j</i>.
 * @author Saurabh Srivastava
 *
 */
public class BasicLogger {

	/**
	 * A static instance of default logger that logs to console (or any other standard output/error streams, if redirected) 
	 */
	private static final BasicLogger defaultLogger = new BasicLogger();
	
	static {
		defaultLogger.stdout = System.out;
		defaultLogger.stderr = System.err;
	}
	
	/**
	 * Returns a logger that logs to the standard output and error streams
	 * @return A <i>default</i> logger
	 */
	public static BasicLogger getDefaultLogger() {
		return defaultLogger;
	}
	
	/**
	 * Creates a logger which outputs to given output file and standard error stream
	 * @param outputFile The output file to produce output to
	 * @return The logger with required specifications
	 * @throws FileNotFoundException If the file requested is not found and cannot be created
	 */
	public static BasicLogger getLogger(File outputFile) throws FileNotFoundException {
		return getLogger(new PrintStream(outputFile), System.err);
	}
	
	/**
	 * Creates a logger which outputs to given output and error files
	 * @param outputFile The output file to produce output to
	 * @param errorFile The error file to produce error text to
	 * @return The logger with required specifications 
	 * @throws FileNotFoundException If either or of the files requested is not found and cannot be created
	 */
	public static BasicLogger getLogger(File outputFile, File errorFile) throws FileNotFoundException {
		BasicLogger logger = new BasicLogger();
		logger.stdout = new PrintStream(outputFile);
		logger.stderr = new PrintStream(errorFile);
		return logger;
	}
	
	/**
	 * Creates a logger with given output stream and standard error stream
	 * @param outputStream The output stream to use
	 * @return The logger with required specifications
	 */
	public static BasicLogger getLogger(PrintStream outputStream) {
		return getLogger(outputStream, System.err);
	}
	
	/**
	 * Creates a logger with given output and error stream
	 * @param outputStream The output stream to use
	 * @param errorStream The error stream to use
	 * @return The logger with required specifications
	 */
	public static BasicLogger getLogger(PrintStream outputStream, PrintStream errorStream) {
		BasicLogger logger = new BasicLogger();
		logger.stdout = outputStream;
		logger.stderr = errorStream;
		return logger;
	}
	
	/**
	 * The error stream for the logger
	 */
	private PrintStream stderr;
	
	/**
	 * The output stream for the logger
	 */
	private PrintStream stdout;
	
	/**
	 * Private constructor - puts it out of bounds for classes to create an instance directly
	 */
	private BasicLogger() {
		// Do not allow creating an instance from outside
	}
	
	/**
	 * Outputs error text to set stream (by calling <cod>toString()</code> method on the parameter)
	 * @param error The error
	 */
	public void error(Object error) {
		stderr.print(error);
	}
	
	/**
	 * Outputs error text to set stream (by calling <cod>toString()</code> method on the parameter), followed by a newline
	 * @param error
	 */
	public void errorln(Object error) {
		stderr.println(error);
	}
	
	/**
	 * Puts the stack trace of the given exception in the error stream
	 * @param ex The exception
	 */
	public void exception(Exception ex) {
		ex.printStackTrace(stderr);
	}
	
	/**
	 * Outputs text to set stream (by calling <cod>toString()</code> method on the parameter)
	 * @param output The output 
	 */
	public void out(Object output) {
		stdout.print(output);
	}
	
	/**
	 * Outputs text to set stream (by calling <cod>toString()</code> method on the parameter), followed by a newline
	 * @param output The output
	 */
	public void outln(Object output) {
		stdout.println(output);
	}
}
