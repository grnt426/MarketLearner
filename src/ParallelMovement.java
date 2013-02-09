import java.util.HashMap;

/**
 * Author:      Grant Kurtz
 */
public class ParallelMovement implements Hypothesis{

	private static HashMap<String, ModelData> data = null;
	private String symbol;

	public ParallelMovement(HashMap<String, ModelData> data, String symbol){
		this.data = data;
		this.symbol = symbol;
	}

	public boolean isRight(Example ex){
		return false;
	}
}
