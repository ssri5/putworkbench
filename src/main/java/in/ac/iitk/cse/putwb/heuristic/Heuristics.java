/**
 * 
 */
package in.ac.iitk.cse.putwb.heuristic;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a set of heuristics that takes a set of inputs, evaluates a set of rules over it, and return a decision.
 * @author Saurabh Srivastava
 *
 */
public abstract class Heuristics {

	/**
	 * A utility method to create an map out of a sequence of key-value pairs
	 * @param inputs A sequence of key-value pairs
	 * @return A map containing the provided key-value pairs
	 * @throws IllegalArgumentException If something goes wrong while processing the inputs
	 */
	public static Map<String, Object> buildMap(Object...inputs) throws IllegalArgumentException {
		if(inputs.length%2 != 0)
			throw new IllegalArgumentException("The number of parameters must be even to form key-value pairs");
		Map<String, Object> inputsMap = new HashMap<String, Object>();
		for(int i = 0; i < inputs.length; i+=2) {
			if(inputs[i] instanceof String) {
				inputsMap.put((String)inputs[i], inputs[i+1]);
			} else
				throw new IllegalArgumentException("The keys must be Strings only");
		}
		return inputsMap;
	}
	
	/**
	 * Provides a decision using a set of pre-defined rules, based on the given inputs 
	 * @param inputs The inputs over which the rules are evaluated, in the form of key-value pairs (keys can only be Strings, values can be anything)
	 * @return The decision provided by this heuristic
	 */
	public abstract Object getDecision(Map<String, Object> inputs) throws Exception;
	
}
