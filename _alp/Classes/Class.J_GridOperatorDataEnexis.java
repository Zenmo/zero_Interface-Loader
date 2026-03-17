/**
 * J_GridOperatorDataEnexis
 */	
public class J_GridOperatorDataEnexis implements I_GridOperatorData  {
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
	Map<String, Double> periodicalPhyscialConnectionCapacityTable = new HashMap();
	
	double transportCostsVastRecht_smallConnections_eurpday = 0.04931;
	Map<String, Double> transportCostsTable_smallConnections_eurpday = Map.ofEntries(
			//Small
			Map.entry("t/m 1 x 10 A (onbemeten)", 0.10865 + transportCostsVastRecht_smallConnections_eurpday),	
			Map.entry("t/m 1 x 10 A", 0.10865 + transportCostsVastRecht_smallConnections_eurpday),	
			Map.entry("> 1 x 10 A t/m 3 x 25 A / 1 x 80 A (onbemeten)", 0.86926 + transportCostsVastRecht_smallConnections_eurpday),			
			Map.entry("> 1 x 10 A t/m 3 x 25 A / 1 x 80 A", 0.86926 + transportCostsVastRecht_smallConnections_eurpday),
			Map.entry("> 3 x 25 A t/m 3 x 35 A", 4.34630 + transportCostsVastRecht_smallConnections_eurpday),
			Map.entry("> 3 x 35 A t/m 3 x 50 A", 6.51945 + transportCostsVastRecht_smallConnections_eurpday),
			Map.entry("> 3 x 50 A t/m 3 x 63 A", 8.69260 + transportCostsVastRecht_smallConnections_eurpday),
			Map.entry("> 3 x 63 A t/m 3 x 80 A", 10.86575 + transportCostsVastRecht_smallConnections_eurpday)
			
			//Large
			/*
			Map.entry("> 3 x 80 A t/m 3 x 250 A (173 kVA)", 381.0),
			Map.entry("> 3 x 250 A (173 kVA) t/m 1.750 kVA", 1653.0),
			Map.entry("> 1.750 kVA t/m 3 MVA", 4560.0),
			Map.entry("> 3 MVA t/m 6 MVA", 4560.0),
			Map.entry("> 6 MVA t/m 10 MVA", 5351.0),
			Map.entry("> 6 MVA N-0 t/m 10 MVA N-0", 5351.0), //MAATWERK -> assumed same as 6-10 MVA.
			Map.entry("> 10 MVA", 5351.0) //MAATWERK -> assumed same as 6-10 MVA.
			);
			*/
			);
	/**
     * Default constructor
     */
    public J_GridOperatorDataEnexis() {
    }

    public double getMonthlyPeakCost_eur(J_ConnectionMetaData connectionMetaData, double monthlyPeak_kW) {
    	return 0;
    }
    public double getYearlyContractDeliveryCapacityCost_eur(J_ConnectionMetaData connectionMetaData){
    	return 0;
    }
    public double getYearlyContractFeedinCapacityCost_eur(J_ConnectionMetaData connectionMetaData){
    	return 0;
    }
    public double getYearlyPhysicalCapacityCost_eur(J_ConnectionMetaData connectionMetaData){
    	return 0;
    }
    public double getTransportCost_eur(J_ConnectionMetaData connectionMetaData, double transportedElectricity_kWh){
    	return 0;
    }

	@Override
	public String toString() {
		return super.toString();
	}
}