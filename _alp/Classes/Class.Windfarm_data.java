/**
 * Windfarm_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Windfarm_data {
		
		String gc_id;
		String gc_name;
		String owner_id;
		String streetname;
		Integer house_number;
		String house_letter;
		String house_addition;
		String postalcode;
		String city;
		String gridnode_id;
		boolean initially_active;
		double capacity_electric_kw;
		double connection_capacity_kw;
		Double contracted_delivery_capacity_kw;
		Double contracted_feed_in_capacity_kw;
		double latitude;
		double longitude;
		String polygon;
}