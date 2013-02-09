import java.io.*;
import java.util.*;

/**
 * Author:      Grant Kurtz
 */
public class Learn{

	private static final String INPUT_FILE = "market_data.txt";
	private static final String INPUT_DIR = "input";
	private static final String INPUT_PATH = INPUT_DIR + File.separator +
											 INPUT_FILE;
	private static final String NASDAQ_FILE = "nasdaq_data.csv";
	private static final String NASDAQ_DIR = "input";
	private static final String NASDAQ_PATH = NASDAQ_DIR + File.separator +
											  NASDAQ_FILE;
	private static final String OUTPUT_DIR = "stump_output";
	private static final String OUTPUT_FILE = "stumps.txt";
	private static final String OUTPUT_PATH = OUTPUT_DIR + File.separator +
											  OUTPUT_FILE;

	/**
	 * For simplicity, this is chosen to allow for easy filtering of the input
	 * file.
	 */
	private HashMap<String, HashMap<String, ModelData>> stocks;
	private HashSet<String> filter;
	private ArrayList<Hypothesis> hypothesises;

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

		// Next, read in the data for individual stocks on DJ30
		processDowJones(input);

		// Create our set of Hypothesis
		createHypothesis();

		// Now, we need to use AdaBoost to improve the accuracy of our
		// hypothesis
		double[] weights = boost();

		// Output a series of (weighted) decision stumps
		outputStumps(output, weights);
	}

	private void createHypothesis(){

		// Our first set of hypothesis is to correlate an individual stock's
		// movement with the NASDAQ
		hypothesises = new ArrayList<Hypothesis>();
		for(String symbol : filter){
			hypothesises.add(new ParallelMovement(stocks, symbol));
		}
	}

	private double[] boost(){

		// Initially, all hypothesis are equally likely, so each hypothesis gets
		// an equal weight
		double[] exampleWeights = new double[nasdaq.size()];
		Arrays.fill(exampleWeights, (1.0 / nasdaq.size()));
		double[] resultWeights = new double[nasdaq.size()];
		for(int model = 0; model < hypothesises.size(); model++){
			double error = 0.0;
			for(int ex = 0; ex < nasdaq.size(); ex++){
				int prediction = hypothesises.get(model).prediction(
						nasdaq.get(ex));
				int movement = nasdaq.get(ex).close > nasdaq.get(ex).close ?
							   1 : -1;
				if(prediction != 0 && prediction != movement){
					error += exampleWeights[ex];
				}
			}
			for(int ex = 0; ex < nasdaq.size(); ex++){
				int prediction = hypothesises.get(model).prediction(
						nasdaq.get(ex));
				int movement = nasdaq.get(ex).close > nasdaq.get(ex).close ?
							   1 : -1;
				if(prediction != 0 && prediction == movement){
					exampleWeights[ex] *= error / (1.0 - error);
				}
			}
			normalize(exampleWeights);
			resultWeights[model] = Math.log((1.0 - error) / error);
		}
		return resultWeights;
	}

	private void normalize(double[] exampleWeights){
		double total = 0.0;
		for(double d : exampleWeights)
			total += d;
		for(int w = 0; w < exampleWeights.length; w++){
			exampleWeights[w] = exampleWeights[w] / total;
		}
	}


	private void outputStumps(BufferedWriter output, double[] weights){
		try{
			int weight = 0;
			for(Hypothesis h : hypothesises){
				output.write(h + "," + weights[weight] + "\n");
				weight++;
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
			int volume;
			try{
				open = Double.parseDouble(values[2]);
				high = Double.parseDouble(values[3]);
				low = Double.parseDouble(values[4]);
				close = Double.parseDouble(values[5]);
				volume = Integer.parseInt(values[6]);
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
			int volume;
			String date = values[0];
			try{
				open = Double.parseDouble(values[1]);
				high = Double.parseDouble(values[2]);
				low = Double.parseDouble(values[3]);
				close = Double.parseDouble(values[4]);
				volume = Integer.parseInt(values[5]);
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
	 * Initializes the stocks map with the 30 stocks listed on the Doq Jones that
	 * we are interested in processing.
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
}
