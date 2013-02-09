import org.omg.CORBA.PRIVATE_MEMBER;

import java.util.HashMap;

/**
 * Author:      Grant Kurtz
 */
public class ParallelMovement implements Hypothesis{

	private static HashMap<String, HashMap<String, ModelData>> data = null;
	private String symbol;
	private boolean result = false;

	public ParallelMovement(HashMap<String, HashMap<String, ModelData>> data, String symbol){
		this.data = data;
		this.symbol = symbol;
	}

	public boolean isRight(Example ex){
		ModelData model = data.get(ex.date).get(symbol);
		return ex.close > ex.open && model.close > model.open;
	}
}
