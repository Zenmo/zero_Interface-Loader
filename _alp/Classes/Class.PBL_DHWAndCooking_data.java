/**
 * PBL_DHWAndCooking_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class PBL_DHWAndCooking_data {
	Integer construction_period;	
	Integer surface_code;
	Integer household_size;
	Double cooking_gas_demand_m3pa;
	Double dhw_gas_demand_m3pa;
}
