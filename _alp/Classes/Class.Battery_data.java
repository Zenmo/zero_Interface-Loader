/**
 * Battery_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Battery_data {
	//Database column name
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
	boolean isSliderGC;
	double storage_capacity_kwh;
	double capacity_electric_kw;
	double connection_capacity_kw;
	Double contracted_delivery_capacity_kw;
	Double contracted_feed_in_capacity_kw;
	String default_operation_mode;
	double latitude;
	double longitude;
	String polygon;
}
