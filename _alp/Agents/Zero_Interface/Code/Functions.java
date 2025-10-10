double f_setColorsBasedOnEnergyLabels(GIS_Object b)
{/*ALCODESTART::1696837759924*/
if (b.gisRegion != null){

	OL_GridConnectionIsolationLabel buildingLowestEnergyLabel = OL_GridConnectionIsolationLabel.NONE;
	
	//Find the lowest energy label in the building
	for(GridConnection GC : b.c_containedGridConnections){
		switch(GC.p_energyLabel){
			case A:
				if(buildingLowestEnergyLabel == OL_GridConnectionIsolationLabel.NONE){
					buildingLowestEnergyLabel = OL_GridConnectionIsolationLabel.A;
				}
			break;
			case B:
				if(buildingLowestEnergyLabel == OL_GridConnectionIsolationLabel.NONE || buildingLowestEnergyLabel == OL_GridConnectionIsolationLabel.A){
					buildingLowestEnergyLabel = OL_GridConnectionIsolationLabel.B;
				}
			break;
			case C:
				if(buildingLowestEnergyLabel == OL_GridConnectionIsolationLabel.NONE || buildingLowestEnergyLabel == OL_GridConnectionIsolationLabel.B
				   || buildingLowestEnergyLabel == OL_GridConnectionIsolationLabel.C){
					buildingLowestEnergyLabel = OL_GridConnectionIsolationLabel.C;
				}
			break;
			case D:
				if(buildingLowestEnergyLabel != OL_GridConnectionIsolationLabel.E || buildingLowestEnergyLabel != OL_GridConnectionIsolationLabel.F
				   || buildingLowestEnergyLabel != OL_GridConnectionIsolationLabel.G){
					buildingLowestEnergyLabel = OL_GridConnectionIsolationLabel.D;
				}
			break;
			case E:
				if(buildingLowestEnergyLabel != OL_GridConnectionIsolationLabel.F || buildingLowestEnergyLabel != OL_GridConnectionIsolationLabel.G){
					buildingLowestEnergyLabel = OL_GridConnectionIsolationLabel.E;
				}
			break;
			case F:
				if(buildingLowestEnergyLabel != OL_GridConnectionIsolationLabel.G){
					buildingLowestEnergyLabel = OL_GridConnectionIsolationLabel.F;
				}
			break;
			case G:
				buildingLowestEnergyLabel = OL_GridConnectionIsolationLabel.G;
			break;								
		}
	}
	
	//Color building based on lowest energy label
	switch(buildingLowestEnergyLabel){
	
		case A:
			b.gisRegion.setFillColor(v_energyLabelAColor);
		break;
		case B:
			b.gisRegion.setFillColor(v_energyLabelBColor);
		break;
		case C:
			b.gisRegion.setFillColor(v_energyLabelCColor);
		break;
		case D:
			b.gisRegion.setFillColor(v_energyLabelDColor);
		break;
		case E:
			b.gisRegion.setFillColor(v_energyLabelEColor);
		break;
		case F:
			b.gisRegion.setFillColor(v_energyLabelFColor);
		break;
		case G:
			b.gisRegion.setFillColor(v_energyLabelGColor);
		break;
		case NONE:
			b.gisRegion.setFillColor(v_energyLabelUnknownColor);
		break;
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

//Get selected map overlay type, based on loaded order of the radio buttons
OL_MapOverlayTypes selectedMapOverlayType;
if(rb_mapOverlay != null){
	selectedMapOverlayType = c_loadedMapOverlayTypes.get(rb_mapOverlay.getValue());
}
else{
	selectedMapOverlayType = OL_MapOverlayTypes.DEFAULT;
}
//Set the correct map overlay
switch(selectedMapOverlayType){
	case DEFAULT:
		gis_area.f_style(null, null, null, null);
		break;
	case ELECTRICITY_CONSUMPTION:
		if (project_data.project_type() == OL_ProjectType.RESIDENTIAL) {
			f_setColorsBasedOnConsumptionProfileHouseholds(gis_area);
		}
		else {
			f_setColorsBasedOnElectricityConsumption(gis_area);
		}
		break;
	case PV_PRODUCTION:
		f_setColorsBasedOnProduction(gis_area);
		break;
	case GRID_NEIGHBOURS:
		f_setColorsBasedOnGridTopology_objects(gis_area);
		break;
	case CONGESTION:
		f_setColorsBasedOnCongestion_objects(gis_area);
		break;
	case ENERGY_LABEL:
		f_setColorsBasedOnEnergyLabels(gis_area);
		break;
	case PARKING_TYPE:
		f_setColorsBasedOnParkingType_objects(gis_area);
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
//Get selected map overlay type, based on loaded order of the radio buttons
OL_MapOverlayTypes selectedMapOverlayType;
if(rb_mapOverlay != null){
	selectedMapOverlayType = c_loadedMapOverlayTypes.get(rb_mapOverlay.getValue());
}
else{
	selectedMapOverlayType = OL_MapOverlayTypes.DEFAULT;
}
//Set the correct map overlay
switch(selectedMapOverlayType){
	case DEFAULT:
	case ELECTRICITY_CONSUMPTION:
	case PV_PRODUCTION:
		if(!b_updateLiveCongestionColors){
			switch( GN.p_nodeType ) {
				case LVLV:
					f_styleLVLV(GN.gisRegion);
					break;
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
		}
		else{
			f_setColorsBasedOnCongestion_gridnodes(GN, false);
		}
		break;
	case GRID_NEIGHBOURS:
		f_setColorsBasedOnGridTopology_gridnodes(GN);
		break;
	case CONGESTION:
		f_setColorsBasedOnCongestion_gridnodes(GN, false);
		break;
	case PARKING_TYPE:
		f_setColorsBasedOnParkingType_gridnodes(GN);
		break;
}
/*ALCODEEND*/}

double f_styleMVLV(GISRegion gisregion)
{/*ALCODESTART::1705505495599*/
gisregion.setLineStyle( LINE_STYLE_SOLID );
gisregion.setLineColor( v_MVLVLineColor );
gisregion.setLineWidth(2);		
gisregion.setFillColor(v_MVLVNodeColor);
/*ALCODEEND*/}

double f_styleHVMV(GISRegion gisregion)
{/*ALCODESTART::1705505509120*/
gisregion.setFillColor(v_HVMVNodeColor);
gisregion.setLineStyle( LINE_STYLE_SOLID );
gisregion.setLineColor( v_HVMVLineColor );
gisregion.setLineWidth(2);
gisregion.setVisible(v_HVMVNodeIsVisible);
/*ALCODEEND*/}

double f_setUITabs()
{/*ALCODESTART::1705925024602*/
//Create the tabs for the project
f_createUITabs_default();

//Initialize the uI_Tabs with the gridconnections
uI_Tabs.f_initializeUI_Tabs(energyModel.f_getGridConnectionsCollectionPointer(), energyModel.f_getPausedGridConnectionsCollectionPointer());

//Initialize sliders with certain presets
f_setSliderPresets();

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

//Check the number of GCs in building
v_nbGridConnectionsInSelectedBuilding = b.c_containedGridConnections.size();

//Multiple GC in building: set additional adress in building info + buttons
if ( v_nbGridConnectionsInSelectedBuilding > 1 ){
	v_selectedGridConnectionIndex = 0;
	v_clickedObjectText = "Pand met " + b.c_containedGridConnections.size() + " adressen: " + b.p_id;
	gr_multipleBuildingInfo.setVisible(true);
}
else {
	String text = "";
	if (project_data.project_type() == OL_ProjectType.BUSINESSPARK) {
		if (b instanceof GIS_Building) {
			if(b.c_containedGridConnections.get(0).p_owner.p_detailedCompany){
				text = b.c_containedGridConnections.get(0).p_owner.p_actorID + ", ";
			}
			else if(b.p_annotation != null){
				text = b.p_annotation + ", ";
			}
		}
		else {
			text = b.p_id + ", ";
		}
	}
	else{
		if(b.p_annotation != null){
			text = b.p_annotation + ", ";
		}
		else{
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
	c_UIResultsInstances.add(companyUI.uI_Results);
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
		companyUI.c_scenarioSettings_Current.add(c_scenarioMap_Current.get(GC.p_uid));
		companyUI.c_scenarioSettings_Future.add(c_scenarioMap_Future.get(GC.p_uid));
		
		//Initialize additional vehicles collection for each GC
		companyUI.c_additionalVehicles.put(GC.p_uid, new ArrayList<J_EAVehicle>());
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
/*ALCODEEND*/}

double f_connectResultsUI()
{/*ALCODESTART::1709716821854*/
//Style resultsUI
f_styleResultsUI();

//Set ResultsUI radiobutton setup
if(settings.resultsUIRadioButtonSetup() != null){
	uI_Results.v_selectedRadioButtonSetup = settings.resultsUIRadioButtonSetup();
}

//Disable summary button if summary is not selected
if(settings.showKPISummary() == null || !settings.showKPISummary()){
	uI_Results.getCheckbox_KPISummary().setVisible(false);
}

//Connect resultsUI
uI_Results.f_initializeResultsUI();

c_UIResultsInstances.add(uI_Results);
/*ALCODEEND*/}

double f_resetSettings()
{/*ALCODESTART::1709718252272*/
if(!b_runningMainInterfaceScenarios){
	b_resultsUpToDate = false;

	// Update asset flow categories of all agents
	energyModel.f_updateActiveAssetsMetaData();
	
	// Switch to the live plots and do not allow the user to switch away from the live plot when the year is not yet simulated	
	for (UI_Results ui_results : c_UIResultsInstances) {
		if (ui_results.f_getSelectedObjectData() != null) {	
			f_enableLivePlotsOnly(ui_results);
		}
	}
	
	// On all screens cover the resultsUI Buttons with the simulate year button
	f_setAllSimulateYearScreens();
	
	runSimulation();
}
/*ALCODEEND*/}

double f_initialPVSystemsOrder()
{/*ALCODESTART::1714130288661*/
List<GCHouse> houses = new ArrayList<GCHouse>(energyModel.Houses.findAll( x -> true));
List<GCHouse> housesWithoutPV = houses.stream().filter( gc -> !gc.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW) ).collect(Collectors.toList());
List<GCHouse> housesWithPV = new ArrayList<>(houses);
housesWithPV.removeAll(housesWithoutPV);

c_orderedPVSystemsHouses = new ArrayList<>(housesWithoutPV);
c_orderedPVSystemsHouses.addAll(housesWithPV);


List<GCUtility> companies = new ArrayList<GCUtility>(energyModel.UtilityConnections.findAll( x -> true));
List<GCUtility> companiesWithoutPV = companies.stream().filter( gc -> !gc.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW) ).collect(Collectors.toList());
List<GCUtility> companiesWithPV = companies.stream().filter( gc -> gc.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW) ).collect(Collectors.toList());
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
List<GCHouse> housesWithoutHP = houses.stream().filter( gc -> gc.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP ).collect(Collectors.toList());
List<GCHouse> housesWithHP = new ArrayList<>(houses);
housesWithHP.removeAll(housesWithoutHP);

c_orderedHeatingSystemsHouses = new ArrayList<>(housesWithoutHP);
c_orderedHeatingSystemsHouses.addAll(housesWithHP);

List<GCUtility> companies = new ArrayList<GCUtility>(energyModel.UtilityConnections.findAll( gc -> gc.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.NONE && gc.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.CUSTOM));
List<GCUtility> companiesWithoutHP = companies.stream().filter( gc -> gc.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP).collect(Collectors.toList());
List<GCUtility> companiesWithHP = companies.stream().filter( gc -> gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP ).collect(Collectors.toList());
List<GCUtility> detailedCompaniesWithHP = companiesWithHP.stream().filter( gc -> gc.p_owner != null && gc.p_owner.p_detailedCompany ).collect(Collectors.toList());
List<GCUtility> genericCompaniesWithHP = new ArrayList<>(companiesWithHP);
genericCompaniesWithHP.removeAll(detailedCompaniesWithHP);

c_orderedHeatingSystemsCompanies = new ArrayList<>(companiesWithoutHP);
c_orderedHeatingSystemsCompanies.addAll(genericCompaniesWithHP);
c_orderedHeatingSystemsCompanies.addAll(detailedCompaniesWithHP);


/*ALCODEEND*/}

double f_initialAssetOrdering()
{/*ALCODESTART::1714135623471*/
f_initialElectricVehiclesOrder();
f_initialPVSystemsOrder();
f_initialPTSystemsOrder_households();
f_initialHeatingSystemsOrder();
f_initialParkingSpacesOrder();
f_initialChargerOrder();
f_initializePrivateAndPublicParkingCarsOrder();
f_projectSpecificOrderedCollectionAdjustments();


/*ALCODEEND*/}

double f_setColorsBasedOnElectricityConsumption(GIS_Object gis_area)
{/*ALCODESTART::1715116336665*/
if(gis_area.c_containedGridConnections.size() > 0){

	double yearlyEnergyConsumption = sum( gis_area.c_containedGridConnections, x -> x.v_rapidRunData.getTotalElectricityConsumed_MWh());
	
	if ( yearlyEnergyConsumption < 10){ gis_area.f_style( rect_mapOverlayLegend_ElectricityConsumption1.getFillColor(), null, null, null);}
	else if ( yearlyEnergyConsumption < 50){ gis_area.f_style( rect_mapOverlayLegend_ElectricityConsumption2.getFillColor(), null, null, null);}
	else if ( yearlyEnergyConsumption < 150){ gis_area.f_style( rect_mapOverlayLegend_ElectricityConsumption3.getFillColor(), null, null, null);}
	else if ( yearlyEnergyConsumption < 500){ gis_area.f_style( rect_mapOverlayLegend_ElectricityConsumption4.getFillColor(), null, null, null);}
	else if ( yearlyEnergyConsumption > 500){ gis_area.f_style( rect_mapOverlayLegend_ElectricityConsumption5.getFillColor(), null, null, null);}
}
/*ALCODEEND*/}

double f_setColorsBasedOnProduction(GIS_Object gis_area)
{/*ALCODESTART::1715118739710*/
if (gis_area.c_containedGridConnections.size() > 0) {
	
	//Define medium PV Value
	double mediumPVValue_kWp = 100;
	if (project_data.project_type() == OL_ProjectType.RESIDENTIAL){
		mediumPVValue_kWp = 5;
	}
	
	//Calculate total pv capacity on the gis object
	double totalPVCapacity_kWp = 0;
	for(GridConnection GC : gis_area.c_containedGridConnections){
		totalPVCapacity_kWp += GC.v_liveAssetsMetaData.totalInstalledPVPower_kW;
	}
	
	//Set color of object based on total pv capacity
	if(totalPVCapacity_kWp == 0){
		gis_area.f_style(rect_mapOverlayLegend_PVProduction1.getFillColor(), null, null, null);
	}
	else if (totalPVCapacity_kWp < mediumPVValue_kWp){
		gis_area.f_style(rect_mapOverlayLegend_PVProduction2.getFillColor(), null, null, null);
	}
	else{
		gis_area.f_style(rect_mapOverlayLegend_PVProduction3.getFillColor(), null, null, null);
	}
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

//Project specific settings
f_projectSpecificSettings();

// Initialize the slider Asset Ordering lists
f_initialAssetOrdering();

//Connect the results UI
f_connectResultsUI();

//Initialize the UITabs
f_setUITabs();

//Initialize the legend
f_initializeLegend();

//Initialize map overlay buttons
f_initializeMapOverlayRadioButton();

// Create the Private UI for companies
f_createAdditionalUIs();
button_goToUI.setVisible(false);

//Create and set the grid topology colors (Netvlakken)
f_setGridTopologyColors();

//Disable cable button if no cables have been loaded in
if(c_LVCables.size() == 0 && c_MVCables.size() == 0){
	checkbox_cabels.setVisible(false);
}

//Set order of certain layovers and submenus
f_initializePresentationOrder();

//Set to public model version styling if activated
if(settings.isPublicModel()){
	f_changeDefaultColorOfPrivateGC();
}

//Turn on update of live congestion colloring
b_updateLiveCongestionColors = true;

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
	if(gc.v_rapidRunData != null){
		yearlyEnergyConsumption += gc.v_rapidRunData.getTotalElectricityConsumed_MWh();
	}
	else{
		for ( J_EAConsumption consumptionAsset : gc.c_consumptionAssets){
			if( consumptionAsset.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND ){
				yearlyEnergyConsumption += consumptionAsset.yearlyDemand_kWh;
			}
		}
	}
}

if ( yearlyEnergyConsumption == 0) { gis_area.f_style( v_unknownConsumptionColor, null, null, null );}
else if ( yearlyEnergyConsumption < 1500){ gis_area.f_style( rect_mapOverlayLegend_ElectricityConsumption1.getFillColor(), null, null, null);}
else if ( yearlyEnergyConsumption < 2500){ gis_area.f_style( rect_mapOverlayLegend_ElectricityConsumption2.getFillColor(), null, null, null);}
else if ( yearlyEnergyConsumption < 4000){ gis_area.f_style( rect_mapOverlayLegend_ElectricityConsumption3.getFillColor(), null, null, null);}
else if ( yearlyEnergyConsumption < 6000){ gis_area.f_style( rect_mapOverlayLegend_ElectricityConsumption4.getFillColor(), null, null, null);}
else if ( yearlyEnergyConsumption > 6000){ gis_area.f_style( rect_mapOverlayLegend_ElectricityConsumption5.getFillColor(), null, null, null);}
	

/*ALCODEEND*/}

double f_updateMainInterfaceSliders()
{/*ALCODESTART::1718288402102*/
uI_Tabs.f_updateSliders();
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
v_outsideTemperature = roundToDecimal(energyModel.pp_ambientTemperature_degC.getCurrentValue(), 1);
v_solarIrradiance = roundToInt(energyModel.pp_PVProduction35DegSouth_fr.getCurrentValue() * 1000);

double windspeed = roundToDecimal(energyModel.pp_windProduction_fr.getCurrentValue(), 1);
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

if(MVsubstations != null || project_data.project_type() == OL_ProjectType.RESIDENTIAL){
	b_gridLoopsAreDefined = true;
}

v_amountOfDefinedGridLoops = 0;

if(MVsubstations != null){
	//Set all unique grid topology colors for each substation and its children if the gridloops are defined
	for (GridNode MVsub : MVsubstations){
		
		//Create a unique color from a spectrum and assign it to the subMV
		MVsub.p_uniqueColor = spectrumColor(v_amountOfDefinedGridLoops, MVsubstations.size());
		
		//Assign unique color to all underlying grid nodes
		MVsub.f_getLowerLVLConnectedGridNodes().forEach(GN -> GN.p_uniqueColor = MVsub.p_uniqueColor);
		
		//Update spectrum color index and total defined colours
		v_amountOfDefinedGridLoops++;
	}
}
else if(project_data.project_type() == OL_ProjectType.RESIDENTIAL){
	int totalNotToplevelGridNodes = energyModel.f_getGridNodesNotTopLevel().size();
	//Set all unique grid topology colors for each substation and its children if the gridloops are defined
	for (GridNode node : energyModel.f_getGridNodesNotTopLevel()){
		
		//Create a unique color from a spectrum and assign it to the subMV
		node.p_uniqueColor = spectrumColor(v_amountOfDefinedGridLoops, totalNotToplevelGridNodes);

		//Update spectrum color index and total defined colours
		v_amountOfDefinedGridLoops++;
	}
}
/*ALCODEEND*/}

double f_styleSUBMV(GISRegion gisregion)
{/*ALCODESTART::1721991963719*/
gisregion.setVisible(false);
/*ALCODEEND*/}

double f_styleMVMV(GISRegion gisregion)
{/*ALCODESTART::1721992103665*/
gisregion.setFillColor(v_MVMVNodeColor);
gisregion.setLineStyle( LINE_STYLE_SOLID );
gisregion.setLineColor( v_MVMVLineColor );
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
f_createPrivateCompanyUI();


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
		
	case HAS_PV:
		f_filterHasPV(toBeFilteredGC);
		break;
		
	case HAS_TRANSPORT:
		f_filterHasTransport(toBeFilteredGC);
		break;
		
	case HAS_EV:
		f_filterHasTransport(toBeFilteredGC);
		break;	
		
	case GRIDTOPOLOGY_SELECTEDLOOP:
		if(!c_filterSelectedGridLoops.isEmpty()){
			f_filterGridLoops(toBeFilteredGC);
		}
		else{
		
			f_setForcedClickScreenText("Selecteer een lus");
			if(!b_inEnergyHubSelectionMode){
				f_setForcedClickScreenVisibility(true);
			}

			if(c_loadedMapOverlayTypes.contains(OL_MapOverlayTypes.GRID_NEIGHBOURS)){
				rb_mapOverlay.setValue(c_loadedMapOverlayTypes.indexOf(OL_MapOverlayTypes.GRID_NEIGHBOURS),true);			
			}
			if(c_selectedFilterOptions.size() > 1){
				c_selectedGridConnections = new ArrayList<>(toBeFilteredGC);	
			}
			else{
				filterCanReturnZero = true;
			}
		}
		break;
		
	case SELECTED_NEIGHBORHOOD:
		if(!c_filterSelectedNeighborhoods.isEmpty()){
			f_filterNeighborhoods(toBeFilteredGC);
		}
		else{
			f_setForcedClickScreenText("Selecteer een buurt");
			if(!b_inEnergyHubSelectionMode){
				f_setForcedClickScreenVisibility(true);
			}
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
	f_setErrorScreen("Geselecteerde filter geeft geen resultaten. De filter is gedeactiveerd.", 0, 0);
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
		selectedFilter_OL = OL_FilterOptionsGC.HAS_PV;
		break;
	case "Met voertuigen":
		selectedFilter_OL = OL_FilterOptionsGC.HAS_TRANSPORT;
		break;
	case "Met EV":
		selectedFilter_OL = OL_FilterOptionsGC.HAS_EV;
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
f_clearSelectionAndSelectEnergyModel();
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
					  	clickedGridConnectionConnectedGridNode.p_nodeType != OL_GridNodeType.MVMV &&
					  	clickedGridConnectionConnectedGridNode.p_nodeType != OL_GridNodeType.HVMV
					  	){
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
					f_setForcedClickScreenText("");
					if(!b_inEnergyHubSelectionMode){
						f_setForcedClickScreenVisibility(false);
					}
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
c_selectedGridConnections = new ArrayList<>(findAll(toBeFilteredGC, GC -> GC.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW)));

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

double f_setErrorScreen(String errorMessage,double xOffset,double yOffset)
{/*ALCODESTART::1736344958050*/
gr_errorScreen.setPos(xOffset, yOffset);

//Reset location and height
button_errorOK.setY(550);
rect_errorMessage.setY(380);
rect_errorMessage.setHeight(200);
t_errorMessage.setY(430);

//Set position above all other things
f_setShapePresentationOnTop(gr_errorScreen);

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

double f_styleResultsUI()
{/*ALCODESTART::1736442051389*/
uI_Results.f_styleResultsUIHeader(zenmocolor_blue3.getFillColor(), zenmocolor_blue3.getFillColor(), 1.0, LINE_STYLE_SOLID);
uI_Results.f_styleAllCharts(v_backgroundColor, zenmocolor_blue3.getFillColor(), 1.0, LINE_STYLE_SOLID);
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
				f_setForcedClickScreenText("");
				if(!b_inEnergyHubSelectionMode){
					f_setForcedClickScreenVisibility(false);
				}
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
if (resultsUI.f_getSelectedObjectData() != null) {
	if(resultsUI.getGr_resultsUIHeader().isVisible()){
		resultsUI.getRadioButtons().setValue(0, true);
	}
	resultsUI.chartProfielen.getPeriodRadioButton().setValue(0, true);
	resultsUI.f_enableNonLivePlotRadioButtons(false);
}
for (ShapeRadioButtonGroup rb :resultsUI.chartProfielen.getAllPeriodRadioButtons()) {
	rb.setValue(0, false);
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

double f_setForcedClickScreenText(String forcedClickScreenText)
{/*ALCODESTART::1742300624199*/
t_forcedClickMessage.setText(forcedClickScreenText);

if(t_forcedClickMessage.getText().equals("")){
	gr_ForceMapSelectionText.setVisible(false);
}
else{
	gr_ForceMapSelectionText.setVisible(true);
}
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
//traceln("f_setStartView() reached!");

if(project_data.map_centre_latitude() != null && project_data.map_centre_longitude() != null && project_data.map_centre_latitude() != 0 && project_data.map_centre_longitude() != 0){
	map.setCenterLatitude(project_data.map_centre_latitude());
	map.setCenterLongitude(project_data.map_centre_longitude());
}
else{
	ArrayList<GIS_Object> gisObjects_for_mapViewBounds = new ArrayList<GIS_Object>();
	if((settings.subscopesToSimulate() == null || settings.subscopesToSimulate().size() == 0) && findAll(energyModel.pop_GIS_Objects, gisObject -> gisObject.p_GISObjectType == OL_GISObjectType.REGION).size() > 0){
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

if(project_data.map_scale() != null){
	map.setMapScale(project_data.map_scale());
}

va_Interface.navigateTo();
v_currentViewArea = va_Interface;
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
	if (xPosition < (v_currentViewArea.getX() + v_currentViewArea.getWidth()/2) ) {
		// bubble is on the left half, so text should appear to the right
		gr_infoText.setX( xPosition + margin_px + infoBubble.getWidth()/2);
	}
	else {
		// bubble is on the right half, so text should appear to the left
		gr_infoText.setX( xPosition - margin_px + infoBubble.getWidth()/2 - rect_infoText.getWidth());
	}
	
	// In AnyLogic the Y-Axis is inverted
	if (yPosition > (v_currentViewArea.getY() + v_currentViewArea.getHeight()/2) ) {
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
c_orderedV1GChargers = new ArrayList<J_EAChargePoint>();
c_orderedV2GChargers = new ArrayList<J_EAChargePoint>();
c_orderedPublicChargers = new ArrayList<GCPublicCharger>();

List<J_EAChargePoint> c_inactiveV1GChargers = new ArrayList<J_EAChargePoint>();
List<J_EAChargePoint> c_inactiveV2GChargers = new ArrayList<J_EAChargePoint>();

for (GridConnection gc : energyModel.f_getActiveGridConnections()) {
	for (J_EAChargePoint charger : gc.c_chargers) {
		if (charger.getV1GCapable()) {
			c_orderedV1GChargers.add(0, charger);
		}
		else {
			c_orderedV1GChargers.add(charger);
		}
		if (charger.getV2GCapable()) {
			c_orderedV2GChargers.add(0, charger);
		}
		else {
			c_orderedV2GChargers.add(charger);
		}
	}
}

for (GridConnection gc : energyModel.f_getPausedGridConnections()) {
	for (J_EAChargePoint charger : gc.c_chargers) {
		if (charger.getV1GCapable()) {
			c_inactiveV1GChargers.add(0, charger);
		}
		else {
			c_inactiveV1GChargers.add(charger);
		}
		if (charger.getV2GCapable()) {
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
		
		if(activeSpecialGISObjectType == OL_GISObjectType.CHARGER){
			numberOfSpecialActiveGISObjectTypes ++;
			legendShapes = f_getNextSpecialLegendShapes(numberOfSpecialActiveGISObjectTypes);
			legendShapes.getFirst().setVisible(true);
			legendShapes.getSecond().setVisible(true);
			legendShapes.getFirst().setText("Laadpaal/plein (Toegevoegd)");
			legendShapes.getSecond().setFillColor(v_newChargingStationColor);
			legendShapes.getSecond().setLineColor(v_newChargingStationLineColor);	
		}
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
		legendText.setText("Laadpaal/plein (Bestaand)");
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

double f_setColorsBasedOnCongestion_objects(GIS_Object gis_area)
{/*ALCODESTART::1752756002220*/
if (gis_area.c_containedGridConnections.size() > 0) {
	double maxLoad_fr_gis_object = 0;
	boolean capacityKnown = false;
	
	for(GridConnection gc : gis_area.c_containedGridConnections){
		if(gc.v_rapidRunData != null){
			double maxLoad_fr_gc = 0;
			double maxLoad_fr_gc_delivery = gc.v_rapidRunData.connectionMetaData.contractedDeliveryCapacity_kW > 0 && gc.v_rapidRunData.connectionMetaData.contractedDeliveryCapacityKnown ? gc.v_rapidRunData.getPeakDelivery_kW()/gc.v_rapidRunData.connectionMetaData.contractedDeliveryCapacity_kW : 0;
			double maxLoad_fr_gc_feedin = gc.v_rapidRunData.connectionMetaData.contractedFeedinCapacity_kW > 0 && gc.v_rapidRunData.connectionMetaData.contractedFeedinCapacityKnown  ? gc.v_rapidRunData.getPeakFeedin_kW()/gc.v_rapidRunData.connectionMetaData.contractedFeedinCapacity_kW : 0;

			switch(rb_mapOverlayLegend_congestion.getValue()){
				case 0:
					maxLoad_fr_gc = maxLoad_fr_gc_delivery;
					
					if(gc.v_rapidRunData.connectionMetaData.contractedDeliveryCapacityKnown){
						capacityKnown = true;
					}
					break;
				case 1:
					maxLoad_fr_gc = maxLoad_fr_gc_feedin;
					if(gc.v_rapidRunData.connectionMetaData.contractedFeedinCapacityKnown){
						capacityKnown = true;
					}
					break;
				case 2:
					maxLoad_fr_gc = max(maxLoad_fr_gc_delivery, maxLoad_fr_gc_feedin);
					if(maxLoad_fr_gc_delivery > maxLoad_fr_gc_feedin && gc.v_rapidRunData.connectionMetaData.contractedDeliveryCapacityKnown){
						capacityKnown = true;
					}
					else if(maxLoad_fr_gc_feedin > maxLoad_fr_gc_delivery && gc.v_rapidRunData.connectionMetaData.contractedFeedinCapacityKnown){
						capacityKnown = true;
					}
					break;
			}
			if(maxLoad_fr_gc > maxLoad_fr_gis_object){
				maxLoad_fr_gis_object = maxLoad_fr_gc;
			}
		}
	}
	
	//Set colour based on found parameters
	if(!capacityKnown && project_data.project_type() != RESIDENTIAL){
		gis_area.gisRegion.setFillColor(v_gridNodeColorCapacityUnknown);
		gis_area.gisRegion.setLineColor(v_gridNodeLineColorCapacityUnknown);
	} else if (maxLoad_fr_gis_object > 1) {
		gis_area.gisRegion.setFillColor(v_gridNodeColorCongested);
		gis_area.gisRegion.setLineColor(v_gridLineColorCongested);
	} else if (maxLoad_fr_gis_object > 0.7) {
		gis_area.gisRegion.setFillColor(v_gridNodeColorStrained);
		gis_area.gisRegion.setLineColor(v_gridNodeLineColorStrained);
	} else {
		gis_area.gisRegion.setFillColor(v_gridNodeColorUncongested);
		gis_area.gisRegion.setLineColor(v_gridNodeLineColorUncongested);
	}
}
/*ALCODEEND*/}

double f_setColorsBasedOnCongestion_gridnodes(GridNode gn,boolean isLiveSim)
{/*ALCODESTART::1752756016324*/
if (gn!=null && gn.gisRegion != null){
	double maxLoad_fr = 0;
	if(isLiveSim){
		maxLoad_fr = abs(gn.v_currentLoad_kW)/gn.p_capacity_kW;	
	}
	else{
		J_LoadDurationCurves loadCurves = gn.f_getDuurkrommes();
		double maxLoad_fr_delivery = gn.p_capacity_kW > 0 ? abs(loadCurves.ds_loadDurationCurveTotal_kW.getY(0))/gn.p_capacity_kW : 0;
		double maxLoad_fr_feedin = gn.p_capacity_kW > 0 ? abs(loadCurves.ds_loadDurationCurveTotal_kW.getY(loadCurves.ds_loadDurationCurveTotal_kW.size()-1))/gn.p_capacity_kW : 0;

		switch(rb_mapOverlayLegend_congestion.getValue()){
			case 0:
				maxLoad_fr = maxLoad_fr_delivery;
				break;
			case 1:
				maxLoad_fr = maxLoad_fr_feedin;
				break;
			case 2:
				maxLoad_fr = max(maxLoad_fr_delivery, maxLoad_fr_feedin);
				break;
		}
	}
	
	if(!isLiveSim && !gn.p_realCapacityAvailable && project_data.project_type() != RESIDENTIAL){
		gn.gisRegion.setFillColor(v_gridNodeColorCapacityUnknown);
		gn.gisRegion.setLineColor(v_gridNodeLineColorCapacityUnknown);
	} else if (maxLoad_fr > 1) {
		gn.gisRegion.setFillColor(v_gridNodeColorCongested);
		gn.gisRegion.setLineColor(v_gridLineColorCongested);
	} else if (maxLoad_fr > 0.7) {
		gn.gisRegion.setFillColor(v_gridNodeColorStrained);
		gn.gisRegion.setLineColor(v_gridNodeLineColorStrained);
	} else {
		gn.gisRegion.setFillColor(v_gridNodeColorUncongested);
		gn.gisRegion.setLineColor(v_gridNodeLineColorUncongested);
	}
	
	if( gn == v_clickedGridNode && gn != v_previousClickedGridNode){ // dit zorgt ervoor dat de kleuringfunctie correct werkt in zowel live stand als pauze stand
		gn.gisRegion.setFillColor( v_selectionColor );
		gn.gisRegion.setLineColor( orange );
	}
}
/*ALCODEEND*/}

double f_clearSelectionAndSelectEnergyModel()
{/*ALCODESTART::1752836715726*/
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

/*ALCODEEND*/}

double f_styleLVLV(GISRegion gisregion)
{/*ALCODESTART::1752837115143*/
gisregion.setLineStyle( LINE_STYLE_SOLID );
gisregion.setLineColor( v_LVLVLineColor );
gisregion.setLineWidth(2);		
gisregion.setFillColor(v_LVLVNodeColor);
/*ALCODEEND*/}

double f_initializeMapOverlayRadioButton()
{/*ALCODESTART::1753085860778*/
//Set active map overlay types if they are set in the project settings
if(settings.activeMapOverlayTypes() != null && settings.activeMapOverlayTypes().size() > 0){
	c_loadedMapOverlayTypes = new ArrayList<OL_MapOverlayTypes>(settings.activeMapOverlayTypes());
	if(!c_loadedMapOverlayTypes.contains(OL_MapOverlayTypes.DEFAULT)){
		c_loadedMapOverlayTypes.add(0, OL_MapOverlayTypes.DEFAULT); // Force default to be available
	}
}
else{//Take the default
	c_loadedMapOverlayTypes.add(OL_MapOverlayTypes.DEFAULT);
	c_loadedMapOverlayTypes.add(OL_MapOverlayTypes.ELECTRICITY_CONSUMPTION);
	c_loadedMapOverlayTypes.add(OL_MapOverlayTypes.PV_PRODUCTION);
	c_loadedMapOverlayTypes.add(OL_MapOverlayTypes.GRID_NEIGHBOURS);
	c_loadedMapOverlayTypes.add(OL_MapOverlayTypes.CONGESTION);
	if(project_data.project_type() == OL_ProjectType.RESIDENTIAL){
		c_loadedMapOverlayTypes.add(OL_MapOverlayTypes.PARKING_TYPE);
	}
}


//Adjust the visualisation of the radiobuttons
Presentable presentable = gr_mapOverlayLegenda.getPresentable();
boolean ispublic = true;
double x = 756;
double y = c_loadedMapOverlayTypes.size() < 6 ? 837 : 837 - 18;
double width = 130;
double height = 0;//Not needed, automatically adjust by adding options
Color textColor = Color.BLACK;
boolean enabled = true;
Font font = new Font("Dialog", Font.PLAIN, 11);
boolean vertical = true;


//Set words for the radiobutton options
List<String> RadioButtonOptions_list = new ArrayList<String>();
for(OL_MapOverlayTypes mapOverlayType : c_loadedMapOverlayTypes){
	switch(mapOverlayType){
		case DEFAULT:
			RadioButtonOptions_list.add("Standaard");
			break;
		case ELECTRICITY_CONSUMPTION:
			RadioButtonOptions_list.add("Elektriciteitsverbruik");
			break;
		case PV_PRODUCTION:
			RadioButtonOptions_list.add("PV Opwek");
			break;
		case GRID_NEIGHBOURS:
			RadioButtonOptions_list.add("Energie Buren");
			break;
		case CONGESTION:
			RadioButtonOptions_list.add("Netbelasting");
			break;
		case ENERGY_LABEL:
			RadioButtonOptions_list.add("Energielabel");
			break;
		case PARKING_TYPE:
			RadioButtonOptions_list.add("Parkeer type");
			break;
	}
} 

String[] RadioButtonOptions = RadioButtonOptions_list.toArray(String[]::new);

//Create the radiobutton and set the correct action.
rb_mapOverlay = new ShapeRadioButtonGroup(presentable, ispublic, x ,y, width, height, textColor, enabled, font, vertical, RadioButtonOptions){
	@Override
	public void action() {
		f_setMapOverlay();
	}
};

presentation.add(rb_mapOverlay);

//For now: Adjust location of radiobutton title if 6 buttons
if(c_loadedMapOverlayTypes.size() > 5){
	gr_colorings.setY(-17);
}
/*ALCODEEND*/}

double f_setMapOverlay()
{/*ALCODESTART::1753096794863*/
//reset legend
gr_defaultLegenda.setVisible(false);
gr_mapOverlayLegend_ElectricityConsumption.setVisible(false);
gr_mapOverlayLegend_PVProduction.setVisible(false);
gr_mapOverlayLegend_gridNeighbours.setVisible(false);
gr_mapOverlayLegend_congestion.setVisible(false);
gr_mapOverlayLegend_EnergyLabel.setVisible(false);
b_updateLiveCongestionColors = false;

if(!b_inEnergyHubMode){
	f_clearSelectionAndSelectEnergyModel();
}

//Get selected map overlay type, based on loaded order of the radio buttons
OL_MapOverlayTypes selectedMapOverlayType = c_loadedMapOverlayTypes.get(rb_mapOverlay.getValue());

//Set the correct map overlay
switch(selectedMapOverlayType){
	case DEFAULT:
		f_setMapOverlay_Default();
		break;
	case ELECTRICITY_CONSUMPTION:
		f_setMapOverlay_ElectricityConsumption();
		break;
	case PV_PRODUCTION:
		f_setMapOverlay_PVProduction();
		break;
	case GRID_NEIGHBOURS:
		f_setMapOverlay_GridTopology();
		break;
	case CONGESTION:
		f_setMapOverlay_Congestion();
		break;
	case ENERGY_LABEL:
		f_setMapOverlay_EnergyLabel();
		break;
	case PARKING_TYPE:
		f_setMapOverlay_ParkingType();
		break;
}
/*ALCODEEND*/}

double f_setMapOverlay_ElectricityConsumption()
{/*ALCODESTART::1753097345978*/
//Set legend
b_updateLiveCongestionColors = true;
gr_mapOverlayLegend_ElectricityConsumption.setVisible(true);

//Colour gis objects
if (project_data.project_type() == OL_ProjectType.RESIDENTIAL){
	for (GIS_Building building : energyModel.pop_GIS_Buildings){
		f_setColorsBasedOnConsumptionProfileHouseholds(building);
	}
}
else {
	if(energyModel.v_rapidRunData == null){
		f_setErrorScreen("Dit overzicht wordt pas beschikbaar na het uitvoeren van een jaarsimulatie. In plaats daarvan is de standaard kaart geselecteerd.", 0, 0);
		rb_mapOverlay.setValue(c_loadedMapOverlayTypes.indexOf(OL_MapOverlayTypes.DEFAULT),true);
		return;			
	}
	gr_mapOverlayLegend_ElectricityConsumption.setVisible(true);
	for (GIS_Building building : energyModel.pop_GIS_Buildings){
		if(building.gisRegion.isVisible()){
			f_setColorsBasedOnElectricityConsumption(building);
		}
	}
	/*for (GIS_Object object : energyModel.pop_GIS_Objects){
		f_setColorsBasedOnConsumpion(object);
	}*/
}
/*ALCODEEND*/}

double f_setMapOverlay_PVProduction()
{/*ALCODESTART::1753097409446*/
//Set legend
b_updateLiveCongestionColors = true;
gr_mapOverlayLegend_PVProduction.setVisible(true);

//Colour gis objects
for (GIS_Building building : energyModel.pop_GIS_Buildings){
	f_setColorsBasedOnProduction(building);
}
/*for (GIS_Object object : energyModel.pop_GIS_Objects){
	f_setColorsBasedOnProduction(object);
}*/
/*ALCODEEND*/}

double f_setMapOverlay_GridTopology()
{/*ALCODESTART::1753097484078*/
//Set legend
gr_mapOverlayLegend_gridNeighbours.setVisible(true);
b_updateLiveCongestionColors = false;

//Colour gis objects
for (GIS_Building building : energyModel.pop_GIS_Buildings){
	f_setColorsBasedOnGridTopology_objects(building);
}
/*for (GIS_Object object : energyModel.pop_GIS_Objects){
	f_setColorsBasedOnGridTopology_objects(object);
}*/
for (GridNode GN : energyModel.pop_gridNodes){
	f_setColorsBasedOnGridTopology_gridnodes(GN);
}
/*ALCODEEND*/}

double f_setMapOverlay_Congestion()
{/*ALCODESTART::1753097518541*/
if(energyModel.v_rapidRunData == null){
	f_setErrorScreen("Dit overzicht wordt pas beschikbaar na het uitvoeren van een jaarsimulatie. In plaats daarvan is de standaard kaart geselecteerd.", 0, 0);
	rb_mapOverlay.setValue(c_loadedMapOverlayTypes.indexOf(OL_MapOverlayTypes.DEFAULT),true);
	return;			
}

//Set legend
gr_mapOverlayLegend_congestion.setVisible(true);
b_updateLiveCongestionColors = false;

//Colour gis objects
for (GIS_Building building : energyModel.pop_GIS_Buildings){
	f_setColorsBasedOnCongestion_objects(building);
}
/*
for (GIS_Object object : energyModel.pop_GIS_Objects){
	f_setColorsBasedOnCongestion_objects(object);
}
*/
for (GridNode GN : energyModel.pop_gridNodes){
	f_setColorsBasedOnCongestion_gridnodes(GN, false);
}
/*ALCODEEND*/}

double f_setMapOverlay_Default()
{/*ALCODESTART::1753097561639*/
b_updateLiveCongestionColors = true;
gr_defaultLegenda.setVisible(true);	
for (GIS_Building b: energyModel.pop_GIS_Buildings) {
	f_styleAreas(b);
}
/*for (GIS_Object object : energyModel.pop_GIS_Objects){
	f_styleAreas(object);
}*/
/*ALCODEEND*/}

double f_setMapOverlay_EnergyLabel()
{/*ALCODESTART::1753108764992*/
//Set legend
b_updateLiveCongestionColors = true;
gr_mapOverlayLegend_EnergyLabel.setVisible(true);

for (GIS_Building building : energyModel.pop_GIS_Buildings){
	f_setColorsBasedOnEnergyLabels(building);
}
/*ALCODEEND*/}

double f_setShapePresentationOnTop(Shape shape)
{/*ALCODESTART::1753440028514*/
presentation.remove(shape);
presentation.insert(presentation.size(), shape);
/*ALCODEEND*/}

double f_updateOrderedListsAfterDeserialising(EnergyModel newEnergyModel)
{/*ALCODESTART::1753713001191*/
// Update references of GClists
for (int i=0; i< c_orderedPVSystemsHouses.size(); i++) {
	String GCid = c_orderedPVSystemsHouses.get(i).p_gridConnectionID;
	c_orderedPVSystemsHouses.set(i,findFirst(newEnergyModel.Houses, x->x.p_gridConnectionID == GCid));
}

for (int i=0; i< c_orderedPVSystemsCompanies.size(); i++) {
	String GCid = c_orderedPVSystemsCompanies.get(i).p_gridConnectionID;
	c_orderedPVSystemsCompanies.set(i,findFirst(newEnergyModel.UtilityConnections, x->x.p_gridConnectionID == GCid));
}

for (int i=0; i< c_orderedHeatingSystemsCompanies.size(); i++) {
	String GCid = c_orderedHeatingSystemsCompanies.get(i).p_gridConnectionID;
	c_orderedHeatingSystemsCompanies.set(i,findFirst(newEnergyModel.UtilityConnections, x->x.p_gridConnectionID == GCid));
}

for (int i=0; i< c_orderedHeatingSystemsHouses.size(); i++) {
	String GCid = c_orderedHeatingSystemsHouses.get(i).p_gridConnectionID;
	c_orderedHeatingSystemsHouses.set(i,findFirst(newEnergyModel.Houses, x->x.p_gridConnectionID == GCid));
}

for (int i=0; i< c_orderedPublicChargers.size(); i++) {
	String GCid = c_orderedPublicChargers.get(i).p_gridConnectionID;
	c_orderedPublicChargers.set(i,findFirst(newEnergyModel.PublicChargers, x->x.p_gridConnectionID == GCid));
}

// TODO: Update references of J_EAlists 

/*ALCODEEND*/}

double f_initializePresentationOrder()
{/*ALCODESTART::1753440184174*/
//Set order of certain layovers and submenus
f_setShapePresentationOnTop(map);
f_setShapePresentationOnTop(gr_zoomButton);
f_setShapePresentationOnTop(gr_sliderClickBlocker);
f_setShapePresentationOnTop(gr_forceMapSelection);
f_setShapePresentationOnTop(gr_filterInterface);
f_setShapePresentationOnTop(gr_infoText);

/*ALCODEEND*/}

double f_setForcedClickScreenVisibility(boolean showForcedClickScreen)
{/*ALCODESTART::1753445407428*/
gr_forceMapSelection.setVisible(showForcedClickScreen);
/*ALCODEEND*/}

double f_selectEnergyHubGC(double clickx,double clicky)
{/*ALCODESTART::1753446312775*/
if(b_inManualFilterSelectionMode){
	f_selectManualFilteredGC(clickx, clicky);
}
else if (c_selectedFilterOptions.contains(OL_FilterOptionsGC.GRIDTOPOLOGY_SELECTEDLOOP) || 
		c_selectedFilterOptions.contains(OL_FilterOptionsGC.SELECTED_NEIGHBORHOOD)){
	
	if(c_selectedFilterOptions.contains(OL_FilterOptionsGC.GRIDTOPOLOGY_SELECTEDLOOP)){
		f_selectGridLoop(clickx, clicky);
	}
	if(c_selectedFilterOptions.contains(OL_FilterOptionsGC.SELECTED_NEIGHBORHOOD)){
		f_selectNeighborhood(clickx, clicky);
	}
}
/*ALCODEEND*/}

double f_startEnergyHubConfiguration()
{/*ALCODESTART::1753698716095*/
pauseSimulation();

b_inEnergyHubMode = true;
b_inEnergyHubSelectionMode = true;

f_setForcedClickScreenText("");
f_setForcedClickScreenVisibility(true);

cb_showFilterInterface.setSelected(true, true);
gr_filterInterface.setPos(170, 580);
/*ALCODEEND*/}

double f_finalizeEnergyHubConfiguration()
{/*ALCODESTART::1753698810590*/
if(b_inEnergyHubSelectionMode){
	if(button_completeManualSelectionMode.isVisible()){
		button_completeManualSelectionMode.action();
	}


	//Move scenario radiobuttons over
	f_getScenarioButtons().setPos( 
		gr_energyHubPresentation.getX() + uI_EnergyHub.rect_scenarios.getX() + 25.0,
		gr_energyHubPresentation.getY() + uI_EnergyHub.rect_scenarios.getY() + 50.0
	);
	
	//Set map in correct pos and navigate to e-hub view
	map.setPos( 
		gr_energyHubPresentation.getX() + uI_EnergyHub.rect_map.getX() + 10.0,
		gr_energyHubPresentation.getY() + uI_EnergyHub.rect_map.getY() + 10.0
	);
	map.setScale( 0.85, 0.85 );
	va_EHubDashboard.navigateTo();
	v_currentViewArea = va_EHubDashboard;
	
	//Copy selected GC and coop to e-hub dashboard
	v_customEnergyCoop.p_actorID = "eHubConfiguratorCoop";
	uI_EnergyHub.v_energyHubCoop = v_customEnergyCoop;
	
	//Set E-hub selection mode false
	b_inEnergyHubSelectionMode = false;
	
	uI_EnergyHub.f_initializeEnergyHubDashboard();
}

/*ALCODEEND*/}

ArrayList<GridConnection> f_updateGClistAfterDeserialisation(ArrayList<? extends GridConnection> GClist,EnergyModel newEnergyModel)
{/*ALCODESTART::1753713085487*/
for (int i=0; i< GClist.size(); i++) {
	String GCid = GClist.get(i).p_gridConnectionID;
	GClist.set(i,findFirst(newEnergyModel.c_gridConnections, x->x.p_gridConnectionID == GCid));
}


/*ALCODEEND*/}

ArrayList<GridConnection> f_updateJ_EAlistAfterDeserialisation(ArrayList<J_EA> EAlist,EnergyModel newEnergyModel)
{/*ALCODESTART::1753713662613*/
for (int i=0; i< EAlist.size(); i++) {
	String GCid = EAlist.get(i).getParentAgent();p_gridConnectionID;
	EAlist.set(i,findFirst(newEnergyModel.c_gridConnections, x->x.p_gridConnectionID == GCid));
}


/*ALCODEEND*/}

double f_createUITabs_default()
{/*ALCODESTART::1753881971788*/
// CHOOSE WHICH TABS YOU WANT TO BE ABLE TO SHOW FOR YOUR PROJECT 
// (OVERRIDE FUNCTION IN CHILD IF YOU WANT OTHER THAN DEFAULT)

// Adding the (child) tabs to the tabArea population

// If you use an extension of a tab, you must update the pointer to the instance of the interface
// Something like: tabElectricity.zero_Interface = loader_Project.zero_Interface;
// No update to the pointer is needed for the generic tabs


uI_Tabs.add_pop_tabElectricity();
uI_Tabs.add_pop_tabHeating();
uI_Tabs.add_pop_tabMobility();

// Group visibilities
// When using an extension of a generic tab don't forget to typecast it!
if (project_data.project_type() == OL_ProjectType.RESIDENTIAL) {
	((tabElectricity)uI_Tabs.pop_tabElectricity.get(0)).getGroupElectricityDemandSliders_ResidentialArea().setVisible(true);
	((tabHeating)uI_Tabs.pop_tabHeating.get(0)).getGroupHeatDemandSlidersResidentialArea().setVisible(true);
	((tabMobility)uI_Tabs.pop_tabMobility.get(0)).getGr_mobilitySliders_residential().setVisible(true);
}
else {
	uI_Tabs.add_pop_tabEHub();
	((tabElectricity)uI_Tabs.pop_tabElectricity.get(0)).getGroupElectricityDemandSliders_Businesspark().setVisible(true);
	((tabHeating)uI_Tabs.pop_tabHeating.get(0)).getGroupHeatDemandSlidersCompanies().setVisible(true);
	((tabMobility)uI_Tabs.pop_tabMobility.get(0)).getGr_mobilitySliders_default().setVisible(true);
	((tabEHub)uI_Tabs.pop_tabEHub.get(0)).getGroupHubSliders().setVisible(true);
}
/*ALCODEEND*/}

double f_initializePrivateAndPublicParkingCarsOrder()
{/*ALCODESTART::1753882411689*/
//Get all public and private parked cars
c_orderedVehiclesPrivateParking = new ArrayList<J_EAVehicle>();
List<J_EADieselVehicle> allPublicParkedCars = new ArrayList<J_EADieselVehicle>();
for (GCHouse house : energyModel.Houses) {
	if (house.p_eigenOprit) {
		c_orderedVehiclesPrivateParking.addAll(house.c_vehicleAssets);
	}
	else{
		allPublicParkedCars.addAll(house.c_dieselVehicles);	
	}
}

//Shuffle the collections to not have skewed initialization
Collections.shuffle(c_orderedVehiclesPrivateParking);
Collections.shuffle(allPublicParkedCars);

//Get the total amount of public chargers
int totalChargers = c_orderedPublicChargers.size();

if(totalChargers > 0){
	// Fair distribution of vehicles across chargers
	List<Integer> numberOfCarsPerCharger = f_getNumberOfCarsPerCharger(allPublicParkedCars.size(), totalChargers);
	
	// Assign vehicles to chargers
	c_mappingOfVehiclesPerCharger.clear();
	int index = 0;
	for (int i = 0; i < totalChargers; i++) {
	    GCPublicCharger charger = c_orderedPublicChargers.get(i);
	    int numberOfCars = numberOfCarsPerCharger.get(i);
	
	    List<J_EADieselVehicle> assignedCars = new ArrayList<>(allPublicParkedCars.subList(index, index + numberOfCars));
	    c_mappingOfVehiclesPerCharger.put(charger.p_uid, assignedCars);
	
	    // Place vehicles depending on whether the charger is active
	    if (charger.v_isActive) {
	        for (J_EADieselVehicle car : assignedCars) {
	           	J_ActivityTrackerTrips tripTracker = car.getTripTracker(); //Needed, as triptracker is removed when removeEnergyAsset is called.
				car.removeEnergyAsset();
				car.setTripTracker(tripTracker);//Re-set the triptracker again, for storing.
	        }
	    }
	
	    index += numberOfCars;
	}
}

/*ALCODEEND*/}

double f_simulateYearFromMainInterface()
{/*ALCODESTART::1753884000493*/

gr_simulateYear.setVisible(false);		
gr_loadIconYearSimulation.setVisible(true);
		

new Thread( () -> {
	//Run rapid run
	energyModel.f_runRapidSimulation();
	
	//After rapid run: remove loading screen
	gr_loadIconYearSimulation.setVisible(false);
	uI_EnergyHub.gr_loadIconYearSimulationEnergyHub.setVisible(false);
			
	if (c_selectedGridConnections.size() == 0){//Update main area collection
		uI_Results.f_updateResultsUI(energyModel);
	}
	else if (c_selectedGridConnections.size() == 1){//Update selected GC area collection
		uI_Results.f_updateResultsUI(c_selectedGridConnections.get(0));
	}
	else if(c_selectedGridConnections.size() > 1){//Update COOP area collection
		uI_Results.f_updateResultsUI(v_customEnergyCoop);
	}
	if (uI_EnergyHub.v_energyHubCoop != null) {
		uI_EnergyHub.uI_Results.f_updateResultsUI(uI_EnergyHub.v_energyHubCoop);
	}
	//Update and show kpi summary chart after run
	if(settings.showKPISummary() != null && settings.showKPISummary() && v_clickedObjectType != OL_GISObjectType.GRIDNODE){
		uI_Results.getCheckbox_KPISummary().setSelected(true, true);
	}
	
	//Enable radio buttons again
	uI_Results.f_enableNonLivePlotRadioButtons(true);
	uI_EnergyHub.uI_Results.f_enableNonLivePlotRadioButtons(true);
	c_companyUIs.forEach(companyUI -> {companyUI.uI_Results.f_enableNonLivePlotRadioButtons(true); companyUI.gr_simulateYearScreen.setVisible(false);});
	
	b_resultsUpToDate = true;
}).start();
/*ALCODEEND*/}

double f_initialPTSystemsOrder_households()
{/*ALCODESTART::1753951802256*/
List<GCHouse> houses = new ArrayList<GCHouse>(energyModel.Houses.findAll( x -> true));
List<GCHouse> housesWithoutPT = houses.stream().filter( gc -> !gc.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.ptProductionHeat_kW) ).collect(Collectors.toList());
List<GCHouse> housesWithPT = new ArrayList<>(houses);
housesWithPT.removeAll(housesWithoutPT);

c_orderedPTSystemsHouses = new ArrayList<>(housesWithoutPT);
c_orderedPTSystemsHouses.addAll(housesWithPT);

/*ALCODEEND*/}

double f_setMapOverlay_ParkingType()
{/*ALCODESTART::1754312747144*/
//Set legend
b_updateLiveCongestionColors = true;

//Colour gis objects
for (GIS_Building building : energyModel.pop_GIS_Buildings){
	f_setColorsBasedOnParkingType_objects(building);
}
for (GridNode GN : energyModel.pop_gridNodes){
	f_setColorsBasedOnParkingType_gridnodes(GN);
}
/*ALCODEEND*/}

double f_setColorsBasedOnParkingType_objects(GIS_Object gis_area)
{/*ALCODESTART::1754312755135*/
if (gis_area.c_containedGridConnections.size() > 0) {
	
	//Unkown by default
	Color objectColor = v_parkingSpaceColor_unkown;
	Color objectLineColor = v_parkingSpaceLineColor_unkown;
	
	//Check if houses and if public parking
	boolean containsHouses = false;
	boolean containsHousesWithPublicParking = false;
	for(GridConnection gc : gis_area.c_containedGridConnections){
		if(gc instanceof GCHouse){
			containsHouses = true;
			if(!((GCHouse)gc).p_eigenOprit){
				containsHousesWithPublicParking = true;
			}
		}
	}
	
	//Change color based on parking type if houses present
	if(containsHouses){
		if(containsHousesWithPublicParking){
			objectColor = v_parkingSpaceColor_public;
			objectLineColor = v_parkingSpaceLineColor_public;		
		}
		else{
			objectColor = v_parkingSpaceColor_private;
			objectLineColor = v_parkingSpaceLineColor_private;
		}
	}
	gis_area.f_style(objectColor, objectLineColor, null, null);
}
/*ALCODEEND*/}

double f_setColorsBasedOnParkingType_gridnodes(GridNode GN)
{/*ALCODESTART::1754314128315*/
if(GN.gisRegion != null){
	GN.gisRegion.setFillColor(v_parkingSpaceColor_unkown);
	GN.gisRegion.setLineColor(v_parkingSpaceLineColor_unkown);
}
/*ALCODEEND*/}

List<Integer> f_getNumberOfCarsPerCharger(int totalPublicParkedCars,int totalPublicChargers)
{/*ALCODESTART::1756122011053*/
List<Integer> numberOfCarsPerCharger = new ArrayList<>();
if(totalPublicChargers > 0){

	    
	int baseNumberOfCars = (int) floor(totalPublicParkedCars / totalPublicChargers); //Could also simply be totalPublicParkedCars / totalPublicChargers, as int/int is already floored, but just to make sure what should happen here it is written in full
	int remainingCars = totalPublicParkedCars % totalPublicChargers;  // extra vehicles that can be distributed
	
	for (int i = 0; i < totalPublicChargers; i++) {
	    if (i < remainingCars) {
	        numberOfCarsPerCharger.add(baseNumberOfCars + 1); // some chargers get one extra
	    } else {
	        numberOfCarsPerCharger.add(baseNumberOfCars);
	    }
	}
}

return numberOfCarsPerCharger;
/*ALCODEEND*/}

ShapeRadioButtonGroup f_getScenarioButtons()
{/*ALCODESTART::1756369604291*/
return rb_scenarios;
/*ALCODEEND*/}

double f_enableAllPlots(UI_Results resultsUI,I_EnergyData selectedObjectInterface)
{/*ALCODESTART::1756994047356*/
if (resultsUI.f_getSelectedObjectData() != null) {	
	uI_Results.f_updateResultsUI(selectedObjectInterface);
	uI_Results.f_enableNonLivePlotRadioButtons(true);
}
/*ALCODEEND*/}

double f_setAllSimulateYearScreens()
{/*ALCODESTART::1756995218301*/
gr_simulateYear.setVisible(true);
uI_EnergyHub.gr_simulateYearEnergyHub.setVisible(true);
for(UI_company companyUI : c_companyUIs){
	companyUI.gr_simulateYearScreen.setVisible(true);
}
/*ALCODEEND*/}

double f_removeAllSimulateYearScreens()
{/*ALCODESTART::1756997038652*/
gr_simulateYear.setVisible(false);
gr_loadIconYearSimulation.setVisible(false);
uI_EnergyHub.gr_simulateYearEnergyHub.setVisible(false);
uI_EnergyHub.gr_loadIconYearSimulationEnergyHub.setVisible(false);
for(UI_company companyUI : c_companyUIs){
	companyUI.gr_simulateYearScreen.setVisible(false);
	companyUI.gr_loadIcon.setVisible(false);
}
/*ALCODEEND*/}

double f_cancelEnergyHubConfiguration()
{/*ALCODESTART::1760014973975*/
button_clearFilters.action();

b_inEnergyHubMode = false;
b_inEnergyHubSelectionMode = false;

f_setForcedClickScreenText("");
f_setForcedClickScreenVisibility(false);

cb_showFilterInterface.setSelected(false, true);
/*ALCODEEND*/}

double f_filterHasEV(ArrayList<GridConnection> toBeFilteredGC)
{/*ALCODESTART::1760085891920*/
c_selectedGridConnections = new ArrayList<>(findAll(toBeFilteredGC, GC -> GC.c_electricVehicles.size() > 0));
//Werkt nog niet helemaal naar behoren, want ghost assets worden nog niet aangemaakt, 
//en dus hebben bedrijven met ghost ev geen c_electricVehicles en dus komen niet door deze filter.
// --> Als ghost vehicles ook worden aangemaakt, werkt het wel.
/*ALCODEEND*/}

