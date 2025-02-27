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

uI_Results.f_updateUIresultsGridNode(uI_Results.v_trafo, GN);
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
	uI_Results.v_selectedRadioButtonSetup = settings.resultsUIRadioButtonSetup();
}

//Connect resultsUI
uI_Results.f_initializeResultsUI();

/*ALCODEEND*/}

double f_resetSettings()
{/*ALCODESTART::1709718252272*/
b_resultsUpToDate = false;
gr_simulateYearScreenSmall.setVisible(true);

// Switch to the live plots and do not allow the user to switch away from the live plot when the year is not yet simulated
f_enableLivePlotsOnly(uI_Results);
uI_Results.f_updateActiveAssetBooleans(c_selectedGridConnections);

//Set simulation and live graph for companyUIs as well!
for(UI_company companyUI : c_companyUIs){
	if(companyUI.uI_Results.v_gridConnection != null){
		f_enableLivePlotsOnly(companyUI.uI_Results);
	}
}

runSimulation();

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
uI_Results.f_updateUIresultsGridConnection(uI_Results.v_gridConnection, c_selectedGridConnections);

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

if(c_selectedGridConnections.size()>1){
	v_customEnergyCoop = energyModel.f_addEnergyCoop(c_selectedGridConnections);
	uI_Results.v_selectedObjectType = OL_GISObjectType.COOP;
	uI_Results.f_updateUIresultsEnergyCoop(uI_Results.v_energyCoop, v_customEnergyCoop);
}
else{
	uI_Results.v_selectedObjectType = OL_GISObjectType.BUILDING;
	uI_Results.f_updateUIresultsGridConnection(uI_Results.v_gridConnection, c_selectedGridConnections);
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

//Find all MVMV and HVMV distribution stations
//List<GridNode> MVMVstations = findAll(energyModel.pop_gridNodes, GN -> GN.p_nodeType == OL_GridNodeType.MVMV);
List<GridNode> HVMVstations = findAll(energyModel.pop_gridNodes, GN -> GN.p_nodeType == OL_GridNodeType.HVMV);

//Set their topology colors (for now black as they are basically top level).
//MVMVstations.forEach(GN -> GN.p_uniqueColor = semiTransparent(black));
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
		
	case SELECTED_NEIGHBORHOOD:
		if(v_selectedNeighborhood != null){
			f_filterNeighborhood(toBeFilteredGC);
		}
		else if(c_selectedFilterOptions.size() > 1){
			c_selectedGridConnections = new ArrayList<>(toBeFilteredGC);
		}
		else{
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
	if(c_selectedGridConnections.size()>1){
		v_customEnergyCoop = energyModel.f_addEnergyCoop(c_selectedGridConnections);
		uI_Results.v_selectedObjectType = OL_GISObjectType.COOP;
		uI_Results.f_updateUIresultsEnergyCoop(uI_Results.v_energyCoop, v_customEnergyCoop);
		traceln("COOP created in filter");
		traceln(v_customEnergyCoop);
	}
	else{
		uI_Results.v_selectedObjectType = OL_GISObjectType.BUILDING;
		uI_Results.f_updateUIresultsGridConnection(uI_Results.v_gridConnection, c_selectedGridConnections);
	}			
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
	case "In de aangewezen 'lus'":
		selectedFilter_OL = OL_FilterOptionsGC.GRIDTOPOLOGY_SELECTEDLOOP;
		break;
	case "In de aangwezen 'buurt'":
		selectedFilter_OL = OL_FilterOptionsGC.SELECTED_NEIGHBORHOOD;
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
v_selectedNeighborhood = null;

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
				f_setFilter("In de aangewezen 'lus'");
				
				//This selects the new selected ring
				v_selectedGridLoop = clickedGridConnectionConnectedGridNode;
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
	uI_Results.f_updateUIresultsGridConnection(uI_Results.v_gridConnection, c_selectedGridConnections);
	uI_Results.v_selectedObjectType = OL_GISObjectType.BUILDING;
	if (p_selectedProjectType == OL_ProjectType.BUSINESSPARK) {
		uI_Results.f_fillAreaCollectionsOfIndividualGCs(c_selectedGridConnections);
		// update in the resultsUI the plots with total connection capacity with the group capacity
		f_calculateGroupATOConnectionCapacity(c_selectedGridConnections);
		uI_Results.b_EHubConfiguration = true;
		uI_Results.v_groupATODeliveryCapacity_kW = v_groupATODeliveryCapacity_kW;
		uI_Results.v_groupATOFeedInCapacity_kW = v_groupATOFeedInCapacity_kW;
		uI_Results.f_EHubCapacityDataSets(c_selectedGridConnections);
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

double f_selectNeighborhood(double clickx,double clicky)
{/*ALCODESTART::1737653178011*/

//Check if click was on Building, if yes, select grid building
for ( GIS_Object region : c_GISNeighborhoods ){
	if( region.gisRegion != null && region.gisRegion.contains(clickx, clicky) ){
		if (region.gisRegion.isVisible()) { //only allow us to click on visible objects	
				
			GIS_Object clickedNeighborhood = region;
			
			//This deselect the previous selected neighborhood
			f_setFilter("In de aangwezen 'buurt'");
			
			//This selects the new selected neighborhood
			v_selectedNeighborhood = clickedNeighborhood;
			f_setFilter("In de aangwezen 'buurt'");
			
			return;	
		}
	}
}

/*ALCODEEND*/}

double f_filterNeighborhood(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1737653178013*/
ArrayList<GridConnection> gridConnectionsInNeighborhood = new ArrayList<GridConnection>();

for(GridConnection GC : toBeFilteredGC){
	if( v_selectedNeighborhood.gisRegion.contains(GC.p_latitude, GC.p_longitude) ){
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
resultsUI.f_setNonLivePlotRadioButtons(false);
/*ALCODEEND*/}

