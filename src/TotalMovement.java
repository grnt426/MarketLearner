import java.util.HashMap;

/**
 * Author:      Grant Kurtz
 */
public class TotalMovement implements Hypothesis{

	private static HashMap<String, HashMap<String, ModelData>> data = null;
	private double weight;

	public TotalMovement(HashMap<String, HashMap<String, ModelData>> data){
		this.data = data;
	}

	public int prediction(Example ex){
		if(data.get(ex.date) == null)
			return 0;
		int totalStocks = data.get(ex.date).size();
		int totalUp = 0;
		for(ModelData md : data.get(ex.date).values()){
			totalUp += md.close > md.open ? 1 : -1;
		}
		return totalUp == 0 ? 0 : totalUp > 0 ? 1 : -1;
	}

	public double getWeight(){
		return weight;
	}

	public void setWeight(double weight){
		this.weight = weight;
	}

	public String toString(){
		return "TO";
	}
}
