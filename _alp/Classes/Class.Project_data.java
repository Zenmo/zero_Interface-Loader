/**
 * Project_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Project_data {
		
		//Project settings
		String project_name;
		OL_ProjectType project_type;
		OL_SurveyType survey_type;
		
		//Database names
		List<String[]> databaseNames;
		
		//Map centre coordinates
		double map_centre_latitude;
		double map_centre_longitude;
		Double map_scale;
		
		//Project specific actors
		String grid_operator;
		Boolean hasCongestionPricing;
		String energy_coop;
		String energy_supplier;
		
		//Project totals
		Double total_electricity_consumption_companies_kWh_p_yr;
		Double total_gas_consumption_companies_m3_p_yr;
		Double avg_electricity_consumption_house_kWh_p_yr;
		Double avg_gas_consumption_house_m3_p_yr;
		Integer total_cars_houses;
		Integer total_cars_companies;
		Integer total_vans_companies;
		Integer total_trucks_companies;
}