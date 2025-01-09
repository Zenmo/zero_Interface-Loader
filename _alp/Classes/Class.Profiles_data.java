/**
 * Profiles_data
 */	
@lombok.Builder
@lombok.Value
@lombok.experimental.Accessors(fluent = true)
public class Profiles_data {
		
		List<Double> argumentsList;
		
		// Various demand profiles
		List<Double> houseEdemandList;
		List<Double> houseHDHWdemandList;
		List<Double> buildingEdemandList;
		List<Double> buildingHeatDemandList;
		List<Double> industrySteelEdemandList;
		List<Double> industrySteelHdemandList;
		List<Double> industryOtherEdemandList;
		List<Double> industryOtherHdemandList;
		List<Double> logisticsFleetEdemandList;
		
		// Weather data
		List<Double> windList;
		List<Double> solarList;
		List<Double> tempList;
		List<Double> epexList;
		
		// Maximums of demand profile
		Double houseEdemandList_maximum;
		Double houseHDHWdemandList_maximum;
		Double buildingEdemandList_maximum;
		Double buildingHeatDemandList_maximum;
		Double industrySteelEdemandList_maximum;
		Double industrySteelHdemandList_maximum;
		Double industryOtherEdemandList_maximum;
		Double industryOtherHdemandList_maximum;
		Double logisticsFleetEdemandList_maximum;
}