/**
 * J_SliderSettings_Residential
 */	
public class J_SliderSettings_Residential {
	
	////Electricity
	private double housesWithPV_pct;
	private double pvHousesWithBattery_pct;
	private double cooking_pct;
	private double electricityDemandIncrease_pct;
	private double averageNeighbourhoodBatterySize_kWh;

	////Heating
	private double housesWithGasBurners_pct;
	private double housesWithHybridHeatpump_pct;
	private double housesWithElectricHeatpump_pct;
	private boolean cb_householdHTDistrictHeatingActive;
	private boolean cb_householdLTDistrictHeatingActive;
	private double housesWithAirco_pct;
	private double housesWithImprovedInsulation_pct;
	private double nbHousesWithPT_pct;

	////Mobility
	private double privateEVs_pct;
	private double privateEVsThatSupportV2G_pct;
	private String selectedChargingAttitudeStringPrivateEVs;
	private boolean V2GActivePrivateEVs;
	private double activePublicChargers_pct;
	private double chargersV1G_pct;
	private double chargersV2G_pct;
	private String selectedChargingAttitudeStringChargers;
	private boolean V2GActiveChargers;
	
	
	
    /**
     * Default constructor
     */
    public J_SliderSettings_Residential() {
    }
	
	// ====== Setters ======
	public void setHousesWithPV_pct(double housesWithPV_pct) {
	    this.housesWithPV_pct = housesWithPV_pct;
	}

	public void setPvHousesWithBattery_pct(double pvHousesWithBattery_pct) {
	    this.pvHousesWithBattery_pct = pvHousesWithBattery_pct;
	}

	public void setCooking_pct(double cooking_pct) {
	    this.cooking_pct = cooking_pct;
	}

	public void setElectricityDemandIncrease_pct(double electricityDemandIncrease_pct) {
	    this.electricityDemandIncrease_pct = electricityDemandIncrease_pct;
	}

	public void setAverageNeighbourhoodBatterySize_kWh(double averageNeighbourhoodBatterySize_kWh) {
	    this.averageNeighbourhoodBatterySize_kWh = averageNeighbourhoodBatterySize_kWh;
	}

	// Heating
	public void setHousesWithGasBurners_pct(double housesWithGasBurners_pct) {
	    this.housesWithGasBurners_pct = housesWithGasBurners_pct;
	}

	public void setHousesWithHybridHeatpump_pct(double housesWithHybridHeatpump_pct) {
	    this.housesWithHybridHeatpump_pct = housesWithHybridHeatpump_pct;
	}

	public void setHousesWithElectricHeatpump_pct(double housesWithElectricHeatpump_pct) {
	    this.housesWithElectricHeatpump_pct = housesWithElectricHeatpump_pct;
	}

	public void setCb_householdHTDistrictHeatingActive(boolean cb_householdHTDistrictHeatingActive) {
	    this.cb_householdHTDistrictHeatingActive = cb_householdHTDistrictHeatingActive;
	}

	public void setCb_householdLTDistrictHeatingActive(boolean cb_householdLTDistrictHeatingActive) {
	    this.cb_householdLTDistrictHeatingActive = cb_householdLTDistrictHeatingActive;
	}

	public void setHousesWithAirco_pct(double housesWithAirco_pct) {
	    this.housesWithAirco_pct = housesWithAirco_pct;
	}

	public void setHousesWithImprovedInsulation_pct(double housesWithImprovedInsulation_pct) {
	    this.housesWithImprovedInsulation_pct = housesWithImprovedInsulation_pct;
	}

	public void setNbHousesWithPT_pct(double nbHousesWithPT_pct) {
	    this.nbHousesWithPT_pct = nbHousesWithPT_pct;
	}

	// Mobility
	public void setPrivateEVs_pct(double privateEVs_pct) {
	    this.privateEVs_pct = privateEVs_pct;
	}

	public void setPrivateEVsThatSupportV2G_pct(double privateEVsThatSupportV2G_pct) {
	    this.privateEVsThatSupportV2G_pct = privateEVsThatSupportV2G_pct;
	}

	public void setSelectedChargingAttitudeStringPrivateEVs(String selectedChargingAttitudeStringPrivateEVs) {
	    this.selectedChargingAttitudeStringPrivateEVs = selectedChargingAttitudeStringPrivateEVs;
	}

	public void setV2GActivePrivateEVs(boolean V2GActivePrivateEVs) {
	    this.V2GActivePrivateEVs = V2GActivePrivateEVs;
	}

	public void setActivePublicChargers_pct(double activePublicChargers_pct) {
	    this.activePublicChargers_pct = activePublicChargers_pct;
	}

	public void setChargersV1G_pct(double chargersV1G_pct) {
	    this.chargersV1G_pct = chargersV1G_pct;
	}

	public void setChargersV2G_pct(double chargersV2G_pct) {
	    this.chargersV2G_pct = chargersV2G_pct;
	}

	public void setSelectedChargingAttitudeStringChargers(String selectedChargingAttitudeStringChargers) {
	    this.selectedChargingAttitudeStringChargers = selectedChargingAttitudeStringChargers;
	}

	public void setV2GActiveChargers(boolean V2GActiveChargers) {
	    this.V2GActiveChargers = V2GActiveChargers;
	}

	// ====== Getters ======
	public double getHousesWithPV_pct() {
	    return housesWithPV_pct;
	}

	public double getPvHousesWithBattery_pct() {
	    return pvHousesWithBattery_pct;
	}

	public double getCooking_pct() {
	    return cooking_pct;
	}

	public double getElectricityDemandIncrease_pct() {
	    return electricityDemandIncrease_pct;
	}

	public double getAverageNeighbourhoodBatterySize_kWh() {
	    return averageNeighbourhoodBatterySize_kWh;
	}

	// Heating
	public double getHousesWithGasBurners_pct() {
	    return housesWithGasBurners_pct;
	}

	public double getHousesWithHybridHeatpump_pct() {
	    return housesWithHybridHeatpump_pct;
	}

	public double getHousesWithElectricHeatpump_pct() {
	    return housesWithElectricHeatpump_pct;
	}

	public boolean getCb_householdHTDistrictHeatingActive() {
	    return cb_householdHTDistrictHeatingActive;
	}

	public boolean getCb_householdLTDistrictHeatingActive() {
	    return cb_householdLTDistrictHeatingActive;
	}

	public double getHousesWithAirco_pct() {
	    return housesWithAirco_pct;
	}

	public double getHousesWithImprovedInsulation_pct() {
	    return housesWithImprovedInsulation_pct;
	}

	public double getNbHousesWithPT_pct() {
	    return nbHousesWithPT_pct;
	}

	// Mobility
	public double getPrivateEVs_pct() {
	    return privateEVs_pct;
	}

	public double getPrivateEVsThatSupportV2G_pct() {
	    return privateEVsThatSupportV2G_pct;
	}

	public String getSelectedChargingAttitudeStringPrivateEVs() {
	    return selectedChargingAttitudeStringPrivateEVs;
	}

	public boolean getV2GActivePrivateEVs() {
	    return V2GActivePrivateEVs;
	}

	public double getActivePublicChargers_pct() {
	    return activePublicChargers_pct;
	}

	public double getChargersV1G_pct() {
	    return chargersV1G_pct;
	}

	public double getChargersV2G_pct() {
	    return chargersV2G_pct;
	}

	public String getSelectedChargingAttitudeStringChargers() {
	    return selectedChargingAttitudeStringChargers;
	}

	public boolean getV2GActiveChargers() {
	    return V2GActiveChargers;
	}
	
}