int f_setHeatingSystemsCompanies(ShapeSlider sliderGasburner,ShapeSlider sliderHeatpump)
{/*ALCODESTART::1722256102007*/
double targetHeatPump_pct = sliderHeatpump.getValue();

//Set the sliders if companyUI is present using the companyUI functions, if not: do it the normal way
if(zero_Interface.c_companyUIs.size()>0){
 	f_setHeatingSystemsWithCompanyUI(targetHeatPump_pct, sliderGasburner, sliderHeatpump);
}
else{
	//Setting Heating systems
	int nbHeatPumps = count(zero_Interface.energyModel.UtilityConnections, gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
	int nbConnectionsWithHeat = count(zero_Interface.energyModel.UtilityConnections, gc -> gc.p_primaryHeatingAsset != null);
	int targetHeatPumpAmount = roundToInt(targetHeatPump_pct/100.0* nbConnectionsWithHeat);

	if( targetHeatPumpAmount < nbHeatPumps){
		while ( targetHeatPumpAmount < nbHeatPumps) { // remove excess heatpumps, replace with gasburners.
			//traceln("While loop for removing heatpumps");
			GridConnection gc = findFirst(zero_Interface.c_orderedHeatingSystemsCompanies, x->x.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
			if (gc!=null) {
				
				J_EA heatPump = gc.p_primaryHeatingAsset;
				//traceln("Found heatpump: " + heatPump);		
				heatPump.removeEnergyAsset();
				zero_Interface.c_orderedHeatingSystemsCompanies.remove(gc);
				zero_Interface.c_orderedHeatingSystemsCompanies.add(0, gc);
				//traceln("Removing heatpump from GridConnection:" + gc.p_gridConnectionID);
				J_EAConsumption heatDemandAsset = findFirst(gc.c_consumptionAssets, j_ea->j_ea.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND);
				J_EAConversionGasBurner gasBurner;
				if (heatDemandAsset != null) {
					gasBurner = new J_EAConversionGasBurner(gc, heatDemandAsset.yearlyDemand_kWh/8760*10, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
				} else {
					J_EAProfile heatDemandProfile = (J_EAProfile)findFirst(gc.c_profileAssets, x->x instanceof J_EAProfile && x.energyCarrier == OL_EnergyCarriers.HEAT);
					double peakHeatDemand_kW = Arrays.stream(heatDemandProfile.a_energyProfile_kWh).max().orElseThrow(() -> new RuntimeException());
					gasBurner = new J_EAConversionGasBurner(gc, peakHeatDemand_kW, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
				}			
			}
			
			nbHeatPumps--;
			//traceln("Number of heatpumps: " + nbHeatPumps);
		}
	 
	} else { 
		while ( targetHeatPumpAmount > nbHeatPumps) { // remove gasburners, add heatpumps.
			//traceln("While loop for adding heatpumps");
			GridConnection gc = findFirst(zero_Interface.c_orderedHeatingSystemsCompanies, x->x.p_primaryHeatingAsset instanceof J_EAConversionGasBurner);
	
			if (gc!=null) {

				J_EA gasBurner = gc.p_primaryHeatingAsset;
				gasBurner.removeEnergyAsset();
				
				J_EAConversionHeatPump heatPump;
				J_EAConsumption heatDemandAsset = findFirst(gc.c_consumptionAssets, j_ea->j_ea.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND);
				if (heatDemandAsset != null) {
					heatPump = new J_EAConversionHeatPump(gc, heatDemandAsset.yearlyDemand_kWh/8760*10 / 3, 0.5, zero_Interface.energyModel.p_timeStep_h, 60, zero_Interface.energyModel.v_currentAmbientTemperature_degC, 0, 1);
				} else {
					J_EAProfile heatDemandProfile = (J_EAProfile)findFirst(gc.c_profileAssets, x->x instanceof J_EAProfile && x.energyCarrier == OL_EnergyCarriers.HEAT);
					double peakHeatDemand_kW = Arrays.stream(heatDemandProfile.a_energyProfile_kWh).max().orElseThrow(() -> new RuntimeException());
					heatPump = new J_EAConversionHeatPump(gc, peakHeatDemand_kW / 3, 0.5, zero_Interface.energyModel.p_timeStep_h, 60, zero_Interface.energyModel.v_currentAmbientTemperature_degC, 0, 1);
				}
				zero_Interface.c_orderedHeatingSystemsCompanies.remove(gc);
				zero_Interface.c_orderedHeatingSystemsCompanies.add(0, gc);		 
				//traceln("Added heatpump to GridConnection: " + gc.p_gridConnectionID);
			} else {
				traceln("No more gasburners to replace!");
				break;
			}	
			
			nbHeatPumps++;
		}	
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setHeatingSystemsHouseholds(double targetHeatPump_pct)
{/*ALCODESTART::1722256221655*/
double nbHeatPumps = count(zero_Interface.energyModel.Houses, house -> house.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
int targetHeatPumpAmount = roundToInt( targetHeatPump_pct / 100.0 *(zero_Interface.energyModel.Houses.size()));

if( targetHeatPumpAmount < nbHeatPumps){
	while ( targetHeatPumpAmount < nbHeatPumps) { // remove excess heatpumps, replace with gasburners.
		GridConnection gc = findFirst(zero_Interface.c_orderedHeatingSystemsHouses, x->x.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
		if (gc != null) {
			J_EA heatPump = gc.p_primaryHeatingAsset;
			heatPump.removeEnergyAsset();
			nbHeatPumps --;
			zero_Interface.c_orderedHeatingSystemsHouses.remove(gc);
			zero_Interface.c_orderedHeatingSystemsHouses.add(0, gc);
			 
			// In loader: f_addEnergyAssetsToHouses staat jaarlijks gasverbruik in else???
			J_EAConsumption heatDemandAsset = findFirst(gc.c_consumptionAssets, j_ea->j_ea.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND);
			J_EAConversionGasBurner gasBurner;
			if (heatDemandAsset != null) {
				gasBurner = new J_EAConversionGasBurner(gc, heatDemandAsset.yearlyDemand_kWh/8760*10, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
			}
			else {
				J_EAProfile heatDemandProfile = (J_EAProfile)findFirst(gc.c_profileAssets, x->x instanceof J_EAProfile && x.energyCarrier == OL_EnergyCarriers.HEAT);
				double peakHeatDemand_kW = Arrays.stream(heatDemandProfile.a_energyProfile_kWh).max().orElseThrow(() -> new RuntimeException());
				gasBurner = new J_EAConversionGasBurner(gc, peakHeatDemand_kW, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
			}	
			gc.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;		
		}
	}
 
} 
else { 
	while ( targetHeatPumpAmount > nbHeatPumps) { // remove gasburners, add heatpumps.
		GCHouse gc = (GCHouse)findFirst(zero_Interface.c_orderedHeatingSystemsHouses, x -> x.p_primaryHeatingAsset instanceof J_EAConversionGasBurner);
		if (gc != null) {			
			gc.p_primaryHeatingAsset.removeEnergyAsset();
			J_EAConversionHeatPump heatPump;
			J_EAConsumption heatDemandAsset = findFirst(gc.c_consumptionAssets, j_ea -> j_ea.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND);
			if (heatDemandAsset != null) { // als house een standaard warmtebehoefte profiel heeft
				heatPump = new J_EAConversionHeatPump(gc, heatDemandAsset.yearlyDemand_kWh/8760*10 / 3, 0.5, zero_Interface.energyModel.p_timeStep_h, 60,  zero_Interface.energyModel.v_currentAmbientTemperature_degC, 0, 1);				
			} 
			else if (gc.p_BuildingThermalAsset != null){ //als huis een building heatmodel heeft
				heatPump = new J_EAConversionHeatPump(gc, 5, 0.5, zero_Interface.energyModel.p_timeStep_h, 60,  zero_Interface.energyModel.v_currentAmbientTemperature_degC, 0, 1);				
				//J_EAStorageHeat buffer = new J_EAStorageHeat(gc, OL_EAStorageTypes.HEATBUFFER, 10, 0, energyModel.p_timeStep_h, 75, 15, 90, 50, 50000, "AIR" );
				//Agent parentAgent, OL_EAStorageTypes heatStorageType, double capacityHeat_kW, double lossFactor_WpK, double timestep_h, double initialTemperature_degC, double minTemperature_degC, double maxTemperature_degC, double setTemperature_degC, double heatCapacity_JpK, String ambientTempType 
				//voeg heatbuffer toe
			}
			else { //anders moet het huis een heatProfiel krijgen
				J_EAProfile heatDemandProfile = (J_EAProfile)findFirst(gc.c_profileAssets, x->x instanceof J_EAProfile && x.energyCarrier == OL_EnergyCarriers.HEAT);
				double peakHeatDemand_kW = Arrays.stream(heatDemandProfile.a_energyProfile_kWh).max().orElseThrow(() -> new RuntimeException());
				heatPump = new J_EAConversionHeatPump(gc, peakHeatDemand_kW / 3, 0.5, zero_Interface.energyModel.p_timeStep_h, 60, zero_Interface.energyModel.v_currentAmbientTemperature_degC, 0, 1);
			}
			nbHeatPumps ++;
			zero_Interface.c_orderedHeatingSystemsHouses.remove(gc);
			zero_Interface.c_orderedHeatingSystemsHouses.add(0, gc);
			gc.p_heatingType = OL_GridConnectionHeatingType.HEATPUMP_AIR;
		} 
		else {
			traceln("No more gasburners to replace!");
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

double f_setDemandReductionHeatingCompanies(double demandReduction_pct)
{/*ALCODESTART::1722335783993*/
double newHeatDemandReduction_pct = demandReduction_pct;
double consumptionScaling_fr = 1  - newHeatDemandReduction_pct/100;

for (J_EA j_ea : zero_Interface.energyModel.f_getEnergyAssets()){
	Agent parentGC = j_ea.getParentAgent();
	if(parentGC != null && parentGC instanceof GCUtility){
		if (j_ea instanceof J_EAConsumption) {
			if (j_ea.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND) {
				((J_EAConsumption)j_ea).setConsumptionScaling_fr(consumptionScaling_fr);
				
				if (zero_Interface.c_companyUIs.size()>0){
					UI_company companyUI = zero_Interface.c_companyUIs.get(((GridConnection)parentGC).p_owner.p_connectionOwnerIndexNr);
					if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == parentGC) { // should also check the setting of selected GC
						companyUI.sl_heatDemandCompanyReduction.setValue(newHeatDemandReduction_pct, false);
					}
				}	
			}
		}
		if (j_ea instanceof J_EAProfile) {
			if (((J_EAProfile) j_ea).energyCarrier == OL_EnergyCarriers.HEAT) {
				((J_EAProfile) j_ea).resetEnergyProfile();
				((J_EAProfile) j_ea).scaleEnergyProfile(consumptionScaling_fr);
				if (zero_Interface.c_companyUIs.size()>0){
					UI_company companyUI = zero_Interface.c_companyUIs.get(((GridConnection)parentGC).p_owner.p_connectionOwnerIndexNr);
					if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == parentGC) { // should also check the setting of selected GC
						companyUI.sl_heatDemandCompanyReduction.setValue(newHeatDemandReduction_pct, false);
					}
				}
			}
		}
	}
}


v_heatDemandReductionCompanies_pct = newHeatDemandReduction_pct;

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setDemandReductionHeatingHouses(double demandReduction_pct)
{/*ALCODESTART::1727269173559*/
double newHeatDemandReduction_pct = demandReduction_pct;
double consumptionScaling_fr = 1  - newHeatDemandReduction_pct/100;

for (J_EA j_ea : zero_Interface.energyModel.f_getEnergyAssets()){
	Agent parentGC = j_ea.getParentAgent();
	if(parentGC != null && parentGC instanceof GCHouse){
		if (j_ea instanceof J_EAConsumption) {
			if (j_ea.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND) {
				((J_EAConsumption)j_ea).setConsumptionScaling_fr(consumptionScaling_fr);
			}
		}
		if (j_ea instanceof J_EAProfile) {
			if (((J_EAProfile) j_ea).energyCarrier == OL_EnergyCarriers.HEAT) {
				((J_EAProfile) j_ea).resetEnergyProfile();
				((J_EAProfile) j_ea).scaleEnergyProfile(consumptionScaling_fr);
			}
		}
	}
}


v_heatDemandReductionHouses_pct = newHeatDemandReduction_pct;

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

int f_setHeatingSystemsWithCompanyUI(double targetHeatPump_pct,ShapeSlider sliderGasburner,ShapeSlider sliderHeatpump)
{/*ALCODESTART::1729259449060*/
//Setting Heating systems
int nbHeatPumps = count(zero_Interface.energyModel.UtilityConnections, gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump && gc.v_isActive) + v_totalNumberOfGhostHeatingSystems_ElectricHeatpumps + v_totalNumberOfGhostHeatingSystems_HybridHeatpumps;// && gc.p_secondaryHeatingAsset == null );
int nbConnectionsWithHeat = count(zero_Interface.energyModel.UtilityConnections, gc -> gc.p_primaryHeatingAsset != null && gc.v_isActive) + v_totalNumberOfGhostHeatingSystems_ElectricHeatpumps + v_totalNumberOfGhostHeatingSystems_HybridHeatpumps;
int targetHeatPumpAmount = roundToInt(targetHeatPump_pct/100.0* nbConnectionsWithHeat);

if( targetHeatPumpAmount < nbHeatPumps){
	while ( targetHeatPumpAmount < nbHeatPumps){ // remove excess heatpumps of companies that didnt start with a heatpump, replace with gasburners.
		
		GridConnection GC = findFirst(zero_Interface.c_orderedHeatingSystemsCompanies, gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
		
		if (GC!=null) {
			UI_company companyUI = zero_Interface.c_companyUIs.get(GC.p_owner.p_connectionOwnerIndexNr);
			
			companyUI.b_runningMainInterfaceSlider = true;
			if(companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) != GC){
				int selectedGC = 0;
				if(companyUI.v_currentSelectedGCnr != 0){
					companyUI.GCnr_selection.setValue(selectedGC, true); 
				}
				while (companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) != GC){ //Check if selected
					companyUI.GCnr_selection.setValue(selectedGC++, true);
				}
			}
			
			// rbSetting Bugfix, does not change the heating system if the radiobutton was disabled!			
			boolean rbSetting = companyUI.rb_heatingTypePrivateUI.isEnabled();
			companyUI.rb_heatingTypePrivateUI.setEnabled(true);
			companyUI.rb_heatingTypePrivateUI.setValue(0, true);
			companyUI.rb_heatingTypePrivateUI.setEnabled(rbSetting);
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
			companyUI.b_runningMainInterfaceSlider = false;

			//Reorder c_orderedHeatingSystems
			zero_Interface.c_orderedHeatingSystemsCompanies.remove(GC);
			zero_Interface.c_orderedHeatingSystemsCompanies.add(0, GC);
			
			nbHeatPumps--;
		}
		else{//No more heating assets to adjust: this is the minimum: set slider to minimum and do nothing else
			int min_nbOfHeatpumps = count(zero_Interface.energyModel.UtilityConnections, gc -> gc.v_isActive && gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump) + v_totalNumberOfGhostHeatingSystems_ElectricHeatpumps + v_totalNumberOfGhostHeatingSystems_HybridHeatpumps;
			int min_pct_ElectricHeatpumpSlider = (int) ((float) min_nbOfHeatpumps / nbConnectionsWithHeat * 100.0);
			sliderHeatpump.setValue(min_pct_ElectricHeatpumpSlider, false);
			sliderGasburner.setValue(100-min_pct_ElectricHeatpumpSlider, false);
			return;
		}
	}
} else { 
	while ( targetHeatPumpAmount > nbHeatPumps) { // remove gasburners, add heatpumps.
			
		GridConnection GC = findFirst(zero_Interface.c_orderedHeatingSystemsCompanies, gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionGasBurner);
		if (GC!=null) {
			UI_company companyUI = zero_Interface.c_companyUIs.get(GC.p_owner.p_connectionOwnerIndexNr);
			companyUI.b_runningMainInterfaceSlider = true;
			if(companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) != GC){
				int selectedGC = 0;
				if(companyUI.v_currentSelectedGCnr != 0){
					companyUI.GCnr_selection.setValue(selectedGC, true); 
				}
				while (companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) != GC){ //Check if selected
					companyUI.GCnr_selection.setValue(selectedGC++, true);
				}
			}
			
			// rbSetting Bugfix, does not change the heating system if the radiobutton was disabled!
			boolean rbSetting = companyUI.rb_heatingTypePrivateUI.isEnabled();
			companyUI.rb_heatingTypePrivateUI.setEnabled(true);
			companyUI.rb_heatingTypePrivateUI.setValue(2, true);
			companyUI.rb_heatingTypePrivateUI.setEnabled(rbSetting);
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
			companyUI.b_runningMainInterfaceSlider = false;
			
			//Reorder c_orderedHeatingSystems
			zero_Interface.c_orderedHeatingSystemsCompanies.remove(GC);
			zero_Interface.c_orderedHeatingSystemsCompanies.add(0, GC);
			
			nbHeatPumps++;
		}
		else{//No more gas burner assets to adjust: this is the minimum: set slider to minimum and do nothing else
			int min_nbOfHeatpumps = count(zero_Interface.energyModel.UtilityConnections, gc -> gc.v_isActive && gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump) + v_totalNumberOfGhostHeatingSystems_ElectricHeatpumps + v_totalNumberOfGhostHeatingSystems_HybridHeatpumps;
			int min_pct_ElectricHeatpumpSlider = (int) ((float) min_nbOfHeatpumps / nbConnectionsWithHeat * 100.0);
			sliderHeatpump.setValue(min_pct_ElectricHeatpumpSlider, false);
			sliderGasburner.setValue(100-min_pct_ElectricHeatpumpSlider, false);
			return;
		}
	}	
}
/*ALCODEEND*/}

double f_calculateNumberOfGhostHeatingSystems()
{/*ALCODESTART::1729262524479*/
v_totalNumberOfGhostHeatingSystems_ElectricHeatpumps = 0;
v_totalNumberOfGhostHeatingSystems_HybridHeatpumps = 0;

for (UI_company companyUI : zero_Interface.c_companyUIs ){
	
	for(int i = 0; i < companyUI.c_ownedGridConnections.size(); i++){
		if(companyUI.c_ownedGridConnections.get(i).v_hasQuarterHourlyValues && companyUI.c_ownedGridConnections.get(i).v_isActive){
			if (companyUI.c_scenarioSettings_Current.get(i).getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP){
				v_totalNumberOfGhostHeatingSystems_ElectricHeatpumps++;
			}
			if (companyUI.c_scenarioSettings_Current.get(i).getCurrentHeatingType() == OL_GridConnectionHeatingType.HYBRID_HEATPUMP){
				v_totalNumberOfGhostHeatingSystems_HybridHeatpumps++;
			}
		}
	}
}

/*ALCODEEND*/}

double f_addDistrictHeatingToAllHouses()
{/*ALCODESTART::1749739532180*/
for (GCHouse house: zero_Interface.energyModel.Houses ) {
	house.p_primaryHeatingAsset.removeEnergyAsset();
	if (house.p_secondaryHeatingAsset != null) {
		house.p_secondaryHeatingAsset.removeEnergyAsset(); 
	}
	if (house.p_tertiaryHeatingAsset != null) {
		house.p_tertiaryHeatingAsset.removeEnergyAsset(); 
	}
	if ( house.p_heatBuffer != null){
		house.p_heatBuffer.removeEnergyAsset();
	}
	house.p_heatingType = OL_GridConnectionHeatingType.DISTRICTHEAT;
	new J_EAConversionHeatDeliverySet(house, 5.0, 1.0, 50, zero_Interface.energyModel.p_timeStep_h);
}
/*ALCODEEND*/}

double f_removeDistrictHeatingFromAllHouses()
{/*ALCODESTART::1749739532202*/
for (GCHouse house: zero_Interface.energyModel.Houses ) {
	house.p_primaryHeatingAsset.removeEnergyAsset();
	house.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
	house.v_districtHeatDelivery_kW = 0;
	
	//add gasburner
	house.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;	

	J_EAConsumption heatDemandAsset = findFirst(house.c_consumptionAssets, j_ea -> j_ea.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND);
	J_EAConversionGasBurner gasBurner;
	//if house has follows the general heat deamnd profile
	if (heatDemandAsset != null) {
		gasBurner = new J_EAConversionGasBurner(house, OL_EnergyAssetType.GAS_BURNER, heatDemandAsset.yearlyDemandHeat_kWh/8760*10, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
	}
	//if house has a thermalBuildingAsset
	else if (house.p_BuildingThermalAsset != null){
		double gasBurnerCapacity_kW = 10;
		gasBurner = new J_EAConversionGasBurner(house, OL_EnergyAssetType.GAS_BURNER, gasBurnerCapacity_kW, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
	}
	// Else house has a customprofiel
	else {
		J_EAProfile heatDemandProfile = (J_EAProfile)findFirst(house.c_profileAssets, x->x instanceof J_EAProfile && x.energyCarrierConsumed == OL_EnergyCarrierType.HEAT);
		double peakHeatDemand_kW = Arrays.stream(heatDemandProfile.a_energyProfile_kWh).max().orElseThrow(() -> new RuntimeException());
		gasBurner = new J_EAConversionGasBurner(house, OL_EnergyAssetType.GAS_BURNER, peakHeatDemand_kW, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
	}	
	
	
}
/*ALCODEEND*/}

double f_setAircos(double desiredShare)
{/*ALCODESTART::1749739532217*/
double nbHousesWithAirco = count(zero_Interface.energyModel.Houses, x -> x.p_airco != null);
double nbHouses = zero_Interface.energyModel.Houses.size();
traceln("Previous nb households with airco: " + nbHousesWithAirco);
while ( roundToInt(nbHouses * desiredShare) > nbHousesWithAirco ) {
	GCHouse house = randomWhere(zero_Interface.energyModel.Houses, x -> x.p_airco == null);
	double aircoPower_kW = roundToDecimal(uniform(3,6),1);
	house.p_airco = new J_EAAirco(house, aircoPower_kW, zero_Interface.energyModel.p_timeStep_h);
	house.c_electricHeatpumpAssets.add(house.p_airco);
	nbHousesWithAirco ++;
}
while ( roundToInt(nbHouses * desiredShare) < nbHousesWithAirco ) {
	GCHouse house = randomWhere(zero_Interface.energyModel.Houses, x -> x.p_airco != null);
	house.c_electricHeatpumpAssets.remove(house.p_airco);
	house.p_airco = null;
	nbHousesWithAirco --;
}

traceln("New nb households with airco: " + nbHousesWithAirco);
/*ALCODEEND*/}

double f_addLTDH()
{/*ALCODESTART::1749739532231*/
for (GCHouse house: zero_Interface.energyModel.Houses ) {
	house.p_primaryHeatingAsset.removeEnergyAsset();
	if (house.p_secondaryHeatingAsset != null) {
		house.p_secondaryHeatingAsset.removeEnergyAsset(); 
	}
	if (house.p_tertiaryHeatingAsset != null) {
		house.p_tertiaryHeatingAsset.removeEnergyAsset(); 
	}
	if ( house.p_heatBuffer != null){
		house.p_heatBuffer.removeEnergyAsset();
	}
	house.p_heatingType = OL_GridConnectionHeatingType.HEATPUMP_AIR;
	traceln( "adding heatpump");
	J_EAConversionHeatPump heatPump;
	J_EAConsumption heatDemandAsset = findFirst(house.c_consumptionAssets, j_ea -> j_ea.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND);
	double heatpumpElectricCapacity_kW = 1.0;
	house.p_heatBuffer = new J_EAStorageHeat(house, OL_EAStorageTypes.HEATBUFFER, 10, 0, zero_Interface.energyModel.p_timeStep_h, 60, 20, 80, 30, 50_000, "AIR" );
	if (heatDemandAsset != null) { // als house een standaard warmtebehoefte profiel heeft
		heatPump = new J_EAConversionHeatPump(house, zero_Interface.energyModel.p_timeStep_h, heatDemandAsset.yearlyDemandHeat_kWh/8760*10 / 3, 0.5, zero_Interface.energyModel.v_currentAmbientTemperature_degC, 60, "AIR", 0, 1);
	} 
	else if (house.p_BuildingThermalAsset != null){ //als huis een building heatmodel heeft
		heatPump = new J_EAConversionHeatPump(house, zero_Interface.energyModel.p_timeStep_h, heatpumpElectricCapacity_kW, 1, zero_Interface.energyModel.v_currentAmbientTemperature_degC, 60, "AIR", 0, 1);
		heatPump.setEnergyAssetName("Heatpump " + heatpumpElectricCapacity_kW );
		heatPump.setEnergyAssetType( OL_EnergyAssetType.HEAT_PUMP_AIR);
	}
	else { //anders moet het huis een heatProfiel krijgen
		J_EAProfile heatDemandProfile = (J_EAProfile)findFirst(house.c_profileAssets, x->x instanceof J_EAProfile && x.energyCarrierConsumed == OL_EnergyCarrierType.HEAT);
		double peakHeatDemand_kW = Arrays.stream(heatDemandProfile.a_energyProfile_kWh).max().orElseThrow(() -> new RuntimeException());
		heatPump = new J_EAConversionHeatPump(house, zero_Interface.energyModel.p_timeStep_h, peakHeatDemand_kW / 3, 0.5, zero_Interface.energyModel.v_currentAmbientTemperature_degC, 60, "AIR", 0, 1);
	}
	
}




/*ALCODEEND*/}

double f_removeLTDH()
{/*ALCODESTART::1749739532244*/
for (GCHouse house: zero_Interface.energyModel.Houses ) {
	house.p_primaryHeatingAsset.removeEnergyAsset();
	house.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;
	house.v_districtHeatDelivery_kW = 0;
	
	//add gasburner
	house.p_heatingType = OL_GridConnectionHeatingType.GASBURNER;	

	J_EAConsumption heatDemandAsset = findFirst(house.c_consumptionAssets, j_ea -> j_ea.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND);
	J_EAConversionGasBurner gasBurner;
	//if house has follows the general heat deamnd profile
	if (heatDemandAsset != null) {
		gasBurner = new J_EAConversionGasBurner(house, OL_EnergyAssetType.GAS_BURNER, heatDemandAsset.yearlyDemandHeat_kWh/8760*10, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
	}
	//if house has a thermalBuildingAsset
	else if (house.p_BuildingThermalAsset != null){
		double gasBurnerCapacity_kW = 10;
		gasBurner = new J_EAConversionGasBurner(house, OL_EnergyAssetType.GAS_BURNER, gasBurnerCapacity_kW, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
	}
	// Else house has a customprofiel
	else {
		J_EAProfile heatDemandProfile = (J_EAProfile)findFirst(house.c_profileAssets, x->x instanceof J_EAProfile && x.energyCarrierConsumed == OL_EnergyCarrierType.HEAT);
		double peakHeatDemand_kW = Arrays.stream(heatDemandProfile.a_energyProfile_kWh).max().orElseThrow(() -> new RuntimeException());
		gasBurner = new J_EAConversionGasBurner(house, OL_EnergyAssetType.GAS_BURNER, peakHeatDemand_kW, 0.99, zero_Interface.energyModel.p_timeStep_h, 90);
	}	
	
	
}
/*ALCODEEND*/}

