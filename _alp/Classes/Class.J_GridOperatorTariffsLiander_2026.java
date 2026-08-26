/**
 * J_GridOperatorTariffsLiander_2026
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

public class J_GridOperatorTariffsLiander_2026 implements I_GridOperatorTariffs {
	//Data source: https://www.acm.nl/system/files/documents/tarievenblad-liander-elektriciteit-2026.xlsx   (Bijlage 2a + 2b bij Tarievenbesluit Elektriciteit 2026 Liander)
	//			&& https://www.liander.nl/tarieven  (kleinverbruik tariefblad 2026, sectie Meetdienst)
	//GEGENEREERD uit bovenstaande bronnen; structuur gelijk aan de J_GridOperatorTariffsLiander_2025 klasse.

	double vat_fr = 0.21; // 21% BTW

	double measurementServiceCostSmallConnection_eurpday = 38.0695/365;	//meetdienst 38.0695 EUR/jaar
	double transportCostsVastRecht_smallConnections_eurpday = 17.9945/365;	//Vastrecht transportdienst t/m 3*80A op LS.

	Map<String, Double> periodicalPhyscialConnectionCapacityCostsTable_eurpyr = Map.ofEntries(
			Map.entry("t/m 1 x 6 A (geschakeld)", 16.06 + 38.0695),
			Map.entry("t/m 1 x 10 A", 41.172 + 38.0695),
			Map.entry("> 1 x 10 A t/m 3 x 25 A", 41.172 + 38.0695),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", 54.8595 + 38.0695),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", 54.8595 + 38.0695),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", 61.8675 + 38.0695),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", 61.8675 + 38.0695),
			Map.entry("> 3 x 80 A t/m 100 kVA", 265.08),
			Map.entry("> 100 kVA t/m 160 kVA", 297.48),
			Map.entry("> 160 kVA t/m 1.000 kVA", 1063.44),
			Map.entry("> 1.000 kVA t/m 2 MVA", 1946.04),
			Map.entry("> 2 MVA t/m 5 MVA", 13284.0),
			Map.entry("> 5 MVA t/m 10 MVA", 15900.0),
			Map.entry("> 10 MVA", 15900.0)
			);

	Map<String, Double> transportCostsTable_smallConnections_eurpday = Map.ofEntries(
			Map.entry("t/m 1 x 6 A (geschakeld)", 3.723/365),
			Map.entry("t/m 1 x 10 A", 37.23/365),
			Map.entry("> 1 x 10 A t/m 3 x 25 A", 297.84/365),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", 1489.2/365),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", 2233.8/365),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", 2978.4/365),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", 3723.0/365)
			);

	Map<String, Double> transportCostsTable_largeConnections_normalTarif_eurpkWh = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0806),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)", 0.0226),
			Map.entry("MS (contract vermogen meer dan 136 kW t/m 2 MW)", 0.0226),
			Map.entry("Trafo HS+TS/MS > 2 MW", 0.0),
			Map.entry("TS > 2 MW", 0.0)
			);
	Map<String, Double> transportCostsTable_largeConnections_lowTarif_eurpkWh = Map.ofEntries( //Between 23:00 and 7:00
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.043),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)", 0.0226),
			Map.entry("MS (contract vermogen meer dan 136 kW t/m 2 MW)", 0.0226),
			Map.entry("Trafo HS+TS/MS > 2 MW", 0.0),
			Map.entry("TS > 2 MW", 0.0)
			);
	Map<String, Double> maxPeakPowerCostsTable_eurpkWpmonth = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)", 3.57),
			Map.entry("MS (contract vermogen meer dan 136 kW t/m 2 MW)", 3.57),
			Map.entry("Trafo HS+TS/MS > 2 MW", 6.24),
			Map.entry("TS > 2 MW", 5.39)
			);
	Map<String, Double> contractCapacityCostsTable_eurpkWpyr = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 17.88),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)", 44.88),
			Map.entry("MS (contract vermogen meer dan 136 kW t/m 2 MW)", 27.48),
			Map.entry("Trafo HS+TS/MS > 2 MW", 47.88),
			Map.entry("TS > 2 MW", 47.16)
			);
	Map<String, Double> baseContractCapacityCostsTable_eurpyr = Map.ofEntries(
			Map.entry("LS (contract vermogen t/m 50 kW)", 18.0),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)", 441.0),
			Map.entry("MS (contract vermogen meer dan 136 kW t/m 2 MW)", 441.0),
			Map.entry("Trafo HS+TS/MS > 2 MW", 2760.0),
			Map.entry("TS > 2 MW", 2760.0)
			);

	/**
     * Default constructor
     */
    public J_GridOperatorTariffsLiander_2026() {
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
    	else if(physicalCapacity_kW <=100) {
    		keyString = "> 3 x 80 A t/m 100 kVA";
    	}
    	else if(physicalCapacity_kW <=160) {
    		keyString = "> 100 kVA t/m 160 kVA";
    	}
    	else if(physicalCapacity_kW <=1000) {
    		keyString = "> 160 kVA t/m 1.000 kVA";
    	}
    	else if(physicalCapacity_kW <=2000) {
    		keyString = "> 1.000 kVA t/m 2 MVA";
    	}
    	else if(physicalCapacity_kW <=5000) {
    		keyString = "> 2 MVA t/m 5 MVA";
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
    	else if(contractCapacityUsedForCalculation_kW <=136) {
    		keyString = "Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)";
    	}
    	else if(contractCapacityUsedForCalculation_kW <=2000) {
    		keyString = "MS (contract vermogen meer dan 136 kW t/m 2 MW)";
    	}
    	//Boven 2 MW splitst de ACM op fysieke aansluitwijze (1-25 kV / 25-50 kV / 110-150 kV). Benaderd met fysieke capaciteit:
    	else if(physicalCapacity_kW <=100_000) {	// t/m 10 MVA is de bovenste PAV-staffel van Liander -> aangesloten via trafo op MS.
    		keyString = "Trafo HS+TS/MS > 2 MW";
    	}
    	else{
    		keyString = "TS > 2 MW";
    	}
    	return keyString;
    }



    @Override
	public String toString() {
		return super.toString();
	}

}
