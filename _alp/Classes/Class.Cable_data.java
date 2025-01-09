/**
 * Cable_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Cable_data {
		String fid;
		boolean status;
		Double nominal_voltage_v;
		String label;
		String description;
		double latitude;
		double longitude;
		String line;
}
