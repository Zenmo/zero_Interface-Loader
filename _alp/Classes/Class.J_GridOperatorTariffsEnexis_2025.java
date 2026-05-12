/**
 * J_GridOperatorTariffsEnexis_2025
 */	
public class J_GridOperatorTariffsEnexis_2025 implements I_GridOperatorTariffs {
	//Data source: https://www.enexis.nl/zakelijk/aansluitingen/tarieven/tariefbladen (2025)
	//			&&	https://www.enexis.nl/tarieven/oudere-tarieven    (2025)  
	
	//Additional Info:
	/*
		Opbouw transporttarief
		Het transporttarief bestaat uit twee delen, een vast en een variabel
		deel. Het vaste deel, ‘vastrecht transport- dienst’, is een vast
		bedrag per jaar. Als u zowel afneemt als (terug)levert wordt het
		vastrecht transportdienst gebaseerd op de contractwaarde
		levering. Het variabele deel wordt berekend op basis van de totale
		omvang van de getransporteerde kilowatt-uren, het
		gecontracteerde transportvermogen en het maandelijks gemeten
		maximum transportvermogen. Voor het terugleveren van
		elektriciteit betaalt u geen variabele transportkosten. 
		
		//Vraag: Gecontracteerd vermogen kosten dan gelijk aan delivery kW + feedin kW ? Wordt niet vermeld.
	*/
	
	double vat_fr = 0.21; // 21% BTW
	
	double measurementServiceCostSmallConnection_eurpday = 0.04183;
	double transportCostsVastRecht_smallConnections_eurpday = 0.04931;	//Assumed as contract capacity costs for small consumers
	
	Map<String, Double> periodicalPhyscialConnectionCapacityCostsTable_eurpyr = Map.ofEntries(
			//Small
			Map.entry("t/m 1 x 10 A (onbemeten)", 0.11720*365),	
			Map.entry("t/m 1 x 10 A", (0.11720 + measurementServiceCostSmallConnection_eurpday)*365),	
			Map.entry("> 1 x 10 A t/m 3 x 25 A / 1 x 80 A (onbemeten)", 0.11720*365),			
			Map.entry("> 1 x 10 A t/m 3 x 25 A / 1 x 80 A", (0.11720 + measurementServiceCostSmallConnection_eurpday)*365),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", (0.15435 + measurementServiceCostSmallConnection_eurpday)*365),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", (0.15435 + measurementServiceCostSmallConnection_eurpday)*365),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", (0.15435 + measurementServiceCostSmallConnection_eurpday)*365),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", (0.15435 + measurementServiceCostSmallConnection_eurpday)*365),
			
			//Large
			Map.entry("> 3 x 80 A t/m 3 x 250 A (173 kVA)", 381.0),
			Map.entry("> 3 x 250 A (173 kVA) t/m 1.750 kVA", 1653.0),
			Map.entry("> 1.750 kVA t/m 3 MVA", 4560.0),
			Map.entry("> 3 MVA t/m 6 MVA", 4560.0),
			Map.entry("> 6 MVA t/m 10 MVA", 5351.0),
			Map.entry("> 6 MVA N-0 t/m 10 MVA N-0", 5351.0), //MAATWERK -> assumed same as 6-10 MVA.
			Map.entry("> 10 MVA", 5351.0) //MAATWERK -> assumed same as 6-10 MVA.
			);
	
	//Map<String, Double> periodicalAdditionalCableLengthCostsTable_eurpyr = new HashMap(); // Ignored for now.
	

	Map<String, Double> transportCostsTable_smallConnections_eurpday = Map.ofEntries(
			//Small
			Map.entry("t/m 1 x 10 A (onbemeten)", 0.10865),	
			Map.entry("t/m 1 x 10 A", 0.10865),	
			Map.entry("> 1 x 10 A t/m 3 x 25 A / 1 x 80 A (onbemeten)", 0.86926),			
			Map.entry("> 1 x 10 A t/m 3 x 25 A / 1 x 80 A", 0.86926),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", 4.34630),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", 6.51945),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", 8.69260),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", 10.86575)
			);
	
	Map<String, Double> transportCostsTable_largeConnections_normalTarif_eurpkWh = Map.ofEntries(
			//Large
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0804),
			Map.entry("MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)",  0.0250 ),
			Map.entry("MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)", 0.0250 ),
			Map.entry("MS-D > 1500 kW", 0.0250),
			Map.entry("MS-T > 1500 kW", 0.0152),
			Map.entry("HS/MS > 1500 kW", 0.0),
			Map.entry("TS > 1500 kW", 0.0)
			);
	Map<String, Double> transportCostsTable_largeConnections_lowTarif_eurpkWh = Map.ofEntries( //Between 23:00 and 7:00
			//Large
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0421),
			Map.entry("MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)",  0.0250 ),
			Map.entry("MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)", 0.0250 ),
			Map.entry("MS-D > 1500 kW", 0.0250),
			Map.entry("MS-T > 1500 kW", 0.0152),
			Map.entry("HS/MS > 1500 kW", 0.0),
			Map.entry("TS > 1500 kW", 0.0)
			);
	
	Map<String, Double> maxPeakPowerCostsTable_eurpkWpmonth = Map.ofEntries(
			//Large
			Map.entry("LS (contract vermogen t/m 50 kW)", 0.0),
			Map.entry("MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)",  3.71 ),
			Map.entry("MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)", 3.71 ),
			Map.entry("MS-D > 1500 kW", 3.71),
			Map.entry("MS-T > 1500 kW", 3.10),
			Map.entry("HS/MS > 1500 kW", 4.48),
			Map.entry("TS > 1500 kW", 3.67)
			);
	Map<String, Double> contractCapacityCostsTable_eurpkWpyr = Map.ofEntries(
			//Large
			Map.entry("LS (contract vermogen t/m 50 kW)", 16.05),
			Map.entry("MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)",  50.40 ),
			Map.entry("MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)", 29.28 ),
			Map.entry("MS-D > 1500 kW", 29.28),
			Map.entry("MS-T > 1500 kW", 27.71),
			Map.entry("HS/MS > 1500 kW", 42.10),
			Map.entry("TS > 1500 kW", 31.37)
			);
	Map<String, Double> baseContractCapacityCostsTable_eurpyr = Map.ofEntries(
			//Large
			Map.entry("LS (contract vermogen t/m 50 kW)", 18.0),
			Map.entry("MS/LS (contract vermogen meer dan 50 kW t/m 125 kW)",  441.0 ),
			Map.entry("MS-D (contract vermogen meer dan 125 kW t/m 1500 kW)", 441.0 ),
			Map.entry("MS-D > 1500 kW", 441.0),
			Map.entry("MS-T > 1500 kW", 441.0),
			Map.entry("HS/MS > 1500 kW", 2760.0),
			Map.entry("TS > 1500 kW", 2760.0)
			);
	/**
     * Default constructor
     */
    public J_GridOperatorTariffsEnexis_2025() {
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
    			contractCapacityUsedForCalculation_kW = connectionMetaData.getContractedDeliveryCapacity_kW(); //Enexis tariff info says: "Levert u elektriciteit terug aan het net? Dan betaalt u hiervoor alleen vastrecht en geen variabele transportkosten."
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
    	
    	//Enexis tariff info says: "Als u zowel afneemt als (terug)levert wordt het	vastrecht transportdienst gebaseerd op de contractwaarde levering."
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
    	else if(contractCapacityUsedForCalculation_kW <1500 && physicalCapacity_kW <=1750) { // Op basis van onderzoek, tot 1750 KVA is MS-D
    		keyString = "MS-D > 1500 kW";
    	}
    	else if(contractCapacityUsedForCalculation_kW <1500 && physicalCapacity_kW <=6000) { // Volgens mij is tot 6000 MS-T (Oud project was dat zo.)
    		keyString = "MS-T > 1500 kW";
    	}
    	else if(contractCapacityUsedForCalculation_kW <1500 && physicalCapacity_kW <=100_000) {
    		keyString = "HS/MS > 1500 kW";
    	}
    	else{// if(contractCapacityUsedForCalculation_kW <1500 && physicalCapacity_kW >100_000) {
    		keyString = "TS > 1500 kW";
    	}
    	return keyString;
    }
	
    	
    	
    @Override
	public String toString() {
		return super.toString();
	}
}