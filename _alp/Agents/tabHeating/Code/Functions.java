int f_setHeatingSystemsCompanies(List<GCUtility> gcList,ShapeSlider sliderGasburner,ShapeSlider sliderHeatpump,ShapeSlider sliderHybridHeatPump,ShapeSlider sliderDistrictHeating,ShapeSlider sliderCustomHeating)
{/*ALCODESTART::1722256102007*/
double targetHeatPump_pct = sliderHeatpump.getValue();

//Set the sliders if companyUI is present using the companyUI functions, if not: do it the normal way
if(zero_Interface.c_companyUIs.size()>0){
 	f_setHeatingSystemsWithCompanyUI(gcList, targetHeatPump_pct, sliderGasburner, sliderHeatpump, sliderHybridHeatPump, sliderDistrictHeating, sliderCustomHeating);
}
else{
	ArrayList<GCUtility> companies = new ArrayList<GCUtility>(zero_Interface.c_orderedHeatingSystemsCompanies.stream().filter(gcList::contains).toList());
	double nbHeatPumps = count(gcList, gc -> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
	int targetHeatPumpAmount = roundToInt( targetHeatPump_pct / 100.0 * gcList.size());
	
	while ( targetHeatPumpAmount < nbHeatPumps) { // remove excess heatpumps, replace with gasburners.
		GCUtility company = findFirst(companies, x->x.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
		if (company != null) {
			company.c_heatingAssets.get(0).removeEnergyAsset();
			nbHeatPumps--;
			companies.remove(company);
			zero_Interface.c_orderedHeatingSystemsCompanies.remove(company);
			zero_Interface.c_orderedHeatingSystemsCompanies.add(0, company);
			double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(company);
			new J_EAConversionGasBurner(company, peakHeatDemand_kW, zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC);
			company.f_addHeatManagementToGC(company, OL_GridConnectionHeatingType.GAS_BURNER, false);
		}
		else {
			throw new RuntimeException("Can't find Heatpump in company that should have heatpump in f_setHeatingSystemsCompanies.");
		}
	}
	
	while ( targetHeatPumpAmount > nbHeatPumps) { // remove gasburners, add heatpumps.
		GCUtility company = findFirst(companies, x -> x.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.GAS_BURNER);
		if (company != null) {			
			company.c_heatingAssets.get(0).removeEnergyAsset();
			nbHeatPumps++;		
			companies.remove(company);
			zero_Interface.c_orderedHeatingSystemsCompanies.remove(company);
			zero_Interface.c_orderedHeatingSystemsCompanies.add(0, company);	
			double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(company);
			new J_EAConversionHeatPump(company, peakHeatDemand_kW, 0.5, zero_Interface.energyModel.p_timeStep_h, 60,  zero_Interface.energyModel.pp_ambientTemperature_degC.getCurrentValue(), 0, 1, OL_AmbientTempType.AMBIENT_AIR);				
			company.f_addHeatManagementToGC(company, OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, false);
		} 
		else {
			throw new RuntimeException("Can't find Gasburner in company that should have gasburner in f_setHeatingSystemsCompanies.");
		}	
	}	
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setHeatingSystemsHouseholds(List<GCHouse> gcList,double targetHeatPump_pct)
{/*ALCODESTART::1722256221655*/
ArrayList<GCHouse> houses = new ArrayList<GCHouse>(zero_Interface.c_orderedHeatingSystemsHouses.stream().filter(gcList::contains).toList());
double nbHeatPumps = count(gcList, gc -> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
int targetHeatPumpAmount = roundToInt( targetHeatPump_pct / 100.0 * gcList.size());

while ( nbHeatPumps > targetHeatPumpAmount) { // remove excess heatpumps, replace with gasburners.
	GCHouse house = findFirst(houses, x->x.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
	if (house != null) {
		house.c_heatingAssets.get(0).removeEnergyAsset();
		nbHeatPumps--;
		houses.remove(house);
		zero_Interface.c_orderedHeatingSystemsHouses.remove(house);
		zero_Interface.c_orderedHeatingSystemsHouses.add(0, house);
		double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(house);
		new J_EAConversionGasBurner(house, peakHeatDemand_kW, zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC);
		house.f_addHeatManagementToGC(house, OL_GridConnectionHeatingType.GAS_BURNER, false);
	}
	else {
		throw new RuntimeException("Can't find Heatpump in house that should have heatpump in f_setHeatingSystemsHouseholds.");
	}
}

while ( nbHeatPumps < targetHeatPumpAmount) { // remove gasburners, add heatpumps.
	GCHouse house = findFirst(houses, x -> x.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.GAS_BURNER);
	if (house != null) {
		house.c_heatingAssets.get(0).removeEnergyAsset();
		nbHeatPumps++;
		houses.remove(house);
		zero_Interface.c_orderedHeatingSystemsHouses.remove(house);
		zero_Interface.c_orderedHeatingSystemsHouses.add(0, house);		
		double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(house);
		new J_EAConversionHeatPump(house, peakHeatDemand_kW/3, 0.5, zero_Interface.energyModel.p_timeStep_h, 60,  zero_Interface.energyModel.pp_ambientTemperature_degC.getCurrentValue(), 0, 1, OL_AmbientTempType.AMBIENT_AIR);				
		house.f_addHeatManagementToGC(house, OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, false);
	} 
	else {
		throw new RuntimeException("Can't find Gasburner in house that should have gasburner in f_setHeatingSystemsHouseholds.");
	}	
}	

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setDemandReductionHeating(List<GridConnection> gcList,double demandReduction_pct)
{/*ALCODESTART::1722335783993*/
double scalingFactor = 1 - demandReduction_pct/100;

for (GridConnection gc : gcList) {
	// Set Consumption Assets
	for (J_EAConsumption j_ea : gc.c_consumptionAssets) {
		if (j_ea.getEAType() == OL_EnergyAssetType.HEAT_DEMAND) {
			j_ea.setConsumptionScaling_fr( scalingFactor );
		}
	}
	// Set Profile Assets
	for (J_EAProfile j_ea : gc.c_profileAssets) {
		if (j_ea.energyCarrier == OL_EnergyCarriers.HEAT) {
			j_ea.setProfileScaling_fr( scalingFactor );
		}
	}
	
	if(gc.p_BuildingThermalAsset != null){
		gc.p_BuildingThermalAsset.setLossScalingFactor_fr(scalingFactor);
	}
	
	// Update Company UI
	if (zero_Interface.c_companyUIs.size()>0){
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
			companyUI.sl_heatDemandCompanyReduction.setValue(demandReduction_pct, false);
		}
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

int f_setHeatingSystemsWithCompanyUI(List<GCUtility> gcList,double targetHeatPump_pct,ShapeSlider sliderGasburner,ShapeSlider sliderHeatpump,ShapeSlider sliderHybridHeatPump,ShapeSlider sliderDistrictHeating,ShapeSlider sliderCustomHeating)
{/*ALCODESTART::1729259449060*/
ArrayList<GCUtility> companies = new ArrayList<GCUtility>(zero_Interface.c_orderedHeatingSystemsCompanies.stream().filter(gcList::contains).filter(x -> x.v_isActive).toList());
int nbActiveCompanies = companies.size() + v_totalNumberOfCustomHeatingSystems;
int nbHeatPumps = count(companies, gc -> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
int targetHeatPumpAmount = roundToInt( targetHeatPump_pct / 100.0 * nbActiveCompanies);


while ( targetHeatPumpAmount < nbHeatPumps){ // remove excess heatpumps of companies that didnt start with a heatpump, replace with gasburners.
	GCUtility company = findFirst(companies, gc -> gc.p_heatingManagement != null && !(gc.p_heatingManagement instanceof J_HeatingManagementGhost) && gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
	if (company != null) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(company.p_owner.p_connectionOwnerIndexNr);
		
		companyUI.b_runningMainInterfaceSlider = true;
		if(companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) != company){
			int i = indexOf(companyUI.c_ownedGridConnections.stream().toArray(), company);
			companyUI.GCnr_selection.setValue(i, true);
		}
		// rbSetting Bugfix, does not change the heating system if the radiobutton was disabled!			
		boolean rbSetting = companyUI.rb_heatingTypePrivateUI.isEnabled();
		companyUI.rb_heatingTypePrivateUI.setEnabled(true);
		companyUI.rb_heatingTypePrivateUI.setValue(0, true); // First option is gasburner
		companyUI.rb_heatingTypePrivateUI.setEnabled(rbSetting);
		companyUI.rb_scenariosPrivateUI.setValue(2, false);
		companyUI.b_runningMainInterfaceSlider = false;

		//Reorder c_orderedHeatingSystems
		zero_Interface.c_orderedHeatingSystemsCompanies.remove(company);
		zero_Interface.c_orderedHeatingSystemsCompanies.add(0, company);
		companies.remove(company);
		nbHeatPumps--;
	}
	else { //No more heating assets to adjust: this is the minimum: set slider to minimum and do nothing else
		int min_nbOfHeatpumps = count(gcList, gc -> gc.v_isActive && gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
		int min_pct_ElectricHeatpumpSlider = roundToInt( min_nbOfHeatpumps * 100.0 / nbActiveCompanies );
		sliderHeatpump.setValue(min_pct_ElectricHeatpumpSlider, false);
		f_setHeatingSliders(1, sliderGasburner, sliderHeatpump, sliderHybridHeatPump, sliderDistrictHeating, sliderCustomHeating);
		return;
	}
}

while ( targetHeatPumpAmount > nbHeatPumps) { // remove gasburners, add heatpumps.
		
	GCUtility company = findFirst(companies, gc -> gc.p_heatingManagement != null && !(gc.p_heatingManagement instanceof J_HeatingManagementGhost) && gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.GAS_BURNER);
	if (company != null) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(company.p_owner.p_connectionOwnerIndexNr);
		companyUI.b_runningMainInterfaceSlider = true;
		if(companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) != company){
			int i = indexOf(companyUI.c_ownedGridConnections.stream().toArray(), company);
			companyUI.GCnr_selection.setValue(i, true);
		}
		
		// rbSetting Bugfix, does not change the heating system if the radiobutton was disabled!
		boolean rbSetting = companyUI.rb_heatingTypePrivateUI.isEnabled();
		companyUI.rb_heatingTypePrivateUI.setEnabled(true);
		companyUI.rb_heatingTypePrivateUI.setValue(2, true); // Third option (index 2) is electric heatpump
		companyUI.rb_heatingTypePrivateUI.setEnabled(rbSetting);
		companyUI.rb_scenariosPrivateUI.setValue(2, false);
		companyUI.b_runningMainInterfaceSlider = false;
		
		//Reorder c_orderedHeatingSystems
		zero_Interface.c_orderedHeatingSystemsCompanies.remove(company);
		zero_Interface.c_orderedHeatingSystemsCompanies.add(0, company);
		companies.remove(company);		
		nbHeatPumps++;
	}
	else { //No more gas burner assets to adjust: this is the minimum: set slider to minimum and do nothing else
		int min_nbOfHeatpumps = count(gcList, gc -> gc.v_isActive && gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
		int min_pct_ElectricHeatpumpSlider = roundToInt( min_nbOfHeatpumps * 100.0 / nbActiveCompanies );
		sliderHeatpump.setValue(min_pct_ElectricHeatpumpSlider, false);
		f_setHeatingSliders(1, sliderGasburner, sliderHeatpump, sliderHybridHeatPump, sliderDistrictHeating, sliderCustomHeating);
		return;
	}
}
/*ALCODEEND*/}

double f_calculatePeakHeatDemand_kW(GridConnection gc)
{/*ALCODESTART::1749116448649*/
double peakHeatDemand_kW = 0.0;
for (J_EAConsumption j_ea : gc.c_consumptionAssets) {
	if (j_ea.getEAType() == OL_EnergyAssetType.HEAT_DEMAND || j_ea.getEAType() == OL_EnergyAssetType.HOT_WATER_CONSUMPTION) {
		double[] profile = j_ea.getProfilePointer().getAllValues();
		double maxFactor = Arrays.stream(profile).max().getAsDouble();
		peakHeatDemand_kW += maxFactor * j_ea.yearlyDemand_kWh * j_ea.getConsumptionScaling_fr();
	}
}
for (J_EAProfile j_ea : gc.c_profileAssets) {
	if (j_ea.energyCarrier == OL_EnergyCarriers.HEAT) {
		double maxValue = j_ea.getProfileScaling_fr() * Arrays.stream(j_ea.a_energyProfile_kWh).max().getAsDouble();
		peakHeatDemand_kW += maxValue / zero_Interface.energyModel.p_timeStep_h * j_ea.getProfileScaling_fr();
	}
}
if (gc.p_BuildingThermalAsset != null) {
	double maximalTemperatureDifference_K = 30.0; // Approximation
	peakHeatDemand_kW += gc.p_BuildingThermalAsset.getLossFactor_WpK() * maximalTemperatureDifference_K / 1000;
}
return peakHeatDemand_kW;
/*ALCODEEND*/}

double f_addDistrictHeatingToAllHouses()
{/*ALCODESTART::1749739532180*/
for (GCHouse house: zero_Interface.energyModel.Houses ) {
	// Remove the existing heating assets
	house.f_removeAllHeatingAssets();
	
	// Add a heat node
	house.p_parentNodeHeat = findFirst(zero_Interface.energyModel.f_getGridNodesTopLevel(), node -> node.p_energyCarrier == OL_EnergyCarriers.HEAT);
	// Create a heat node if it does not exist yet
	if(house.p_parentNodeHeat == null){
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
		house.p_parentNodeHeat = GN_heat;
		
		//Show warning that heat grid is not a simple solution
		zero_Interface.f_setErrorScreen("LET OP: Er is nu een 'warmtenet' gecreëerd. Maar er is geen warmtebron aanwezig in het model. Daarom zal de benodigde warmte voor het warmtenet in de resultaten te zien zijn als warmte import.", 0, 0);
	}
	house.p_parentNodeHeatID = house.p_parentNodeHeat.p_gridNodeID;
	
	double outputTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureDistrictHeatingDeliverySet_degC;
	double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(house);
	double efficiency = 1.0;
	
	new J_EAConversionHeatDeliverySet(house, peakHeatDemand_kW, efficiency, zero_Interface.energyModel.p_timeStep_h, outputTemperature_degC);
	
	house.f_addHeatManagementToGC(house, OL_GridConnectionHeatingType.DISTRICTHEAT, false);
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_removeDistrictHeatingFromAllHouses()
{/*ALCODESTART::1749739532202*/
for (GCHouse house: zero_Interface.energyModel.Houses ) {
	house.f_removeAllHeatingAssets();
	house.p_parentNodeHeat = null;
	house.p_parentNodeHeatID = null;
		
	//add gasburner
	J_EAConsumption heatDemandAsset = findFirst(house.c_consumptionAssets, j_ea -> j_ea.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND);
	J_EAConversionGasBurner gasBurner;
	//if house has follows the general heat deamnd profile
	if (heatDemandAsset != null) {
		gasBurner = new J_EAConversionGasBurner(house, heatDemandAsset.yearlyDemand_kWh/8760*10, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
	}
	//if house has a thermalBuildingAsset
	else if (house.p_BuildingThermalAsset != null){
		double gasBurnerCapacity_kW = 10;
		gasBurner = new J_EAConversionGasBurner(house, gasBurnerCapacity_kW, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
	}
	// Else house has a customprofiel
	else {
		J_EAProfile heatDemandProfile = (J_EAProfile)findFirst(house.c_profileAssets, x->x instanceof J_EAProfile && x.energyCarrier == OL_EnergyCarriers.HEAT);
		double peakHeatDemand_kW = heatDemandProfile.getProfileScaling_fr() * Arrays.stream(heatDemandProfile.a_energyProfile_kWh).max().orElseThrow(() -> new RuntimeException("Unable to find the maximum of the heat demand profile"));
		gasBurner = new J_EAConversionGasBurner(house, peakHeatDemand_kW, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
	}	
	house.f_addHeatManagementToGC(house, OL_GridConnectionHeatingType.GAS_BURNER, false);
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setAircos(List<GCHouse> gcListHouses,double desiredShare)
{/*ALCODESTART::1749739532217*/
double nbHousesWithAirco = count(gcListHouses, x -> x.p_airco != null);
double nbHouses = gcListHouses.size();
traceln("Previous nb households with airco: " + nbHousesWithAirco);
while ( roundToInt(nbHouses * desiredShare) > nbHousesWithAirco ) {
	GCHouse house = randomWhere(gcListHouses, x -> x.p_airco == null);
	double aircoPower_kW = roundToDecimal(uniform(3,6),1);
	new J_EAAirco(house, aircoPower_kW, zero_Interface.energyModel.p_timeStep_h);
	nbHousesWithAirco ++;
}
while ( roundToInt(nbHouses * desiredShare) < nbHousesWithAirco ) {
	GCHouse house = randomWhere(gcListHouses, x -> x.p_airco != null);
	house.p_airco.removeEnergyAsset();
	nbHousesWithAirco --;
}

traceln("New nb households with airco: " + nbHousesWithAirco);

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();

/*ALCODEEND*/}

double f_addLTDH()
{/*ALCODESTART::1749739532231*/
for (GCHouse house: zero_Interface.energyModel.Houses ) {
	house.f_removeAllHeatingAssets();

	// Add a heat node
	house.p_parentNodeHeat = findFirst(zero_Interface.energyModel.f_getGridNodesTopLevel(), node -> node.p_energyCarrier == OL_EnergyCarriers.HEAT);
	// Create a heat node if it does not exist yet
	if(house.p_parentNodeHeat == null){
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
		house.p_parentNodeHeat = GN_heat;
		
		//Show warning that heat grid is not a simple solution
		zero_Interface.f_setErrorScreen("LET OP: Er is nu een 'warmtenet' gecreëerd. Maar er is geen warmtebron aanwezig in het model. Daarom zal de benodigde warmte voor het warmtenet in de resultaten te zien zijn als warmte import.", 0, 0);
	}
	house.p_parentNodeHeatID = house.p_parentNodeHeat.p_gridNodeID;
	double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(house);
	double heatpumpElectricCapacity_kW = min(peakHeatDemand_kW / 3, 1.0);
	double efficiency_fr = 0.5;
	double inputTemperature_degC = 15.0;  // TODO: Look at these temperatures!
	double outputTemperature_degC = 50.0;
	double sourceAssetHeatPower_kW = 0.0;
	double belowZeroHeatpumpEtaReductionFactor = 1.0;
	J_EAConversionHeatPump heatpump = new J_EAConversionHeatPump(house,
		heatpumpElectricCapacity_kW,
		efficiency_fr,
		zero_Interface.energyModel.p_timeStep_h,
		outputTemperature_degC,
		zero_Interface.energyModel.pp_ambientTemperature_degC.getCurrentValue(),
		sourceAssetHeatPower_kW,
		belowZeroHeatpumpEtaReductionFactor,
		OL_AmbientTempType.HEAT_GRID
	);
	heatpump.updateParameters(inputTemperature_degC, outputTemperature_degC);		
	house.f_addHeatManagementToGC(house, OL_GridConnectionHeatingType.LT_DISTRICTHEAT, false);
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_removeLTDH()
{/*ALCODESTART::1749739532244*/
for (GCHouse house: zero_Interface.energyModel.Houses ) {
	// Disconnect from GridNode Heat
	house.p_parentNodeHeat = null;
	
	// Remove Heatpump and replace with gasburner
	house.f_removeAllHeatingAssets();
	double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(house);
	new J_EAConversionGasBurner(house, peakHeatDemand_kW, zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC);	
	house.f_addHeatManagementToGC(house, OL_GridConnectionHeatingType.GAS_BURNER, false);
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_householdInsulation(List<GCHouse> housesGCList,double houses_pct)
{/*ALCODESTART::1752227724432*/
int nbHouses = count(housesGCList, x -> x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
int nbHousesWithImprovedInsulation = count(housesGCList, x -> x.p_BuildingThermalAsset.getLossScalingFactor_fr() < 1 && x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
int targetNbHousesWithImprovedInsulation = roundToInt(houses_pct / 100.0 * nbHouses);

while (nbHousesWithImprovedInsulation < targetNbHousesWithImprovedInsulation) {
	GCHouse house = findFirst(housesGCList, x -> !x.p_hasAdditionalInsulation && x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
	if (house != null) {
		house.p_BuildingThermalAsset.setLossScalingFactor_fr( 0.7 );
		nbHousesWithImprovedInsulation++;
	}
	else {
		throw new RuntimeException("Unable to find house that does not yet have additional insulation");
	}
}
while (nbHousesWithImprovedInsulation > targetNbHousesWithImprovedInsulation) {
	GCHouse house = findFirst(housesGCList, x -> x.p_hasAdditionalInsulation && x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
	if (house != null) {
		house.p_BuildingThermalAsset.setLossScalingFactor_fr( 0 );
		nbHousesWithImprovedInsulation--;
	}
	else {
		throw new RuntimeException("Unable to find house that has additional insulation");
	}
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setPTSystemHouses(List<GCHouse> gcList,double PT_pct)
{/*ALCODESTART::1753950993262*/
ArrayList<GCHouse> houses = new ArrayList<GCHouse>(zero_Interface.c_orderedPTSystemsHouses.stream().filter(gcList::contains).toList());
int nbHouses = houses.size();
int nbHousesWithPT = count(houses, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.ptProductionHeat_kW));
int nbHousesWithPTGoal = roundToInt(PT_pct / 100.0 * nbHouses);

while ( nbHousesWithPTGoal < nbHousesWithPT ) { // remove excess PV systems
	GCHouse house = findFirst(houses, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.ptProductionHeat_kW));	
	J_EA ptAsset = findFirst(house.c_productionAssets, p -> p.energyAssetType == OL_EnergyAssetType.PHOTOTHERMAL );
		
	if (ptAsset != null) {
		ptAsset.removeEnergyAsset();
		houses.remove(house);
		zero_Interface.c_orderedPTSystemsHouses.remove(house);
		zero_Interface.c_orderedPTSystemsHouses.add(0, house);
		
		if(house.p_heatBuffer != null){
			house.p_heatBuffer.removeEnergyAsset();
		}
		if(house.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW)){
			J_EAProduction pvAsset = findFirst(house.c_productionAssets, ea -> ea.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC);
			if(pvAsset != null){
				double newInstalledPVCapacity = min(house.v_liveAssetsMetaData.PVPotential_kW, pvAsset.getCapacityElectric_kW() + zero_Interface.energyModel.avgc_data.p_avgPTPanelSize_m2*zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2);
				pvAsset.setCapacityElectric_kW(newInstalledPVCapacity);
			}
		}
		nbHousesWithPT --; 
	}
	else {
		traceln(" cant find PV asset in house that should have PV asset in f_setPVHouses (Interface)");
	}
}

while ( nbHousesWithPTGoal > nbHousesWithPT ) {
	GCHouse house = findFirst(houses, x -> !x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.ptProductionHeat_kW));
	if (house == null){
		traceln("No gridconnection without PT panels found! Current PVsystems count: %s", nbHousesWithPT);
		break;
	}
	else {
		String assetName = "Rooftop PT";
		double installedPTCapacity_kW = zero_Interface.energyModel.avgc_data.p_avgPTPanelSize_m2*zero_Interface.energyModel.avgc_data.p_avgPTPower_kWpm2;//roundToDecimal(uniform(3,6),2);
		
		//Compensate for pt if it is present
		if(house.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW)){
			J_EAProduction pvAsset = findFirst(house.c_productionAssets, ea -> ea.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC);
			if(pvAsset != null){
				double newInstalledPVCapacity = max(0, pvAsset.getCapacityElectric_kW() - zero_Interface.energyModel.avgc_data.p_avgPTPanelSize_m2*zero_Interface.energyModel.avgc_data.p_avgPVPower_kWpm2);
				pvAsset.setCapacityElectric_kW(newInstalledPVCapacity);
			}
		}
		J_EAProduction productionAsset = new J_EAProduction ( house, OL_EnergyAssetType.PHOTOTHERMAL, assetName, OL_EnergyCarriers.HEAT, installedPTCapacity_kW, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.pp_PVProduction35DegSouth_fr );
		
		//Get parameters for the heatbuffer
		double lossFactor_WpK = 0;// For now no loss factor
		double minTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgMinHeatBufferTemperature_degC;
		double maxTemperature_degC = zero_Interface.energyModel.avgc_data.p_avgMaxHeatBufferTemperature_degC;
		double initialTemperature_degC = (minTemperature_degC + maxTemperature_degC)/2; 
		double setTemperature_degC = initialTemperature_degC; 
		double heatBufferStorageCapacity_m3 = zero_Interface.energyModel.avgc_data.p_avgHeatBufferWaterVolumePerPTSurface_m3pm2 * installedPTCapacity_kW/zero_Interface.energyModel.avgc_data.p_avgPTPower_kWpm2;
		double heatCapacity_JpK = zero_Interface.energyModel.avgc_data.p_waterHeatCapacity_JpkgK*(heatBufferStorageCapacity_m3*zero_Interface.energyModel.avgc_data.p_waterDensity_kgpm3);
		
		//Add heatbuffer
		J_EAStorageHeat heatbufferAsset = new J_EAStorageHeat ( house, OL_EnergyAssetType.STORAGE_HEAT, installedPTCapacity_kW, lossFactor_WpK, zero_Interface.energyModel.p_timeStep_h, initialTemperature_degC, minTemperature_degC, maxTemperature_degC, setTemperature_degC, heatCapacity_JpK, OL_AmbientTempType.FIXED); 

		houses.remove(house);
		zero_Interface.c_orderedPTSystemsHouses.remove(house);
		zero_Interface.c_orderedPTSystemsHouses.add(0, house);
		nbHousesWithPT ++;	
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

int f_calculateNumberOfCustomHeatingSystems(List<GridConnection> gcList)
{/*ALCODESTART::1754385214478*/
int numberOfCustomHeatingSystems = 0;

for (GridConnection gc : gcList) {
	if ( gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.CUSTOM && gc.v_isActive ) {
		numberOfCustomHeatingSystems++;
	}
}
return numberOfCustomHeatingSystems;
/*ALCODEEND*/}

double f_setHeatingSliders(int sliderIndex,ShapeSlider gasBurnerSlider,ShapeSlider heatPumpSlider,ShapeSlider hybridHeatPumpSlider,ShapeSlider districtHeatingSlider,ShapeSlider customHeatingSlider)
{/*ALCODESTART::1754391096443*/
double pct_naturalGasBurner = gasBurnerSlider.getValue();
double pct_electricHeatPump = heatPumpSlider.getValue();
double pct_hybridHeatPump = hybridHeatPumpSlider != null ? hybridHeatPumpSlider.getValue() : 0;
double pct_districtHeating = districtHeatingSlider != null ? districtHeatingSlider.getValue() : 0;
double pct_customHeatingSlider = customHeatingSlider != null ? customHeatingSlider.getValue() : 0;

//Set array with pct values
double pctArray[] = {
    pct_naturalGasBurner,
    pct_electricHeatPump,
    pct_hybridHeatPump,
    pct_districtHeating,
    pct_customHeatingSlider
};



double pctFixed = pctArray[2] + pctArray[3] + pctArray[4]; // For now: hybrid heatpump, district heating and custom are fixed.
double pctAdjustable = 100 - pctFixed;

//Only adjust between gas and heat pump for now
if (sliderIndex == 0) { // Gas moved
    pctArray[0] = min(pctArray[0], pctAdjustable); // Limit gas to the available room
    pctArray[1] = pctAdjustable - pctArray[0];     // HP gets the remaining
} else if (sliderIndex == 1) { // Heat pump moved
    pctArray[1] = min(pctArray[1], pctAdjustable); // Limit HP to the available room
    pctArray[0] = pctAdjustable - pctArray[1];     // Gas gets the remaining
}



/*
double pctExcess = Arrays.stream(pctArray).sum() - 100;

for (int i = 0; i < pctArray.length; i++) {
    if (i != sliderIndex && (i== 1 || i ==0)){// Skip moved slider & custom slider
    	pctExcess = Arrays.stream(pctArray).sum() - 100; // recalc each time
        
        if(pctExcess == 0){
       	 	break;
        }
        
        double newValue = pctArray[i] - pctExcess;
        pctArray[i] = Math.max(0, newValue);
	}
}


if (pctExcess != 0) { //If still excess, reduce moved slider
	double newSliderValue = pctArray[sliderIndex] - pctExcess;
    pctArray[sliderIndex] = Math.max(0, newSliderValue);
}
*/

// Set Sliders
gasBurnerSlider.setValue(roundToInt(pctArray[0]), false);
heatPumpSlider.setValue(roundToInt(pctArray[1]), false);
if(hybridHeatPumpSlider != null){
	hybridHeatPumpSlider.setValue(roundToInt(pctArray[2]), false);
}
if(districtHeatingSlider != null){
	districtHeatingSlider.setValue(roundToInt(pctArray[3]), false);
}
if(customHeatingSlider != null){
	customHeatingSlider.setValue(roundToInt(pctArray[4]), false);
}


/*ALCODEEND*/}

double f_updateSliders_Heating()
{/*ALCODESTART::1754923748794*/
if(gr_heatingSliders_default.isVisible()){
	f_updateHeatingSliders_default();
}
else if(gr_heatingSliders_businesspark.isVisible()){
	f_updateHeatingSliders_businesspark();
}
else if(gr_heatingSliders_residential.isVisible()){
	f_updateHeatingSliders_residential();
}
else{
	f_updateHeatingSliders_custom();
}
/*ALCODEEND*/}

double f_updateHeatingSliders_default()
{/*ALCODESTART::1754924509667*/
////Companies
List<GCUtility> utilityGridConnections = uI_Tabs.f_getSliderGridConnections_utilities();

//Savings (IN PROGRESS, WHAT ABOUT THERMAL BUILDINGS?????)
double totalBaseConsumption_kWh = 0;
double totalSavedConsumption_kWh = 0;
for(GridConnection GC : utilityGridConnections){
	if(GC.v_isActive){
		List<J_EAProfile> profileEAs = findAll(GC.c_profileAssets, profile -> profile.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
		List<J_EAConsumption> consumptionEAs = findAll(GC.c_consumptionAssets, consumption -> consumption.getActiveEnergyCarriers().contains(OL_EnergyCarriers.HEAT));
		for(J_EAProfile profileEA : profileEAs){
			double baseConsumption_kWh = ZeroMath.arraySum(profileEA.a_energyProfile_kWh);
			totalBaseConsumption_kWh += baseConsumption_kWh;
			totalSavedConsumption_kWh += (1 - profileEA.getProfileScaling_fr()) * baseConsumption_kWh;
		}
		for(J_EAConsumption consumptionEA : consumptionEAs){
			totalBaseConsumption_kWh += consumptionEA.yearlyDemand_kWh;
			totalSavedConsumption_kWh += (1-consumptionEA.getConsumptionScaling_fr())*consumptionEA.yearlyDemand_kWh;
		}
		
		if(GC.p_BuildingThermalAsset != null){
			traceln("WARNING: SLIDER SAVINGS UPDATE FUNCTION IS NOT FUNCTIONAL YET FOR COMPANIES WITH THERMAL BUILDING ASSETS");
		}
	}
}

double heatSavings_pct = totalBaseConsumption_kWh > 0 ? (totalSavedConsumption_kWh/totalBaseConsumption_kWh * 100) : 0;
sl_heatDemandReductionCompanies_pct.setValue(roundToInt(heatSavings_pct), false);


//Heating assets
int GasBurners = count(utilityGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.GAS_BURNER && gc.v_isActive);
int GasBurners_pct = roundToInt(100.0 * GasBurners / count(utilityGridConnections, x -> x.v_isActive && x.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE));

sl_gasBurnerCompanies_pct.setValue(GasBurners_pct, false);
f_setHeatingSliders( 0, sl_gasBurnerCompanies_pct, sl_electricHeatPumpCompanies_pct, null, null, null );

////Houses
List<GCHouse> houseGridConnections = uI_Tabs.f_getSliderGridConnections_houses();

//Savings
double averageScalingFactor = 0;
double totalScalingFactors = 0;
for(GCHouse GC : houseGridConnections){
	if(GC.v_isActive){
		List<J_EAProfile> profileEAs = findAll(GC.c_profileAssets, profile -> profile.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
		List<J_EAConsumption> consumptionEAs = findAll(GC.c_consumptionAssets, consumption -> consumption.getActiveEnergyCarriers().contains(OL_EnergyCarriers.HEAT));
		for(J_EAProfile profileEA : profileEAs){
			double totalScalingFactorValue = averageScalingFactor*totalScalingFactors;
			double newTotalScalingFactorValue = totalScalingFactorValue + profileEA.getProfileScaling_fr();
			totalScalingFactors++;
			averageScalingFactor = newTotalScalingFactorValue/totalScalingFactors;
		}
		for(J_EAConsumption consumptionEA : consumptionEAs){
			double totalScalingFactorValue = averageScalingFactor*totalScalingFactors;
			double newTotalScalingFactorValue = totalScalingFactorValue + consumptionEA.getConsumptionScaling_fr();
			totalScalingFactors++;
			averageScalingFactor = newTotalScalingFactorValue/totalScalingFactors;
		}
		
		if(GC.p_BuildingThermalAsset != null){
			double totalScalingFactorValue = averageScalingFactor*totalScalingFactors;
			double newTotalScalingFactorValue = totalScalingFactorValue + GC.p_BuildingThermalAsset.getLossScalingFactor_fr();
			totalScalingFactors++;
			averageScalingFactor = newTotalScalingFactorValue/totalScalingFactors;
		}
	}
}
double averageSavingsFactor_pct = (1-averageScalingFactor)*100.0;
sl_heatDemandReductionHouseholds_pct.setValue(roundToInt(averageSavingsFactor_pct), false);


//Assets
GasBurners = count(houseGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.GAS_BURNER && gc.v_isActive);
GasBurners_pct = roundToInt(100.0 * GasBurners / (count(houseGridConnections, x -> x.v_isActive && x.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE)));

sl_gasBurnerHouseholds_pct.setValue(GasBurners_pct, false);
f_setHeatingSliders( 0, sl_gasBurnerHouseholds_pct, sl_electricHeatPumpHouseholds_pct, null, null, null );

/*ALCODEEND*/}

double f_updateHeatingSliders_residential()
{/*ALCODESTART::1754924542535*/
List<GCHouse> houseGridConnections = uI_Tabs.f_getSliderGridConnections_houses();


//Heating type
int GasBurners = count(houseGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.GAS_BURNER && gc.v_isActive);
int GasBurners_pct = roundToInt(100.0 * GasBurners / (count(houseGridConnections, x -> x.v_isActive && x.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE)));
int numberOfHousesOnHTHeatGrid = count(houseGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.DISTRICTHEAT && gc.v_isActive);
int numberOfHousesOnLTHeatGrid = count(houseGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.LT_DISTRICTHEAT && gc.v_isActive);
cb_householdHTDistrictHeatingResidentialArea.setSelected(false, false);
cb_householdLTDistrictHeatingResidentialArea.setSelected(false, false);

if(numberOfHousesOnHTHeatGrid > 0 || numberOfHousesOnLTHeatGrid > 0){
	sl_householdElectricHeatPumpResidentialArea_pct.setValue(0, false);
	sl_householdGasBurnerResidentialArea_pct.setValue(0, false);
	if(numberOfHousesOnHTHeatGrid > 0){
		cb_householdHTDistrictHeatingResidentialArea.setSelected(true, false);
	}
	else{
		cb_householdLTDistrictHeatingResidentialArea.setSelected(true, false);		
	}
}
else{
	sl_householdGasBurnerResidentialArea_pct.setValue(GasBurners_pct, false);
	f_setHeatingSliders( 0, sl_householdGasBurnerResidentialArea_pct, sl_householdElectricHeatPumpResidentialArea_pct, null, null, null );
}
//Houses with Airco
double nbHouses = houseGridConnections.size();
double nbHousesWithAirco = count(houseGridConnections, x -> x.p_airco != null);
double pctOfHousesWithAirco = (nbHousesWithAirco*100.0)/nbHouses;
sl_householdAircoResidentialArea_pct.setValue(pctOfHousesWithAirco, false);


//Houses with better isolation
int nbHousesThatCanGetImprovedIsolation = count(houseGridConnections, x -> x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
int nbHousesWithImprovedInsulation = count(houseGridConnections, x -> x.p_BuildingThermalAsset.getLossScalingFactor_fr() < 1 && x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
double pctOfHousesWithImprovedInsulation = 100.0 * ((double)nbHousesWithImprovedInsulation)/nbHousesThatCanGetImprovedIsolation;
sl_householdHeatDemandReductionResidentialArea_pct.setValue(roundToInt(pctOfHousesWithImprovedInsulation), false);


//PT
int nbHousesWithPT = count(houseGridConnections, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.ptProductionHeat_kW));

sl_rooftopPTHouses_pct.setValue((nbHousesWithPT*100.0)/nbHouses, false);
/*ALCODEEND*/}

double f_updateHeatingSliders_businesspark()
{/*ALCODESTART::1754924544023*/
List<GCUtility> utilityGridConnections = uI_Tabs.f_getSliderGridConnections_utilities();

//Savings (IN PROGRESS, WHAT ABOUT THERMAL BUILDINGS?????)
double totalBaseConsumption_kWh = 0;
double totalSavedConsumption_kWh = 0;
for(GridConnection GC : utilityGridConnections){
	if(GC.v_isActive){
		List<J_EAProfile> profileEAs = findAll(GC.c_profileAssets, profile -> profile.getEnergyCarrier() == OL_EnergyCarriers.HEAT); // FIX FOR HOT WATER/PT IN LONG RUN
		List<J_EAConsumption> consumptionEAs = findAll(GC.c_consumptionAssets, consumption -> consumption.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND);
		for(J_EAProfile profileEA : profileEAs){
			double baseConsumption_kWh = ZeroMath.arraySum(profileEA.a_energyProfile_kWh);
			totalBaseConsumption_kWh += baseConsumption_kWh;
			totalSavedConsumption_kWh += (1 - profileEA.getProfileScaling_fr()) * baseConsumption_kWh;
		}
		for(J_EAConsumption consumptionEA : consumptionEAs){
			totalBaseConsumption_kWh += consumptionEA.yearlyDemand_kWh;
			totalSavedConsumption_kWh += (1-consumptionEA.getConsumptionScaling_fr())*consumptionEA.yearlyDemand_kWh;
		}
		
		if(GC.p_BuildingThermalAsset != null){
			traceln("WARNING: SLIDER SAVINGS UPDATE FUNCTION IS NOT FUNCTIONAL YET FOR COMPANIES WITH THERMAL BUILDING ASSETS");
		}
	}
}

double heatSavings_pct = totalBaseConsumption_kWh > 0 ? (totalSavedConsumption_kWh/totalBaseConsumption_kWh * 100) : 0;
sl_heatDemandSlidersCompaniesHeatDemandReductionCompanies_pct.setValue(roundToInt(heatSavings_pct), false);



//For now custom only for businessparks, future it should be made functional for other models as well!
v_totalNumberOfCustomHeatingSystems = f_calculateNumberOfCustomHeatingSystems(new ArrayList<GridConnection>(utilityGridConnections));

int totalCompaniesWithHeating = count(utilityGridConnections, x -> x.v_isActive && x.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE);

int gasBurners = count(utilityGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.GAS_BURNER && gc.v_isActive);
int hybridHeatpump = count(utilityGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.HYBRID_HEATPUMP && gc.v_isActive);
int electricHeatpump = count(utilityGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP && gc.v_isActive);
int districtHeating = count(utilityGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.DISTRICTHEAT && gc.v_isActive);
int LTDistrictHeating = count(utilityGridConnections, gc-> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.LT_DISTRICTHEAT && gc.v_isActive);

int gasBurners_pct = roundToInt(100.0 * gasBurners / totalCompaniesWithHeating);
int hybridHeatpump_pct = roundToInt(100.0 * hybridHeatpump / totalCompaniesWithHeating);
int electricHeatpump_pct = roundToInt(100.0 * electricHeatpump / totalCompaniesWithHeating);
int districtHeating_pct = roundToInt(100.0 * districtHeating / totalCompaniesWithHeating);
int LTDistrictHeating_pct = roundToInt(100.0 * LTDistrictHeating / totalCompaniesWithHeating);
int customHeating_pct = roundToInt(100.0 * v_totalNumberOfCustomHeatingSystems/totalCompaniesWithHeating);

sl_heatDemandSlidersCompaniesGasBurnerCompanies_pct.setValue(gasBurners_pct, false);
sl_heatDemandSlidersCompaniesHybridHeatPumpCompanies_pct.setValue(hybridHeatpump_pct, false);
sl_heatDemandSlidersCompaniesElectricHeatPumpCompanies_pct.setValue(electricHeatpump_pct, false);
sl_heatDemandSlidersCompaniesDistrictHeatingCompanies_pct.setValue(districtHeating_pct, false);
//sl_heatDemandSlidersCompaniesLTDistrictHeatingCompanies_pct.setValue(LTDistrictHeating_pct, false); Doesnt exist (yet) for companies
sl_heatingTypeSlidersCompaniesCustom_pct.setValue(customHeating_pct, false);

/*ALCODEEND*/}

double f_updateHeatingSliders_custom()
{/*ALCODESTART::1754924574713*/
//If you have a custom tab, 
//override this function to make it update automatically
traceln("Forgot to override the update custom heating sliders functionality");
/*ALCODEEND*/}

