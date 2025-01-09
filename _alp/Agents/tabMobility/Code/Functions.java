double f_setElectricTrucks(double pctElectricTrucksGoal,ShapeSlider sliderElectricTrucks,ShapeSlider sliderFFTrucks)
{/*ALCODESTART::1722256088432*/
//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

//Get totals
int nbEtruckCurrent = count(zero_Interface.energyModel.f_getEnergyAssets(), p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK && !(p.getParentAgent() instanceof GCPublicCharger) && ((GridConnection)p.getParentAgent()).v_isActive) + v_totalNumberOfGhostVehicle_Trucks;
int nbDieseltrucksCurrent = count(zero_Interface.energyModel.f_getEnergyAssets(), p->p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive);
int nbElectricTrucksGoal = roundToInt((nbEtruckCurrent + nbDieseltrucksCurrent)*pctElectricTrucksGoal/100.0);
//traceln("%s diesel trucks and %s eTrucks found", nbDieseltrucksCurrent, nbEtruckCurrent);

//Remember how much vehicles there are initially
int total_vehicles = nbEtruckCurrent + nbDieseltrucksCurrent;
//traceln(total_vehicles);

if( nbEtruckCurrent > nbElectricTrucksGoal){
	while ( nbEtruckCurrent > nbElectricTrucksGoal && nbEtruckCurrent > 0) { // remove excess EVs systems !!!! Should also add diesel vehicle again!
		J_EAEV ev = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive);
		if (ev!=null) {
			//traceln("Found eTrucks: " + ev);
			GridConnection gc = (GridConnection)ev.getParentAgent();
			
			// update UI company
			UI_company companyUI = null;
			boolean isAdditionalVehicle = false;
			if (zero_Interface.c_companyUIs.size()>0){
				companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
				if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
					int ghostTrucks = 0;
					if(gc.v_hasQuarterHourlyValues){
						ghostTrucks = companyUI.v_minEVTruckSlider;
					}
					
					int nbGCElectricTrucks = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK) + ghostTrucks;
					int nbGCDieselTrucks = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK);
					if (companyUI.v_minEVTruckSlider >= nbGCElectricTrucks) {
						traceln("Removed already existing Electric Truck from GC: " + companyUI.p_companyName);
					}
					else {
						companyUI.sl_electricTrucksCompany.setValue(nbGCElectricTrucks-1, false);
						companyUI.v_nbEVTrucks--;
						companyUI.sl_dieselTrucksCompany.setValue(nbGCDieselTrucks+1, false);
						companyUI.v_nbDieselTrucks++;
					}
					companyUI.rb_scenariosPrivateUI.setValue(2, false);
					
				}
				if(companyUI != null){
					for(GridConnection GC: companyUI.c_ownedGridConnections){
						if(companyUI.c_additionalVehicles.get(GC).contains(ev)){
							companyUI.c_additionalVehicles.get(GC).remove(ev);
							isAdditionalVehicle = true;
						}
					}
				}
			}
			
			J_ActivityTrackerTrips tripTracker = ev.tripTracker;
			boolean available = true;
			available = ev.getAvailability();
			zero_Interface.c_orderedVehicles.remove(ev);
			ev.removeEnergyAsset();
			//traceln("Removing EV from gridConnection:" + GC.p_gridConnectionID);
		
			// Re-add diesel vehicle
			double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionTruck_kWhpkm;
			double vehicleScaling = 1.0;
			J_EADieselVehicle dieselVehicle = new J_EADieselVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, vehicleScaling, OL_EnergyAssetType.DIESEL_TRUCK, tripTracker);
			dieselVehicle.available = available;
			
			zero_Interface.c_orderedVehicles.add(0, dieselVehicle);
			
			//check if was additional vehicle in companyUI, if so: add to collection
			if(companyUI != null && isAdditionalVehicle){
				companyUI.c_additionalVehicles.get(gc).add(dieselVehicle);
			}
			
			nbEtruckCurrent--; 
			nbDieseltrucksCurrent++;
		}
		else{//No more vehicles to adjust: this is the minimum: set slider to minimum and do nothing else
				int total_electricTrucks = count(zero_Interface.energyModel.f_getEnergyAssets(), p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK && !(p.getParentAgent() instanceof GCPublicCharger)  && ((GridConnection)p.getParentAgent()).v_isActive) + v_totalNumberOfGhostVehicle_Trucks;
				int min_pct_electricTruckSlider = roundToInt(100.0*total_electricTrucks/total_vehicles);
				sliderElectricTrucks.setValue(min_pct_electricTruckSlider, false);
				sliderFFTrucks.setValue(100-min_pct_electricTruckSlider, false);
			return;
		}
	} 
} else { 
	while ( nbEtruckCurrent < nbElectricTrucksGoal && nbDieseltrucksCurrent > 0) {
	
		
		// Remove diesel vehicle
		J_EADieselVehicle dieselVehicle = (J_EADieselVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive);
		//traceln("Found diesel vehicle: " + dieselVehicle);
		if (dieselVehicle!=null) {
			GridConnection gc = (GridConnection)dieselVehicle.getParentAgent();
			
			
			// update UI company
			UI_company companyUI = null;
			boolean isAdditionalVehicle = false;
			if (zero_Interface.c_companyUIs.size()>0){
				companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
				if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
					int ghostTrucks = 0;
					if(gc.v_hasQuarterHourlyValues){
						ghostTrucks = companyUI.v_minEVTruckSlider;
					}
					int nbGCElectricTrucks = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK) + ghostTrucks;
					int nbGCDieselTrucks = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK);
					companyUI.sl_electricTrucksCompany.setValue(nbGCElectricTrucks+1, false);
					companyUI.v_nbEVTrucks++;
					companyUI.sl_dieselTrucksCompany.setValue(nbGCDieselTrucks-1, false);
					companyUI.v_nbDieselTrucks--;
					companyUI.rb_scenariosPrivateUI.setValue(2, false);
				}
				if(companyUI != null){
					for(GridConnection GC: companyUI.c_ownedGridConnections){
						if(companyUI.c_additionalVehicles.get(GC).contains(dieselVehicle)){
							companyUI.c_additionalVehicles.get(GC).remove(dieselVehicle);
							isAdditionalVehicle = true;
						}
					}
				}
			}
			
			J_ActivityTrackerTrips tripTracker = dieselVehicle.tripTracker;
			boolean available = true;
			available = dieselVehicle.getAvailability();
			zero_Interface.c_orderedVehicles.remove(dieselVehicle);
			dieselVehicle.removeEnergyAsset();
			//traceln("Removing household DIESEL VEHICLE from household:" + GC.p_gridConnectionID);
	
			double capacityElectric_kW = zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerTruck_kW;
			double storageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgEVStorageTruck_kWh;
			double initialStateOfCharge_r = 1.0;
			double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionTruck_kWhpkm;
			double vehicleScalingElectric = 1.0;
			J_EAEV ev = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_r, zero_Interface.energyModel.p_timeStep_h, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_TRUCK, tripTracker);  
			ev.available = available;
			
			zero_Interface.c_orderedVehicles.add(0, ev);	
			
			//check if was additional vehicle in companyUI, if so: add to collection
			if(companyUI != null && isAdditionalVehicle){
				companyUI.c_additionalVehicles.get(gc).add(ev);
			}
			
			nbEtruckCurrent++; 
			nbDieseltrucksCurrent--;
		}
	}
	
}	

//traceln("%s diesel trucks and %s eTrucks: ", nbDieseltrucksCurrent, nbEtruckCurrent);
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setVehicleSliders(int sliderIndex,ShapeSlider electricSlider,ShapeSlider hydrogenSlider,ShapeSlider fossilFuelSlider)
{/*ALCODESTART::1722256088443*/
double pct_electric = electricSlider.getValue();
double pct_fossilFuel = fossilFuelSlider.getValue();
double pct_hydrogen = 0;

if (hydrogenSlider != null) {
	pct_hydrogen = hydrogenSlider.getValue();
}

//Set array with pct values
double pctArray[]={pct_electric, pct_fossilFuel, pct_hydrogen};
double pctExcess = Arrays.stream(pctArray).sum() - 100;
for (int i = 0; i<pctArray.length; i++){
	if (!(i==(int)sliderIndex)) {
		pctArray[i] = max(0,pctArray[i] - pctExcess);
		pctExcess = Arrays.stream(pctArray).sum() - 100;
	}
}
if (pctExcess != 0) {
	traceln("Sliders don't add up to 100%!");
}

//Set Sliders
electricSlider.setValue(pctArray[0], false);
fossilFuelSlider.setValue(pctArray[1], false);
if (hydrogenSlider != null) {
	hydrogenSlider.setValue(pctArray[2], false);
}
/*ALCODEEND*/}

double f_setElectricVans(double pctElectricVansGoal,ShapeSlider sliderElectricVans,ShapeSlider sliderFFVans)
{/*ALCODESTART::1722256088451*/
//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

int nbEVansCurrent = count(zero_Interface.energyModel.f_getEnergyAssets(), p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN && !(p.getParentAgent() instanceof GCPublicCharger) && ((GridConnection)p.getParentAgent()).v_isActive) + v_totalNumberOfGhostVehicle_Vans;
int nbDieselVansCurrent = count(zero_Interface.energyModel.f_getEnergyAssets(), p->p.energyAssetType == OL_EnergyAssetType.DIESEL_VAN && ((GridConnection)p.getParentAgent()).v_isActive);
int nbElectricVansGoal = roundToInt((nbEVansCurrent + nbDieselVansCurrent)*pctElectricVansGoal/100.0);

//Remember how much vehicles there are initially
int total_vehicles = nbEVansCurrent + nbDieselVansCurrent;


if( nbEVansCurrent > nbElectricVansGoal){
	while ( nbEVansCurrent > nbElectricVansGoal && nbEVansCurrent > 0) { // remove excess EVs systems !!!! Should also add diesel vehicle again!
		J_EAEV ev = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN && ((GridConnection)p.getParentAgent()).v_isActive);
		if (ev!=null) {
			//traceln("Found eTrucks: " + ev);
			GridConnection gc = (GridConnection)ev.getParentAgent();
			
			// update UI company
			UI_company companyUI = null;
			boolean isAdditionalVehicle = false;
			if (zero_Interface.c_companyUIs.size()>0){
				companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
				if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
					int ghostVans = 0;
					if(gc.v_hasQuarterHourlyValues){
						ghostVans = companyUI.v_minEVVanSlider;
					}
					
					int nbGCElectricVans = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN) + ghostVans;
					int nbGCDieselVans = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_VAN);
					if (companyUI.v_minEVVanSlider >= nbGCElectricVans) {
						traceln("Removed already existing Electric Van from GC: " + companyUI.p_companyName);
					}
					else {
						companyUI.sl_electricVansCompany.setValue(nbGCElectricVans-1, false);
						companyUI.v_nbEVVans--;
						companyUI.sl_dieselVansCompany.setValue(nbGCDieselVans+1, false);
						companyUI.v_nbDieselVans++;
					}
					companyUI.rb_scenariosPrivateUI.setValue(2, false);
				}
				if(companyUI != null){
					for(GridConnection GC: companyUI.c_ownedGridConnections){
						if(companyUI.c_additionalVehicles.get(GC).contains(ev)){
							companyUI.c_additionalVehicles.get(GC).remove(ev);
							isAdditionalVehicle = true;
						}
					}
				}
			}
			
			J_ActivityTrackerTrips tripTracker = ev.tripTracker;
			boolean available = true;
			available = ev.getAvailability();
			zero_Interface.c_orderedVehicles.remove(ev);
			ev.removeEnergyAsset();
			//traceln("Removing EV from gridConnection:" + GC.p_gridConnectionID);
		
			// Re-add diesel vehicle
			double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionVan_kWhpkm;
			double vehicleScaling = 1.0;
			J_EADieselVehicle dieselVehicle = new J_EADieselVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, vehicleScaling, OL_EnergyAssetType.DIESEL_VAN, tripTracker);
			dieselVehicle.available = available;
			
			zero_Interface.c_orderedVehicles.add(0, dieselVehicle);

			//check if was additional vehicle in companyUI, if so: add to collection
			if(companyUI != null && isAdditionalVehicle){
				companyUI.c_additionalVehicles.get(gc).add(dieselVehicle);
			}
			
			nbEVansCurrent--;
			nbDieselVansCurrent++;
		}
		else{//No more vehicles to adjust: this is the minimum: set slider to minimum and do nothing else
			int total_electricVans = count(zero_Interface.energyModel.f_getEnergyAssets(), p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN && !(p.getParentAgent() instanceof GCPublicCharger)  && ((GridConnection)p.getParentAgent()).v_isActive) + v_totalNumberOfGhostVehicle_Vans;
			int min_pct_electricVanSlider = roundToInt(100.0*total_electricVans/total_vehicles);
			sliderElectricVans.setValue(min_pct_electricVanSlider, false);
			sliderFFVans.setValue(100-min_pct_electricVanSlider, false);
			return;
		}
	} 
} else { 
	while ( nbEVansCurrent < nbElectricVansGoal && nbDieselVansCurrent > 0) {

		// Remove diesel vehicle
		J_EADieselVehicle dieselVehicle = (J_EADieselVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_VAN && ((GridConnection)p.getParentAgent()).v_isActive);
		//traceln("Found diesel vehicle: " + dieselVehicle);
		if (dieselVehicle!=null) {
			GridConnection gc = (GridConnection)dieselVehicle.getParentAgent();
			
			
			// update UI company
			UI_company companyUI = null;
			boolean isAdditionalVehicle = false;
			if (zero_Interface.c_companyUIs.size()>0){
				companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
				if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
					int ghostVans = 0;
					if(gc.v_hasQuarterHourlyValues){
						ghostVans = companyUI.v_minEVVanSlider;
					}
					
					int nbGCElectricVans = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN) + ghostVans;
					int nbGCDieselVans = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_VAN);
					companyUI.sl_electricVansCompany.setValue(nbGCElectricVans+1, false);	
					companyUI.v_nbEVVans++;
					companyUI.sl_dieselVansCompany.setValue(nbGCDieselVans-1, false);
					companyUI.v_nbDieselVans--;
					companyUI.rb_scenariosPrivateUI.setValue(2, false);
				}
				if(companyUI != null){
					for(GridConnection GC: companyUI.c_ownedGridConnections){
						if(companyUI.c_additionalVehicles.get(GC).contains(dieselVehicle)){
							companyUI.c_additionalVehicles.get(GC).remove(dieselVehicle);
							isAdditionalVehicle = true;
						}
					}
				}
			}
			
			J_ActivityTrackerTrips tripTracker = dieselVehicle.tripTracker;
			boolean available = true;
			available = dieselVehicle.getAvailability();
			zero_Interface.c_orderedVehicles.remove(dieselVehicle);
			dieselVehicle.removeEnergyAsset();
			//traceln("Removing household DIESEL VEHICLE from household:" + GC.p_gridConnectionID);
			
			double capacityElectric_kW = zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerVan_kW;
			double storageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgEVStorageVan_kWh;
			double initialStateOfCharge_r = 1.0;
			double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionVan_kWhpkm;
			double vehicleScalingElectric = 1.0;
			J_EAEV ev = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_r, zero_Interface.energyModel.p_timeStep_h, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_VAN, tripTracker);  

			ev.available = available;
			
			zero_Interface.c_orderedVehicles.add(0, ev);	
			
			//check if was additional vehicle in companyUI, if so: add to collection
			if(companyUI != null && isAdditionalVehicle){
				companyUI.c_additionalVehicles.get(gc).add(ev);
			}
			
			nbEVansCurrent++;
			nbDieselVansCurrent--;
		}
	}
	
}	

//traceln("%s diesel vans and %s EVans: ", nbDieselVansCurrent, nbEVansCurrent);
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setElectricCars(double pctEVsGoal,ShapeSlider sliderElectricCars,ShapeSlider sliderFFCars)
{/*ALCODESTART::1722256088460*/
//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

int nbEVsCurrent = count(zero_Interface.energyModel.f_getEnergyAssets(), p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VEHICLE && !(p.getParentAgent() instanceof GCPublicCharger) && ((GridConnection)p.getParentAgent()).v_isActive) + v_totalNumberOfGhostVehicle_Cars;
int nbDieselCarsCurrent = count(zero_Interface.energyModel.f_getEnergyAssets(), p->p.energyAssetType == OL_EnergyAssetType.DIESEL_VEHICLE && ((GridConnection)p.getParentAgent()).v_isActive);
int nbEVsGoal = roundToInt((nbEVsCurrent + nbDieselCarsCurrent)*pctEVsGoal/100.0);

//Remember how much vehicles there are initially
int total_vehicles = nbEVsCurrent + nbDieselCarsCurrent;

if( nbEVsCurrent > nbEVsGoal){
	while ( nbEVsCurrent > nbEVsGoal && nbEVsCurrent > 0) { // remove excess EVs systems !!!! Should also add diesel vehicle again!
		J_EAEV ev = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VEHICLE && ((GridConnection)p.getParentAgent()).v_isActive);
		if (ev!=null) {
			//traceln("Found eTrucks: " + ev);
			GridConnection gc = (GridConnection)ev.getParentAgent();
			
			// update UI company
			UI_company companyUI = null;
			boolean isAdditionalVehicle = false;
			if (zero_Interface.c_companyUIs.size()>0){
				companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
				if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
					
					int ghostCars = 0;
					if(gc.v_hasQuarterHourlyValues){
						ghostCars = companyUI.v_minEVCarSlider;
					}
					
					int nbGCEVs = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VEHICLE) + ghostCars;
					int nbGCDieselCars = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_VEHICLE);
					if (companyUI.v_minEVCarSlider >= nbGCEVs) {
						traceln("Removed already existing Electric Car from GC: " + companyUI.p_companyName);
					}
					else {
						companyUI.sl_electricCarsCompany.setValue(nbGCEVs-1, false);
						companyUI.v_nbEVCars--;
						companyUI.sl_dieselCarsCompany.setValue(nbGCDieselCars+1, false);
						companyUI.v_nbDieselCars++;
					}
					companyUI.rb_scenariosPrivateUI.setValue(2, false);
				}
				if(companyUI != null){
					for(GridConnection GC: companyUI.c_ownedGridConnections){
						if(companyUI.c_additionalVehicles.get(GC).contains(ev)){
							companyUI.c_additionalVehicles.get(GC).remove(ev);
							isAdditionalVehicle = true;		
						}
					}
				}
			}
		
			J_ActivityTrackerTrips tripTracker = ev.tripTracker;
			boolean available = true;
			available = ev.getAvailability();
			zero_Interface.c_orderedVehicles.remove(ev);
			ev.removeEnergyAsset();
			//traceln("Removing EV from gridConnection:" + GC.p_gridConnectionID);
		
			// Re-add diesel vehicle
			double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionCar_kWhpkm;
			double vehicleScaling = 1.0;	
			J_EADieselVehicle dieselVehicle = new J_EADieselVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, vehicleScaling, OL_EnergyAssetType.DIESEL_VEHICLE, tripTracker);
			dieselVehicle.available = available;
			
			zero_Interface.c_orderedVehicles.add(0, dieselVehicle);	
				
			//check if was additional vehicle in companyUI, if so: add to collection
			if(companyUI != null && isAdditionalVehicle){
				companyUI.c_additionalVehicles.get(gc).add(dieselVehicle);
			}
			
			nbEVsCurrent--;
			nbDieselCarsCurrent++;
		}
		else{//No more vehicles to adjust: this is the minimum: set slider to minimum and do nothing else
				int total_electricCars = count(zero_Interface.energyModel.f_getEnergyAssets(), p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VEHICLE && !(p.getParentAgent() instanceof GCPublicCharger)  && ((GridConnection)p.getParentAgent()).v_isActive) + v_totalNumberOfGhostVehicle_Cars;
				int min_pct_electricCarSlider = roundToInt(100.0*total_electricCars/total_vehicles);
				sliderElectricCars.setValue(min_pct_electricCarSlider, false);
				sliderFFCars.setValue(100-min_pct_electricCarSlider, false);
			return;
		}
	} 
} else { 
	while ( nbEVsCurrent < nbEVsGoal && nbDieselCarsCurrent > 0) {
		// Remove diesel vehicle
		J_EADieselVehicle dieselVehicle = (J_EADieselVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_VEHICLE && ((GridConnection)p.getParentAgent()).v_isActive);
		//traceln("Found diesel vehicle: " + dieselVehicle);
		if (dieselVehicle!=null) {
			GridConnection gc = (GridConnection)dieselVehicle.getParentAgent();
			
			
			// update UI company
			UI_company companyUI = null;
			boolean isAdditionalVehicle = false;
			if (zero_Interface.c_companyUIs.size()>0){
				companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
				if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
					
					int ghostCars = 0;
					if(gc.v_hasQuarterHourlyValues){
						ghostCars = companyUI.v_minEVCarSlider;
					}
					
					int nbGCEVs = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VEHICLE) + ghostCars;
					int nbGCDieselCars = count(gc.c_vehicleAssets, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_VEHICLE);
					companyUI.sl_electricCarsCompany.setValue(nbGCEVs+1, false);
					companyUI.v_nbEVCars++;
					companyUI.sl_dieselCarsCompany.setValue(nbGCDieselCars-1, false);
					companyUI.v_nbDieselCars--;
					companyUI.rb_scenariosPrivateUI.setValue(2, false);
				}
				if(companyUI != null){
					for(GridConnection GC: companyUI.c_ownedGridConnections){
						if(companyUI.c_additionalVehicles.get(GC).contains(dieselVehicle)){
							companyUI.c_additionalVehicles.get(GC).remove(dieselVehicle);
							isAdditionalVehicle = true;
						}
					}
				}
			}
			
			J_ActivityTrackerTrips tripTracker = dieselVehicle.tripTracker;
			boolean available = true;
			available = dieselVehicle.getAvailability();
			zero_Interface.c_orderedVehicles.remove(dieselVehicle);
			dieselVehicle.removeEnergyAsset();
			//traceln("Removing household DIESEL VEHICLE from household:" + GC.p_gridConnectionID);

			double capacityElectric_kW = zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerCar_kW;
			double storageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgEVStorageCar_kWh;
			double initialStateOfCharge_r = 1.0;
			double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionCar_kWhpkm;
			double vehicleScalingElectric = 1.0;
			J_EAEV ev = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_r, zero_Interface.energyModel.p_timeStep_h, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_VEHICLE, tripTracker);  
			ev.available = available;
			
			zero_Interface.c_orderedVehicles.add(0, ev);	

			//check if was additional vehicle in companyUI, if so: add to collection
			if(companyUI != null && isAdditionalVehicle){
				companyUI.c_additionalVehicles.get(gc).add(ev);
			}
			
			nbEVsCurrent++;
			nbDieselCarsCurrent--;
		}
	}
	
}	

//traceln("%s diesel cars and %s EVs: ", nbDieselCarsCurrent, nbEVsCurrent);
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setDemandReduction(double demandReduction_pct)
{/*ALCODESTART::1722342449665*/
// TODO: when new triptrackers are created, for example in the company ui sliders,
// make sure they have this distance scaling fraction!

double mobilityDemandReduction_pct = demandReduction_pct;

for (J_EA j_ea : zero_Interface.energyModel.f_getEnergyAssets()) {
	if (j_ea instanceof J_EAVehicle) {
		((J_EAVehicle)j_ea).getTripTracker().distanceScaling_fr =  1 - mobilityDemandReduction_pct/100.0;
		if (zero_Interface.c_companyUIs.size()>0){
			UI_company companyUI = zero_Interface.c_companyUIs.get(((GridConnection)j_ea.getParentAgent()).p_owner.p_connectionOwnerIndexNr);
			if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == j_ea.getParentAgent()) { // should also check the setting of selected GC
				companyUI.sl_mobilityDemandCompanyReduction.setValue(mobilityDemandReduction_pct, false);
			}
		}	
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_calculateNumberOfGhostVehicles()
{/*ALCODESTART::1729168033110*/
/*
v_totalNumberOfGhostVehicle_Cars = 0;
v_totalNumberOfGhostVehicle_Vans = 0;
v_totalNumberOfGhostVehicle_Trucks = 0;

for (UI_company companyUI : zero_Interface.c_companyUIs ){
	
	v_totalNumberOfGhostVehicle_Cars += companyUI.v_minEVCarSlider;
	v_totalNumberOfGhostVehicle_Vans += companyUI.v_minEVVanSlider;
	v_totalNumberOfGhostVehicle_Trucks += companyUI.v_minEVTruckSlider;
}
traceln("Ghost vehicles:");
traceln(v_totalNumberOfGhostVehicle_Cars);
traceln(v_totalNumberOfGhostVehicle_Vans);
traceln(v_totalNumberOfGhostVehicle_Trucks);
*/

v_totalNumberOfGhostVehicle_Cars = 0;
v_totalNumberOfGhostVehicle_Vans = 0;
v_totalNumberOfGhostVehicle_Trucks = 0;
for (UI_company companyUI : zero_Interface.c_companyUIs ){
	
	for(int i = 0; i < companyUI.c_ownedGridConnections.size(); i++){
		if(companyUI.c_ownedGridConnections.get(i).v_hasQuarterHourlyValues && companyUI.c_ownedGridConnections.get(i).v_isActive){
			v_totalNumberOfGhostVehicle_Cars += companyUI.v_minEVCarSlider;
			v_totalNumberOfGhostVehicle_Vans += companyUI.v_minEVVanSlider;
			v_totalNumberOfGhostVehicle_Trucks += companyUI.v_minEVTruckSlider;
		}
	}
}

//traceln(v_totalNumberOfGhostVehicle_Cars);
//traceln(v_totalNumberOfGhostVehicle_Vans);
//traceln(v_totalNumberOfGhostVehicle_Trucks);

/*ALCODEEND*/}

