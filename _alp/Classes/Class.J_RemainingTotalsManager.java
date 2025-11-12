/**
 * J_RemainingTotalsManager
 */	
public class J_RemainingTotalsManager {
	
	//Supported EC and vehicle types	
	List<OL_EnergyCarriers> supportedEnergyCarriers= new ArrayList<>(Arrays.asList(OL_EnergyCarriers.ELECTRICITY, 
																				   OL_EnergyCarriers.METHANE));
	List<OL_VehicleType> supportedVehicleTypes = new ArrayList<>(Arrays.asList(OL_VehicleType.CAR, 
																			   OL_VehicleType.VAN, 
																			   OL_VehicleType.TRUCK));
	
	//Original Model total
	private final String originalModelTotalName = "Original model total";
	
	//Remaining Model total (used as backup for gc that are not in an area)
	private final String remainingModelTotalName = "Remaining model total";	
	
	//Initialize the remainingTotalsMap
	private Map<String, J_RemainingTotals> remainingTotalsMap = new HashMap<>();
	
	//Class State
	private boolean isInitialized = false;
	private boolean isFinalized = false;
	
	//Total value is added total or manual input
	private boolean totalElectricityConsumptionCompaniesTotalIsManualInput = false;
	private boolean totalGasConsumptionCompaniesTotalIsManualInput = false;
	private boolean totalCarsCompaniesTotalIsManualInput = false;
	private boolean totalVansCompaniesTotalIsManualInput = false;
	private boolean totalTrucksCompaniesTotalIsManualInput = false;
	
	//TEMPORARY SOLUTION FOR NOW TO CONVERT GAS IN M3, UNTIL LOMBOK PACKAGE IS IN KWH OR OTHER SOLUTION
	private AVGC_data avgc_data;
	
    /**
     * Default constructor
     */
    public J_RemainingTotalsManager(AVGC_data avgc_data) {
    	this.avgc_data = avgc_data;
    }
    
    //Initialize model totals
    public void initializeModelTotals(Project_data project_data) {
    	double avg_house_elec_delivery_kwh_p_yr = 0;
    	double avg_house_gas_delivery_m3_p_yr = 0;
    	double avg_number_of_cars_per_house = 0;
    	double total_comp_elec_delivery_kwh_p_yr = 0;
    	double total_comp_gas_delivery_m3_p_yr = 0;
    	int total_nr_comp_cars = 0;
    	int total_nr_comp_vans = 0;
    	int total_nr_comp_trucks = 0;
    	
    	//Energy totals
    	if(project_data.total_electricity_consumption_companies_kWh_p_yr() != null && project_data.total_electricity_consumption_companies_kWh_p_yr() > 0){
    		total_comp_elec_delivery_kwh_p_yr = project_data.total_electricity_consumption_companies_kWh_p_yr();
    		this.totalElectricityConsumptionCompaniesTotalIsManualInput = true;
    	}
    	if(project_data.total_gas_consumption_companies_m3_p_yr() != null && project_data.total_gas_consumption_companies_m3_p_yr() > 0){	
    		total_comp_gas_delivery_m3_p_yr = project_data.total_gas_consumption_companies_m3_p_yr();
    		this.totalGasConsumptionCompaniesTotalIsManualInput = true;
    	}
    	if(project_data.total_cars_companies() != null && project_data.total_cars_companies() > 0){	
    		total_nr_comp_cars = project_data.total_cars_companies();
    		this.totalCarsCompaniesTotalIsManualInput = true;
    	}
    	if(project_data.total_vans_companies() != null && project_data.total_vans_companies() > 0){	
    		total_nr_comp_vans = project_data.total_vans_companies();
    		this.totalVansCompaniesTotalIsManualInput = true;
    	}
    	if(project_data.total_trucks_companies() != null && project_data.total_trucks_companies() > 0){	
    		total_nr_comp_trucks = project_data.total_trucks_companies();
    		this.totalTrucksCompaniesTotalIsManualInput = true;
    	}

    	//Initialize default remaining model total instances
    	List<String> defaultRemainingModelTotalNames = new ArrayList<>(List.of(this.originalModelTotalName, this.remainingModelTotalName));
    	for(String defaultModelTotalInstance : defaultRemainingModelTotalNames) {
        	remainingTotalsMap.put(defaultModelTotalInstance, new J_RemainingTotals(
	    	Neighbourhood_data.builder()
	    	.districtname(defaultModelTotalInstance)
	    	//Energy totals
	    	.avg_house_elec_delivery_kwh_p_yr(avg_house_elec_delivery_kwh_p_yr)
	    	.avg_house_gas_delivery_m3_p_yr(avg_house_gas_delivery_m3_p_yr)
	    	.avg_number_of_cars_per_house(avg_number_of_cars_per_house)
	    	.total_comp_elec_delivery_kwh_p_yr(total_comp_elec_delivery_kwh_p_yr)
	    	.total_comp_gas_delivery_m3_p_yr(total_comp_gas_delivery_m3_p_yr)
	    	.total_nr_comp_cars(total_nr_comp_cars)
	    	.total_nr_comp_vans(total_nr_comp_vans)
	    	.total_nr_comp_trucks(total_nr_comp_trucks)
	    	.build(), 
	    	this.avgc_data
        	));
    	}
    	this.isInitialized = true;
    }
    

    //Initialize remaining totals for each NBH
    public void addNBH(Neighbourhood_data dataNBH) {
    	if(!isInitialized) {
    		throw new RuntimeException("Adding NBH to J_RemainingTotalsManager, while the class has not been initialized, this will cause energy total mismatches and therefor is not allowed!");
    	}
    	if(remainingTotalsMap.containsKey(dataNBH.districtname())) {
    		throw new RuntimeException("Adding NBH: " + dataNBH.districtname() + " to J_RemainingTotalsManager for the second time, This will cause energy total mismatches and therefor is not allowed!");
    	}
    	if(isFinalized) {
    		throw new RuntimeException("Adding NBH: " + dataNBH.districtname() + " to J_RemainingTotalsManager after the J_RemainingTotals has been finalized, This will cause energy total mismatches and therefor is not allowed!");
    	}
    	
    	//Create remaining totals instance for the NBH and add to Map
    	remainingTotalsMap.put(dataNBH.districtname(), new J_RemainingTotals(dataNBH, this.avgc_data));
    	
    	//Manage model totals and backup
    	if(dataNBH.total_comp_elec_delivery_kwh_p_yr() != null && dataNBH.total_comp_elec_delivery_kwh_p_yr() >= 0){
    		if(totalElectricityConsumptionCompaniesTotalIsManualInput) { // Remove total of this nb from the 'backup total'
    			remainingTotalsMap.get(this.originalModelTotalName).adjustRemainingECDeliveryCompanies_kWh(OL_EnergyCarriers.ELECTRICITY, -dataNBH.total_comp_elec_delivery_kwh_p_yr());
        	}
    		else {
    			remainingTotalsMap.get(this.originalModelTotalName).adjustRemainingECDeliveryCompanies_kWh(OL_EnergyCarriers.ELECTRICITY, dataNBH.total_comp_elec_delivery_kwh_p_yr());
    		}
    	}
    	if(dataNBH.total_comp_gas_delivery_m3_p_yr() != null && dataNBH.total_comp_gas_delivery_m3_p_yr() >= 0){	
    		if(totalGasConsumptionCompaniesTotalIsManualInput) { // Remove total of this nb from the 'backup total'
    			remainingTotalsMap.get(this.remainingModelTotalName).adjustRemainingECDeliveryCompanies_kWh(OL_EnergyCarriers.METHANE, -dataNBH.total_comp_gas_delivery_m3_p_yr());
        	}
    		else {
    			remainingTotalsMap.get(this.originalModelTotalName).adjustRemainingECDeliveryCompanies_kWh(OL_EnergyCarriers.METHANE, dataNBH.total_comp_gas_delivery_m3_p_yr());
    		}
    	}
    	if(dataNBH.total_nr_comp_cars() != null && dataNBH.total_nr_comp_cars() >= 0){	
    		if(totalCarsCompaniesTotalIsManualInput) { // Remove total of this nb from the 'backup total'
    			remainingTotalsMap.get(this.remainingModelTotalName).adjustRemainingNumberOfVehiclesCompanies(OL_VehicleType.CAR, -dataNBH.total_nr_comp_cars());
        	}
    		else {
    			remainingTotalsMap.get(this.originalModelTotalName).adjustRemainingNumberOfVehiclesCompanies(OL_VehicleType.CAR, dataNBH.total_nr_comp_cars());
    		}
    	}
    	if(dataNBH.total_nr_comp_vans() != null && dataNBH.total_nr_comp_vans() >= 0){	
    		if(totalVansCompaniesTotalIsManualInput) { // Remove total of this nb from the 'backup total'
    			remainingTotalsMap.get(this.remainingModelTotalName).adjustRemainingNumberOfVehiclesCompanies(OL_VehicleType.VAN, -dataNBH.total_nr_comp_vans());
            }
    		else {
    			remainingTotalsMap.get(this.originalModelTotalName).adjustRemainingNumberOfVehiclesCompanies(OL_VehicleType.VAN, dataNBH.total_nr_comp_vans());
    		}
    	}
    	if(dataNBH.total_nr_comp_trucks() != null && dataNBH.total_nr_comp_trucks() >= 0){	
    		if(totalTrucksCompaniesTotalIsManualInput) { // Remove total of this nb from the 'backup total'
    			remainingTotalsMap.get(this.remainingModelTotalName).adjustRemainingNumberOfVehiclesCompanies(OL_VehicleType.TRUCK, -dataNBH.total_nr_comp_trucks());
            }
    		else {
    			remainingTotalsMap.get(this.originalModelTotalName).adjustRemainingNumberOfVehiclesCompanies(OL_VehicleType.TRUCK, dataNBH.total_nr_comp_trucks());
    		}
    	}
    }
    
    
    //Adjust remaining totals
    public void adjustRemainingElectricityDeliveryCompanies_kWh(GridConnection GC, double adjustment_kWh) {
    	adjustRemainingECDeliveryCompanies_kWh(GC, OL_EnergyCarriers.ELECTRICITY, adjustment_kWh);
    }
    public void adjustRemainingGasDeliveryCompanies_m3(GridConnection GC, double adjustment_m3) {
    	adjustRemainingECDeliveryCompanies_kWh(GC, OL_EnergyCarriers.METHANE, adjustment_m3 * avgc_data.p_gas_kWhpm3);
    }
	    private void adjustRemainingECDeliveryCompanies_kWh(GridConnection GC, OL_EnergyCarriers EC, double adjustment_kWh) {
			if(!supportedEnergyCarriers.contains(EC)) {
				throw new RuntimeException("Trying to adjustRemainingECDeliveryCompanies_kWh for an unsupported EC");
			}
	    	
	    	String idNBH = getNBHIdOfGC(GC);
	        if(remainingTotalsMap.get(idNBH).getRemainingECDeliveryCompanies_kWh(EC) == null) {
	        	idNBH = this.remainingModelTotalName;
	        }
	        remainingTotalsMap.get(idNBH).adjustRemainingECDeliveryCompanies_kWh(EC, adjustment_kWh);
	   
	    }
    
	public void adjustRemainingNumberOfVehiclesCompanies(GridConnection GC, OL_VehicleType vehicleType, int deltaNumberOfVehicles) {
		if(!supportedVehicleTypes.contains(vehicleType)) {
			throw new RuntimeException("Trying to adjustRemainingNumberOfVehiclesCompanies for an unsupported vehicleType");
		}
		
		String idNBH = getNBHIdOfGC(GC);
        if(remainingTotalsMap.get(idNBH).getRemainingNumberOfVehiclesCompanies(vehicleType) == null) {
        	idNBH = this.remainingModelTotalName;
        }
        remainingTotalsMap.get(idNBH).adjustRemainingNumberOfVehiclesCompanies(vehicleType, deltaNumberOfVehicles);
	}
		
    public void adjustTotalFloorSurfaceAnonymousCompanies_m2(GridConnection GC, double deltaFloorSurface_m2) {
        remainingTotalsMap.get(getNBHIdOfGC(GC)).adjustTotalFloorSurfaceAnonymousCompanies_m2(deltaFloorSurface_m2);
        
        //Update model total as well
        remainingTotalsMap.get(this.originalModelTotalName).adjustTotalFloorSurfaceAnonymousCompanies_m2(deltaFloorSurface_m2);
    }
    
    public void adjustTotalNumberOfAnonymousCompanies(GridConnection GC, int deltaNumberOfAnonymousCompanies) {
        remainingTotalsMap.get(getNBHIdOfGC(GC)).adjustTotalNumberOfAnonymousCompanies(deltaNumberOfAnonymousCompanies);
        
        //Update model total as well
        remainingTotalsMap.get(this.originalModelTotalName).adjustTotalNumberOfAnonymousCompanies(deltaNumberOfAnonymousCompanies);
    }
    
    ////Getters
    
    //EnergyCarrier delivery getters
    public double getRemainingElectricityDeliveryCompanies_kWh(GridConnection GC) {
        return getRemainingECDeliveryOfAnonymousCompanies_kWh(GC, OL_EnergyCarriers.ELECTRICITY);
    }
    public double getRemainingGasDeliveryCompanies_m3(GridConnection GC) {
        return getRemainingECDeliveryOfAnonymousCompanies_kWh(GC, OL_EnergyCarriers.METHANE);
    }
    private double getRemainingECDeliveryOfAnonymousCompanies_kWh(GridConnection GC, OL_EnergyCarriers EC) {
		if(!supportedEnergyCarriers.contains(EC)) {
			throw new RuntimeException("Trying to getRemainingECDeliveryOfAnonymousCompanies_kWh for an unsupported EC");
		}
        String idNBH = getNBHIdOfGC(GC);
        if(remainingTotalsMap.get(idNBH).getRemainingECDeliveryCompanies_kWh(EC) == null) {
        	idNBH = this.remainingModelTotalName;
        }
        return remainingTotalsMap.get(idNBH).getRemainingECDeliveryCompanies_kWh(EC);
    }
    
    //Vehicle getters 
	public Integer getRemainingNumberOfVehiclesCompanies(GridConnection GC, OL_VehicleType vehicleType) {
		if(!supportedVehicleTypes.contains(vehicleType)) {
			throw new RuntimeException("Trying to getRemainingNumberOfVehiclesCompanies for an unsupported VehicleType");
		}
        String idNBH = getNBHIdOfGC(GC);
        if(remainingTotalsMap.get(idNBH).getRemainingNumberOfVehiclesCompanies(vehicleType) == null) {
        	idNBH = this.remainingModelTotalName;
        }
		return remainingTotalsMap.get(idNBH).getRemainingNumberOfVehiclesCompanies(vehicleType);
	}
    
	//Default getters
    public double getTotalFloorSurfaceAnonymousCompanies_m2(GridConnection GC) {
        return remainingTotalsMap.get(getNBHIdOfGC(GC)).getTotalFloorSurfaceAnonymousCompanies_m2();
    }   
    public int getTotalNumberOfAnonymousCompanies(GridConnection GC) {
        return remainingTotalsMap.get(getNBHIdOfGC(GC)).getTotalNumberOfAnonymousCompanies();
    } 
    
    
    ////Finalize the classes
    public void finalizeRemainingTotalsDistributionCompanies() {
		if(this.isFinalized) {
			throw new RuntimeException("Trying to finalizeRemainingTotalsDistributionCompanies for a second time");
		}
		
    	//Finalize the EC per m2 calculation
    	this.finalizeRemainingECTotalsPerM2Calculation();
    	
    	//Set the starting number of anonymous companies for the vehicle distribution
    	this.setStartingNumberOfAnonymousCompaniesForVehicleDistribution();
    	
    	//Set class state to finalized
    	this.remainingTotalsMap.values().forEach(remainingTotals -> remainingTotals.finalize());
    	this.isFinalized = true;
    }
    
    //Set the starting number of anonymous companies for the vehicle distribution
    private void setStartingNumberOfAnonymousCompaniesForVehicleDistribution() {
		if(this.isFinalized) {
			throw new RuntimeException("Trying to setStartingNumberOfAnonymousCompaniesForVehicleDistribution after finalization.");
		}
    	//Set setStartingNumberOfAnonymousCompaniesForVehicleDistribution for all that have specific vehicle totals
    	this.remainingTotalsMap.values().forEach(remainingTotals -> remainingTotals.setStartingNumberOfAnonymousCompaniesForVehicleDistribution());
    	
    	//If NBH doesnt have specific vehicle total, add its total anonymous companies to the 'backup'
		for(J_RemainingTotals remainingTotalsNBH : this.remainingTotalsMap.values()) {
			for(OL_VehicleType vehicleType : supportedVehicleTypes) {
				if(remainingTotalsNBH.getRemainingNumberOfVehiclesCompanies(vehicleType) == null) {
					this.remainingTotalsMap.get(this.remainingModelTotalName).adjustRemainingNumberOfAnonymousCompaniesForVehicleType(vehicleType, remainingTotalsNBH.getTotalNumberOfAnonymousCompanies());
				}
			}
		}
    }
    
    //Calculate certain remaining totals per m2 and finalize the nbh totals -> no longer adjustable
    private void finalizeRemainingECTotalsPerM2Calculation() {
		if(this.isFinalized) {
			throw new RuntimeException("Trying to finalizeRemainingECTotalsPerM2Calculation after finalization.");
		}
    	//Calculate the remaining EC delivery per m2 for all EC, where uknown NBH are grouped together with the backup to define the average
    	Map<OL_EnergyCarriers, Double> totalRemainingAnonymousCompaniesFloorSurfacePerEC_m2= new HashMap<>();
    	for(OL_EnergyCarriers supportedEC : supportedEnergyCarriers) {
    		totalRemainingAnonymousCompaniesFloorSurfacePerEC_m2.put(supportedEC, 0.0);
    	}
    	
    	for(J_RemainingTotals remainingTotalsNBH : this.remainingTotalsMap.values()) {
    		for(OL_EnergyCarriers EC : totalRemainingAnonymousCompaniesFloorSurfacePerEC_m2.keySet()) {
    			if(remainingTotalsNBH.getRemainingECDeliveryCompanies_kWh(EC) == null) {
    				totalRemainingAnonymousCompaniesFloorSurfacePerEC_m2.put(EC, totalRemainingAnonymousCompaniesFloorSurfacePerEC_m2.get(EC) + remainingTotalsNBH.getTotalFloorSurfaceAnonymousCompanies_m2());
    			}
    		}
		}
    	
    	for(J_RemainingTotals remainingTotalsNBH : this.remainingTotalsMap.values()) {
    		for(OL_EnergyCarriers EC : totalRemainingAnonymousCompaniesFloorSurfacePerEC_m2.keySet()) {
				if(remainingTotalsNBH.getRemainingECDeliveryCompanies_kWh(EC) != null) {
					double floorSurfaceAnonymousCompanies_m2 = remainingTotalsNBH.getTotalFloorSurfaceAnonymousCompanies_m2();
					if(remainingTotalsNBH.getIdNBH().equals(this.remainingModelTotalName)) {
						floorSurfaceAnonymousCompanies_m2 += totalRemainingAnonymousCompaniesFloorSurfacePerEC_m2.get(EC);
					}
					if(floorSurfaceAnonymousCompanies_m2 > 0) {
						remainingTotalsNBH.setECDeliveryOfAnonymousCompanies_kWhpm2(EC, remainingTotalsNBH.getRemainingECDeliveryCompanies_kWh(EC) / floorSurfaceAnonymousCompanies_m2); 
					}
				}
    		}
		}
    }
    
    
    ////After finalization getters
    public double getElectricityDeliveryOfAnonymousCompanies_kWhpm2(GridConnection GC) {
    	return getECDeliveryOfAnonymousCompanies_kWhpm2(GC, OL_EnergyCarriers.ELECTRICITY);
    } 
    public double getGasDeliveryOfAnonymousCompanies_m3pm2(GridConnection GC) {
    	return getECDeliveryOfAnonymousCompanies_kWhpm2(GC, OL_EnergyCarriers.METHANE) / avgc_data.p_gas_kWhpm3;
    } 
	    private double getECDeliveryOfAnonymousCompanies_kWhpm2(GridConnection GC, OL_EnergyCarriers EC) {
	    	if(this.isFinalized){
				if(!supportedEnergyCarriers.contains(EC)) {
					throw new RuntimeException("Trying to getECDeliveryOfAnonymousCompanies_kWhpm2 for an unsupported EC");
				}
				
		    	String idNBH = getNBHIdOfGC(GC);
		        if(remainingTotalsMap.get(idNBH).getECDeliveryOfAnonymousCompanies_kWhpm2(EC) == null) {
		        	idNBH = this.remainingModelTotalName;
		        }
		        return remainingTotalsMap.get(idNBH).getECDeliveryOfAnonymousCompanies_kWhpm2(EC);
	    	}
	    	else {
	        	throw new RuntimeException("The getter 'getEnergyCarrierDeliveryOfAnonymousCompanies_kWhpm2' is called while the calculation has not been finalized yet.");
	    	}
	    }
    
    //Get vehicles per company per nbh
    public int getCeiledRemainingNumberOfVehiclesPerCompany(GridConnection GC, OL_VehicleType vehicleType) {
        if(this.isFinalized){
    		if(!supportedVehicleTypes.contains(vehicleType)) {
    			throw new RuntimeException("Trying to getCeiledRemainingNumberOfVehiclesPerCompany for an unsupported VehicleType");
    		}
	    	String idNBH = getNBHIdOfGC(GC);
			if(remainingTotalsMap.get(idNBH).getRemainingNumberOfVehiclesCompanies(vehicleType) == null) {
				idNBH = this.remainingModelTotalName;
			}	
			
			//Get remaining number of vehicles and total nr of anonymous companies left for those vehicles
			int remainingNumberOfVehicles = remainingTotalsMap.get(idNBH).getRemainingNumberOfVehiclesCompanies(vehicleType);
			int remainingNumberOfAnonymousCompaniesForVehicleType = remainingTotalsMap.get(idNBH).getRemainingNumberOfAnonymousCompaniesForVehicleType(vehicleType); // -> += to add anonymous companies for remainingModelTotalName as well
			
			//Initialize the ceiledRemainingNumberOfCarsPerCompany
			int ceiledRemainingNumberOfVehiclesPerCompany = 0;
			if(remainingNumberOfVehicles != 0 && remainingNumberOfAnonymousCompaniesForVehicleType != 0) {
				ceiledRemainingNumberOfVehiclesPerCompany = roundToInt(ceil((double)remainingNumberOfVehicles/(double)remainingNumberOfAnonymousCompaniesForVehicleType));
			}
	
			return ceiledRemainingNumberOfVehiclesPerCompany;
        }
        else {
        	throw new RuntimeException("The getter 'getCeiledRemainingNumberOfVehiclesPerCompany' is called while the RemainingTotalsClass has not been finalized yet.");
        }
	}
    
    public void adjustRemainingNumberOfAnonymousCompaniesForVehicleType(GridConnection GC, OL_VehicleType vehicleType, int deltaNumberOfAnonymousCompanies) {
        if(this.isFinalized) {
    		if(!supportedVehicleTypes.contains(vehicleType)) {
    			throw new RuntimeException("Trying to adjustRemainingNumberOfAnonymousCompaniesForVehicleType for an unsupported VehicleType");
    		}
    		
	    	String idNBH = getNBHIdOfGC(GC);
	        if(remainingTotalsMap.get(idNBH).getRemainingNumberOfAnonymousCompaniesForVehicleType(vehicleType) == null) {
	        	idNBH = this.remainingModelTotalName;
	        }
	        remainingTotalsMap.get(idNBH).adjustRemainingNumberOfAnonymousCompaniesForVehicleType(vehicleType, deltaNumberOfAnonymousCompanies);
	        }
        else {
        	
        }
    }
    
    
    
    ////Get NBH id where GC is located in that has a 'J_RemainingTotals', if not: return backup.
    private String getNBHIdOfGC(GridConnection GC) {
    	String idNBH = this.remainingModelTotalName;
    	if(GC != null) {
    		GIS_Object area = findFirst(GC.energyModel.pop_GIS_Objects, nbh -> nbh.p_GISObjectType == OL_GISObjectType.REGION && nbh.gisRegion.contains(GC.p_latitude, GC.p_longitude));
        	if(area != null && remainingTotalsMap.containsKey(area.p_id)){
        		idNBH = area.p_id;
        	}
    	}
    	return idNBH;
    }
    
    
	@Override
	public String toString() {
    	String completeToString = "";
    	//Put model original and remaining totals on top.
    	completeToString += remainingTotalsMap.get(originalModelTotalName).toString();
    	completeToString += "\n" + "\n" + remainingTotalsMap.get(remainingModelTotalName).toString();
    	
    	for(J_RemainingTotals remainingTotalsNBH : this.remainingTotalsMap.values()) {
    		if(remainingTotalsNBH.getIdNBH().equals(this.originalModelTotalName) || remainingTotalsNBH.getIdNBH().equals(this.remainingModelTotalName)) {
    			continue;
    		}
    		completeToString += "\n" + "\n" + remainingTotalsNBH.toString();
		}
    	return completeToString;
	}
}