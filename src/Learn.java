import java.io.*;
import java.util.*;

/**
 * Author:      Grant Kurtz
 * <p/>
 * Learn will attempt to find the set of Hypothesis (stumps) that best correlate
 * with movements on the NASDAQ. The stumps are given weights as determined by
 * the AdaBoost algorithm.
 */
public class Learn{

	// The input file used for training our learning models.
	private static final String INPUT_FILE = "market_data.txt";
	private static final String INPUT_DIR = "input";
	private static final String INPUT_PATH = INPUT_DIR + File.separator +
											 INPUT_FILE;

	// The expected result for the NASDAQ movement on a given day.
	private static final String NASDAQ_FILE = "nasdaq_data.csv";
	private static final String NASDAQ_DIR = "input";
	private static final String NASDAQ_PATH = NASDAQ_DIR + File.separator +
											  NASDAQ_FILE;

	// The Hypothesis (stumps) and their weights used to build our model.
	private static final String OUTPUT_DIR = "stump_output";
	private static final String OUTPUT_FILE = "stumps.txt";
	private static final String OUTPUT_PATH = OUTPUT_DIR + File.separator +
											  OUTPUT_FILE;

	/**
	 * This nested HashMap was chosen to efficiently aid in Hypothesis answers.
	 * Since all lookups are highly dependent on the data and the stock, we want to
	 * hash on those values.
	 */
	private HashMap<String, HashMap<String, ModelData>> stocks;

	/*
	 * The INPUT_FILE has data for stocks not listed on the DJ30, so this simple
	 * filter is used to ignore stocks not on the DJ30.
	 */
	private HashSet<String> filter;

	/*
	 * The list of hypothesis about the NASDAQ market movements.
	 */
	private ArrayList<Hypothesis> hypothesises;

	/*
	 * Market data that explicitly states how the market moved for a given date.
	 */
	private ArrayList<Example> nasdaq;

	public static void main(String[] args){
		System.out.println("Looking for file '" + INPUT_PATH + "'...");
		System.out.println("Looking for file '" + NASDAQ_PATH + "'...");
		System.out.println("Looking for file '" + OUTPUT_PATH + "'...");
		Scanner input = null;
		Scanner nasdaqInput = null;
		BufferedWriter output = null;

		// Make sure we get all the file handles we need before we get
		// started doing the heavy-lifting, no need to have the user wait for
		// several seconds before they know we can't read/write to a needed
		// file!
		try{
			input = new Scanner(new File(INPUT_PATH));
			nasdaqInput = new Scanner(new File(NASDAQ_PATH));
			output = new BufferedWriter(new FileWriter(OUTPUT_PATH));
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
		System.out.println("Found!\nProcessing...");
		new Learn(input, nasdaqInput, output);

		// Just for some easy house keeping
		input.close();
		nasdaqInput.close();
		System.out.println("Complete!");
	}

	public Learn(Scanner input, Scanner nasdaqInput, BufferedWriter output){
		setupFilter();

		// first, process the NASDAQ file, compiling a list of days where it
		// went up/down for each date
		processNASDAQ(nasdaqInput);

		// Next, read in the data for individual stocks on the DJ30
		processDowJones(input);

		// Create our set of Hypothesis
		createHypothesis();

		// Now, we need to use AdaBoost to improve the accuracy of our
		// hypothesis
		boost();

		// Output a series of (weighted) decision stumps
		outputStumps(output);
	}

	/**
	 * Given the stock data that represents the time period we are training
	 * against, the Hypothesis will make predictions on that data about the
	 * NASDAQ.
	 */
	private void createHypothesis(){

		// Our first set of hypothesis is to correlate an individual stock's
		// movement with the NASDAQ
		hypothesises = new ArrayList<Hypothesis>();
		for(String symbol : filter){
			hypothesises.add(new ParallelMovement(stocks, symbol));
		}

		// We can try and correlate total stock market movements
		hypothesises.add(new TotalMovement(stocks));
	}

	/**
	 * The implementation of AdaBoost, as specified during lecture and in the book,
	 * "Artificial Intelligence: A Modern Approach", Third Edition, Section 18.10,
	 * figure 18.34 (page 751). The only liberty taken was to ignore results from
	 * Hypothesis if a zero was returned.  This is done because the Hypothesis
	 * probably didn't have data for that day, and so to avoid skewing the model in
	 * any particular direction error is not accumulated or applied for those
	 * days.
	 */
	private void boost(){

		// Initially, all hypothesis are equally likely, so each hypothesis gets
		// an equal weight.
		double[] exampleWeights = new double[nasdaq.size()];
		Arrays.fill(exampleWeights, (1.0 / nasdaq.size()));

		// After testing several values for iterations, below ~47 and above ~48
		// the accuracy on the training data would taper off.  In the extreme,
		// several weights would approach Infinity, suggesting that AdaBoost
		// was starting to overfit some models.
		for(int iterations = 0; iterations < 47; iterations++){
			double error = 0.0;
			int model = getBestModel(exampleWeights);
			for(int ex = 0; ex < nasdaq.size(); ex++){
				int prediction = hypothesises.get(model).prediction(
						nasdaq.get(ex));
				int movement = nasdaq.get(ex).close > nasdaq.get(ex).open ?
							   1 : -1;
				if(prediction != 0 && prediction != movement){
					error += exampleWeights[ex];
				}
			}

			// Computing the error was done with this method as it produced
			// reliable results, whereas the method the book gives never
			// produced useful values.
			hypothesises.get(model).setWeight(Math.log((1.0 - error) / error));
			for(int ex = 0; ex < nasdaq.size(); ex++){
				int prediction = hypothesises.get(model).prediction(
						nasdaq.get(ex));
				int movement = nasdaq.get(ex).close > nasdaq.get(ex).open ?
							   1 : -1;
				if(prediction != 0 && prediction == movement){
					double sum = getSumOfWeights()
								 / hypothesises.get(model).getWeight();
					exampleWeights[ex] = exampleWeights[ex] * Math.exp(
							-sum * prediction * movement);
				}
			}

			// Normalize our results for simplicity
			normalize(exampleWeights);
		}
	}

	/**
	 * Nothing special, just a standard normalize function.  Normalization is made
	 * for 1.0.
	 *
	 * @param exampleWeights The array of weights to normalize across.
	 */
	private void normalize(final double[] exampleWeights){
		double total = 0.0;
		for(double d : exampleWeights){
			total += d;
		}
		for(int w = 0; w < exampleWeights.length; w++){
			exampleWeights[w] = exampleWeights[w] / total;
		}
	}

	/**
	 * Outputs a "stringified" version of the Hypothesis.  This was chosen over
	 * serialization for its simplicity. In particular, stringifying is easier for
	 * the TestTraining program where the Hypothesis isn't rebuilt.
	 *
	 * @param output The file handler to output the results to.
	 */
	private void outputStumps(BufferedWriter output){
		try{
			for(Hypothesis h : hypothesises){
				output.write(h + "," + h.getWeight() + "\n");
			}
		}
		catch(IOException e){
			System.err.println("Unable to write results to file '"
							   + OUTPUT_PATH + "'! Exiting...");
			System.exit(1);
		}
		finally{
			try{
				output.close();
			}
			catch(IOException e){
				System.err.println("Unable to close file handle for '"
								   + OUTPUT_PATH + "'! Results may not have "
								   + "been saved! Exiting...");
				System.exit(1);
			}
		}
	}

	/**
	 * Dumps all the Dow Jones Industrial Average data for later use in producing
	 * predictions for Hypothesis.
	 * <p/>
	 * The expected format is as follows: YYYYMMDD,SYMBOL,OPEN,HIGH,LOW,CLOSE,VOLUME
	 * <p/>
	 * Each line will be unique across the Date and Symbol, and any line not
	 * conforming to the above format is discarded. Any unparsable values will also
	 * have the data for that stock on that day thrown out.
	 *
	 * @param input The file handler to read from.
	 */
	private void processDowJones(Scanner input){
		String line;
		String[] values;
		stocks = new HashMap<String, HashMap<String, ModelData>>();

		while(input.hasNext() && (line = input.nextLine()) != null){
			values = line.split(",");

			// Discard garbage lines, we don't know what to do with them anyway
			if(values == null || values.length != 7){
				continue;
			}

			String date = values[0];
			String symbol = values[1];

			// Only process stocks on the Dow Jones
			if(!filter.contains(symbol)){
				continue;
			}

			double open, close, high, low;
			long volume;
			try{
				open = Double.parseDouble(values[2]);
				high = Double.parseDouble(values[3]);
				low = Double.parseDouble(values[4]);
				close = Double.parseDouble(values[5]);
				volume = Long.parseLong(values[6]);
			}
			catch(NumberFormatException nfe){
				// Ignore it, we can't do anything with bad data
				continue;
			}

			// Store this result
			if(stocks.get(date) == null){
				stocks.put(date, new HashMap<String, ModelData>());
			}
			stocks.get(date).put(symbol, new ModelData(symbol, open, high, low,
													   close, volume));
		}
	}

	/**
	 * Dumps all the NASDAQ results for later use in fine-tuning the predictions of
	 * Hypothesis.
	 * <p/>
	 * The expected format of each line is as follows: YYYYMMDD,OPEN,HIGH,LOW,CLOSE,VOLUME,ADJUSTED_CLOSE
	 * <p/>
	 * Each line will be unique across the Date, and any line not conforming to the
	 * above format is discarded. Any unparsable values will also have the data for
	 * that day thrown out.
	 *
	 * @param nasdaqInput The file handler to read from.
	 */
	private void processNASDAQ(Scanner nasdaqInput){

		String line;
		String[] values;
		nasdaq = new ArrayList<Example>();

		while(nasdaqInput.hasNext() && (line = nasdaqInput.nextLine()) != null){
			values = line.split(",");

			// Discard garbage lines, we don't know what to do with them anyway
			if(values == null || values.length != 7){
				continue;
			}

			double open, close, high, low, adjustedClosed;
			long volume;
			String date = values[0];
			try{
				open = Double.parseDouble(values[1]);
				high = Double.parseDouble(values[2]);
				low = Double.parseDouble(values[3]);
				close = Double.parseDouble(values[4]);
				volume = Long.parseLong(values[5]);
				adjustedClosed = Double.parseDouble(values[6]);
			}
			catch(NumberFormatException nfe){
				// Ignore it, we can't do anything with bad data
				continue;
			}

			// Determine the movement (Up/Down)
			nasdaq.add(new Example(date, open, high, low, close, volume, adjustedClosed));
		}
	}

	/**
	 * Initializes the filter with the 30 stocks listed on the Doq Jones that we
	 * are interested in processing.
	 */
	private void setupFilter(){
		filter = new HashSet<String>();
		filter.add("MMM");
		filter.add("AA");
		filter.add("AXP");
		filter.add("T");
		filter.add("BAC");
		filter.add("BA");
		filter.add("CAT");
		filter.add("CVX");
		filter.add("CSCO");
		filter.add("KO");
		filter.add("DD");
		filter.add("XOM");
		filter.add("GE");
		filter.add("HPQ");
		filter.add("HD");
		filter.add("INTC");
		filter.add("IBM");
		filter.add("JNJ");
		filter.add("JPM");
		filter.add("MCD");
		filter.add("MRK");
		filter.add("MSFT");
		filter.add("PFE");
		filter.add("PG");
		filter.add("TRV");
		filter.add("UNH");
		filter.add("UTX");
		filter.add("VZ");
		filter.add("WMT");
		filter.add("DIS");
	}

	public int getBestModel(double[] exampleWeights){
		int bestModel = 0;
		double bestWeight = Double.NEGATIVE_INFINITY;
		for(int h = 0; h < hypothesises.size(); h++){
			double adjWeight = 0.0;
			for(int ex = 0; ex < nasdaq.size(); ex++){
				int movement =
						nasdaq.get(ex).close > nasdaq.get(ex).open ? 1 : -1;
				int pred = hypothesises.get(h).prediction(nasdaq.get(ex));
				adjWeight += movement * pred * exampleWeights[ex];
			}
			if(adjWeight > bestWeight){
				bestModel = h;
				bestWeight = adjWeight;
			}
		}
		return bestModel;
	}

	public double getSumOfWeights(){
		double sumOfWeights = 0.0;
		for(Hypothesis h : hypothesises){
			sumOfWeights += h.getWeight();
		}
		return sumOfWeights;
	}
}
