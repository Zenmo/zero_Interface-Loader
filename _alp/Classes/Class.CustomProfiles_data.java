/**
 * CustomProfiles_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class CustomProfiles_data {
		
		List<Double> argumentsList;
		
		// Various demand profiles
		List<Double> customProfileList;

		double[] getValuesArray() {
			return ListUtil.doubleListToArray(customProfileList);
		}
}
