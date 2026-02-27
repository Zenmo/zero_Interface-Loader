/**
 * Chargingstation_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Chargingstation_data {
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
	Double connection_capacity_kw;	
	boolean is_charging_centre;
	OL_EnergyAssetType vehicle_type;
	Integer number_of_chargers;
	Double power_per_charger_kw;
	boolean initially_active;
	double latitude;
	double longitude;
	String polygon;
}