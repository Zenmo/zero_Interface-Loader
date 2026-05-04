/**
 * DHWProfile_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class DHWProfile_data {
	
	String DHWProfileID;
	List<Double> arguments_hr;
	List<Double> valuesList;
	OL_ProfileUnits profileUnits;
	
	public double[] getArgumentsArray() {
		return ListUtil.doubleListToArray(arguments_hr);
	}
	public double[] getValuesArray() {
		return ListUtil.doubleListToArray(valuesList);
	}

}