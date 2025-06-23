/**
 * ParkingSpace_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class ParkingSpace_data {
	//Database column name
	String parking_id;	
	String street;
	OL_ParkingSpaceType type;	
	String additional_info;
	
	Double pv_potential_kwp;
	
	String gridnode_id;	

	Double latitude;
	Double longitude;
	String polygon;
}