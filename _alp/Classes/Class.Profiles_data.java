/**
 * Profiles_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Profiles_data {
		
	//Arguments
	List<Double> argumentsList;

	// Weather data
	List<Double> tempList;
	List<Double> solarList35DegSouth;
	List<Double> solarList15DegEastWest;
	List<Double> windList;
	
	//EPEX data
	List<Double> epexList;
	
	// Various demand profiles
	List<Double> houseEdemandList;
	List<Double> houseHDHWdemandList;
	List<Double> buildingEdemandList;
	List<Double> buildingHeatDemandList;
	
	//Maximum getters
	public double getBuildingEdemandList_maximum() {
		return Collections.max(buildingEdemandList);
	}
	public double getBuildingHeatDemandList_maximum() {
		return Collections.max(buildingHeatDemandList);
	}
}