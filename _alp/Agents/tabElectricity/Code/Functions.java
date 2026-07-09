double f_setPVOnLand(double hectare,List<GCEnergyProduction> gcListProduction)
{/*ALCODESTART::1722256117103*/
// Single solarfarm at GridNode T0
for ( GCEnergyProduction GCEP : gcListProduction) {
	for(J_EAProduction j_ea : GCEP.c_productionAssets) {
		if (j_ea.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC) {
			if (!GCEP.v_isActive) {
				GCEP.f_setActive(true, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.p_timeVariables);
			}
			
			double solarFieldPower_kW = (double)roundToInt(hectare * zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha);
			j_ea.setCapacityElectric_kW(solarFieldPower_kW, GCEP);
			GCEP.v_liveConnectionMetaData.setCapacities_kW(GCEP.v_liveConnectionMetaData.getContractedDeliveryCapacity_kW(), solarFieldPower_kW, solarFieldPower_kW);
			
			if(hectare == 0){
				GCEP.f_setActive(false, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.p_timeVariables);
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
			house.f_removeExternalAssetManagement(I_BatteryManagement.class);
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
		double installedPVCapacity_kW = house.v_liveAssetsMetaData.PVPotential_kW;
		
		//Compensate for pt if it is present
		if(house.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.ptProductionHeat_kW)){
			installedPVCapacity_kW = max(0, installedPVCapacity_kW-zero_Interface.energyModel.avgc_data.p_avgPTPanelSize_m2*zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2); //For now just 1 panel
		}
		J_ProfilePointer profilePointer = f_getPVTProfilePointer(house.v_liveAssetsMetaData.PVOrientation, house.p_gridConnectionID);
		J_EAProduction productionAsset = new J_EAProduction ( house, OL_EnergyAssetType.PHOTOVOLTAIC, assetName, OL_EnergyCarriers.ELECTRICITY, installedPVCapacity_kW, zero_Interface.energyModel.p_timeParameters, profilePointer );
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
// Single windfarm at GridNode T0
for ( GCEnergyProduction GCEP : gcListProduction) {
	for(J_EAProduction j_ea : GCEP.c_productionAssets) {
		if (j_ea.getEAType() == OL_EnergyAssetType.WINDMILL) {
			if (!GCEP.v_isActive) {
				GCEP.f_setActive(true, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.p_timeVariables);
			}
			double setCapacity_kW = roundToInt(1000*AllocatedWindPower_MW);
			j_ea.setCapacityElectric_kW(setCapacity_kW, GCEP);
			GCEP.v_liveConnectionMetaData.setCapacities_kW(GCEP.v_liveConnectionMetaData.getContractedDeliveryCapacity_kW(), setCapacity_kW, setCapacity_kW);
			
			if(AllocatedWindPower_MW == 0){
				GCEP.f_setActive(false, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.p_timeVariables);
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
	// Set Profile Assets
	for (J_EAProfile j_ea : gc.c_profileAssets) {
		if(j_ea.getAssetFlowCategory() == OL_AssetFlowCategories.fixedConsumptionElectric_kW){
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

double f_getInitialPVOnLandAndWindturbineValues()
{/*ALCODESTART::1745483988251*/
p_initialPVOnLand_ha = 0;
p_initialWindTurbines_MW = 0;
for(GCEnergyProduction GCProd : uI_Tabs.f_getAllSliderGridConnections_production()){
	if(!c_electricityTabEASliderGCs.contains(GCProd) && !c_customSolarfarmGCs.contains(GCProd) && !c_customWindfarmGCs.contains(GCProd) && GCProd.v_isActive){
		for(J_EAProduction ea : GCProd.c_productionAssets){
			if(ea.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC){
				p_initialPVOnLand_ha += ea.getCapacityElectric_kW()/zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha;
			}
			else if(ea.getEAType() == OL_EnergyAssetType.WINDMILL){
				p_initialWindTurbines_MW += ea.getCapacityElectric_kW()/1000;
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
	J_ProfilePointer profilePointer = f_getPVTProfilePointer(gc.v_liveAssetsMetaData.PVOrientation, gc.p_gridConnectionID);
	J_EAProduction productionAsset = new J_EAProduction ( gc, assetType, assetName, OL_EnergyCarriers.ELECTRICITY, capacity_kWp, zero_Interface.energyModel.p_timeParameters, profilePointer );
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

double f_setHouseholdBatteries(double homeBatteries_pct,List<GCHouse> gcListHouses)
{/*ALCODESTART::1750063382310*/
// Setting houseBatteries
double nbHouseBatteries = count(gcListHouses, h -> h.p_batteryAsset != null); //f_getEnergyAssets(), p -> p.energyAssetType == OL_EnergyAssetType.STORAGE_ELECTRIC && p.getParentAgent() instanceof GCHouse);
double nbHousesWithPV = count(gcListHouses, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW)); //count(energyModel.f_getGridConnections(), p->p instanceof GCHouse);
double nbHousesWithBatteryGoal = roundToInt(nbHousesWithPV * homeBatteries_pct / 100);

if( nbHousesWithPV > 0 ){
	while ( nbHouseBatteries > nbHousesWithBatteryGoal ) {
		GCHouse house = findFirst(gcListHouses, p -> p.p_batteryAsset != null );
		house.p_batteryAsset.removeEnergyAsset();
		house.f_removeExternalAssetManagement(I_BatteryManagement.class);
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
		battery.f_setActive(true, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.p_timeVariables);
	}
	
	
	double capacity_kW = storageCapacity_kWh / zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power;
	if(batteryAsset.getCapacityElectric_kW() > 0 && batteryAsset.getStorageCapacity_kWh() > 0){ //If already existing power present: keep relation between power and storage capacity the same.
		capacity_kW = storageCapacity_kWh * ( batteryAsset.getStorageCapacity_kWh() / batteryAsset.getCapacityElectric_kW());
	}
	batteryAsset.setCapacityElectric_kW( capacity_kW );
	batteryAsset.setStorageCapacity_kWh( storageCapacity_kWh, battery );
	battery.v_liveConnectionMetaData.setCapacities_kW(capacity_kW, capacity_kW, capacity_kW);
	
	if(storageCapacity_kWh == 0){
		battery.f_setActive(false, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.p_timeVariables);
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
		new J_EAConsumption(house, OL_EnergyAssetType.GAS_HOB, "default_house_cooking_demand_fr", yearlyCookingDemand_kWh, OL_EnergyCarriers.METHANE, zero_Interface.energyModel.p_timeParameters, pp);
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
		J_EAConsumption cookingAsset = findFirst(house.c_consumptionAssets, p -> p.getEAType() == OL_EnergyAssetType.GAS_HOB );
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
// Update all loaded pages
for (ShapeGroup page : c_loadedPageGroups) {
	if(page == gr_electricitySliders_households){
		f_updateElectricitySliders_households();
	}
	else if(page == gr_electricitySliders_companies){
		f_updateElectricitySliders_companies();
	}
	else if(page == gr_electricitySliders_collective){
		f_updateElectricitySliders_collective();
	}
	else if(page == gr_customGCSolarfarmSettings){
		f_updateCustomGCSolarfarmSettings();
	}
	else if(page == gr_customGCWindfarmSettings){
		f_updateCustomGCWindfarmSettings();
	}
	else if(page == gr_customGCGridBatterySettings){
		f_updateCustomGCGridBatterySettings();
	}
	else{
		f_updateElectricitySliders_custom(); 
	}
}

/*ALCODEEND*/}

double f_updateElectricitySliders_households()
{/*ALCODESTART::1754926103687*/
//Get the house grid connections
List<GCHouse> houseGridConnections = uI_Tabs.f_getActiveSliderGridConnections_houses();

//Rooftop PV
int nbHouses = houseGridConnections.size();
int nbHousesWithPV = count(houseGridConnections, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW));
double pv_pct = 100.0 * nbHousesWithPV / nbHouses;
sl_householdRooftopPV_pct.setValue(roundToInt(pv_pct), false);

//Home batteries
if ( nbHousesWithPV != 0 ) {
	int nbHousesWithHomeBattery = count(houseGridConnections, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW) && x.p_batteryAsset != null);
	double battery_pct = 100.0 * nbHousesWithHomeBattery / nbHousesWithPV;
	sl_householdBatteries_pct.setValue(roundToInt(battery_pct), false);
}

//Curtailment PV houses
boolean curtailment = true;
for(GridConnection GC : houseGridConnections){
	if(!GC.f_isAssetManagementActive(I_CurtailManagement.class)){
		curtailment = false;
		break;
	}
}
cb_householdCurtailment.setSelected(curtailment, false);

//Electric cooking
int nbHousesWithElectricCooking = count(houseGridConnections, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC);
double cooking_pct = 100.0 * nbHousesWithElectricCooking / nbHouses;
sl_householdElectricCooking_pct.setValue(roundToInt(cooking_pct), false);

//Consumption growth
double totalBaseConsumption_kWh = 0;
double totalSavedConsumption_kWh = 0;
for(GCHouse GC : houseGridConnections){
	if(GC.v_isActive){
		List<J_EAProfile> profileEAs = findAll(GC.c_profileAssets, profile -> profile.getAssetFlowCategory() == OL_AssetFlowCategories.fixedConsumptionElectric_kW);
		for(J_EAProfile profileEA : profileEAs){
			double baseConsumption_kWh = profileEA.getBaseConsumption_kWh();
			totalBaseConsumption_kWh += baseConsumption_kWh;
			totalSavedConsumption_kWh += (1 - profileEA.getProfileScaling_fr()) * baseConsumption_kWh;
		}
	}
}

double electricityDemandIncrease_pct = totalBaseConsumption_kWh > 0 ? ( (- totalSavedConsumption_kWh)/totalBaseConsumption_kWh * 100) : 0;
sl_householdElectricityDemandIncrease_pct.setValue(roundToInt(electricityDemandIncrease_pct), false);
/*ALCODEEND*/}

double f_updateElectricitySliders_companies()
{/*ALCODESTART::1754926103689*/
//Get the utility grid connections
List<GridConnection> utilityGridConnections = new ArrayList<>(uI_Tabs.f_getActiveSliderGridConnections_utilities());

//Electricity savings
double totalBaseConsumption_kWh = 0;
double totalSavedConsumption_kWh = 0;
for(GridConnection GC : utilityGridConnections){
	if(GC.v_isActive){
		List<J_EAProfile> profileEAs = findAll(GC.c_profileAssets, profile -> profile.getAssetFlowCategory() == OL_AssetFlowCategories.fixedConsumptionElectric_kW);
		for(J_EAProfile profileEA : profileEAs){
			double baseConsumption_kWh = profileEA.getBaseConsumption_kWh();
			totalBaseConsumption_kWh += baseConsumption_kWh;
			totalSavedConsumption_kWh += (1 - profileEA.getProfileScaling_fr()) * baseConsumption_kWh;
		}
	}
}

double electricitySavings_pct = totalBaseConsumption_kWh > 0 ? (totalSavedConsumption_kWh/totalBaseConsumption_kWh * 100) : 0;
sl_companiesElectricityDemandReduction_pct.setValue(roundToInt(electricitySavings_pct), false);

// Rooftop PV:
Pair<Double, Double> pair = f_getPVSystemPercentage( utilityGridConnections );
int PV_pct = roundToInt(100.0 * pair.getFirst() / pair.getSecond());
sl_companiesRooftopPV_pct.setValue(PV_pct, false);

//Curtailment PV companies
boolean curtailment = true;
for(GridConnection GC : utilityGridConnections){
	if(!GC.f_isAssetManagementActive(I_CurtailManagement.class)){
		curtailment = false;
		break;
	}
}
cb_companiesCurtailment.setSelected(curtailment, false);
/*ALCODEEND*/}

double f_updateElectricitySliders_custom()
{/*ALCODESTART::1754926103691*/
//If you have a custom tab, override this function to make it update automatically
throw new RuntimeException("Forgot to override the update custom electricity sliders functionality");
/*ALCODEEND*/}

double f_setCurtailment(boolean activateCurtailment,List<GridConnection> gcList)
{/*ALCODESTART::1754986167346*/
for (GridConnection GC : gcList) {
	if(activateCurtailment){
		GC.f_setExternalAssetManagement(new J_CurtailManagementContractCapacity(GC, zero_Interface.energyModel.p_timeParameters));
	}
	else{
		GC.f_removeExternalAssetManagement(I_CurtailManagement.class);
	}
}



//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_initializeTab_Electricity(List<GridConnection> electricityTabEASliderGCs)
{/*ALCODESTART::1756302457919*/
c_electricityTabEASliderGCs.clear();
c_electricityTabEASliderGCs.addAll(electricityTabEASliderGCs);

f_getInitialPVOnLandAndWindturbineValues();
f_getInitialGridBatterySize();

f_initializeElectricityPages();
/*ALCODEEND*/}

double f_getInitialGridBatterySize()
{/*ALCODESTART::1765276703854*/
p_initialTotalGridBatteryCapacity_MWh = 0;
for(GCGridBattery GCBat : uI_Tabs.f_getAllSliderGridConnections_gridBatteries()){
	if(!c_electricityTabEASliderGCs.contains(GCBat) && !c_customGridBatteryGCs.contains(GCBat) && GCBat.v_isActive){
		p_initialTotalGridBatteryCapacity_MWh += (GCBat.p_batteryAsset.getStorageCapacity_kWh()/1000.0);
	}
}
/*ALCODEEND*/}

J_ProfilePointer f_getPVTProfilePointer(OL_PVOrientation pvtOrientation,String gridConnectionID)
{/*ALCODESTART::1773764103422*/
J_ProfilePointer profilePointer = null;

switch (pvtOrientation){
	case EASTWEST:
		profilePointer = zero_Interface.energyModel.pp_PVProduction15DegEastWest_fr;
		break;
	case SOUTH:
		profilePointer = zero_Interface.energyModel.pp_PVProduction35DegSouth_fr;
		break;
	case CUSTOM:
		profilePointer = zero_Interface.energyModel.f_findProfile("GC: " + gridConnectionID + " custom pv profile");
		if(profilePointer == null){
			throw new RuntimeException("Can't find custom profile pointer for GC with custom orientation.");
		}
		break;
}

return profilePointer;
/*ALCODEEND*/}

ShapeGroup f_goToPage(int pageIndex)
{/*ALCODESTART::1777552836057*/
for (ShapeGroup group : c_loadedPageGroups) {
    group.setVisible(false);
}

if (c_loadedPageGroups.isEmpty()) return;

v_previousPageIndex = v_currentPageIndex;
v_currentPageIndex = pageIndex;
c_loadedPageGroups.get(v_currentPageIndex).setVisible(true); // Show the selected page group
f_updatePageIndicator(); // Update the page indicator text
/*ALCODEEND*/}

ShapeGroup f_nextPage()
{/*ALCODESTART::1777552936318*/
if (c_loadedPageGroups.isEmpty()) return;
int nextIndex = (v_currentPageIndex + 1) % c_loadedPageGroups.size();
f_goToPage(nextIndex);
/*ALCODEEND*/}

ShapeGroup f_previousPage()
{/*ALCODESTART::1777553025667*/
if (c_loadedPageGroups.isEmpty()) return;
int prevIndex = (v_currentPageIndex - 1 + c_loadedPageGroups.size()) % c_loadedPageGroups.size();
f_goToPage(prevIndex);
/*ALCODEEND*/}

ShapeGroup f_updatePageIndicator()
{/*ALCODESTART::1777553071510*/
t_pageIndicator.setText("Pagina " + (v_currentPageIndex + 1) + "/" + c_loadedPageGroups.size());
presentation.remove(gr_pageIndicator);
presentation.add(gr_pageIndicator);
/*ALCODEEND*/}

double f_updateElectricitySliders_collective()
{/*ALCODESTART::1777579296267*/
List<GridConnection> productionGridConnections = new ArrayList<>(uI_Tabs.f_getAllSliderGridConnections_production());

//Large scale EA production systems (PV/Wind on land)
f_getInitialPVOnLandAndWindturbineValues(); // Used for slider minimum: non adjustable GCProductions

double totalPVOnLand_kW = 0; // Of movable slider GC
double totalWind_kW = 0; // Of movable slider GC

for(GridConnection productionGC : c_electricityTabEASliderGCs){ // Default slider solarfarm/windfarm collection
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

double totalCustomPVOnLand_kW = 0;
for(GridConnection customSF : c_customSolarfarmGCs){
    if(customSF.v_isActive){
        for(J_EAProduction ea : customSF.c_productionAssets){
            if(ea.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC){
                totalCustomPVOnLand_kW += ea.getCapacityElectric_kW();
            }
        }
    }
}

double totalCustomWind_kW = 0;
for(GridConnection customWF : c_customWindfarmGCs){
    if(customWF.v_isActive){
        for(J_EAProduction ea : customWF.c_productionAssets){
            if(ea.getEAType() == OL_EnergyAssetType.WINDMILL){
                totalCustomWind_kW += ea.getCapacityElectric_kW();
            }
        }
    }
}

double minSliderPVOnLand_ha = p_initialPVOnLand_ha + totalCustomPVOnLand_kW/zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha;
double maxSliderPVOnLand_ha = minSliderPVOnLand_ha + 50;
sl_largeScalePV_ha.setRange(minSliderPVOnLand_ha, maxSliderPVOnLand_ha);
sl_largeScalePV_ha.setValue((totalPVOnLand_kW/zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha) + minSliderPVOnLand_ha, false);

double minSliderWind_MW = p_initialWindTurbines_MW + totalCustomWind_kW/1000;
double maxSliderWind_MW = minSliderWind_MW + 20;
sl_largeScaleWind_MW.setRange(minSliderWind_MW, maxSliderWind_MW);
sl_largeScaleWind_MW.setValue((totalWind_kW/1000) + minSliderWind_MW, false);


//Grid batteries
f_getInitialGridBatterySize(); // Used for slider minimum: non adjustable GCGridBatteries

double totalDefaultBatteryCapacity_MWh = 0;
for(GridConnection sliderGC : c_electricityTabEASliderGCs){ // Default slider battery collection
	if(sliderGC.v_isActive && sliderGC instanceof GCGridBattery sliderGridBattery){
		totalDefaultBatteryCapacity_MWh += sliderGridBattery.p_batteryAsset.getStorageCapacity_kWh()/1000;
	}
}

double totalCustomBatteryCapacity_MWh = 0;
for(GridConnection customGB : c_customGridBatteryGCs){
	if(customGB.v_isActive && customGB.p_batteryAsset != null){
		totalCustomBatteryCapacity_MWh += customGB.p_batteryAsset.getStorageCapacity_kWh()/1000;
	}
}

double minSliderGridBattery_MWh = p_initialTotalGridBatteryCapacity_MWh + totalCustomBatteryCapacity_MWh;
double maxSliderGridBattery_MWh = minSliderGridBattery_MWh + zero_Interface.energyModel.avgc_data.p_maxGridBatteryStorageCapacityMVMVBusinesspark_MW;
sl_gridBatteries_MWh.setRange(minSliderGridBattery_MWh, maxSliderGridBattery_MWh);
sl_gridBatteries_MWh.setValue(totalDefaultBatteryCapacity_MWh + minSliderGridBattery_MWh, false);

//Curtailment large scale PV and wind
boolean curtailment = true;
for(GridConnection GC : productionGridConnections){
	if(!GC.f_isAssetManagementActive(I_CurtailManagement.class)){
		curtailment = false;
		break;
	}
}
cb_gridCurtailment.setSelected(curtailment, false);
/*ALCODEEND*/}

double f_initializeElectricityPages()
{/*ALCODESTART::1777985169528*/
// CHOOSE WHICH PAGES IN YOUR TAB YOU WANT TO BE ABLE TO SHOW FOR YOUR PROJECT 
boolean hasHouses = uI_Tabs.f_getActiveSliderGridConnections_houses().size() > 0;
boolean hasCompanies = uI_Tabs.f_getActiveSliderGridConnections_utilities().size() > 0;

c_loadedPageGroups = new ArrayList<>();
// Load in the existing pages you want to include in the tab
if (hasHouses) {
	c_loadedPageGroups.add(gr_electricitySliders_households);
} 
if (hasCompanies) {
	c_loadedPageGroups.add(gr_electricitySliders_companies);
}
c_loadedPageGroups.add(gr_electricitySliders_collective);

// If you have a custom page, add it by using f_addCustomPage:
f_addCustomPage();

// Show/hide page indicator based on number of pages
if (c_loadedPageGroups.size() <= 1) {
    gr_pageIndicator.setVisible(false);
} else {
    gr_pageIndicator.setVisible(true);
}

// Navigate to the first page
f_goToPage(0);
/*ALCODEEND*/}

double f_addCustomPage()
{/*ALCODESTART::1778055837807*/
// Override this function to add your custom page to c_loadedPageGroups, for instance, like this:
//c_loadedPageGroups.add(gr_electricitySliders_custom);
/*ALCODEEND*/}

double f_updateCustomGCSolarfarmSettings()
{/*ALCODESTART::1783088206556*/
if (zero_Interface.c_selectedGridConnections.isEmpty()) {
    return;
}

GridConnection selectedGC = zero_Interface.c_selectedGridConnections.get(0);
if (!(selectedGC instanceof GCEnergyProduction) || !c_customSolarfarmGCs.contains(selectedGC)) {
    return;
}

GCEnergyProduction gc = (GCEnergyProduction) selectedGC;
J_EAProduction pvAsset = (J_EAProduction) gc.c_productionAssets.get(0);

// Installed capacity per hectare
double currentCapacity_kW = pvAsset.getCapacityElectric_kW();
double area_m2 = gc.c_connectedGISObjects.get(0).gisRegion.area();
double area_ha = area_m2 / 10000.0;
double currentCapacity_kWpha = currentCapacity_kW / area_ha;

sl_customGCSolarfarmInstalledCapacity_kWpha.setRange((int)(0.8*zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha), (int)(1.2*zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha));
sl_customGCSolarfarmInstalledCapacity_kWpha.setValue(currentCapacity_kWpha, false);

// PV Orientation
J_ProfilePointer currentProfile = pvAsset.getProfilePointer();
String currentOrientationLabel = "Zuid (15°)"; // Default
if (currentProfile == zero_Interface.energyModel.pp_PVProduction15DegEastWest_fr) {
    currentOrientationLabel = "Oost/West (35°)";
}
cb_customGCSolarfarmPVOrientation.setValue(currentOrientationLabel, false);

// Curtailment
boolean hasCurtailment = gc.f_isAssetManagementActive(I_CurtailManagement.class);
cb_customGCSolarfarmCurtailment.setSelected(hasCurtailment, false);

// Contracted capacity limit
double maxContractedCapacity_kW = ceil(currentCapacity_kW / 10.0) * 10.0;

sl_customGCSolarfarmContractedCapacity_kW.setRange(0, maxContractedCapacity_kW);
sl_customGCSolarfarmContractedCapacity_kW.setValue(gc.v_liveConnectionMetaData.getContractedFeedinCapacity_kW(), false);

/*ALCODEEND*/}

double f_updateCustomGCWindfarmSettings()
{/*ALCODESTART::1783088206569*/
if (zero_Interface.c_selectedGridConnections.isEmpty()) { 
    return;
}

GridConnection selectedGC = zero_Interface.c_selectedGridConnections.get(0);
if (!(selectedGC instanceof GCEnergyProduction) || !c_customWindfarmGCs.contains(selectedGC)) {
    return;
}

GCEnergyProduction gc = (GCEnergyProduction) selectedGC;
J_EAProduction windAsset = (J_EAProduction) gc.c_productionAssets.get(0);

// Installed capacity
double currentCapacity_MW = windAsset.getCapacityElectric_kW()/1000;

sl_customGCWindfarmInstalledCapacity_MW.setRange(0.1, 5);
sl_customGCWindfarmInstalledCapacity_MW.setValue(currentCapacity_MW, false); // false prevents triggering ActionCode

// Curtailment
boolean hasCurtailment = gc.f_isAssetManagementActive(I_CurtailManagement.class);
cb_customGCWindfarmCurtailment.setSelected(hasCurtailment, false);

// Contracted capacity limit
sl_customGCWindfarmContractedCapacity_MW.setRange(0, currentCapacity_MW + 1E-10);
sl_customGCWindfarmContractedCapacity_MW.setValue(gc.v_liveConnectionMetaData.getContractedFeedinCapacity_kW()/1000 + 1E-10, false);
/*ALCODEEND*/}

double f_updateCustomGCGridBatterySettings()
{/*ALCODESTART::1783088206581*/
if (zero_Interface.c_selectedGridConnections.isEmpty()) {
    return;
}

GridConnection selectedGC = zero_Interface.c_selectedGridConnections.get(0);
if (!(selectedGC instanceof GCGridBattery) || !c_customGridBatteryGCs.contains(selectedGC)) {
    return;
}

GCGridBattery gc = (GCGridBattery) selectedGC;
J_EAStorageElectric batteryAsset = (J_EAStorageElectric)gc.c_storageAssets.get(0);

// Installed capacity
double currentCapacity_MWh = batteryAsset.getStorageCapacity_kWh() / 1000;
double currentCapacity_MW = batteryAsset.getCapacityElectric_kW() / 1000;

sl_customGCGridBatteryInstalledCapacity_MWh.setRange(zero_Interface.energyModel.avgc_data.p_minGridBatteryStorageCapacityMVLVResidential_kW/1000, zero_Interface.energyModel.avgc_data.p_maxGridBatteryStorageCapacityMVMVBusinesspark_MW);
sl_customGCGridBatteryInstalledCapacity_MWh.setValue(currentCapacity_MWh, false);

sl_customGCGridBatteryInstalledCapacity_MW.setRange(0.05, currentCapacity_MWh / zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power + 1E-10);
sl_customGCGridBatteryInstalledCapacity_MW.setValue(currentCapacity_MW, false);

// Battery management selection
I_BatteryManagement currentBatteryManagement = gc.f_getBatteryManagement();
String currentBMS_str = "Zelfverbruik"; // Default fallback

if (currentBatteryManagement instanceof J_BatteryManagementSelfConsumptionGridNode) {
    currentBMS_str = "Zelfverbruik";
} else if (currentBatteryManagement instanceof J_BatteryManagementPeakShaving) {
    currentBMS_str = "Peak shaving";
} else if (currentBatteryManagement instanceof J_BatteryManagementPrice) {
    currentBMS_str = "Prijssturing";
}
cb_customGCGridBatteryAlgorithm.setValue(currentBMS_str, false);
/*ALCODEEND*/}

double f_updateCustomGCSettings()
{/*ALCODESTART::1783088235401*/
boolean hasCustomGCSelected = false;
ShapeGroup expectedPage = null;
if (!zero_Interface.c_selectedGridConnections.isEmpty()) {
    GridConnection selectedGC = zero_Interface.c_selectedGridConnections.get(0);
    if (c_customSolarfarmGCs.contains(selectedGC)) {
        hasCustomGCSelected = true;
        expectedPage = gr_customGCSolarfarmSettings;
        f_updateCustomGCSolarfarmSettings();
    } else if (c_customWindfarmGCs.contains(selectedGC)) {
        hasCustomGCSelected = true;
        expectedPage = gr_customGCWindfarmSettings;
        f_updateCustomGCWindfarmSettings();
    } else if (c_customGridBatteryGCs.contains(selectedGC)) {
        hasCustomGCSelected = true;
        expectedPage = gr_customGCGridBatterySettings;
        f_updateCustomGCGridBatterySettings();
    }
}
boolean isExpectedPageLoaded = (expectedPage != null && c_loadedPageGroups.contains(expectedPage));
boolean hasCustomPageLoaded = c_loadedPageGroups.contains(gr_customGCSolarfarmSettings) || 
                              c_loadedPageGroups.contains(gr_customGCWindfarmSettings) || 
                              c_loadedPageGroups.contains(gr_customGCGridBatterySettings);
// Reinitialize pages only if selection transitioned into or out of a custom GC
if ((hasCustomGCSelected && !isExpectedPageLoaded) || (!hasCustomGCSelected && hasCustomPageLoaded)) {
    f_addCustomGCSettingsPage();
}
/*ALCODEEND*/}

double f_addCustomSolarfarmGC(GridNode gn)
{/*ALCODESTART::1783089962320*/
v_customSolarfarmGCCounter++;
String id = "Custom_Solarfarm_" + v_customSolarfarmGCCounter;

// 1. Initialize GridConnection
GridConnection solarpark = zero_Interface.energyModel.f_createGridConnectionDuringRuntime(GCEnergyProduction.class, id, zero_Interface.p_defaultMainSliderGCName_solarfarm, gn.p_gridNodeID, null);

// 2. Create the GIS Object to calculate installed capacity
GIS_Object area = f_createAndLinkGISObjectCustomGC(solarpark, id, OL_GISObjectType.SOLARFARM, zero_Interface.v_solarParkColor, zero_Interface.v_solarParkLineColor);
double area_ha = area.gisRegion.area() / 10000;
double installedCapacity_kW = area_ha * zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha;
double maxContractedCapacity_kW = ceil(installedCapacity_kW / 10.0) * 10.0;

// 3. Update GridConnection data
solarpark.v_liveConnectionMetaData.setCapacities_kW(0.0, maxContractedCapacity_kW, maxContractedCapacity_kW);
solarpark.v_liveConnectionMetaData.setCapacitiesKnown(true, true, true);
solarpark.v_liveAssetsMetaData.PVOrientation = OL_PVOrientation.SOUTH;

// 4. Create the energy asset
J_EAProduction pvAsset = new J_EAProduction(solarpark, OL_EnergyAssetType.PHOTOVOLTAIC, "Custom PV", OL_EnergyCarriers.ELECTRICITY, installedCapacity_kW, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.pp_PVProduction35DegSouth_fr);

// 6. Update collections, sliders and legend
c_customSolarfarmGCs.add(solarpark);
if (!zero_Interface.c_modelActiveSpecialGISObjects.contains(area.p_GISObjectType)) {
    zero_Interface.c_modelActiveSpecialGISObjects.add(area.p_GISObjectType);
}
f_updateSliders_Electricity();
zero_Interface.f_refreshLegend();

// 7. Select the newly created asset immediately to update and set settings panel sliders
ArrayList<GIS_Object> selectedList = new ArrayList<>();
selectedList.add(area);
zero_Interface.f_selectBuilding(area, selectedList);
/*ALCODEEND*/}

double f_removeCustomGC(GridConnection gc)
{/*ALCODESTART::1783089962332*/
gc.f_setActive(false, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.p_timeVariables);

// 1. Remove energy assets
for(J_EA ea : new ArrayList<>(gc.c_energyAssets)) {
    ea.removeEnergyAsset();
}

// 2. Remove GIS Object
OL_GISObjectType removedGISType = null;
for (GIS_Object obj : new ArrayList<>(gc.c_connectedGISObjects)) {
	removedGISType = obj.p_GISObjectType;
    obj.gisRegion.setVisible(false);
    zero_Interface.energyModel.remove_pop_GIS_Objects(obj);
}

// 3. Remove from collections
zero_Interface.energyModel.c_pausedGridConnections.remove(gc);
if (gc instanceof GCEnergyProduction) {
	zero_Interface.energyModel.remove_EnergyProductionSites((GCEnergyProduction)gc);
	if(c_customSolarfarmGCs.contains((GCEnergyProduction)gc)){
		c_customSolarfarmGCs.remove(gc);
	}
	else if(c_customWindfarmGCs.contains((GCEnergyProduction)gc)){
		c_customWindfarmGCs.remove(gc);
	}
} else if (gc instanceof GCGridBattery) {
    zero_Interface.energyModel.remove_GridBatteries((GCGridBattery)gc);
    c_customGridBatteryGCs.remove((GCGridBattery)gc);
}

// 4. Refresh slider + legend to account for changes
if (removedGISType != null) {
    boolean typeExists = false;
    // Verify if any assets of this type still exist in the simulation
    for (GIS_Object obj : zero_Interface.energyModel.pop_GIS_Objects) {
        if (obj.p_GISObjectType == removedGISType && obj.gisRegion != null && obj.gisRegion.isVisible()) {
            typeExists = true;
            break;
        }
    }
    if (!typeExists) {
        zero_Interface.c_modelActiveSpecialGISObjects.remove(removedGISType);
    }
}

f_updateSliders_Electricity();
zero_Interface.f_refreshLegend();
/*ALCODEEND*/}

double f_addCustomWindfarmGC(GridNode gn)
{/*ALCODESTART::1783089962343*/
v_customWindfarmGCCounter++;
String id = "Custom_Windfarm_" + v_customWindfarmGCCounter;

// 1. Initialize GridConnection
GridConnection windpark = zero_Interface.energyModel.f_createGridConnectionDuringRuntime(GCEnergyProduction.class, id, zero_Interface.p_defaultMainSliderGCName_windfarm, gn.p_gridNodeID, null);

// 2. Set capacity
double defaultCapacity_kW = 1000;
windpark.v_liveConnectionMetaData.setCapacities_kW(0.0, defaultCapacity_kW, defaultCapacity_kW);
windpark.v_liveConnectionMetaData.setCapacitiesKnown(true, true, true);

// 3. Create the Energy Asset
J_EAProduction windAsset = new J_EAProduction(windpark, OL_EnergyAssetType.WINDMILL, "Custom Windpark", OL_EnergyCarriers.ELECTRICITY, defaultCapacity_kW, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.pp_windProduction_fr);

// 4. Create the GIS Object
GIS_Object area = f_createAndLinkGISObjectCustomGC(windpark, id, OL_GISObjectType.WINDFARM, zero_Interface.v_windFarmColor, zero_Interface.v_windFarmLineColor);

// 5. Update collections, sliders and legend
c_customWindfarmGCs.add(windpark);
if (!zero_Interface.c_modelActiveSpecialGISObjects.contains(area.p_GISObjectType)) {
    zero_Interface.c_modelActiveSpecialGISObjects.add(area.p_GISObjectType);
}
f_updateSliders_Electricity();
zero_Interface.f_refreshLegend();

// 6. Select the newly created asset immediately to update and set settings panel sliders
ArrayList<GIS_Object> selectedList = new ArrayList<>();
selectedList.add(area);
zero_Interface.f_selectBuilding(area, selectedList);
/*ALCODEEND*/}

double f_addCustomGridBatteryGC(GridNode gn)
{/*ALCODESTART::1783089962354*/
v_customGridBatteryGCCounter++;
String id = "Custom_Grid_Battery_" + v_customGridBatteryGCCounter;

// 1. Initialize GridConnection
GridConnection battery = zero_Interface.energyModel.f_createGridConnectionDuringRuntime(GCGridBattery.class, id, zero_Interface.p_defaultMainSliderGCName_battery, gn.p_gridNodeID, null);

// 2. Set capacity
double defaultCapacity_kW = 1000;
double defaultStorageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power*defaultCapacity_kW;
battery.v_liveConnectionMetaData.setCapacities_kW(defaultCapacity_kW, defaultCapacity_kW, defaultCapacity_kW);
battery.v_liveConnectionMetaData.setCapacitiesKnown(true, true, true);

// 3. Create the energy asset + pick default operation mode management class
J_EAStorageElectric batteryAsset = new J_EAStorageElectric(battery, defaultCapacity_kW, defaultStorageCapacity_kWh, 0.5, zero_Interface.energyModel.p_timeParameters);
I_BatteryManagement batteryAlgorithm = new J_BatteryManagementSelfConsumptionGridNode(battery, zero_Interface.energyModel.p_timeParameters);
battery.f_setBatteryManagement(batteryAlgorithm);
		
// 4. Create GIS Object 
GIS_Object area = f_createAndLinkGISObjectCustomGC(battery, id, OL_GISObjectType.BATTERY, zero_Interface.v_batteryColor, zero_Interface.v_batteryLineColor);

// 5. Update collections, sliders and legend
c_customGridBatteryGCs.add(battery);
if (!zero_Interface.c_modelActiveSpecialGISObjects.contains(area.p_GISObjectType)) {
    zero_Interface.c_modelActiveSpecialGISObjects.add(area.p_GISObjectType);
}
f_updateSliders_Electricity();
zero_Interface.f_refreshLegend();

// 6. Select the newly created asset immediately to update and set settings panel sliders
ArrayList<GIS_Object> selectedList = new ArrayList<>();
selectedList.add(area);
zero_Interface.f_selectBuilding(area, selectedList);
/*ALCODEEND*/}

GIS_Object f_createAndLinkGISObjectCustomGC(GridConnection gc,String id,OL_GISObjectType gisType,Color fillColor,Color lineColor)
{/*ALCODESTART::1783089962365*/
GIS_Object area = zero_Interface.energyModel.add_pop_GIS_Objects();
area.p_id = id;
area.p_GISObjectType = gisType;
area.p_latitude = c_tempSavedPointCoordinatesCustomGC.get(0).getX();
area.p_longitude = c_tempSavedPointCoordinatesCustomGC.get(0).getY();

// 2. Generate coordinates (circular for windfarm, square for others) and assign to region
double[] polyCoords;
if (gisType == OL_GISObjectType.WINDFARM) {
	double area_m2 = 1000; // Rule-of-Thumb windfarm: 1 hectare per 5 MW (5000 kW) capacity
	polyCoords = GISUtil.calculateCircleCoordinates(area.p_latitude, area.p_longitude, area_m2);
	area.p_annotation = "Windpark " + v_customWindfarmGCCounter;
} else if (gisType == OL_GISObjectType.BATTERY) {
	double area_m2 = 400; // Rule-of-Thumb grid battery: 1 hectare per 100 MWh storage capacity (100 m2 per MWh)
	polyCoords = GISUtil.calculateSquareCoordinates(area.p_latitude, area.p_longitude, area_m2);
	area.p_annotation = "Buurtbatterij " + v_customGridBatteryGCCounter;
} else {
	polyCoords = GISUtil.calculateCustomPolygonCoordinates(c_tempSavedPointCoordinatesCustomGC);
	area.p_annotation = "Zonnepark " + v_customSolarfarmGCCounter;
}
area.gisRegion = zero_Interface.f_createGISObject(polyCoords);

// 3. Add to collections
area.c_containedGridConnections.add(gc);
gc.c_connectedGISObjects.add(area);

// 4. Apply styling
area.p_defaultFillColor = fillColor;
area.p_defaultLineColor = lineColor;
area.p_defaultLineWidth = zero_Interface.v_energyAssetLineWidth;
zero_Interface.f_styleAreas(area);

return area;
/*ALCODEEND*/}

double f_addCustomGCLocationSelection(double clickx,double clicky)
{/*ALCODESTART::1783089962376*/
// --- PHASE 1: Drawing Polygon Vertices ---

// Add a vertex to the coordinates list
Point clickedCoord = new Point(clickx, clicky);
c_tempSavedPointCoordinatesCustomGC.add(clickedCoord);

// Place a small square dot on the map representing this vertex
double[] polyCoords = GISUtil.calculateSquareCoordinates(clickx, clicky, 25);
GISRegion dot = zero_Interface.f_createGISObject(polyCoords);
dot.setFillColor(Color.RED);
dot.setLineColor(Color.WHITE);
dot.setLineWidth(1.0);
c_tempSavedDotGISRegionsCustomGC.add(dot);

if (v_addCustomGCType == OL_EnergyAssetType.WINDMILL || v_addCustomGCType == OL_EnergyAssetType.STORAGE_ELECTRIC){
	b_customGCPolygonCreated = true;
	zero_Interface.f_setForcedClickScreenMessageText("Kies een trafo op de kaart");
} else {
	// Update instruction text
	int minNbRequiredVertices = 3;
	int currentNbOfSavedCoordinates = c_tempSavedPointCoordinatesCustomGC.size();
	if(minNbRequiredVertices - currentNbOfSavedCoordinates > 1){
		zero_Interface.f_setForcedClickScreenMessageText("Teken je locatie op de kaart. Kies nog minimaal " + (minNbRequiredVertices - currentNbOfSavedCoordinates) + " hoekpunten.");
	}
	else if(minNbRequiredVertices - currentNbOfSavedCoordinates == 1){
		zero_Interface.f_setForcedClickScreenMessageText("Teken je locatie op de kaart. Kies nog minimaal " + (minNbRequiredVertices - currentNbOfSavedCoordinates) + " hoekpunt.");
	} else {
		if (previewGISRegionCustomGC != null) {
	        previewGISRegionCustomGC.remove();
	    }
	    double[] previewCoords = GISUtil.calculateCustomPolygonCoordinates(c_tempSavedPointCoordinatesCustomGC);
	    previewGISRegionCustomGC = zero_Interface.f_createGISObject(previewCoords);
	    previewGISRegionCustomGC.setFillColor(new Color(255, 0, 0, 50)); // Semi-transparent red
	    previewGISRegionCustomGC.setLineColor(Color.RED);
	    previewGISRegionCustomGC.setLineWidth(1.0);
		zero_Interface.f_setForcedClickScreenMessageText("Huidig aantal hoekpunten: " + currentNbOfSavedCoordinates + ". Klik op 'Voltooien' om te bevestigen.");
	}
}

zero_Interface.f_deselectPreviousSelect();
/*ALCODEEND*/}

double f_addCustomGCTransformerSelection(double clickx,double clicky)
{/*ALCODESTART::1783089962387*/
// --- PHASE 2: Choose transformer to connect to ---
GridNode clickedGN = null;
for (GridNode GN : zero_Interface.energyModel.pop_gridNodes) {
    if (GN.gisRegion != null && GN.gisRegion.contains(clickx, clicky) && GN.gisRegion.isVisible()) {
        clickedGN = GN;
        break;
    }
}

if (clickedGN != null) {    
	if (v_addCustomGCType == OL_EnergyAssetType.PHOTOVOLTAIC){
    	f_addCustomSolarfarmGC(clickedGN);
    } else if (v_addCustomGCType == OL_EnergyAssetType.WINDMILL){
    	f_addCustomWindfarmGC(clickedGN);
    } else if (v_addCustomGCType == OL_EnergyAssetType.STORAGE_ELECTRIC){
    	f_addCustomGridBatteryGC(clickedGN);
    }
    // Clean up coordinate temporary dots, state variables, lists
    f_stopCustomGCCreation();
}
/*ALCODEEND*/}

double f_removeCustomGCSelection(double clickx,double clicky)
{/*ALCODESTART::1783089962397*/
// Group all GIS objects to check for the click
List<GIS_Object> allGISObjects = new ArrayList<>();
for(GIS_Building b : zero_Interface.energyModel.pop_GIS_Buildings) {
	allGISObjects.add(b);
}
for(GIS_Object object : zero_Interface.energyModel.pop_GIS_Objects){
	allGISObjects.add(object);
}
for (GIS_Object GISObject : allGISObjects) {
    if (GISObject.gisRegion != null && GISObject.gisRegion.contains(clickx, clicky) && GISObject.gisRegion.isVisible()) {
        if (GISObject.c_containedGridConnections.size() > 0) {
            GridConnection gc = GISObject.c_containedGridConnections.get(0);
            // Only allow deletion of custom created GCs
            if (c_customSolarfarmGCs.contains(gc) || c_customWindfarmGCs.contains(gc) || c_customGridBatteryGCs.contains(gc)) {
                f_removeCustomGC(gc);
                f_stopCustomGCCreation();
                return;
            }
        }
    }
}
// If the user clicks elsewhere, cancel the deletion mode
f_stopCustomGCCreation();
/*ALCODEEND*/}

double f_stopCustomGCCreation()
{/*ALCODESTART::1783089962419*/
// Clean up temporary dot markers, preview polygon, and coordinates list
f_resetCustomGCCreation();

// Reset state variables
b_customGCPolygonCreated = false;
b_addCustomGC = false;
v_addCustomGCType = null;
b_removeCustomGC = false;

// Hide forced click screen, if needed
zero_Interface.f_setForcedClickScreenVisibility(false);
zero_Interface.f_setForcedClickScreenTextBoxes("", new Color(255, 255, 255), new Color(0, 0, 0), "", new Color(255, 255, 255), new Color(0, 0, 0));
/*ALCODEEND*/}

double f_resetCustomGCCreation()
{/*ALCODESTART::1783089962430*/
// Clean up temporary dot markers
for (GISRegion dot : c_tempSavedDotGISRegionsCustomGC) {
    if (dot != null) {
        dot.remove();
    }
}
c_tempSavedDotGISRegionsCustomGC.clear();

// Clean up preview polygon
if (previewGISRegionCustomGC != null) {
    previewGISRegionCustomGC.remove();
    previewGISRegionCustomGC = null;
}

// Clear coordinates list
c_tempSavedPointCoordinatesCustomGC.clear();
/*ALCODEEND*/}

double f_addCustomGCSettingsPage()
{/*ALCODESTART::1783101013511*/
// Identify the active page group before removing custom settings pages
ShapeGroup activePageGroup = null;
if (v_currentPageIndex >= 0 && v_currentPageIndex < c_loadedPageGroups.size()) {
    activePageGroup = c_loadedPageGroups.get(v_currentPageIndex);
}

// Hide custom settings pages by default so they are not visible when deselected
gr_customGCSolarfarmSettings.setVisible(false);
gr_customGCWindfarmSettings.setVisible(false);
gr_customGCGridBatterySettings.setVisible(false);
c_loadedPageGroups.remove(gr_customGCSolarfarmSettings);
c_loadedPageGroups.remove(gr_customGCWindfarmSettings);
c_loadedPageGroups.remove(gr_customGCGridBatterySettings);

boolean customGCAdded = false;
int targetPage = 0;
if (!zero_Interface.c_selectedGridConnections.isEmpty()) {
    GridConnection selectedGC = zero_Interface.c_selectedGridConnections.get(0);
    if (c_customSolarfarmGCs.contains(selectedGC)) {
        c_loadedPageGroups.add(gr_customGCSolarfarmSettings);
        targetPage = c_loadedPageGroups.indexOf(gr_customGCSolarfarmSettings);
        customGCAdded = true;
    } else if (c_customWindfarmGCs.contains(selectedGC)) {
        c_loadedPageGroups.add(gr_customGCWindfarmSettings);
        targetPage = c_loadedPageGroups.indexOf(gr_customGCWindfarmSettings);
        customGCAdded = true;
    } else if (c_customGridBatteryGCs.contains(selectedGC)) {
        c_loadedPageGroups.add(gr_customGCGridBatterySettings);
        targetPage = c_loadedPageGroups.indexOf(gr_customGCGridBatterySettings);
        customGCAdded = true;
    }
}

// Show/hide page indicator based on number of active pages
if (c_loadedPageGroups.size() <= 1) {
    gr_pageIndicator.setVisible(false);
} else {
    gr_pageIndicator.setVisible(true);
}

// Navigate to the correct page: the selected custom settings page, stay on the current page, or go to previous/default page
if (customGCAdded) { // Custom GC is selected
	zero_Interface.uI_Tabs.f_setTab(OL_CustomScenarioTabs.ELECTRICITY);
    f_goToPage(targetPage);
} else { // Custom GC is deselected
    int newPageIndex = -1;
    if (activePageGroup != null) {
        newPageIndex = c_loadedPageGroups.indexOf(activePageGroup);
    }
    
    if (newPageIndex >= 0) { // Stay on the current page
        f_goToPage(newPageIndex);
    } else { // Go to previously shown page if settings page was currently shown 
        int fallbackPageIndex = 0;
        if (v_previousPageIndex >= 0 && v_previousPageIndex < c_loadedPageGroups.size()) {
        	ShapeGroup prevGroup = c_loadedPageGroups.get(v_previousPageIndex);
            if (prevGroup != gr_customGCSolarfarmSettings && prevGroup != gr_customGCWindfarmSettings && prevGroup != gr_customGCGridBatterySettings) {
                fallbackPageIndex = v_previousPageIndex;
            }
        }
        f_goToPage(fallbackPageIndex);
    }
}
/*ALCODEEND*/}

