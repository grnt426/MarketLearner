import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Author:      Grant Kurtz
 *
 * Consumes the output from Learn and outputs to a separate file the best
 * MAX_STUMPS stumps for use in generating predictions.
 */
public class TestTraining{

	// The total set of stumps to sift through.
	private static final String INPUT_DIR = "stump_output";
	private static final String INPUT_FILE = "stumps.txt";
	private static final String INPUT_PATH = INPUT_DIR + File.separator +
											 INPUT_FILE;

	// The location for the best stumps.
	private static final String OUTPUT_DIR = "stump_output";
	private static final String OUTPUT_FILE = "best_stumps.txt";
	private static final String OUTPUT_PATH = OUTPUT_DIR + File.separator +
											  OUTPUT_FILE;

	/**
	 * The maximum number of stumps is chosen based on how the stumps performed
	 * as a whole from the training data provided. This heuristic was derived
	 * by looking at the stumps and seeing where the drop-off point was, and
	 * choosing the stumps above that drop-off point. The most effective value
	 * is the one below. Results for this value and more are discussed in
	 * README.md.
	 */
	private final int MAX_STUMPS = 5;

	public static void main(String[] args){
		System.out.println("Looking for input file '" + INPUT_PATH + "'...");
		System.out.println("Looking for input file '" + OUTPUT_PATH + "'...");
		Scanner input = null;
		BufferedWriter output = null;

		try{
			output = new BufferedWriter(new FileWriter(OUTPUT_PATH));
			input = new Scanner(new File(INPUT_PATH));
		}
		catch(FileNotFoundException e){
			System.err.println("Unable to open file for processing! " +
							   "Exiting...");
			System.exit(1);
		}
		catch(IOException e){
			System.err.println("Unable to open output file for processing! " +
							   "Exiting...");
			System.exit(1);
		}
		System.out.println("Found! Processing...");
		try{
			new TestTraining(input, output);
		}
		catch(IOException e){
			System.err.println("Something happened while writing! Exiting...");
			System.exit(1);
		}
		finally{
			try{
				output.close();
			}
			catch(IOException e){
				System.err.println("Couldn't close the output file! Data " +
								   "probably lost! Exiting...");
				System.exit(1);
			}
		}

		// Make sure the buffer is flushed by closing the file handle
		try{
			output.close();
		}
		catch(IOException e){
			System.err.println("Unable to close the output file! Data " +
							   "probably lost! Exiting...");
			System.exit(1);
		}
	}

	public TestTraining(Scanner input, BufferedWriter output)
			throws IOException{
		String line;
		String[] values;
		ArrayList<String> hypothesisData = new ArrayList<String>();
		ArrayList<Double> hypothesisWeight = new ArrayList<Double>();

		while(input.hasNext() && (line = input.nextLine()) != null){
			values = line.split(",");

			// All we need is the weight of the hypothesis, no need to do
			// all the heavy processing of actually building the Hypothesis
			// object since we are choosing the best MAX_STUMPS stumps based
			// on the highest numerical weight.
			double weight = Double.parseDouble(values[values.length - 1]);
			insertIfBest(weight, line, hypothesisData, hypothesisWeight);
		}

		// Again, for simplicity, just output whatever the contents were
		// of the stringified Hypothesis.
		for(int i = 0; i < MAX_STUMPS; i++){
			System.out.println(hypothesisData.get(i));
			output.write(hypothesisData.get(i) + "\n");
		}
	}

	/**
	 * For simplicity, create a really lazy Insertion Sort routine.
	 *
	 * @param weight           The new weight to add.
	 * @param line             The new Stringified hypothesis to add.
	 * @param hypothesisData   The list of currently best hypothesis data.
	 * @param hypothesisWeight The list of currently best hypothesis data weight
	 *                         (used for sorting).
	 */
	private void insertIfBest(double weight, String line,
							  ArrayList<String> hypothesisData,
							  ArrayList<Double> hypothesisWeight){
		int toBump = 0;
		while(toBump < hypothesisWeight.size()){
			if(hypothesisWeight.get(toBump) < weight){
				break;
			}
			toBump++;
		}

		// We only want to store the top scoring hypothesis
		if(toBump == MAX_STUMPS){
			return;
		}

		hypothesisData.add(toBump, line);
		hypothesisWeight.add(toBump, weight);
		if(hypothesisData.size() > MAX_STUMPS){
			hypothesisData.remove(MAX_STUMPS);
			hypothesisWeight.remove(MAX_STUMPS);
		}
	}
}
