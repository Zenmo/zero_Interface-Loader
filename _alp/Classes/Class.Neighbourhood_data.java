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
	Double total_electricity_consumption_companies_kWh_p_yr;
	Double total_gas_consumption_companies_m3_p_yr;
	Double avg_electricity_consumption_house_kWh_p_yr;
	Double avg_gas_consumption_house_m3_p_yr;
	Double avg_number_of_cars_per_house;
	Integer total_cars_companies;
	Integer total_vans_companies;
	Integer total_trucks_companies;
}