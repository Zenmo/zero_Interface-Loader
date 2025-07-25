double f_setVehicleSliders(int sliderIndex,ShapeSlider electricSlider,ShapeSlider hydrogenSlider,ShapeSlider fossilFuelSlider)
{/*ALCODESTART::1722256088443*/
double pct_electric = electricSlider.getValue();
double pct_hydrogen = 0;
double pct_fossilFuel = fossilFuelSlider.getValue();
if (hydrogenSlider != null) {
	pct_hydrogen = hydrogenSlider.getValue();
}
 
 
//Set array with pct values
double pctArray[]={pct_electric, pct_hydrogen, pct_fossilFuel};
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
if (hydrogenSlider != null) {
	hydrogenSlider.setValue(pctArray[1], false);
}
fossilFuelSlider.setValue(pctArray[2], false);

/*
double pct_fossilFuel = fossilFuelSlider.getValue();
double pct_electric = electricSlider.getValue();
double pct_hydrogen = 0;
if (hydrogenSlider != null) {
	pct_hydrogen = hydrogenSlider.getValue();
}
 
 
//Set array with pct values
double pctArray[]={pct_fossilFuel, pct_electric, pct_hydrogen};
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
fossilFuelSlider.setValue(pctArray[0], false);
electricSlider.setValue(pctArray[1], false);
if (hydrogenSlider != null) {
	hydrogenSlider.setValue(pctArray[2], false);
}
*/
/*ALCODEEND*/}

double f_setDemandReduction(List<GridConnection> gcList,double demandReduction_pct)
{/*ALCODESTART::1722342449665*/
// TODO: when new triptrackers are created, for example in the company ui sliders,
// make sure they have this distance scaling fraction!

double scalingFactor = 1 - demandReduction_pct/100;

for (GridConnection gc : gcList) {
	for (J_EAVehicle j_ea : gc.c_vehicleAssets) {
		j_ea.getTripTracker().distanceScaling_fr = scalingFactor;
	}
	if (zero_Interface.c_companyUIs.size() > 0) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null) {
			//if(companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) != gc){
				//int i = indexOf(companyUI.c_ownedGridConnections.stream().toArray(), gc);
				//companyUI.GCnr_selection.setValue(i, true);
			//}
			companyUI.sl_mobilityDemandCompanyReduction.setValue(demandReduction_pct, false);			
		}
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

Triple<Integer, Integer, Integer> f_calculateNumberOfGhostVehicles(List<GridConnection> gcList)
{/*ALCODESTART::1729168033110*/
Integer numberOfGhostVehicle_Cars = 0;
Integer numberOfGhostVehicle_Vans = 0;
Integer numberOfGhostVehicle_Trucks = 0;

if (zero_Interface.c_companyUIs.size() > 0) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		for(int i = 0; i < companyUI.c_ownedGridConnections.size(); i++){
			if(companyUI.c_ownedGridConnections.get(i).v_hasQuarterHourlyValues && companyUI.c_ownedGridConnections.get(i).v_isActive){
				numberOfGhostVehicle_Cars += companyUI.v_minEVCarSlider;
				numberOfGhostVehicle_Vans += companyUI.v_minEVVanSlider;
				numberOfGhostVehicle_Trucks += companyUI.v_minEVTruckSlider;
			}
		}
	}
}

return new Triple(numberOfGhostVehicle_Cars, numberOfGhostVehicle_Vans, numberOfGhostVehicle_Trucks);
/*ALCODEEND*/}

double f_setDieselTrucks(List<GridConnection> gcList,ShapeSlider sliderElectricTrucks,ShapeSlider sliderHydrogenTrucks,ShapeSlider sliderFossilFuelTrucks)
{/*ALCODESTART::1746008458894*/
double pctDieselTrucksGoal = sliderFossilFuelTrucks.getValue();

int numberOfGhostVehicle_Trucks = f_calculateNumberOfGhostVehicles(gcList).getThird();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfTrucks( gcList, numberOfGhostVehicle_Trucks );

int nbEtruckCurrent = triple.getFirst();
int nbHydrogentrucksCurrent = triple.getSecond();
int nbDieseltrucksCurrent = triple.getThird();
int total_vehicles = nbEtruckCurrent + nbHydrogentrucksCurrent + nbDieseltrucksCurrent;

int nbDieselTrucksGoal = roundToInt(total_vehicles*pctDieselTrucksGoal/100.0);
boolean finishedLookingThroughElectricTrucks = false;

if (nbDieseltrucksCurrent < nbDieselTrucksGoal) {
	// Add Diesel Trucks
	while ( nbDieseltrucksCurrent < nbDieselTrucksGoal ) {
		if ( nbEtruckCurrent > numberOfGhostVehicle_Trucks && !finishedLookingThroughElectricTrucks ) {
			// replace electric truck with a diesel truck
			if (!f_electricToDieselTruck(gcList)) {
				finishedLookingThroughElectricTrucks = true;
			}
			else {
				nbDieseltrucksCurrent++;
				nbEtruckCurrent--;
			}
		}
		else{// ( nbHydrogentrucksCurrent > 0 ) {
			// replace hydrogen truck with diesel truck
			if (!f_hydrogenToDieselTruck(gcList)) {
				f_setTruckSlidersToCurrentEngineState(gcList, sliderElectricTrucks, sliderHydrogenTrucks, sliderFossilFuelTrucks);
				return;
			}
			nbDieseltrucksCurrent++;
			nbHydrogentrucksCurrent--;
		}
		/*
		else {
			throw new RuntimeException("Can not add another diesel vehicle as there are no other vehicles to replace.");
		}
		*/
	}
}
else {
	// Remove Diesel Trucks
	while ( nbDieseltrucksCurrent > nbDieselTrucksGoal ) {
		// replace a diesel truck with an electric truck
		if (!f_dieselToElectricTruck(gcList)) {
			f_setTruckSlidersToCurrentEngineState(gcList, sliderElectricTrucks, sliderHydrogenTrucks, sliderFossilFuelTrucks);
			return;
		}
		nbDieseltrucksCurrent--;
		nbEtruckCurrent++;
	}
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setHydrogenTrucks(List<GridConnection> gcList,ShapeSlider sliderElectricTrucks,ShapeSlider sliderHydrogenTrucks,ShapeSlider sliderFossilFuelTrucks)
{/*ALCODESTART::1746008458907*/
double pctHydrogenTrucksGoal = sliderHydrogenTrucks.getValue();

int numberOfGhostVehicle_Trucks = f_calculateNumberOfGhostVehicles(gcList).getThird();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfTrucks( gcList, numberOfGhostVehicle_Trucks );

int nbEtruckCurrent = triple.getFirst();
int nbHydrogentrucksCurrent = triple.getSecond();
int nbDieseltrucksCurrent = triple.getThird();
int total_vehicles = nbEtruckCurrent + nbHydrogentrucksCurrent + nbDieseltrucksCurrent;

int nbHydrogenTrucksGoal = roundToInt(total_vehicles*pctHydrogenTrucksGoal/100.0);
boolean finishedLookingThroughDieselTrucks = false;

if (nbHydrogentrucksCurrent < nbHydrogenTrucksGoal) {
	// Add Hydrogen Trucks
	while ( nbHydrogentrucksCurrent < nbHydrogenTrucksGoal && !finishedLookingThroughDieselTrucks ) {
		if ( nbDieseltrucksCurrent > 0 ) {
			// replace a diesel truck with a hydrogen truck
			if (!f_dieselToHydrogenTruck(gcList)) {
				finishedLookingThroughDieselTrucks = true;
			}
			else {
				nbHydrogentrucksCurrent++;
				nbDieseltrucksCurrent--;
			}
		}
		else{// ( nbEtruckCurrent > v_totalNumberOfGhostVehicle_Trucks ) {
			// replace an electric truck with a hydrogen truck
			if (!f_electricToHydrogenTruck(gcList)) {
				f_setTruckSlidersToCurrentEngineState(gcList, sliderElectricTrucks, sliderHydrogenTrucks, sliderFossilFuelTrucks);
				return;
			}
			nbHydrogentrucksCurrent++;
			nbEtruckCurrent--;	
		}
		/*else {
			throw new RuntimeException("Can not add another hydrogen vehicle as there are no other vehicles to replace.");
		}*/
	}
}
else {
	// Remove Hydrogen Trucks
	while ( nbHydrogentrucksCurrent > nbHydrogenTrucksGoal ) {
		// replace a hydrogen truck with a diesel truck
		if (!f_hydrogenToDieselTruck(gcList)) {
			f_setTruckSlidersToCurrentEngineState(gcList, sliderElectricTrucks, sliderHydrogenTrucks, sliderFossilFuelTrucks);
			return;
		}
		nbHydrogentrucksCurrent--;
		nbDieseltrucksCurrent++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setElectricTrucks(List<GridConnection> gcList,ShapeSlider sliderElectricTrucks,ShapeSlider sliderHydrogenTrucks,ShapeSlider sliderFossilFuelTrucks)
{/*ALCODESTART::1746008458917*/
double pctElectricTrucksGoal = sliderElectricTrucks.getValue();

int numberOfGhostVehicle_Trucks = f_calculateNumberOfGhostVehicles(gcList).getThird();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfTrucks( gcList, numberOfGhostVehicle_Trucks );

int nbEtruckCurrent = triple.getFirst();
int nbHydrogentrucksCurrent = triple.getSecond();
int nbDieseltrucksCurrent = triple.getThird();
int total_vehicles = nbEtruckCurrent + nbHydrogentrucksCurrent + nbDieseltrucksCurrent;

int nbElectricTrucksGoal = roundToInt(total_vehicles*pctElectricTrucksGoal/100.0);
boolean finishedLookingThroughDieselTrucks = false;

if (nbEtruckCurrent < nbElectricTrucksGoal) {
	// Add Electric Trucks
	while ( nbEtruckCurrent < nbElectricTrucksGoal && !finishedLookingThroughDieselTrucks ) {
		if ( nbDieseltrucksCurrent > 0 ) {
			// replace a diesel truck with an electric truck
			if (!f_dieselToElectricTruck(gcList)) {
				finishedLookingThroughDieselTrucks = true;
			}
			else {
				nbEtruckCurrent++;
				nbDieseltrucksCurrent--;
			}
		}
		else{// if ( nbHydrogentrucksCurrent > 0 ) {
			// replace a hydrogen truck with an electric truck
			if (!f_hydrogenToElectricTruck(gcList)) {
				f_setTruckSlidersToCurrentEngineState(gcList, sliderElectricTrucks, sliderHydrogenTrucks, sliderFossilFuelTrucks);
				return;
			}
			nbEtruckCurrent++;
			nbHydrogentrucksCurrent--;
		}
		/*else {
			throw new RuntimeException("Can not add another hydrogen vehicle as there are no other vehicles to replace.");
		}
		*/
	}
}
else {
	// Remove Electric Trucks
	while ( nbEtruckCurrent > nbElectricTrucksGoal ) {
		// replace an electric truck with a diesel truck
		if (!f_electricToDieselTruck(gcList)) {
			f_setTruckSlidersToCurrentEngineState(gcList, sliderElectricTrucks, sliderHydrogenTrucks, sliderFossilFuelTrucks);
			return;
		}
		nbEtruckCurrent--;
		nbDieseltrucksCurrent++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

boolean f_dieselToElectricTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458938*/
J_EADieselVehicle dieselTruck = null;
boolean foundAdditionalVehicle = false;

// find the diesel truck to remove, search through additional vehicles first
if (zero_Interface.c_companyUIs.size() > 0 ) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		List<J_EAVehicle> totalList = new ArrayList<J_EAVehicle>();
		for (List<J_EAVehicle> list : companyUI.c_additionalVehicles.values() ) {
			totalList.addAll(list);
		}
		dieselTruck = (J_EADieselVehicle)findFirst(totalList, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK && gc.v_isActive);
		if ( dieselTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}
// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {	
	dieselTruck = (J_EADieselVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive && gcList.contains((GridConnection)p.getParentAgent()));
}
if (dieselTruck!=null) {
	GridConnection gc = (GridConnection)dieselTruck.getParentAgent();
	
	// update UI company
	UI_company companyUI = null;
	if (zero_Interface.c_companyUIs.size() > 0){
		companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC						
			companyUI.v_nbDieselTrucks--;
			companyUI.sl_dieselTrucksCompany.setValue(companyUI.v_nbDieselTrucks, false);
			companyUI.v_nbEVTrucks++;
			companyUI.sl_electricTrucksCompany.setValue(companyUI.v_nbEVTrucks, false);
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
			if (foundAdditionalVehicle) {
				companyUI.c_additionalVehicles.get(gc).remove(dieselTruck);
			}
		}
	}
		
	J_ActivityTrackerTrips tripTracker = dieselTruck.tripTracker;
	boolean available = dieselTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(dieselTruck);
	dieselTruck.removeEnergyAsset();

	// Re-add Electric vehicle
	double capacityElectric_kW = zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerTruck_kW;
	double storageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgEVStorageTruck_kWh;
	double initialStateOfCharge_fr = 1.0;
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionTruck_kWhpkm;
	double vehicleScalingElectric = 1.0;
	J_EAEV electricTruck = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_fr, zero_Interface.energyModel.p_timeStep_h, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_TRUCK, tripTracker);  
	electricTruck.available = available;
	
	zero_Interface.c_orderedVehicles.add(0, electricTruck);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(companyUI != null && foundAdditionalVehicle){
		companyUI.c_additionalVehicles.get(gc).add(electricTruck);
	}
}

else {
	return false;
	//throw new RuntimeException("Numbers suggest there is a diesel truck left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_hydrogenToElectricTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458950*/
J_EAHydrogenVehicle hydrogenTruck = null;
boolean foundAdditionalVehicle = false;

// find the hydrogen truck to remove, search through additional vehicles first
if (zero_Interface.c_companyUIs.size() > 0 ) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		List<J_EAVehicle> totalList = new ArrayList<J_EAVehicle>();
		for (List<J_EAVehicle> list : companyUI.c_additionalVehicles.values() ) {
			totalList.addAll(list);
		}
		hydrogenTruck = (J_EAHydrogenVehicle)findFirst(totalList, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive);
		if ( hydrogenTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}
// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	hydrogenTruck = (J_EAHydrogenVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.HYDROGEN_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive && gcList.contains((GridConnection)p.getParentAgent()));
}
if (hydrogenTruck!=null) {
	GridConnection gc = (GridConnection)hydrogenTruck.getParentAgent();
	
	// update UI company
	UI_company companyUI = null;
	if (zero_Interface.c_companyUIs.size() > 0){
		companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
			companyUI.v_nbHydrogenTrucks--;
			companyUI.sl_hydrogenTrucksCompany.setValue(companyUI.v_nbHydrogenTrucks, false);
			companyUI.v_nbEVTrucks++;
			companyUI.sl_electricTrucksCompany.setValue(companyUI.v_nbEVTrucks, false);
			companyUI.rb_scenariosPrivateUI.setValue(2, false);	
		}
		if(foundAdditionalVehicle){
			companyUI.c_additionalVehicles.get(gc).remove(hydrogenTruck);
		}
	}
	
	J_ActivityTrackerTrips tripTracker = hydrogenTruck.tripTracker;
	boolean available = true;
	available = hydrogenTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(hydrogenTruck);
	hydrogenTruck.removeEnergyAsset();


	// Re-add Electric vehicle
	double capacityElectric_kW = zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerTruck_kW;
	double storageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgEVStorageTruck_kWh;
	double initialStateOfCharge_fr = 1.0;
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionTruck_kWhpkm;
	double vehicleScalingElectric = 1.0;
	J_EAEV electricTruck = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_fr, zero_Interface.energyModel.p_timeStep_h, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_TRUCK, tripTracker);  
	electricTruck.available = available;
	
	zero_Interface.c_orderedVehicles.add(0, electricTruck);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(companyUI != null && foundAdditionalVehicle){
		companyUI.c_additionalVehicles.get(gc).add(electricTruck);
	}
}
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is a hydrogen truck left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_electricToHydrogenTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458960*/
J_EAEV electricTruck = null;
boolean foundAdditionalVehicle = false;

// find the electric truck to remove, search through additional vehicles first
if (zero_Interface.c_companyUIs.size() > 0 ) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		List<J_EAVehicle> totalList = new ArrayList<J_EAVehicle>();
		for (List<J_EAVehicle> list : companyUI.c_additionalVehicles.values() ) {
			totalList.addAll(list);
		}
		electricTruck = (J_EAEV)findFirst(totalList, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive);
		if ( electricTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}
// if no additional vehicle was found, search through the regular ordering of vehicle
if (!foundAdditionalVehicle) {
	electricTruck = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive && gcList.contains((GridConnection)p.getParentAgent()));
}
if (electricTruck!=null) {
	GridConnection gc = (GridConnection)electricTruck.getParentAgent();
	
	// update UI company
	UI_company companyUI = null;
	if (zero_Interface.c_companyUIs.size() > 0){
		companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC						
			companyUI.v_nbEVTrucks--;
			companyUI.sl_electricTrucksCompany.setValue(companyUI.v_nbEVTrucks, false);
			companyUI.v_nbHydrogenTrucks++;
			companyUI.sl_hydrogenTrucksCompany.setValue(companyUI.v_nbHydrogenTrucks, false);
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
			
		}
		if(foundAdditionalVehicle){
			companyUI.c_additionalVehicles.get(gc).remove(electricTruck);
		}
	}
	
	J_ActivityTrackerTrips tripTracker = electricTruck.tripTracker;
	boolean available = true;
	available = electricTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(electricTruck);
	electricTruck.removeEnergyAsset();

	// Re-add hydrogen vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgHydrogenConsumptionTruck_kWhpkm;			
	double vehicleScaling = 1.0;
	J_EAHydrogenVehicle hydrogenVehicle = new J_EAHydrogenVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, vehicleScaling, OL_EnergyAssetType.HYDROGEN_TRUCK, tripTracker);				
	hydrogenVehicle.available = available;
	
	zero_Interface.c_orderedVehicles.add(0, hydrogenVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(companyUI != null && foundAdditionalVehicle){
		companyUI.c_additionalVehicles.get(gc).add(hydrogenVehicle);
	}
}				
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is an electric truck left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_dieselToHydrogenTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458968*/
J_EADieselVehicle dieselTruck = null;
boolean foundAdditionalVehicle = false;

// find the diesel truck to remove, search through additional vehicles first
if (zero_Interface.c_companyUIs.size() > 0 ) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		List<J_EAVehicle> totalList = new ArrayList<J_EAVehicle>();
		for (List<J_EAVehicle> list : companyUI.c_additionalVehicles.values() ) {
			totalList.addAll(list);
		}
		dieselTruck = (J_EADieselVehicle)findFirst(totalList, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive);
		if ( dieselTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}
// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	dieselTruck = (J_EADieselVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive && gcList.contains((GridConnection)p.getParentAgent()));
}
if (dieselTruck!=null) {
	GridConnection gc = (GridConnection)dieselTruck.getParentAgent();
	
	// update UI company
	UI_company companyUI = null;
	if (zero_Interface.c_companyUIs.size() > 0){
		companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC						
			companyUI.v_nbDieselTrucks--;
			companyUI.sl_dieselTrucksCompany.setValue(companyUI.v_nbDieselTrucks, false);
			companyUI.v_nbHydrogenTrucks++;
			companyUI.sl_hydrogenTrucksCompany.setValue(companyUI.v_nbHydrogenTrucks, false);
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
			
		}
		if(foundAdditionalVehicle){
			companyUI.c_additionalVehicles.get(gc).remove(dieselTruck);
		}
	}
	
	J_ActivityTrackerTrips tripTracker = dieselTruck.tripTracker;
	boolean available = true;
	available = dieselTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(dieselTruck);
	dieselTruck.removeEnergyAsset();

	// Re-add hydrogen vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgHydrogenConsumptionTruck_kWhpkm;			
	double vehicleScaling = 1.0;
	J_EAHydrogenVehicle hydrogenVehicle = new J_EAHydrogenVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, vehicleScaling, OL_EnergyAssetType.HYDROGEN_TRUCK, tripTracker);				
	hydrogenVehicle.available = available;
	
	zero_Interface.c_orderedVehicles.add(0, hydrogenVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(companyUI != null && foundAdditionalVehicle){
		companyUI.c_additionalVehicles.get(gc).add(hydrogenVehicle);
	}
}
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is a diesel truck left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_electricToDieselTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458977*/
J_EAEV electricTruck = null;
boolean foundAdditionalVehicle = false;

// find the electric truck to remove, search through additional vehicles first
if (zero_Interface.c_companyUIs.size() > 0 ) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		List<J_EAVehicle> totalList = new ArrayList<J_EAVehicle>();
		for (List<J_EAVehicle> list : companyUI.c_additionalVehicles.values() ) {
			totalList.addAll(list);
		}
		electricTruck = (J_EAEV)findFirst(totalList, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive);
		if ( electricTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}
// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	electricTruck = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive && gcList.contains((GridConnection)p.getParentAgent()));
}
if ( electricTruck != null ) {
	GridConnection gc = (GridConnection)electricTruck.getParentAgent();
	
	// update UI company
	UI_company companyUI = null;
	if (zero_Interface.c_companyUIs.size() > 0){
		companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
			if (companyUI.v_minEVTruckSlider >= companyUI.v_nbEVTrucks) {
				traceln("Removed already existing Electric Truck from GC: " + companyUI.p_companyName);
			}
			else {
				companyUI.v_nbEVTrucks--;
				companyUI.sl_electricTrucksCompany.setValue(companyUI.v_nbEVTrucks, false);
				companyUI.v_nbDieselTrucks++;
				companyUI.sl_dieselTrucksCompany.setValue(companyUI.v_nbDieselTrucks, false);
			}
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
			
		}
		if(foundAdditionalVehicle){
			companyUI.c_additionalVehicles.get(gc).remove(electricTruck);
		}
	}
	
	J_ActivityTrackerTrips tripTracker = electricTruck.tripTracker;
	boolean available = true;
	available = electricTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(electricTruck);
	electricTruck.removeEnergyAsset();

	// Re-add diesel vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionTruck_kWhpkm;
	double vehicleScaling = 1.0;
	J_EADieselVehicle dieselVehicle = new J_EADieselVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, vehicleScaling, OL_EnergyAssetType.DIESEL_TRUCK, tripTracker);
	dieselVehicle.available = available;
	
	zero_Interface.c_orderedVehicles.add(0, dieselVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(companyUI != null && foundAdditionalVehicle){
		companyUI.c_additionalVehicles.get(gc).add(dieselVehicle);
	}
}
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is an electric truck left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_hydrogenToDieselTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458987*/
J_EAHydrogenVehicle hydrogenTruck = null;
boolean foundAdditionalVehicle = false;

// find the hydrogen truck to remove, search through additional vehicles first
if (zero_Interface.c_companyUIs.size() > 0 ) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		List<J_EAVehicle> totalList = new ArrayList<J_EAVehicle>();
		for (List<J_EAVehicle> list : companyUI.c_additionalVehicles.values() ) {
			totalList.addAll(list);
		}
		hydrogenTruck = (J_EAHydrogenVehicle)findFirst(totalList, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive);
		if ( hydrogenTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}
// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	hydrogenTruck = (J_EAHydrogenVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.HYDROGEN_TRUCK && ((GridConnection)p.getParentAgent()).v_isActive && gcList.contains((GridConnection)p.getParentAgent()));
}
if ( hydrogenTruck != null ) {
	GridConnection gc = (GridConnection)hydrogenTruck.getParentAgent();
	
	// update UI company
	UI_company companyUI = null;
	if (zero_Interface.c_companyUIs.size() > 0){
		companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
			companyUI.v_nbHydrogenTrucks--;
			companyUI.sl_hydrogenTrucksCompany.setValue(companyUI.v_nbHydrogenTrucks, false);
			companyUI.v_nbDieselTrucks++;
			companyUI.sl_dieselTrucksCompany.setValue(companyUI.v_nbDieselTrucks, false);
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
		}
		if(foundAdditionalVehicle){
			companyUI.c_additionalVehicles.get(gc).remove(hydrogenTruck);
		}
	}
	
	J_ActivityTrackerTrips tripTracker = hydrogenTruck.tripTracker;
	boolean available = true;
	available = hydrogenTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(hydrogenTruck);
	hydrogenTruck.removeEnergyAsset();

	// Re-add diesel vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionTruck_kWhpkm;
	double vehicleScaling = 1.0;
	J_EADieselVehicle dieselVehicle = new J_EADieselVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, vehicleScaling, OL_EnergyAssetType.DIESEL_TRUCK, tripTracker);
	dieselVehicle.available = available;
	
	zero_Interface.c_orderedVehicles.add(0, dieselVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(companyUI != null && foundAdditionalVehicle){
		companyUI.c_additionalVehicles.get(gc).add(dieselVehicle);
	}
}
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is a hydrogen truck left, but could not find it.");
}

return true;
/*ALCODEEND*/}

double f_setTruckSlidersToCurrentEngineState(List<GridConnection> gcList,ShapeSlider sliderElectricTrucks,ShapeSlider sliderHydrogenTrucks,ShapeSlider sliderFossilFuelTrucks)
{/*ALCODESTART::1746018501114*/
List<J_EAVehicle> vehicles = new ArrayList<J_EAVehicle>();
for (GridConnection gc : gcList) {
	if (gc.v_isActive) {
		vehicles.addAll(gc.c_vehicleAssets);
	}
}

int numberOfGhostVehicle_Trucks = f_calculateNumberOfGhostVehicles(gcList).getThird();

int nbElectricTrucksCurrent = count(vehicles, p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK && !(p.getParentAgent() instanceof GCPublicCharger)) + numberOfGhostVehicle_Trucks;
int nbDieseltrucksCurrent = count(vehicles, p->p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK);
int nbHydrogentrucksCurrent = count(vehicles, p->p.energyAssetType == OL_EnergyAssetType.HYDROGEN_TRUCK);

int totalVehicles = nbElectricTrucksCurrent + nbDieseltrucksCurrent + nbHydrogentrucksCurrent;



int pct_electricTruckSlider = roundToInt(100.0*nbElectricTrucksCurrent/totalVehicles);
int pct_hydrogenTruckSlider = roundToInt(100.0*nbHydrogentrucksCurrent/totalVehicles);
int pct_dieselTruckSlider = roundToInt(100.0*nbDieseltrucksCurrent/totalVehicles);

sliderElectricTrucks.setValue(pct_electricTruckSlider, false);
if ( sliderHydrogenTrucks != null ) {
	sliderHydrogenTrucks.setValue(pct_hydrogenTruckSlider, false);
}
else if ( sliderHydrogenTrucks == null && pct_hydrogenTruckSlider != 0 ) {
	throw new RuntimeException("Hydrogen trucks found but no hydrogen slider passed to f_setTruckSlidersToCurrentEngineState");
}sliderFossilFuelTrucks.setValue(pct_dieselTruckSlider, false);


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

Triple<Integer, Integer, Integer> f_calculateCurrentNumberOfTrucks(List<GridConnection> gcList,Integer numberOfGhostVehicle_Trucks)
{/*ALCODESTART::1749134655530*/
if (numberOfGhostVehicle_Trucks == null) {
	numberOfGhostVehicle_Trucks = f_calculateNumberOfGhostVehicles(gcList).getThird();
}

int nbEtruckCurrent = numberOfGhostVehicle_Trucks;
int nbHydrogenTrucksCurrent = 0;
int nbDieselTrucksCurrent = 0;
for ( GridConnection gc : gcList ) {
	nbEtruckCurrent += count(gc.c_vehicleAssets, p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK && !(gc instanceof GCPublicCharger) && gc.v_isActive);
	nbHydrogenTrucksCurrent += count(gc.c_vehicleAssets, p->p.energyAssetType == OL_EnergyAssetType.HYDROGEN_TRUCK && gc.v_isActive);
	nbDieselTrucksCurrent += count(gc.c_vehicleAssets, p->p.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK && gc.v_isActive);
}

return new Triple(nbEtruckCurrent, nbHydrogenTrucksCurrent, nbDieselTrucksCurrent);
/*ALCODEEND*/}

boolean f_dieselToElectricVan(List<GridConnection> gcList)
{/*ALCODESTART::1749645625091*/
J_EADieselVehicle dieselVan = null;
boolean foundAdditionalVehicle = false;

// find the diesel van to remove, search through additional vehicles first
if (zero_Interface.c_companyUIs.size() > 0 ) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		List<J_EAVehicle> totalList = new ArrayList<J_EAVehicle>();
		for (List<J_EAVehicle> list : companyUI.c_additionalVehicles.values() ) {
			totalList.addAll(list);
		}
		dieselVan = (J_EADieselVehicle)findFirst(totalList, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_VAN && gc.v_isActive);
		if ( dieselVan != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}
// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {	
	dieselVan = (J_EADieselVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_VAN && ((GridConnection)p.getParentAgent()).v_isActive && gcList.contains((GridConnection)p.getParentAgent()));
}
if (dieselVan!=null) {
	GridConnection gc = (GridConnection)dieselVan.getParentAgent();
	
	// update UI company
	UI_company companyUI = null;
	if (zero_Interface.c_companyUIs.size() > 0){
		companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC						
			companyUI.v_nbDieselVans--;
			companyUI.sl_dieselVansCompany.setValue(companyUI.v_nbDieselVans, false);
			companyUI.v_nbEVVans++;
			companyUI.sl_electricVansCompany.setValue(companyUI.v_nbEVVans, false);
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
			if (foundAdditionalVehicle) {
				companyUI.c_additionalVehicles.get(gc).remove(dieselVan);
			}
		}
	}
		
	J_ActivityTrackerTrips tripTracker = dieselVan.tripTracker;
	boolean available = dieselVan.getAvailability();
	zero_Interface.c_orderedVehicles.remove(dieselVan);
	dieselVan.removeEnergyAsset();

	// Re-add Electric vehicle
	double capacityElectric_kW = zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerVan_kW;
	double storageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgEVStorageVan_kWh;
	double initialStateOfCharge_fr = 1.0;
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionVan_kWhpkm;
	double vehicleScalingElectric = 1.0;
	J_EAEV electricVan = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_fr, zero_Interface.energyModel.p_timeStep_h, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_VAN, tripTracker);  
	electricVan.available = available;
	
	zero_Interface.c_orderedVehicles.add(0, electricVan);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(companyUI != null && foundAdditionalVehicle){
		companyUI.c_additionalVehicles.get(gc).add(electricVan);
	}
}

else {
	return false;
	//throw new RuntimeException("Numbers suggest there is a diesel van left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_electricToDieselVan(List<GridConnection> gcList)
{/*ALCODESTART::1749645629101*/
J_EAEV electricVan = null;
boolean foundAdditionalVehicle = false;

// find the electric van to remove, search through additional vehicles first
if (zero_Interface.c_companyUIs.size() > 0 ) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		List<J_EAVehicle> totalList = new ArrayList<J_EAVehicle>();
		for (List<J_EAVehicle> list : companyUI.c_additionalVehicles.values() ) {
			totalList.addAll(list);
		}
		electricVan = (J_EAEV)findFirst(totalList, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN && ((GridConnection)p.getParentAgent()).v_isActive);
		if ( electricVan != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}
// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	electricVan = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN && ((GridConnection)p.getParentAgent()).v_isActive && gcList.contains((GridConnection)p.getParentAgent()));
}
if ( electricVan != null ) {
	GridConnection gc = (GridConnection)electricVan.getParentAgent();
	
	// update UI company
	UI_company companyUI = null;
	if (zero_Interface.c_companyUIs.size() > 0){
		companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
			if (companyUI.v_minEVVanSlider >= companyUI.v_nbEVVans) {
				traceln("Removed already existing Electric Van from GC: " + companyUI.p_companyName);
			}
			else {
				companyUI.v_nbEVVans--;
				companyUI.sl_electricVansCompany.setValue(companyUI.v_nbEVVans, false);
				companyUI.v_nbDieselVans++;
				companyUI.sl_dieselVansCompany.setValue(companyUI.v_nbDieselVans, false);
			}
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
			
		}
		if(foundAdditionalVehicle){
			companyUI.c_additionalVehicles.get(gc).remove(electricVan);
		}
	}
	
	J_ActivityTrackerTrips tripTracker = electricVan.tripTracker;
	boolean available = true;
	available = electricVan.getAvailability();
	zero_Interface.c_orderedVehicles.remove(electricVan);
	electricVan.removeEnergyAsset();

	// Re-add diesel vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionTruck_kWhpkm;
	double vehicleScaling = 1.0;
	J_EADieselVehicle dieselVehicle = new J_EADieselVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, vehicleScaling, OL_EnergyAssetType.DIESEL_VAN, tripTracker);
	dieselVehicle.available = available;
	
	zero_Interface.c_orderedVehicles.add(0, dieselVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(companyUI != null && foundAdditionalVehicle){
		companyUI.c_additionalVehicles.get(gc).add(dieselVehicle);
	}
}
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is an electric van left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_dieselToElectricCar(List<GridConnection> gcList)
{/*ALCODESTART::1749646233660*/
J_EADieselVehicle dieselCar = null;
boolean foundAdditionalVehicle = false;

// find the diesel car to remove, search through additional vehicles first
if (zero_Interface.c_companyUIs.size() > 0 ) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		List<J_EAVehicle> totalList = new ArrayList<J_EAVehicle>();
		for (List<J_EAVehicle> list : companyUI.c_additionalVehicles.values() ) {
			totalList.addAll(list);
		}
		dieselCar = (J_EADieselVehicle)findFirst(totalList, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_VEHICLE && gc.v_isActive);
		if ( dieselCar != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}
// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {	
	dieselCar = (J_EADieselVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.DIESEL_VEHICLE && ((GridConnection)p.getParentAgent()).v_isActive && gcList.contains((GridConnection)p.getParentAgent()));
}
if (dieselCar!=null) {
	GridConnection gc = (GridConnection)dieselCar.getParentAgent();
	
	// update UI company
	UI_company companyUI = null;
	if (zero_Interface.c_companyUIs.size() > 0){
		companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC						
			companyUI.v_nbDieselCars--;
			companyUI.sl_dieselCarsCompany.setValue(companyUI.v_nbDieselCars, false);
			companyUI.v_nbEVCars++;
			companyUI.sl_electricCarsCompany.setValue(companyUI.v_nbEVCars, false);
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
			if (foundAdditionalVehicle) {
				companyUI.c_additionalVehicles.get(gc).remove(dieselCar);
			}
		}
	}
		
	J_ActivityTrackerTrips tripTracker = dieselCar.tripTracker;
	boolean available = dieselCar.getAvailability();
	zero_Interface.c_orderedVehicles.remove(dieselCar);
	dieselCar.removeEnergyAsset();

	// Re-add Electric vehicle
	double capacityElectric_kW = zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerCar_kW;
	double storageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgEVStorageCar_kWh;
	double initialStateOfCharge_fr = 1.0;
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionCar_kWhpkm;
	double vehicleScalingElectric = 1.0;
	J_EAEV electricCar = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_fr, zero_Interface.energyModel.p_timeStep_h, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_VEHICLE, tripTracker);  
	electricCar.available = available;
	
	zero_Interface.c_orderedVehicles.add(0, electricCar);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(companyUI != null && foundAdditionalVehicle){
		companyUI.c_additionalVehicles.get(gc).add(electricCar);
	}
}

else {
	return false;
	//throw new RuntimeException("Numbers suggest there is a diesel car left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_electricToDieselCar(List<GridConnection> gcList)
{/*ALCODESTART::1749646235580*/
J_EAEV electricCar = null;
boolean foundAdditionalVehicle = false;

// find the electric car to remove, search through additional vehicles first
if (zero_Interface.c_companyUIs.size() > 0 ) {
	for (GridConnection gc : gcList ) {
		UI_company companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		List<J_EAVehicle> totalList = new ArrayList<J_EAVehicle>();
		for (List<J_EAVehicle> list : companyUI.c_additionalVehicles.values() ) {
			totalList.addAll(list);
		}
		electricCar = (J_EAEV)findFirst(totalList, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VEHICLE && ((GridConnection)p.getParentAgent()).v_isActive);
		if ( electricCar != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}
// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	electricCar = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VEHICLE && ((GridConnection)p.getParentAgent()).v_isActive && gcList.contains((GridConnection)p.getParentAgent()));
}
if ( electricCar != null ) {
	GridConnection gc = (GridConnection)electricCar.getParentAgent();
	
	// update UI company
	UI_company companyUI = null;
	if (zero_Interface.c_companyUIs.size() > 0){
		companyUI = zero_Interface.c_companyUIs.get(gc.p_owner.p_connectionOwnerIndexNr);
		if (companyUI != null && companyUI.c_ownedGridConnections.get(companyUI.v_currentSelectedGCnr) == gc) { // should also check the setting of selected GC
			if (companyUI.v_minEVCarSlider >= companyUI.v_nbEVCars) {
				traceln("Removed already existing Electric Car from GC: " + companyUI.p_companyName);
			}
			else {
				companyUI.v_nbEVCars--;
				companyUI.sl_electricCarsCompany.setValue(companyUI.v_nbEVCars, false);
				companyUI.v_nbDieselCars++;
				companyUI.sl_dieselCarsCompany.setValue(companyUI.v_nbDieselCars, false);
			}
			companyUI.rb_scenariosPrivateUI.setValue(2, false);
			
		}
		if(foundAdditionalVehicle){
			companyUI.c_additionalVehicles.get(gc).remove(electricCar);
		}
	}
	
	J_ActivityTrackerTrips tripTracker = electricCar.tripTracker;
	boolean available = true;
	available = electricCar.getAvailability();
	zero_Interface.c_orderedVehicles.remove(electricCar);
	electricCar.removeEnergyAsset();

	// Re-add diesel vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionTruck_kWhpkm;
	double vehicleScaling = 1.0;
	J_EADieselVehicle dieselVehicle = new J_EADieselVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeStep_h, vehicleScaling, OL_EnergyAssetType.DIESEL_VEHICLE, tripTracker);
	dieselVehicle.available = available;
	
	zero_Interface.c_orderedVehicles.add(0, dieselVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(companyUI != null && foundAdditionalVehicle){
		companyUI.c_additionalVehicles.get(gc).add(dieselVehicle);
	}
}
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is an electric car left, but could not find it.");
}

return true;
/*ALCODEEND*/}

Triple<Integer, Integer, Integer> f_calculateCurrentNumberOfVans(List<GridConnection> gcList,Integer numberOfGhostVehicle_Vans)
{/*ALCODESTART::1749655535164*/
if (numberOfGhostVehicle_Vans == null) {
	numberOfGhostVehicle_Vans = f_calculateNumberOfGhostVehicles(gcList).getSecond();
}

int nbEVansCurrent = numberOfGhostVehicle_Vans;
int nbHydrogenVansCurrent = 0;
int nbDieselVansCurrent = 0;
for ( GridConnection gc : gcList ) {
	nbEVansCurrent += count(gc.c_vehicleAssets, p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN && !(gc instanceof GCPublicCharger) && gc.v_isActive);
	nbHydrogenVansCurrent += count(gc.c_vehicleAssets, p->p.energyAssetType == OL_EnergyAssetType.HYDROGEN_VAN && gc.v_isActive);
	nbDieselVansCurrent += count(gc.c_vehicleAssets, p->p.energyAssetType == OL_EnergyAssetType.DIESEL_VAN && gc.v_isActive);
}

return new Triple(nbEVansCurrent, nbHydrogenVansCurrent, nbDieselVansCurrent);
/*ALCODEEND*/}

Triple<Integer, Integer, Integer> f_calculateCurrentNumberOfCars(List<GridConnection> gcList,Integer numberOfGhostVehicle_Cars)
{/*ALCODESTART::1749655667960*/
if (numberOfGhostVehicle_Cars == null) {
	numberOfGhostVehicle_Cars = f_calculateNumberOfGhostVehicles(gcList).getSecond();
}

int nbEVsCurrent = numberOfGhostVehicle_Cars;
int nbHydrogenCarsCurrent = 0;
int nbDieselCarsCurrent = 0;
for ( GridConnection gc : gcList ) {
	nbEVsCurrent += count(gc.c_vehicleAssets, p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VEHICLE && !(gc instanceof GCPublicCharger) && gc.v_isActive);
	nbHydrogenCarsCurrent += count(gc.c_vehicleAssets, p->p.energyAssetType == OL_EnergyAssetType.HYDROGEN_VEHICLE && gc.v_isActive);
	nbDieselCarsCurrent += count(gc.c_vehicleAssets, p->p.energyAssetType == OL_EnergyAssetType.DIESEL_VEHICLE && gc.v_isActive);
}

return new Triple(nbEVsCurrent, nbHydrogenCarsCurrent, nbDieselCarsCurrent);
/*ALCODEEND*/}

double f_setDieselVans(List<GridConnection> gcList,ShapeSlider sliderElectricVans,ShapeSlider sliderFossilFuelVans)
{/*ALCODESTART::1749655752858*/
double pctDieselVansGoal = sliderFossilFuelVans.getValue();

int numberOfGhostVehicle_Vans = f_calculateNumberOfGhostVehicles(gcList).getSecond();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfVans( gcList, numberOfGhostVehicle_Vans );

int nbEvanCurrent = triple.getFirst();
int nbDieselvansCurrent = triple.getThird();

int total_vehicles = nbEvanCurrent + nbDieselvansCurrent;

int nbDieselVansGoal = roundToInt(total_vehicles*pctDieselVansGoal/100.0);

if (nbDieselvansCurrent < nbDieselVansGoal) {
	// Add Diesel Vans
	while ( nbDieselvansCurrent < nbDieselVansGoal ) {
		if ( nbEvanCurrent > numberOfGhostVehicle_Vans ) {
			// replace electric van with a diesel van
			if (!f_electricToDieselVan(gcList)) {
				f_setVanSlidersToCurrentEngineState(gcList, sliderElectricVans, null, sliderFossilFuelVans);
				return;
			}
			else {
				nbDieselvansCurrent++;
				nbEvanCurrent--;
			}
		}
	}
}
else {
	// Remove Diesel Vans
	while ( nbDieselvansCurrent > nbDieselVansGoal ) {
		// replace a diesel van with an electric van
		if (!f_dieselToElectricVan(gcList)) {
			f_setVanSlidersToCurrentEngineState(gcList, sliderElectricVans, null, sliderFossilFuelVans);
			return;
		}
		nbDieselvansCurrent--;
		nbEvanCurrent++;
	}
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setElectricVans(List<GridConnection> gcList,ShapeSlider sliderElectricVans,ShapeSlider sliderFossilFuelVans)
{/*ALCODESTART::1749656346356*/
double pctElectricVansGoal = sliderElectricVans.getValue();

int numberOfGhostVehicle_Vans = f_calculateNumberOfGhostVehicles(gcList).getSecond();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfVans( gcList, numberOfGhostVehicle_Vans );

int nbEvanCurrent = triple.getFirst();
int nbDieselvansCurrent = triple.getThird();

int total_vehicles = nbEvanCurrent + nbDieselvansCurrent;

int nbElectricVansGoal = roundToInt(total_vehicles*pctElectricVansGoal/100.0);

if (nbEvanCurrent < nbElectricVansGoal) {
	// Add Electric Vans
	while ( nbEvanCurrent < nbElectricVansGoal ) {
		if ( nbDieselvansCurrent > 0 ) {
			// replace a diesel van with an electric van
			if (!f_dieselToElectricVan(gcList)) {
				f_setVanSlidersToCurrentEngineState(gcList, sliderElectricVans, null, sliderFossilFuelVans);
				return;
			}
			else {
				nbEvanCurrent++;
				nbDieselvansCurrent--;
			}
		}
	}
}
else {
	// Remove Electric Vans
	while ( nbEvanCurrent > nbElectricVansGoal ) {
		// replace an electric van with a diesel van
		if (!f_electricToDieselVan(gcList)) {
			f_setVanSlidersToCurrentEngineState(gcList, sliderElectricVans, null, sliderFossilFuelVans);
			return;
		}
		nbEvanCurrent--;
		nbDieselvansCurrent++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setDieselCars(List<GridConnection> gcList,ShapeSlider sliderElectricCars,ShapeSlider sliderFossilFuelCars)
{/*ALCODESTART::1749717776391*/
double pctDieselCarsGoal = sliderFossilFuelCars.getValue();

int numberOfGhostVehicle_Cars = f_calculateNumberOfGhostVehicles(gcList).getSecond();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfCars( gcList, numberOfGhostVehicle_Cars );

int nbEcarCurrent = triple.getFirst();
int nbDieselcarsCurrent = triple.getThird();

int total_vehicles = nbEcarCurrent + nbDieselcarsCurrent;

int nbDieselCarsGoal = roundToInt(total_vehicles*pctDieselCarsGoal/100.0);

if (nbDieselcarsCurrent < nbDieselCarsGoal) {
	// Add Diesel Cars
	while ( nbDieselcarsCurrent < nbDieselCarsGoal ) {
		if ( nbEcarCurrent > numberOfGhostVehicle_Cars ) {
			// replace electric car with a diesel car
			if (!f_electricToDieselCar(gcList)) {
				f_setCarSlidersToCurrentEngineState(gcList, sliderElectricCars, null, sliderFossilFuelCars);
				return;
			}
			else {
				nbDieselcarsCurrent++;
				nbEcarCurrent--;
			}
		}
	}
}
else {
	// Remove Diesel Cars
	while ( nbDieselcarsCurrent > nbDieselCarsGoal ) {
		// replace a diesel car with an electric car
		if (!f_dieselToElectricCar(gcList)) {
			f_setCarSlidersToCurrentEngineState(gcList, sliderElectricCars, null, sliderFossilFuelCars);
			return;
		}
		nbDieselcarsCurrent--;
		nbEcarCurrent++;
	}
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setElectricCars(List<GridConnection> gcList,ShapeSlider sliderElectricCars,ShapeSlider sliderFossilFuelCars)
{/*ALCODESTART::1749717779317*/
double pctElectricCarsGoal = sliderElectricCars.getValue();

int numberOfGhostVehicle_Cars = f_calculateNumberOfGhostVehicles(gcList).getSecond();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfCars( gcList, numberOfGhostVehicle_Cars );

int nbEcarCurrent = triple.getFirst();
int nbDieselcarsCurrent = triple.getThird();

int total_vehicles = nbEcarCurrent + nbDieselcarsCurrent;

int nbElectricCarsGoal = roundToInt(total_vehicles*pctElectricCarsGoal/100.0);

if (nbEcarCurrent < nbElectricCarsGoal) {
	// Add Electric Cars
	while ( nbEcarCurrent < nbElectricCarsGoal ) {
		if ( nbDieselcarsCurrent > 0 ) {
			// replace a diesel car with an electric car
			if (!f_dieselToElectricCar(gcList)) {
				f_setCarSlidersToCurrentEngineState(gcList, sliderElectricCars, null, sliderFossilFuelCars);
				return;
			}
			else {
				nbEcarCurrent++;
				nbDieselcarsCurrent--;
			}
		}
	}
}
else {
	// Remove Electric Cars
	while ( nbEcarCurrent > nbElectricCarsGoal ) {
		// replace an electric car with a diesel car
		if (!f_electricToDieselCar(gcList)) {
			f_setCarSlidersToCurrentEngineState(gcList, sliderElectricCars, null, sliderFossilFuelCars);
			return;
		}
		nbEcarCurrent--;
		nbDieselcarsCurrent++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setVanSlidersToCurrentEngineState(List<GridConnection> gcList,ShapeSlider sliderElectricVans,ShapeSlider sliderHydrogenVans,ShapeSlider sliderFossilFuelVans)
{/*ALCODESTART::1749717909708*/
List<J_EAVehicle> vehicles = new ArrayList<J_EAVehicle>();
for (GridConnection gc : gcList) {
	if (gc.v_isActive) {
		vehicles.addAll(gc.c_vehicleAssets);
	}
}

int numberOfGhostVehicle_Vans = f_calculateNumberOfGhostVehicles(gcList).getThird();

int nbElectricVansCurrent = count(vehicles, p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN && !(p.getParentAgent() instanceof GCPublicCharger)) + numberOfGhostVehicle_Vans;
int nbDieselvansCurrent = count(vehicles, p->p.energyAssetType == OL_EnergyAssetType.DIESEL_VAN);
int nbHydrogenvansCurrent = count(vehicles, p->p.energyAssetType == OL_EnergyAssetType.HYDROGEN_VAN);

int totalVehicles = nbElectricVansCurrent + nbDieselvansCurrent + nbHydrogenvansCurrent;



int pct_electricVanSlider = roundToInt(100.0*nbElectricVansCurrent/totalVehicles);
int pct_hydrogenVanSlider = roundToInt(100.0*nbHydrogenvansCurrent/totalVehicles);
int pct_dieselVanSlider = roundToInt(100.0*nbDieselvansCurrent/totalVehicles);

sliderElectricVans.setValue(pct_electricVanSlider, false);
if ( sliderHydrogenVans != null ) {
	sliderHydrogenVans.setValue(pct_hydrogenVanSlider, false);
}
else if ( sliderHydrogenVans == null && pct_hydrogenVanSlider != 0 ) {
	throw new RuntimeException("Hydrogen vans found but no hydrogen slider passed to f_setVanSlidersToCurrentEngineState");
}
sliderFossilFuelVans.setValue(pct_dieselVanSlider, false);


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setCarSlidersToCurrentEngineState(List<GridConnection> gcList,ShapeSlider sliderElectricCars,ShapeSlider sliderHydrogenCars,ShapeSlider sliderFossilFuelCars)
{/*ALCODESTART::1749717987139*/
List<J_EAVehicle> vehicles = new ArrayList<J_EAVehicle>();
for (GridConnection gc : gcList) {
	if (gc.v_isActive) {
		vehicles.addAll(gc.c_vehicleAssets);
	}
}

int numberOfGhostVehicle_Cars = f_calculateNumberOfGhostVehicles(gcList).getThird();

int nbElectricCarsCurrent = count(vehicles, p->p.energyAssetType == OL_EnergyAssetType.ELECTRIC_VEHICLE && !(p.getParentAgent() instanceof GCPublicCharger)) + numberOfGhostVehicle_Cars;
int nbDieselcarsCurrent = count(vehicles, p->p.energyAssetType == OL_EnergyAssetType.DIESEL_VEHICLE);
int nbHydrogencarsCurrent = count(vehicles, p->p.energyAssetType == OL_EnergyAssetType.HYDROGEN_VEHICLE);

int totalVehicles = nbElectricCarsCurrent + nbDieselcarsCurrent + nbHydrogencarsCurrent;



int pct_electricCarSlider = roundToInt(100.0*nbElectricCarsCurrent/totalVehicles);
int pct_hydrogenCarSlider = roundToInt(100.0*nbHydrogencarsCurrent/totalVehicles);
int pct_dieselCarSlider = roundToInt(100.0*nbDieselcarsCurrent/totalVehicles);

sliderElectricCars.setValue(pct_electricCarSlider, false);
if ( sliderHydrogenCars != null ) {
	sliderHydrogenCars.setValue(pct_hydrogenCarSlider, false);
}
else if ( sliderHydrogenCars == null && pct_hydrogenCarSlider != 0 ) {
	throw new RuntimeException("Hydrogen cars found but no hydrogen slider passed to f_setCarSlidersToCurrentEngineState");
}
sliderFossilFuelCars.setValue(pct_dieselCarSlider, false);


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.b_changeToCustomScenario = true;
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

