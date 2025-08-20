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

//Add slider GC Needs to happen before EnergyCoop creation in some way!!!
//f_addSliderEAGridConnections();

//Initialize the ui_results
f_initializeEnergyHubResultsUI();

//Initialize the ui_tabs
f_initializeEnergyHubTabs();

runSimulation();
/*ALCODEEND*/}

double f_initializeEnergyHubResultsUI()
{/*ALCODESTART::1753694353046*/
//Initialize the ui_results
uI_Results.energyModel = zero_Interface.energyModel;

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
uI_Tabs.gr_energyDemandSettings.setX(uI_Tabs.gr_energyDemandSettings.getX()+40);

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


/*ALCODEEND*/}

double f_styleEnergyHubResultsUI()
{/*ALCODESTART::1753694556229*/
uI_Results.f_styleAllCharts(white, p_energyHubLineColor, p_energyHubLineWidth, p_energyHubLineStyle);
uI_Results.f_styleResultsUIHeader(p_energyHubLineColor, p_energyHubLineColor, p_energyHubLineWidth, p_energyHubLineStyle);
/*ALCODEEND*/}

double f_addSliderEAGridConnections()
{/*ALCODESTART::1755014317038*/
GCEnergyProduction sliderGC_solarFarm = null;
GCEnergyProduction sliderGC_windFarm = null;
for (GCEnergyProduction GC : zero_Interface.energyModel.EnergyProductionSites) {
    if (GC.p_isSliderGC) {
        if(sliderGC_solarFarm != null && GC.c_productionAssets.stream().anyMatch(asset -> asset.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC)){
			sliderGC_solarFarm = GC;
		}
		else if(sliderGC_windFarm != null && GC.c_productionAssets.stream().anyMatch(asset -> asset.energyAssetType == OL_EnergyAssetType.WINDMILL)){
			sliderGC_windFarm = GC;
		}
    }
}

GCGridBattery sliderGC_gridBattery = findFirst(zero_Interface.energyModel.GridBatteries, battery -> battery.p_isSliderGC && battery.p_batteryAsset != null);

//Add to coop and other collections
if(sliderGC_solarFarm != null){
	v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer().add(sliderGC_solarFarm);
	c_selectedEnergyHubGC.add(sliderGC_gridBattery);
}
if(sliderGC_windFarm != null){
	v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer().add(sliderGC_windFarm);
	c_selectedEnergyHubGC.add(sliderGC_gridBattery);
}
if(sliderGC_gridBattery != null){
	v_energyHubCoop.f_getMemberGridConnectionsCollectionPointer().add(sliderGC_gridBattery);
	c_selectedEnergyHubGC.add(sliderGC_gridBattery);
}
/*ALCODEEND*/}

