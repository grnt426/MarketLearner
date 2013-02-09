import org.omg.CORBA.PRIVATE_MEMBER;

import java.util.HashMap;

/**
 * Author:      Grant Kurtz
 */
public class ParallelMovement implements Hypothesis{

	private static HashMap<String, HashMap<String, ModelData>> data = null;
	private String symbol;

	public ParallelMovement(HashMap<String, HashMap<String, ModelData>> data, String symbol){
		this.data = data;
		this.symbol = symbol;
	}

	public int prediction(Example ex){
		if(data.get(ex.date) == null)
			return 0;
		ModelData model = data.get(ex.date).get(symbol);
		if(model == null)
			return 0;
		return model.close > model.open ? 1 : -1;
	}

	public String toString(){
		return "PA," + symbol;
	}
}
