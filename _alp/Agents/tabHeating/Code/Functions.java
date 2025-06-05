int f_setHeatingSystemsCompanies(List<GCUtility> gcList,ShapeSlider sliderGasburner,ShapeSlider sliderHeatpump)
{/*ALCODESTART::1722256102007*/
//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

double targetHeatPump_pct = sliderHeatpump.getValue();


//Set the sliders if companyUI is present using the companyUI functions, if not: do it the normal way
if(zero_Interface.c_companyUIs.size()>0){
 	f_setHeatingSystemsWithCompanyUI(gcList, targetHeatPump_pct, sliderGasburner, sliderHeatpump);
}
else{
	ArrayList<GCUtility> companies = new ArrayList<GCUtility>(zero_Interface.c_orderedHeatingSystemsCompanies.stream().filter(gcList::contains).toList());
	double nbHeatPumps = count(gcList, gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
	int targetHeatPumpAmount = roundToInt( targetHeatPump_pct / 100.0 * gcList.size());
	
	while ( targetHeatPumpAmount < nbHeatPumps) { // remove excess heatpumps, replace with gasburners.
		GCUtility company = findFirst(companies, x->x.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
		if (company != null) {
			company.p_primaryHeatingAsset.removeEnergyAsset();
			nbHeatPumps--;
			companies.remove(company);
			zero_Interface.c_orderedHeatingSystemsCompanies.remove(company);
			zero_Interface.c_orderedHeatingSystemsCompanies.add(0, company);
			double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(company);
			new J_EAConversionGasBurner(company, peakHeatDemand_kW, zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC);
			company.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
		}
		else {
			throw new RuntimeException("Can't find Heatpump in company that should have heatpump in f_setHeatingSystemsCompanies.");
		}
	}
	
	while ( targetHeatPumpAmount > nbHeatPumps) { // remove gasburners, add heatpumps.
		GCUtility company = findFirst(companies, x -> x.p_primaryHeatingAsset instanceof J_EAConversionGasBurner);
		if (company != null) {			
			company.p_primaryHeatingAsset.removeEnergyAsset();
			nbHeatPumps++;		
			companies.remove(company);
			zero_Interface.c_orderedHeatingSystemsCompanies.remove(company);
			zero_Interface.c_orderedHeatingSystemsCompanies.add(0, company);	
			double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(company);
			new J_EAConversionHeatPump(company, peakHeatDemand_kW, 0.5, zero_Interface.energyModel.p_timeStep_h, 60,  zero_Interface.energyModel.v_currentAmbientTemperature_degC, 0, 1);				
			company.p_heatingType = OL_GridConnectionHeatingType.HEATPUMP_AIR;
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
double nbHeatPumps = count(gcList, gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
int targetHeatPumpAmount = roundToInt( targetHeatPump_pct / 100.0 * gcList.size());

while ( targetHeatPumpAmount < nbHeatPumps) { // remove excess heatpumps, replace with gasburners.
	GCHouse house = findFirst(houses, x->x.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
	if (house != null) {
		house.p_primaryHeatingAsset.removeEnergyAsset();
		nbHeatPumps--;
		houses.remove(house);
		zero_Interface.c_orderedHeatingSystemsHouses.remove(house);
		zero_Interface.c_orderedHeatingSystemsHouses.add(0, house);
		double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(house);
		new J_EAConversionGasBurner(house, peakHeatDemand_kW, zero_Interface.energyModel.avgc_data.p_avgEfficiencyGasBurner, zero_Interface.energyModel.p_timeStep_h, zero_Interface.energyModel.avgc_data.p_avgOutputTemperatureGasBurner_degC);
		house.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
	}
	else {
		throw new RuntimeException("Can't find Heatpump in house that should have heatpump in f_setHeatingSystemsHouseholds.");
	}
}

while ( targetHeatPumpAmount > nbHeatPumps) { // remove gasburners, add heatpumps.
	GCHouse house = findFirst(houses, x -> x.p_primaryHeatingAsset instanceof J_EAConversionGasBurner);
	if (house != null) {			
		house.p_primaryHeatingAsset.removeEnergyAsset();
		nbHeatPumps++;		
		houses.remove(house);
		zero_Interface.c_orderedHeatingSystemsHouses.remove(house);
		zero_Interface.c_orderedHeatingSystemsHouses.add(0, house);		
		double peakHeatDemand_kW = f_calculatePeakHeatDemand_kW(house);
		new J_EAConversionHeatPump(house, peakHeatDemand_kW, 0.5, zero_Interface.energyModel.p_timeStep_h, 60,  zero_Interface.energyModel.v_currentAmbientTemperature_degC, 0, 1);				
		house.p_heatingType = OL_GridConnectionHeatingType.HEATPUMP_AIR;
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

double f_setHeatingSliders(int sliderIndex,ShapeSlider gasBurnerSlider,ShapeSlider heatPumpSlider,ShapeSlider hybridHeatPumpSlider,ShapeSlider districtHeatingSlider)
{/*ALCODESTART::1722256269495*/
double pct_naturalGasBurner = gasBurnerSlider.getValue();
double pct_electricHeatPump = heatPumpSlider.getValue();
double pct_hybridHeatPump = 0;
double pct_districtHeating = 0;

if ( hybridHeatPumpSlider != null ) {
	pct_hybridHeatPump = hybridHeatPumpSlider.getValue();
}
if ( districtHeatingSlider != null ) {
	pct_districtHeating = districtHeatingSlider.getValue();
}

//Set array with pct values
double pctArray[] = {pct_naturalGasBurner, pct_electricHeatPump, pct_hybridHeatPump, pct_districtHeating};
double pctExcess = Arrays.stream(pctArray).sum() - 100;
for (int i = 0; i < 4; i++){
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
int nbActiveCompanies = companies.size();
Pair<Integer, Integer> pair = f_calculateNumberOfGhostHeatingSystems(companies);
int nbOfGhostHeatingSystems = pair.getFirst() + pair.getSecond(); // Both Electric and Hybrid heatpumps
int nbHeatPumps = count(companies, gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump) + nbOfGhostHeatingSystems;
int targetHeatPumpAmount = roundToInt( targetHeatPump_pct / 100.0 * nbActiveCompanies) + nbOfGhostHeatingSystems;


while ( targetHeatPumpAmount < nbHeatPumps){ // remove excess heatpumps of companies that didnt start with a heatpump, replace with gasburners.
	GCUtility company = findFirst(companies, gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
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
		int min_nbOfHeatpumps = count(gcList, gc -> gc.v_isActive && gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump) + nbOfGhostHeatingSystems;
		int min_pct_ElectricHeatpumpSlider = roundToInt( min_nbOfHeatpumps * 100.0 / nbActiveCompanies );
		sliderHeatpump.setValue(min_pct_ElectricHeatpumpSlider, false);
		sliderGasburner.setValue(100-min_pct_ElectricHeatpumpSlider, false);
		return;
	}
}

while ( targetHeatPumpAmount > nbHeatPumps) { // remove gasburners, add heatpumps.
		
	GCUtility company = findFirst(companies, gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionGasBurner);
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
		int min_nbOfHeatpumps = count(gcList, gc -> gc.v_isActive && gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump) + nbOfGhostHeatingSystems;
		int min_pct_ElectricHeatpumpSlider = roundToInt( min_nbOfHeatpumps * 100.0 / nbActiveCompanies );
		sliderHeatpump.setValue(min_pct_ElectricHeatpumpSlider, false);
		sliderGasburner.setValue(100-min_pct_ElectricHeatpumpSlider, false);
		return;
	}
}
/*ALCODEEND*/}

Pair<Integer, Integer> f_calculateNumberOfGhostHeatingSystems(List<GCUtility> gcList)
{/*ALCODESTART::1729262524479*/
Integer numberOfGhostHeatingSystems_ElectricHeatpumps = 0;
Integer numberOfGhostHeatingSystems_HybridHeatpumps = 0;

for (GCUtility gc : gcList) {
	if ( gc.v_hasQuarterHourlyValues && gc.v_isActive ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		int i = indexOf(companyUI.c_ownedGridConnections.stream().toArray(), gc);
		if (companyUI.c_scenarioSettings_Current.get(i).getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP){
			numberOfGhostHeatingSystems_ElectricHeatpumps++;
		}
		if (companyUI.c_scenarioSettings_Current.get(i).getCurrentHeatingType() == OL_GridConnectionHeatingType.HYBRID_HEATPUMP){
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
	if (j_ea.getEAType() == OL_EnergyAssetType.HEAT_DEMAND) {
		TableFunction tf = j_ea.getProfilePointer().getTableFunction();
		double maxFactor = Arrays.stream(tf.getValues()).max().getAsDouble();
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
	peakHeatDemand_kW += gc.p_BuildingThermalAsset.getCapacityHeat_kW();
}
return peakHeatDemand_kW;
/*ALCODEEND*/}

