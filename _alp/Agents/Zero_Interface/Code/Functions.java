double f_setLegendaColors()
{/*ALCODESTART::1696837759921*/
/*
if (ra_legendaOptions.getValue() == 0){
	gr_heatDemandLegenda.setVisible(true);
	gr_heatSupplyLegenda.setVisible(false);
	
	for (GIS_Building b : energyModel.pop_GIS_Buildings){	
		f_setColorToEnergyLabels(b);
	}
	
}
else if (ra_legendaOptions.getValue() == 1){
	gr_heatDemandLegenda.setVisible(false);
	gr_heatSupplyLegenda.setVisible(true);
	
	for (GIS_Building b : energyModel.pop_GIS_Buildings){	
		//b.f_countHeatSystems();
		//b.f_setColorToHeatSupplyMethod();
	}
	
}
*/



/*ALCODEEND*/}

double f_setColorToEnergyLabels(GIS_Object b)
{/*ALCODESTART::1696837759924*/
if (b.gisRegion != null){
	if (indexOfMax(b.ar_countEnergyLabels) < 4){
		b.gisRegion.setFillColor(v_energieLabelAColor);
	}
	else if (indexOfMax(b.ar_countEnergyLabels) == 4){
		b.gisRegion.setFillColor(v_energieLabelBColor);
	}
	else if (indexOfMax(b.ar_countEnergyLabels) == 5){
		b.gisRegion.setFillColor(v_energieLabelCColor);
	}
	else if (indexOfMax(b.ar_countEnergyLabels) == 6){
		b.gisRegion.setFillColor(v_energieLabelDColor);
	}
	else if (indexOfMax(b.ar_countEnergyLabels) == 7){
		b.gisRegion.setFillColor(v_energieLabelEColor);
	}
	else if (indexOfMax(b.ar_countEnergyLabels) == 8){
		b.gisRegion.setFillColor(v_energieLabelFColor);
	}
	else if (indexOfMax(b.ar_countEnergyLabels) == 9){
		b.gisRegion.setFillColor(v_energieLabelGColor);
	}
	else if (indexOfMax(b.ar_countEnergyLabels) == 10){
		b.gisRegion.setFillColor(v_energieLabelOnbekendColor);
	}
	else {
		b.gisRegion.setFillColor(black);
	}
}



/*ALCODEEND*/}

double f_selectGISRegion(double clickx,double clicky)
{/*ALCODESTART::1696863329251*/
//After a click, reset previous clicked building/gridNode colors and text
v_previousClickedObjectType = v_clickedObjectType;
c_previousSelectedObjects = new ArrayList<GIS_Object>(c_selectedObjects);
ArrayList<GIS_Object> buildingsConnectedToSelectedBuildingsList = new ArrayList<>();
c_selectedGridConnections.clear();
c_selectedObjects.clear();

//Deselect previous selection
if( v_previousClickedObjectType != null){
	f_deselectPreviousSelect();
}

//Check if click was on Gridnode, if yes, select grid node
for ( GridNode GN : energyModel.pop_gridNodes ){
	if( GN.gisRegion != null && GN.gisRegion.contains(clickx, clicky) && GN.gisRegion.isVisible() ){
		f_selectGridNode(GN);
		uI_Results.v_selectedObjectType = OL_GISObjectType.GRIDNODE;
		uI_Results.f_showCorrectChart();
		return;
	}
}

//Check if click was on Building, if yes, select grid building
for ( GIS_Building b : energyModel.pop_GIS_Buildings ){
	if( b.gisRegion != null && b.gisRegion.contains(clickx, clicky) ){
		if (b.gisRegion.isVisible()) { //only allow us to click on visible objects
			if (b.c_containedGridConnections.size() > 0 ) { // only allow buildings with gridconnections
				buildingsConnectedToSelectedBuildingsList = b.c_containedGridConnections.get(0).c_connectedGISObjects; // Find buildings powered by the same GC as the clicked building
				f_selectBuilding(b, buildingsConnectedToSelectedBuildingsList);
				uI_Results.v_selectedObjectType = OL_GISObjectType.BUILDING;
				uI_Results.f_showCorrectChart();
				return;
			}
		}
	}
}

//Check if click was on remaining objects such as chargers, solarfields, parcels: if yes, select object
for ( GIS_Object GISobject : energyModel.pop_GIS_Objects ){
	if( GISobject.gisRegion != null && GISobject.gisRegion.contains(clickx, clicky) ) {
		if (GISobject.gisRegion.isVisible()) { //only allow us to click on visible objects
			if (GISobject.c_containedGridConnections.size() > 0 ) { // only allow objects with gridconnections
				
				// Find buildings powered by the same GC as the clicked object
				buildingsConnectedToSelectedBuildingsList = GISobject.c_containedGridConnections.get(0).c_connectedGISObjects; 
				
				//Find the (first) connected GC in the object
				GridConnection selectedGC = GISobject.c_containedGridConnections.get(0);
				
				//Set the selected GIS object type
				v_clickedObjectType = GISobject.p_GISObjectType;
				c_selectedObjects.add(GISobject);
				
				//Set the correct interface view for each object type
				switch(v_clickedObjectType){
				
				case CHARGER:
					f_selectCharger((GCPublicCharger)selectedGC, GISobject );
					break;
				
					
				default:
					buildingsConnectedToSelectedBuildingsList = GISobject.c_containedGridConnections.get(0).c_connectedGISObjects; // Find buildings powered by the same GC as the clicked building
					f_selectBuilding(GISobject, buildingsConnectedToSelectedBuildingsList);		
					break;
				}
				
				uI_Results.v_selectedObjectType = v_clickedObjectType;				
				uI_Results.f_showCorrectChart();
				return;
			}
		}
	}
}

//Still no clicked object? :select basic region
v_clickedObjectType = OL_GISObjectType.REGION;
uI_Results.v_selectedObjectType = OL_GISObjectType.REGION;
uI_Results.f_showCorrectChart();

//Enable kpi summary button
uI_Results.getCheckbox_KPISummary().setEnabled(true);
/*ALCODEEND*/}

double f_styleAreas(GIS_Object gis_area)
{/*ALCODESTART::1702045084338*/
double width = 1;

if (gis_area instanceof GIS_Parcel ||
	gis_area.c_containedGridConnections.size() == 0 ||
	gis_area.c_containedGridConnections.get(0) instanceof GCPublicCharger ) {
	// Parcels, Chargers and GIS Objects without GCs are always the default color
	gis_area.f_style(null, null, null, null);
	return;
}

switch(rb_buildingColors.getValue()) {
	case 0:
		gis_area.f_style(null, null, null, null);
		break;
	case 1:
		if (p_selectedProjectType == OL_ProjectType.RESIDENTIAL) {
			f_setColorsBasedOnConsumpionProfileHouseholds(gis_area);
		}
		else {
			f_setColorsBasedOnConsumpion(gis_area);
		}
		break;
	case 2:
		if (p_selectedProjectType == OL_ProjectType.RESIDENTIAL) {
			f_setColorsBasedOnProductionHouseholds(gis_area);
		}
		else {
			f_setColorsBasedOnProduction(gis_area);
		}
		break;
	case 3:
		f_setColorsBasedOnGridTopology_objects(gis_area);
		break;
	case 4:
		break;
	default:
		break;
}
/*ALCODEEND*/}

double f_styleSimulationAreas(GIS_Object area)
{/*ALCODESTART::1702385530773*/
GISRegion gisregion = area.gisRegion; 

switch( area.p_GISObjectType ) {
	case ANTI_LAYER:
		gisregion.setVisible(true);	
		gisregion.setLineStyle( LINE_STYLE_SOLID );
		gisregion.setLineWidth( 0);
		gisregion.setLineColor( v_antiLayerColor );
		gisregion.setFillColor( v_antiLayerColor );
		GISregion_antiLaag = gisregion;

	break;
	
	default:
		gisregion.setVisible(true);	
		gisregion.setLineColor( v_simulationArea1LineColor );
		gisregion.setLineStyle( LINE_STYLE_SOLID );
		gisregion.setLineWidth( 1);
		gisregion.setFillColor( v_simulationArea1Color );
}
/*ALCODEEND*/}

double f_styleGridNodes(GridNode GN)
{/*ALCODESTART::1705499586056*/
switch(rb_buildingColors.getValue()) {
	case 0:
	case 1:
	case 2:
	case 4:
		switch( GN.p_nodeType ) {
			case MVLV:
				f_styleMVLV(GN.gisRegion);
				break;
			case SUBMV:
				f_styleSUBMV(GN.gisRegion);
				break;
			case MVMV:
				f_styleMVMV(GN.gisRegion);
				break;
			case HVMV:
				f_styleHVMV(GN.gisRegion);
				break;
			case HT:
				
				break;
			case MT:
				
				break;
			case LT:
				
				break;
			default:
		}
		break;
	case 3:
		f_setColorsBasedOnGridTopology_gridnodes(GN);
		break;

	default:
		break;
}
/*ALCODEEND*/}

double f_styleMVLV(GISRegion gisregion)
{/*ALCODESTART::1705505495599*/
gisregion.setLineStyle( LINE_STYLE_SOLID );
gisregion.setLineColor( v_gridNodeColor_net.brighter() );
gisregion.setLineWidth(2);		

/*
switch( klant_net_station ) {
		case "Klantstation":
			gisregion.setFillColor(v_gridNodeColor_klant);
		break;
		case "Klantstation met medegebr":
			gisregion.setFillColor(v_gridNodeColor_klant);
		break;
		case "Netstation":
			gisregion.setFillColor(v_gridNodeColor_net);
		break;
		case "Transportverdeelstation":
			gisregion.setFillColor(v_gridNodeColor_net);;	
		break;
		case "Hoofdstation":
			gisregion.setFillColor(v_gridNodeColor_net);
		break;
		default:
			gisregion.setFillColor(v_gridNodeColor_net);
}
*/

gisregion.setFillColor(v_gridNodeColor_net);

/*
if ( nodeStatus != null && nodeStatus.equals("x")){
	gisregion.setFillColor(new Color(239,204,211,90));
	gisregion.setLineColor(new Color(0,0,0,0));
}
*/
/*ALCODEEND*/}

double f_styleHVMV(GISRegion gisregion)
{/*ALCODESTART::1705505509120*/
gisregion.setFillColor(new Color(253, 223, 134, 206));
gisregion.setLineStyle( LINE_STYLE_SOLID );
gisregion.setLineColor( navy );
gisregion.setLineWidth(2);
/*ALCODEEND*/}

double f_setUITabs()
{/*ALCODESTART::1705925024602*/
// CHOOSE WHICH TABS YOU WANT TO BE ABLE TO SHOW FOR YOUR PROJECT 
// (OVERRIDE FUNCTION IN CHILD IF YOU WANT OTHER THAN DEFAULT)

// Adding the (child) tabs to the tabArea population

// If you use an extension of a tab, you must update the pointer to the instance of the interface
// Something like: tabElectricity.zero_Interface = loader_Project.zero_Interface;
// No update to the pointer is needed for the generic tabs

tabElectricity electricityTab = new tabElectricity();
electricityTab.goToPopulation(uI_Tabs.pop_tabElectricity);

tabHeating heatingTab = new tabHeating();
heatingTab.goToPopulation(uI_Tabs.pop_tabHeating);

tabMobility mobilityTab = new tabMobility();
mobilityTab.goToPopulation(uI_Tabs.pop_tabMobility);

tabEHub EHubTab = new tabEHub();
EHubTab.goToPopulation(uI_Tabs.pop_tabEHub);

// Group visibilities
// When using an extension of a generic tab don't forget to typecast it!
((tabElectricity)uI_Tabs.pop_tabElectricity.get(0)).getGroupElectricityDemandSliders().setVisible(true);
((tabHeating)uI_Tabs.pop_tabHeating.get(0)).getGroupHeatDemandSlidersCompanies().setVisible(true);
((tabMobility)uI_Tabs.pop_tabMobility.get(0)).getGroupMobilityDemandSliders().setVisible(true);
((tabEHub)uI_Tabs.pop_tabEHub.get(0)).getGroupHubSliders().setVisible(true);

uI_Tabs.f_showCorrectTab();
/*ALCODEEND*/}

double f_selectGridNode(GridNode GN)
{/*ALCODESTART::1707918668161*/
v_clickedGridNode = GN;
v_clickedObjectType = OL_GISObjectType.GRIDNODE;

//Disable the KPI summary (button)
uI_Results.getCheckbox_KPISummary().setSelected(false, true);
uI_Results.getCheckbox_KPISummary().setEnabled(false);


// Set info text
if ( GN.p_realCapacityAvailable ) {
	v_clickedObjectText = GN.p_nodeType + "-station, " + Integer.toString( ((int)GN.p_capacity_kW) ) + " kW, ID: " + GN.p_gridNodeID + ", aansluitingen: " + GN.f_getConnectedGridConnections().size() + ", Type station: " + GN.p_description;
}
else {
	v_clickedObjectText =  GN.p_nodeType + "-station, " + Integer.toString( ((int)GN.p_capacity_kW) ) + " kW (ingeschat), ID: " + GN.p_gridNodeID + ", aansluitingen: " + GN.f_getConnectedGridConnections().size() + ", Type station: " + GN.p_description;
}

v_clickedObjectAdress = "";
v_clickedObjectDetails = "Type station:\t" + GN.p_description;


// Color the GridNode
GN.gisRegion.setFillColor( v_selectionColor );
GN.gisRegion.setLineColor( orange );

// Color the connected GridConnections
for ( GridConnection GC : GN.f_getAllLowerLVLConnectedGridConnections()){
	if (GC.c_connectedGISObjects.size() == 0){
		//traceln("Gridconnection with ID " + GC.p_ownerID + " and index " + GC.getIndex() + " has no GIS building");
	}
	else {
		GC.c_connectedGISObjects.forEach(gb -> gb.f_style(v_gridNodeFeedinColor, v_gridNodeFeedinColor, 2.0, null));
	}
}

f_updateUIresultsGridNode(uI_Results.v_trafo, GN);
/*ALCODEEND*/}

double f_selectBuilding(GIS_Object b,ArrayList<GIS_Object> buildingsConnectedToSelectedGC_list)
{/*ALCODESTART::1707918668163*/
c_selectedObjects = new ArrayList<GIS_Object>(buildingsConnectedToSelectedGC_list);
v_clickedObjectType = b.p_GISObjectType;

//Enable checkbox
uI_Results.getCheckbox_KPISummary().setEnabled(true);

// Color all buildings of the GridConnection associated with the selected building
if (!c_selectedObjects.get(0).c_containedGridConnections.get(0).p_ownerID.equals("-") && !c_selectedObjects.get(0).c_containedGridConnections.get(0).p_ownerID.contains("woonfunctie") && !c_selectedObjects.get(0).c_containedGridConnections.get(0).p_ownerID.contains("Onbekend")){
	for (GIS_Object obj : c_selectedObjects) { //Buildings that are grouped, select as well.
		obj.gisRegion.setFillColor(v_selectionColorAddBuildings);
	}
}

//Check the amount of GC in building
v_nbGridConnectionsInSelectedBuilding = b.c_containedGridConnections.size();

//Multiple GC in building: set additional adress in building info + buttons
if ( v_nbGridConnectionsInSelectedBuilding > 1 ){
	v_clickedObjectText = "Pand met " + b.c_containedGridConnections.size() + " adressen: " + b.p_id;
	gr_multipleBuildingInfo.setVisible(true);
}
else {
	String text = "";
	if (p_selectedProjectType == OL_ProjectType.BUSINESSPARK) {
		if (b instanceof GIS_Building) {
			if(((GIS_Building)b).p_annotation != null){
				text = ((GIS_Building)b).p_annotation + ", ";
			}
		}
		else {
			text = b.p_id + ", ";
		}
	}
	
	//Set adres text
	if (c_selectedObjects.get(0).c_containedGridConnections.get(0).p_address == null || c_selectedObjects.get(0).c_containedGridConnections.get(0).p_address.getAddress() == null) {
		text = text + "Onbekend adres";
	}
	else {
		text = text + c_selectedObjects.get(0).c_containedGridConnections.get(0).p_address.getAddress();
	}
	
	v_clickedObjectText = text;
	gr_multipleBuildingInfo.setVisible(false);
}



//Update the resultsUI
f_updateUIResultsData();

//Set the button for going to the company UI (needs to be at the end of this function!)
f_setUIButton();

//alle panden met meerdere adressen hebben op dit moment (16-7-24) dezelfde functie(s) voor ieder adres, dus dit is op dit moment zinloos
//f_listFunctions();

/*ALCODEEND*/}

double f_deselectPreviousSelect()
{/*ALCODESTART::1707918668165*/
// Update for results_ui when deselecting objects to show entire area again as default option
v_clickedObjectText = "None";
v_clickedObjectAdress = "";
v_clickedObjectDetails = "";
v_clickedObjectType = null;
button_goToUI.setVisible(false);

// We restore the colors of what we clicked on before
if (v_previousClickedObjectType == OL_GISObjectType.GRIDNODE){
	v_previousClickedGridNode = v_clickedGridNode;
	f_styleGridNodes(v_clickedGridNode);
	for ( Agent agent : v_previousClickedGridNode.f_getAllLowerLVLConnectedGridConnections()){	
		if (agent instanceof GridConnection) {
			GridConnection GC = (GridConnection)agent;
			for (GIS_Object a : GC.c_connectedGISObjects) {
				f_styleAreas(a);
			}
		}
	}
}
else if (v_previousClickedObjectType == OL_GISObjectType.BUILDING ||
		 v_previousClickedObjectType == OL_GISObjectType.SOLARFARM ||
		 v_previousClickedObjectType == OL_GISObjectType.WINDFARM ||
		 v_previousClickedObjectType == OL_GISObjectType.ELECTROLYSER ||
		 v_previousClickedObjectType == OL_GISObjectType.BATTERY ||
		 v_previousClickedObjectType == OL_GISObjectType.CHARGER){
	for(GIS_Object previousClickedObject: c_previousSelectedObjects){
		f_styleAreas(previousClickedObject);
	}
}
/*ALCODEEND*/}

double f_createPrivateCompanyUI()
{/*ALCODESTART::1708595258540*/
int i = 0;

//Create list of connection owners companies
List<ConnectionOwner> c_COCompanies = findAll(energyModel.pop_connectionOwners, p -> p.p_connectionOwnerType == OL_ConnectionOwnerType.COMPANY); 

// Give every connection owner an index nr, used to navigate to the correct private ui using the button.
while (i < c_COCompanies.size()){

	ConnectionOwner CO = c_COCompanies.get(i);
		
	CO.p_connectionOwnerIndexNr = i;
	i++;
}

//Create the private ui for every connection owner
while (v_connectionOwnerIndexNr < c_COCompanies.size()){
	
	UI_company companyUI = add_ui_companies();
	ConnectionOwner COC = findFirst(c_COCompanies, p -> p.p_connectionOwnerIndexNr == v_connectionOwnerIndexNr );	
	
	////Set unique parameters for every company_ui
	companyUI.p_company = COC;
	companyUI.p_companyName = COC.p_actorID;
	companyUI.p_amountOfGC = COC.f_getOwnedGridConnections().size();

	
	//Links with engine
	companyUI.c_ownedGridConnections = COC.f_getOwnedGridConnections();
	
	for (GridConnection GC : companyUI.c_ownedGridConnections) {
		
		//Get all buildings
		companyUI.c_ownedBuildings.addAll(GC.c_connectedGISObjects);
		
		//Add connected trafos for each GC
		companyUI.c_connectedTrafos.add(GC.p_parentNodeElectricID);
	
		//Add scenario settings for each GC
		companyUI.c_scenarioSettings_Current.add(c_scenarioMap_Current.get(GC));
		companyUI.c_scenarioSettings_Future.add(c_scenarioMap_Future.get(GC));
		
		//Initialize additional vehicles collection for each GC
		companyUI.c_additionalVehicles.put(GC, new ArrayList<J_EAVehicle>());
	}
	
	companyUI.p_amountOfBuildings = companyUI.c_ownedBuildings.size();
	companyUI.c_subTenants = COC.c_subTenants;
	
	//Initialize adress variable (changes with selected GC)
	companyUI.v_adressGC = companyUI.c_ownedGridConnections.get(0).p_address.getAddress();
	
	//Set annotation as company name, if its a generic company (otherwise potentially the addres_id name)
	if(!COC.p_detailedCompany){
		if(companyUI.c_ownedBuildings.get(0).p_annotation != null){
			companyUI.p_companyName = companyUI.c_ownedBuildings.get(0).p_annotation;
		}
	}

	//Initialize the companyUI
	companyUI.f_initializeCompanyUI();

	//Add to the collection of companyUIs
	c_companyUIs.add( companyUI );
	
	//set boolean for has privateUI in owner: True
	COC.b_hasPrivateUI = true;
	
	v_connectionOwnerIndexNr++;
}

v_connectionOwnerIndexNr = 0;

//Get the ghost vehicles for the transport slider tab
uI_Tabs.pop_tabMobility.get(0).f_calculateNumberOfGhostVehicles();

//Get the ghost heating systems
uI_Tabs.pop_tabHeating.get(0).f_calculateNumberOfGhostHeatingSystems();

/*ALCODEEND*/}

double f_connectResultsUI()
{/*ALCODESTART::1709716821854*/
//Style resultsUI
f_styleResultsUI();

//Set ResultsUI radiobutton setup
if(settings.resultsUIRadioButtonSetup() != null){
	uI_Results.v_selectedRadioButton = settings.resultsUIRadioButtonSetup();
}

//Initialize main area collection
AreaCollection mainArea = uI_Results.add_pop_areaResults(OL_GISObjectType.REGION, "Main");
uI_Results.v_area = mainArea;

//Set active energyCarriers
mainArea.v_activeProductionEnergyCarriers = energyModel.v_activeProductionEnergyCarriers;
mainArea.v_activeConsumptionEnergyCarriers = energyModel.v_activeConsumptionEnergyCarriers;



//Set boolean if it has connection with heatgrid
for (GridNode GN : energyModel.f_getGridNodesTopLevel()) {
	if(GN.p_energyCarrier == OL_EnergyCarriers.HEAT){
		mainArea.b_hasHeatGridConnection = true;
		break;
	}
}
//Set booleans
f_updateActiveAssetBooleansGC(mainArea, energyModel.f_getGridConnections());

//Datasets for live charts
//Demand
mainArea.dsm_liveConsumption_kW = energyModel.dsm_liveDemand_kW;
mainArea.v_dataElectricityBaseloadConsumptionLiveWeek_kW = energyModel.data_baseloadElectricityDemand_kW;
mainArea.v_dataElectricityForHeatConsumptionLiveWeek_kW = energyModel.data_heatPumpElectricityDemand_kW;
mainArea.v_dataElectricityForTransportConsumptionLiveWeek_kW = energyModel.data_electricVehicleDemand_kW;
mainArea.v_dataElectricityForStorageConsumptionLiveWeek_kW = energyModel.data_batteryCharging_kW;
mainArea.v_dataElectricityForHydrogenConsumptionLiveWeek_kW = energyModel.data_hydrogenElectricityDemand_kW;
mainArea.v_dataElectricityForCookingConsumptionLiveWeek_kW = energyModel.data_cookingElectricityDemand_kW;

//Supply
mainArea.dsm_liveProduction_kW = energyModel.dsm_liveSupply_kW;

mainArea.v_dataWindElectricityProductionLiveWeek_kW = energyModel.data_windGeneration_kW;
mainArea.v_dataPVElectricityProductionLiveWeek_kW = energyModel.data_PVGeneration_kW;
mainArea.v_dataStorageElectricityProductionLiveWeek_kW = energyModel.data_batteryDischarging_kW;
mainArea.v_dataV2GElectricityProductionLiveWeek_kW = energyModel.data_V2GSupply_kW;
mainArea.v_dataCHPElectricityProductionLiveWeek_kW = energyModel.data_CHPElectricityProductionLiveWeek_kW;

//Datasets for live summerWeek chart
//Demand
mainArea.dsm_summerWeekConsumptionDataSets_kW = energyModel.dsm_summerWeekDemandDataSets_kW;
mainArea.v_dataElectricityBaseloadConsumptionSummerWeek_kW = energyModel.data_summerWeekBaseloadElectricityDemand_kW;
mainArea.v_dataElectricityForHeatConsumptionSummerWeek_kW = energyModel.data_summerWeekHeatPumpElectricityDemand_kW;
mainArea.v_dataElectricityForTransportConsumptionSummerWeek_kW = energyModel.data_summerWeekElectricVehicleDemand_kW;
mainArea.v_dataElectricityForStorageConsumptionSummerWeek_kW = energyModel.data_summerWeekBatteriesDemand_kW;
mainArea.v_dataElectricityForHydrogenConsumptionSummerWeek_kW = energyModel.data_summerWeekElectrolyserDemand_kW;
mainArea.v_dataElectricityForCookingConsumptionSummerWeek_kW = energyModel.data_summerWeekCookingElectricityDemand_kW;

//Supply
mainArea.dsm_summerWeekProductionDataSets_kW = energyModel.dsm_summerWeekSupplyDataSets_kW;

mainArea.v_dataElectricityWindProductionSummerWeek_kW = energyModel.data_summerWeekWindGeneration_kW;
mainArea.v_dataElectricityPVProductionSummerWeek_kW = energyModel.data_summerWeekPVGeneration_kW;
mainArea.v_dataElectricityStorageProductionSummerWeek_kW = energyModel.data_summerWeekBatteriesSupply_kW;
mainArea.v_dataElectricityV2GProductionSummerWeek_kW = energyModel.data_summerWeekV2GSupply_kW;

mainArea.v_dataNetLoadSummerWeek_kW = energyModel.data_summerWeekNetLoad_kW;
mainArea.v_dataElectricityDeliveryCapacitySummerWeek_kW = new DataSet((int) (168 / energyModel.p_timeStep_h));
mainArea.v_dataElectricityFeedInCapacitySummerWeek_kW = new DataSet((int) (168 / energyModel.p_timeStep_h));
mainArea.v_dataElectricityDeliveryCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek, energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
mainArea.v_dataElectricityDeliveryCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek + 7*24, energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
mainArea.v_dataElectricityFeedInCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek, -energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
mainArea.v_dataElectricityFeedInCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek + 7*24, -energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);


//Datasets for live winterWeek chart
//Demand
mainArea.dsm_winterWeekConsumptionDataSets_kW = energyModel.dsm_winterWeekDemandDataSets_kW;
mainArea.v_dataElectricityBaseloadConsumptionWinterWeek_kW = energyModel.data_winterWeekBaseloadElectricityDemand_kW;
mainArea.v_dataElectricityForHeatConsumptionWinterWeek_kW = energyModel.data_winterWeekHeatPumpElectricityDemand_kW;
mainArea.v_dataElectricityForTransportConsumptionWinterWeek_kW = energyModel.data_winterWeekElectricVehicleDemand_kW;
mainArea.v_dataElectricityForStorageConsumptionWinterWeek_kW = energyModel.data_winterWeekBatteriesDemand_kW;
mainArea.v_dataElectricityForCookingConsumptionWinterWeek_kW = energyModel.data_winterWeekCookingElectricityDemand_kW;

//Supply
mainArea.dsm_winterWeekProductionDataSets_kW = energyModel.dsm_winterWeekSupplyDataSets_kW;

mainArea.v_dataElectricityWindProductionWinterWeek_kW = energyModel.data_winterWeekWindGeneration_kW;
mainArea.v_dataElectricityPVProductionWinterWeek_kW = energyModel.data_winterWeekPVGeneration_kW;
mainArea.v_dataElectricityStorageProductionWinterWeek_kW = energyModel.data_winterWeekBatteriesSupply_kW;
mainArea.v_dataElectricityV2GProductionWinterWeek_kW = energyModel.data_winterWeekV2GSupply_kW;

mainArea.v_dataNetLoadWinterWeek_kW = energyModel.data_winterWeekNetLoad_kW;
mainArea.v_dataElectricityDeliveryCapacityWinterWeek_kW = new DataSet((int) (168 / energyModel.p_timeStep_h));
mainArea.v_dataElectricityFeedInCapacityWinterWeek_kW = new DataSet((int) (168 / energyModel.p_timeStep_h));
mainArea.v_dataElectricityDeliveryCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek, energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
mainArea.v_dataElectricityDeliveryCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek+ 7*24, energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
mainArea.v_dataElectricityFeedInCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek, -energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
mainArea.v_dataElectricityFeedInCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek+ 7*24, -energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);


//Datasets for yearly profiles chart
//Demand
mainArea.dsm_dailyAverageConsumptionDataSets_kW = energyModel.dsm_dailyAverageDemandDataSets_kW;
mainArea.dsm_dailyAverageProductionDataSets_kW = energyModel.dsm_dailyAverageSupplyDataSets_kW;

mainArea.v_dataElectricityBaseloadConsumptionYear_kW = energyModel.data_annualBaseloadElectricityDemand_kW;
mainArea.v_dataElectricityForHeatConsumptionYear_kW = energyModel.data_annualHeatPumpElectricityDemand_kW;
mainArea.v_dataElectricityForTransportConsumptionYear_kW = energyModel.data_annualElectricVehicleDemand_kW;
mainArea.v_dataElectricityForStorageConsumptionYear_kW = energyModel.data_annualBatteriesDemand_kW;
mainArea.v_dataElectricityForHydrogenConsumptionYear_kW = energyModel.data_annualElectrolyserDemand_kW;
mainArea.v_dataElectricityForCookingConsumptionYear_kW = energyModel.data_annualCookingElectricityDemand_kW;

//Final energy consumption dataset year
mainArea.data_dailyAverageFinalEnergyConsumption_kW = energyModel.data_totalFinalEnergyConsumption_kW;

//Supply
mainArea.v_dataElectricityWindProductionYear_kW = energyModel.data_annualWindGeneration_kW;
mainArea.v_dataElectricityPVProductionYear_kW = energyModel.data_annualPVGeneration_kW;
mainArea.v_dataElectricityStorageProductionYear_kW = energyModel.data_annualBatteriesSupply_kW;
mainArea.v_dataElectricityV2GProductionYear_kW = energyModel.data_annualV2GSupply_kW;
mainArea.v_dataElectricityCHPProductionYear_kW = energyModel.data_annualCHPElectricityProduction_kW;

//Datasets for netloaddurationcurves
mainArea.v_dataNetbelastingDuurkrommeYear_kW = energyModel.data_netbelastingDuurkromme_kW;
mainArea.v_dataNetbelastingDuurkrommeYearVorige_kW = energyModel.data_netbelastingDuurkrommeVorige_kW;

mainArea.v_dataNetbelastingDuurkrommeSummer_kW = energyModel.data_summerWeekNetbelastingDuurkromme_kW;
mainArea.v_dataNetbelastingDuurkrommeWinter_kW = energyModel.data_winterWeekNetbelastingDuurkromme_kW;
mainArea.v_dataNetbelastingDuurkrommeDaytime_kW = energyModel.data_daytimeNetbelastingDuurkromme_kW;
mainArea.v_dataNetbelastingDuurkrommeNighttime_kW = energyModel.data_nighttimeNetbelastingDuurkromme_kW;
mainArea.v_dataNetbelastingDuurkrommeWeekend_kW = energyModel.data_weekendNetbelastingDuurkromme_kW;
mainArea.v_dataNetbelastingDuurkrommeWeekday_kW = energyModel.data_weekdayNetbelastingDuurkromme_kW;


uI_Results.v_gridConnection = uI_Results.add_pop_areaResults(OL_GISObjectType.BUILDING, "GC");
uI_Results.v_trafo = uI_Results.add_pop_areaResults(OL_GISObjectType.GRIDNODE, "GN");
uI_Results.v_collective = uI_Results.add_pop_areaResults();

//uI_Results.energyModel = energyModel;
uI_Results.f_initialize();

/*ALCODEEND*/}

double f_updateUIresultsMainArea()
{/*ALCODESTART::1709716821856*/
AreaCollection area = uI_Results.v_area;

//Set active energyCarriers
area.v_activeProductionEnergyCarriers = energyModel.v_activeProductionEnergyCarriers;
area.v_activeConsumptionEnergyCarriers = energyModel.v_activeConsumptionEnergyCarriers;

//Previous totals
area.v_previousTotals.setPreviousTotalImports_MWh(area.fm_totalImports_MWh);
area.v_previousTotals.setPreviousTotalExports_MWh(area.fm_totalExports_MWh);
area.v_previousTotals.setPreviousTotalConsumedEnergy_MWh(area.v_totalEnergyConsumed_MWh);
area.v_previousTotals.setPreviousTotalProducedEnergy_MWh(area.v_totalEnergyProduced_MWh);
area.v_previousTotals.setPreviousSelfConsumedEnergy_MWh(area.v_totalEnergySelfConsumed_MWh);
area.v_previousTotals.setPreviousImportedEnergy_MWh(area.v_totalEnergyImport_MWh);
area.v_previousTotals.setPreviousExportedEnergy_MWh(area.v_totalEnergyExport_MWh);
area.v_previousTotals.setPreviousSelfConsumedElectricity_MWh(area.v_totalElectricitySelfConsumed_MWh);
area.v_previousTotals.setPreviousElectricityConsumed_MWh(area.v_totalElectricityConsumed_MWh);
area.v_previousTotals.setPreviousTotalTimeOverloadedTransformers_hr(area.v_totalTimeOverloadedTransformers_h);

// Net Load
area.v_dataNetLoadYear_kW = energyModel.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries();
area.data_gridCapacityDeliveryYear_kW.add(0, energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
area.data_gridCapacityDeliveryYear_kW.add(8760, energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
area.data_gridCapacityFeedInYear_kW.add(0, -energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
area.data_gridCapacityFeedInYear_kW.add(8760, -energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);

//Datasets for live charts
//Demand
area.dsm_liveConsumption_kW = energyModel.dsm_liveDemand_kW;
area.v_dataElectricityBaseloadConsumptionLiveWeek_kW = energyModel.data_baseloadElectricityDemand_kW;
area.v_dataElectricityForHeatConsumptionLiveWeek_kW = energyModel.data_heatPumpElectricityDemand_kW;
area.v_dataElectricityForTransportConsumptionLiveWeek_kW = energyModel.data_electricVehicleDemand_kW;
area.v_dataElectricityForStorageConsumptionLiveWeek_kW = energyModel.data_batteryCharging_kW;
area.v_dataElectricityForHydrogenConsumptionLiveWeek_kW = energyModel.data_hydrogenElectricityDemand_kW;
area.v_dataElectricityForCookingConsumptionLiveWeek_kW = energyModel.data_cookingElectricityDemand_kW;

//Supply
area.dsm_liveProduction_kW = energyModel.dsm_liveSupply_kW;
area.v_dataWindElectricityProductionLiveWeek_kW = energyModel.data_windGeneration_kW;
area.v_dataPVElectricityProductionLiveWeek_kW = energyModel.data_PVGeneration_kW;
area.v_dataStorageElectricityProductionLiveWeek_kW = energyModel.data_batteryDischarging_kW;
area.v_dataV2GElectricityProductionLiveWeek_kW = energyModel.data_V2GSupply_kW;
//area.v_dataHydrogenSupplyLiveWeek_kW = energyModel.data_hydrogenSupply_kW;
area.v_dataCHPElectricityProductionLiveWeek_kW = energyModel.data_CHPElectricityProductionLiveWeek_kW;

//Total
area.v_dataNetLoadLiveWeek_kW = energyModel.data_totalGridLoad_kW;

//Capacity
area.v_dataElectricityDeliveryCapacityLiveWeek_kW = energyModel.data_gridCapacityDemand_kW;
area.v_dataElectricityFeedInCapacityLiveWeek_kW = energyModel.data_gridCapacitySupply_kW;


//Datasets for live summerWeek chart
//Demand
area.dsm_summerWeekConsumptionDataSets_kW = energyModel.dsm_summerWeekDemandDataSets_kW;
area.v_dataElectricityBaseloadConsumptionSummerWeek_kW = energyModel.data_summerWeekBaseloadElectricityDemand_kW;
area.v_dataElectricityForHeatConsumptionSummerWeek_kW = energyModel.data_summerWeekHeatPumpElectricityDemand_kW;
area.v_dataElectricityForTransportConsumptionSummerWeek_kW = energyModel.data_summerWeekElectricVehicleDemand_kW;
area.v_dataElectricityForStorageConsumptionSummerWeek_kW = energyModel.data_summerWeekBatteriesDemand_kW;
area.v_dataElectricityForHydrogenConsumptionSummerWeek_kW = energyModel.data_summerWeekElectrolyserDemand_kW;
area.v_dataElectricityForCookingConsumptionSummerWeek_kW = energyModel.data_summerWeekCookingElectricityDemand_kW;

//Supply
area.dsm_summerWeekProductionDataSets_kW = energyModel.dsm_summerWeekSupplyDataSets_kW;
area.v_dataElectricityWindProductionSummerWeek_kW = energyModel.data_summerWeekWindGeneration_kW;
area.v_dataElectricityPVProductionSummerWeek_kW = energyModel.data_summerWeekPVGeneration_kW;
area.v_dataElectricityStorageProductionSummerWeek_kW = energyModel.data_summerWeekBatteriesSupply_kW;
area.v_dataElectricityV2GProductionSummerWeek_kW = energyModel.data_summerWeekV2GSupply_kW;
area.v_dataElectricityCHPProductionSummerWeek_kW = energyModel.data_summerWeekCHPElectricityProduction_kW;

//Net load
area.v_dataNetLoadSummerWeek_kW = energyModel.data_summerWeekNetLoad_kW;
area.v_dataElectricityDeliveryCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek, energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
area.v_dataElectricityDeliveryCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek + 7*24, energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
area.v_dataElectricityFeedInCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek, -energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
area.v_dataElectricityFeedInCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek + 7*24, -energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);


//Datasets for live winterWeek chart
//Demand
area.dsm_winterWeekConsumptionDataSets_kW = energyModel.dsm_winterWeekDemandDataSets_kW;
area.v_dataElectricityBaseloadConsumptionWinterWeek_kW = energyModel.data_winterWeekBaseloadElectricityDemand_kW;
area.v_dataElectricityForHeatConsumptionWinterWeek_kW = energyModel.data_winterWeekHeatPumpElectricityDemand_kW;
area.v_dataElectricityForTransportConsumptionWinterWeek_kW = energyModel.data_winterWeekElectricVehicleDemand_kW;
area.v_dataElectricityForStorageConsumptionWinterWeek_kW = energyModel.data_winterWeekBatteriesDemand_kW;
area.v_dataElectricityForCookingConsumptionWinterWeek_kW = energyModel.data_winterWeekCookingElectricityDemand_kW;

//Supply
area.dsm_winterWeekProductionDataSets_kW = energyModel.dsm_winterWeekSupplyDataSets_kW;
area.v_dataElectricityWindProductionWinterWeek_kW = energyModel.data_winterWeekWindGeneration_kW;
area.v_dataElectricityPVProductionWinterWeek_kW = energyModel.data_winterWeekPVGeneration_kW;
area.v_dataElectricityStorageProductionWinterWeek_kW = energyModel.data_winterWeekBatteriesSupply_kW;
area.v_dataElectricityV2GProductionWinterWeek_kW = energyModel.data_winterWeekV2GSupply_kW;
area.v_dataElectricityCHPProductionWinterWeek_kW = energyModel.data_winterWeekCHPElectricityProduction_kW;

//Netload
area.v_dataNetLoadWinterWeek_kW = energyModel.data_winterWeekNetLoad_kW;
area.v_dataElectricityDeliveryCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek, energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
area.v_dataElectricityDeliveryCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek + 7*24, energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
area.v_dataElectricityFeedInCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek, -energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);
area.v_dataElectricityFeedInCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek + 7*24, -energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW);


//Datasets for yearly profiles chart
//Demand
area.dsm_dailyAverageConsumptionDataSets_kW = energyModel.dsm_dailyAverageDemandDataSets_kW;
area.dsm_dailyAverageProductionDataSets_kW = energyModel.dsm_dailyAverageSupplyDataSets_kW;
area.v_dataElectricityBaseloadConsumptionYear_kW = energyModel.data_annualBaseloadElectricityDemand_kW;
area.v_dataElectricityForHeatConsumptionYear_kW = energyModel.data_annualHeatPumpElectricityDemand_kW;
area.v_dataElectricityForTransportConsumptionYear_kW = energyModel.data_annualElectricVehicleDemand_kW;
area.v_dataElectricityForStorageConsumptionYear_kW = energyModel.data_annualBatteriesDemand_kW;
area.v_dataElectricityForHydrogenConsumptionYear_kW = energyModel.data_annualElectrolyserDemand_kW;
area.v_dataElectricityForCookingConsumptionYear_kW = energyModel.data_annualCookingElectricityDemand_kW;

//Supply
area.v_dataElectricityWindProductionYear_kW = energyModel.data_annualWindGeneration_kW;
area.v_dataElectricityPVProductionYear_kW = energyModel.data_annualPVGeneration_kW;
area.v_dataElectricityStorageProductionYear_kW = energyModel.data_annualBatteriesSupply_kW;
area.v_dataElectricityV2GProductionYear_kW = energyModel.data_annualV2GSupply_kW;
area.v_dataElectricityCHPProductionYear_kW = energyModel.data_annualCHPElectricityProduction_kW;

// Data for gespreksleidraad1
area.v_dataNetLoadYear_kW = energyModel.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries();

//Datasets for netloaddurationcurves
area.v_dataNetbelastingDuurkrommeYear_kW = energyModel.data_netbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeYearVorige_kW = energyModel.data_netbelastingDuurkrommeVorige_kW;

area.v_dataNetbelastingDuurkrommeSummer_kW = energyModel.data_summerWeekNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeWinter_kW = energyModel.data_winterWeekNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeDaytime_kW = energyModel.data_daytimeNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeNighttime_kW = energyModel.data_nighttimeNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeWeekend_kW = energyModel.data_weekendNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeWeekday_kW = energyModel.data_weekdayNetbelastingDuurkromme_kW;


// Can't use pointer for (immutable) primitives in Java, so need to manually update results after a year-sim!!
area.v_gridCapacityDelivery_kW = energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW;
area.v_gridCapacityFeedIn_kW = energyModel.f_getGridNodesTopLevel().get(0).p_capacity_kW;
area.b_isRealDeliveryCapacityAvailable = energyModel.f_getGridNodesTopLevel().get(0).p_realCapacityAvailable;
area.b_isRealFeedinCapacityAvailable = energyModel.f_getGridNodesTopLevel().get(0).p_realCapacityAvailable;

// KPIs for 'samenvatting' 
area.v_modelSelfConsumption_fr = energyModel.v_modelSelfConsumption_fr;
area.v_individualSelfSufficiency_fr = energyModel.v_individualSelfSufficiency_fr;
area.v_individualSelfconsumption_fr = energyModel.v_individualSelfConsumption_fr;
area.v_modelSelfSufficiency_fr = energyModel.v_modelSelfSufficiency_fr;
area.v_totalTimeOverloadedTransformers_h = energyModel.v_gridOverloadDuration_h;

//Yearly
area.fm_totalImports_MWh = energyModel.fm_totalImports_MWh;
area.fm_totalExports_MWh = energyModel.fm_totalExports_MWh;

area.v_totalEnergyImport_MWh = energyModel.v_totalEnergyImport_MWh;
area.v_totalEnergyExport_MWh = energyModel.v_totalEnergyExport_MWh;

area.v_totalEnergyProduced_MWh = energyModel.v_totalEnergyProduced_MWh;
area.v_totalEnergyConsumed_MWh = energyModel.v_totalEnergyConsumed_MWh;
area.v_totalEnergySelfConsumed_MWh = energyModel.v_totalEnergySelfConsumed_MWh;

// Where is the Produced Electricity ???
area.v_totalElectricityConsumed_MWh = energyModel.v_totalElectricityConsumed_MWh;
area.v_totalElectricitySelfConsumed_MWh = energyModel.v_totalElectricitySelfConsumed_MWh;

// Summer/winter
area.fm_summerWeekImports_MWh = energyModel.fm_summerWeekImports_MWh;
area.fm_summerWeekExports_MWh = energyModel.fm_summerWeekImports_MWh;
area.fm_winterWeekImports_MWh = energyModel.fm_summerWeekImports_MWh;
area.fm_winterWeekExports_MWh = energyModel.fm_summerWeekImports_MWh;

area.v_summerWeekEnergyImport_MWh = energyModel.v_summerWeekEnergyImport_MWh;
area.v_summerWeekEnergyExport_MWh = energyModel.v_summerWeekEnergyExport_MWh;

area.v_summerWeekEnergyProduced_MWh = energyModel.v_summerWeekEnergyProduced_MWh;
area.v_summerWeekEnergyConsumed_MWh = energyModel.v_summerWeekEnergyConsumed_MWh;
area.v_summerWeekEnergySelfConsumed_MWh = energyModel.v_summerWeekEnergySelfConsumed_MWh;

area.v_summerWeekElectricityProduced_MWh = energyModel.v_summerWeekElectricityProduced_MWh;
area.v_summerWeekElectricityConsumed_MWh = energyModel.v_summerWeekElectricityConsumed_MWh;
area.v_summerWeekElectricitySelfConsumed_MWh = energyModel.v_summerWeekElectricitySelfConsumed_MWh;

area.v_winterWeekEnergyImport_MWh = energyModel.v_winterWeekEnergyImport_MWh;
area.v_winterWeekEnergyExport_MWh = energyModel.v_winterWeekEnergyExport_MWh;

area.v_winterWeekEnergyProduced_MWh = energyModel.v_winterWeekEnergyProduced_MWh;
area.v_winterWeekEnergyConsumed_MWh = energyModel.v_winterWeekEnergyConsumed_MWh;
area.v_winterWeekEnergySelfConsumed_MWh = energyModel.v_winterWeekEnergySelfConsumed_MWh;

area.v_winterWeekElectricityProduced_MWh = energyModel.v_winterWeekElectricityProduced_MWh;
area.v_winterWeekElectricityConsumed_MWh = energyModel.v_winterWeekElectricityConsumed_MWh;
area.v_winterWeekElectricitySelfConsumed_MWh = energyModel.v_winterWeekElectricitySelfConsumed_MWh;

// Day/night
area.fm_daytimeImports_MWh = energyModel.fm_daytimeImports_MWh;
area.fm_daytimeExports_MWh = energyModel.fm_daytimeExports_MWh;
area.fm_nighttimeImports_MWh = energyModel.fm_nighttimeImports_MWh;
area.fm_nighttimeExports_MWh = energyModel.fm_nighttimeExports_MWh;

area.v_daytimeEnergyImport_MWh = energyModel.v_daytimeEnergyImport_MWh;
area.v_daytimeEnergyExport_MWh = energyModel.v_daytimeEnergyExport_MWh;

area.v_daytimeEnergyProduced_MWh = energyModel.v_daytimeEnergyProduced_MWh;
area.v_daytimeEnergyConsumed_MWh = energyModel.v_daytimeEnergyConsumed_MWh;
area.v_daytimeEnergySelfConsumed_MWh = energyModel.v_daytimeEnergySelfConsumed_MWh;

area.v_daytimeElectricityProduced_MWh = energyModel.v_daytimeElectricityProduced_MWh;
area.v_daytimeElectricityConsumed_MWh = energyModel.v_daytimeElectricityConsumed_MWh;
area.v_daytimeElectricitySelfConsumed_MWh = energyModel.v_daytimeElectricitySelfConsumed_MWh;

area.v_nighttimeEnergyImport_MWh = energyModel.v_nighttimeEnergyImport_MWh;
area.v_nighttimeEnergyExport_MWh = energyModel.v_nighttimeEnergyExport_MWh;

area.v_nighttimeEnergyProduced_MWh = energyModel.v_nighttimeEnergyProduced_MWh;
area.v_nighttimeEnergyConsumed_MWh = energyModel.v_nighttimeEnergyConsumed_MWh;
area.v_nighttimeEnergySelfConsumed_MWh = energyModel.v_nighttimeEnergySelfConsumed_MWh;

area.v_nighttimeElectricityProduced_MWh = energyModel.v_nighttimeElectricityProduced_MWh;
area.v_nighttimeElectricityConsumed_MWh = energyModel.v_nighttimeElectricityConsumed_MWh;
area.v_nighttimeElectricitySelfConsumed_MWh = energyModel.v_nighttimeElectricitySelfConsumed_MWh;

// Week/weekend
area.fm_weekdayImports_MWh = energyModel.fm_weekdayImports_MWh;
area.fm_weekdayExports_MWh = energyModel.fm_weekdayExports_MWh;
area.fm_weekendImports_MWh = energyModel.fm_weekendImports_MWh;
area.fm_weekendExports_MWh = energyModel.fm_weekendExports_MWh;

area.v_weekdayEnergyImport_MWh = energyModel.v_weekdayEnergyImport_MWh;
area.v_weekdayEnergyExport_MWh = energyModel.v_weekdayEnergyExport_MWh;

area.v_weekdayEnergyProduced_MWh = energyModel.v_weekdayEnergyProduced_MWh;
area.v_weekdayEnergyConsumed_MWh = energyModel.v_weekdayEnergyConsumed_MWh;
area.v_weekdayEnergySelfConsumed_MWh = energyModel.v_weekdayEnergySelfConsumed_MWh;

area.v_weekdayElectricityProduced_MWh = energyModel.v_weekdayElectricityProduced_MWh;
area.v_weekdayElectricityConsumed_MWh = energyModel.v_weekdayElectricityConsumed_MWh;
area.v_weekdayElectricitySelfConsumed_MWh = energyModel.v_weekdayElectricitySelfConsumed_MWh;

area.v_weekendEnergyImport_MWh = energyModel.v_weekendEnergyImport_MWh;
area.v_weekendEnergyExport_MWh = energyModel.v_weekendEnergyExport_MWh;

area.v_weekendEnergyProduced_MWh = energyModel.v_weekendEnergyProduced_MWh;
area.v_weekendEnergyConsumed_MWh = energyModel.v_weekendEnergyConsumed_MWh;
area.v_weekendEnergySelfConsumed_MWh = energyModel.v_weekendEnergySelfConsumed_MWh;

area.v_weekendElectricityProduced_MWh = energyModel.v_weekendElectricityProduced_MWh;
area.v_weekendElectricityConsumed_MWh = energyModel.v_weekendElectricityConsumed_MWh;
area.v_weekendElectricitySelfConsumed_MWh = energyModel.v_weekendElectricitySelfConsumed_MWh;

////Gespreksleidraad Additions

//Final energy consumption dataset year
area.data_dailyAverageFinalEnergyConsumption_kW = energyModel.data_totalFinalEnergyConsumption_kW;

//Subdivision of heating assets
area.v_totalElectricityConsumptionHeatpumps_MWh = energyModel.v_totalElectricityConsumptionHeatpumps_MWh;
area.v_totalEnergyConsumptionForDistrictHeating_MWh = energyModel.v_totalEnergyConsumptionForDistrictHeating_MWh;

//Subdivision of Production
area.v_totalPVGeneration_MWh = energyModel.v_totalPVGeneration_MWh;
area.v_totalWindGeneration_MWh = energyModel.v_totalWindGeneration_MWh;

//Potential
//area.v_PVPotential_kW = p_rooftopPVPotential_kW;
//area.v_windPotential_kW = 0;

//Curtailment
area.v_totalEnergyCurtailed_MWh = energyModel.v_totalEnergyCurtailed_MWh;
/*ALCODEEND*/}

double f_updateUIresultsEnergyCoop(AreaCollection area,EnergyCoop EC)
{/*ALCODESTART::1709716821860*/
//Set active energyCarriers
area.v_activeProductionEnergyCarriers = energyModel.v_activeProductionEnergyCarriers;
area.v_activeConsumptionEnergyCarriers = energyModel.v_activeConsumptionEnergyCarriers;

// Can't use pointer for (immutable) primitives in Java, so need to manually update results after a year-sim!!
area.v_gridCapacityDelivery_kW = EC.p_connectionCapacity_kW;
area.v_gridCapacityFeedIn_kW = EC.p_connectionCapacity_kW;

// KPIs for 'samenvatting' 
area.v_modelSelfConsumption_fr = EC.v_totalEnergyProduced_MWh > 0 ? EC.v_totalEnergySelfConsumed_MWh/EC.v_totalEnergyProduced_MWh : 0.0;
//area.v_individualSelfSufficiency_fr = EC.v_individualSelfSufficiency_fr;
area.v_modelSelfSufficiency_fr = EC.v_totalEnergyProduced_MWh > 0 ? EC.v_totalEnergySelfConsumed_MWh/EC.v_totalEnergyConsumed_MWh: 0.0;
//area.v_totalTimeOverloadedTransformers_h = EC.v_netOverloadDuration_h;

//Datasets for live demand chart
//Demand
//AreaCollection companyArea = uI_Results.v_company;
area.dsm_liveConsumption_kW = EC.dsm_liveDemand_kW;
//area.v_dataPetroleumProductsDemandLiveWeek_kW = EC.data_dieselDemand_kW;
//area.v_dataNaturalGasDemandLiveWeek_kW = EC.data_naturalGasDemand_kW;
area.v_dataElectricityBaseloadConsumptionLiveWeek_kW = EC.data_baseloadElectricityDemand_kW;
area.v_dataElectricityForHeatConsumptionLiveWeek_kW = EC.data_heatPumpElectricityDemand_kW;
area.v_dataElectricityForTransportConsumptionLiveWeek_kW = EC.data_electricVehicleDemand_kW;
area.v_dataElectricityForStorageConsumptionLiveWeek_kW = EC.data_batteryCharging_kW;
area.v_dataElectricityForCookingConsumptionLiveWeek_kW = EC.data_cookingElectricityDemand_kW;

//Supply
area.dsm_liveProduction_kW = EC.dsm_liveSupply_kW;
area.v_dataWindElectricityProductionLiveWeek_kW = EC.data_windGeneration_kW;
area.v_dataPVElectricityProductionLiveWeek_kW = EC.data_PVGeneration_kW;
area.v_dataStorageElectricityProductionLiveWeek_kW = EC.data_batteryDischarging_kW;
area.v_dataV2GElectricityProductionLiveWeek_kW = EC.data_V2GSupply_kW;

//Datasets for live summerWeek chart
//Demand
//area.v_dataPetroleumProductsDemandSummerWeek_kW = EC.data_summerWeekDieselDemand_kW;
//area.v_dataNaturalGasDemandSummerWeek_kW = EC.data_summerWeekNaturalGasDemand_kW;
/*area.v_dataElectricityBaseloadDemandSummerWeek_kW = EC.data_summerWeekBaseloadElectricityDemand_kW;
area.v_dataElectricityForHeatDemandSummerWeek_kW = EC.data_summerWeekHeatPumpElectricityDemand_kW;
area.v_dataElectricityForTransportDemandSummerWeek_kW = EC.data_summerWeekElectricTrucksDemand_kW;
area.v_dataElectricityForStorageDemandSummerWeek_kW = EC.data_summerWeekNeighborhoodBatteryDemand_kW;
//Supply
area.v_dataWindElectricitySupplySummerWeek_kW = EC.data_summerWeekWindGeneration_kW;
area.v_dataPVElectricitySupplySummerWeek_kW = EC.data_summerWeekPVGeneration_kW;
area.v_dataStorageElectricitySupplySummerWeek_kW = EC.data_summerWeekNeighborhoodBatterySupply_kW;
area.v_dataV2GElectricitySupplySummerWeek_kW = EC.data_summerWeekV2GSupply_kW;

//Datasets for live winterWeek chart
//Demand
//area.v_dataPetroleumProductsDemandWinterWeek_kW = EC.data_winterWeekDieselDemand_kW;
//area.v_dataNaturalGasDemandWinterWeek_kW = EC.data_winterWeekNaturalGasDemand_kW;
area.v_dataElectricityBaseloadDemandWinterWeek_kW = EC.data_winterWeekBaseloadElectricityDemand_kW;
area.v_dataElectricityForHeatDemandWinterWeek_kW = EC.data_winterWeekHeatPumpElectricityDemand_kW;
area.v_dataElectricityForTransportDemandWinterWeek_kW = EC.data_winterWeekElectricTrucksDemand_kW;
area.v_dataElectricityForStorageDemandWinterWeek_kW = EC.data_winterWeekNeighborhoodBatteryDemand_kW;
//Supply
area.v_dataWindElectricitySupplyWinterWeek_kW = EC.data_winterWeekWindGeneration_kW;
area.v_dataPVElectricitySupplyWinterWeek_kW = EC.data_winterWeekPVGeneration_kW;
area.v_dataStorageElectricitySupplyWinterWeek_kW = EC.data_winterWeekNeighborhoodBatterySupply_kW;
area.v_dataV2GElectricitySupplyWinterWeek_kW = EC.data_winterWeekV2GSupply_kW;


//Datasets for yearly profiles chart
//Demand
//area.v_dataPetroleumProductsDemandYear_MWh = EC.data_dieselDemand_MWh;
//area.v_dataNaturalGasDemandYear_MWh = EC.data_naturalGasDemand_MWh;
area.v_dataElectricityBaseloadDemandYear_MWh = EC.data_annualBaseloadElectricityDemand_MWh;
area.v_dataElectricityForHeatDemandYear_MWh = EC.data_annualHeatPumpElectricityDemand_MWh;
area.v_dataElectricityForTransportDemandYear_MWh = EC.data_annualElectricTrucksDemand_MWh;
area.v_dataElectricityForStorageDemandYear_MWh = EC.data_annualNeighborhoodBatteryDemand_MWh;

//Supply
area.v_dataElectricityWindSupplyYear_MWh = EC.data_annualWindGeneration_MWh;
area.v_dataElectricityPVSupplyYear_MWh = EC.data_annualPVGeneration_MWh;
area.v_dataElectricityStorageSupplyYear_MWh = EC.data_annualNeighborhoodBatterySupply_MWh;
area.v_dataElectricityV2GSupplyYear_MWh = EC.data_annualV2GSupply_MWh;

// Data for gespreksleidraad1
area.v_dataElectricityDemandYear_MWh = EC.data_annualElectricityDemand_MWh;
area.v_dataElectricitySupplyYear_MWh = EC.data_annualElectricitySupply_MWh;*/
area.v_dataNetLoadYear_kW = EC.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries();

//Datasets for netloaddurationcurves
//EC.f_duurkrommes();
area.v_dataNetbelastingDuurkrommeYear_kW = EC.f_getDuurkromme();
/*area.v_dataNetbelastingDuurkrommeSummer_kW = EC.data_summerWeekBusinessParkNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeWinter_kW = EC.data_winterWeekBusinessParkNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeDaytime_kW = EC.data_daytimeNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeNighttime_kW = EC.data_nighttimeNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeWeekend_kW = EC.data_weekendNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeWeekday_kW = EC.data_weekdayNetbelastingDuurkromme_kW;*/



/////////////////////////////////////////


//Yearly
for (OL_EnergyCarriers energyCarrier : energyModel.v_activeEnergyCarriers) {
	area.fm_totalImports_MWh.put( energyCarrier, EC.fm_totalImports_MWh.get(energyCarrier));
	area.fm_totalExports_MWh.put( energyCarrier, EC.fm_totalExports_MWh.get(energyCarrier));
}
area.v_totalEnergyImport_MWh = EC.v_totalEnergyImport_MWh;
area.v_totalEnergyExport_MWh = EC.v_totalEnergyExport_MWh;

area.v_totalEnergyProduced_MWh = EC.v_totalEnergyProduced_MWh;
area.v_totalEnergyConsumed_MWh = EC.v_totalEnergyConsumed_MWh;
area.v_totalEnergySelfConsumed_MWh = EC.v_totalEnergySelfConsumed_MWh;

area.v_totalElectricityProduced_MWh = EC.v_totalElectricityProduced_MWh;
area.v_totalElectricityConsumed_MWh = EC.v_totalElectricityConsumed_MWh;
area.v_totalElectricitySelfConsumed_MWh = EC.v_totalElectricitySelfConsumed_MWh;


// Summer/winter
for (OL_EnergyCarriers energyCarrier : energyModel.v_activeEnergyCarriers) {
	area.fm_summerWeekImports_MWh.put( energyCarrier, EC.fm_summerWeekImports_MWh.get(energyCarrier));
	area.fm_summerWeekExports_MWh.put( energyCarrier, EC.fm_summerWeekExports_MWh.get(energyCarrier));
	area.fm_winterWeekImports_MWh.put( energyCarrier, EC.fm_winterWeekImports_MWh.get(energyCarrier));
	area.fm_winterWeekExports_MWh.put( energyCarrier, EC.fm_winterWeekExports_MWh.get(energyCarrier));
}

area.v_summerWeekEnergyImport_MWh = EC.v_summerWeekEnergyImport_MWh;
area.v_summerWeekEnergyExport_MWh = EC.v_summerWeekEnergyExport_MWh;

area.v_summerWeekEnergyProduced_MWh = EC.v_summerWeekEnergyProduced_MWh;
area.v_summerWeekEnergyConsumed_MWh = EC.v_summerWeekEnergyConsumed_MWh;
area.v_summerWeekEnergySelfConsumed_MWh = EC.v_summerWeekEnergySelfConsumed_MWh;

area.v_summerWeekElectricityProduced_MWh = EC.v_summerWeekElectricityProduced_MWh;
area.v_summerWeekElectricityConsumed_MWh = EC.v_summerWeekElectricityConsumed_MWh;
area.v_summerWeekElectricitySelfConsumed_MWh = EC.v_summerWeekElectricitySelfConsumed_MWh;

area.v_winterWeekEnergyImport_MWh = EC.v_winterWeekEnergyImport_MWh;
area.v_winterWeekEnergyExport_MWh = EC.v_winterWeekEnergyExport_MWh;

area.v_winterWeekEnergyProduced_MWh = EC.v_winterWeekEnergyProduced_MWh;
area.v_winterWeekEnergyConsumed_MWh = EC.v_winterWeekEnergyConsumed_MWh;
area.v_winterWeekEnergySelfConsumed_MWh = EC.v_winterWeekEnergySelfConsumed_MWh;

area.v_winterWeekElectricityProduced_MWh = EC.v_winterWeekElectricityProduced_MWh;
area.v_winterWeekElectricityConsumed_MWh = EC.v_winterWeekElectricityConsumed_MWh;
area.v_winterWeekElectricitySelfConsumed_MWh = EC.v_winterWeekElectricitySelfConsumed_MWh;

// Day/night
for (OL_EnergyCarriers energyCarrier : energyModel.v_activeEnergyCarriers) {
	area.fm_daytimeImports_MWh.put( energyCarrier, EC.fm_daytimeImports_MWh.get(energyCarrier));
	area.fm_daytimeExports_MWh.put( energyCarrier, EC.fm_daytimeExports_MWh.get(energyCarrier));
	area.fm_nighttimeImports_MWh.put( energyCarrier, EC.fm_nighttimeImports_MWh.get(energyCarrier));
	area.fm_nighttimeExports_MWh.put( energyCarrier, EC.fm_nighttimeExports_MWh.get(energyCarrier));
}

area.v_daytimeEnergyImport_MWh = EC.v_daytimeEnergyImport_MWh;
area.v_daytimeEnergyExport_MWh = EC.v_daytimeEnergyExport_MWh;

area.v_daytimeEnergyProduced_MWh = EC.v_daytimeEnergyProduced_MWh;
area.v_daytimeEnergyConsumed_MWh = EC.v_daytimeEnergyConsumed_MWh;
area.v_daytimeEnergySelfConsumed_MWh = EC.v_daytimeEnergySelfConsumed_MWh;

area.v_daytimeElectricityProduced_MWh = EC.v_daytimeElectricityProduced_MWh;
area.v_daytimeElectricityConsumed_MWh = EC.v_daytimeElectricityConsumed_MWh;
area.v_daytimeElectricitySelfConsumed_MWh = EC.v_daytimeElectricitySelfConsumed_MWh;


area.v_nighttimeEnergyImport_MWh = EC.v_nighttimeEnergyImport_MWh;
area.v_nighttimeEnergyExport_MWh = EC.v_nighttimeEnergyExport_MWh;

area.v_nighttimeEnergyProduced_MWh = EC.v_nighttimeEnergyProduced_MWh;
area.v_nighttimeEnergyConsumed_MWh = EC.v_nighttimeEnergyConsumed_MWh;
area.v_nighttimeEnergySelfConsumed_MWh = EC.v_nighttimeEnergySelfConsumed_MWh;

area.v_nighttimeElectricityProduced_MWh = EC.v_nighttimeElectricityProduced_MWh;
area.v_nighttimeElectricityConsumed_MWh = EC.v_nighttimeElectricityConsumed_MWh;
area.v_nighttimeElectricitySelfConsumed_MWh = EC.v_nighttimeElectricitySelfConsumed_MWh;

// Week/weekend
for (OL_EnergyCarriers energyCarrier : energyModel.v_activeEnergyCarriers) {
	area.fm_weekdayImports_MWh.put( energyCarrier, EC.fm_weekdayImports_MWh.get(energyCarrier));
	area.fm_weekdayExports_MWh.put( energyCarrier, EC.fm_weekdayExports_MWh.get(energyCarrier));
	area.fm_weekendImports_MWh.put( energyCarrier, EC.fm_weekendImports_MWh.get(energyCarrier));
	area.fm_weekendExports_MWh.put( energyCarrier, EC.fm_weekendExports_MWh.get(energyCarrier));
}

area.v_weekdayEnergyImport_MWh = EC.v_weekdayEnergyImport_MWh;
area.v_weekdayEnergyExport_MWh = EC.v_weekdayEnergyExport_MWh;

area.v_weekdayEnergyProduced_MWh = EC.v_weekdayEnergyProduced_MWh;
area.v_weekdayEnergyConsumed_MWh = EC.v_weekdayEnergyConsumed_MWh;
area.v_weekdayEnergySelfConsumed_MWh = EC.v_weekdayEnergySelfConsumed_MWh;

area.v_weekdayElectricityProduced_MWh = EC.v_weekdayElectricityProduced_MWh;
area.v_weekdayElectricityConsumed_MWh = EC.v_weekdayElectricityConsumed_MWh;
area.v_weekdayElectricitySelfConsumed_MWh = EC.v_weekdayElectricitySelfConsumed_MWh;


area.v_weekendEnergyImport_MWh = EC.v_weekendEnergyImport_MWh;
area.v_weekendEnergyExport_MWh = EC.v_weekendEnergyExport_MWh;

area.v_weekendEnergyProduced_MWh = EC.v_weekendEnergyProduced_MWh;
area.v_weekendEnergyConsumed_MWh = EC.v_weekendEnergyConsumed_MWh;
area.v_weekendEnergySelfConsumed_MWh = EC.v_weekendEnergySelfConsumed_MWh;

area.v_weekendElectricityProduced_MWh = EC.v_weekendElectricityProduced_MWh;
area.v_weekendElectricityConsumed_MWh = EC.v_weekendElectricityConsumed_MWh;
area.v_weekendElectricitySelfConsumed_MWh = EC.v_weekendElectricitySelfConsumed_MWh;

/*ALCODEEND*/}

double f_resetSettings()
{/*ALCODESTART::1709718252272*/
b_resultsUpToDate = false;
gr_simulateYearScreenSmall.setVisible(true);

f_updateActiveAssetBooleans();
runSimulation();

/*ALCODEEND*/}

double f_updateUIresultsGridNode(AreaCollection area,GridNode GN)
{/*ALCODESTART::1712584842532*/
// Can't use pointer for (immutable) primitives in Java, so need to manually update results after a year-sim!!
area.v_gridCapacityDelivery_kW = GN.p_capacity_kW;
area.v_gridCapacityFeedIn_kW = GN.p_capacity_kW;
area.b_isRealDeliveryCapacityAvailable = GN.p_realCapacityAvailable;
area.b_isRealFeedinCapacityAvailable = GN.p_realCapacityAvailable;


// Datasets for profile plot
area.v_dataElectricityBaseloadConsumptionLiveWeek_kW = GN.data_liveLoad_kW;
area.v_dataElectricityBaseloadConsumptionYear_kW = GN.data_totalLoad_kW;
area.v_dataElectricityBaseloadConsumptionSummerWeek_kW = GN.data_summerWeekLoad_kW;
area.v_dataElectricityBaseloadConsumptionWinterWeek_kW = GN.data_winterWeekLoad_kW;


area.v_dataElectricityDeliveryCapacityLiveWeek_kW = GN.data_liveCapacityDemand_kW;
area.v_dataElectricityFeedInCapacityLiveWeek_kW = GN.data_liveCapacitySupply_kW;
area.data_gridCapacityDeliveryYear_kW.add(0, GN.p_capacity_kW);
area.data_gridCapacityDeliveryYear_kW.add(8760, GN.p_capacity_kW);
area.data_gridCapacityFeedInYear_kW.add(0, -GN.p_capacity_kW);
area.data_gridCapacityFeedInYear_kW.add(8760, -GN.p_capacity_kW);
area.data_gridCapacityDeliverySummerWeek_kW.add(energyModel.p_startHourSummerWeek, GN.p_capacity_kW);
area.data_gridCapacityDeliverySummerWeek_kW.add(energyModel.p_startHourSummerWeek+24*7, GN.p_capacity_kW);
area.data_gridCapacityFeedInSummerWeek_kW.add(energyModel.p_startHourSummerWeek, -GN.p_capacity_kW);
area.data_gridCapacityFeedInSummerWeek_kW.add(energyModel.p_startHourSummerWeek+24*7, -GN.p_capacity_kW);
area.data_gridCapacityDeliveryWinterWeek_kW.add(energyModel.p_startHourWinterWeek, GN.p_capacity_kW);
area.data_gridCapacityDeliveryWinterWeek_kW.add(energyModel.p_startHourWinterWeek+24*7, GN.p_capacity_kW);
area.data_gridCapacityFeedInWinterWeek_kW.add(energyModel.p_startHourWinterWeek, -GN.p_capacity_kW);
area.data_gridCapacityFeedInWinterWeek_kW.add(energyModel.p_startHourWinterWeek+24*7, -GN.p_capacity_kW);

// Data for Opwek & Verbruik
// Year
area.fm_totalImports_MWh.put(GN.p_energyCarrier, GN.v_totalImport_MWh);
area.fm_totalExports_MWh.put(GN.p_energyCarrier, GN.v_totalExport_MWh);
area.v_annualExcessImport_MWh = GN.v_annualExcessImport_MWh;
area.v_annualExcessExport_MWh = GN.v_annualExcessExport_MWh;

// Summer / Winter
area.fm_summerWeekImports_MWh.put(GN.p_energyCarrier, GN.v_summerWeekImport_MWh);
area.fm_summerWeekExports_MWh.put(GN.p_energyCarrier, GN.v_summerWeekExport_MWh);
area.v_summerWeekExcessImport_MWh = GN.v_summerWeekExcessImport_MWh;
area.v_summerWeekExcessExport_MWh = GN.v_summerWeekExcessExport_MWh;

area.fm_winterWeekImports_MWh.put(GN.p_energyCarrier, GN.v_winterWeekImport_MWh);
area.fm_winterWeekExports_MWh.put(GN.p_energyCarrier, GN.v_winterWeekExport_MWh);
area.v_winterWeekExcessImport_MWh = GN.v_winterWeekExcessImport_MWh;
area.v_winterWeekExcessExport_MWh = GN.v_winterWeekExcessExport_MWh;

// Day / Night
area.fm_daytimeImports_MWh.put(GN.p_energyCarrier, GN.v_daytimeImport_MWh);
area.fm_daytimeExports_MWh.put(GN.p_energyCarrier, GN.v_daytimeExport_MWh);
area.v_daytimeExcessImport_MWh = GN.v_daytimeExcessImport_MWh;
area.v_daytimeExcessExport_MWh = GN.v_daytimeExcessExport_MWh;

area.fm_nighttimeImports_MWh.put(GN.p_energyCarrier, GN.v_nighttimeImport_MWh);
area.fm_nighttimeExports_MWh.put(GN.p_energyCarrier, GN.v_nighttimeExport_MWh);
area.v_nighttimeExcessImport_MWh = GN.v_nighttimeExcessImport_MWh;
area.v_nighttimeExcessExport_MWh = GN.v_nighttimeExcessExport_MWh;

// Weekday / Weekend
area.fm_weekdayImports_MWh.put(GN.p_energyCarrier, GN.v_weekdayImport_MWh);
area.fm_weekdayExports_MWh.put(GN.p_energyCarrier, GN.v_weekdayExport_MWh);
area.v_weekdayExcessImport_MWh = GN.v_weekdayExcessImport_MWh;
area.v_weekdayExcessExport_MWh = GN.v_weekdayExcessExport_MWh;

area.fm_weekendImports_MWh.put(GN.p_energyCarrier, GN.v_weekendImport_MWh);
area.fm_weekendExports_MWh.put(GN.p_energyCarrier, GN.v_weekendExport_MWh);
area.v_weekendExcessImport_MWh = GN.v_weekendExcessImport_MWh;
area.v_weekendExcessExport_MWh = GN.v_weekendExcessExport_MWh;

// Datasets for netloaddurationcurves
area.v_dataNetLoadYear_kW = GN.acc_annualElectricityBalance_kW.getTimeSeries();

// Year
area.v_dataNetbelastingDuurkrommeYear_kW = GN.f_getDuurkromme();;
area.v_dataNetbelastingDuurkrommeYearVorige_kW = GN.data_netbelastingDuurkrommeVorige_kW;

// Summer / Winter
area.v_dataNetbelastingDuurkrommeSummer_kW = GN.data_summerWeekNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeWinter_kW = GN.data_winterWeekNetbelastingDuurkromme_kW;
// Day / Night
area.v_dataNetbelastingDuurkrommeDaytime_kW = GN.data_daytimeNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeNighttime_kW = GN.data_nighttimeNetbelastingDuurkromme_kW;

// Weekday / Weekend
area.v_dataNetbelastingDuurkrommeWeekday_kW = GN.data_weekdayNetbelastingDuurkromme_kW;
area.v_dataNetbelastingDuurkrommeWeekend_kW = GN.data_weekendNetbelastingDuurkromme_kW;
/*ALCODEEND*/}

double f_setGridNodeCongestionColor(GridNode gn)
{/*ALCODESTART::1714043663127*/
if (gn!=null){
	double maxLoad_fr = abs(gn.v_currentLoad_kW)/gn.p_capacity_kW;
	if (maxLoad_fr > 1) {
		gn.gisRegion.setFillColor(v_gridNodeColorCongested);
		gn.gisRegion.setLineColor(v_gridLineColorCongested);
	} else if (maxLoad_fr > 0.7) {
		gn.gisRegion.setFillColor(v_gridNodeColorStrained);
		gn.gisRegion.setLineColor(v_gridNodeLineColorStrained);
	} else {
		gn.gisRegion.setFillColor(v_MVLVNodeColor);
		gn.gisRegion.setLineColor(v_MVLVLineColor);
	}
	
	if( gn == v_clickedGridNode && gn != v_previousClickedGridNode){ // dit zorgt ervoor dat de kleuringfunctie correct werkt in zowel live stand als pauze stand
		gn.gisRegion.setFillColor( v_selectionColor );
		gn.gisRegion.setLineColor( orange );
	}
}
/*ALCODEEND*/}

double f_initialPVSystemsOrder()
{/*ALCODESTART::1714130288661*/
// First we make a copy of all the Uitility GridConnections
List<GridConnection> GCs = new ArrayList<>(energyModel.f_getGridConnections());
List<GridConnection> GCs_detailedCompanies = new ArrayList<>(energyModel.f_getGridConnections());
if( p_selectedProjectType == OL_ProjectType.RESIDENTIAL){
	GCs = GCs.stream().filter(gc -> gc instanceof GCHouse).collect(Collectors.toList());
}
else {
	GCs_detailedCompanies = GCs.stream().filter(gc -> gc instanceof GCUtility && gc.p_owner.p_detailedCompany).collect(Collectors.toList());
	GCs = GCs.stream().filter(gc -> gc instanceof GCUtility && !gc.p_owner.p_detailedCompany).collect(Collectors.toList());
}
// Find all the GCs with PV at the start of the simulation
ArrayList<GridConnection> GCsWithPV = GCs.stream().filter(gc -> gc.v_hasPV).collect(Collectors.toCollection(ArrayList::new));
ArrayList<GridConnection> otherGCs = GCs.stream().filter(gc -> !gc.v_hasPV).collect(Collectors.toCollection(ArrayList::new));
//Collections.shuffle(otherGCs);
ArrayList<GridConnection> detailedCompanyGCsWithPV = GCs_detailedCompanies.stream().filter(gc -> gc.v_hasPV).collect(Collectors.toCollection(ArrayList::new));
ArrayList<GridConnection> datailedCompanyGCsnoPV = GCs_detailedCompanies.stream().filter(gc -> !gc.v_hasPV).collect(Collectors.toCollection(ArrayList::new));

// We make sure that the GCs with PV at the start of the simulation are the last in the list

//traceln("amount of GCs with PV at start: " + GCsWithPV.size());
//traceln("amount of other GCs at start: " + otherGCs.size());

if(c_companyUIs.size() == 0){
	otherGCs.addAll(GCsWithPV);
}

c_orderedPVSystems = otherGCs;
c_orderedPVSystems.addAll(datailedCompanyGCsnoPV);
/*ALCODEEND*/}

double f_initialElectricVehiclesOrder()
{/*ALCODESTART::1714130342440*/
// First we make a copy of all the vehicle energy assets
List<J_EA> EAs = new ArrayList<>(findAll(energyModel.f_getEnergyAssets(), ea -> !(ea.getParentAgent() instanceof GCPublicCharger)));
EAs = EAs.stream().filter(ea -> ea instanceof J_EAVehicle).collect(Collectors.toList());
// Find all the EVs at the start of the simulation
ArrayList<J_EA> otherEAs = EAs.stream().filter(ea -> !(ea instanceof J_EAEV)).collect(Collectors.toCollection(ArrayList::new));
// We make sure that the EVs at the start of the simulation are the last in the list

//traceln("amount of EVs at start: " + EAEVs.size());
//traceln("amount of other EAs at start: " + otherEAs.size());

if(c_companyUIs.size() == 0){ // Dont add the ev to the pool if there are companyUIs
	ArrayList<J_EA> EAEVs = EAs.stream().filter(ea -> (ea instanceof J_EAEV)).collect(Collectors.toCollection(ArrayList::new));
	otherEAs.addAll(EAEVs);
}

c_orderedVehicles = otherEAs;
/*ALCODEEND*/}

double f_initialHeatingSystemsOrder()
{/*ALCODESTART::1714131269202*/
// First we make a copy of all the GridConnections
List<GridConnection> companyList = new ArrayList<>(energyModel.f_getGridConnections());
companyList = companyList.stream().filter(gc -> gc instanceof GCUtility && gc.p_primaryHeatingAsset != null).collect(Collectors.toList());

// Find all the GCs with Heatpumps at the start of the simulation
ArrayList<GridConnection> GCsWithHP = companyList.stream().filter(gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump).collect(Collectors.toCollection(ArrayList::new));
ArrayList<GridConnection> otherGCs = companyList.stream().filter(gc -> !(gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump)).collect(Collectors.toCollection(ArrayList::new));
// We make sure that the GCs with Heatpumps at the start of the simulation are the last in the list (if there is no companyUI)
if(c_companyUIs.size() == 0){
	otherGCs.addAll(GCsWithHP);
}
c_orderedHeatingSystemsCompanies = otherGCs;


// Doe same for houses
List<GridConnection> houseList = new ArrayList<>(energyModel.f_getGridConnections());
houseList = houseList.stream().filter(gc -> gc instanceof GCHouse).collect(Collectors.toList());
ArrayList<GridConnection> housesWithHP = houseList.stream().filter(gc -> gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump).collect(Collectors.toCollection(ArrayList::new));
ArrayList<GridConnection> otherHouses = houseList.stream().filter(gc -> !(gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump)).collect(Collectors.toCollection(ArrayList::new));
otherHouses.addAll(housesWithHP);
c_orderedHeatingSystemsHouses = otherHouses;
/*ALCODEEND*/}

double f_initalAssetOrdering()
{/*ALCODESTART::1714135623471*/
f_initialElectricVehiclesOrder();
f_initialPVSystemsOrder();
f_initialHeatingSystemsOrder();
f_projectSpecificOrderedCollectionAdjustments();


/*ALCODEEND*/}

double f_setColorsBasedOnConsumpion(GIS_Object gis_area)
{/*ALCODESTART::1715116336665*/
if(gis_area.c_containedGridConnections.size() > 0){

	double yearlyEnergyConsumption = sum( gis_area.c_containedGridConnections, x -> x.v_totalElectricityConsumed_MWh);
	
	if ( yearlyEnergyConsumption < 10){ gis_area.f_style( rect_tinyCosumption.getFillColor(), null, null, null);}
	else if ( yearlyEnergyConsumption < 50){ gis_area.f_style( rect_smallCosumption.getFillColor(), null, null, null);}
	else if ( yearlyEnergyConsumption < 150){ gis_area.f_style( rect_mediumCosumption.getFillColor(), null, null, null);}
	else if ( yearlyEnergyConsumption < 500){ gis_area.f_style( rect_largeCosumption.getFillColor(), null, null, null);}
	else if ( yearlyEnergyConsumption > 500){ gis_area.f_style( rect_hugeCosumption.getFillColor(), null, null, null);}
}
/*ALCODEEND*/}

double f_setColorsBasedOnProduction(GIS_Object gis_area)
{/*ALCODESTART::1715118739710*/
/*
for (GIS_Building building : energyModel.pop_GIS_Buildings){
	double electricityDeliveredToGrid_kWh = sum( building.c_gridConnectionsInBuilding, x -> x.v_electricityDeliveredToGrid_kWh);
	if ( electricityDeliveredToGrid_kWh < 10){ building.f_setFillColor( v_10mwhColor);}
	else if ( electricityDeliveredToGrid_kWh < 50){ building.f_setFillColor( v_100mwhColor);}
	else if ( electricityDeliveredToGrid_kWh > 50){ building.f_setFillColor( lime);}
}
*/

if (gis_area.c_containedGridConnections.size() > 0) {
	if (gis_area.c_containedGridConnections.get(0).v_hasPV) {
		if (gis_area.c_containedGridConnections.get(0).c_productionAssets.get(0).getCapacityElectric_kW() < 100){
			gis_area.f_style(rect_smallProduction.getFillColor(), null, null, null);
		}
		else {
			gis_area.f_style(rect_largeProduction.getFillColor(), null, null, null);
		}
		return;
	}
	gis_area.f_style(rect_noProduction.getFillColor(), null, null, null);
}



/*ALCODEEND*/}

double f_projectSpecificSettings()
{/*ALCODESTART::1715157302225*/
//Should be overridden in child interface!!!
traceln("Forgot to override project specific settings!!");
/*ALCODEEND*/}

double f_UIStartup()
{/*ALCODESTART::1715859145993*/
// UI elements

// We call this function to set the parameters so there is no nullpointer errors, but its not loading the correct values yet FIX Later to remove this call.
f_initalAssetOrdering();

//Connect the results UI
f_connectResultsUI();

//Initialize the UI elements (positioning, etc.)
f_setUITabs();

//Project specific settings
f_projectSpecificSettings();

// Create the Private UI for companies
f_createAdditionalUIs();
button_goToUI.setVisible(false);

// Now that the sliders are set we can load the correct starting values
f_initalAssetOrdering();

//Create and set the grid topology colors (Netvlakken)
f_setGridTopologyColors();

//Initialize the sliders of the main UI
f_setSliderPresets();

//Disable cable button if no cables have been loaded in
if(c_LVCables.size() == 0 && c_MVCables.size() == 0){
	checkbox_cabels.setVisible(false);
}

//Disable summary button if summary is not selected
if(settings.showKPISummary() == null || !settings.showKPISummary()){
	uI_Results.getCheckbox_KPISummary().setVisible(false);
}
/*ALCODEEND*/}

GISRegion f_createGISObject(double[] gisTokens)
{/*ALCODESTART::1715868403475*/
GISRegion gisregion = new GISRegion(map, gisTokens);
return gisregion;
/*ALCODEEND*/}

double f_setStartView()
{/*ALCODESTART::1715869498509*/
map.setCenterLatitude(map_centre_latitude);
map.setCenterLongitude(map_centre_longitude);

if(map_scale != null){
	map.setMapScale(map_scale);
}

va_Interface.navigateTo();
/*ALCODEEND*/}

double f_enableTraceln(PrintStream originalPrintStream)
{/*ALCODESTART::1716419446045*/
System.setOut(originalPrintStream);
/*ALCODEEND*/}

PrintStream f_disableTraceln()
{/*ALCODESTART::1716419448047*/
PrintStream originalPrintStream = System.out;

System.setOut(new PrintStream(new OutputStream() {
        public void write(int b) {
        }
    }));
return originalPrintStream;
/*ALCODEEND*/}

double f_setColorsBasedOnConsumpionProfileHouseholds(GIS_Object gis_area)
{/*ALCODESTART::1718263685462*/
double yearlyEnergyConsumption = 0;
for( GridConnection gc : gis_area.c_containedGridConnections){
	for ( J_EAConsumption consumptionAsset : gc.c_consumptionAssets){
		if( consumptionAsset.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND ){
			yearlyEnergyConsumption += consumptionAsset.yearlyDemand_kWh;
		}
	}
}

if ( yearlyEnergyConsumption == 0) { gis_area.f_style( v_unknownConsumptionColor, null, null, null );}
else if ( yearlyEnergyConsumption < 1500){ gis_area.f_style( rect_householdTinyCosumption.getFillColor(), null, null, null);}
else if ( yearlyEnergyConsumption < 2500){ gis_area.f_style( rect_householdSmallCosumption.getFillColor(), null, null, null);}
else if ( yearlyEnergyConsumption < 4000){ gis_area.f_style( rect_householdMediumCosumption.getFillColor(), null, null, null);}
else if ( yearlyEnergyConsumption < 6000){ gis_area.f_style( rect_householdLargeCosumption.getFillColor(), null, null, null);}
else if ( yearlyEnergyConsumption > 6000){ gis_area.f_style( rect_householdHugeCosumption.getFillColor(), null, null, null);}
	

/*ALCODEEND*/}

double f_setColorsBasedOnProductionHouseholds(GIS_Object gis_area)
{/*ALCODESTART::1718265697364*/
if (gis_area.c_containedGridConnections.size() > 0) {
	if (gis_area.c_containedGridConnections.get(0).v_hasPV) {
		if (gis_area.c_containedGridConnections.get(0).c_productionAssets.get(0).getCapacityElectric_kW() < 5){
			gis_area.f_style( rect_householdSmallProduction.getFillColor(), null, null, null );
		}
		else {
			gis_area.f_style( rect_householdLargeProduction.getFillColor(), null, null, null );
		}
		return;
	}
}
gis_area.f_style( rect_householdNoProduction.getFillColor(), null, null, null );


/*ALCODEEND*/}

double f_updateMainInterfaceSliders()
{/*ALCODESTART::1718288402102*/
// ATTENTION: If you have custom tabs it may be neccesary to override this function and add updates to your custom sliders!

if(c_companyUIs.size()>0){//Update ghost vehicles and heating systems present if there are companyUIs
	uI_Tabs.pop_tabHeating.get(0).f_calculateNumberOfGhostHeatingSystems();
	uI_Tabs.pop_tabMobility.get(0).f_calculateNumberOfGhostVehicles();
}



// PV SYSTEMS:
double PVsystems = count(energyModel.UtilityConnections, x->x.v_hasPV == true && x.v_isActive);		
int PV_pct = roundToInt(100 * PVsystems / count(energyModel.UtilityConnections, x->x.v_isActive));
uI_Tabs.pop_tabElectricity.get(0).getSliderRooftopPVCompanies_pct().setValue(PV_pct, false);

// GAS_BURNER / HEATING SYSTEMS: // Still a slight error. GasBurners + HeatPumps != total, because some GC have primary heating asset null
int GasBurners = count(energyModel.UtilityConnections, gc->gc.p_primaryHeatingAsset instanceof J_EAConversionGasBurner && gc.v_isActive);
int GasBurners_pct = roundToInt(100.0 * GasBurners / (count(energyModel.UtilityConnections, x -> x.v_isActive && x.p_primaryHeatingAsset != null) + uI_Tabs.pop_tabHeating.get(0).v_totalNumberOfGhostHeatingSystems_ElectricHeatpumps + uI_Tabs.pop_tabHeating.get(0).v_totalNumberOfGhostHeatingSystems_HybridHeatpumps));

uI_Tabs.pop_tabHeating.get(0).getSliderGasBurnerCompanies_pct().setValue(GasBurners_pct, false);
uI_Tabs.pop_tabHeating.get(0).f_setHeatingSliders( 0, uI_Tabs.pop_tabHeating.get(0).getSliderGasBurnerCompanies_pct(), uI_Tabs.pop_tabHeating.get(0).getSliderElectricHeatPumpCompanies_pct(), null, null );

uI_Tabs.pop_tabHeating.get(0).getSliderHeatDemandSlidersCompaniesGasBurnerCompanies_pct().setValue(GasBurners_pct, false);
uI_Tabs.pop_tabHeating.get(0).f_setHeatingSliders( 0, uI_Tabs.pop_tabHeating.get(0).getSliderHeatDemandSlidersCompaniesGasBurnerCompanies_pct(), uI_Tabs.pop_tabHeating.get(0).getSliderHeatDemandSlidersCompaniesElectricHeatPumpCompanies_pct(), null, null );


// HEAT_PUMP_AIR:
//		int HeatPumps = count(energyModel.UtilityConnections, gc->gc.p_primaryHeatingAsset instanceof J_EAConversionHeatPump);
//		int HeatPumps_pct = roundToInt(100 * HeatPumps / energyModel.UtilityConnections.size());
//		sl_electricHeatPumpCompanies.setValue(HeatPumps_pct, false);
//		f_setHeatingSlidersCompanies(1);
	
// TRUCKS:
int DieselTrucks = 0;
int ElectricTrucks = uI_Tabs.pop_tabMobility.get(0).v_totalNumberOfGhostVehicle_Trucks;
int HydrogenTrucks = 0;
for (GCUtility gc : energyModel.UtilityConnections) {
	if(gc.v_isActive){
		for (J_EAVehicle vehicle : gc.c_vehicleAssets) {
			if (vehicle instanceof J_EADieselVehicle && vehicle.getEAType() == OL_EnergyAssetType.DIESEL_TRUCK) {
				DieselTrucks += 1;
			}
			else if (vehicle instanceof J_EAEV && vehicle.getEAType() == OL_EnergyAssetType.ELECTRIC_TRUCK) {
				ElectricTrucks += 1;
			}
			else if (vehicle instanceof J_EAHydrogenVehicle && vehicle.getEAType() == OL_EnergyAssetType.HYDROGEN_TRUCK) {
				HydrogenTrucks += 1;
			}
		}
	}
}

int totalTrucks = DieselTrucks + ElectricTrucks + HydrogenTrucks;
int DieselTrucks_pct = 100;
int ElectricTrucks_pct = 0;
int HydrogenTrucks_pct = 0;
if (totalTrucks != 0) {
	DieselTrucks_pct = roundToInt(100.0 * DieselTrucks / totalTrucks);
	ElectricTrucks_pct = roundToInt(100.0 * ElectricTrucks / totalTrucks);
	HydrogenTrucks_pct = roundToInt(100.0 * HydrogenTrucks / totalTrucks);
}
uI_Tabs.pop_tabMobility.get(0).getSliderFossilFuelTrucks_pct().setValue(DieselTrucks_pct, false);
uI_Tabs.pop_tabMobility.get(0).getSliderElectricTrucks_pct().setValue(ElectricTrucks_pct, false);
uI_Tabs.pop_tabMobility.get(0).getSliderHydrogenTrucks_pct().setValue(HydrogenTrucks_pct, false);

// VANS:
int DieselVans = 0;
int ElectricVans = uI_Tabs.pop_tabMobility.get(0).v_totalNumberOfGhostVehicle_Vans;
int HydrogenVans = 0;
for (GCUtility gc : energyModel.UtilityConnections) {
	if(gc.v_isActive){
		for (J_EAVehicle vehicle : gc.c_vehicleAssets) {
			if (vehicle instanceof J_EADieselVehicle && vehicle.getEAType() == OL_EnergyAssetType.DIESEL_VAN) {
				DieselVans += 1;
			}
			else if (vehicle instanceof J_EAEV && vehicle.getEAType() == OL_EnergyAssetType.ELECTRIC_VAN) {
				ElectricVans += 1;
			}
			else if (vehicle instanceof J_EAHydrogenVehicle && vehicle.getEAType() == OL_EnergyAssetType.HYDROGEN_VAN) {
				HydrogenVans += 1;
			}
		}
	}
}

int totalVans = DieselVans + ElectricVans + HydrogenVans;
int DieselVans_pct = 100;
int ElectricVans_pct = 0;
int HydrogenVans_pct = 0;
if (totalVans != 0) {
	DieselVans_pct = roundToInt(100.0 * DieselVans / totalVans);
	ElectricVans_pct = roundToInt(100.0 * ElectricVans / totalVans);
	HydrogenVans_pct = roundToInt(100.0 * HydrogenVans / totalVans);
}
uI_Tabs.pop_tabMobility.get(0).getSliderFossilFuelVans_pct().setValue(DieselVans_pct, false);
uI_Tabs.pop_tabMobility.get(0).getSliderElectricVans_pct().setValue(ElectricVans_pct, false);
//sl_hydrogenVans.setValue(HydrogenVans_pct, false);
		
// DIESEL_VEHICLE:  // Currently only for Company Cars not household Cars / EVs
int DieselCars = 0;
int ElectricCars = uI_Tabs.pop_tabMobility.get(0).v_totalNumberOfGhostVehicle_Cars;
int HydrogenCars = 0;
for (GCUtility gc : energyModel.UtilityConnections) {
	if(gc.v_isActive){
		for (J_EAVehicle vehicle : gc.c_vehicleAssets) {
			if (vehicle instanceof J_EADieselVehicle && vehicle.getEAType() == OL_EnergyAssetType.DIESEL_VEHICLE) {
				DieselCars += 1;
			}
			else if (vehicle instanceof J_EAEV && vehicle.getEAType() == OL_EnergyAssetType.ELECTRIC_VEHICLE) {
				ElectricCars += 1;
			}
			else if (vehicle instanceof J_EAHydrogenVehicle && vehicle.getEAType() == OL_EnergyAssetType.HYDROGEN_VEHICLE) {
				HydrogenCars += 1;
			}
		}
	}
}

int totalCars = DieselCars + ElectricCars + HydrogenCars;
int DieselCars_pct = 100;
int ElectricCars_pct = 0;
int HydrogenCars_pct = 0;
if (totalCars != 0) {
	DieselCars_pct = roundToInt((100.0 * DieselCars) / totalCars);
	ElectricCars_pct = roundToInt((100.0 * ElectricCars) / totalCars);
	HydrogenCars_pct = roundToInt((100.0 * HydrogenCars) / totalCars);
}
uI_Tabs.pop_tabMobility.get(0).getSliderFossilFuelCars_pct().setValue(DieselCars_pct, false);
uI_Tabs.pop_tabMobility.get(0).getSliderElectricCars_pct().setValue(ElectricCars_pct, false);
//sl_hydrogenCars.setValue(HydrogenCars_pct, false);

/*ALCODEEND*/}

double f_selectCharger(GCPublicCharger charger,GIS_Object objectGIS)
{/*ALCODESTART::1718552624959*/
objectGIS.gisRegion.setFillColor( v_selectionColor );
objectGIS.gisRegion.setLineColor( orange );

//set info text
v_clickedObjectText = ""; //charger.p_CPOName + " laadpunt, ";
if (charger.p_address == null || charger.p_address.getAddress() == null) {
	v_clickedObjectAdress = "Onbekend adres";
}
else{
	v_clickedObjectAdress = charger.p_address.getStreetName();
}
v_clickedObjectDetails = "No detaild info of charger available";

//v_clickedGridConnection = charger;
c_selectedGridConnections = new ArrayList<GridConnection>(Arrays.asList(charger));
f_updateUIresultsGridConnection(uI_Results.v_gridConnection, c_selectedGridConnections);

//Set the UI button
f_setUIButton();
/*ALCODEEND*/}

double f_setColorsBasedOnGridTopology_objects(GIS_Object gis_area)
{/*ALCODESTART::1718566260603*/
if (gis_area.c_containedGridConnections.size() > 0) {
	Color c = gis_area.c_containedGridConnections.get(0).l_parentNodeElectric.getConnectedAgent().p_uniqueColor;
	gis_area.f_style(c, black, 1.0, null);
}
/*ALCODEEND*/}

double f_getWeatherInfo()
{/*ALCODESTART::1719830600300*/
v_outsideTemperature = roundToDecimal(energyModel.v_currentAmbientTemperature_degC, 1);
v_solarIrradiance = roundToInt(energyModel.v_currentSolarPowerNormalized_r * 1200);

double windspeed = roundToDecimal(energyModel.v_currentWindPowerNormalized_r, 1);
if (windspeed < 0.2){
	v_windspeed = "Laag";
}
else if (windspeed < 0.6){
	v_windspeed = "Midden";
}
else if (windspeed < 0.8){
	v_windspeed = "Hoog";
}
else {
	v_windspeed = "Zeer hoog";
}
/*ALCODEEND*/}

double f_updateUIResultsData()
{/*ALCODESTART::1720793723819*/
v_selectedGridConnectionIndex = 0;

for (GridConnection gc : c_selectedObjects.get(0).c_containedGridConnections) {
	if (!c_selectedGridConnections.contains(gc)) {
		c_selectedGridConnections.add(gc);
	}
}

f_updateUIresultsGridConnection(uI_Results.v_gridConnection, c_selectedGridConnections);
/*ALCODEEND*/}

double f_updateUIresultsGridConnection(AreaCollection area,ArrayList<GridConnection> gcList)
{/*ALCODESTART::1720799287420*/
//Update to new variables
f_updateVariablesOfGCData(area, gcList);
f_updateActiveAssetBooleansGC(area, gcList);
f_updateLiveDataSets(area, gcList);
f_updateYearlyGCData(area, gcList);
f_updateWeeklyGCData(area, gcList);
f_updateBelastingduurKromme(area, gcList);

/*ALCODEEND*/}

double f_updateYearlyGCData(AreaCollection area,ArrayList<GridConnection> gcList)
{/*ALCODESTART::1720820424632*/
//ArrayList<GridConnection> gcList = c_gcList;
EnumSet<OL_EnergyCarriers> activeEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class);
for (GridConnection gc : gcList) {
	activeEnergyCarriers.addAll(gc.v_activeEnergyCarriers);
}

//reset the datasets (is this required)?
if( area.v_dataElectricityBaseloadConsumptionYear_kW != null ){
	area.v_dataElectricityBaseloadConsumptionYear_kW.reset();
	area.v_dataElectricityForHeatConsumptionYear_kW.reset();
	area.v_dataElectricityForTransportConsumptionYear_kW.reset();
	area.v_dataElectricityForStorageConsumptionYear_kW.reset();
	area.v_dataElectricityForHydrogenConsumptionYear_kW.reset();
	area.v_dataElectricityForCookingConsumptionYear_kW.reset();
	//area.v_dataNaturalGasDemandYear_kW.reset();
	//area.v_dataPetroleumProductsDemandYear_kW.reset();
	area.v_dataElectricityPVProductionYear_kW.reset();
	area.v_dataElectricityWindProductionYear_kW.reset(); 
	area.v_dataElectricityStorageProductionYear_kW.reset();
	area.v_dataElectricityV2GProductionYear_kW.reset();
	//area.v_dataHydrogenSupplyYear_kW.reset();
	area.v_dataElectricityCHPProductionYear_kW.reset();
}
else{
	area.v_dataElectricityBaseloadConsumptionYear_kW = new DataSet(365);
	area.v_dataElectricityForHeatConsumptionYear_kW = new DataSet(365);
	area.v_dataElectricityForTransportConsumptionYear_kW = new DataSet(365);
	area.v_dataElectricityForStorageConsumptionYear_kW = new DataSet(365);
	area.v_dataElectricityForHydrogenConsumptionYear_kW = new DataSet(365);
	area.v_dataElectricityForCookingConsumptionYear_kW = new DataSet(365);
	//area.v_dataNaturalGasDemandYear_kW= new DataSet(365);
	//area.v_dataPetroleumProductsDemandYear_kW= new DataSet(365);
	//area.v_dataElectricityForHydrogenDemandYear_kW= new DataSet(365);
	area.v_dataElectricityPVProductionYear_kW = new DataSet(365);
	area.v_dataElectricityWindProductionYear_kW = new DataSet(365);
	area.v_dataElectricityStorageProductionYear_kW = new DataSet(365);
	area.v_dataElectricityV2GProductionYear_kW = new DataSet(365);
	//area.v_dataHydrogenSupplyYear_kW= new DataSet(365);
	area.v_dataElectricityCHPProductionYear_kW = new DataSet(365);
}

area.dsm_dailyAverageConsumptionDataSets_kW.createEmptyDataSets(activeEnergyCarriers, 365);
area.dsm_dailyAverageProductionDataSets_kW.createEmptyDataSets(activeEnergyCarriers, 365);

for (int i=0; i < gcList.get(0).data_annualBaseloadElectricityDemand_kW.size(); i++){ //we sume over the size of a random dataset (all datasets in this loop should ahve same size)
	
	//Create local variables
	double timeAxisValue = gcList.get(0).data_annualBaseloadElectricityDemand_kW.getX(i); // we get the X value from a random dataset 
	
	J_FlowsMap fm_dailyAverageDemand_kW = new J_FlowsMap();
	J_FlowsMap fm_dailyAverageSupply_kW = new J_FlowsMap();
	
	double electricityBaseloadDemandYear_kW = 0;
	double electricityForHeatDemandYear_kW = 0;
	double electricityForTransportDemandYear_kW = 0;
	double electricityForStorageDemandYear_kW = 0;
	double electricityForElectrolyser_kW = 0;
	double electricityForCookingConsumptionYear_kW = 0;
	//double naturalGasDemandYear_kW = 0;
	//double petroleumProductsDemandYear_kW = 0;
	//double electricityForHydrogenDemandYear_kW = 0;
	double electricityPVSupplyYear_kW = 0;
	double electricityWindSupplyYear_kW = 0;
	double electricityStorageSupplyYear_kW = 0;
	double electricityV2GSupplyYear_kW = 0;
	//double hydrogenSupplyYear_kW = 0;
	double electricityCHPSupplyYear_kW = 0;
	
	//accumulate values over gridcongestion
	for (GridConnection gc : gcList){
		
		for (OL_EnergyCarriers EC : gc.v_activeEnergyCarriers) {
			if (gc.dsm_dailyAverageDemandDataSets_kW.get(EC).getXMin() < i) {
				fm_dailyAverageDemand_kW.addFlow( EC, gc.dsm_dailyAverageDemandDataSets_kW.get(EC).getY(i) );
				fm_dailyAverageSupply_kW.addFlow( EC, gc.dsm_dailyAverageSupplyDataSets_kW.get(EC).getY(i) );
			}
		}
	
		electricityBaseloadDemandYear_kW += gc.data_annualBaseloadElectricityDemand_kW.getY(i);
		electricityForHeatDemandYear_kW += gc.data_annualHeatPumpElectricityDemand_kW.getY(i);
		electricityForTransportDemandYear_kW += gc.data_annualElectricVehicleDemand_kW.getY(i);
		electricityForStorageDemandYear_kW += gc.data_annualBatteriesDemand_kW.getY(i);
		if (gc instanceof GCEnergyConversion) {
			electricityForElectrolyser_kW += ((GCEnergyConversion)gc).data_annualElectrolyserDemand_kW.getY(i);
		}
		electricityForCookingConsumptionYear_kW += gc.data_annualCookingElectricityDemand_kW.getY(i);
		/*
		// These if statements can be removed if the AreaCollection is updated and we can loop over keysets.
		if ( gc.v_dailyAverageDemandDataSets_kW.get(OL_EnergyCarriers.METHANE) != null ) {
			naturalGasDemandYear_kW += gc.v_dailyAverageDemandDataSets_kW.get(OL_EnergyCarriers.METHANE).getY(i);
		}
		if ( gc.v_dailyAverageDemandDataSets_kW.get(OL_EnergyCarriers.DIESEL) != null ) {
			petroleumProductsDemandYear_kW += gc.v_dailyAverageDemandDataSets_kW.get(OL_EnergyCarriers.DIESEL).getY(i);
		}
		if ( gc.v_dailyAverageDemandDataSets_kW.get(OL_EnergyCarriers.HYDROGEN) != null ) {
			electricityForHydrogenDemandYear_kW += gc.v_dailyAverageDemandDataSets_kW.get(OL_EnergyCarriers.HYDROGEN).getY(i);
		}
		*/
		
		electricityPVSupplyYear_kW += gc.data_annualPVGeneration_kW.getY(i);
		electricityWindSupplyYear_kW += gc.data_annualWindGeneration_kW.getY(i);
		electricityStorageSupplyYear_kW += gc.data_annualBatteriesSupply_kW.getY(i);
		electricityV2GSupplyYear_kW += gc.data_annualV2GSupply_kW.getY(i);
		electricityCHPSupplyYear_kW += gc.data_annualCHPElectricitySupply_kW.getY(i);
		/*
		if ( gc.v_dailyAverageSupplyDataSets_kW.get(OL_EnergyCarriers.HYDROGEN) != null ) {
			hydrogenSupplyYear_kW += gc.v_dailyAverageSupplyDataSets_kW.get(OL_EnergyCarriers.HYDROGEN).getY(i);
		}
		*/
	}
	
	//add accumulated values to dataset
	area.v_dataElectricityBaseloadConsumptionYear_kW.add(timeAxisValue, electricityBaseloadDemandYear_kW);
	area.v_dataElectricityForHeatConsumptionYear_kW.add(timeAxisValue, electricityForHeatDemandYear_kW);
	area.v_dataElectricityForTransportConsumptionYear_kW.add(timeAxisValue, electricityForTransportDemandYear_kW);
	area.v_dataElectricityForStorageConsumptionYear_kW.add(timeAxisValue, electricityForStorageDemandYear_kW);
	area.v_dataElectricityForHydrogenConsumptionYear_kW.add(timeAxisValue, electricityForElectrolyser_kW);
	area.v_dataElectricityForCookingConsumptionYear_kW.add(timeAxisValue, electricityForCookingConsumptionYear_kW);
	//area.v_dataNaturalGasDemandYear_kW.add(timeAxisValue, naturalGasDemandYear_kW);
	//area.v_dataPetroleumProductsDemandYear_kW.add(timeAxisValue, petroleumProductsDemandYear_kW);
	//area.v_dataElectricityForHydrogenDemandYear_kW.add(timeAxisValue, electricityForHydrogenDemandYear_kW);
	
	area.v_dataElectricityPVProductionYear_kW.add(timeAxisValue, electricityPVSupplyYear_kW);
	area.v_dataElectricityWindProductionYear_kW.add(timeAxisValue, electricityWindSupplyYear_kW);
	area.v_dataElectricityStorageProductionYear_kW.add(timeAxisValue, electricityStorageSupplyYear_kW);
	area.v_dataElectricityV2GProductionYear_kW.add(timeAxisValue, electricityV2GSupplyYear_kW);
	//area.v_dataHydrogenSupplyYear_kW.add(timeAxisValue, hydrogenSupplyYear_kW);
	area.v_dataElectricityCHPProductionYear_kW.add(timeAxisValue, electricityCHPSupplyYear_kW);
	
	for (OL_EnergyCarriers EC : activeEnergyCarriers) {
		area.dsm_dailyAverageConsumptionDataSets_kW.get(EC).add(timeAxisValue, fm_dailyAverageDemand_kW.get(EC) );
		area.dsm_dailyAverageProductionDataSets_kW.get(EC).add(timeAxisValue, fm_dailyAverageSupply_kW.get(EC) );
	}
}
/*ALCODEEND*/}

double f_updateWeeklyGCData(AreaCollection area,ArrayList<GridConnection> gcList)
{/*ALCODESTART::1720820539394*/
//ArrayList<GridConnection> gcList = c_gcList;
EnumSet<OL_EnergyCarriers> activeEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class);
for (GridConnection gc : gcList) {
	activeEnergyCarriers.addAll(gc.v_activeEnergyCarriers);
}

//reset the datasets (is this required)?
if( area.v_dataElectricityBaseloadConsumptionSummerWeek_kW != null ){
	area.v_dataElectricityBaseloadConsumptionSummerWeek_kW.reset();
	area.v_dataElectricityForHeatConsumptionSummerWeek_kW.reset();
	area.v_dataElectricityForTransportConsumptionSummerWeek_kW.reset();
	area.v_dataElectricityForStorageConsumptionSummerWeek_kW.reset();
	area.v_dataElectricityForCookingConsumptionSummerWeek_kW.reset();
	//area.v_dataNaturalGasDemandSummerWeek_kW.reset();
	//area.v_dataPetroleumProductsDemandSummerWeek_kW.reset();
	area.v_dataElectricityForHydrogenConsumptionSummerWeek_kW.reset();
	area.v_dataElectricityPVProductionSummerWeek_kW.reset();
	area.v_dataElectricityWindProductionSummerWeek_kW.reset(); 
	area.v_dataElectricityStorageProductionSummerWeek_kW.reset();
	area.v_dataElectricityV2GProductionSummerWeek_kW.reset();
	//area.v_dataHydrogenSupplySummerWeek_kW.reset();
	area.v_dataElectricityCHPProductionSummerWeek_kW.reset();
	
	area.v_dataElectricityBaseloadConsumptionWinterWeek_kW.reset();
	area.v_dataElectricityForHeatConsumptionWinterWeek_kW.reset();
	area.v_dataElectricityForTransportConsumptionWinterWeek_kW.reset();
	area.v_dataElectricityForStorageConsumptionWinterWeek_kW.reset();
	area.v_dataElectricityForCookingConsumptionWinterWeek_kW.reset();
	//area.v_dataNaturalGasDemandWinterWeek_kW.reset();
	//area.v_dataPetroleumProductsDemandWinterWeek_kW.reset();
	area.v_dataElectricityForHydrogenConsumptionWinterWeek_kW.reset();
	area.v_dataElectricityPVProductionWinterWeek_kW.reset();
	area.v_dataElectricityWindProductionWinterWeek_kW.reset(); 
	area.v_dataElectricityStorageProductionWinterWeek_kW.reset();
	area.v_dataElectricityV2GProductionWinterWeek_kW.reset();
	//area.v_dataHydrogenSupplyWinterWeek_kW.reset();
	area.v_dataElectricityCHPProductionWinterWeek_kW.reset();
	
	area.v_dataNetLoadSummerWeek_kW.reset();
	area.v_dataNetLoadWinterWeek_kW.reset();
	area.v_dataElectricityDeliveryCapacitySummerWeek_kW.reset();
	area.v_dataElectricityDeliveryCapacityWinterWeek_kW.reset();
	area.v_dataElectricityFeedInCapacitySummerWeek_kW.reset();
	area.v_dataElectricityFeedInCapacityWinterWeek_kW.reset();
	
}
else {
	area.v_dataElectricityBaseloadConsumptionSummerWeek_kW = new DataSet(672);
	area.v_dataElectricityForHeatConsumptionSummerWeek_kW = new DataSet(672);
	area.v_dataElectricityForTransportConsumptionSummerWeek_kW = new DataSet(672);
	area.v_dataElectricityForStorageConsumptionSummerWeek_kW = new DataSet(672);
	//area.v_dataNaturalGasDemandSummerWeek_kW = new DataSet(672);
	//area.v_dataPetroleumProductsDemandSummerWeek_kW = new DataSet(672);
	area.v_dataElectricityForHydrogenConsumptionSummerWeek_kW = new DataSet(672);
	area.v_dataElectricityForCookingConsumptionSummerWeek_kW = new DataSet(672);
	area.v_dataElectricityPVProductionSummerWeek_kW = new DataSet(672);
	area.v_dataElectricityWindProductionSummerWeek_kW = new DataSet(672);
	area.v_dataElectricityStorageProductionSummerWeek_kW = new DataSet(672);
	area.v_dataElectricityV2GProductionSummerWeek_kW = new DataSet(672);
	//area.v_dataHydrogenSupplySummerWeek_kW = new DataSet(672);
	area.v_dataElectricityCHPProductionSummerWeek_kW = new DataSet(672);
	
	area.v_dataElectricityBaseloadConsumptionWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityForHeatConsumptionWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityForTransportConsumptionWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityForStorageConsumptionWinterWeek_kW = new DataSet(672);
	//area.v_dataNaturalGasDemandWinterWeek_kW = new DataSet(672);
	//area.v_dataPetroleumProductsDemandWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityForHydrogenConsumptionWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityForCookingConsumptionWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityPVProductionWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityWindProductionWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityStorageProductionWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityV2GProductionWinterWeek_kW = new DataSet(672);
	//area.v_dataHydrogenSupplyWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityCHPProductionWinterWeek_kW = new DataSet(672);
	
	area.v_dataNetLoadSummerWeek_kW = new DataSet(672);
	area.v_dataNetLoadWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityDeliveryCapacitySummerWeek_kW = new DataSet(672);
	area.v_dataElectricityDeliveryCapacityWinterWeek_kW = new DataSet(672);
	area.v_dataElectricityFeedInCapacitySummerWeek_kW = new DataSet(672);
	area.v_dataElectricityFeedInCapacityWinterWeek_kW = new DataSet(672);
	
	
	area.dsm_summerWeekConsumptionDataSets_kW.createEmptyDataSets(energyModel.v_activeEnergyCarriers, (int) (168 / energyModel.p_timeStep_h));
	area.dsm_summerWeekProductionDataSets_kW.createEmptyDataSets(energyModel.v_activeEnergyCarriers, (int) (168 / energyModel.p_timeStep_h));
	area.dsm_winterWeekConsumptionDataSets_kW.createEmptyDataSets(energyModel.v_activeEnergyCarriers, (int) (168 / energyModel.p_timeStep_h));
	area.dsm_winterWeekProductionDataSets_kW.createEmptyDataSets(energyModel.v_activeEnergyCarriers, (int) (168 / energyModel.p_timeStep_h));
}


for (int i=0; i < gcList.get(0).data_summerWeekBaseloadElectricityDemand_kW.size(); i++){ //we sum over the size of a random dataset (all datasets in this loop should ahve same size)
	
	//Create local variables
	double timeAxisValueSummer = gcList.get(0).data_summerWeekBaseloadElectricityDemand_kW.getX(i); // we get the X value from a random dataset 
	double timeAxisValueWinter = gcList.get(0).data_winterWeekBaseloadElectricityDemand_kW.getX(i); // we get the X value from a random dataset 
	
	J_FlowsMap demandSummerWeek_kW = new J_FlowsMap();
	J_FlowsMap supplySummerWeek_kW = new J_FlowsMap();
	
	J_FlowsMap demandWinterWeek_kW = new J_FlowsMap();
	J_FlowsMap supplyWinterWeek_kW = new J_FlowsMap();
	
	
	double electricityBaseloadDemandSummerWeek_kW = 0;
	double electricityForHeatDemandSummerWeek_kW = 0;
	double electricityForTransportDemandSummerWeek_kW = 0;
	double electricityForStorageDemandSummerWeek_kW = 0;
	//double naturalGasDemandSummerWeek_kW = 0;
	//double petroleumProductsDemandSummerWeek_kW = 0;
	double electricityForHydrogenDemandSummerWeek_kW = 0;
	double electricityForCookingConsumptionSummerWeek_kW = 0;
	double electricityPVSupplySummerWeek_kW = 0;
	double electricityWindSupplySummerWeek_kW = 0;
	double electricityStorageSupplySummerWeek_kW = 0;
	double electricityV2GSupplySummerWeek_kW = 0;
	double electricityCHPSupplySummerWeek_kW = 0;
	//double hydrogenSupplySummerWeek_kW = 0;
	
	double electricityBaseloadDemandWinterWeek_kW = 0;
	double electricityForHeatDemandWinterWeek_kW = 0;
	double electricityForTransportDemandWinterWeek_kW = 0;
	double electricityForStorageDemandWinterWeek_kW = 0;
	//double naturalGasDemandWinterWeek_kW = 0;
	//double petroleumProductsDemandWinterWeek_kW = 0;
	double electricityForHydrogenDemandWinterWeek_kW = 0;
	double electricityForCookingConsumptionWinterWeek_kW = 0;
	double electricityPVSupplyWinterWeek_kW = 0;
	double electricityWindSupplyWinterWeek_kW = 0;
	double electricityStorageSupplyWinterWeek_kW = 0;
	double electricityV2GSupplyWinterWeek_kW = 0;
	//double hydrogenSupplyWinterWeek_kW = 0;
	double electricityCHPSupplyWinterWeek_kW = 0;
	
	double netLoadSummerWeek_kW = 0;
	double netLoadWinterWeek_kW = 0;
	double electricityDemandCapacitySummerWeek_kW = 0;
	double electricityDemandCapacityWinterWeek_kW = 0;
	double electricitySupplyCapacitySummerWeek_kW = 0;
	double electricitySupplyCapacityWinterWeek_kW = 0;
	
	//accumulate values over gridcongestion
	for (GridConnection gc : gcList){
		for (OL_EnergyCarriers EC : gc.v_activeEnergyCarriers) {
			//if (gc.dsm_summerWeekDemandDataSets_kW.get(EC).getXMin() < i) {
				demandSummerWeek_kW.addFlow(EC, gc.dsm_summerWeekDemandDataSets_kW.get(EC).getY(i));
				supplySummerWeek_kW.addFlow(EC, gc.dsm_summerWeekSupplyDataSets_kW.get(EC).getY(i));
				demandWinterWeek_kW.addFlow(EC, gc.dsm_winterWeekDemandDataSets_kW.get(EC).getY(i));
				supplyWinterWeek_kW.addFlow(EC, gc.dsm_winterWeekSupplyDataSets_kW.get(EC).getY(i));
			//}
		}
	
		electricityBaseloadDemandSummerWeek_kW += gc.data_summerWeekBaseloadElectricityDemand_kW.getY(i);
		electricityForHeatDemandSummerWeek_kW += gc.data_summerWeekHeatPumpElectricityDemand_kW.getY(i);
		electricityForTransportDemandSummerWeek_kW += gc.data_summerWeekElectricVehicleDemand_kW.getY(i);
		electricityForStorageDemandSummerWeek_kW += gc.data_summerWeekBatteriesDemand_kW.getY(i);
		electricityForCookingConsumptionSummerWeek_kW += gc.data_summerWeekCookingElectricityDemand_kW.getY(i);
		/*
		if ( gc.v_summerWeekDemandDataSets_kW.get(OL_EnergyCarriers.METHANE) != null ) {
			naturalGasDemandSummerWeek_kW += gc.v_summerWeekDemandDataSets_kW.get(OL_EnergyCarriers.METHANE).getY(i);
		}
		if ( gc.v_summerWeekDemandDataSets_kW.get(OL_EnergyCarriers.DIESEL) != null ) {
			petroleumProductsDemandSummerWeek_kW += gc.v_summerWeekDemandDataSets_kW.get(OL_EnergyCarriers.DIESEL).getY(i);
		}
		if ( gc.v_summerWeekDemandDataSets_kW.get(OL_EnergyCarriers.HYDROGEN) != null ) {
			electricityForHydrogenDemandSummerWeek_kW += gc.v_summerWeekDemandDataSets_kW.get(OL_EnergyCarriers.HYDROGEN).getY(i);
		}*/
		
		electricityPVSupplySummerWeek_kW += gc.data_summerWeekPVGeneration_kW.getY(i);
		electricityWindSupplySummerWeek_kW += gc.data_summerWeekWindGeneration_kW.getY(i);
		electricityStorageSupplySummerWeek_kW += gc.data_summerWeekBatteriesSupply_kW.getY(i);
		electricityV2GSupplySummerWeek_kW += gc.data_summerWeekV2GSupply_kW.getY(i);
		electricityCHPSupplySummerWeek_kW += gc.data_summerWeekCHPElectricityProduction_kW.getY(i);
		
		if (gc instanceof GCEnergyConversion) {
			//if (((GCEnergyConversion)gc).data_summerWeekElectrolyserDemand_kW.getXMin() < i) {
				electricityForHydrogenDemandSummerWeek_kW += ((GCEnergyConversion)gc).data_summerWeekElectrolyserDemand_kW.getY(i);
			//}
		}
		/*
		if ( gc.v_summerWeekSupplyDataSets_kW.get(OL_EnergyCarriers.HYDROGEN) != null ) {
			hydrogenSupplySummerWeek_kW += gc.v_summerWeekSupplyDataSets_kW.get(OL_EnergyCarriers.HYDROGEN).getY(i);
		}*/
		
		electricityBaseloadDemandWinterWeek_kW += gc.data_winterWeekBaseloadElectricityDemand_kW.getY(i);
		electricityForHeatDemandWinterWeek_kW += gc.data_winterWeekHeatPumpElectricityDemand_kW.getY(i);
		electricityForTransportDemandWinterWeek_kW += gc.data_winterWeekElectricVehicleDemand_kW.getY(i);
		electricityForStorageDemandWinterWeek_kW += gc.data_winterWeekBatteriesDemand_kW.getY(i);
		if (gc instanceof GCEnergyConversion) {
			//if (((GCEnergyConversion)gc).data_winterWeekElectrolyserDemand_kW.getXMin() < i) {
				electricityForHydrogenDemandWinterWeek_kW += ((GCEnergyConversion)gc).data_winterWeekElectrolyserDemand_kW.getY(i);
			//}
		}
		electricityForCookingConsumptionWinterWeek_kW += gc.data_winterWeekCookingElectricityDemand_kW.getY(i);
		/*
		if ( gc.v_winterWeekDemandDataSets_kW.get(OL_EnergyCarriers.METHANE) != null ) {
			naturalGasDemandWinterWeek_kW += gc.v_winterWeekDemandDataSets_kW.get(OL_EnergyCarriers.METHANE).getY(i);
		}
		if ( gc.v_winterWeekDemandDataSets_kW.get(OL_EnergyCarriers.DIESEL) != null ) {
			petroleumProductsDemandWinterWeek_kW += gc.v_winterWeekDemandDataSets_kW.get(OL_EnergyCarriers.DIESEL).getY(i);
		}
		if ( gc.v_winterWeekDemandDataSets_kW.get(OL_EnergyCarriers.HYDROGEN) != null ) {
			electricityForHydrogenDemandWinterWeek_kW += gc.v_winterWeekDemandDataSets_kW.get(OL_EnergyCarriers.HYDROGEN).getY(i);
		}*/
		
		electricityPVSupplyWinterWeek_kW += gc.data_winterWeekPVGeneration_kW.getY(i);
		electricityWindSupplyWinterWeek_kW += gc.data_winterWeekWindGeneration_kW.getY(i);
		electricityStorageSupplyWinterWeek_kW += gc.data_winterWeekBatteriesSupply_kW.getY(i);
		electricityV2GSupplyWinterWeek_kW += gc.data_winterWeekV2GSupply_kW.getY(i);
		electricityCHPSupplyWinterWeek_kW += gc.data_winterWeekCHPElectricityProduction_kW.getY(i);
		/*
		if ( gc.v_winterWeekSupplyDataSets_kW.get(OL_EnergyCarriers.HYDROGEN) != null ) {
			hydrogenSupplyWinterWeek_kW += gc.v_winterWeekSupplyDataSets_kW.get(OL_EnergyCarriers.HYDROGEN).getY(i);
		}*/
		
		//netLoadSummerWeek_kW += gc.acc_summerElectricityBalance_kW.getY(i);
		netLoadWinterWeek_kW += gc.am_winterWeekBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getY(i);
		electricityDemandCapacitySummerWeek_kW += gc.acc_summerWeekDeliveryCapacity_kW.getY(i);
		electricityDemandCapacityWinterWeek_kW += gc.acc_winterWeekDeliveryCapacity_kW.getY(i);
		electricitySupplyCapacitySummerWeek_kW -= gc.acc_summerWeekFeedinCapacity_kW.getY(i);
		electricitySupplyCapacityWinterWeek_kW -= gc.acc_winterWeekFeedinCapacity_kW.getY(i);
	}
	
	//add accumulated values to dataset
	area.v_dataElectricityBaseloadConsumptionSummerWeek_kW.add(timeAxisValueSummer, electricityBaseloadDemandSummerWeek_kW);
	area.v_dataElectricityForHeatConsumptionSummerWeek_kW.add(timeAxisValueSummer, electricityForHeatDemandSummerWeek_kW);
	area.v_dataElectricityForTransportConsumptionSummerWeek_kW.add(timeAxisValueSummer, electricityForTransportDemandSummerWeek_kW);
	area.v_dataElectricityForStorageConsumptionSummerWeek_kW.add(timeAxisValueSummer, electricityForStorageDemandSummerWeek_kW);
	//area.v_dataNaturalGasDemandSummerWeek_kW.add(timeAxisValueSummer, naturalGasDemandSummerWeek_kW);
	//area.v_dataPetroleumProductsDemandSummerWeek_kW.add(timeAxisValueSummer, petroleumProductsDemandSummerWeek_kW);
	area.v_dataElectricityForHydrogenConsumptionSummerWeek_kW.add(timeAxisValueSummer, electricityForHydrogenDemandSummerWeek_kW);
	area.v_dataElectricityForCookingConsumptionSummerWeek_kW.add(timeAxisValueSummer, electricityForCookingConsumptionSummerWeek_kW);
	area.v_dataElectricityPVProductionSummerWeek_kW.add(timeAxisValueSummer, electricityPVSupplySummerWeek_kW);
	area.v_dataElectricityWindProductionSummerWeek_kW.add(timeAxisValueSummer, electricityWindSupplySummerWeek_kW);
	area.v_dataElectricityStorageProductionSummerWeek_kW.add(timeAxisValueSummer, electricityStorageSupplySummerWeek_kW);
	area.v_dataElectricityV2GProductionSummerWeek_kW.add(timeAxisValueSummer, electricityV2GSupplySummerWeek_kW);
	area.v_dataElectricityCHPProductionSummerWeek_kW.add(timeAxisValueSummer, electricityCHPSupplySummerWeek_kW);
	
	//area.v_dataHydrogenSupplySummerWeek_kW.add(timeAxisValueSummer, hydrogenSupplySummerWeek_kW);
	
	area.v_dataNetLoadSummerWeek_kW.add(timeAxisValueSummer, netLoadSummerWeek_kW);
	area.v_dataElectricityDeliveryCapacitySummerWeek_kW.add(timeAxisValueSummer, electricityDemandCapacitySummerWeek_kW);
	area.v_dataElectricityFeedInCapacitySummerWeek_kW.add(timeAxisValueSummer, electricitySupplyCapacitySummerWeek_kW);
	
	area.v_dataElectricityBaseloadConsumptionWinterWeek_kW.add(timeAxisValueWinter, electricityBaseloadDemandWinterWeek_kW);
	area.v_dataElectricityForHeatConsumptionWinterWeek_kW.add(timeAxisValueWinter, electricityForHeatDemandWinterWeek_kW);
	area.v_dataElectricityForTransportConsumptionWinterWeek_kW.add(timeAxisValueWinter, electricityForTransportDemandWinterWeek_kW);
	area.v_dataElectricityForStorageConsumptionWinterWeek_kW.add(timeAxisValueWinter, electricityForStorageDemandWinterWeek_kW);
	//area.v_dataNaturalGasDemandWinterWeek_kW.add(timeAxisValueWinter, naturalGasDemandWinterWeek_kW);
	//area.v_dataPetroleumProductsDemandWinterWeek_kW.add(timeAxisValueWinter, petroleumProductsDemandWinterWeek_kW);
	area.v_dataElectricityForHydrogenConsumptionWinterWeek_kW.add(timeAxisValueWinter, electricityForHydrogenDemandWinterWeek_kW);
	area.v_dataElectricityForCookingConsumptionWinterWeek_kW.add(timeAxisValueWinter, electricityForCookingConsumptionWinterWeek_kW);
	area.v_dataElectricityPVProductionWinterWeek_kW.add(timeAxisValueWinter, electricityPVSupplyWinterWeek_kW);
	area.v_dataElectricityWindProductionWinterWeek_kW.add(timeAxisValueWinter, electricityWindSupplyWinterWeek_kW);
	area.v_dataElectricityStorageProductionWinterWeek_kW.add(timeAxisValueWinter, electricityStorageSupplyWinterWeek_kW);
	area.v_dataElectricityV2GProductionWinterWeek_kW.add(timeAxisValueWinter, electricityV2GSupplyWinterWeek_kW);
	//area.v_dataHydrogenSupplyWinterWeek_kW.add(timeAxisValueWinter, hydrogenSupplyWinterWeek_kW);	
	area.v_dataElectricityCHPProductionWinterWeek_kW.add(timeAxisValueWinter, electricityCHPSupplyWinterWeek_kW);
	
	area.v_dataNetLoadWinterWeek_kW.add(timeAxisValueWinter, netLoadWinterWeek_kW);
	area.v_dataElectricityDeliveryCapacityWinterWeek_kW.add(timeAxisValueWinter, electricityDemandCapacityWinterWeek_kW);
	area.v_dataElectricityFeedInCapacityWinterWeek_kW.add(timeAxisValueWinter, electricitySupplyCapacityWinterWeek_kW);
	
	for (OL_EnergyCarriers EC : activeEnergyCarriers) {
		area.dsm_summerWeekConsumptionDataSets_kW.get(EC).add(timeAxisValueSummer, demandSummerWeek_kW.get(EC));
		area.dsm_summerWeekProductionDataSets_kW.get(EC).add(timeAxisValueSummer, supplySummerWeek_kW.get(EC));
		area.dsm_winterWeekConsumptionDataSets_kW.get(EC).add(timeAxisValueWinter, demandWinterWeek_kW.get(EC));
		area.dsm_winterWeekProductionDataSets_kW.get(EC).add(timeAxisValueWinter, supplyWinterWeek_kW.get(EC));
	}
}
/*ALCODEEND*/}

double f_updateBelastingduurKromme(AreaCollection area,ArrayList<GridConnection> gcList)
{/*ALCODESTART::1720820888674*/
//ArrayList<GridConnection> gcList = c_gcList;

if( area.v_dataNetbelastingDuurkrommeYear_kW != null ){

	area.v_dataNetLoadYear_kW = new double[35040];
	area.v_dataNetbelastingDuurkrommeYear_kW.reset();
	//area.v_dataNetbelastingDuurkrommeYearVorige_kW.reset();
	area.v_dataNetbelastingDuurkrommeSummer_kW.reset();
	area.v_dataNetbelastingDuurkrommeWinter_kW.reset();
	area.v_dataNetbelastingDuurkrommeDaytime_kW.reset();
	area.v_dataNetbelastingDuurkrommeNighttime_kW.reset();
	area.v_dataNetbelastingDuurkrommeWeekday_kW.reset();
	area.v_dataNetbelastingDuurkrommeWeekend_kW.reset();

}
else {
	area.v_dataNetLoadYear_kW = new double[roundToInt(365*24/energyModel.p_timeStep_h)];
	area.v_dataNetbelastingDuurkrommeYear_kW = new DataSet(roundToInt(365*24/energyModel.p_timeStep_h));
	//area.v_dataNetbelastingDuurkrommeYearVorige_kW = new DataSet(roundToInt(365*24/energyModel.p_timeStep_h));
	area.v_dataNetbelastingDuurkrommeSummer_kW = new DataSet(roundToInt(7*24/energyModel.p_timeStep_h));
	area.v_dataNetbelastingDuurkrommeWinter_kW = new DataSet(roundToInt(7*24/energyModel.p_timeStep_h));
	area.v_dataNetbelastingDuurkrommeDaytime_kW = new DataSet(roundToInt(365*24/2/energyModel.p_timeStep_h));
	area.v_dataNetbelastingDuurkrommeNighttime_kW = new DataSet(roundToInt(365*24/2/energyModel.p_timeStep_h));
	area.v_dataNetbelastingDuurkrommeWeekday_kW = new DataSet(roundToInt(365*24/7*5/energyModel.p_timeStep_h)+100);
	area.v_dataNetbelastingDuurkrommeWeekend_kW = new DataSet(roundToInt(365*24/7*2/energyModel.p_timeStep_h)+100);
}

// loop over gcs and call f_getduurkromme? Also sum the connection capacities
double totalDeliveryCapacity_kW = 0;
double totalFeedInCapacity_kW = 0;

for (GridConnection gc : gcList) {
	gc.f_getDuurkromme();
	gc.f_nfatoSetConnectionCapacity(true);
	totalDeliveryCapacity_kW += gc.p_contractedDeliveryCapacity_kW;
	totalFeedInCapacity_kW += gc.p_contractedFeedinCapacity_kW;
	gc.f_nfatoSetConnectionCapacity(false);
}

double[] NetbelastingDuurkrommeYear_kW = new double[roundToInt(365*24/energyModel.p_timeStep_h)];
//double[] NetbelastingDuurkrommeYearVorige_kW = new double[roundToInt(365*24/energyModel.p_timeStep_h)];
double[] NetbelastingDuurkrommeSummer_kW = new double[roundToInt(7*24/energyModel.p_timeStep_h)];
double[] NetbelastingDuurkrommeWinter_kW = new double[roundToInt(7*24/energyModel.p_timeStep_h)];
double[] NetbelastingDuurkrommeDaytime_kW = new double[roundToInt(365*24/2/energyModel.p_timeStep_h)];
double[] NetbelastingDuurkrommeNighttime_kW = new double[roundToInt(365*24/2/energyModel.p_timeStep_h)];
double[] NetbelastingDuurkrommeWeekday_kW = new double[roundToInt(365*24/7*5/energyModel.p_timeStep_h)+100];
double[] NetbelastingDuurkrommeWeekend_kW = new double[roundToInt(365*24/7*2/energyModel.p_timeStep_h)+100];

// Year
for (int i = 0; i < roundToInt(365*24/energyModel.p_timeStep_h); i++ ) {
	//double netLoadYear_kW = 0;
	double netbelastingDuurkrommeYear_kW = 0;
	//double netbelastingDuurkrommeYearVorige_kW = 0;
	// loop over gcs and add the values
	for (GridConnection gc : gcList) {	
		netbelastingDuurkrommeYear_kW += gc.data_netbelastingDuurkromme_kW.getY(i);
		//netbelastingDuurkrommeYearVorige_kW += gc.data_netbelastingDuurkrommeVorige_kW.getY(i);
	}
	NetbelastingDuurkrommeYear_kW[i] = -netbelastingDuurkrommeYear_kW;
	//NetbelastingDuurkrommeYearVorige_kW[i] = -netbelastingDuurkrommeYearVorige_kW;
	
}
// Summer / Winter
for (int i = 0; i < roundToInt(7*24/energyModel.p_timeStep_h); i++ ) {
	double netbelastingDuurkrommeSummer_kW = 0;
	double netbelastingDuurkrommeWinter_kW = 0;
	// loop over gcs and add the values
	for (GridConnection gc : gcList) {
		netbelastingDuurkrommeSummer_kW += gc.data_summerWeekNetbelastingDuurkromme_kW.getY(i);
		netbelastingDuurkrommeWinter_kW += gc.data_winterWeekNetbelastingDuurkromme_kW.getY(i);
	}
	NetbelastingDuurkrommeSummer_kW[i] = -netbelastingDuurkrommeSummer_kW;
	NetbelastingDuurkrommeWinter_kW[i] = -netbelastingDuurkrommeWinter_kW;

}
// Day / Night
for (int i = 0; i < roundToInt(365*24/2/energyModel.p_timeStep_h); i++ ) {
	double netbelastingDuurkrommeDaytime_kW = 0;
	double netbelastingDuurkrommeNighttime_kW = 0;
	// loop over gcs and add the values
	for (GridConnection gc : gcList) {
		netbelastingDuurkrommeDaytime_kW += gc.data_daytimeNetbelastingDuurkromme_kW.getY(i);
		netbelastingDuurkrommeNighttime_kW += gc.data_nighttimeNetbelastingDuurkromme_kW.getY(i);
	}
	NetbelastingDuurkrommeDaytime_kW[i] = -netbelastingDuurkrommeDaytime_kW;
	NetbelastingDuurkrommeNighttime_kW[i] = -netbelastingDuurkrommeNighttime_kW;

}
// Weekday
for (int i = 0; i < gcList.get(0).data_weekdayNetbelastingDuurkromme_kW.size(); i++ ) {
	double netbelastingDuurkrommeWeekday_kW = 0;
	// loop over gcs and add the values
	for (GridConnection gc : gcList) {
		netbelastingDuurkrommeWeekday_kW += gc.data_weekdayNetbelastingDuurkromme_kW.getY(i);
	}
	NetbelastingDuurkrommeWeekday_kW[i] = -netbelastingDuurkrommeWeekday_kW;

}
// Weekend
for (int i = 0; i < gcList.get(0).data_weekendNetbelastingDuurkromme_kW.size(); i++ ) {
	double netbelastingDuurkrommeWeekend_kW = 0;
	// loop over gcs and add the values
	for (GridConnection gc : gcList) {
		netbelastingDuurkrommeWeekend_kW += gc.data_weekendNetbelastingDuurkromme_kW.getY(i);
	}
	NetbelastingDuurkrommeWeekend_kW[i] = -netbelastingDuurkrommeWeekend_kW;
}

// Resort all the arrays 

Arrays.sort(NetbelastingDuurkrommeYear_kW);
//Arrays.sort(NetbelastingDuurkrommeYearVorige_kW);
Arrays.sort(NetbelastingDuurkrommeSummer_kW);
Arrays.sort(NetbelastingDuurkrommeWinter_kW);
Arrays.sort(NetbelastingDuurkrommeDaytime_kW);
Arrays.sort(NetbelastingDuurkrommeNighttime_kW);
Arrays.sort(NetbelastingDuurkrommeWeekday_kW);
Arrays.sort(NetbelastingDuurkrommeWeekend_kW);

// Fill the AreaCollection DataSets

int arraySize;
// Year
arraySize = NetbelastingDuurkrommeYear_kW.length;
for(int i=0; i< arraySize; i++) {
	area.v_dataNetbelastingDuurkrommeYear_kW.add(i*energyModel.p_timeStep_h, -NetbelastingDuurkrommeYear_kW[i]);
}
//for(int i=0; i< arraySize; i++) {
//	area.v_dataNetbelastingDuurkrommeYearVorige_kW.add(i*energyModel.p_timeStep_h, -NetbelastingDuurkrommeYearVorige_kW[i]);
//}
// Week
arraySize = NetbelastingDuurkrommeSummer_kW.length;
for(int i=0; i< arraySize; i++) {
	area.v_dataNetbelastingDuurkrommeSummer_kW.add(i*energyModel.p_timeStep_h, -NetbelastingDuurkrommeSummer_kW[i]);
}
for(int i=0; i< arraySize; i++) {
	area.v_dataNetbelastingDuurkrommeWinter_kW.add(i*energyModel.p_timeStep_h, -NetbelastingDuurkrommeWinter_kW[i]);
}
// Day / Night
arraySize = NetbelastingDuurkrommeDaytime_kW.length;
for(int i=0; i< arraySize; i++) {
	area.v_dataNetbelastingDuurkrommeDaytime_kW.add(i*energyModel.p_timeStep_h, -NetbelastingDuurkrommeDaytime_kW[i]);
}
for(int i=0; i< arraySize; i++) {
	area.v_dataNetbelastingDuurkrommeNighttime_kW.add(i*energyModel.p_timeStep_h, -NetbelastingDuurkrommeNighttime_kW[i]);
}
// Weekday
arraySize = NetbelastingDuurkrommeWeekday_kW.length;
for(int i=0; i< arraySize; i++) {
	area.v_dataNetbelastingDuurkrommeWeekday_kW.add(i*energyModel.p_timeStep_h, -NetbelastingDuurkrommeWeekday_kW[i]);
}
// Weekend
arraySize = NetbelastingDuurkrommeWeekend_kW.length;
for(int i=0; i< arraySize; i++) {
	area.v_dataNetbelastingDuurkrommeWeekend_kW.add(i*energyModel.p_timeStep_h, -NetbelastingDuurkrommeWeekend_kW[i]);
}

// Connection Capacity
area.data_gridCapacityDeliveryYear_kW.add(0, totalDeliveryCapacity_kW);
area.data_gridCapacityDeliveryYear_kW.add(8760, totalDeliveryCapacity_kW);
area.data_gridCapacityFeedInYear_kW.add(0, -totalFeedInCapacity_kW);
area.data_gridCapacityFeedInYear_kW.add(8760, -totalFeedInCapacity_kW);
/*ALCODEEND*/}

double f_multiSelect(double clickx,double clicky)
{/*ALCODESTART::1721290570561*/
// TODO: if selected object was a trafo before enabling this deselect it?

//Check if click was on Building
for ( GIS_Building b : energyModel.pop_GIS_Buildings ){
	if( b.gisRegion != null && b.gisRegion.contains(clickx, clicky) ){
		if (b.gisRegion.isVisible()) {
			for (GridConnection GC : b.c_containedGridConnections) {
				if (GC != null) {
					if (c_selectedGridConnections.contains(GC)) {
						c_selectedGridConnections.remove(GC);
						for (GIS_Object a : GC.c_connectedGISObjects) {
							f_styleAreas(a);
						}
					}
					else {
						c_selectedGridConnections.add(GC);
						for (GIS_Object a : GC.c_connectedGISObjects) {
							a.gisRegion.setFillColor(v_selectionColorAddBuildings);
						}
					}
				}
			}
		}
	}
}

if (c_selectedGridConnections.size() > 0) {
	f_updateUIresultsGridConnection(uI_Results.v_gridConnection, c_selectedGridConnections);
	uI_Results.v_selectedObjectType = OL_GISObjectType.BUILDING;
	if (p_selectedProjectType == OL_ProjectType.BUSINESSPARK) {
		f_fillAreaCollectionsOfIndividualGCs();
	}
}
else {
	uI_Results.v_selectedObjectType = OL_GISObjectType.REGION;
}
uI_Results.f_showCorrectChart();
/*ALCODEEND*/}

double f_setGridTopologyColors()
{/*ALCODESTART::1721991420806*/
//Find all MV substations
List<GridNode> MVsubstations = findAll(energyModel.pop_gridNodes, GN -> GN.p_nodeType == OL_GridNodeType.SUBMV);



int i = 0;
//Set all unique grid topology colors for each substation and its children
for (GridNode MVsub : MVsubstations){
	
	//Create a unique color from a spectrum and assign it to the subMV
	MVsub.p_uniqueColor = spectrumColor(i, MVsubstations.size());
	
	//Assign unique color to all underlying grid nodes
	MVsub.f_getLowerLVLConnectedGridNodes().forEach(GN -> GN.p_uniqueColor = MVsub.p_uniqueColor);
	
	//Update spectrum color index
	i++;
}

//Find all MVMV and HVMV distribution stations
List<GridNode> MVMVstations = findAll(energyModel.pop_gridNodes, GN -> GN.p_nodeType == OL_GridNodeType.MVMV);
List<GridNode> HVMVstations = findAll(energyModel.pop_gridNodes, GN -> GN.p_nodeType == OL_GridNodeType.HVMV);

//Set their topology colors (for now black as they are basically top level).
MVMVstations.forEach(GN -> GN.p_uniqueColor = semiTransparent(black));
HVMVstations.forEach(GN -> GN.p_uniqueColor = semiTransparent(black));

/*ALCODEEND*/}

double f_styleSUBMV(GISRegion gisregion)
{/*ALCODESTART::1721991963719*/
gisregion.setVisible(false);

/*ALCODEEND*/}

double f_styleMVMV(GISRegion gisregion)
{/*ALCODESTART::1721992103665*/
gisregion.setFillColor(new Color(253, 223, 134, 206));
gisregion.setLineStyle( LINE_STYLE_SOLID );
gisregion.setLineColor( navy );
gisregion.setLineWidth(2);

/*ALCODEEND*/}

double f_listFunctions()
{/*ALCODESTART::1721049341787*/
if(c_selectedObjects.get(0).c_containedGridConnections.size() > 1){
	HashMap<String, Integer> functionsList = new HashMap<String, Integer>();
	
	for (int i = 0; i < c_selectedObjects.get(0).c_containedGridConnections.size(); i++) {
		//split functies als er meerdere zijn
		String[] splitFunctions = c_selectedObjects.get(0).c_containedGridConnections.get(i).p_purposeBAG.split(",");
		
		for (int j = 0; j < splitFunctions.length; j++) {
			// als de key al bestaat, itereer
			if (functionsList.get(splitFunctions[j]) != null) {
				functionsList.put(splitFunctions[j], functionsList.get(splitFunctions[j]) + 1);
			}
			// zo niet, voeg toe
			else {
				functionsList.put(splitFunctions[j],1);
			}
		}
	}
	
	/*
	for (String i : functionsList.keySet()) {
		traceln(i + ": " + functionsList.get(i));
	}
	*/
}
/*ALCODEEND*/}

double f_createAdditionalUIs()
{/*ALCODESTART::1724857887019*/
//Create the additional dashboards, control panels and private UIs

//Create PrivateUIs
if (p_selectedProjectType == BUSINESSPARK){
	f_createPrivateCompanyUI();
}

//Create Hydrogen UI
//f_createHydrogenUI();


//Create Battery UI
//f_createBatteryUI();
/*ALCODEEND*/}

double f_createHydrogenUI()
{/*ALCODESTART::1724857983890*/
//Create the hydrogen ui
UI_Hydrogen hydrogenUI = add_ui_Hydrogen();

//Fill list of connection owners companies 
hydrogenUI.c_connectionOwners_Hydrogen.addAll(findAll(energyModel.pop_connectionOwners, co -> co.p_connectionOwnerType == OL_ConnectionOwnerType.ELECTROLYSER_OP)); 

//Fill the hydrogen GC collection (For now only searched for Electrolyser_OP, what about (the non existing) Fuelcell_OP 
for(ConnectionOwner COHydrogen : hydrogenUI.c_connectionOwners_Hydrogen){
	hydrogenUI.c_gridConnections_Hydrogen.addAll(findAll(
    COHydrogen.f_getOwnedGridConnections(), gc -> gc instanceof GCEnergyConversion && (
    gc.c_energyAssets.stream().anyMatch(asset -> asset instanceof J_EAConversionElectrolyser) ||
    gc.c_energyAssets.stream().anyMatch(asset -> asset instanceof J_EAConversionFuelCell) ||
    gc.c_energyAssets.stream().anyMatch(asset -> asset instanceof J_EAStorageGas)    )));
}


for (GridConnection GC : hydrogenUI.c_gridConnections_Hydrogen) {

	//Add all GIS objects
	hydrogenUI.c_GISObjects_Hydrogen.addAll(GC.c_connectedGISObjects);
	
	//Add connected gridnodes for each GC
	hydrogenUI.c_connectedGridNodes.add(GC.p_parentNodeElectricID);
	
	//Find all energy assets and add them to the correct collection
	List<J_EAConversion> electrolysers = findAll(GC.c_conversionAssets, asset -> asset instanceof J_EAConversionElectrolyser);
	List<J_EAStorage> storages = findAll(GC.c_storageAssets, asset -> asset instanceof J_EAStorageGas);
	List<J_EAConversion> fuelcells = findAll(GC.c_conversionAssets, asset -> asset instanceof J_EAConversionFuelCell);
	
	electrolysers.forEach(asset -> hydrogenUI.c_hydrogenElectrolysers.add((J_EAConversionElectrolyser)asset));
	storages.forEach(asset -> hydrogenUI.c_hydrogenStorages.add((J_EAStorageGas)asset));
	fuelcells.forEach(asset -> hydrogenUI.c_hydrogenFuelCells.add((J_EAConversionFuelCell)asset));
}


//Totals
hydrogenUI.p_amountOfGC = hydrogenUI.c_gridConnections_Hydrogen.size();
hydrogenUI.p_amountOfGISObjects = hydrogenUI.c_GISObjects_Hydrogen.size();


//Initialize the UI




//Add to the collection of UIs ???



/*ALCODEEND*/}

double f_createBatteryUI()
{/*ALCODESTART::1724858039724*/
//Create the hydrogen ui
UI_Battery batteryUI = add_ui_Battery();

//Fill list of connection owners companies 
batteryUI.c_connectionOwners_Battery.addAll(findAll(energyModel.pop_connectionOwners, co -> co.p_connectionOwnerType == OL_ConnectionOwnerType.BATTERY_OP)); 

//Fill the hydrogen GC collection (For now only searched for Electrolyser_OP, what about (the non existing) Fuelcell_OP 
for(ConnectionOwner COBattery : batteryUI.c_connectionOwners_Battery){
	batteryUI.c_gridConnections_Battery.addAll(findAll(
    COBattery.f_getOwnedGridConnections(), gc -> gc instanceof GCGridBattery &&
    gc.c_storageAssets.stream().anyMatch(asset -> asset instanceof J_EAStorageElectric)    ));
}


for (GridConnection GC : batteryUI.c_gridConnections_Battery) {

	//Add all GIS objects
	batteryUI.c_GISObjects_Battery.addAll(GC.c_connectedGISObjects);
	
	//Add connected gridnodes for each GC
	batteryUI.c_connectedGridNodes.add(GC.p_parentNodeElectricID);
	
	//Find all energy assets and add them to the correct collection
	List<J_EAStorage> batteries = findAll(GC.c_storageAssets, asset -> asset instanceof J_EAStorageElectric);

	batteries.forEach(asset -> batteryUI.c_gridBatteries.add((J_EAStorageElectric)asset));
}


//Totals
batteryUI.p_amountOfGC = batteryUI.c_gridConnections_Battery.size();
batteryUI.p_amountOfGISObjects = batteryUI.c_GISObjects_Battery.size();


//Initialize the UI




//Add to the collection of UIs ???
/*ALCODEEND*/}

double f_setUIButton()
{/*ALCODESTART::1725006890451*/
switch(v_clickedObjectType){

case BUILDING:
	if (c_selectedGridConnections.size() > 1 || !c_selectedGridConnections.get(0).p_owner.b_hasPrivateUI || !c_selectedGridConnections.get(0).v_isActive){
		button_goToUI.setVisible(false);
	}
	else{
		// Index number of the connection owner used to change the button '
		v_connectionOwnerIndexNr = c_selectedGridConnections.get(0).p_owner.p_connectionOwnerIndexNr;
		button_goToUI.setText("Ga naar het Bedrijfsportaal");
		button_goToUI.setVisible(true);
	}
	break;
	
case ELECTROLYSER:
	if(ui_Hydrogen.size() > 0){
		button_goToUI.setText("Ga naar het Waterstof Dashboard");
		button_goToUI.setVisible(true);
	}
	break;
	
case BATTERY:
	if(ui_Battery.size() > 0){
		button_goToUI.setText("Ga naar het Batterijen Dashboard");
		button_goToUI.setVisible(true);
	}
	break;	

case CHARGER:
	button_goToUI.setVisible(false);
	break;
	
default:
	button_goToUI.setVisible(false);
	break;
}

/*ALCODEEND*/}

GISRoute f_createGISLine(double[] GISCoords,String networkType)
{/*ALCODESTART::1725266804325*/

Curve curve = new Curve();
for(int i = 0; i+3 < GISCoords.length; i += 2){
	GISMarkupSegmentLine segment = new GISMarkupSegmentLine(GISCoords[i], GISCoords[i+1], GISCoords[i+2], GISCoords[i+3]);
	curve.addSegment(segment);
}

//Initialize curve and create route
//curve.initialize();
GISRoute gisroute = new GISRoute(map, curve, true);



//FOR NOW STYLING ETC. HERE, I WANT TO MOVE IT TO NETWORKS (BUT NOT DONE YET).
switch(networkType){

case "LVGrid":
	c_LVCables.add(gisroute);
	//Styling
	gisroute.setLineStyle(LINE_STYLE_SOLID);
	gisroute.setLineWidth(0.8);
	gisroute.setLineColor(v_LVGridColor);
	gisroute.setVisible(false);
	break;

case "MVGrid":
	c_MVCables.add(gisroute);
	//Styling
	gisroute.setLineStyle(LINE_STYLE_SOLID);
	gisroute.setLineWidth(1);
	gisroute.setLineColor(v_MVGridColor);
	gisroute.setVisible(false);
	break;
}

return gisroute;

/*ALCODEEND*/}

double f_createGISNetwork(GISRoute[] gisroutes,String networkType)
{/*ALCODESTART::1725274862894*/
//Add route/line to correct network
//GISNetwork network = findFirst(c_GISNetworks, nw -> nw.getName().equals(networkType));

//if(network == null){
	GISNetwork network = new GISNetwork(map, networkType, true, gisroutes);
	c_GISNetworks.add(network);
//}

/*ALCODEEND*/}

double f_updateTotalLiveDataSets()
{/*ALCODESTART::1725439643960*/
for (int j = 0; j < c_selectedGridConnections.size(); j++) {
	AreaCollection AC = uI_Results.c_individualGridConnections.get(j);
	
	/*
	
	AC.v_dataTotalElectricityDemandLiveWeek_kW = new DataSet(672);
	AC.v_dataTotalElectricitySupplyLiveWeek_kW = new DataSet(672);

	int liveWeekSize = AC.v_dataElectricityDeliveryCapacityLiveWeek_kW.size();
	for (int k = 0; k < liveWeekSize; k++) {
		double timeAxisValue = AC.v_dataElectricityDeliveryCapacityLiveWeek_kW.getX(k);
		
		double totalElectricityDemandLiveWeek = 0;
		double totalElectricitySupplyLiveWeek = 0;
		
		totalElectricityDemandLiveWeek += AC.v_dataElectricityBaseloadConsumptionLiveWeek_kW.getY(k);
		totalElectricityDemandLiveWeek += AC.v_dataElectricityForHeatConsumptionLiveWeek_kW.getY(k);
		totalElectricityDemandLiveWeek += AC.v_dataElectricityForTransportConsumptionLiveWeek_kW.getY(k);
		totalElectricityDemandLiveWeek += AC.v_dataElectricityForStorageConsumptionLiveWeek_kW.getY(k);
		totalElectricityDemandLiveWeek += AC.v_dataElectricityForHydrogenConsumptionLiveWeek_kW.getY(k);
		
		totalElectricitySupplyLiveWeek += AC.v_dataWindElectricityProductionLiveWeek_kW.getY(k);
		totalElectricitySupplyLiveWeek += AC.v_dataPVElectricityProductionLiveWeek_kW.getY(k);
		totalElectricitySupplyLiveWeek += AC.v_dataStorageElectricityProductionLiveWeek_kW.getY(k);
		totalElectricitySupplyLiveWeek += AC.v_dataV2GElectricityProductionLiveWeek_kW.getY(k);

		AC.v_dataTotalElectricityDemandLiveWeek_kW.add(timeAxisValue, totalElectricityDemandLiveWeek);
		AC.v_dataTotalElectricitySupplyLiveWeek_kW.add(timeAxisValue, totalElectricitySupplyLiveWeek);
	}
	
	*/
	
}

/*ALCODEEND*/}

double f_addTimeStepTotalLiveDataSets()
{/*ALCODESTART::1725440073691*/
int i = max(0, c_selectedGridConnections.get(0).data_gridCapacityDemand_kW.size() - 1);
double timeAxisValue = c_selectedGridConnections.get(0).data_gridCapacityDemand_kW.getX(i); // we get the X value from a random dataset 

for (int j = 0; j < c_selectedGridConnections.size(); j++) {

	AreaCollection AC = uI_Results.c_individualGridConnections.get(j);
	
	double totalElectricityDemandLiveWeek = 0;
	double totalElectricitySupplyLiveWeek = 0;
	
	/*
	
	totalElectricityDemandLiveWeek += AC.v_dataElectricityBaseloadConsumptionLiveWeek_kW.getY(i);
	totalElectricityDemandLiveWeek += AC.v_dataElectricityForHeatConsumptionLiveWeek_kW.getY(i);
	totalElectricityDemandLiveWeek += AC.v_dataElectricityForTransportConsumptionLiveWeek_kW.getY(i);
	totalElectricityDemandLiveWeek += AC.v_dataElectricityForStorageConsumptionLiveWeek_kW.getY(i);
	totalElectricityDemandLiveWeek += AC.v_dataElectricityForHydrogenConsumptionLiveWeek_kW.getY(i);
	
	totalElectricitySupplyLiveWeek += AC.v_dataWindElectricityProductionLiveWeek_kW.getY(i);
	totalElectricitySupplyLiveWeek += AC.v_dataPVElectricityProductionLiveWeek_kW.getY(i);
	totalElectricitySupplyLiveWeek += AC.v_dataStorageElectricityProductionLiveWeek_kW.getY(i);
	totalElectricitySupplyLiveWeek += AC.v_dataV2GElectricityProductionLiveWeek_kW.getY(i);

	AC.v_dataTotalElectricityDemandLiveWeek_kW.add(timeAxisValue, totalElectricityDemandLiveWeek);
	AC.v_dataTotalElectricitySupplyLiveWeek_kW.add(timeAxisValue, totalElectricitySupplyLiveWeek);
	
	*/
	
}



/*ALCODEEND*/}

double f_setColorsBasedOnGridTopology_gridnodes(GridNode GN)
{/*ALCODESTART::1725968656820*/
GN.gisRegion.setFillColor(GN.p_uniqueColor);
GN.gisRegion.setLineColor(GN.p_uniqueColor.brighter());
/*ALCODEEND*/}

double f_setSliderPresets()
{/*ALCODESTART::1725977409304*/
//Should be overridden in child interface!!!
f_updateMainInterfaceSliders();
traceln("Forgot to override project specific slider settings!!");
/*ALCODEEND*/}

double f_projectSpecificStyling()
{/*ALCODESTART::1726068314849*/
//Function used to set the colors, styling, and other parameters/functions for each specific project
//Should be overridden!!
traceln("DID NOT OVERRIDE THE PROJECT SPECIFIC STYLING!");
/*ALCODEEND*/}

double f_updatePreviousTotalsGC()
{/*ALCODESTART::1727085747538*/
for (GridConnection GC : energyModel.f_getGridConnections()){	
	J_previousTotals previousTotals = uI_Results.c_previousTotals_GC.get(GC);
	
	previousTotals.setPreviousTotalImports_MWh(GC.fm_totalImports_MWh);
	previousTotals.setPreviousTotalExports_MWh(GC.fm_totalExports_MWh);
	
	previousTotals.setPreviousTotalConsumedEnergy_MWh(GC.v_totalEnergyConsumed_MWh);
	previousTotals.setPreviousTotalProducedEnergy_MWh(GC.v_totalEnergyProduced_MWh);
	previousTotals.setPreviousSelfConsumedEnergy_MWh(GC.v_totalEnergySelfConsumed_MWh);
	previousTotals.setPreviousImportedEnergy_MWh(GC.v_totalEnergyImport_MWh);
	previousTotals.setPreviousExportedEnergy_MWh(GC.v_totalEnergyExport_MWh);
	previousTotals.setPreviousSelfConsumedElectricity_MWh(GC.v_totalElectricitySelfConsumed_MWh);
	previousTotals.setPreviousElectricityConsumed_MWh(GC.v_totalElectricityConsumed_MWh);
	
	//Overload
	previousTotals.setPreviousOverloadDurationDelivery_hr(GC.v_totalOverloadDurationDelivery_hr);
	previousTotals.setPreviousOverloadDurationFeedin_hr(GC.v_totalOverloadDurationFeedin_hr);
}

/*ALCODEEND*/}

double f_projectSpecificOrderedCollectionAdjustments()
{/*ALCODESTART::1729685968993*/
//Function that can be used to make project specific adjustments to the ordered collection
//SHOULD BE OVERRIDEN IF YOU WANT TO USE THIS
/*ALCODEEND*/}

double f_updateVariablesOfGCData(AreaCollection area,ArrayList<GridConnection> gcList)
{/*ALCODESTART::1733490994454*/
//ArrayList<GridConnection> gcList = c_gcList;
EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class);
EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class);
for (GridConnection gc : gcList) {
	activeProductionEnergyCarriers.addAll(gc.v_activeProductionEnergyCarriers);
	activeConsumptionEnergyCarriers.addAll(gc.v_activeConsumptionEnergyCarriers);
}

//Set active energyCarriers
area.v_activeProductionEnergyCarriers = activeProductionEnergyCarriers;
area.v_activeConsumptionEnergyCarriers = activeConsumptionEnergyCarriers;


//Set boolean if it has connection with heatgrid
boolean hasHeatGridConnection = false;
for (GridConnection gc : gcList) {
	if(gc.l_parentNodeHeat != null){
		area.b_hasHeatGridConnection = true;
	}
}

// Can't use pointer for (immutable) primitives in Java, so need to manually update results after a year-sim!!
area.v_gridCapacityDelivery_kW = sum(gcList, x -> x.p_contractedDeliveryCapacity_kW);
area.v_gridCapacityFeedIn_kW = sum(gcList, x -> x.p_contractedFeedinCapacity_kW);
boolean isRealDeliveryCapacityAvailable = true;
boolean isRealFeedinCapacityAvailable = true;
for(GridConnection GC : gcList){
	if(!GC.b_isRealDeliveryCapacityAvailable){
		isRealDeliveryCapacityAvailable = false;
		break;
	}
}
for(GridConnection GC : gcList){
	if(!GC.b_isRealFeedinCapacityAvailable){
		isRealFeedinCapacityAvailable = false;
		break;
	}
}
area.b_isRealDeliveryCapacityAvailable = isRealDeliveryCapacityAvailable;
area.b_isRealFeedinCapacityAvailable = isRealFeedinCapacityAvailable;


// KPIs for 'samenvatting' 
area.v_modelSelfConsumption_fr = sum(gcList, x -> x.v_totalEnergyProduced_MWh) > 0 ? sum(gcList, x -> x.v_totalEnergySelfConsumed_MWh) / sum(gcList, x -> x.v_totalEnergyProduced_MWh) : 0.0;
//area.v_individualSelfSufficiency_fr = GC.v_individualSelfSufficiency_fr;
area.v_modelSelfSufficiency_fr = sum(gcList, x -> x.v_totalEnergyProduced_MWh) > 0 ? sum(gcList, x -> x.v_totalEnergySelfConsumed_MWh) / sum(gcList, x -> x.v_totalEnergyConsumed_MWh): 0.0;
//area.v_totalTimeOverloadedTransformers_h = GC.v_netOverloadDuration_h;

//Yearly
area.fm_totalImports_MWh.clear();
area.fm_totalExports_MWh.clear();
for (OL_EnergyCarriers EC : activeProductionEnergyCarriers) {
	area.fm_totalExports_MWh.put( EC, sum(gcList, x -> x.fm_totalExports_MWh.get(EC)) );
}
for (OL_EnergyCarriers EC : activeConsumptionEnergyCarriers) {
	area.fm_totalImports_MWh.put( EC, sum(gcList, x -> x.fm_totalImports_MWh.get(EC)) );
}

area.v_totalEnergyImport_MWh = sum(gcList, x -> x.v_totalEnergyImport_MWh);
area.v_totalEnergyExport_MWh = sum(gcList, x -> x.v_totalEnergyExport_MWh);

area.v_totalEnergyProduced_MWh = sum(gcList, x -> x.v_totalEnergyProduced_MWh);
area.v_totalEnergyConsumed_MWh = sum(gcList, x -> x.v_totalEnergyConsumed_MWh);
area.v_totalEnergySelfConsumed_MWh = sum(gcList, x -> x.v_totalEnergySelfConsumed_MWh);

area.v_totalElectricityProduced_MWh = sum(gcList, x -> x.v_totalElectricityProduced_MWh);
area.v_totalElectricityConsumed_MWh = sum(gcList, x -> x.v_totalElectricityConsumed_MWh);
area.v_totalElectricitySelfConsumed_MWh = sum(gcList, x -> x.v_totalElectricitySelfConsumed_MWh);

//Overload duration (for multiple GC this does not really make sense right?, would be more interesting to see the influence of their combined contract capacity)
area.v_annualOverloadDurationDelivery_hr = sum(gcList, x -> x.v_totalOverloadDurationDelivery_hr);
area.v_annualOverloadDurationFeedin_hr = sum(gcList, x -> x.v_totalOverloadDurationFeedin_hr);

//Previous annual values: does not work (yet) for multiselect.
area.v_previousTotals = uI_Results.c_previousTotals_GC.get(gcList.get(0));


// Summer/winter
area.fm_summerWeekImports_MWh.clear();
area.fm_summerWeekExports_MWh.clear();
area.fm_winterWeekImports_MWh.clear();
area.fm_winterWeekExports_MWh.clear();
for (OL_EnergyCarriers EC : activeProductionEnergyCarriers) {
	area.fm_summerWeekExports_MWh.put( EC, sum(gcList, x -> x.fm_summerWeekExports_MWh.get(EC)) );
	area.fm_winterWeekExports_MWh.put( EC, sum(gcList, x -> x.fm_winterWeekExports_MWh.get(EC)) );
}
for (OL_EnergyCarriers EC : activeConsumptionEnergyCarriers) {
	area.fm_summerWeekImports_MWh.put( EC, sum(gcList, x -> x.fm_summerWeekImports_MWh.get(EC)) );
	area.fm_winterWeekImports_MWh.put( EC, sum(gcList, x -> x.fm_winterWeekImports_MWh.get(EC)) );
}

area.v_summerWeekEnergyImport_MWh = sum(gcList, x -> x.v_summerWeekEnergyImport_MWh);
area.v_summerWeekEnergyExport_MWh = sum(gcList, x -> x.v_summerWeekEnergyExport_MWh);

area.v_summerWeekEnergyProduced_MWh = sum(gcList, x -> x.v_summerWeekEnergyProduced_MWh);
area.v_summerWeekEnergyConsumed_MWh = sum(gcList, x -> x.v_summerWeekEnergyConsumed_MWh);
area.v_summerWeekEnergySelfConsumed_MWh = sum(gcList, x -> x.v_summerWeekEnergySelfConsumed_MWh);

area.v_summerWeekElectricityProduced_MWh = sum(gcList, x -> x.v_summerWeekElectricityProduced_MWh);
area.v_summerWeekElectricityConsumed_MWh = sum(gcList, x -> x.v_summerWeekElectricityConsumed_MWh);
area.v_summerWeekElectricitySelfConsumed_MWh = sum(gcList, x -> x.v_summerWeekElectricitySelfConsumed_MWh);

area.v_winterWeekEnergyImport_MWh = sum(gcList, x -> x.v_winterWeekEnergyImport_MWh);
area.v_winterWeekEnergyExport_MWh = sum(gcList, x -> x.v_winterWeekEnergyExport_MWh);

area.v_winterWeekEnergyProduced_MWh = sum(gcList, x -> x.v_winterWeekEnergyProduced_MWh);
area.v_winterWeekEnergyConsumed_MWh = sum(gcList, x -> x.v_winterWeekEnergyConsumed_MWh);
area.v_winterWeekEnergySelfConsumed_MWh = sum(gcList, x -> x.v_winterWeekEnergySelfConsumed_MWh);

area.v_winterWeekElectricityProduced_MWh = sum(gcList, x -> x.v_winterWeekElectricityProduced_MWh);
area.v_winterWeekElectricityConsumed_MWh = sum(gcList, x -> x.v_winterWeekElectricityConsumed_MWh);
area.v_winterWeekElectricitySelfConsumed_MWh = sum(gcList, x -> x.v_winterWeekElectricitySelfConsumed_MWh);

// Day/night
area.fm_daytimeImports_MWh.clear();
area.fm_daytimeExports_MWh.clear();
area.fm_nighttimeImports_MWh.clear();
area.fm_nighttimeExports_MWh.clear();
for (OL_EnergyCarriers EC : activeProductionEnergyCarriers) {
	area.fm_daytimeExports_MWh.put( EC, sum(gcList, x -> x.fm_daytimeExports_MWh.get(EC)) );
	area.fm_nighttimeExports_MWh.put( EC, sum(gcList, x -> x.fm_nighttimeExports_MWh.get(EC)) );
}
for (OL_EnergyCarriers EC : activeConsumptionEnergyCarriers) {
	area.fm_daytimeImports_MWh.put( EC, sum(gcList, x -> x.fm_daytimeImports_MWh.get(EC)) );
	area.fm_nighttimeImports_MWh.put( EC, sum(gcList, x -> x.fm_nighttimeImports_MWh.get(EC)) );
}

area.v_daytimeEnergyImport_MWh = sum(gcList, x -> x.v_daytimeEnergyImport_MWh);
area.v_daytimeEnergyExport_MWh = sum(gcList, x -> x.v_daytimeEnergyExport_MWh);

area.v_daytimeEnergyProduced_MWh = sum(gcList, x -> x.v_daytimeEnergyProduced_MWh);
area.v_daytimeEnergyConsumed_MWh = sum(gcList, x -> x.v_daytimeEnergyConsumed_MWh);
area.v_daytimeEnergySelfConsumed_MWh = sum(gcList, x -> x.v_daytimeEnergySelfConsumed_MWh);

area.v_daytimeElectricityProduced_MWh = sum(gcList, x -> x.v_daytimeElectricityProduced_MWh);
area.v_daytimeElectricityConsumed_MWh = sum(gcList, x -> x.v_daytimeElectricityConsumed_MWh);
area.v_daytimeElectricitySelfConsumed_MWh = sum(gcList, x -> x.v_daytimeElectricitySelfConsumed_MWh);


area.v_nighttimeEnergyImport_MWh = sum(gcList, x -> x.v_nighttimeEnergyImport_MWh);
area.v_nighttimeEnergyExport_MWh = sum(gcList, x -> x.v_nighttimeEnergyExport_MWh);

area.v_nighttimeEnergyProduced_MWh = sum(gcList, x -> x.v_nighttimeEnergyProduced_MWh);
area.v_nighttimeEnergyConsumed_MWh = sum(gcList, x -> x.v_nighttimeEnergyConsumed_MWh);
area.v_nighttimeEnergySelfConsumed_MWh = sum(gcList, x -> x.v_nighttimeEnergySelfConsumed_MWh);

area.v_nighttimeElectricityProduced_MWh = sum(gcList, x -> x.v_nighttimeElectricityProduced_MWh);
area.v_nighttimeElectricityConsumed_MWh = sum(gcList, x -> x.v_nighttimeElectricityConsumed_MWh);
area.v_nighttimeElectricitySelfConsumed_MWh = sum(gcList, x -> x.v_nighttimeElectricitySelfConsumed_MWh);

// Week/weekend
area.fm_weekdayImports_MWh.clear();
area.fm_weekdayExports_MWh.clear();
area.fm_weekendImports_MWh.clear();
area.fm_weekendExports_MWh.clear();
for (OL_EnergyCarriers EC : activeProductionEnergyCarriers) {
	area.fm_weekdayExports_MWh.put( EC, sum(gcList, x -> x.fm_weekdayExports_MWh.get(EC)) );
	area.fm_weekendExports_MWh.put( EC, sum(gcList, x -> x.fm_weekendExports_MWh.get(EC)) );
}
for (OL_EnergyCarriers EC : activeConsumptionEnergyCarriers) {
	area.fm_weekdayImports_MWh.put( EC, sum(gcList, x -> x.fm_weekdayImports_MWh.get(EC)) );
	area.fm_weekendImports_MWh.put( EC, sum(gcList, x -> x.fm_weekendImports_MWh.get(EC)) );
}


area.v_weekdayEnergyImport_MWh = sum(gcList, x -> x.v_weekdayEnergyImport_MWh);
area.v_weekdayEnergyExport_MWh = sum(gcList, x -> x.v_weekdayEnergyExport_MWh);

area.v_weekdayEnergyProduced_MWh = sum(gcList, x -> x.v_weekdayEnergyProduced_MWh);
area.v_weekdayEnergyConsumed_MWh = sum(gcList, x -> x.v_weekdayEnergyConsumed_MWh);
area.v_weekdayEnergySelfConsumed_MWh = sum(gcList, x -> x.v_weekdayEnergySelfConsumed_MWh);

area.v_weekdayElectricityProduced_MWh = sum(gcList, x -> x.v_weekdayElectricityProduced_MWh);
area.v_weekdayElectricityConsumed_MWh = sum(gcList, x -> x.v_weekdayElectricityConsumed_MWh);
area.v_weekdayElectricitySelfConsumed_MWh = sum(gcList, x -> x.v_weekdayElectricitySelfConsumed_MWh);


area.v_weekendEnergyImport_MWh = sum(gcList, x -> x.v_weekendEnergyImport_MWh);
area.v_weekendEnergyExport_MWh = sum(gcList, x -> x.v_weekendEnergyExport_MWh);

area.v_weekendEnergyProduced_MWh = sum(gcList, x -> x.v_weekendEnergyProduced_MWh);
area.v_weekendEnergyConsumed_MWh = sum(gcList, x -> x.v_weekendEnergyConsumed_MWh);
area.v_weekendEnergySelfConsumed_MWh = sum(gcList, x -> x.v_weekendEnergySelfConsumed_MWh);

area.v_weekendElectricityProduced_MWh = sum(gcList, x -> x.v_weekendElectricityProduced_MWh);
area.v_weekendElectricityConsumed_MWh = sum(gcList, x -> x.v_weekendElectricityConsumed_MWh);
area.v_weekendElectricitySelfConsumed_MWh = sum(gcList, x -> x.v_weekendElectricitySelfConsumed_MWh);

/*ALCODEEND*/}

double f_updateLiveDataSets(AreaCollection area,ArrayList<GridConnection> gcList)
{/*ALCODESTART::1733857321674*/
EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class);
EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class);

for (GridConnection gc : gcList) {	
	activeConsumptionEnergyCarriers.addAll(gc.v_activeConsumptionEnergyCarriers);
	activeProductionEnergyCarriers.addAll(gc.v_activeProductionEnergyCarriers);
}

int liveWeekSize = gcList.get(0).data_gridCapacityDemand_kW.size();

//traceln("List of selected gridconnections " + gcList.toString());

//reset the datasets (is this required)?
if( area.v_dataElectricityDeliveryCapacityLiveWeek_kW != null ){
	// Demand
	area.dsm_liveConsumption_kW.createEmptyDataSets(energyModel.v_activeConsumptionEnergyCarriers, (int)(168 / energyModel.p_timeStep_h));

	area.v_dataElectricityDeliveryCapacityLiveWeek_kW.reset();		
	area.v_dataElectricityFeedInCapacityLiveWeek_kW.reset();
	area.v_dataNetLoadLiveWeek_kW.reset();
	
	area.v_dataElectricityBaseloadConsumptionLiveWeek_kW.reset();
	area.v_dataElectricityForHeatConsumptionLiveWeek_kW.reset();
	area.v_dataElectricityForTransportConsumptionLiveWeek_kW.reset();
	area.v_dataElectricityForStorageConsumptionLiveWeek_kW.reset();
	area.v_dataElectricityForHydrogenConsumptionLiveWeek_kW.reset();
	area.v_dataElectricityForCookingConsumptionLiveWeek_kW.reset();
	
	area.v_dataDistrictHeatConsumptionLiveWeek_kW.reset();
	
	// Supply
	area.dsm_liveProduction_kW.createEmptyDataSets(energyModel.v_activeProductionEnergyCarriers, (int)(168 / energyModel.p_timeStep_h));

	area.v_dataWindElectricityProductionLiveWeek_kW.reset();
	area.v_dataPVElectricityProductionLiveWeek_kW.reset();
	area.v_dataStorageElectricityProductionLiveWeek_kW.reset();
	area.v_dataV2GElectricityProductionLiveWeek_kW.reset();
	area.v_dataCHPElectricityProductionLiveWeek_kW.reset();
}
else {
	// Demand
	area.dsm_liveConsumption_kW.createEmptyDataSets(energyModel.v_activeConsumptionEnergyCarriers, (int)(168 / energyModel.p_timeStep_h));
	
	area.v_dataElectricityDeliveryCapacityLiveWeek_kW = new DataSet(672);
	area.v_dataElectricityFeedInCapacityLiveWeek_kW = new DataSet(672);
	area.v_dataNetLoadLiveWeek_kW = new DataSet(672);
	
	area.v_dataElectricityBaseloadConsumptionLiveWeek_kW = new DataSet(672);
	area.v_dataElectricityForHeatConsumptionLiveWeek_kW = new DataSet(672);
	area.v_dataElectricityForTransportConsumptionLiveWeek_kW = new DataSet(672);
	area.v_dataElectricityForStorageConsumptionLiveWeek_kW = new DataSet(672);
	area.v_dataElectricityForHydrogenConsumptionLiveWeek_kW = new DataSet(672);
	area.v_dataElectricityForCookingConsumptionLiveWeek_kW = new DataSet(672);
	area.v_dataDistrictHeatConsumptionLiveWeek_kW = new DataSet(672);
	
	// Supply
	area.dsm_liveProduction_kW.createEmptyDataSets(energyModel.v_activeProductionEnergyCarriers, (int)(168 / energyModel.p_timeStep_h));

	area.v_dataWindElectricityProductionLiveWeek_kW = new DataSet(672);
	area.v_dataPVElectricityProductionLiveWeek_kW = new DataSet(672);
	area.v_dataStorageElectricityProductionLiveWeek_kW = new DataSet(672);
	area.v_dataV2GElectricityProductionLiveWeek_kW = new DataSet(672);
	area.v_dataCHPElectricityProductionLiveWeek_kW = new DataSet(672);
}

for (int i=0; i < liveWeekSize; i++){ //we go back to update the existing live week data
	
	double timeAxisValue = gcList.get(0).data_gridCapacityDemand_kW.getX(i); // we get the X value from a random dataset 
	
	// Demand
	J_FlowsMap fm_demand_kW = new J_FlowsMap();
	
	double electricityDemandCapacityLiveWeek_kW = 0;
	double electricitySupplyCapacityLiveWeek_kW = 0;
	double netLoadLiveWeek_kW = 0;
	
	double baseloadElectricityDemandLiveWeek_kW = 0;
	double electricityForHeatDemandLiveWeek_kW = 0;
	double electricityForTransportDemandLiveWeek_kW = 0;
	double petroleumProductsDemandLiveWeek_kW = 0;
	double naturalGasDemandLiveWeek_kW = 0;
	double electricityForStorageDemandLiveWeek_kW = 0;
	double electricityForHydrogenDemandLiveWeek_kW = 0;
	double electricityForCookingConsumptionLiveWeek_kW = 0;
	
	double districtHeatingDemandLiveWeek_kW = 0;
	
	// Supply
	J_FlowsMap fm_supply_kW = new J_FlowsMap();

	double windElectricitySupplyLiveWeek_kW = 0;
	double PVElectricitySupplyLiveWeek_kW = 0;
	double storageElectricitySupplyLiveWeek_kW = 0;
	double V2GElectricitySupplyLiveWeek_kW = 0;
	double hydrogenSupplyLiveWeek_kW = 0;
	double CHPElectricitySupplyLiveWeek_kW = 0;
	
	for (GridConnection gc : gcList){
		for (OL_EnergyCarriers EC_consumption : gc.v_activeConsumptionEnergyCarriers) {
			fm_demand_kW.addFlow( EC_consumption, gc.dsm_liveDemand_kW.get(EC_consumption).getY(i));
		}
		for (OL_EnergyCarriers EC_production : gc.v_activeProductionEnergyCarriers) {
			fm_supply_kW.addFlow( EC_production, gc.dsm_liveSupply_kW.get(EC_production).getY(i));
		}
		
		electricityDemandCapacityLiveWeek_kW += gc.data_gridCapacityDemand_kW.getY(i);
		electricitySupplyCapacityLiveWeek_kW += gc.data_gridCapacitySupply_kW.getY(i);
		netLoadLiveWeek_kW  += gc.data_liveElectricityBalance_kW.getY(i);
	
		baseloadElectricityDemandLiveWeek_kW  += gc.data_baseloadElectricityDemand_kW.getY(i);
		electricityForHeatDemandLiveWeek_kW  += gc.data_heatPumpElectricityDemand_kW.getY(i);
		electricityForTransportDemandLiveWeek_kW += gc.data_electricVehicleDemand_kW.getY(i);
		electricityForStorageDemandLiveWeek_kW  += gc.data_batteryCharging_kW.getY(i);
		electricityForHydrogenDemandLiveWeek_kW  += gc.data_hydrogenElectricityDemand_kW.getY(i);
		electricityForCookingConsumptionLiveWeek_kW += gc.data_cookingElectricityDemand_kW.getY(i);
		districtHeatingDemandLiveWeek_kW += gc.data_districtHeatDelivery_kW.getY(i);
		
		// Supply
		windElectricitySupplyLiveWeek_kW  += gc.data_windGeneration_kW.getY(i);
		PVElectricitySupplyLiveWeek_kW  += gc.data_PVGeneration_kW.getY(i);
		storageElectricitySupplyLiveWeek_kW  += gc.data_batteryDischarging_kW.getY(i);
		V2GElectricitySupplyLiveWeek_kW  += gc.data_V2GSupply_kW.getY(i);
		CHPElectricitySupplyLiveWeek_kW  += gc.data_CHPElectricityProductionLiveWeek_kW.getY(i);
	}
	
	for (OL_EnergyCarriers EC_consumption : activeConsumptionEnergyCarriers) {
		area.dsm_liveConsumption_kW.get(EC_consumption).add(timeAxisValue, fm_demand_kW.get(EC_consumption));
	}
	for (OL_EnergyCarriers EC_production : activeProductionEnergyCarriers) {
		area.dsm_liveProduction_kW.get(EC_production).add(timeAxisValue, fm_supply_kW.get(EC_production));
	}
	
		
	area.v_dataElectricityDeliveryCapacityLiveWeek_kW.add(timeAxisValue, electricityDemandCapacityLiveWeek_kW);
	area.v_dataElectricityFeedInCapacityLiveWeek_kW.add(timeAxisValue, electricitySupplyCapacityLiveWeek_kW);
	area.v_dataNetLoadLiveWeek_kW.add(timeAxisValue, netLoadLiveWeek_kW);
	
	area.v_dataElectricityBaseloadConsumptionLiveWeek_kW.add(timeAxisValue, baseloadElectricityDemandLiveWeek_kW);
	area.v_dataElectricityForHeatConsumptionLiveWeek_kW.add(timeAxisValue, electricityForHeatDemandLiveWeek_kW);
	area.v_dataElectricityForTransportConsumptionLiveWeek_kW.add(timeAxisValue, electricityForTransportDemandLiveWeek_kW);
	area.v_dataElectricityForStorageConsumptionLiveWeek_kW.add(timeAxisValue, electricityForStorageDemandLiveWeek_kW);
	area.v_dataElectricityForHydrogenConsumptionLiveWeek_kW.add(timeAxisValue, electricityForHydrogenDemandLiveWeek_kW);
	area.v_dataElectricityForCookingConsumptionLiveWeek_kW.add(timeAxisValue, electricityForCookingConsumptionLiveWeek_kW);
	area.v_dataDistrictHeatConsumptionLiveWeek_kW.add(timeAxisValue, districtHeatingDemandLiveWeek_kW);
	
	// Supply
	area.v_dataWindElectricityProductionLiveWeek_kW.add(timeAxisValue, windElectricitySupplyLiveWeek_kW);
	area.v_dataPVElectricityProductionLiveWeek_kW.add(timeAxisValue, PVElectricitySupplyLiveWeek_kW);
	area.v_dataStorageElectricityProductionLiveWeek_kW.add(timeAxisValue, storageElectricitySupplyLiveWeek_kW);
	area.v_dataV2GElectricityProductionLiveWeek_kW.add(timeAxisValue, V2GElectricitySupplyLiveWeek_kW);
	area.v_dataCHPElectricityProductionLiveWeek_kW.add(timeAxisValue, CHPElectricitySupplyLiveWeek_kW);	
}

/*ALCODEEND*/}

double f_addTimeStepLiveDataSets(AreaCollection area,ArrayList<GridConnection> gcList)
{/*ALCODESTART::1733857328199*/

EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class);
EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class);

for (GridConnection gc : gcList) {	
	activeConsumptionEnergyCarriers.addAll(gc.v_activeConsumptionEnergyCarriers);
	activeProductionEnergyCarriers.addAll(gc.v_activeProductionEnergyCarriers);
}


int i = max(0, gcList.get(0).data_gridCapacityDemand_kW.size() - 1);
	
double timeAxisValue = gcList.get(0).data_gridCapacityDemand_kW.getX(i); // we get the X value from a random dataset 

// Demand
J_FlowsMap fm_demand_kW = new J_FlowsMap();
double electricityDemandCapacityLiveWeek_kW = 0;
double electricitySupplyCapacityLiveWeek_kW = 0;
double netLoadLiveWeek_kW = 0;

double baseloadElectricityDemandLiveWeek_kW = 0;
double electricityForHeatDemandLiveWeek_kW = 0;
double electricityForTransportDemandLiveWeek_kW = 0;
double electricityForStorageDemandLiveWeek_kW = 0;
double electricityForHydrogenDemandLiveWeek_kW = 0;
double electricityForCookingDemandLiveWeek_kW = 0;
double districtHeatingDemandLiveWeek_kW = 0;

// Supply
J_FlowsMap fm_supply_kW = new J_FlowsMap();
double windElectricitySupplyLiveWeek_kW = 0;
double PVElectricitySupplyLiveWeek_kW = 0;
double storageElectricitySupplyLiveWeek_kW = 0;
double V2GElectricitySupplyLiveWeek_kW = 0;
double CHPElectricitySupplyLiveWeek_kW = 0;
for (GridConnection gc : gcList){
	
	for (OL_EnergyCarriers EC_consumption : gc.v_activeConsumptionEnergyCarriers) {
		fm_demand_kW.addFlow( EC_consumption, gc.dsm_liveDemand_kW.get(EC_consumption).getY(i));
	}
	for (OL_EnergyCarriers EC_production : gc.v_activeProductionEnergyCarriers) {
		fm_supply_kW.addFlow( EC_production, gc.dsm_liveSupply_kW.get(EC_production).getY(i));
	}
	
	// Demand
	electricityDemandCapacityLiveWeek_kW += gc.data_gridCapacityDemand_kW.getY(i);
	electricitySupplyCapacityLiveWeek_kW += gc.data_gridCapacitySupply_kW.getY(i);
	netLoadLiveWeek_kW  += gc.data_liveElectricityBalance_kW.getY(i);

	baseloadElectricityDemandLiveWeek_kW  += gc.data_baseloadElectricityDemand_kW.getY(i);
	electricityForHeatDemandLiveWeek_kW  += gc.data_heatPumpElectricityDemand_kW.getY(i);
	electricityForTransportDemandLiveWeek_kW += gc.data_electricVehicleDemand_kW.getY(i);
	electricityForStorageDemandLiveWeek_kW  += gc.data_batteryCharging_kW.getY(i);
	electricityForHydrogenDemandLiveWeek_kW  += gc.data_hydrogenElectricityDemand_kW.getY(i);
	electricityForCookingDemandLiveWeek_kW += gc.data_cookingElectricityDemand_kW.getY(i);
	districtHeatingDemandLiveWeek_kW += gc.data_districtHeatDelivery_kW.getY(i);
	
	
	// Supply
	windElectricitySupplyLiveWeek_kW  += gc.data_windGeneration_kW.getY(i);
	PVElectricitySupplyLiveWeek_kW  += gc.data_PVGeneration_kW.getY(i);
	storageElectricitySupplyLiveWeek_kW  += gc.data_batteryDischarging_kW.getY(i);
	V2GElectricitySupplyLiveWeek_kW  += gc.data_V2GSupply_kW.getY(i);
	CHPElectricitySupplyLiveWeek_kW  += gc.data_CHPElectricityProductionLiveWeek_kW.getY(i);		
}

for (OL_EnergyCarriers EC_consumption : activeConsumptionEnergyCarriers) {	
	area.dsm_liveConsumption_kW.get(EC_consumption).add(timeAxisValue, fm_demand_kW.get(EC_consumption));	
}

for (OL_EnergyCarriers EC_production : activeProductionEnergyCarriers) {	
	area.dsm_liveProduction_kW.get(EC_production).add(timeAxisValue, fm_supply_kW.get(EC_production));	
}


area.v_dataElectricityDeliveryCapacityLiveWeek_kW.add(timeAxisValue, electricityDemandCapacityLiveWeek_kW);
area.v_dataElectricityFeedInCapacityLiveWeek_kW.add(timeAxisValue, electricitySupplyCapacityLiveWeek_kW);
area.v_dataNetLoadLiveWeek_kW.add(timeAxisValue, netLoadLiveWeek_kW);

area.v_dataElectricityBaseloadConsumptionLiveWeek_kW.add(timeAxisValue, baseloadElectricityDemandLiveWeek_kW);
area.v_dataElectricityForHeatConsumptionLiveWeek_kW.add(timeAxisValue, electricityForHeatDemandLiveWeek_kW);
area.v_dataElectricityForTransportConsumptionLiveWeek_kW.add(timeAxisValue, electricityForTransportDemandLiveWeek_kW);
area.v_dataElectricityForStorageConsumptionLiveWeek_kW.add(timeAxisValue, electricityForStorageDemandLiveWeek_kW);
area.v_dataElectricityForHydrogenConsumptionLiveWeek_kW.add(timeAxisValue, electricityForHydrogenDemandLiveWeek_kW);
area.v_dataElectricityForCookingConsumptionLiveWeek_kW.add(timeAxisValue, electricityForCookingDemandLiveWeek_kW);
area.v_dataDistrictHeatConsumptionLiveWeek_kW.add(timeAxisValue, districtHeatingDemandLiveWeek_kW);

// Supply
area.v_dataWindElectricityProductionLiveWeek_kW.add(timeAxisValue, windElectricitySupplyLiveWeek_kW);
area.v_dataPVElectricityProductionLiveWeek_kW.add(timeAxisValue, PVElectricitySupplyLiveWeek_kW);
area.v_dataStorageElectricityProductionLiveWeek_kW.add(timeAxisValue, storageElectricitySupplyLiveWeek_kW);
area.v_dataV2GElectricityProductionLiveWeek_kW.add(timeAxisValue, V2GElectricitySupplyLiveWeek_kW);
area.v_dataCHPElectricityProductionLiveWeek_kW.add(timeAxisValue, CHPElectricitySupplyLiveWeek_kW);
		

/*ALCODEEND*/}

double f_updateActiveAssetBooleansGC(AreaCollection area,ArrayList<GridConnection> gcList)
{/*ALCODESTART::1734361725171*/
area.b_hasElectricHeating = false;
area.b_hasElectricTransport = false;
area.b_hasPV = false;
area.b_hasWindturbine = false;
area.b_hasBattery = false;
area.b_hasHeatGridConnection = false;
area.b_hasElectrolyser = false;
area.b_hasCHP = false;
area.b_hasV2G = false;
area.b_hasElectricCooking = false;

//Electric heating
for(GridConnection GC : gcList){
	if(GC.c_electricHeatpumpAssets.size()>0 && GC.v_isActive){
		area.b_hasElectricHeating = true;
		break;
	}
}
//Electric vehicles
for(GridConnection GC : gcList){
	if(GC.c_EvAssets.size()>0 && GC.v_isActive){
		area.b_hasElectricTransport = true;
		break;
	}
}
//PV
for(GridConnection GC : gcList){
	if(GC.c_pvAssets.size()>0 && GC.v_isActive){
		area.b_hasPV = true;
		break;
	}
}
//Wind
for(GridConnection GC : gcList){
	if(GC.c_windAssets.size()>0 && GC.v_isActive){
		area.b_hasWindturbine = true;
		break;
	}
}
//Battery
for(GridConnection GC : gcList){
	if(GC.c_batteryAssets.size()>0 && GC.v_isActive){
		for(J_EA battery : GC.c_batteryAssets){
			if(((J_EAStorageElectric)battery).getStorageCapacity_kWh() > 0){
				area.b_hasBattery = true;
				break;
			}
		}
	}
}
//Heat grid
for(GridConnection GC : gcList){
	if(GC.l_parentNodeHeat.getConnectedAgent() != null && GC.v_isActive){
		area.b_hasHeatGridConnection = true;
		break;
	}
}
//Electrolyser
for(GridConnection GC : gcList){
	if(GC.c_electrolyserAssets.size()>0 && GC.v_isActive){
		area.b_hasElectrolyser = true;
		break;
	}
}
//CHP
for(GridConnection GC : gcList){
	if(GC.c_chpAssets.size()>0 && GC.v_isActive){
		area.b_hasCHP = true;
		break;
	}
}
//V2g
for(GridConnection GC : gcList){
	if(GC.p_chargingAttitudeVehicles == OL_ChargingAttitude.V2G && GC.c_EvAssets.size()>0 && GC.v_isActive){
		area.b_hasV2G = true;
		break;
	}
}
//Electric cooking
for(GridConnection GC : gcList){
	if(GC.c_electricHobAssets.size()>0 && GC.v_isActive){
		area.b_hasElectricCooking = true;
		break;
	}
}

/*ALCODEEND*/}

double f_updateActiveAssetBooleans()
{/*ALCODESTART::1734368189128*/
f_updateActiveAssetBooleansGC(uI_Results.v_area, energyModel.f_getGridConnections());
if(b_multiSelect && c_selectedGridConnections.size() > 1){
	for (int i = 0; i < c_selectedGridConnections.size(); i++) {
		f_updateActiveAssetBooleansGC(uI_Results.c_individualGridConnections.get(i), new ArrayList<GridConnection>(c_selectedGridConnections.subList(i, i+1)));
	}
}
else{
	f_updateActiveAssetBooleansGC(uI_Results.v_gridConnection, c_selectedGridConnections);
}
uI_Results.f_showCorrectChart();
/*ALCODEEND*/}

double f_applyFilter(OL_FilterOptionsGC selectedFilter,String selectedFilterName)
{/*ALCODESTART::1734442458629*/
c_selectedFilterOptions.add(selectedFilter);

ArrayList<GridConnection> toBeFilteredGC = new ArrayList<GridConnection>();

if(c_selectedFilterOptions.size()>1 && c_selectedGridConnections.size()> 0){ // Already filtering
	toBeFilteredGC = new ArrayList<GridConnection>(c_selectedGridConnections);
}
else{ // First filter
	toBeFilteredGC = new ArrayList<GridConnection>(energyModel.f_getGridConnections());
}

//After a filter selecttion, reset previous clicked building/gridNode colors and text
v_previousClickedObjectType = v_clickedObjectType;
c_previousSelectedObjects = new ArrayList<GIS_Object>(c_selectedObjects);
c_selectedGridConnections.clear();
c_selectedObjects.clear();


//Deselect previous selection
if( v_previousClickedObjectType != null){
	f_deselectPreviousSelect();
}

//Can filter return 0? (Only allowed for filters who are not inmediately active (gridLoops, nbh, etc.)
boolean filterCanReturnZero = false;

switch(selectedFilter){
	case COMPANIES:
			f_filterCompanies(toBeFilteredGC);
		break;
		
	case HOUSES:
			f_filterHouses(toBeFilteredGC);
		break;
		
	case DETAILED:
			f_filterDetailed(toBeFilteredGC);
		break;
		
	case NONDETAILED:
			f_filterEstimated(toBeFilteredGC);
		break;
		
	case HASPV:
			f_filterHasPV(toBeFilteredGC);
		break;
		
	case HASTRANSPORT:
			f_filterHasTransport(toBeFilteredGC);
		break;
		
	case GRIDTOPOLOGY_SELECTEDLOOP:
		//
		if(v_selectedGridLoop != null){
			f_filterGridLoop(toBeFilteredGC);
		}
		else if(c_selectedFilterOptions.size() > 1){
			rb_buildingColors.setValue(3,true);
			c_selectedGridConnections = new ArrayList<>(toBeFilteredGC);	
		}
		else{
			rb_buildingColors.setValue(3,true);
			filterCanReturnZero = true;
		}
		break;
}

if(c_selectedGridConnections.size() == 0 && !filterCanReturnZero){ // Not allowed to return zero, while returning zero
	f_removeFilter(selectedFilter, selectedFilterName);
	//cb_showFilterInterface.setValueToDefault();
	
	//Notify filter has not been applied, cause no results are given
	f_setErrorScreen("Geselecteerde filter geeft geen resultaten.\n" + "De filter is gedeactiveerd.");
}
else if(c_selectedGridConnections.size() == 0 && filterCanReturnZero){//Allowed to return zero filtered gc, while returning zero
	//Do nothing
}
else{//Filtered GC returns GC
	//Set color of all gis objects of new filter selection
	v_clickedObjectType = OL_GISObjectType.BUILDING;
		
	for (GridConnection GC: c_selectedGridConnections){
		for (GIS_Object objectGIS : GC.c_connectedGISObjects) {
			objectGIS.gisRegion.setFillColor(v_selectionColorAddBuildings);
			c_selectedObjects.add(objectGIS);
		}
	}
	
	//Set graphs
	f_updateUIresultsGridConnection(uI_Results.v_gridConnection, c_selectedGridConnections);
	uI_Results.v_selectedObjectType = v_clickedObjectType;				
	uI_Results.f_showCorrectChart();
}
/*ALCODEEND*/}

double f_setFilter(String selectedFilterName)
{/*ALCODESTART::1734442462084*/
OL_FilterOptionsGC selectedFilter_OL = null;
switch(selectedFilterName){
	case "-":
		//Do nothing
		break;
	case "Bedrijfspanden":
		selectedFilter_OL = OL_FilterOptionsGC.COMPANIES;
		break;
	case "Woonhuizen":
		selectedFilter_OL = OL_FilterOptionsGC.HOUSES;
		break;
	case "Met bekende data":
		selectedFilter_OL = OL_FilterOptionsGC.DETAILED;
		break;
	case "Met geschatte data":
		selectedFilter_OL = OL_FilterOptionsGC.NONDETAILED;
		break;
	case "Met zonnepanelen":
		selectedFilter_OL = OL_FilterOptionsGC.HASPV;
		break;
	case "Met voertuigen":
		selectedFilter_OL = OL_FilterOptionsGC.HASTRANSPORT;
		break;
	case "In de aangewezen 'netbuurt'":
		selectedFilter_OL = OL_FilterOptionsGC.GRIDTOPOLOGY_SELECTEDLOOP;
		break;
}


if(!selectedFilterName.equals("-") && !c_selectedFilterOptions.contains(selectedFilter_OL)){ // Set filter
	traceln("Geselecteerde filter ( " + selectedFilterName + " ) toegevoegd.");
	t_activeFilters.setText( t_activeFilters.getText() + selectedFilterName + "\n");
	f_applyFilter(selectedFilter_OL, selectedFilterName);
}
else if(c_selectedFilterOptions.contains(selectedFilter_OL)){ // Remove filter
	f_removeFilter(selectedFilter_OL, selectedFilterName);
}


/*ALCODEEND*/}

double f_removeAllFilters()
{/*ALCODESTART::1734445008646*/
c_selectedFilterOptions.clear();
t_activeFilters.setText("");

//Deselect everything and set region as main
//After a filter selecttion, reset previous clicked building/gridNode colors and text
v_previousClickedObjectType = v_clickedObjectType;
c_previousSelectedObjects = new ArrayList<GIS_Object>(c_selectedObjects);
c_selectedGridConnections.clear();
c_selectedObjects.clear();

//Deselect previous selection
if( v_previousClickedObjectType != null){
	f_deselectPreviousSelect();
}

v_selectedGridLoop = null;

v_clickedObjectType = OL_GISObjectType.REGION;
uI_Results.v_selectedObjectType = OL_GISObjectType.REGION;
uI_Results.f_showCorrectChart();

traceln("Alle filters zijn verwijderd.");
/*ALCODEEND*/}

double f_selectGridLoop(double clickx,double clicky)
{/*ALCODESTART::1734447122780*/

//Check if click was on Building, if yes, select grid building
for ( GIS_Building b : energyModel.pop_GIS_Buildings ){
	if( b.gisRegion != null && b.gisRegion.contains(clickx, clicky) ){
		if (b.gisRegion.isVisible()) { //only allow us to click on visible objects
			if (b.c_containedGridConnections.size() > 0 ) { // only allow buildings with gridconnections
				GridConnection clickedGridConnection = b.c_containedGridConnections.get(0); // Find buildings powered by the same GC as the clicked building
				GridNode clickedGridConnectionConnectedGridNode = clickedGridConnection.l_parentNodeElectric.getConnectedAgent();
				ArrayList<GridNode> allGridNodes = new ArrayList<GridNode>(energyModel.f_getGridNodesTopLevel());
				allGridNodes.addAll(energyModel.f_getGridNodesNotTopLevel());
				
				while(	clickedGridConnectionConnectedGridNode.p_parentNodeID != null && 
					  	clickedGridConnectionConnectedGridNode.p_nodeType != OL_GridNodeType.SUBMV &&
					  	clickedGridConnectionConnectedGridNode.p_nodeType != OL_GridNodeType.MVMV){
					String parentNodeName = clickedGridConnectionConnectedGridNode.p_parentNodeID;
					if(parentNodeName != null && !parentNodeName.equals("-") && !parentNodeName.equals("")){
						clickedGridConnectionConnectedGridNode = findFirst(allGridNodes, GN -> GN.p_gridNodeID.equals(parentNodeName));
					}
					else{ // At top node --> select the directly attached grid node instead, and break out of while loop.
						clickedGridConnectionConnectedGridNode = clickedGridConnection.l_parentNodeElectric.getConnectedAgent();
						break;
					}
				}	
				
				//This deselect the previous selected ring
				f_setFilter("In de aangewezen 'netbuurt'");
				
				//This selects the new selected ring
				v_selectedGridLoop = clickedGridConnectionConnectedGridNode;
				f_setFilter("In de aangewezen 'netbuurt'");
				
				return;
				
			}
		}
	}
}

/*ALCODEEND*/}

double f_filterCompanies(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1734448628428*/
c_selectedGridConnections = new ArrayList<>(findAll(toBeFilteredGC, GC -> GC instanceof GCUtility));


/*ALCODEEND*/}

double f_filterHouses(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1734448687355*/
c_selectedGridConnections = new ArrayList<>(findAll(toBeFilteredGC, GC -> GC instanceof GCHouse));


/*ALCODEEND*/}

double f_filterDetailed(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1734448688472*/
c_selectedGridConnections = new ArrayList<>(findAll(toBeFilteredGC, GC -> GC.p_owner.p_detailedCompany));

/*ALCODEEND*/}

double f_filterEstimated(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1734448689519*/
c_selectedGridConnections = new ArrayList<>(findAll(toBeFilteredGC, GC -> !GC.p_owner.p_detailedCompany));

/*ALCODEEND*/}

double f_filterHasPV(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1734448690487*/
c_selectedGridConnections = new ArrayList<>(findAll(toBeFilteredGC, GC -> GC.v_hasPV));

/*ALCODEEND*/}

double f_filterHasTransport(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1734448691508*/
c_selectedGridConnections = new ArrayList<>(findAll(toBeFilteredGC, GC -> GC.c_vehicleAssets.size() > 0));


/*ALCODEEND*/}

double f_removeFilter(OL_FilterOptionsGC selectedFilter,String selectedFilterName)
{/*ALCODESTART::1734451505770*/
c_selectedFilterOptions.remove(selectedFilter);

ArrayList<OL_FilterOptionsGC> toBeReappliedFilters = new ArrayList<OL_FilterOptionsGC>(c_selectedFilterOptions);
c_selectedFilterOptions.clear();

if(toBeReappliedFilters.size() > 0){
	for(OL_FilterOptionsGC filterOption : toBeReappliedFilters){
		f_applyFilter(filterOption, selectedFilterName);
	}
	String toBeAdjustedFilterText = t_activeFilters.getText();
	String newActiveFilterText = toBeAdjustedFilterText.replace(selectedFilterName + "\n", "");
	t_activeFilters.setText(newActiveFilterText);
	
	traceln("Filter ( " + selectedFilterName + " ) is verwijderd.");
}
else{ // All filters removed
	traceln("Filter ( " + selectedFilterName + " ) is verwijderd.");
	f_removeAllFilters();
}
/*ALCODEEND*/}

double f_filterGridLoop(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1734517589341*/
OL_GridNodeType loopTopNodeType= v_selectedGridLoop.p_nodeType;


ArrayList<GridConnection> gridConnectionsOnLoop = new ArrayList<GridConnection>();

if(b_gridLoopsAreDefined){
	switch(loopTopNodeType){
		case MVLV:
			for(GridConnection GC : v_selectedGridLoop.f_getConnectedGridConnections()){
				if(toBeFilteredGC.contains(GC)){
					gridConnectionsOnLoop.add(GC);
				}
			}
			c_selectedGridConnections = new ArrayList<>(gridConnectionsOnLoop);

		case SUBMV:
			for(GridConnection GC : v_selectedGridLoop.f_getAllLowerLVLConnectedGridConnections()){
				if(toBeFilteredGC.contains(GC)){
					gridConnectionsOnLoop.add(GC);
				}
			}
			c_selectedGridConnections = new ArrayList<>(gridConnectionsOnLoop);
			break;
		
		case MVMV:
			for(GridConnection GC : v_selectedGridLoop.f_getConnectedGridConnections()){
				if(toBeFilteredGC.contains(GC)){
					gridConnectionsOnLoop.add(GC);
				}
			}
			c_selectedGridConnections = new ArrayList<>(gridConnectionsOnLoop);
			break;
			
		case HVMV:
			for(GridConnection GC : v_selectedGridLoop.f_getConnectedGridConnections()){
				if(toBeFilteredGC.contains(GC)){
					gridConnectionsOnLoop.add(GC);
				}
			}
			c_selectedGridConnections = new ArrayList<>(gridConnectionsOnLoop);
			break; 
	}
}
else{
	for(GridConnection GC : v_selectedGridLoop.f_getAllLowerLVLConnectedGridConnections()){
		if(toBeFilteredGC.contains(GC)){
			gridConnectionsOnLoop.add(GC);
		}
	}
	c_selectedGridConnections = new ArrayList<>(gridConnectionsOnLoop);
}
/*ALCODEEND*/}

double f_setErrorScreen(String errorMessage)
{/*ALCODESTART::1736344958050*/
//Set position above all other things
presentation.remove(gr_errorScreen);
presentation.insert(presentation.size(), gr_errorScreen);

t_errorMessage.setText(errorMessage);
gr_errorScreen.setVisible(true);
/*ALCODEEND*/}

double[] f_calculateGroupATOConnectionCapacity(ArrayList<GridConnection> gcList)
{/*ALCODESTART::1736425024531*/
// TODO: Add as an argument the grid operator when different calculations are available.
// For now only Stedin is implemented, so this calculation is always chosen.

double deliveryCapacity_kW = 0;
double feedInCapacity_kW = 0;

ArrayList<String> warnings = new ArrayList<>();

//TODO: First we check if all the gridconnections are on a single ring in the grid topology. If not we add a warning
HashSet<GridNode> parentNodes = new HashSet<>();

for (GridConnection gc : gcList) {
	
	// GridTopology Check
	GridNode gn = gc.l_parentNodeElectric.getConnectedAgent();
	// TODO: Improve this so it uses an OptionList instead of string comparison
	if ( gn.p_description.toLowerCase().contains("klantstation") ) {
		GridNode parentNode = findFirst(energyModel.pop_gridNodes, p -> p.p_gridNodeID.equals(gn.p_parentNodeID));
		if (parentNode == null) {
			traceln("Warning! Could not find parentnode of klantstation of GC: " + gc.p_ownerID);			
		}
		else if (parentNode.p_nodeType != OL_GridNodeType.SUBMV) {
			// add warning, gc has klantstation, but this one is not on a ring
			traceln("Warning! GC: " + gc.p_ownerID + " is not connected to a ring in the grid topology.");
			warnings.add(gc.p_ownerID + " is niet aangesloten op een ring");
			parentNodes.add(parentNode);
		}
		else {
			parentNodes.add(parentNode);
		}
	}
	else if ( gn.p_nodeType != OL_GridNodeType.SUBMV ) {
		// add warning, this one is not on a ring
		traceln("Warning! GC: " + gc.p_ownerID + " is not connected to a ring in the grid topology.");
		warnings.add(gc.p_ownerID + " is niet aangesloten op een ring");
		parentNodes.add(gn);
	}
	else {
		parentNodes.add(gn);	
	}
	
	// Adding up the GTO Connection Capacity contributions
	traceln("gto capacities: " + Arrays.toString( v_GCGTOConnectionCapacities.get(gc)));
	deliveryCapacity_kW += v_GCGTOConnectionCapacities.get(gc)[0];
	feedInCapacity_kW += v_GCGTOConnectionCapacities.get(gc)[1];
}

if ( parentNodes.size() > 1 ) {
	// add warning
	traceln("Warning! Selected GridConnections for E-Hub are not on a single ring.");
	warnings.add("Let op! Er zijn bedrijven op verschillende ringen geselecteerd");
}

v_groupATODeliveryCapacity_kW = deliveryCapacity_kW;
v_groupATOFeedInCapacity_kW = feedInCapacity_kW;


f_EHubTabCapacityInformation(true, null);

if (gcList.size() > 0) {
	
	// fix for the WKK
	List<GridConnection> gcListWithoutWKK = new ArrayList<>(gcList);
	GridConnection GCWKK = findFirst(energyModel.EnergyConversionSites, gc -> gc.p_name.toLowerCase().contains("wkk"));
	//if (GCWKK != null) {
	gcListWithoutWKK.remove(GCWKK);
	//}
	
	f_EHubTabCapacityInformation(false, "De ring van " + gcList.get(0).p_ownerID + " is geselecteerd. \n");
	
	f_EHubTabCapacityInformation(false, 
		"De bedrijven hebben samen een leveringscapaciteit van " +
		(int) sum(gcListWithoutWKK, gc -> gc.p_contractedDeliveryCapacity_kW)  +
		"  kW. \nDe E-Hub zou een groepscontract kunnen krijgen van " +
		(int) deliveryCapacity_kW + " kW. \n"
		);
	if (warnings.size() > 0) {
		f_EHubTabCapacityInformation(false, "Waarschuwingen: \n");
		for (String str : warnings) {
			f_EHubTabCapacityInformation(false, str);
			f_EHubTabCapacityInformation(false, "\n");
		}
	}
}
else {
	f_EHubTabCapacityInformation(false, "Nog geen E-Hub samengesteld");
}
/*ALCODEEND*/}

double f_resetEHubConfigurationButton()
{/*ALCODESTART::1736425024533*/
v_clickedObjectText = "None";
b_EHubSelect = false;
uI_Results.b_EHubConfiguration = false;
uI_Tabs.pop_tabEHub.get(0).cb_EHubSelect.setSelected(false);
uI_Tabs.pop_tabEHub.get(0).t_baseGroepInfo.setText("Selecteer minimaal twee panden");
uI_Tabs.pop_tabEHub.get(0).t_groepsGTV_kW.setText("");
uI_Tabs.pop_tabEHub.get(0).t_cumulatiefGTV_kW.setText("");
uI_Tabs.pop_tabEHub.get(0).t_warnings.setText("");
/*ALCODEEND*/}

double f_EHubTabCapacityInformation(boolean reset,String textToAdd)
{/*ALCODESTART::1736425024535*/
if (reset) {
	uI_Tabs.pop_tabEHub.get(0).t_baseGroepInfo.setText("");
	uI_Tabs.pop_tabEHub.get(0).t_groepsGTV_kW.setText("");
	uI_Tabs.pop_tabEHub.get(0).t_cumulatiefGTV_kW.setText("");
	uI_Tabs.pop_tabEHub.get(0).t_warnings.setText("");
}
else {
	String currentWarningString = uI_Tabs.pop_tabEHub.get(0).t_warnings.getText();
	uI_Tabs.pop_tabEHub.get(0).t_warnings.setText(currentWarningString + textToAdd);
}


/*ALCODEEND*/}

double f_calculateGTOConnectionCapacities()
{/*ALCODESTART::1736425024537*/
// Calculation
// Stedin: remove the top 0.1% of peak loads of the past years quarterhourly values, then add the remaining maximum to the group capacity
// First we find the quarterhourly values, or if they are not available the assigned base load and add a warning that not all data was available

List<GridConnection> gcList = new ArrayList<>();
gcList.addAll(energyModel.f_getGridConnections());
//gcList.addAll(energyModel.f_getPausedGridConnections());

for (GridConnection gc : gcList) {
	int amountOfDataPoints = gc.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries().length;
	double[] quarterHourlyValues = Arrays.copyOf(gc.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries(), amountOfDataPoints);
	Arrays.sort(quarterHourlyValues);
	double filteredMaximum_kW = min(gc.p_contractedDeliveryCapacity_kW , max(0, quarterHourlyValues[amountOfDataPoints - (int) (amountOfDataPoints*0.001) - 1]));
	double filteredMinimum_kW = min(gc.p_contractedFeedinCapacity_kW, -min(0, quarterHourlyValues[(int)(amountOfDataPoints*0.001)]));
	v_GCGTOConnectionCapacities.put(gc, new double[]{filteredMaximum_kW, filteredMinimum_kW});
}

// fix for the WKK
GridConnection GCWKK = findFirst(energyModel.EnergyConversionSites, gc -> gc.p_name.toLowerCase().contains("wkk"));
if (GCWKK != null) {
	v_GCGTOConnectionCapacities.put(GCWKK, new double[]{0.0, 0.0});
}
/*ALCODEEND*/}

double f_EHubCapacityDataSets()
{/*ALCODESTART::1736425260898*/
if (c_selectedGridConnections.size() > 0) {
	
	uI_Results.v_dataEHubDeliveryCapacityLiveWeek_kW = new DataSet((int) (168 / energyModel.p_timeStep_h));
	uI_Results.v_dataEHubFeedInCapacityLiveWeek_kW = new DataSet((int) (168 / energyModel.p_timeStep_h));
	
	int liveWeekSize = c_selectedGridConnections.get(0).data_gridCapacityDemand_kW.size();
	
	for (int i=0; i < liveWeekSize; i++){ //we go back to update the existing live week data
		double timeAxisValue = c_selectedGridConnections.get(0).data_gridCapacityDemand_kW.getX(i); // we get the X value from a random dataset
		uI_Results.v_dataEHubDeliveryCapacityLiveWeek_kW.add(timeAxisValue, v_groupATODeliveryCapacity_kW);
		uI_Results.v_dataEHubFeedInCapacityLiveWeek_kW.add(timeAxisValue, -v_groupATOFeedInCapacity_kW);
	}
	
	// Year
	uI_Results.v_dataEHubDeliveryCapacityYear_kW = new DataSet(2);
	uI_Results.v_dataEHubDeliveryCapacityYear_kW.add(energyModel.p_runStartTime_h, v_groupATODeliveryCapacity_kW);
	uI_Results.v_dataEHubDeliveryCapacityYear_kW.add(energyModel.p_runEndTime_h - energyModel.p_runStartTime_h, v_groupATODeliveryCapacity_kW);
	
	uI_Results.v_dataEHubFeedInCapacityYear_kW = new DataSet(2);
	uI_Results.v_dataEHubFeedInCapacityYear_kW.add(energyModel.p_runStartTime_h, -v_groupATOFeedInCapacity_kW);
	uI_Results.v_dataEHubFeedInCapacityYear_kW.add(energyModel.p_runEndTime_h - energyModel.p_runStartTime_h, -v_groupATOFeedInCapacity_kW);
	
	// SummerWeek
	uI_Results.v_dataEHubDeliveryCapacitySummerWeek_kW = new DataSet((int) (168 / energyModel.p_timeStep_h));
	uI_Results.v_dataEHubDeliveryCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek, v_groupATODeliveryCapacity_kW);
	uI_Results.v_dataEHubDeliveryCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek + 168, v_groupATODeliveryCapacity_kW);
	
	uI_Results.v_dataEHubFeedInCapacitySummerWeek_kW = new DataSet((int) (168 / energyModel.p_timeStep_h));
	uI_Results.v_dataEHubFeedInCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek, -v_groupATOFeedInCapacity_kW);
	uI_Results.v_dataEHubFeedInCapacitySummerWeek_kW.add(energyModel.p_startHourSummerWeek + 168, -v_groupATOFeedInCapacity_kW);
	
	// WinterWeek
	uI_Results.v_dataEHubDeliveryCapacityWinterWeek_kW = new DataSet((int) (168 / energyModel.p_timeStep_h));
	uI_Results.v_dataEHubDeliveryCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek, v_groupATODeliveryCapacity_kW);
	uI_Results.v_dataEHubDeliveryCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek + 168, v_groupATODeliveryCapacity_kW);
	
	uI_Results.v_dataEHubFeedInCapacityWinterWeek_kW = new DataSet((int) (168 / energyModel.p_timeStep_h));
	uI_Results.v_dataEHubFeedInCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek, -v_groupATOFeedInCapacity_kW);
	uI_Results.v_dataEHubFeedInCapacityWinterWeek_kW.add(energyModel.p_startHourWinterWeek + 168, -v_groupATOFeedInCapacity_kW);
	
}

/*ALCODEEND*/}

double f_updateLiveEHubCapacityDataSets()
{/*ALCODESTART::1736425260900*/
int i = max(0, c_selectedGridConnections.get(0).data_gridCapacityDemand_kW.size() - 1);	
double timeAxisValue = c_selectedGridConnections.get(0).data_gridCapacityDemand_kW.getX(i); // we get the X value from a random dataset 

uI_Results.v_dataEHubDeliveryCapacityLiveWeek_kW.add(timeAxisValue, v_groupATODeliveryCapacity_kW);
uI_Results.v_dataEHubFeedInCapacityLiveWeek_kW.add(timeAxisValue, -v_groupATOFeedInCapacity_kW);

/*ALCODEEND*/}

double f_fillAreaCollectionsOfIndividualGCs()
{/*ALCODESTART::1736425260902*/
if (c_selectedGridConnections.size() >= 1) {
	uI_Results.c_individualGridConnections = new ArrayList<AreaCollection>();
	for (int i = 0; i < c_selectedGridConnections.size(); i++) {
		AreaCollection AC = new AreaCollection();
		AC.f_initializeMaps();
		uI_Results.c_individualGridConnections.add(AC);
		uI_Results.c_individualGridConnections.get(i).p_name = c_selectedGridConnections.get(i).p_ownerID;
		f_updateUIresultsGridConnection(uI_Results.c_individualGridConnections.get(i), new ArrayList<GridConnection>(c_selectedGridConnections.subList(i, i+1)));
	}
	
	//f_updateTotalLiveDataSets();
	//f_updateTotalYearlyDataSets();
	//f_updateTotalWeeklyDataSets();
}
/*ALCODEEND*/}

double f_EHubSelect(double clickx,double clicky)
{/*ALCODESTART::1736425463188*/
//Check if click was on Building
for ( GIS_Building b : energyModel.pop_GIS_Buildings ){
	if( b.gisRegion != null && b.gisRegion.contains(clickx, clicky) ){
		if (b.gisRegion.isVisible()) {
			for (GridConnection GC : b.c_containedGridConnections) {
				if (GC != null) {
					if (c_selectedGridConnections.contains(GC)) {
						c_selectedGridConnections.remove(GC);
						for (GIS_Object a : GC.c_connectedGISObjects) {
							f_styleAreas(a);
						}
					}
					else {
						c_selectedGridConnections.add(GC);
						for (GIS_Object a : GC.c_connectedGISObjects) {
							a.gisRegion.setFillColor(v_selectionColorAddBuildings);
						}
					}
				}
			}
		}
	}
}

if (c_selectedGridConnections.size() > 0) {
	f_updateUIresultsGridConnection(uI_Results.v_gridConnection, c_selectedGridConnections);
	uI_Results.v_selectedObjectType = OL_GISObjectType.BUILDING;
	if (p_selectedProjectType == OL_ProjectType.BUSINESSPARK) {
		f_fillAreaCollectionsOfIndividualGCs();
		// update in the resultsUI the plots with total connection capacity with the group capacity
		f_calculateGroupATOConnectionCapacity(c_selectedGridConnections);
		uI_Results.b_EHubConfiguration = true;
		uI_Results.v_groupATODeliveryCapacity_kW = v_groupATODeliveryCapacity_kW;
		uI_Results.v_groupATOFeedInCapacity_kW = v_groupATOFeedInCapacity_kW;
		f_EHubCapacityDataSets();
	}

}
else {
	uI_Results.c_individualGridConnections = new ArrayList<AreaCollection>();
	uI_Results.v_selectedObjectType = OL_GISObjectType.REGION;
}
uI_Results.f_showCorrectChart();
/*ALCODEEND*/}

double f_styleResultsUI()
{/*ALCODESTART::1736442051389*/
uI_Results.f_styleAllCharts(v_backgroundColor, lavender, 1.0, LINE_STYLE_SOLID);
/*ALCODEEND*/}

