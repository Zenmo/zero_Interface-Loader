double f_showCorrectTab()
{/*ALCODESTART::1722245473562*/
pop_tabElectricity_presentation.setVisible(false);
pop_tabHeating_presentation.setVisible(false);
pop_tabMobility_presentation.setVisible(false);
pop_tabEHub_presentation.setVisible(false);

switch (v_selectedTabType) {
	case ELECTRICITY:
		pop_tabElectricity_presentation.setVisible(true);
		break;
	case HEAT:
		pop_tabHeating_presentation.setVisible(true);
		break;
	case MOBILITY:
		pop_tabMobility_presentation.setVisible(true);
		break;
	case HUB:
	case NFATO:
		pop_tabEHub_presentation.setVisible(true);
		break;
}

/*ALCODEEND*/}

double f_setTab(EnergyDemandTab tab)
{/*ALCODESTART::1722259092945*/
v_selectedTabType = tab;
f_showCorrectTab();

/*ALCODEEND*/}

double f_updateBusinessparkSliders()
{/*ALCODESTART::1753869504810*/
// ATTENTION: If you have custom tabs it may be neccesary to override this function and add updates to your custom sliders!

// PV SYSTEMS:
//double PVsystems = count(energyModel.UtilityConnections, x->x.v_liveAssetsMetaData.hasPV == true && x.v_isActive);		
//int PV_pct = roundToInt(100 * PVsystems / count(energyModel.UtilityConnections, x->x.v_isActive));

List<GCUtility> utilityGridConnections = f_getSliderGridConnections_utilities();

Pair<Double, Double> pair = pop_tabElectricity.get(0).f_getPVSystemPercentage( new ArrayList<GridConnection>(findAll(utilityGridConnections, x -> x.v_isActive) ) );
int PV_pct = roundToInt(100.0 * pair.getFirst() / pair.getSecond());
pop_tabElectricity.get(0).getSliderRooftopPVCompanies_pct().setValue(PV_pct, false);

// GAS_BURNER / HEATING SYSTEMS: // Still a slight error. GasBurners + HeatPumps != total, because some GC have primary heating asset null
int GasBurners = count(utilityGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.GAS_BURNER && gc.v_isActive);
int GasBurners_pct = roundToInt(100.0 * GasBurners / (count(utilityGridConnections, x -> x.v_isActive && x.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE) + pop_tabHeating.get(0).v_totalNumberOfGhostHeatingSystems_ElectricHeatpumps + pop_tabHeating.get(0).v_totalNumberOfGhostHeatingSystems_HybridHeatpumps));

pop_tabHeating.get(0).getSliderGasBurnerCompanies_pct().setValue(GasBurners_pct, false);
pop_tabHeating.get(0).f_setHeatingSliders( 0, pop_tabHeating.get(0).getSliderGasBurnerCompanies_pct(), pop_tabHeating.get(0).getSliderElectricHeatPumpCompanies_pct(), null, null, null );

pop_tabHeating.get(0).getSliderHeatDemandSlidersCompaniesGasBurnerCompanies_pct().setValue(GasBurners_pct, false);
pop_tabHeating.get(0).f_setHeatingSliders( 0, pop_tabHeating.get(0).getSliderHeatDemandSlidersCompaniesGasBurnerCompanies_pct(), pop_tabHeating.get(0).getSliderHeatDemandSlidersCompaniesElectricHeatPumpCompanies_pct(), null, null, null );


// HEAT_PUMP_AIR:
//		int HeatPumps = count(energyModel.UtilityConnections, gc->gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
//		int HeatPumps_pct = roundToInt(100 * HeatPumps / energyModel.UtilityConnections.size());
//		sl_electricHeatPumpCompanies.setValue(HeatPumps_pct, false);
//		f_setHeatingSlidersCompanies(1);
	
// TRUCKS:
int DieselTrucks = 0;
int ElectricTrucks = pop_tabMobility.get(0).v_totalNumberOfGhostVehicle_Trucks;
int HydrogenTrucks = 0;
for (GCUtility gc : utilityGridConnections) {
	if(gc.v_isActive){
		for (J_EAVehicle vehicle : gc.c_vehicleAssets) {
			if (vehicle instanceof J_EADieselVehicle && vehicle.getEAType() == OL_EnergyAssetType.DIESEL_TRUCK) {
				DieselTrucks += 1;
			}
			else if (vehicle instanceof J_EAEV && vehicle.getEAType() == OL_EnergyAssetType.ELECTRIC_TRUCK) {
				ElectricTrucks += 1;
			}
			else if (vehicle instanceof J_EAHydrogenVehicle && vehicle.getEAType() == OL_EnergyAssetType.HYDROGEN_TRUCK) {
				HydrogenTrucks += 1;
			}
		}
	}
}

int totalTrucks = DieselTrucks + ElectricTrucks + HydrogenTrucks;
int DieselTrucks_pct = 100;
int ElectricTrucks_pct = 0;
int HydrogenTrucks_pct = 0;
if (totalTrucks != 0) {
	DieselTrucks_pct = roundToInt(100.0 * DieselTrucks / totalTrucks);
	ElectricTrucks_pct = roundToInt(100.0 * ElectricTrucks / totalTrucks);
	HydrogenTrucks_pct = roundToInt(100.0 * HydrogenTrucks / totalTrucks);
}
pop_tabMobility.get(0).getSliderFossilFuelTrucks_pct().setValue(DieselTrucks_pct, false);
pop_tabMobility.get(0).getSliderElectricTrucks_pct().setValue(ElectricTrucks_pct, false);
pop_tabMobility.get(0).getSliderHydrogenTrucks_pct().setValue(HydrogenTrucks_pct, false);

// VANS:
int DieselVans = 0;
int ElectricVans = pop_tabMobility.get(0).v_totalNumberOfGhostVehicle_Vans;
int HydrogenVans = 0;
for (GCUtility gc : utilityGridConnections) {
	if(gc.v_isActive){
		for (J_EAVehicle vehicle : gc.c_vehicleAssets) {
			if (vehicle instanceof J_EADieselVehicle && vehicle.getEAType() == OL_EnergyAssetType.DIESEL_VAN) {
				DieselVans += 1;
			}
			else if (vehicle instanceof J_EAEV && vehicle.getEAType() == OL_EnergyAssetType.ELECTRIC_VAN) {
				ElectricVans += 1;
			}
			else if (vehicle instanceof J_EAHydrogenVehicle && vehicle.getEAType() == OL_EnergyAssetType.HYDROGEN_VAN) {
				HydrogenVans += 1;
			}
		}
	}
}

int totalVans = DieselVans + ElectricVans + HydrogenVans;
int DieselVans_pct = 100;
int ElectricVans_pct = 0;
int HydrogenVans_pct = 0;
if (totalVans != 0) {
	DieselVans_pct = roundToInt(100.0 * DieselVans / totalVans);
	ElectricVans_pct = roundToInt(100.0 * ElectricVans / totalVans);
	HydrogenVans_pct = roundToInt(100.0 * HydrogenVans / totalVans);
}
pop_tabMobility.get(0).getSliderFossilFuelVans_pct().setValue(DieselVans_pct, false);
pop_tabMobility.get(0).getSliderElectricVans_pct().setValue(ElectricVans_pct, false);
//sl_hydrogenVans.setValue(HydrogenVans_pct, false);
		
// DIESEL_VEHICLE:  // Currently only for Company Cars not household Cars / EVs
int DieselCars = 0;
int ElectricCars = pop_tabMobility.get(0).v_totalNumberOfGhostVehicle_Cars;
int HydrogenCars = 0;
for (GCUtility gc : utilityGridConnections) {
	if(gc.v_isActive){
		for (J_EAVehicle vehicle : gc.c_vehicleAssets) {
			if (vehicle instanceof J_EADieselVehicle && vehicle.getEAType() == OL_EnergyAssetType.DIESEL_VEHICLE) {
				DieselCars += 1;
			}
			else if (vehicle instanceof J_EAEV && vehicle.getEAType() == OL_EnergyAssetType.ELECTRIC_VEHICLE) {
				ElectricCars += 1;
			}
			else if (vehicle instanceof J_EAHydrogenVehicle && vehicle.getEAType() == OL_EnergyAssetType.HYDROGEN_VEHICLE) {
				HydrogenCars += 1;
			}
		}
	}
}

int totalCars = DieselCars + ElectricCars + HydrogenCars;
int DieselCars_pct = 100;
int ElectricCars_pct = 0;
int HydrogenCars_pct = 0;
if (totalCars != 0) {
	DieselCars_pct = roundToInt((100.0 * DieselCars) / totalCars);
	ElectricCars_pct = roundToInt((100.0 * ElectricCars) / totalCars);
	HydrogenCars_pct = roundToInt((100.0 * HydrogenCars) / totalCars);
}
pop_tabMobility.get(0).getSliderFossilFuelCars_pct().setValue(DieselCars_pct, false);
pop_tabMobility.get(0).getSliderElectricCars_pct().setValue(ElectricCars_pct, false);
//sl_hydrogenCars.setValue(HydrogenCars_pct, false);

/*ALCODEEND*/}

double f_updateResidentialSliders()
{/*ALCODESTART::1753870299960*/
List<GCHouse> houseGridConnections = new ArrayList<>();
List<GCPublicCharger> chargerGridConnections = new ArrayList<>();
List<GCGridBattery> gridBatteryGridConnections = new ArrayList<>();

for (GridConnection GC : findAll(v_sliderGridConnections, gc -> gc.v_isActive)) {
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

int nbHouses = houseGridConnections.size();
int nbHousesWithPV = count(houseGridConnections, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW));
double pv_pct = 100.0 * nbHousesWithPV / nbHouses;
pop_tabElectricity.get(0).sl_householdPVResidentialArea_pct.setValue(pv_pct, false);

if ( nbHousesWithPV != 0 ) {
	int nbHousesWithHomeBattery = count(houseGridConnections, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW) && x.p_batteryAsset != null);
	double battery_pct = 100.0 * nbHousesWithHomeBattery / nbHousesWithPV;
	pop_tabElectricity.get(0).sl_householdBatteriesResidentialArea_pct.setValue(battery_pct, false);
}

int nbHousesWithElectricCooking = count(houseGridConnections, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC);
double cooking_pct = 100.0 * nbHousesWithElectricCooking / nbHouses;
pop_tabElectricity.get(0).sl_householdElectricCookingResidentialArea_pct.setValue(cooking_pct, false);

if (zero_Interface.c_orderedVehiclesPrivateParking.size() > 0) {
	int nbPrivateEVs = count(zero_Interface.c_orderedVehiclesPrivateParking, x -> x instanceof J_EAEV);
	double privateEVs_pct = 100.0 * nbPrivateEVs / zero_Interface.c_orderedVehiclesPrivateParking.size();
	pop_tabElectricity.get(0).sl_privateEVsResidentialArea_pct.setValue(privateEVs_pct, false);
}


//Update chargers
int nbPublicChargers = chargerGridConnections.size();
int nbActivePublicChargers = count(chargerGridConnections, x -> x.v_isActive);
double activePublicChargers_pct = 100.0 * nbActivePublicChargers / nbPublicChargers;
pop_tabElectricity.get(0).sl_publicChargersResidentialArea_pct.setValue(activePublicChargers_pct, false);

int nbV1GChargers = count(zero_Interface.c_orderedV1GChargers, x -> chargerGridConnections.contains(x) && x.V1GCapable);
int nbV2GChargers =count(zero_Interface.c_orderedV2GChargers, x -> chargerGridConnections.contains(x) && x.V2GCapable);
double V1G_pct = 100.0 * nbV1GChargers / nbPublicChargers;
double V2G_pct = 100.0 * nbV2GChargers / nbPublicChargers;
pop_tabElectricity.get(0).sl_chargersThatSupportV1G_pct.setValue(V1G_pct, false);
pop_tabElectricity.get(0).sl_chargersThatSupportV2G_pct.setValue(V2G_pct, false);



double averageNeighbourhoodBatterySize_kWh = 0;
for (GCGridBattery gc : gridBatteryGridConnections) {
	averageNeighbourhoodBatterySize_kWh += gc.p_batteryAsset.getStorageCapacity_kWh();
}
averageNeighbourhoodBatterySize_kWh /= gridBatteryGridConnections.size();
pop_tabElectricity.get(0).sl_gridBatteriesResidentialArea_kWh.setValue(averageNeighbourhoodBatterySize_kWh, false);

/*ALCODEEND*/}

double f_initializeSliderGridConnections(List<GridConnection> gridConnections)
{/*ALCODESTART::1753881126492*/
c_utilityGridConnections.clear();
c_houseGridConnections.clear();
c_solarFarmGridConnections.clear();
c_windFarmGridConnections.clear();
c_gridBatteryGridConnections.clear();
c_chargerGridConnections.clear();


for(GridConnection GC : gridConnections){
	if(GC instanceof GCUtility){
		c_utilityGridConnections.add((GCUtility)GC);
	}
	else if(GC instanceof GCHouse){
		c_houseGridConnections.add((GCHouse)GC);		
	}
	else if(GC instanceof GCEnergyProduction){
		if(GC.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW)){
			c_solarFarmGridConnections.add((GCEnergyProduction)GC);		
		}
		if(GC.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.windProductionElectric_kW)){
			c_windFarmGridConnections.add((GCEnergyProduction)GC);
		}
	}
	else if(GC instanceof GCGridBattery){
		c_gridBatteryGridConnections.add((GCGridBattery)GC);		
	}
	else if(GC instanceof GCPublicCharger){
		c_chargerGridConnections.add((GCPublicCharger)GC);		
	}
}
/*ALCODEEND*/}

double f_initializeSliderGCPointer(List<GridConnection> gridConnections)
{/*ALCODESTART::1754908006859*/
v_sliderGridConnections = gridConnections;
/*ALCODEEND*/}

double f_initializeUI_Tabs(List<GridConnection> gridConnections)
{/*ALCODESTART::1754908356478*/
//Initialize the GridConnections
f_initializeSliderGCPointer(gridConnections);

//Set sliders to engine state of the gridconnections
f_updateSliders();

//Show correct tab
f_showCorrectTab();
/*ALCODEEND*/}

List<GCHouse> f_getSliderGridConnections_houses()
{/*ALCODESTART::1754919017244*/
List<GCHouse> houseGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCHouse){
		houseGridConnections.add((GCHouse)GC);		
	}
}

return houseGridConnections;
/*ALCODEEND*/}

List<GCUtility> f_getSliderGridConnections_utilities()
{/*ALCODESTART::1754922545766*/
List<GCUtility> utilityGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCUtility){
		utilityGridConnections.add((GCUtility)GC);
	}
}

return utilityGridConnections;
/*ALCODEEND*/}

List<GCGridBattery> f_getSliderGridConnections_gridBatteries()
{/*ALCODESTART::1754922546986*/
List<GCGridBattery> gridBatteryGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCGridBattery){
		gridBatteryGridConnections.add((GCGridBattery)GC);		
	}
}

return gridBatteryGridConnections;
/*ALCODEEND*/}

List<GCPublicCharger> f_getSliderGridConnections_chargers()
{/*ALCODESTART::1754922547989*/
List<GCPublicCharger> chargerGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCPublicCharger){
		chargerGridConnections.add((GCPublicCharger)GC);		
	}
}

return chargerGridConnections;
/*ALCODEEND*/}

List<GCEnergyProduction> f_getSliderGridConnections_production()
{/*ALCODESTART::1754922591622*/
List<GCEnergyProduction> productionGridConnections = new ArrayList<>();

for(GridConnection GC : v_sliderGridConnections){
	if(GC instanceof GCEnergyProduction){
		productionGridConnections.add((GCEnergyProduction)GC);
	}
}

return productionGridConnections;
/*ALCODEEND*/}

double f_updateSliders()
{/*ALCODESTART::1754929564839*/
if(!pop_tabElectricity.isEmpty()){
	pop_tabElectricity.get(0).f_updateSliders_Electricity();
}
if(!pop_tabHeating.isEmpty()){
	pop_tabHeating.get(0).f_updateSliders_Heating();
}
if(!pop_tabMobility.isEmpty()){
	pop_tabMobility.get(0).f_updateSliders_Mobility();
}
if(!pop_tabEHub.isEmpty()){
	pop_tabEHub.get(0).f_updateSliders_EHub();
}
/*ALCODEEND*/}

