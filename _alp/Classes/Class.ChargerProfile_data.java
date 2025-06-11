/**
 * ChargerProfile_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class ChargerProfile_data {
	
	String chargerProfileID;
	List<String> valuesList;

}