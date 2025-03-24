/**
 * Building_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Building_data {
	//Database column name
	String address_id;
	String building_id;	
	Integer pand_nr;
	Integer pandcluster_nr;
	String streetname;
	Integer house_number;
	String house_letter;
	String house_addition;
	String postalcode;
	String city;
	Integer build_year;	
	String status;
	String purpose;
	Double cumulative_floor_surface_m2;	
	Double polygon_area_m2;
	String gc_id;
	String annotation;
	String extra_info;
	Double contracted_capacity_kw;
	Double electricity_consumption_kwhpa;
	Double gas_consumption_kwhpa;
	String energy_label;
	String gridnode_id;
	Double latitude;
	Double longitude;
	String polygon;
}