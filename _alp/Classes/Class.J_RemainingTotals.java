/**
 * J_RemainingTotals
 */	
public class J_RemainingTotals {
	
	//Model total (also used as backup for gc that are not in area)
	private String totalModelName = "Totaal model";
	
	private Map<String, Double> remainingElectricityDeliveryCompanies_kWh = new HashMap<>(Map.of(totalModelName, 0.0));
	private Map<String, Double> remainingGasDeliveryCompanies_m3 = new HashMap<>(Map.of(totalModelName, 0.0));
	private Map<String, Integer> remainingNumberOfCarsCompanies = new HashMap<>(Map.of(totalModelName, 0));
	private Map<String, Integer> remainingNumberOfVansCompanies = new HashMap<>(Map.of(totalModelName, 0));
	private Map<String, Integer> remainingNumberOfTrucksCompanies = new HashMap<>(Map.of(totalModelName, 0));
	private Map<String, Double> totalFloorAreaAnonymousCompanies_m2 = new HashMap<>(Map.of(totalModelName, 0.0));
	private Map<String, Integer> totalNumberOfAnonymousCompanies = new HashMap<>(Map.of(totalModelName, 0));
	
	//Calculated values
	private boolean finalized = false;
	private Map<String, Double> remainingElectricityDeliveryOfAnonymousCompanies_kWhpm2 = new HashMap<>(Map.of(totalModelName, 0.0));
	private Map<String, Double> remainingGasDeliveryOfAnonymousCompanies_m3pm2 = new HashMap<>(Map.of(totalModelName, 0.0));
	
	
    /**
     * Default constructor
     */
    public J_RemainingTotals() {
    }
    
    //Initialize model totals
    public void initializeModelTotals(Project_data project_data) {
    	//Energy totals
    	if(project_data.total_electricity_consumption_companies_kWh_p_yr() != null && project_data.total_electricity_consumption_companies_kWh_p_yr() > 0){
    		remainingElectricityDeliveryCompanies_kWh.put(totalModelName, project_data.total_electricity_consumption_companies_kWh_p_yr());
    	}
    	if(project_data.total_gas_consumption_companies_m3_p_yr() != null && project_data.total_gas_consumption_companies_m3_p_yr() > 0){	
    		remainingGasDeliveryCompanies_m3.put(totalModelName, project_data.total_gas_consumption_companies_m3_p_yr());
    	}
    	if(project_data.total_cars_companies() != null && project_data.total_cars_companies() > 0){	
    		remainingNumberOfCarsCompanies.put(totalModelName, project_data.total_cars_companies());
    	}
    	if(project_data.total_vans_companies() != null && project_data.total_vans_companies() > 0){	
    		remainingNumberOfVansCompanies.put(totalModelName, project_data.total_vans_companies());
    	}
    	if(project_data.total_trucks_companies() != null && project_data.total_trucks_companies() > 0){	
    		remainingNumberOfTrucksCompanies.put(totalModelName, project_data.total_trucks_companies());
    	}
    }
    
    //Initialize remaining totals
    public void addNBH(Neighbourhood_data NBH) {
    	
    	if(NBH.total_electricity_delivery_companies_kWh_p_yr() != null && NBH.total_electricity_delivery_companies_kWh_p_yr() > 0){
    		remainingElectricityDeliveryCompanies_kWh.put(NBH.districtname(), NBH.total_electricity_delivery_companies_kWh_p_yr());
    		//adjustRemainingElectricityDeliveryCompanies_kWh(null, NBH.total_electricity_delivery_companies_kWh_p_yr());
    	}
    	if(NBH.total_gas_delivery_companies_m3_p_yr() != null && NBH.total_gas_delivery_companies_m3_p_yr() > 0){	
    		remainingGasDeliveryCompanies_m3.put(NBH.districtname(), NBH.total_gas_delivery_companies_m3_p_yr());
    		//adjustRemainingGasDeliveryCompanies_m3(null, NBH.total_gas_delivery_companies_m3_p_yr());
    	}
    	if(NBH.total_cars_companies() != null && NBH.total_cars_companies() > 0){	
    		remainingNumberOfCarsCompanies.put(NBH.districtname(), NBH.total_cars_companies());
    		//adjustRemainingNumberOfCarsCompanies(null, NBH.total_cars_companies());
    	}
    	if(NBH.total_vans_companies() != null && NBH.total_vans_companies() > 0){	
    		remainingNumberOfVansCompanies.put(NBH.districtname(), NBH.total_vans_companies());
    		//adjustRemainingNumberOfVansCompanies(null, NBH.total_vans_companies());
    	}
    	if(NBH.total_trucks_companies() != null && NBH.total_trucks_companies() > 0){	
    		remainingNumberOfTrucksCompanies.put(NBH.districtname(), NBH.total_trucks_companies());
    		//adjustRemainingNumberOfTrucksCompanies(null, NBH.total_trucks_companies());
    	}

    	totalFloorAreaAnonymousCompanies_m2.put(NBH.districtname(), 0.0);
    }
    
    
    //Adjust remaining totals
    public void adjustRemainingElectricityDeliveryCompanies_kWh(GridConnection GC, double adjustment_kWh) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingElectricityDeliveryCompanies_kWh.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        remainingElectricityDeliveryCompanies_kWh.put(idNBH, remainingElectricityDeliveryCompanies_kWh.get(idNBH) + adjustment_kWh);
        
        //Also update model total
        if(GC != null && !idNBH.equals(this.totalModelName)) {
        	adjustRemainingElectricityDeliveryCompanies_kWh(null, adjustment_kWh);
        }
    }
    public void adjustRemainingGasDeliveryCompanies_m3(GridConnection GC, double adjustment_m3) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingGasDeliveryCompanies_m3.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        remainingGasDeliveryCompanies_m3.put(idNBH, remainingGasDeliveryCompanies_m3.get(idNBH) + adjustment_m3);
        
        //Also update model total
        if(GC != null && !idNBH.equals(this.totalModelName)) {
        	adjustRemainingGasDeliveryCompanies_m3(null, adjustment_m3);
        }
    }
    public void adjustRemainingNumberOfCarsCompanies(GridConnection GC, int deltaNbOfCars) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingNumberOfCarsCompanies.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        remainingNumberOfCarsCompanies.put(idNBH, remainingNumberOfCarsCompanies.get(idNBH) + deltaNbOfCars);
        
        //Also update model total
        if(GC != null && !idNBH.equals(this.totalModelName)) {
        	adjustRemainingNumberOfCarsCompanies(null, deltaNbOfCars);
        }
    }
    public void adjustRemainingNumberOfVansCompanies(GridConnection GC, int deltaNbOfVans) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingNumberOfVansCompanies.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        remainingNumberOfVansCompanies.put(idNBH, remainingNumberOfVansCompanies.get(idNBH) + deltaNbOfVans);
        
        //Also update model total
        if(GC != null && !idNBH.equals(this.totalModelName)) {
        	adjustRemainingNumberOfVansCompanies(null, deltaNbOfVans);
        }
    }
    public void adjustRemainingNumberOfTrucksCompanies(GridConnection GC, int deltaNbOfTrucks) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingNumberOfTrucksCompanies.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        remainingNumberOfTrucksCompanies.put(idNBH, remainingNumberOfTrucksCompanies.get(idNBH) + deltaNbOfTrucks);
        
        //Also update model total
        if(GC != null && !idNBH.equals(this.totalModelName)) {
        	adjustRemainingNumberOfTrucksCompanies(null, deltaNbOfTrucks);
        }
    }
    public void adjustTotalFloorAreaAnonymousCompanies_m2(GridConnection GC, double deltaFloorArea_m2) {
        String idNBH = getNBHIdOfGC(GC);
        if(totalFloorAreaAnonymousCompanies_m2.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        totalFloorAreaAnonymousCompanies_m2.put(idNBH, totalFloorAreaAnonymousCompanies_m2.get(idNBH) + deltaFloorArea_m2);
        
        //Also update model total
        if(GC != null && !idNBH.equals(this.totalModelName)) {
        	adjustTotalFloorAreaAnonymousCompanies_m2(null, deltaFloorArea_m2);
        }
    }
    public void adjustTotalNumberOfAnonymousCompanies(GridConnection GC, int deltaNumberOfAnonymousCompanies) {
        String idNBH = getNBHIdOfGC(GC);
        if(totalNumberOfAnonymousCompanies.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        totalNumberOfAnonymousCompanies.put(idNBH, totalNumberOfAnonymousCompanies.get(idNBH) + deltaNumberOfAnonymousCompanies);
        
        //Also update model total
        if(GC != null && !idNBH.equals(this.totalModelName)) {
        	adjustTotalNumberOfAnonymousCompanies(null, deltaNumberOfAnonymousCompanies);
        }
    }    
    
    
    
    
    //Get current remaining totals
    public double getRemainingElectricityDeliveryCompanies_kWh(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingElectricityDeliveryCompanies_kWh.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        return remainingElectricityDeliveryCompanies_kWh.get(idNBH);
    }

    public double getRemainingGasDeliveryCompanies_m3(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingGasDeliveryCompanies_m3.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        return remainingGasDeliveryCompanies_m3.get(idNBH);
    }
    
    public int getRemainingNumberOfCarsCompanies(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingNumberOfCarsCompanies.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        return remainingNumberOfCarsCompanies.get(idNBH);
    }
    public int getRemainingNumberOfVansCompanies(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingNumberOfVansCompanies.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        return remainingNumberOfVansCompanies.get(idNBH);
    }   
    public int getRemainingNumberOfTrucksCompanies(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(remainingNumberOfTrucksCompanies.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        return remainingNumberOfTrucksCompanies.get(idNBH);
    } 
    public double getTotalFloorAreaAnonymousCompanies_m2(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(totalFloorAreaAnonymousCompanies_m2.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        return totalFloorAreaAnonymousCompanies_m2.get(idNBH);
    }   
    public int getTotalNumberOfAnonymousCompanies(GridConnection GC) {
        String idNBH = getNBHIdOfGC(GC);
        if(totalNumberOfAnonymousCompanies.get(idNBH) == null) {
        	idNBH = this.totalModelName;
        }
        return totalNumberOfAnonymousCompanies.get(idNBH);
    } 
    
    
    //Calculate certain remaining totals per m2 and finalize the nbh totals -> no longer adjustable
    public void finalizeRemainingTotalsPerM2Calculation() {
    	//Calculate the remaining electricity delivery per m2
    	for(String idNBH : remainingElectricityDeliveryCompanies_kWh.keySet()) {
    		if(totalFloorAreaAnonymousCompanies_m2.get(idNBH) > 0) {
    			remainingElectricityDeliveryOfAnonymousCompanies_kWhpm2.put(idNBH, remainingElectricityDeliveryCompanies_kWh.get(idNBH) / totalFloorAreaAnonymousCompanies_m2.get(idNBH));
    		}
    	}
    	//Calculate the remaining gas delivery per m2    	
    	for(String idNBH : remainingGasDeliveryCompanies_m3.keySet()) {
    		if(totalFloorAreaAnonymousCompanies_m2.get(idNBH) > 0) {
    			remainingGasDeliveryOfAnonymousCompanies_m3pm2.put(idNBH, remainingGasDeliveryCompanies_m3.get(idNBH) / totalFloorAreaAnonymousCompanies_m2.get(idNBH));
    		}
    	}
    	
    	//Make the maps final, so they can no longer be adjusted (if tried, run time exception is thrown.
    	this.remainingElectricityDeliveryCompanies_kWh = Collections.unmodifiableMap(this.remainingElectricityDeliveryCompanies_kWh);
    	this.remainingGasDeliveryCompanies_m3 = Collections.unmodifiableMap(this.remainingGasDeliveryCompanies_m3);
    	this.totalFloorAreaAnonymousCompanies_m2 = Collections.unmodifiableMap(this.totalFloorAreaAnonymousCompanies_m2);
    	this.totalNumberOfAnonymousCompanies = Collections.unmodifiableMap(this.totalNumberOfAnonymousCompanies);
    	this.remainingElectricityDeliveryOfAnonymousCompanies_kWhpm2 = Collections.unmodifiableMap(this.remainingElectricityDeliveryOfAnonymousCompanies_kWhpm2);
    	this.remainingGasDeliveryOfAnonymousCompanies_m3pm2 = Collections.unmodifiableMap(this.remainingGasDeliveryOfAnonymousCompanies_m3pm2);
    
    	this.finalized = true;
    }
    
    public double getRemainingElectricityDeliveryOfAnonymousCompanies_kWhpm2(GridConnection GC) {
        if(this.finalized){
	    	String idNBH = getNBHIdOfGC(GC);
	        if(remainingElectricityDeliveryOfAnonymousCompanies_kWhpm2.get(idNBH) == null) {
	        	idNBH = this.totalModelName;
	        }
	        return remainingElectricityDeliveryOfAnonymousCompanies_kWhpm2.get(idNBH);
        }
        else {
        	throw new RuntimeException("The getter 'remainingElectricityDeliveryOfAnonymousCompanies_kWhpm2' is called while the calculation has not been finalized yet.");
        }
    }   
    public double getRemainingGasDeliveryOfAnonymousCompanies_m3pm2(GridConnection GC) {
        if(this.finalized){
	    	String idNBH = getNBHIdOfGC(GC);
	        if(remainingGasDeliveryOfAnonymousCompanies_m3pm2.get(idNBH) == null) {
	        	idNBH = this.totalModelName;
	        }
	        return remainingGasDeliveryOfAnonymousCompanies_m3pm2.get(idNBH);
    	}
    	else {
        	throw new RuntimeException("The getter 'remainingGasDeliveryOfAnonymousCompanies_m3pm2' is called while the calculation has not been finalized yet.");
    	}	
    } 
    
    private String getNBHIdOfGC(GridConnection GC) {
    	String idNBH = this.totalModelName;
    	
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
		return "Model totaal: \n" +
				"remainingElectricityDeliveryCompanies_kWh: " + remainingElectricityDeliveryCompanies_kWh.get(this.totalModelName) + "\n" + 
				"remainingGasDeliveryCompanies_m3: " + remainingGasDeliveryCompanies_m3.get(this.totalModelName) + "\n" + 
				"remainingNumberOfCarsCompanies: " + remainingNumberOfCarsCompanies.get(this.totalModelName) + "\n" + 
				"remainingNumberOfVansCompanies: " + remainingNumberOfVansCompanies.get(this.totalModelName) + "\n" + 
				"remainingNumberOfTrucksCompanies: " + remainingNumberOfTrucksCompanies.get(this.totalModelName) + "\n" + 
				"totalFloorAreaAnonymousCompanies_m2: " + totalFloorAreaAnonymousCompanies_m2.get(this.totalModelName) + "\n" + 
				"totalNumberOfAnonymousCompanies: " + totalNumberOfAnonymousCompanies.get(this.totalModelName);
	}
}