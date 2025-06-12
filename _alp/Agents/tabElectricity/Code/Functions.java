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
//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

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
		double outputTemperature_degC = 0.0;
		double installedPVCapacity_kW = uniform(3,6);

		J_EAProduction productionAsset = new J_EAProduction ( house, OL_EnergyAssetType.PHOTOVOLTAIC, assetName, installedPVCapacity_kW, capacityHeat_kW, yearlyProductionMethane_kWh, yearlyProductionHydrogen_kWh, zero_Interface.energyModel.p_timeStep_h, outputTemperature_degC, zero_Interface.energyModel.pp_solarPVproduction );
		houses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.remove(house);
		zero_Interface.c_orderedPVSystemsHouses.add(0, house);
		nbHousesWithPV ++;	
	}
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setPublicChargingStations(int sliderValue)
{/*ALCODESTART::1722256239566*/
double desiredNbOfChargers = roundToInt(sliderValue * p_nbChargersInDatabase / 100.0) ;

if ( v_currentNbChargers > desiredNbOfChargers){
	while ( v_currentNbChargers > desiredNbOfChargers){
		GCPublicCharger charger = zero_Interface.c_activePublicChargers.get(0);
		zero_Interface.c_activePublicChargers.remove(0); 
		
		if( charger != null ){
			charger.c_profileAssets.get(0).removeEnergyAsset(); //get rid of the charging profile
			charger.v_isActiveCharger = false;
			zero_Interface.c_inactivePublicChargers.add(charger); //add the charger to the other list
			charger.c_connectedGISObjects.get(0).gisRegion.setVisible(false);
			v_currentNbChargers --;
		}
		else{
			traceln("Charger slider error: there are not sufficient chargers to remove");
			return;
		}
	}
}
else if ( v_currentNbChargers < desiredNbOfChargers ){
	while ( v_currentNbChargers < desiredNbOfChargers){
		int index = uniform_discr(1, zero_Interface.c_inactivePublicChargers.size())-1;
		GCPublicCharger charger = zero_Interface.c_inactivePublicChargers.get(index);
		zero_Interface.c_inactivePublicChargers.remove(index);
		
		if( charger != null ){
			String profileName = charger.p_chargingProfileName;
			J_EAProfile profile = new J_EAProfile(charger, OL_EnergyCarriers.ELECTRICITY, null, OL_ProfileAssetType.CHARGING, zero_Interface.energyModel.p_timeStep_h);		
			profile.energyAssetName = "charging profile";
			List<Double> quarterlyEnergyDemand_kWh = selectValues(double.class, "SELECT " + profileName + " FROM charging_profiles;");			
			profile.a_energyProfile_kWh = quarterlyEnergyDemand_kWh.stream().mapToDouble(d -> max(0,d)).map( d -> d / 4).toArray();
			charger.v_isActiveCharger = true;
			charger.c_connectedGISObjects.get(0).gisRegion.setVisible(true);
			zero_Interface.c_activePublicChargers.add(charger);
			v_currentNbChargers ++;
		}
		else{
			traceln("Charger slider error: there are no more chargers to add");
			return;
		}
	}	
}


zero_Interface.f_resetSettings();
//f_setDieselVehiclesAtPublicParkingHouses();
/*ALCODEEND*/}

double f_setDieselVehiclesAtPublicParkingHouses()
{/*ALCODESTART::1722256239578*/

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

double f_setResidentialBatteries(double homeBatteries_pct)
{/*ALCODESTART::1722256291599*/
// Setting houseBatteries
double nbHouseBatteries = count(zero_Interface.energyModel.Houses, h -> h.p_batteryAsset != null); //f_getEnergyAssets(), p -> p.energyAssetType == OL_EnergyAssetType.STORAGE_ELECTRIC && p.getParentAgent() instanceof GCHouse);
double nbHousesWithPV = count(zero_Interface.energyModel.Houses, x -> x.v_liveAssetsMetaData.hasPV == true); //count(energyModel.f_getGridConnections(), p->p instanceof GCHouse);
double batteryShare_pct = homeBatteries_pct;

if( nbHousesWithPV > 0 ){
	if( batteryShare_pct / 100.0 < nbHouseBatteries / nbHousesWithPV ){
		while ( batteryShare_pct / 100.0 < nbHouseBatteries / nbHousesWithPV) {
			GCHouse house = findFirst(zero_Interface.energyModel.Houses, p -> p.p_batteryAsset != null );
			house.p_batteryAsset.removeEnergyAsset();
			house.p_batteryOperationMode = OL_BatteryOperationMode.BALANCE; // reset to default
			nbHouseBatteries--;
		}
	}
	else {
		while ( batteryShare_pct / 100.0 > nbHouseBatteries / nbHousesWithPV) {
			GCHouse house = findFirst(zero_Interface.energyModel.Houses, p -> p.p_batteryAsset == null && p.v_liveAssetsMetaData.hasPV == true);
			
			double batteryStorageCapacity_kWh = 15;
			double batteryCapacity_kW = batteryStorageCapacity_kWh / zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power;
			double batteryStateOfCharge = 0.5;
					
			new J_EAStorageElectric(house, batteryCapacity_kW, batteryStorageCapacity_kWh, batteryStateOfCharge, zero_Interface.energyModel.p_timeStep_h );
			house.p_batteryOperationMode = OL_BatteryOperationMode.HOUSEHOLD_LOAD;
			nbHouseBatteries++;
		}
	}
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setEVsAtPrivateParkingHouses()
{/*ALCODESTART::1722261212790*/

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

double f_setGridBatteries(double AllocatedCapacity_kW)
{/*ALCODESTART::1724164287280*/
// TODO: make this work (copied from f_setWindTurbines)

for ( GCGridBattery battery : zero_Interface.energyModel.GridBatteries) {
	for(J_EAStorage j_ea : battery.c_storageAssets) {
		J_EAStorageElectric batteryAsset = ((J_EAStorageElectric)j_ea);
		if (!battery.v_isActive) {
			battery.f_setActive(true);
		}
		batteryAsset.setCapacityElectric_kW(AllocatedCapacity_kW);
		battery.v_liveConnectionMetaData.physicalCapacity_kW = AllocatedCapacity_kW;
		battery.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = AllocatedCapacity_kW;
		battery.v_liveConnectionMetaData.contractedFeedinCapacity_kW = AllocatedCapacity_kW;
		//break;
	}
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setPublicChargingStationsScale(double sliderValue)
{/*ALCODESTART::1725550668581*/
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

traceln( "New number of active chargers: " + v_currentNbChargers);
traceln( "New number of inactive chargers: " + zero_Interface.c_inactivePublicChargers.size());

zero_Interface.f_resetSettings();
//f_setDieselVehiclesAtPublicParkingHouses();
/*ALCODEEND*/}

List<Double> f_getChargingProfile(GCPublicCharger charger)
{/*ALCODESTART::1725553029166*/
String profileName = charger.p_parentNodeElectricID;
List<Double> quarterlyEnergyDemand_kWh;

if (chargingMethodScale.getValue() == 0){
	quarterlyEnergyDemand_kWh = selectValues(double.class, "SELECT " + profileName + " FROM charging_profiles_unc;");
}
else if (chargingMethodScale.getValue() == 1){
	profileName = profileName + f_getProfileNameAdditions();
	quarterlyEnergyDemand_kWh = selectValues(double.class, "SELECT " + profileName + " FROM charging_profiles_v1g;");
}
else {
	profileName = profileName + f_getProfileNameAdditions();
	quarterlyEnergyDemand_kWh = selectValues(double.class, "SELECT " + profileName + " FROM charging_profiles_v2g;");
}

return quarterlyEnergyDemand_kWh;
/*ALCODEEND*/}

String f_getProfileNameAdditions()
{/*ALCODESTART::1725564597264*/
String string = "_";

double EVShare = sl_EVsWoonwijkScale.getValue();
if(  EVShare < 0.49 ){
	string += "25EV_";
}
else if  (EVShare < 0.74 ){
	string += "50EV_";
}
else if ( EVShare < 0.99 ){
	string += "75EV_";
}
else {
	string += "100EV_";
}

int WPshare = uI_Tabs.tabHeating.sl_householdsElectricHeatPump_pct.getIntValue();

if(  EVShare < 33 ){
	string += "0WP";
}
else if  (EVShare < 66 ){
	string += "50WP";
}
else {
	string += "100WP";
}


return string;
/*ALCODEEND*/}

double f_setChargerInactive(GCPublicCharger charger)
{/*ALCODESTART::1725568178701*/
zero_Interface.c_activePublicChargers.remove(charger); 
charger.c_profileAssets.get(0).removeEnergyAsset(); //get rid of the charging profile
charger.v_isActiveCharger = false;
zero_Interface.c_inactivePublicChargers.add(charger); //add the charger to the other list
charger.c_connectedGISObjects.get(0).gisRegion.setVisible(false);
v_currentNbChargers --;

/*ALCODEEND*/}

double f_setChargerActive(GCPublicCharger charger)
{/*ALCODESTART::1725568192004*/
zero_Interface.c_inactivePublicChargers.remove( charger );
J_EAProfile profile = new J_EAProfile(charger, OL_EnergyCarrierType.ELECTRICITY, null, OL_ProfileAssetType.CHARGING, zero_Interface.energyModel.p_timeStep_h);		
profile.energyAssetName = "charging profile";		
List<Double> quarterlyEnergyDemand_kWh = f_getChargingProfile(charger);
profile.a_energyProfile_kWh = quarterlyEnergyDemand_kWh.stream().mapToDouble(d -> max(0,d)).map( d -> d / 4).toArray();
charger.v_isActiveCharger = true;
charger.c_connectedGISObjects.get(0).gisRegion.setVisible(true);
zero_Interface.c_activePublicChargers.add(charger);
v_currentNbChargers ++;
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

double f_setPVSystemCompanies(List<GCUtility> gcList,double target_pct)
{/*ALCODESTART::1747297871195*/
Pair<Double, Double> pair = f_getPVSystemPercentage( new ArrayList<GridConnection>(gcList));
double remaining_kWp = target_pct * pair.getSecond() - pair.getFirst();
double averageEffectivePV_kWppm2 = zero_Interface.energyModel.avgc_data.p_avgRatioRoofPotentialPV * zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2;

if ( remaining_kWp > 0 ) {
	// add more PV
	for ( GCUtility company : new ArrayList<GCUtility>(zero_Interface.c_orderedPVSystemsCompanies) ) {
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
	for ( GCUtility company : new ArrayList<GCUtility>(zero_Interface.c_orderedPVSystemsCompanies) ) {
		if ( company.v_liveAssetsMetaData.hasPV ) {
			remaining_kWp += company.v_liveAssetsMetaData.totalInstalledPVPower_kW;
			f_removePVSystem( company );
		}
		if ( remaining_kWp >= 0 ) {
			// removed slightly too much pv
			f_addPVSystem( company, remaining_kWp );
			zero_Interface.f_resetSettings();			
			return;
		}
	}
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
	
	J_EAProduction productionAsset = new J_EAProduction ( gc, assetType, assetName, capacity_kWp, capacityHeat_kW, yearlyProductionMethane_kWh, yearlyProductionHydrogen_kWh, zero_Interface.energyModel.p_timeStep_h, outputTemperature_degC, zero_Interface.energyModel.pp_solarPVproduction );
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
			if ( companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc ) {
				companyUI.sl_rooftopPVCompany.setValue(roundToInt(capacity_kWp), false);
				companyUI.v_defaultPVSlider = roundToInt(capacity_kWp);
			}
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
	}
}
/*ALCODEEND*/}

double f_setPVSystemHouses(List<GCHouse> gcList,double target_pct)
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

