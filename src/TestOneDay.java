import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Author:      Grant Kurtz
 */
public class TestOneDay{

	private static final String STUMP_DIR = "stump_output";
	private static final String STUMP_FILE = "stumps.txt";
	private static final String STUMP_PATH = STUMP_DIR + File.separator +
											 STUMP_FILE;

	public static void main(String[] args){
		if(args.length != 1){
			System.err.println("Usage: java TestOneDay input_file");
			System.exit(1);
		}
		Scanner input = null;
		Scanner stumps = null;
		try{
			input = new Scanner(new File(args[0]));
			stumps = new Scanner(new File(STUMP_PATH));
		}
		catch(FileNotFoundException e){
			System.err.println("Unable to open input files! Exiting...");
		}

		new TestOneDay(input, stumps);
	}

	public TestOneDay(Scanner input, Scanner stumps){

	}
}
