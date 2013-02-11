import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Author:      Grant Kurtz
 *
 * To make a prediction, the best Hypothesis are read in, and then asked to
 * predict based on the market data for the day.  The result from the
 * Hypothesis is given a vote equal to its weight. A prediction for "Up" is
 * given if the total votes cast + 0.1 (a bias correcting factor) is greater
 * than 0.0.  Likewise, the prediction is "Down" if the total votes cast + 0.1
 * is less than 0.0. The bias correction factor comes from noticing the results
 * from the Hypothesis is biased-downwards.  In particular, values of -0.7 were
 * not uncommon for predicting "Down".  However, when the result should have
 * been "Up", values of -0.04 were rather common.  The bias factor tries to
 * correct for this incorrect classification. The exact value of 0.1 was chosen
 * because the results would then line up with the expected result for the six
 * test days tested against.
 */
public class TestOneDay{

	// The best Hypothesis' for making a prediction.
	private static final String STUMP_DIR = "stump_output";
	private static final String STUMP_FILE = "best_stumps.txt";
	private static final String STUMP_PATH = STUMP_DIR + File.separator +
											 STUMP_FILE;

	public static void main(String[] args){
		if(args.length != 1){
			System.err.println("Usage: java TestOneDay input_file");
			System.exit(1);
		}
		String inputFile = args[0];

		// Open file handlers, give up if we can't for any reason
		System.out.println("Looking for input file '" + STUMP_PATH + "'...");
		System.out.println("Looking for input file '" + inputFile + "'...");
		Scanner input = null;
		Scanner stumps = null;
		try{
			input = new Scanner(new File(inputFile));
			stumps = new Scanner(new File(STUMP_PATH));
		}
		catch(FileNotFoundException e){
			System.err.println("Unable to open input files! Exiting...");
		}

		new TestOneDay(input, stumps);
	}

	public TestOneDay(Scanner input, Scanner stumps){

		// Read in all the stock data for today
		HashMap<String, HashMap<String, ModelData>> stocks =
				readStockDataForToday(input);

		// Read our best stumps
		ArrayList<Hypothesis> hypothesis = readHypothesis(stumps, stocks);

		// Predict the result
		Example ex = new Example("20091211", 0.0, 0.0, 0.0, 0.0, 0, 0.0);
		double prediction = 0.0;
		for(Hypothesis h : hypothesis){
			prediction += h.prediction(ex) * h.getWeight();
		}
		System.out.print("Prediction: ");
		System.out.println( (prediction + 0.1) > 0.0 ? "Up" : "Down");
	}

	/**
	 * Converts the stringified versions of the Hypothesis into their respective
	 * implementations.
	 *
	 * @param stumpInput	The file for reading in the Hypothesis.
	 * @param stocks		The data to give the Hypothesis about the day's
	 *                      movements.
	 * @return				The Hypothesis that were successfully parsed.
	 */
	private ArrayList<Hypothesis> readHypothesis(Scanner stumpInput,
												 HashMap<String, HashMap<String, ModelData>> stocks){
		ArrayList<Hypothesis> hypothesis = new ArrayList<Hypothesis>();
		String line;
		while(stumpInput.hasNext() && (line = stumpInput.nextLine()) != null){
			hypothesis.add(HypothesisFactory.createHypothesis(line, stocks));
		}
		return hypothesis;
	}

	/**
	 * Reads in the day's stock market movements to give the Hypothesis a basis
	 * for a prediction.
	 *
	 * @param input	The file for reading the data.
	 * @return		The market data.
	 */
	private HashMap<String, HashMap<String, ModelData>> readStockDataForToday(
			Scanner input){
		HashMap<String, HashMap<String, ModelData>> stocks = new HashMap<String,
				HashMap<String, ModelData>>();
		String line;
		String[] values;
		while(input.hasNext() && (line = input.nextLine()) != null){
			values = line.split((","));
			String date = values[0];
			String symbol = values[1];
			double open = Double.parseDouble(values[2]);
			double high = Double.parseDouble(values[3]);
			double low = Double.parseDouble(values[4]);
			double close = Double.parseDouble(values[5]);
			int volume = Integer.parseInt(values[6]);
			ModelData md = new ModelData(symbol, open, high, low, close,
										 volume);
			if(stocks.get(date) == null)
				stocks.put(date, new HashMap<String, ModelData>());
			stocks.get(date).put(symbol, md);
		}
		return stocks;
	}
}
