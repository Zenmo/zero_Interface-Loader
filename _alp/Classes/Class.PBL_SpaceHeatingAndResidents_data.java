/**
 * PBL_SpaceHeatingAndResidents_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class PBL_SpaceHeatingAndResidents_data {	
	OL_PBL_DwellingType dwelling_type;	
	Integer construction_period;	
	OL_PBL_OwnershipType ownership_type;	
	OL_GridConnectionInsulationLabel insulation_label;	
	Integer regression_population; // 1, 2 or 3	
	Double slope_space_heating_gas_m3pm2a;	
	Double constant_space_heating_gas_m3pa;	
	Double slope_residents;	
	Double constant_residents;
}