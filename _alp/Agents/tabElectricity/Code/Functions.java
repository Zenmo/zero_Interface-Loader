double f_setPVSystemCompanies(double PV_pct,ShapeSlider usedSlider)
{/*ALCODESTART::1722256095319*/
//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

double PVsystems = count(zero_Interface.energyModel.UtilityConnections, x->x.v_hasPV == true && x.v_isActive);
double totalActiveUtilityConnections = count(zero_Interface.energyModel.UtilityConnections, gc -> gc.v_isActive);

if( PV_pct/100.0 < PVsystems/totalActiveUtilityConnections ){
	while ( PV_pct/100.0*totalActiveUtilityConnections < PVsystems) { // remove excess PV systems
		GCUtility gc = findFirst(zero_Interface.c_orderedPVSystemsCompanies, x->x.v_hasPV==true);
		
		if(gc != null){
			// update UI company
			if (zero_Interface.c_companyUIs.size()>0){
				UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
				if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
					if (companyUI.v_minPVSlider != 0) {
						traceln("Removed already existing PV system");
					}
					else {
						companyUI.sl_rooftopPVCompany.setValue(0, false);
						companyUI.v_defaultPVSlider = 0;
					}
				}
			}
			
			J_EA pvAsset = findFirst(gc.c_productionAssets, p -> p.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC );
			//traceln("Found PV system: " + pvAsset);
			if (pvAsset!=null) {
				pvAsset.removeEnergyAsset();
				zero_Interface.c_orderedPVSystemsCompanies.remove(gc);
				zero_Interface.c_orderedPVSystemsCompanies.add(0, gc);
				//traceln("Removing PV from GridConnection:" + h.p_gridConnectionID);
			}
			else{
				gc.v_hasPV = false;
			}
			//Color the buildings when in solar color mode
			if(zero_Interface.rb_buildingColors.getValue() == 2){
				for(GIS_Object building : gc.c_connectedGISObjects){
					zero_Interface.f_styleAreas(building);
				}
			}
			
		// Recalculating the amount of PV Systems instead of just decreasing by one to account for possibility of having multiple PV Assets
		PVsystems--; //=count(zero_Interface.energyModel.UtilityConnections, x->x.v_hasPV == true);		
		}
		else{//No pv systems to adjust, set slider to minimum and do nothing else
			int min_pct_pvSystems = roundToInt(100.0 * ((double)count(zero_Interface.energyModel.UtilityConnections, x->x.v_hasPV == true && x.v_isActive) / totalActiveUtilityConnections));
			usedSlider.setValue(min_pct_pvSystems, false);
			return;
		}
	}
}
else {
	while ( PV_pct/100.0 > PVsystems/totalActiveUtilityConnections) {

		GCUtility gc = findFirst(zero_Interface.c_orderedPVSystemsCompanies, x->x.v_hasPV==false);
		if (gc == null){
			traceln("No gridconnection without PV panels found! Current PVsystems count: %s", PVsystems);
			break;
		}
		else {
			OL_EnergyAssetType assetType = OL_EnergyAssetType.PHOTOVOLTAIC;
			String assetName = "Rooftop PV";

			double roofAreaForPV_m2 = zero_Interface.energyModel.avgc_data.p_avgRatioRoofPotentialPV * gc.p_roofSurfaceArea_m2;					
			double capacityElectricity_kW = max(0.1, roofAreaForPV_m2*zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2);
			double capacityHeat_kW = 0.0;
			double yearlyProductionHydrogen_kWh = 0.0;
			double yearlyProductionMethane_kWh = 0.0;
			double outputTemperature_degC = 0.0;
			
			J_EAProduction productionAsset = new J_EAProduction ( gc, assetType, assetName, capacityElectricity_kW, capacityHeat_kW, yearlyProductionMethane_kWh, yearlyProductionHydrogen_kWh, zero_Interface.energyModel.p_timeStep_h, outputTemperature_degC, zero_Interface.energyModel.pp_solarPVproduction );
			
			//Add GC to top of the orderd pv systems so it will be found first when removing.
			zero_Interface.c_orderedPVSystemsCompanies.remove(gc);
			zero_Interface.c_orderedPVSystemsCompanies.add(0, gc);
		
			PVsystems++;
			//traceln("Added PV to GridConnection: " + gc.p_gridConnectionID + "With capacity [kW]: " + capacityElectricity_kW);
			
			// update UI company
			if (zero_Interface.c_companyUIs.size()>0){
				UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
				if (companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
					companyUI.sl_rooftopPVCompany.setValue(roundToInt(capacityElectricity_kW), false);
					companyUI.v_defaultPVSlider = roundToInt(capacityElectricity_kW);
				}
			}
			
			//Color the buildings when in solar color mode
			if(zero_Interface.rb_buildingColors.getValue() == 2){
				for(GIS_Object building : gc.c_connectedGISObjects){
					zero_Interface.f_styleAreas(building);
				}
			}
		}
	}
}	

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

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

double f_setPVSystemHouses(double PV_pct)
{/*ALCODESTART::1722256142375*/
//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

double nbHousesWithPV = count(zero_Interface.energyModel.Houses, x -> x.v_hasPV == true);

if( PV_pct /100.0 < nbHousesWithPV / zero_Interface.energyModel.Houses.size() ){
	while ( PV_pct / 100.0 * zero_Interface.energyModel.Houses.size() < nbHousesWithPV ) { // remove excess PV systems
		GCHouse house = findFirst(zero_Interface.c_orderedPVSystemsHouses, x -> x.v_hasPV == true);	
		J_EA pvAsset = findFirst(house.c_productionAssets, p -> p.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC );
		if (pvAsset != null) {
			pvAsset.removeEnergyAsset();
			zero_Interface.c_orderedPVSystemsHouses.remove( house) ;
			zero_Interface.c_orderedPVSystemsHouses.add(0, house) ;
			nbHousesWithPV --; 
		}
		else {
			traceln(" cant find PV asset in house that should have PV asset in f_setPVHouses (Interface)");
		}
	}
 
}
else {
	while ( PV_pct / 100.0 > nbHousesWithPV / zero_Interface.energyModel.Houses.size()) {
		GCHouse house = findFirst(zero_Interface.c_orderedPVSystemsHouses, x -> x.v_hasPV == false);
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
			zero_Interface.c_orderedPVSystemsHouses.remove(house);
			zero_Interface.c_orderedPVSystemsHouses.add(0, house);
			nbHousesWithPV ++;	
		}
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
double nbHousesWithPV = count(zero_Interface.energyModel.Houses, x -> x.v_hasPV == true); //count(energyModel.f_getGridConnections(), p->p instanceof GCHouse);
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
			GCHouse house = findFirst(zero_Interface.energyModel.Houses, p -> p.p_batteryAsset == null && p.v_hasPV == true);
			
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

double f_setDemandReduction(double demandReduction_pct)
{/*ALCODESTART::1722335253834*/
double newElectricityDemandReduction_pct = demandReduction_pct;
double consumptionScaling_fr = 1  - newElectricityDemandReduction_pct/100;

for (J_EA j_ea : zero_Interface.energyModel.f_getEnergyAssets()) {
	if (j_ea instanceof J_EAConsumption) {
		if (j_ea.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND) {
			((J_EAConsumption)j_ea).setConsumptionScaling_fr(consumptionScaling_fr);
			
			if (zero_Interface.c_companyUIs.size()>0){
				UI_company companyUI = zero_Interface.c_companyUIs.get(((GridConnection)j_ea.getParentAgent()).p_owner.p_connectionOwnerIndexNr);
				if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == j_ea.getParentAgent()) { // should also check the setting of selected GC
					companyUI.sl_electricityDemandCompanyReduction.setValue(newElectricityDemandReduction_pct, false);
				}
			}
		}
	}
	if (j_ea instanceof J_EAProfile) {
		if (((J_EAProfile) j_ea).energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
			((J_EAProfile) j_ea).resetEnergyProfile();
			((J_EAProfile) j_ea).scaleEnergyProfile(consumptionScaling_fr);
			
			if (zero_Interface.c_companyUIs.size()>0){
				UI_company companyUI = zero_Interface.c_companyUIs.get(((GridConnection)j_ea.getParentAgent()).p_owner.p_connectionOwnerIndexNr);
				if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == j_ea.getParentAgent()) { // should also check the setting of selected GC
					companyUI.sl_electricityDemandCompanyReduction.setValue(newElectricityDemandReduction_pct, false);
				}
			}
		}
	}
}

v_electricityDemandReduction_pct = newElectricityDemandReduction_pct;

//Update variable to change to custom scenario
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
	if ( gc.v_hasPV ) {
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
Pair<Double, Double> pair = f_getPVSystemPercentage();
double remaining_kWp = target_pct * pair.getSecond() - pair.getFirst();
double averageEffectivePV_kWppm2 = zero_Interface.energyModel.avgc_data.p_avgRatioRoofPotentialPV * zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2;

if ( remaining_kWp > 0 ) {
	// add more PV
	for ( GCUtility company : zero_Interface.c_orderedPVSystemsCompany ) {
		double remainingPotential_kWp = company.p_roofSurfaceArea_m2 * averageEffectivePV_kWppm2 - company.v_liveAssetsMetaData.totalInstalledPVPower_kW;
		if ( remainingPotential_kWp > 0 ) {
			remaining_kWp -= remainingPotential_kWp
			f_addPVSystem( company, remainingPotential_kWp );
		}
		
		if ( remaining_kWp <= 0 ) {
			return;
		}
	}
}
else {
	// remove pv
	for ( GCUtility company : zero_Interface.c_orderedPVSystemsCompany ) {
		if ( company.v_liveAssetsMetaData.hasPV || gc.v_hasPV ) {
			remaining_kWp += company.v_liveAssetsMetaData.totalInstalledPVPower_kW
			f_removePVSystem( company );
		}
		if ( remaining_kWp >= 0 ) {
			// removed slightly too much pv
			f_addPVSystem( company, remaining_kWp );
			return;
		}
	}
}
/*ALCODEEND*/}

double f_addPVSystem(GridConnection gc,double capacity_kWp)
{/*ALCODESTART::1747306690517*/
if ( gc.v_liveAssetsMetaData.hasPV || gc.v_hasPV ) { // This boolean exists in 2 places...
	// Add the capacity to the existing asset
	J_EAProduction pvAsset = findFirst(gc.c_productionAssets, p -> p.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC);
	if ( pvAsset == null ) {
		throw new RuntimeException("Could not find photovoltaic asset in GridConnection: " + gc.p_ownerID + ", even though hasPV is True.");
	}
	pvAsset.setCapacityElectric_kW( pvAsset.getCapacityElectric_kW() + capacity_kWp );
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
	zero_Interface.c_orderedPVSystemsHouses.add(0, gc);	
}
else if ( gc instanceof GCUtility ) {
	zero_Interface.c_orderedPVSystemsCompanies.remove(gc);
	zero_Interface.c_orderedPVSystemsCompanies.add(0, gc);
	// update company UI
	if ( zero_Interface.c_companyUIs.size() > 0 ) {
		if ( gc.p_owner != null ) {
			UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
			if ( companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc ) {
				companyUI.sl_rooftopPVCompany.setValue(roundToInt(capacityElectricity_kW), false);
				companyUI.v_defaultPVSlider = roundToInt(capacityElectricity_kW);
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
}
/*ALCODEEND*/}

double f_updateCompanyUI()
{/*ALCODESTART::1747307194121*/

/*ALCODEEND*/}

