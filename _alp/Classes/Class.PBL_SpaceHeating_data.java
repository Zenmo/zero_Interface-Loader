/**
 * PBL_SpaceHeating_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class PBL_SpaceHeating_data {
	OL_PBL_BuildingType building_type;	
	OL_PBL_ConstructionPeriod construction_period;	
	OL_PBL_OwnershipType ownership_type;	
	OL_GridConnectionIsolationLabel insulation_label;	
	//model_population;	
	double slope_space_heating_gjpm2a;	
	double constant_space_heating_gjpa;	
	double slope_residents;	
	double constant_residents;
}
