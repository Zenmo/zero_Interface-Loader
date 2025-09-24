/**
 * J_InfoText
 */	
public class J_InfoText implements Serializable {

	
	public String lorumIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui	officia deserunt mollit anim id est laborum.";
    
	//// Main Interface \\\\
	// TODO
	
	//// Public version
	public String publicVersionWarning = "In de publieke variant zijn een aantal functionaliteiten uitgezet en zijn bepaalde bedrijven en hun data afgeschermd.";
	
	//// TABS \\\\
	//// Electricity Tab
    public String electricityDemandReduction = "Met deze slider kun je het basisverbruik van stroom voor huizen en bedrijven aanpassen. Een positief percentage betekent dat ze minder verbruiken dan in het basis scenario. Een negatief percentage betekent dat het verbruik is toegenomen. Dit past niet de hoeveelheid stroombehoefte aan voor specifieke apparaten die apart in het model zitten zoals warmtepompen en elektrische auto's. Zie hiervoor de besparingsslider in de tabjes warmte of transport.";
    public String householdElectricityDemandReduction = "Met deze slider kun je het basisverbruik van stroom voor huizen aanpassen. Een positief percentage betekent dat ze minder verbruiken dan in het basis scenario. Een negatief percentage betekent dat het verbruik is toegenomen. Dit past niet de hoeveelheid stroombehoefte aan voor specifieke apparaten die apart in het model zitten zoals warmtepompen en elektrische auto's. Zie hiervoor de besparingsslider in de tabjes warmte of transport.";
    public String companyElectricityDemandReduction = "Met deze slider kun je het basisverbruik van stroom voor bedrijven aanpassen. Een positief percentage betekent dat ze minder verbruiken dan in het basis scenario. Een negatief percentage betekent dat het verbruik is toegenomen. Dit past niet de hoeveelheid stroombehoefte aan voor specifieke apparaten die apart in het model zitten zoals warmtepompen en elektrische auto's. Zie hiervoor de besparingsslider in de tabjes warmte of transport.";
    public String householdRooftopPV = "Met deze slider kun je instellen welk aandeel van de huizen zonnepanelen op hun dak heeft liggen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String companyRooftopPV = "Met deze slider kun je instellen welk deel van alle daken van de bedrijven zonnepanelen bevat. Het aantal zonnepanelen voor een bedrijf wordt geschaald naar het dak oppervlak van het bedrijf. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String landPV = "Met deze slider kun je instellen hoeveel zonneparken er in het model zitten. 1 hectare zonnepark heeft een piek opwek van 1 MW. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String landWind = "Met deze slider kun je instellen hoeveel windmolens er in het model zitten. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String householdBatteries = "Met deze slider kun je instellen welk aandeel van de huizen die zonnepanelen op hun dak hebben liggen een batterij hebben. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String householdElectricCooking = "Met deze slider kun je instellen welk aandeel van de huizen elektrisch koken.";
	public String householdElectricityConsumptionGrowth = "Met deze slider kun je het basisverbruik van stroom voor huizen aanpassen. Een positief percentage betekent dat ze meer verbruiken dan in het basis scenario. Een negatief percentage betekent dat het verbruik is afgenomen. Dit past niet de hoeveelheid stroombehoefte aan voor specifieke apparaten die apart in het model zitten zoals warmtepompen en elektrische auto's. Zie hiervoor de besparingsslider in de tabjes warmte of transport.";
	public String curtailment = "Met deze knop kun je curtailment van stroom productie door huizen en bedrijven aan of uit zetten. Zet je hem aan, dan wordt alle energie productie die niet in de gecontracteerde teruglevercapaciteit past gecurtailt (oftewel: 'weggegooit')";
	public String gridBattery_default = "Met deze slider kun je instellen hoe groot de buurtbatterij is. Deze batterij probeert het profiel van het gehele model vlak te trekken.";
	public String gridBattery_residential = "Met deze slider kun je instellen hoe groot de buurtbatterijen zijn. Stel je hem bijvoorbeeld in op 1 MWh, dan heeft elke trafo in het model een buurtbatterij van 1 MWh. Deze batterijen proberen vervolgens het profiel van hun eigen trafo vlak te trekken.";	
	
	//// Heat Tab
	// generic
    public String heatDemandReduction = "Met deze slider kun je de behoefte aan warmte voor huizen en bedrijven aanpassen. Een positief percentage betekent dat ze minder warmte nodig hebben dan in het basis scenario. Een negatief percentage betekent dat ze meer warmte nodig hebben. Dit verandert niet de temperatuur in het gebouw, maar de benodigde hoeveelheid energie om te verwarmen. Deze slider gaat bijvoorbeeld over betere isolatie in de gebouwen.";
	public String gasBoiler = "Met deze slider kun je instellen welk aandeel van de huizen en bedrijven verwarmen met een gasboiler. Dit heeft betrekking op het verwarmen van de ruimtes, de vraag naar warm water en eventuele bedrijfsprocessen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String electricHeatpump = "Met deze slider kun je instellen welk aandeel van de huizen en bedrijven verwarmen met een elektrische warmtepomp. Dit heeft betrekking op het verwarmen van de ruimtes, de vraag naar warm water en eventuele bedrijfsprocessen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String hybridHeatpump = "Met deze slider kun je instellen welk aandeel van de huizen en bedrijven verwarmen met een hybride warmtepomp. Dit heeft betrekking op het verwarmen van de ruimtes, de vraag naar warm water en eventuele bedrijfsprocessen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String heatGrid = "Met deze slider kun je instellen welk aandeel van de huizen en bedrijven verwarmen via een warmtenet. Dit heeft betrekking op het verwarmen van de ruimtes, de vraag naar warm water en eventuele bedrijfsprocessen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String electricBoiler = "Met deze slider kun je instellen welk aandeel van de huizen en bedrijven verwarmen met een elektrische boiler. Dit heeft betrekking op het verwarmen van de ruimtes, de vraag naar warm water en eventuele bedrijfsprocessen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	
	// household
	public String householdHeatDemandReduction = "Met deze slider kun je de behoefte aan warmte voor huizen aanpassen. Een positief percentage betekent dat ze minder warmte nodig hebben dan in het basis scenario. Een negatief percentage betekent dat ze meer warmte nodig hebben. Dit verandert niet de temperatuur in het gebouw, maar de benodigde hoeveelheid energie om te verwarmen. Deze slider gaat bijvoorbeeld over betere isolatie in de gebouwen.";
	public String householdGasBoiler = "Met deze slider kun je instellen welk aandeel van de huizen verwarmen met een gasboiler. Dit heeft betrekking op het verwarmen van de ruimtes en de vraag naar warm water. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String householdElectricHeatpump = "Met deze slider kun je instellen welk aandeel van de huizen verwarmen met een elektrische warmtepomp. Dit heeft betrekking op het verwarmen van de ruimtes en de vraag naar warm water. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String householdHybridHeatpump = "Met deze slider kun je instellen welk aandeel van de huizen verwarmen met een hybride warmtepomp. Dit heeft betrekking op het verwarmen van de ruimtes en de vraag naar warm water. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String householdHeatGrid = "Met deze slider kun je instellen welk aandeel van de huizen verwarmen via een warmtenet. Dit heeft betrekking op het verwarmen van de ruimtes en de vraag naar warm water. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String householdElectricBoiler = "Met deze slider kun je instellen welk aandeel van de huizen verwarmen met een elektrische boiler. Dit heeft betrekking op het verwarmen van de ruimtes en de vraag naar warm water. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String householdHTDistrictHeating = "Met deze knop kun je alle huizen aansluiten op een hoog-temperatuur warmtenet. Als er geen warmtebron ingesteld is voor het warmtenet dan wordt de warmte als import gerekend.";
	public String householdLTDistrictHeating = "Met deze knop kun je alle huizen aansluiten op een laag-temperatuur warmtenet. In de huizen wordt een warmtepomp geplaatst om de warmte verder op te waarderen. Als er geen warmtebron ingesteld is voor het warmtenet dan wordt de warmte als import gerekend.";
	public String householdAirconditioning = "Met deze slider kun je instellen welk aandeel van de huizen een air conditioning unit hebben.";
	public String householdAdditionalInsulation = "Met deze slider kun je instellen welk aandeel van de huizen extra geisoleerd zijn. Deze huizen hebben 30% minder warmtebehoefte voor het verwarmen van ruimtes.";
	public String householdRooftopPT = "Met deze slider kun je instellen welk aandeel van de huizen PhotoThermische panelen op hun dak heeft liggen. Maar let op: Als er PT op het dak ligt, is er minder ruimte voor PV panelen, en zal de maximale potentiele opbrengst van PV voor die huizen dus verminderen.";
	// company
	public String companyHeatDemandReduction = "Met deze slider kun je de behoefte aan warmte voor bedrijven aanpassen. Een positief percentage betekent dat ze minder warmte nodig hebben dan in het basis scenario. Een negatief percentage betekent dat ze meer warmte nodig hebben. Dit verandert niet de temperatuur in het gebouw, maar de benodigde hoeveelheid energie om te verwarmen. Deze slider gaat bijvoorbeeld over betere isolatie in de gebouwen.";
	public String companyGasBoiler = "Met deze slider kun je instellen welk aandeel van de bedrijven verwarmen met een gasboiler. Dit heeft betrekking op het verwarmen van de ruimtes, de vraag naar warm water en eventuele bedrijfsprocessen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String companyElectricHeatpump = "Met deze slider kun je instellen welk aandeel van de bedrijven verwarmen met een elektrische warmtepomp. Dit heeft betrekking op het verwarmen van de ruimtes, de vraag naar warm water en eventuele bedrijfsprocessen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String companyHybridHeatpump = "Met deze slider kun je instellen welk aandeel van de bedrijven verwarmen met een hybride warmtepomp. Dit heeft betrekking op het verwarmen van de ruimtes, de vraag naar warm water en eventuele bedrijfsprocessen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String companyHeatGrid = "Met deze slider kun je instellen welk aandeel van de bedrijven verwarmen via een warmtenet. Dit heeft betrekking op het verwarmen van de ruimtes, de vraag naar warm water en eventuele bedrijfsprocessen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String companyElectricBoiler = "Met deze slider kun je instellen welk aandeel van de bedrijven verwarmen met een elektrische boiler. Dit heeft betrekking op het verwarmen van de ruimtes, de vraag naar warm water en eventuele bedrijfsprocessen. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String companyCustomHeating = "Deze slider is puur visueel. In deze slider is weergegeven welk aandeel van de bedrijven verwarmen met een Custom warmte systeem. Een Custom systeem betekent hier een systeem dat bestaat uit meerdere verschillende warmte producerende apparaten of andere complexe situaties. Omdat deze systemen zo complex zijn, zijn ze niet simpelweg te vervangen door een enkel ander systeem. Daarom kan je deze slider ook niet aanpassen.";
			
	//// Mobility Tab
	//DEFAULT
	public String mobilityDemandReduction = "Met deze slider kun je de hoeveelheid transportbewegingen van wagens aanpassen. Een positief percentage betekent dat ze minder kilometers per jaar rijden dan in het basis scenario. Een negatief percentage betekent dat ze meer kilometers gaan rijden. Deze slider verandert niet het aantal voertuigen.";
	public String chargingBehaviour = "Hier kun je verschillende laadstrategieën selecteren. Standaard wordt er dom geladen, dat wil zeggen dat de wagen op moment van aankomst inprikt en op vol vermogen laad tot de batterij vol is. Bij Max Spread kijkt de wagen op moment van aankomst wanneer hij weer moet vertrekken en verdeelt hij zijn laadbehoefte gelijk over deze tijd. Bij Max Power probeert hij te laden op het maximale vermogen wat nog binnen de aansluiting past, echter als de accu daardoor niet vol zou zijn voordat de wagen vertrekt wordt aan het einde alsnog op vol vermogen geladen. Hierdoor kan de aansluitingscapaciteit alsnog worden overschreden.";
	public String mobilitySmartCharging = "Standaard laden autos gewoon met een standaard vermogen. Vink je deze knop aan, dan gaan de autos slim laden: Voertuigen laden op met een zo vlak mogelijk profiel, waardoor ze vervolgens wel hun geplande rit kunnen maken, zonder voor grote pieken te zorgen."; 
	
	// trucks
	public String electricTrucks = "Met deze slider kun je instellen welk aandeel van alle vrachtwagens elektrisch is. Als je deze slider verhoogt dan wordt eerst geprobeerd vrachtwagens die rijden op fossiele brandstof te vervangen door elektrische vrachtwagens. Eventueel daarna worden vrachtwagens die rijden op waterstof omgezet in elektrische vrachtwagens. Als je deze slider verlaagt dan worden elektrische vrachtwagens omgezet naar vrachtwagens die rijden op fossiele brandstof. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String hydrogenTrucks = "Met deze slider kun je instellen welk aandeel van alle vrachtwagens op waterstof rijdt. Als je deze slider verhoogt dan wordt eerst geprobeerd vrachtwagens die rijden op fossiele brandstof te vervangen door vrachtwagens die rijden op waterstof. Eventueel daarna worden elektrische vrachtwagens omgezet in vrachtwagens die rijden op waterstof. Als je deze slider verlaagt dan worden vrachtwagens die rijden op waterstof omgezet naar vrachtwagens die rijden op fossiele brandstof. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String fossilTrucks = "Met deze slider kun je instellen welk aandeel van alle vrachtwagens op fossiele brandstof rijdt. Als je deze slider verhoogt dan wordt eerst geprobeerd elektrische vrachtwagens te vervangen door vrachtwagens die rijden op fossiele brandstof. Eventueel daarna worden vrachtwagens die rijden op waterstof omgezet in vrachtwagens die rijden op fossiele brandstof. Als je deze slider verlaagt dan worden vrachtwagens die rijden op fossiele brandstof omgezet naar elektrische vrachtwagens. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	// vans
	public String electricVans = "Met deze slider kun je instellen welk aandeel van alle busjes elektrisch is. Als je deze slider verhoogt dan worden busjes die rijden op fossiele brandstof vervangen door elektrische busjes. Als je deze slider verlaagt dan worden elektrische busjes omgezet naar busjes die rijden op fossiele brandstof. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String fossilVans = "Met deze slider kun je instellen welk aandeel van alle busjes op fossiele brandstof rijdt. Als je deze slider verhoogt dan worden elektrische busjes vervangen door busjes die rijden op fossiele brandstof. Als je deze slider verlaagt dan worden busjes die rijden op fossiele brandstof omgezet naar elektrische busjes. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	// cars
	public String electricCars = "Met deze slider kun je instellen welk aandeel van alle auto's elektrisch is. Als je deze slider verhoogt dan worden auto's die rijden op fossiele brandstof vervangen door elektrische auto's. Als je deze slider verlaagt dan worden elektrische auto's omgezet naar auto's die rijden op fossiele brandstof. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	public String fossilCars = "Met deze slider kun je instellen welk aandeel van alle auto's op fossiele brandstof rijdt. Als je deze slider verhoogt dan worden elektrische auto's vervangen door auto's die rijden op fossiele brandstof. Als je deze slider verlaagt dan worden auto's die rijden op fossiele brandstof omgezet naar elektrische auto's. De minimum waarde van de slider is gezet op de hoveelheid die al in het huidige scenario aanwezig is.";
	
	//RESIDENTIAL	
	public String householdPublicChargersBehaviour = "Met deze knoppen kun je instellen wat het laadgedrag is van de publieke laadpalen. Met V1G wordt uitgesteld laden of 'slim' laden bedoeld. Met V2G kunnen de voertuigen ook terugleveren aan het net.";
	public String householdPublicParkingV1G = "Met deze slider kun je instellen welk aandeel van de publieke laadpalen V1G ondersteunen.";
	public String householdPublicParkingV2G = "Met deze slider kun je instellen welk aandeel van de publieke laadpalen V2G ondersteunen.";
	public String householdPrivateParking = "Met deze slider kun je instellen welk aandeel huizen met een eigen oprit een EV heeft.";
	public String householdPublicParking = "Met deze slider kun je instellen hoe veel publieke laadpalen er in het model zitten. Door meer publieke laadpalen toe te voegen verdwijnen er auto's die op fossiele brandstoffen rijden.";

	public String EVsThatSupportV2G = "Met deze slider kun je instellen welk aandeel van EVs de mogelijkheid hebben om te ontladen (V2G)";
	public String activateV2GPrivatePublicChargers = "Met deze knop kun je V2G activeren voor publieke laadpalen. Hierbij zullen alle laadpalen die het ondersteunen V2G toepassen wanneer het volgens de geselecteerde laadstrategie gewenst is.";
	public String activateV2GPrivateParkedCars = "Met deze knop kun je V2G activeren voor privé geparkeerde EVs . Hierbij zullen alle EVs die het ondersteunen V2G toepassen wanneer het volgens de geselecteerde laadstrategie gewenst is.";
	public String chargingStrategyPublicChargers = "Met dit drop down menu kun je uitkiezen welke laad strategie je wilt toepassen op alle actieve publieke laadpalen.";
	public String chargingAttitudePrivateParkedCars = "Met dit drop down menu kun je uitkiezen welke laad strategie je wilt toepassen op alle prive geladen EVs";
	
	//// E-Hub Tab
	//TODO
	
	////Map overlays
	
	//Congestion
	public String i_mapOverlayLegend_congestion_Degrees = "Als een GIS-object (zoals een pand of transformator) groen wordt weergegeven, is er nog voldoende beschikbare capaciteit. Een oranje object duidt erop dat tijdens de jaarsimulatie de grens van 70% van de capaciteit (voor panden: de contractcapaciteit) is overschreden. Een rood object betekent dat de volledige capaciteit (100%) is overschreden.";
	public String i_mapOverlayLegend_congestion_Types = "Met deze knoppen kun je selecteren welk type belasting je wilt visualiseren: Afname, teruglevering, of de maximum belasting van beide.";
	
	/**
     * Default constructor
     */
    public J_InfoText() {
    }
    
    //public Pair<String, Integer> getLorumIpsum(int width_ch, String descriptionText) {
    	//return this.restrictWidth(descriptionText, width_ch);
    //}

    public Pair<String, Integer> restrictWidth( String txt, int width_ch ) {
    	StringBuilder output = new StringBuilder();
    	int remainingTextSize = txt.length();
    	int currentIndex = 0;
    	int lines = 0;
    	while (remainingTextSize > width_ch) {
    		int i = 0;
    		while (!Character.isWhitespace(txt.charAt(currentIndex + width_ch - i))) {
	    		i++;
	    		if (i > width_ch) {
	    			throw new RuntimeException("Impossible to format string to fit within width.");
	    		}
    		}
			output.append(txt.substring(currentIndex, currentIndex + width_ch - i));
    		output.append('\n');
    		currentIndex += width_ch - i + 1;
    		remainingTextSize -= width_ch - i + 1;
    		lines++;
    	}
    	output.append(txt.substring(currentIndex, txt.length()));
		lines++;
    	return new Pair(output.toString(), lines);
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