double f_initializeEnergyHubDashboard()
{/*ALCODESTART::1753446461346*/
//Set map to correct layout
zero_Interface.rb_mapOverlay.setValue(zero_Interface.c_loadedMapOverlayTypes.indexOf(OL_MapOverlayTypes.DEFAULT),true);
zero_Interface.b_updateLiveCongestionColors = false;

for (GridConnection GC : v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer()) { //Buildings that are grouped, select as well.
	for (GIS_Object object : GC.c_connectedGISObjects) { //Buildings that are grouped, select as well.
		object.gisRegion.setFillColor(zero_Interface.v_selectionColorAddBuildings);
	}
}

// Sets the names of the members below the map (call before adding sliders)
f_initializeEnergyHubMemberNames();

//Add slider GC Needs to happen before EnergyCoop creation in some way!!!
f_addSliderEAGridConnections();

//Initialize the ui_results
f_initializeEnergyHubResultsUI();

//Initialize the ui_tabs
f_initializeEnergyHubTabs();

//Initialize custom user saved scenarios
f_initializeUserSavedScenarios(combobox_selectScenario);

runSimulation();
/*ALCODEEND*/}

double f_initializeEnergyHubResultsUI()
{/*ALCODESTART::1753694353046*/
//Initialize the ui_results
uI_Results.energyModel = zero_Interface.energyModel;
uI_Results.v_interfaceViewAreaXOffset = zero_Interface.va_EHubDashboard.getX();
uI_Results.v_interfaceViewAreaYOffset = zero_Interface.va_EHubDashboard.getY();

//Style resultsUI
f_styleEnergyHubResultsUI();

//Set ResultsUI radiobutton setup
if(zero_Interface.settings.resultsUIRadioButtonSetup() != null){
	uI_Results.v_selectedRadioButtonSetup = zero_Interface.settings.resultsUIRadioButtonSetup();
}

//Connect resultsUI
uI_Results.f_initializeResultsUI();
uI_Results.f_updateResultsUI(v_energyHubCoop);
/*ALCODEEND*/}

double f_initializeEnergyHubTabs()
{/*ALCODESTART::1753694406870*/
//Initialize the ui_tabs
uI_Tabs.add_pop_tabElectricity();
uI_Tabs.add_pop_tabHeating();
uI_Tabs.add_pop_tabMobility();

//Adjust location of buttons to account for missing e-hub tab
uI_Tabs.gr_energyDemandSettings.setX(zero_Interface.uI_Tabs.gr_energyDemandSettings.getX()+40);

// Group visibilities
// When using an extension of a generic tab don't forget to typecast it!
if (zero_Interface.project_data.project_type() == OL_ProjectType.RESIDENTIAL) {
	((tabElectricity)uI_Tabs.pop_tabElectricity.get(0)).getGroupElectricityDemandSliders_ResidentialArea().setVisible(true);
	((tabHeating)uI_Tabs.pop_tabHeating.get(0)).getGroupHeatDemandSlidersResidentialArea().setVisible(true);
	((tabMobility)uI_Tabs.pop_tabMobility.get(0)).getGr_mobilitySliders_residential().setVisible(true);
}
else {
	((tabElectricity)uI_Tabs.pop_tabElectricity.get(0)).getGroupElectricityDemandSliders_Businesspark().setVisible(true);
	((tabHeating)uI_Tabs.pop_tabHeating.get(0)).getGroupHeatDemandSlidersCompanies().setVisible(true);
	((tabMobility)uI_Tabs.pop_tabMobility.get(0)).getGr_mobilitySliders_default().setVisible(true);
}

//Initialize slider gcs and set sliders
uI_Tabs.f_initializeUI_Tabs(v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer(), null);

uI_Tabs.v_presentationXOffset += zero_Interface.va_EHubDashboard.getX();
uI_Tabs.v_presentationYOffset += zero_Interface.va_EHubDashboard.getY();
/*ALCODEEND*/}

double f_styleEnergyHubResultsUI()
{/*ALCODESTART::1753694556229*/
uI_Results.f_styleAllCharts(white, p_energyHubLineColor, p_energyHubLineWidth, p_energyHubLineStyle);
uI_Results.f_styleResultsUIHeader(p_energyHubLineColor, p_energyHubLineColor, p_energyHubLineWidth, p_energyHubLineStyle);
/*ALCODEEND*/}

double f_addSliderEAGridConnections()
{/*ALCODESTART::1755014317038*/
//Check if a slider EA GC has been manually selected
GridConnection manualSelectedSliderGC_solarFarm = null;
GridConnection manualSelectedSliderGC_windFarm = null;
GridConnection manualSelectedSliderGC_gridBattery = null;
for (GridConnection GC : v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer()) {
	if(GC instanceof GCEnergyProduction){
	    if (((GCEnergyProduction)GC).p_isSliderGC) {
	        if(manualSelectedSliderGC_solarFarm == null && GC.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.pvProductionElectric_kW)){
				manualSelectedSliderGC_solarFarm = GC;
			}
			else if(manualSelectedSliderGC_windFarm == null && GC.v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.windProductionElectric_kW)){
				manualSelectedSliderGC_windFarm = GC;
			}
	    }
	}
	if(manualSelectedSliderGC_gridBattery == null && GC instanceof GCGridBattery && ((GCGridBattery)GC).p_isSliderGC && GC.p_batteryAsset != null){
		manualSelectedSliderGC_gridBattery = GC;
	}
}

//If a slider EA GC has not been manually selected:
GridConnection sliderGC_solarFarm = null;
GridConnection sliderGC_windFarm = null;
GridConnection sliderGC_gridBattery = null;
if(manualSelectedSliderGC_solarFarm == null){
	sliderGC_solarFarm = findFirst(zero_Interface.energyModel.EnergyProductionSites, gc -> gc.p_gridConnectionID.equals("EnergyHub solarfarm slider"));
}
if(manualSelectedSliderGC_windFarm == null){
	sliderGC_windFarm = findFirst(zero_Interface.energyModel.EnergyProductionSites, gc -> gc.p_gridConnectionID.equals("EnergyHub windfarm slider"));
}
if(manualSelectedSliderGC_gridBattery == null){
	sliderGC_gridBattery = findFirst(zero_Interface.energyModel.GridBatteries, gc -> gc.p_gridConnectionID.equals("EnergyHub battery slider"));
}

//Add to coop and other collections
if(sliderGC_solarFarm != null){
	c_sliderEAGCs.add(sliderGC_solarFarm);
}
if(sliderGC_windFarm != null){
	c_sliderEAGCs.add(sliderGC_windFarm);
}
if(sliderGC_gridBattery != null){
	c_sliderEAGCs.add(sliderGC_gridBattery);
}

v_energyHubCoop.f_addMemberGCs(c_sliderEAGCs);
/*ALCODEEND*/}

double[] f_calculateGroupATOConnectionCapacity(ArrayList<GridConnection> gcList)
{/*ALCODESTART::1756124281754*/
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
{/*ALCODESTART::1756124281759*/
v_clickedObjectText = "None";
uI_Results.b_showGroupContractValues = false;
uI_Tabs.pop_tabEHub.get(0).cb_EHubSelect.setSelected(false);
uI_Tabs.pop_tabEHub.get(0).t_baseGroepInfo.setText("Selecteer minimaal twee panden");
uI_Tabs.pop_tabEHub.get(0).t_groepsGTV_kW.setText("");
uI_Tabs.pop_tabEHub.get(0).t_cumulatiefGTV_kW.setText("");
uI_Tabs.pop_tabEHub.get(0).t_warnings.setText("");
/*ALCODEEND*/}

double f_EHubTabCapacityInformation(boolean reset,String textToAdd)
{/*ALCODESTART::1756124281764*/
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
{/*ALCODESTART::1756124281769*/
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

double f_initializeEnergyHubMemberNames()
{/*ALCODESTART::1756302765458*/
t_energyHubMember1.setVisible(false);
t_energyHubMember2.setVisible(false);
t_energyHubMember3.setVisible(false);
t_energyHubMember4.setVisible(false);
t_energyHubMember5.setVisible(false);
t_energyHubMember6.setVisible(false);
t_energyHubMember7.setVisible(false);
t_energyHubMemberOthers.setVisible(false);

int maxChars = 20;
String name = "";

try {
	List<GridConnection> members = v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer();
	name = members.get(0).p_ownerID;
	t_energyHubMember1.setText(name.substring(0, min(maxChars, name.length()) ));
	t_energyHubMember1.setVisible(true);
	name = members.get(1).p_ownerID;
	t_energyHubMember2.setText(name.substring(0, min(maxChars, name.length()) ));
	t_energyHubMember2.setVisible(true);
	name = members.get(2).p_ownerID;
	t_energyHubMember3.setText(name.substring(0, min(maxChars, name.length()) ));
	t_energyHubMember3.setVisible(true);
	name = members.get(3).p_ownerID;
	t_energyHubMember4.setText(name.substring(0, min(maxChars, name.length()) ));
	t_energyHubMember4.setVisible(true);
	name = members.get(4).p_ownerID;
	t_energyHubMember5.setText(name.substring(0, min(maxChars, name.length()) ));
	t_energyHubMember5.setVisible(true);
	name = members.get(5).p_ownerID;
	t_energyHubMember6.setText(name.substring(0, min(maxChars, name.length()) ));
	t_energyHubMember6.setVisible(true);
	name = members.get(6).p_ownerID;
	t_energyHubMember7.setText(name.substring(0, min(maxChars, name.length()) ));
	t_energyHubMember7.setVisible(true);
	if (members.size() == 8) {
		name = members.get(7).p_ownerID;		
		t_energyHubMemberOthers.setText(name.substring(0, min(maxChars, name.length()) ));
		t_energyHubMemberOthers.setVisible(true);
	}
	else if (members.size() > 8) {
		int nbOthers = members.size() - 7;
		t_energyHubMemberOthers.setText("En nog " + nbOthers + " andere leden");
		t_energyHubMemberOthers.setVisible(true);
	}
}
catch (Exception exception) {
	if (exception instanceof IndexOutOfBoundsException) {
		traceln("Ignoring IndexOutOfBoundsException"); // do nothing
	}
	else {
		exception.printStackTrace();
	}
}
/*ALCODEEND*/}

double f_initializeUserSavedScenarios(ShapeComboBox combo)
{/*ALCODESTART::1756395572049*/
if ( zero_Interface.user.userIdToken() == null || zero_Interface.user.userIdToken() == "") {
	return;
}

var repository = UserScenarioRepository.builder()
    .userId(UUID.fromString(zero_Interface.user.userIdToken()))
    .modelName(zero_Interface.project_data.project_name())
    .build();

var scenarioList = repository.listUserScenarios();
int nbScenarios = scenarioList.size();
String[] scenarioNames = new String[nbScenarios];
for (int i = 0; i < nbScenarios; i++) {
	scenarioNames[i] = scenarioList.get(i).getName();
}

combo.setItems(scenarioNames);
/*ALCODEEND*/}

double f_loadScenario(int index)
{/*ALCODESTART::1756805429105*/
if ( zero_Interface.user.userIdToken() == null || zero_Interface.user.userIdToken() == "") {
	zero_Interface.f_setErrorScreen("Niet mogelijk om scenario's in te laden. Er is geen gebruiker ingelogd.", zero_Interface.va_EHubDashboard.getX(), zero_Interface.va_EHubDashboard.getY());
	return;
}
traceln("Loading modelSave...");
//pauseSimulation();

// Collect GIS_Objects into hashmap, to link to new EnergyModel.
zero_Interface.energyModel.pop_GIS_Buildings.forEach(x->{c_GISregions.put(x.p_id, x.gisRegion);});
zero_Interface.energyModel.pop_GIS_Objects.forEach(x->{c_GISregions.put(x.p_id, x.gisRegion);});
zero_Interface.energyModel.pop_GIS_Parcels.forEach(x->{c_GISregions.put(x.p_id, x.gisRegion);});
pauseSimulation();
try {
	v_objectMapper = new ObjectMapper();
	f_addMixins();
	
	var repository = UserScenarioRepository.builder()
	    .userId(UUID.fromString(zero_Interface.user.userIdToken()))
	    .modelName(zero_Interface.project_data.project_name())
        .build();
      
    var scenarioList = repository.listUserScenarios();   

	// Deserialize the JSON into a new EnergyModel instance:
    var jsonStream = repository.fetchUserScenarioContent(scenarioList.get(index).getId());
    
 	/*int n = 200; // how many characters you want
    byte[] buffer = new byte[n];
    int read = jsonStream.read(buffer, 0, n);

    if (read > 0) {
        String preview = new String(buffer, 0, read, "UTF-8");
        System.out.println(preview);
    }*/
	
    //jsonStream.close();
    
    //traceln("jsonStream: %s", jsonStream.toString().substring(0,30));
	J_ModelSave saveObject = v_objectMapper.readValue(jsonStream, J_ModelSave.class);
	//J_ModelSave saveObject = v_objectMapper.readValue(new File("ModelSave.json"), J_ModelSave.class);
	
	EnergyModel deserializedEnergyModel = saveObject.energyModel;
		
	// Reconstruct all Agents
	f_reconstructEnergyModel(deserializedEnergyModel);		
	f_reconstructGridConnections(deserializedEnergyModel);
	f_reconstructActors(deserializedEnergyModel);
	f_reconstructGridNodes(deserializedEnergyModel, saveObject.c_gridNodes);
	
	f_reconstructGIS_Objects(deserializedEnergyModel, saveObject.c_GISObjects);
	
	// Get profilePointer tableFunctions from 'original' energyModel
	deserializedEnergyModel.c_profiles.forEach(x->{
		J_ProfilePointer origProfile = zero_Interface.energyModel.f_findProfile(x.name);
		x.setTableFunction(origProfile.getTableFunction());
	});
	
	zero_Interface.zero_loader.energyModel = deserializedEnergyModel;
	zero_Interface.energyModel = deserializedEnergyModel;
	zero_Interface.uI_Results.energyModel = deserializedEnergyModel;
	uI_Results.energyModel = deserializedEnergyModel;
	
	deserializedEnergyModel.f_startAfterDeserialisation();
	
	f_setEngineInputDataAfterDeserialisation(deserializedEnergyModel);
	

	//Date startDate = getExperiment().getEngine().getStartDate();
	//traceln("startDate# " + startDate);
	//Date currentDate = date();
	//traceln("currentDate# " + currentDate);
	//startDate.setYear(startDate.getYear() - currentDate.getYear());
	//startDate.setMonth(startDate.getMonth() - currentDate.getMonth());
	//startDate.setDate(startDate.getDate() - currentDate.getDate());
	//getExperiment().getEngine().setStartDate(startDate);
	//traceln("Reduced anylogic date by one year, looping all data");
	
	// Reinitialize energy model
	
	//deserializedEnergyModel.f_initializeEngineAfterLoad();	
		
	// Putting back the ordered collections in the interface
	f_reconstructOrderedCollections(saveObject);
	
	//zero_Interface.f_clearSelectionAndSelectEnergyModel();
	
	/*
	zero_Interface.uI_Tabs.f_initializeUI_Tabs(zero_Interface.energyModel.f_getGridConnectionsCollectionPointer(), zero_Interface.energyModel.f_getPausedGridConnectionsCollectionPointer());
	uI_Tabs.f_initializeUI_Tabs(v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer(), null);
	zero_Interface.f_updateMainInterfaceSliders();
	
	zero_Interface.f_resetSettings();
	*/
	
	///button_exit.action();
	
	///zero_Interface.uI_Tabs.f_initializeUI_Tabs(zero_Interface.energyModel.f_getGridConnectionsCollectionPointer(), zero_Interface.energyModel.f_getPausedGridConnectionsCollectionPointer());
	// v_energyHubCoop not updated to point to 'new' coop
	//uI_Tabs.f_initializeUI_Tabs(v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer(), null);
	
	///zero_Interface.f_simulateYearFromMainInterface();
	
	v_energyHubCoop = findFirst(zero_Interface.energyModel.pop_energyCoops,x->x.p_actorID.equals("eHubConfiguratorCoop"));
	if (v_energyHubCoop == null){
		throw new RuntimeException("No energyCoop found with p_actorID = eHubConfiguratorCoop");
	}
	zero_Interface.v_customEnergyCoop = v_energyHubCoop;
	// Update the E-Hub Dashboard with the loaded E-Hub from savefile
	f_initializeEnergyHubMemberNames();
	uI_Tabs.f_initializeUI_Tabs(v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer(), null);
	uI_Results.f_updateResultsUI(v_energyHubCoop);
	
	// Update the main interface with the loaded E-Hub from savefile
	zero_Interface.c_selectedGridConnections = new ArrayList<>(v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer());
	
	// Reset all colors on the GIS map
	zero_Interface.energyModel.pop_GIS_Buildings.forEach(x -> zero_Interface.f_styleAreas(x));
	zero_Interface.energyModel.pop_GIS_Objects.forEach(x -> zero_Interface.f_styleAreas(x));
	zero_Interface.energyModel.pop_GIS_Parcels.forEach(x -> zero_Interface.f_styleAreas(x));
	
	// Color all selected GC 
	for (GridConnection gc : zero_Interface.c_selectedGridConnections) {
		gc.c_connectedGISObjects.forEach(x -> x.gisRegion.setFillColor(zero_Interface.v_selectionColor));		
	}
	
	// Simulate a year
	gr_simulateYearEnergyHub.setVisible(false);		
	gr_loadIconYearSimulationEnergyHub.setVisible(true);
	
	
	zero_Interface.f_simulateYearFromMainInterface();	
	
	traceln("ModelSave loaded succesfully!");
	
	//zero_Interface.b_inEnergyHubSelectionMode = true;
	//zero_Interface.f_finalizeEnergyHubConfiguration();
	
	//zero_Interface.f_updateOrderedListsAfterDeserialising(deserializedEnergyModel);
	
	/*
	Date startDate = getExperiment().getEngine().getStartDate();
	int day = getExperiment().getEngine().getDayOfMonth();
	int month = getExperiment().getEngine().getMonth();
	traceln("day: " + day);
	traceln("month: " + month);
	startDate.setMonth(startDate.getMonth() - month);
	startDate.setDate(startDate.getDate() - day);
	getExperiment().getEngine().setStartDate(startDate);
	*/
	
} catch (IOException e) {
	e.printStackTrace();
}


/*ALCODEEND*/}

double f_saveScenario(String scenarioName)
{/*ALCODESTART::1756805443177*/
if ( zero_Interface.user.userIdToken() == null || zero_Interface.user.userIdToken() == "") {
	zero_Interface.f_setErrorScreen("Niet mogelijk om scenario's op te slaan. Er is geen gebruiker ingelogd.", zero_Interface.va_EHubDashboard.getX(), zero_Interface.va_EHubDashboard.getY());
	return;
}

traceln("Starting model serialisation...");
J_ModelSave saveObject = new J_ModelSave();
saveObject.energyModel = zero_Interface.energyModel;

zero_Interface.energyModel.pop_gridNodes.forEach(x->saveObject.c_gridNodes.add(x));
zero_Interface.energyModel.pop_GIS_Buildings.forEach(x->{saveObject.c_GISObjects.add(x); c_GISregions.put(x.p_id, x.gisRegion);x.f_writeStyleStrings();});
zero_Interface.energyModel.pop_GIS_Objects.forEach(x->{saveObject.c_GISObjects.add(x); c_GISregions.put(x.p_id, x.gisRegion);x.f_writeStyleStrings();});
zero_Interface.energyModel.pop_GIS_Parcels.forEach(x->{saveObject.c_GISObjects.add(x); c_GISregions.put(x.p_id, x.gisRegion);x.f_writeStyleStrings();});

saveObject.c_orderedPVSystemsCompanies = zero_Interface.c_orderedPVSystemsCompanies;
saveObject.c_orderedPVSystemsHouses = zero_Interface.c_orderedPVSystemsHouses;
saveObject.c_orderedVehicles = zero_Interface.c_orderedVehicles;
saveObject.c_orderedHeatingSystemsCompanies = zero_Interface.c_orderedHeatingSystemsCompanies;
saveObject.c_orderedHeatingSystemsHouses = zero_Interface.c_orderedHeatingSystemsHouses;
saveObject.c_orderedVehiclesPrivateParking = zero_Interface.c_orderedVehiclesPrivateParking;
saveObject.c_orderedParkingSpaces = zero_Interface.c_orderedParkingSpaces;
saveObject.c_orderedV1GChargers = zero_Interface.c_orderedV1GChargers;
saveObject.c_orderedV2GChargers = zero_Interface.c_orderedV2GChargers;
saveObject.c_orderedPublicChargers = zero_Interface.c_orderedPublicChargers;
saveObject.c_mappingOfVehiclesPerCharger = zero_Interface.c_mappingOfVehiclesPerCharger;
saveObject.c_scenarioMap_Current = zero_Interface.c_scenarioMap_Current;
saveObject.c_scenarioMap_Future = zero_Interface.c_scenarioMap_Future;

List<LinkedHashMap<String, List<J_EAVehicle>>> c_additionalVehicleHashMaps = new ArrayList<LinkedHashMap<String, List<J_EAVehicle>>>();
for (UI_company companyUI : zero_Interface.c_companyUIs) {
	c_additionalVehicleHashMaps.add(companyUI.c_additionalVehicles);
}
saveObject.c_additionalVehicleHashMaps = c_additionalVehicleHashMaps;

v_objectMapper = new ObjectMapper();
f_addMixins();
v_objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);

try {
	traceln("Trying to save to file");
	//v_objectMapper.writeValue(new File("energyModel.json"), energyModel);

	//v_objectMapper.writeValue(new File("ModelSave.json"), saveObject);

	var repository = UserScenarioRepository.builder()
	    .userId(UUID.fromString(zero_Interface.user.userIdToken()))
	    .modelName(zero_Interface.project_data.project_name())
        .build();
    
	repository.saveUserScenario(
        scenarioName,
        v_objectMapper.writeValueAsBytes(saveObject)
	);
	
	
} catch (IOException e) {
	e.printStackTrace();
}

/*ALCODEEND*/}

double f_addMixins()
{/*ALCODESTART::1756806431955*/
v_objectMapper.addMixIn(Agent.class, AgentMixin.class);
//v_objectMapper.addMixIn(EnergyModel.class, EnergyModelMixin.class);

//v_objectMapper.addMixIn(Actor.class, ActorMixin.class);
//v_objectMapper.addMixIn(DataSet.class, DataSetMixin.class);

//Ignore classes

v_objectMapper.addMixIn(java.awt.Font.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(java.awt.Color.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(java.awt.Paint.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(ShapeLine.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(ShapeLineFill.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(ShapeText.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(AgentArrayList.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(ViewArea.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(TextFile.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(EnergyDataViewer.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(DataSet.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(Level.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(TableFunction.class, IgnoreClassMixin.class);
//objectMapper.addMixIn(com.anylogic.engine.TableFunction.class, TableFunctionMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.GISRegion.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.presentation.ViewArea.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.AgentSpacePosition.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.presentation.ShapeModelElementsGroup.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.presentation.ShapeButton.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.analysis.TimePlot.class, IgnoreClassMixin.class);


// Weirdness regarding material handling toolbox	
v_objectMapper.addMixIn(com.anylogic.engine.markup.AbstractWall.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.RailwayTrack.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.PalletRack.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.RoadNetwork.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.AreaNode.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.AbstractFluidMarkup.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.Lift.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.ConveyorNode.class, IgnoreClassMixin.class);
v_objectMapper.addMixIn(com.anylogic.engine.markup.Node.class, IgnoreClassMixin.class);

/*ALCODEEND*/}

double f_setEngineInputDataAfterDeserialisation(EnergyModel deserializedEnergyModel)
{/*ALCODESTART::1756806501010*/
deserializedEnergyModel.p_truckTripsCsv = zero_Interface.energyModel.p_truckTripsCsv;
deserializedEnergyModel.p_householdTripsCsv = zero_Interface.energyModel.p_householdTripsCsv;
deserializedEnergyModel.p_cookingPatternCsv = zero_Interface.energyModel.p_cookingPatternCsv;

//deserializedEnergyModel.avgc_data = zero_Interface.energyModel.avgc_data;
deserializedEnergyModel.c_defaultHeatingStrategies = zero_Interface.energyModel.c_defaultHeatingStrategies;
/*ALCODEEND*/}

double f_reconstructGridConnections(EnergyModel deserializedEnergyModel)
{/*ALCODESTART::1756806501019*/
ArrayList<GridConnection> allConnections = new ArrayList<>();
allConnections.addAll(deserializedEnergyModel.c_gridConnections);
allConnections.addAll(deserializedEnergyModel.c_pausedGridConnections);

for(GridConnection GC : allConnections){
	GC.energyModel = deserializedEnergyModel;
	if (GC instanceof GCHouse){
		//toMove.add(GC);
		f_reconstructAgent(GC, deserializedEnergyModel.Houses, deserializedEnergyModel);
	} else if (GC instanceof GCEnergyProduction) {
		f_reconstructAgent(GC, deserializedEnergyModel.EnergyProductionSites, deserializedEnergyModel);
	} else if (GC instanceof GCEnergyConversion) {
		f_reconstructAgent(GC, deserializedEnergyModel.EnergyConversionSites, deserializedEnergyModel);
	} else if (GC instanceof GCGridBattery) {
		f_reconstructAgent(GC, deserializedEnergyModel.GridBatteries, deserializedEnergyModel);
	} else if (GC instanceof GCNeighborhood) {
		f_reconstructAgent(GC, deserializedEnergyModel.Neighborhoods, deserializedEnergyModel);
	} else if (GC instanceof GCPublicCharger) {
		f_reconstructAgent(GC, deserializedEnergyModel.PublicChargers, deserializedEnergyModel);
	} else if (GC instanceof GCUtility) {
		f_reconstructAgent(GC, deserializedEnergyModel.UtilityConnections, deserializedEnergyModel);
	}
	//GC.f_startAfterDeserialisation();
}


/*ALCODEEND*/}

double f_reconstructEnergyModel(EnergyModel energyModel)
{/*ALCODESTART::1756806501029*/
// Code Instead of Agent.goToPopulation() (which resets all parameters to default!)	
/*
try{ // Reflection trick to get to Agent.owner private field
	energyModel.forceSetOwner(energyModel, pop_energyModels);
} catch (Exception e) {
	e.printStackTrace();
}
*/

Agent root = zero_Interface.getRootAgent();
traceln("root: " + root);
energyModel.restoreOwner(zero_Interface.zero_loader);

energyModel.setEngine(getEngine());	
energyModel.instantiateBaseStructure_xjal();
energyModel.setEnvironment(zero_Interface.zero_loader.getEnvironment());

traceln("EnergyModel owner: %s", energyModel.getOwner());

energyModel.create();
//energyModel.start(); // Why is this needed?
/*ALCODEEND*/}

double f_reconstructAgent(Agent agent,AgentArrayList pop,EnergyModel energyModel)
{/*ALCODESTART::1756806501040*/
/* // Code Instead of Agent.goToPopulation() (which resets many variables to default!)	
try{ // Reflection trick to get to Agent.owner private field
	if (agent instanceof GridNode) {
		((GridNode)agent).forceSetOwner(agent,pop);
	} else if (agent instanceof GridConnection) {
		((GridConnection)agent).forceSetOwner(agent,pop);
	} else if (agent instanceof Actor) {
		((Actor)agent).forceSetOwner(agent,pop);
	} else if (agent instanceof GIS_Object) {
		((GIS_Object)agent).forceSetOwner(agent,pop);
	}
} catch (Exception e) {
	e.printStackTrace();
}*/ 

agent.restoreOwner(energyModel); // simply sets agent.d = root, should replace reflection hack
agent.restoreCollection_xjal(pop); // simple sets agent.j = pop, should replace reflection hack

agent.setEngine(getEngine());	
agent.instantiateBaseStructure_xjal();
agent.setEnvironment(pop.getEnvironment());

pop._add(agent); // Add to the population	
//int popSize = pop.size(); 
//pop.callCreate(agent, popSize); // Update population object
agent.create();
/*ALCODEEND*/}

double f_reconstructActors(EnergyModel deserializedEnergyModel)
{/*ALCODESTART::1756806501050*/
for(Actor AC : deserializedEnergyModel.c_actors){
		
		if (AC instanceof ConnectionOwner) {
			((ConnectionOwner)AC).energyModel = deserializedEnergyModel;
			f_reconstructAgent(AC, deserializedEnergyModel.pop_connectionOwners, deserializedEnergyModel);
		} else if (AC instanceof EnergySupplier) {
			((EnergySupplier)AC).energyModel = deserializedEnergyModel;
			f_reconstructAgent(AC, deserializedEnergyModel.pop_energySuppliers, deserializedEnergyModel);
		} else if (AC instanceof EnergyCoop) {
			((EnergyCoop)AC).energyModel = deserializedEnergyModel;
			f_reconstructAgent(AC, deserializedEnergyModel.pop_energyCoops, deserializedEnergyModel);
			//((EnergyCoop)AC).f_startAfterDeserialisation();
		} else if (AC instanceof GridOperator) {
			((GridOperator)AC).energyModel = deserializedEnergyModel;
			f_reconstructAgent(AC, deserializedEnergyModel.pop_gridOperators, deserializedEnergyModel);
		}
	}

/*ALCODEEND*/}

double f_reconstructGIS_Objects(EnergyModel deserializedEnergyModel,ArrayList<GIS_Object> c_GISObjects)
{/*ALCODESTART::1756806501060*/
for(GIS_Object GO : c_GISObjects){
	GO.gisRegion = c_GISregions.get(GO.p_id);
	
	if (GO instanceof GIS_Building) {
		((GIS_Building)GO).energyModel = deserializedEnergyModel;
		f_reconstructAgent(GO, deserializedEnergyModel.pop_GIS_Buildings, deserializedEnergyModel);
	} else if (GO instanceof GIS_Parcel) {
		((GIS_Parcel)GO).energyModel = deserializedEnergyModel;
		f_reconstructAgent(GO, deserializedEnergyModel.pop_GIS_Parcels, deserializedEnergyModel);
	} else {
		GO.energyModel = deserializedEnergyModel;
		f_reconstructAgent(GO, deserializedEnergyModel.pop_GIS_Objects, deserializedEnergyModel);
		//GO.f_startAfterDeserialisation();
	}
	GO.f_resetStyle();
}
/*ALCODEEND*/}

double f_reconstructGridNodes(EnergyModel deserializedEnergyModel,ArrayList<GridNode> c_gridNodes)
{/*ALCODESTART::1756806501070*/
for(GridNode GN : c_gridNodes){
	GN.energyModel = deserializedEnergyModel;
	f_reconstructAgent(GN, deserializedEnergyModel.pop_gridNodes, deserializedEnergyModel);
}

/*ALCODEEND*/}

double f_reconstructOrderedCollections(J_ModelSave saveObject)
{/*ALCODESTART::1756806501080*/
zero_Interface.c_orderedPVSystemsCompanies = saveObject.c_orderedPVSystemsCompanies;
zero_Interface.c_orderedPVSystemsHouses = saveObject.c_orderedPVSystemsHouses;
zero_Interface.c_orderedVehicles = saveObject.c_orderedVehicles;
zero_Interface.c_orderedHeatingSystemsCompanies = saveObject.c_orderedHeatingSystemsCompanies;
zero_Interface.c_orderedHeatingSystemsHouses = saveObject.c_orderedHeatingSystemsHouses;
zero_Interface.c_orderedVehiclesPrivateParking = saveObject.c_orderedVehiclesPrivateParking;
zero_Interface.c_orderedParkingSpaces = saveObject.c_orderedParkingSpaces;
zero_Interface.c_orderedV1GChargers = saveObject.c_orderedV1GChargers;
zero_Interface.c_orderedV2GChargers = saveObject.c_orderedV2GChargers;
zero_Interface.c_orderedPublicChargers = saveObject.c_orderedPublicChargers;
zero_Interface.c_mappingOfVehiclesPerCharger = saveObject.c_mappingOfVehiclesPerCharger;
zero_Interface.c_scenarioMap_Current = saveObject.c_scenarioMap_Current;
zero_Interface.c_scenarioMap_Future = saveObject.c_scenarioMap_Future;

List<ConnectionOwner> c_COCompanies = findAll(zero_Interface.energyModel.pop_connectionOwners, p -> p.p_connectionOwnerType == OL_ConnectionOwnerType.COMPANY); 

int i = 0;
for (ConnectionOwner CO : c_COCompanies) {
	UI_company companyUI = zero_Interface.c_companyUIs.get(CO.p_connectionOwnerIndexNr);
	companyUI.p_company = CO;
	companyUI.c_ownedGridConnections = companyUI.p_company.f_getOwnedGridConnections();
	companyUI.c_additionalVehicles = saveObject.c_additionalVehicleHashMaps.get(i);
	companyUI.f_setSelectedGCSliders();
	i++;
}

/*ALCODEEND*/}

