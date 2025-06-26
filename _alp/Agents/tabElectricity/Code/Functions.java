double f_setPVOnLand(double hectare)
{/*ALCODESTART::1722256117103*/
// TODO: Change to work for multiple solar fields in one model.
// to do so it should probably first calculate the total installed pv in all solar fields

// TODO: make this work nicer with the new pause function (when setting capacity to 0 pause again?)
for ( GCEnergyProduction GCEP : zero_Interface.energyModel.EnergyProductionSites) {
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
int nbHousesWithPV = count(houses, x -> x.v_liveAssetsMetaData.hasPV == true);
int nbHousesWithPVGoal = roundToInt(PV_pct / 100.0 * nbHouses);

while ( nbHousesWithPVGoal < nbHousesWithPV ) { // remove excess PV systems
	GCHouse house = findFirst(houses, x -> x.v_liveAssetsMetaData.hasPV == true);	
	J_EA pvAsset = findFirst(house.c_productionAssets, p -> p.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC );
	if (pvAsset != null) {
		pvAsset.removeEnergyAsset();
		houses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.add(0, house);
		nbHousesWithPV --; 
	}
	else {
		traceln(" cant find PV asset in house that should have PV asset in f_setPVHouses (Interface)");
	}
}

while ( nbHousesWithPVGoal > nbHousesWithPV ) {
	GCHouse house = findFirst(houses, x -> x.v_liveAssetsMetaData.hasPV == false);
	if (house == null){
		traceln("No gridconnection without PV panels found! Current PVsystems count: %s", nbHousesWithPV);
		break;
	}
	else {
		String assetName = "Rooftop PV";
		double capacityHeat_kW = 0.0;
		double yearlyProductionHydrogen_kWh = 0.0;
		double yearlyProductionMethane_kWh = 0.0;
		double installedPVCapacity_kW = uniform(3,6);

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

double f_setWindTurbines(double AllocatedWindPower_MW)
{/*ALCODESTART::1722256248965*/
// TODO: Change to work for multiple wind farms in one model.
// to do so it should probably first calculate the total installed wind power in all wind farms

// TODO: make this work nicer with the new pause function (when setting capacity to 0 pause again?)

for ( GCEnergyProduction GCEP : zero_Interface.energyModel.EnergyProductionSites) {
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
	if ( gc.v_liveAssetsMetaData.hasPV ) {
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
		if ( company.v_liveAssetsMetaData.hasPV ) {
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
if ( gc.v_liveAssetsMetaData.hasPV ) {
	// Add the capacity to the existing asset
	J_EAProduction pvAsset = findFirst(gc.c_productionAssets, p -> p.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC);
	if ( pvAsset == null ) {
		throw new RuntimeException("Could not find photovoltaic asset in GridConnection: " + gc.p_ownerID + ", even though hasPV is True.");
	}
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


//Color the buildings when in solar color mode
if ( zero_Interface.rb_buildingColors.getValue() == 2 ) {
	for ( GIS_Object building : gc.c_connectedGISObjects ) {
		zero_Interface.f_styleAreas(building);
	}
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

double f_setPVSystemHouses1(List<GCHouse> gcList,double target_pct)
{/*ALCODESTART::1747825874398*/
Pair<Double, Double> pair = f_getPVSystemPercentage( new ArrayList<GridConnection>(gcList));

double remaining_kWp = target_pct / 100 * pair.getSecond()  - pair.getFirst();
double averageEffectivePV_kWppm2 = zero_Interface.energyModel.avgc_data.p_avgRatioRoofPotentialPV * zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2;

if ( remaining_kWp > 0 ) {
	// add more PV
	for ( GCHouse house : new ArrayList<GCHouse>(zero_Interface.c_orderedPVSystemsHouses) ) {
		double remainingPotential_kWp = min( remaining_kWp, house.p_roofSurfaceArea_m2 * averageEffectivePV_kWppm2 - house.v_liveAssetsMetaData.totalInstalledPVPower_kW );
		if ( remainingPotential_kWp > 0 ) {
			remaining_kWp -= remainingPotential_kWp;
			f_addPVSystem( house, remainingPotential_kWp );
		}
		
		if ( remaining_kWp <= 0 ) {
			zero_Interface.f_resetSettings();		
			return;
		}
	}
}
else {
	// remove pv
	for ( GCHouse house : new ArrayList<GCHouse>(zero_Interface.c_orderedPVSystemsHouses) ) {
		if ( house.v_liveAssetsMetaData.hasPV ) {
			remaining_kWp += house.v_liveAssetsMetaData.totalInstalledPVPower_kW;
			f_removePVSystem( house );
		}
		if ( remaining_kWp >= 0 ) {
			// removed slightly too much pv
			f_addPVSystem( house, remaining_kWp );
			zero_Interface.f_resetSettings();			
			return;
		}
	}
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setResidentialBatteries(double homeBatteries_pct)
{/*ALCODESTART::1750063382310*/
// Setting houseBatteries
double nbHouseBatteries = count(zero_Interface.energyModel.Houses, h -> h.p_batteryAsset != null); //f_getEnergyAssets(), p -> p.energyAssetType == OL_EnergyAssetType.STORAGE_ELECTRIC && p.getParentAgent() instanceof GCHouse);
double nbHousesWithPV = count(zero_Interface.energyModel.Houses, x -> x.v_liveAssetsMetaData.hasPV == true); //count(energyModel.f_getGridConnections(), p->p instanceof GCHouse);
double nbHousesWithBatteryGoal = roundToInt(nbHousesWithPV * homeBatteries_pct / 100);

if( nbHousesWithPV > 0 ){
	while ( nbHouseBatteries > nbHousesWithBatteryGoal ) {
		GCHouse house = findFirst(zero_Interface.energyModel.Houses, p -> p.p_batteryAsset != null );
		house.p_batteryAsset.removeEnergyAsset();
		house.p_batteryOperationMode = OL_BatteryOperationMode.BALANCE; // reset to default
		nbHouseBatteries--;
	}
	while ( nbHouseBatteries < nbHousesWithBatteryGoal) {
		GCHouse house = findFirst(zero_Interface.energyModel.Houses, p -> p.p_batteryAsset == null && p.v_liveAssetsMetaData.hasPV == true);
		
		double batteryStorageCapacity_kWh = 15;
		double batteryCapacity_kW = batteryStorageCapacity_kWh / zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power;
		double batteryStateOfCharge = 0.5;

		new J_EAStorageElectric(house, batteryCapacity_kW, batteryStorageCapacity_kWh, batteryStateOfCharge, zero_Interface.energyModel.p_timeStep_h );
		house.p_batteryOperationMode = OL_BatteryOperationMode.HOUSEHOLD_LOAD;
		nbHouseBatteries++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setGridBatteries(double storageCapacity_kWh)
{/*ALCODESTART::1750063382312*/
for ( GCGridBattery battery : zero_Interface.energyModel.GridBatteries) { // Should this be a gridbattery under each gridnode? loader issue?
	for(J_EAStorage j_ea : battery.c_storageAssets) {
		J_EAStorageElectric batteryAsset = ((J_EAStorageElectric)j_ea);
		if (!battery.v_isActive) {
			battery.f_setActive(true);
		}
		double capacity_kW = storageCapacity_kWh / 2;
		batteryAsset.setCapacityElectric_kW( capacity_kW );
		batteryAsset.setStorageCapacity_kWh( storageCapacity_kWh );
		battery.v_liveConnectionMetaData.physicalCapacity_kW = capacity_kW;
		battery.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = capacity_kW;
		battery.v_liveConnectionMetaData.contractedFeedinCapacity_kW = capacity_kW;
	}
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setPublicChargingStationsScale(double sliderValue)
{/*ALCODESTART::1750063382314*/
ArrayList<GCPublicCharger> activeChargers = new ArrayList<>();
for ( GCPublicCharger charger : zero_Interface.c_activePublicChargers){
	activeChargers.add(charger);
}
for ( GCPublicCharger charger : activeChargers){
	if ( charger.p_nbOfChargers > sliderValue){
		f_setChargerInactive( charger );
	}
}
ArrayList<GCPublicCharger> inactiveChargers = new ArrayList<>();
for ( GCPublicCharger charger : zero_Interface.c_inactivePublicChargers){
	inactiveChargers.add(charger);
}
for ( GCPublicCharger charger : inactiveChargers){
	if( charger.p_nbOfChargers <= sliderValue ){
		f_setChargerActive( charger );
	}
}

/*
for ( GridNode node : zero_Interface.energyModel.f_getGridNodesNotTopLevel()){
	traceln( "Node  " + node.p_gridNodeID);
	int i = 0;
	for( GridConnection gc : node.f_getConnectedGridConnections() ){
		if ( gc instanceof GCPublicCharger){
			GCPublicCharger charger =((GCPublicCharger)gc);
			if( charger.v_isActiveCharger){
				i ++;
			}
		}
	}
	traceln( "Number of chargers:  " + i);
}
*/

traceln( "New number of active chargers: " + v_currentNbChargers);
traceln( "New number of inactive chargers: " + zero_Interface.c_inactivePublicChargers.size());

zero_Interface.f_resetSettings();
//f_setDieselVehiclesAtPublicParkingHouses();
/*ALCODEEND*/}

List<Double> f_getChargingProfile(GCPublicCharger charger)
{/*ALCODESTART::1750063382316*/
String profileName = charger.p_parentNodeElectricID.toLowerCase();
List<Double> quarterlyEnergyDemand_kWh;

if (chargingMethodResidentialArea.getValue() == 0){
	profileName = profileName + f_getProfileNameEVAddition();
	quarterlyEnergyDemand_kWh = selectValues(double.class, "SELECT " + profileName + " FROM charging_profiles_unc;");
}
else if (chargingMethodResidentialArea.getValue() == 1){
	profileName = profileName + f_getProfileNameEVAddition() + f_getProfileNameWPAddition();
	//traceln( profileName);
	quarterlyEnergyDemand_kWh = selectValues(double.class, "SELECT " + profileName + " FROM charging_profiles_v1g;");
}
else {
	profileName = profileName + f_getProfileNameEVAddition() + f_getProfileNameWPAddition();
	//traceln ( profileName);
	quarterlyEnergyDemand_kWh = selectValues(double.class, "SELECT " + profileName + " FROM charging_profiles_v2g;");
}

return quarterlyEnergyDemand_kWh;
/*ALCODEEND*/}

String f_getProfileNameEVAddition()
{/*ALCODESTART::1750063382318*/
String string = "_";

double EVShare = sl_publicEVsResidentialArea_pct.getValue();
if(  EVShare < 0.49 ){
	string += "ev25";
}
else if  (EVShare < 0.74 ){
	string += "ev50";
}
else if ( EVShare < 0.99 ){
	string += "ev75";
}
else {
	string += "ev100";
}

return string;
/*ALCODEEND*/}

double f_setChargerInactive(GCPublicCharger charger)
{/*ALCODESTART::1750063382320*/
zero_Interface.c_activePublicChargers.remove(charger); 
charger.c_EvAssets.remove(charger.c_profileAssets.get(0));
charger.c_profileAssets.get(0).removeEnergyAsset(); //get rid of the charging profile
charger.v_isActiveCharger = false;
zero_Interface.c_inactivePublicChargers.add(charger); //add the charger to the other list
charger.c_connectedGISObjects.get(0).gisRegion.setVisible(false);
v_currentNbChargers --;

/*ALCODEEND*/}

double f_setChargerActive(GCPublicCharger charger)
{/*ALCODESTART::1750063382322*/
zero_Interface.c_inactivePublicChargers.remove( charger );
J_EAProfile profile = new J_EAProfile(charger, OL_EnergyCarriers.ELECTRICITY, null, OL_ProfileAssetType.CHARGING, zero_Interface.energyModel.p_timeStep_h);		
profile.energyAssetName = "charging profile";		
List<Double> quarterlyEnergyDemand_kWh = f_getChargingProfile(charger);
profile.a_energyProfile_kWh = quarterlyEnergyDemand_kWh.stream().mapToDouble(d -> max(0,d)).map( d -> d / 4).toArray();
charger.v_isActiveCharger = true;
charger.c_connectedGISObjects.get(0).gisRegion.setVisible(true);
zero_Interface.c_activePublicChargers.add(charger);
v_currentNbChargers ++;
/*ALCODEEND*/}

double f_setElectricCooking(double electricCookingGoal_pct)
{/*ALCODESTART::1750063382324*/
int nbHousesWithElectricCooking = findAll(zero_Interface.energyModel.Houses, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC).size();
int nbHousesWithElectricCookingGoal = roundToInt(electricCookingGoal_pct / 100 * zero_Interface.energyModel.Houses.size());


while ( nbHousesWithElectricCooking > nbHousesWithElectricCookingGoal ) { // remove excess cooking systems
	GCHouse house = randomWhere(zero_Interface.energyModel.Houses, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC);		
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
	GCHouse house = randomWhere(zero_Interface.energyModel.Houses, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.GAS);
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

String f_getProfileNameWPAddition()
{/*ALCODESTART::1750063382328*/
String string = "_";

int WPshare = uI_Tabs.pop_tabHeating.get(0).sl_householdElectricHeatPumpResidentialArea_pct.getIntValue();

if(  WPshare < 33 ){
	string += "hp0";
}
else if  (WPshare < 66 ){
	string += "hp50";
}
else {
	string += "hp100";
}


return string;
/*ALCODEEND*/}

double f_setPublicChargingStations(double publicChargers_pct)
{/*ALCODESTART::1750063382330*/
int totalNbChargers = zero_Interface.c_orderedPublicChargers.size();
int currentNbChargers = count(zero_Interface.c_orderedPublicChargers, x -> x.v_isActive);
int nbChargersGoal = roundToInt(publicChargers_pct / 100 * totalNbChargers) ;

// TODO: for now we assume that a public charger supports two EVs. Just to implement the code that removes diesel vehicles.
// We will need to decide if we want to make this a fixed number, or have it depend on the number of chargers / diesel vehicles.
int amountOfVehiclesPerCharger = zero_Interface.energyModel.avgc_data.p_avgEVsPerPublicCharger;

while ( currentNbChargers > nbChargersGoal ) {
	GCPublicCharger gc = findFirst(zero_Interface.c_orderedPublicChargers, x -> x.v_isActive);
	if (gc != null) {
		gc.f_setActive(false);
		zero_Interface.c_orderedPublicChargers.remove(gc);
		zero_Interface.c_orderedPublicChargers.add(0, gc);
		currentNbChargers--;
		
		List<J_EADieselVehicle> cars = new ArrayList<>(zero_Interface.c_orderedActiveVehiclesPublicParking.subList(0, amountOfVehiclesPerCharger));
		for (J_EADieselVehicle car : cars) {
			zero_Interface.c_orderedActiveVehiclesPublicParking.remove(car);
			zero_Interface.c_orderedNonActiveVehiclesPublicParking.add(0, car);
			car.removeEnergyAsset();
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
		
		List<J_EADieselVehicle> cars = new ArrayList<>(zero_Interface.c_orderedNonActiveVehiclesPublicParking.subList(0, amountOfVehiclesPerCharger));
		for (J_EADieselVehicle car : cars) {
			zero_Interface.c_orderedNonActiveVehiclesPublicParking.remove(car);
			zero_Interface.c_orderedActiveVehiclesPublicParking.add(0, car);
			car.reRegisterEnergyAsset();
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
	J_EACharger j_ea = findFirst(zero_Interface.c_orderedV1GChargers, x -> !x.V1GCapable);
	j_ea.V1GCapable = true;
	currentNbChargers++;
	zero_Interface.c_orderedV1GChargers.remove(j_ea);
	zero_Interface.c_orderedV1GChargers.add(0, j_ea);
	
}
while (currentNbChargers > nbChargersGoal) {
	J_EACharger j_ea = findFirst(zero_Interface.c_orderedV1GChargers, x -> x.V1GCapable);
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
	J_EACharger j_ea = findFirst(zero_Interface.c_orderedV2GChargers, x -> !x.V2GCapable);
	j_ea.V2GCapable = true;
	currentNbChargers++;
	zero_Interface.c_orderedV2GChargers.remove(j_ea);
	zero_Interface.c_orderedV2GChargers.add(0, j_ea);
	
}
while (currentNbChargers > nbChargersGoal) {
	J_EACharger j_ea = findFirst(zero_Interface.c_orderedV2GChargers, x -> x.V2GCapable);
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

double f_setVehiclesPrivateParking(double privateParking_pct)
{/*ALCODESTART::1750328750011*/
int nbHousesWithEV = count(zero_Interface.energyModel.Houses, x -> x.p_eigenOprit && x.p_householdEV != null);
int desiredNbOfHousesWithEV = roundToInt(privateParking_pct / 100 * zero_Interface.c_orderedVehiclesPrivateParking.size());

// we scale the consumption instead of getting the diesel/EV parameter from avgc data to represent the 'size' of the car
double ratioEVToDieselConsumption = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionCar_kWhpkm / zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionCar_kWhpkm;

while ( nbHousesWithEV > desiredNbOfHousesWithEV){
	J_EAVehicle j_ea = findFirst( zero_Interface.c_orderedVehiclesPrivateParking, h -> h instanceof J_EAEV);
	if (j_ea.vehicleScaling != 1) {
		throw new RuntimeException("f_setVehiclesPrivateParking does not work with vehicles that have a vehicleScaling other than 1");
	}
	J_ActivityTrackerTrips triptracker = j_ea.tripTracker;
	double energyConsumption_kWhpkm = j_ea.getEnergyConsumption_kWhpkm() / ratioEVToDieselConsumption; 
	j_ea.removeEnergyAsset();
	zero_Interface.c_orderedVehiclesPrivateParking.remove(j_ea);
	J_EADieselVehicle dieselCar = new J_EADieselVehicle(j_ea.getParentAgent(), energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, 1, OL_EnergyAssetType.DIESEL_VEHICLE, triptracker);
	zero_Interface.c_orderedVehiclesPrivateParking.add(dieselCar);
	nbHousesWithEV --;
}
while ( nbHousesWithEV < desiredNbOfHousesWithEV){
	J_EAVehicle j_ea = findFirst( zero_Interface.c_orderedVehiclesPrivateParking, h -> h instanceof J_EADieselVehicle);
	if (j_ea.vehicleScaling != 1) {
		throw new RuntimeException("f_setVehiclesPrivateParking does not work with vehicles that have a vehicleScaling other than 1");
	}
	J_ActivityTrackerTrips triptracker = j_ea.tripTracker;
	double energyConsumption_kWhpkm = j_ea.getEnergyConsumption_kWhpkm() * ratioEVToDieselConsumption;
	j_ea.removeEnergyAsset();	
	zero_Interface.c_orderedVehiclesPrivateParking.remove(j_ea);
	double capacityElectricity_kW = randomTrue(0.6) ? randomTrue(0.4) ? 3.2 : 5.6 : 11.0;
	double storageCapacity_kWh = uniform_discr(65,90);
	J_EAEV ev = new J_EAEV(j_ea.getParentAgent(), capacityElectricity_kW, storageCapacity_kWh, 1, zero_Interface.energyModel.p_timeStep_h, energyConsumption_kWhpkm, 1, OL_EnergyAssetType.ELECTRIC_VEHICLE, triptracker);	
	zero_Interface.c_orderedVehiclesPrivateParking.add(ev);
	nbHousesWithEV++;
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

