double f_setPVOnLand(double hectare,List<GCEnergyProduction> gcListProduction)
{/*ALCODESTART::1722256117103*/
// TODO: Change to work for multiple solar fields in one model.
// to do so it should probably first calculate the total installed pv in all solar fields
for ( GCEnergyProduction GCEP : gcListProduction) {
	for(J_EAProduction j_ea : GCEP.c_productionAssets) {
		if (j_ea.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC) {
			if (!GCEP.v_isActive) {
				GCEP.f_setActive(true, zero_Interface.energyModel.p_timeVariables);
			}
			
			double solarFieldPower = (double)roundToInt(hectare * zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha);
			j_ea.setCapacityElectric_kW(solarFieldPower, GCEP);
			GCEP.v_liveConnectionMetaData.physicalCapacity_kW = solarFieldPower;
			GCEP.v_liveConnectionMetaData.contractedFeedinCapacity_kW = solarFieldPower;
			
			if(hectare == 0){
				GCEP.f_setActive(false, zero_Interface.energyModel.p_timeVariables);
			}
			
			break;
		}
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setPVSystemHouses(List<GCHouse> gcList,double PV_pct)
{/*ALCODESTART::1722256142375*/
ArrayList<GCHouse> houses = new ArrayList<GCHouse>(zero_Interface.c_orderedPVSystemsHouses.stream().filter(gcList::contains).toList());
int nbHouses = houses.size();
int nbHousesWithPV = count(houses, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW));
int nbHousesWithPVGoal = roundToInt(PV_pct / 100.0 * nbHouses);

while ( nbHousesWithPVGoal < nbHousesWithPV ) { // remove excess PV systems
	GCHouse house = findFirst(houses, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW));	
	J_EA pvAsset = findFirst(house.c_productionAssets, p -> p.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC );
	if (pvAsset != null) {
		pvAsset.removeEnergyAsset();
		houses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.add(0, house);
		nbHousesWithPV --; 
		
		if(house.p_batteryAsset != null ){
			house.p_batteryAsset.removeEnergyAsset();
			house.f_setBatteryManagement(null);
		}
	}
	else {
		traceln(" cant find PV asset in house that should have PV asset in f_setPVHouses (Interface)");
	}
}

while ( nbHousesWithPVGoal > nbHousesWithPV ) {
	GCHouse house = findFirst(houses, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW) == false);
	if (house == null){
		traceln("No gridconnection without PV panels found! Current PVsystems count: %s", nbHousesWithPV);
		break;
	}
	else {
		String assetName = "Rooftop PV";
		double capacityHeat_kW = 0.0;
		double yearlyProductionHydrogen_kWh = 0.0;
		double yearlyProductionMethane_kWh = 0.0;
		double installedPVCapacity_kW = house.v_liveAssetsMetaData.PVPotential_kW;//roundToDecimal(uniform(3,6),2);
		
		//Compensate for pt if it is present
		if(house.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.ptProductionHeat_kW)){
			installedPVCapacity_kW = max(0, installedPVCapacity_kW-zero_Interface.energyModel.avgc_data.p_avgPTPanelSize_m2*zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2); //For now just 1 panel
		}
		J_EAProduction productionAsset = new J_EAProduction ( house, OL_EnergyAssetType.PHOTOVOLTAIC, assetName, OL_EnergyCarriers.ELECTRICITY, installedPVCapacity_kW, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.pp_PVProduction35DegSouth_fr );
		houses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.add(0, house);
		nbHousesWithPV ++;	
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setWindTurbines(double AllocatedWindPower_MW,List<GCEnergyProduction> gcListProduction)
{/*ALCODESTART::1722256248965*/
// TODO: Change to work for multiple wind farms in one model.
// to do so it should probably first calculate the total installed wind power in all wind farms

for ( GCEnergyProduction GCEP : gcListProduction) {
	for(J_EAProduction j_ea : GCEP.c_productionAssets) {
		if (j_ea.getEAType() == OL_EnergyAssetType.WINDMILL) {
			if (!GCEP.v_isActive) {
				GCEP.f_setActive(true, zero_Interface.energyModel.p_timeVariables);
			}
			j_ea.setCapacityElectric_kW(roundToInt(1000*AllocatedWindPower_MW), GCEP);
			GCEP.v_liveConnectionMetaData.physicalCapacity_kW = (double)roundToInt(1000*AllocatedWindPower_MW);
			GCEP.v_liveConnectionMetaData.contractedFeedinCapacity_kW = (double)roundToInt(1000*AllocatedWindPower_MW);
			
			if(AllocatedWindPower_MW == 0){
				GCEP.f_setActive(false, zero_Interface.energyModel.p_timeVariables);
			}
			break;
		}
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setDemandReduction(List<GridConnection> gcList,double demandReduction_pct)
{/*ALCODESTART::1722335253834*/
double scalingFactor = 1 - demandReduction_pct/100;

for (GridConnection gc : gcList) {
	// Set Consumption Assets
	for (J_EAConsumption j_ea : gc.c_consumptionAssets) {
		if (j_ea.getEAType() == OL_EnergyAssetType.ELECTRICITY_DEMAND) {
			j_ea.setConsumptionScaling_fr( scalingFactor );
		}
	}
	// Set Profile Assets
	for (J_EAProfile j_ea : gc.c_profileAssets) {
		if (j_ea.getEnergyCarrier() == OL_EnergyCarriers.ELECTRICITY) {
			j_ea.setProfileScaling_fr( scalingFactor );
		}
	}
}

// Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_getCurrentPVOnLandAndWindturbineValues()
{/*ALCODESTART::1745483988251*/
p_currentPVOnLand_ha = 0;
p_currentWindTurbines_MW = 0;
for(GCEnergyProduction GCProd : uI_Tabs.f_getAllSliderGridConnections_production()){
	if(!c_electricityTabEASliderGCs.contains(GCProd) && GCProd.v_isActive){
		for(J_EAProduction ea : GCProd.c_productionAssets){
			if(ea.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC){
				p_currentPVOnLand_ha += ea.getCapacityElectric_kW()/zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha;
			}
			else if(ea.getEAType() == OL_EnergyAssetType.WINDMILL){
				p_currentWindTurbines_MW += ea.getCapacityElectric_kW()/1000;
			}
		}
	}
}
/*ALCODEEND*/}

Pair<Double, Double> f_getPVSystemPercentage(List<GridConnection> gcList)
{/*ALCODESTART::1747294812333*/
double installedPV_kWp = 0.0;
double PVPotential_kWp = 0.0;
double averageEffectivePV_kWppm2 = zero_Interface.energyModel.avgc_data.p_avgRatioRoofPotentialPV * zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2;

for (GridConnection gc : gcList ) {
	double gcInstalledPV_kWp = 0.0;
	if ( gc.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW) ) {
		for ( J_EAProduction j_ea : gc.c_productionAssets ) {
			if ( j_ea.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC ) {
				gcInstalledPV_kWp += j_ea.getCapacityElectric_kW();
			}
		}
	}
	installedPV_kWp += gcInstalledPV_kWp;
	PVPotential_kWp += max( gcInstalledPV_kWp, max(0.1, gc.p_roofSurfaceArea_m2 * averageEffectivePV_kWppm2) );
}

return new Pair(installedPV_kWp, PVPotential_kWp);
/*ALCODEEND*/}

double f_setPVSystemCompanies(List<GCUtility> gcList,double target_pct,ShapeSlider slider)
{/*ALCODESTART::1747297871195*/
List<GCUtility> activeGCs = new ArrayList<GCUtility>(zero_Interface.c_orderedPVSystemsCompanies.stream().filter(x -> x.v_isActive).filter(gcList::contains).toList());
Pair<Double, Double> pair = f_getPVSystemPercentage( new ArrayList<GridConnection>(activeGCs) );
double remaining_kWp = target_pct / 100 * pair.getSecond() - pair.getFirst();
double averageEffectivePV_kWppm2 = zero_Interface.energyModel.avgc_data.p_avgRatioRoofPotentialPV * zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2;
if ( remaining_kWp > 0 ) {
	// add more PV
	for ( GCUtility company : new ArrayList<GCUtility>(activeGCs) ) {
		double remainingPotential_kWp = min( remaining_kWp, company.p_roofSurfaceArea_m2 * averageEffectivePV_kWppm2 - company.v_liveAssetsMetaData.totalInstalledPVPower_kW );
		
		if ( remainingPotential_kWp > 0 ) {
			remaining_kWp -= remainingPotential_kWp;
			f_addPVSystem( company, remainingPotential_kWp );
		}
		
		if ( remaining_kWp <= 0 ) {
			// Update variable to change to custom scenario
			if(!zero_Interface.b_runningMainInterfaceScenarios){
				zero_Interface.f_setScenarioToCustom();
			}
			zero_Interface.f_resetSettings();		
			return;
		}
	}
}
else {
	// remove pv
	for ( GCUtility company : new ArrayList<GCUtility>(activeGCs) ) {
		if ( company.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW) ) {
			// find companyUI to check if the company already has PV on model startup			
			remaining_kWp += company.v_liveAssetsMetaData.totalInstalledPVPower_kW;
			f_removePVSystem( company );
			double PVAtStartup_kWp = zero_Interface.c_scenarioMap_Current.get(company.p_uid).getCurrentPV_kW();
			if (PVAtStartup_kWp != 0) {
				f_addPVSystem( company, PVAtStartup_kWp );
				remaining_kWp -= PVAtStartup_kWp;
			}
		}
		if ( remaining_kWp >= 0 ) {
			// removed slightly too much pv
			f_addPVSystem( company, remaining_kWp );
			
			// Update variable to change to custom scenario
			if(!zero_Interface.b_runningMainInterfaceScenarios){
				zero_Interface.f_setScenarioToCustom();
			}
			
			zero_Interface.f_resetSettings();			
			return;
		}
	}
	// All companies are at the starting PV amount. Set slider to corresponding value.
	pair = f_getPVSystemPercentage( new ArrayList<GridConnection>( activeGCs ) );
	int installed_pct = roundToInt(100.0 * pair.getFirst() / pair.getSecond());	
	slider.setValue(installed_pct, false);
}

// Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_addPVSystem(GridConnection gc,double capacity_kWp)
{/*ALCODESTART::1747306690517*/
J_EAProduction pvAsset = findFirst(gc.c_productionAssets, p -> p.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC);
if (pvAsset != null) {
	capacity_kWp += pvAsset.getCapacityElectric_kW();
	pvAsset.setCapacityElectric_kW( capacity_kWp, gc );
}
else {
	// Create a new asset
	OL_EnergyAssetType assetType = OL_EnergyAssetType.PHOTOVOLTAIC;
	String assetName = "Rooftop PV";
	double capacityHeat_kW = 0.0;
	double yearlyProductionMethane_kWh = 0.0;
	double yearlyProductionHydrogen_kWh = 0.0;
	double outputTemperature_degC = 0.0;
	
	J_EAProduction productionAsset = new J_EAProduction ( gc, assetType, assetName, OL_EnergyCarriers.ELECTRICITY, capacity_kWp, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.pp_PVProduction35DegSouth_fr );
}

// Update the ordered collection
if ( gc instanceof GCHouse ) {
	zero_Interface.c_orderedPVSystemsHouses.remove(gc);
	zero_Interface.c_orderedPVSystemsHouses.add(0, (GCHouse)gc);	
}
else if ( gc instanceof GCUtility ) {
	zero_Interface.c_orderedPVSystemsCompanies.remove(gc);
	zero_Interface.c_orderedPVSystemsCompanies.add(0, (GCUtility)gc);
}
else {
	throw new RuntimeException("Unknown GridConnection type passed to f_addPVSystem.");
}
/*ALCODEEND*/}

double f_removePVSystem(GridConnection gc)
{/*ALCODESTART::1747306699629*/
J_EAProduction pvAsset = findFirst(gc.c_productionAssets, p -> p.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC);
if ( pvAsset != null ) {
	pvAsset.removeEnergyAsset();

	if ( gc instanceof GCHouse ) {
		zero_Interface.c_orderedPVSystemsHouses.remove(gc);
		zero_Interface.c_orderedPVSystemsHouses.add(0, (GCHouse)gc);	
	}
	else if ( gc instanceof GCUtility ) {
		zero_Interface.c_orderedPVSystemsCompanies.remove(gc);
		zero_Interface.c_orderedPVSystemsCompanies.add(0, (GCUtility)gc);
	}
}
/*ALCODEEND*/}

double f_setResidentialBatteries(double homeBatteries_pct,List<GCHouse> gcListHouses)
{/*ALCODESTART::1750063382310*/
// Setting houseBatteries
double nbHouseBatteries = count(gcListHouses, h -> h.p_batteryAsset != null); //f_getEnergyAssets(), p -> p.energyAssetType == OL_EnergyAssetType.STORAGE_ELECTRIC && p.getParentAgent() instanceof GCHouse);
double nbHousesWithPV = count(gcListHouses, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW)); //count(energyModel.f_getGridConnections(), p->p instanceof GCHouse);
double nbHousesWithBatteryGoal = roundToInt(nbHousesWithPV * homeBatteries_pct / 100);

if( nbHousesWithPV > 0 ){
	while ( nbHouseBatteries > nbHousesWithBatteryGoal ) {
		GCHouse house = findFirst(gcListHouses, p -> p.p_batteryAsset != null );
		house.p_batteryAsset.removeEnergyAsset();
		house.f_setBatteryManagement(null);
		nbHouseBatteries--;
	}
	while ( nbHouseBatteries < nbHousesWithBatteryGoal) {
		GCHouse house = findFirst(gcListHouses, p -> p.p_batteryAsset == null && p.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW));
		
		double batteryStorageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgRatioHouseBatteryStorageCapacity_v_PVPower*house.v_liveAssetsMetaData.totalInstalledPVPower_kW;
		double batteryCapacity_kW = batteryStorageCapacity_kWh / zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power;
		double batteryStateOfCharge = 0.5;

		new J_EAStorageElectric(house, batteryCapacity_kW, batteryStorageCapacity_kWh, batteryStateOfCharge, zero_Interface.energyModel.p_timeParameters );
		house.f_setBatteryManagement(new J_BatteryManagementSelfConsumption( house, zero_Interface.energyModel.p_timeParameters ));
		nbHouseBatteries++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setGridBatteries(double storageCapacity_kWh,List<GCGridBattery> gcListGridBatteries)
{/*ALCODESTART::1750063382312*/
for ( GCGridBattery battery : gcListGridBatteries) {
	if(battery.p_batteryAsset == null){
		throw new RuntimeException("GCGridBattery found without p_batteryAsset");
	}
	
	J_EAStorageElectric batteryAsset = battery.p_batteryAsset;
	if (!battery.v_isActive) {
		battery.f_setActive(true, zero_Interface.energyModel.p_timeVariables);
	}
	
	
	double capacity_kW = storageCapacity_kWh / zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power;
	if(batteryAsset.getCapacityElectric_kW() > 0 && batteryAsset.getStorageCapacity_kWh() > 0){ //If already existing power present: keep relation between power and storage capacity the same.
		capacity_kW = storageCapacity_kWh * ( batteryAsset.getStorageCapacity_kWh() / batteryAsset.getCapacityElectric_kW());
	}
	batteryAsset.setCapacityElectric_kW( capacity_kW );
	batteryAsset.setStorageCapacity_kWh( storageCapacity_kWh, battery );
	battery.v_liveConnectionMetaData.physicalCapacity_kW = capacity_kW;
	battery.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = capacity_kW;
	battery.v_liveConnectionMetaData.contractedFeedinCapacity_kW = capacity_kW;
	
	if(storageCapacity_kWh == 0){
		battery.f_setActive(false, zero_Interface.energyModel.p_timeVariables);
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setElectricCooking(List<GCHouse> gcListHouses,double electricCookingGoal_pct)
{/*ALCODESTART::1750063382324*/
int nbHousesWithElectricCooking = findAll(gcListHouses, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC).size();
int nbHousesWithElectricCookingGoal = roundToInt(electricCookingGoal_pct / 100 * gcListHouses.size());


while ( nbHousesWithElectricCooking > nbHousesWithElectricCookingGoal ) { // remove excess cooking systems
	GCHouse house = randomWhere(gcListHouses, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC);		
	J_EAConsumption cookingAsset = findFirst(house.c_consumptionAssets, p -> p.getEAType() == OL_EnergyAssetType.ELECTRIC_HOB );
	if (cookingAsset != null) {
		double yearlyCookingDemand_kWh = cookingAsset.getBaseConsumption_kWh();
		cookingAsset.removeEnergyAsset();
   		
   		J_ProfilePointer pp = zero_Interface.energyModel.f_findProfile("default_house_cooking_demand_fr");
		new J_EAConsumption(house, OL_EnergyAssetType.GAS_PIT, "default_house_cooking_demand_fr", yearlyCookingDemand_kWh, OL_EnergyCarriers.METHANE, zero_Interface.energyModel.p_timeParameters, pp);
		house.p_cookingMethod = OL_HouseholdCookingMethod.GAS;
		nbHousesWithElectricCooking --; 
	}
	else {
		throw new RuntimeException("Cant find cooking asset in house that should have cooking asset in f_ElectricCooking (tabElectricity)");
	}
}
 
while ( nbHousesWithElectricCooking < nbHousesWithElectricCookingGoal) {
	GCHouse house = randomWhere(gcListHouses, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.GAS);
	if (house == null){
		throw new RuntimeException("No gridconnection without GAS cooking asset found! Current electric cooking count: " + nbHousesWithElectricCooking);
	}
	else {
		J_EAConsumption cookingAsset = findFirst(house.c_consumptionAssets, p -> p.getEAType() == OL_EnergyAssetType.GAS_PIT );
		if (cookingAsset != null) {
			double yearlyCookingDemand_kWh = cookingAsset.getBaseConsumption_kWh();
			cookingAsset.removeEnergyAsset();
			
			J_ProfilePointer pp = zero_Interface.energyModel.f_findProfile("default_house_cooking_demand_fr");
			new J_EAConsumption(house, OL_EnergyAssetType.ELECTRIC_HOB, "default_house_cooking_demand_fr", yearlyCookingDemand_kWh, OL_EnergyCarriers.ELECTRICITY, zero_Interface.energyModel.p_timeParameters, pp);
			house.p_cookingMethod = OL_HouseholdCookingMethod.ELECTRIC;
			nbHousesWithElectricCooking ++; 
		}
		else {
			throw new RuntimeException("Cant find cooking asset in house that should have cooking asset in f_ElectricCooking (tabElectricity)");
		}
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setDemandIncrease(List<GridConnection> gcList,double demandReduction_pct)
{/*ALCODESTART::1750326729005*/
f_setDemandReduction(gcList, -demandReduction_pct);
/*ALCODEEND*/}

double f_updateSliders_Electricity()
{/*ALCODESTART::1754926103683*/
if(gr_electricitySliders_default.isVisible()){
	f_updateElectricitySliders_default();
}
else if(gr_electricitySliders_businesspark.isVisible()){
	f_updateElectricitySliders_businesspark();
}
else if(gr_electricitySliders_residential.isVisible()){
	f_updateElectricitySliders_residential();
}
else{
	f_updateElectricitySliders_custom();
}
/*ALCODEEND*/}

double f_updateElectricitySliders_default()
{/*ALCODESTART::1754926103685*/
List<GridConnection> allConsumerGridConnections = uI_Tabs.f_getActiveSliderGridConnections_consumption();


//Savings
double totalBaseConsumption_kWh = 0;
double totalSavedConsumption_kWh = 0;
for(GridConnection GC : allConsumerGridConnections){
	if(GC.v_isActive){
		List<J_EAProfile> profileEAs = findAll(GC.c_profileAssets, profile -> profile.getAssetFlowCategory() == OL_AssetFlowCategories.fixedConsumptionElectric_kW);
		List<J_EAConsumption> consumptionEAs = findAll(GC.c_consumptionAssets, consumption -> consumption.getAssetFlowCategory() == OL_AssetFlowCategories.fixedConsumptionElectric_kW);
		for(J_EAProfile profileEA : profileEAs){
			double baseConsumption_kWh = profileEA.getBaseConsumption_kWh(); //ZeroMath.arraySum(profileEA.a_energyProfile_kWh);
			totalBaseConsumption_kWh += baseConsumption_kWh;
			totalSavedConsumption_kWh += (1 - profileEA.getProfileScaling_fr()) * baseConsumption_kWh;
		}
		for(J_EAConsumption consumptionEA : consumptionEAs){
			totalBaseConsumption_kWh += consumptionEA.getBaseConsumption_kWh();
			totalSavedConsumption_kWh += (1-consumptionEA.getConsumptionScaling_fr())*consumptionEA.getBaseConsumption_kWh();
		}
	}
}

double electricitySavings_pct = totalBaseConsumption_kWh > 0 ? (totalSavedConsumption_kWh/totalBaseConsumption_kWh * 100) : 0;
sl_electricityDemandReduction_pct.setValue(roundToInt(electricitySavings_pct), false);


//Companies rooftop PV
List<GCUtility> utilityGridConnections = uI_Tabs.f_getActiveSliderGridConnections_utilities();

List<GridConnection> utilityGridConnections_GC = new ArrayList<>(utilityGridConnections);
Pair<Double, Double> pair = f_getPVSystemPercentage( utilityGridConnections_GC );
int PV_pct = roundToInt(100.0 * pair.getFirst() / pair.getSecond());
sl_rooftopPVCompanies_pct.setValue(PV_pct, false);

//Houses rooftop PV
List<GCHouse> houseGridConnections = uI_Tabs.f_getActiveSliderGridConnections_houses();

List<GridConnection> houseGridConnections_GC = new ArrayList<>(utilityGridConnections);
pair = f_getPVSystemPercentage( houseGridConnections_GC );
PV_pct = roundToInt(100.0 * pair.getFirst() / pair.getSecond());
sl_rooftopPVHouses_pct.setValue(PV_pct, false);

//Large scale EA production systems (PV on land And Wind)
f_getCurrentPVOnLandAndWindturbineValues(); // Used for slider minimum: non adjustable GCProductions

double totalPVOnLand_kW = 0; // Of movable slider GC
double totalWind_kW = 0; // Of movable slider GC

for(GridConnection productionGC : c_electricityTabEASliderGCs){
	if(productionGC instanceof GCEnergyProduction && productionGC.v_isActive){
		for(J_EAProduction productionEA : productionGC.c_productionAssets){
			if(productionEA.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC){
				totalPVOnLand_kW += productionEA.getCapacityElectric_kW();
				break;
			}
			else if(productionEA.getEAType() == OL_EnergyAssetType.WINDMILL){
				totalWind_kW += productionEA.getCapacityElectric_kW();
				break;
			}
		}
	}
}
sl_largeScalePV_ha_Businesspark.setRange(0, 1000); // Needed to prevent anylogic range bug
sl_largeScalePV_ha.setValue((totalPVOnLand_kW/zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha) + p_currentPVOnLand_ha, false);
sl_largeScaleWind_MW.setRange(0, 1000); // Needed to prevent anylogic range bug
sl_largeScaleWind_MW.setValue((totalWind_kW/1000) + p_currentWindTurbines_MW, false);

//Curtailment
boolean curtailment = true;
for(GridConnection GC : allConsumerGridConnections){
	if(!GC.v_enableCurtailment){
		curtailment = false;
		break;
	}
}
cb_curtailment_default.setSelected(curtailment, false);


//Large scale battery systems
f_getCurrentGridBatterySize(); // Used for slider minimum: non adjustable GCGridBatteries

double totalBatteryStorage_kWh = 0;
for(GridConnection batteryGC : c_electricityTabEASliderGCs){
	if(batteryGC instanceof GCGridBattery && batteryGC.v_isActive){
		totalBatteryStorage_kWh += batteryGC.p_batteryAsset.getStorageCapacity_kWh();
	}
}
sl_collectiveBattery_MWh_default.setRange(0, 1000);
sl_collectiveBattery_MWh_default.setValue((totalBatteryStorage_kWh/1000.0) + p_currentTotalGridBatteryCapacity_MWh, false);

/*ALCODEEND*/}

double f_updateElectricitySliders_residential()
{/*ALCODESTART::1754926103687*/
List<GCHouse> houseGridConnections = uI_Tabs.f_getActiveSliderGridConnections_houses();

int nbHouses = houseGridConnections.size();
int nbHousesWithPV = count(houseGridConnections, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW));
double pv_pct = 100.0 * nbHousesWithPV / nbHouses;
sl_householdPVResidentialArea_pct.setValue(roundToInt(pv_pct), false);

if ( nbHousesWithPV != 0 ) {
	int nbHousesWithHomeBattery = count(houseGridConnections, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW) && x.p_batteryAsset != null);
	double battery_pct = 100.0 * nbHousesWithHomeBattery / nbHousesWithPV;
	sl_householdBatteriesResidentialArea_pct.setValue(roundToInt(battery_pct), false);
}

//Electric cooking
int nbHousesWithElectricCooking = count(houseGridConnections, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC);
double cooking_pct = 100.0 * nbHousesWithElectricCooking / nbHouses;
sl_householdElectricCookingResidentialArea_pct.setValue(roundToInt(cooking_pct), false);

//Consumption growth
double totalBaseConsumption_kWh = 0;
double totalSavedConsumption_kWh = 0;
for(GCHouse GC : houseGridConnections){
	if(GC.v_isActive){
		List<J_EAProfile> profileEAs = findAll(GC.c_profileAssets, profile -> profile.getAssetFlowCategory() == OL_AssetFlowCategories.fixedConsumptionElectric_kW);
		List<J_EAConsumption> consumptionEAs = findAll(GC.c_consumptionAssets, consumption -> consumption.getAssetFlowCategory() == OL_AssetFlowCategories.fixedConsumptionElectric_kW);
		for(J_EAProfile profileEA : profileEAs){
			double baseConsumption_kWh = profileEA.getBaseConsumption_kWh(); //ZeroMath.arraySum(profileEA.a_energyProfile_kWh);
			totalBaseConsumption_kWh += baseConsumption_kWh;
			totalSavedConsumption_kWh += (1 - profileEA.getProfileScaling_fr()) * baseConsumption_kWh;
		}
		for(J_EAConsumption consumptionEA : consumptionEAs){
			totalBaseConsumption_kWh += consumptionEA.getBaseConsumption_kWh();
			totalSavedConsumption_kWh += (1-consumptionEA.getConsumptionScaling_fr())*consumptionEA.getBaseConsumption_kWh();
		}
	}
}

double electricityDemandIncrease_pct = totalBaseConsumption_kWh > 0 ? ( (- totalSavedConsumption_kWh)/totalBaseConsumption_kWh * 100) : 0;
sl_electricityDemandIncreaseResidentialArea_pct.setValue(roundToInt(electricityDemandIncrease_pct), false);


//Gridbatteries
List<GCGridBattery> sliderGridBatteryGridConnections = new ArrayList<>();
for(GridConnection sliderGC : c_electricityTabEASliderGCs){
	if(sliderGC.v_isActive && sliderGC instanceof GCGridBattery sliderGridBattery){
		sliderGridBatteryGridConnections.add(sliderGridBattery);
	}
}

double averageNeighbourhoodBatterySize_kWh = 0;
for (GCGridBattery gc : sliderGridBatteryGridConnections) {
	averageNeighbourhoodBatterySize_kWh += gc.p_batteryAsset.getStorageCapacity_kWh()/sliderGridBatteryGridConnections.size();
}
sl_gridBatteriesResidentialArea_kWh.setValue(averageNeighbourhoodBatterySize_kWh, false);

/*ALCODEEND*/}

double f_updateElectricitySliders_businesspark()
{/*ALCODESTART::1754926103689*/
//Get the utility connections
List<GridConnection> utilityGridConnections = new ArrayList<>(uI_Tabs.f_getActiveSliderGridConnections_utilities());


//Savings
double totalBaseConsumption_kWh = 0;
double totalSavedConsumption_kWh = 0;
for(GridConnection GC : utilityGridConnections){
	if(GC.v_isActive){
		List<J_EAProfile> profileEAs = findAll(GC.c_profileAssets, profile -> profile.getAssetFlowCategory() == OL_AssetFlowCategories.fixedConsumptionElectric_kW);
		List<J_EAConsumption> consumptionEAs = findAll(GC.c_consumptionAssets, consumption -> consumption.getAssetFlowCategory() == OL_AssetFlowCategories.fixedConsumptionElectric_kW);
		for(J_EAProfile profileEA : profileEAs){
			double baseConsumption_kWh = profileEA.getBaseConsumption_kWh();//ZeroMath.arraySum(profileEA.a_energyProfile_kWh);
			totalBaseConsumption_kWh += baseConsumption_kWh;
			totalSavedConsumption_kWh += (1 - profileEA.getProfileScaling_fr()) * baseConsumption_kWh;
		}
		for(J_EAConsumption consumptionEA : consumptionEAs){
			totalBaseConsumption_kWh += consumptionEA.getBaseConsumption_kWh();
			totalSavedConsumption_kWh += (1-consumptionEA.getConsumptionScaling_fr())*consumptionEA.getBaseConsumption_kWh();
		}
	}
}

double electricitySavings_pct = totalBaseConsumption_kWh > 0 ? (totalSavedConsumption_kWh/totalBaseConsumption_kWh * 100) : 0;
sl_electricityDemandReduction_pct_Businesspark.setValue(roundToInt(electricitySavings_pct), false);

// Rooftop PV SYSTEMS:
Pair<Double, Double> pair = f_getPVSystemPercentage( utilityGridConnections );
int PV_pct = roundToInt(100.0 * pair.getFirst() / pair.getSecond());
sl_rooftopPVCompanies_pct_Businesspark.setValue(PV_pct, false);

//Large scale EA production systems (PV on land And Wind)
f_getCurrentPVOnLandAndWindturbineValues(); // Used for slider minimum: non adjustable GCProductions

double totalPVOnLand_kW = 0; // Of movable slider GC
double totalWind_kW = 0; // Of movable slider GC

for(GridConnection productionGC : c_electricityTabEASliderGCs){
	if(productionGC instanceof GCEnergyProduction && productionGC.v_isActive){
		for(J_EAProduction productionEA : productionGC.c_productionAssets){
			if(productionEA.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC){
				totalPVOnLand_kW += productionEA.getCapacityElectric_kW();
				break;
			}
			else if(productionEA.getEAType() == OL_EnergyAssetType.WINDMILL){
				totalWind_kW += productionEA.getCapacityElectric_kW();
				break;
			}
		}
	}
}
sl_largeScalePV_ha_Businesspark.setRange(0, 1000); // Needed to prevent anylogic range bug
sl_largeScalePV_ha_Businesspark.setValue((totalPVOnLand_kW/zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha) + p_currentPVOnLand_ha, false);
sl_largeScaleWind_MW_Businesspark.setRange(0, 1000); // Needed to prevent anylogic range bug
sl_largeScaleWind_MW_Businesspark.setValue((totalWind_kW/1000) + p_currentWindTurbines_MW, false);

//Curtailment
boolean curtailment = true;
for(GridConnection GC : utilityGridConnections){
	if(!GC.v_enableCurtailment){
		curtailment = false;
		break;
	}
}
cb_curtailment_businesspark.setSelected(curtailment, false);


//Large scale battery systems
f_getCurrentGridBatterySize(); // Used for slider minimum: non adjustable GCGridBatteries

double totalBatteryStorage_kWh = 0;
for(GridConnection batteryGC : c_electricityTabEASliderGCs){
	if(batteryGC instanceof GCGridBattery && batteryGC.v_isActive){
		totalBatteryStorage_kWh += batteryGC.p_batteryAsset.getStorageCapacity_kWh();
	}
}
sl_collectiveBattery_MWh_businesspark.setRange(0, 1000); // Needed to prevent anylogic range bug
sl_collectiveBattery_MWh_businesspark.setValue((totalBatteryStorage_kWh/1000.0) + p_currentTotalGridBatteryCapacity_MWh, false);


/*ALCODEEND*/}

double f_updateElectricitySliders_custom()
{/*ALCODESTART::1754926103691*/
//If you have a custom tab, 
//override this function to make it update automatically
traceln("Forgot to override the update custom electricity sliders functionality");
/*ALCODEEND*/}

double f_setCurtailment(boolean activateCurtailment,List<GridConnection> gcList)
{/*ALCODESTART::1754986167346*/
for (GridConnection GC : gcList) {
	GC.v_enableCurtailment = activateCurtailment;
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_initializeTab_Electricity(List<GridConnection> electricityTabEASliderGCs)
{/*ALCODESTART::1756302457919*/
c_electricityTabEASliderGCs.addAll(electricityTabEASliderGCs);

f_getCurrentPVOnLandAndWindturbineValues();
f_getCurrentGridBatterySize();
/*ALCODEEND*/}

double f_getCurrentGridBatterySize()
{/*ALCODESTART::1765276703854*/
p_currentTotalGridBatteryCapacity_MWh = 0;
for(GCGridBattery GCBat : uI_Tabs.f_getAllSliderGridConnections_gridBatteries()){
	if(!c_electricityTabEASliderGCs.contains(GCBat) && GCBat.v_isActive){
		p_currentTotalGridBatteryCapacity_MWh += (GCBat.p_batteryAsset.getStorageCapacity_kWh()/1000.0);
	}
}
/*ALCODEEND*/}

