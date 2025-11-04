/**
 * J_RemainingTotals
 */	
public class J_RemainingTotals {
	
	//Original Model total (Model to
	private final String originalModelTotalName = "Original model total";
	private List<String> activeNBH = new ArrayList<>(List.of(originalModelTotalName));
	private Map<String, Double> remainingElectricityDeliveryCompanies_kWh = new HashMap<>(Map.of(originalModelTotalName, 0.0));
	private Map<String, Double> remainingGasDeliveryCompanies_m3 = new HashMap<>(Map.of(originalModelTotalName, 0.0));
	private Map<String, Integer> remainingNumberOfCarsCompanies = new HashMap<>(Map.of(originalModelTotalName, 0));
	private Map<String, Integer> remainingNumberOfVansCompanies = new HashMap<>(Map.of(originalModelTotalName, 0));
	private Map<String, Integer> remainingNumberOfTrucksCompanies = new HashMap<>(Map.of(originalModelTotalName, 0));
	private Map<String, Double> totalFloorAreaAnonymousCompanies_m2 = new HashMap<>(Map.of(originalModelTotalName, 0.0));
	private Map<String, Integer> totalNumberOfAnonymousCompanies = new HashMap<>(Map.of(originalModelTotalName, 0));
	
	//Remaining Model total (used as backup for gc that are not in an area)
	private final String remainingModelTotalName = "Remaining model total";	
	
	//Calculated values
	private boolean finalized = false;
	private Map<String, Double> electricityDeliveryOfAnonymousCompanies_kWhpm2 = new HashMap<>(Map.of(originalModelTotalName, 0.0));
	private Map<String, Double> gasDeliveryOfAnonymousCompanies_m3pm2 = new HashMap<>(Map.of(originalModelTotalName, 0.0));
	
	//Total value is added total or manual input
	private boolean totalElectricityConsumptionCompaniesTotalIsManualInput = false;
	private boolean totalGasConsumptionCompaniesTotalIsManualInput = false;
	private boolean totalCarsCompaniesTotalIsManualInput = false;
	private boolean totalVansCompaniesTotalIsManualInput = false;
	private boolean totalTrucksCompaniesTotalIsManualInput = false;
	
    /**
     * Default constructor
     */
    public J_RemainingTotals() {
    	//Initialize backup
    	activeNBH.add(remainingModelTotalName);
    	remainingElectricityDeliveryCompanies_kWh.put(this.remainingModelTotalName, 0.0);
    	remainingGasDeliveryCompanies_m3.put(this.remainingModelTotalName, 0.0);
    	remainingNumberOfCarsCompanies.put(this.remainingModelTotalName, 0);
    	remainingNumberOfVansCompanies.put(this.remainingModelTotalName, 0);
    	remainingNumberOfTrucksCompanies.put(this.remainingModelTotalName, 0);
    	totalFloorAreaAnonymousCompanies_m2.put(this.remainingModelTotalName, 0.0);
    	totalNumberOfAnonymousCompanies.put(this.remainingModelTotalName, 0);
    	
    	electricityDeliveryOfAnonymousCompanies_kWhpm2.put(this.remainingModelTotalName, 0.0);
    	gasDeliveryOfAnonymousCompanies_m3pm2.put(this.remainingModelTotalName, 0.0);
    }
    
    //Initialize model totals
    public void initializeModelTotals(Project_data project_data) {
    	//Energy totals
    	if(project_data.total_electricity_consumption_companies_kWh_p_yr() != null && project_data.total_electricity_consumption_companies_kWh_p_yr() > 0){
    		remainingElectricityDeliveryCompanies_kWh.put(originalModelTotalName, project_data.total_electricity_consumption_companies_kWh_p_yr());
    		remainingElectricityDeliveryCompanies_kWh.put(remainingModelTotalName, project_data.total_electricity_consumption_companies_kWh_p_yr());
    		this.totalElectricityConsumptionCompaniesTotalIsManualInput = true;
    	}
    	if(project_data.total_gas_consumption_companies_m3_p_yr() != null && project_data.total_gas_consumption_companies_m3_p_yr() > 0){	
    		remainingGasDeliveryCompanies_m3.put(originalModelTotalName, project_data.total_gas_consumption_companies_m3_p_yr());
    		remainingGasDeliveryCompanies_m3.put(remainingModelTotalName, project_data.total_gas_consumption_companies_m3_p_yr());
    		this.totalGasConsumptionCompaniesTotalIsManualInput = true;
    	}
    	if(project_data.total_cars_companies() != null && project_data.total_cars_companies() > 0){	
    		remainingNumberOfCarsCompanies.put(originalModelTotalName, project_data.total_cars_companies());
    		remainingNumberOfCarsCompanies.put(remainingModelTotalName, project_data.total_cars_companies());
    		this.totalCarsCompaniesTotalIsManualInput = true;
    	}
    	if(project_data.total_vans_companies() != null && project_data.total_vans_companies() > 0){	
    		remainingNumberOfVansCompanies.put(originalModelTotalName, project_data.total_vans_companies());
    		remainingNumberOfVansCompanies.put(remainingModelTotalName, project_data.total_vans_companies());
    		this.totalVansCompaniesTotalIsManualInput = true;
    	}
    	if(project_data.total_trucks_companies() != null && project_data.total_trucks_companies() > 0){	
    		remainingNumberOfTrucksCompanies.put(originalModelTotalName, project_data.total_trucks_companies());
    		remainingNumberOfTrucksCompanies.put(remainingModelTotalName, project_data.total_trucks_companies());
    		this.totalTrucksCompaniesTotalIsManualInput = true;
    	}
    }
    
    
    
    //Initialize remaining totals for each NBH
    public void addNBH(Neighbourhood_data NBH) {
    	if(activeNBH.contains(NBH.districtname())) {
    		throw new RuntimeException("Adding NBH: " + NBH.districtname() + " to J_RemainingTotals for the second time, This will cause energy total mismatches and therefor is not allowed!");
    	}
    	if(finalized) {
    		throw new RuntimeException("Adding NBH: " + NBH.districtname() + " to J_RemainingTotals after the J_RemainingTotals has been finalized, This will cause energy total mismatches and therefor is not allowed!");
    	}
    	
    	//Add to presentNBH list
    	activeNBH.add(NBH.districtname());
    	
    	if(NBH.total_comp_elec_delivery_kwh_p_yr() != null && NBH.total_comp_elec_delivery_kwh_p_yr() >= 0){
    		remainingElectricityDeliveryCompanies_kWh.put(NBH.districtname(), NBH.total_comp_elec_delivery_kwh_p_yr());
    		if(totalElectricityConsumptionCompaniesTotalIsManualInput) { // Remove total of this nb from the 'backup total'
    			adjustRemainingElectricityDeliveryCompanies_kWh(null, -NBH.total_comp_elec_delivery_kwh_p_yr());
    		}
    		else {
    			remainingElectricityDeliveryCompanies_kWh.put(originalModelTotalName, remainingElectricityDeliveryCompanies_kWh.get(originalModelTotalName) + NBH.total_comp_elec_delivery_kwh_p_yr());
    		}
    	}
    	if(NBH.total_comp_gas_delivery_m3_p_yr() != null && NBH.total_comp_gas_delivery_m3_p_yr() >= 0){	
    		remainingGasDeliveryCompanies_m3.put(NBH.districtname(), NBH.total_comp_gas_delivery_m3_p_yr());
    		if(totalGasConsumptionCompaniesTotalIsManualInput) { // Remove total of this nb from the 'backup total'
    			adjustRemainingGasDeliveryCompanies_m3(null, -NBH.total_comp_gas_delivery_m3_p_yr());
    		}
    		else {
    			remainingGasDeliveryCompanies_m3.put(originalModelTotalName, remainingGasDeliveryCompanies_m3.get(originalModelTotalName) + NBH.total_comp_gas_delivery_m3_p_yr());
    		}
    	}
    	if(NBH.total_nr_comp_cars() != null && NBH.total_nr_comp_cars() >= 0){	
    		remainingNumberOfCarsCompanies.put(NBH.districtname(), NBH.total_nr_comp_cars());
    		if(totalCarsCompaniesTotalIsManualInput) { // Remove total of this nb from the 'backup total'
    			adjustRemainingNumberOfCarsCompanies(null, -NBH.total_nr_comp_cars());
    		}
    		else {
    			remainingNumberOfCarsCompanies.put(originalModelTotalName, remainingNumberOfCarsCompanies.get(originalModelTotalName) + NBH.total_nr_comp_cars());
    		}
    	}
    	if(NBH.total_nr_comp_vans() != null && NBH.total_nr_comp_vans() >= 0){	
    		remainingNumberOfVansCompanies.put(NBH.districtname(), NBH.total_nr_comp_vans());
    		if(totalVansCompaniesTotalIsManualInput) { // Remove total of this nb from the 'backup total'
    			adjustRemainingNumberOfVansCompanies(null, -NBH.total_nr_comp_vans());
    		}
    		else {
    			remainingNumberOfVansCompanies.put(originalModelTotalName, remainingNumberOfVansCompanies.get(originalModelTotalName) + NBH.total_nr_comp_vans());
    		}
    	}
    	if(NBH.total_nr_comp_trucks() != null && NBH.total_nr_comp_trucks() >= 0){	
    		remainingNumberOfTrucksCompanies.put(NBH.districtname(), NBH.total_nr_comp_trucks());
    		if(totalTrucksCompaniesTotalIsManualInput) { // Remove total of this nb from the 'backup total'
    			adjustRemainingNumberOfTrucksCompanies(null, -NBH.total_nr_comp_trucks());
    		}
    		else {
    			remainingNumberOfTrucksCompanies.put(originalModelTotalName, remainingNumberOfTrucksCompanies.get(originalModelTotalName) + NBH.total_nr_comp_trucks());
    		}
    	}

    	totalFloorAreaAnonymousCompanies_m2.put(NBH.districtname(), 0.0);
    	totalNumberOfAnonymousCompanies.put(NBH.districtname(), 0);
    }
    
    
    
    //Adjust remaining totals
    public void adjustRemainingElectricityDeliveryCompanies_kWh(GridConnection GC, double adjustment_kWh) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingElectricityDeliveryCompanies_kWh.get(idNBH) == null) {
        	idNBH = this.remainingModelTotalName;
        }
        remainingElectricityDeliveryCompanies_kWh.put(idNBH, remainingElectricityDeliveryCompanies_kWh.get(idNBH) + adjustment_kWh);
    }
    public void adjustRemainingGasDeliveryCompanies_m3(GridConnection GC, double adjustment_m3) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingGasDeliveryCompanies_m3.get(idNBH) == null) {
        	idNBH = this.remainingModelTotalName;
        }
        remainingGasDeliveryCompanies_m3.put(idNBH, remainingGasDeliveryCompanies_m3.get(idNBH) + adjustment_m3);
    }
    public void adjustRemainingNumberOfCarsCompanies(GridConnection GC, int deltaNbOfCars) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingNumberOfCarsCompanies.get(idNBH) == null) {
        	idNBH = this.remainingModelTotalName;
        }
        remainingNumberOfCarsCompanies.put(idNBH, remainingNumberOfCarsCompanies.get(idNBH) + deltaNbOfCars);
    }
    public void adjustRemainingNumberOfVansCompanies(GridConnection GC, int deltaNbOfVans) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingNumberOfVansCompanies.get(idNBH) == null) {
        	idNBH = this.remainingModelTotalName;
        }
        remainingNumberOfVansCompanies.put(idNBH, remainingNumberOfVansCompanies.get(idNBH) + deltaNbOfVans);
    }
    public void adjustRemainingNumberOfTrucksCompanies(GridConnection GC, int deltaNbOfTrucks) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingNumberOfTrucksCompanies.get(idNBH) == null) {
        	idNBH = this.remainingModelTotalName;
        }
        remainingNumberOfTrucksCompanies.put(idNBH, remainingNumberOfTrucksCompanies.get(idNBH) + deltaNbOfTrucks);
    }
    public void adjustTotalFloorAreaAnonymousCompanies_m2(GridConnection GC, double deltaFloorArea_m2) {
        String idNBH = getNBHIdOfGC(GC);
        if(totalFloorAreaAnonymousCompanies_m2.get(idNBH) == null) {
        	idNBH = this.remainingModelTotalName;
        }
        totalFloorAreaAnonymousCompanies_m2.put(idNBH, totalFloorAreaAnonymousCompanies_m2.get(idNBH) + deltaFloorArea_m2);
        
        //Update model total as well
        totalFloorAreaAnonymousCompanies_m2.put(this.originalModelTotalName, totalFloorAreaAnonymousCompanies_m2.get(this.originalModelTotalName) + deltaFloorArea_m2);
    }
    public void adjustTotalNumberOfAnonymousCompanies(GridConnection GC, int deltaNumberOfAnonymousCompanies) {
        String idNBH = getNBHIdOfGC(GC);
        if(totalNumberOfAnonymousCompanies.get(idNBH) == null) {
        	idNBH = this.remainingModelTotalName;
        }
        totalNumberOfAnonymousCompanies.put(idNBH, totalNumberOfAnonymousCompanies.get(idNBH) + deltaNumberOfAnonymousCompanies);
        
        //Update model total as well
        totalNumberOfAnonymousCompanies.put(this.originalModelTotalName, totalNumberOfAnonymousCompanies.get(this.originalModelTotalName) + deltaNumberOfAnonymousCompanies);
    }    
    
    
    
    
    //Get current remaining totals
    public double getRemainingElectricityDeliveryCompanies_kWh(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(!remainingElectricityDeliveryCompanies_kWh.containsKey(idNBH)) {
        	idNBH = this.remainingModelTotalName;
        }
        return remainingElectricityDeliveryCompanies_kWh.get(idNBH);
    }

    public double getRemainingGasDeliveryCompanies_m3(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(!remainingGasDeliveryCompanies_m3.containsKey(idNBH)) {
        	idNBH = this.remainingModelTotalName;
        }
        return remainingGasDeliveryCompanies_m3.get(idNBH);
    }
    
    public int getRemainingNumberOfCarsCompanies(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(!remainingNumberOfCarsCompanies.containsKey(idNBH)) {
        	idNBH = this.remainingModelTotalName;
        }
        return remainingNumberOfCarsCompanies.get(idNBH);
    }
    public int getRemainingNumberOfVansCompanies(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(!remainingNumberOfVansCompanies.containsKey(idNBH)) {
        	idNBH = this.remainingModelTotalName;
        }
        return remainingNumberOfVansCompanies.get(idNBH);
    }   
    public int getRemainingNumberOfTrucksCompanies(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(!remainingNumberOfTrucksCompanies.containsKey(idNBH)) {
        	idNBH = this.remainingModelTotalName;
        }
        return remainingNumberOfTrucksCompanies.get(idNBH);
    } 
    public double getTotalFloorAreaAnonymousCompanies_m2(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(!totalFloorAreaAnonymousCompanies_m2.containsKey(idNBH)) {
        	idNBH = this.remainingModelTotalName;
        }
        return totalFloorAreaAnonymousCompanies_m2.get(idNBH);
    }   
    public int getTotalNumberOfAnonymousCompanies(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(!totalNumberOfAnonymousCompanies.containsKey(idNBH)) {
        	idNBH = this.remainingModelTotalName;
        }
        return totalNumberOfAnonymousCompanies.get(idNBH);
    } 
    
    
    //Calculate certain remaining totals per m2 and finalize the nbh totals -> no longer adjustable
    public void finalizeRemainingTotalsPerM2Calculation() {
    	//Calculate the remaining electricity delivery per m2
    	double totalRemainingAnonymousCompaniesFloorSurface_m2_elec = 0;
    	for(String activeNBHid : this.activeNBH) {
			if(!remainingElectricityDeliveryCompanies_kWh.containsKey(activeNBHid)) {
				totalRemainingAnonymousCompaniesFloorSurface_m2_elec += totalFloorAreaAnonymousCompanies_m2.get(activeNBHid);
			}
		}
    	for(String idNBH : remainingElectricityDeliveryCompanies_kWh.keySet()) {
			double floorSurfaceAnonymousCompanies_m2 = totalFloorAreaAnonymousCompanies_m2.get(idNBH);
			if(idNBH.equals(this.remainingModelTotalName)) {
				floorSurfaceAnonymousCompanies_m2 += totalRemainingAnonymousCompaniesFloorSurface_m2_elec;
			}
			if(floorSurfaceAnonymousCompanies_m2 > 0) {
				electricityDeliveryOfAnonymousCompanies_kWhpm2.put(idNBH, max(0, remainingElectricityDeliveryCompanies_kWh.get(idNBH) / floorSurfaceAnonymousCompanies_m2));
			}
    	}
    	
    	//Calculate the remaining gas delivery per m2
    	double totalRemainingAnonymousCompaniesFloorSurface_m2_gas = 0;
    	for(String activeNBHid : this.activeNBH) {
			if(!remainingGasDeliveryCompanies_m3.containsKey(activeNBHid)) {
				totalRemainingAnonymousCompaniesFloorSurface_m2_gas += totalFloorAreaAnonymousCompanies_m2.get(activeNBHid);
			}
		}
    	for(String idNBH : remainingGasDeliveryCompanies_m3.keySet()) {
			double floorSurfaceAnonymousCompanies_m2 = totalFloorAreaAnonymousCompanies_m2.get(idNBH);
			if(idNBH.equals(this.remainingModelTotalName)) {
				floorSurfaceAnonymousCompanies_m2 += totalRemainingAnonymousCompaniesFloorSurface_m2_gas;
			}
			if(floorSurfaceAnonymousCompanies_m2 > 0) {
				gasDeliveryOfAnonymousCompanies_m3pm2.put(idNBH, max(0, remainingGasDeliveryCompanies_m3.get(idNBH) / floorSurfaceAnonymousCompanies_m2));
			}
    	}
    	
    	//Make the maps final, so they can no longer be adjusted (if tried, run time exception is thrown.
    	this.remainingElectricityDeliveryCompanies_kWh = Collections.unmodifiableMap(this.remainingElectricityDeliveryCompanies_kWh);
    	this.remainingGasDeliveryCompanies_m3 = Collections.unmodifiableMap(this.remainingGasDeliveryCompanies_m3);
    	this.totalFloorAreaAnonymousCompanies_m2 = Collections.unmodifiableMap(this.totalFloorAreaAnonymousCompanies_m2);
    	this.totalNumberOfAnonymousCompanies = Collections.unmodifiableMap(this.totalNumberOfAnonymousCompanies);
    	this.electricityDeliveryOfAnonymousCompanies_kWhpm2 = Collections.unmodifiableMap(this.electricityDeliveryOfAnonymousCompanies_kWhpm2);
    	this.gasDeliveryOfAnonymousCompanies_m3pm2 = Collections.unmodifiableMap(this.gasDeliveryOfAnonymousCompanies_m3pm2);
    
    	this.finalized = true;
    }
    
    public double getElectricityDeliveryOfAnonymousCompanies_kWhpm2(GridConnection GC) {
        if(this.finalized){
	    	String idNBH = getNBHIdOfGC(GC);
	        if(!electricityDeliveryOfAnonymousCompanies_kWhpm2.containsKey(idNBH)) {
	        	idNBH = this.remainingModelTotalName;
	        }
	        return electricityDeliveryOfAnonymousCompanies_kWhpm2.get(idNBH);
        }
        else {
        	throw new RuntimeException("The getter 'remainingElectricityDeliveryOfAnonymousCompanies_kWhpm2' is called while the calculation has not been finalized yet.");
        }
    }   
    public double getGasDeliveryOfAnonymousCompanies_m3pm2(GridConnection GC) {
        if(this.finalized){
	    	String idNBH = getNBHIdOfGC(GC);
	        if(!gasDeliveryOfAnonymousCompanies_m3pm2.containsKey(idNBH)) {
	        	idNBH = this.remainingModelTotalName;
	        }
	        return gasDeliveryOfAnonymousCompanies_m3pm2.get(idNBH);
    	}
    	else {
        	throw new RuntimeException("The getter 'remainingGasDeliveryOfAnonymousCompanies_m3pm2' is called while the calculation has not been finalized yet.");
    	}	
    } 
    
    
    
    
    //Get vehicles per company per nbh
    public int getCeiledRemainingNumberOfCarsPerCompany(GridConnection GC) {
		return getCeiledRemainingNumberOfVehiclesPerCompany(GC, this.remainingNumberOfCarsCompanies);
	}
    public int getCeiledRemainingNumberOfVansPerCompany(GridConnection GC) {
    	return getCeiledRemainingNumberOfVehiclesPerCompany(GC, this.remainingNumberOfVansCompanies);
	}
    public int getCeiledRemainingNumberOfTrucksPerCompany(GridConnection GC) {
    	return getCeiledRemainingNumberOfVehiclesPerCompany(GC, this.remainingNumberOfTrucksCompanies);
	}
    private int getCeiledRemainingNumberOfVehiclesPerCompany(GridConnection GC, Map<String, Integer> remainingNumberOfVehiclesCompanies) {
    	int remainingNumberOfVehicles = 0;
    	int totalNrOfAnonymousCompanies = 0;

    	String idNBH = getNBHIdOfGC(GC);
		if(remainingNumberOfVehiclesCompanies.get(idNBH) == null) {
			idNBH = this.remainingModelTotalName;
			
			//Get total anyonoymous companies that are in NBH without specific car total
			for(String activeNBHid : this.activeNBH) {
				if(remainingNumberOfVehiclesCompanies.get(activeNBHid) == null) {
					totalNrOfAnonymousCompanies += totalNumberOfAnonymousCompanies.get(activeNBHid);
				}
			}
		}
		
		//Get remaining number of cars
		remainingNumberOfVehicles = remainingNumberOfVehiclesCompanies.get(idNBH);
		totalNrOfAnonymousCompanies += totalNumberOfAnonymousCompanies.get(idNBH); // -> += to add anonymous companies for remainingModelTotalName as well
		
		//Initialize the ceiledRemainingNumberOfCarsPerCompany
		int ceiledRemainingNumberOfVehiclesPerCompany = 0;
		if(remainingNumberOfVehicles != 0 && totalNrOfAnonymousCompanies != 0) {
			ceiledRemainingNumberOfVehiclesPerCompany = roundToInt(ceil((double)remainingNumberOfVehicles/totalNrOfAnonymousCompanies));
		}
		
		return ceiledRemainingNumberOfVehiclesPerCompany;
	}
    
    
    
    
    //Get NBH where GC is located in
    private String getNBHIdOfGC(GridConnection GC) {
    	String idNBH = this.remainingModelTotalName;
    	
    	if(GC != null) {
    		GIS_Object area = findFirst(GC.energyModel.pop_GIS_Objects, nbh -> nbh.p_GISObjectType == OL_GISObjectType.REGION && nbh.gisRegion.contains(GC.p_latitude, GC.p_longitude));
        	if(area != null){
        		idNBH = area.p_id;
        	}
    	}
    	
    	return idNBH;
    }
    
    
	@Override
	public String toString() {
    	String completeToString = "";
    	for(String idNBH : this.activeNBH) {
    		if(!completeToString.equals("")) {
    			completeToString += "\n" + "\n";
    		}
			completeToString += idNBH + ":";
			
			if(remainingElectricityDeliveryCompanies_kWh.get(idNBH) != null) {
				completeToString += "\n" + "remainingElectricityDeliveryCompanies_kWh: " + remainingElectricityDeliveryCompanies_kWh.get(idNBH);
			}
			if(remainingGasDeliveryCompanies_m3.get(idNBH) != null) {
				completeToString += "\n" + "remainingGasDeliveryCompanies_m3: " + remainingGasDeliveryCompanies_m3.get(idNBH);
			}
			if(remainingNumberOfCarsCompanies.get(idNBH) != null) {
				completeToString += "\n" + "remainingNumberOfCarsCompanies: " + remainingNumberOfCarsCompanies.get(idNBH);
			}
			if(remainingNumberOfVansCompanies.get(idNBH) != null) {
				completeToString += "\n" + "remainingNumberOfVansCompanies: " + remainingNumberOfVansCompanies.get(idNBH);
			}
			if(remainingNumberOfTrucksCompanies.get(idNBH) != null) {
				completeToString += "\n" + "remainingNumberOfTrucksCompanies: " + remainingNumberOfTrucksCompanies.get(idNBH);
			}
			if(totalFloorAreaAnonymousCompanies_m2.get(idNBH) != null) {
				completeToString += "\n" + "totalFloorAreaAnonymousCompanies_m2: " + totalFloorAreaAnonymousCompanies_m2.get(idNBH);
			}
			if(totalNumberOfAnonymousCompanies.get(idNBH) != null) {
				completeToString += "\n" + "totalNumberOfAnonymousCompanies: " + totalNumberOfAnonymousCompanies.get(idNBH);
			}
			if(electricityDeliveryOfAnonymousCompanies_kWhpm2.get(idNBH) != null) {
				completeToString += "\n" + "electricityDeliveryOfAnonymousCompanies_kWhpm2: " + electricityDeliveryOfAnonymousCompanies_kWhpm2.get(idNBH);
			}
			if(gasDeliveryOfAnonymousCompanies_m3pm2.get(idNBH) != null) {
				completeToString += "\n" + "gasDeliveryOfAnonymousCompanies_m3pm2: " + gasDeliveryOfAnonymousCompanies_m3pm2.get(idNBH);
			}
		}
    	return completeToString;
	}
}