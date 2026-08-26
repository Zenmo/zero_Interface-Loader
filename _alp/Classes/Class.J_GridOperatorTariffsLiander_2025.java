/**
 * J_GridOperatorTariffsLiander_2025
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

public class J_GridOperatorTariffsLiander_2025 implements I_GridOperatorTariffs {
	//Data source: https://www.acm.nl/system/files/documents/tarievenblad-liander-elektriciteit-2025.xlsx   (Bijlage 2a + 2b bij Tarievenbesluit Elektriciteit 2025 Liander)
	//			&& https://www.liander.nl/-/media/files/tarieven/consument/2025/jaarlijkse-netwerkkosten-stroom-2025.pdf   (kleinverbruik, incl. meetdienst)
	//			&& https://www.liander.nl/-/media/files/tarieven/grootzakelijk/tarieven-2025/tarieven-voor-aansluiting-en-transport-elektriciteit-2025.pdf?v=1&d=20241129T122349Z	(grootverbruik, incl. meetdienst)

	//Additional Info:
	/*
		Opbouw kleinverbruiktarief (Liander publiceert dit expliciet als 3 delen):
		aansluitdienst (periodieke aansluitvergoeding) + transportdienst (vastrecht + capaciteitstarief) + meetdienst.
		Dat is precies de opsplitsing die deze klasse aanhoudt:
			periodicalPhyscialConnectionCapacityCosts	= aansluitdienst + meetdienst
			transportCostsTable_smallConnections		= capaciteitstarief transport
			transportCostsVastRecht_smallConnections	= vastrecht transport

		Verschillen t.o.v. Enexis (deelmarktgrenzen, bijlage 2b):
		- Liander kent GEEN aparte MS-Transport deelmarkt ("nvt" in bijlage 2b), waar Enexis MS-Transport
		  en MS-Distributie apart tarifeert. Liander heeft één MS deelmarkt.
		- Liander kent WEL een eigen HS deelmarkt (110-150 kV), die bij Enexis "n.v.t. ingaande 2009" is.
		- De grenzen liggen anders: Trafo MS/LS t/m 136 kW (Enexis 125 kW) en MS t/m 2 MW (Enexis 1500 kW).
	*/

	double vat_fr = 0.21; // 21% BTW

	//Meetdienst: staat NIET in het ACM tariefblad, wel in het Liander kleinverbruik tariefblad (sectie 3 Meetdienst).
	double measurementServiceCostSmallConnection_eurpday = 30.6235/365;
	double transportCostsVastRecht_smallConnections_eurpday = 17.9945/365;	//Vastrecht transportdienst t/m 3*80A op LS. Assumed as contract capacity costs for small consumers.

	//Periodieke aansluitvergoeding (PAV). Kleinverbruik incl. meetdienst.
	Map<String, Double> periodicalPhyscialConnectionCapacityCostsTable_eurpyr = Map.ofEntries(
			//Small
			Map.entry("t/m 1 x 6 A (geschakeld)", 15.0015 + 30.6235),
			Map.entry("t/m 1 x 10 A", 38.9455 + 30.6235),
			Map.entry("> 1 x 10 A t/m 3 x 25 A", 38.9455 + 30.6235),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", 52.4505 + 30.6235),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", 52.4505 + 30.6235),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", 61.2835 + 30.6235),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", 61.2835 + 30.6235),

			//Large
			Map.entry("> 3 x 80 A t/m 100 kVA", 254.16),
			Map.entry("> 100 kVA t/m 160 kVA", 283.68),
			Map.entry("> 160 kVA t/m 1.000 kVA", 1013.88),	//ACM splitst 160-630 en 630-1000 kVA ("met LS meting"), beide 1013.88 -> samengevoegd.
			Map.entry("> 1.000 kVA t/m 2 MVA", 1926.84),
			Map.entry("> 2 MVA t/m 5 MVA", 12672.0),
			Map.entry("> 5 MVA t/m 10 MVA", 15132.0),
			Map.entry("> 10 MVA", 15132.0) //MAATWERK -> assumed same as 5-10 MVA.
			);

	//Capaciteitstarief transportdienst kleinverbruik. Liander: kW tarief 72.27 EUR/rekencapaciteit/jaar.
	Map<String, Double> transportCostsTable_smallConnections_eurpday = Map.ofEntries(
			//Small
			Map.entry("t/m 1 x 6 A (geschakeld)", 3.6135/365),
			Map.entry("t/m 1 x 10 A", 36.135/365),
			Map.entry("> 1 x 10 A t/m 3 x 25 A", 289.08/365),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", 1445.4/365),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", 2168.1/365),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", 2890.8/365),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", 3613.5/365)
			);

	Map<String, Double> transportCostsTable_largeConnections_normalTarif_eurpkWh = Map.ofEntries(
			//Large
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0758),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)", 0.022),
			Map.entry("MS (contract vermogen meer dan 136 kW t/m 2 MW)", 0.022),
			Map.entry("Trafo HS+TS/MS > 2 MW", 0.0),
			Map.entry("TS > 2 MW", 0.0),
			Map.entry("HS > 2 MW", 0.0)
			);
	Map<String, Double> transportCostsTable_largeConnections_lowTarif_eurpkWh = Map.ofEntries( //Between 23:00 and 7:00
			//Large
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0403),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)", 0.022),
			Map.entry("MS (contract vermogen meer dan 136 kW t/m 2 MW)", 0.022),
			Map.entry("Trafo HS+TS/MS > 2 MW", 0.0),
			Map.entry("TS > 2 MW", 0.0),
			Map.entry("HS > 2 MW", 0.0)
			);

	Map<String, Double> maxPeakPowerCostsTable_eurpkWpmonth = Map.ofEntries(
			//Large
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0),	//ACM kent geen "kW max per maand" op LS.
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)", 3.46),
			Map.entry("MS (contract vermogen meer dan 136 kW t/m 2 MW)", 3.46),
			Map.entry("Trafo HS+TS/MS > 2 MW", 5.94),
			Map.entry("TS > 2 MW", 5.23),
			Map.entry("HS > 2 MW", 2.28)
			);
	Map<String, Double> contractCapacityCostsTable_eurpkWpyr = Map.ofEntries(
			//Large
			Map.entry("LS (contract vermogen t/m 50 kW)", 16.92),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)", 43.881),
			Map.entry("MS (contract vermogen meer dan 136 kW t/m 2 MW)", 26.68),
			Map.entry("Trafo HS+TS/MS > 2 MW", 45.6),
			Map.entry("TS > 2 MW", 45.6),
			Map.entry("HS > 2 MW", 22.08)
			);
	Map<String, Double> baseContractCapacityCostsTable_eurpyr = Map.ofEntries(
			//Large
			Map.entry("LS (contract vermogen t/m 50 kW)", 18.0),
			Map.entry("Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)", 441.0),
			Map.entry("MS (contract vermogen meer dan 136 kW t/m 2 MW)", 441.0),
			Map.entry("Trafo HS+TS/MS > 2 MW", 2760.0),
			Map.entry("TS > 2 MW", 2760.0),
			Map.entry("HS > 2 MW", 2760.0)
			);
	/**
     * Default constructor
     */
    public J_GridOperatorTariffsLiander_2025() {
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
    			contractCapacityUsedForCalculation_kW = connectionMetaData.getContractedDeliveryCapacity_kW(); //Zelfde systematiek als Enexis: bij zowel afname als teruglevering is de contractwaarde levering leidend.
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
    	else if(physicalCapacity_kW <= 2.3) {
    		keyString = "t/m 1 x 10 A";
    	}
    	else if(physicalCapacity_kW <= 17.25) {
    		keyString = "> 1 x 10 A t/m 3 x 25 A";
    	}
    	else if(physicalCapacity_kW <= 24.15) {
    		keyString = "> 3 x 25 A t/m 3 x 35 A";
    	}
    	else if(physicalCapacity_kW <= 34.5) {
    		keyString = "> 3 x 35 A t/m 3 x 50 A";
    	}
    	else if(physicalCapacity_kW <= 43.47) {
    		keyString = "> 3 x 50 A t/m 3 x 63 A";
    	}
    	else if(physicalCapacity_kW <= 55.2) {
    		keyString = "> 3 x 63 A t/m 3 x 80 A";
    	}
    	else if(physicalCapacity_kW <= 100) {	//kVA gelijkgesteld aan kW, zelfde aanname als bij Enexis.
    		keyString = "> 3 x 80 A t/m 100 kVA";
    	}
    	else if(physicalCapacity_kW <= 160) {
    		keyString = "> 100 kVA t/m 160 kVA";
    	}
    	else if(physicalCapacity_kW <= 1000) {
    		keyString = "> 160 kVA t/m 1.000 kVA";
    	}
    	else if(physicalCapacity_kW <= 2000) {
    		keyString = "> 1.000 kVA t/m 2 MVA";
    	}
    	else if(physicalCapacity_kW <= 5000) {
    		keyString = "> 2 MVA t/m 5 MVA";
    	}
    	else if(physicalCapacity_kW <= 10000) {
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

        if(contractCapacityUsedForCalculation_kW <= 50) {
    		keyString = "LS (contract vermogen t/m 50 kW)";
    	}
    	else if(contractCapacityUsedForCalculation_kW <= 136) {
    		keyString = "Trafo MS/LS (contract vermogen meer dan 50 kW t/m 136 kW)";
    	}
    	else if(contractCapacityUsedForCalculation_kW <= 2000) {
    		keyString = "MS (contract vermogen meer dan 136 kW t/m 2 MW)";
    	}
    	//Boven 2 MW splitst de ACM op fysieke aansluitwijze (1-25 kV / 25-50 kV / 110-150 kV). Benaderd met fysieke capaciteit:
    	else if(physicalCapacity_kW <= 10_000) {	// t/m 10 MVA is de bovenste PAV-staffel van Liander -> aangesloten via trafo op MS.
    		keyString = "Trafo HS+TS/MS > 2 MW";
    	}
    	else if(physicalCapacity_kW <= 100_000) {
    		keyString = "TS > 2 MW";
    	}
    	else{// if(physicalCapacity_kW >100_000) {
    		keyString = "HS > 2 MW";
    	}
    	return keyString;
    }



    @Override
	public String toString() {
		return super.toString();
	}

}
