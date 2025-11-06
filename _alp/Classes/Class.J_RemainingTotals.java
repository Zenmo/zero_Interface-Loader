/**
 * J_RemainingTotals
 */	
public class J_RemainingTotals {
	//Parameters
	private String idNBH;
	Map<OL_EnergyCarriers, Double> remainingECDeliveryCompanies_kWh = new HashMap<>();
	Map<OL_VehicleType, Integer> remainingNumberOfVehiclesCompanies = new HashMap<>();
	private double totalFloorSurfaceAnonymousCompanies_m2 = 0.0;
	private int totalNumberOfAnonymousCompanies = 0;
	
	//Calculated values
	Map<OL_EnergyCarriers, Double> ECDeliveryOfAnonymousCompanies_kWhpm2 = new HashMap<>();
	
	//Distribution support values
	Map<OL_VehicleType, Integer> remainingNumberOfAnonymousCompaniesPerVehicleType= new HashMap<>();

	//Class state
	private boolean isFinalized = false;
	
	
	//TEMPORARY SOLUTION FOR NOW TO CONVERT GAS IN M3, UNTIL LOMBOK PACKAGE IS IN KWH OR OTHER SOLUTION
	private AVGC_data avgc_data;
	
	public J_RemainingTotals(Neighbourhood_data dataNBH, AVGC_data avgc_data) {
		
		//TEMPORARY
		this.avgc_data = avgc_data;
		
		this.idNBH = dataNBH.districtname();
		
    	if(dataNBH.total_comp_elec_delivery_kwh_p_yr() != null && dataNBH.total_comp_elec_delivery_kwh_p_yr() >= 0){
    		this.remainingECDeliveryCompanies_kWh.put(OL_EnergyCarriers.ELECTRICITY, dataNBH.total_comp_elec_delivery_kwh_p_yr());
    	}
    	if(dataNBH.total_comp_gas_delivery_m3_p_yr() != null && dataNBH.total_comp_gas_delivery_m3_p_yr() >= 0){	
    		this.remainingECDeliveryCompanies_kWh.put(OL_EnergyCarriers.METHANE, dataNBH.total_comp_gas_delivery_m3_p_yr() * this.avgc_data.p_gas_kWhpm3);
        }
    	if(dataNBH.total_nr_comp_cars() != null && dataNBH.total_nr_comp_cars() >= 0){	
    		this.remainingNumberOfVehiclesCompanies.put(OL_VehicleType.CAR, dataNBH.total_nr_comp_cars());
    	}
    	if(dataNBH.total_nr_comp_vans() != null && dataNBH.total_nr_comp_vans() >= 0){	
    		this.remainingNumberOfVehiclesCompanies.put(OL_VehicleType.VAN, dataNBH.total_nr_comp_vans());
    	}
    	if(dataNBH.total_nr_comp_trucks() != null && dataNBH.total_nr_comp_trucks() >= 0){	
    		this.remainingNumberOfVehiclesCompanies.put(OL_VehicleType.TRUCK, dataNBH.total_nr_comp_trucks());
    	}
	}
	
	////Setters for finalization
	public void setECDeliveryOfAnonymousCompanies_kWhpm2(OL_EnergyCarriers EC, double value_kWh) {
		if(this.isFinalized) {
			throw new RuntimeException("Trying to setECDeliveryOfAnonymousCompanies_kWhpm2 while class is finalized");
		}
		if(ECDeliveryOfAnonymousCompanies_kWhpm2.containsKey(EC)) {
			throw new RuntimeException("Setting ECDeliveryOfAnonymousCompanies_kWhpm2 for " + EC + " for a second time!");
		}
		this.ECDeliveryOfAnonymousCompanies_kWhpm2.put(EC, value_kWh);
    }
	
	public void setStartingNumberOfAnonymousCompaniesForVehicleDistribution() {
		if(this.isFinalized) {
			throw new RuntimeException("Trying to setStartingNumberOfAnonymousCompaniesForVehicleDistribution while class is finalized");
		}
		//Set the starting number of anonymous companies for the vehicle distribution
		for(OL_VehicleType vehicleType : remainingNumberOfVehiclesCompanies.keySet()) {
			remainingNumberOfAnonymousCompaniesPerVehicleType.put(vehicleType, this.totalNumberOfAnonymousCompanies);
		}
	}
	
	public void finalize() {
		//Finalize the maps
		this.remainingECDeliveryCompanies_kWh = Collections.unmodifiableMap(this.remainingECDeliveryCompanies_kWh);
		this.ECDeliveryOfAnonymousCompanies_kWhpm2 = Collections.unmodifiableMap(this.ECDeliveryOfAnonymousCompanies_kWhpm2);
		
		this.isFinalized = true;
	}
	
	
	////Adjustment functions
	public void adjustTotalFloorSurfaceAnonymousCompanies_m2(double deltaFloorSurface_m2) {
		if(this.isFinalized) {
			throw new RuntimeException("Trying to adjustTotalFloorSurfaceAnonymousCompanies_m2 while class is finalized");
		}
		this.totalFloorSurfaceAnonymousCompanies_m2 += deltaFloorSurface_m2;
    }
	
	public void adjustTotalNumberOfAnonymousCompanies(int deltaNumberOfAnonymousCompanies) {
		if(this.isFinalized) {
			throw new RuntimeException("Trying to adjustTotalNumberOfAnonymousCompanies while class is finalized");
		}
		this.totalNumberOfAnonymousCompanies += deltaNumberOfAnonymousCompanies;
    }
	
	public void adjustRemainingECDeliveryCompanies_kWh(OL_EnergyCarriers EC, double adjustment_kWh) {
		if(this.isFinalized) {
			throw new RuntimeException("Trying to adjustRemainingECDeliveryCompanies_kWh while class is finalized");
		}
		remainingECDeliveryCompanies_kWh.put(EC, this.remainingECDeliveryCompanies_kWh.get(EC) + adjustment_kWh);
	}
	
	public void adjustRemainingNumberOfVehiclesCompanies(OL_VehicleType vehicleType, int deltaNumberOfVehicles) {
		this.remainingNumberOfVehiclesCompanies.put(vehicleType, this.remainingNumberOfVehiclesCompanies.get(vehicleType) + deltaNumberOfVehicles);
    }
	
	public void adjustRemainingNumberOfAnonymousCompaniesForVehicleType(OL_VehicleType vehicleType, int deltaNumberOfAnonymousCompanies) {
		this.remainingNumberOfAnonymousCompaniesPerVehicleType.put(vehicleType, this.remainingNumberOfAnonymousCompaniesPerVehicleType.get(vehicleType) + deltaNumberOfAnonymousCompanies);
    }
    
	
	////Getters
	public String getIdNBH() {
		return this.idNBH;
	}
	public double getTotalFloorSurfaceAnonymousCompanies_m2() {
		return this.totalFloorSurfaceAnonymousCompanies_m2;
    }
	public int getTotalNumberOfAnonymousCompanies() {
		return this.totalNumberOfAnonymousCompanies;
    }
	
	public Double getRemainingECDeliveryCompanies_kWh(OL_EnergyCarriers EC) {
		return this.remainingECDeliveryCompanies_kWh.get(EC);
	}
	
	public Double getECDeliveryOfAnonymousCompanies_kWhpm2(OL_EnergyCarriers EC) {
		return this.ECDeliveryOfAnonymousCompanies_kWhpm2.get(EC);
	}
	
	public Integer getRemainingNumberOfVehiclesCompanies(OL_VehicleType vehicleType) {
		return this.remainingNumberOfVehiclesCompanies.get(vehicleType);
	}
	public Integer getRemainingNumberOfAnonymousCompaniesForVehicleType(OL_VehicleType vehicleType) {
		return this.remainingNumberOfAnonymousCompaniesPerVehicleType.get(vehicleType);
	}
	

	@Override
	public String toString() {
		String completeToString = idNBH + ":";
		for(OL_EnergyCarriers EC : remainingECDeliveryCompanies_kWh.keySet()) {
			if(this.remainingECDeliveryCompanies_kWh.get(EC) != null) {
				completeToString += "\n" + "remaining " + EC + " DeliveryCompanies: " + this.remainingECDeliveryCompanies_kWh.get(EC) + " kWh";
			}
		}
		for(OL_EnergyCarriers EC : ECDeliveryOfAnonymousCompanies_kWhpm2.keySet()) {
			if(this.ECDeliveryOfAnonymousCompanies_kWhpm2.get(EC) != null) {
				completeToString += "\n" + EC + " Delivery Of Anonymous Companies: " + this.ECDeliveryOfAnonymousCompanies_kWhpm2.get(EC) + " kWh/m2";
			}
		}
		for(OL_VehicleType vehicleType : remainingNumberOfVehiclesCompanies.keySet()) {
			if(this.remainingNumberOfVehiclesCompanies.get(vehicleType) != null) {
				completeToString += "\n" + "remainingNumberOf" + vehicleType + "Companies: " + this.remainingNumberOfVehiclesCompanies.get(vehicleType);
			}
		}
		completeToString += "\n" + "totalFloorSurfaceAnonymousCompanies_m2: " + this.totalFloorSurfaceAnonymousCompanies_m2;
		completeToString += "\n" + "totalNumberOfAnonymousCompanies: " + this.totalNumberOfAnonymousCompanies;
		return completeToString;
	}
}