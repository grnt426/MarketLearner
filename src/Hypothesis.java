/**
 * Author:      Grant Kurtz
 */
public interface Hypothesis{
	public int prediction(Example ex);
	public double getWeight();
	public void setWeight(double weight);
}
