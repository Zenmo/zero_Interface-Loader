/**
 * DefaultProfiles_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class DefaultProfiles_data {
		
	//Arguments
	List<Double> arguments_hr;

	// Weather data
	List<Double> ambientTemperatureProfile_degC;
	List<Double> PVProductionProfile35DegSouth_fr;
	List<Double> PVProductionProfile15DegEastWest_fr;
	List<Double> windProductionProfile_fr;
	
	//EPEX data
	List<Double> epexProfile_eurpMWh;
	
	// Various demand profiles
	List<Double> defaultHouseElectricityDemandProfile_fr;
	List<Double> defaultHouseHotWaterDemandProfile_fr;
	List<Double> defaultHouseCookingDemandProfile_fr;
	List<Double> defaultOfficeElectricityDemandProfile_fr;
	List<Double> defaultBuildingHeatDemandProfile_fr;
	List<Double> defaultHouseCookingDemandProfile_fr;
	
	//Maximum getters
	public double getDefaultOfficeElectricityDemandProfileMaximum_fr() {
		return Collections.max(defaultOfficeElectricityDemandProfile_fr);
	}
	public double getDefaultBuildingHeatDemandProfileMaximum_fr() {
		return Collections.max(defaultBuildingHeatDemandProfile_fr);
	}
}