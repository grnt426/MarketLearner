import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Author:      Grant Kurtz
 */
public class TestTraining{

	private static final String INPUT_DIR = "stump_output";
	private static final String INPUT_FILE = "stumps.txt";
	private static final String INPUT_PATH = INPUT_DIR + File.separator +
											 INPUT_FILE;

	public static void main(String[] args){
		System.out.println("Looking for input file '" + INPUT_PATH + "'...");
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
		new TestTraining(input);
	}

	public TestTraining(Scanner input){

	}
}
