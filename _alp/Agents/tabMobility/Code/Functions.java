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
	for (I_Vehicle vehicle : gc.c_vehicleAssets) {
		vehicle.getTripTracker().distanceScaling_fr = scalingFactor;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

Triple<Integer, Integer, Integer> f_calculateNumberOfGhostVehicles(List<GridConnection> gcList)
{/*ALCODESTART::1729168033110*/
Integer numberOfGhostVehicle_Cars = 0;
Integer numberOfGhostVehicle_Vans = 0;
Integer numberOfGhostVehicle_Trucks = 0;

for (GridConnection gc : gcList ) {
	if(gc.v_hasQuarterHourlyValues && gc.v_isActive){
		numberOfGhostVehicle_Cars += zero_Interface.c_scenarioMap_Current.get(gc.p_uid).getCurrentEVCars();
		numberOfGhostVehicle_Vans += zero_Interface.c_scenarioMap_Current.get(gc.p_uid).getCurrentEVVans();
		numberOfGhostVehicle_Trucks += zero_Interface.c_scenarioMap_Current.get(gc.p_uid).getCurrentEVTrucks();
	}
}


return Triple.of(numberOfGhostVehicle_Cars, numberOfGhostVehicle_Vans, numberOfGhostVehicle_Trucks);
/*ALCODEEND*/}

double f_setPetroleumFuelTrucks(List<GridConnection> gcList,ShapeSlider sliderElectricTrucks,ShapeSlider sliderHydrogenTrucks,ShapeSlider sliderFossilFuelTrucks)
{/*ALCODESTART::1746008458894*/
double pctPetroleumFuelTrucksGoal = sliderFossilFuelTrucks.getValue();

int numberOfGhostVehicle_Trucks = f_calculateNumberOfGhostVehicles(gcList).getRight();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfTrucks( gcList, numberOfGhostVehicle_Trucks );

int nbEtruckCurrent = triple.getLeft();
int nbHydrogentrucksCurrent = triple.getMiddle();
int nbPetroleumFueltrucksCurrent = triple.getRight();
int total_vehicles = nbEtruckCurrent + nbHydrogentrucksCurrent + nbPetroleumFueltrucksCurrent;

int nbPetroleumFuelTrucksGoal = roundToInt(total_vehicles*pctPetroleumFuelTrucksGoal/100.0);
boolean finishedLookingThroughElectricTrucks = false;

if (nbPetroleumFueltrucksCurrent < nbPetroleumFuelTrucksGoal) {
	// Add PetroleumFuel Trucks
	while ( nbPetroleumFueltrucksCurrent < nbPetroleumFuelTrucksGoal ) {
		if ( nbEtruckCurrent > numberOfGhostVehicle_Trucks && !finishedLookingThroughElectricTrucks ) {
			// replace electric truck with a petroleumFuel truck
			if (!f_electricToPetroleumFuelTruck(gcList)) {
				finishedLookingThroughElectricTrucks = true;
			}
			else {
				nbPetroleumFueltrucksCurrent++;
				nbEtruckCurrent--;
			}
		}
		else{// ( nbHydrogentrucksCurrent > 0 ) {
			// replace hydrogen truck with petroleumFuel truck
			if (!f_hydrogenToPetroleumFuelTruck(gcList)) {
				f_setTruckSlidersToCurrentEngineState(gcList, sliderElectricTrucks, sliderHydrogenTrucks, sliderFossilFuelTrucks);
				return;
			}
			nbPetroleumFueltrucksCurrent++;
			nbHydrogentrucksCurrent--;
		}
		/*
		else {
			throw new RuntimeException("Can not add another petroleumFuel vehicle as there are no other vehicles to replace.");
		}
		*/
	}
}
else {
	// Remove PetroleumFuel Trucks
	while ( nbPetroleumFueltrucksCurrent > nbPetroleumFuelTrucksGoal ) {
		// replace a petroleumFuel truck with an electric truck
		if (!f_petroleumFuelToElectricTruck(gcList)) {
			f_setTruckSlidersToCurrentEngineState(gcList, sliderElectricTrucks, sliderHydrogenTrucks, sliderFossilFuelTrucks);
			return;
		}
		nbPetroleumFueltrucksCurrent--;
		nbEtruckCurrent++;
	}
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setHydrogenTrucks(List<GridConnection> gcList,ShapeSlider sliderElectricTrucks,ShapeSlider sliderHydrogenTrucks,ShapeSlider sliderFossilFuelTrucks)
{/*ALCODESTART::1746008458907*/
double pctHydrogenTrucksGoal = sliderHydrogenTrucks.getValue();

int numberOfGhostVehicle_Trucks = f_calculateNumberOfGhostVehicles(gcList).getRight();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfTrucks( gcList, numberOfGhostVehicle_Trucks );

int nbEtruckCurrent = triple.getLeft();
int nbHydrogentrucksCurrent = triple.getMiddle();
int nbPetroleumFueltrucksCurrent = triple.getRight();
int total_vehicles = nbEtruckCurrent + nbHydrogentrucksCurrent + nbPetroleumFueltrucksCurrent;

int nbHydrogenTrucksGoal = roundToInt(total_vehicles*pctHydrogenTrucksGoal/100.0);
boolean finishedLookingThroughPetroleumFuelTrucks = false;

if (nbHydrogentrucksCurrent < nbHydrogenTrucksGoal) {
	// Add Hydrogen Trucks
	while ( nbHydrogentrucksCurrent < nbHydrogenTrucksGoal && !finishedLookingThroughPetroleumFuelTrucks ) {
		if ( nbPetroleumFueltrucksCurrent > 0 ) {
			// replace a petroleumFuel truck with a hydrogen truck
			if (!f_petroleumFuelToHydrogenTruck(gcList)) {
				finishedLookingThroughPetroleumFuelTrucks = true;
			}
			else {
				nbHydrogentrucksCurrent++;
				nbPetroleumFueltrucksCurrent--;
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
		// replace a hydrogen truck with a petroleumFuel truck
		if (!f_hydrogenToPetroleumFuelTruck(gcList)) {
			f_setTruckSlidersToCurrentEngineState(gcList, sliderElectricTrucks, sliderHydrogenTrucks, sliderFossilFuelTrucks);
			return;
		}
		nbHydrogentrucksCurrent--;
		nbPetroleumFueltrucksCurrent++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setElectricTrucks(List<GridConnection> gcList,ShapeSlider sliderElectricTrucks,ShapeSlider sliderHydrogenTrucks,ShapeSlider sliderFossilFuelTrucks)
{/*ALCODESTART::1746008458917*/
double pctElectricTrucksGoal = sliderElectricTrucks.getValue();

int numberOfGhostVehicle_Trucks = f_calculateNumberOfGhostVehicles(gcList).getRight();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfTrucks( gcList, numberOfGhostVehicle_Trucks );

int nbEtruckCurrent = triple.getLeft();
int nbHydrogentrucksCurrent = triple.getMiddle();
int nbPetroleumFueltrucksCurrent = triple.getRight();
int total_vehicles = nbEtruckCurrent + nbHydrogentrucksCurrent + nbPetroleumFueltrucksCurrent;

int nbElectricTrucksGoal = roundToInt(total_vehicles*pctElectricTrucksGoal/100.0);
boolean finishedLookingThroughPetroleumFuelTrucks = false;

if (nbEtruckCurrent < nbElectricTrucksGoal) {
	// Add Electric Trucks
	while ( nbEtruckCurrent < nbElectricTrucksGoal && !finishedLookingThroughPetroleumFuelTrucks ) {
		if ( nbPetroleumFueltrucksCurrent > 0 ) {
			// replace a petroleumFuel truck with an electric truck
			if (!f_petroleumFuelToElectricTruck(gcList)) {
				finishedLookingThroughPetroleumFuelTrucks = true;
			}
			else {
				nbEtruckCurrent++;
				nbPetroleumFueltrucksCurrent--;
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
		// replace an electric truck with a petroleumFuel truck
		if (!f_electricToPetroleumFuelTruck(gcList)) {
			f_setTruckSlidersToCurrentEngineState(gcList, sliderElectricTrucks, sliderHydrogenTrucks, sliderFossilFuelTrucks);
			return;
		}
		nbEtruckCurrent--;
		nbPetroleumFueltrucksCurrent++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

boolean f_petroleumFuelToElectricTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458938*/
J_EAFuelVehicle petroleumFuelTruck = null;
boolean foundAdditionalVehicle = false;

// find the petroleumFuel truck to remove, search through additional vehicles first
for (GridConnection gc : gcList ) {
	if(gc instanceof GCUtility && gc.v_isActive){
		petroleumFuelTruck = (J_EAFuelVehicle)findFirst(zero_Interface.c_additionalVehicles.get(gc.p_uid), p -> p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK);
		if ( petroleumFuelTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}

// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {	
	petroleumFuelTruck = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK && ((GridConnection)p.getOwner()).v_isActive && gcList.contains((GridConnection)p.getOwner()));
}
if (petroleumFuelTruck!=null) {
	GridConnection gc = (GridConnection)petroleumFuelTruck.getOwner();
		
	J_ActivityTrackerTrips tripTracker = petroleumFuelTruck.getTripTracker();
	boolean available = petroleumFuelTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(petroleumFuelTruck);
	petroleumFuelTruck.removeEnergyAsset();

	// Re-add Electric vehicle
	double capacityElectric_kW = zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerTruck_kW;
	double storageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgEVStorageTruck_kWh;
	double initialStateOfCharge_fr = 1.0;
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionTruck_kWhpkm;
	double vehicleScalingElectric = 1.0;
	J_EAEV electricTruck = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_fr, zero_Interface.energyModel.p_timeParameters, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_TRUCK, tripTracker, available);  
	
	zero_Interface.c_orderedVehicles.add(0, electricTruck);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(foundAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(gc.p_uid).add(electricTruck);
	}
}

else {
	return false;
	//throw new RuntimeException("Numbers suggest there is a petroleumFuel truck left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_hydrogenToElectricTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458950*/
J_EAFuelVehicle hydrogenTruck = null;
boolean foundAdditionalVehicle = false;

// find the hydrogen truck to remove, search through additional vehicles first
for (GridConnection gc : gcList ) {
	if(gc instanceof GCUtility && gc.v_isActive){
		hydrogenTruck = (J_EAFuelVehicle)findFirst(zero_Interface.c_additionalVehicles.get(gc.p_uid), p -> p.getEAType() == OL_EnergyAssetType.HYDROGEN_TRUCK);
		if ( hydrogenTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}

// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	hydrogenTruck = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == OL_EnergyAssetType.HYDROGEN_TRUCK && ((GridConnection)p.getOwner()).v_isActive && gcList.contains((GridConnection)p.getOwner()));
}
if (hydrogenTruck!=null) {
	GridConnection gc = (GridConnection)hydrogenTruck.getOwner();
	
	J_ActivityTrackerTrips tripTracker = hydrogenTruck.getTripTracker();
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
	J_EAEV electricTruck = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_fr, zero_Interface.energyModel.p_timeParameters, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_TRUCK, tripTracker, available);  

	zero_Interface.c_orderedVehicles.add(0, electricTruck);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(foundAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(gc.p_uid).add(electricTruck);
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
for (GridConnection gc : gcList ) {
	if(gc instanceof GCUtility && gc.v_isActive){
		electricTruck = (J_EAEV)findFirst(zero_Interface.c_additionalVehicles.get(gc.p_uid), p -> p.getEAType() == OL_EnergyAssetType.ELECTRIC_TRUCK);
		if ( electricTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}

// if no additional vehicle was found, search through the regular ordering of vehicle
if (!foundAdditionalVehicle) {
	electricTruck = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == OL_EnergyAssetType.ELECTRIC_TRUCK && ((GridConnection)p.getOwner()).v_isActive && gcList.contains((GridConnection)p.getOwner()));
}
if (electricTruck!=null) {
	GridConnection gc = (GridConnection)electricTruck.getOwner();
	
	J_ActivityTrackerTrips tripTracker = electricTruck.getTripTracker();
	boolean available = true;
	available = electricTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(electricTruck);
	electricTruck.removeEnergyAsset();

	// Re-add hydrogen vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgHydrogenConsumptionTruck_kWhpkm;			
	double vehicleScaling = 1.0;
	J_EAFuelVehicle hydrogenVehicle = new J_EAFuelVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeParameters, vehicleScaling, OL_EnergyAssetType.HYDROGEN_TRUCK, tripTracker, OL_EnergyCarriers.HYDROGEN, available);				
	
	zero_Interface.c_orderedVehicles.add(0, hydrogenVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(foundAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(gc.p_uid).add(hydrogenVehicle);
	}
}				
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is an electric truck left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_petroleumFuelToHydrogenTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458968*/
J_EAFuelVehicle petroleumFuelTruck = null;
boolean foundAdditionalVehicle = false;

// find the petroleumFuel truck to remove, search through additional vehicles first
for (GridConnection gc : gcList ) {
	if(gc instanceof GCUtility && gc.v_isActive){
		petroleumFuelTruck = (J_EAFuelVehicle)findFirst(zero_Interface.c_additionalVehicles.get(gc.p_uid), p -> p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK);
		if ( petroleumFuelTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}

// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	petroleumFuelTruck = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK && ((GridConnection)p.getOwner()).v_isActive && gcList.contains((GridConnection)p.getOwner()));
}
if (petroleumFuelTruck!=null) {
	GridConnection gc = (GridConnection)petroleumFuelTruck.getOwner();
	
	J_ActivityTrackerTrips tripTracker = petroleumFuelTruck.getTripTracker();
	boolean available = true;
	available = petroleumFuelTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(petroleumFuelTruck);
	petroleumFuelTruck.removeEnergyAsset();

	// Re-add hydrogen vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgHydrogenConsumptionTruck_kWhpkm;			
	double vehicleScaling = 1.0;
	J_EAFuelVehicle hydrogenVehicle = new J_EAFuelVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeParameters, vehicleScaling, OL_EnergyAssetType.HYDROGEN_TRUCK, tripTracker, OL_EnergyCarriers.HYDROGEN, available);				
	
	zero_Interface.c_orderedVehicles.add(0, hydrogenVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(foundAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(gc.p_uid).add(hydrogenVehicle);
	}
}
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is a petroleumFuel truck left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_electricToPetroleumFuelTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458977*/
J_EAEV electricTruck = null;
boolean foundAdditionalVehicle = false;

// find the electric truck to remove, search through additional vehicles first
for (GridConnection gc : gcList ) {
	if(gc instanceof GCUtility && gc.v_isActive){
		electricTruck = (J_EAEV)findFirst(zero_Interface.c_additionalVehicles.get(gc.p_uid), p -> p.getEAType() == OL_EnergyAssetType.ELECTRIC_TRUCK);
		if ( electricTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}

// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	electricTruck = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == OL_EnergyAssetType.ELECTRIC_TRUCK && ((GridConnection)p.getOwner()).v_isActive && gcList.contains((GridConnection)p.getOwner()));
}
if ( electricTruck != null ) {
	GridConnection gc = (GridConnection)electricTruck.getOwner();
	
	J_ActivityTrackerTrips tripTracker = electricTruck.getTripTracker();
	boolean available = true;
	available = electricTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(electricTruck);
	electricTruck.removeEnergyAsset();

	// Re-add petroleumFuel vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionTruck_kWhpkm;
	double vehicleScaling = 1.0;
	J_EAFuelVehicle petroleumFuelVehicle = new J_EAFuelVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeParameters, vehicleScaling, OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK, tripTracker, OL_EnergyCarriers.PETROLEUM_FUEL, available);
	
	zero_Interface.c_orderedVehicles.add(0, petroleumFuelVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(foundAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(gc.p_uid).add(petroleumFuelVehicle);
	}
}
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is an electric truck left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_hydrogenToPetroleumFuelTruck(List<GridConnection> gcList)
{/*ALCODESTART::1746008458987*/
J_EAFuelVehicle hydrogenTruck = null;
boolean foundAdditionalVehicle = false;

// find the hydrogen truck to remove, search through additional vehicles first
for (GridConnection gc : gcList ) {
	if(gc instanceof GCUtility && gc.v_isActive){
		hydrogenTruck = (J_EAFuelVehicle)findFirst(zero_Interface.c_additionalVehicles.get(gc.p_uid), p -> p.getEAType() == OL_EnergyAssetType.HYDROGEN_TRUCK);
		if ( hydrogenTruck != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}

// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	hydrogenTruck = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == OL_EnergyAssetType.HYDROGEN_TRUCK && ((GridConnection)p.getOwner()).v_isActive && gcList.contains((GridConnection)p.getOwner()));
}
if ( hydrogenTruck != null ) {
	GridConnection gc = (GridConnection)hydrogenTruck.getOwner();
	
	J_ActivityTrackerTrips tripTracker = hydrogenTruck.getTripTracker();
	boolean available = true;
	available = hydrogenTruck.getAvailability();
	zero_Interface.c_orderedVehicles.remove(hydrogenTruck);
	hydrogenTruck.removeEnergyAsset();

	// Re-add petroleumFuel vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionTruck_kWhpkm;
	double vehicleScaling = 1.0;
	J_EAFuelVehicle petroleumFuelVehicle = new J_EAFuelVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeParameters, vehicleScaling, OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK, tripTracker, OL_EnergyCarriers.PETROLEUM_FUEL, available);
	
	zero_Interface.c_orderedVehicles.add(0, petroleumFuelVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(foundAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(gc.p_uid).add(petroleumFuelVehicle);
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
List<J_EAEV> electricVehicles = new ArrayList<>();
List<J_EAFuelVehicle> petroleumVehicles = new ArrayList<>();
List<J_EAFuelVehicle> hydrogenVehicles = new ArrayList<>();
for (GridConnection gc : gcList) {
	if (gc.v_isActive) {
		electricVehicles.addAll(gc.c_electricVehicles);
		petroleumVehicles.addAll(gc.c_petroleumFuelVehicles);
		hydrogenVehicles.addAll(gc.c_hydrogenVehicles);
	}
}

int numberOfGhostVehicle_Trucks = f_calculateNumberOfGhostVehicles(gcList).getRight();

int nbElectricTrucksCurrent = count(electricVehicles, p->p.getEAType() == OL_EnergyAssetType.ELECTRIC_TRUCK && !(p.getOwner() instanceof GCPublicCharger)) + numberOfGhostVehicle_Trucks;
int nbPetroleumFueltrucksCurrent = count(petroleumVehicles, p->p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK);
int nbHydrogentrucksCurrent = count(hydrogenVehicles, p->p.getEAType() == OL_EnergyAssetType.HYDROGEN_TRUCK);

int totalVehicles = nbElectricTrucksCurrent + nbPetroleumFueltrucksCurrent + nbHydrogentrucksCurrent;



int pct_electricTruckSlider = roundToInt(100.0*nbElectricTrucksCurrent/totalVehicles);
int pct_hydrogenTruckSlider = roundToInt(100.0*nbHydrogentrucksCurrent/totalVehicles);
int pct_petroleumFuelTruckSlider = roundToInt(100.0*nbPetroleumFueltrucksCurrent/totalVehicles);

sliderElectricTrucks.setValue(pct_electricTruckSlider, false);
if ( sliderHydrogenTrucks != null ) {
	sliderHydrogenTrucks.setValue(pct_hydrogenTruckSlider, false);
}
else if ( sliderHydrogenTrucks == null && pct_hydrogenTruckSlider != 0 ) {
	throw new RuntimeException("Hydrogen trucks found but no hydrogen slider passed to f_setTruckSlidersToCurrentEngineState");
}sliderFossilFuelTrucks.setValue(pct_petroleumFuelTruckSlider, false);


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

Triple<Integer, Integer, Integer> f_calculateCurrentNumberOfTrucks(List<GridConnection> gcList,Integer numberOfGhostVehicle_Trucks)
{/*ALCODESTART::1749134655530*/
if (numberOfGhostVehicle_Trucks == null) {
	numberOfGhostVehicle_Trucks = f_calculateNumberOfGhostVehicles(gcList).getRight();
}

int nbEtruckCurrent = numberOfGhostVehicle_Trucks;
int nbHydrogenTrucksCurrent = 0;
int nbPetroleumFuelTrucksCurrent = 0;
for ( GridConnection gc : gcList ) {
	nbEtruckCurrent += count(gc.c_electricVehicles, p->p.getEAType() == OL_EnergyAssetType.ELECTRIC_TRUCK && !(gc instanceof GCPublicCharger) && gc.v_isActive);
	nbHydrogenTrucksCurrent += count(gc.c_hydrogenVehicles, p->p.getEAType() == OL_EnergyAssetType.HYDROGEN_TRUCK && gc.v_isActive);
	nbPetroleumFuelTrucksCurrent += count(gc.c_petroleumFuelVehicles, p->p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK && gc.v_isActive);
}

return Triple.of(nbEtruckCurrent, nbHydrogenTrucksCurrent, nbPetroleumFuelTrucksCurrent);
/*ALCODEEND*/}

boolean f_petroleumFuelToElectricVan(List<GridConnection> gcList)
{/*ALCODESTART::1749645625091*/
J_EAFuelVehicle petroleumFuelVan = null;
boolean foundAdditionalVehicle = false;

// find the petroleumFuel van to remove, search through additional vehicles first
for (GridConnection gc : gcList ) {
	if(gc instanceof GCUtility && gc.v_isActive){
		petroleumFuelVan = (J_EAFuelVehicle)findFirst(zero_Interface.c_additionalVehicles.get(gc.p_uid), p -> p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VAN);
		if ( petroleumFuelVan != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}

// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {	
	petroleumFuelVan = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VAN && ((GridConnection)p.getOwner()).v_isActive && gcList.contains((GridConnection)p.getOwner()));
}
if (petroleumFuelVan!=null) {
	GridConnection gc = (GridConnection)petroleumFuelVan.getOwner();
		
	J_ActivityTrackerTrips tripTracker = petroleumFuelVan.getTripTracker();
	boolean available = petroleumFuelVan.getAvailability();
	zero_Interface.c_orderedVehicles.remove(petroleumFuelVan);
	petroleumFuelVan.removeEnergyAsset();

	// Re-add Electric vehicle
	double capacityElectric_kW = zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerVan_kW;
	double storageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgEVStorageVan_kWh;
	double initialStateOfCharge_fr = 1.0;
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionVan_kWhpkm;
	double vehicleScalingElectric = 1.0;
	J_EAEV electricVan = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_fr, zero_Interface.energyModel.p_timeParameters, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_VAN, tripTracker, available);  
	
	zero_Interface.c_orderedVehicles.add(0, electricVan);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(foundAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(gc.p_uid).add(electricVan);
	}
}

else {
	return false;
	//throw new RuntimeException("Numbers suggest there is a petroleumFuel van left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_electricToPetroleumFuelVan(List<GridConnection> gcList)
{/*ALCODESTART::1749645629101*/
J_EAEV electricVan = null;
boolean foundAdditionalVehicle = false;

// find the electric van to remove, search through additional vehicles first
for (GridConnection gc : gcList ) {
	if(gc instanceof GCUtility && gc.v_isActive){
		electricVan = (J_EAEV)findFirst(zero_Interface.c_additionalVehicles.get(gc.p_uid), p -> p.getEAType() == OL_EnergyAssetType.ELECTRIC_VAN);
		if ( electricVan != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}

// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	electricVan = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == OL_EnergyAssetType.ELECTRIC_VAN && ((GridConnection)p.getOwner()).v_isActive && gcList.contains((GridConnection)p.getOwner()));
}
if ( electricVan != null ) {
	GridConnection gc = (GridConnection)electricVan.getOwner();
	
	J_ActivityTrackerTrips tripTracker = electricVan.getTripTracker();
	boolean available = true;
	available = electricVan.getAvailability();
	zero_Interface.c_orderedVehicles.remove(electricVan);
	electricVan.removeEnergyAsset();

	// Re-add petroleumFuel vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgDieselConsumptionVan_kWhpkm;
	double vehicleScaling = 1.0;
	J_EAFuelVehicle petroleumFuelVehicle = new J_EAFuelVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeParameters, vehicleScaling, OL_EnergyAssetType.PETROLEUM_FUEL_VAN, tripTracker, OL_EnergyCarriers.PETROLEUM_FUEL, available);
	
	zero_Interface.c_orderedVehicles.add(0, petroleumFuelVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(foundAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(gc.p_uid).add(petroleumFuelVehicle);
	}
}
else {
	return false;
	//throw new RuntimeException("Numbers suggest there is an electric van left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_petroleumFuelToElectricCar(List<GridConnection> gcList)
{/*ALCODESTART::1749646233660*/
J_EAFuelVehicle petroleumFuelCar = null;
boolean foundAdditionalVehicle = false;

// find the petroleumFuel car to remove, search through additional vehicles first
for (GridConnection gc : gcList ) {
	if(gc instanceof GCUtility && gc.v_isActive){
		petroleumFuelCar = (J_EAFuelVehicle)findFirst(zero_Interface.c_additionalVehicles.get(gc.p_uid), p -> p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE);
		if ( petroleumFuelCar != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}

// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {	
	petroleumFuelCar = (J_EAFuelVehicle)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE && ((GridConnection)p.getOwner()).v_isActive && gcList.contains((GridConnection)p.getOwner()));
}
if (petroleumFuelCar!=null) {
	GridConnection gc = (GridConnection)petroleumFuelCar.getOwner();
		
	J_ActivityTrackerTrips tripTracker = petroleumFuelCar.getTripTracker();
	boolean available = petroleumFuelCar.getAvailability();
	zero_Interface.c_orderedVehicles.remove(petroleumFuelCar);
	petroleumFuelCar.removeEnergyAsset();

	// Re-add Electric vehicle
	double capacityElectric_kW = zero_Interface.energyModel.avgc_data.p_avgEVMaxChargePowerCar_kW;
	double storageCapacity_kWh = zero_Interface.energyModel.avgc_data.p_avgEVStorageCar_kWh;
	double initialStateOfCharge_fr = 1.0;
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionCar_kWhpkm;
	double vehicleScalingElectric = 1.0;
	J_EAEV electricCar = new J_EAEV(gc, capacityElectric_kW, storageCapacity_kWh, initialStateOfCharge_fr, zero_Interface.energyModel.p_timeParameters, energyConsumption_kWhpkm, vehicleScalingElectric, OL_EnergyAssetType.ELECTRIC_VEHICLE, tripTracker, available);  
	
	zero_Interface.c_orderedVehicles.add(0, electricCar);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(foundAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(gc.p_uid).add(electricCar);
	}
}

else {
	return false;
	//throw new RuntimeException("Numbers suggest there is a petroleumFuel car left, but could not find it.");
}

return true;
/*ALCODEEND*/}

boolean f_electricToPetroleumFuelCar(List<GridConnection> gcList)
{/*ALCODESTART::1749646235580*/
J_EAEV electricCar = null;
boolean foundAdditionalVehicle = false;

// find the electric car to remove, search through additional vehicles first
for (GridConnection gc : gcList ) {
	if(gc instanceof GCUtility && gc.v_isActive){
		electricCar = (J_EAEV)findFirst(zero_Interface.c_additionalVehicles.get(gc.p_uid), p -> p.getEAType() == OL_EnergyAssetType.ELECTRIC_VEHICLE);
		if ( electricCar != null ) {
			foundAdditionalVehicle = true;
			break;
		}
	}
}

// if no additional vehicle was found, search through the regular ordering of vehicles
if (!foundAdditionalVehicle) {
	electricCar = (J_EAEV)findFirst(zero_Interface.c_orderedVehicles, p -> p.getEAType() == OL_EnergyAssetType.ELECTRIC_VEHICLE && ((GridConnection)p.getOwner()).v_isActive && gcList.contains((GridConnection)p.getOwner()));
}
if ( electricCar != null ) {
	GridConnection gc = (GridConnection)electricCar.getOwner();
	
	J_ActivityTrackerTrips tripTracker = electricCar.getTripTracker();
	boolean available = true;
	available = electricCar.getAvailability();
	zero_Interface.c_orderedVehicles.remove(electricCar);
	electricCar.removeEnergyAsset();

	// Re-add petroleumFuel vehicle
	double energyConsumption_kWhpkm = zero_Interface.energyModel.avgc_data.p_avgGasolineConsumptionCar_kWhpkm;
	double vehicleScaling = 1.0;
	J_EAFuelVehicle petroleumFuelVehicle = new J_EAFuelVehicle(gc, energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeParameters, vehicleScaling, OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE, tripTracker, OL_EnergyCarriers.PETROLEUM_FUEL, available);
	
	zero_Interface.c_orderedVehicles.add(0, petroleumFuelVehicle);
	
	//check if was additional vehicle in companyUI, if so: add to collection
	if(foundAdditionalVehicle){
		zero_Interface.c_additionalVehicles.get(gc.p_uid).add(petroleumFuelVehicle);
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
	numberOfGhostVehicle_Vans = f_calculateNumberOfGhostVehicles(gcList).getMiddle();
}

int nbEVansCurrent = numberOfGhostVehicle_Vans;
int nbHydrogenVansCurrent = 0;
int nbPetroleumFuelVansCurrent = 0;
for ( GridConnection gc : gcList ) {
	nbEVansCurrent += count(gc.c_electricVehicles, p->p.getEAType() == OL_EnergyAssetType.ELECTRIC_VAN && !(gc instanceof GCPublicCharger) && gc.v_isActive);
	nbHydrogenVansCurrent += count(gc.c_hydrogenVehicles, p->p.getEAType() == OL_EnergyAssetType.HYDROGEN_VAN && gc.v_isActive);
	nbPetroleumFuelVansCurrent += count(gc.c_petroleumFuelVehicles, p->p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VAN && gc.v_isActive);
}

return Triple.of(nbEVansCurrent, nbHydrogenVansCurrent, nbPetroleumFuelVansCurrent);
/*ALCODEEND*/}

Triple<Integer, Integer, Integer> f_calculateCurrentNumberOfCars(List<GridConnection> gcList,Integer numberOfGhostVehicle_Cars)
{/*ALCODESTART::1749655667960*/
if (numberOfGhostVehicle_Cars == null) {
	numberOfGhostVehicle_Cars = f_calculateNumberOfGhostVehicles(gcList).getMiddle();
}

int nbEVsCurrent = numberOfGhostVehicle_Cars;
int nbHydrogenCarsCurrent = 0;
int nbPetroleumFuelCarsCurrent = 0;
for ( GridConnection gc : gcList ) {
	nbEVsCurrent += count(gc.c_electricVehicles, p->p.getEAType() == OL_EnergyAssetType.ELECTRIC_VEHICLE && !(gc instanceof GCPublicCharger) && gc.v_isActive);
	nbHydrogenCarsCurrent += count(gc.c_hydrogenVehicles, p->p.getEAType() == OL_EnergyAssetType.HYDROGEN_VEHICLE && gc.v_isActive);
	nbPetroleumFuelCarsCurrent += count(gc.c_petroleumFuelVehicles, p->p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE && gc.v_isActive);
}

return Triple.of(nbEVsCurrent, nbHydrogenCarsCurrent, nbPetroleumFuelCarsCurrent);
/*ALCODEEND*/}

double f_setPetroleumFuelVans(List<GridConnection> gcList,ShapeSlider sliderElectricVans,ShapeSlider sliderFossilFuelVans)
{/*ALCODESTART::1749655752858*/
double pctPetroleumFuelVansGoal = sliderFossilFuelVans.getValue();

int numberOfGhostVehicle_Vans = f_calculateNumberOfGhostVehicles(gcList).getMiddle();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfVans( gcList, numberOfGhostVehicle_Vans );

int nbEvanCurrent = triple.getLeft();
int nbPetroleumFuelvansCurrent = triple.getRight();

int total_vehicles = nbEvanCurrent + nbPetroleumFuelvansCurrent;

int nbPetroleumFuelVansGoal = roundToInt(total_vehicles*pctPetroleumFuelVansGoal/100.0);

if (nbPetroleumFuelvansCurrent < nbPetroleumFuelVansGoal) {
	// Add PetroleumFuel Vans
	while ( nbPetroleumFuelvansCurrent < nbPetroleumFuelVansGoal ) {
		if ( nbEvanCurrent > numberOfGhostVehicle_Vans ) {
			// replace electric van with a petroleumFuel van
			if (!f_electricToPetroleumFuelVan(gcList)) {
				f_setVanSlidersToCurrentEngineState(gcList, sliderElectricVans, null, sliderFossilFuelVans);
				return;
			}
			else {
				nbPetroleumFuelvansCurrent++;
				nbEvanCurrent--;
			}
		}
	}
}
else {
	// Remove PetroleumFuel Vans
	while ( nbPetroleumFuelvansCurrent > nbPetroleumFuelVansGoal ) {
		// replace a petroleumFuel van with an electric van
		if (!f_petroleumFuelToElectricVan(gcList)) {
			f_setVanSlidersToCurrentEngineState(gcList, sliderElectricVans, null, sliderFossilFuelVans);
			return;
		}
		nbPetroleumFuelvansCurrent--;
		nbEvanCurrent++;
	}
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setElectricVans(List<GridConnection> gcList,ShapeSlider sliderElectricVans,ShapeSlider sliderFossilFuelVans)
{/*ALCODESTART::1749656346356*/
double pctElectricVansGoal = sliderElectricVans.getValue();

int numberOfGhostVehicle_Vans = f_calculateNumberOfGhostVehicles(gcList).getMiddle();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfVans( gcList, numberOfGhostVehicle_Vans );

int nbEvanCurrent = triple.getLeft();
int nbPetroleumFuelvansCurrent = triple.getRight();

int total_vehicles = nbEvanCurrent + nbPetroleumFuelvansCurrent;

int nbElectricVansGoal = roundToInt(total_vehicles*pctElectricVansGoal/100.0);

if (nbEvanCurrent < nbElectricVansGoal) {
	// Add Electric Vans
	while ( nbEvanCurrent < nbElectricVansGoal ) {
		if ( nbPetroleumFuelvansCurrent > 0 ) {
			// replace a petroleumFuel van with an electric van
			if (!f_petroleumFuelToElectricVan(gcList)) {
				f_setVanSlidersToCurrentEngineState(gcList, sliderElectricVans, null, sliderFossilFuelVans);
				return;
			}
			else {
				nbEvanCurrent++;
				nbPetroleumFuelvansCurrent--;
			}
		}
	}
}
else {
	// Remove Electric Vans
	while ( nbEvanCurrent > nbElectricVansGoal ) {
		// replace an electric van with a petroleumFuel van
		if (!f_electricToPetroleumFuelVan(gcList)) {
			f_setVanSlidersToCurrentEngineState(gcList, sliderElectricVans, null, sliderFossilFuelVans);
			return;
		}
		nbEvanCurrent--;
		nbPetroleumFuelvansCurrent++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setPetroleumFuelCars(List<GridConnection> gcList,ShapeSlider sliderElectricCars,ShapeSlider sliderFossilFuelCars)
{/*ALCODESTART::1749717776391*/
double pctPetroleumFuelCarsGoal = sliderFossilFuelCars.getValue();

int numberOfGhostVehicle_Cars = f_calculateNumberOfGhostVehicles(gcList).getMiddle();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfCars( gcList, numberOfGhostVehicle_Cars );

int nbEcarCurrent = triple.getLeft();
int nbPetroleumFuelcarsCurrent = triple.getRight();

int total_vehicles = nbEcarCurrent + nbPetroleumFuelcarsCurrent;

int nbPetroleumFuelCarsGoal = roundToInt(total_vehicles*pctPetroleumFuelCarsGoal/100.0);

if (nbPetroleumFuelcarsCurrent < nbPetroleumFuelCarsGoal) {
	// Add PetroleumFuel Cars
	while ( nbPetroleumFuelcarsCurrent < nbPetroleumFuelCarsGoal ) {
		if ( nbEcarCurrent > numberOfGhostVehicle_Cars ) {
			// replace electric car with a petroleumFuel car
			if (!f_electricToPetroleumFuelCar(gcList)) {
				f_setCarSlidersToCurrentEngineState(gcList, sliderElectricCars, null, sliderFossilFuelCars);
				return;
			}
			else {
				nbPetroleumFuelcarsCurrent++;
				nbEcarCurrent--;
			}
		}
	}
}
else {
	// Remove PetroleumFuel Cars
	while ( nbPetroleumFuelcarsCurrent > nbPetroleumFuelCarsGoal ) {
		// replace a petroleumFuel car with an electric car
		if (!f_petroleumFuelToElectricCar(gcList)) {
			f_setCarSlidersToCurrentEngineState(gcList, sliderElectricCars, null, sliderFossilFuelCars);
			return;
		}
		nbPetroleumFuelcarsCurrent--;
		nbEcarCurrent++;
	}
}


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setElectricCars(List<GridConnection> gcList,ShapeSlider sliderElectricCars,ShapeSlider sliderFossilFuelCars)
{/*ALCODESTART::1749717779317*/
double pctElectricCarsGoal = sliderElectricCars.getValue();

int numberOfGhostVehicle_Cars = f_calculateNumberOfGhostVehicles(gcList).getMiddle();
Triple<Integer, Integer, Integer> triple = f_calculateCurrentNumberOfCars( gcList, numberOfGhostVehicle_Cars );

int nbEcarCurrent = triple.getLeft();
int nbPetroleumFuelcarsCurrent = triple.getRight();

int total_vehicles = nbEcarCurrent + nbPetroleumFuelcarsCurrent;

int nbElectricCarsGoal = roundToInt(total_vehicles*pctElectricCarsGoal/100.0);

if (nbEcarCurrent < nbElectricCarsGoal) {
	// Add Electric Cars
	while ( nbEcarCurrent < nbElectricCarsGoal ) {
		if ( nbPetroleumFuelcarsCurrent > 0 ) {
			// replace a petroleumFuel car with an electric car
			if (!f_petroleumFuelToElectricCar(gcList)) {
				f_setCarSlidersToCurrentEngineState(gcList, sliderElectricCars, null, sliderFossilFuelCars);
				return;
			}
			else {
				nbEcarCurrent++;
				nbPetroleumFuelcarsCurrent--;
			}
		}
	}
}
else {
	// Remove Electric Cars
	while ( nbEcarCurrent > nbElectricCarsGoal ) {
		// replace an electric car with a petroleumFuel car
		if (!f_electricToPetroleumFuelCar(gcList)) {
			f_setCarSlidersToCurrentEngineState(gcList, sliderElectricCars, null, sliderFossilFuelCars);
			return;
		}
		nbEcarCurrent--;
		nbPetroleumFuelcarsCurrent++;
	}
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setVanSlidersToCurrentEngineState(List<GridConnection> gcList,ShapeSlider sliderElectricVans,ShapeSlider sliderHydrogenVans,ShapeSlider sliderFossilFuelVans)
{/*ALCODESTART::1749717909708*/
List<J_EAEV> electricVehicles = new ArrayList<>();
List<J_EAFuelVehicle> petroleumVehicles = new ArrayList<>();
List<J_EAFuelVehicle> hydrogenVehicles = new ArrayList<>();
for (GridConnection gc : gcList) {
	if (gc.v_isActive) {
		electricVehicles.addAll(gc.c_electricVehicles);
		petroleumVehicles.addAll(gc.c_petroleumFuelVehicles);
		hydrogenVehicles.addAll(gc.c_hydrogenVehicles);
	}
}

int numberOfGhostVehicle_Vans = f_calculateNumberOfGhostVehicles(gcList).getRight();

int nbElectricVansCurrent = count(electricVehicles, p->p.getEAType() == OL_EnergyAssetType.ELECTRIC_VAN && !(p.getOwner() instanceof GCPublicCharger)) + numberOfGhostVehicle_Vans;
int nbPetroleumFuelvansCurrent = count(petroleumVehicles, p->p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VAN);
int nbHydrogenvansCurrent = count(hydrogenVehicles, p->p.getEAType() == OL_EnergyAssetType.HYDROGEN_VAN);

int totalVehicles = nbElectricVansCurrent + nbPetroleumFuelvansCurrent + nbHydrogenvansCurrent;



int pct_electricVanSlider = roundToInt(100.0*nbElectricVansCurrent/totalVehicles);
int pct_hydrogenVanSlider = roundToInt(100.0*nbHydrogenvansCurrent/totalVehicles);
int pct_petroleumFuelVanSlider = roundToInt(100.0*nbPetroleumFuelvansCurrent/totalVehicles);

sliderElectricVans.setValue(pct_electricVanSlider, false);
if ( sliderHydrogenVans != null ) {
	sliderHydrogenVans.setValue(pct_hydrogenVanSlider, false);
}
else if ( sliderHydrogenVans == null && pct_hydrogenVanSlider != 0 ) {
	throw new RuntimeException("Hydrogen vans found but no hydrogen slider passed to f_setVanSlidersToCurrentEngineState");
}
sliderFossilFuelVans.setValue(pct_petroleumFuelVanSlider, false);


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setCarSlidersToCurrentEngineState(List<GridConnection> gcList,ShapeSlider sliderElectricCars,ShapeSlider sliderHydrogenCars,ShapeSlider sliderFossilFuelCars)
{/*ALCODESTART::1749717987139*/
List<J_EAEV> electricVehicles = new ArrayList<>();
List<J_EAFuelVehicle> petroleumVehicles = new ArrayList<>();
List<J_EAFuelVehicle> hydrogenVehicles = new ArrayList<>();
for (GridConnection gc : gcList) {
	if (gc.v_isActive) {
		electricVehicles.addAll(gc.c_electricVehicles);
		petroleumVehicles.addAll(gc.c_petroleumFuelVehicles);
		hydrogenVehicles.addAll(gc.c_hydrogenVehicles);
	}
}

int numberOfGhostVehicle_Cars = f_calculateNumberOfGhostVehicles(gcList).getRight();

int nbElectricCarsCurrent = count(electricVehicles, p->p.getEAType() == OL_EnergyAssetType.ELECTRIC_VEHICLE && !(p.getOwner() instanceof GCPublicCharger)) + numberOfGhostVehicle_Cars;
int nbPetroleumFuelcarsCurrent = count(petroleumVehicles, p->p.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE);
int nbHydrogencarsCurrent = count(hydrogenVehicles, p->p.getEAType() == OL_EnergyAssetType.HYDROGEN_VEHICLE);

int totalVehicles = nbElectricCarsCurrent + nbPetroleumFuelcarsCurrent + nbHydrogencarsCurrent;



int pct_electricCarSlider = roundToInt(100.0*nbElectricCarsCurrent/totalVehicles);
int pct_hydrogenCarSlider = roundToInt(100.0*nbHydrogencarsCurrent/totalVehicles);
int pct_petroleumFuelCarSlider = roundToInt(100.0*nbPetroleumFuelcarsCurrent/totalVehicles);

sliderElectricCars.setValue(pct_electricCarSlider, false);
if ( sliderHydrogenCars != null ) {
	sliderHydrogenCars.setValue(pct_hydrogenCarSlider, false);
}
else if ( sliderHydrogenCars == null && pct_hydrogenCarSlider != 0 ) {
	throw new RuntimeException("Hydrogen cars found but no hydrogen slider passed to f_setCarSlidersToCurrentEngineState");
}
sliderFossilFuelCars.setValue(pct_petroleumFuelCarSlider, false);


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}
zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_updateSliders_Mobility()
{/*ALCODESTART::1754928402690*/
Triple<Integer, Integer, Integer> triple = f_calculateNumberOfGhostVehicles( new ArrayList<GridConnection>(uI_Tabs.f_getActiveSliderGridConnections_utilities()) );
v_totalNumberOfGhostVehicle_Cars = triple.getLeft();
v_totalNumberOfGhostVehicle_Vans = triple.getMiddle();
v_totalNumberOfGhostVehicle_Trucks = triple.getRight();


if(gr_mobilitySliders_default.isVisible()){
	f_updateMobilitySliders_default();
}
else if(gr_mobilitySliders_residential.isVisible()){
	f_updateMobilitySliders_residential();
}
else{
	f_updateMobilitySliders_custom();
}
/*ALCODEEND*/}

double f_updateMobilitySliders_default()
{/*ALCODESTART::1754928402694*/
List<GridConnection> allConsumerGridConnections = uI_Tabs.f_getActiveSliderGridConnections_consumption();

////Savings
double totalBaseTravelDistance_km = 0;
double totalSavedTravelDistance_km = 0;
for(GridConnection GC : allConsumerGridConnections){
	if(GC.v_isActive){
		for(J_ActivityTrackerTrips tripTracker : GC.c_tripTrackers){
			totalBaseTravelDistance_km += tripTracker.getAnnualDistance_km();
			totalSavedTravelDistance_km += (1-tripTracker.getDistanceScaling_fr())*tripTracker.getAnnualDistance_km();
		}
	}
}

double mobilitySavings_pct = totalBaseTravelDistance_km > 0 ? (totalSavedTravelDistance_km/totalBaseTravelDistance_km * 100) : 0;
sl_mobilityDemandReduction_pct.setValue(roundToInt(mobilitySavings_pct), false);


//Smart charging
boolean smartCharging = false;
for(GridConnection GC : allConsumerGridConnections){
	if(GC.c_electricVehicles.size() > 0 && GC.f_getCurrentChargingType() != OL_ChargingAttitude.SIMPLE){
		smartCharging = true;
		break;
	}
}
cb_spreadChargingEVs.setSelected(smartCharging, false);


////Vehicles
// Initialize the vehicle counters
int PetroleumFuelCars = 0;
int ElectricCars = v_totalNumberOfGhostVehicle_Cars;
int HydrogenCars = 0;

int PetroleumFuelVans = 0;
int ElectricVans = v_totalNumberOfGhostVehicle_Vans;
int HydrogenVans = 0;

int PetroleumFuelTrucks = 0;
int ElectricTrucks = v_totalNumberOfGhostVehicle_Trucks;
int HydrogenTrucks = 0;

//Count the amount of vehicles for each type
for (GridConnection gc : allConsumerGridConnections) {
	if(gc.v_isActive){
		for (J_EAFuelVehicle vehicle : gc.c_petroleumFuelVehicles) {
			switch(vehicle.getEAType()){
		 		case PETROLEUM_FUEL_VEHICLE:
					PetroleumFuelCars += 1;
				break;
				case PETROLEUM_FUEL_VAN:
					PetroleumFuelVans += 1;
				break;
				case PETROLEUM_FUEL_TRUCK:
					PetroleumFuelTrucks += 1;
				break;
			}
		}
		for (J_EAEV vehicle : gc.c_electricVehicles) {
		 	switch(vehicle.getEAType()){
		 		case ELECTRIC_VEHICLE:
					ElectricCars += 1;
				break;
				case ELECTRIC_VAN:
					ElectricVans += 1;
				break;
				case ELECTRIC_TRUCK:
					ElectricTrucks += 1;
				break;
			}
		}
		for (J_EAFuelVehicle vehicle : gc.c_hydrogenVehicles) {
			switch(vehicle.getEAType()){
		 		case HYDROGEN_VEHICLE:
					HydrogenCars += 1;
				break;
				case HYDROGEN_VAN:
					HydrogenVans += 1;
				break;
				case HYDROGEN_TRUCK:
					HydrogenTrucks += 1;
				break;
			}
		}
	}
}


//Set CAR sliders
int totalCars = PetroleumFuelCars + ElectricCars + HydrogenCars;
int PetroleumFuelCars_pct = 0;
int ElectricCars_pct = 0;
int HydrogenCars_pct = 0;
if (totalCars != 0) {
	PetroleumFuelCars_pct = roundToInt((100.0 * PetroleumFuelCars) / totalCars);
	ElectricCars_pct = roundToInt((100.0 * ElectricCars) / totalCars);
	HydrogenCars_pct = roundToInt((100.0 * HydrogenCars) / totalCars);
}
else{
	sl_fossilFuelCars_pct.setEnabled(false);
	sl_electricCars_pct.setEnabled(false);
}
sl_fossilFuelCars_pct.setValue(PetroleumFuelCars_pct, false);
sl_electricCars_pct.setValue(ElectricCars_pct, false);


//Set VAN sliders
int totalVans = PetroleumFuelVans + ElectricVans + HydrogenVans;
int PetroleumFuelVans_pct = 0;
int ElectricVans_pct = 0;
int HydrogenVans_pct = 0;
if (totalVans != 0) {
	PetroleumFuelVans_pct = roundToInt(100.0 * PetroleumFuelVans / totalVans);
	ElectricVans_pct = roundToInt(100.0 * ElectricVans / totalVans);
	HydrogenVans_pct = roundToInt(100.0 * HydrogenVans / totalVans);
}
else{
	sl_fossilFuelVans_pct.setEnabled(false);
	sl_electricVans_pct.setEnabled(false);
}
sl_fossilFuelVans_pct.setValue(PetroleumFuelVans_pct, false);
sl_electricVans_pct.setValue(ElectricVans_pct, false);


//Set TRUCK sliders
int totalTrucks = PetroleumFuelTrucks + ElectricTrucks + HydrogenTrucks;
int PetroleumFuelTrucks_pct = 0;
int ElectricTrucks_pct = 0;
int HydrogenTrucks_pct = 0;
if (totalTrucks != 0) {
	PetroleumFuelTrucks_pct = roundToInt(100.0 * PetroleumFuelTrucks / totalTrucks);
	ElectricTrucks_pct = roundToInt(100.0 * ElectricTrucks / totalTrucks);
	HydrogenTrucks_pct = roundToInt(100.0 * HydrogenTrucks / totalTrucks);
}
else{
	sl_fossilFuelTrucks_pct.setEnabled(false);
	sl_electricTrucks_pct.setEnabled(false);
	sl_hydrogenTrucks_pct.setEnabled(false);
}
sl_fossilFuelTrucks_pct.setValue(PetroleumFuelTrucks_pct, false);
sl_electricTrucks_pct.setValue(ElectricTrucks_pct, false);
sl_hydrogenTrucks_pct.setValue(HydrogenTrucks_pct, false);
/*ALCODEEND*/}

double f_updateMobilitySliders_custom()
{/*ALCODESTART::1754928402700*/
//If you have a custom tab, 
//override this function to make it update automatically
traceln("Forgot to override the update custom electricity sliders functionality");
/*ALCODEEND*/}

double f_setChargingAttitude(OL_ChargingAttitude selectedChargingAttitude,List<GridConnection> gcList)
{/*ALCODESTART::1754990674760*/
gcList.forEach(x -> x.f_addChargingManagement(selectedChargingAttitude));


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_updateMobilitySliders_residential()
{/*ALCODESTART::1758183013077*/
////Private EV
gr_activateV2GPrivateParkedCars.setVisible(false);
cb_activateV2GPrivateParkedCars.setSelected(false, false);
gr_settingsV2G_privateParkedCars.setVisible(false);

List<GCHouse> houseGridConnectionsWithPrivateParking = findAll(uI_Tabs.f_getActiveSliderGridConnections_houses(), house -> house.p_eigenOprit);
List<I_Vehicle> privateParkedCars = new ArrayList<>();
houseGridConnectionsWithPrivateParking.forEach(gc -> privateParkedCars.addAll(gc.c_vehicleAssets));

if (privateParkedCars.size() > 0) {
	int nbPrivateEVs = count(privateParkedCars, x -> x instanceof J_EAEV);
	int nbPrivateEVsThatSupportV2G = count(privateParkedCars, x -> x instanceof J_EAEV && ((J_EAEV)x).getV2GCapable());
	double privateEVs_pct = 100.0 * nbPrivateEVs / privateParkedCars.size();
	double privateEVsThatSupportV2G_pct = 100.0 * nbPrivateEVsThatSupportV2G / nbPrivateEVs;
	sl_privateEVsResidentialArea_pct.setValue(roundToInt(privateEVs_pct), false);
	sl_EVsThatSupportV2G_pct.setValue(roundToInt(privateEVsThatSupportV2G_pct), false);
	
	//Selected charging mode
	GCHouse GCWithPrivateParkedEV = findFirst(houseGridConnectionsWithPrivateParking, gc -> gc.c_electricVehicles.size() > 0);
	OL_ChargingAttitude currentChargingAttitude = (GCWithPrivateParkedEV != null) ? GCWithPrivateParkedEV.f_getCurrentChargingType() : OL_ChargingAttitude.SIMPLE;
	boolean V2GActive = (GCWithPrivateParkedEV != null) ? GCWithPrivateParkedEV.f_getV2GActive() : false;
	for(GridConnection GC : houseGridConnectionsWithPrivateParking){
		if(GC.c_electricVehicles.size() > 0 ){
			if(currentChargingAttitude != OL_ChargingAttitude.CUSTOM && GC.f_getCurrentChargingType() != currentChargingAttitude){
				currentChargingAttitude = OL_ChargingAttitude.CUSTOM; // Here used as varied: in other words: custom setting
			}
			if(V2GActive && !GC.f_getV2GActive()){
				V2GActive = false;
			}
		}
		
		if(currentChargingAttitude == OL_ChargingAttitude.CUSTOM && !V2GActive){
			break;
		}
		
	}
	
	String selectedChargingAttitudeString = "";
	switch(currentChargingAttitude){
		case SIMPLE:
			selectedChargingAttitudeString = "Niet slim laden";
			break;
		case PRICE:
			selectedChargingAttitudeString = "Slim laden: Prijs gestuurd";
			gr_activateV2GPrivateParkedCars.setVisible(true);
			break;
		case BALANCE_LOCAL:
			selectedChargingAttitudeString = "Slim laden: Netbewust";
			gr_activateV2GPrivateParkedCars.setVisible(true);
			break;
		case CUSTOM:
			selectedChargingAttitudeString = "Gevarieerd";
			break;
	}
	
	cb_chargingAttitudePrivateParkedCars.setValue(selectedChargingAttitudeString, false);
	cb_activateV2GPrivateParkedCars.setSelected(V2GActive, false);
	
	if(gr_activateV2GPrivateParkedCars.isVisible() && V2GActive){
		gr_settingsV2G_privateParkedCars.setVisible(true);
	}
}
else{
	sl_privateEVsResidentialArea_pct.setEnabled(false);
}

////Chargers
OL_ChargingAttitude selectedChargingAttitude = null;
gr_activateV2GPublicChargers.setVisible(false);
cb_activateV2GPublicChargers.setSelected(false, false);
gr_settingsV1G_publicChargers.setVisible(false);
gr_settingsV2G_publicChargers.setVisible(false);

List<GCPublicCharger> activeChargerGridConnections = uI_Tabs.f_getSliderGridConnections_chargers();
List<GCPublicCharger> pausedChargerGridConnections = uI_Tabs.f_getPausedSliderGridConnections_chargers();


int nbPublicChargerGC = activeChargerGridConnections.size() + pausedChargerGridConnections.size();

if(nbPublicChargerGC > 0 ){
	int nbActivePublicChargersGC = activeChargerGridConnections.size();
	double activePublicChargers_pct = 100.0 * nbActivePublicChargersGC / nbPublicChargerGC;
	sl_publicChargersResidentialArea_pct.setValue(roundToInt(activePublicChargers_pct), false);
	
	int nbV1GChargers = count(activeChargerGridConnections, x -> x.f_getChargePoint().getV1GCapable());
	int nbV2GChargers =count(activeChargerGridConnections, x -> x.f_getChargePoint().getV2GCapable());
	int nbPublicChargers = activeChargerGridConnections.size();
		
	double V1G_pct = 100.0 * nbV1GChargers / nbPublicChargers;
	double V2G_pct = 100.0 * nbV2GChargers / nbPublicChargers;
	sl_chargersThatSupportV1G_pct.setValue(roundToInt(V1G_pct), false);
	sl_chargersThatSupportV2G_pct.setValue(roundToInt(V2G_pct), false);
	
	//Selected charging mode
	OL_ChargingAttitude currentChargingAttitude = activeChargerGridConnections.size() > 0 ? activeChargerGridConnections.get(0).f_getCurrentChargingType(): OL_ChargingAttitude.SIMPLE;
	boolean V2GActive = activeChargerGridConnections.size() > 0 ? activeChargerGridConnections.get(0).f_getChargingManagement().getV2GActive(): false;
	for(GCPublicCharger charger : activeChargerGridConnections){
		if(currentChargingAttitude != OL_ChargingAttitude.CUSTOM && charger.f_getCurrentChargingType() != currentChargingAttitude){
			currentChargingAttitude = OL_ChargingAttitude.CUSTOM; // Here used as varied: in other words: custom setting
		}
		if(V2GActive && !charger.f_getChargingManagement().getV2GActive()){
			V2GActive = false;
		}
		
		if(currentChargingAttitude == OL_ChargingAttitude.CUSTOM && !V2GActive){
			break;
		}
	}
	
	String selectedChargingAttitudeString = "";
	switch(currentChargingAttitude){
		case SIMPLE:
			selectedChargingAttitudeString = "Niet slim laden";
			break;
		case PRICE:
			selectedChargingAttitudeString = "Slim laden: Prijs gestuurd";
			gr_settingsV1G_publicChargers.setVisible(true);
			gr_activateV2GPublicChargers.setVisible(true);
			break;
		case BALANCE_GRID:
			selectedChargingAttitudeString = "Slim laden: Netbewust";
			gr_settingsV1G_publicChargers.setVisible(true);
			gr_activateV2GPublicChargers.setVisible(true);
			break;
		case CUSTOM:
			selectedChargingAttitudeString = "Gevarieerd";
			break;
	}
	
	cb_chargingAttitudePrivatePublicChargers.setValue(selectedChargingAttitudeString, false);
	cb_activateV2GPublicChargers.setSelected(V2GActive, false);
	
	if(gr_activateV2GPublicChargers.isVisible() && V2GActive){
		gr_settingsV2G_publicChargers.setVisible(true);
	}
}
else{
	sl_publicChargersResidentialArea_pct.setEnabled(false);
}
/*ALCODEEND*/}

double f_setPublicChargingStations(List<GCPublicCharger> gcListChargers,double publicChargers_pct,ShapeSlider V1GCapableChargerSlider,ShapeSlider V2GCapableChargerSlider)
{/*ALCODESTART::1758183975214*/
int totalNbChargers = gcListChargers.size();
int currentNbChargers = count(gcListChargers, x -> x.v_isActive);
int nbChargersGoal = roundToInt(publicChargers_pct / 100 * totalNbChargers) ;

while ( currentNbChargers > nbChargersGoal ) {
	GCPublicCharger gc = findFirst(zero_Interface.c_orderedPublicChargers, x -> x.v_isActive);
	if (gc != null) {
		gc.f_setActive(false, zero_Interface.energyModel.p_timeVariables);
		zero_Interface.c_orderedPublicChargers.remove(gc);
		zero_Interface.c_orderedPublicChargers.add(0, gc);
		currentNbChargers--;
		
		for (J_EAFuelVehicle car : zero_Interface.c_mappingOfVehiclesPerCharger.get(gc.p_uid)) {
			car.reRegisterEnergyAsset(zero_Interface.energyModel.p_timeParameters);
		}
	}
	else {
		throw new RuntimeException("Charger slider error: there are not sufficient chargers to remove");
	}
}
while ( currentNbChargers < nbChargersGoal){
	GCPublicCharger gc = findFirst(zero_Interface.c_orderedPublicChargers, x -> !x.v_isActive);
	if (gc != null) {
		gc.f_setActive(true, zero_Interface.energyModel.p_timeVariables);
		zero_Interface.c_orderedPublicChargers.remove(gc);
		zero_Interface.c_orderedPublicChargers.add(0, gc);
		currentNbChargers++;
		
		for (J_EAFuelVehicle car : zero_Interface.c_mappingOfVehiclesPerCharger.get(gc.p_uid)) {
			J_ActivityTrackerTrips tripTracker = car.getTripTracker(); //Needed, as triptracker is removed when removeEnergyAsset is called.
			car.removeEnergyAsset();
			car.setTripTracker(tripTracker);//Re-set the triptracker again, for storing.
		}
	}
	else {
		throw new RuntimeException("Charger slider error: there are no more chargers to add");
	}
}

//Update the V1G and V2G capable vehicle slider accordingly to the change in vehicle dynamics
int totalActiveChargers = 0;
int totalCapableV1GChargers = 0;
int totalCapableV2GChargers = 0;

for(GCPublicCharger GC : gcListChargers){
	if(GC.v_isActive){
		totalActiveChargers++;
		if(GC.f_getChargePoint().getV1GCapable()){
			totalCapableV1GChargers++;
		}
		if(GC.f_getChargePoint().getV2GCapable()){
			totalCapableV2GChargers++;			
		}	
	}
}
V1GCapableChargerSlider.setValue(roundToInt(100.0 * totalCapableV1GChargers/totalActiveChargers));
V2GCapableChargerSlider.setValue(roundToInt(100.0 * totalCapableV2GChargers/totalActiveChargers));


//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();

/*ALCODEEND*/}

double f_setV1GChargerCapabilities(List<GCPublicCharger> gcListChargers,double goal_pct)
{/*ALCODESTART::1758183975221*/
int totalNbChargers = gcListChargers.size();
int currentNbChargers = count(gcListChargers, x -> x.f_getChargePoint().getV1GCapable());
int nbChargersGoal = roundToInt(goal_pct / 100.0 * totalNbChargers);

while (currentNbChargers < nbChargersGoal) {
	GCPublicCharger charger = findFirst(zero_Interface.c_orderedV1GChargers, x -> gcListChargers.contains(x) && !x.f_getChargePoint().getV1GCapable());
	charger.f_getChargePoint().setV1GCapability(true);
	currentNbChargers++;
	zero_Interface.c_orderedV1GChargers.remove(charger);
	zero_Interface.c_orderedV1GChargers.add(0, charger);
	
}
while (currentNbChargers > nbChargersGoal) {
	GCPublicCharger charger = findFirst(zero_Interface.c_orderedV1GChargers, x -> gcListChargers.contains(x) && x.f_getChargePoint().getV1GCapable());
	charger.f_getChargePoint().setV1GCapability(false);
	currentNbChargers--;
	zero_Interface.c_orderedV1GChargers.remove(charger);
	zero_Interface.c_orderedV1GChargers.add(0, charger);
}

// Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setV2GChargerCapabilities(List<GCPublicCharger> gcListChargers,double goal_pct)
{/*ALCODESTART::1758183975227*/
int totalNbChargers = gcListChargers.size();
int currentNbChargers = count(gcListChargers, x -> x.f_getChargePoint().getV2GCapable());
int nbChargersGoal = roundToInt(goal_pct / 100.0 * totalNbChargers);

while (currentNbChargers < nbChargersGoal) {
	GCPublicCharger charger = findFirst(zero_Interface.c_orderedV2GChargers, x -> gcListChargers.contains(x) && !x.f_getChargePoint().getV2GCapable());
	charger.f_getChargePoint().setV2GCapability(true);
	currentNbChargers++;
	zero_Interface.c_orderedV2GChargers.remove(charger);
	zero_Interface.c_orderedV2GChargers.add(0, charger);
	
}
while (currentNbChargers > nbChargersGoal) {
	GCPublicCharger charger = findFirst(zero_Interface.c_orderedV2GChargers, x -> gcListChargers.contains(x) && x.f_getChargePoint().getV2GCapable());
	charger.f_getChargePoint().setV2GCapability(false);
	currentNbChargers--;
	zero_Interface.c_orderedV2GChargers.remove(charger);
	zero_Interface.c_orderedV2GChargers.add(0, charger);
}

// Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setVehiclesPrivateParking(List<GCHouse> gcListHouses,double privateParkingEV_pct,ShapeSlider V2GCapableVehicleSlider)
{/*ALCODESTART::1758183975234*/
//Voor nu zo opgelost, echter gaat dit niet goed werken met de volgorde. BEDENK EEN BETER MANIER!?
List<I_Vehicle> gcListOrderedVehiclesPrivateParking = findAll( zero_Interface.c_orderedVehiclesPrivateParking, h -> gcListHouses.contains(((J_EA)h).getOwner()));

int nbOfPrivateParkedEV = (int)sum(findAll(gcListHouses, gc -> gc.p_eigenOprit), x -> x.c_electricVehicles.size());
int desiredNbOfPrivateParkedEV = roundToInt(privateParkingEV_pct / 100 * gcListOrderedVehiclesPrivateParking.size());


// we scale the consumption instead of getting the petroleumFuel/EV parameter from avgc data to represent the 'size' of the car
double ratioEVToPetroleumFuelConsumption = zero_Interface.energyModel.avgc_data.p_avgEVEnergyConsumptionCar_kWhpkm / zero_Interface.energyModel.avgc_data.p_avgGasolineConsumptionCar_kWhpkm;

while ( nbOfPrivateParkedEV > desiredNbOfPrivateParkedEV){
	J_EAEV j_ea = (J_EAEV) findFirst( gcListOrderedVehiclesPrivateParking, h -> h instanceof J_EAEV);
	if (j_ea.getVehicleScaling_fr() != 1) {
		throw new RuntimeException("f_setVehiclesPrivateParking does not work with vehicles that have a vehicleScaling other than 1");
	}
	J_ActivityTrackerTrips triptracker = j_ea.getTripTracker();
	boolean availability = j_ea.getAvailability();
	double energyConsumption_kWhpkm = j_ea.getEnergyConsumption_kWhpkm() / ratioEVToPetroleumFuelConsumption; 
	j_ea.removeEnergyAsset();
	gcListOrderedVehiclesPrivateParking.remove(j_ea);
	zero_Interface.c_orderedVehiclesPrivateParking.remove(j_ea);
	J_EAFuelVehicle petroleumFuelCar = new J_EAFuelVehicle(j_ea.getOwner(), energyConsumption_kWhpkm, zero_Interface.energyModel.p_timeParameters, 1, OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE, triptracker, OL_EnergyCarriers.PETROLEUM_FUEL, availability);
	gcListOrderedVehiclesPrivateParking.add(petroleumFuelCar);
	zero_Interface.c_orderedVehiclesPrivateParking.add(petroleumFuelCar);
	nbOfPrivateParkedEV --;
}
while ( nbOfPrivateParkedEV < desiredNbOfPrivateParkedEV){
	J_EAFuelVehicle petroleumFuelVehicle = (J_EAFuelVehicle) findFirst( gcListOrderedVehiclesPrivateParking, h -> h.getEAType() == OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE);
	if (petroleumFuelVehicle.getVehicleScaling_fr() != 1) {
		throw new RuntimeException("f_setVehiclesPrivateParking does not work with vehicles that have a vehicleScaling other than 1");
	}
	J_ActivityTrackerTrips triptracker = petroleumFuelVehicle.getTripTracker();
	boolean availability = petroleumFuelVehicle.getAvailability();
	double energyConsumption_kWhpkm = petroleumFuelVehicle.getEnergyConsumption_kWhpkm() * ratioEVToPetroleumFuelConsumption;
	petroleumFuelVehicle.removeEnergyAsset();
	gcListOrderedVehiclesPrivateParking.remove(petroleumFuelVehicle);
	zero_Interface.c_orderedVehiclesPrivateParking.remove(petroleumFuelVehicle);
	double capacityElectricity_kW = randomTrue(0.6) ? randomTrue(0.4) ? 3.2 : 5.6 : 11.0;
	double storageCapacity_kWh = uniform_discr(65,90);
	J_EAEV ev = new J_EAEV(petroleumFuelVehicle.getOwner(), capacityElectricity_kW, storageCapacity_kWh, 1, zero_Interface.energyModel.p_timeParameters, energyConsumption_kWhpkm, 1, OL_EnergyAssetType.ELECTRIC_VEHICLE, triptracker, availability);	
	gcListOrderedVehiclesPrivateParking.add(ev);
	zero_Interface.c_orderedVehiclesPrivateParking.add(ev);
	nbOfPrivateParkedEV++;
}


//Update the V2G capable vehicle slider accordingly to the change in vehicle dynamics
int totalCapableV2GEVs = count(gcListOrderedVehiclesPrivateParking, vehicle -> vehicle instanceof J_EAEV && ((J_EAEV)vehicle).getV2GCapable());
V2GCapableVehicleSlider.setValue(roundToInt(100.0*totalCapableV2GEVs/nbOfPrivateParkedEV));

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_setV2GEVCapabilities(List<GCHouse> gcListHouses,double goal_pct)
{/*ALCODESTART::1758275653317*/
//Voor nu zo werkend gemaakt met gclist als input, echter gaat dit niet goed werken mochten er minder gcs tussen zitten dan in de zero_interface ordered collectie staat en gaat de volgorde veranderen. 
// -> BEDENK EEN BETER MANIER!? --> Bestaat er uberhaupt een manier voor? 

List<I_Vehicle> gcListOrderedVehiclesPrivateParking_all = findAll( zero_Interface.c_orderedVehiclesPrivateParking, vehicle -> gcListHouses.contains(((J_EA)vehicle).getOwner()) && vehicle instanceof J_EAEV && ((J_EAEV)vehicle).getEAType() == OL_EnergyAssetType.ELECTRIC_VEHICLE);

List<J_EAEV> gcListOrderedVehiclesPrivateParking = gcListOrderedVehiclesPrivateParking_all.stream().map(v -> (J_EAEV) v).collect(Collectors.toList());

int totalNbEVs = gcListOrderedVehiclesPrivateParking.size();
int currentNbEVsV2GCapable = count(gcListOrderedVehiclesPrivateParking, x -> x.getV2GCapable());
int nbEVsV2GCapableGoal = roundToInt(goal_pct / 100.0 * totalNbEVs);

while (currentNbEVsV2GCapable < nbEVsV2GCapableGoal) {
	J_EAEV j_ea = findFirst(gcListOrderedVehiclesPrivateParking, x -> x.getEAType() == OL_EnergyAssetType.ELECTRIC_VEHICLE && !x.getV2GCapable());
	j_ea.setV2GCapable(true);
	currentNbEVsV2GCapable++;
	gcListOrderedVehiclesPrivateParking.remove(j_ea);
	gcListOrderedVehiclesPrivateParking.add(0, j_ea);
	
}
while (currentNbEVsV2GCapable > nbEVsV2GCapableGoal) {
	J_EAEV j_ea = findFirst(gcListOrderedVehiclesPrivateParking, x -> x.getEAType() == OL_EnergyAssetType.ELECTRIC_VEHICLE && x.getV2GCapable());
	j_ea.setV2GCapable(false);
	currentNbEVsV2GCapable--;
	gcListOrderedVehiclesPrivateParking.remove(j_ea);
	gcListOrderedVehiclesPrivateParking.add(0, j_ea);
}

// Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_activateV2G(List<GridConnection> gcList,boolean activateV2G)
{/*ALCODESTART::1758276936913*/
for(GridConnection GC : gcList){
	GC.f_activateV2GChargingMode(activateV2G, zero_Interface.energyModel.p_timeParameters, zero_Interface.energyModel.p_timeVariables);
}

//Update variable to change to custom scenario
if(!zero_Interface.b_runningMainInterfaceScenarios){
	zero_Interface.f_setScenarioToCustom();
}

zero_Interface.f_resetSettings();
/*ALCODEEND*/}

double f_initializeTab_Mobility()
{/*ALCODESTART::1764004906148*/
//Use this function to initialize mobility tab settings at start of simulation
/*ALCODEEND*/}

