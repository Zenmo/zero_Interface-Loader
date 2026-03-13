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
	
	Map<String, Double> periodicalPhyscialConnectionCapacityCostsTable_eurpyr = Map.of(
			//Small
			"> ", 381.0,			
			"> ", 381.0,
			"> ", 381.0,
			"> ", 381.0,
			"> ", 381.0,
			"> ", 381.0,
			
			//Large
			"> 3 x 80 A t/m 3 x 250 A (173 kVA)", 381.0,
			"> 3 x 250 A (173 kVA) t/m 1.750 kVA", 1653.0,
			"> 1.750 kVA t/m 3 MVA", 4560.0,
			"> 3 MVA t/m 6 MVA", 4560.0,
			"> 6 MVA t/m 10 MVA", 5351.0,
			"> 6 MVA N-0 t/m 10 MVA N-0", 5351.0, //MAATWERK -> assumed same as 6-10 MVA.
			"> 10 MVA", 5351.0 //MAATWERK -> assumed same as 6-10 MVA.
			);
	//Map<String, Double> periodicalAdditionalCableLengthCostsTable_eurpyr = new HashMap(); // Ignored for now.
	Map<String, Double> periodicalPhyscialConnectionCapacityTable = new HashMap();
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