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
	Double gas_consumption_kwhpa;
	Double space_heating_consumption_kwhpa;
	Double dhw_consumption_kwhpa;
	Double cooking_consumption_kwhpa;
	Double pv_installed_kwp;
	Double pv_potential_kwp;
	OL_GridConnectionIsolationLabel energy_label;
	Boolean has_private_parking;
	String gridnode_id;
	Double latitude;
	Double longitude;
	String polygon;
	
	Integer ownership_int;
	Integer constructionPeriod_int;
	Integer buildingType_int;
	Double localFactor;
	Double regionalClimateCorrectionFactor;
}