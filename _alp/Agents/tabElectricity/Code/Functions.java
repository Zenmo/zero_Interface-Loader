double f_setPVOnLand(double hectare,List<GCEnergyProduction> gcListProduction)
{/*ALCODESTART::1722256117103*/
// TODO: Change to work for multiple solar fields in one model.
// to do so it should probably first calculate the total installed pv in all solar fields
for ( GCEnergyProduction GCEP : gcListProduction) {
	for(J_EAProduction j_ea : GCEP.c_productionAssets) {
		if (j_ea.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC && GCEP.p_isSliderGC) {
			if (!GCEP.v_isActive) {
				GCEP.f_setActive(true);
			}
			
			double solarFieldPower = (double)roundToInt(hectare * zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha);
			j_ea.setCapacityElectric_kW(solarFieldPower);
			GCEP.v_liveConnectionMetaData.physicalCapacity_kW = solarFieldPower;
			GCEP.v_liveConnectionMetaData.contractedFeedinCapacity_kW = solarFieldPower;
			
			if(hectare == 0){
				GCEP.f_setActive(false);
			}
			
			break;
		}
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
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
	J_EA pvAsset = findFirst(house.c_productionAssets, p -> p.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC );
	if (pvAsset != null) {
		pvAsset.removeEnergyAsset();
		houses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.add(0, house);
		nbHousesWithPV --; 
		
		if(house.p_batteryAsset != null ){
			house.p_batteryAsset.removeEnergyAsset();
			house.p_batteryAlgorithm = null;
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
		J_EAProduction productionAsset = new J_EAProduction ( house, OL_EnergyAssetType.PHOTOVOLTAIC, assetName, OL_EnergyCarriers.ELECTRICITY, installedPVCapacity_kW, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.pp_PVProduction35DegSouth_fr );
		houses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.add(0, house);
		nbHousesWithPV ++;	
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setWindTurbines(double AllocatedWindPower_MW,List<GCEnergyProduction> gcListProduction)
{/*ALCODESTART::1722256248965*/
// TODO: Change to work for multiple wind farms in one model.
// to do so it should probably first calculate the total installed wind power in all wind farms

for ( GCEnergyProduction GCEP : gcListProduction) {
	for(J_EAProduction j_ea : GCEP.c_productionAssets) {
		if (j_ea.getEAType() == OL_EnergyAssetType.WINDMILL && GCEP.p_isSliderGC) {
			if (!GCEP.v_isActive) {
				GCEP.f_setActive(true);
			}
			j_ea.setCapacityElectric_kW(roundToInt(1000*AllocatedWindPower_MW));
			GCEP.v_liveConnectionMetaData.physicalCapacity_kW = (double)roundToInt(1000*AllocatedWindPower_MW);
			GCEP.v_liveConnectionMetaData.contractedFeedinCapacity_kW = (double)roundToInt(1000*AllocatedWindPower_MW);
			
			if(AllocatedWindPower_MW == 0){
				GCEP.f_setActive(false);
			}
			break;
		}
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
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
		if (j_ea.energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
			j_ea.resetEnergyProfile();
			j_ea.scaleEnergyProfile( scalingFactor );
		}
	}
	
	// Update Company UI
	if (zero_Interface.c_companyUIs.size()>0){
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
			companyUI.sl_electricityDemandCompanyReduction.setValue(demandReduction_pct, false);
		}
	}
}

// Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_getCurrentPVOnLandAndWindturbineValues()
{/*ALCODESTART::1745483988251*/
for(GCEnergyProduction GCProd : zero_Interface.energyModel.EnergyProductionSites){
	if(!GCProd.p_isSliderGC && GCProd.v_isActive){
		for(J_EAProduction ea : GCProd.c_productionAssets){
			if(ea.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC){
				p_currentPVOnLand_ha += ea.getCapacityElectric_kW()/zero_Interface.energyModel.avgc_data.p_avgSolarFieldPower_kWppha;
			}
			else if(ea.energyAssetType == OL_EnergyAssetType.WINDMILL){
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
			double PVAtStartup_kWp = 0;
			if (zero_Interface.c_companyUIs.size() > 0) {
				PVAtStartup_kWp = zero_Interface.c_scenarioMap_Current.get(company).getCurrentPV_kW();
			}
			if (PVAtStartup_kWp != 0) {
				f_addPVSystem( company, PVAtStartup_kWp );
				remaining_kWp -= PVAtStartup_kWp;
			}
		}
		if ( remaining_kWp >= 0 ) {
			// removed slightly too much pv
			f_addPVSystem( company, remaining_kWp );
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
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_addPVSystem(GridConnection gc,double capacity_kWp)
{/*ALCODESTART::1747306690517*/
J_EAProduction pvAsset = findFirst(gc.c_productionAssets, p -> p.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC);
if (pvAsset != null) {
	capacity_kWp += pvAsset.getCapacityElectric_kW();
	pvAsset.setCapacityElectric_kW( capacity_kWp );
}
else {
	// Create a new asset
	OL_EnergyAssetType assetType = OL_EnergyAssetType.PHOTOVOLTAIC;
	String assetName = "Rooftop PV";
	double capacityHeat_kW = 0.0;
	double yearlyProductionMethane_kWh = 0.0;
	double yearlyProductionHydrogen_kWh = 0.0;
	double outputTemperature_degC = 0.0;
	
	J_EAProduction productionAsset = new J_EAProduction ( gc, assetType, assetName, OL_EnergyCarriers.ELECTRICITY, capacity_kWp, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.pp_PVProduction35DegSouth_fr );
}

// Update the ordered collection
if ( gc instanceof GCHouse ) {
	zero_Interface.c_orderedPVSystemsHouses.remove(gc);
	zero_Interface.c_orderedPVSystemsHouses.add(0, (GCHouse)gc);	
}
else if ( gc instanceof GCUtility ) {
	zero_Interface.c_orderedPVSystemsCompanies.remove(gc);
	zero_Interface.c_orderedPVSystemsCompanies.add(0, (GCUtility)gc);
	// update company UI
	if ( zero_Interface.c_companyUIs.size() > 0 ) {
		if ( gc.p_owner != null ) {
			UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
			companyUI.b_runningMainInterfaceSlider = true;
			if(companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) != gc){
				int i = indexOf(companyUI.c_ownedGridConnections.stream().toArray(), gc);
				companyUI.GCnr_selection.setValue(i, true);
			}
			companyUI.b_runningMainInterfaceSlider = false;	
			
			companyUI.sl_rooftopPVCompany.setValue(roundToInt(capacity_kWp), false);
			companyUI.v_defaultPVSlider = roundToInt(capacity_kWp);
		}
	}
}
else {
	throw new RuntimeException("Unknown GridConnection type passed to f_addPVSystem.");
}
/*ALCODEEND*/}

double f_removePVSystem(GridConnection gc)
{/*ALCODESTART::1747306699629*/
J_EAProduction pvAsset = findFirst(gc.c_productionAssets, p -> p.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC);
if ( pvAsset != null ) {
	pvAsset.removeEnergyAsset();

	if ( gc instanceof GCHouse ) {
		zero_Interface.c_orderedPVSystemsHouses.remove(gc);
		zero_Interface.c_orderedPVSystemsHouses.add(0, (GCHouse)gc);	
	}
	else if ( gc instanceof GCUtility ) {
		zero_Interface.c_orderedPVSystemsCompanies.remove(gc);
		zero_Interface.c_orderedPVSystemsCompanies.add(0, (GCUtility)gc);
		if ( zero_Interface.c_companyUIs.size() > 0 ) {
			if ( gc.p_owner != null ) {
				UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
				companyUI.b_runningMainInterfaceSlider = true;
				if(companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) != gc){
					int i = indexOf(companyUI.c_ownedGridConnections.stream().toArray(), gc);
					companyUI.GCnr_selection.setValue(i, true);
				}
				companyUI.b_runningMainInterfaceSlider = false;	
				
				companyUI.sl_rooftopPVCompany.setValue(0, false);
				companyUI.v_defaultPVSlider = roundToInt(0);
			}
		}
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
		house.p_batteryAlgorithm = null;
		nbHouseBatteries--;
	}
	while ( nbHouseBatteries < nbHousesWithBatteryGoal) {
		GCHouse house = findFirst(gcListHouses, p -> p.p_batteryAsset == null && p.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW));
		
		double batteryStorageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgRatioHouseBatteryStorageCapacity_v_PVPower*house.v_liveAssetsMetaData.totalInstalledPVPower_kW;
		double batteryCapacity_kW = batteryStorageCapacity_kWh / zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power;
		double batteryStateOfCharge = 0.5;

		new J_EAStorageElectric(house, batteryCapacity_kW, batteryStorageCapacity_kWh, batteryStateOfCharge, zero_Interface.energyModel.p_timeStep_h );
		house.p_batteryAlgorithm = new J_BatteryManagementSelfConsumption( house );
		nbHouseBatteries++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setGridBatteries_residential(double storageCapacity_kWh,List<GCGridBattery> gcListGridBatteries)
{/*ALCODESTART::1750063382312*/
for ( GCGridBattery battery : gcListGridBatteries) {
	if(battery.p_isSliderGC){
		for(J_EAStorage j_ea : battery.c_storageAssets) {
			J_EAStorageElectric batteryAsset = ((J_EAStorageElectric)j_ea);
			if (!battery.v_isActive) {
				battery.f_setActive(true);
			}
			double capacity_kW = storageCapacity_kWh / zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power;
			batteryAsset.setCapacityElectric_kW( capacity_kW );
			batteryAsset.setStorageCapacity_kWh( storageCapacity_kWh );
			battery.v_liveConnectionMetaData.physicalCapacity_kW = capacity_kW;
			battery.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = capacity_kW;
			battery.v_liveConnectionMetaData.contractedFeedinCapacity_kW = capacity_kW;
		}
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setElectricCooking(List<GCHouse> gcListHouses,double electricCookingGoal_pct)
{/*ALCODESTART::1750063382324*/
int nbHousesWithElectricCooking = findAll(gcListHouses, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC).size();
int nbHousesWithElectricCookingGoal = roundToInt(electricCookingGoal_pct / 100 * gcListHouses.size());


while ( nbHousesWithElectricCooking > nbHousesWithElectricCookingGoal ) { // remove excess cooking systems
	GCHouse house = randomWhere(gcListHouses, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC);		
	J_EAConsumption cookingAsset = findFirst(house.c_consumptionAssets, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB );
	if (cookingAsset != null) {
		double yearlyCookingDemand_kWh = cookingAsset.yearlyDemand_kWh;
		cookingAsset.removeEnergyAsset();
   		
		new J_EAConsumption(house, OL_EnergyAssetType.GAS_PIT, "default_house_cooking_demand_fr", yearlyCookingDemand_kWh, OL_EnergyCarriers.METHANE, zero_Interface.energyModel.p_timeStep_h, null);
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
		J_EAConsumption cookingAsset = findFirst(house.c_consumptionAssets, p -> p.energyAssetType == OL_EnergyAssetType.GAS_PIT );
		if (cookingAsset != null) {
			double yearlyCookingDemand_kWh = cookingAsset.yearlyDemand_kWh;
			cookingAsset.removeEnergyAsset();
	
			new J_EAConsumption(house, OL_EnergyAssetType.ELECTRIC_HOB, "default_house_cooking_demand_fr", yearlyCookingDemand_kWh, OL_EnergyCarriers.ELECTRICITY, zero_Interface.energyModel.p_timeStep_h, null);
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
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setPublicChargingStations(double publicChargers_pct)
{/*ALCODESTART::1750063382330*/
int totalNbChargers = zero_Interface.c_orderedPublicChargers.size();
int currentNbChargers = count(zero_Interface.c_orderedPublicChargers, x -> x.v_isActive);
int nbChargersGoal = roundToInt(publicChargers_pct / 100 * totalNbChargers) ;

while ( currentNbChargers > nbChargersGoal ) {
	GCPublicCharger gc = findFirst(zero_Interface.c_orderedPublicChargers, x -> x.v_isActive);
	if (gc != null) {
		gc.f_setActive(false);
		zero_Interface.c_orderedPublicChargers.remove(gc);
		zero_Interface.c_orderedPublicChargers.add(0, gc);
		currentNbChargers--;
		
		for (J_EADieselVehicle car : zero_Interface.c_mappingOfVehiclesPerCharger.get(gc)) {
			car.reRegisterEnergyAsset();
		}
	}
	else {
		throw new RuntimeException("Charger slider error: there are not sufficient chargers to remove");
	}
}
while ( currentNbChargers < nbChargersGoal){
	GCPublicCharger gc = findFirst(zero_Interface.c_orderedPublicChargers, x -> !x.v_isActive);
	if (gc != null) {
		gc.f_setActive(true);
		zero_Interface.c_orderedPublicChargers.remove(gc);
		zero_Interface.c_orderedPublicChargers.add(0, gc);
		currentNbChargers++;
		
		for (J_EADieselVehicle car : zero_Interface.c_mappingOfVehiclesPerCharger.get(gc)) {
			J_ActivityTrackerTrips tripTracker = car.getTripTracker(); //Needed, as triptracker is removed when removeEnergyAsset is called.
			car.removeEnergyAsset();
			car.setTripTracker(tripTracker);//Re-set the triptracker again, for storing.
		}
	}
	else {
		throw new RuntimeException("Charger slider error: there are no more chargers to add");
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();

/*ALCODEEND*/}

double f_setV1GChargerCapabilities(double goal_pct)
{/*ALCODESTART::1750259219309*/
int totalNbChargers = zero_Interface.c_orderedV1GChargers.size();
int currentNbChargers = count(zero_Interface.c_orderedV1GChargers, x -> x.V1GCapable);
int nbChargersGoal = roundToInt(goal_pct / 100.0 * totalNbChargers);

while (currentNbChargers < nbChargersGoal) {
	J_EAChargePoint j_ea = findFirst(zero_Interface.c_orderedV1GChargers, x -> !x.V1GCapable);
	j_ea.V1GCapable = true;
	currentNbChargers++;
	zero_Interface.c_orderedV1GChargers.remove(j_ea);
	zero_Interface.c_orderedV1GChargers.add(0, j_ea);
	
}
while (currentNbChargers > nbChargersGoal) {
	J_EAChargePoint j_ea = findFirst(zero_Interface.c_orderedV1GChargers, x -> x.V1GCapable);
	j_ea.V1GCapable = false;
	currentNbChargers--;
	zero_Interface.c_orderedV1GChargers.remove(j_ea);
	zero_Interface.c_orderedV1GChargers.add(0, j_ea);
}

// Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setV2GChargerCapabilities(double goal_pct)
{/*ALCODESTART::1750259468109*/
int totalNbChargers = zero_Interface.c_orderedV2GChargers.size();
int currentNbChargers = count(zero_Interface.c_orderedV2GChargers, x -> x.V2GCapable);
int nbChargersGoal = roundToInt(goal_pct / 100.0 * totalNbChargers);

while (currentNbChargers < nbChargersGoal) {
	J_EAChargePoint j_ea = findFirst(zero_Interface.c_orderedV2GChargers, x -> !x.V2GCapable);
	j_ea.V2GCapable = true;
	currentNbChargers++;
	zero_Interface.c_orderedV2GChargers.remove(j_ea);
	zero_Interface.c_orderedV2GChargers.add(0, j_ea);
	
}
while (currentNbChargers > nbChargersGoal) {
	J_EAChargePoint j_ea = findFirst(zero_Interface.c_orderedV2GChargers, x -> x.V2GCapable);
	j_ea.V2GCapable = false;
	currentNbChargers--;
	zero_Interface.c_orderedV2GChargers.remove(j_ea);
	zero_Interface.c_orderedV2GChargers.add(0, j_ea);
}

// Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setDemandIncrease(List<GridConnection> gcList,double demandReduction_pct)
{/*ALCODESTART::1750326729005*/
f_setDemandReduction(gcList, -demandReduction_pct);
/*ALCODEEND*/}

double f_setVehiclesPrivateParking(List<GCHouse> gcListHouses,double privateParkingEV_pct)
{/*ALCODESTART::1750328750011*/
List<J_EAVehicle> gcListOrderedVehiclesPrivateParking = findAll( zero_Interface.c_orderedVehiclesPrivateParking, h -> gcListHouses.contains(h.getParentAgent()));

int nbOfPrivateParkedEV = (int)sum(findAll(gcListHouses, gc -> gc.p_eigenOprit), x -> x.c_electricVehicles.size());
int desiredNbOfPrivateParkedEV = roundToInt(privateParkingEV_pct / 100 * gcListOrderedVehiclesPrivateParking.size());


// we scale the consumption instead of getting the diesel/EV parameter from avgc data to represent the 'size' of the car
double ratioEVToDieselConsumption = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionCar_kWhpkm / zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionCar_kWhpkm;

while ( nbOfPrivateParkedEV > desiredNbOfPrivateParkedEV){
	J_EAVehicle j_ea = findFirst( gcListOrderedVehiclesPrivateParking, h -> h instanceof J_EAEV);
	if (j_ea.vehicleScaling != 1) {
		throw new RuntimeException("f_setVehiclesPrivateParking does not work with vehicles that have a vehicleScaling other than 1");
	}
	J_ActivityTrackerTrips triptracker = j_ea.tripTracker;
	double energyConsumption_kWhpkm = j_ea.getEnergyConsumption_kWhpkm() / ratioEVToDieselConsumption; 
	j_ea.removeEnergyAsset();
	gcListOrderedVehiclesPrivateParking.remove(j_ea);
	zero_Interface.c_orderedVehiclesPrivateParking.remove(j_ea);
	J_EADieselVehicle dieselCar = new J_EADieselVehicle(j_ea.getParentAgent(), energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, 1, OL_EnergyAssetType.DIESEL_VEHICLE, triptracker);
	gcListOrderedVehiclesPrivateParking.add(dieselCar);
	zero_Interface.c_orderedVehiclesPrivateParking.add(dieselCar);
	nbOfPrivateParkedEV --;
}
while ( nbOfPrivateParkedEV < desiredNbOfPrivateParkedEV){
	J_EAVehicle j_ea = findFirst( gcListOrderedVehiclesPrivateParking, h -> h instanceof J_EADieselVehicle);
	if (j_ea.vehicleScaling != 1) {
		throw new RuntimeException("f_setVehiclesPrivateParking does not work with vehicles that have a vehicleScaling other than 1");
	}
	J_ActivityTrackerTrips triptracker = j_ea.tripTracker;
	double energyConsumption_kWhpkm = j_ea.getEnergyConsumption_kWhpkm() * ratioEVToDieselConsumption;
	j_ea.removeEnergyAsset();
	gcListOrderedVehiclesPrivateParking.remove(j_ea);
	zero_Interface.c_orderedVehiclesPrivateParking.remove(j_ea);
	double capacityElectricity_kW = randomTrue(0.6) ? randomTrue(0.4) ? 3.2 : 5.6 : 11.0;
	double storageCapacity_kWh = uniform_discr(65,90);
	J_EAEV ev = new J_EAEV(j_ea.getParentAgent(), capacityElectricity_kW, storageCapacity_kWh, 1, zero_Interface.energyModel.p_timeStep_h, energyConsumption_kWhpkm, 1, OL_EnergyAssetType.ELECTRIC_VEHICLE, triptracker);	
	gcListOrderedVehiclesPrivateParking.add(ev);
	zero_Interface.c_orderedVehiclesPrivateParking.add(ev);
	nbOfPrivateParkedEV++;
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
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
//Companies
List<GCUtility> utilityGridConnections = uI_Tabs.f_getSliderGridConnections_utilities();

List<GridConnection> utilityGridConnections_GC = new ArrayList<>(utilityGridConnections);
Pair<Double, Double> pair = f_getPVSystemPercentage( utilityGridConnections_GC );
int PV_pct = roundToInt(100.0 * pair.getFirst() / pair.getSecond());
sl_rooftopPVCompanies_pct_Businesspark.setValue(PV_pct, false);

//Houses
List<GCHouse> houseGridConnections = uI_Tabs.f_getSliderGridConnections_houses();

List<GridConnection> houseGridConnections_GC = new ArrayList<>(utilityGridConnections);
pair = f_getPVSystemPercentage( houseGridConnections_GC );
PV_pct = roundToInt(100.0 * pair.getFirst() / pair.getSecond());
sl_rooftopPVHouses_pct.setValue(PV_pct, false);
/*ALCODEEND*/}

double f_updateElectricitySliders_residential()
{/*ALCODESTART::1754926103687*/
List<GCHouse> houseGridConnections = new ArrayList<>();
List<GCPublicCharger> chargerGridConnections = new ArrayList<>();
List<GCGridBattery> gridBatteryGridConnections = new ArrayList<>();

for (GridConnection GC : findAll(uI_Tabs.f_getSliderGridConnections_all(), gc -> gc.v_isActive)) {
   	if(GC instanceof GCHouse){
		houseGridConnections.add((GCHouse)GC);		
	}
	else if(GC instanceof GCPublicCharger){
		chargerGridConnections.add((GCPublicCharger)GC);		
	}
	else if(GC instanceof GCGridBattery){
		gridBatteryGridConnections.add((GCGridBattery)GC);		
	}
}
chargerGridConnections.addAll(uI_Tabs.f_getPausedSliderGridConnections_chargers());


int nbHouses = houseGridConnections.size();
int nbHousesWithPV = count(houseGridConnections, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW));
double pv_pct = 100.0 * nbHousesWithPV / nbHouses;
sl_householdPVResidentialArea_pct.setValue(pv_pct, false);

if ( nbHousesWithPV != 0 ) {
	int nbHousesWithHomeBattery = count(houseGridConnections, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW) && x.p_batteryAsset != null);
	double battery_pct = 100.0 * nbHousesWithHomeBattery / nbHousesWithPV;
	sl_householdBatteriesResidentialArea_pct.setValue(battery_pct, false);
}

int nbHousesWithElectricCooking = count(houseGridConnections, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC);
double cooking_pct = 100.0 * nbHousesWithElectricCooking / nbHouses;
sl_householdElectricCookingResidentialArea_pct.setValue(cooking_pct, false);

if (zero_Interface.c_orderedVehiclesPrivateParking.size() > 0) {
	int nbPrivateEVs = count(zero_Interface.c_orderedVehiclesPrivateParking, x -> x instanceof J_EAEV);
	double privateEVs_pct = 100.0 * nbPrivateEVs / zero_Interface.c_orderedVehiclesPrivateParking.size();
	sl_privateEVsResidentialArea_pct.setValue(privateEVs_pct, false);
}


//Chargers
int nbPublicChargers = chargerGridConnections.size();
int nbActivePublicChargers = count(chargerGridConnections, x -> x.v_isActive);
double activePublicChargers_pct = 100.0 * nbActivePublicChargers / nbPublicChargers;
sl_publicChargersResidentialArea_pct.setValue(activePublicChargers_pct, false);

int nbV1GChargers = count(zero_Interface.c_orderedV1GChargers, x -> chargerGridConnections.contains(x) && x.V1GCapable);
int nbV2GChargers =count(zero_Interface.c_orderedV2GChargers, x -> chargerGridConnections.contains(x) && x.V2GCapable);
double V1G_pct = 100.0 * nbV1GChargers / nbPublicChargers;
double V2G_pct = 100.0 * nbV2GChargers / nbPublicChargers;
sl_chargersThatSupportV1G_pct.setValue(V1G_pct, false);
sl_chargersThatSupportV2G_pct.setValue(V2G_pct, false);



//Gridbatteries
double averageNeighbourhoodBatterySize_kWh = 0;
for (GCGridBattery gc : gridBatteryGridConnections) {
	averageNeighbourhoodBatterySize_kWh += gc.p_batteryAsset.getStorageCapacity_kWh()/gridBatteryGridConnections.size();
	traceln("gc.p_batteryAsset.getStorageCapacity_kWh(): " + gc.p_batteryAsset.getStorageCapacity_kWh());
}
sl_gridBatteriesResidentialArea_kWh.setValue(averageNeighbourhoodBatterySize_kWh, false);

/*ALCODEEND*/}

double f_updateElectricitySliders_businesspark()
{/*ALCODESTART::1754926103689*/
// Rooftop PV SYSTEMS:
List<GridConnection> utilityGridConnections = new ArrayList<>(uI_Tabs.f_getSliderGridConnections_utilities());
Pair<Double, Double> pair = f_getPVSystemPercentage( utilityGridConnections );
int PV_pct = roundToInt(100.0 * pair.getFirst() / pair.getSecond());
sl_rooftopPVCompanies_pct_Businesspark.setValue(PV_pct, false);


/*ALCODEEND*/}

double f_updateElectricitySliders_custom()
{/*ALCODESTART::1754926103691*/
//If you have a custom tab, 
//override this function to make it update automatically
traceln("Forgot to override the update custom electricity sliders functionality");
/*ALCODEEND*/}

double f_setGridBatteries(double capacity_MWh,List<GCGridBattery> gcListGridBatteries)
{/*ALCODESTART::1754985710087*/
// TODO: make this work nicer with the new pause function (when setting capacity to 0 pause again?)

if ( gcListGridBatteries.size() > 0 ){	
	GCGridBattery GC = findFirst(gcListGridBatteries, GB -> GB.p_isSliderGC);
	if(GC == null){
		traceln("WARNING: no specified slider grid battery in the model: random grid battery selected");
		GC = zero_Interface.energyModel.GridBatteries.get(0);
	}

	if (!GC.v_isActive) {
		GC.f_setActive(true);
	}
	GC.p_batteryAsset.setStorageCapacity_kWh(1000*capacity_MWh);
	double capacityElectric_kW = 1000*capacity_MWh / zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power;
	GC.p_batteryAsset.setCapacityElectric_kW(capacityElectric_kW);
	GC.v_liveConnectionMetaData.physicalCapacity_kW = capacityElectric_kW;
	GC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = capacityElectric_kW;
	GC.v_liveConnectionMetaData.contractedFeedinCapacity_kW = capacityElectric_kW;
}
else {
	throw new IllegalStateException("Model does not contain any GCGridBattery agent");
}
/*ALCODEEND*/}

double f_setCurtailment(boolean activateCurtailment,List<GridConnection> gcList)
{/*ALCODESTART::1754986167346*/
for (GridConnection GC : gcList) {
	GC.v_enableCurtailment = activateCurtailment;
	
	if (zero_Interface.c_companyUIs.size()>0 && GC instanceof GCUtility){
		UI_company companyUI = zero_Interface.c_companyUIs.get(GC.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == GC) { // should also check the setting of selected GC
			companyUI.cb_curtailmentCompany.setSelected(activateCurtailment, false);
		}
	}
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

