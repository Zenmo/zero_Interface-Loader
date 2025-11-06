/**
 * Neighbourhood_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Neighbourhood_data {
	String districtcode;
	String districtname;
	String neighbourhoodcode;
	String neighbourhoodtype; // OL van maken
	double latitude;
	double longitude;
	String polygon;
	
	//Energy totals
	Double avg_house_elec_delivery_kwh_p_yr;
	Double avg_house_gas_delivery_m3_p_yr;
	Double avg_number_of_cars_per_house;
	Double total_comp_elec_delivery_kwh_p_yr;
	Double total_comp_gas_delivery_m3_p_yr;
	Integer total_nr_comp_cars;
	Integer total_nr_comp_vans;
	Integer total_nr_comp_trucks;
}