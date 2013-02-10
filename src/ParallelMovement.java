import org.omg.CORBA.PRIVATE_MEMBER;

import java.util.HashMap;

/**
 * Author:      Grant Kurtz
 *
 * This Hypothesis assumes if our stock price closed higher than opened (went
 * up), then the NASDAQ will do the same.
 */
public class ParallelMovement implements Hypothesis{

	private static HashMap<String, HashMap<String, ModelData>> data = null;
	private String symbol;
	private double weight;

	public ParallelMovement(HashMap<String, HashMap<String, ModelData>> data, String symbol){
		this.data = data;
		this.symbol = symbol;
		weight = 1.0;
	}

	/**
	 * Returns the sign of the predicted parallel movement (both up, both down).
	 *
	 * @param ex	The example to test against.
	 * @return		1 to mean that our stock price went up and the prediction is
	 * 				that the NASDAQ will increase, while -1 indicates this stock
	 * 				price fell, and the NASDAQ is expected to fall.  A 0
	 * 				indicates missing data. A 0 suggests to the caller to ignore
	 * 				the result, but not necessary to do so.
	 */
	public int prediction(Example ex){
		if(data.get(ex.date) == null)
			return 0;
		ModelData model = data.get(ex.date).get(symbol);
		if(model == null)
			return 0;
		return model.close > model.open ? 1 : -1;
	}

	public double getWeight(){
		return weight;
	}

	public void setWeight(double weight){
		this.weight = weight;
	}

	public String toString(){
		return "PA," + symbol;
	}
}
