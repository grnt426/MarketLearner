import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Author:      Grant Kurtz
 */
public class TestAgainstTraining{

	private static final String INPUT_FILE = "market_data.txt";
	private static final String INPUT_DIR = "input";
	private static final String INPUT_PATH = INPUT_DIR + File.separator +
											 INPUT_FILE;

	// The expected result for the NASDAQ movement on a given day.
	private static final String NASDAQ_FILE = "nasdaq_data.csv";
	private static final String NASDAQ_DIR = "input";
	private static final String NASDAQ_PATH = NASDAQ_DIR + File.separator +
											  NASDAQ_FILE;

	// The best Hypothesis' for making a prediction.
	private static final String STUMP_DIR = "stump_output";
	private static final String STUMP_FILE = "best_stumps.txt";
	private static final String STUMP_PATH = STUMP_DIR + File.separator +
											 STUMP_FILE;

	private HashMap<String, HashMap<String, ModelData>> stocks;
	private HashSet<String> filter;
	private ArrayList<Example> nasdaq;

	public static void main(String[] args){
		Scanner input = null;
		Scanner nasdaqInput = null;
		Scanner stumps = null;

		// Make sure we get all the file handles we need before we get
		// started doing the heavy-lifting, no need to have the user wait for
		// several seconds before they know we can't read/write to a needed
		// file!
		try{
			input = new Scanner(new File(INPUT_PATH));
			nasdaqInput = new Scanner(new File(NASDAQ_PATH));
			stumps = new Scanner(new File(STUMP_PATH));
		}
		catch(FileNotFoundException e){
			System.err.println("Unable to open file for processing! " +
							   "Exiting...");
			System.exit(1);
		}

		new TestAgainstTraining(input, nasdaqInput, stumps);
	}

	public TestAgainstTraining(Scanner input, Scanner nasdaqInput,
							   Scanner stumps){
		setupFilter();
		processNASDAQ(nasdaqInput);
		processDowJones(input);
		ArrayList<Hypothesis> hypothesis = readHypothesis(stumps, stocks);
		double correct = 0.0;
		for(Example ex : nasdaq){
			double vote = 0.0;
			for(Hypothesis h : hypothesis){
				vote += h.prediction(ex) * h.getWeight();
			}
			if(vote > 0 && ex.close > ex.open){
				correct += 1.0;
			}
			else if(vote < 0 && ex.open > ex.close){
				correct += 1.0;
			}
		}
		System.out.println("Percent Correct: " + correct / nasdaq.size() * 100 + "%");
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

	/**
	 * Converts the stringified versions of the Hypothesis into their respective
	 * implementations.
	 *
	 * @param stumpInput The file for reading in the Hypothesis.
	 * @param stocks     The data to give the Hypothesis about the day's
	 *                   movements.
	 *
	 * @return The Hypothesis that were successfully parsed.
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
}
