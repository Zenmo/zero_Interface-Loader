/**
 * Cable_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Cable_data {
		String fid;
		OL_GISObjectType type; // LV_CABLE or MV_CABLE, maybe more in future
		boolean status;
		Double nominal_voltage_v;
		String label;
		String description;
		double latitude;
		double longitude;
		String line;
}
