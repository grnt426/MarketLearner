/**
 * Author:      Grant Kurtz
 */
public class ModelData{

	public String symbol;
	public double open;
	public double high;
	public double low;
	public double close;
	public long volume;

	public ModelData(String symbol, double open, double high, double low,
					 double close, long volume){
		this.symbol = symbol;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
	}
}
