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
if(settings.isPublicModel()){
	f_selectGISRegion_publicModel(clickx, clicky);
	return;
}

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
				return;
			}
		}
	}
}

//Still no clicked object? :select basic region
v_clickedObjectType = OL_GISObjectType.REGION;
uI_Results.f_updateResultsUI(energyModel);

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
			f_setColorsBasedOnConsumptionProfileHouseholds(gis_area);
		}
		else {
			f_setColorsBasedOnConsumption(gis_area);
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
		
		//Pas p_id aan naar een normale naam
		area.p_id = "Buitengebied";
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
gisregion.setVisible(v_HVMVNodeIsVisible);
/*ALCODEEND*/}

double f_setUITabs()
{/*ALCODESTART::1705925024602*/
// CHOOSE WHICH TABS YOU WANT TO BE ABLE TO SHOW FOR YOUR PROJECT 
// (OVERRIDE FUNCTION IN CHILD IF YOU WANT OTHER THAN DEFAULT)

// Adding the (child) tabs to the tabArea population

// If you use an extension of a tab, you must update the pointer to the instance of the interface
// Something like: tabElectricity.zero_Interface = loader_Project.zero_Interface;
// No update to the pointer is needed for the generic tabs

uI_Tabs.add_pop_tabElectricity();

uI_Tabs.add_pop_tabHeating();

uI_Tabs.add_pop_tabMobility();

uI_Tabs.add_pop_tabEHub();

// Group visibilities
// When using an extension of a generic tab don't forget to typecast it!
if (p_selectedProjectType == OL_ProjectType.RESIDENTIAL) {
	((tabElectricity)uI_Tabs.pop_tabElectricity.get(0)).getGroupElectricityDemandSliders_ResidentialArea().setVisible(true);
	((tabHeating)uI_Tabs.pop_tabHeating.get(0)).getGroupHeatDeandSliders_ResidentialArea().setVisible(true);
	((tabMobility)uI_Tabs.pop_tabMobility.get(0)).getGroupMobilityDemandSliders().setVisible(true);
	((tabEHub)uI_Tabs.pop_tabEHub.get(0)).getGroupHubSliders().setVisible(true);
}
else {
	((tabElectricity)uI_Tabs.pop_tabElectricity.get(0)).getGroupElectricityDemandSliders().setVisible(true);
	((tabHeating)uI_Tabs.pop_tabHeating.get(0)).getGroupHeatDemandSlidersCompanies().setVisible(true);
	((tabMobility)uI_Tabs.pop_tabMobility.get(0)).getGroupMobilityDemandSliders().setVisible(true);
	((tabEHub)uI_Tabs.pop_tabEHub.get(0)).getGroupHubSliders().setVisible(true);
}
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

uI_Results.f_updateUIresultsGridNode(GN);
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
	v_selectedGridConnectionIndex = 0;
	v_clickedObjectText = "Pand met " + b.c_containedGridConnections.size() + " adressen: " + b.p_id;
	gr_multipleBuildingInfo.setVisible(true);
}
else {
	String text = "";
	if (p_selectedProjectType == OL_ProjectType.BUSINESSPARK) {
		if (b instanceof GIS_Building) {
			if(b.c_containedGridConnections.get(0).p_owner.p_detailedCompany){
				text = b.c_containedGridConnections.get(0).p_owner.p_actorID + ", ";
			}
			else if(((GIS_Building)b).p_annotation != null){
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
gr_multipleBuildingInfo.setVisible(false);

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
		 v_previousClickedObjectType == OL_GISObjectType.CHARGER ||
		 v_previousClickedObjectType == OL_GISObjectType.PARKING){
	for(GIS_Object previousClickedObject: c_previousSelectedObjects){
		f_styleAreas(previousClickedObject);
	}
}

if(v_customEnergyCoop != null){
	energyModel.f_removeEnergyCoop(v_customEnergyCoop);
	v_customEnergyCoop = null;
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
Triple<Integer, Integer, Integer> triple = uI_Tabs.pop_tabMobility.get(0).f_calculateNumberOfGhostVehicles( new ArrayList<GridConnection>(energyModel.UtilityConnections.findAll( x -> true)) );
uI_Tabs.pop_tabMobility.get(0).v_totalNumberOfGhostVehicle_Cars = triple.getFirst();
uI_Tabs.pop_tabMobility.get(0).v_totalNumberOfGhostVehicle_Vans = triple.getSecond();
uI_Tabs.pop_tabMobility.get(0).v_totalNumberOfGhostVehicle_Trucks = triple.getThird();
//Get the ghost heating systems
Pair<Integer, Integer> pair = uI_Tabs.pop_tabHeating.get(0).f_calculateNumberOfGhostHeatingSystems( energyModel.UtilityConnections.findAll( x -> true) );
uI_Tabs.pop_tabHeating.get(0).v_totalNumberOfGhostHeatingSystems_ElectricHeatpumps = pair.getFirst();
uI_Tabs.pop_tabHeating.get(0).v_totalNumberOfGhostHeatingSystems_HybridHeatpumps = pair.getSecond();

/*ALCODEEND*/}

double f_connectResultsUI()
{/*ALCODESTART::1709716821854*/
//Style resultsUI
f_styleResultsUI();

//Set ResultsUI radiobutton setup
if(settings.resultsUIRadioButtonSetup() != null){
	uI_Results.v_selectedRadioButtonSetup = settings.resultsUIRadioButtonSetup();
}

//Connect resultsUI
uI_Results.f_initializeResultsUI();

/*ALCODEEND*/}

double f_resetSettings()
{/*ALCODESTART::1709718252272*/
if(!b_runningMainInterfaceScenarios){
	b_resultsUpToDate = false;
	gr_simulateYearScreenSmall.setVisible(true);
	
	// Switch to the live plots and do not allow the user to switch away from the live plot when the year is not yet simulated
	energyModel.f_updateActiveAssetsMetaData();
	f_enableLivePlotsOnly(uI_Results);
	
	
	//Set simulation and live graph for companyUIs as well!
	for(UI_company companyUI : c_companyUIs){
		if(companyUI.uI_Results.f_getSelectedObjectData() != null){
			f_enableLivePlotsOnly(companyUI.uI_Results);
		}
	}
	runSimulation();
}
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
List<GCHouse> houses = new ArrayList<GCHouse>(energyModel.Houses.findAll( x -> true));
List<GCHouse> housesWithoutPV = houses.stream().filter( gc -> !gc.v_liveAssetsMetaData.hasPV ).collect(Collectors.toList());
List<GCHouse> housesWithPV = new ArrayList<>(houses);
housesWithPV.removeAll(housesWithoutPV);

c_orderedPVSystemsHouses = new ArrayList<>(housesWithoutPV);
c_orderedPVSystemsHouses.addAll(housesWithPV);


List<GCUtility> companies = new ArrayList<GCUtility>(energyModel.UtilityConnections.findAll( x -> true));
List<GCUtility> companiesWithoutPV = companies.stream().filter( gc -> !gc.v_liveAssetsMetaData.hasPV ).collect(Collectors.toList());
List<GCUtility> companiesWithPV = companies.stream().filter( gc -> gc.v_liveAssetsMetaData.hasPV ).collect(Collectors.toList());
List<GCUtility> detailedCompaniesWithPV = companiesWithPV.stream().filter( gc -> gc.p_owner != null && gc.p_owner.p_detailedCompany ).collect(Collectors.toList());
List<GCUtility> genericCompaniesWithPV = new ArrayList<>(companiesWithPV);
genericCompaniesWithPV.removeAll(detailedCompaniesWithPV);

c_orderedPVSystemsCompanies = new ArrayList<>(companiesWithoutPV);
c_orderedPVSystemsCompanies.addAll(genericCompaniesWithPV);
c_orderedPVSystemsCompanies.addAll(detailedCompaniesWithPV);


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
List<GCHouse> houses = new ArrayList<GCHouse>(energyModel.Houses.findAll( x -> true));
List<GCHouse> housesWithoutHP = houses.stream().filter( gc -> gc.p_heatingType != OL_GridConnectionHeatingType.HEATPUMP_AIR ).collect(Collectors.toList());
List<GCHouse> housesWithHP = new ArrayList<>(houses);
housesWithHP.removeAll(housesWithoutHP);

c_orderedHeatingSystemsHouses = new ArrayList<>(housesWithoutHP);
c_orderedHeatingSystemsHouses.addAll(housesWithHP);


List<GCUtility> companies = new ArrayList<GCUtility>(energyModel.UtilityConnections.findAll( gc -> gc.p_heatingType != OL_GridConnectionHeatingType.NONE));
List<GCUtility> companiesWithoutHP = companies.stream().filter( gc -> gc.p_heatingType != OL_GridConnectionHeatingType.HEATPUMP_AIR).collect(Collectors.toList());
List<GCUtility> companiesWithHP = companies.stream().filter( gc -> gc.p_heatingType == OL_GridConnectionHeatingType.HEATPUMP_AIR ).collect(Collectors.toList());
List<GCUtility> detailedCompaniesWithHP = companiesWithHP.stream().filter( gc -> gc.p_owner != null && gc.p_owner.p_detailedCompany ).collect(Collectors.toList());
List<GCUtility> genericCompaniesWithHP = new ArrayList<>(companiesWithHP);
genericCompaniesWithHP.removeAll(detailedCompaniesWithHP);

c_orderedHeatingSystemsCompanies = new ArrayList<>(companiesWithoutHP);
c_orderedHeatingSystemsCompanies.addAll(genericCompaniesWithHP);
c_orderedHeatingSystemsCompanies.addAll(detailedCompaniesWithHP);


/*ALCODEEND*/}

double f_initalAssetOrdering()
{/*ALCODESTART::1714135623471*/
f_initialElectricVehiclesOrder();
f_initialPVSystemsOrder();
f_initialHeatingSystemsOrder();
f_initialParkingSpacesOrder();
f_initialHouseholdOrder();
f_initialChargerOrder();
f_projectSpecificOrderedCollectionAdjustments();


/*ALCODEEND*/}

double f_setColorsBasedOnConsumption(GIS_Object gis_area)
{/*ALCODESTART::1715116336665*/
if(gis_area.c_containedGridConnections.size() > 0){

	double yearlyEnergyConsumption = sum( gis_area.c_containedGridConnections, x -> x.v_rapidRunData.getTotalElectricityConsumed_MWh());
	
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
	if (gis_area.c_containedGridConnections.get(0).v_liveAssetsMetaData.hasPV) {
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

//Initialize the legend
f_initializeLegend();

//Disable cable button if no cables have been loaded in
if(c_LVCables.size() == 0 && c_MVCables.size() == 0){
	checkbox_cabels.setVisible(false);
}

//Disable summary button if summary is not selected
if(settings.showKPISummary() == null || !settings.showKPISummary()){
	uI_Results.getCheckbox_KPISummary().setVisible(false);
}

//Set order of certain layovers and submenus
presentation.remove(gr_sliderClickBlocker);
presentation.insert(presentation.size(), gr_sliderClickBlocker);
presentation.remove(gr_forceMapSelection);
presentation.insert(presentation.size(), gr_forceMapSelection);
presentation.remove(gr_filterInterface);
presentation.insert(presentation.size(), gr_filterInterface);

if(settings.isPublicModel()){
	f_changeDefaultColorOfPrivateGC();
}
/*ALCODEEND*/}

GISRegion f_createGISObject(double[] gisTokens)
{/*ALCODESTART::1715868403475*/
GISRegion gisregion = new GISRegion(map, gisTokens);
return gisregion;
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

double f_setColorsBasedOnConsumptionProfileHouseholds(GIS_Object gis_area)
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
	if (gis_area.c_containedGridConnections.get(0).v_liveAssetsMetaData.hasPV) {
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

// PV SYSTEMS:
//double PVsystems = count(energyModel.UtilityConnections, x->x.v_liveAssetsMetaData.hasPV == true && x.v_isActive);		
//int PV_pct = roundToInt(100 * PVsystems / count(energyModel.UtilityConnections, x->x.v_isActive));
Pair<Double, Double> pair = uI_Tabs.pop_tabElectricity.get(0).f_getPVSystemPercentage( new ArrayList<GridConnection>(findAll(energyModel.UtilityConnections, x -> x.v_isActive) ) );
int PV_pct = roundToInt(100.0 * pair.getFirst() / pair.getSecond());
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
uI_Results.f_updateResultsUI(c_selectedGridConnections.get(0));

//Set the UI button
f_setUIButton();
/*ALCODEEND*/}

double f_setColorsBasedOnGridTopology_objects(GIS_Object gis_area)
{/*ALCODESTART::1718566260603*/
if (gis_area.c_containedGridConnections.size() > 0) {
	Color c = gis_area.c_containedGridConnections.get(0).p_parentNodeElectric.p_uniqueColor;
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
for (GridConnection gc : c_selectedObjects.get(0).c_containedGridConnections) {
	if (!c_selectedGridConnections.contains(gc)) {
		c_selectedGridConnections.add(gc);
	}
}

if(c_selectedGridConnections.size()>1){
	v_customEnergyCoop = energyModel.f_addEnergyCoop(c_selectedGridConnections);
	uI_Results.f_updateResultsUI(v_customEnergyCoop);
}
else{
	uI_Results.f_updateResultsUI(c_selectedGridConnections.get(0));
}

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

/*
//Find all MVMV and HVMV distribution stations
List<GridNode> MVMVstations = findAll(energyModel.pop_gridNodes, GN -> GN.p_nodeType == OL_GridNodeType.MVMV);
List<GridNode> HVMVstations = findAll(energyModel.pop_gridNodes, GN -> GN.p_nodeType == OL_GridNodeType.HVMV);

//Set their topology colors (for now black as they are basically top level).
MVMVstations.forEach(GN -> GN.p_uniqueColor = semiTransparent(black));
HVMVstations.forEach(GN -> GN.p_uniqueColor = semiTransparent(black));
*/
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

GISRoute f_createGISLine(double[] GISCoords,OL_GISObjectType objectType)
{/*ALCODESTART::1725266804325*/

Curve curve = new Curve();
for(int i = 0; i+3 < GISCoords.length; i += 2){
	GISMarkupSegmentLine segment = new GISMarkupSegmentLine(GISCoords[i], GISCoords[i+1], GISCoords[i+2], GISCoords[i+3]);
	curve.addSegment(segment);
}

//Create route (line)
GISRoute gisroute = new GISRoute(map, curve, true);

//Object styling
switch(objectType){

	case LV_CABLE:
		c_LVCables.add(gisroute);
		//Styling
		gisroute.setLineStyle(LINE_STYLE_SOLID);
		gisroute.setLineWidth(0.8);
		gisroute.setLineColor(v_LVGridColor);
		gisroute.setVisible(false);
		break;
		
	case MV_CABLE:
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

double f_setColorsBasedOnGridTopology_gridnodes(GridNode GN)
{/*ALCODESTART::1725968656820*/
if(GN.gisRegion != null){
	GN.gisRegion.setFillColor(GN.p_uniqueColor);
	GN.gisRegion.setLineColor(GN.p_uniqueColor.brighter());
}
/*ALCODEEND*/}

double f_setSliderPresets()
{/*ALCODESTART::1725977409304*/
//Should be overridden in child interface!!!
f_updateMainInterfaceSliders();
f_initialisationOfResidentialSliders();
traceln("Forgot to override project specific slider settings!!");
/*ALCODEEND*/}

double f_projectSpecificStyling()
{/*ALCODESTART::1726068314849*/
//Function used to set the colors, styling, and other parameters/functions for each specific project
//Should be overridden!!
traceln("DID NOT OVERRIDE THE PROJECT SPECIFIC STYLING!");
/*ALCODEEND*/}

double f_projectSpecificOrderedCollectionAdjustments()
{/*ALCODESTART::1729685968993*/
//Function that can be used to make project specific adjustments to the ordered collection
//SHOULD BE OVERRIDEN IF YOU WANT TO USE THIS
/*ALCODEEND*/}

double f_applyFilter(OL_FilterOptionsGC selectedFilter,String selectedFilterName)
{/*ALCODESTART::1734442458629*/
c_selectedFilterOptions.add(selectedFilter);

ArrayList<GridConnection> toBeFilteredGC = new ArrayList<GridConnection>();

if(c_selectedFilterOptions.size()>1 && c_selectedGridConnections.size()> 0){ // Already filtering
	toBeFilteredGC = new ArrayList<GridConnection>(c_selectedGridConnections);
}
else{ // First filter
	toBeFilteredGC = new ArrayList<GridConnection>(energyModel.f_getActiveGridConnections());
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
		if(!c_filterSelectedGridLoops.isEmpty()){
			f_filterGridLoops(toBeFilteredGC);
		}
		else{
			f_setForcedClickScreen(true, "Selecteer een lus");
			
			if(c_selectedFilterOptions.size() > 1){
				rb_buildingColors.setValue(3,true);
				c_selectedGridConnections = new ArrayList<>(toBeFilteredGC);	
			}
			else{
				rb_buildingColors.setValue(3,true);
				filterCanReturnZero = true;
			}
		}
		break;
		
	case SELECTED_NEIGHBORHOOD:
		if(!c_filterSelectedNeighborhoods.isEmpty()){
			f_filterNeighborhoods(toBeFilteredGC);
		}
		else{
			f_setForcedClickScreen(true, "Selecteer een buurt");
			
			if(c_selectedFilterOptions.size() > 1){
				c_selectedGridConnections = new ArrayList<>(toBeFilteredGC);
			}
			else{
				filterCanReturnZero = true;
			}
		}
		break;
	case MANUAL_SELECTION:
		if(c_manualFilterSelectedGC.size() > 0){
			f_filterManualSelection(toBeFilteredGC);
		}
		else if(c_selectedFilterOptions.size() > 1){ 
			if(c_manualFilterDeselectedGC.size() > 0){
				f_filterManualSelection(toBeFilteredGC);
			}
			else{
				c_selectedGridConnections = new ArrayList<>(toBeFilteredGC);
			}
		}
		else{
			filterCanReturnZero = true;
		}
			
		break;
}

if(c_selectedGridConnections.size() == 0 && !filterCanReturnZero){ // Not allowed to return zero, while returning zero
	f_removeFilter(selectedFilter, selectedFilterName);
	
	//Notify filter has not been applied, cause no results are given
	f_setErrorScreen("Geselecteerde filter geeft geen resultaten. De filter is gedeactiveerd.");
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
	if(c_selectedGridConnections.size()>1){
		v_customEnergyCoop = energyModel.f_addEnergyCoop(c_selectedGridConnections);
		uI_Results.f_updateResultsUI(v_customEnergyCoop);
	}
	else{
		uI_Results.f_updateResultsUI(c_selectedGridConnections.get(0));
	}			
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
	case "In de aangewezen 'lus'":
		selectedFilter_OL = OL_FilterOptionsGC.GRIDTOPOLOGY_SELECTEDLOOP;
		break;
	case "In de aangwezen 'buurt'":
		selectedFilter_OL = OL_FilterOptionsGC.SELECTED_NEIGHBORHOOD;
		break;
	case "Handmatige selectie":
		selectedFilter_OL = OL_FilterOptionsGC.MANUAL_SELECTION;
		break;
}

boolean manualSelectionFilterActive = false;

//Remove manual filter first
if(!selectedFilterName.equals("Handmatige selectie") && c_selectedFilterOptions.contains(OL_FilterOptionsGC.MANUAL_SELECTION)){
	/*
	PrintStream originalPrintStream = f_disableTraceln();
	manualSelectionFilterActive = true;
	f_removeFilter(OL_FilterOptionsGC.MANUAL_SELECTION, "Handmatige selectie");
	f_enableTraceln(originalPrintStream);
	*/
	button_removeManualSelection.action();
	
}


if(!selectedFilterName.equals("-") && !c_selectedFilterOptions.contains(selectedFilter_OL)){ // Set filter
	traceln("Geselecteerde filter ( " + selectedFilterName + " ) toegevoegd.");
	t_activeFilters.setText( t_activeFilters.getText() + selectedFilterName + "\n");
	f_applyFilter(selectedFilter_OL, selectedFilterName);
}
else if(c_selectedFilterOptions.contains(selectedFilter_OL)){ // Remove filter
	f_removeFilter(selectedFilter_OL, selectedFilterName);
}

/*
//Reactivate manual filter at the end always (if it was active before)
if(manualSelectionFilterActive){
	t_activeFilters.setText( t_activeFilters.getText() + "Handmatige selectie" + "\n");
	f_applyFilter(OL_FilterOptionsGC.MANUAL_SELECTION, "Handmatige selectie");
}
*/

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



v_clickedObjectType = OL_GISObjectType.REGION;
uI_Results.f_updateResultsUI(energyModel);

//traceln("Alle filters zijn verwijderd.");
/*ALCODEEND*/}

double f_selectGridLoop(double clickx,double clicky)
{/*ALCODESTART::1734447122780*/

//Check if click was on Building, if yes, select grid building
for ( GIS_Building b : energyModel.pop_GIS_Buildings ){
	if( b.gisRegion != null && b.gisRegion.contains(clickx, clicky) ){
		if (b.gisRegion.isVisible()) { //only allow us to click on visible objects
			if (b.c_containedGridConnections.size() > 0 ) { // only allow buildings with gridconnections
				GridConnection clickedGridConnection = b.c_containedGridConnections.get(0); // Find buildings powered by the same GC as the clicked building
				GridNode clickedGridConnectionConnectedGridNode = clickedGridConnection.p_parentNodeElectric;
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
						clickedGridConnectionConnectedGridNode = clickedGridConnection.p_parentNodeElectric;
						break;
					}
				}	
				
				//This deselect the previous selection of gridloops
				f_setFilter("In de aangewezen 'lus'");
				
				if(c_filterSelectedGridLoops.contains(clickedGridConnectionConnectedGridNode)){
					c_filterSelectedGridLoops.remove(clickedGridConnectionConnectedGridNode);
				}
				else{
					c_filterSelectedGridLoops.add(clickedGridConnectionConnectedGridNode);
				}
			
				if(gr_forceMapSelection.isVisible()){
					f_setForcedClickScreen(false, "");
				}
				
				//This selects the new selection of gridloops
				f_setFilter("In de aangewezen 'lus'");
				
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
c_selectedGridConnections = new ArrayList<>(findAll(toBeFilteredGC, GC -> GC.v_liveAssetsMetaData.hasPV));

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

double f_filterGridLoops(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1734517589341*/
HashSet<GridConnection> gridConnectionsOnLoop = new HashSet<GridConnection>();

for(GridNode GridLoop : c_filterSelectedGridLoops)
	if(b_gridLoopsAreDefined){
		OL_GridNodeType loopTopNodeType= GridLoop.p_nodeType;
		switch(loopTopNodeType){
			case MVLV:
				for(GridConnection GC : GridLoop.f_getConnectedGridConnections()){
					if(toBeFilteredGC.contains(GC)){
						gridConnectionsOnLoop.add(GC);
					}
				}
				break;
			case SUBMV:
				for(GridConnection GC : GridLoop.f_getAllLowerLVLConnectedGridConnections()){
					if(toBeFilteredGC.contains(GC)){
						gridConnectionsOnLoop.add(GC);
					}
				}
				break;
			
			case MVMV:
				for(GridConnection GC : GridLoop.f_getConnectedGridConnections()){
					if(toBeFilteredGC.contains(GC)){
						gridConnectionsOnLoop.add(GC);
					}
				}
				
				break;
				
			case HVMV:
				for(GridConnection GC : GridLoop.f_getConnectedGridConnections()){
					if(toBeFilteredGC.contains(GC)){
						gridConnectionsOnLoop.add(GC);
					}
				}
				break;
		}
	}
	else{
		for(GridConnection GC : GridLoop.f_getAllLowerLVLConnectedGridConnections()){
			if(toBeFilteredGC.contains(GC)){
				gridConnectionsOnLoop.add(GC);
			}
		}
	}

c_selectedGridConnections = new ArrayList<>(gridConnectionsOnLoop);
/*ALCODEEND*/}

double f_setErrorScreen(String errorMessage)
{/*ALCODESTART::1736344958050*/
//Reset location and height
button_errorOK.setY(50);
rect_errorMessage.setY(-120);
rect_errorMessage.setHeight(200);
t_errorMessage.setY(-70);

//Set position above all other things
presentation.remove(gr_errorScreen);
presentation.insert(presentation.size(), gr_errorScreen);

int width_numberOfCharacters = 44;

// Set Text
Pair<String, Integer> p = v_infoText.restrictWidth(errorMessage, width_numberOfCharacters);
errorMessage = p.getFirst();
int numberOfLines = p.getSecond();
int additionalLines = max(0, numberOfLines - 3);

// Set Size
rect_errorMessage.setHeight(rect_errorMessage.getHeight() + additionalLines * 40);
rect_errorMessage.setY(rect_errorMessage.getY() - 40 * additionalLines);
//button_errorOK.setY(button_errorOK.getY() - 10 * additionalLines);
t_errorMessage.setY(t_errorMessage.getY() - 40 * additionalLines);

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
uI_Results.b_showGroupContractValues = false;
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
	int amountOfDataPoints = gc.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW().length;
	double[] quarterHourlyValues = Arrays.copyOf(gc.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW(), amountOfDataPoints);
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

double f_styleResultsUI()
{/*ALCODESTART::1736442051389*/
uI_Results.f_styleAllCharts(v_backgroundColor, lavender, 1.0, LINE_STYLE_SOLID);
/*ALCODEEND*/}

double f_selectNeighborhood(double clickx,double clicky)
{/*ALCODESTART::1737653178011*/

//Check if click was on Building, if yes, select grid building
for ( GIS_Object region : c_GISNeighborhoods ){
	if( region.gisRegion != null && region.gisRegion.contains(clickx, clicky) ){
		if (region.gisRegion.isVisible()) { //only allow us to click on visible objects	
				
			GIS_Object clickedNeighborhood = region;
			

			//This deselects the previous selected neighborhood filter
			f_setFilter("In de aangwezen 'buurt'");
			
			if(c_filterSelectedNeighborhoods.contains(clickedNeighborhood)){
				c_filterSelectedNeighborhoods.remove(clickedNeighborhood);
			}
			else{
				c_filterSelectedNeighborhoods.add(clickedNeighborhood);
			}

			if(gr_forceMapSelection.isVisible()){
				f_setForcedClickScreen(false, "");
			}
			//This sets the new selected neighborhoods filter
			f_setFilter("In de aangwezen 'buurt'");
			
			return;	
		}
	}
}

/*ALCODEEND*/}

double f_filterNeighborhoods(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1737653178013*/
ArrayList<GridConnection> gridConnectionsInNeighborhood = new ArrayList<GridConnection>();

for(GridConnection GC : toBeFilteredGC){
	for(GIS_Object nbh : c_filterSelectedNeighborhoods)
		if( nbh.gisRegion.contains(GC.p_latitude, GC.p_longitude) ){
			gridConnectionsInNeighborhood.add(GC);
		}
}

c_selectedGridConnections = new ArrayList<>(gridConnectionsInNeighborhood);
/*ALCODEEND*/}

double f_enableLivePlotsOnly(UI_Results resultsUI)
{/*ALCODESTART::1740043548084*/
if(resultsUI.getGr_resultsUIHeader().isVisible()){
	resultsUI.getRadioButtons().setValue(0, true);
}
resultsUI.chartProfielen.getPeriodRadioButton().setValue(0, true);
resultsUI.f_enableNonLivePlotRadioButtons(false);
/*ALCODEEND*/}

double f_filterManualSelection(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1742226689515*/
ArrayList<GridConnection> resultingGridConnectionSelection = new ArrayList<GridConnection>();

if(c_selectedFilterOptions.size() > 1){
	resultingGridConnectionSelection.addAll(toBeFilteredGC);
}
else{//Manual selection is the only active filter -> Resulting grid connection selection should start empty
}

for(GridConnection manualSelectedGC : c_manualFilterSelectedGC){
	if(!resultingGridConnectionSelection.contains(manualSelectedGC)){
		resultingGridConnectionSelection.add(manualSelectedGC);
	}
}
for(GridConnection manualDeselectedGC : c_manualFilterDeselectedGC){
	if(resultingGridConnectionSelection.contains(manualDeselectedGC)){
		resultingGridConnectionSelection.remove(manualDeselectedGC);
	}
}


c_selectedGridConnections = new ArrayList<>(resultingGridConnectionSelection);
/*ALCODEEND*/}

double f_selectManualFilteredGC(double clickx,double clicky)
{/*ALCODESTART::1742226787560*/
//Initialize clickedObject
GIS_Object clickedObject = null;

//Check if click was on Building, if yes, select building
for ( GIS_Object object : energyModel.pop_GIS_Buildings ){//pop_GIS_Buildings
	if( object.gisRegion != null && object.gisRegion.contains(clickx, clicky) ){
		if (object.gisRegion.isVisible()) { //only allow us to click on visible objects	
			if (object.c_containedGridConnections.size() > 0 ){
				clickedObject = object;
				break;
			}
		}
	}
}

//If click was not on a building, check if click was on an EA, if yes, select EA
if(clickedObject == null){
	for ( GIS_Object object : energyModel.pop_GIS_Objects ){//pop_GIS_Buildings
		if( object.gisRegion != null && object.gisRegion.contains(clickx, clicky) ){
			if (object.gisRegion.isVisible()) { //only allow us to click on visible objects	
				if (object.c_containedGridConnections.size() > 0 ){
					clickedObject = object;
					break;
				}
			}
		}
	}
}

//If a building or EA has been selected perform click functionality
if(clickedObject != null){
	boolean select = true; // Deselect == false;
	boolean removedFromSelectedGC = false;
	boolean removedFromDeselectedGC = false;

	ArrayList<GridConnection> clickedGridConnections = new ArrayList<GridConnection>(clickedObject.c_containedGridConnections);
	
	for (GridConnection clickedGC : clickedGridConnections){
		if(c_selectedGridConnections.contains(clickedGC)){
			c_selectedGridConnections.remove(clickedGC);
			select = false;
		}
		
		if(c_manualFilterSelectedGC.contains(clickedGC)){
			c_manualFilterSelectedGC.remove(clickedGC);
		}
		else if(c_manualFilterDeselectedGC.contains(clickedGC)){
			c_manualFilterDeselectedGC.remove(clickedGC);
		}
	}
	
	if(select){
		c_selectedGridConnections.addAll(clickedGridConnections);
		c_manualFilterSelectedGC.addAll(clickedGridConnections);
		traceln("Handmatig geselecteerd object toegevoegd aan selectie");
	}
	else{
		c_manualFilterDeselectedGC.addAll(clickedGridConnections);
		traceln("Handmatig geselecteerd object verwijderd van selectie");
	}
	
	
	//Disable traceln
	PrintStream originalPrintStream = f_disableTraceln();
	
	//This deactivates the previous selection
	f_setFilter("Handmatige selectie");
				
	//This activates the new selection
	f_setFilter("Handmatige selectie");
	
	//Enable traceln
	f_enableTraceln(originalPrintStream);
	
	return;
}
/*ALCODEEND*/}

double f_setForcedClickScreen(boolean showForcedClickScreen,String forcedClickScreenText)
{/*ALCODESTART::1742300624199*/
t_forcedClickMessage.setText(forcedClickScreenText);

gr_forceMapSelection.setVisible(showForcedClickScreen);
/*ALCODEEND*/}

double f_setMapViewBounds(ArrayList<GIS_Object> GISObjects)
{/*ALCODESTART::1743509491686*/
// Initialize min and max values
double minLat = Double.MAX_VALUE;
double maxLat = Double.MIN_VALUE;
double minLon = Double.MAX_VALUE;
double maxLon = Double.MIN_VALUE;
 
// Loop through all GISRegions and find the bounding box
for(GIS_Object go : GISObjects){
	
	GISRegion region = go.gisRegion;
    double[] points = region.getPoints(); // Get the boundary points of the region
 
    for (int i = 0; i < points.length; i += 2) { // i+=2 because data is in lat, lon pairs
        double lat = points[i];       // Latitude
        double lon = points[i + 1];   // Longitude
 
 
 
        // Update min/max latitude and longitude
        minLat = Math.min(minLat, lat);
        maxLat = Math.max(maxLat, lat);
        minLon = Math.min(minLon, lon);
        maxLon = Math.max(maxLon, lon);
    }
}

//Make it slightly bigger, so it isnt exact on the line of the regions
minLat = minLat - 0.0001;
maxLat = maxLat + 0.0001;
minLon = minLon - 0.0001;
maxLon = maxLon + 0.0001;
        
// Set the map to fit the calculated bounds
map.fitBounds(minLat, minLon, maxLat, maxLon);
/*ALCODEEND*/}

double f_setStartView()
{/*ALCODESTART::1743518032245*/
if(map_centre_latitude != null && map_centre_longitude != null && map_centre_latitude != 0 && map_centre_longitude != 0){
	map.setCenterLatitude(map_centre_latitude);
	map.setCenterLongitude(map_centre_longitude);
}
else{
	ArrayList<GIS_Object> gisObjects_for_mapViewBounds = new ArrayList<GIS_Object>();
	if(settings.subscopesToSimulate() == null || settings.subscopesToSimulate().size() == 0){
		gisObjects_for_mapViewBounds.addAll(findAll(energyModel.pop_GIS_Objects, gisObject -> gisObject.p_GISObjectType == OL_GISObjectType.REGION));
	}
	else{
		for (GIS_Object building : energyModel.pop_GIS_Buildings) {
			if(building.gisRegion.isVisible()){
				gisObjects_for_mapViewBounds.add(building);
			}
		}
	}
	f_setMapViewBounds(gisObjects_for_mapViewBounds);
}

if(map_scale != null){
	map.setMapScale(map_scale);
}

va_Interface.navigateTo();
/*ALCODEEND*/}

double f_setInfoText(ShapeImage infoBubble,String descriptionText,double xPosition,double yPosition)
{/*ALCODESTART::1743665953113*/
if ( p_currentActiveInfoBubble.size() > 0 && p_currentActiveInfoBubble.get(0) == infoBubble ) {
	// If we click a second time on the same bubble it should close the window
	p_currentActiveInfoBubble.clear();
	gr_infoText.setVisible(false);
}
else {
	p_currentActiveInfoBubble.clear();
	p_currentActiveInfoBubble.add(infoBubble);
	
	int width_ch = 50;
	// Set Text
	Pair<String, Integer> p = v_infoText.restrictWidth(descriptionText, width_ch);
	t_infoTextDescription.setText(p.getFirst());
	
	// Set Size
	rect_infoText.setWidth(width_ch * 7.5); // about 7.5 px per char for sans serif 14 pt
	rect_infoText.setHeight(50 + p.getSecond() * 20); // about 50 px for title and 20 px per line for sans serif 14 pt
	
	// Set Position
	// The group position is on the top left, not the centre.
	double margin_px = 15;
	//double posX = f_getAbsolutePosition(infoBubble).getX();
	//double posY = f_getAbsolutePosition(infoBubble).getY();
	if (xPosition < (va_Interface.getX() + va_Interface.getWidth()/2) ) {
		// bubble is on the left half, so text should appear to the right
		gr_infoText.setX( xPosition + margin_px + infoBubble.getWidth()/2);
	}
	else {
		// bubble is on the right half, so text should appear to the left
		gr_infoText.setX( xPosition - margin_px + infoBubble.getWidth()/2 - rect_infoText.getWidth());
	}
	
	// In AnyLogic the Y-Axis is inverted
	if (yPosition > (va_Interface.getY() + va_Interface.getHeight()/2) ) {
		// bubble is on the bottom half, so text should appear above
		gr_infoText.setY( yPosition - margin_px + infoBubble.getHeight()/2 - rect_infoText.getHeight());
	}
	else {
		// bubble is on the top half, so text should appear below
		gr_infoText.setY( yPosition + margin_px + infoBubble.getHeight()/2);
	}
	
	// Position of close button
	gr_closeInfoText.setX( width_ch * 7.5 - 20 ); // 20 px offset from the right hand side
	
	gr_infoText.setVisible(true);
}
/*ALCODEEND*/}

Pair<ShapeGroup, Point> f_getGroupPositionIteration(Pair<ShapeGroup, Point> pair)
{/*ALCODESTART::1744894817569*/
return new Pair(pair.getFirst().getGroup(), new Point(pair.getFirst().getX() + pair.getSecond().getX(), pair.getFirst().getY() + pair.getSecond().getY()));
/*ALCODEEND*/}

Point f_getAbsolutePosition(Shape shape)
{/*ALCODESTART::1744894817571*/
// Note: Only works if the Agent is not living in the space of the interface!

// Start with the shape position
Point point = new Point(shape.getX(), shape.getY());
traceln("point0: " + point);

// Find presentation the shape is in to get the offset.
if (shape.getPresentable() == this) {
	// The shape is on this canvas, no additional offset
}
else {
	// The shape is in a (possibly nested) presentation
	traceln("shape.getPresentable(): " + shape.getPresentable());
	traceln("shapetoplevel: " + shape.getPresentable().getPresentationShape());
	for (ShapeEmbeddedObjectPresentation ap : c_presentations) {
		traceln("AP: " + ap);
		traceln("AG: " + ap.getEmbeddedObject());
	}
	ShapeEmbeddedObjectPresentation presentation = findFirst(c_presentations, ap -> ap.getEmbeddedObject() == shape.getPresentable());
	if (presentation == null) {
		throw new RuntimeException("Shape not inside any presentation. Is the collection c_presentations filled with all agent presentations?");
	}
	traceln("point1: " + point);
	
	point.add( new Point(presentation.getX(), presentation.getY()) );
	// It is possible that the agent presentation is also inside a group. See AnyLogic update 8.9.2. We assume these are not in nested groups.
	traceln("point2: " + point);
	
	point.add( new Point(presentation.getGroup().getX(), presentation.getGroup().getY()) );
	
	traceln("point3: " + point);
	Pair<ShapeEmbeddedObjectPresentation, Point> pair = new Pair(presentation, point);
	while ( pair.getFirst().getPresentable() != this ) {
		pair = f_getPresentationPositionIteration(pair);
		traceln("pair: " + pair);
		traceln("point_i: " + pair.getSecond());
	}
	point = pair.getSecond();
}

// Recursively add the group offsets.
ShapeGroup group = shape.getGroup();
traceln("group x: " + group.getX());
traceln("group y: " + group.getY());
Pair<ShapeGroup, Point> pair = new Pair(group, point);
while ( !(pair.getFirst() instanceof ShapeTopLevelPresentationGroup) ) {
	pair = f_getGroupPositionIteration(pair);
	traceln("point_j: " + pair.getSecond());
}
return pair.getSecond();





/*
(main) tabs_presentation (tabs_presentation.getEmbeddedobject() = agent1)
	(agent 1) tab_elec_presentation  (tab_elec_presentation.getEmbeddedobject() = agent2)
		(agent 2) shape (shape.getpresentable() = agent2)


findfirst(c_presentations, ap -> ap.getEmbeddedObject() == shape.getPresentable() ) => tab_elec_presentation

tab_elec_presentation.getPresentable() => agent 1, so use this in the next iteration

findfirst(c_presentations, ap -> ap.getEmbeddedObject() == tab_elec_presentation.getPresentable() ) => tabs_presentation

*/


/*

double presentationOffsetX;
double presentationOffsetY;
if (shape.getPresentable() == this) {
	// The shape is on this canvas, no additional offset
	presentationOffsetX = 0.0;
	presentationOffsetY = 0.0;
}
else {
	traceln("getEmbeddedObject: " + c_presentations.get(0).getEmbeddedObject());
	traceln("getEmbeddedObject: " + c_presentations.get(1).getEmbeddedObject());
	traceln("shape.getPresentable()" + shape.getPresentable());
	traceln("agent presentable: " + agent.presentation);
	traceln("this presentable: " + this.presentation);
	ShapeEmbeddedObjectPresentation presentation = findFirst(c_presentations, ap -> ap.getEmbeddedObject() == shape.getPresentable());
	if (presentation == null) {
		throw new RuntimeException("Shape not inside any presentation. Is the collection c_presentations filled with all agent presentations?");
	}
	presentationOffsetX = presentation.getX();
	presentationOffsetY = presentation.getY();
	// It is possible that the agent presentation is also inside a group. See AnyLogic update 8.9.2. We assume these are not in nested groups.
	presentationOffsetX += presentation.getGroup().getX();
	presentationOffsetY += presentation.getGroup().getY();
	traceln("presentationOffsetX: " + presentationOffsetX);
}

// Add the presentation offset to the shape position and then recursively add the group offsets.
Point point = new Point(shape.getX() + presentationOffsetX, shape.getY() + presentationOffsetY);
ShapeGroup group = shape.getGroup();
Pair<ShapeGroup, Point> pair = new Pair(group, point);
while ( !(pair.getFirst() instanceof ShapeTopLevelPresentationGroup) ) {
	pair = f_getGroupPositionIteration(pair);
}
return pair.getSecond();

*/

/*ALCODEEND*/}

Pair<ShapeEmbeddedObjectPresentation, Point> f_getPresentationPositionIteration(Pair<ShapeEmbeddedObjectPresentation. Point> pair)
{/*ALCODESTART::1744894817573*/
ShapeEmbeddedObjectPresentation presentation = findFirst(c_presentations, ap -> ap.getEmbeddedObject() == pair.getFirst().getPresentable());
if (presentation == null) {
	throw new RuntimeException("Shape not inside any presentation. Is the collection c_presentations filled with all agent presentations?");
}
Point point = pair.getSecond();
traceln("presentation agent: " + presentation.getEmbeddedObject());
traceln("point in presentation iteration before: " + point);
point.add( new Point(presentation.getX(), presentation.getY()) );
// It is possible that the agent presentation is also inside a group. See AnyLogic update 8.9.2. We assume these are not in nested groups.
traceln("point in presentation iteration middle: " + point);
point.add( new Point(presentation.getGroup().getX(), presentation.getGroup().getY()) );
traceln("point in presentation iteration after: " + point);

return new Pair(presentation, point);
/*ALCODEEND*/}

double f_harvestEnergyModelLoadData()
{/*ALCODESTART::1744624088848*/
traceln("Start writing Electricity Load Balance data to excel!");

//Clear the sheet first
f_clearExportSheet();

//Set column names
excel_exportBalanceLoadData.setCellValue("Tijd [u]", "Electricity Load Balance", 1, 1);
excel_exportBalanceLoadData.setCellValue("Totale load van het Hele gebied [kWh]", "Electricity Load Balance", 1, 2);

//Get energyModel profile
double[] loadArray_kW = energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW();

for (int i = 0; i < loadArray_kW.length ; i++) {
	
	//Time series
	excel_exportBalanceLoadData.setCellValue((i) * energyModel.p_timeStep_h, "Electricity Load Balance", i+2, 1);

	//Data
	excel_exportBalanceLoadData.setCellValue( loadArray_kW[i] * energyModel.p_timeStep_h, "Electricity Load Balance", i+2, 2);
}

//Write file
excel_exportBalanceLoadData.writeFile();

traceln("Finished writing Electricity Load Balance data to excel!");
/*ALCODEEND*/}

double f_harvestSelectedGCLoadData()
{/*ALCODESTART::1744624088850*/
traceln("Start writing Electricity Load Balance data to excel!");

//Clear the sheet first
f_clearExportSheet();

//Initialize column index
int columnIndex = 2;

//Initialize total balance flow for all selected GC
double[] cumulativeLoadArray_kW = new double[energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW().length];

//Loop over gc and add the data
for(GridConnection GC : c_selectedGridConnections){

	//Add gc data
	excel_exportBalanceLoadData.setCellValue(GC.p_ownerID, "Electricity Load Balance", 1, columnIndex);
	
	double[] loadArray_kW = GC.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW();

	for (int i = 0; i < loadArray_kW.length; i++ ) {		
		excel_exportBalanceLoadData.setCellValue( loadArray_kW[i] * energyModel.p_timeStep_h, "Electricity Load Balance", i+2, columnIndex);
		
		//Add to cumulative load array
		cumulativeLoadArray_kW[i] += loadArray_kW[i];
	}
	
	//Add timestep column (only the first time)
	if (columnIndex == 2) {
		excel_exportBalanceLoadData.setCellValue("Tijd [u]", "Electricity Load Balance", 1, 1);
		traceln("ArraySize: %s", loadArray_kW.length);
		for (int i = 0; i < loadArray_kW.length ; i++) {
			excel_exportBalanceLoadData.setCellValue((i) * energyModel.p_timeStep_h, "Electricity Load Balance", i+2, 1);
		}
	}
	
	//Increase columnIndex
	columnIndex++;
}

//Cumulative data column
if(c_selectedGridConnections.size() > 1){
	excel_exportBalanceLoadData.setCellValue("Totale load [kWh]", "Electricity Load Balance", 1, columnIndex);
	for (int i = 0; i < cumulativeLoadArray_kW.length ; i++) {
		excel_exportBalanceLoadData.setCellValue( cumulativeLoadArray_kW[i] * energyModel.p_timeStep_h, "Electricity Load Balance", i+2, columnIndex);
	}
}

//Write the file
excel_exportBalanceLoadData.writeFile();

traceln("Finished writing Electricity Load Balance data to excel!");
/*ALCODEEND*/}

double f_harvestTotalBalanceLoadOfSelectedEnergyCoop()
{/*ALCODESTART::1744624088852*/
traceln("Start writing Electricity Load Balance data to excel!");

//Clear the sheet first
f_clearExportSheet();

//Set column names
excel_exportBalanceLoadData.setCellValue("Tijd [u]", "Electricity Load Balance", 1, 1);
excel_exportBalanceLoadData.setCellValue("Totale load van de geselecteerde EnergyCoop [kWh]", "Electricity Load Balance", 1, 2);

//Get energyModel profile
double[] loadArray_kW = v_customEnergyCoop.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW();

for (int i = 0; i < loadArray_kW.length ; i++) {
	
	//Time series
	excel_exportBalanceLoadData.setCellValue((i) * energyModel.p_timeStep_h, "Electricity Load Balance", i+2, 1);

	//Data
	excel_exportBalanceLoadData.setCellValue( loadArray_kW[i] * energyModel.p_timeStep_h, "Electricity Load Balance", i+2, 2);
}

//Write file
excel_exportBalanceLoadData.writeFile();

traceln("Finished writing Electricity Load Balance data to excel!");
/*ALCODEEND*/}

double f_setAllFileDownloadersDisabled()
{/*ALCODESTART::1744985599017*/
fileChooser_exportBalanceLoadEnergyModel.setEnabled(false);
fileChooser_exportBalanceLoadSelectedEnergyCoop.setEnabled(false);
fileChooser_exportBalanceLoadSelectedCompanies.setEnabled(false);
/*ALCODEEND*/}

double f_clearExportSheet()
{/*ALCODESTART::1744986150240*/
//Clear the sheet first
for (int row = 1; row <= 35137; row++) {
    for (int col = 1; col <= p_maxNrSelectedGCForExport + 2; col++) {
        excel_exportBalanceLoadData.setCellValue("", "Electricity Load Balance", row, col);
    }
}
/*ALCODEEND*/}

double f_selectGISRegion_publicModel(double clickx,double clicky)
{/*ALCODESTART::1745936595905*/
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
		if(GN.f_getAllLowerLVLConnectedGridConnections().size() >= p_minSelectedGCForPublicAggregation){
			f_selectGridNode(GN);
		}
		else{
			//Data sharing not agreed?
			v_clickedObjectType = OL_GISObjectType.REGION;
			uI_Results.f_updateResultsUI(energyModel);
			
			//Enable kpi summary button
			uI_Results.getCheckbox_KPISummary().setEnabled(true);
		}
		return;
	}
}

//Check if click was on Building, if yes, select grid building
for ( GIS_Building b : energyModel.pop_GIS_Buildings ){
	if( b.gisRegion != null && b.gisRegion.contains(clickx, clicky) ){
		if (b.gisRegion.isVisible()) { //only allow us to click on visible objects
			if (b.c_containedGridConnections.size() > 0 ) { // only allow buildings with gridconnections
				if(b.c_containedGridConnections.get(0).p_owner.b_dataSharingAgreed){
					buildingsConnectedToSelectedBuildingsList = b.c_containedGridConnections.get(0).c_connectedGISObjects; // Find buildings powered by the same GC as the clicked building
					f_selectBuilding(b, buildingsConnectedToSelectedBuildingsList);
				}
				else{
					//Data sharing not agreed?
					v_clickedObjectType = OL_GISObjectType.REGION;
					uI_Results.f_updateResultsUI(energyModel);
					
					//Enable kpi summary button
					uI_Results.getCheckbox_KPISummary().setEnabled(true);
				}
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
				if(GISobject.c_containedGridConnections.get(0).p_owner.b_dataSharingAgreed){
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
				}
				else{
					//Data sharing not agreed?
					v_clickedObjectType = OL_GISObjectType.REGION;
					uI_Results.f_updateResultsUI(energyModel);
					
					//Enable kpi summary button
					uI_Results.getCheckbox_KPISummary().setEnabled(true);
				}
				return;
			}
		}
	}
}

//Still no clicked object? :select basic region
v_clickedObjectType = OL_GISObjectType.REGION;
uI_Results.f_updateResultsUI(energyModel);

//Enable kpi summary button
uI_Results.getCheckbox_KPISummary().setEnabled(true);
/*ALCODEEND*/}

double f_changeDefaultColorOfPrivateGC()
{/*ALCODESTART::1746085650084*/
for(GIS_Object object : energyModel.pop_GIS_Objects){
	for(GridConnection GC : object.c_containedGridConnections){
		if(!GC.p_owner.b_dataSharingAgreed){
			object.p_defaultFillColor = transparent(object.p_defaultFillColor, 0.6);//v_dataSharingDisagreedColor;
			object.p_defaultLineStyle = LINE_STYLE_DOTTED;
			object.f_style(null, null, null, null);
			break;
		}
	}
}

for(GIS_Building building : energyModel.pop_GIS_Buildings){
	for(GridConnection GC : building.c_containedGridConnections){
		if(!GC.p_owner.b_dataSharingAgreed){
			building.p_defaultFillColor = transparent(building.p_defaultFillColor, 0.6);//v_dataSharingDisagreedColor;
			building.p_defaultLineStyle = LINE_STYLE_DOTTED;
			building.f_style(null, null, null, null);
			break;
		}
	}
}
/*ALCODEEND*/}

double f_initialParkingSpacesOrder()
{/*ALCODESTART::1749741185117*/
Collections.shuffle(c_orderedParkingSpaces);
/*ALCODEEND*/}

double f_initialChargerOrder()
{/*ALCODESTART::1750247111856*/
c_orderedV1GChargers = new ArrayList<J_EACharger>();
c_orderedV2GChargers = new ArrayList<J_EACharger>();
c_orderedPublicChargers = new ArrayList<GCPublicCharger>();

List<J_EACharger> c_inactiveV1GChargers = new ArrayList<J_EACharger>();
List<J_EACharger> c_inactiveV2GChargers = new ArrayList<J_EACharger>();

for (GridConnection gc : energyModel.f_getActiveGridConnections()) {
	for (J_EACharger charger : gc.c_chargers) {
		if (charger.V1GCapable) {
			c_orderedV1GChargers.add(0, charger);
		}
		else {
			c_orderedV1GChargers.add(charger);
		}
		if (charger.V2GCapable) {
			c_orderedV2GChargers.add(0, charger);
		}
		else {
			c_orderedV2GChargers.add(charger);
		}
	}
}

for (GridConnection gc : energyModel.f_getPausedGridConnections()) {
	for (J_EACharger charger : gc.c_chargers) {
		if (charger.V1GCapable) {
			c_inactiveV1GChargers.add(0, charger);
		}
		else {
			c_inactiveV1GChargers.add(charger);
		}
		if (charger.V2GCapable) {
			c_inactiveV2GChargers.add(0, charger);
		}
		else {
			c_inactiveV2GChargers.add(charger);
		}
	}
}

c_orderedV1GChargers.addAll( c_inactiveV1GChargers );
c_orderedV2GChargers.addAll( c_inactiveV2GChargers );

for (GCPublicCharger gc : energyModel.PublicChargers) {
	if ( !gc.p_isChargingCentre ) {
		c_orderedPublicChargers.add(gc);
	}
}

Collections.shuffle(c_orderedPublicChargers);
/*ALCODEEND*/}

double f_initializeSpecialGISObjectsLegend()
{/*ALCODESTART::1750078798174*/
int numberOfSpecialActiveGISObjectTypes = 0;

for(OL_GISObjectType activeSpecialGISObjectType : c_modelActiveSpecialGISObjects){
	if(activeSpecialGISObjectType == OL_GISObjectType.PARKING){
		for(OL_ParkingSpaceType activeParkingSpaceType : c_modelActiveParkingSpaceTypes){
			numberOfSpecialActiveGISObjectTypes ++;
			Pair<ShapeText, ShapeRectangle> legendShapes = f_getNextSpecialLegendShapes(numberOfSpecialActiveGISObjectTypes);
			f_setParkingSpaceLegendItem(activeParkingSpaceType, legendShapes.getFirst(), legendShapes.getSecond());
		}
	}
	else{
		numberOfSpecialActiveGISObjectTypes ++;
		Pair<ShapeText, ShapeRectangle> legendShapes = f_getNextSpecialLegendShapes(numberOfSpecialActiveGISObjectTypes);
		f_setSpecialGISObjectLegendItem(activeSpecialGISObjectType, legendShapes.getFirst(), legendShapes.getSecond());
	}
}
/*ALCODEEND*/}

double f_setTrafoText()
{/*ALCODESTART::1750261221085*/
if ( v_clickedGridNode.p_realCapacityAvailable ) {
	v_clickedObjectText = v_clickedGridNode.p_nodeType + "-station, " + Integer.toString( ((int)v_clickedGridNode.p_capacity_kW) ) + " kW, ID: " + v_clickedGridNode.p_gridNodeID + ", aansluitingen: " + v_clickedGridNode.f_getConnectedGridConnections().size() + ", Type station: " + v_clickedGridNode.p_description;
}
else {
	v_clickedObjectText =  v_clickedGridNode.p_nodeType + "-station, " + Integer.toString( ((int)v_clickedGridNode.p_capacity_kW) ) + " kW (ingeschat), ID: " + v_clickedGridNode.p_gridNodeID + ", aansluitingen: " + v_clickedGridNode.f_getConnectedGridConnections().size() + ", Type station: " + v_clickedGridNode.p_description;
}
/*ALCODEEND*/}

double f_setSpecialGISObjectLegendItem(OL_GISObjectType activeSpecialGISObjectType,ShapeText legendText,ShapeRectangle legendRect)
{/*ALCODESTART::1750079113839*/
legendText.setVisible(true);
legendRect.setVisible(true);

switch(activeSpecialGISObjectType){
	case SOLARFARM:
		legendText.setText("Zonneveld");
		legendRect.setFillColor(v_solarParkColor);
		legendRect.setLineColor(v_solarParkLineColor);
		break;
	case WINDFARM:
		legendText.setText("Windmolen");
		legendRect.setFillColor(v_windFarmColor);
		legendRect.setLineColor(v_windFarmLineColor);
		break;
	case CHARGER:
		legendText.setText("Laadpaal/plein");
		legendRect.setFillColor(v_chargingStationColor);
		legendRect.setLineColor(v_chargingStationLineColor);
		break;
	case BATTERY:	
		legendText.setText("Batterij");
		legendRect.setFillColor(v_batteryColor);
		legendRect.setLineColor(v_batteryLineColor);
		break;
	case PARCEL:
		legendText.setText("Nieuw Perceel");
		legendRect.setFillColor(v_parcelColor);
		legendRect.setLineColor(v_parcelLineColor);
		break;
	case ELECTROLYSER:
		legendText.setText("Electrolyser");
		legendRect.setFillColor(v_electrolyserColor);
		legendRect.setLineColor(v_electrolyserLineColor);
		break;
}
/*ALCODEEND*/}

double f_initializeLegend()
{/*ALCODESTART::1750080865693*/
//Default GIS buildings
f_initializeDefaultGISBuildingsLegend();

//Special gis objects
f_initializeSpecialGISObjectsLegend();
/*ALCODEEND*/}

double f_setParkingSpaceLegendItem(OL_ParkingSpaceType activeParkingSpaceType,ShapeText legendText,ShapeRectangle legendRect)
{/*ALCODESTART::1750089851073*/
legendText.setVisible(true);
legendRect.setVisible(true);

switch(activeParkingSpaceType){
	case PUBLIC:
		legendText.setText("Parkeerplek: publiek");
		legendRect.setFillColor(v_parkingSpaceColor_public);
		legendRect.setLineColor(v_parkingSpaceLineColor_public);
		break;
	case PRIVATE:
		legendText.setText("Parkeerplek: privé");
		legendRect.setFillColor(v_parkingSpaceColor_private);
		legendRect.setLineColor(v_parkingSpaceLineColor_private);
		break;
	case ELECTRIC:
		legendText.setText("Parkeerplek: electrisch");
		legendRect.setFillColor(v_parkingSpaceColor_electric);
		legendRect.setLineColor(v_parkingSpaceLineColor_electric);
		break;
}
/*ALCODEEND*/}

Pair<ShapeText, ShapeRectangle> f_getNextSpecialLegendShapes(int legendShapesNumber)
{/*ALCODESTART::1750092444018*/
ShapeText legendText;
ShapeRectangle legendRect;

switch(legendShapesNumber){
	case 1:
		legendText = t_specialGISObjectLegend1;
		legendRect = rect_specialGISObjectLegend1;
		break;
	case 2:
		legendText = t_specialGISObjectLegend2;
		legendRect = rect_specialGISObjectLegend2;
		break;
	case 3:
		legendText = t_specialGISObjectLegend3;
		legendRect = rect_specialGISObjectLegend3;
		break;
	case 4:
		legendText = t_specialGISObjectLegend4;
		legendRect = rect_specialGISObjectLegend4;
		break;
	case 5:
		legendText = t_specialGISObjectLegend5;
		legendRect = rect_specialGISObjectLegend5;
		break;
	case 6:
		legendText = t_specialGISObjectLegend6;
		legendRect = rect_specialGISObjectLegend6;
		break;
	case 7:
		legendText = t_specialGISObjectLegend7;
		legendRect = rect_specialGISObjectLegend7;
		break;
	case 8:
		legendText = t_specialGISObjectLegend8;
		legendRect = rect_specialGISObjectLegend8;
		break;
	case 9:
		legendText = t_specialGISObjectLegend9;
		legendRect = rect_specialGISObjectLegend9;
		break;
	case 10:
		legendText = t_specialGISObjectLegend10;
		legendRect = rect_specialGISObjectLegend10;
		break;
	case 11:
		legendText = t_specialGISObjectLegend11;
		legendRect = rect_specialGISObjectLegend11;
		break;
	case 12:
		legendText = t_specialGISObjectLegend12;
		legendRect = rect_specialGISObjectLegend12;
		break;
	default:
		legendText = t_specialGISObjectLegend1;
		legendRect = rect_specialGISObjectLegend1;
		break;
}

return new Pair(legendText, legendRect);

/*ALCODEEND*/}

double f_initializeDefaultGISBuildingsLegend()
{/*ALCODESTART::1750162397332*/
int numberOfDefaultActiveGISObjectTypes = 1;//Always start at 2 (1 ++) for the building types, cause 'selection' is always present (for now).

for(OL_GISBuildingTypes activeDefaultGISBuildingType : c_modelActiveDefaultGISBuildings){
	numberOfDefaultActiveGISObjectTypes ++;
	Pair<ShapeText, ShapeOval> legendShapes = f_getNextDefaultLegendShapes(numberOfDefaultActiveGISObjectTypes);
	f_setDefaultGISBuildingLegendItem(activeDefaultGISBuildingType, legendShapes.getFirst(), legendShapes.getSecond());
}
/*ALCODEEND*/}

Pair<ShapeText, ShapeOval> f_getNextDefaultLegendShapes(int legendShapesNumber)
{/*ALCODESTART::1750162514744*/
ShapeText legendText;
ShapeOval legendOval;

switch(legendShapesNumber){
	case 1:
		legendText = t_defaultLegend1;
		legendOval = oval_defaultLegend1;
		break;
	case 2:
		legendText = t_defaultLegend2;
		legendOval = oval_defaultLegend2;
		break;
	case 3:
		legendText = t_defaultLegend3;
		legendOval = oval_defaultLegend3;
		break;
	case 4:
		legendText = t_defaultLegend4;
		legendOval = oval_defaultLegend4;
		break;
	case 5:
		legendText = t_defaultLegend5;
		legendOval = oval_defaultLegend5;
		break;
	default:
		legendText = t_defaultLegend1;
		legendOval = oval_defaultLegend1;
}

return new Pair(legendText, legendOval);

/*ALCODEEND*/}

double f_setDefaultGISBuildingLegendItem(OL_GISBuildingTypes activeDefaultGISBuildingType,ShapeText legendText,ShapeOval legendOval)
{/*ALCODESTART::1750165143690*/
legendText.setVisible(true);
legendOval.setVisible(true);

switch(activeDefaultGISBuildingType){
	case DETAILED_COMPANY:
		legendText.setText("Gedetaileerd bedrijf: " + v_numberOfSurveyCompanies);
		legendOval.setFillColor(v_detailedCompanyBuildingColor);
		legendOval.setLineColor(v_detailedCompanyBuildingLineColor);
		break;
	case DEFAULT_COMPANY:
		legendText.setText("Standaard bedrijf");
		legendOval.setFillColor(v_companyBuildingColor);
		legendOval.setLineColor(v_companyBuildingLineColor);
		break;
	case HOUSE:
		legendText.setText("Huizen");
		legendOval.setFillColor(v_houseBuildingColor);
		legendOval.setLineColor(v_houseBuildingLineColor);
		break;
	case REMAINING:
		legendText.setText("Overige gebouwen");
		legendOval.setFillColor(v_restBuildingColor);
		legendOval.setLineColor(v_restBuildingLineColor);
		break;
}

/*ALCODEEND*/}

double f_initialHouseholdOrder()
{/*ALCODESTART::1750333147816*/
c_orderedActiveVehiclesPublicParking = new ArrayList<J_EADieselVehicle>();
c_orderedVehiclesPrivateParking = new ArrayList<J_EAVehicle>();

for (GCHouse house : energyModel.Houses) {
	if (!house.p_eigenOprit) {
		c_orderedActiveVehiclesPublicParking.addAll(house.c_dieselVehicles);
	}
	else {
		c_orderedVehiclesPrivateParking.addAll(house.c_vehicleAssets);
	}
}

Collections.shuffle(c_orderedActiveVehiclesPublicParking);
Collections.shuffle(c_orderedVehiclesPrivateParking);

/*ALCODEEND*/}

double f_initialisationOfResidentialSliders()
{/*ALCODESTART::1750340574107*/
int nbHouses = energyModel.Houses.size();
int nbHousesWithPV = count(energyModel.Houses, x -> x.v_liveAssetsMetaData.hasPV);
double pv_pct = 100.0 * nbHousesWithPV / nbHouses;
uI_Tabs.pop_tabElectricity.get(0).sl_householdPVResidentialArea_pct.setValue(pv_pct, false);

if ( nbHousesWithPV != 0 ) {
	int nbHousesWithHomeBattery = count(energyModel.Houses, x -> x.v_liveAssetsMetaData.hasPV && x.p_batteryAsset != null);
	double battery_pct = 100.0 * nbHousesWithHomeBattery / nbHousesWithPV;
	uI_Tabs.pop_tabElectricity.get(0).sl_householdBatteriesResidentialArea_pct.setValue(battery_pct, false);
}

int nbHousesWithElectricCooking = count(energyModel.Houses, x -> x.p_cookingMethod == OL_HouseholdCookingMethod.ELECTRIC);
double cooking_pct = 100.0 * nbHousesWithElectricCooking / nbHouses;
uI_Tabs.pop_tabElectricity.get(0).sl_householdElectricCookingResidentialArea_pct.setValue(cooking_pct, false);

if (c_orderedVehiclesPrivateParking.size() > 0) {
	int nbPrivateEVs = count(c_orderedVehiclesPrivateParking, x -> x instanceof J_EAEV);
	double privateEVs_pct = 100.0 * nbPrivateEVs / c_orderedVehiclesPrivateParking.size();
	uI_Tabs.pop_tabElectricity.get(0).sl_privateEVsResidentialArea_pct.setValue(privateEVs_pct, false);
}

if (c_orderedPublicChargers.size() > 0) {
	int nbPublicChargers = c_orderedPublicChargers.size();
	int nbActivePublicChargers = count(c_orderedPublicChargers, x -> x.v_isActive);
	double activePublicChargers_pct = 100.0 * nbActivePublicChargers / c_orderedPublicChargers.size();
	uI_Tabs.pop_tabElectricity.get(0).sl_publicChargersResidentialArea_pct.setValue(activePublicChargers_pct, false);
	if (c_orderedActiveVehiclesPublicParking.size() > 0) {
		// Put some of the diesel cars into the non active vehicles
		int nbCarsPerCharger = energyModel.avgc_data.p_avgEVsPerPublicCharger;
		List<J_EADieselVehicle> cars = new ArrayList<>(c_orderedActiveVehiclesPublicParking.subList(0, nbActivePublicChargers * nbCarsPerCharger));
		for (J_EADieselVehicle car : cars) {
			c_orderedActiveVehiclesPublicParking.remove(car);
			c_orderedNonActiveVehiclesPublicParking.add(0, car);
			car.removeEnergyAsset();
		}
	}
	int nbV1GChargers = count(c_orderedV1GChargers, x -> x.V1GCapable);
	int nbV2GChargers =count(c_orderedV2GChargers, x -> x.V2GCapable);
	double V1G_pct = 100.0 * nbV1GChargers / nbPublicChargers;
	double V2G_pct = 100.0 * nbV2GChargers / nbPublicChargers;
	uI_Tabs.pop_tabElectricity.get(0).sl_chargersThatSupportV1G_pct.setValue(V1G_pct, false);
	uI_Tabs.pop_tabElectricity.get(0).sl_chargersThatSupportV2G_pct.setValue(V2G_pct, false);
}


double averageNeighbourhoodBatterySize_kWh = 0;
for (GCGridBattery gc : energyModel.GridBatteries) {
	averageNeighbourhoodBatterySize_kWh += gc.p_batteryAsset.getStorageCapacity_kWh();
}
averageNeighbourhoodBatterySize_kWh /= energyModel.GridBatteries.size();
uI_Tabs.pop_tabElectricity.get(0).sl_gridBatteriesResidentialArea_kWh.setValue(averageNeighbourhoodBatterySize_kWh, false);

/*ALCODEEND*/}

