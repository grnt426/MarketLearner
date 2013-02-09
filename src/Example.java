/**
 * Author:      Grant Kurtz
 */
public class Example{
	public String date;
	public double open;
	public double high;
	public double low;
	public double close;
	public int volume;
	public double adjustedClosed;

	public Example(String date, double open, double high, double low,
				   double close,
				   int volume, double adjustedClosed){
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.adjustedClosed = adjustedClosed;
	}
}
