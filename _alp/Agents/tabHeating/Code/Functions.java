int f_setHeatingSystemsCompanies(List<GCUtility> gcList,ShapeSlider sliderGasburner,ShapeSlider sliderHeatpump)
{/*ALCODESTART::1722256102007*/
double targetHeatPump_pct = sliderHeatpump.getValue();

//Set the sliders if companyUI is present using the companyUI functions, if not: do it the normal way
if(zero_Interface.c_companyUIs.size()>0){
 	f_setHeatingSystemsWithCompanyUI(gcList, targetHeatPump_pct, sliderGasburner, sliderHeatpump);
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
			new J_EAConversionHeatPump(company, peakHeatDemand_kW, 0.5, zero_Interface.energyModel.p_timeStep_h, 60,  zero_Interface.energyModel.v_currentAmbientTemperature_degC, 0, 1, OL_AmbientTempType.AMBIENT_AIR);				
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
		new J_EAConversionHeatPump(house, peakHeatDemand_kW/3, 0.5, zero_Interface.energyModel.p_timeStep_h, 60,  zero_Interface.energyModel.v_currentAmbientTemperature_degC, 0, 1, OL_AmbientTempType.AMBIENT_AIR);				
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

double f_setHeatingSliders_OLD(int sliderIndex,ShapeSlider gasBurnerSlider,ShapeSlider heatPumpSlider,ShapeSlider hybridHeatPumpSlider,ShapeSlider districtHeatingSlider)
{/*ALCODESTART::1722256269495*/
double pct_naturalGasBurner = gasBurnerSlider.getValue();
double pct_electricHeatPump = heatPumpSlider.getValue();
double pct_hybridHeatPump = 0;
double pct_districtHeating = 0;
//double pct_customHeatingSlider = customHeatingSlider.getValue();

if ( hybridHeatPumpSlider != null ) {
	pct_hybridHeatPump = hybridHeatPumpSlider.getValue();
}
if ( districtHeatingSlider != null ) {
	pct_districtHeating = districtHeatingSlider.getValue();
}

//Set array with pct values
double pctArray[] = {pct_naturalGasBurner, pct_electricHeatPump, pct_hybridHeatPump, pct_districtHeating};//, pct_customHeatingSlider};
double pctExcess = Arrays.stream(pctArray).sum() - 100;
for (int i = 0; i < pctArray.length; i++){
	if (i != (int) sliderIndex) {
		pctArray[i] = max(0, pctArray[i] - pctExcess);
		pctExcess = Arrays.stream(pctArray).sum() - 100;
	}
}
if (pctExcess != 0) {
	traceln("Sliders don't add up to 100%!");
}

// Set Sliders
gasBurnerSlider.setValue(pctArray[0], false);
heatPumpSlider.setValue(pctArray[1], false);
if ( hybridHeatPumpSlider != null ) {
	hybridHeatPumpSlider.setValue(pctArray[2], false);
}
if ( districtHeatingSlider != null ) {
	districtHeatingSlider.setValue(pctArray[3], false);
}
//customHeatingSlider.setValue(pctArray[4], false);



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
			j_ea.resetEnergyProfile();
			j_ea.scaleEnergyProfile( scalingFactor );
		}
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

int f_setHeatingSystemsWithCompanyUI(List<GCUtility> gcList,double targetHeatPump_pct,ShapeSlider sliderGasburner,ShapeSlider sliderHeatpump)
{/*ALCODESTART::1729259449060*/
ArrayList<GCUtility> companies = new ArrayList<GCUtility>(zero_Interface.c_orderedHeatingSystemsCompanies.stream().filter(gcList::contains).filter(x -> x.v_isActive).toList());
int nbActiveCompanies = companies.size() + v_totalNumberOfCustomHeatingSystems;
Pair<Integer, Integer> pair = f_calculateNumberOfGhostHeatingSystems(companies);
int nbOfGhostHeatingSystems = pair.getSecond();// -> Only hybrid added, due to change in ghost asset functionality pair.getFirst() + pair.getSecond(); // Both Electric and Hybrid heatpumps
//Rewrite these functions
int nbHeatPumps = count(companies, gc -> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP) + nbOfGhostHeatingSystems;
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
		int min_nbOfHeatpumps = count(gcList, gc -> gc.v_isActive && gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP) + nbOfGhostHeatingSystems;
		int min_pct_ElectricHeatpumpSlider = roundToInt( min_nbOfHeatpumps * 100.0 / nbActiveCompanies );
		sliderHeatpump.setValue(min_pct_ElectricHeatpumpSlider, false);
		sliderGasburner.setValue(100- sl_heatingTypeSlidersCompaniesCustom_pct.getValue() - min_pct_ElectricHeatpumpSlider, false);
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
		int min_nbOfHeatpumps = count(gcList, gc -> gc.v_isActive && gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP) + nbOfGhostHeatingSystems;
		int min_pct_ElectricHeatpumpSlider = roundToInt( min_nbOfHeatpumps * 100.0 / nbActiveCompanies );
		sliderHeatpump.setValue(min_pct_ElectricHeatpumpSlider, false);
		sliderGasburner.setValue(100 - sl_heatingTypeSlidersCompaniesCustom_pct.getValue() - min_pct_ElectricHeatpumpSlider, false);
		return;
	}
}
/*ALCODEEND*/}

Pair<Integer, Integer> f_calculateNumberOfGhostHeatingSystems(List<GCUtility> gcList)
{/*ALCODESTART::1729262524479*/
int numberOfGhostHeatingSystems_ElectricHeatpumps = 0;
int numberOfGhostHeatingSystems_HybridHeatpumps = 0;
for (GCUtility gc : gcList) {
	if ( gc.p_heatingManagement != null && gc.p_heatingManagement instanceof J_HeatingManagementGhost && gc.v_isActive ) {
		if (gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP){
			numberOfGhostHeatingSystems_ElectricHeatpumps++;
		}
		if (gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.HYBRID_HEATPUMP){
			numberOfGhostHeatingSystems_HybridHeatpumps++;
		}
	}
}

return new Pair(numberOfGhostHeatingSystems_ElectricHeatpumps, numberOfGhostHeatingSystems_HybridHeatpumps);
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
		double maxValue = Arrays.stream(j_ea.a_energyProfile_kWh).max().getAsDouble();
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
		zero_Interface.f_setErrorScreen("LET OP: Er is nu een 'warmtenet' gecreëerd. Maar er is geen warmtebron aanwezig in het model. Daarom zal de benodigde warmte voor het warmtenet in de resultaten te zien zijn als warmte import.");
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
	
	//house.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
	house.v_districtHeatDelivery_kW = 0;
	
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
		double peakHeatDemand_kW = Arrays.stream(heatDemandProfile.a_energyProfile_kWh).max().orElseThrow(() -> new RuntimeException());
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

double f_setAircos(double desiredShare)
{/*ALCODESTART::1749739532217*/
double nbHousesWithAirco = count(zero_Interface.energyModel.Houses, x -> x.p_airco != null);
double nbHouses = zero_Interface.energyModel.Houses.size();
traceln("Previous nb households with airco: " + nbHousesWithAirco);
while ( roundToInt(nbHouses * desiredShare) > nbHousesWithAirco ) {
	GCHouse house = randomWhere(zero_Interface.energyModel.Houses, x -> x.p_airco == null);
	double aircoPower_kW = roundToDecimal(uniform(3,6),1);
	new J_EAAirco(house, aircoPower_kW, zero_Interface.energyModel.p_timeStep_h);
	nbHousesWithAirco ++;
}
while ( roundToInt(nbHouses * desiredShare) < nbHousesWithAirco ) {
	GCHouse house = randomWhere(zero_Interface.energyModel.Houses, x -> x.p_airco != null);
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
		zero_Interface.f_setErrorScreen("LET OP: Er is nu een 'warmtenet' gecreëerd. Maar er is geen warmtebron aanwezig in het model. Daarom zal de benodigde warmte voor het warmtenet in de resultaten te zien zijn als warmte import.");
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
		zero_Interface.energyModel.v_currentAmbientTemperature_degC,
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
	house.v_districtHeatDelivery_kW = 0;
	
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

double f_householdInsulation(double houses_pct)
{/*ALCODESTART::1752227724432*/
int nbHouses = count(zero_Interface.energyModel.Houses, x -> x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
int nbHousesWithImprovedInsulation = count(zero_Interface.energyModel.Houses, x -> x.p_hasAdditionalInsulation && x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
int targetNbHousesWithImprovedInsulation = roundToInt(houses_pct / 100.0 * nbHouses);

while (nbHousesWithImprovedInsulation < targetNbHousesWithImprovedInsulation) {
	GCHouse house = findFirst(zero_Interface.energyModel.Houses, x -> !x.p_hasAdditionalInsulation && x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
	if (house != null) {
		house.p_hasAdditionalInsulation = true;
		double lossFactor_WpK = house.p_BuildingThermalAsset.getLossFactor_WpK();
		house.p_BuildingThermalAsset.setLossFactor_WpK( 0.7 * lossFactor_WpK );
		nbHousesWithImprovedInsulation++;
	}
	else {
		throw new RuntimeException("Unable to find house that does not yet have additional insulation");
	}
}
while (nbHousesWithImprovedInsulation > targetNbHousesWithImprovedInsulation) {
	GCHouse house = findFirst(zero_Interface.energyModel.Houses, x -> x.p_hasAdditionalInsulation && x.p_energyLabel != OL_GridConnectionIsolationLabel.A);
	if (house != null) {
		house.p_hasAdditionalInsulation = false;
		double lossFactor_WpK = house.p_BuildingThermalAsset.getLossFactor_WpK();
		house.p_BuildingThermalAsset.setLossFactor_WpK( lossFactor_WpK / 0.7 );
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
int nbHousesWithPT = count(houses, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.ptProductionHeat_kW) == true);
int nbHousesWithPTGoal = roundToInt(PT_pct / 100.0 * nbHouses);

while ( nbHousesWithPTGoal < nbHousesWithPT ) { // remove excess PV systems
	GCHouse house = findFirst(houses, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.ptProductionHeat_kW) == true);	
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
	GCHouse house = findFirst(houses, x -> x.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.ptProductionHeat_kW) == false);
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

double pctExcess = Arrays.stream(pctArray).sum() - 100;

for (int i = 0; i < pctArray.length; i++) {
    if (i != sliderIndex && i != 4){// Skip moved slider & custom slider
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

// Set Sliders
gasBurnerSlider.setValue(pctArray[0], false);
heatPumpSlider.setValue(pctArray[1], false);
if(hybridHeatPumpSlider != null){
	hybridHeatPumpSlider.setValue(pctArray[2], false);
}
if(districtHeatingSlider != null){
	districtHeatingSlider.setValue(pctArray[3], false);
}
if(customHeatingSlider != null){
	customHeatingSlider.setValue(pctArray[4], false);
}


/*ALCODEEND*/}

