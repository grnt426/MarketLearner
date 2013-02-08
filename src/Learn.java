import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

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

	private static final int DATE = 0;
	private static final int SYMB = 1;
	private static final int OPEN = 2;
	private static final int HIGH = 3;
	private static final int LOW = 4;
	private static final int CLOS = 5;
	private static final int VOL = 6;

	/**
	 * For simplicity, this is chosen to allow for easy filtering of the input
	 * file.
	 */
	private HashMap<String, Double> stocks;

	private HashMap<String, Boolean> nasdaq;

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
		nasdaq = new HashMap<String, Boolean>();

		// first, process the NASDAQ file, compiling a list of days where it
		// went up/down for each date
		processNASDAQ(nasdaqInput);

		// Next, read in the data for individual stocks on DJ30 and determine if
		// the sign movement is in the same direction (Up)
		processDowJones(input);

		// Output a series of (weighted) decision stumps
		createStumps(output);
	}

	private void createStumps(BufferedWriter output){
		try{
			for(String s : stocks.keySet()){
				output.write(s + ":" + stocks.get(s) + "\n");
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
		String prevSymbol = null;
		int prevSymbolCount = 0;

		while(input.hasNext() && (line = input.nextLine()) != null){
			values = line.split(",");

			// Discard garbage lines, we don't know what to do with them anyway
			if(values == null || values.length != 7){
				continue;
			}

			String date = values[DATE];
			String symbol = values[SYMB];

			// Only process stocks on the Dow Jones
			if(!stocks.containsKey(symbol)){
				continue;
			}

			if(prevSymbol == null || !prevSymbol.equals(symbol)){
				if(prevSymbol != null){
					stocks.put(prevSymbol,
							   stocks.get(prevSymbol) / prevSymbolCount);
				}
				prevSymbol = symbol;
				prevSymbolCount = 0;
			}
			else{
				prevSymbolCount++;
			}

			double open, close;
			try{
				open = Double.parseDouble(values[OPEN]);
				close = Double.parseDouble(values[CLOS]);
			}
			catch(NumberFormatException nfe){
				// Ignore it, we can't do anything with bad data
				continue;
			}

			// For robustness, make sure we have the same dates, if not, just
			// ignore it.  Again, there isn't much we can do anyway
			if(!nasdaq.containsKey(date)){
				continue;
			}

			// Increment if parallel movement (For now, only both Up)
			stocks.put(symbol, stocks.get(symbol)
							   + (nasdaq.get(date) && close > open ? 1 : 0));
		}
		if(prevSymbol != null){
			stocks.put(prevSymbol, stocks.get(prevSymbol) / prevSymbolCount);
		}
	}

	private void processNASDAQ(Scanner nasdaqInput){

		String line;
		String[] values;

		while(nasdaqInput.hasNext() && (line = nasdaqInput.nextLine()) != null){
			values = line.split(",");

			// Discard garbage lines, we don't know what to do with them anyway
			if(values == null || values.length != 7){
				continue;
			}

			double open, close;
			try{
				open = Double.parseDouble(values[OPEN - 1]);
				close = Double.parseDouble(values[CLOS - 1]);
			}
			catch(NumberFormatException nfe){
				// Ignore it, we can't do anything with bad data
				continue;
			}

			// Determine the movement (Up/Down)
			nasdaq.put(values[DATE], (close > open));
		}
	}

	/**
	 * Initializes the stocks map with the 30 stocks listed on the Doq Jones that
	 * we are interested in processing.
	 */
	private void setupFilter(){
		stocks = new HashMap<String, Double>();
		stocks.put("MMM", 0.0);
		stocks.put("AA", 0.0);
		stocks.put("AXP", 0.0);
		stocks.put("T", 0.0);
		stocks.put("BAC", 0.0);
		stocks.put("BA", 0.0);
		stocks.put("CAT", 0.0);
		stocks.put("CVX", 0.0);
		stocks.put("CSCO", 0.0);
		stocks.put("KO", 0.0);
		stocks.put("DD", 0.0);
		stocks.put("XOM", 0.0);
		stocks.put("GE", 0.0);
		stocks.put("HPQ", 0.0);
		stocks.put("HD", 0.0);
		stocks.put("INTC", 0.0);
		stocks.put("IBM", 0.0);
		stocks.put("JNJ", 0.0);
		stocks.put("JPM", 0.0);
		stocks.put("MCD", 0.0);
		stocks.put("MRK", 0.0);
		stocks.put("MSFT", 0.0);
		stocks.put("PFE", 0.0);
		stocks.put("PG", 0.0);
		stocks.put("TRV", 0.0);
		stocks.put("UNH", 0.0);
		stocks.put("UTX", 0.0);
		stocks.put("VZ", 0.0);
		stocks.put("WMT", 0.0);
		stocks.put("DIS", 0.0);
	}
}
