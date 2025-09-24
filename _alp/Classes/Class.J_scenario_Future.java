/**
 * Scenario_future
 */	

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;


@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // ✅ only public fields are serialized
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"  // 👈 this will be the field name in your JSON
	)
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

public class J_scenario_Future implements Serializable {

	private Agent parentAgent;
	private boolean isActiveInFuture = true; //Boolean used to see if gc is active in future scenario
	private Double requestedContractDeliveryCapacity_kW = 0.0;
	private Double requestedContractFeedinCapacity_kW = 0.0;
	private Double requestedPhysicalConnectionCapacity_kW = 0.0;
	private double plannedHeatSavings = 0;
	private OL_GridConnectionHeatingType plannedHeatingType = OL_GridConnectionHeatingType.NONE;
	private double plannedElectricitySavings = 0;
	private boolean plannedCurtailment = false;
	private Integer plannedPV_kW = 0;
	private Integer plannedPV_year;
	private Float plannedWind_kW = 0f;
	private Float plannedBatteryPower_kW = 0f;
	private Float plannedBatteryCapacity_kWh = 0f;
	//Integer plannedWind_year;
	private double plannedTransportSavings = 0;
	private Integer plannedEVCars = 0;
	private Integer plannedEVVans = 0;
	private Integer plannedEVTrucks = 0;
	private Integer plannedHydrogenCars = 0;
	private Integer plannedHydrogenVans = 0;
	private Integer plannedHydrogenTrucks = 0; 

	
		
    /**
     * Default constructor
     */
    public J_scenario_Future() {
    }
    
    /**
     * Constructor initializing the fields
     */
    /*
    public J_scenario_Future(Agent parentAgent, Integer plannedPV_kW, Integer plannedPV_year, Float plannedWind_kW, Integer plannedEVCars, Integer plannedEVVans, Integer plannedEVTrucks) {

		
    }
    */
    
    // Setters
    public void setParentAgent(Agent parentAgent) {
        this.parentAgent = parentAgent;
    }
    
    public void setIsActiveInFuture(boolean isActiveInFuture) {
        this.isActiveInFuture = isActiveInFuture;
    }

    public void setRequestedContractDeliveryCapacity_kW(Double requestedContractDeliveryCapacity_kW) {
        this.requestedContractDeliveryCapacity_kW = requestedContractDeliveryCapacity_kW;
    }
    
    public void setRequestedContractFeedinCapacity_kW(Double requestedContractFeedinCapacity_kW) {
        this.requestedContractFeedinCapacity_kW = requestedContractFeedinCapacity_kW;
    }
    
    public void setRequestedPhysicalConnectionCapacity_kW(Double requestedPhysicalConnectionCapacity_kW) {
        this.requestedPhysicalConnectionCapacity_kW = requestedPhysicalConnectionCapacity_kW;
    }
    
    public void setPlannedHeatSavings(double plannedHeatSavings) {
    	this.plannedHeatSavings = plannedHeatSavings;
    }
    
    public void setPlannedHeatingType(OL_GridConnectionHeatingType plannedHeatingType) {
    	this.plannedHeatingType = plannedHeatingType;
    }
    
    public void setPlannedElectricitySavings(double plannedElectricitySavings) {
    	this.plannedElectricitySavings = plannedElectricitySavings;
    }
    
    public void setPlannedCurtailment(boolean plannedCurtailment) {
    	this.plannedCurtailment = plannedCurtailment;
    }
    
    public void setPlannedPV_kW(Integer plannedPV_kW) {
        this.plannedPV_kW = plannedPV_kW;
    }

    public void setPlannedPV_year(Integer plannedPV_year) {
        this.plannedPV_year = plannedPV_year;
    }

    public void setPlannedWind_kW(Float plannedWind_kW) {
        this.plannedWind_kW = plannedWind_kW;
    }

    public void setPlannedBatteryPower_kW(Float plannedBatteryPower_kW) {
        this.plannedBatteryPower_kW = plannedBatteryPower_kW;
    }

    public void setPlannedBatteryCapacity_kWh(Float plannedBatteryCapacity_kWh) {
        this.plannedBatteryCapacity_kWh = plannedBatteryCapacity_kWh;
    }
    
    public void setPlannedTransportSavings(double plannedTransportSavings) {
    	this.plannedTransportSavings = plannedTransportSavings;
    }
    
    public void setPlannedEVCars(Integer plannedEVCars) {
        this.plannedEVCars = plannedEVCars;
    }

    public void setPlannedEVVans(Integer plannedEVVans) {
        this.plannedEVVans = plannedEVVans;
    }

    public void setPlannedEVTrucks(Integer plannedEVTrucks) {
        this.plannedEVTrucks = plannedEVTrucks;
    }

    public void setPlannedHydrogenCars(Integer plannedHydrogenCars) {
        this.plannedHydrogenCars = plannedHydrogenCars;
    }

    public void setPlannedHydrogenVans(Integer plannedHydrogenVans) {
        this.plannedHydrogenVans = plannedHydrogenVans;
    }

    public void setPlannedHydrogenTrucks(Integer plannedHydrogenTrucks) {
        this.plannedHydrogenTrucks = plannedHydrogenTrucks;
    }
    
    
    // Getters
    public Agent getParentAgent() {
        return parentAgent;
    }
    
    public boolean getIsActiveInFuture() {
        return isActiveInFuture;
    }
    
    public Double getRequestedContractDeliveryCapacity_kW() {
        return requestedContractDeliveryCapacity_kW;
    } 
    
    public Double getRequestedContractFeedinCapacity_kW() {
        return requestedContractFeedinCapacity_kW;
    } 

    public Double getRequestedPhysicalConnectionCapacity_kW() {
    	return requestedPhysicalConnectionCapacity_kW;
    }
    
    public double getPlannedHeatSavings() {
    	return plannedHeatSavings;
    }
    
    public OL_GridConnectionHeatingType getPlannedHeatingType() {
    	return plannedHeatingType;
    }
    
    public double getPlannedElectricitySavings() {
    	return plannedElectricitySavings;
    } 
    
    public boolean getPlannedCurtailment() {
    	return plannedCurtailment;
    }
    
    public Integer getPlannedPV_kW() {
    	if (plannedPV_kW == null) {
    		return 0;
    	}
        return plannedPV_kW;
    }

    public Integer getPlannedPV_year() {
        return plannedPV_year;
    }

    public Float getPlannedWind_kW() {
        return plannedWind_kW;
    }

    public Float getPlannedBatteryPower_kW() {
    	if (plannedBatteryPower_kW == null) {
    		return 0f;
    	}
        return plannedBatteryPower_kW;
    }
    
    public Float getPlannedBatteryCapacity_kWh() {
    	if (plannedBatteryCapacity_kWh == null) {
    		return 0f;
    	}
        return plannedBatteryCapacity_kWh;
    }
    
    public double getPlannedTransportSavings() {
    	return plannedTransportSavings;
    }
    
    public Integer getPlannedEVCars() {
        return plannedEVCars;
    }

    public Integer getPlannedEVVans() {
        return plannedEVVans;
    }

    public Integer getPlannedEVTrucks() {
        return plannedEVTrucks;
    }
    
    public Integer getPlannedHydrogenCars() {
        return plannedHydrogenCars;
    }

    public Integer getPlannedHydrogenVans() {
        return plannedHydrogenVans;
    }

    public Integer getPlannedHydrogenTrucks() {
        return plannedHydrogenTrucks;
    }
    
    
       
	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}