/**
 * CustomProfiles_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class CustomProfile_data {
	
	String customProfileID;
	List<Double> argumentsList;
	List<Double> valuesList;
	
	double[] getArgumentsArray() {
		return ListUtil.doubleListToArray(argumentsList);
	}
	double[] getValuesArray() {
		return ListUtil.doubleListToArray(valuesList);
	}

}