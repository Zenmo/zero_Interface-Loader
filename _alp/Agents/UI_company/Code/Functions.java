double f_setScenarioFuture()
{/*ALCODESTART::1713445281785*/
//Set button to custom early on, so traceln will get ignored.
rb_scenariosPrivateUI.setValue(2, false);


////Heating

//Heating savings
sl_heatDemandCompanyReduction.setValue(p_scenarioSettings_Future.getPlannedHeatSavings_pct(), true);

//Heating type (aangenomen dat het hetzelfde blijft, want hebben geen vraag die dat stelt in het formulier)
int nr_currentHeatingType = 0;
switch (p_scenarioSettings_Future.getPlannedHeatingType()){
	case GAS_BURNER:
		nr_currentHeatingType = 0;
		break;

	case HYBRID_HEATPUMP:
		nr_currentHeatingType = 1;
		break;

	case ELECTRIC_HEATPUMP:
		nr_currentHeatingType = 2;
		break;
		
	//case HYDROGENBURNER:
	case DISTRICTHEAT:
		nr_currentHeatingType = 3;
		break;

	case GAS_CHP:
		nr_currentHeatingType = 4;
		break;
		
	default:
}
rb_heatingTypePrivateUI.setValue(nr_currentHeatingType, true);		


////Electricity

//Electricity savings
sl_electricityDemandCompanyReduction.setValue(p_scenarioSettings_Future.getPlannedElectricitySavings_pct(), true);

//Connection capacity (Delivery)
sl_GCCapacityCompany.setValue(p_scenarioSettings_Future.getRequestedContractDeliveryCapacity_kW(), true);

//Connection capacity (Feedin)
sl_GCCapacityCompany_Feedin.setValue(p_scenarioSettings_Future.getRequestedContractFeedinCapacity_kW(), true);

//Connection capacity (Physical)
v_physicalConnectionCapacity_kW = p_scenarioSettings_Future.getRequestedPhysicalConnectionCapacity_kW();
p_gridConnection.v_liveConnectionMetaData.physicalCapacity_kW = v_physicalConnectionCapacity_kW;

//Solar panel power
sl_rooftopPVCompany.setValue(p_scenarioSettings_Future.getPlannedPV_kW(), true);

//Battery capacity
sl_batteryCompany.setValue(p_scenarioSettings_Future.getPlannedBatteryCapacity_kWh(), true);

//Curtailment setting
cb_curtailmentCompany.setSelected(p_scenarioSettings_Future.getPlannedCurtailment(), true);

////Mobility

//Mobility savings
sl_mobilityDemandCompanyReduction.setValue(p_scenarioSettings_Future.getPlannedTransportSavings_pct(), true);

//Cars (VOLGORDE BELANGRIJK)
sl_hydrogenCarsCompany.setValue(p_scenarioSettings_Future.getPlannedHydrogenCars(), true);
sl_electricCarsCompany.setValue(p_scenarioSettings_Future.getPlannedEVCars(), true);
//sl_petroleumFuelCarsCompany.setValue(c_scenarioSettings_Future.getPlannedPetroleumFuelCars(), true);

//Vans (VOLGORDE BELANGRIJK)
sl_hydrogenVansCompany.setValue(p_scenarioSettings_Future.getPlannedHydrogenVans(), true);
sl_electricVansCompany.setValue(p_scenarioSettings_Future.getPlannedEVVans(), true);
//sl_petroleumFuelVansCompany.setValue(c_scenarioSettings_Future.getPlannedPetroleumFuelVans(), true);

//Trucks (VOLGORDE BELANGRIJK)
sl_hydrogenTrucksCompany.setValue(p_scenarioSettings_Future.getPlannedHydrogenTrucks(), true);
sl_electricTrucksCompany.setValue(p_scenarioSettings_Future.getPlannedEVTrucks(), true);
//sl_petroleumFuelTrucksCompany.setValue(c_scenarioSettings_Future.getPlannedPetroleumFuelTrucks(), true);

//set active if active in future
p_gridConnection.f_setActive(p_scenarioSettings_Future.getIsActiveInFuture(), zero_Interface.energyModel.p_timeVariables);


//Reset button to future, due to sliders putting it on custom
rb_scenariosPrivateUI.setValue(1, false);
/*ALCODEEND*/}

double f_setScenario(int scenario_nr)
{/*ALCODESTART::1713447383903*/
switch (scenario_nr){

	case 0: // Current
		f_setScenarioCurrent();

		if(!b_runningMainInterfaceScenarioSettings){
			traceln("Selected scenario: Current");
		}
		
	break;
	
	case 1: // Future
		f_setScenarioFuture();

		if(!b_runningMainInterfaceScenarioSettings){
			traceln("Selected scenario: Future");
		}
	break;
	
	case 2: // Custom
		if(rb_scenariosPrivateUI.getValue() == 2){
			return;
		}
		rb_scenariosPrivateUI.setValue(2, false);
		
		if(!b_runningMainInterfaceSlider){
			traceln("Selected scenario: Custom");
		}
	break;
	
	default:
}

//Set 'results up to date' to false
zero_Interface.b_resultsUpToDate = false;
/*ALCODEEND*/}

double f_setScenarioCurrent()
{/*ALCODESTART::1713447428490*/
//Set button to custom early on, so traceln will get ignored.
rb_scenariosPrivateUI.setValue(2, false);


////Heating

//Heating savings
sl_heatDemandCompanyReduction.setValue(0, true);

//Heating type
int nr_currentHeatingType = 0;
switch (p_scenarioSettings_Current.getCurrentHeatingType()){
	case GAS_BURNER:
		nr_currentHeatingType = 0;
		break;

	case HYBRID_HEATPUMP:
		nr_currentHeatingType = 1;
		break;

	case ELECTRIC_HEATPUMP:
		nr_currentHeatingType = 2;
		break;
		
	//case HYDROGENBURNER:
	case DISTRICTHEAT:
		nr_currentHeatingType = 3;
		break;
	
	case GAS_CHP:
		nr_currentHeatingType = 4;
		break;
		
	default:
}
rb_heatingTypePrivateUI.setValue(nr_currentHeatingType, true);		


////Electricity

//Electricity savings
sl_electricityDemandCompanyReduction.setValue(0, true);

//Connection capacity (Delivery)
sl_GCCapacityCompany.setValue(p_scenarioSettings_Current.getCurrentContractDeliveryCapacity_kW(), true);

//Connection capacity (Feedin)
sl_GCCapacityCompany_Feedin.setValue(p_scenarioSettings_Current.getCurrentContractFeedinCapacity_kW(), true);

//Connection capacity (Physical)
v_physicalConnectionCapacity_kW = p_scenarioSettings_Current.getCurrentPhysicalConnectionCapacity_kW();
p_gridConnection.v_liveConnectionMetaData.physicalCapacity_kW = v_physicalConnectionCapacity_kW;

//Solar panel power
sl_rooftopPVCompany.setValue(v_minPVSlider, true);

//Battery capacity
sl_batteryCompany.setValue(v_minBatSlider, true);

//Curtailment setting
cb_curtailmentCompany.setSelected(false, false);

////Mobility

//Mobility savings
sl_mobilityDemandCompanyReduction.setValue(0, true);

//Cars (VOLGORDE BELANGRIJK)
sl_hydrogenCarsCompany.setValue(p_scenarioSettings_Current.getCurrentHydrogenCars(), true);
sl_electricCarsCompany.setValue(p_scenarioSettings_Current.getCurrentEVCars(), true);
//sl_petroleumFuelCarsCompany.setValue(c_scenarioSettings_Current.getCurrentPetroleumFuelCars(), true);

//Vans (VOLGORDE BELANGRIJK)
sl_hydrogenVansCompany.setValue(p_scenarioSettings_Current.getCurrentHydrogenVans(), true);
sl_electricVansCompany.setValue(p_scenarioSettings_Current.getCurrentEVVans(), true);
//sl_petroleumFuelVansCompany.setValue(c_scenarioSettings_Current.getCurrentPetroleumFuelVans(), true);

//Trucks (VOLGORDE BELANGRIJK)
sl_hydrogenTrucksCompany.setValue(p_scenarioSettings_Current.getCurrentHydrogenTrucks(), true);
sl_electricTrucksCompany.setValue(p_scenarioSettings_Current.getCurrentEVTrucks(), true);
//sl_petroleumFuelTrucksCompany.setValue(c_scenarioSettings_Current.getCurrentPetroleumFuelTrucks(), true);

//set active if active in present
p_gridConnection.f_setActive(p_scenarioSettings_Current.getIsCurrentlyActive(), zero_Interface.energyModel.p_timeVariables);

//Reset button to current, due to sliders putting it on custom
rb_scenariosPrivateUI.setValue(0, false);
/*ALCODEEND*/}

double f_setHeatingType(GridConnection GC,OL_GridConnectionHeatingType selectedHeatingType)
{/*ALCODESTART::1713537591106*/
//Check if selected is not the same as previous, if not: continue with the setting of new heating type
if (GC.f_getCurrentHeatingType() == selectedHeatingType){
	//traceln("Selected heating type is the same as previous heating type");
	return;
}

//Remove from heat grid if it was connected to one.
GC.p_parentNodeHeat = null;
GC.p_parentNodeHeatID = null;

//Remove primary heating asset
GC.f_removeAllHeatingAssets();

//Get needed cacacity
double capacityThermal_kW;

//Select heat demand consumption asset 
J_EAConsumption heatDemandAsset = findFirst(GC.c_consumptionAssets, j_ea->j_ea.getEAType() == OL_EnergyAssetType.HEAT_DEMAND);

//Check heating demand asset is null (shouldnt be possible)
if (heatDemandAsset != null){
	capacityThermal_kW = heatDemandAsset.yearlyDemand_kWh/8760*10; // --> average hourly consumption * 10 --> to always have enough capacity
}
else{
	//Select profile heat demand asset 
	J_EAProfile heatDemandAsset_Profile = findFirst(GC.c_profileAssets, j_ea->j_ea.energyCarrier == OL_EnergyCarriers.HEAT);
	
	if(heatDemandAsset_Profile != null){
		capacityThermal_kW = heatDemandAsset_Profile.getProfileScaling_fr() * max(heatDemandAsset_Profile.a_energyProfile_kWh)*4;
	}
	else{
		traceln("No heating demand asset found for GC:" + GC.p_gridConnectionID);
		traceln("--> No heating asset created");
		return;
	}
		
}

capacityThermal_kW = capacityThermal_kW * 2;//For now just make it always twice as high, to be able to support savings/additional consumption slider settings.

//Algemeen
J_TimeParameters timeParameters = zero_Interface.energyModel.p_timeParameters;
double efficiency;
double outputTemperature_degC;

//Heatpump specifieke parameters
double baseTemperature_degC = zero_Interface.energyModel.pp_ambientTemperature_degC.getCurrentValue();
double capacityElectric_kW;		
OL_AmbientTempType ambientTempType;
double sourceAssetHeatPower_kW;
double belowZeroHeatpumpEtaReductionFactor;



//Create selected heating type
switch (selectedHeatingType){
	case GAS_BURNER:
		
		//Add primary heating asset (gasburner)
		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner_fr;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC;
		
		new J_EAConversionGasBurner(GC, capacityThermal_kW, efficiency, timeParameters, outputTemperature_degC);
		
		break;
	
	case HYBRID_HEATPUMP:
		
		//Add primary heating asset (heatpump)
		capacityElectric_kW = capacityThermal_kW / 3; //-- /3, want is hybride, dus kleiner
		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyHeatpump_fr;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureHybridHeatpump_degC;
		ambientTempType = OL_AmbientTempType.AMBIENT_AIR;
		sourceAssetHeatPower_kW = 0;
		belowZeroHeatpumpEtaReductionFactor = 1;
		
		J_EAConversionHeatPump heatPumpHybrid = new J_EAConversionHeatPump(GC, capacityElectric_kW, efficiency, timeParameters, outputTemperature_degC, baseTemperature_degC, sourceAssetHeatPower_kW, belowZeroHeatpumpEtaReductionFactor, ambientTempType );
		zero_Interface.energyModel.c_ambientDependentAssets.add(heatPumpHybrid);
		
		//Add secondary heating asset (gasburner)
		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner_fr;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC;
		
		J_EAConversionGasBurner gasBurnerHybrid = new J_EAConversionGasBurner(GC, capacityThermal_kW, efficiency, timeParameters, outputTemperature_degC);
		//GC.p_secondaryHeatingAsset = gasBurnerHybrid;
		
		break;
	
	case ELECTRIC_HEATPUMP:

		//Add primary heating asset (heatpump)
		capacityElectric_kW = capacityThermal_kW;
		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyHeatpump_fr;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureElectricHeatpump_degC;
		ambientTempType = OL_AmbientTempType.AMBIENT_AIR;
		sourceAssetHeatPower_kW = 0;
		belowZeroHeatpumpEtaReductionFactor = 1;
		
		new J_EAConversionHeatPump(GC, capacityElectric_kW, efficiency, timeParameters, outputTemperature_degC, baseTemperature_degC, sourceAssetHeatPower_kW, belowZeroHeatpumpEtaReductionFactor, ambientTempType );	
		
		//Add secondary heating asset (if needed??)		//E-boiler!!??		
		break;
	
	case HYDROGENBURNER:
		
		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyHydrogenBurner_fr;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureHydrogenBurner_degC;
	    
		//Add primary heating asset (hydrogenburner)
		new J_EAConversionHydrogenBurner(GC, capacityThermal_kW, efficiency, timeParameters, outputTemperature_degC);
		
		break;
	
	case DISTRICTHEAT:

		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyDistrictHeatingDeliverySet_fr;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureDistrictHeatingDeliverySet_degC;
				
		new J_EAConversionHeatDeliverySet(GC, capacityThermal_kW, efficiency, timeParameters, outputTemperature_degC);
		
		//Add GC to heat grid if it exists, else create new one
		GC.p_parentNodeHeat = findFirst(zero_Interface.energyModel.f_getGridNodesTopLevel(), node -> node.p_energyCarrier == OL_EnergyCarriers.HEAT);
		if(GC.p_parentNodeHeat == null){
			GridNode GN_heat = zero_Interface.energyModel.add_pop_gridNodes();
			zero_Interface.energyModel.f_getGridNodesTopLevel().add(GN_heat);
			GN_heat.p_gridNodeID = "Heatgrid";
			
			// Check wether transformer capacity is known or estimated
			GN_heat.p_capacity_kW = 1000000;	
			GN_heat.p_realCapacityAvailable = false;
			
			// Basic GN information
			GN_heat.p_description = "Warmtenet";

			//Define node type
			GN_heat.p_nodeType = OL_GridNodeType.HT;
			GN_heat.p_energyCarrier = OL_EnergyCarriers.HEAT;
			
			//Define GN location
			GN_heat.p_latitude = 0;
			GN_heat.p_longitude = 0;
			GN_heat.setLatLon(GN_heat.p_latitude, GN_heat.p_longitude);
			
			//Connect
			GC.p_parentNodeHeat = GN_heat;
			
			//Show warning that heat grid is not a simple solution
			f_setErrorScreen("LET OP: Er is nu een 'warmtenet' gecreëerd. Maar er is geen warmtebron aanwezig in het model. Daarom zal de benodigde warmte voor het warmtenet in de resultaten te zien zijn als warmte import.");
		}
		GC.p_parentNodeHeatID = GC.p_parentNodeHeat.p_gridNodeID;
		break;
	
	case GAS_CHP:

		efficiency = zero_Interface.energyModel.avgc_data.p_avgEfficiencyCHP_thermal_fr + zero_Interface.energyModel.avgc_data.p_avgEfficiencyCHP_electric_fr;
		outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureCHP_degC;
		double outputCapacityElectric_kW = (capacityThermal_kW/zero_Interface.energyModel.avgc_data.p_avgEfficiencyCHP_thermal_fr) * zero_Interface.energyModel.avgc_data.p_avgEfficiencyCHP_electric_fr;
		
		new J_EAConversionGasCHP(GC, outputCapacityElectric_kW, capacityThermal_kW, efficiency, timeParameters, outputTemperature_degC );
			
		break;
}

// Add a management for the chosen heating type
GC.f_addHeatManagement(selectedHeatingType, false);		
/*ALCODEEND*/}

double f_setGCCapacity(GridConnection GC,double setGridConnectionCapacity_kW,String type)
{/*ALCODESTART::1713537591117*/
GC.f_nfatoSetConnectionCapacity(true, zero_Interface.energyModel.p_timeVariables);

switch(type){
	case "DELIVERY":
		GC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW = setGridConnectionCapacity_kW;
		break;
	case "FEEDIN":
		GC.v_liveConnectionMetaData.contractedFeedinCapacity_kW = setGridConnectionCapacity_kW;
		break;
	case "PHYSICAL":
		GC.v_liveConnectionMetaData.physicalCapacity_kW = setGridConnectionCapacity_kW;
		break;
}

GC.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);
/*ALCODEEND*/}

double f_setBattery(GridConnection GC,double setBatteryCapacity_kWh)
{/*ALCODESTART::1713537591121*/
J_EAStorage batteryAsset = findFirst(GC.c_storageAssets, p -> p.getEAType() == OL_EnergyAssetType.STORAGE_ELECTRIC );

if (setBatteryCapacity_kWh == 0) {	
	if (batteryAsset != null) {
		batteryAsset.removeEnergyAsset();
	}
}
else {
	double c_rate = 1.0 / zero_Interface.energyModel.avgc_data.p_avgRatioBatteryCapacity_v_Power;
	if (batteryAsset == null) {
		batteryAsset = new J_EAStorageElectric(GC, setBatteryCapacity_kWh * c_rate, setBatteryCapacity_kWh, 0.5, zero_Interface.energyModel.p_timeStep_h);	
	}
	else {		
		if (batteryAsset.getStorageCapacity_kWh() != 0) {
			c_rate = ((J_EAStorageElectric)batteryAsset).getCapacityElectric_kW()/((J_EAStorageElectric)batteryAsset).getStorageCapacity_kWh();
		}
		((J_EAStorageElectric)batteryAsset).setStorageCapacity_kWh(setBatteryCapacity_kWh, GC);
		((J_EAStorageElectric)batteryAsset).setCapacityElectric_kW(c_rate * setBatteryCapacity_kWh);
	}
}

//Add battery algorithm if it is not present
if(GC.f_getBatteryManagement() == null){
	GC.f_setBatteryManagement(new J_BatteryManagementSelfConsumption(GC));
}

/*ALCODEEND*/}

double f_setPVSystem(GridConnection GC,double v_rooftopPV_kWp)
{/*ALCODESTART::1713954180112*/
if (GC.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW)){
	J_EAProduction pvAsset = findFirst(GC.c_productionAssets, p -> p.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC );
	if (v_rooftopPV_kWp == 0) {
		pvAsset.removeEnergyAsset();
	}
	else {
		pvAsset.setCapacityElectric_kW(v_rooftopPV_kWp, GC);
	}
}
else{
	if (v_rooftopPV_kWp != 0) {
		f_addPVAsset(GC, OL_EnergyAssetType.PHOTOVOLTAIC, v_rooftopPV_kWp);
	}
}
/*ALCODEEND*/}

double f_setSliderPresets()
{/*ALCODESTART::1713956765904*/
//Heating radio button
f_setHeatingRB();

//Set grid capacity slider (delivery)
f_setGCCapacitySliderPresets();

//Set connection capacity slider (feedin)
f_setGCCapacitySliderPresets_Feedin();

//PV slider
f_setPVSliderPresets();

//Battery slider
f_setBatSliderPresets();

//Vehicles sliders
f_setVehicleSliderPresets();

//Demand Reduction sliders
f_setDemandReductionSliderPresets();
/*ALCODEEND*/}

double f_setComboBoxOwnedGC()
{/*ALCODESTART::1713961813474*/
String currentSelectedGCString = "";
int i = 1;
List<String> ownedGCs = new ArrayList<String>();
for(GridConnection GC : p_gridConnection.p_owner.f_getOwnedGridConnections()){
	if(GC instanceof GCUtility){
		String GCDisplayName = "Aansluiting " + i + ": " + GC.p_address.getAddress();
		ownedGCs.add(GCDisplayName);
		i++;
		
		if(GC == p_gridConnection){
			currentSelectedGCString = GCDisplayName;
		}
	}
}
String[] ownedGCsArray = new String[ownedGCs.size()];
for(int j = 0; j < ownedGCsArray.length; j++){
	ownedGCsArray[j] = ownedGCs.get(j);
}

cb_selectGC.setItems(ownedGCsArray, false);

//Set cb to correct gc
cb_selectGC.setValue(currentSelectedGCString, false);
/*ALCODEEND*/}

double f_setPVSliderPresets()
{/*ALCODESTART::1714139629738*/
//Set back end range (to prevent anylogic errors)
sl_rooftopPVCompany.setRange(0, 2000000);

//Set range specific for each company
v_minPVSlider = roundToInt(p_scenarioSettings_Current.getCurrentPV_kW());
v_maxPVSlider = roundToInt(zero_Interface.energyModel.avgc_data.p_avgRatioRoofPotentialPV* p_gridConnection.p_roofSurfaceArea_m2*zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2);
if(v_maxPVSlider <= v_minPVSlider){
	v_maxPVSlider = v_minPVSlider + 1000;
}
v_defaultPVSlider = v_minPVSlider;
/*ALCODEEND*/}

double f_setBatSliderPresets()
{/*ALCODESTART::1714139648227*/
//Set back end range (to prevent anylogic errors)
sl_batteryCompany.setRange(0, 10000);

double upperLimit = 1000 + 1000 * Math.ceil(p_scenarioSettings_Current.getCurrentPV_kW()/1000.0);
v_minBatSlider = roundToInt(p_scenarioSettings_Current.getCurrentBatteryCapacity_kWh());
v_maxBatSlider = Math.max(v_minBatSlider*2, upperLimit);
v_defaultBatSlider = v_minBatSlider;
/*ALCODEEND*/}

double f_setVehicleSliderPresets()
{/*ALCODESTART::1714139684603*/
//Cars
f_setCarSliderPresets();

//Vans
f_setVanSliderPresets();

//Trucks
f_setTruckSliderPresets();
/*ALCODEEND*/}

double f_setCarSliderPresets()
{/*ALCODESTART::1714140108358*/
//Set back end range (to prevent anylogic errors)
sl_electricCarsCompany.setRange(0, 500);
sl_petroleumFuelCarsCompany.setRange(0, 500);
sl_hydrogenCarsCompany.setRange(0, 500);


//Get default values
int default_nbEVCars = p_scenarioSettings_Current.getCurrentEVCars();
int default_nbPetroleumFuelCars = p_scenarioSettings_Current.getCurrentPetroleumFuelCars();
int default_nbHydrogenCars = p_scenarioSettings_Current.getCurrentHydrogenCars();

//Set minimum value
v_minEVCarSlider = default_nbEVCars;
v_minPetroleumFuelCarSlider = 0;
v_minHydrogenCarSlider = 0;

//Determine realistic max additional vehicles
int max_additonal_vehicles = p_maxAddedVehicles; //min((default_nbEVCars + default_nbPetroleumFuelCars + default_nbHydrogenCars)*1, 20);

//Set maximum
v_maxEVCarSlider = default_nbEVCars + default_nbPetroleumFuelCars + default_nbHydrogenCars + max_additonal_vehicles; // loading of EV is included in the quarter electricity data --> Cant filter --> cant get less EV than atm.
v_maxPetroleumFuelCarSlider = default_nbPetroleumFuelCars + default_nbHydrogenCars + max_additonal_vehicles;
v_maxHydrogenCarSlider = default_nbPetroleumFuelCars + default_nbHydrogenCars + max_additonal_vehicles;

//Set default values
v_nbEVCars = default_nbEVCars;
v_nbPetroleumFuelCars = default_nbPetroleumFuelCars;
v_nbHydrogenCars = default_nbHydrogenCars;

//Set slider knobs
sl_electricCarsCompany.setValue(v_nbEVCars, false);
sl_petroleumFuelCarsCompany.setValue(v_nbPetroleumFuelCars, false);
sl_hydrogenCarsCompany.setValue(v_nbHydrogenCars, false);

/*ALCODEEND*/}

double f_setVanSliderPresets()
{/*ALCODESTART::1714140134819*/
//Set back end range (to prevent anylogic errors)
sl_electricVansCompany.setRange(0, 500);
sl_petroleumFuelVansCompany.setRange(0, 500);
sl_hydrogenVansCompany.setRange(0, 500);

//Get default values
int default_nbEVVans = p_scenarioSettings_Current.getCurrentEVVans();
int default_nbPetroleumFuelVans = p_scenarioSettings_Current.getCurrentPetroleumFuelVans();
int default_nbHydrogenVans = p_scenarioSettings_Current.getCurrentHydrogenVans();

//Set minimum value
v_minEVVanSlider = default_nbEVVans;
v_minPetroleumFuelVanSlider = 0;
v_minHydrogenVanSlider = 0;

//Determine realistic max additional vehicles
int max_additonal_vehicles = p_maxAddedVehicles; //min((default_nbEVVans + default_nbPetroleumFuelVans + default_nbHydrogenVans)*2, 20);

//Set maximum
v_maxEVVanSlider = default_nbEVVans + default_nbPetroleumFuelVans + default_nbHydrogenVans + max_additonal_vehicles; // loading of EV is included in the quarter electricity data --> Cant filter --> cant get less EV than atm.
v_maxPetroleumFuelVanSlider = default_nbPetroleumFuelVans + default_nbHydrogenVans + max_additonal_vehicles;
v_maxHydrogenVanSlider = default_nbPetroleumFuelVans + default_nbHydrogenVans + max_additonal_vehicles;

//Set default values
v_nbEVVans = default_nbEVVans;
v_nbPetroleumFuelVans = default_nbPetroleumFuelVans;
v_nbHydrogenVans = default_nbHydrogenVans;

//Set slider knob
sl_electricVansCompany.setValue(v_nbEVVans, false);
sl_petroleumFuelVansCompany.setValue(v_nbPetroleumFuelVans, false);
sl_hydrogenVansCompany.setValue(v_nbHydrogenVans, false);

/*ALCODEEND*/}

double f_setTruckSliderPresets()
{/*ALCODESTART::1714140156233*/
//Set back end range (to prevent anylogic errors)
sl_electricTrucksCompany.setRange(0, 500);
sl_petroleumFuelTrucksCompany.setRange(0, 500);
sl_hydrogenTrucksCompany.setRange(0, 500);

//Get default values
int default_nbEVTrucks = p_scenarioSettings_Current.getCurrentEVTrucks();
int default_nbPetroleumFuelTrucks = p_scenarioSettings_Current.getCurrentPetroleumFuelTrucks();
int default_nbHydrogenTrucks = p_scenarioSettings_Current.getCurrentHydrogenTrucks();

//Set minimum value
v_minEVTruckSlider = default_nbEVTrucks;
v_minPetroleumFuelTruckSlider = 0;
v_minHydrogenTruckSlider = 0;

//Determine realistic max additional vehicles
int max_additonal_vehicles = p_maxAddedVehicles; //min((default_nbEVTrucks + default_nbPetroleumFuelTrucks + default_nbHydrogenTrucks)*2, 20);

//Set maximum
v_maxEVTruckSlider = default_nbEVTrucks + default_nbPetroleumFuelTrucks + default_nbHydrogenTrucks + max_additonal_vehicles; // loading of EV is included in the quarter electricity data --> Cant filter --> cant get less EV than atm.
v_maxPetroleumFuelTruckSlider = default_nbPetroleumFuelTrucks + default_nbHydrogenTrucks + max_additonal_vehicles;
v_maxHydrogenTruckSlider = default_nbPetroleumFuelTrucks + default_nbHydrogenTrucks + max_additonal_vehicles;

//Set default values
v_nbEVTrucks = default_nbEVTrucks;
v_nbPetroleumFuelTrucks = default_nbPetroleumFuelTrucks;
v_nbHydrogenTrucks = default_nbHydrogenTrucks;

//Set slider knob
sl_electricTrucksCompany.setValue(v_nbEVTrucks, false);
sl_petroleumFuelTrucksCompany.setValue(v_nbPetroleumFuelTrucks, false);
sl_hydrogenTrucksCompany.setValue(v_nbHydrogenTrucks, false);

/*ALCODEEND*/}

double f_createVehicle(GridConnection parentGC,OL_EnergyAssetType vehicleType,J_ActivityTrackerTrips tripTracker,boolean available,boolean isAdditionalVehicle)
{/*ALCODESTART::1714410040303*/
double energyConsumption_kWhpkm = 0;
double vehicleScaling 			= 1.0;
J_TimeParameters timeParameters	= zero_Interface.energyModel.p_timeParameters;

if (vehicleType == OL_EnergyAssetType.ELECTRIC_VEHICLE || vehicleType == OL_EnergyAssetType.ELECTRIC_VAN || vehicleType == OL_EnergyAssetType.ELECTRIC_TRUCK ){ // Create EVS
	double storageCapacity_kWh 		= 0;
	double capacityElectricity_kW 	= 0;
	double stateOfCharge_fr  		= 1; // Initial state of charge

	switch(vehicleType){
		case ELECTRIC_VEHICLE:
			capacityElectricity_kW	= (p_scenarioSettings_Current.getCurrentEVCarChargePower_kW() > 0) ? p_scenarioSettings_Current.getCurrentEVCarChargePower_kW() : zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerCar_kW;
			storageCapacity_kWh		= zero_Interface.energyModel.avgc_data.p_avgEVStorageCar_kWh;
			energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionCar_kWhpkm;
		break;
		case ELECTRIC_VAN:
			capacityElectricity_kW	= (p_scenarioSettings_Current.getCurrentEVVanChargePower_kW() > 0) ? p_scenarioSettings_Current.getCurrentEVVanChargePower_kW() : zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerVan_kW;
			storageCapacity_kWh		= zero_Interface.energyModel.avgc_data.p_avgEVStorageVan_kWh;
			energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionVan_kWhpkm;
		break;
		case ELECTRIC_TRUCK:
			capacityElectricity_kW	= (p_scenarioSettings_Current.getCurrentEVTruckChargePower_kW() > 0) ? p_scenarioSettings_Current.getCurrentEVTruckChargePower_kW() : zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerTruck_kW;
			storageCapacity_kWh		= zero_Interface.energyModel.avgc_data.p_avgEVStorageTruck_kWh;
			energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionTruck_kWhpkm;
		break;
	
	}
	
	//Create EV and connect to GC and selected trip tracker
	J_EAEV electricVehicle = new J_EAEV(parentGC, capacityElectricity_kW, storageCapacity_kWh, stateOfCharge_fr, timeParameters, energyConsumption_kWhpkm, vehicleScaling, vehicleType, tripTracker, available);	
	
	
	if (isAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).add(electricVehicle);
	}
	else{
		zero_Interface.c_orderedVehicles.add(0, electricVehicle);
	}
}

else if (vehicleType == OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE || vehicleType == OL_EnergyAssetType.PETROLEUM_FUEL_VAN || vehicleType == OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK ){ // Create petroleumFuel vehicles
	switch (vehicleType){
		
		case PETROLEUM_FUEL_VEHICLE:
			energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgGasolineConsumptionCar_kWhpkm;
		break;
		
		case PETROLEUM_FUEL_VAN:
			energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionVan_kWhpkm;
		break;
		
		case PETROLEUM_FUEL_TRUCK:
			energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionTruck_kWhpkm;
		break;
	}
	
	//Create PetroleumFuel vehicle and connect to GC and selected trip tracker
	J_EAFuelVehicle petroleumFuelVehicle = new J_EAFuelVehicle(parentGC, energyConsumption_kWhpkm, timeParameters, vehicleScaling, vehicleType, tripTracker, OL_EnergyCarriers.PETROLEUM_FUEL, available);
	
	
	
	if (isAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).add(petroleumFuelVehicle);
	}
	else{
		zero_Interface.c_orderedVehicles.add(0, petroleumFuelVehicle);
	}
}

else{ // (Hydrogen vehicles)
	switch (vehicleType){
		case HYDROGEN_VEHICLE:
			energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgHydrogenConsumptionCar_kWhpkm;
		break;
		case HYDROGEN_VAN:
			energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgHydrogenConsumptionVan_kWhpkm;
		break;
		case HYDROGEN_TRUCK:
			energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgHydrogenConsumptionTruck_kWhpkm;
		break;
		
	}
	
	//Create Hydrogen vehicle and connect to GC and selected trip tracker
	J_EAFuelVehicle hydrogenVehicle = new J_EAFuelVehicle(parentGC, energyConsumption_kWhpkm, timeParameters, vehicleScaling, vehicleType, tripTracker, OL_EnergyCarriers.HYDROGEN, available);
	
	
	
	if (isAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).add(hydrogenVehicle);
	}
	else{
		zero_Interface.c_orderedVehicles.add(0, hydrogenVehicle);
	}
}


/*ALCODEEND*/}

int f_setElectricVehicleSliders(GridConnection GC,OL_EnergyAssetType vehicleType,int setAmountOfVehicles)
{/*ALCODESTART::1714411599586*/
int local_EV_nb;
int local_PetroleumFuelV_nb;
int local_HydrogenV_nb;
int max_amount_petroleumFuel_vehicles;

OL_EnergyAssetType vehicleType_petroleumFuel;
OL_EnergyAssetType vehicleType_hydrogen;

switch (vehicleType){
	
	case ELECTRIC_VEHICLE:

	vehicleType_petroleumFuel = OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE;
	vehicleType_hydrogen = OL_EnergyAssetType.HYDROGEN_VEHICLE;

	local_EV_nb = v_nbEVCars;
	local_PetroleumFuelV_nb = v_nbPetroleumFuelCars;
	local_HydrogenV_nb = v_nbHydrogenCars;
	
	max_amount_petroleumFuel_vehicles = v_maxPetroleumFuelCarSlider;
	
	break;
	
	case ELECTRIC_VAN:
	
	vehicleType_petroleumFuel = OL_EnergyAssetType.PETROLEUM_FUEL_VAN;
	vehicleType_hydrogen = OL_EnergyAssetType.HYDROGEN_VAN;
	
	local_EV_nb = v_nbEVVans;
	local_PetroleumFuelV_nb = v_nbPetroleumFuelVans;
	local_HydrogenV_nb = v_nbHydrogenVans;
	
	max_amount_petroleumFuel_vehicles = v_maxPetroleumFuelVanSlider;
	
	break;
	
	case ELECTRIC_TRUCK:

	vehicleType_petroleumFuel = OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK;
	vehicleType_hydrogen = OL_EnergyAssetType.HYDROGEN_TRUCK;
	
	local_EV_nb = v_nbEVTrucks;
	local_PetroleumFuelV_nb = v_nbPetroleumFuelTrucks;
	local_HydrogenV_nb = v_nbHydrogenTrucks;
	
	max_amount_petroleumFuel_vehicles = v_maxPetroleumFuelTruckSlider;
		
	break;
	
	default:
	traceln("SLIDER SET TO WRONG VEHICLE TYPE, DO NOTHING");
	return;
}


if (setAmountOfVehicles > local_EV_nb){ // Slider has increased the amount of selected vehicles
	
	//First convert all other existing additional vehicles
	int nbOfOtherAdditionalVehiclesOfThisClass = findAll(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), p -> p.getEAType() == vehicleType_petroleumFuel || p.getEAType() == vehicleType_hydrogen).size();
	while(setAmountOfVehicles > local_EV_nb && nbOfOtherAdditionalVehiclesOfThisClass > 0 ){
		
		// Find an additional PetroleumFuel vehicle
		J_EAFuelVehicle petroleumFuelVehicle = (J_EAFuelVehicle)findFirst(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), p -> p.getEAType() == vehicleType_petroleumFuel);
		
		if(petroleumFuelVehicle != null){
			J_ActivityTrackerTrips tripTracker = petroleumFuelVehicle.getTripTracker();
			
			// Remove PetroleumFuel vehicle		
			boolean available = petroleumFuelVehicle.getAvailability();
			petroleumFuelVehicle.removeEnergyAsset();
			zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).remove(petroleumFuelVehicle);
			zero_Interface.c_orderedVehicles.remove(petroleumFuelVehicle);
			
			//Create new additional EV
			f_createVehicle(GC, vehicleType, tripTracker, available, true);			

			//Update local variables
			local_EV_nb++;
			local_PetroleumFuelV_nb--;
			nbOfOtherAdditionalVehiclesOfThisClass--;
		}
		else{
			// Find an additional Hydrogen vehicle
			J_EAFuelVehicle hydrogenVehicle = (J_EAFuelVehicle)findFirst(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), p -> p.getEAType() == vehicleType_hydrogen);
			J_ActivityTrackerTrips tripTracker = hydrogenVehicle.getTripTracker();
			
			// Remove Hydrogen vehicle		
			boolean available = hydrogenVehicle.getAvailability();
			hydrogenVehicle.removeEnergyAsset();
			zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).remove(hydrogenVehicle);
			zero_Interface.c_orderedVehicles.remove(hydrogenVehicle);
			
			//Create new additional EV
			f_createVehicle(GC, vehicleType, tripTracker, available, true);

			//Update local variables
			local_EV_nb++;
			local_HydrogenV_nb--;
			nbOfOtherAdditionalVehiclesOfThisClass--;
		}
	}
	
	while ( setAmountOfVehicles > local_EV_nb && local_PetroleumFuelV_nb > 0) {
		
		// Find a PetroleumFuel vehicle
		J_EAFuelVehicle petroleumFuelVehicle = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == vehicleType_petroleumFuel && ((GridConnection)p.getOwner()) == GC);
		J_ActivityTrackerTrips tripTracker = petroleumFuelVehicle.getTripTracker(); 
		
		// Remove PetroleumFuel vehicle		
		boolean available = petroleumFuelVehicle.getAvailability();
		zero_Interface.c_orderedVehicles.remove(petroleumFuelVehicle);
		petroleumFuelVehicle.removeEnergyAsset();

		//Create new EV
		f_createVehicle(GC, vehicleType, tripTracker, available, false);
			
		//Update variables
		local_EV_nb++;
		local_PetroleumFuelV_nb--;
	}
	while (setAmountOfVehicles > local_EV_nb && local_HydrogenV_nb > 0){
	
		// Find a Hydrogen vehicle
		J_EAFuelVehicle hydrogenVehicle = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == vehicleType_hydrogen  && ((GridConnection)p.getOwner()) == GC);
		J_ActivityTrackerTrips tripTracker = hydrogenVehicle.getTripTracker();
		
		// Remove Hydrogen vehicle		
		boolean available = hydrogenVehicle.getAvailability();
		zero_Interface.c_orderedVehicles.remove(hydrogenVehicle);
		hydrogenVehicle.removeEnergyAsset();

		//Create new EV
		f_createVehicle(GC, vehicleType, tripTracker, available, false);
	
		//Update variables
		local_EV_nb++;
		local_HydrogenV_nb--;
	}
	while (setAmountOfVehicles > local_EV_nb){ //If still not enough EV:
		
		// Create additional vehicles
		f_createVehicle(GC, vehicleType, null, true, true);
		
		//Update variables
		local_EV_nb++;
	}
	
}
else if(setAmountOfVehicles < local_EV_nb){ // Slider has decreased the amount of selected vehicles
	
	ArrayList<I_Vehicle> additionalVehicles = new ArrayList<>(findAll(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), vehicle -> vehicle.getEAType() == vehicleType ));
	while(setAmountOfVehicles < local_EV_nb && additionalVehicles.size() > 0){ //If there are additional EV, remove them first

		//Find additional created vehicle
		J_EAEV ev = (J_EAEV)additionalVehicles.get(additionalVehicles.size()-1); // Get latest added
		
		// Remove electric vehicle
		additionalVehicles.remove(ev);
		zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).remove(ev);
		zero_Interface.c_orderedVehicles.remove(ev);
		ev.removeEnergyAsset();
			
		//Update variable
		local_EV_nb--;
	}
	while ( setAmountOfVehicles < local_EV_nb && local_PetroleumFuelV_nb < max_amount_petroleumFuel_vehicles) {

		//Find a to be removed EV
		J_EAEV ev = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == vehicleType && !zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).contains(p)  && ((GridConnection)p.getOwner()) == GC);
		J_ActivityTrackerTrips tripTracker = ev.getTripTracker();

		//Remove EV
		boolean available = ev.getAvailability();
		zero_Interface.c_orderedVehicles.remove(ev);
		ev.removeEnergyAsset();
		
		// Create petroleumFuel vehicle	
		f_createVehicle(GC, vehicleType_petroleumFuel, tripTracker, available, false);				
		
		local_EV_nb--;
		local_PetroleumFuelV_nb++;
		}
}



//Update variables
switch (vehicleType){
	
	case ELECTRIC_VEHICLE:
	
	v_nbEVCars = local_EV_nb;
	v_nbPetroleumFuelCars = local_PetroleumFuelV_nb;
	v_nbHydrogenCars = local_HydrogenV_nb;
	
	break;
	
	case ELECTRIC_VAN:
	
	v_nbEVVans = local_EV_nb;
	v_nbPetroleumFuelVans = local_PetroleumFuelV_nb;
	v_nbHydrogenVans = local_HydrogenV_nb;
	
	break;
	
	case ELECTRIC_TRUCK:
	
	v_nbEVTrucks = local_EV_nb;
	v_nbPetroleumFuelTrucks = local_PetroleumFuelV_nb;
	v_nbHydrogenTrucks = local_HydrogenV_nb;
		
	break;
}
/*ALCODEEND*/}

int f_setPetroleumFuelVehicleSliders(GridConnection GC,OL_EnergyAssetType vehicleType,int setAmountOfVehicles)
{/*ALCODESTART::1714471183392*/
int local_EV_nb;
int local_PetroleumFuelV_nb;
int local_HydrogenV_nb;

int min_amount_EV;
int	max_amount_EV;

OL_EnergyAssetType vehicleType_electric;
OL_EnergyAssetType vehicleType_hydrogen;
	
switch (vehicleType){
	
	case PETROLEUM_FUEL_VEHICLE:

	vehicleType_electric = OL_EnergyAssetType.ELECTRIC_VEHICLE;
	vehicleType_hydrogen = OL_EnergyAssetType.HYDROGEN_VEHICLE;

	local_EV_nb = v_nbEVCars;
	local_PetroleumFuelV_nb = v_nbPetroleumFuelCars;
	local_HydrogenV_nb = v_nbHydrogenCars;
	
	min_amount_EV = v_minEVCarSlider;
	max_amount_EV = v_maxEVCarSlider;
	
	break;
	
	case PETROLEUM_FUEL_VAN:
	
	vehicleType_electric = OL_EnergyAssetType.ELECTRIC_VAN;
	vehicleType_hydrogen = OL_EnergyAssetType.HYDROGEN_VAN;
	
	local_EV_nb = v_nbEVVans;
	local_PetroleumFuelV_nb = v_nbPetroleumFuelVans;
	local_HydrogenV_nb = v_nbHydrogenVans;
	
	min_amount_EV = v_minEVVanSlider;
	max_amount_EV = v_maxEVVanSlider;
	
	break;
	
	case PETROLEUM_FUEL_TRUCK:

	vehicleType_electric = OL_EnergyAssetType.ELECTRIC_TRUCK;
	vehicleType_hydrogen = OL_EnergyAssetType.HYDROGEN_TRUCK;
	
	local_EV_nb = v_nbEVTrucks;
	local_PetroleumFuelV_nb = v_nbPetroleumFuelTrucks;
	local_HydrogenV_nb = v_nbHydrogenTrucks;
	
	min_amount_EV = v_minEVTruckSlider;
	max_amount_EV = v_maxEVTruckSlider;
		
	break;
	
	default:
	traceln("SLIDER SET TO WRONG VEHICLE TYPE, DO NOTHING");
	return;
}


if (setAmountOfVehicles > local_PetroleumFuelV_nb){ // Slider has increased the amount of selected vehicles
	//First convert all other existing additional vehicles
	int nbOfOtherAdditionalVehiclesOfThisClass = findAll(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), p -> p.getEAType() == vehicleType_hydrogen || p.getEAType() == vehicleType_electric).size();
	while(setAmountOfVehicles > local_PetroleumFuelV_nb && nbOfOtherAdditionalVehiclesOfThisClass > 0 ){

		// Find an additional EV vehicle
		J_EAEV ev = (J_EAEV)findFirst(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), p -> p.getEAType() == vehicleType_electric);
			
		if(ev != null){
			J_ActivityTrackerTrips tripTracker = ev.getTripTracker();
		
			// Remove EV
			boolean available = ev.getAvailability();
			ev.removeEnergyAsset();
			zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).remove(ev);
			zero_Interface.c_orderedVehicles.remove(ev);
			
			//Create new additional PetroleumFuel vehicle
			f_createVehicle(GC, vehicleType, tripTracker, available, true);

			//Update local variables
			local_PetroleumFuelV_nb++;
			local_EV_nb--;
			nbOfOtherAdditionalVehiclesOfThisClass--;
		}
		else{
			// Find an additional Hydrogen vehicle
			J_EAFuelVehicle hydrogenVehicle = (J_EAFuelVehicle)findFirst(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), p -> p.getEAType() == vehicleType_hydrogen);
			J_ActivityTrackerTrips tripTracker = hydrogenVehicle.getTripTracker();
			
			// Remove Hydrogen vehicle		
			boolean available = hydrogenVehicle.getAvailability();
			hydrogenVehicle.removeEnergyAsset();
			zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).remove(hydrogenVehicle);
			zero_Interface.c_orderedVehicles.remove(hydrogenVehicle);
			
			//Create new additional PetroleumFuel vehicle
			f_createVehicle(GC, vehicleType, tripTracker, available, true);

			//Update local variables
			local_PetroleumFuelV_nb++;
			local_HydrogenV_nb--;
			nbOfOtherAdditionalVehiclesOfThisClass--;
		}
	}
	while ( setAmountOfVehicles > local_PetroleumFuelV_nb && local_EV_nb > min_amount_EV) {

		// Find an EV
		J_EAEV ev = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == vehicleType_electric  && ((GridConnection)p.getOwner()) == GC);
		J_ActivityTrackerTrips tripTracker = ev.getTripTracker();
		
		//Remove one EV
		boolean available = ev.getAvailability();
		zero_Interface.c_orderedVehicles.remove(ev);
		ev.removeEnergyAsset();
		
		//Create new PetroleumFuel vehicle
		f_createVehicle(GC, vehicleType, tripTracker, available, false);
			
		//Update variables
		local_PetroleumFuelV_nb++;
		local_EV_nb--;
	}
	while (setAmountOfVehicles > local_PetroleumFuelV_nb && local_HydrogenV_nb > 0){
	
		// Find a Hydrogen vehicle
		J_EAFuelVehicle hydrogenVehicle = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == vehicleType_hydrogen  && ((GridConnection)p.getOwner()) == GC);
		J_ActivityTrackerTrips tripTracker = hydrogenVehicle.getTripTracker();
		
		// Remove hydrogen vehicle			
		boolean available = hydrogenVehicle.getAvailability();
		zero_Interface.c_orderedVehicles.remove(hydrogenVehicle);
		hydrogenVehicle.removeEnergyAsset();
		
		//Create new PetroleumFuel vehicle
		f_createVehicle(GC, vehicleType, tripTracker, available, false);
	
		//Update variables
		local_PetroleumFuelV_nb++;
		local_HydrogenV_nb--;
	}
	while (setAmountOfVehicles > local_PetroleumFuelV_nb){ // Create additional vehicles
	
	f_createVehicle(GC, vehicleType, null, true, true);

	local_PetroleumFuelV_nb++;
	}
}
else if(setAmountOfVehicles < local_PetroleumFuelV_nb){ // Slider has decreased the amount of selected vehicles
	
	ArrayList<I_Vehicle> additionalVehicles = new ArrayList<>(findAll(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), vehicle -> vehicle.getEAType() == vehicleType ));
	while(setAmountOfVehicles < local_PetroleumFuelV_nb && additionalVehicles.size() > 0){ //Remove additional PetroleumFuel vehicles first
	
	//Find additional created vehicle
	J_EAFuelVehicle petroleumFuelVehicle = (J_EAFuelVehicle)additionalVehicles.get(additionalVehicles.size()-1); // Get latest added
	
	// Remove petroleumFuel vehicle
	additionalVehicles.remove(petroleumFuelVehicle);
	zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).remove(petroleumFuelVehicle);
	petroleumFuelVehicle.removeEnergyAsset();
	zero_Interface.c_orderedVehicles.remove(petroleumFuelVehicle);
	
	//Update variable
	local_PetroleumFuelV_nb--;
	}
	while ( setAmountOfVehicles < local_PetroleumFuelV_nb && local_EV_nb < max_amount_EV) {
	
	// Find a to be removed PetroleumFuel vehicle
		J_EAFuelVehicle petroleumFuelVehicle = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == vehicleType && !zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).contains(p)  && ((GridConnection)p.getOwner()) == GC);
		J_ActivityTrackerTrips tripTracker = petroleumFuelVehicle.getTripTracker();
		
		// Remove petroleumFuel vehicle		
		boolean available = petroleumFuelVehicle.getAvailability();
		zero_Interface.c_orderedVehicles.remove(petroleumFuelVehicle);
		petroleumFuelVehicle.removeEnergyAsset();
		
		//Create new EV
		f_createVehicle(GC, vehicleType_electric, tripTracker, available, false);
			
		//Update variables
		local_PetroleumFuelV_nb--;
		local_EV_nb++;
	}
}



//Update variables
switch (vehicleType){
	
	case PETROLEUM_FUEL_VEHICLE:
	v_nbEVCars = local_EV_nb;
	v_nbPetroleumFuelCars = local_PetroleumFuelV_nb;
	v_nbHydrogenCars = local_HydrogenV_nb;
	break;
	
	case PETROLEUM_FUEL_VAN:
	v_nbEVVans = local_EV_nb;
	v_nbPetroleumFuelVans = local_PetroleumFuelV_nb;
	v_nbHydrogenVans = local_HydrogenV_nb;
	break;
	
	case PETROLEUM_FUEL_TRUCK:
	v_nbEVTrucks = local_EV_nb;
	v_nbPetroleumFuelTrucks = local_PetroleumFuelV_nb;
	v_nbHydrogenTrucks = local_HydrogenV_nb;
	break;
}
/*ALCODEEND*/}

int f_setHydrogenVehicleSliders(GridConnection GC,OL_EnergyAssetType vehicleType,int setAmountOfVehicles)
{/*ALCODESTART::1714474430338*/
int local_EV_nb;
int local_PetroleumFuelV_nb;
int local_HydrogenV_nb;

int min_amount_EV;
int	max_amount_EV;

OL_EnergyAssetType vehicleType_electric;
OL_EnergyAssetType vehicleType_petroleumFuel;
	
switch (vehicleType){
	
	case HYDROGEN_VEHICLE:

	vehicleType_electric = OL_EnergyAssetType.ELECTRIC_VEHICLE;
	vehicleType_petroleumFuel = OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE;

	local_EV_nb = v_nbEVCars;
	local_PetroleumFuelV_nb = v_nbPetroleumFuelCars;
	local_HydrogenV_nb = v_nbHydrogenCars;
	
	min_amount_EV = v_minEVCarSlider;
	max_amount_EV = v_maxEVCarSlider;
	
	break;
	
	case HYDROGEN_VAN:
	
	vehicleType_electric = OL_EnergyAssetType.ELECTRIC_VAN;
	vehicleType_petroleumFuel = OL_EnergyAssetType.PETROLEUM_FUEL_VAN;
	
	local_EV_nb = v_nbEVVans;
	local_PetroleumFuelV_nb = v_nbPetroleumFuelVans;
	local_HydrogenV_nb = v_nbHydrogenVans;
	
	min_amount_EV = v_minEVVanSlider;
	max_amount_EV = v_maxEVVanSlider;
	
	break;
	
	case HYDROGEN_TRUCK:

	vehicleType_electric = OL_EnergyAssetType.ELECTRIC_TRUCK;
	vehicleType_petroleumFuel = OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK;
	
	local_EV_nb = v_nbEVTrucks;
	local_PetroleumFuelV_nb = v_nbPetroleumFuelTrucks;
	local_HydrogenV_nb = v_nbHydrogenTrucks;
	
	min_amount_EV = v_minEVTruckSlider;
	max_amount_EV = v_maxEVTruckSlider;
		
	break;
	
	default:
	traceln("SLIDER SET TO WRONG VEHICLE TYPE, DO NOTHING");
	return;
}

if (setAmountOfVehicles > local_HydrogenV_nb){ // Slider has increased the amount of selected vehicles
	
	//First convert all other existing additional vehicles
	int nbOfOtherAdditionalVehiclesOfThisClass = findAll(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), p -> p.getEAType() == vehicleType_petroleumFuel || p.getEAType() == vehicleType_electric).size();
	while(setAmountOfVehicles > local_HydrogenV_nb && nbOfOtherAdditionalVehiclesOfThisClass > 0 ){
		
		// Find an additional PetroleumFuel vehicle
		J_EAFuelVehicle petroleumFuelVehicle = (J_EAFuelVehicle)findFirst(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), p -> p.getEAType() == vehicleType_petroleumFuel);
		
		if(petroleumFuelVehicle != null){
			J_ActivityTrackerTrips tripTracker = petroleumFuelVehicle.getTripTracker();
			
			// Remove PetroleumFuel vehicle		
			boolean available = petroleumFuelVehicle.getAvailability();
			petroleumFuelVehicle.removeEnergyAsset();
			zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).remove(petroleumFuelVehicle);
			zero_Interface.c_orderedVehicles.remove(petroleumFuelVehicle);
			
			//Create new additional Hydrogen vehicle
			f_createVehicle(GC, vehicleType, tripTracker, available, true);			

			//Update local variables
			local_HydrogenV_nb++;
			local_PetroleumFuelV_nb--;
			nbOfOtherAdditionalVehiclesOfThisClass--;
		}
		else{
			// Find an additional EV vehicle
			J_EAEV ev = (J_EAEV)findFirst(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), p -> p.getEAType() == vehicleType_electric);
			J_ActivityTrackerTrips tripTracker = ev.getTripTracker();
		
			// Remove EV
			boolean available = ev.getAvailability();
			ev.removeEnergyAsset();
			zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).remove(ev);
			zero_Interface.c_orderedVehicles.remove(ev);
			
			//Create new additional Hydrogen vehicle
			f_createVehicle(GC, vehicleType, tripTracker, available, true);

			//Update local variables
			local_HydrogenV_nb++;
			local_EV_nb--;
			nbOfOtherAdditionalVehiclesOfThisClass--;
		}
	}
	while ( setAmountOfVehicles > local_HydrogenV_nb && local_PetroleumFuelV_nb > 0) {

		// Find a to be removed PetroleumFuel vehicle
		J_EAFuelVehicle petroleumFuelVehicle = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == vehicleType_petroleumFuel  && ((GridConnection)p.getOwner()) == GC);
		J_ActivityTrackerTrips tripTracker = petroleumFuelVehicle.getTripTracker();

		//Remove petroleumFuel vehicle
		boolean available = petroleumFuelVehicle.getAvailability();
		zero_Interface.c_orderedVehicles.remove(petroleumFuelVehicle);
		petroleumFuelVehicle.removeEnergyAsset();
		
		//Create new Hydrogen vehicle
		f_createVehicle(GC, vehicleType, tripTracker, available, false);
			
		//Update variables
		local_HydrogenV_nb++;
		local_PetroleumFuelV_nb--;
		
	}
	while (setAmountOfVehicles > local_HydrogenV_nb && local_EV_nb > min_amount_EV){
		
		// Find a to be removed EV
		J_EAEV ev = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == vehicleType_electric  && ((GridConnection)p.getOwner()) == GC);
		J_ActivityTrackerTrips tripTracker = ev.getTripTracker();
		
		// Remove EV
		boolean available = ev.getAvailability();
		zero_Interface.c_orderedVehicles.remove(ev);
		ev.removeEnergyAsset();
		
		//Create new Hydrogen vehicle
		f_createVehicle(GC, vehicleType, tripTracker, available, false);
			
		//Update variables
		local_HydrogenV_nb++;
		local_EV_nb--;
	}
	while (setAmountOfVehicles > local_HydrogenV_nb){ // Create additional vehicles
	
	f_createVehicle(GC, vehicleType, null, true, true);
	local_HydrogenV_nb++;	
	}

	
}
else if(setAmountOfVehicles < local_HydrogenV_nb){ // Slider has decreased the amount of selected vehicles
	
	ArrayList<I_Vehicle> additionalVehicles = new ArrayList<>(findAll(zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid), vehicle -> vehicle.getEAType() == vehicleType ));
	while(setAmountOfVehicles < local_HydrogenV_nb && additionalVehicles.size() > 0){//Remove additional Hydrogen vehicles first

	//Find additional created vehicle
	J_EAFuelVehicle hydrogenVehicle = (J_EAFuelVehicle)additionalVehicles.get(additionalVehicles.size()-1); // Get latest added
	
	// Remove hydrogen vehicle
	additionalVehicles.remove(hydrogenVehicle);
	zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).remove(hydrogenVehicle);
	hydrogenVehicle.removeEnergyAsset();
	zero_Interface.c_orderedVehicles.remove(hydrogenVehicle);
	
	//Update variable
	local_HydrogenV_nb--;
	}
	while ( setAmountOfVehicles < local_HydrogenV_nb && local_EV_nb < max_amount_EV) {
	
		// Find a to be removed Hydrogen vehicle
		J_EAFuelVehicle hydrogenVehicle = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == vehicleType && !zero_Interface.c_additionalVehicles.get(p_gridConnection.p_uid).contains(p)  && ((GridConnection)p.getOwner()) == GC);
		J_ActivityTrackerTrips tripTracker = hydrogenVehicle.getTripTracker();
		
		// Remove hydrogen vehicle			
		boolean available = hydrogenVehicle.getAvailability();
		zero_Interface.c_orderedVehicles.remove(hydrogenVehicle);
		hydrogenVehicle.removeEnergyAsset();
		
		//Create new EV vehicle
		f_createVehicle(GC, vehicleType_electric, tripTracker, available, false);
	
		//Update variables
		local_HydrogenV_nb--;
		local_EV_nb++;
	}
}

//Update variables
switch (vehicleType){
	
	case HYDROGEN_VEHICLE:
	
	v_nbEVCars = local_EV_nb;
	v_nbPetroleumFuelCars = local_PetroleumFuelV_nb;
	v_nbHydrogenCars = local_HydrogenV_nb;
	
	break;
	
	case HYDROGEN_VAN:
	
	v_nbEVVans = local_EV_nb;
	v_nbPetroleumFuelVans = local_PetroleumFuelV_nb;
	v_nbHydrogenVans = local_HydrogenV_nb;
	
	break;
	
	case HYDROGEN_TRUCK:
	
	v_nbEVTrucks = local_EV_nb;
	v_nbPetroleumFuelTrucks = local_PetroleumFuelV_nb;
	v_nbHydrogenTrucks = local_HydrogenV_nb;
	
	break;
}
/*ALCODEEND*/}

double f_setResultsUIPresets()
{/*ALCODESTART::1714654645264*/
//Set the order of the resultsUI to front but behind simulation screen group and load icon
presentation.remove(uI_Results_presentation);
presentation.insert(presentation.size()-1, uI_Results_presentation);
presentation.remove(gr_loadIcon);
presentation.insert(presentation.size()-1, gr_loadIcon);
presentation.remove(gr_simulateYearScreen);
presentation.insert(presentation.size()-1, gr_simulateYearScreen);
presentation.remove(gr_GCisPausedScreen);
presentation.insert(presentation.size()-1, gr_GCisPausedScreen);



//Set the locations and visibilities of the ResultsUI agents
uI_Results.f_setChartProfiles_Presentation(0, 0, true);
uI_Results.f_setChartBalance_Presentation(530, 0, true);
uI_Results.f_setChartGridLoad_Presentation(1060, 0, false);
uI_Results.f_setChartSankey_Presentation(1060, 0, true);
uI_Results.f_setResultsUIHeader(null, null, false);

//Disable KPIsummary button if KPIsummary is not selected
if(zero_Interface.settings.showKPISummary() == null || !zero_Interface.settings.showKPISummary()){
	uI_Results.getCheckbox_KPISummary().setVisible(false);
}
else{
	//uI_Results.f_setCB_KPISummary_Presentation(10, -30, true);
}

//Set selected object display flase
uI_Results.b_isCompanyUIResultsUI = true;

//Set the color of the charts
uI_Results.f_styleAllCharts(v_chartBackgroundColor, v_companyUILineColor, v_chartLineWidth, LINE_STYLE_SOLID);
/*ALCODEEND*/}

double f_setCompanyUI(GridConnection GC)
{/*ALCODESTART::1714655282643*/
//Initialize parameters
p_gridConnection = GC;
p_companyName = GC.p_ownerID;
v_adressGC = GC.p_address.getAddress();
p_scenarioSettings_Current = zero_Interface.c_scenarioMap_Current.get(GC.p_uid);
p_scenarioSettings_Future = zero_Interface.c_scenarioMap_Future.get(GC.p_uid);

//Scale companyName to the box size
f_setNameTextSize();

//Set the sliders to the correct settings
f_setSelectedGCSliders();

//Set the new graphs/building selection
if(!b_runningMainInterfaceScenarioSettings && !b_runningMainInterfaceSlider && p_gridConnection.v_isActive){
	f_updateUIResultsCompanyUI();
	if(p_gridConnection.v_rapidRunData != null){
		uI_Results.f_setAllCharts();
	}
}

//Set connected GC combobox 
f_setComboBoxOwnedGC();

//Enable/disable all sliders (based on paused)
f_enableAllSliders(p_gridConnection.v_isActive);

/*ALCODEEND*/}

double f_updateUIResultsCompanyUI()
{/*ALCODESTART::1714656835269*/
uI_Results.f_updateResultsUI(p_gridConnection);
uI_Results.f_setChartProfiles_Presentation(null, null, true);
uI_Results.f_setChartBalance_Presentation(null, null, true);

if(cb_showGridloadPlot.isSelected())
	uI_Results.f_setChartGridLoad_Presentation(null, null, true);
else{
	uI_Results.f_setChartSankey_Presentation(null, null, true);
}
/*ALCODEEND*/}

double f_setHeatingRB()
{/*ALCODESTART::1715713362876*/
int nr_currentHeatingType = 0;
String rbHeating_acces = "enabled";

switch (p_scenarioSettings_Current.getCurrentHeatingType()){
	case GAS_BURNER:
		nr_currentHeatingType = 0;
		break;
	case HYBRID_HEATPUMP:
		nr_currentHeatingType = 1;
		rbHeating_acces = "disabled";
		break;
	case ELECTRIC_HEATPUMP:
		nr_currentHeatingType = 2;
		rbHeating_acces = "disabled";
		break;
	case DISTRICTHEAT:
		nr_currentHeatingType = 3;
		break;
	case LT_DISTRICTHEAT:
		nr_currentHeatingType = 3;
		rbHeating_acces = "disabled";
		break;
	case CUSTOM:
		nr_currentHeatingType = 4;
		rbHeating_acces = "disabled";
		break;
	default:
		rbHeating_acces = "invisible";
}

if (rbHeating_acces.equals("disabled") || rbHeating_acces.equals("invisible")){
	rb_heatingTypePrivateUI.setEnabled(false);
	
	if(p_gridConnection.v_hasQuarterHourlyValues){
		sl_heatDemandCompanyReduction.setEnabled(false);
	}
	
	if (rbHeating_acces.equals("invisible")){
		rb_heatingTypePrivateUI.setVisible(false);
		gr_heatDemandReductionSlider.setVisible(false);
	}
	else {
		gr_heatDemandReductionSlider.setVisible(true);
	}
}
else{ // if(rbHeating_acces.equals("enabled"){
	rb_heatingTypePrivateUI.setEnabled(true);
	rb_heatingTypePrivateUI.setVisible(true);
	sl_heatDemandCompanyReduction.setEnabled(true);
	gr_heatDemandReductionSlider.setVisible(true);
}

/*ALCODEEND*/}

double f_addPVAsset(GridConnection parentGC,OL_EnergyAssetType asset_type,double installedPower_kW)
{/*ALCODESTART::1715952034311*/
String asset_name					= "Solar Panels";
double capacityElectric_kW			= installedPower_kW;
double capacityHeat_kW				= 0;
double yearlyProductionMethane_kWh 	= 0;
double yearlyProductionHydrogen_kWh = 0;
J_TimeParameters timeParameters 					= zero_Interface.energyModel.p_timeParameters;
double outputTemperature_degC 		= 0;

J_EAProduction production_asset = new J_EAProduction(parentGC, asset_type, asset_name, OL_EnergyCarriers.ELECTRICITY, capacityElectric_kW, timeParameters, zero_Interface.energyModel.pp_PVProduction35DegSouth_fr);
parentGC.v_liveAssetsMetaData.updateActiveAssetData(new ArrayList<GridConnection>(List.of(parentGC)));

/*ALCODEEND*/}

double f_setSelectedGCSliders()
{/*ALCODESTART::1725439625846*/
//Reset GC capacities to without NFATO values
p_gridConnection.f_nfatoSetConnectionCapacity(true, zero_Interface.energyModel.p_timeVariables);
	
//Initialize slider presets to selected GC (min, max, etc.)
f_setSliderPresets();

//If GC not active in current situation, disable scenario rb
rb_scenariosPrivateUI.setEnabled(p_scenarioSettings_Current.getIsCurrentlyActive());

//Find the current heating type
int nr_currentHeatingType = 0;
switch (p_gridConnection.f_getCurrentHeatingType()){
	case GAS_BURNER:
		nr_currentHeatingType = 0;
	break;

	case HYBRID_HEATPUMP:
		nr_currentHeatingType = 1;
	break;

	case ELECTRIC_HEATPUMP:
		nr_currentHeatingType = 2;
	break;
		
	case DISTRICTHEAT:
	case LT_DISTRICTHEAT:
		nr_currentHeatingType = 3;
	break;
	case CUSTOM:
		nr_currentHeatingType = 4;
	break;	
	default:
		nr_currentHeatingType = 4;
}

//Find the current heat saving percentage
int currentHeatSavings = 0;

J_EAConsumption consumptionEAHEAT = findFirst(p_gridConnection.c_consumptionAssets, consumptionAsset -> consumptionAsset.getEAType() == OL_EnergyAssetType.HEAT_DEMAND);
if (consumptionEAHEAT != null){
	currentHeatSavings = roundToInt((consumptionEAHEAT.getConsumptionScaling_fr() - 1)*-100);
}
else{   
	J_EAProfile profileEAHEAT = findFirst(p_gridConnection.c_profileAssets, profileAsset -> profileAsset.energyCarrier == OL_EnergyCarriers.HEAT);
	if (profileEAHEAT != null){
		currentHeatSavings = roundToInt((profileEAHEAT.getProfileScaling_fr() - 1)*-100);
	}
}

//Find the current electricity savings percentage
int currentElectricitySavings = 0;

J_EAConsumption consumptionEAELECTRIC = findFirst(p_gridConnection.c_consumptionAssets, consumptionAsset -> consumptionAsset.getEAType() == OL_EnergyAssetType.ELECTRICITY_DEMAND);
if (consumptionEAELECTRIC != null){
	currentElectricitySavings = roundToInt((consumptionEAELECTRIC.getConsumptionScaling_fr() - 1)*-100);
}
else{
	J_EAProfile profileEAELECTRIC = findFirst(p_gridConnection.c_profileAssets, profileAsset -> profileAsset.getAssetFlowCategory() == OL_AssetFlowCategories.fixedConsumptionElectric_kW);
	if (profileEAELECTRIC != null){
		currentElectricitySavings = roundToInt((profileEAELECTRIC.getProfileScaling_fr() - 1)*-100);
	}
}

//Find the current Connection capacity (delivery)
int GCContractCapacityCurrent_Delivery = roundToInt(p_gridConnection.v_liveConnectionMetaData.contractedDeliveryCapacity_kW);

//Find the current Connection capacity (feedin)
int GCContractCapacityCurrent_Feedin = roundToInt(p_gridConnection.v_liveConnectionMetaData.contractedFeedinCapacity_kW);

//Set the nfato values
f_getNFATOValues();

//Find the current battery capacity
int BatteryCapacityCurrent = 0;
J_EAStorage batteryAsset = findFirst(p_gridConnection.c_storageAssets, p -> p.getEAType() == OL_EnergyAssetType.STORAGE_ELECTRIC );
if (batteryAsset != null){
	BatteryCapacityCurrent = roundToInt(((J_EAStorageElectric)batteryAsset).getStorageCapacity_kWh());
}

//Find the current PV capacity
int PVCapacityCurrent = 0;
if (p_gridConnection.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW)){
	J_EAProduction pvAsset = findFirst(p_gridConnection.c_productionAssets, p -> p.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC );
	PVCapacityCurrent = roundToInt(pvAsset.getCapacityElectric_kW());
}

//Find the current curtailment setting
boolean currentCurtailmentSetting = p_gridConnection.v_enableCurtailment;

//Find the current transport savings
int currentTransportSavings = 0;
if (p_gridConnection.c_tripTrackers.size() > 0){
	currentTransportSavings = - roundToInt(p_gridConnection.c_tripTrackers.get(0).getDistanceScaling_fr()*100) + 100;
}


//Find the current number of vehicles for each type
int nbEcarsCurrent = count(p_gridConnection.c_electricVehicles, p->p.getEAType() == OL_EnergyAssetType.ELECTRIC_VEHICLE);
int nbHydrogencarsCurrent = count(p_gridConnection.c_hydrogenVehicles, p->p.getEAType() == OL_EnergyAssetType.HYDROGEN_VEHICLE);
int nbPetroleumFuelcarsCurrent = count(p_gridConnection.c_petroleumFuelVehicles, p->p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE);

int nbEvansCurrent = count(p_gridConnection.c_electricVehicles, p->p.getEAType() == OL_EnergyAssetType.ELECTRIC_VAN);
int nbHydrogenvansCurrent = count(p_gridConnection.c_hydrogenVehicles, p->p.getEAType() == OL_EnergyAssetType.HYDROGEN_VAN);
int nbPetroleumFuelvansCurrent = count(p_gridConnection.c_petroleumFuelVehicles, p->p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VAN);

int nbEtrucksCurrent = count(p_gridConnection.c_electricVehicles, p->p.getEAType() == OL_EnergyAssetType.ELECTRIC_TRUCK);
int nbHydrogentrucksCurrent = count(p_gridConnection.c_hydrogenVehicles, p->p.getEAType() == OL_EnergyAssetType.HYDROGEN_TRUCK);
int nbPetroleumFueltrucksCurrent = count(p_gridConnection.c_petroleumFuelVehicles, p->p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK);

//Check on electric cars, cause for companies that have quarterlyhour electricity data, the initial ea for EV (and other electric appliances) are not made.
if (p_gridConnection.v_hasQuarterHourlyValues){
	
	nbEcarsCurrent += v_minEVCarSlider;
	nbEvansCurrent += v_minEVVanSlider;
	nbEtrucksCurrent += v_minEVTruckSlider;
}


////Set slider knobs at the currently (!) correct points

//heating
rb_heatingTypePrivateUI.setValue(nr_currentHeatingType, false);		

//Heat savings
sl_heatDemandCompanyReduction.setValue(currentHeatSavings, false);

//Electricity savings
sl_electricityDemandCompanyReduction.setValue(currentElectricitySavings, false);

//Contract connection capacity (delivery)
sl_GCCapacityCompany.setValue(GCContractCapacityCurrent_Delivery, false);
v_defaultGCCapacitySlider = GCContractCapacityCurrent_Delivery;

//Contract connection capacity (feedin)
sl_GCCapacityCompany_Feedin.setValue(GCContractCapacityCurrent_Feedin, false);
v_defaultGCCapacitySlider_Feedin = GCContractCapacityCurrent_Feedin;

//Battery capacity
sl_batteryCompany.setValue(BatteryCapacityCurrent, false);
v_defaultBatSlider = BatteryCapacityCurrent;

//Solar panel power
sl_rooftopPVCompany.setValue(PVCapacityCurrent, false);
v_defaultPVSlider = PVCapacityCurrent;

//Curtailment setting
cb_curtailmentCompany.setSelected(currentCurtailmentSetting, false);


//Mobility savings
sl_mobilityDemandCompanyReduction.setValue(currentTransportSavings, false);

//Cars 
sl_electricCarsCompany.setValue(nbEcarsCurrent, false);
sl_hydrogenCarsCompany.setValue(nbHydrogencarsCurrent, false);
sl_petroleumFuelCarsCompany.setValue(nbPetroleumFuelcarsCurrent, false);

v_nbEVCars = nbEcarsCurrent;
v_nbHydrogenCars = nbHydrogencarsCurrent;
v_nbPetroleumFuelCars = nbPetroleumFuelcarsCurrent;


//Vans
sl_electricVansCompany.setValue(nbEvansCurrent, false);
sl_hydrogenVansCompany.setValue(nbHydrogenvansCurrent, false);
sl_petroleumFuelVansCompany.setValue(nbPetroleumFuelvansCurrent, false);

v_nbEVVans = nbEvansCurrent;
v_nbHydrogenVans = nbHydrogenvansCurrent;
v_nbPetroleumFuelVans = nbPetroleumFuelvansCurrent;


//Trucks
sl_electricTrucksCompany.setValue(nbEtrucksCurrent, false);
sl_hydrogenTrucksCompany.setValue(nbHydrogentrucksCurrent, false);
sl_petroleumFuelTrucksCompany.setValue(nbPetroleumFueltrucksCurrent, false);

v_nbEVTrucks = nbEtrucksCurrent;
v_nbHydrogenTrucks = nbHydrogentrucksCurrent;
v_nbPetroleumFuelTrucks = nbPetroleumFueltrucksCurrent;

//Add nfato again
p_gridConnection.f_nfatoSetConnectionCapacity(false, zero_Interface.energyModel.p_timeVariables);
/*ALCODEEND*/}

double f_selectGCOnMainInterface()
{/*ALCODESTART::1725439635605*/
//Select the newly selected GC also on the main interface (if not paused)
zero_Interface.f_clearSelectionAndSelectEnergyModel();

if(p_gridConnection.v_isActive){
	zero_Interface.f_selectBuilding(p_gridConnection.c_connectedGISObjects.get(0), p_gridConnection.c_connectedGISObjects);
}
/*ALCODEEND*/}

double f_setSimulateYearScreen()
{/*ALCODESTART::1725607045331*/
if(!b_runningMainInterfaceScenarioSettings && !b_runningMainInterfaceSlider){
	//Update main interface sliders according to the companyUI changes 
	zero_Interface.f_updateMainInterfaceSliders();
	
	//Set it for main interface as well
	zero_Interface.f_resetSettings();
	
	//Update variable to change to custom scenario
	zero_Interface.f_setScenarioToCustom();
}
/*ALCODEEND*/}

double f_setGCCapacitySliderPresets()
{/*ALCODESTART::1725614403909*/
//Set back end range (to prevent anylogic errors)
sl_GCCapacityCompany.setRange(0, 100000);

//Get current grid capacity
double defaultGCCapacitySlider = p_scenarioSettings_Current.getCurrentContractDeliveryCapacity_kW();

//Get future grid capacity
double futureGCCapacity_delivery_kW = p_scenarioSettings_Future.getRequestedContractDeliveryCapacity_kW();

//Get current physical capacity
v_physicalConnectionCapacity_kW = p_scenarioSettings_Current.getCurrentPhysicalConnectionCapacity_kW();

//Set range specific for specific intervals of capacity
v_minGCCapacitySlider = 0;

if(futureGCCapacity_delivery_kW < 100 && defaultGCCapacitySlider < 100){
	v_maxGCCapacitySlider = 150;
}
else if(futureGCCapacity_delivery_kW < 1000 && defaultGCCapacitySlider < 1000){
	v_maxGCCapacitySlider = 2000;
}
else if(futureGCCapacity_delivery_kW < 8000 && defaultGCCapacitySlider < 8000){
	v_maxGCCapacitySlider = 10000;
}
else if(futureGCCapacity_delivery_kW < 15000 && defaultGCCapacitySlider < 15000){
	v_maxGCCapacitySlider = 20000;
}
else{
	v_maxGCCapacitySlider = max(futureGCCapacity_delivery_kW, defaultGCCapacitySlider);
}

v_defaultGCCapacitySlider = roundToInt(defaultGCCapacitySlider);

//Set slider knob
sl_GCCapacityCompany.setValue(v_defaultGCCapacitySlider, false);
/*ALCODEEND*/}

double f_setNameTextSize()
{/*ALCODESTART::1727712593952*/
if (p_companyName == null) {
    return;
}

int nameLength = p_companyName.length();

int i = 0;
if(nameLength > 24){
	while(24+i != nameLength){
	
		t_companyName.setScale(0.9);
		i++;
	}
}
//Works for now: Possible to make it more accurate using getFontMetrics package and comparing width of text with the name text box width.
//--> Not done for now, as it feels unnecessary.
/*ALCODEEND*/}

double f_setGCCapacitySliderPresets_Feedin()
{/*ALCODESTART::1727798399503*/
//Set back end range (to prevent anylogic errors)
sl_GCCapacityCompany_Feedin.setRange(0, 100000);

//Get current grid capacity
double defaultGCCapacitySlider_Feedin = p_scenarioSettings_Current.getCurrentContractFeedinCapacity_kW();

//Set range specific for specific intervals of capacity
v_minGCCapacitySlider_Feedin = 0;

if(defaultGCCapacitySlider_Feedin < 100){
	v_maxGCCapacitySlider_Feedin = 150;
}
else if(defaultGCCapacitySlider_Feedin < 1000){
	v_maxGCCapacitySlider_Feedin = 2000;
}
else if(defaultGCCapacitySlider_Feedin < 8000){
	v_maxGCCapacitySlider_Feedin = 10000;
}
else if(defaultGCCapacitySlider_Feedin < 15000){
	v_maxGCCapacitySlider_Feedin = 20000;
}
else{
	v_maxGCCapacitySlider_Feedin = defaultGCCapacitySlider_Feedin;
}

v_defaultGCCapacitySlider_Feedin = roundToInt(defaultGCCapacitySlider_Feedin);

//Set slider knob
sl_GCCapacityCompany_Feedin.setValue(v_defaultGCCapacitySlider_Feedin, false);
/*ALCODEEND*/}

double f_getNFATOValues()
{/*ALCODESTART::1727884380899*/
v_NFATO_active = p_gridConnection.v_enableNFato;
v_NFATO_kW_delivery = p_gridConnection.v_liveConnectionMetaData.contractedDeliveryCapacity_kW - v_defaultGCCapacitySlider;
v_NFATO_kW_feedin = p_gridConnection.v_liveConnectionMetaData.contractedFeedinCapacity_kW- v_defaultGCCapacitySlider_Feedin;

if(v_NFATO_kW_delivery > 0){
	t_GCCapacityCompany_delivery_nfato.setColor(green);
}
else if(v_NFATO_kW_delivery < 0){
	t_GCCapacityCompany_delivery_nfato.setColor(red);
}
else{
	t_GCCapacityCompany_delivery_nfato.setColor(black);
}

if(v_NFATO_kW_feedin > 0){
	t_GCCapacityCompany_Feedin_nfato.setColor(green);
}
else if(v_NFATO_kW_feedin < 0){
	t_GCCapacityCompany_Feedin_nfato.setColor(red);
}
else{
	t_GCCapacityCompany_Feedin_nfato.setColor(black);
}

/*ALCODEEND*/}

double f_enableAllSliders(boolean enable)
{/*ALCODESTART::1729515671654*/
sl_heatDemandCompanyReduction.setEnabled(enable);
rb_heatingTypePrivateUI.setEnabled(enable);

sl_electricityDemandCompanyReduction.setEnabled(enable);
sl_GCCapacityCompany.setEnabled(enable);
sl_GCCapacityCompany_Feedin.setEnabled(enable);
sl_batteryCompany.setEnabled(enable);
sl_rooftopPVCompany.setEnabled(enable);
cb_curtailmentCompany.setEnabled(enable);

sl_mobilityDemandCompanyReduction.setEnabled(enable);

sl_electricCarsCompany.setEnabled(enable);
sl_hydrogenCarsCompany.setEnabled(enable);
sl_petroleumFuelCarsCompany.setEnabled(enable);

sl_electricVansCompany.setEnabled(enable);
sl_hydrogenVansCompany.setEnabled(enable);
sl_petroleumFuelVansCompany.setEnabled(enable);

sl_electricTrucksCompany.setEnabled(enable);
sl_hydrogenTrucksCompany.setEnabled(enable);
sl_petroleumFuelTrucksCompany.setEnabled(enable);

// Disabled / Invisible heating based on current scenario settings
if (enable) {
	f_setHeatingRB();
}
/*ALCODEEND*/}

double f_setErrorScreen(String errorMessage)
{/*ALCODESTART::1747316158336*/
//Reset location and height
button_errorOK.setY(50);
rect_errorMessage.setY(-120);
rect_errorMessage.setHeight(200);
t_errorMessage.setY(-70);

//Set position above all other things
presentation.remove(gr_errorScreen);
presentation.insert(presentation.size(), gr_errorScreen);

int width_numberOfCharacters = 44;

// Set Text
Pair<String, Integer> p = zero_Interface.v_infoText.restrictWidth(errorMessage, width_numberOfCharacters);
errorMessage = p.getFirst();
int numberOfLines = p.getSecond();
int additionalLines = max(0, numberOfLines - 3);

// Set Size
rect_errorMessage.setHeight(rect_errorMessage.getHeight() + additionalLines * 40);
rect_errorMessage.setY(rect_errorMessage.getY() - 40 * additionalLines);
//button_errorOK.setY(button_errorOK.getY() - 10 * additionalLines);
t_errorMessage.setY(t_errorMessage.getY() - 40 * additionalLines);

t_errorMessage.setText(errorMessage);
gr_errorScreen.setVisible(true);
/*ALCODEEND*/}

double f_setDemandReductionSliderPresets()
{/*ALCODESTART::1756898097088*/
v_minSavingsSliders = p_minSavingsSliders_default;
v_maxSavingsSliders = p_maxSavingsSliders_default;
sl_heatDemandCompanyReduction.setRange(v_minSavingsSliders, v_maxSavingsSliders);
sl_electricityDemandCompanyReduction.setRange(v_minSavingsSliders, v_maxSavingsSliders);
sl_mobilityDemandCompanyReduction.setRange(v_minSavingsSliders, v_maxSavingsSliders);
/*ALCODEEND*/}

double f_selectDifferentOwnedGC(int selectedOwnedGCIndex)
{/*ALCODESTART::1760976810989*/
//Set companyUI to the new GC
f_setCompanyUI(p_gridConnection.p_owner.f_getOwnedGridConnections().get(selectedOwnedGCIndex));

//Select the gc on the main interface (map) aswell
f_selectGCOnMainInterface();


/*ALCODEEND*/}

