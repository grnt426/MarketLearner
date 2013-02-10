import java.util.HashMap;

/**
 * Author:      Grant Kurtz
 */
public class HypothesisFactory{
	public static Hypothesis createHypothesis(String hString,
											  HashMap<String, HashMap<String, ModelData>> stocks){
		Hypothesis h = null;
		String[] values = hString.split(",");
		String name = values[0];
		if(name.equals("PA")){
			String symbol = values[1];
			h = new ParallelMovement(stocks, symbol);
			h.setWeight(Double.parseDouble(values[2]));
		}
		else if(name.equals("TO")){
			h = new TotalMovement(stocks);
			h.setWeight(Double.parseDouble(values[1]));
		}
		return h;
	}
}
