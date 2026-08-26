/**
 * J_GridOperatorTariffsStedin_2026
 */

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    //
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

public class J_GridOperatorTariffsStedin_2026 implements I_GridOperatorTariffs {
	//Data source: https://www.acm.nl/system/files/documents/tarievenblad-stedin-elektriciteit-2026.xlsx   (Bijlage 2a + 2b bij Tarievenbesluit Elektriciteit 2026 Stedin)
	//			&& https://www.stedin.net/tarieven/download-tarieven  (kleinverbruik tariefblad 2026, tabel Metertarieven)
	//GEGENEREERD uit bovenstaande bronnen; structuur gelijk aan de J_GridOperatorTariffsStedin_2025 klasse.

	double vat_fr = 0.21; // 21% BTW

	double measurementServiceCostSmallConnection_eurpday = 38.11/365;	//meetdienst 38.11 EUR/jaar
	double transportCostsVastRecht_smallConnections_eurpday = 18.0/365;	//Vastrecht transportdienst t/m 3*80A op LS.

	Map<String, Double> periodicalPhyscialConnectionCapacityCostsTable_eurpyr = Map.ofEntries(
			Map.entry("t/m 1 x 6 A (geschakeld)", 18.81 + 38.11),
			Map.entry("t/m 1 x 10 A", 46.15 + 38.11),
			Map.entry("> 1 x 10 A t/m 3 x 25 A", 46.15 + 38.11),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", 75.4 + 38.11),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", 75.4 + 38.11),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", 75.4 + 38.11),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", 75.4 + 38.11),
			Map.entry("> 3 x 80 A t/m 175 kVA", 171.84),
			Map.entry("> 175 kVA t/m 1.750 kVA", 1505.0),
			Map.entry("> 1.750 kVA t/m 5 MVA", 3958.95),
			Map.entry("> 5 MVA t/m 10 MVA", 17149.08),
			Map.entry("> 10 MVA", 17149.08)
			);

	Map<String, Double> transportCostsTable_smallConnections_eurpday = Map.ofEntries(
			Map.entry("t/m 1 x 6 A (geschakeld)", 3.647/365),
			Map.entry("t/m 1 x 10 A", 36.47/365),
			Map.entry("> 1 x 10 A t/m 3 x 25 A", 291.76/365),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", 1458.8/365),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", 2188.2/365),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", 2917.6/365),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", 3647.0/365)
			);

	Map<String, Double> transportCostsTable_largeConnections_normalTarif_eurpkWh = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0749),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 150 kW)", 0.0198),
			Map.entry("MS (contract vermogen meer dan 150 kW t/m 1500 kW)", 0.0198),
			Map.entry("Trafo HS+TS/MS > 1500 kW", 0.0),
			Map.entry("TS > 1500 kW", 0.0)
			);
	Map<String, Double> transportCostsTable_largeConnections_lowTarif_eurpkWh = Map.ofEntries( //Between 23:00 and 7:00
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.046),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 150 kW)", 0.0198),
			Map.entry("MS (contract vermogen meer dan 150 kW t/m 1500 kW)", 0.0198),
			Map.entry("Trafo HS+TS/MS > 1500 kW", 0.0),
			Map.entry("TS > 1500 kW", 0.0)
			);
	Map<String, Double> maxPeakPowerCostsTable_eurpkWpmonth = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 150 kW)", 3.0966),
			Map.entry("MS (contract vermogen meer dan 150 kW t/m 1500 kW)", 3.0966),
			Map.entry("Trafo HS+TS/MS > 1500 kW", 5.2943),
			Map.entry("TS > 1500 kW", 4.1455)
			);
	Map<String, Double> contractCapacityCostsTable_eurpkWpyr = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 18.5799),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 150 kW)", 47.169),
			Map.entry("MS (contract vermogen meer dan 150 kW t/m 1500 kW)", 24.2737),
			Map.entry("Trafo HS+TS/MS > 1500 kW", 45.4508),
			Map.entry("TS > 1500 kW", 37.4994)
			);
	Map<String, Double> baseContractCapacityCostsTable_eurpyr = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 18.0),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 150 kW)", 441.0),
			Map.entry("MS (contract vermogen meer dan 150 kW t/m 1500 kW)", 441.0),
			Map.entry("Trafo HS+TS/MS > 1500 kW", 2760.0),
			Map.entry("TS > 1500 kW", 2760.0)
			);

	/**
     * Default constructor
     */
    public J_GridOperatorTariffsStedin_2026() {
    }

    public double getPhysicalCapacityCost_eurpyr(J_ConnectionMetaData connectionMetaData){
    	if(connectionMetaData.getPhysicalCapacity_kW() <= 0) {
    		return 0;
    	}
    	else {
        	return periodicalPhyscialConnectionCapacityCostsTable_eurpyr.get(getPhysicalConnectionKey(connectionMetaData));
    	}
    }

    public double getContractCapacityCost_eurpyr(J_ConnectionMetaData connectionMetaData){
    	if(connectionMetaData.getConnectionSizeType() == OL_ConnectionSizeType.SMALL_CONNECTION) {
    		return 365 * transportCostsVastRecht_smallConnections_eurpday;
    	}
    	else {
    		String keyString = getLargeConnectionContractKey(connectionMetaData);
    		double baseContractCapacityCosts_eurpyr = baseContractCapacityCostsTable_eurpyr.get(keyString);
    		double contractCapacityUsedForCalculation_kW =0;
    		if(connectionMetaData.getContractedDeliveryCapacity_kW() > 0) {
    			contractCapacityUsedForCalculation_kW = connectionMetaData.getContractedDeliveryCapacity_kW(); //Bij zowel afname als teruglevering is de contractwaarde levering leidend.
    		}
    		else if(connectionMetaData.getContractedFeedinCapacity_kW() > 0){
    			contractCapacityUsedForCalculation_kW = connectionMetaData.getContractedFeedinCapacity_kW();
    		}
    		double contractSizeDependendContractCapacityCost = contractCapacityUsedForCalculation_kW * contractCapacityCostsTable_eurpkWpyr.get(keyString);
    		return baseContractCapacityCosts_eurpyr + contractSizeDependendContractCapacityCost;
    	}
    }

    public double getTransportCost_eur(J_ConnectionMetaData connectionMetaData, double transportedElectricity_kWh){
    	if(connectionMetaData.getConnectionSizeType() == OL_ConnectionSizeType.SMALL_CONNECTION) {
    		return 365 * transportCostsTable_smallConnections_eurpday.get(getPhysicalConnectionKey(connectionMetaData));
    	}
    	else { //NOTE: EVERYTHING IS COUNTED AS NORMAL TARIF FOR NOW: ONLY SMALLEST GROUP HAS DIFFERENCE IN COST
    		return transportedElectricity_kWh*transportCostsTable_largeConnections_normalTarif_eurpkWh.get(getLargeConnectionContractKey(connectionMetaData));
    	}
    }

    public double getMonthlyPeakCost_eur(J_ConnectionMetaData connectionMetaData, double monthlyPeakLoad_kW){
    	if(connectionMetaData.getConnectionSizeType() == OL_ConnectionSizeType.SMALL_CONNECTION) {
    		return 0;
    	}
    	else {
    		return monthlyPeakLoad_kW*maxPeakPowerCostsTable_eurpkWpmonth.get(getLargeConnectionContractKey(connectionMetaData));
    	}
    }

    private String getPhysicalConnectionKey(J_ConnectionMetaData connectionMetaData) {
    	String keyString = null;
    	double physicalCapacity_kW = connectionMetaData.getPhysicalCapacity_kW();
    	if(physicalCapacity_kW <=1.38) {
    		keyString = "t/m 1 x 6 A (geschakeld)";
    	}
    	else if(physicalCapacity_kW <=2.3) {
    		keyString = "t/m 1 x 10 A";
    	}
    	else if(physicalCapacity_kW <=17.25) {
    		keyString = "> 1 x 10 A t/m 3 x 25 A";
    	}
    	else if(physicalCapacity_kW <=24.15) {
    		keyString = "> 3 x 25 A t/m 3 x 35 A";
    	}
    	else if(physicalCapacity_kW <=34.5) {
    		keyString = "> 3 x 35 A t/m 3 x 50 A";
    	}
    	else if(physicalCapacity_kW <=43.47) {
    		keyString = "> 3 x 50 A t/m 3 x 63 A";
    	}
    	else if(physicalCapacity_kW <=55.2) {
    		keyString = "> 3 x 63 A t/m 3 x 80 A";
    	}
    	else if(physicalCapacity_kW <=175) {
    		keyString = "> 3 x 80 A t/m 175 kVA";
    	}
    	else if(physicalCapacity_kW <=1750) {
    		keyString = "> 175 kVA t/m 1.750 kVA";
    	}
    	else if(physicalCapacity_kW <=5000) {
    		keyString = "> 1.750 kVA t/m 5 MVA";
    	}
    	else if(physicalCapacity_kW <=10000) {
    		keyString = "> 5 MVA t/m 10 MVA";
    	}
    	else{// if(physicalCapacity_kW >10000) {
    		keyString = "> 10 MVA";
    	}
    	return keyString;
    }

    private String getLargeConnectionContractKey(J_ConnectionMetaData connectionMetaData) {
    	String keyString = null;

    	if(connectionMetaData.getConnectionSizeType() == OL_ConnectionSizeType.SMALL_CONNECTION) {
    		throw new RuntimeException("getLargeConnectionContractKey not useable for small connnection meta data.");
    	}

		double contractCapacityUsedForCalculation_kW =0;
		if(connectionMetaData.getContractedDeliveryCapacity_kW() > 0) {
			contractCapacityUsedForCalculation_kW = connectionMetaData.getContractedDeliveryCapacity_kW();
		}
		else if(connectionMetaData.getContractedFeedinCapacity_kW() > 0){
			contractCapacityUsedForCalculation_kW = connectionMetaData.getContractedFeedinCapacity_kW();
		}
		double physicalCapacity_kW = connectionMetaData.getPhysicalCapacity_kW();

        if(contractCapacityUsedForCalculation_kW <=50) {
    		keyString = "LS (contract vermogen t/m 50 kW)";
    	}
    	else if(contractCapacityUsedForCalculation_kW <=150) {
    		keyString = "Trafo MS/LS (contract vermogen meer dan 50 kW t/m 150 kW)";
    	}
    	else if(contractCapacityUsedForCalculation_kW <=1500) {
    		keyString = "MS (contract vermogen meer dan 150 kW t/m 1500 kW)";
    	}
    	//Boven 1,5 MW splitst de ACM op fysieke aansluitwijze (Trafo HS+TS/MS of TS). Benaderd met fysieke capaciteit:
    	else if(physicalCapacity_kW <=100_000) {	
    		keyString = "Trafo HS+TS/MS > 1500 kW";
    	}
    	else{
    		keyString = "TS > 1500 kW";
    	}
    	return keyString;
    }



    @Override
	public String toString() {
		return super.toString();
	}

}
