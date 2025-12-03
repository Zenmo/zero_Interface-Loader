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
			double heatOutputPower_kW = max(zero_Interface.energyModel.avgc_data.p_minGasBurnerOutputCapacity_kW, peakHeatDemand_kW);
			new J_EAConversionGasBurner(company, heatOutputPower_kW, zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner_fr, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC);
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
			double electricInputPower_kW = max(zero_Interface.energyModel.avgc_data.p_minHeatpumpElectricCapacity_kW, peakHeatDemand_kW);
			new J_EAConversionHeatPump(company, electricInputPower_kW, zero_Interface.energyModel.avgc_data.p_avgEfficiencyHeatpump_fr, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureHeatpump_degC,  zero_Interface.energyModel.pp_ambientTemperature_degC.getCurrentValue(), 0, 1, OL_AmbientTempType.AMBIENT_AIR);				
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
		double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(house) * 2;//Just make it always twice as high, to be able to support savings/additional consumption slider settings.
		double heatOutputPower_kW = max(zero_Interface.energyModel.avgc_data.p_minGasBurnerOutputCapacity_kW, peakHeatDemand_kW);
		new J_EAConversionGasBurner(house, heatOutputPower_kW, zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner_fr, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC);
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
		double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(house) * 2;//Just make it always twice as high, to be able to support savings/additional consumption slider settings.
		double electricInputPower_kW = max(zero_Interface.energyModel.avgc_data.p_minHeatpumpElectricCapacity_kW, peakHeatDemand_kW);
		new J_EAConversionHeatPump(house, electricInputPower_kW, 0.5, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureHeatpump_degC,  zero_Interface.energyModel.pp_ambientTemperature_degC.getCurrentValue(), 0, 1, OL_AmbientTempType.AMBIENT_AIR);				
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
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
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

double f_addDistrictHeatingToAllHouses(List<GCHouse> housesGCList)
{/*ALCODESTART::1749739532180*/
for (GCHouse house: housesGCList ) {
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
	
	house.f_addHeatManagement(OL_GridConnectionHeatingType.DISTRICTHEAT, false);
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_removeDistrictHeatingFromAllHouses(List<GCHouse> housesGCList)
{/*ALCODESTART::1749739532202*/
for (GCHouse house: housesGCList ) {
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
	house.f_addHeatManagement(OL_GridConnectionHeatingType.GAS_BURNER, false);
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setAircos(List<GCHouse> gcListHouses,double desiredShare)
{/*ALCODESTART::1749739532217*/
double nbHousesWithAirco = count(gcListHouses, x -> x.p_airco != null);
double nbHouses = gcListHouses.size();

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

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();

/*ALCODEEND*/}

double f_addLTDH(List<GCHouse> housesGCList)
{/*ALCODESTART::1749739532231*/
for (GCHouse house: housesGCList ) {
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
	house.f_addHeatManagement(OL_GridConnectionHeatingType.LT_DISTRICTHEAT, false);		
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_removeLTDH(List<GCHouse> housesGCList)
{/*ALCODESTART::1749739532244*/
for (GCHouse house: housesGCList ) {
	// Disconnect from GridNode Heat
	house.p_parentNodeHeat = null;
	house.p_parentNodeHeatID = null;
		
	// Remove Heatpump and replace with gasburner
	house.f_removeAllHeatingAssets();
	double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(house);
	new J_EAConversionGasBurner(house, peakHeatDemand_kW, zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner_fr, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC);	
	house.f_addHeatManagement(OL_GridConnectionHeatingType.GAS_BURNER, false);
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_householdInsulation(List<GCHouse> housesGCList,double houses_pct)
{/*ALCODESTART::1752227724432*/
int nbHouses = count(housesGCList, x -> x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
int nbHousesWithImprovedInsulation = count(housesGCList, x -> x.p_BuildingThermalAsset.getLossScalingFactor_fr() < 1 && x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
int targetNbHousesWithImprovedInsulation = roundToInt(houses_pct / 100.0 * nbHouses);

while (nbHousesWithImprovedInsulation < targetNbHousesWithImprovedInsulation) {
	GCHouse house = findFirst(housesGCList, x -> x.p_BuildingThermalAsset.getLossScalingFactor_fr() >= 1 && x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
	if (house != null) {
		house.p_BuildingThermalAsset.setLossScalingFactor_fr( 0.7 );
		nbHousesWithImprovedInsulation++;
	}
	else {
		throw new RuntimeException("Unable to find house that does not yet have additional insulation");
	}
}
while (nbHousesWithImprovedInsulation > targetNbHousesWithImprovedInsulation) {
	GCHouse house = findFirst(housesGCList, x -> x.p_BuildingThermalAsset.getLossScalingFactor_fr() < 1 && x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
	if (house != null) {
		house.p_BuildingThermalAsset.setLossScalingFactor_fr( 1 );
		nbHousesWithImprovedInsulation--;
	}
	else {
		throw new RuntimeException("Unable to find house that has additional insulation");
	}
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
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
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setHeatingSliders(List<GridConnection> gcList,List<GridConnection> orderedHeatingSystemGCList,OL_GridConnectionHeatingType changedSliderHeatingType,ShapeSlider gasBurnerSlider,ShapeSlider hybridHeatPumpSlider,ShapeSlider heatPumpSlider,ShapeSlider HTdistrictHeatingSlider,ShapeSlider LTdistrictHeatingSlider,ShapeSlider customHeatingSlider)
{/*ALCODESTART::1754391096443*/
////Determine the changed slider index used in the distribution for loop
int changedSliderIndex = 0;
switch(changedSliderHeatingType){
	case GAS_BURNER:
		changedSliderIndex = 0;
		break;
	case HYBRID_HEATPUMP:
		changedSliderIndex = 1;
		break;
	case ELECTRIC_HEATPUMP:
		changedSliderIndex = 2;
		break;
	case DISTRICTHEAT:
		changedSliderIndex = 3;
		break;
	case LT_DISTRICTHEAT:
	case CUSTOM:
		throw new RuntimeException("Changed a heating type slider with a currently unsupported type!");
}

//// Get current pct values (after slider change, but before correction; In other words total_pct != 100)
double pct_naturalGasBurner = gasBurnerSlider.getValue();
double pct_hybridHeatPump = hybridHeatPumpSlider != null ? hybridHeatPumpSlider.getValue() : 0;
double pct_electricHeatPump = heatPumpSlider != null ? heatPumpSlider.getValue() : 0;
double pct_HTdistrictHeating = HTdistrictHeatingSlider != null ? HTdistrictHeatingSlider.getValue() : 0;
double pct_LTdistrictHeating = LTdistrictHeatingSlider != null ? LTdistrictHeatingSlider.getValue() : 0;
double pct_customHeatingSlider = customHeatingSlider != null ? customHeatingSlider.getValue() : 0;

//Set array with pct values
double pctArray[] = {
    pct_naturalGasBurner,
    pct_hybridHeatPump,
    pct_electricHeatPump,
    pct_HTdistrictHeating,
    pct_LTdistrictHeating,    
    pct_customHeatingSlider
};

//// Create a ghost asset array, and fill it. This is used to find minimums of certain sliders. Also get the total amount of GC with heating systems.
int ghostAssetTotalArray[] = new int[pctArray.length];
int totalGCWithHeating = 0;
for(GridConnection GC : gcList){
	if(GC.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE && GC.v_isActive){
		totalGCWithHeating++;
		
		if(GC.f_getHeatingTypeIsGhost()){
			switch(GC.f_getCurrentHeatingType()){
				case GAS_BURNER:
					ghostAssetTotalArray[0]++;
					break;
				case HYBRID_HEATPUMP:
					ghostAssetTotalArray[1]++;
					break;
				case ELECTRIC_HEATPUMP:
					ghostAssetTotalArray[2]++;
					break;
				case DISTRICTHEAT:
					ghostAssetTotalArray[3]++;
					break;
				case LT_DISTRICTHEAT:
					ghostAssetTotalArray[4]++;
					break;
				case CUSTOM:
					ghostAssetTotalArray[5]++;
					break;
			}
		}
	}
}

//// Correct the slider change trough to the other sliders. But don't move the custom slider or set sliders lower than their unchangable ghost assets.
int customSliderIndex = 5;
double pctExcess = Arrays.stream(pctArray).sum() - 100;

for (int i = 0; i < pctArray.length && pctExcess != 0 ; i++) {
    if (i != changedSliderIndex && i != customSliderIndex) {
        double ghostMin_pct = ((double) 100.0 * ghostAssetTotalArray[i]) / totalGCWithHeating;
        double maxDown_pct = pctArray[i] - ghostMin_pct;
        double maxUp_pct = 100 - pctArray[i];

        // Determine the slider delta, positive delta -> slider increases, negative -> slider decreases
        double deltaSlider_pct = 0;
        if(pctExcess > 0){
        	deltaSlider_pct = -min(maxDown_pct, pctExcess);
        }
        else{
        	deltaSlider_pct = min(maxUp_pct, -pctExcess);
        }

        pctArray[i] += deltaSlider_pct;
        pctExcess = Arrays.stream(pctArray).sum() - 100;
    }
}

//If still not 0, then adjust the changedSlider back to solve it.
if (pctExcess != 0){
	pctArray[changedSliderIndex] = max(0, pctArray[changedSliderIndex] - pctExcess);
}

//// Set refound values to the sliders again
gasBurnerSlider.setValue(roundToInt(pctArray[0]), false);
if(hybridHeatPumpSlider != null){
	hybridHeatPumpSlider.setValue(roundToInt(pctArray[1]), false);
}
if(heatPumpSlider != null){
	heatPumpSlider.setValue(roundToInt(pctArray[2]), false);
}
if(HTdistrictHeatingSlider != null){
	HTdistrictHeatingSlider.setValue(roundToInt(pctArray[3]), false);
}
if(LTdistrictHeatingSlider != null){
	LTdistrictHeatingSlider.setValue(roundToInt(pctArray[4]), false);
}
if(customHeatingSlider != null){
	customHeatingSlider.setValue(roundToInt(pctArray[5]), false);
}


//Set the heating systems in the engine to the correct setting
f_setHeatingSystems(gcList, orderedHeatingSystemGCList, changedSliderHeatingType, pctArray[changedSliderIndex]);
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
List<GCUtility> utilityGridConnections = uI_Tabs.f_getActiveSliderGridConnections_utilities();

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
//Heating type
int totalCompaniesWithHeating = 0;
int nbOfCompaniesWithGasBurners = 0;
int nbOfCompaniesWithHybridHeatpumps = 0;
int nbOfCompaniesWithElectricHeatpumps = 0;
int nbOfCompaniesOnHTHeatGrid = 0;
int nbOfCompaniesOnLTHeatGrid = 0;

for(GCUtility GC : utilityGridConnections){
	if(GC.v_isActive && GC.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE){
		totalCompaniesWithHeating++;
		switch(GC.f_getCurrentHeatingType()){
			case GAS_BURNER:
				nbOfCompaniesWithGasBurners++;
				break;
			case HYBRID_HEATPUMP:
				nbOfCompaniesWithHybridHeatpumps++;
				break;
			case ELECTRIC_HEATPUMP:
				nbOfCompaniesWithElectricHeatpumps++;
				break;
			case DISTRICTHEAT:
				nbOfCompaniesOnHTHeatGrid++;
				break;
			case LT_DISTRICTHEAT:
				nbOfCompaniesOnLTHeatGrid++;
				break;
		}
	}
}

int companiesWithGasBurners_pct = roundToInt(100.0 * nbOfCompaniesWithGasBurners / totalCompaniesWithHeating);
int companiesWithHybridHeatpump_pct = roundToInt(100.0 * nbOfCompaniesWithHybridHeatpumps / totalCompaniesWithHeating);
int companiesWithElectricHeatpump_pct = roundToInt(100.0 * nbOfCompaniesWithElectricHeatpumps / totalCompaniesWithHeating);
int companiesWithHTDistrictHeat_pct = roundToInt(100.0 * nbOfCompaniesOnHTHeatGrid / totalCompaniesWithHeating);

sl_gasBurnerCompanies_pct.setValue(companiesWithGasBurners_pct, false);
sl_hybridHeatPumpCompanies_pct.setValue(companiesWithHybridHeatpump_pct, false);
sl_electricHeatPumpCompanies_pct.setValue(companiesWithElectricHeatpump_pct, false);
sl_districtHeatingCompanies_pct.setValue(companiesWithHTDistrictHeat_pct, false);


////Houses
List<GCHouse> houseGridConnections = uI_Tabs.f_getActiveSliderGridConnections_houses();

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


//Heating type
int totalHousesWithHeating = 0;
int nbOfHousesWithGasBurners = 0;
int nbOfHousesWithHybridHeatpumps = 0;
int nbOfHousesWithElectricHeatpumps = 0;
int nbOfHousesOnHTHeatGrid = 0;
int nbOfHousesOnLTHeatGrid = 0;

for(GCHouse GC : houseGridConnections){
	if(GC.v_isActive && GC.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE){
		totalHousesWithHeating++;
		switch(GC.f_getCurrentHeatingType()){
			case GAS_BURNER:
				nbOfHousesWithGasBurners++;
				break;
			case HYBRID_HEATPUMP:
				nbOfHousesWithHybridHeatpumps++;
				break;
			case ELECTRIC_HEATPUMP:
				nbOfHousesWithElectricHeatpumps++;
				break;
			case DISTRICTHEAT:
				nbOfHousesOnHTHeatGrid++;
				break;
			case LT_DISTRICTHEAT:
				nbOfHousesOnLTHeatGrid++;
				break;
		}
	}
}

int housesWithGasBurners_pct = roundToInt(100.0 * nbOfHousesWithGasBurners / totalHousesWithHeating);
int housesWithHybridHeatpump_pct = roundToInt(100.0 * nbOfHousesWithHybridHeatpumps / totalHousesWithHeating);
int housesWithElectricHeatpump_pct = roundToInt(100.0 * nbOfHousesWithElectricHeatpumps / totalHousesWithHeating);
int housesWithHTDistrictHeat_pct = roundToInt(100.0 * nbOfHousesOnHTHeatGrid / totalHousesWithHeating);

sl_gasBurnerHouseholds_pct.setValue(housesWithGasBurners_pct, false);
sl_hybridHeatPumpHouseholds_pct.setValue(housesWithHybridHeatpump_pct, false);
sl_electricHeatPumpHouseholds_pct.setValue(housesWithElectricHeatpump_pct, false);
sl_districtHeatingHouseholds_pct.setValue(housesWithHTDistrictHeat_pct, false);

/*ALCODEEND*/}

double f_updateHeatingSliders_residential()
{/*ALCODESTART::1754924542535*/
List<GCHouse> houseGridConnections = uI_Tabs.f_getActiveSliderGridConnections_houses();


//Heating type
int totalHousesWithHeating = 0;
int nbOfHousesWithGasBurners = 0;
int nbOfHousesWithHybridHeatpumps = 0;
int nbOfHousesWithElectricHeatpumps = 0;
int nbOfHousesOnHTHeatGrid = 0;
int nbOfHousesOnLTHeatGrid = 0;

for(GCHouse GC : houseGridConnections){
	if(GC.v_isActive && GC.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE){
		totalHousesWithHeating++;
		switch(GC.f_getCurrentHeatingType()){
			case GAS_BURNER:
				nbOfHousesWithGasBurners++;
				break;
			case HYBRID_HEATPUMP:
				nbOfHousesWithHybridHeatpumps++;
				break;
			case ELECTRIC_HEATPUMP:
				nbOfHousesWithElectricHeatpumps++;
				break;
			case DISTRICTHEAT:
				nbOfHousesOnHTHeatGrid++;
				break;
			case LT_DISTRICTHEAT:
				nbOfHousesOnLTHeatGrid++;
				break;
		}
	}
}

int housesWithGasBurners_pct = roundToInt(100.0 * nbOfHousesWithGasBurners / totalHousesWithHeating);
int housesWithHybridHeatpump_pct = roundToInt(100.0 * nbOfHousesWithHybridHeatpumps / totalHousesWithHeating);
int housesWithElectricHeatpump_pct = roundToInt(100.0 * nbOfHousesWithElectricHeatpumps / totalHousesWithHeating);

sl_householdGasBurnerResidentialArea_pct.setValue(housesWithGasBurners_pct, false);
sl_householdHybridHeatpumpResidentialArea.setValue(housesWithHybridHeatpump_pct, false);
sl_householdElectricHeatPumpResidentialArea_pct.setValue(housesWithElectricHeatpump_pct, false);
cb_householdHTDistrictHeatingResidentialArea.setSelected(false, false);
cb_householdLTDistrictHeatingResidentialArea.setSelected(false, false);

if(nbOfHousesOnHTHeatGrid == totalHousesWithHeating){
	cb_householdHTDistrictHeatingResidentialArea.setSelected(true, false);
}
if(nbOfHousesOnLTHeatGrid == totalHousesWithHeating){
	cb_householdLTDistrictHeatingResidentialArea.setSelected(true, false);
}


//Electric heatpump heating management
Triple<OL_GridConnectionHeatingType, Boolean, Boolean> triple = Triple.of( OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, true, false );
Class<? extends I_HeatingManagement> managementClass = zero_Interface.energyModel.c_defaultHeatingStrategies.get(triple);
if(J_HeatingManagementPIcontrol.class.isAssignableFrom(managementClass) || J_HeatingManagementSimple.class.isAssignableFrom(managementClass)){
	cb_householdFullElectricHeatpumpManagement.setValue("Standaard");
	cb_householdFullElectricHeatpumpManagement.setEnabled(true);
}
else if(J_HeatingManagementHeatpumpOffPeak.class.isAssignableFrom(managementClass)){
	cb_householdFullElectricHeatpumpManagement.setValue("Piekmijden (Interval)");
	cb_householdFullElectricHeatpumpManagement.setEnabled(true);
}
/*
else if(J_HeatingManagementGridAware.class.isAssignableFrom(managementClass)){
	cb_householdFullElectricHeatpumpManagement.setValue("Netbewust");
	cb_householdFullElectricHeatpumpManagement.setEnabled(true);
}
*/
else{
	cb_householdFullElectricHeatpumpManagement.setValue("Standaard");
	cb_householdFullElectricHeatpumpManagement.setEnabled(false);
}

//Initialize Off peak interval
J_HeatingManagementHeatpumpOffPeak heatpumpOffPeakManagementClass = new J_HeatingManagementHeatpumpOffPeak();
eb_reducedHeatingIntervalStart.setText(heatpumpOffPeakManagementClass.getStartTimeOfReducedHeatingInterval_hr() != null ? heatpumpOffPeakManagementClass.getStartTimeOfReducedHeatingInterval_hr() : -1);
eb_reducedHeatingIntervalEnd.setText(heatpumpOffPeakManagementClass.getEndTimeOfReducedHeatingInterval_hr() != null ? heatpumpOffPeakManagementClass.getEndTimeOfReducedHeatingInterval_hr() : -1);

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

sl_rooftopPTHouses_pct.setValue(roundToInt((nbHousesWithPT*100.0)/nbHouses), false);
/*ALCODEEND*/}

double f_updateHeatingSliders_businesspark()
{/*ALCODESTART::1754924544023*/
List<GCUtility> utilityGridConnections = uI_Tabs.f_getActiveSliderGridConnections_utilities();

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


//Heating type
int totalCompaniesWithHeating = 0;
int nbOfCompaniesWithGasBurners = 0;
int nbOfCompaniesWithHybridHeatpumps = 0;
int nbOfCompaniesWithElectricHeatpumps = 0;
int nbOfCompaniesOnHTHeatGrid = 0;
int nbOfCompaniesOnLTHeatGrid = 0;
int nbOfCompaniesWithCustomHeating = 0;

for(GCUtility GC : utilityGridConnections){
	if(GC.v_isActive && GC.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE){
		totalCompaniesWithHeating++;
		switch(GC.f_getCurrentHeatingType()){
			case GAS_BURNER:
				nbOfCompaniesWithGasBurners++;
				break;
			case HYBRID_HEATPUMP:
				nbOfCompaniesWithHybridHeatpumps++;
				break;
			case ELECTRIC_HEATPUMP:
				nbOfCompaniesWithElectricHeatpumps++;
				break;
			case DISTRICTHEAT:
				nbOfCompaniesOnHTHeatGrid++;
				break;
			case LT_DISTRICTHEAT:
				nbOfCompaniesOnLTHeatGrid++;
				break;
			case CUSTOM:
				nbOfCompaniesWithCustomHeating++;
				break;
		}
	}
}

int companiesWithGasBurners_pct = roundToInt(100.0 * nbOfCompaniesWithGasBurners / totalCompaniesWithHeating);
int companiesWithHybridHeatpump_pct = roundToInt(100.0 * nbOfCompaniesWithHybridHeatpumps / totalCompaniesWithHeating);
int companiesWithElectricHeatpump_pct = roundToInt(100.0 * nbOfCompaniesWithElectricHeatpumps / totalCompaniesWithHeating);
int companiesWithHTDistrictHeat_pct = roundToInt(100.0 * nbOfCompaniesOnHTHeatGrid / totalCompaniesWithHeating);
int companiesWithLTDistrictHeat_pct = roundToInt(100.0 * nbOfCompaniesOnLTHeatGrid / totalCompaniesWithHeating);
int companiesWithCustomHeating_pct = roundToInt(100.0 * nbOfCompaniesWithCustomHeating / totalCompaniesWithHeating);


sl_heatDemandSlidersCompaniesGasBurnerCompanies_pct.setValue(companiesWithGasBurners_pct, false);
sl_heatDemandSlidersCompaniesHybridHeatPumpCompanies_pct.setValue(companiesWithHybridHeatpump_pct, false);
sl_heatDemandSlidersCompaniesElectricHeatPumpCompanies_pct.setValue(companiesWithElectricHeatpump_pct, false);
sl_heatDemandSlidersCompaniesDistrictHeatingCompanies_pct.setValue(companiesWithHTDistrictHeat_pct, false);
//sl_heatDemandSlidersCompaniesLTDistrictHeatingCompanies_pct.setValue(companiesWithLTDistrictHeat_pct, false); Doesnt exist (yet) for companies
sl_heatingTypeSlidersCompaniesCustom_pct.setValue(companiesWithCustomHeating_pct, false);

/*ALCODEEND*/}

double f_updateHeatingSliders_custom()
{/*ALCODESTART::1754924574713*/
//If you have a custom tab, 
//override this function to make it update automatically
traceln("Forgot to override the update custom heating sliders functionality");
/*ALCODEEND*/}

double f_setHeatingSystems(List<GridConnection> gcList,List<GridConnection> orderedHeatingSystemGCList,OL_GridConnectionHeatingType changedSliderHeatingType,double sliderGoal_pct)
{/*ALCODESTART::1760350664957*/
int totalNrOfHeatingSystems = 0;
int nbGasBurners = 0;
int nbHybridHeatpumps = 0;
int nbElectricHeatpumps = 0;
int nbHTHeatGrid = 0;
int nbLTHeatGrid = 0;
int nbCustomHeating = 0;
List<GridConnection> gasBurnerGCs = new ArrayList<>();
List<GridConnection> hybridHeatpumpGCs = new ArrayList<>();
List<GridConnection> electricHeatpumpGCs = new ArrayList<>();
List<GridConnection> HTHeatGridGCs = new ArrayList<>();
List<GridConnection> LTHeatGridGCs = new ArrayList<>();

for(GridConnection GC : gcList){
	if(GC.v_isActive && GC.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE){
		totalNrOfHeatingSystems++; 		
		switch(GC.f_getCurrentHeatingType()){
			case GAS_BURNER:
				nbGasBurners++;
				if(!GC.f_getHeatingTypeIsGhost()){
					gasBurnerGCs.add(GC);
				}
				break;
			case HYBRID_HEATPUMP:
				nbHybridHeatpumps++;
				if(!GC.f_getHeatingTypeIsGhost()){
					hybridHeatpumpGCs.add(GC);
				}
				break;
			case ELECTRIC_HEATPUMP:
				nbElectricHeatpumps++;
				if(!GC.f_getHeatingTypeIsGhost()){
					electricHeatpumpGCs.add(GC);
				}
				break;
			case DISTRICTHEAT:
				nbHTHeatGrid++;
				if(!GC.f_getHeatingTypeIsGhost()){
					HTHeatGridGCs.add(GC);
				}
				break;
			case LT_DISTRICTHEAT:
				nbLTHeatGrid++;
				if(!GC.f_getHeatingTypeIsGhost()){
					LTHeatGridGCs.add(GC);
				}
				break;
			case CUSTOM:
					nbCustomHeating++;
				if(!GC.f_getHeatingTypeIsGhost()){
					//No collection, as ghost cant be changed anyway
				}
				break;
		}
	}
}

int nbChangedHeatingTypeGoal = roundToInt(totalNrOfHeatingSystems*sliderGoal_pct/100.0);

int currentNumberOfChangedHeatingType = 0;

switch(changedSliderHeatingType){
	case GAS_BURNER:
		currentNumberOfChangedHeatingType = nbGasBurners;
		break;
	case HYBRID_HEATPUMP:
		currentNumberOfChangedHeatingType = nbHybridHeatpumps;
		break;
	case ELECTRIC_HEATPUMP:
		currentNumberOfChangedHeatingType = nbElectricHeatpumps;
		break;
	case DISTRICTHEAT:
		currentNumberOfChangedHeatingType = nbHTHeatGrid;
		break;
	case LT_DISTRICTHEAT:
		currentNumberOfChangedHeatingType = nbLTHeatGrid;
		break;
}



if (currentNumberOfChangedHeatingType < nbChangedHeatingTypeGoal) {
	while ( currentNumberOfChangedHeatingType < nbChangedHeatingTypeGoal ) {
	
		GridConnection changingGC = null;
		if(changedSliderHeatingType != OL_GridConnectionHeatingType.GAS_BURNER && gasBurnerGCs.size() > 0){
			changingGC = findFirst(orderedHeatingSystemGCList, gc -> gasBurnerGCs.contains(gc));
			gasBurnerGCs.remove(changingGC);		
		}
		else if(changedSliderHeatingType != OL_GridConnectionHeatingType.HYBRID_HEATPUMP && hybridHeatpumpGCs.size() > 0){
			changingGC = findFirst(orderedHeatingSystemGCList, gc -> hybridHeatpumpGCs.contains(gc));
			hybridHeatpumpGCs.remove(changingGC);
		}
		else if (changedSliderHeatingType != OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP && electricHeatpumpGCs.size() > 0 ) {
			changingGC = findFirst(orderedHeatingSystemGCList, gc -> electricHeatpumpGCs.contains(gc));
			electricHeatpumpGCs.remove(changingGC);
		}
		else if (changedSliderHeatingType != OL_GridConnectionHeatingType.DISTRICTHEAT && HTHeatGridGCs.size() > 0 ) {
			changingGC = findFirst(orderedHeatingSystemGCList, gc -> HTHeatGridGCs.contains(gc));
			HTHeatGridGCs.remove(changingGC);
		}
		else if (changedSliderHeatingType != OL_GridConnectionHeatingType.LT_DISTRICTHEAT && LTHeatGridGCs.size() > 0 ) {
			changingGC = findFirst(orderedHeatingSystemGCList, gc -> LTHeatGridGCs.contains(gc));
			LTHeatGridGCs.remove(changingGC);
		}
		else{
			throw new RuntimeException("No more GC left to change into raised slider type!" );
		}
		
		if(changingGC == null){
			throw new RuntimeException("orderedHeatingSystemGClist does not contain a GC found by this function! This should not be possible!" );
		}
		
		//Change the current heating type to the new one
		changingGC.f_removeAllHeatingAssets();
		f_addHeatAsset(changingGC, changedSliderHeatingType, f_calculatePeakHeatDemand_kW(changingGC));
		changingGC.f_addHeatManagement(changedSliderHeatingType, false);
		
		//Set reduced heating interval to correct settings (Only if correct heating management is assigned)
		f_setReducedHeatingInterval(changingGC, eb_reducedHeatingIntervalStart.getDoubleValue(), eb_reducedHeatingIntervalEnd.getDoubleValue());
		
		currentNumberOfChangedHeatingType ++;
	}
}
else {
	// Remove Gas burners Trucks
	while ( currentNumberOfChangedHeatingType > nbChangedHeatingTypeGoal ) {
		// replace a gasburner with a hybrid heatpump
		GridConnection changingGC = null;
		OL_GridConnectionHeatingType newHeatingType = OL_GridConnectionHeatingType.GAS_BURNER; // Always change into gasburner system.
		if(changedSliderHeatingType == OL_GridConnectionHeatingType.GAS_BURNER){
			changingGC = findFirst(orderedHeatingSystemGCList, gc -> gasBurnerGCs.contains(gc));
			gasBurnerGCs.remove(changingGC);
			newHeatingType = OL_GridConnectionHeatingType.HYBRID_HEATPUMP; // If removing gasburner, change into hybrid system.
		}
		else if(changedSliderHeatingType == OL_GridConnectionHeatingType.HYBRID_HEATPUMP){
			changingGC = findFirst(orderedHeatingSystemGCList, gc -> hybridHeatpumpGCs.contains(gc));
			hybridHeatpumpGCs.remove(changingGC);
		}
		else if(changedSliderHeatingType == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP){
			changingGC = findFirst(orderedHeatingSystemGCList, gc -> electricHeatpumpGCs.contains(gc));
			electricHeatpumpGCs.remove(changingGC);
		}
		else if(changedSliderHeatingType == OL_GridConnectionHeatingType.DISTRICTHEAT){
			changingGC = findFirst(orderedHeatingSystemGCList, gc -> HTHeatGridGCs.contains(gc));
			HTHeatGridGCs.remove(changingGC);
		}
		else if(changedSliderHeatingType == OL_GridConnectionHeatingType.LT_DISTRICTHEAT){
			changingGC = findFirst(orderedHeatingSystemGCList, gc -> LTHeatGridGCs.contains(gc));
			LTHeatGridGCs.remove(changingGC);
		}
		changingGC.f_removeAllHeatingAssets();
		f_addHeatAsset(changingGC, newHeatingType, f_calculatePeakHeatDemand_kW(changingGC));
		changingGC.f_addHeatManagement(newHeatingType, false);
		
		//Set reduced heating interval to correct settings (Only if correct heating management is assigned)
		f_setReducedHeatingInterval(changingGC, eb_reducedHeatingIntervalStart.getDoubleValue(), eb_reducedHeatingIntervalEnd.getDoubleValue());
		
		currentNumberOfChangedHeatingType--;
	}
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_addHeatAsset(GridConnection parentGC,OL_GridConnectionHeatingType heatAssetType,double maxHeatOutputPower_kW)
{/*ALCODESTART::1760368810352*/
//Initialize parameters
double heatOutputCapacityGasBurner_kW;
double inputCapacityElectric_kW;
double efficiency;
double baseTemperature_degC;
double outputTemperature_degC;
OL_AmbientTempType ambientTempType;
double sourceAssetHeatPower_kW;
double belowZeroHeatpumpEtaReductionFactor;
maxHeatOutputPower_kW = maxHeatOutputPower_kW*2; // Make the asset capacity twice as high, to make sure it can handle the load in other scenarios with more heat consumption.
J_AVGC_data avgc_data = zero_Interface.energyModel.avgc_data;
double timeStep_h = zero_Interface.energyModel.p_timeStep_h;

switch (heatAssetType){ // There is always only one heatingType, If there are many assets the type is CUSTOM

	case GAS_BURNER:
		heatOutputCapacityGasBurner_kW = max(avgc_data.p_minGasBurnerOutputCapacity_kW, maxHeatOutputPower_kW);			
		J_EAConversionGasBurner gasBurner = new J_EAConversionGasBurner(parentGC, heatOutputCapacityGasBurner_kW , avgc_data.p_avgEfficiencyGasBurner_fr, timeStep_h, 90);
		break;
	
	case HYBRID_HEATPUMP:
	
		//Add primary heating asset (heatpump) (if its not part of the basic profile already
		inputCapacityElectric_kW = max(avgc_data.p_minHeatpumpElectricCapacity_kW, maxHeatOutputPower_kW / 3); //-- /3, kan nog kleiner want is hybride zodat gasbrander ook bij springt, dus kleiner MOETEN aanname voor hoe klein onderzoeken
		
		efficiency = avgc_data.p_avgEfficiencyHeatpump_fr;
		baseTemperature_degC = zero_Interface.energyModel.pp_ambientTemperature_degC.getCurrentValue();
		outputTemperature_degC = avgc_data.p_avgOutputTemperatureHeatpump_degC;
		ambientTempType = OL_AmbientTempType.AMBIENT_AIR;
		sourceAssetHeatPower_kW = 0;
		belowZeroHeatpumpEtaReductionFactor = 1;
		
		J_EAConversionHeatPump heatPumpHybrid = new J_EAConversionHeatPump(parentGC, inputCapacityElectric_kW, efficiency, timeStep_h, outputTemperature_degC, baseTemperature_degC, sourceAssetHeatPower_kW, belowZeroHeatpumpEtaReductionFactor, ambientTempType);

		zero_Interface.energyModel.c_ambientDependentAssets.add(heatPumpHybrid);
		
		//Add secondary heating asset (gasburner)
		heatOutputCapacityGasBurner_kW = max(avgc_data.p_minGasBurnerOutputCapacity_kW, maxHeatOutputPower_kW);	
		efficiency = avgc_data.p_avgEfficiencyGasBurner_fr;
		outputTemperature_degC = avgc_data.p_avgOutputTemperatureGasBurner_degC;
	
		J_EAConversionGasBurner gasBurnerHybrid = new J_EAConversionGasBurner(parentGC, heatOutputCapacityGasBurner_kW, efficiency, timeStep_h, outputTemperature_degC);		
		break;
	
	case ELECTRIC_HEATPUMP:
		//Add primary heating asset (heatpump)
		inputCapacityElectric_kW = max(avgc_data.p_minHeatpumpElectricCapacity_kW, maxHeatOutputPower_kW); // Could be smaller due to high cop	
		efficiency = avgc_data.p_avgEfficiencyHeatpump_fr;
		baseTemperature_degC = zero_Interface.energyModel.pp_ambientTemperature_degC.getCurrentValue();
		outputTemperature_degC = avgc_data.p_avgOutputTemperatureHeatpump_degC;
		ambientTempType = OL_AmbientTempType.AMBIENT_AIR;
		sourceAssetHeatPower_kW = 0;
		belowZeroHeatpumpEtaReductionFactor = 1;
		
		new J_EAConversionHeatPump(parentGC, inputCapacityElectric_kW, efficiency, timeStep_h, outputTemperature_degC, baseTemperature_degC, sourceAssetHeatPower_kW, belowZeroHeatpumpEtaReductionFactor, ambientTempType );		
		break;

	case GAS_CHP:
		
		double outputCapacityElectric_kW = (maxHeatOutputPower_kW/avgc_data.p_avgEfficiencyCHP_thermal_fr) * avgc_data.p_avgEfficiencyCHP_electric_fr;
		outputTemperature_degC = avgc_data.p_avgOutputTemperatureCHP_degC;
		efficiency = avgc_data.p_avgEfficiencyCHP_thermal_fr + avgc_data.p_avgEfficiencyCHP_electric_fr;
		
		new J_EAConversionGasCHP(parentGC, outputCapacityElectric_kW, maxHeatOutputPower_kW, efficiency, timeStep_h, outputTemperature_degC );
		break;

	case DISTRICTHEAT:
		double heatOutputCapacityDeliverySet_kW = max(avgc_data.p_minDistrictHeatingDeliverySetOutputCapacity_kW, maxHeatOutputPower_kW);		
		outputTemperature_degC = avgc_data.p_avgOutputTemperatureDistrictHeatingDeliverySet_degC;
		efficiency = avgc_data.p_avgEfficiencyDistrictHeatingDeliverySet_fr;
		
		new J_EAConversionHeatDeliverySet(parentGC, heatOutputCapacityDeliverySet_kW, efficiency, timeStep_h, outputTemperature_degC);
		
		//Add GC to heat grid
		GridNode heatgrid = findFirst(zero_Interface.energyModel.pop_gridNodes, node -> node.p_energyCarrier == OL_EnergyCarriers.HEAT);
		if(heatgrid == null){
			heatgrid = f_createNewHeatGrid();
		}
		//Connect
		parentGC.p_parentNodeHeatID = heatgrid.p_gridNodeID;
		parentGC.p_parentNodeHeat = heatgrid;
		break;
}

/*ALCODEEND*/}

GridNode f_createNewHeatGrid()
{/*ALCODESTART::1760370085949*/
GridNode GN_heat = zero_Interface.energyModel.add_pop_gridNodes();
zero_Interface.energyModel.f_getGridNodesTopLevel().add(GN_heat);
GN_heat.p_gridNodeID = "Heatgrid";

// Check wether transformer capacity is known or estimated
GN_heat.p_capacity_kW = 1000000;	
GN_heat.p_realCapacityAvailable = false;

// Basic GN information
GN_heat.p_description = "Custom toegevoegde Warmtenet";

//Define node type
GN_heat.p_nodeType = OL_GridNodeType.HT;
GN_heat.p_energyCarrier = OL_EnergyCarriers.HEAT;

//Define GN location
GN_heat.p_latitude = 0;
GN_heat.p_longitude = 0;
GN_heat.setLatLon(GN_heat.p_latitude, GN_heat.p_longitude);

//Show warning that heat grid is not a simple solution
zero_Interface.f_setErrorScreen("LET OP: Er is nu een 'warmtenet' gecreëerd. Maar er is geen warmtebron aanwezig in het model. Daarom zal de benodigde warmte voor het warmtenet in de resultaten te zien zijn als warmte import.", 0, 0);

return GN_heat;
/*ALCODEEND*/}

double f_setHeatpumpManagementType(List<GCHouse> gcListHouses,String aansturingsModus)
{/*ALCODESTART::1761582021205*/
//Set interface objects false by default
button_setHeatpumpHeatingManagementOffPeakInterval.setEnabled(false);

//Initialize class type
Class<? extends I_HeatingManagement> heatingManagementClassType;

switch(aansturingsModus){
	case "Standaard":
		heatingManagementClassType = J_HeatingManagementPIcontrol.class;
		break;
	case "Piekmijden (Interval)":
		button_setHeatpumpHeatingManagementOffPeakInterval.setEnabled(true);
		heatingManagementClassType = J_HeatingManagementHeatpumpOffPeak.class;
		break;
	
	case "Aggregator":
		heatingManagementClassType = J_HeatingManagementExternalSetpoint.class;
		break;
	
	default:
		heatingManagementClassType = J_HeatingManagementPIcontrol.class;
}


Triple<OL_GridConnectionHeatingType, Boolean, Boolean> triple = null;

triple = Triple.of(OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, true, false);
zero_Interface.energyModel.c_defaultHeatingStrategies.put( triple, heatingManagementClassType );
triple = Triple.of(OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, true, true);
zero_Interface.energyModel.c_defaultHeatingStrategies.put( triple, heatingManagementClassType );

for(GCHouse GC : gcListHouses){
	if(GC.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP && !GC.f_getHeatingTypeIsGhost()){
		GC.f_addHeatManagement(OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, false);
	}
}
/*ALCODEEND*/}

double f_setReducedHeatingInterval(GridConnection GC,double startTimeOfReducedHeatingInterval_hr,double endTimeOfReducedHeatingInterval_hr)
{/*ALCODESTART::1761645149965*/
if(GC.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP && GC.f_getHeatingManagement() instanceof J_HeatingManagementHeatpumpOffPeak ){
	((J_HeatingManagementHeatpumpOffPeak)GC.f_getHeatingManagement()).setStartTimeOfReducedHeatingInterval_hr(startTimeOfReducedHeatingInterval_hr);
	((J_HeatingManagementHeatpumpOffPeak)GC.f_getHeatingManagement()).setEndTimeOfReducedHeatingInterval_hr(endTimeOfReducedHeatingInterval_hr);
}
/*ALCODEEND*/}

double f_setAllReducedHeatingIntervals(List<GridConnection> gcList,double startTimeOfReducedHeatingInterval_hr,double endTimeOfReducedHeatingInterval_hr)
{/*ALCODESTART::1761645229479*/
for(GridConnection GC : gcList){
	f_setReducedHeatingInterval(GC, startTimeOfReducedHeatingInterval_hr, endTimeOfReducedHeatingInterval_hr);
}
/*ALCODEEND*/}

