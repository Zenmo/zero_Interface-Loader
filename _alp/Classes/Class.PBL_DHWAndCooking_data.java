/**
 * PBL_DHWAndCooking_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class PBL_DHWAndCooking_data {
	OL_PBL_ConstructionPeriod construction_period;	
	int surface_code;
	int household_size;
	double cooking_gas_demand_m3pa;
	double dhw_gas_demand_m3pa;
}
