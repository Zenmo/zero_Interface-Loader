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
f_initializeUserSavedScenarios();

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
if (zero_Interface.p_selectedProjectType == OL_ProjectType.RESIDENTIAL) {
	((tabElectricity)uI_Tabs.pop_tabElectricity.get(0)).getGroupElectricityDemandSliders_ResidentialArea().setVisible(true);
	((tabHeating)uI_Tabs.pop_tabHeating.get(0)).getGroupHeatDemandSlidersResidentialArea().setVisible(true);
	((tabMobility)uI_Tabs.pop_tabMobility.get(0)).getGroupMobilityDemandSliders().setVisible(true);
}
else {
	((tabElectricity)uI_Tabs.pop_tabElectricity.get(0)).getGroupElectricityDemandSliders_Businesspark().setVisible(true);
	((tabHeating)uI_Tabs.pop_tabHeating.get(0)).getGroupHeatDemandSlidersCompanies().setVisible(true);
	((tabMobility)uI_Tabs.pop_tabMobility.get(0)).getGroupMobilityDemandSliders().setVisible(true);
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

double f_initializeUserSavedScenarios()
{/*ALCODESTART::1756395572049*/
String userIdToken = zero_Interface.user.userIdToken();

if(userIdToken != null){
	
}
/*ALCODEEND*/}

