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
	String streetname;
	Integer house_number;
	String house_letter;
	String house_addition;
	String postalcode;
	String city;
	Integer build_year;	
	String status;
	String purpose;
	Double address_floor_surface_m2;	
	Double polygon_area_m2;
	String gc_id;
	String annotation;
	String extra_info;
	Double contracted_capacity_kw;
	Double electricity_consumption_kwhpa;
	Double gas_consumption_m3pa;
	Double pv_installed_kwp;
	Double pv_potential_kwp;
	OL_GridConnectionEnergyLabel energy_label;
	Boolean has_private_parking;
	String gridnode_id;
	Double latitude;
	Double longitude;
	String polygon;
	
	//Heating customizability
	OL_GridConnectionHeatingType heating_type;
	String heat_node_id;
	
	//PBL data
	boolean pbl_data_available;
	OL_PBL_DwellingType dwelling_type;		
	OL_PBL_OwnershipType ownership_type;
	OL_GridConnectionInsulationLabel insulation_label;
	Double local_factor;
	Double regional_climate_correction_factor;
}