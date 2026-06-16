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
	//boolean is_charging_centre;
	OL_VehicleType vehicle_type;
	boolean uses_charging_sessions;
	Integer number_of_sockets;
	Double power_per_socket_kw;
	boolean initially_active;
	double latitude;
	double longitude;
	String polygon;
}