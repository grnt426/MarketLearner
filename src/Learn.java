import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import sun.dc.path.PathError;
import sun.dc.pr.PathStroker;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Author:      Grant Kurtz
 */
public class Learn{

	private static final String INPUT_FILE = "input_data.txt";
	private static final String INPUT_DIR = "input";
	private static final String INPUT_PATH = INPUT_DIR + File.separator +
											 INPUT_FILE;
	private static final String OUTPUT_DIR = "stump_output";

	private HashMap<String, Integer> stocks;

	public static void main(String[] args){
		System.out.println("Looking for file '" + INPUT_PATH + "'...");
		Scanner input = null;
		try{
			input = new Scanner(new File(INPUT_PATH));
		}
		catch(FileNotFoundException e){
			System.err.println("Unable to open file for processing! " +
							   "Exiting...");
			System.exit(1);
		}
		System.out.println("Found! Processing...");
		new Learn(input);
	}

	public Learn(Scanner input){
		setupFilter();
		String line;
		String values[];

		// Simply process the whole file
		while(input.hasNext() && (line = input.nextLine()) != null){
			values = line.split(",");

			// Discard garbage lines, we don't know what to do with them anyway
			if(values == null || values.length != 7)
				continue;

			// Only process stocks on the Dow Jones
			if(stocks.containsKey(values[1]))
				stocks.put(values[1], stocks.get(values[1])+1);
			else
				continue;
		}

		for(String s : stocks.keySet()){
			System.out.println(s + ": " + stocks.get(s));
		}
	}

	/**
	 * Initializes the stocks map with the 30 stocks listed on the Doq Jones
	 * that we are interested in processing.
	 */
	private void setupFilter(){
		stocks = new HashMap<String, Integer>();
		stocks.put("MMM", 0);
		stocks.put("AA", 0);
		stocks.put("AXP", 0);
		stocks.put("T", 0);
		stocks.put("BAC", 0);
		stocks.put("BA", 0);
		stocks.put("CAT", 0);
		stocks.put("CVX", 0);
		stocks.put("CSCO", 0);
		stocks.put("KO", 0);
		stocks.put("DD", 0);
		stocks.put("XOM", 0);
		stocks.put("GE", 0);
		stocks.put("HPQ", 0);
		stocks.put("HD", 0);
		stocks.put("INTC", 0);
		stocks.put("IBM", 0);
		stocks.put("JNJ", 0);
		stocks.put("JPM", 0);
		stocks.put("MCD", 0);
		stocks.put("MRK", 0);
		stocks.put("MSFT", 0);
		stocks.put("PFE", 0);
		stocks.put("PG", 0);
		stocks.put("TRV", 0);
		stocks.put("UNH", 0);
		stocks.put("UTX", 0);
		stocks.put("VZ", 0);
		stocks.put("WMT", 0);
		stocks.put("DIS", 0);
	}
}
