/**
 * J_GridOperatorTariffsEnexis_2026
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

public class J_GridOperatorTariffsEnexis_2026 implements I_GridOperatorTariffs {
	//Data source: https://www.acm.nl/system/files/documents/tarievenblad-enexis-elektriciteit-2026.xlsx   (Bijlage 2a + 2b bij Tarievenbesluit Elektriciteit 2026 Enexis)
	//			&& https://www.enexis.nl/tarieven/oudere-tarieven  (kleinverbruik tariefblad 2026, meetdienst EUR/dag)
	//GEGENEREERD uit bovenstaande bronnen; structuur gelijk aan de J_GridOperatorTariffsEnexis_2025 klasse.

	double vat_fr = 0.21; // 21% BTW

	double measurementServiceCostSmallConnection_eurpday = 0.0681;	//meetdienst, gepubliceerd per dag
	double transportCostsVastRecht_smallConnections_eurpday = 18.0/365;	//Vastrecht transportdienst t/m 3*80A op LS.

	Map<String, Double> periodicalPhyscialConnectionCapacityCostsTable_eurpyr = Map.ofEntries(
			Map.entry("t/m 1 x 10 A (onbemeten)", 45.06),
			Map.entry("t/m 1 x 10 A", 45.06 + 24.8565),
			Map.entry("> 1 x 10 A t/m 3 x 25 A / 1 x 80 A (onbemeten)", 45.06),
			Map.entry("> 1 x 10 A t/m 3 x 25 A / 1 x 80 A", 45.06 + 24.8565),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", 59.34 + 24.8565),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", 59.34 + 24.8565),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", 59.34 + 24.8565),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", 59.34 + 24.8565),
			Map.entry("> 3 x 80 A t/m 3 x 250 A (173 kVA)", 401.0),
			Map.entry("> 3 x 250 A (173 kVA) t/m 1.750 kVA", 1742.0),
			Map.entry("> 1.750 kVA t/m 3 MVA", 4804.0),
			Map.entry("> 3 MVA t/m 6 MVA", 4804.0),
			Map.entry("> 6 MVA t/m 10 MVA", 5638.0),
			Map.entry("> 10 MVA", 5638.0),
			Map.entry("> 6 MVA N-0 t/m 10 MVA N-0", 5638.0)
			);

	Map<String, Double> transportCostsTable_smallConnections_eurpday = Map.ofEntries(
			Map.entry("t/m 1 x 10 A (onbemeten)", 38.17/365),
			Map.entry("t/m 1 x 10 A", 38.17/365),
			Map.entry("> 1 x 10 A t/m 3 x 25 A / 1 x 80 A (onbemeten)", 305.36/365),
			Map.entry("> 1 x 10 A t/m 3 x 25 A / 1 x 80 A", 305.36/365),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", 1526.8/365),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", 2290.2/365),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", 3053.6/365),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", 3817.0/365)
			);

	Map<String, Double> transportCostsTable_largeConnections_normalTarif_eurpkWh = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0788),
			Map.entry("MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)", 0.0247),
			Map.entry("MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)", 0.0247),
			Map.entry("MS-D > 1500 kW", 0.0247),
			Map.entry("MS-T > 1500 kW", 0.015),
			Map.entry("HS/MS > 1500 kW", 0.0),
			Map.entry("TS > 1500 kW", 0.0)
			);
	Map<String, Double> transportCostsTable_largeConnections_lowTarif_eurpkWh = Map.ofEntries( //Between 23:00 and 7:00
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0413),
			Map.entry("MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)", 0.0247),
			Map.entry("MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)", 0.0247),
			Map.entry("MS-D > 1500 kW", 0.0247),
			Map.entry("MS-T > 1500 kW", 0.015),
			Map.entry("HS/MS > 1500 kW", 0.0),
			Map.entry("TS > 1500 kW", 0.0)
			);
	Map<String, Double> maxPeakPowerCostsTable_eurpkWpmonth = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0),
			Map.entry("MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)", 3.66),
			Map.entry("MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)", 3.66),
			Map.entry("MS-D > 1500 kW", 3.66),
			Map.entry("MS-T > 1500 kW", 3.06),
			Map.entry("HS/MS > 1500 kW", 4.46),
			Map.entry("TS > 1500 kW", 3.62)
			);
	Map<String, Double> contractCapacityCostsTable_eurpkWpyr = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 15.87),
			Map.entry("MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)", 49.26),
			Map.entry("MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)", 28.91),
			Map.entry("MS-D > 1500 kW", 28.91),
			Map.entry("MS-T > 1500 kW", 27.37),
			Map.entry("HS/MS > 1500 kW", 41.87),
			Map.entry("TS > 1500 kW", 30.94)
			);
	Map<String, Double> baseContractCapacityCostsTable_eurpyr = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 18.0),
			Map.entry("MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)", 441.0),
			Map.entry("MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)", 441.0),
			Map.entry("MS-D > 1500 kW", 441.0),
			Map.entry("MS-T > 1500 kW", 441.0),
			Map.entry("HS/MS > 1500 kW", 2760.0),
			Map.entry("TS > 1500 kW", 2760.0)
			);

	/**
     * Default constructor
     */
    public J_GridOperatorTariffsEnexis_2026() {
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
    	boolean everythingIsMeasured = true;
    	boolean connectionIsMeasured = everythingIsMeasured; // connectionMetaData.getConnectionIsMeasured();
    	double physicalCapacity_kW = connectionMetaData.getPhysicalCapacity_kW();
    	if(physicalCapacity_kW <=2.3 && !connectionIsMeasured) {
    		keyString = "t/m 1 x 10 A (onbemeten)";
    	}
    	else if(physicalCapacity_kW <=2.3) {
    		keyString = "t/m 1 x 10 A";
    	}
    	else if(physicalCapacity_kW <=17.25 && !connectionIsMeasured) {
    		keyString = "> 1 x 10 A t/m 3 x 25 A / 1 x 80 A (onbemeten)";
    	}
    	else if(physicalCapacity_kW <=17.25) {
    		keyString = "> 1 x 10 A t/m 3 x 25 A / 1 x 80 A";
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
    	else if(physicalCapacity_kW <=173) {
    		keyString = "> 3 x 80 A t/m 3 x 250 A (173 kVA)";
    	}
    	else if(physicalCapacity_kW <=1750) {
    		keyString = "> 3 x 250 A (173 kVA) t/m 1.750 kVA";
    	}
    	else if(physicalCapacity_kW <=3000) {
    		keyString = "> 1.750 kVA t/m 3 MVA";
    	}
    	else if(physicalCapacity_kW <=6000) {
    		keyString = "> 3 MVA t/m 6 MVA";
    	}
    	else if(physicalCapacity_kW <=10000) {
    		keyString = "> 6 MVA t/m 10 MVA";
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
    	else if(contractCapacityUsedForCalculation_kW <=125) {
    		keyString = "MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)";
    	}
    	else if(contractCapacityUsedForCalculation_kW <=1500) {
    		keyString = "MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)";
    	}
    	//Boven 1500 kW splitst de ACM op fysieke aansluitwijze. Benaderd met fysieke capaciteit:
    	else if(physicalCapacity_kW <=1_750) {	// Op basis van onderzoek, tot 1750 KVA is MS-D
    		keyString = "MS-D > 1500 kW";
    	}
    	else if(physicalCapacity_kW <=6_000) {	// Volgens mij is tot 6000 MS-T (Oud project was dat zo.)
    		keyString = "MS-T > 1500 kW";
    	}
    	else if(physicalCapacity_kW <=100_000) {	
    		keyString = "HS/MS > 1500 kW";
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
