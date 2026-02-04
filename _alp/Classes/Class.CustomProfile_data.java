/**
 * CustomProfile_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class CustomProfile_data {
	
	String customProfileID;
	List<Double> argumentsList;
	List<Double> valuesList;
	OL_ProfileUnits profileUnits;
	
	public double[] getArgumentsArray() {
		return ListUtil.doubleListToArray(argumentsList);
	}
	public double[] getValuesArray() {
		return ListUtil.doubleListToArray(valuesList);
	}

}